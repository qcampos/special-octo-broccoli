package notification_security.upem.fr.securitynotification.home;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.net.HttpCookie;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.network.NetworkService;
import notification_security.upem.fr.securitynotification.network.ProtocolConstants;

import static notification_security.upem.fr.securitynotification.ViewUtilities.showShortToast;

public class SplashFragment extends FragmentReceiver.BaseFragmentReceiver {


    // The logging TAG
    private static final String TAG = SplashFragment.class.getSimpleName();

    // Connection fields for auto-connection.
    private String login;
    private String pin;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Getting local views.
        initializeAutoConnection();
    }


    @Override
    void performNetworkRequest(HomeActivity homeActivity, String... params) {
        NetworkService.startConnectAction(homeActivity, login, pin);
    }

    @Override
    void processNetworkResult(HomeActivity homeActivity, Intent intent) {
        boolean result = intent.getBooleanExtra(NetworkService.EXTRA_RES, false);
        // Incorrect informations.
        if (!result) {
            showShortToast(homeActivity, "Impossible de se connecter automatiquement.");
            stopWaitingNetworkResult();
            homeActivity.showFragment(new ConnectionFragment());
            return;
        }
        // Correct informations. We can test if an alert was triggered or not.
        if (isAlerting()) {
            homeActivity.showFragment(new HomeAlertedFragment());
            return;
        }
        // Normal home screen.
        homeActivity.showFragment(new HomeIdleFragment());
    }

    @Override
    public String getFilteredAction() {
        return NetworkService.ACTION_CONNECT_RES;
    }

    private void initializeAutoConnection() {
        final HomeActivity homeActivity = getHomeActivity();
        // If an automatic connection can be performed, doing it.
        if (autoConnect()) {
            // Asking for manual connection.
            Log.d(TAG, "initializeAutoConnection - No auto-connect");
            homeActivity.showFragment(new ConnectionFragment());
            return;
        }
        Log.d(TAG, "initializeAutoConnection - Auto conenction requested : login " + login + " pin " + pin);
        // Requesting automatic connection.
        requestNetworkAction();
    }

    private boolean autoConnect() {
        SharedPreferences preferences = getHomeActivity().getPreferences(Context.MODE_PRIVATE);
        login = preferences.getString(ProtocolConstants.LOGIN, ProtocolConstants.UNSET_PREFERENCE);
        pin = preferences.getString(ProtocolConstants.PIN, ProtocolConstants.UNSET_PREFERENCE);
        return true; //login == ProtocolConstants.UNSET_PREFERENCE || pin == ProtocolConstants.UNSET_PREFERENCE;
    }

    private boolean isAlerting() {
        SharedPreferences preferences = getHomeActivity().getPreferences(Context.MODE_PRIVATE);
        return preferences.getBoolean(ProtocolConstants.IS_ALERTING_KEY, false);
    }

    @Override
    void disableFields() {
    }

    @Override
    void enableFields() {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
