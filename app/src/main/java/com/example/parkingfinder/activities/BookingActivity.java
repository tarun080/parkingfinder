package com.example.parkingfinder.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.parkingfinder.R;
import com.example.parkingfinder.firebase.FirebaseAuthManager;
import com.example.parkingfinder.firebase.FirestoreManager;
import com.example.parkingfinder.models.Booking;
import com.example.parkingfinder.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BookingActivity extends AppCompatActivity {

    private FirebaseAuthManager authManager;
    private FirestoreManager firestoreManager;

    // Intent extras
    private String parkingAreaId;
    private String parkingAreaName;
    private String parkingSpotId;
    private String parkingSpotNumber;
    private double hourlyRate;

    // Date and time variables
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    // UI components
    private TextView parkingNameTextView;
    private TextView spotNumberTextView;
    private TextView hourlyRateTextView;
    private TextView startDateTimeTextView;
    private TextView endDateTimeTextView;
    private Button startDatePickerButton;
    private Button startTimePickerButton;
    private Button endDatePickerButton;
    private Button endTimePickerButton;
    private TextView durationTextView;
    private TextView totalCostTextView;
    private EditText vehicleRegistrationEditText;
    private Button confirmBookingButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize Firebase managers
        authManager = FirebaseAuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Book Parking");

        // Get intent extras
        if (getIntent().hasExtra("parking_area_id") && getIntent().hasExtra("parking_spot_id")) {
            parkingAreaId = getIntent().getStringExtra("parking_area_id");
            parkingAreaName = getIntent().getStringExtra("parking_area_name");
            parkingSpotId = getIntent().getStringExtra("parking_spot_id");
            parkingSpotNumber = getIntent().getStringExtra("parking_spot_number");
            hourlyRate = getIntent().getDoubleExtra("hourly_rate", 0);
        } else {
            Toast.makeText(this, "Error: Missing parking information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        parkingNameTextView = findViewById(R.id.text_view_parking_name);
        spotNumberTextView = findViewById(R.id.text_view_spot_number);
        hourlyRateTextView = findViewById(R.id.text_view_hourly_rate);
        startDateTimeTextView = findViewById(R.id.text_view_start_datetime);
        endDateTimeTextView = findViewById(R.id.text_view_end_datetime);
        startDatePickerButton = findViewById(R.id.button_start_date);
        startTimePickerButton = findViewById(R.id.button_start_time);
        endDatePickerButton = findViewById(R.id.button_end_date);
        endTimePickerButton = findViewById(R.id.button_end_time);
        durationTextView = findViewById(R.id.text_view_duration);
        totalCostTextView = findViewById(R.id.text_view_total_cost);
        vehicleRegistrationEditText = findViewById(R.id.edit_text_vehicle_registration);
        confirmBookingButton = findViewById(R.id.button_confirm_booking);
        progressBar = findViewById(R.id.progress_bar);

        // Set initial data
        parkingNameTextView.setText(parkingAreaName);
        spotNumberTextView.setText("Spot " + parkingSpotNumber);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        hourlyRateTextView.setText(currencyFormat.format(hourlyRate) + "/hour");

        // Set default date/time (starting now, ending in 2 hours)
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.HOUR_OF_DAY, 2);

        updateStartDateTimeText();
        updateEndDateTimeText();
        updateDurationAndCost();

        // Set click listeners for date/time pickers
        startDatePickerButton.setOnClickListener(v -> showStartDatePicker());
        startTimePickerButton.setOnClickListener(v -> showStartTimePicker());
        endDatePickerButton.setOnClickListener(v -> showEndDatePicker());
        endTimePickerButton.setOnClickListener(v -> showEndTimePicker());

        // Set click listener for confirm button
        confirmBookingButton.setOnClickListener(v -> confirmBooking());
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        startCalendar.set(Calendar.YEAR, year);
                        startCalendar.set(Calendar.MONTH, month);
                        startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateStartDateTimeText();
                        updateDurationAndCost();
                    }
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showStartTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startCalendar.set(Calendar.MINUTE, minute);
                        updateStartDateTimeText();
                        updateDurationAndCost();
                    }
                },
                startCalendar.get(Calendar.HOUR_OF_DAY),
                startCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        endCalendar.set(Calendar.YEAR, year);
                        endCalendar.set(Calendar.MONTH, month);
                        endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateEndDateTimeText();
                        updateDurationAndCost();
                    }
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showEndTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endCalendar.set(Calendar.MINUTE, minute);
                        updateEndDateTimeText();
                        updateDurationAndCost();
                    }
                },
                endCalendar.get(Calendar.HOUR_OF_DAY),
                endCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateStartDateTimeText() {
        startDateTimeTextView.setText(DateTimeUtils.formatDateTime(startCalendar.getTime()));
    }

    private void updateEndDateTimeText() {
        endDateTimeTextView.setText(DateTimeUtils.formatDateTime(endCalendar.getTime()));
    }

    private void updateDurationAndCost() {
        // Ensure end time is after start time
        if (endCalendar.before(startCalendar)) {
            endCalendar.setTime(startCalendar.getTime());
            endCalendar.add(Calendar.HOUR_OF_DAY, 1);
            updateEndDateTimeText();
        }

        // Calculate duration in hours
        long durationMillis = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        double durationHours = durationMillis / (1000.0 * 60 * 60);

        // Format duration string
        int hours = (int) durationHours;
        int minutes = (int) ((durationHours - hours) * 60);
        String durationText = hours + " hours";
        if (minutes > 0) {
            durationText += ", " + minutes + " minutes";
        }
        durationTextView.setText(durationText);

        // Calculate total cost
        double totalCost = durationHours * hourlyRate;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        totalCostTextView.setText(currencyFormat.format(totalCost));
    }

    private void confirmBooking() {
        // Validate input
        String vehicleReg = vehicleRegistrationEditText.getText().toString().trim();
        if (TextUtils.isEmpty(vehicleReg)) {
            vehicleRegistrationEditText.setError("Please enter your vehicle registration");
            return;
        }

        // Verify user is logged in
        if (!authManager.isUserLoggedIn()) {
            Toast.makeText(this, "Please log in to book a parking spot", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        confirmBookingButton.setEnabled(false);

        // Create booking object
        Booking booking = new Booking(
                authManager.getCurrentUser().getUid(),
                parkingAreaId,
                parkingSpotId,
                parkingAreaName,
                parkingSpotNumber,
                startCalendar.getTime(),
                endCalendar.getTime(),
                calculateTotalCost()
        );
        booking.setVehicleRegistration(vehicleReg);
        booking.setConfirmationCode(generateConfirmationCode());

        // Save booking to Firestore
        firestoreManager.createBooking(booking, new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingActivity.this, "Booking confirmed!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                confirmBookingButton.setEnabled(true);
                Toast.makeText(BookingActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private double calculateTotalCost() {
        long durationMillis = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        double durationHours = durationMillis / (1000.0 * 60 * 60);
        return durationHours * hourlyRate;
    }

    private String generateConfirmationCode() {
        // Generate a random confirmation code
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}