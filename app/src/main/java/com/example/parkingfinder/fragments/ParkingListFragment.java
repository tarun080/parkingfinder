package com.example.parkingfinder.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parkingfinder.R;
import com.example.parkingfinder.activities.ParkingDetailsActivity;
import com.example.parkingfinder.adapters.ParkingListAdapter;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.ParkingArea;
import com.example.parkingfinder.utils.PermissionUtils;
import com.example.parkingfinder.viewmodels.ParkingViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ParkingListFragment extends Fragment implements ParkingListAdapter.OnParkingItemClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ParkingViewModel parkingViewModel;
    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;
    private FusedLocationProviderClient fusedLocationClient;

    // UI components
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private ImageButton sortButton;
    private ImageButton filterButton;

    private ParkingListAdapter adapter;

    // State variables
    private List<ParkingArea> parkingAreas = new ArrayList<>();
    private List<ParkingArea> filteredParkingAreas = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentSortOption = "distance"; // distance, price, availability, rating
    private double userLatitude = 0;
    private double userLongitude = 0;
    private boolean onlyShowAvailable = false;
    private double maxPrice = Double.MAX_VALUE;
    private boolean hasEVCharging = false;
    private boolean hasDisabledAccess = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parking_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize managers
        authManager = FirebaseAuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize UI components
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyTextView = view.findViewById(R.id.text_view_empty);
        searchEditText = view.findViewById(R.id.edit_text_search);
        clearSearchButton = view.findViewById(R.id.button_clear_search);
        sortButton = view.findViewById(R.id.button_sort);
        filterButton = view.findViewById(R.id.button_filter);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ParkingListAdapter(getContext(), filteredParkingAreas, this);
        recyclerView.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadParkingAreas);

        // Initialize ViewModel
        parkingViewModel = new ViewModelProvider(requireActivity()).get(ParkingViewModel.class);

        // Observe parking areas
        observeParkingAreas();

        // Setup search
        setupSearch();

        // Setup sort and filter buttons
        setupSortAndFilter();

        // Check location permission and get user location
        checkLocationPermissionAndGetLocation();

        // Load parking areas
        loadParkingAreas();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadParkingAreas();
    }

    private void observeParkingAreas() {
        parkingViewModel.getParkingAreas().observe(getViewLifecycleOwner(), new Observer<List<ParkingArea>>() {
            @Override
            public void onChanged(List<ParkingArea> areas) {
                parkingAreas.clear();
                if (areas != null) {
                    parkingAreas.addAll(areas);
                }
                applyFilters();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        parkingViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        parkingViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                updateClearButton();
                applyFilters();
            }
        });

        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            currentSearchQuery = "";
            updateClearButton();
            applyFilters();
        });

        updateClearButton();
    }

    private void updateClearButton() {
        if (currentSearchQuery.isEmpty()) {
            clearSearchButton.setVisibility(View.GONE);
        } else {
            clearSearchButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupSortAndFilter() {
        sortButton.setOnClickListener(v -> showSortMenu());
        filterButton.setOnClickListener(v -> showFilterDialog());
    }

    private void showSortMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), sortButton);
        popup.getMenuInflater().inflate(R.menu.menu_sort_parking, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.sort_distance) {
                currentSortOption = "distance";
            } else if (itemId == R.id.sort_price) {
                currentSortOption = "price";
            } else if (itemId == R.id.sort_availability) {
                currentSortOption = "availability";
            } else if (itemId == R.id.sort_rating) {
                currentSortOption = "rating";
            }

            applyFilters();
            return true;
        });

        popup.show();
    }

    private void showFilterDialog() {
        FilterDialog filterDialog = new FilterDialog(
                requireContext(),
                onlyShowAvailable,
                maxPrice,
                hasEVCharging,
                hasDisabledAccess,
                (onlyAvailable, maxPriceValue, hasEV, hasDisabled) -> {
                    onlyShowAvailable = onlyAvailable;
                    maxPrice = maxPriceValue;
                    hasEVCharging = hasEV;
                    hasDisabledAccess = hasDisabled;
                    applyFilters();
                }
        );
        filterDialog.show();
    }

    private void applyFilters() {
        if (parkingAreas.isEmpty()) {
            showEmptyState("No parking areas found");
            return;
        }

        // Apply search filter
        List<ParkingArea> searchFiltered = new ArrayList<>();
        if (currentSearchQuery.isEmpty()) {
            searchFiltered.addAll(parkingAreas);
        } else {
            for (ParkingArea area : parkingAreas) {
                if (area.getName().toLowerCase().contains(currentSearchQuery) ||
                        area.getAddress().toLowerCase().contains(currentSearchQuery)) {
                    searchFiltered.add(area);
                }
            }
        }

        // Apply other filters
        filteredParkingAreas.clear();
        for (ParkingArea area : searchFiltered) {
            boolean includeArea = true;

            // Filter by availability
            if (onlyShowAvailable && area.getAvailableSpots() <= 0) {
                includeArea = false;
            }

            // Filter by price
            if (area.getHourlyRate() > maxPrice) {
                includeArea = false;
            }

            // Filter by EV charging
            if (hasEVCharging && !area.isHasElectricCharging()) {
                includeArea = false;
            }

            // Filter by disabled access
            if (hasDisabledAccess && !area.isHasDisabledAccess()) {
                includeArea = false;
            }

            if (includeArea) {
                filteredParkingAreas.add(area);
            }
        }

        // Apply sorting
        sortFilteredList();

        // Update adapter
        adapter.updateData(filteredParkingAreas);

        // Show/hide empty state
        if (filteredParkingAreas.isEmpty()) {
            showEmptyState("No parking areas match your criteria");
        } else {
            hideEmptyState();
        }
    }

    private void sortFilteredList() {
        switch (currentSortOption) {
            case "distance":
                if (userLatitude != 0 && userLongitude != 0) {
                    Collections.sort(filteredParkingAreas, (a, b) -> {
                        double distanceA = a.distanceFrom(userLatitude, userLongitude);
                        double distanceB = b.distanceFrom(userLatitude, userLongitude);
                        return Double.compare(distanceA, distanceB);
                    });
                }
                break;
            case "price":
                Collections.sort(filteredParkingAreas, Comparator.comparingDouble(ParkingArea::getHourlyRate));
                break;
            case "availability":
                Collections.sort(filteredParkingAreas, (a, b) -> {
                    // Sort by percentage available (descending)
                    double percentA = a.getTotalSpots() > 0 ? ((double) a.getAvailableSpots() / a.getTotalSpots()) : 0;
                    double percentB = b.getTotalSpots() > 0 ? ((double) b.getAvailableSpots() / b.getTotalSpots()) : 0;
                    return Double.compare(percentB, percentA);
                });
                break;
            case "rating":
                Collections.sort(filteredParkingAreas, (a, b) -> Float.compare(b.getRating(), a.getRating()));
                break;
        }
    }

    private void showEmptyState(String message) {
        emptyTextView.setText(message);
        emptyTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void loadParkingAreas() {
        if (parkingViewModel != null) {
            parkingViewModel.loadParkingAreas(userLatitude, userLongitude, 10.0);
        }
    }

    private void checkLocationPermissionAndGetLocation() {
        if (PermissionUtils.hasLocationPermission(requireContext())) {
            getUserLocation();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLatitude = location.getLatitude();
                            userLongitude = location.getLongitude();

                            // Update adapter with user location
                            adapter.setUserLocation(userLatitude, userLongitude);

                            // Re-apply sorting in case we're sorting by distance
                            if ("distance".equals(currentSortOption)) {
                                applyFilters();
                            }

                            // Reload parking areas with user location
                            loadParkingAreas();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(getContext(), "Location permission is required to find nearby parking", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onParkingItemClick(ParkingArea parkingArea) {
        // Navigate to parking details
        Intent intent = new Intent(getActivity(), ParkingDetailsActivity.class);
        intent.putExtra("parking_area_id", parkingArea.getId());
        startActivity(intent);
    }

    @Override
    public void onBookButtonClick(ParkingArea parkingArea) {
        // Navigate directly to parking details with "book" flag
        Intent intent = new Intent(getActivity(), ParkingDetailsActivity.class);
        intent.putExtra("parking_area_id", parkingArea.getId());
        intent.putExtra("auto_book", true);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(ParkingArea parkingArea, boolean isFavorite) {
        // Update favorite status in Firestore
        if (authManager.isUserLoggedIn()) {
            firestoreManager.updateFavoriteStatus(
                    authManager.getCurrentUser().getUid(),
                    parkingArea.getId(),
                    isFavorite,
                    new FirestoreManager.FirestoreCallback() {
                        @Override
                        public void onSuccess() {
                            // Success is already reflected in UI due to immediate feedback
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            // Revert UI change if failed
                            parkingArea.setFavorite(!isFavorite);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Error updating favorite: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Revert UI change if not logged in
            parkingArea.setFavorite(!isFavorite);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Please log in to save favorites", Toast.LENGTH_SHORT).show();
        }
    }
}