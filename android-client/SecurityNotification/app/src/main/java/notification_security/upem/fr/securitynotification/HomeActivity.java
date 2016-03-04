package notification_security.upem.fr.securitynotification;

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

import java.util.HashMap;

import notification_security.upem.fr.securitynotification.network.NetworkService;

import static notification_security.upem.fr.securitynotification.ViewUtilities.getFragmentById;

/**
 * Home activity. Performs life cycle control and binds other fragments.
 * TODO tell what fragments.
 */
public class HomeActivity extends AppCompatActivity {

    private NetworkServiceReceiver serviceReceiver;
    private HashMap<Class<? extends FragmentReceiver>, FragmentReceiver> fragmentsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Our receiver, dispatching answers from the network service to our fragments.
        serviceReceiver = new NetworkServiceReceiver();
        // Storing all managed fragments receiver.
        fragmentsMap = new HashMap<>();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentsMap.put(ConnectionFragment.class, getFragmentById(fragmentManager, R.id.fragment_connection));
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
        unregisterReceiver(serviceReceiver);
    }


    @NonNull
    public static IntentFilter createHomeFilters() {
        IntentFilter filter = new IntentFilter(NetworkService.ACTION_CHANGE_ACCESS_RES);
        filter.addAction(NetworkService.ACTION_CONNECT_RES);
        return filter;
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
            switch (action) {
                case NetworkService.ACTION_CHANGE_ACCESS_RES:
                    break;
                case NetworkService.ACTION_CONNECT_RES:
                    fragmentsMap.get(ConnectionFragment.class).onReceiveNetworkIntent(intent);
                    break;
                default:
                    Log.e(TAG, "onReceive - unknown intent action : " + action);
                    break;
            }
        }
    }
}
