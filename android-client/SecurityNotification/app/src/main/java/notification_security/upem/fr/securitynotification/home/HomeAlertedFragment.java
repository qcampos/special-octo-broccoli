package notification_security.upem.fr.securitynotification.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.home.FragmentReceiver.BaseFragmentReceiver;
import notification_security.upem.fr.securitynotification.network.NetworkService;
import notification_security.upem.fr.securitynotification.network.ProtocolConstants;

import static notification_security.upem.fr.securitynotification.ViewUtilities.showShortToast;

/**
 * Fragment handling home in alert state, logic.
 */
public class HomeAlertedFragment extends BaseFragmentReceiver {

    // The logging TAG
    private static final String TAG = HomeAlertedFragment.class.getSimpleName();

    // View.
    private Button btEndAlert;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_alerted, container, false);
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
        NetworkService.startStopAlertAction(homeActivity);
    }

    @Override
    void processNetworkResult(HomeActivity homeActivity, Intent intent) {
        boolean result = intent.getBooleanExtra(NetworkService.EXTRA_RES, false);
        // Incorrect informations.
        if (!result) {
            showShortToast(homeActivity, "Impossible d'arrêter l'alerte.");
            stopWaitingNetworkResult();
            return;
        }
        // Correct informations. We can deactivate the alert preference and pass to the HomeIdleFragment.
        deactivateAlertInPreferences();
        homeActivity.showFragment(new HomeIdleFragment());
    }

    private void deactivateAlertInPreferences() {
        SharedPreferences preferences = getHomeActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ProtocolConstants.IS_ALERTING_KEY, false);
        editor.commit();
    }

    @Override
    void disableFields() {
        btEndAlert.setEnabled(false);
    }

    @Override
    void enableFields() {
        btEndAlert.setEnabled(true);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public String getFilteredAction() {
        return NetworkService.ACTION_STOP_ALERT_RES;
    }

    private void setClickListeners() {
        btEndAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNetworkAction();
            }
        });
    }

    private void setLocalViews() {
        View view = getView();
        if (view != null) {
            btEndAlert = (Button) view.findViewById(R.id.home_alerted_btEndAlert);
            return;
        }
        Log.e(TAG, "setLocalViews - can not retrieve the enclosing view.");
    }
}
