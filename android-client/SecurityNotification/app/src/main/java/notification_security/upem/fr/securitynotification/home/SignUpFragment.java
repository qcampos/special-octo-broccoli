package notification_security.upem.fr.securitynotification.home;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import notification_security.upem.fr.securitynotification.R;
import notification_security.upem.fr.securitynotification.home.FragmentReceiver.BaseFragmentReceiver;

/**
 * Fragment handling Sign up logic.
 */
public class SignUpFragment extends BaseFragmentReceiver {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
}
