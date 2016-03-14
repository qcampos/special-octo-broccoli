package notification_security.upem.fr.securitynotification.network.stub;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BILISSOR YANN
 * @version 1.0
 *
 *          This class provides methods for converting network requests to JSON
 *          format in order to call REST API. It uses Jackson-core and Jackson-databind
 *          to perform all its operations. This class also contains inner classes for binding
 *          purposes.
 */
class RequestFactory {

    /* This object is used to serialize or deserialize */
    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     * Map value from input stream to given class
     *
     * @param in
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> List<T> mapValuesToCollection(InputStream in, Class<T> internal) throws IOException {
        JavaType type = mapper.getTypeFactory().constructType(List.class, internal);
        return mapper.readValue(in, type);
    }

    /**
     * Create new JSON output based on given parameters for register request
     *
     * @param phone
     * @param mail
     * @param firstName
     * @param lastName
     * @param pin
     * @return
     */
    static String createRegisterAsJSON(String phone, String mail, String firstName, String lastName, String pin) throws JsonProcessingException {
        return mapper.writeValueAsString(new User(phone, mail, lastName, firstName, pin));
    }

    /**
     * Create new JSON output based on given params for connect request
     *
     * @param login
     * @param password
     * @return
     */
    static String createConnectAsJSON(String login, String password) throws JsonProcessingException {
        return mapper.writeValueAsString(new Credential(login, password));
    }

    /**
     * Read data from given stream and returns it. @DataReader is shared ONLY
     * into stub package
     *
     * @param stream
     * @return
     * @throws IOException
     */
    static DataReader readDataFrom(InputStream stream) throws IOException {
        return mapper.readValue(stream, DataReader.class);
    }

    /**
     * Create check request. Must be replaced with reflexion API
     *
     * @param field
     * @param value
     * @return
     */
    private static String createUserCheckRequest(String field, String value) throws JsonProcessingException {

        //Template user for fitting as well request
        User template = new User();

        switch (field) {

            case "mail":
                template.setPhone(value);
                break;
            case "phone":
                template.setPhone(value);
                break;
            default:
                throw new IllegalArgumentException("Undefined field given");
        }

        return mapper.writeValueAsString(template);
    }


    /**
     * Create Check mail request as JSON
     *
     * @param value
     * @return
     * @throws JsonProcessingException
     */
    public static String createCheckEmailRequest(String value) throws JsonProcessingException {
        return createUserCheckRequest("mail", value);
    }

    /**
     * CReate check phone request as JSON
     *
     * @param value
     * @return
     * @throws JsonProcessingException
     */
    public static String createCheckPhoneRequest(String value) throws JsonProcessingException {
        return createUserCheckRequest("phone", value);
    }

    /**
     * Create Get alerts request as JSON
     *
     * @param session
     * @param lat
     * @param longitude
     * @param radius
     * @return
     * @throws JsonProcessingException
     */
    public static String createGetAlertsRequest(String session, double lat, double longitude, long radius) throws JsonProcessingException {
        return mapper.writeValueAsString(new Event(session, lat, longitude, 0, 0, radius));
    }

    /**
     * Create Add alert request as JSON
     *
     * @param uuid
     * @param latitude
     * @param longitude
     * @param name
     * @return
     * @throws JsonProcessingException
     */
    public static String createAddAlertRequest(String uuid, double latitude, double longitude, String name) throws JsonProcessingException {
        return mapper.writeValueAsString(new Event(uuid, name, latitude, longitude));
    }


    /**
     * Create close alert request as JSON
     *
     * @param session
     * @param alertID
     * @return
     * @throws JsonProcessingException
     */
    public static String createCloseAlertRequest(String session, String alertID) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("session", session);
        map.put("alert", alertID);
        return mapper.writeValueAsString(map);
    }

    /**
     * Create Validate Request
     *
     * @param uuid
     * @param alertID
     * @param state
     * @return
     */
    public static String createValidateRequest(String uuid, String alertID, boolean state) throws JsonProcessingException {

        Map<String, Object> map = new HashMap<>();
        map.put("session", uuid);
        map.put("alert", alertID);
        map.put("validate", state);
        return mapper.writeValueAsString(map);
    }

    /**
     * Create Get desc alert request
     *
     * @param uuid
     * @param values
     * @return
     * @throws JsonProcessingException
     */
    public static String createDescAlertRequest(String uuid, String... values) throws JsonProcessingException {

        Map<String, Object> map = new HashMap<>();
        map.put("session", uuid);

        List<Map<String, String>> keys = new ArrayList<>();

        for (String id : values) {

            Map<String, String> alertsKeys = new HashMap<>();
            alertsKeys.put("id", id);
            keys.add(alertsKeys);
        }

        map.put("alerts", keys);

        return mapper.writeValueAsString(map);
    }


    /**
     * Details of implementation. All classes below are Hidden from outside package
     * an exists only to make easy translation from java Object to JSON String
     */
    static class User {

        private String phone;
        private String mail;
        private String last_name;
        private String first_name;
        private String pin;

        public User() {
        }

        public User(String phone, String email, String lastName, String firstName, String pin) {
            this.phone = phone;
            this.mail = email;
            this.last_name = lastName;
            this.first_name = firstName;
            this.pin = pin;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getPin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }
    }


    /**
     * Represent a user auth
     */
    static class Credential {

        private String login;
        private String pass;

        public Credential(String login, String password) {
            this.login = login;
            this.pass = password;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }
    }


    /**
     * represent any http request response. The only class shared in package
     * Fields is public because it is an hidden classes. Plus Jackson need it
     * to perform serialization
     */
    static class DataReader {

        public boolean success;
        public String expire;
        public String session;
        public Error error;

        @JsonProperty("user")
        public String uuid;

        @JsonProperty("id")
        public String alertID;

        public DataReader() {
        }

    }

    /**
     * Represents a servor Error
     */
     static class Error {

        public String code;
        public String message;
        public Map<String, String> data;

        @Override
        public String toString() {
            return " CODE ERROR " + code + " : " + message;
        }
    }

    /**
     * Data transfert object used by Jackson to parse JSON
     * <p/>
     * Fields is public because it is an hidden classes. Plus Jackson need it
     * to perform serialization
     */
    static class Event {

        public String author;
        public String name;
        public String session;
        public boolean hasVoted;

        @JsonProperty("lat")
        public double latitude;

        @JsonProperty("long")
        public double longitude;
        public double distance;
        public double score;
        public long radius;
        public String eventID;


        public Event() {
        }

        public Event(String session, double latitude, double longitude, double distance, double score, long radius) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.distance = distance;
            this.score = score;
            this.radius = radius;
            this.session = session;
        }

        public Event(String session, String name, double latitude, double longitude) {
            this.session = session;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

    }
}
