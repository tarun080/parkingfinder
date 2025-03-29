package com.example.parkingfinder.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for location-related operations
 */
public class LocationUtils {

    private static final String TAG = "LocationUtils";
    private static final double EARTH_RADIUS = 6371; // Radius of the earth in km

    /**
     * Check if location permissions are granted
     */
    public static boolean hasLocationPermissions(Context context) {
        if (context == null) return false;

        boolean hasFineLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean hasCoarseLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return hasFineLocation || hasCoarseLocation;
    }

    /**
     * Check if background location permission is granted
     */
    public static boolean hasBackgroundLocationPermission(Context context) {
        if (context == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        // Before Android Q, background location was included in normal location permissions
        return hasLocationPermissions(context);
    }

    /**
     * Check if storage permissions are granted for OSMDroid tile cache
     */
    public static boolean hasStoragePermission(Context context) {
        if (context == null) return false;

        // For Android 10+ (Q), we don't need external storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        return ActivityCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get the last known location asynchronously
     */
    public static void getLastLocation(Context context, final LocationCallback callback) {
        if (context == null || callback == null) return;

        if (!hasLocationPermissions(context)) {
            callback.onError("Location permission not granted");
            return;
        }

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                callback.onLocationReceived(location);
                            } else {
                                callback.onError("Location not available");
                            }
                        }
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } catch (SecurityException e) {
            callback.onError("Security exception: " + e.getMessage());
        }
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in km
    }

    /**
     * Calculate distance between two locations
     */
    public static double calculateDistance(Location location1, Location location2) {
        if (location1 == null || location2 == null) return -1;

        return calculateDistance(
                location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude());
    }

    /**
     * Calculate distance between a location and a GeoPoint
     */
    public static double calculateDistance(Location location, GeoPoint point) {
        if (location == null || point == null) return -1;

        return calculateDistance(
                location.getLatitude(), location.getLongitude(),
                point.getLatitude(), point.getLongitude());
    }

    /**
     * Calculate distance between two GeoPoints
     */
    public static double calculateDistance(GeoPoint point1, GeoPoint point2) {
        if (point1 == null || point2 == null) return -1;

        return calculateDistance(
                point1.getLatitude(), point1.getLongitude(),
                point2.getLatitude(), point2.getLongitude());
    }

    /**
     * Convert Location to GeoPoint
     */
    public static GeoPoint locationToGeoPoint(Location location) {
        if (location == null) return null;
        return new GeoPoint(location.getLatitude(), location.getLongitude());
    }

    /**
     * Get address from location coordinates (Geocoding)
     */
    public static void getAddressFromLocation(Context context, double latitude, double longitude,
                                              final GeocodingCallback callback) {
        if (context == null || callback == null) return;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                callback.onAddressReceived(address);
            } else {
                callback.onError("No address found");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed: " + e.getMessage());
            callback.onError("Geocoding failed: " + e.getMessage());
        }
    }

    /**
     * Get coordinates from address (Reverse Geocoding)
     */
    public static void getLocationFromAddress(Context context, String addressString,
                                              final GeocodingCallback callback) {
        if (context == null || addressString == null || callback == null) return;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                callback.onAddressReceived(address);
            } else {
                callback.onError("No location found for address");
            }
        } catch (IOException e) {
            Log.e(TAG, "Reverse geocoding failed: " + e.getMessage());
            callback.onError("Reverse geocoding failed: " + e.getMessage());
        }
    }

    /**
     * Format address into a readable string
     */
    public static String formatAddress(Address address) {
        if (address == null) return "";

        StringBuilder sb = new StringBuilder();

        // Add street address
        String streetNumber = address.getSubThoroughfare();
        String street = address.getThoroughfare();

        if (streetNumber != null && !streetNumber.isEmpty()) {
            sb.append(streetNumber).append(" ");
        }

        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }

        // Add city, state and postal code
        String city = address.getLocality();
        String state = address.getAdminArea();
        String postalCode = address.getPostalCode();

        if ((city != null && !city.isEmpty()) ||
                (state != null && !state.isEmpty()) ||
                (postalCode != null && !postalCode.isEmpty())) {

            if (sb.length() > 0) {
                sb.append(", ");
            }

            if (city != null && !city.isEmpty()) {
                sb.append(city);

                if (state != null && !state.isEmpty()) {
                    sb.append(", ");
                }
            }

            if (state != null && !state.isEmpty()) {
                sb.append(state);

                if (postalCode != null && !postalCode.isEmpty()) {
                    sb.append(" ");
                }
            }

            if (postalCode != null && !postalCode.isEmpty()) {
                sb.append(postalCode);
            }
        }

        // If no detailed address is available, use a more general description
        if (sb.length() == 0) {
            String featureName = address.getFeatureName();
            String countryName = address.getCountryName();

            if (featureName != null && !featureName.isEmpty() &&
                    !featureName.equals(streetNumber)) {
                sb.append(featureName);

                if (countryName != null && !countryName.isEmpty()) {
                    sb.append(", ");
                }
            }

            if (countryName != null && !countryName.isEmpty()) {
                sb.append(countryName);
            }
        }

        return sb.toString();
    }

    /**
     * Create an intent to open an external maps application with navigation
     * Works with multiple map apps including OSMAnd, Maps.me, and Google Maps
     */
    public static Intent createNavigationIntent(double latitude, double longitude) {
        // Generic geo intent that should work with most mapping apps
        Uri geoUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);

        return mapIntent;
    }

    /**
     * Create an intent to open a maps application showing a location
     * Works with multiple map apps
     */
    public static Intent createMapViewIntent(double latitude, double longitude, String label) {
        Uri geoUri;
        if (label != null && !label.isEmpty()) {
            geoUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + Uri.encode(label) + ")");
        } else {
            geoUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
        }
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);

        return mapIntent;
    }

    /**
     * Create an intent to open OSMAnd with navigation if installed
     */
    public static Intent createOSMAndNavigationIntent(double latitude, double longitude, String name) {
        Uri osmUri = Uri.parse("osmand.navigation:q=" + latitude + "," + longitude);
        if (name != null && !name.isEmpty()) {
            osmUri = Uri.parse("osmand.navigation:q=" + latitude + "," + longitude + "&name=" + Uri.encode(name));
        }
        Intent osmIntent = new Intent(Intent.ACTION_VIEW, osmUri);
        osmIntent.setPackage("net.osmand");

        return osmIntent;
    }

    /**
     * Create an intent to open app settings to enable location
     */
    public static Intent createLocationSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    /**
     * Format a distance in kilometers to a readable string
     */
    public static String formatDistance(double distanceInKm) {
        if (distanceInKm < 0) return "Unknown distance";

        if (distanceInKm < 1) {
            int meters = (int) (distanceInKm * 1000);
            return meters + " m";
        } else if (distanceInKm < 10) {
            return String.format(Locale.getDefault(), "%.1f km", distanceInKm);
        } else {
            return String.format(Locale.getDefault(), "%.0f km", distanceInKm);
        }
    }

    /**
     * Callback interface for location operations
     */
    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onError(String message);
    }

    /**
     * Callback interface for geocoding operations
     */
    public interface GeocodingCallback {
        void onAddressReceived(Address address);
        void onError(String message);
    }
}