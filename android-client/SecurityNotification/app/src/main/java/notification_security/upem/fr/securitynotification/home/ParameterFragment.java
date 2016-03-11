package notification_security.upem.fr.securitynotification.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.home.FragmentReceiver.BaseFragmentReceiver;
import notification_security.upem.fr.securitynotification.network.ProtocolConstants;

/**
 * Fragment handling parameter logic.
 */
public class ParameterFragment extends BaseFragmentReceiver {

    private static final String TAG = ParameterFragment.class.getSimpleName();
    private CheckBox cb1;
    private CheckBox cb2;
    private CheckBox cb3;
    private TextView tvText1;
    private TextView tvText2;
    private TextView tvText3;
    private Button btSave;
    private TextView tvCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parameter, container, false);
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
    void performNetworkRequest(HomeActivity homeActivity, String... params) {

    }

    @Override
    void processNetworkResult(HomeActivity homeActivity, Intent intent) {

    }

    @Override
    void disableFields() {

    }

    @Override
    void enableFields() {

    }

    @Override
    public String getFilteredAction() {
        return null;
    }

    private void setClickListeners() {
        setCheckBox1Listener();
        setCheckBox2Listener();
        setCheckBox3Listener();
        setSaveButtonListener();
        setCancelTextViewListener();
    }

    private void setLocalViews() {
        View view = getView();
        int radius = getHomeActivity().getPreferences(Context.MODE_PRIVATE)
                .getInt(ProtocolConstants.RADIUS_KEY, ProtocolConstants.DEFAULT_RADIUS);
        if (view != null) {
            cb1 = (CheckBox) view.findViewById(R.id.parameter_checkBox1);
            cb2 = (CheckBox) view.findViewById(R.id.parameter_checkBox2);
            cb3 = (CheckBox) view.findViewById(R.id.parameter_checkBox3);

            cb1.setChecked(ProtocolConstants.RADIUS1 == radius);
            cb2.setChecked(ProtocolConstants.RADIUS2 == radius);
            cb3.setChecked(ProtocolConstants.RADIUS3 == radius);

            tvText1 = (TextView) view.findViewById(R.id.parameter_text1);
            tvText2 = (TextView) view.findViewById(R.id.parameter_text2);
            tvText3 = (TextView) view.findViewById(R.id.parameter_text3);

            tvText1.setText("Petit (" + ProtocolConstants.RADIUS1 + ")");
            tvText2.setText("Moyen (" + ProtocolConstants.RADIUS2 + ")");
            tvText3.setText("Grand (" + ProtocolConstants.RADIUS3 + ")");

            btSave = (Button) view.findViewById(R.id.parameter_btSave);
            tvCancel = (TextView) view.findViewById(R.id.parameter_tvCancel);

            return;
        }
        Log.e(TAG, "setLocalViews - can not retrieve the enclosing view.");
    }

    private void setCancelTextViewListener() {
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHomeActivity().showFragment(new HomeIdleFragment());
            }
        });
    }

    private void setSaveButtonListener() {
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getHomeActivity().getPreferences(Context.MODE_PRIVATE);
                int radius = preferences.getInt(ProtocolConstants.RADIUS_KEY, ProtocolConstants.DEFAULT_RADIUS);
                if (cb1.isChecked()) radius = ProtocolConstants.RADIUS1;
                if (cb2.isChecked()) radius = ProtocolConstants.RADIUS2;
                if (cb3.isChecked()) radius = ProtocolConstants.RADIUS3;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(ProtocolConstants.RADIUS_KEY, radius);
                editor.commit();
                getHomeActivity().showFragment(new HomeIdleFragment());
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        getHomeActivity().showFragment(new HomeIdleFragment());
        return true;
    }

    private void setCheckBox3Listener() {
        cb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = cb3.isChecked();
                cb1.setChecked(!isChecked);
                cb2.setChecked(!isChecked);
            }
        });
    }

    private void setCheckBox2Listener() {
        cb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = cb2.isChecked();
                cb1.setChecked(!isChecked);
                cb3.setChecked(!isChecked);
            }
        });
    }

    private void setCheckBox1Listener() {
        cb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = cb1.isChecked();
                cb2.setChecked(!isChecked);
                cb3.setChecked(!isChecked);
            }
        });
    }
}
