package notification_security.upem.fr.securitynotification.map;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import notification_security.upem.fr.securitynotification.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class UrgenceMapActivityFragment extends Fragment {

    public UrgenceMapActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_urgence_map, container, false);
    }
}
