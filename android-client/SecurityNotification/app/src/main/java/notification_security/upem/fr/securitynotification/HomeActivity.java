package notification_security.upem.fr.securitynotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import notification_security.upem.fr.securitynotification.network.NetworkService;

/**
 * Home activity. Performs life cycle control and binds other fragments.
 * TODO tell what fragments.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Telling the NetworkService that we are alive.
        // Allowing it to switch to the DIRECT_ACCESS communication mode.
        // It will send local broadcasts, but no asynchronous notifications.
        NetworkService.startChangeAccessAction(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkService.startChangeAccessAction(this, true);
    }

    /**
     * Broadcast manager receiving local intents from the NetworkService.
     * The intent received will be dispatch in corresponding FragmentReceiver // TODO FragmentReceiver interface
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
                default:
                    Log.e(TAG, "onReceive - unknown intent action : " + action);
                    break;
            }
        }
    }
}
