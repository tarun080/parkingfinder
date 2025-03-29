package com.example.parkingfinder.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.parkingfinder.R;
import com.example.parkingfinder.activities.MainActivity;
import com.example.parkingfinder.utils.GoogleApiHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service implements GoogleApiHelper.ConnectionListener {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "location_notification_channel";
    private static final int NOTIFICATION_ID = 12345;

    // Configuration constants
    private static final long UPDATE_INTERVAL = 10000; // 10 seconds
    private static final long FASTEST_INTERVAL = 5000; // 5 seconds
    private static final float SMALLEST_DISPLACEMENT = 10; // 10 meters

    // Location components
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private GoogleApiHelper googleApiHelper;
    private boolean isRequestingLocationUpdates = false;

    // Intent actions
    public static final String ACTION_START_LOCATION_SERVICE = "com.example.parkingfinder.action.START_LOCATION_SERVICE";
    public static final String ACTION_STOP_LOCATION_SERVICE = "com.example.parkingfinder.action.STOP_LOCATION_SERVICE";
    public static final String ACTION_LOCATION_BROADCAST = "com.example.parkingfinder.action.LOCATION_BROADCAST";

    // Intent extras
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize GoogleApiHelper first
        googleApiHelper = new GoogleApiHelper(this, this);

        // Initialize other location components
        initializeLocationComponents();
    }

    private void initializeLocationComponents() {
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create location request using modern builder
        locationRequest = new LocationRequest.Builder(UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMinUpdateDistanceMeters(SMALLEST_DISPLACEMENT)
                .build();

        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    // Log location
                    Log.d(TAG, "New location: " + location.getLatitude() + ", " + location.getLongitude());

                    // Broadcast location update
                    broadcastLocationUpdate(location);
                }
            }
        };
    }

    private void broadcastLocationUpdate(Location location) {
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, location.getLatitude());
        intent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ACTION_START_LOCATION_SERVICE)) {
                    // Connect to Google API first, which will trigger startLocationUpdates when connected
                    startForeground(NOTIFICATION_ID, buildNotification());
                    googleApiHelper.connect();
                } else if (action.equals(ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationUpdates();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onConnected() {
        // Google API connected, now we can start location updates
        startLocationUpdates();
    }

    @Override
    public void onConnectionFailed() {
        Log.e(TAG, "Google API connection failed, trying to start location updates directly");
        // Try to start location updates directly as fallback
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        // Don't start twice
        if (isRequestingLocationUpdates) {
            return;
        }

        // Create notification channel for Oreo and above
        createNotificationChannel();

        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            stopSelf();
            return;
        }

        // Request location updates with explicit looper
        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
            isRequestingLocationUpdates = true;
            Log.d(TAG, "Location updates started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start location updates: " + e.getMessage(), e);
            stopSelf();
        }
    }

    private void stopLocationUpdates() {
        // Remove location updates
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isRequestingLocationUpdates = false;
        }

        // Disconnect Google API
        if (googleApiHelper != null) {
            googleApiHelper.disconnect();
        }

        // Stop foreground service
        stopForeground(true);
        stopSelf();

        Log.d(TAG, "Location updates stopped");
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Parking Finder Active")
                .setContentText("Looking for nearby parking spots")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            serviceChannel.setDescription("Used for tracking location to find nearby parking");
            serviceChannel.enableVibration(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Ensure we clean up resources
        stopLocationUpdates();
    }

    // Helper method for starting the service
    public static void startService(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(ACTION_START_LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    // Helper method for stopping the service
    public static void stopService(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(ACTION_STOP_LOCATION_SERVICE);
        context.startService(intent);
    }
}