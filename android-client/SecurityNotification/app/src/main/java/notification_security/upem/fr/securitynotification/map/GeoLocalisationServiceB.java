package notification_security.upem.fr.securitynotification.map;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by anis on 06/03/16.
 */
public class GeoLocalisationServiceB extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener /*, com.google.android.gms.location.LocationListener */{
    private final IBinder mBinder = new LocalBinder();
    private static final String TAG = GeoLocalisationServiceB.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    List<Map.Entry<LocationListenerFragmentActivity, Integer>> requestWaitingConnectionQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(GeoLocalisationServiceB.this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
        requestWaitingConnectionQueue = new ArrayList<>();
        mGoogleApiClient.connect();
        Log.d(TAG, "service creation");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service destroyed");
    }

    public void subscribeUpdateLocation(final LocationListenerFragmentActivity listener, final int nbUpdate){
        if(mGoogleApiClient.isConnected()){
            requestUpdateLocation(listener, nbUpdate);
        }
        else{
            requestWaitingConnectionQueue.add(new Map.Entry<LocationListenerFragmentActivity, Integer>() {
                @Override
                public LocationListenerFragmentActivity getKey() {
                    return listener;
                }

                @Override
                public Integer getValue() {
                    return nbUpdate;
                }

                @Override
                public Integer setValue(Integer object) {
                    return null;
                }
            });
        }
    }

    private void requestUpdateLocation(LocationListenerFragmentActivity listener, int nbUpdate){
        if (ActivityCompat.checkSelfPermission(GeoLocalisationServiceB.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
        LocationRequest mLocationRequest = createLocationRequest(nbUpdate);
        checkLocationSettings(listener, mLocationRequest);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, listener);
    }

    private LocationRequest createLocationRequest(int nbUpdate) {
        LocationRequest mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(10000);
        //mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(nbUpdate);
        return mLocationRequest;
    }

    private void checkLocationSettings(final LocationListenerFragmentActivity listener, LocationRequest mLocationRequest){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.
                        checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        //Intent intent = new Intent(ACTION_ASK_POSITION_LOCATION_DISABLED_RES);
                        //intent.putExtra(EXTRA_LOCATION_REQUEST_STATUS, status);
                        //sendLocalBroadcast(intent);
                        try {
                            status.startResolutionForResult(listener, 1);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        for(Map.Entry<LocationListenerFragmentActivity, Integer> request : requestWaitingConnectionQueue){
            requestUpdateLocation(request.getKey(), request.getValue());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { Log.d(TAG, "connection failed"); }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection suspended");
    }

    public class LocalBinder extends Binder {
        GeoLocalisationServiceB getService() {
            return GeoLocalisationServiceB.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /*    @Override
    public void onLocationChanged(Location location) {
        Position position = new Position(location.getLatitude(), location.getLongitude());
        Intent intent = new Intent(ACTION_ASK_POSITION_RES);
        intent.putExtra(EXTRA_POSITION,position);
        sendLocalBroadcast(intent);
    } */

    /**
     * Sends a local broadcast with the given intent.
     */
}
