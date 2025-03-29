package com.example.parkingfinder.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.parkingfinder.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

public class SimpleMapFragment extends Fragment {

    private static final String TAG = "SimpleMapFragment";
    private MapView map = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load OSMDroid configuration
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Log.d(TAG, "OSMDroid configuration loaded");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_simple_map, container, false);

        try {
            // Set up the map
            map = view.findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);

            // Set initial position (default to a known location)
            GeoPoint startPoint = new GeoPoint(37.7749, -122.4194); // San Francisco
            map.getController().setZoom(16.0);
            map.getController().setCenter(startPoint);

            // Add compass
            CompassOverlay compassOverlay = new CompassOverlay(getContext(),
                    new InternalCompassOrientationProvider(getContext()), map);
            compassOverlay.enableCompass();
            map.getOverlays().add(compassOverlay);

            Log.d(TAG, "Map setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map: " + e.getMessage(), e);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}