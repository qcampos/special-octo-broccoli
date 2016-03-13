package notification_security.upem.fr.securitynotification.map;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by anis on 06/03/16.
 * Bound service that provide a subscribeLocationUpdate method.
 */
public class GeoLocalisationServiceB extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener /*, com.google.android.gms.location.LocationListener */{
    private final IBinder mBinder = new LocalBinder();
    private static final String TAG = GeoLocalisationServiceB.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private List<Runnable> requestWaitingConnectionQueue;

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

    public void subscribeLocationUpdate(final Activity activity, final LocationListener listener, final int nbUpdate){
        if(mGoogleApiClient.isConnected()){
            requestUpdateLocation(activity,listener, nbUpdate);
        }
        else{
            requestWaitingConnectionQueue.add(new Runnable() {
                @Override
                public void run() {
                    requestUpdateLocation(activity,listener, nbUpdate);
                }
            });
        }
    }

    private void requestUpdateLocation(Activity activity, LocationListener listener, int nbUpdate){
        Log.d(TAG, "requested");
        if (ActivityCompat.checkSelfPermission(GeoLocalisationServiceB.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"shit");
            return ;
        }
        LocationRequest mLocationRequest = createLocationRequest(nbUpdate);
        checkLocationSettings(activity, mLocationRequest);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, listener);
    }


    private LocationRequest createLocationRequest(int nbUpdate) {
        LocationRequest mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(10000);
        //mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(nbUpdate > 0)
            mLocationRequest.setNumUpdates(nbUpdate);
        return mLocationRequest;
    }

    private void checkLocationSettings(final Activity activity, LocationRequest mLocationRequest){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.
                        checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
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
                            status.startResolutionForResult(activity, 1);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.d(TAG, "GPS unavailable");
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "connected");
        for(Runnable request : requestWaitingConnectionQueue){
            request.run();
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

}
