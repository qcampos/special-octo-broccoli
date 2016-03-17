package notification_security.upem.fr.securitynotification.network.stub;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import notification_security.upem.fr.securitynotification.geolocalisation.Position;


/**
 * Created by algorithmia on 05/03/2016.
 */
public class NotificationStub implements ISecurityNotification {


    // List of ACTION, and their EXTRA constants keys this service can perform.
    private static final String TAG = NotificationStub.class.getSimpleName();
    private static final String SERVER_URL = "http://vps253024.ovh.net";
    private static final String REGISTER_REQUEST = "/users/register";
    private static final String LOGIN_REQUEST = "/users/login";
    private static final String CHECK_MAIL_REQUEST = "/users/check/mail";
    private static final String CHECK_PHONE_REQUEST = "/users/check/phone";
    private static final String GET_ALERTS_FROM_AREA_REQUEST = "/alerts/getlist";
    private static final String GET_ALERTS_EVENTS = "/alerts/get";
    private static final String ADD_ALERT_REQUEST = "/alerts/add";
    private static final String CLOSE_EVENT_REQUEST = "/alerts/close";
    private static final String VALIDATE_EVENT_REQUEST = "/alerts/validate";
    public static final String ERROR_CODE = SERVER_URL + "/ERROR_CODE";
    public static final String ERROR_NAME = SERVER_URL + "/ERROR_NAME";
    private Context context;
    private LinkedHashMap<String, Object> values;

    /**
     * @param ctx
     */
    public NotificationStub(Context ctx) {

        this.context = ctx;
    }

