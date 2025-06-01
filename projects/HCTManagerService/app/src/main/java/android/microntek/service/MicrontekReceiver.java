package android.microntek.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.microntek.Constant;
import android.os.UserHandle;
import android.util.Log;
/* loaded from: classes.dex */
public class MicrontekReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i("chun", "MicrontekReceiver>>>>> BOOT_COMPLETED");
            startMtcManage(context);
            GpsTimeService(context);
            RadioService(context);
        } else if (action.equals("android.intent.action.LOCKED_BOOT_COMPLETED")) {
            Log.i("chun", "MicrontekReceiver>>>>> LOCKED_BOOT_COMPLETED");
        }
    }

    private void startMtcManage(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("android.microntek.service", "android.microntek.service.ManageService"));
        context.startServiceAsUser(intent, UserHandle.OWNER);
    }

    private void GpsTimeService(Context context) {
        Intent gpstimeserviceintent = new Intent();
        gpstimeserviceintent.setComponent(new ComponentName("android.microntek.service", "android.microntek.service.MicrontekTimeUpdateServer"));
        context.startServiceAsUser(gpstimeserviceintent, UserHandle.OWNER);
    }

    private void RadioService(Context context) {
        try {
            Intent radioserviceintent = new Intent();
            radioserviceintent.setComponent(new ComponentName(Constant.RADIOPACKAGE, "com.microntek.radio.RadioService"));
            context.startServiceAsUser(radioserviceintent, UserHandle.OWNER);
        } catch (Exception e) {
        }
    }
}
