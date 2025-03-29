package com.example.parkingfinder.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Helper class to manage Google API Client connections
 */
public class GoogleApiHelper {
    private static final String TAG = "GoogleApiHelper";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private ConnectionListener mListener;

    public interface ConnectionListener {
        void onConnected();
        void onConnectionFailed();
    }

    public GoogleApiHelper(@NonNull Context context, ConnectionListener listener) {
        this.mContext = context.getApplicationContext();
        this.mListener = listener;
        buildGoogleApiClient();
    }

    private synchronized void buildGoogleApiClient() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(android.os.Bundle bundle) {
                            Log.d(TAG, "Google API Client connected");
                            if (mListener != null) {
                                mListener.onConnected();
                            }
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(TAG, "Google API Client connection suspended");
                        }
                    })
                    .addOnConnectionFailedListener(connectionResult -> {
                        Log.e(TAG, "Google API Client connection failed: " + connectionResult.getErrorMessage());
                        if (mListener != null) {
                            mListener.onConnectionFailed();
                        }
                    })
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Error building GoogleApiClient: " + e.getMessage(), e);
        }
    }

    public void connect() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Check if Google Play Services is available and up to date
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }
}