    @Override
    public boolean register(String phone, String mail, String firstName, String lastName, String pin) {

        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + REGISTER_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createRegisterAsJSON(phone, mail, firstName, lastName, pin));
            return getOperationState(connection);

        } catch (IOException e) {
            Log.v(TAG, "[STUB-REGISTER]", e);

        }

        return false;
    }

    @Override
    public String[] connect(String login, String password) {

        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + LOGIN_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createConnectAsJSON(login, password));
            RequestFactory.DataReader result = getServerResponse(connection);
            return new String[]{result.uuid, result.session};

        } catch (IOException e) {
            Log.v(TAG, "[STUB-CONNECT (" + login + "," + password + ") ]", e);
        }


        return null;
    }

    @Override
    public boolean isEmailAvailable(String email) {

        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + CHECK_MAIL_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createCheckEmailRequest(email));
            return getOperationState(connection);

        } catch (IOException e) {
            Log.v(TAG, "[STUB-CHECK-MAIL]", e);
        }

        return false;
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + CHECK_PHONE_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createCheckPhoneRequest(phone));
            return getOperationState(connection);

        } catch (IOException e) {
            Log.v(TAG, "[STUB-CHECK-PHONE]", e);
        }

        return false;
    }

    @Override
    public List<String> getAlerts(String uuid, double latitude, double longitude, long radius) {


        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + GET_ALERTS_FROM_AREA_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createGetAlertsRequest(uuid, latitude, longitude, radius));

            if (isSuccessfulRequest(connection)) {

                /* Map JSON to Java Object*/
                List<LinkedHashMap> items = RequestFactory.mapValuesToCollection(connection.getInputStream(), LinkedHashMap.class);
                List<String> res = new ArrayList<>();

                /* Map Entry to ID. No java 8 */
                for (LinkedHashMap<String, String> tmp : items) {
                    res.add(tmp.get("id"));
                }

                return res;
            }

        } catch (IOException e) {
            Log.v(TAG, "[STUB-GET-ALERTS]", e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<Position> getDesc(String uuid, String... alertsID) {

        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + GET_ALERTS_EVENTS, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createDescAlertRequest(uuid, alertsID));

            if (isSuccessfulRequest(connection)) {

                String value = IOHelper.readFully(connection.getInputStream());
                LinkedHashMap<String, LinkedHashMap<String, Object>> events = RequestFactory.mapValue(value, LinkedHashMap.class);
                List<RequestFactory.Event> res = new ArrayList<>();

                for (String key : alertsID) {

                    LinkedHashMap<String, Object> values = events.get(key);
                    RequestFactory.Event e = new RequestFactory.Event();

                    e.eventID = values.get("id").toString();
                    e.hasVoted = Boolean.valueOf(values.get("hasVoted") + "");
                    e.latitude = Double.valueOf(values.get("lat").toString());
                    e.longitude = Double.valueOf(values.get("long").toString());
                    e.score = Double.valueOf(values.get("score") + "");
                    res.add(e);

                }

                return mapEventToPosition(res);

            }

        } catch (IOException e) {
            Log.v(TAG, "[STUB-GET-ALERTS]", e);
        }

        return Collections.emptyList();
    }

    /**
     * Map Each private Event Object to Shared Position model
     *
     * @param events the list of events
     * @return the mapped list
     */
    private List<Position> mapEventToPosition(List<RequestFactory.Event> events) {

        List<Position> positions = new ArrayList<>();

        for (RequestFactory.Event event : events) {
            positions.add(new Position(event.latitude, event.longitude, event.eventID, event.hasVoted));
        }

        return positions;
    }

    @Override
    public String addAlert(String uuid, double latitude, double longitude, String name) {

        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + ADD_ALERT_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createAddAlertRequest(uuid, latitude, longitude, name));
            RequestFactory.DataReader reader = getServerResponse(connection);
            return reader.alertID;

        } catch (IOException e) {
            Log.v(TAG, "[STUB-ADDALERT (" + latitude + "," + longitude + ") ]", e);
        }

        return null;
    }

    @Override
    public boolean closeAlert(String uuid, String alertID) {

        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + CLOSE_EVENT_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createCloseAlertRequest(uuid, alertID));
            return getOperationState(connection);

        } catch (IOException e) {
            Log.v(TAG, "[STUB-CLOSE-ALERT]", e);
        }
        return false;
    }

    @Override
    public boolean validateAlert(String uuid, String alertID, boolean state) {

        try {

            HttpURLConnection connection = IOHelper.openConnection(SERVER_URL + VALIDATE_EVENT_REQUEST, IOHelper.POST);
            IOHelper.post(connection, RequestFactory.createCloseAlertRequest(uuid, alertID));
            return getOperationState(connection);

        } catch (IOException e) {
            Log.v(TAG, "[STUB-VALIDATE-ALERT]", e);
        }
        return false;
    }

    /**
     * Check if requests goes well
     *
     * @param connection
     * @return
     * @throws IOException
     */
    private boolean getOperationState(HttpURLConnection connection) throws IOException {

        if (!isSuccessfulRequest(connection))
            return false;

        RequestFactory.DataReader reader = RequestFactory.readDataFrom(connection.getInputStream());
        return reader.success;

    }

    /**
     * Check if latest request goes well
     *
     * @param connection
     * @return
     * @throws IOException
     */
    private boolean isSuccessfulRequest(HttpURLConnection connection) throws IOException {

        if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
            RequestFactory.DataReader reader = RequestFactory.readDataFrom(connection.getErrorStream());
            persistErrors(context, reader.error.code, reader.error.message);
            return false;
        }

        return true;
    }

    /**
     * Reads data from given connection. If operation was successful then then read from
     * InputStream otherwise reads from ErrorStream
     *
     * @param connection
     * @return
     * @throws IOException
     */
    private RequestFactory.DataReader getServerResponse(HttpURLConnection connection) throws IOException {

        if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
            RequestFactory.DataReader reader = RequestFactory.readDataFrom(connection.getErrorStream());
            Log.v(TAG, reader.error.message);
            persistErrors(context, reader.error.code, reader.error.message);
            return reader;
        }

        return RequestFactory.readDataFrom(connection.getInputStream());
    }


    /**
     * Log errors for observer to retrieves
     *
     * @param code  the errors code
     * @param value the value of errors
     */
    private void persistErrors(Context context, String code, String value) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(ERROR_CODE, code).apply();
        sharedPreferences.edit().putString(ERROR_NAME, value).apply();
    }

    /**
     * This internal class performs IO operations
     */
    private static class IOHelper {

        // Default HHTP Verbs
        static final String POST = "POST";
        static final String GET = "GET";

        /**
         * Opens new remote connections
         *
         * @param urlStr the server's address
         * @return
         */

        static HttpURLConnection openConnection(String urlStr, String method) throws IOException {

            URL url = new URL(urlStr);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod(method);
            httpConn.setDoOutput(true);
            return httpConn;
        }


        /**
         * Read all text from input stream
         *
         * @param in
         * @return
         */
        static String readFully(InputStream in) throws IOException {

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String rawData;

            while ((rawData = reader.readLine()) != null) {
                builder.append(rawData);
            }

            return builder.toString();
        }

        /**
         * Post new request with given params
         *
         * @param url
         * @param query
         * @throws IOException
         */
        static void post(String url, String query) throws IOException {

            HttpURLConnection connection = IOHelper.openConnection(url, IOHelper.POST);
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(query);
            out.close();
            connection.disconnect();
        }


        /**
         * Post new request using given HttpConnection
         *
         * @param connection
         * @throws IOException
         */
        static void post(HttpURLConnection connection, String args) throws IOException {

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(args);
            out.flush();
        }

    }
}
