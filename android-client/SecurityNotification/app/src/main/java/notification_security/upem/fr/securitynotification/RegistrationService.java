package notification_security.upem.fr.securitynotification;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import notification_security.upem.fr.securitynotification.network.ProtocolConstants;

/**
 * @author Yann BILISSOR
 * @version 1.0
 *          <p> This class handles all operations of registration
 *          required by Google Cloud Messaging.</p>
 */
public class RegistrationService extends IntentService {

    private static final String TAG = "RegIntentService";

    //Available topics
    public static final String ALERT_TOPIC = "/topics/alert-";
    public static final String USER_TOPIC = "/topics/user-";

    //Token definition
    private static final String TOKEN_SENT = "Token sent";
    private static final String TOPIC_PROPERTY = "topic";
    public static final String TOKEN_NOT_REGISTERED = "notregistered";
    public static final String TOPIC_NAME = "TP_NAME";
    public static final String SUBSCRIPTION_FAILED = "fr.upem.securitynotification.network.res.SUBSCRIPTION_FAILED";

    //available actions
    private static final String ACTION_SUBSCRIBE = "fr.upem.securitynotification.network.action.SUBSCRIBE";
    public static final String ACTION_REGISTRATION = "fr.upem.securitynotification.network.action.REGISTRATION";

    //Token ID sent by  GCM server
    private static Bundle data = new Bundle();


    public RegistrationService() {
        super(TAG);
    }

    /**
     * Start service by subscribing to a new topic from alert
     *
     * @param context
     * @param keys
     */
    public static void startSubscribeAlertService(Context context, String[] keys) {

        Intent intent = new Intent(context, RegistrationService.class);
        intent.setAction(ACTION_SUBSCRIBE);
        intent.putExtra(TOPIC_NAME, ALERT_TOPIC);
        intent.putExtra(TOPIC_PROPERTY,keys);
        context.startService(intent);

    }

    /**
     * Start service by subscribing to a new topic from user
     *
     * @param context
     * @param keys
     */
    public static void startSubscribeUserService(Context context, String[] keys) {

        Intent intent = new Intent(context, RegistrationService.class);
        intent.setAction(ACTION_SUBSCRIBE);
        intent.putExtra(TOPIC_NAME, USER_TOPIC);
        intent.putExtra(TOPIC_PROPERTY, keys);
        context.startService(intent);

    }

    /**
     * Start registration service
     *
     * @param context
     */
    public static void startRegisterService(Context context) {
        Intent intent = new Intent(context, RegistrationService.class);
        intent.setAction(ACTION_REGISTRATION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null)
            return;

        String action = intent.getAction();

        switch (action) {

            case ACTION_SUBSCRIBE:
                handleSubscription(intent.getStringExtra(TOPIC_NAME),intent.getStringArrayExtra(TOPIC_PROPERTY));
                break;

            case ACTION_REGISTRATION:
                handleRegistrationID();
                break;
            default:
                Log.v(TAG, " Register request sent");
        }

    }

    /**
     * Try Getting TOKEN ID from GCM
     */
    private void handleRegistrationID() {


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token;

        try {

            //Ask for Unique Registration ID
            //This token is unique and must be done once for all
            InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(ProtocolConstants.SERVER_TOKEN_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);


            // We should store a boolean that indicates whether the generated token has been
            // sent to the server.
            sharedPreferences.edit().putBoolean(TOKEN_SENT, true).apply();

            //Save token
            data.putString(TOKEN_SENT, token);


        } catch (IOException ex) {
            Log.v(TAG, "Failed to complete token refresh", ex);
            sharedPreferences.edit().putBoolean(TOKEN_SENT, false).apply();

            /* Must return NOT_REGISTERED Intent*/
            return;

        }


        Intent reg = new Intent(ProtocolConstants.REGISTRATION_COMPLETE);
        reg.putExtra(ProtocolConstants.REGISTRATION_TOKEN_NAME, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(reg);
        Log.v(TAG, "Token is " + token);
    }


    /**
     * Subscribe to given topic
     *
     * @param topic
     * @throws IOException
     */
    private void handleSubscription(String topic,String...keys) {

        //Not registered
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasGotToken = sharedPreferences.getBoolean(TOKEN_SENT, false);

        if (!hasGotToken) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(TOKEN_NOT_REGISTERED));
            return;
        }

        try {

            GcmPubSub pubSub = GcmPubSub.getInstance(this);

            for(String key:keys) {

                pubSub.subscribe(data.getString(TOKEN_SENT), topic + key, null);
                Log.v(TAG, "Subscription successful for topic " + topic);

            }

        } catch (IOException e) {
            Log.v(TAG, "Subscription failed for topic " + topic);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(SUBSCRIPTION_FAILED));
        }

    }


}
