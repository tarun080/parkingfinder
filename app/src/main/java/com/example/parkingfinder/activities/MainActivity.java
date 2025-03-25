package com.example.parkingfinder.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

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
}