package com.example.parkingfinder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parkingfinder.R;
import com.example.parkingfinder.models.ParkingArea;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ParkingListAdapter extends RecyclerView.Adapter<ParkingListAdapter.ParkingViewHolder> {

    private Context context;
    private List<ParkingArea> parkingAreas;
    private OnParkingItemClickListener listener;
    private double userLatitude;
    private double userLongitude;

    public interface OnParkingItemClickListener {
        void onParkingItemClick(ParkingArea parkingArea);
        void onBookButtonClick(ParkingArea parkingArea);
        void onFavoriteClick(ParkingArea parkingArea, boolean isFavorite);
    }

    public ParkingListAdapter(Context context, List<ParkingArea> parkingAreas, OnParkingItemClickListener listener) {
        this.context = context;
        this.parkingAreas = parkingAreas;
        this.listener = listener;
    }

    public void setUserLocation(double latitude, double longitude) {
        this.userLatitude = latitude;
        this.userLongitude = longitude;
        notifyDataSetChanged(); // Refresh to update distances
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingArea parkingArea = parkingAreas.get(position);

        // Set parking name and address
        holder.nameTextView.setText(parkingArea.getName());
        holder.addressTextView.setText(parkingArea.getAddress());

        // Set availability
        String availabilityText = parkingArea.getAvailableSpots() + "/" + parkingArea.getTotalSpots() + " spots available";
        holder.availabilityTextView.setText(availabilityText);

        // Set price
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        holder.priceTextView.setText(currencyFormat.format(parkingArea.getHourlyRate()) + "/hour");

        // Set rating
        String ratingText = String.format(Locale.getDefault(), "%.1f", parkingArea.getRating());
        holder.ratingTextView.setText(ratingText);
        holder.ratingBar.setRating(parkingArea.getRating());

        // Set distance (if user location is available)
        if (userLatitude != 0 && userLongitude != 0) {
            double distance = parkingArea.distanceFrom(userLatitude, userLongitude);
            String distanceText = String.format(Locale.getDefault(), "%.1f km", distance);
            holder.distanceTextView.setText(distanceText);
            holder.distanceTextView.setVisibility(View.VISIBLE);
        } else {
            holder.distanceTextView.setVisibility(View.GONE);
        }

        // Load parking image
        if (parkingArea.getImageUrl() != null && !parkingArea.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(parkingArea.getImageUrl())
                    .placeholder(R.drawable.placeholder_parking)
                    .error(R.drawable.placeholder_parking)
                    .centerCrop()
                    .into(holder.parkingImageView);
        } else {
            holder.parkingImageView.setImageResource(R.drawable.placeholder_parking);
        }

        // Set available status color
        if (parkingArea.getAvailableSpots() > 0) {
            holder.availabilityTextView.setTextColor(context.getResources().getColor(R.color.available));
            holder.bookButton.setEnabled(true);
        } else {
            holder.availabilityTextView.setTextColor(context.getResources().getColor(R.color.unavailable));
            holder.bookButton.setEnabled(false);
        }

        // Set favorite icon
        if (parkingArea.isFavorite()) {
            holder.favoriteImageView.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.favoriteImageView.setImageResource(R.drawable.ic_favorite_border);
        }

        // Special features indicators
        if (parkingArea.isHasCoveredParking()) {
            holder.coveredParkingImageView.setVisibility(View.VISIBLE);
        } else {
            holder.coveredParkingImageView.setVisibility(View.GONE);
        }

        if (parkingArea.isHasDisabledAccess()) {
            holder.disabledAccessImageView.setVisibility(View.VISIBLE);
        } else {
            holder.disabledAccessImageView.setVisibility(View.GONE);
        }

        if (parkingArea.isHasElectricCharging()) {
            holder.evChargingImageView.setVisibility(View.VISIBLE);
        } else {
            holder.evChargingImageView.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onParkingItemClick(parkingArea);
            }
        });

        holder.bookButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookButtonClick(parkingArea);
            }
        });

        holder.favoriteImageView.setOnClickListener(v -> {
            if (listener != null) {
                boolean newFavoriteState = !parkingArea.isFavorite();
                // Update UI immediately for better responsiveness
                if (newFavoriteState) {
                    holder.favoriteImageView.setImageResource(R.drawable.ic_favorite_filled);
                } else {
                    holder.favoriteImageView.setImageResource(R.drawable.ic_favorite_border);
                }
                parkingArea.setFavorite(newFavoriteState);
                listener.onFavoriteClick(parkingArea, newFavoriteState);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parkingAreas.size();
    }

    public void updateData(List<ParkingArea> newParkingAreas) {
        this.parkingAreas = newParkingAreas;
        notifyDataSetChanged();
    }

    static class ParkingViewHolder extends RecyclerView.ViewHolder {
        ImageView parkingImageView;
        TextView nameTextView;
        TextView addressTextView;
        TextView availabilityTextView;
        TextView priceTextView;
        TextView ratingTextView;
        android.widget.RatingBar ratingBar;
        TextView distanceTextView;
        Button bookButton;
        ImageView favoriteImageView;
        ImageView coveredParkingImageView;
        ImageView disabledAccessImageView;
        ImageView evChargingImageView;

        public ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingImageView = itemView.findViewById(R.id.image_view_parking);
            nameTextView = itemView.findViewById(R.id.text_view_name);
            addressTextView = itemView.findViewById(R.id.text_view_address);
            availabilityTextView = itemView.findViewById(R.id.text_view_availability);
            priceTextView = itemView.findViewById(R.id.text_view_price);
            ratingTextView = itemView.findViewById(R.id.text_view_rating);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            distanceTextView = itemView.findViewById(R.id.text_view_distance);
            bookButton = itemView.findViewById(R.id.button_book);
            favoriteImageView = itemView.findViewById(R.id.image_view_favorite);
            coveredParkingImageView = itemView.findViewById(R.id.image_view_covered);
            disabledAccessImageView = itemView.findViewById(R.id.image_view_disabled);
            evChargingImageView = itemView.findViewById(R.id.image_view_ev);
        }
    }
}