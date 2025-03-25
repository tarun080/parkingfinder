package com.example.parkingfinder.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.parkingfinder.R;

import java.text.NumberFormat;

public class FilterDialog extends Dialog {

    private boolean onlyShowAvailable;
    private double maxPrice;
    private boolean hasEVCharging;
    private boolean hasDisabledAccess;
    private FilterDialogListener listener;

    // UI components
    private SeekBar distanceSeekBar;
    private TextView distanceValueTextView;
    private SeekBar priceSeekBar;
    private TextView priceValueTextView;
    private CheckBox availableOnlyCheckBox;
    private CheckBox evChargingCheckBox;
    private CheckBox disabledAccessCheckBox;
    private Button resetButton;
    private Button applyButton;

    // Constants
    private static final int MAX_PRICE = 50; // $50 max hourly rate
    private static final int MAX_DISTANCE = 10; // 10km max radius

    public interface FilterDialogListener {
        void onFiltersApplied(boolean onlyAvailable, double maxPrice, boolean hasEV, boolean hasDisabled);
    }

    public FilterDialog(@NonNull Context context, boolean onlyShowAvailable, double maxPrice,
                        boolean hasEVCharging, boolean hasDisabledAccess, FilterDialogListener listener) {
        super(context);
        this.onlyShowAvailable = onlyShowAvailable;
        this.maxPrice = maxPrice;
        this.hasEVCharging = hasEVCharging;
        this.hasDisabledAccess = hasDisabledAccess;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_filter);

        // Initialize UI components
        distanceSeekBar = findViewById(R.id.seek_bar_distance);
        distanceValueTextView = findViewById(R.id.text_view_distance_value);
        priceSeekBar = findViewById(R.id.seek_bar_price);
        priceValueTextView = findViewById(R.id.text_view_price_value);
        availableOnlyCheckBox = findViewById(R.id.checkbox_available_only);
        evChargingCheckBox = findViewById(R.id.checkbox_ev_charging);
        disabledAccessCheckBox = findViewById(R.id.checkbox_disabled_access);
        resetButton = findViewById(R.id.button_reset_filter);
        applyButton = findViewById(R.id.button_apply_filter);

        // Set initial values
        availableOnlyCheckBox.setChecked(onlyShowAvailable);
        evChargingCheckBox.setChecked(hasEVCharging);
        disabledAccessCheckBox.setChecked(hasDisabledAccess);

        // Set price seekbar progress based on maxPrice
        int priceProgress = (maxPrice == Double.MAX_VALUE) ? MAX_PRICE : (int) Math.min(maxPrice, MAX_PRICE);
        priceSeekBar.setMax(MAX_PRICE);
        priceSeekBar.setProgress(priceProgress);
        updatePriceText(priceProgress);

        // Set distance seekbar initial values
        distanceSeekBar.setMax(MAX_DISTANCE);
        distanceSeekBar.setProgress(5); // Default to 5km
        updateDistanceText(5);

        // Setup seekbar listeners
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateDistanceText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });

        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePriceText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });

        // Setup button listeners
        resetButton.setOnClickListener(v -> resetFilters());

        applyButton.setOnClickListener(v -> {
            boolean onlyAvailable = availableOnlyCheckBox.isChecked();

            double maxPriceValue;
            if (priceSeekBar.getProgress() == MAX_PRICE) {
                maxPriceValue = Double.MAX_VALUE;
            } else {
                maxPriceValue = priceSeekBar.getProgress();
            }

            boolean hasEV = evChargingCheckBox.isChecked();
            boolean hasDisabled = disabledAccessCheckBox.isChecked();

            listener.onFiltersApplied(onlyAvailable, maxPriceValue, hasEV, hasDisabled);
            dismiss();
        });
    }

    private void updateDistanceText(int progress) {
        if (progress == 0) {
            progress = 1; // Minimum 1km radius
        }
        String distanceText = progress + " km";
        distanceValueTextView.setText(distanceText);
    }

    private void updatePriceText(int progress) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        String priceText;

        if (progress == MAX_PRICE) {
            priceText = "Any price";
        } else {
            priceText = currencyFormat.format(progress) + "/hour max";
        }

        priceValueTextView.setText(priceText);
    }

    private void resetFilters() {
        // Reset UI components
        distanceSeekBar.setProgress(5); // 5km default
        priceSeekBar.setProgress(MAX_PRICE); // Any price
        availableOnlyCheckBox.setChecked(true); // Only show available by default
        evChargingCheckBox.setChecked(false);
        disabledAccessCheckBox.setChecked(false);

        // Update text views
        updateDistanceText(5);
        updatePriceText(MAX_PRICE);
    }
}