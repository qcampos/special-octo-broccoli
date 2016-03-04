package notification_security.upem.fr.securitynotification;

import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Fragments managing
 */
public class ConnectionFragment extends Fragment {

    // Views.
    private EditText etLoging;
    private EditText etPin;
    private Button btConnect;
    private TextView tvNewAccount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Update after the activity has finished to load itself.
        // Set adapter, get view by ids...
        // getActivity and so on.
        // Getting local views.
        setLocalViews();

    }

    private void setLocalViews() {
        View view = getView();
        etLoging = (EditText) view.findViewById(R.id.connection_etLogin);
        etPin = (EditText) view.findViewById(R.id.connection_etPin);
        btConnect = (Button) view.findViewById(R.id.connection_btConnect);
        tvNewAccount = (TextView) view.findViewById(R.id.connection_tvNewAccount);
    }
}
