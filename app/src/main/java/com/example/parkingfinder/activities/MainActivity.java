package com.example.parkingfinder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 200 ;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private ParkingListFragment listFragment;
    private BookingsFragment bookingsFragment;
    private ProfileFragment profileFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ParkingFinder);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            }
        }

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