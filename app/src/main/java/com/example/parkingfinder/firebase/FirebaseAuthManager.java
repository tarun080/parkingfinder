package com.example.parkingfinder.firebase;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {
    private static final String TAG = "FirebaseAuthManager";
    private static FirebaseAuthManager instance;
    private FirebaseAuth mAuth;

    // Callback interfaces
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    private FirebaseAuthManager() {
        try {
            mAuth = FirebaseAuth.getInstance();
            Log.d(TAG, "FirebaseAuth initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FirebaseAuth: " + e.getMessage());

            // Try to recover by ensuring Firebase is initialized
            try {
                if (FirebaseApp.getApps(FirebaseApp.getInstance().getApplicationContext()).isEmpty()) {
                    Log.d(TAG, "No FirebaseApp instance found, initializing...");
                    FirebaseApp.initializeApp(FirebaseApp.getInstance().getApplicationContext());
                }
                mAuth = FirebaseAuth.getInstance();
            } catch (Exception e2) {
                Log.e(TAG, "Failed to recover FirebaseAuth initialization: " + e2.getMessage());
            }
        }
    }

    public static synchronized FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }

    public void registerUser(String email, String password, final AuthCallback callback) {
        if (mAuth == null) {
            callback.onFailure("Firebase Authentication not initialized");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(mAuth.getCurrentUser());
                        } else {
                            String errorMessage = getErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void loginUser(String email, String password, final AuthCallback callback) {
        if (mAuth == null) {
            callback.onFailure("Firebase Authentication not initialized");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(mAuth.getCurrentUser());
                        } else {
                            String errorMessage = getErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    // Helper method to get meaningful error messages
    private String getErrorMessage(Exception exception) {
        Log.e(TAG, "Auth error: " + exception.getMessage(), exception);

        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "Invalid user: Account doesn't exist or has been disabled";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Invalid credentials: Please check your email and password";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            return "User already exists: Try logging in instead";
        } else if (exception instanceof FirebaseNetworkException) {
            return "Network error: Please check your internet connection";
        } else if (exception.getMessage() != null && exception.getMessage().contains("CONFIGURATION_NOT_FOUND")) {
            return "Firebase configuration issue: Please reinstall the app or contact support";
        } else {
            return exception.getMessage() != null ? exception.getMessage()
                    : "Authentication failed: Please try again later";
        }
    }

    public void logoutUser() {
        if (mAuth != null) {
            mAuth.signOut();
        }
    }

    public FirebaseUser getCurrentUser() {
        return mAuth != null ? mAuth.getCurrentUser() : null;
    }

    public boolean isUserLoggedIn() {
        return mAuth != null && mAuth.getCurrentUser() != null;
    }

    public void sendPasswordResetEmail(String email, final AuthCallback callback) {
        if (mAuth == null) {
            callback.onFailure("Firebase Authentication not initialized");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            String errorMessage = getErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void reauthenticate(String password, final AuthCallback callback) {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            callback.onFailure("No user is currently logged in");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        // Create credential with current email and provided password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        // Reauthenticate user with credential
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(user);
                        } else {
                            String errorMessage = getErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void updatePassword(String newPassword, final AuthCallback callback) {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            callback.onFailure("No user is currently logged in");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(user);
                        } else {
                            String errorMessage = getErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void deleteAccount(final AuthCallback callback) {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            callback.onFailure("No user is currently logged in");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            String errorMessage = getErrorMessage(task.getException());
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }
}