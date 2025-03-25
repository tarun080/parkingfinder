package com.example.parkingfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parkingfinder.R;
import com.example.parkingfinder.adapters.ParkingSpotAdapter;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.ParkingArea;
import com.example.parkingfinder.models.ParkingSpot;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParkingDetailsActivity extends AppCompatActivity implements ParkingSpotAdapter.OnSpotClickListener {

    private FirestoreManager firestoreManager;
    private ParkingArea parkingArea;
    private ParkingSpotAdapter spotAdapter;
    private List<ParkingSpot> parkingSpots = new ArrayList<>();

    // UI components
    private ImageView headerImageView;
    private TextView addressTextView;
    private TextView ratingTextView;
    private RatingBar ratingBar;
    private TextView availabilityTextView;
    private TextView priceTextView;
    private TextView hoursTextView;
    private TextView amenitiesTextView;
    private RecyclerView spotsRecyclerView;
    private Button bookButton;
    private TextView noSpotsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_details);

        // Initialize Firestore manager
        firestoreManager = FirestoreManager.getInstance();

        // Set up the toolbar and collapsing toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);

        // Initialize views
        headerImageView = findViewById(R.id.image_view_header);
        addressTextView = findViewById(R.id.text_view_address);
        ratingTextView = findViewById(R.id.text_view_rating);
        ratingBar = findViewById(R.id.rating_bar);
        availabilityTextView = findViewById(R.id.text_view_availability);
        priceTextView = findViewById(R.id.text_view_price);
        hoursTextView = findViewById(R.id.text_view_hours);
        amenitiesTextView = findViewById(R.id.text_view_amenities);
        spotsRecyclerView = findViewById(R.id.recycler_view_spots);
        bookButton = findViewById(R.id.button_book);
        noSpotsTextView = findViewById(R.id.text_view_no_spots);

        // Get parking area from intent
        if (getIntent().hasExtra("parking_area_id")) {
            String parkingAreaId = getIntent().getStringExtra("parking_area_id");
            loadParkingAreaDetails(parkingAreaId);
        } else {
            Toast.makeText(this, "Error: No parking area specified", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up recycler view for parking spots
        spotAdapter = new ParkingSpotAdapter(this, parkingSpots, this);
        spotsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        spotsRecyclerView.setAdapter(spotAdapter);

        // Set up book button click listener
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spotAdapter.getSelectedSpot() != null) {
                    // Navigate to booking screen with the selected spot
                    navigateToBooking(spotAdapter.getSelectedSpot());
                } else {
                    Toast.makeText(ParkingDetailsActivity.this,
                            "Please select a parking spot first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadParkingAreaDetails(String parkingAreaId) {
        // Load the parking area details from Firestore
        // For now, we'll simulate with dummy data
        // In a real app, you would fetch this from your Firebase

        // Example of how to fetch from Firestore:
        /*
        DocumentReference docRef = FirebaseFirestore.getInstance()
            .collection("parking_areas").document(parkingAreaId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            parkingArea = documentSnapshot.toObject(ParkingArea.class);
            if (parkingArea != null) {
                parkingArea.setId(documentSnapshot.getId());
                displayParkingAreaDetails();
                loadParkingSpots();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading details: " + e.getMessage(),
                          Toast.LENGTH_SHORT).show();
        });
        */

        // For demo purposes, we'll use the FirestoreManager
        // This is a placeholder - implement proper fetching using your FirestoreManager
        parkingArea = new ParkingArea(); // Dummy data
        parkingArea.setId(parkingAreaId);
        parkingArea.setName("Central Parking Garage");
        parkingArea.setAddress("123 Main Street, Downtown");
        parkingArea.setLatitude(37.7749);
        parkingArea.setLongitude(-122.4194);
        parkingArea.setTotalSpots(50);
        parkingArea.setAvailableSpots(15);
        parkingArea.setHourlyRate(2.50);
        parkingArea.setOperatingHours("24/7");
        parkingArea.setRating(4.2f);
        parkingArea.setNumberOfRatings(124);
        parkingArea.setImageUrl("https://example.com/parking_image.jpg");

        // Display the details
        displayParkingAreaDetails();

        // Load parking spots
        loadParkingSpots();
    }

    private void displayParkingAreaDetails() {
        if (parkingArea == null) return;

        // Set toolbar title
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(parkingArea.getName());

        // Load header image
        if (parkingArea.getImageUrl() != null && !parkingArea.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(parkingArea.getImageUrl())
                    .placeholder(R.drawable.placeholder_parking)
                    .error(R.drawable.placeholder_parking)
                    .into(headerImageView);
        }

        // Set text views
        addressTextView.setText(parkingArea.getAddress());

        // Rating
        String ratingText = String.format(Locale.getDefault(), "%.1f (%d reviews)",
                parkingArea.getRating(), parkingArea.getNumberOfRatings());
        ratingTextView.setText(ratingText);
        ratingBar.setRating(parkingArea.getRating());

        // Availability
        String availabilityText = String.format(Locale.getDefault(), "%d/%d spots available",
                parkingArea.getAvailableSpots(), parkingArea.getTotalSpots());
        availabilityTextView.setText(availabilityText);

        // Price
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        priceTextView.setText(currencyFormat.format(parkingArea.getHourlyRate()) + "/hour");

        // Hours
        hoursTextView.setText(parkingArea.getOperatingHours());

        // Amenities (if any)
        if (parkingArea.getAmenities() != null && !parkingArea.getAmenities().isEmpty()) {
            StringBuilder amenitiesBuilder = new StringBuilder();
            for (String amenity : parkingArea.getAmenities()) {
                amenitiesBuilder.append("• ").append(amenity).append("\n");
            }
            amenitiesTextView.setText(amenitiesBuilder.toString().trim());
        } else {
            amenitiesTextView.setText("No special amenities");
        }
    }

    private void loadParkingSpots() {
        if (parkingArea == null) return;

        // Load parking spots for this area from Firestore
        firestoreManager.getParkingSpots(parkingArea.getId(),
                new FirestoreManager.GetParkingSpotsCallback() {
                    @Override
                    public void onSuccess(List<ParkingSpot> spots) {
                        parkingSpots.clear();
                        parkingSpots.addAll(spots);
                        spotAdapter.notifyDataSetChanged();

                        // Show/hide "no spots" message
                        if (spots.isEmpty()) {
                            noSpotsTextView.setVisibility(View.VISIBLE);
                            spotsRecyclerView.setVisibility(View.GONE);
                        } else {
                            noSpotsTextView.setVisibility(View.GONE);
                            spotsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ParkingDetailsActivity.this,
                                "Error loading spots: " + errorMessage, Toast.LENGTH_SHORT).show();
                        noSpotsTextView.setVisibility(View.VISIBLE);
                        spotsRecyclerView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onSpotClick(ParkingSpot spot) {
        // Update the book button state based on spot availability
        if (spot.isAvailable()) {
            bookButton.setEnabled(true);
            bookButton.setText("Book Spot " + spot.getSpotNumber());
        } else {
            bookButton.setEnabled(false);
            bookButton.setText("Spot Not Available");
        }
    }

    private void navigateToBooking(ParkingSpot spot) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("parking_area_id", parkingArea.getId());
        intent.putExtra("parking_area_name", parkingArea.getName());
        intent.putExtra("parking_spot_id", spot.getId());
        intent.putExtra("parking_spot_number", spot.getSpotNumber());
        intent.putExtra("hourly_rate", parkingArea.getHourlyRate());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}