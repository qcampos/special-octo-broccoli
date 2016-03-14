package notification_security.upem.fr.securitynotification.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.geolocalisation.Position;
import notification_security.upem.fr.securitynotification.network.NetworkService;
import notification_security.upem.fr.securitynotification.network.ProtocolConstants;

public class MapsActivity extends LocationListenerFragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    // TODO Rename List geoLocalisationServiceB
    private GeoLocalisationServiceB geoLocalisationServiceB;
    private boolean mBound;
    private NetworkServiceReceiver serviceReceiver;
    private HashMap<String, Position> markerIdToAlertID;
    private List<Runnable> toDoOnMapReady;
    private boolean localisationNeeded;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GeoLocalisationServiceB.LocalBinder binder = (GeoLocalisationServiceB.LocalBinder) service;
            geoLocalisationServiceB = binder.getService();
            geoLocalisationServiceB.subscribeLocationUpdate(MapsActivity.this, MapsActivity.this, 1);
            Log.d(TAG, "subscribed");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        markerIdToAlertID = new HashMap<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (savedInstanceState != null) {
            toDoOnMapReady = new ArrayList<>();
            toDoOnMapReady.add(new Runnable() {
                @Override
                public void run() {
                    double lat = savedInstanceState.getDouble("lat");
                    double lng = savedInstanceState.getDouble("lng");
                    float zoom = savedInstanceState.getFloat("zoom");
                    moveCamera(lat, lng, zoom);
                    HashMap<String, Position> positions = (HashMap<String, Position>) savedInstanceState.getSerializable("positions");
                    for (Position position : positions.values()) {
                        addMarker(position);
                    }
                }
            });
            localisationNeeded = false;
        } else {
            localisationNeeded = true;
        }
        serviceReceiver = new NetworkServiceReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindGeoLocalisationService();
        registerNetworkServiceReceiver();
        Log.d(TAG, "onstrat");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unBindGeoLocalisationService();
        unregisterNetworkServiceReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("positions", markerIdToAlertID);
        CameraPosition cameraPosition = mMap.getCameraPosition();
        outState.putDouble("lat", cameraPosition.target.latitude);
        outState.putDouble("lng", cameraPosition.target.longitude);
        outState.putFloat("zoom", cameraPosition.zoom);
    }

    private void bindGeoLocalisationService() {
        if (localisationNeeded) {
            Intent intent = new Intent(this, GeoLocalisationServiceB.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unBindGeoLocalisationService() {
        if (mBound) {
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
                if (!markerIdToAlertID.containsKey(marker.getId())) {
                    Log.d(TAG, "Weird... this marker is not related to any alert ID");
                    return false;
                }
                Position alertTargeted = markerIdToAlertID.get(marker.getId());
                Log.d(TAG, "Alert targeted  " + alertTargeted.getId() + "has voted " + alertTargeted.isHasVoted() + " ");
                AlertDialog dialog = createDialog(alertTargeted);
                try {
                    dialog.setMessage(translateCoordinateToAdress(marker.getPosition()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.show();
                return true;
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (toDoOnMapReady != null) {
            for (Runnable toDo : toDoOnMapReady) {
                toDo.run();
            }
        }
    }

    private void addMarker(Position position) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(position.getLatitude(), position.getLongitude())));
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerIdToAlertID.put(marker.getId(), position);
        Log.d(TAG, "Adding marker " + position.getLatitude() + " " + position.getLongitude());
    }

    private void addMarkers(List<Position> positions) {
        for (Position position : positions) {
            addMarker(position);
        }
    }

    private void moveCamera(Position position) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(position.getLatitude(), position.getLongitude()), 11));
    }

    private void moveCamera(double lat, double lng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }

    @Override
    public void onLocationChanged(Location location) {
        Position position = new Position(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "New position : position.toString()");
        moveCamera(position);

        int radius = this.getPreferences(Context.MODE_PRIVATE)
                .getInt(ProtocolConstants.RADIUS_KEY, ProtocolConstants.DEFAULT_RADIUS);
        NetworkService.startActionGetListAlert(this, position, radius);
    }

    /**
     * create a  dialog box
     */
    private AlertDialog createDialog(final Position alert) {
        final String alertId = alert.getId();
        boolean showButtons = !alert.isHasVoted();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (showButtons) {
            builder.setPositiveButton("Je m'y trouve", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "User clicked OK button number " + alertId);
                    alert.setHasVoted(true);
                    NetworkService.startValidateAction(MapsActivity.this, alertId, true, false);
                }
            });
            builder.setNegativeButton("Fausse alerte", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "User cancelled number " + alertId + " " + alert.getId());
                    alert.setHasVoted(true);
                    NetworkService.startValidateAction(MapsActivity.this, alertId, false, true);
                }
            });
        } else {
            builder.setNeutralButton("Vous avez déja voté", null);
        }
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("red"));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MapsActivity.this, R.color.yellowGreen));
            }
        });
        return dialog;
    }


    private String translateCoordinateToAdress(LatLng latLng) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> adresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        if (adresses.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= adresses.get(0).getMaxAddressLineIndex(); i++) {
                sb.append(adresses.get(0).getAddressLine(i));
                if (i < adresses.get(0).getMaxAddressLineIndex()) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        } else {
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
        filter.addAction(NetworkService.ACTION_GET_ALERT_LIST_RES);
        return filter;
    }

    public class NetworkServiceReceiver extends BroadcastReceiver {

        private final String TAG = NetworkServiceReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case NetworkService.ACTION_GET_ALERT_LIST_RES:
                    ArrayList<Position> positions = intent.getParcelableArrayListExtra(NetworkService.EXTRA_LIST_POSITIONS);
                    Log.d(TAG, "size " + positions.size());
                    addMarkers(positions);
                    break;
                default:
                    break;
            }
        }
    }

}
