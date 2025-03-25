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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {

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
        initializeLocationComponents();
    }

    private void initializeLocationComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create location request
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setSmallestDisplacement(SMALLEST_DISPLACEMENT);

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
                    startLocationUpdates();
                } else if (action.equals(ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationUpdates();
                }
            }
        }
        return START_STICKY;
    }

    private void startLocationUpdates() {
        // Create notification channel for Oreo and above
        createNotificationChannel();

        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, buildNotification());

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Log.d(TAG, "Location updates started");
    }

    private void stopLocationUpdates() {
        // Remove location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);

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
            manager.createNotificationChannel(serviceChannel);
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

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
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