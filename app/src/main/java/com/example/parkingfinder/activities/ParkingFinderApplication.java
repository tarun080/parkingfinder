package com.example.parkingfinder.activities;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.osmdroid.config.Configuration;

import java.io.File;

public class ParkingFinderApplication extends Application {

    private static final String TAG = "ParkingFinderApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Important: Set user agent to avoid getting banned from OSM servers
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Set custom tile cache path in app's internal storage (no permissions needed)
        File osmdroidCacheDir = new File(getCacheDir(), "osmdroid");
        if (!osmdroidCacheDir.exists()) {
            osmdroidCacheDir.mkdirs();
        }
        Configuration.getInstance().setOsmdroidTileCache(osmdroidCacheDir);
        Configuration.getInstance().setOsmdroidBasePath(getFilesDir());

        Log.d(TAG, "OSMDroid cache path: " + osmdroidCacheDir.getAbsolutePath());
        Log.d(TAG, "OSMDroid base path: " + getFilesDir().getAbsolutePath());

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