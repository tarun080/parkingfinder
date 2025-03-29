package com.example.parkingfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkingfinder.R;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;

    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Make sure Firebase is initialized
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                Log.e(TAG, "Firebase not initialized. Attempting to initialize...");
                FirebaseApp.initializeApp(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing Firebase. Please restart the app.", Toast.LENGTH_LONG).show();
        }

        // Initialize managers
        try {
            authManager = FirebaseAuthManager.getInstance();
            firestoreManager = FirestoreManager.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing managers: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app services: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Initialize views
        nameEditText = findViewById(R.id.edit_text_name);
        emailEditText = findViewById(R.id.edit_text_email);
        passwordEditText = findViewById(R.id.edit_text_password);
        confirmPasswordEditText = findViewById(R.id.edit_text_confirm_password);
        registerButton = findViewById(R.id.button_register);
        loginTextView = findViewById(R.id.text_view_login);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to Login activity
            }
        });
    }

    private void registerUser() {
        final String name = nameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        try {
            // Direct Firebase registration as fallback
            if (authManager == null) {
                Log.w(TAG, "AuthManager is null, using direct Firebase Auth");
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                // Create user in Firestore
                                User user = new User();
                                user.setUid(firebaseUser.getUid());
                                user.setName(name);
                                user.setEmail(email);
                                user.setPhoneNumber("");
                                user.setProfileImageUrl("");

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(firebaseUser.getUid())
                                        .set(user)
                                        .addOnCompleteListener(firestoreTask -> {
                                            progressBar.setVisibility(View.GONE);
                                            registerButton.setEnabled(true);

                                            if (firestoreTask.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(RegisterActivity.this,
                                                        "User created but profile setup failed. Please update your profile later.",
                                                        Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                registerButton.setEnabled(true);
                                String errorMessage = task.getException() != null ?
                                        task.getException().getMessage() : "Registration failed";
                                Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                return;
            }

            // Register user with Firebase through AuthManager
            authManager.registerUser(email, password, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser firebaseUser) {
                    // Create user profile in Firestore
                    User user = new User();
                    user.setUid(firebaseUser.getUid());
                    user.setName(name);
                    user.setEmail(email);
                    user.setPhoneNumber("");
                    user.setProfileImageUrl("");

                    firestoreManager.createUserProfile(user, new FirestoreManager.FirestoreCallback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            progressBar.setVisibility(View.GONE);
                            registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            Log.e(TAG, "Exception during registration: " + e.getMessage(), e);
            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}