package notification_security.upem.fr.securitynotification;

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
}
