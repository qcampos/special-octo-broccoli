package notification_security.upem.fr.securitynotification;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import notification_security.upem.fr.securitynotification.network.NetworkService;

/**
 * Home activity. Performs life cycle control and binds other fragments.
 * TODO tell what fragments.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When launching the activity, telling the NetworkService that
        // we are alive. Allowing it to switch to the DIRECT_ACCESS communication
        // mode. It will send local broadcasts, but no asynchronous notifications.

        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NetworkService.startChangeAccessAction(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkService.startChangeAccessAction(this, true);
    }
}
