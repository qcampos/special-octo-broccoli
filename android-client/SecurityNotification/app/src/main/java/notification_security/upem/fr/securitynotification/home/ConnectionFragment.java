package notification_security.upem.fr.securitynotification.home;

import android.app.Activity;
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

import notification_security.upem.fr.securitynotification.home.FragmentReceiver.BaseFragmentReceiver;
import notification_security.upem.fr.securitynotification.network.ProtocolConstants;
import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.network.NetworkService;

import static notification_security.upem.fr.securitynotification.ViewUtilities.showShortToast;

/**
 * Fragment handling Connection logic.
 */
public class ConnectionFragment extends BaseFragmentReceiver {

    // The logging TAG.
    private static final String TAG = ConnectionFragment.class.getSimpleName();

    // Views.
    private EditText etLogin;
    private EditText etPin;
    private Button btConnect;
    private TextView tvNewAccount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // We have to use false to attache the current fragment to the desired homeActivity.
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Getting local views.
        setLocalViews();
        // Setting listeners.
        setClickListeners();
    }


    @Override
    public void performNetworkRequest(HomeActivity homeActivity, String... params) {
        String login = params[0];
        String pin = params[1];
        NetworkService.startConnectAction(homeActivity, login, pin);
    }

    @Override
    public String getFilteredAction() {
        return NetworkService.ACTION_CONNECT_RES;
    }

    @Override
    void processNetworkResult(HomeActivity homeActivity, Intent intent) {
        boolean result = intent.getBooleanExtra(NetworkService.EXTRA_RES, false);
        // Incorrect informations.
        if (!result) {
            showShortToast(homeActivity, "Informations incorrectes");
            stopWaitingNetworkResult();
            return;
        }
        // Correct informations. We can pass to the HomeIdleFragment.
        homeActivity.showFragment(new HomeIdleFragment());
    }

    @Override
    void disableFields() {
        etPin.setEnabled(false);
        etLogin.setEnabled(false);
        tvNewAccount.setEnabled(false);
        btConnect.setEnabled(false);
        btConnect.setText("CONNEXION...");
    }


    @Override
    void enableFields() {
        etPin.setEnabled(true);
        etLogin.setEnabled(true);
        tvNewAccount.setEnabled(true);
        btConnect.setEnabled(true);
        btConnect.setText("SE CONNECTER");
    }


    /**
     * Fetches all views local to this very fragment.
     */
    private void setLocalViews() {
        View view = getView();
        etLogin = (EditText) view.findViewById(R.id.connection_etLogin);
        etPin = (EditText) view.findViewById(R.id.connection_etPin);
        btConnect = (Button) view.findViewById(R.id.connection_btConnect);
        tvNewAccount = (TextView) view.findViewById(R.id.connection_tvNewAccount);
    }


    /**
     * Sets the click listener.
     */
    private void setClickListeners() {
        setConnectButtonListener();
        tvNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHomeActivity().showFragment(new SignUpFragment());
            }
        });
    }

    /**
     * Sets the connect button listener.
     */
    private void setConnectButtonListener() {
        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieving logging and pin fields.
                String logging = etLogin.getText().toString().trim();
                String pin = etPin.getText().toString().trim();
                Log.v(TAG, "Connect button onClick (logging : " + logging + " - pin : " + pin + ")");
                // Checking if fields are correct, aborting if not.
                if (validateTFInputs(logging, pin)) return;
                // Now requesting the connection and updating the button accordingly.
                requestNetworkAction(logging, pin);
            }
        });
    }

    /**
     * Parses text fields inputs value. Sends toast if any error
     * are detected.
     *
     * @return true if every fields are valid, false otherwise.
     */
    private boolean validateTFInputs(String logging, String pin) {
        Activity activity = getActivity();
        if (logging.isEmpty() || pin.isEmpty()) {
            showShortToast(activity, "Veuillez remplir tous les champs");
            return true;
        }
        // Checking the minimal length.
        if (pin.length() < ProtocolConstants.PIN_LENGTH) {
            showShortToast(activity, "PIN nécessite 4 chiffres");
            return true;
        }
        return false;
    }

}
