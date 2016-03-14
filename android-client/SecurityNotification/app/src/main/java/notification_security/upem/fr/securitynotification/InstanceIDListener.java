package notification_security.upem.fr.securitynotification;

import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by algorithmia on 08/03/2016.
 */
public class InstanceIDListener extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * This may occured if previous Token was compromised
     */
    @Override
    public void onTokenRefresh() {

        Log.v(TAG, " Security may be comprised. Ask for new registration");
        RegistrationService.startRegisterService(this);
    }

}
