package com.example.parkingfinder.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.parkingfinder.database.AppDatabase;
import com.example.parkingfinder.database.dao.UserDao;
import com.example.parkingfinder.database.entities.UserEntity;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.User;
import com.example.parkingfinder.utils.Constants;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {

    private static final String TAG = "UserViewModel";

    // Firebase managers
    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;
    private StorageReference storageReference;

    // Database access
    private UserDao userDao;
    private ExecutorService databaseExecutor;

    // LiveData
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> profileUpdateSuccess = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);

        // Initialize Firebase managers
        authManager = FirebaseAuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference(Constants.Firebase.STORAGE_PROFILE_IMAGES);

        // Initialize database access
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        // Check if user is logged in
        isLoggedIn.setValue(authManager.isUserLoggedIn());

        // Load user data if logged in
        if (authManager.isUserLoggedIn()) {
            loadCurrentUser();
        }
    }

    // Getters for LiveData
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public LiveData<Boolean> getProfileUpdateSuccess() {
        return profileUpdateSuccess;
    }

    /**
     * Register a new user
     */
    public void registerUser(String email, String password, String name, String phone) {
        isLoading.setValue(true);

        authManager.registerUser(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Create user profile in Firestore
                User user = new User();
                user.setUid(firebaseUser.getUid());
                user.setName(name);
                user.setEmail(email);
                user.setPhoneNumber(phone);
                user.setProfileImageUrl("");

                firestoreManager.createUserProfile(user, new FirestoreManager.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        isLoading.setValue(false);
                        isLoggedIn.setValue(true);
                        currentUser.setValue(user);

                        // Save to local database
                        saveUserToLocalDatabase(user);
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Error creating profile: " + errorMsg);
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Registration failed: " + errorMsg);
            }
        });
    }

    /**
     * Login existing user
     */
    public void loginUser(String email, String password) {
        isLoading.setValue(true);

        authManager.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                isLoading.setValue(false);
                isLoggedIn.setValue(true);
                loadCurrentUser();
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Login failed: " + errorMsg);
            }
        });
    }

    /**
     * Logout current user
     */
    public void logoutUser() {
        authManager.logoutUser();
        isLoggedIn.setValue(false);
        currentUser.setValue(null);
    }

    /**
     * Load current user profile
     */
    public void loadCurrentUser() {
        if (!authManager.isUserLoggedIn()) {
            isLoggedIn.setValue(false);
            return;
        }

        isLoading.setValue(true);
        FirebaseUser firebaseUser = authManager.getCurrentUser();

        // First try to get from local database
        loadUserFromLocalDatabase(firebaseUser.getUid());

        // Then fetch from Firestore to ensure we have the latest data
        firestoreManager.getUserProfile(firebaseUser.getUid(), new FirestoreManager.GetUserCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.setValue(false);
                currentUser.setValue(user);

                // Save to local database
                saveUserToLocalDatabase(user);
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Error loading profile: " + errorMsg);
            }
        });
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(String name, String phone, Uri imageUri) {
        if (!authManager.isUserLoggedIn()) {
            errorMessage.setValue("You must be logged in to update your profile");
            return;
        }

        isLoading.setValue(true);
        User user = currentUser.getValue();

        if (user == null) {
            user = new User();
            user.setUid(authManager.getCurrentUser().getUid());
            user.setEmail(authManager.getCurrentUser().getEmail());
        }

        user.setName(name);
        user.setPhoneNumber(phone);

        // If image is selected, upload it first
        if (imageUri != null) {
            uploadImageAndSaveProfile(user, imageUri);
        } else {
            saveUserProfile(user);
        }
    }

    /**
     * Upload profile image to Firebase Storage
     */
    private void uploadImageAndSaveProfile(User user, Uri imageUri) {
        String userId = user.getUid();
        StorageReference fileReference = storageReference.child(userId + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        user.setProfileImageUrl(imageUrl);
                        saveUserProfile(user);
                    });
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to upload image: " + e.getMessage());
                });
    }

    /**
     * Save user profile to Firestore
     */
    private void saveUserProfile(User user) {
        firestoreManager.updateUserProfile(user, new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                currentUser.setValue(user);
                profileUpdateSuccess.setValue(true);

                // Save to local database
                saveUserToLocalDatabase(user);
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Error updating profile: " + errorMsg);
                profileUpdateSuccess.setValue(false);
            }
        });
    }

    /**
     * Change user password
     */
    public void changePassword(String currentPassword, String newPassword) {
        if (!authManager.isUserLoggedIn()) {
            errorMessage.setValue("You must be logged in to change your password");
            return;
        }

        isLoading.setValue(true);

        authManager.reauthenticate(currentPassword, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                authManager.updatePassword(newPassword, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        isLoading.setValue(false);
                        profileUpdateSuccess.setValue(true);
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Error updating password: " + errorMsg);
                        profileUpdateSuccess.setValue(false);
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Authentication failed: " + errorMsg);
                profileUpdateSuccess.setValue(false);
            }
        });
    }

    /**
     * Send password reset email
     */
    public void resetPassword(String email) {
        isLoading.setValue(true);

        authManager.sendPasswordResetEmail(email, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                isLoading.setValue(false);
                profileUpdateSuccess.setValue(true);
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to send reset email: " + errorMsg);
                profileUpdateSuccess.setValue(false);
            }
        });
    }

    /**
     * Delete user account
     */
    public void deleteAccount(String password) {
        if (!authManager.isUserLoggedIn()) {
            errorMessage.setValue("You must be logged in to delete your account");
            return;
        }

        isLoading.setValue(true);

        // First reauthenticate
        authManager.reauthenticate(password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Delete user data from Firestore
                firestoreManager.deleteUserData(user.getUid(), new FirestoreManager.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        // Delete the authentication account
                        authManager.deleteAccount(new FirebaseAuthManager.AuthCallback() {
                            @Override
                            public void onSuccess(FirebaseUser user) {
                                isLoading.setValue(false);
                                isLoggedIn.setValue(false);
                                currentUser.setValue(null);

                                // Delete from local database
                                deleteUserFromLocalDatabase();
                            }

                            @Override
                            public void onFailure(String errorMsg) {
                                isLoading.setValue(false);
                                errorMessage.setValue("Error deleting account: " + errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Error deleting user data: " + errorMsg);
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Authentication failed: " + errorMsg);
            }
        });
    }

    /**
     * Load user from local database
     */
    private void loadUserFromLocalDatabase(String userId) {
        databaseExecutor.execute(() -> {
            try {
                UserEntity entity = userDao.getUserByIdSync(userId);
                if (entity != null) {
                    User user = convertEntityToModel(entity);
                    currentUser.postValue(user);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading user from local database", e);
            }
        });
    }

    /**
     * Save user to local database
     */
    private void saveUserToLocalDatabase(User user) {
        databaseExecutor.execute(() -> {
            try {
                UserEntity entity = convertModelToEntity(user);
                userDao.insert(entity);
            } catch (Exception e) {
                Log.e(TAG, "Error saving user to local database", e);
            }
        });
    }

    /**
     * Delete user from local database
     */
    private void deleteUserFromLocalDatabase() {
        databaseExecutor.execute(() -> {
            try {
                userDao.deleteAll();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user from local database", e);
            }
        });
    }

    /**
     * Convert User model to UserEntity
     */
    private UserEntity convertModelToEntity(User model) {
        return new UserEntity(
                model.getUid(),
                model.getName(),
                model.getEmail(),
                model.getPhoneNumber(),
                model.getProfileImageUrl()
        );
    }

    /**
     * Convert UserEntity to User model
     */
    private User convertEntityToModel(UserEntity entity) {
        User model = new User();
        model.setUid(entity.getUid());
        model.setName(entity.getName());
        model.setEmail(entity.getEmail());
        model.setPhoneNumber(entity.getPhoneNumber());
        model.setProfileImageUrl(entity.getProfileImageUrl());
        return model;
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        databaseExecutor.shutdown();
    }
}