package com.example.parkingfinder.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkingfinder.R;
import com.example.parkingfinder.models.ParkingArea;
import com.example.parkingfinder.utils.PermissionUtils;
import com.example.parkingfinder.viewmodels.MapViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements Marker.OnMarkerClickListener {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    private MapViewModel mapViewModel;
    private Map<Marker, ParkingArea> markerParkingAreaMap = new HashMap<>();

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize OSMDroid configuration
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize map view
        map = rootView.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Add rotation support
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(map);
        rotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(rotationGestureOverlay);

        // Add my location overlay
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);

        // Get map controller
        mapController = map.getController();
        mapController.setZoom(15.0);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize view model
        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Observe parking areas
        mapViewModel.getParkingAreas().observe(getViewLifecycleOwner(), new Observer<List<ParkingArea>>() {
            @Override
            public void onChanged(List<ParkingArea> parkingAreas) {
                displayParkingAreas(parkingAreas);
            }
        });

        // Check for location permission
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (PermissionUtils.hasLocationPermission(requireContext())) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocationOverlay.enableMyLocation();
            getLastKnownLocation();
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location
                        if (location != null) {
                            // Move camera to user's location
                            GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                            mapController.setCenter(userLocation);

                            // Load nearby parking areas
                            mapViewModel.loadNearbyParkingAreas(location.getLatitude(), location.getLongitude(), 5.0);
                        }
                    }
                });
    }

    private void displayParkingAreas(List<ParkingArea> parkingAreas) {
        if (map == null) return;

        // Clear existing markers
        for (Marker marker : markerParkingAreaMap.keySet()) {
            map.getOverlays().remove(marker);
        }
        markerParkingAreaMap.clear();

        for (ParkingArea parkingArea : parkingAreas) {
            GeoPoint position = new GeoPoint(parkingArea.getLatitude(), parkingArea.getLongitude());

            Marker marker = new Marker(map);
            marker.setPosition(position);
            marker.setTitle(parkingArea.getName());
            marker.setSnippet("Available: " + parkingArea.getAvailableSpots() + "/" + parkingArea.getTotalSpots());

            // Customize marker based on available spots
            if (parkingArea.getAvailableSpots() > 0) {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_marker_available));
            } else if (parkingArea.getAvailableSpots() <= 5) {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_marker_limited));
            } else {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_marker_unavailable));
            }

            marker.setOnMarkerClickListener(this);
            map.getOverlays().add(marker);
            markerParkingAreaMap.put(marker, parkingArea);
        }

        map.invalidate(); // Refresh map view
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        ParkingArea parkingArea = markerParkingAreaMap.get(marker);
        if (parkingArea != null) {
            // Navigate to parking details
            mapViewModel.selectParkingArea(parkingArea);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show nearby parking", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // This is needed for OSMDroid
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // This is needed for OSMDroid
        map.onPause();
    }
}