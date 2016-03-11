package notification_security.upem.fr.securitynotification.map;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by anis on 11/03/16.
 */
public class NetworkService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NetworkService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
