package notification_security.upem.fr.securitynotification.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import notification_security.upem.fr.securitynotification.R;

public class MapsActivity extends LocationListenerFragmentActivity implements OnMapReadyCallback{

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private GeoLocalisationServiceB geoLocalisationServiceB;
    private boolean mBound;
    private int longitude;
    private AlertDialog dialog;
    private NetworkServiceReceiver serviceReceiver;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GeoLocalisationServiceB.LocalBinder binder = (GeoLocalisationServiceB.LocalBinder) service;
            geoLocalisationServiceB = binder.getService();
            geoLocalisationServiceB.subscribeUpdateLocation(MapsActivity.this, 1);
            serviceReceiver = new NetworkServiceReceiver();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createDialog();

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindGeoLocalisationService();
        Log.d(TAG, "onstrat");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerNetworkServiceReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterNetworkServiceReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unBindGeoLocalisationService();
    }

    private void bindGeoLocalisationService(){
        Intent intent = new Intent(this, GeoLocalisationServiceB.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unBindGeoLocalisationService(){
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                addMarker(new Alert("ici c'est paris ", 48.8, 2.3 + longitude));
                longitude++;
                try {
                    dialog.setMessage(translateCoordinateToAdress(marker.getPosition()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.show();
                return true;
            }
        });

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(new LatLng(47, 2.5)).title("Major alert")).setSnippet("oh mon snippet");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));

    }

    private void addMarker(Alert alert) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(alert.lat, alert.lng)).title(alert.title));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(alert.lat, alert.lng), 12));
    }

    private void addMarkers(List<Alert> alerts) {
        for (Alert alert : alerts) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(alert.lat, alert.lng)).title(alert.title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(alert.lat, alert.lng), 12));
        }
    }

    private void moveCamera(Position position ){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(position.latitude, position.longitude), 14));
    }

    @Override
    public void onLocationChanged(Location location) {
        Position position = new Position(location.getLatitude(),location.getLongitude());
        Log.d(TAG, position.toString());
        moveCamera(position);
    }

    /**
     * create a  dialog box
     */
    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Je m'y trouve", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "User clicked OK button");
            }
        });
        builder.setNegativeButton("Fausse alerte", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "User cancelled");
            }
        });

        dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("red"));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MapsActivity.this, R.color.yellowGreen));
            }
        });
    }

    private String translateCoordinateToAdress(LatLng latLng) throws IOException {
        Geocoder geocoder;
        List<Address> yourAddresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        yourAddresses= geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        if (yourAddresses.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i<=yourAddresses.get(0).getMaxAddressLineIndex(); i++){
                sb.append(yourAddresses.get(0).getAddressLine(i));
                if(i < yourAddresses.get(0).getMaxAddressLineIndex()){
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        else{
            return "Weird place";
        }
    }

    private void registerNetworkServiceReceiver() {
        IntentFilter filter = createHomeFilters();
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, filter);
    }

    private void unregisterNetworkServiceReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver);
    }

    @NonNull
    private static IntentFilter createHomeFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("");
        return filter;
    }

    public class NetworkServiceReceiver extends BroadcastReceiver {

        private final String TAG = NetworkServiceReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case "" :
                    break;
                default :
                    break;
            }
        }
    }

    private void oldButGold(){
            /*
            private GeoLocalisationServiceReceiver geoLocalisationServiceReceiver;

    public class GeoLocalisationServiceReceiver extends BroadcastReceiver {

    @NonNull
    private static IntentFilter createHomeFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GeoLocalisationService.ACTION_ASK_POSITION_LOCATION_DISABLED_RES);
        filter.addAction(GeoLocalisationService.ACTION_ASK_POSITION_RES);
        return filter;
    }
        private final String TAG = GeoLocalisationServiceReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case GeoLocalisationService.ACTION_ASK_POSITION_RES :
                    Position position = (Position) intent.getSerializableExtra(GeoLocalisationService.EXTRA_POSITION);
                    moveCamera(position);
                    break;
                case GeoLocalisationService.ACTION_ASK_POSITION_LOCATION_DISABLED_RES:
                    Status status = intent.getParcelableExtra(GeoLocalisationService.EXTRA_LOCATION_REQUEST_STATUS);
                    askLocationSetting(status);
                default :
                    break;
            }
        }
    }

    private void askLocationSetting(Status status){
        try {
            status.startResolutionForResult(this,1);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connection failed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection suspended");
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://upem.fr.securitynotification/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);

               createLocationRequest();
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    MapsActivity.this.runOnUiThread((new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapsActivity.this, String.valueOf(mLastLocation.getLatitude()) + " , " + String.valueOf(mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 12));
                        }
                    }));
                    Log.d("marker position ", mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
                } else {
                    Toast.makeText(MapsActivity.this, "Can't find location. ", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MapsActivity.this, "Probably because you disabled the wifi or the gps. Moron. ", Toast.LENGTH_LONG).show();
                    Toast.makeText(MapsActivity.this, "Please enable location.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MapsActivity.this, "Moron.", Toast.LENGTH_SHORT).show();
                }
                protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.d("onResult", "everythoing is satisfied");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    0x1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
        return mLocationRequest;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("tag" , "going to request permission");
            ActivityCompat.requestPermissions( MapsActivity.this, new String[] {  Manifest.permission.ACCESS_COARSE_LOCATION  },
                    1);
            Log.d("tag", "permission requested");
        }
        /*
        Log.d("onConnected", mLastLocation + " ");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("onConnected", mLastLocation + " "); */
        //LocationRequest mLocationRequest = createLocationRequest();
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }


  /*  @Override
    public void onLocationChanged(Location loc) {
//      editLocation.setText("");
//      View pb;
//      pb.setVisibility(View.INVISIBLE);
        Toast.makeText(
                getBaseContext(),
                "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                        + loc.getLongitude(), Toast.LENGTH_SHORT).show();
        String longitude = "Longitude: " + loc.getLongitude();
        Log.d("longitude : ", longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.d("latitude", latitude);

        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                + cityName;
    }

        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                /*if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("tag" , "going to request permission");
                    ActivityCompat.requestPermissions( MapsActivity.this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                             1);
                    Log.d("tag", "permission requested");
                }
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, MapsActivity.this);

    }
    */

}
