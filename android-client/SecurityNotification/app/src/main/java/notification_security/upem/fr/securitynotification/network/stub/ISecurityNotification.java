package notification_security.upem.fr.securitynotification.network.stub;

import java.util.List;

import notification_security.upem.fr.securitynotification.geolocalisation.Position;

/**
 * Created by algorithmia on 05/03/2016.
 */
public interface ISecurityNotification {

    /**
     * Register new user
     * @param phone
     * @param mail
     * @param firstName
     * @param lastName
     * @param pin
     * @return
     */
     boolean register(String phone, String mail, String firstName, String lastName, String pin);

    /**
     * Connect new User
     * @param login
     * @param password
     * @return the session uuid
     */
     String[] connect(String login, String password);

    /**
     * Check whether or not an email is available
     * @param email
     * @return
     */
     boolean isEmailAvailable(String email);

    /**
     * Check if phone is available or not
     * @param phone
     * @return
     */
    boolean isPhoneAvailable(String phone);


    /**
     * Retrives all alerts position around given circle
     * @param uuid
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     */
    List<String> getAlerts(String uuid, double latitude, double longitude, long radius);

    /**
     * Retrieves informations about given alerts ID
     * @param alertsID
     * @param uuid
     * @return
     */
    List<Position> getDesc(String uuid, String... alertsID);


    /**
     * Add new alert
     * @param uuid
     * @param latitude
     * @param longitude
     * @param name
     * @return alert ID
     */
    String addAlert(String uuid, double latitude, double longitude, String name);


    /**
     * Close given alert ID
     * @param uuid
     * @param alertID
     * @return
     */
    boolean closeAlert(String uuid, String alertID);


    /**
     * Validate given alert ID
     * @param uuid
     * @param alertID
     * @param state
     * @return
     */
    boolean validateAlert(String uuid, String alertID, boolean state);


}
