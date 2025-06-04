package android.microntek.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

/**
 * MicrontekReceiver is a BroadcastReceiver that listens for system boot events
 * and starts the ManageService when the device boots up.
 */
public class MicrontekReceiver extends BroadcastReceiver {

    private static final String TAG = "MicrontekReceiver";

    public static final ComponentName MANAGE_SERVICE_COMPONENT =
            new ComponentName("android.microntek.service", "android.microntek.service.ManageService");

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG, "BOOT_COMPLETED");
            startManageService(context);
        } else if (action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            Log.i(TAG, "LOCKED_BOOT_COMPLETED");
        }
    }

    private void startManageService(Context context) {
        Intent intent = new Intent();
        intent.setComponent(MANAGE_SERVICE_COMPONENT);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
    }

}
