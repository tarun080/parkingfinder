package com.example.parkingfinder.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parkingfinder.R;
import com.example.parkingfinder.activities.BookingActivity;
import com.example.parkingfinder.activities.BookingDetailsActivity;
import com.example.parkingfinder.activities.LoginActivity;
import com.example.parkingfinder.adapters.BookingHistoryAdapter;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.Booking;
import com.example.parkingfinder.viewmodels.BookingViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BookingsFragment extends Fragment implements BookingHistoryAdapter.OnBookingClickListener {

    private BookingViewModel bookingViewModel;
    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;

    // UI components
    private SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private View loginPromptView;
    private BookingHistoryAdapter adapter;

    // State variables
    private String currentTab = "all"; // all, upcoming, active, past
    private List<Booking> bookings = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase managers
        authManager = FirebaseAuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();

        // Initialize UI components
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        tabLayout = view.findViewById(R.id.tab_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyTextView = view.findViewById(R.id.text_view_empty);
        loginPromptView = view.findViewById(R.id.login_prompt_container);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingHistoryAdapter(getContext(), bookings, this);
        recyclerView.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (authManager.isUserLoggedIn()) {
                loadBookings();
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Setup tab layout
        setupTabLayout();

        // Setup login prompt
        if (!authManager.isUserLoggedIn()) {
            showLoginPrompt();
        } else {
            hideLoginPrompt();

            // Initialize ViewModel
            bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);

            // Observe bookings
            observeBookings();

            // Load bookings
            loadBookings();
        }

        // Setup login button in the prompt
        view.findViewById(R.id.button_login).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check login status and reload bookings if logged in
        if (authManager.isUserLoggedIn()) {
            hideLoginPrompt();

            // Initialize ViewModel if not already initialized
            if (bookingViewModel == null) {
                bookingViewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
                observeBookings();
            }

            loadBookings();
        } else {
            showLoginPrompt();
        }
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Upcoming"));
        tabLayout.addTab(tabLayout.newTab().setText("Active"));
        tabLayout.addTab(tabLayout.newTab().setText("Past"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "all";
                        break;
                    case 1:
                        currentTab = "upcoming";
                        break;
                    case 2:
                        currentTab = "active";
                        break;
                    case 3:
                        currentTab = "past";
                        break;
                }
                filterBookings();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not used
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not used
            }
        });
    }

    private void observeBookings() {
        bookingViewModel.getBookings().observe(getViewLifecycleOwner(), new Observer<List<Booking>>() {
            @Override
            public void onChanged(List<Booking> bookingList) {
                bookings.clear();
                if (bookingList != null) {
                    bookings.addAll(bookingList);
                }
                filterBookings();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                // Show empty view if no bookings
                if (bookings.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        bookingViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        bookingViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadBookings() {
        if (authManager.isUserLoggedIn() && bookingViewModel != null) {
            bookingViewModel.loadBookings(authManager.getCurrentUser().getUid());
        }
    }

    private void filterBookings() {
        if (bookings == null || bookings.isEmpty()) {
            adapter.updateData(new ArrayList<>());
            return;
        }

        List<Booking> filteredBookings = new ArrayList<>();
        Date now = new Date();

        for (Booking booking : bookings) {
            switch (currentTab) {
                case "all":
                    filteredBookings.add(booking);
                    break;
                case "upcoming":
                    if (booking.getStartTime().after(now) &&
                            ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus()))) {
                        filteredBookings.add(booking);
                    }
                    break;
                case "active":
                    if ((booking.getStartTime().before(now) && booking.getEndTime().after(now)) ||
                            "ACTIVE".equals(booking.getStatus())) {
                        filteredBookings.add(booking);
                    }
                    break;
                case "past":
                    if (booking.getEndTime().before(now) ||
                            "COMPLETED".equals(booking.getStatus()) ||
                            "CANCELLED".equals(booking.getStatus())) {
                        filteredBookings.add(booking);
                    }
                    break;
            }
        }

        adapter.updateData(filteredBookings);

        // Update empty text based on current tab
        if (filteredBookings.isEmpty()) {
            switch (currentTab) {
                case "all":
                    emptyTextView.setText("You don't have any bookings yet.");
                    break;
                case "upcoming":
                    emptyTextView.setText("You don't have any upcoming bookings.");
                    break;
                case "active":
                    emptyTextView.setText("You don't have any active bookings.");
                    break;
                case "past":
                    emptyTextView.setText("You don't have any past bookings.");
                    break;
            }
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoginPrompt() {
        loginPromptView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    private void hideLoginPrompt() {
        loginPromptView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBookingClick(Booking booking) {
        // Navigate to booking details
        Intent intent = new Intent(getActivity(), BookingDetailsActivity.class);
        intent.putExtra("booking_id", booking.getId());
        startActivity(intent);
    }

    @Override
    public void onCancelBooking(Booking booking) {
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    firestoreManager.cancelBooking(
                            booking.getId(),
                            booking.getParkingAreaId(),
                            booking.getParkingSpotId(),
                            new FirestoreManager.FirestoreCallback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onExtendBooking(Booking booking) {
        // Navigate to booking extension screen
        Intent intent = new Intent(getActivity(), BookingActivity.class);
        intent.putExtra("parking_area_id", booking.getParkingAreaId());
        intent.putExtra("parking_area_name", booking.getParkingAreaName());
        intent.putExtra("parking_spot_id", booking.getParkingSpotId());
        intent.putExtra("parking_spot_number", booking.getParkingSpotNumber());
        intent.putExtra("extend_booking_id", booking.getId());
        intent.putExtra("current_end_time", booking.getEndTime().getTime());
        startActivity(intent);
    }
}