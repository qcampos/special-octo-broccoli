package notification_security.upem.fr.securitynotification;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import notification_security.upem.fr.securitynotification.network.NetworkService;


/**
 * @author Yann BILISSOR
 * @version 1.0
 *
 *          This class handle all incomming message from Google Cloud Messaging
 */
public class ListenerService extends GcmListenerService {

    /* TAG use for logging */
    private static final String TAG = ListenerService.class.getSimpleName();

    /**
     * This methods is called each times a new incoming message is
     * available. It does nothing but calling Network Service which
     * handles communication with view.
     *
     * @param from
     * @param data
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {

        if(from.startsWith(RegistrationService.USER_TOPIC)){
            NetworkService.startOnReceiveMessageFromUser(this, data);
        }
        else if(from.startsWith(RegistrationService.ALERT_TOPIC)){
            NetworkService.startOnReceiveMessageFromAlert(this, data);
        }

        Log.v(TAG, "Got new message from : " + from+" with data : "+data);

    }


}
