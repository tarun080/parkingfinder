package com.example.parkingfinder.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.parkingfinder.R;
import com.example.parkingfinder.activities.LoginActivity;
import com.example.parkingfinder.activities.MainActivity;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;
    private StorageReference storageReference;
    private User currentUser;
    private Uri selectedImageUri = null;

    // UI components for authenticated view
    private View authenticatedView;
    private ImageView profileImageView;
    private TextView emailTextView;
    private TextInputLayout nameInputLayout;
    private EditText nameEditText;
    private TextInputLayout phoneInputLayout;
    private EditText phoneEditText;
    private Button saveButton;
    private Button changePasswordButton;
    private Button viewBookingsButton;
    private Button viewFavoritesButton;
    private Button logoutButton;
    private ProgressBar progressBar;

    // UI components for guest view
    private View guestView;
    private Button loginButton;
    private Button registerButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase managers
        authManager = FirebaseAuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize UI components
        authenticatedView = view.findViewById(R.id.authenticated_view);
        guestView = view.findViewById(R.id.guest_view);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize authenticated view components
        profileImageView = view.findViewById(R.id.image_view_profile);
        emailTextView = view.findViewById(R.id.text_view_email);
        nameInputLayout = view.findViewById(R.id.text_input_layout_name);
        nameEditText = view.findViewById(R.id.edit_text_name);
        phoneInputLayout = view.findViewById(R.id.text_input_layout_phone);
        phoneEditText = view.findViewById(R.id.edit_text_phone);
        saveButton = view.findViewById(R.id.button_save);
        changePasswordButton = view.findViewById(R.id.button_change_password);
        viewBookingsButton = view.findViewById(R.id.button_view_bookings);
        viewFavoritesButton = view.findViewById(R.id.button_view_favorites);
        logoutButton = view.findViewById(R.id.button_logout);

        // Initialize guest view components
        loginButton = view.findViewById(R.id.button_login);
        registerButton = view.findViewById(R.id.button_register);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set click listeners for authenticated view
        profileImageView.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveProfile());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        viewBookingsButton.setOnClickListener(v -> navigateToBookings());
        viewFavoritesButton.setOnClickListener(v -> navigateToFavorites());
        logoutButton.setOnClickListener(v -> logout());

        // Set click listeners for guest view
        loginButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.putExtra("open_register", true);
            startActivity(intent);
        });

        // Check if user is logged in and update UI accordingly
        if (authManager.isUserLoggedIn()) {
            showAuthenticatedView();
            loadUserProfile();
        } else {
            showGuestView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if user is logged in and refresh the view
        if (authManager.isUserLoggedIn()) {
            showAuthenticatedView();
            loadUserProfile();
        } else {
            showGuestView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Display selected image
            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(profileImageView);
        }
    }

    private void showAuthenticatedView() {
        authenticatedView.setVisibility(View.VISIBLE);
        guestView.setVisibility(View.GONE);
    }

    private void showGuestView() {
        authenticatedView.setVisibility(View.GONE);
        guestView.setVisibility(View.VISIBLE);
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
                    Glide.with(ProfileFragment.this)
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
                Toast.makeText(getContext(), "Error loading profile: " + errorMessage, Toast.LENGTH_SHORT).show();
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
            saveUserData(name, phone, currentUser != null ? currentUser.getProfileImageUrl() : null);
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
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                selectedImageUri = null;
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error updating profile: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        EditText currentPasswordEditText = dialogView.findViewById(R.id.edit_text_current_password);
        EditText newPasswordEditText = dialogView.findViewById(R.id.edit_text_new_password);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.edit_text_confirm_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
            Toast.makeText(getContext(), "Please enter your current password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getContext(), "Please enter a new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error updating password: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToBookings() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchToBookingsTab();
        }
    }

    private void navigateToFavorites() {
        // Implement navigation to favorites screen
        Toast.makeText(getContext(), "Favorites feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    authManager.logoutUser();
                    showGuestView();
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}