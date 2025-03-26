package com.example.parkingfinder.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MapViewModel mapViewModel;
    private Map<Marker, ParkingArea> markerParkingAreaMap = new HashMap<>();

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize view model
        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Observe parking areas
        mapViewModel.getParkingAreas().observe(getViewLifecycleOwner(), new Observer<List<ParkingArea>>() {
            @Override
            public void onChanged(List<ParkingArea> parkingAreas) {
                displayParkingAreas(parkingAreas);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set custom map style if needed
        try {
            // Use ResourcesCompat to load the raw resource
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(), R.raw.map_style
                    )
            );

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Set up map UI settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Set marker click listener
        mMap.setOnMarkerClickListener(this);

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
            mMap.setMyLocationEnabled(true);
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
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                            // Load nearby parking areas
                            mapViewModel.loadNearbyParkingAreas(location.getLatitude(), location.getLongitude(), 5.0);
                        }
                    }
                });
    }

    private void displayParkingAreas(List<ParkingArea> parkingAreas) {
        if (mMap == null) return;

        // Clear existing markers
        mMap.clear();
        markerParkingAreaMap.clear();

        for (ParkingArea parkingArea : parkingAreas) {
            LatLng position = new LatLng(parkingArea.getLatitude(), parkingArea.getLongitude());

            // Customize marker based on available spots
            float markerColor = BitmapDescriptorFactory.HUE_RED;
            if (parkingArea.getAvailableSpots() > 0) {
                markerColor = BitmapDescriptorFactory.HUE_GREEN;
            } else if (parkingArea.getAvailableSpots() <= 5) {
                markerColor = BitmapDescriptorFactory.HUE_ORANGE;
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(parkingArea.getName())
                    .snippet("Available: " + parkingArea.getAvailableSpots() + "/" + parkingArea.getTotalSpots())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

            Marker marker = mMap.addMarker(markerOptions);
            markerParkingAreaMap.put(marker, parkingArea);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
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
}