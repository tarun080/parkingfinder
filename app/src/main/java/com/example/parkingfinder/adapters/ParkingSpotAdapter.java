package com.example.parkingfinder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkingfinder.R;
import com.example.parkingfinder.models.ParkingSpot;

import java.util.List;

public class ParkingSpotAdapter extends RecyclerView.Adapter<ParkingSpotAdapter.SpotViewHolder> {

    private Context context;
    private List<ParkingSpot> spots;
    private OnSpotClickListener listener;
    private ParkingSpot selectedSpot = null;
    private int selectedPosition = -1;

    public interface OnSpotClickListener {
        void onSpotClick(ParkingSpot spot);
    }

    public ParkingSpotAdapter(Context context, List<ParkingSpot> spots, OnSpotClickListener listener) {
        this.context = context;
        this.spots = spots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        ParkingSpot spot = spots.get(position);
        holder.spotNumberTextView.setText(spot.getSpotNumber());

        // Set card background color based on availability and selection
        if (!spot.isAvailable()) {
            // Unavailable spot
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.unavailable_spot));
            holder.spotNumberTextView.setTextColor(
                    ContextCompat.getColor(context, R.color.white));
        } else if (position == selectedPosition) {
            // Selected spot
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.selected_spot));
            holder.spotNumberTextView.setTextColor(
                    ContextCompat.getColor(context, R.color.white));
        } else {
            // Available spot
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.available_spot));
            holder.spotNumberTextView.setTextColor(
                    ContextCompat.getColor(context, R.color.black));
        }

        // Special markers for handicapped, EV charging, etc.
        if (spot.isHandicapped()) {
            holder.spotTypeTextView.setText("H");
            holder.spotTypeTextView.setVisibility(View.VISIBLE);
        } else if (spot.isElectricCharging()) {
            holder.spotTypeTextView.setText("EV");
            holder.spotTypeTextView.setVisibility(View.VISIBLE);
        } else if (spot.isReserved()) {
            holder.spotTypeTextView.setText("R");
            holder.spotTypeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.spotTypeTextView.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spot.isAvailable()) {
                    int previousSelected = selectedPosition;
                    selectedPosition = holder.getAdapterPosition();
                    selectedSpot = spot;

                    // Update UI for previously selected and newly selected spot
                    if (previousSelected != -1) {
                        notifyItemChanged(previousSelected);
                    }
                    notifyItemChanged(selectedPosition);

                    // Notify listener
                    listener.onSpotClick(spot);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    public ParkingSpot getSelectedSpot() {
        return selectedSpot;
    }

    static class SpotViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView spotNumberTextView;
        TextView spotTypeTextView;

        public SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_spot);
            spotNumberTextView = itemView.findViewById(R.id.text_view_spot_number);
            spotTypeTextView = itemView.findViewById(R.id.text_view_spot_type);
        }
    }
}