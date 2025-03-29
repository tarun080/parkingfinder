package com.example.parkingfinder.activities;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.osmdroid.config.Configuration;

import java.io.File;

public class ParkingFinderApplication extends Application implements
        ProviderInstaller.ProviderInstallListener {

    private static final String TAG = "ParkingFinderApp";
    private static Context appContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // Enable MultiDex if needed
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        // Update security provider to avoid SSL issues
        try {
            ProviderInstaller.installIfNeededAsync(this, this);
        } catch (Exception e) {
            Log.e(TAG, "Error installing security provider: " + e.getMessage());
        }

        // Initialize OSMDroid configuration properly
        initializeOSMDroid();

        // Initialize Firebase
        initializeFirebase();
    }

    public static Context getAppContext() {
        return appContext;
    }

    private void initializeOSMDroid() {
        try {
            // Initialize OSMDroid configuration
            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

            // Important: Set user agent to avoid getting banned from OSM servers
            Configuration.getInstance().setUserAgentValue(getPackageName() + "/" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

            // Set custom tile cache path in app's internal storage (no permissions needed)
            File osmdroidCacheDir = new File(getCacheDir(), "osmdroid");
            if (!osmdroidCacheDir.exists()) {
                boolean created = osmdroidCacheDir.mkdirs();
                if (!created) {
                    Log.w(TAG, "Failed to create OSMDroid cache directory");
                }
            }

            Configuration.getInstance().setOsmdroidTileCache(osmdroidCacheDir);
            Configuration.getInstance().setOsmdroidBasePath(getFilesDir());

            // Additional configuration to avoid permission issues
            Configuration.getInstance().setMapTileDownloaderFollowRedirects(true);

            // Set a reasonable tile download limit to avoid overloading servers
            Configuration.getInstance().setTileDownloadThreads((short) 2);
            Configuration.getInstance().setTileFileSystemThreads((short) 4);
            Configuration.getInstance().setTileDownloadMaxQueueSize((short) 50);

            Log.d(TAG, "OSMDroid successfully initialized");
            Log.d(TAG, "OSMDroid cache path: " + osmdroidCacheDir.getAbsolutePath());
            Log.d(TAG, "OSMDroid base path: " + getFilesDir().getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing OSMDroid: " + e.getMessage(), e);
        }
    }

    private void initializeFirebase() {
        Log.d(TAG, "Initializing Firebase...");

        try {
            // Ensure we don't initialize Firebase multiple times
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully");
            } else {
                Log.d(TAG, "Firebase was already initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.getMessage(), e);

            // Try alternative initialization if default fails
            try {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setProjectId("parkingfinder-ae2a6") // Using your actual project ID from google-services.json
                        .setApplicationId("com.example.parkingfinder")
                        .build();

                if (FirebaseApp.getApps(this).isEmpty()) {
                    FirebaseApp.initializeApp(this, options);
                    Log.d(TAG, "Firebase initialized with manual options");
                }
            } catch (Exception e2) {
                Log.e(TAG, "Failed to initialize Firebase with manual options: " + e2.getMessage(), e2);
            }
        }
    }

    // Add this method to your ParkingFinderApplication.java class

    private void initializeFirebase() {
        Log.d(TAG, "Initializing Firebase...");

        try {
            // Make sure we're initializing Firebase properly
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully");

                // Verify Firebase Auth is initialized
                try {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    Log.d(TAG, "Firebase Auth initialized successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing Firebase Auth: " + e.getMessage(), e);
                }
            } else {
                Log.d(TAG, "Firebase was already initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.getMessage(), e);

            // Try manual initialization as a fallback
            try {
                // Read values from google-services.json manually
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("com.example.parkingfinder")
                        .setProjectId("parkingfinder-ae2a6")
                        .setApiKey("AIzaSyAOOaTi7sP4_mXOFYW0tY5STocGPlNDo5I") // From your google-services.json
                        .setDatabaseUrl("https://parkingfinder-ae2a6.firebaseio.com")
                        .setStorageBucket("parkingfinder-ae2a6.firebasestorage.app")
                        .build();

                if (FirebaseApp.getApps(this).isEmpty()) {
                    FirebaseApp.initializeApp(this, options);
                    Log.d(TAG, "Firebase initialized manually with options");
                }
            } catch (Exception e2) {
                Log.e(TAG, "Failed to initialize Firebase manually: " + e2.getMessage(), e2);
            }
        }
    }

    @Override
    public void onProviderInstallFailed(int errorCode, android.content.Intent recoveryIntent) {
        GoogleApiAvailability.getInstance().showErrorNotification(this, errorCode);
    }

    @Override
    public void onProviderInstalled() {
        Log.d(TAG, "Security provider installed successfully");
    }
}