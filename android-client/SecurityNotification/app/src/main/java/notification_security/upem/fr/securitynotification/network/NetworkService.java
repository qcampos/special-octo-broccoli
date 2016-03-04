package notification_security.upem.fr.securitynotification.network;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import notification_security.upem.fr.securitynotification.geolocalisation.Position;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class NetworkService extends IntentService {
    // List of ACTION, and their EXTRA constants keys this service can perform.
    private static final String TAG = NetworkService.class.getSimpleName();
    // Result constant. It is always set in answers' intent.
    public static final String EXTRA_RES = "fr.upem.securitynotification.network.extra.RES";
    // Change access constants.
    private static final String ACTION_CHANGE_ACCESS = "fr.upem.securitynotification.network.action.CHANGE_ACCESS";
    public static final String ACTION_CHANGE_ACCESS_RES = "fr.upem.securitynotification.network.action.CHANGE_ACCESS_RES";
    public static final String EXTRA_CHANGE_ACCESS = "fr.upem.securitynotification.network.extra.CHANGE_ACCESS";
    // Connect constants.
    private static final String ACTION_CONNECT = "fr.upem.securitynotification.network.action.CONNECT";
    public static final String ACTION_CONNECT_RES = "fr.upem.securitynotification.network.action.CONNECT_RES";
    private static final String EXTRA_CONNECT_LOGGING = "fr.upem.securitynotification.network.extra.LOGGING";
    private static final String EXTRA_CONNECT_PIN = "fr.upem.securitynotification.network.extra.PIN";
    // SignUp constants.
    private static final String ACTION_SIGNUP = "fr.upem.securitynotification.network.action.SIGNUP";
    public static final String ACTION_SIGNUP_RES = "fr.upem.securitynotification.network.action.SIGNUP_RES";
    /*_______ Make your extras for the factory _______*/
    // Add Alert constants.
    private static final String ACTION_ADD_ALERT = "fr.upem.securitynotification.network.action.ADD_ALERT";
    public static final String ACTION_ADD_ALERT_RES = "fr.upem.securitynotification.network.action.ADD_ALERT_RES";
    /*_______ Make your extras for the factory _______*/


    private boolean accessActivityDirectly = false;

    public NetworkService() {
        super("NetworkService");
    }

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

    /**
     * Starts this service to perform Connect Action with the given parameters.
     * If the service is already performing a task ths action will be queued.
     *
     * @param context the context invoking this method.
     * @param logging the logging to connect the app.
     * @param pin     the pin associated with the logging to connect the app.
     * @see IntentService
     */
    public static void startConnectAction(Context context, String logging, String pin) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_CONNECT_LOGGING, logging);
        intent.putExtra(EXTRA_CONNECT_PIN, pin);
        context.startService(intent);
    }

    /**
     * Starts this service to perform SignUp Action with the given parameters.
     * If the service is already performing a task ths action will be queued.
     *
     * @see IntentService
     */
    public static void startSignUpAction(Context context, String firstName, String lastName,
                                         String email, String phone, String PIN) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_SIGNUP);
        /* TODO intent.putExtra(EXTRA_CONNECT_LOGGING, logging); */
        context.startService(intent);
    }

    /**
     * Starts this service to perform Add Alert Action with the given parameters.
     * If the service is already performing a task ths action will be queued.
     *
     * @see IntentService
     */
    public static void startAddAlertAction(Context context, Position position) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_ADD_ALERT);
        /* TODO intent.putExtra(EXTRA_CONNECT_LOGGING, logging); */
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_CHANGE_ACCESS:
                    // This is a handle method for example. You can do all your stuff
                    // On this basis.
                    final boolean isDirect = intent.getBooleanExtra(EXTRA_CHANGE_ACCESS, accessActivityDirectly);
                    handleActionChangeAccess(isDirect);
                    break;
                // All next actions are grouped because for the moment we only want to receive true/false
                // values in the view, but it will be separated.
                case ACTION_ADD_ALERT:
                case ACTION_CONNECT:
                case ACTION_SIGNUP:

                    // TODO call the corresponding handle method.
                    Log.v(TAG, "handleActionXX receiving new parameters...");
                    // TODO this is the type of answer made when accessActivityDirectly == true.
                    Intent localIntent = new Intent(action + "_RES");
                    // Always put this boolean which tells us if everything went good, or if we
                    // will have to process error fields.
                    localIntent.putExtra(EXTRA_RES, true);
                    sendLocalBroadcast(localIntent);

                    break;

                default:
                    Log.e(TAG, "onHandleIntent error in communication protocol.");
                    break;
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

    /**
     * Sends a local broadcast with the given intent.
     */
    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
