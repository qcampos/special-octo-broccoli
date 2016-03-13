package notification_security.upem.fr.securitynotification.home;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import notification_security.upem.fr.securitynotification.ViewUtilities;
import notification_security.upem.fr.securitynotification.geolocalisation.Position;

/**
 * AsyncTask provinding the current GPS position, in one call.
 * No need to use more complex services provided by the very application
 * In the HomeActivity.
 *
 * @see notification_security.upem.fr.securitynotification.map.GeoLocalisationServiceB
 * for more evolved functionnalities.
 */
public class AsyncGPSProvider extends AsyncTask<Void, Void, Position> implements LocationListener {

    // The logging TAG.
    private static final String TAG = AsyncGPSProvider.class.getSimpleName();

    /**
     * Observer listener notified for the position computed.
     */
    public interface AsyncGPSListener {
        public void onUpdatedPositionSuccess(Position position);

        public void onUpdatedPositionFailed();

        void onPermissionFailed();
    }

    private final Activity context;
    private final AsyncGPSListener listener;

    // Inner fields.
    private LocationManager locationManager;
    private String bestProvider;
    private Position position;

    public AsyncGPSProvider(Activity context, AsyncGPSListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "Récupération de la position.");
        if (!initRequest()) {
            cancel(true);
            listener.onPermissionFailed();
            Log.e(TAG, "onPreExecute - init Failed.");
            return;
        }
        ViewUtilities.showShortToast(context, "Debug : Fetching position");
        Log.d(TAG, "onPreExecute - preExecute finished.");
    }

    private boolean initRequest() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Creating the criteria.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false); // No monetary cost ? TODO put it in a parametrization ?
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); // No requirements on features.
        bestProvider = locationManager.getBestProvider(criteria, false);
        // Using the GPS provider.
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            bestProvider = LocationManager.GPS_PROVIDER;
            Log.d(TAG, "initRequest - GPS is enabled, using it.");
            // With the network.
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            bestProvider = LocationManager.NETWORK_PROVIDER;
            ViewUtilities.showShortToast(context, "Debug : GPS is disabled, using network.");
            Log.d(TAG, "initRequest - GPS is disabled, using network.");
        } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            bestProvider = LocationManager.PASSIVE_PROVIDER;
            Log.d(TAG, "initRequest -  No GPS Available !");
            ViewUtilities.showShortToast(context, "Debug : No GPS Available ! "); // TODO set no transmissions.
        }

        // Checking permissions.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.d(TAG, "initRequest - requestPermission.");
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
            Log.d(TAG, "initRequest - permission granted.");
        }
        Log.d(TAG, "initRequest - Permissions ok for provider : " + bestProvider);
        locationManager.requestLocationUpdates(bestProvider, 0, 0, this);
        return true;
    }


    @Override
    protected Position doInBackground(Void... params) {

        Log.d(TAG, "doInBackground - starting poll : ");
        while (position == null) {
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        locationManager.removeUpdates(this);
        Log.d(TAG, "doInBackground - poll ended : " + position.getLatitude() + " " + position.getLongitude());
        return position;
    }

    @Override
    protected void onPostExecute(Position position) {
        super.onPostExecute(position);
        if (position == null) {
            listener.onUpdatedPositionFailed();
        } else {
            listener.onUpdatedPositionSuccess(position);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Creating the new position according to the updated location received.
        position = new Position(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "onLocationChanged -  position : " + position.getLatitude() + " " + position.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
