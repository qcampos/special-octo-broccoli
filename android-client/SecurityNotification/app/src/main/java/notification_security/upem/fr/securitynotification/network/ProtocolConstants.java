package notification_security.upem.fr.securitynotification.network;

/**
 * Stores some protocol constants setting our view.
 */
public class ProtocolConstants {
    // For inputs.
    public static final int PIN_LENGTH = 4;
    // For default values.
    public static final int RADIUS1 = 100;
    public static final int RADIUS2 = 200;
    public static final int RADIUS3 = 300;
    public static final int DEFAULT_RADIUS = RADIUS1;
    // For preferences.
    public static final String RADIUS_KEY = "RADIUS";
    public static final String IS_ALERTING_KEY = "IS_ALERTED";
    public static final String LOGIN = "LOGIN";
    public static final String PIN = "PIN";
    public static final String UNSET_PREFERENCE = "__UNSET__";

    //Server Token ID
    public static String SERVER_TOKEN_ID ="191695741360";
    public static String REGISTRATION_COMPLETE="Obtained Token";
    public static String REGISTRATION_NOT_COMPLETE="not Obtained Token";
    public static String REGISTRATION_TOKEN_NAME="TOKEN_NAME";

}
