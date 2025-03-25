package com.example.parkingfinder.activities;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class ParkingFinderApplication extends Application {

    private static final String TAG = "ParkingFinderApp";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Initializing Firebase...");

        // Initialize Firebase with logging to track if this code is executed
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.getMessage(), e);

            // Try alternative initialization if default fails
            try {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setProjectId("parkingfinder-ae2a6") // Update with your actual project ID
                        .setApplicationId("com.example.parkingfinder")
                        .build();

                FirebaseApp.initializeApp(this, options);
                Log.d(TAG, "Firebase initialized with manual options");
            } catch (Exception e2) {
                Log.e(TAG, "Failed to initialize Firebase with manual options: " + e2.getMessage(), e2);
            }
        }
    }
}