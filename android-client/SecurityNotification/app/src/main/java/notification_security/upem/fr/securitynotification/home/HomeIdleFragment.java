package notification_security.upem.fr.securitynotification.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.geolocalisation.Position;
import notification_security.upem.fr.securitynotification.home.FragmentReceiver.BaseFragmentReceiver;
import notification_security.upem.fr.securitynotification.map.UrgenceMapActivity;
import notification_security.upem.fr.securitynotification.network.NetworkService;

import static notification_security.upem.fr.securitynotification.ViewUtilities.showShortToast;


/**
 * Fragment handling home in idle state, logic.
 */
public class HomeIdleFragment extends BaseFragmentReceiver {

    // The logging TAG.
    private static final String TAG = HomeIdleFragment.class.getSimpleName();

    // Counter of click to launch an alert. This parameter is only in this class.
    public static final int CLICK_NUMBER = 3;

    // Views.
    private Button btAlert;
    private Button btParameter;
    private Button btUrgenceMap;

    private int count = CLICK_NUMBER;

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
    }

    @Override
    void performNetworkRequest(HomeActivity homeActivity, String... params) {
        // TODO retrieve good position.
        NetworkService.startAddAlertAction(homeActivity, new Position(7777, 7777));
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
        homeActivity.showFragment(new HomeAlertedFragment());
    }

    @Override
    void disableFields() {
        btAlert.setEnabled(false);
        btUrgenceMap.setEnabled(false);
        btParameter.setEnabled(false);
        btAlert.setText("ÉMISSION DE L'URGENCE\n...");
    }

    @Override
    void enableFields() {
        btAlert.setEnabled(true);
        btUrgenceMap.setEnabled(true);
        btParameter.setEnabled(true);
        count = CLICK_NUMBER;
        updateAlerteCount();
    }

    @Override
    public String getFilteredAction() {
        return NetworkService.ACTION_ADD_ALERT_RES;
    }

    private void setLocalViews() {
        View view = getView();
        if (view != null) {
            btAlert = (Button) view.findViewById(R.id.home_idle_btAlert);
            btUrgenceMap = (Button) view.findViewById(R.id.home_idle_btUrgenceMap);
            btParameter = (Button) view.findViewById(R.id.home_idle_btSettings);
            return;
        }
        Log.e(TAG, "setLocalViews - can not retrieve the enclosing view.");
    }

    private void setClickListeners() {
        setParameterButtonListener();
        setUrgenceMapButtonListener();
        setAlertButtonListener();
    }

    private void setAlertButtonListener() {
        btAlert.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                count--;
                if (count <= 0) {
                    requestNetworkAction();
                    return;
                }
                updateAlerteCount();
            }
        });
    }

    private void updateAlerteCount() {
        btAlert.setText("EMETTRE URGENCE\n(" + count + " clics rapides)");
    }

    private void setUrgenceMapButtonListener() {
        btUrgenceMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getHomeActivity(), UrgenceMapActivity.class);
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
}
