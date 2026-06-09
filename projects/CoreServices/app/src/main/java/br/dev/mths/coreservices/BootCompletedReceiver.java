package br.dev.mths.coreservices;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
        switch (action) {
            case Intent.ACTION_LOCKED_BOOT_COMPLETED -> Log.i(TAG, "LOCKED_BOOT_COMPLETED");
            case Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "Device boot completed, starting Manager Service");
                startManagerService(context);
            }
        }
    }

    private void startManagerService(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, ManagerService.class));
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
    }

}