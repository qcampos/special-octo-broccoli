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

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.ViewUtilities;
import notification_security.upem.fr.securitynotification.home.FragmentReceiver.BaseFragmentReceiver;
import notification_security.upem.fr.securitynotification.network.NetworkService;
import notification_security.upem.fr.securitynotification.network.ProtocolConstants;

import static notification_security.upem.fr.securitynotification.ViewUtilities.showShortToast;

/**
 * Fragment handling Sign up logic.
 */
public class SignUpFragment extends BaseFragmentReceiver {

    // The logging tag.
    private static final String TAG = SignUpFragment.class.getSimpleName();

    // Views.
    private EditText etFirstname;
    private EditText etLastname;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPIN;
    private Button btCreate;
    private TextView tvUseExistingAccount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
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
    public String getFilteredAction() {
        return NetworkService.ACTION_SIGNUP_RES;
    }


    /**
     * Fetches all views local to this very fragment.
     */
    private void setLocalViews() {
        View view = getView();
        if (view != null) {
            etFirstname = (EditText) view.findViewById(R.id.signUp_etFirstname);
            etLastname = (EditText) view.findViewById(R.id.signUp_etLastname);
            etEmail = (EditText) view.findViewById(R.id.signUp_etMail);
            etPhone = (EditText) view.findViewById(R.id.signUp_etPhone);
            etPIN = (EditText) view.findViewById(R.id.signUp_etPIN);
            btCreate = (Button) view.findViewById(R.id.signUp_btCreate);
            tvUseExistingAccount = (TextView) view.findViewById(R.id.signUp_etUseExistingAccount);
            return;
        }
        Log.e(TAG, "setLocalViews - can not retrieve the enclosing view.");
    }

    @Override
    void performNetworkRequest(HomeActivity homeActivity, String... params) {
        // Order firstName, lastName, email, phone, pin.
        NetworkService.startSignUpAction(homeActivity, params[0], params[1], params[2], params[3], params[4]);
    }

    @Override
    void disableFields() {
        // No iterable or so, because sometimes we have specific actions to do.
        tvUseExistingAccount.setEnabled(false);
        etFirstname.setEnabled(false);
        etLastname.setEnabled(false);
        etEmail.setEnabled(false);
        etPhone.setEnabled(false);
        etPIN.setEnabled(false);
        btCreate.setEnabled(false);
        tvUseExistingAccount.setEnabled(false);
    }

    @Override
    void enableFields() {
        // No iterable or so, because sometimes we have specific actions to do.
        etFirstname.setEnabled(true);
        etLastname.setEnabled(true);
        etEmail.setEnabled(true);
        etPhone.setEnabled(true);
        etPIN.setEnabled(true);
        btCreate.setEnabled(true);
    }

    private void setClickListeners() {
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = etFirstname.getText().toString().trim();
                String lastName = etFirstname.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String pin = etPIN.getText().toString().trim();
                if (!validateTFInputs(firstName, lastName, email, phone, pin)) return;
                requestNetworkAction(firstName, lastName, email, phone, pin);
            }
        });

        tvUseExistingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHomeActivity().showFragment(new ConnectionFragment());
            }
        });
    }

    private boolean validateTFInputs(String firstName, String lastName, String email, String phone, String pin) {
        Activity activity = getHomeActivity();
        // Verifying contents.
        if (ViewUtilities.areEmpty(firstName, lastName, email, phone, pin)) {
            showShortToast(activity, "Veuillez remplir tous les champs");
            return false;
        }
        // Verifying sizes.
        if (pin.length() < ProtocolConstants.PIN_LENGTH) {
            showShortToast(activity, "PIN nécessite 4 chiffres");
            return false;
        }
        return true;
    }
}
