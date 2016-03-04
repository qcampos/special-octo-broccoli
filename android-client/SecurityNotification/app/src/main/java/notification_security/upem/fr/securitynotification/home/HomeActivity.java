package notification_security.upem.fr.securitynotification.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.network.NetworkService;

/**
 * Home activity. Performs life cycle control and binds other fragments.
 * TODO tell the list of fragments handled in HomeActivity.
 */
public class HomeActivity extends AppCompatActivity {

    // The logging TAG
    private static final String TAG = HomeActivity.class.getSimpleName();

    private NetworkServiceReceiver serviceReceiver;
    private FragmentReceiver fragmentDisplayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Our receiver, dispatching answers from the network service to our fragments.
        serviceReceiver = new NetworkServiceReceiver();
        // Storing all managed fragments receiver.
        setContentView(R.layout.activity_home);
        // Show code.
        fragmentDisplayed = new ConnectionFragment();
        showFirstFragment(fragmentDisplayed);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkServiceReceiver();
        // Telling the NetworkService that we are alive.
        // Allowing it to switch to the DIRECT_ACCESS communication mode.
        // It will send local broadcasts, instead of asynchronous notifications.
        NetworkService.startChangeAccessAction(this, true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkServiceReceiver();
        NetworkService.startChangeAccessAction(this, false);
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
        IntentFilter filter = new IntentFilter(NetworkService.ACTION_CHANGE_ACCESS_RES);
        filter.addAction(NetworkService.ACTION_CONNECT_RES);
        filter.addAction(NetworkService.ACTION_SIGNUP_RES);
        filter.addAction(NetworkService.ACTION_ADD_ALERT_RES);
        return filter;
    }

    /**
     * Shows the given FragmentReceiver by replacing the current one.
     * Incoming intents from the NetworkService will be delegate to the
     * new FragmentReceiver displayed.
     *
     * @param fragmentToShow the fragment to show.
     */
    void showFragment(FragmentReceiver fragmentToShow) {
        fragmentDisplayed = fragmentToShow;
        Fragment fragment = (Fragment) fragmentToShow;
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.home_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        Log.d(TAG, "showFragment - called with : " + fragmentToShow);
    }

    /**
     * The first fragment to show. It is inserted inside the home's fragment_container
     * Incoming intents from the NetworkService will be delegate to the
     * new FragmentReceiver displayed.
     *
     * @param fragmentToShow the fragment to show.
     */
    private void showFirstFragment(FragmentReceiver fragmentToShow) {
        Fragment fragment = (Fragment) fragmentToShow;
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.home_fragment_container, fragment)
                .commit();
        Log.d(TAG, "showFirstFragment - called with : " + fragmentToShow);
    }

    /**
     * Broadcast manager receiving local intents from the NetworkService.
     * The intent received will be dispatch in corresponding FragmentReceiver
     * handled by the enclosing HomeActivity.
     */
    public class NetworkServiceReceiver extends BroadcastReceiver {

        private final String TAG = NetworkServiceReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!fragmentDisplayed.getFilteredAction().equals(action)) {
                Log.e(TAG, "onReceive - action not corresponding to current fragment : " + action);
                return;
            }
            // TODO if we receive an urgent map action when we are elsewhere, we must print it (perhaps in the scroll bar).
            fragmentDisplayed.onReceiveNetworkIntent(intent);
        }
    }
}
