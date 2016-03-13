package notification_security.upem.fr.securitynotification.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.HashMap;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.ViewUtilities;
import notification_security.upem.fr.securitynotification.geolocalisation.Position;
import notification_security.upem.fr.securitynotification.home.FragmentReceiver.BaseFragmentReceiver;
import notification_security.upem.fr.securitynotification.map.GeoLocalisationServiceB;
import notification_security.upem.fr.securitynotification.map.MapsActivity;
import notification_security.upem.fr.securitynotification.map.UrgencyMapActivity;
import notification_security.upem.fr.securitynotification.network.NetworkService;
import notification_security.upem.fr.securitynotification.network.ProtocolConstants;

import static notification_security.upem.fr.securitynotification.ViewUtilities.showShortToast;


/**
 * Fragment handling home in idle state, logic.
 */
public class HomeIdleFragment extends BaseFragmentReceiver implements AsyncGPSProvider.AsyncGPSListener {

    // The logging TAG.
    private static final String TAG = HomeIdleFragment.class.getSimpleName();

    // Counter of click to launch an alert. This parameter is only in this class.
    public static final int CLICK_NUMBER = 3;

    // Views.
    private Button btAlert;
    private Button btParameter;
    private Button btUrgencyMap;
    private Position position;

    private int count = CLICK_NUMBER;
    private AsyncGPSProvider asyncGPSProvider;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_idle, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Getting local views.
        setLocalViews();
        // Setting listeners.
        setClickListeners();
        // The gps asyncGPSProvider.
        asyncGPSProvider = new AsyncGPSProvider(getActivity(), this);
    }

    @Override
    void performNetworkRequest(HomeActivity homeActivity, String... params) {
        // TODO retrieve good position.
        int radius = homeActivity.getPreferences(Context.MODE_PRIVATE)
                .getInt(ProtocolConstants.RADIUS_KEY, ProtocolConstants.DEFAULT_RADIUS);
        Log.d(TAG, "performNetworkRequest - requesting with position : " + position.getLatitude() + " " + position.getLongitude());
        NetworkService.startAddAlertAction(homeActivity, position, Integer.toString(radius));
    }

    @Override
    void processNetworkResult(HomeActivity homeActivity, Intent intent) {
        boolean result = intent.getBooleanExtra(NetworkService.EXTRA_RES, false);
        // Incorrect informations.
        if (!result) {
            showShortToast(homeActivity, "Impossible de lancer l'alerte.\nVeuillez redémarrer l'application.");
            stopWaitingNetworkResult();
            return;
        }
        // Correct informations. We can pass to the HomeIdleFragment.
        activateAlertInPreferences();
        homeActivity.showFragment(new HomeAlertedFragment());
    }

    private void activateAlertInPreferences() {
        SharedPreferences preferences = getHomeActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ProtocolConstants.IS_ALERTING_KEY, true);
        editor.commit();
    }

    @Override
    void disableFields() {
        btAlert.setEnabled(false);
        btUrgencyMap.setEnabled(false);
        btParameter.setEnabled(false);
        btAlert.setText("ÉMISSION DE L'URGENCE\n...");
    }

    @Override
    void enableFields() {
        btAlert.setEnabled(true);
        btUrgencyMap.setEnabled(true);
        btParameter.setEnabled(true);
        count = CLICK_NUMBER;
        updateAlertCount();
    }

    @Override
    public String getFilteredAction() {
        return NetworkService.ACTION_ADD_ALERT_RES;
    }

    private void setLocalViews() {
        View view = getView();
        if (view != null) {
            btAlert = (Button) view.findViewById(R.id.home_idle_btAlert);
            btUrgencyMap = (Button) view.findViewById(R.id.home_idle_btUrgencyMap);
            btParameter = (Button) view.findViewById(R.id.home_idle_btSettings);
            return;
        }
        Log.e(TAG, "setLocalViews - can not retrieve the enclosing view.");
    }

    private void setClickListeners() {
        setParameterButtonListener();
        setUrgencyMapButtonListener();
        setAlertButtonListener();
    }

    private void setAlertButtonListener() {
        btAlert.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                count--;
                if (count <= 0) {
                    disableFields(); // TODO don't disable all fields here, please allows the cancel.
                    asyncGPSProvider.execute();
                    return;
                }
                updateAlertCount();
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void updateAlertCount() {
        btAlert.setText("EMETTRE URGENCE\n(" + count + " clics rapides)");
    }

    private void setUrgencyMapButtonListener() {
        btUrgencyMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getHomeActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setParameterButtonListener() {
        btParameter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getHomeActivity().showFragment(new ParameterFragment());
            }
        });
    }

    @Override
    public void onUpdatedPositionSuccess(Position position) {
        this.position = position;
        requestNetworkAction();
    }

    @Override
    public void onUpdatedPositionFailed() {
        // TODO handle error when retrieving by showing an error prompt. (With please retrieve).
    }

    @Override
    public void onPermissionFailed() {
        HomeActivity homeActivity = getHomeActivity();
        homeActivity.showFragment(new HomeIdleFragment());
        ViewUtilities.showShortToast(homeActivity, "Veuillez accepter la permission.");
    }
}
