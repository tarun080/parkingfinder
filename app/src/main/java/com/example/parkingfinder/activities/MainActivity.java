package com.example.parkingfinder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.parkingfinder.R;
import com.example.parkingfinder.fragments.BookingsFragment;
import com.example.parkingfinder.fragments.MapFragment;
import com.example.parkingfinder.fragments.ParkingListFragment;
import com.example.parkingfinder.fragments.ProfileFragment;
import com.example.parkingfinder.fragments.SimpleMapFragment;
import com.example.parkingfinder.utils.PermissionUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 200;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private ParkingListFragment listFragment;
    private BookingsFragment bookingsFragment;
    private ProfileFragment profileFragment;

    // Activity result launchers for permissions
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    // Precise location access granted
                    requestBackgroundLocationIfNeeded();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Only approximate location access granted
                    requestBackgroundLocationIfNeeded();
                } else {
                    // No location access granted
                    Toast.makeText(this, "Location permission is required for this app to work properly",
                            Toast.LENGTH_LONG).show();
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ParkingFinder);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Check and request permissions
        checkAndRequestPermissions();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SimpleMapFragment())
                    .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize fragment manager
        fragmentManager = getSupportFragmentManager();

        // Initialize fragments (only create new if needed)
        if (savedInstanceState == null) {
            mapFragment = new MapFragment();
            listFragment = new ParkingListFragment();
            bookingsFragment = new BookingsFragment();
            profileFragment = new ProfileFragment();
        }

        // Set up bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_map) {
                showFragment(mapFragment);
                return true;
            } else if (itemId == R.id.navigation_list) {
                showFragment(listFragment);
                return true;
            } else if (itemId == R.id.navigation_bookings) {
                showFragment(bookingsFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                showFragment(profileFragment);
                return true;
            }

            return false;
        });

        // Check if we have a navigation instruction from intent
        if (getIntent().getBooleanExtra("navigate_to_bookings", false)) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_bookings);
        } else {
            // Default to map fragment
            bottomNavigationView.setSelectedItemId(R.id.navigation_map);
        }

        // Check Google Play Services
        checkPlayServices();
    }

    private void checkAndRequestPermissions() {
        // Check location permission
        if (!PermissionUtils.hasLocationPermission(this)) {
            // Request location permissions using the Activity Result API
            locationPermissionRequest.launch(REQUIRED_PERMISSIONS);
        } else {
            // Location permission already granted, check for background location if needed
            requestBackgroundLocationIfNeeded();
        }

        // Check storage permission for older Android versions (for OSMDroid)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            }
        }

        // Check if location is enabled
        if (!PermissionUtils.isLocationEnabled(this)) {
            PermissionUtils.showLocationSettingsDialog(this);
        }
    }

    private void requestBackgroundLocationIfNeeded() {
        // For Android 10+ (Q), request background location separately if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!PermissionUtils.hasBackgroundLocationPermission(this)) {
                // Show dialog explaining why background location is needed
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Background Location")
                        .setMessage("This app needs background location access to find parking spaces while running in the background.")
                        .setPositiveButton("Grant", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    PermissionUtils.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Not Now", null)
                        .create()
                        .show();
            }
        }
    }

    // Helper method to show a fragment
    private void showFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Method to switch to bookings tab (used from other activities)
    public void switchToBookingsTab() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_bookings);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, proceed
                requestBackgroundLocationIfNeeded();
            } else {
                // Permission denied
                if (PermissionUtils.shouldShowRequestPermissionRationale(this)) {
                    // User denied but didn't check "Don't ask again"
                    PermissionUtils.showPermissionExplanationDialog(this,
                            "Location permission is required to find nearby parking spots.");
                } else {
                    // User denied and checked "Don't ask again"
                    PermissionUtils.showPermissionPermanentlyDeniedDialog(this,
                            "Location permission is required. Please enable it in app settings.");
                }
            }
        } else if (requestCode == PermissionUtils.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            // Background location permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Background location granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Background location denied - some features may be limited",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            // Handle storage permission result if needed
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Storage permission granted for OSMDroid tile caching
            }
        }
    }

    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 2404)
                        .show();
            } else {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}