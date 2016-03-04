package notification_security.upem.fr.securitynotification.home;

import android.content.Intent;

/**
 * Created by Baxtalou on 04/03/2016.
 */
public interface FragmentReceiver {

    /**
     * A FragmentState tells if the current enclosing Fragment is waiting
     * for a network result or not. It could have been replaced by a simple boolean,
     * but it would have been a far less elegant way.
     */
    enum FragmentState {
        IDLE,
        WAITING_NETWORK_RESULT
    }

    /**
     * Receives the given intent from NetworkService to process.
     * If the receiver is not in WAITING_NETWORK_RESULT, it will
     * discard the call.
     *
     * @param intent the intent to process.
     */
    void onReceiveNetworkIntent(Intent intent);
}
