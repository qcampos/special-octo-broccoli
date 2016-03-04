package notification_security.upem.fr.securitynotification.home;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Baxtalou on 04/03/2016.
 */
public interface FragmentReceiver {

    /**
     * A FragmentState tells if the current enclosing Fragment is waiting
     * for a network result or not. It could have been replaced by a simple boolean,
     * but it would have been a far less elegant way.
     */
    enum FragmentState {
        IDLE,
        WAITING_NETWORK_RESULT
    }

    /**
     * Receives the given intent from NetworkService to process.
     * If the receiver is not in WAITING_NETWORK_RESULT, it will
     * discard the call.
     *
     * @param intent the intent to process.
     */
    void onReceiveNetworkIntent(Intent intent);

    /**
     * Returns the filter action filtered by the current FragmentReceiver.
     * It only listen to this action.
     *
     * @return the filter action filtered.
     */
    String getFilteredAction();


    /**
     * Abstract Fragment factorizing and handling all network verifications
     * during exchanges with the NetworkService and a HomeActivity's fragment.
     * A fragment used by HomeActivity has to inherit from this abstract class
     * and implement its methods, in order to communicate swiftly with the NetworkService.
     */
    abstract class BaseFragmentReceiver extends Fragment implements FragmentReceiver {

        // Logging TAG.
        private static final String TAG = BaseFragmentReceiver.class.getSimpleName();

        // LifeCycle variables.
        private HomeActivity homeActivity;
        private FragmentState state;


        /**
         * Override this method to perform a request on the NetworkService, by using
         * the homeActivity as the context. The params String are these originally passed
         * to the method requestNetworkAction.
         * This method will be called after a requestNetworkAction, only if the current
         * instance of FragmentReceiver is in the right state (not already WAITING_NETWORK_RESULT).
         *
         * @param homeActivity The activity on which to perform the service request.
         * @param params       The params passed during the requestNetworkAction call, which
         *                     can be used to parametrize the request.
         */
        abstract void performNetworkRequest(HomeActivity homeActivity, String... params);

        /**
         * The HomeActivity instance allows the view fragment to notify of changes
         * accordingly to the
         *
         * @param homeActivity
         * @param intent
         */
        abstract void processNetworkResult(HomeActivity homeActivity, Intent intent);

        /**
         * Disables all fields of the current BaseFragmentReceiver.
         * It is called when the current instance is waiting for a response
         * from the network. Right after a successful call to requestNetworkAction.
         */
        abstract void disableFields();

        /**
         * Enables all fields of the current BaseFragmentReceiver.
         * It is called when the current instance is no longer waiting for a response
         * from the network. Right after a successful call to stopWaitingNetworkResult.
         */
        abstract void enableFields();

        /**
         * Requests a new action on the NetworkService with the given params.
         * If the current instance is in the right states, it will call performNetworkRequest
         * with the given params. It will not if it is already in WAITING_NETWORK_RESULT state.
         */
        final public void requestNetworkAction(String... params) {
            // Guarding the request of multiple requests (sanity check).
            if (state == FragmentState.WAITING_NETWORK_RESULT) {
                return;
            }
            state = FragmentState.WAITING_NETWORK_RESULT;
            disableFields();
            performNetworkRequest(homeActivity, params);
        }

        @Override
        public void onReceiveNetworkIntent(Intent intent) {
            if (state != FragmentState.WAITING_NETWORK_RESULT) {
                Log.e(TAG, "onReceiveNetworkIntent - Receiving not waited intent.");
                return;
            }
            Log.d(TAG, "onReceiveNetworkIntent - receiving waited intent.");
            processNetworkResult(homeActivity, intent);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // Setting the enclosing HomeActivity.
            homeActivity = (HomeActivity) getActivity();
            // Starting this fragment's life in IDLE state.
            state = FragmentState.IDLE;
        }


        /**
         * Calls this method when every network result are received.
         * Switches back the current BaseFragmentReceiver on its idle state. It is no longer
         * waiting for intent from onReceiveNetworkIntent calls.
         */
        void stopWaitingNetworkResult() {
            state = FragmentState.IDLE;
            enableFields();
        }
    }
}
