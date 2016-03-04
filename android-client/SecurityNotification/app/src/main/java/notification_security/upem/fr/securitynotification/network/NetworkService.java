package notification_security.upem.fr.securitynotification.network;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class NetworkService extends IntentService {
    // List of ACTION, and their EXTRA constants keys this service can perform.
    private static final String ACTION_CHANGE_ACCESS = "fr.upem.securitynotification.network.action.CHANGE_ACCESS";
    private static final String EXTRA_CHANGE_ACCESS = "fr.upem.securitynotification.network.extra.CHANGE_ACCESS";
    private static final String TAG = NetworkService.class.getSimpleName();
    private boolean accessActivityDirectly = false;


    /**
     * Starts this service to perform Change Access with the given mode.
     * If the service is already performing a task ths action will be queued.
     *
     * @param context  the context invoking this method.
     * @param isDirect true if the current NetworkService has to send local broadcasts
     *                 to contact the view. false when it has to prepare
     *                 a new notification, which will launch the view activity when it is clicked.
     * @see IntentService
     */
    public static void startChangeAccessAction(Context context, boolean isDirect) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_CHANGE_ACCESS);
        intent.putExtra(EXTRA_CHANGE_ACCESS, isDirect);
        context.startService(intent);
    }


    public NetworkService() {
        super("NetworkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_CHANGE_ACCESS:
                    final boolean isDirect = intent.getBooleanExtra(EXTRA_CHANGE_ACCESS, accessActivityDirectly);
                    handleActionChangeAccess(isDirect);
                default:
                    Log.e(TAG, "onHandleIntent ");
            }
        }
    }

    /**
     * Handles the action change access.
     *
     * @param accessActivityDirectly true if the current NetworkService has to send local broadcasts
     *                               to contact the view. false when it has to prepare
     *                               a new notification, which will launch the view activity when it is clicked.
     * @see NetworkService#startChangeAccessAction(Context, boolean)
     */
    private void handleActionChangeAccess(boolean accessActivityDirectly) {
        Log.d(TAG, "handleActionChangeAccess receives new direct access : " + accessActivityDirectly);
        this.accessActivityDirectly = accessActivityDirectly;
    }

}
