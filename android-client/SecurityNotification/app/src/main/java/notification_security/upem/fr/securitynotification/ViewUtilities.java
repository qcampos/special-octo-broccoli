package notification_security.upem.fr.securitynotification;

import android.app.FragmentManager;
import android.content.Context;
import android.widget.Toast;

/**
 * Provides utilities methods for views.
 */
public class ViewUtilities {

    /**
     * Performs a long toast with the given parameters.
     */
    public static void showLongToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Performs a short toast with the given parameters.
     */
    public static void showShortToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Returns the corresponding FragmentReceiver fetch inside the given FragmentManager.
     *
     * @param fragmentManager the fragment manager managing the requested FragmentReceiver.
     * @param fragmentID      the FragmentReceiver's id.
     * @return the instance of FragmentReceiver found.
     */
    public static FragmentReceiver getFragmentById(FragmentManager fragmentManager, int fragmentID) {
        return (FragmentReceiver) fragmentManager.findFragmentById(fragmentID);
    }
}
