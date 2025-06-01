package android.microntek.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.microntek.Constant;
import android.microntek.HCTApi;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import java.util.ArrayList;
/* loaded from: classes.dex */
public class ManageService extends Service {
    private static final String TAG = "ManageService";
    private static final boolean adbon = false;
    private int nTryStartAsrServiceCount = 0;
    private int mTryStartSuzukiServiceCount = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunAsr = new Runnable() { // from class: android.microntek.service.ManageService.1
        @Override // java.lang.Runnable
        public void run() {
            if (!ManageService.this.StartAISpeechService()) {
                ManageService.this.StartTxzAsrService();
            }
        }
    };
    private Runnable mRunSuzuki = new Runnable() { // from class: android.microntek.service.ManageService.2
        @Override // java.lang.Runnable
        public void run() {
            ManageService manageService = ManageService.this;
            if (!manageService.isServiceRunning(manageService.getBaseContext(), "com.tecno.unitapp")) {
                ManageService.this.StartSuzukiService();
            }
        }
    };

    private boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return adbon;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList) myManager.getRunningServices(1000);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return adbon;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void StartCarService() {
        Intent carserviceintent = new Intent();
        carserviceintent.setComponent(new ComponentName("android.microntek.service", "android.microntek.service.MicrontekServer"));
        startServiceAsUser(carserviceintent, UserHandle.OWNER);
    }

    private void StartCanBusService() {
        try {
            Intent canserviceintent = new Intent();
            canserviceintent.setComponent(new ComponentName("android.microntek.canbus", "android.microntek.canbus.CanBusServer"));
            startServiceAsUser(canserviceintent, UserHandle.OWNER);
        } catch (Exception ignored) {
        }
    }

    private void RadioService() {
        try {
            Intent radioserviceintent = new Intent();
            radioserviceintent.setComponent(new ComponentName(Constant.RADIOPACKAGE, "com.microntek.radio.RadioService"));
            startServiceAsUser(radioserviceintent, UserHandle.OWNER);
        } catch (Exception ignored) {
        }
    }

    private void startNaviBarService() {
        if (HCTApi.getHctwmPloy() != 1) {
            return;
        }
        try {
            Intent naviBarServiceIntent = new Intent();
            naviBarServiceIntent.setComponent(new ComponentName("com.microntek.NavigationBar.service", "com.microntek.NavigationBar.service.EventService"));
            startServiceAsUser(naviBarServiceIntent, UserHandle.OWNER);
        } catch (Exception ignored) {
        }
    }

    private void StartBTService() {
        try {
            Intent btserviceintent = new Intent();
            btserviceintent.setComponent(new ComponentName("android.microntek.mtcser", "android.microntek.mtcser.BlueToothService"));
            startServiceAsUser(btserviceintent, UserHandle.OWNER);
        } catch (Exception ignored) {
        }
    }

    private void StartBoxService() {
        if ("BOX".equals(SystemProperties.get("ro.product.project", ""))) {
            try {
                Intent boxserverintent = new Intent();
                boxserverintent.setComponent(new ComponentName("android.microntek.box", "android.microntek.box.BoxServer"));
                startServiceAsUser(boxserverintent, UserHandle.OWNER);
            } catch (Exception ignored) {
            }
        }
    }

    private void StartAdbOn() {
    }

    private boolean StartTxzAsrService() {
        String txzAsr = SystemProperties.get("ro.product.txz.asr");
        if (txzAsr != null && !txzAsr.isEmpty() && !"null".equals(txzAsr)) {
            try {
                Log.i("wuwq", "StartTxzAsrService Thread -------------  nTryStartAsrServiceCount = " + this.nTryStartAsrServiceCount);
                Intent txzserviceintent = new Intent();
                if ("Chinese".equals(txzAsr)) {
                    txzserviceintent.setComponent(new ComponentName("com.txznet.adapter", "com.txznet.adapter.service.StartService"));
                } else if (!"English".equals(txzAsr)) {
                    return adbon;
                } else {
                    txzserviceintent.setComponent(new ComponentName("com.txznet.smartadapter", "com.txznet.smartadapter.StartService"));
                }
                ComponentName cmpName = startServiceAsUser(txzserviceintent, UserHandle.OWNER);
                Log.i("wuwq", "txz cmpName: " + cmpName.toString());
            } catch (Exception e) {
                Log.i("wuwq", "StartTxzAsrService Exception: " + e.toString());
                int i = this.nTryStartAsrServiceCount + 1;
                this.nTryStartAsrServiceCount = i;
                if (i < 10) {
                    this.mHandler.postDelayed(this.mRunAsr, 3000L);
                }
            }
            return true;
        }
        return adbon;
    }

    private void StartSuzukiService() {
        try {
            Intent suzukiserviceintent = new Intent();
            suzukiserviceintent.setComponent(new ComponentName("com.tecno.unitapp", "com.tecno.unitapp.helper.CarService"));
            getApplicationContext().startForegroundService(suzukiserviceintent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("lxf", "StartSuzukiService() catch");
            int i = this.mTryStartSuzukiServiceCount + 1;
            this.mTryStartSuzukiServiceCount = i;
            if (i < 10) {
                this.mHandler.postDelayed(this.mRunSuzuki, 3000L);
            }
        }
    }

    private boolean StartAISpeechService() {
        String aispeechAsr = SystemProperties.get("ro.product.aispeech.asr");
        if (aispeechAsr != null && !aispeechAsr.isEmpty() && !"null".equals(aispeechAsr)) {
            try {
                Log.i("wuwq", "StartAISpeechService Thread -------------  nTryStartAsrServiceCount = " + this.nTryStartAsrServiceCount);
                Intent aispeechserviceintent = new Intent();
                if ("Chinese".equals(aispeechAsr)) {
                    aispeechserviceintent.setComponent(new ComponentName("com.aispeech.lyra.daemon", "com.aispeech.lyra.daemon.DaemonActivity"));
                } else if (!"English".equals(aispeechAsr)) {
                    return adbon;
                } else {
                    aispeechserviceintent.setComponent(new ComponentName("com.aispeech.hotwords", "com.aispeech.hotwords.speech.LiteService"));
                }
                ComponentName cmpName = startServiceAsUser(aispeechserviceintent, UserHandle.OWNER);
                Log.i("wuwq", "aispeech cmpName: " + cmpName.toString());
            } catch (Exception e) {
                Log.i("wuwq", "StartAISpeechService Exception: " + e.toString());
                int i = this.nTryStartAsrServiceCount + 1;
                this.nTryStartAsrServiceCount = i;
                if (i < 10) {
                    this.mHandler.postDelayed(this.mRunAsr, 3000L);
                }
            }
            return true;
        }
        return adbon;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.i("chun", "ManageService start");
        StartCarService();
        StartBTService();
        RadioService();
        StartCanBusService();
        StartBoxService();
        startNaviBarService();
        this.nTryStartAsrServiceCount = 0;
        this.mHandler.postDelayed(this.mRunAsr, 10000L);
        Settings.System.putInt(getContentResolver(), "show_touches", 0);
        try {
            new LockPatternUtils(this).clearLock((byte[]) null, 0);
        } catch (Exception ignored) {
        }
        StartAdbOn();
    }

}
