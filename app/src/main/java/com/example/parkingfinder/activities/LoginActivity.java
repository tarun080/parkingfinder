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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private ProgressBar progressBar;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        // Initialize Firebase Auth Manager
        authManager = FirebaseAuthManager.getInstance();

        // Check if user is already logged in
        if (authManager.isUserLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Initialize views
        emailEditText = findViewById(R.id.edit_text_email);
        passwordEditText = findViewById(R.id.edit_text_password);
        loginButton = findViewById(R.id.button_login);
        registerTextView = findViewById(R.id.text_view_register);
        forgotPasswordTextView = findViewById(R.id.text_view_forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        try {
            // Direct Firebase authentication as a fallback
            if (authManager == null) {
                Log.w(TAG, "AuthManager is null, using direct Firebase Auth");
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);

                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                String errorMessage = task.getException() != null ?
                                        task.getException().getMessage() : "Authentication failed";
                                Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                return;
            }

            // Attempt login through AuthManager
            authManager.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Login error: " + errorMessage);
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            Log.e(TAG, "Exception during login: " + e.getMessage(), e);
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPasswordResetDialog() {
        // Implement password reset dialog here
        // For simplicity, we'll just use the email already entered
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        try {
            if (authManager == null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Reset email sent to " + email, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Error sending reset email", Toast.LENGTH_LONG).show();
                            }
                        });
                return;
            }

            authManager.sendPasswordResetEmail(email, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Reset email sent to " + email, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Exception during password reset: " + e.getMessage(), e);
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}