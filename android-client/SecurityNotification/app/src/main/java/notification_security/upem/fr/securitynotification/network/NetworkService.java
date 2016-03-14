package notification_security.upem.fr.securitynotification.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import notification_security.upem.fr.securitynotification.RegistrationService;
import notification_security.upem.fr.securitynotification.geolocalisation.Position;
import notification_security.upem.fr.securitynotification.map.MapsActivity;
import notification_security.upem.fr.securitynotification.network.stub.NotificationStub;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods. EXTRA_ERROR_CODE
 */
public class NetworkService extends IntentService {
    // List of ACTION, and their EXTRA constants keys this service can perform.
    private static final String TAG = NetworkService.class.getSimpleName();
    // Result constant. It is always set in answers' intent.
    public static final String EXTRA_RES = "fr.upem.securitynotification.network.extra.RES";
    public static final String EXTRA_ERROR_CODE = "fr.upem.securitynotification.network.EXTRA_ERROR_CODE.RES";

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
    private static final String EXTRA_SIGNUP_PHONE = "fr.upem.securitynotification.network.extra.PHONE";
    private static final String EXTRA_SIGNUP_EMAIL = "fr.upem.securitynotification.network.extra.EMAIL";
    private static final String EXTRA_SIGNUP_LASTNAME = "fr.upem.securitynotification.network.extra.LASTNAME";
    private static final String EXTRA_SIGNUP_FIRSTNAME = "fr.upem.securitynotification.network.extra.FIRSTNAME";
    private static final String EXTRA_SIGNUP_PIN = "fr.upem.securitynotification.network.extra.PIN";

    /*_______ Make your extras for the factory _______*/
    // Add Alert constants.
    private static final String ACTION_ADD_ALERT = "fr.upem.securitynotification.network.action.ADD_ALERT";
    public static final String ACTION_ADD_ALERT_RES = "fr.upem.securitynotification.network.action.ADD_ALERT_RES";
    private static final String EXTRA_ADD_ALERT_LAT = "fr.upem.securitynotification.network.extra.LAT";
    private static final String EXTRA_ADD_ALERT_LONG = "fr.upem.securitynotification.network.extra.LONG";
    private static final String EXTRA_ADD_ALERT_NAME = "fr.upem.securitynotification.network.extra.NAME";

    /*_______ Make your extras for the factory _______*/
    // Add Alert constants.
    private static final String ACTION_STOP_ALERT = "fr.upem.securitynotification.network.action.STOP_ALERT";
    public static final String ACTION_STOP_ALERT_RES = "fr.upem.securitynotification.network.action.STOP_ALERT_RES";

    /*_______ Make your extras for the factory _______*/
    private static final String EXTRA_SESSION_ID = "fr.upem.securitynotification.network.extra.ID";
    private static final String APP_IS_REGISTERED = "fr.upem.securitynotification.network.registered";
    private static final String EXTRA_LAST_ALERT_ID = "fr.upem.securitynotification.network.alertID";
    private static final String EXTRA_USER_ID = "fr.upem.securitynotification.network.uuid";

    /* Network Proxy */
    private NotificationStub stub;

    /* Data bundle */
    private static Bundle data = new Bundle();

    /* GCM */
    public static final String ACTION_ALERT_STATE_RES = "fr.upem.securitynotification.network.action.ALERT_STATE_RES";
    private static final String ACTION_ON_RECEIVE_USR_MSG = "fr.upem.securitynotification.network.RECEIVE_USR_MSG";
    private static final String ACTION_ON_RECEIVE_ALERT_MSG = "fr.upem.securitynotification.network.RECEIVE_ALERT_MSG";
    private static final String EXTRA_GCM_MSG = "fr.upem.securitynotification.network.GCM_MSG";
    private static final String DANGER_MSG = " personnes potentiellement end anger";

