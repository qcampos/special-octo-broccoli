package notification_security.upem.fr.securitynotification;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import notification_security.upem.fr.securitynotification.network.NetworkService;

/**
 * Fragments managing
 */
public class ConnectionFragment extends Fragment implements FragmentReceiver {

    // The logging TAG.
    private static final String TAG = ConnectionFragment.class.getSimpleName();

    // Views.
    private EditText etLogging;
    private EditText etPin;
    private Button btConnect;
    private TextView tvNewAccount;

    // LifeCycle variables.
    private FragmentState state;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Starting this fragment's life in IDLE state.
        state = FragmentState.IDLE;
        // Getting local views.
        setLocalViews();
        // Setting listeners.
        setClickListeners();
    }


    /**
     * Fetches all views local to this very fragment.
     */
    private void setLocalViews() {
        View view = getView();
        etLogging = (EditText) view.findViewById(R.id.connection_etLogin);
        etPin = (EditText) view.findViewById(R.id.connection_etPin);
        btConnect = (Button) view.findViewById(R.id.connection_btConnect);
        tvNewAccount = (TextView) view.findViewById(R.id.connection_tvNewAccount);
    }


    /**
     * Sets the click listener.
     */
    private void setClickListeners() {
        setConnectButtonListener();
    }

    /**
     * Sets the connect button listener.
     */
    private void setConnectButtonListener() {
        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieving logging and pin fields.
                String logging = etLogging.getText().toString().trim();
                String pin = etPin.getText().toString().trim();
                Log.v(TAG, "Connect button onClick (logging : " + logging + " - pin : " + pin + ")");
                // Checking if fields are correct, aborting if not.
                if (parseTFInputs(logging, pin)) return;
                // Now requesting the connection and updating the button accordingly.
                startConnectionState(logging, pin);
            }
        });
    }

    /**
     * Performs a connection request on the NetworkService
     * with the given parameters.
     * It disables the connect button and put the current fragment
     * in a WAITING_NETWORK_RESULT state.
     */
    private void requestConnection(String logging, String pin) {
        NetworkService.startConnectAction(getActivity(), logging, pin);
    }

    /**
     * Starts a connection request with the given parameters. It updates the view accordingly.
     */
    private void startConnectionState(String logging, String pin) {
        // Guarding the request.
        if (state == FragmentState.WAITING_NETWORK_RESULT) {
            return;
        }
        state = FragmentState.WAITING_NETWORK_RESULT;
        // Requesting the connection to the network.
        requestConnection(logging, pin);
        // Setting button states.
        disableFields();
    }


    @Override
    public void onReceiveNetworkIntent(Intent intent) {
        // TODO discard guard on state.
        Log.d(TAG, "onReceiveNetworkIntent - receiving a response intent.");
    }

    /**
     * Parses text fields inputs value. Sends toast if any error
     * are detected.
     *
     * @return true if every fields are valid, false otherwise.
     */
    private boolean parseTFInputs(String logging, String pin) {
        Activity activity = getActivity();
        if (logging.isEmpty() || pin.isEmpty()) {
            ViewUtilities.showShortToast(activity, "Veuillez remplir tous les champs");
            return true;
        }
        // Checking the minimal length.
        if (pin.length() < ProtocolConstants.PIN_LENGTH) {
            ViewUtilities.showShortToast(activity, "PIN nécessite 4 chiffres");
            return true;
        }
        return false;
    }

    private void disableFields() {
        etPin.setEnabled(false);
        etLogging.setEnabled(false);
        btConnect.setEnabled(false);
        btConnect.setText("CONNEXION...");
    }
}
