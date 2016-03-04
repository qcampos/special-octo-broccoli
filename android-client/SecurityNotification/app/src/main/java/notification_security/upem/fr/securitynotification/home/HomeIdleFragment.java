package notification_security.upem.fr.securitynotification.home;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import notification_security.upem.fr.securitynotification.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeIdleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeIdleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeIdleFragment extends Fragment implements FragmentReceiver {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_idle, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Update after the activity has finished to load itself.
        // Set adapter, get view by ids...
        // getActivity and so on.
    }

    @Override
    public void onReceiveNetworkIntent(Intent intent) {

    }

    @Override
    public String getFilteredAction() {
        return null; // TODO auto-generated.
    }
}
