package com.example.parkingfinder.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.parkingfinder.R;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;
    private StorageReference storageReference;
    private User currentUser;
    private Uri selectedImageUri = null;

    // UI components
    private ImageView profileImageView;
    private TextView emailTextView;
    private TextInputLayout nameInputLayout;
    private EditText nameEditText;
    private TextInputLayout phoneInputLayout;
    private EditText phoneEditText;
    private Button saveButton;
    private Button changePasswordButton;
    private Button logoutButton;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        // Initialize Firebase managers
        authManager = FirebaseAuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Check if user is logged in
        if (!authManager.isUserLoggedIn()) {
            // Redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        profileImageView = findViewById(R.id.image_view_profile);
        emailTextView = findViewById(R.id.text_view_email);
        nameInputLayout = findViewById(R.id.text_input_layout_name);
        nameEditText = findViewById(R.id.edit_text_name);
        phoneInputLayout = findViewById(R.id.text_input_layout_phone);
        phoneEditText = findViewById(R.id.edit_text_phone);
        saveButton = findViewById(R.id.button_save);
        changePasswordButton = findViewById(R.id.button_change_password);
        logoutButton = findViewById(R.id.button_logout);
        progressBar = findViewById(R.id.progress_bar);

        // Load user data
        loadUserProfile();

        // Set click listeners
        profileImageView.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveProfile());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser firebaseUser = authManager.getCurrentUser();
        emailTextView.setText(firebaseUser.getEmail());

        firestoreManager.getUserProfile(firebaseUser.getUid(), new FirestoreManager.GetUserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;

                // Set UI values
                nameEditText.setText(user.getName());
                phoneEditText.setText(user.getPhoneNumber());

                // Load profile image if available
                if (!TextUtils.isEmpty(user.getProfileImageUrl())) {
                    Glide.with(ProfileActivity.this)
                            .load(user.getProfileImageUrl())
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(profileImageView);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ProfileActivity.this, "Error loading profile: " + errorMessage, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Display selected image
            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(profileImageView);
        }
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError("Name is required");
            return;
        } else {
            nameInputLayout.setError(null);
        }

        progressBar.setVisibility(View.VISIBLE);

        // If image is selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(name, phone);
        } else {
            saveUserData(name, phone, currentUser.getProfileImageUrl());
        }
    }

    private void uploadImageAndSaveProfile(String name, String phone) {
        String userId = authManager.getCurrentUser().getUid();
        StorageReference fileReference = storageReference.child(userId + ".jpg");

        fileReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveUserData(name, phone, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserData(String name, String phone, String imageUrl) {
        if (currentUser == null) {
            currentUser = new User();
            currentUser.setUid(authManager.getCurrentUser().getUid());
            currentUser.setEmail(authManager.getCurrentUser().getEmail());
        }

        currentUser.setName(name);
        currentUser.setPhoneNumber(phone);
        currentUser.setProfileImageUrl(imageUrl);

        firestoreManager.updateUserProfile(currentUser, new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                selectedImageUri = null;
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Error updating profile: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText currentPasswordEditText = dialogView.findViewById(R.id.edit_text_current_password);
        EditText newPasswordEditText = dialogView.findViewById(R.id.edit_text_new_password);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.edit_text_confirm_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = currentPasswordEditText.getText().toString().trim();
                    String newPassword = newPasswordEditText.getText().toString().trim();
                    String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                    if (validatePasswordInput(currentPassword, newPassword, confirmPassword)) {
                        changePassword(currentPassword, newPassword);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean validatePasswordInput(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void changePassword(String currentPassword, String newPassword) {
        progressBar.setVisibility(View.VISIBLE);

        // Reauthenticate and change password
        authManager.reauthenticate(currentPassword, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                authManager.updatePassword(newPassword, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Error updating password: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    authManager.logoutUser();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.button_view_bookings) {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("navigate_to_bookings", true));
            return true;
        } else if (id == R.id.button_view_favorites) {
            // Navigate to favorites
            return true;
        }
//        else if (id == R.id.) {
//            showDeleteAccountConfirmationDialog();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Show password confirmation dialog
                    showPasswordConfirmationDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPasswordConfirmationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_password, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText passwordEditText = dialogView.findViewById(R.id.edit_text_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Password")
                .setMessage("Please enter your password to confirm account deletion")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(password)) {
                        deleteAccount(password);
                    } else {
                        Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount(String password) {
        progressBar.setVisibility(View.VISIBLE);

        // Reauthenticate user before deletion
        authManager.reauthenticate(password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Delete user data from Firestore first
                firestoreManager.deleteUserData(user.getUid(), new FirestoreManager.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        // Then delete the authentication account
                        authManager.deleteAccount(new FirebaseAuthManager.AuthCallback() {
                            @Override
                            public void onSuccess(FirebaseUser user) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "Error deleting account: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Error deleting user data: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}