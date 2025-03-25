package com.example.parkingfinder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkingfinder.R;
import com.example.parkingfinder.models.Booking;
import com.example.parkingfinder.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookings;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
        void onCancelBooking(Booking booking);
        void onExtendBooking(Booking booking);
    }

    public BookingHistoryAdapter(Context context, List<Booking> bookings, OnBookingClickListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        // Set parking info
        holder.parkingNameTextView.setText(booking.getParkingAreaName());
        holder.spotNumberTextView.setText("Spot " + booking.getParkingSpotNumber());

        // Set date and time
        holder.dateTextView.setText(DateTimeUtils.formatDate(booking.getStartTime()));
        String timeText = DateTimeUtils.formatTime(booking.getStartTime()) + " - " +
                DateTimeUtils.formatTime(booking.getEndTime());
        holder.timeTextView.setText(timeText);

        // Set duration and price
        double duration = booking.getDurationInHours();
        int hours = (int) duration;
        int minutes = (int) ((duration - hours) * 60);

        String durationText = hours + " hours";
        if (minutes > 0) {
            durationText += ", " + minutes + " minutes";
        }
        holder.durationTextView.setText(durationText);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        holder.priceTextView.setText(currencyFormat.format(booking.getTotalCost()));

        // Set status and style based on status
        holder.statusTextView.setText(booking.getStatus());

        switch (booking.getStatus()) {
            case "PENDING":
                holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.pending));
                break;
            case "CONFIRMED":
                holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.confirmed));
                break;
            case "ACTIVE":
                holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.active));
                break;
            case "COMPLETED":
                holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.completed));
                break;
            case "CANCELLED":
                holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.cancelled));
                break;
        }

        // Set vehicle registration
        if (booking.getVehicleRegistration() != null && !booking.getVehicleRegistration().isEmpty()) {
            holder.vehicleRegTextView.setText(booking.getVehicleRegistration());
            holder.vehicleRegTextView.setVisibility(View.VISIBLE);
        } else {
            holder.vehicleRegTextView.setVisibility(View.GONE);
        }

        // Setup buttons based on status and time
        setupActionButtons(holder, booking);

        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    private void setupActionButtons(BookingViewHolder holder, Booking booking) {
        // Hide both buttons by default
        holder.cancelButton.setVisibility(View.GONE);
        holder.extendButton.setVisibility(View.GONE);

        Date now = new Date();

        // Check if booking is in the future
        if ("PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
            // For pending or confirmed bookings, show cancel button
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking);
                }
            });
        } else if ("ACTIVE".equals(booking.getStatus())) {
            // For active bookings, show both buttons
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.extendButton.setVisibility(View.VISIBLE);

            holder.cancelButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking);
                }
            });

            holder.extendButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExtendBooking(booking);
                }
            });
        }

        // If end time is in the past, hide action buttons
        if (booking.getEndTime().before(now)) {
            holder.cancelButton.setVisibility(View.GONE);
            holder.extendButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateData(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView parkingNameTextView;
        TextView spotNumberTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView durationTextView;
        TextView priceTextView;
        TextView statusTextView;
        TextView vehicleRegTextView;
        Button cancelButton;
        Button extendButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingNameTextView = itemView.findViewById(R.id.text_view_parking_name);
            spotNumberTextView = itemView.findViewById(R.id.text_view_spot_number);
            dateTextView = itemView.findViewById(R.id.text_view_date);
            timeTextView = itemView.findViewById(R.id.text_view_time);
            durationTextView = itemView.findViewById(R.id.text_view_duration);
            priceTextView = itemView.findViewById(R.id.text_view_price);
            statusTextView = itemView.findViewById(R.id.text_view_status);
            vehicleRegTextView = itemView.findViewById(R.id.text_view_vehicle_reg);
            cancelButton = itemView.findViewById(R.id.button_cancel);
            extendButton = itemView.findViewById(R.id.button_extend);
        }
    }
}