    /* List Alert */
    private static final String ACTION_VALIDATE = "fr.upem.securitynotification.network.ACTION_VALIDATE";
    private static final String ACTION_VALIDATE_RES = "fr.upem.securitynotification.network.ACTION_VALIDATE_RES";
    private static final String ACTION_GET_ALERT_LIST = "fr.upem.securitynotification.network.ACTION_LIST_ALERT";
    public static final String ACTION_GET_ALERT_LIST_RES = "fr.upem.securitynotification.network.ACTION_LIST_ALERT_RES";
    private static final String EXTRA_LIST_ALERT_RADIUS = "fr.upem.securitynotification.network.EXTRA_LIST_ALERT_RADIUS";
    public static final String EXTRA_LIST_POSITIONS = "fr.upem.securitynotification.network.EXTRA_LIST_POSITIONS";
    public static final String ALERT_IS_ACTIVE = "fr.upem.securitynotification.network.ALERT_IS_ACTIVE";


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
        intent.putExtra(EXTRA_SIGNUP_PHONE, phone);
        intent.putExtra(EXTRA_SIGNUP_EMAIL, email);
        intent.putExtra(EXTRA_SIGNUP_FIRSTNAME, firstName);
        intent.putExtra(EXTRA_SIGNUP_LASTNAME, lastName);
        intent.putExtra(EXTRA_SIGNUP_PIN, PIN);
        context.startService(intent);
    }

    /**
     * Starts this service to perform Add Alert Action with the given parameters.
     * If the service is already performing a task ths action will be queued.
     *
     * @see IntentService
     */
    public static void startAddAlertAction(Context context, Position position, String name) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_ADD_ALERT);
        intent.putExtra(EXTRA_ADD_ALERT_LAT, position.getLatitude());
        intent.putExtra(EXTRA_ADD_ALERT_LONG, position.getLongitude());
        intent.putExtra(EXTRA_ADD_ALERT_NAME, name);
        context.startService(intent);
    }

    /**
     * Starts this service to perform Stop Alert Action with the given parameters.
     * If the service is already performing a task ths action will be queued.
     *
     * @see IntentService
     */
    public static void startStopAlertAction(Context context) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_STOP_ALERT);
        context.startService(intent);
    }

    /**
     * Start service when GCMListener has got new message to deliver
     *
     * @param data
     */
    public static void startOnReceiveMessageFromUser(Context ctx, Bundle data) {

        Intent intent = new Intent(ctx, NetworkService.class);
        intent.setAction(ACTION_ON_RECEIVE_USR_MSG);
        intent.putExtra(EXTRA_GCM_MSG, data);
        ctx.startService(intent);
    }


    /**
     * Start service when GCMListener has got new message to deliver
     *
     * @param data
     * @param ctx  The context of the caller
     */
    public static void startOnReceiveMessageFromAlert(Context ctx, Bundle data) {

        Intent intent = new Intent(ctx, NetworkService.class);
        intent.setAction(ACTION_ON_RECEIVE_ALERT_MSG);
        intent.putExtra(EXTRA_GCM_MSG, data);
        ctx.startService(intent);
    }


    /**
     * Start Get list action. This method also updates current user position
     *
     * @param context
     * @param position
     * @param radius
     */
    public static void startActionGetListAlert(Context context, Position position, int radius) {

        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_GET_ALERT_LIST);
        intent.putExtra(EXTRA_ADD_ALERT_LAT, position.getLatitude());
        intent.putExtra(EXTRA_ADD_ALERT_LONG, position.getLongitude());
        intent.putExtra(EXTRA_LIST_ALERT_RADIUS, radius);
        context.startService(intent);

    }

    /**
     * start Validate action. This methods cancel/validate  an alert
     *
     * @param context
     * @param alertID
     * @param itsFake
     */
