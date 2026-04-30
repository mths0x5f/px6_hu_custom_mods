package android.mths.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.mths.Constants;
import android.os.UserHandle;
import android.util.Log;

/**
 * BroadcastReceiver that listens for system boot events
 * and starts the ManageService when the device boots up.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG, "Device boot completed, starting Manager Service");
            startManageService(context);
        } else if (action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            Log.i(TAG, "Device boot completed but still locked, noop for now");
        }
    }

    private void startManageService(Context context) {
        Intent intent = new Intent();
        intent.setComponent(Constants.Services.MANAGE_SERVICE.getComponentName());
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
    }

}