public static void startValidateAction(Context context, String alertID, boolean itsFake,boolean imHere) {

        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_VALIDATE);
        intent.putExtra(EXTRA_LAST_ALERT_ID, alertID);
        intent.putExtra(ALERT_IS_ACTIVE, itsFake);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();
            boolean operationStatus;

            switch (action) {

                case ACTION_CHANGE_ACCESS:
                    final boolean isDirect = intent.getBooleanExtra(EXTRA_CHANGE_ACCESS, data.getBoolean(EXTRA_CHANGE_ACCESS));
                    handleActionChangeAccess(isDirect);
                    return;

                case ACTION_ADD_ALERT:
                    double latitude = intent.getDoubleExtra(EXTRA_ADD_ALERT_LAT, 0);
                    double longitude = intent.getDoubleExtra(EXTRA_ADD_ALERT_LONG, 0);
                    String name = intent.getStringExtra(EXTRA_ADD_ALERT_NAME);
                    operationStatus = handleActionAddAlert(data.getString(EXTRA_SESSION_ID), latitude, longitude, name);
                    break;

                case ACTION_STOP_ALERT:
                    operationStatus = handleActionCloseAlert(data.getString(EXTRA_SESSION_ID), data.getString(EXTRA_LAST_ALERT_ID));
                    break;

                case ACTION_CONNECT:
                    operationStatus = handleActionConnect(intent.getStringExtra(EXTRA_CONNECT_LOGGING), intent.getStringExtra(EXTRA_CONNECT_PIN));
                    Log.v(TAG, " Connection State : " + operationStatus);

                    /* only subscribe if connection is successful */
                    if (operationStatus)
                        RegistrationService.startSubscribeUserService(this, new String[]{data.getString(EXTRA_USER_ID)});

                    break;

                case ACTION_SIGNUP:

                    String phone = intent.getStringExtra(EXTRA_SIGNUP_PHONE);
                    String email = intent.getStringExtra(EXTRA_SIGNUP_EMAIL);
                    String fname = intent.getStringExtra(EXTRA_SIGNUP_FIRSTNAME);
                    String lname = intent.getStringExtra(EXTRA_SIGNUP_LASTNAME);
                    String pin = intent.getStringExtra(EXTRA_SIGNUP_PIN);
                    operationStatus = handleActionSignUp(phone, email, fname, lname, pin);

                    if(operationStatus)
                        operationStatus = handleActionConnect(email,pin);
                    break;

                case ACTION_ON_RECEIVE_USR_MSG:
                    Bundle userExtra = intent.getBundleExtra(EXTRA_GCM_MSG);
                    handleActionUserTopic(userExtra);
                    return;

                case ACTION_ON_RECEIVE_ALERT_MSG:
                    Bundle alertExtra = intent.getBundleExtra(EXTRA_GCM_MSG);
                    handleActionAlertTopic(alertExtra);
                    return;

                case ACTION_GET_ALERT_LIST:
                    double var1 = intent.getDoubleExtra(EXTRA_ADD_ALERT_LAT, 0);
                    double var2 = intent.getDoubleExtra(EXTRA_ADD_ALERT_LONG, 0);
                    int radius = intent.getIntExtra(EXTRA_LIST_ALERT_RADIUS, 0);
                    handleActionListAlert(var1, var2, radius);
                    return;

                case ACTION_VALIDATE:
                    boolean isFake = intent.getBooleanExtra(ALERT_IS_ACTIVE, false);
                    String alertID = intent.getStringExtra(EXTRA_LAST_ALERT_ID);
                    operationStatus = handleActionValidate(alertID, isFake);
                    break;

                default:
                    Log.e(TAG, "onHandleIntent error in communication protocol.");
                    return;
            }

            replyWithSuitableMethod(action, operationStatus);
        }
    }

    /**
     * Handle validate action
     *
     * @param alertID
     * @param isFake
     * @return
     */
    private boolean handleActionValidate(String alertID, boolean isFake) {
        Log.d(TAG, "handleActionValidate for alert " + alertID + " with value " + isFake);
        return stub.validateAlert(data.getString(EXTRA_SESSION_ID), alertID, isFake);
    }

    /**
     * Handle Get Alert list Action
     *
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     */
    private void handleActionListAlert(double latitude, double longitude, int radius) {

        /* Network request */
        List<String> keys = stub.getAlerts(data.getString(EXTRA_SESSION_ID), latitude, longitude, radius);

        if(keys.size() == 0)
            return;

        Log.d(TAG, "handleActionListAlert :  "+keys.toString());
        /* Get each position details */
        List<Position> positions = stub.getDesc(data.getString(EXTRA_SESSION_ID), keys.toArray(new String[keys.size()]));

        /* Send new positions*/
        onNewPositionsAvailable(true, positions);

    }


    /**
     * Displatch received message from Alert topic
     *
     * @param extra
     * @return
     */
    private boolean handleActionAlertTopic(Bundle extra) {

        final boolean isDirectAccess = data.getBoolean(EXTRA_CHANGE_ACCESS);
        final boolean isActive = Boolean.valueOf(extra.getString("isActive"));

        if (isDirectAccess) {

            Intent localIntent = new Intent(ACTION_ALERT_STATE_RES);
            localIntent.putExtra(EXTRA_RES, true);
            localIntent.putExtra(ALERT_IS_ACTIVE, isActive);
            sendLocalBroadcast(localIntent);

        } else {
            //TODO do we have to notify if an alert has been deactivated ?
        }

        return false;
    }

    /**
     * Dispatch received message from User topic
     *
     * @param extra
     * @return
     */
    private boolean handleActionUserTopic(Bundle extra) {

        final String id = extra.getString("id");
        final double lat = Double.valueOf(extra.getString("lat"));
        final double longi = Double.valueOf(extra.getString("long"));

        /* Ignore it because i'm the sender */
        if (id.equals(data.get(EXTRA_LAST_ALERT_ID))) {
            return true;
        }

        /* Get and Update position */
        //TODO replace lat and longitude by real coordinates
        //TODO it is possible to call for GPSService.CurrentLatitude and GPSService.CurrentLongitude
        final List<String> keys = stub.getAlerts(data.getString(EXTRA_SESSION_ID), lat, longi, ProtocolConstants.DEFAULT_RADIUS);

        /* Subscribe to each alerts*/
        subscriveTo(keys);

        /* Get positions */
        final List<Position> positions = stub.getDesc(data.getString(EXTRA_SESSION_ID), keys.toArray(new String[keys.size()]));
        Log.d(TAG, "handleActionUserTopic - number : " + keys.size() + " positions : " + positions);

        /* Get Access mode */
        final boolean isDirectAccess = data.getBoolean(EXTRA_CHANGE_ACCESS);

        /* we only to send back the new intent locally */
        if (isDirectAccess) {
            Log.d(TAG, "handleActionUserTopic answering in direct mode : ");
            onNewPositionsAvailable(true, positions);

        }
        /* Toolbar notification */
        else {

            /* Send a new notification*/
            Log.d(TAG, "handleActionUserTopic answering in indirect mode (notification) : " + keys.size());
            notification(this, keys.size() + DANGER_MSG, "Danger", positions);
        }

        return true;
    }

    /**
     * Sibscribe to alert topic of all given keys
     *
     * @param keys
     */
    private void subscriveTo(List<String> keys) {
        RegistrationService.startSubscribeAlertService(this, keys.toArray(new String[keys.size()]));
    }

    /**
     * Handle close alert
     *
     * @param alertID
     * @return
     */
    private boolean handleActionCloseAlert(String session, String alertID) {
        Log.v(TAG, "handleActionCloseAlert receiving new parameters...");
        return stub.closeAlert(session, alertID);
    }

    /**
     * Handle Add alert action
     *
     * @param session
     * @param latitude
     * @param longitude
     * @param name
     * @return
     */
    private boolean handleActionAddAlert(String session, double latitude, double longitude, String name) {
        Log.v(TAG, "handleActionAddAlert receiving new parameters...");
        String lastAlertID = stub.addAlert(session, latitude, longitude, name);
        data.putString(EXTRA_LAST_ALERT_ID, lastAlertID);
        return lastAlertID != null;
    }

    /**
     * Handle Signup action
     *
     * @param phone
     * @param email
     * @param fname
     * @param lname
     * @param pin
     * @return
     */
    private boolean handleActionSignUp(String phone, String email, String fname, String lname, String pin) {
        Log.v(TAG, "handleActionSignup receiving new parameters...");
        return stub.register(phone, email, fname, lname, pin);
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
        data.putBoolean(EXTRA_CHANGE_ACCESS, accessActivityDirectly);
    }

    private boolean handleActionConnect(String login, String pass) {
        Log.v(TAG, "handleActionConnect receiving new parameters...");
        String[] params = stub.connect(login, pass);
        if (params == null)
            return false;

        if(params[0] == null && params[1] == null)
            return false;

        Log.v(TAG, "User connected with session "+params[1]);

        data.putString(EXTRA_USER_ID, params[0]);
        data.putString(EXTRA_SESSION_ID, params[1]);
        return true;
    }

    /**
     * Reply by choosing suitable access (direct or not)
     *
     * @param action
     * @param success
     */
    private void replyWithSuitableMethod(String action, boolean success) {

        Intent localIntent = new Intent(action + "_RES");
        localIntent.putExtra(EXTRA_RES, success);

        /* An error occured */
        if (!success) {
            localIntent.putExtra(EXTRA_ERROR_CODE, PreferenceManager.getDefaultSharedPreferences(this).getString(stub.ERROR_CODE, "-1"));
        }

        sendLocalBroadcast(localIntent);
    }

    /**
     * Sends a local broadcast with the given intent.
     */
    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /* init stub */
        stub = new NotificationStub(this);

        /* Read persisted if available*/
        resetSettings();

        /* register receiver */
        registerReceiver();

        /* Start registration service */
        RegistrationService.startRegisterService(this);
    }


    /**
     * Register BroadCast Receiver for GCM token
     */
    private void registerReceiver() {

        if (!data.getBoolean(APP_IS_REGISTERED)) {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            data.putBoolean(APP_IS_REGISTERED, true);
                            Log.v(TAG, "GCM sent Token.... OK");
                        }
                    },
                    new IntentFilter(ProtocolConstants.REGISTRATION_COMPLETE));
        }
    }

    /**
     * Persist  bundle to ensure persistence of settings
     */
    @Override
    public void onDestroy() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(EXTRA_SESSION_ID, data.getString(EXTRA_SESSION_ID)).apply();
        sharedPreferences.edit().putString(EXTRA_USER_ID, data.getString(EXTRA_USER_ID)).apply();
        sharedPreferences.edit().putBoolean(EXTRA_CHANGE_ACCESS, data.getBoolean(EXTRA_CHANGE_ACCESS)).apply();
        sharedPreferences.edit().commit();

    }

    /**
     * Read settings from preferences
     */
    private void resetSettings() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        data.putString(EXTRA_SESSION_ID, sharedPreferences.getString(EXTRA_SESSION_ID, null));
        data.putString(EXTRA_USER_ID, sharedPreferences.getString(EXTRA_USER_ID, null));
        data.putBoolean(EXTRA_CHANGE_ACCESS, sharedPreferences.getBoolean(EXTRA_CHANGE_ACCESS, false));

    }

    /**
     * Create intent for sending new positions
     *
     * @param operationsState
     * @param positions
     */
    private void onNewPositionsAvailable(boolean operationsState, List<Position> positions) {

        Intent intent = new Intent(ACTION_GET_ALERT_LIST_RES);
        intent.putExtra(EXTRA_RES, operationsState);
        intent.putParcelableArrayListExtra(EXTRA_LIST_POSITIONS, new ArrayList<>(positions));
        sendLocalBroadcast(intent);
    }

    /**
     * display a new notification on toolbar
     *
     * @param ctx     the context of the caller
     * @param message the message to display
     */
    public void notification(Context ctx, String title, String message, List<Position> positions) {
        Intent intent = new Intent(ctx, MapsActivity.class);
        intent.setAction(ACTION_GET_ALERT_LIST_RES);
        intent.putExtra(EXTRA_RES, true);
        intent.putParcelableArrayListExtra(EXTRA_LIST_POSITIONS, new ArrayList<>(positions));

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx)
                //TODO Must define shared icon
                .setSmallIcon(android.support.v7.appcompat.R.drawable.abc_switch_thumb_material)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

}
