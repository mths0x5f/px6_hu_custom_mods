package android.microntek.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.microntek.ClearProcess;
import android.microntek.Constant;
import android.microntek.HCTApi;
import android.microntek.HctUtil;
import android.microntek.InstallUtil;
import android.microntek.app.AppManager;
import android.microntek.mfi.MfiListener;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/* loaded from: classes.dex */
public class MicrontekServer extends MicrontekServiceBase {
    public static final String CARTOUCH_SHOW = "cartouch_show";
    private static final String[] DIALOG_MESSAGE = {"Copy.", "Copy..", "Copy...", "Copy....", "Copy......", "Copy.......", "Copy........"};
    private static final int MSG_AV_REFRESH = 22;
    private static final int MSG_COPY_OK = 25;
    private static final int MSG_COPY_STATE = 24;
    private static final int MSG_DEVICE_LOCK = 3;
    private static final int MSG_DUAL_CNTCLEAR = 15;
    private static final int MSG_DUAL_HOME = 16;
    private static final int MSG_DUAL_MODE = 14;
    private static final int MSG_GO_DRIVING_ASSISTANCE = 21;
    private static final int MSG_GPS_BACK = 2;
    private static final int MSG_GPS_TOP = 1;
    private static final int MSG_HIDE_YH_LOGO = 18;
    private static final int MSG_MUSIC_CLOCK = 4;
    private static final int MSG_POWEROFF_CMD = 7;
    private static final int MSG_POWER_DIALOG = 8;
    private static final int MSG_PROMPT_OFF = 5;
    private static final int MSG_PWR_SCREEN = 23;
    private static final int MSG_SINGLE_HOME = 17;
    private static final int MSG_TIME_FRESH = 0;
    private static final int MSG_TOUCH_UPDATE = 6;
    private static final int MSG_UPDATE_LAUNCHER = 13;
    private static final int MSG_VENDING_ENABLE = 9;
    private static final int MSG_VOL_DOWN = 10;
    private static final int MSG_VOL_UP = 11;
    private static final int MSG_WIFI_AP_CHECK = 12;
    private long durationTime;
    private long initialTime;
    private AppOpsManager mAppOpsManager;
    private PackageInfo mPackageInfo;
    private long onCraeteTime;
    private PackageManager mPm = null;
    private final int MSG_MFI_CONNECTED = 65296;
    private final int MSG_MFI_DISCONNECTED = 65297;
    private final int WIFIAP_CHECK_CNT = 20;
    private final long ACC_OFF_WIFI_AP_DELAYMILLIS = 100;
    private final long SLEEP_WIFI_AP_DELAYMILLIS = 3000;
    private long mlPwerOnWifiApCheckDelayMillis = 3000;
    boolean screenOn = false;
    boolean keyscreenOn = false;
    private boolean launcherFlag = false;
    private boolean firstflag = false;
    private boolean isYoutube = false;
    private int screensaverTimeout = -1;
    private boolean screensaverEnableLocal = false;
    private boolean screensaverEnable = false;
    private int screensaverTimer = 0;
    private boolean ScreenSaverOn = false;
    private int touchCount = 5;
    private int mModeDoulbe = 0;
    private int mHomeDoulbe = 0;
    private AlertDialog mApkDialog = null;
    private String[] mApkFileNames = null;
    private int mCurInstallApk = 0;
    private boolean isInstallClear = false;
    private boolean isBoxStartApp = false;
    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i != 65535) {
                switch (i) {
                    case 0:
                        TickTask();
                        return;
                    case 1:
                        if (MicrontekServiceBase.gpsOpen && MicrontekServiceBase.gpsIsFront) {
                            initGps();
                            setParameters("av_gps_ontop=true");
                            return;
                        }
                        return;
                    case 2:
                        if (!MicrontekServiceBase.gpsOpen || !MicrontekServiceBase.gpsIsFront) {
                            setParameters("av_gps_ontop=false");
                            return;
                        }
                        return;
                    case 3:
                        MicrontekServiceBase.mDeviceLock = false;
                        if (MicrontekServiceBase.needRunNavi) {
                            MicrontekServiceBase.needRunNavi = false;
                            runApp(MicrontekServiceBase.GPSPKNAME);
                            return;
                        }
                        return;
                    case 4:
                        startMusicClock();
                        return;
                    case 5:
                        setParameters("av_voiceprompt_on=false");
                        return;
                    case 6:
                        SendTouchUpdate((List) msg.obj);
                        return;
                    case 7:
                        setParameters("rpt_power=false");
                        return;
                    case 8:
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            return;
                        }
                        return;
                    case 9:
                        SystemProperties.set("service.vending.enable", "1");
                        return;
                    case 10:
                        MTCAdjVolume(0);
                        return;
                    case 11:
                        MTCAdjVolume(1);
                        return;
                    case 12:
                        updataWifiAPState();
                        return;
                    case 13:
                        MicrontekServer microntekServer = MicrontekServer.this;
                        microntekServer.setDefaultLauncher(microntekServer.mPackageName, mClassName);
                        return;
                    case 14:
                        handler.removeMessages(15);
                        MicrontekServer.access$408(MicrontekServer.this);
                        if (mModeDoulbe >= 2) {
                            mModeDoulbe = 0;
                            HCTApi.switchDualScreen();
                            return;
                        }
                        handler.sendEmptyMessageDelayed(15, 600L);
                        return;
                    case 15:
                        mModeDoulbe = 0;
                        ModeSwitch();
                        return;
                    case 16:
                        handler.removeMessages(17);
                        MicrontekServer.access$508(MicrontekServer.this);
                        if (mHomeDoulbe >= 2) {
                            mHomeDoulbe = 0;
                            HCTApi.switchDualScreen();
                            return;
                        }
                        handler.sendEmptyMessageDelayed(17, 600L);
                        return;
                    case 17:
                        mHomeDoulbe = 0;
                        SystemKey(3, 0);
                        return;
                    case 18:
                        showYHLogoView(false);
                        return;
                    default:
                        switch (i) {
                            case 21:
                                if ("YH".equals(customer) && !backviewState && !"com.microntek.dvr".equals(HctUtil.getTopActivityPackageName(mContext))) {
                                    startPkg("com.microntek.dvr", "com.microntek.dvr.MainActivity");
                                    return;
                                }
                                return;
                            case 22:
                                audioManager.setParameters("av_refresh=true");
                                return;
                            case MicrontekServer.MSG_PWR_SCREEN /* 23 */:
                                NeedStartApp();
                                return;
                            case 24:
                                handler.removeMessages(24);
                                String result = SystemProperties.get("sys.hct.copy.result", "");
                                if (TextUtils.isEmpty(result)) {
                                    msgIndex = (msgIndex + 1) % 6;
                                    copyDialog.setMessage(MicrontekServer.DIALOG_MESSAGE[msgIndex]);
                                    handler.sendEmptyMessageDelayed(24, 800L);
                                    return;
                                } else if ("0".equals(result)) {
                                    copyDialog.setMessage("Copy error !!!!");
                                    handler.sendEmptyMessageDelayed(MicrontekServer.MSG_COPY_OK, 3000L);
                                    return;
                                } else if ("1".equals(result)) {
                                    copyDialog.setMessage("Copy success");
                                    handler.sendEmptyMessageDelayed(MicrontekServer.MSG_COPY_OK, 3000L);
                                    return;
                                } else {
                                    return;
                                }
                            case MicrontekServer.MSG_COPY_OK /* 25 */:
                                if (copyDialog != null) {
                                    copyDialog.dismiss();
                                    return;
                                }
                                return;
                            default:
                                switch (i) {
                                    case 65296:
                                        durationTime = System.currentTimeMillis();
                                        if (((durationTime - initialTime) / 1000 >= 10 || isRunUsbIpod) && !mUsbIpod && powerState == 2 && !MicrontekServiceBase.btLock && !backviewState) {
                                            startUsbIpod(0);
                                        }
                                        mUsbIpod = true;
                                        isRunUsbIpod = false;
                                        return;
                                    case 65297:
                                        mUsbIpod = false;
                                        return;
                                    default:
                                        return;
                                }
                        }
                }
            }
            MicrontekServer.access$1008(MicrontekServer.this);
            if (mCurInstallApk >= mApkFileNames.length || msg.arg1 == -1) {
                if (mApkDialog != null && mApkDialog.isShowing()) {
                    mApkDialog.dismiss();
                    return;
                }
                return;
            }
            MicrontekServer microntekServer3 = MicrontekServer.this;
            microntekServer3.instatllBatch(microntekServer3.mApkFileNames[mCurInstallApk]);
        }
    };

    private final MfiListener mfiListener = new MfiListener() {
        public void onConnected() {
            Message msg = handler.obtainMessage();
            msg.what = MSG_MFI_CONNECTED;
            handler.sendMessage(msg);
        }

        public void onDisconnected() {
            Message msg = handler.obtainMessage();
            msg.what = MSG_MFI_DISCONNECTED;
            handler.sendMessage(msg);
        }
    };

    private final BroadcastReceiver screenClockBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("changescreenclock".equals(action)) {
                int value = intent.getIntExtra("myscreenclock", 0);
                if (1 == value) {
                    screensaverEnableLocal = true;
                    screensaverEnable = true;
                    screensaverTimeout = Settings.System.getInt(getContentResolver(), "musicscreen_timeout", 30);
                } else if (value == 0) {
                    screensaverEnableLocal = false;
                    screensaverEnable = false;
                    screensaverTimeout = -1;
                }
                screensaverTimer = 0;
            }
        }
    };

    private final BroadcastReceiver installApkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String path = intent.getExtras().getString("value");
            if (action.equals(Constant.MSG_INSTALL_XRROSS) && path != null && new File(path).exists()) {
                new InstallUtil(getBaseContext(), path, new Handler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Intent intent2 = new Intent(InstallUtil.MSG_INSTALL_XRROSS_OK);
                        intent2.putExtra("package", (String) msg.obj);
                        sendBroadcastAsUser(intent2, UserHandle.ALL);
                    }
                });
            }
        }
    };

    private final BroadcastReceiver installReceiver = new BroadcastReceiver() {
        @SuppressLint("WrongConstant")
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                String packageName = intent.getData().getSchemeSpecificPart();
                if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    if ("1".equals(SystemProperties.get("ro.product.market.mode", "0"))) {
                        int flags = PackageManager.GET_PERMISSIONS |
                                PackageManager.MATCH_DISABLED_COMPONENTS |
                                PackageManager.MATCH_STATIC_SHARED_LIBRARIES |
                                PackageManager.GET_SIGNATURES;

                        mPackageInfo = getPackageManager().getPackageInfo(packageName, flags);
                        String[] permissions = mPackageInfo.requestedPermissions;
                        boolean needToSetCanInstallApps = false;
                        for (String permission : permissions) {
                            if (Manifest.permission.INSTALL_PACKAGES.equals(permission) ||
                                    Manifest.permission.REQUEST_INSTALL_PACKAGES.equals(permission)) {
                                needToSetCanInstallApps = true;
                                break;
                            }
                        }
                        if (needToSetCanInstallApps) {
                            setCanInstallApps(true, packageName);
                        }
                    }
                    if ("android.microntek.canbus".equals(packageName)) {
                        Intent canserviceintent = new Intent();
                        canserviceintent.setComponent(new ComponentName("android.microntek.canbus", "android.microntek.canbus.CanBusServer"));
                        startServiceAsUser(canserviceintent, UserHandle.SYSTEM);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    };

    private final BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_LOCALE_CHANGED)) {
                progressDialog = null;
            }
        }
    };

    private final BroadcastReceiver hdmiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constant.ACTION_PLUGGED)) {
                boolean state = intent.getBooleanExtra("state", false);
                boolean bootCompleted = SystemProperties.get("sys.boot_completed", "0").equals("1");
                if (bootCompleted && state) {
                    handler.postDelayed(() -> startExtShow(), 10_000L);
                } else if (!state) {
                    sendKeyCode(1026);
                }
            } else if (action.equals("com.microntek.extshow.start")) {
                startExtShow();
            }
        }
    };

    private boolean wifiFirstRevFlag = true;
    private int wifiApCheckCount = 20;
    private boolean isFirstUpdateWifiAPState = true;
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (MicrontekServiceBase.POWER_STA_ON == powerState || wifiFirstRevFlag) {
                    int wifiState = wifiManager.getWifiState();
                    if (
                        (WifiManager.WIFI_STATE_ENABLED == wifiState || WifiManager.WIFI_STATE_DISABLED == wifiState) &&
                        getWifiDriverState() &&
                        (!wifiFirstRevFlag || Settings.System.getInt(getContentResolver(), "status_acc_off_ap_opened", 0) != 1)
                    ) {
                        boolean apState = wifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED;
                        boolean wifiEnabled = wifiManager.isWifiEnabled();
                        Settings.System.putInt(getContentResolver(), "status_acc_off_wifi_opened", wifiEnabled ? 1 : 0);
                        Settings.System.putInt(getContentResolver(), "status_acc_off_ap_opened", apState ? 1 : 0);
                    }
                } else if (powerState == MicrontekServiceBase.POWER_STA_ACC_OFF) {
                    if (WifiManager.WIFI_STATE_ENABLED == wifiManager.getWifiState()) {
                        setWifiOn(false);
                    }
                }
            } else if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                if (MicrontekServiceBase.POWER_STA_ON == powerState || wifiFirstRevFlag) {
                    int wifiApState = getWifiApState();
                    if (WifiManager.WIFI_AP_STATE_ENABLED == wifiApState || WifiManager.WIFI_AP_STATE_DISABLED == wifiApState) {
                        waitingForTerminalState = false;
                        if (getWifiDriverState()) {
                            boolean apState = wifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED;
                            boolean wifiEnabled = wifiManager.isWifiEnabled();
                            Settings.System.putInt(getContentResolver(), "status_acc_off_wifi_opened", wifiEnabled ? 1 : 0);
                            Settings.System.putInt(getContentResolver(), "status_acc_off_ap_opened", apState ? 1 : 0);
                        }
                    } else if (WifiManager.WIFI_AP_STATE_FAILED == wifiApState) {
                        Log.i(TAG, "wifiReceiver *** WIFI_AP_STATE_FAILED state is Failed. ");
                        wifiApCheckCount = 20;
                        isFirstUpdateWifiAPState = true;
                        handler.removeMessages(MSG_WIFI_AP_CHECK);
                        Message msg = handler.obtainMessage();
                        msg.what = MSG_WIFI_AP_CHECK;
                        handler.sendMessageDelayed(msg, 3_000L);
                    }
                } else if (powerState == MicrontekServiceBase.POWER_STA_ACC_OFF) {
                    if (WifiManager.WIFI_AP_STATE_ENABLED == getWifiApState()) {
                        setWifiApEnabled(false);
                    }
                }
            } else {
                if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action) && MicrontekServiceBase.POWER_STA_ON == powerState) {
                    SupplicantState supplicantState = intent.getParcelableExtra("newState");
                    if (SupplicantState.INTERFACE_DISABLED.equals(supplicantState)) {
                        Log.i(TAG, "wifiReceiver supplicant state is disabled. ");
                        wifiApCheckCount = 20;
                        isFirstUpdateWifiAPState = true;
                        handler.removeMessages(MSG_WIFI_AP_CHECK);
                        Message msg = handler.obtainMessage();
                        msg.what = MSG_WIFI_AP_CHECK;
                        handler.sendMessageDelayed(msg, 3_000L);
                    }
                }
            }
        }
    };

    private boolean updatingWifiAPState = false;
    private final Runnable powerOffRunnable = this::powerOffAction;

    private int msgIndex = 0;
    private AlertDialog copyDialog;


    private final BroadcastReceiver phoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
                setParameters("av_phone_sim=out");
                simPhoneLock = true;
                MTCAdjVolume(2);
            }
        }
    };

    private final BroadcastReceiver mediaDetectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int autoPlayEnabled = Settings.System.getInt(getContentResolver(), Constant.MEDIAAUTOEN_STRING, 0);
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                if (powerState != POWER_STA_ON || backviewState) {
                    return;
                }
                String path = convertStorageToMnt(intent.getData().getPath());
                String devString = getDeviceType(context, convertMntToStorage(path));
                if (devString.equals("GPS") && needRunNavi) {
                    needRunNavi = false;
                    runApp(MicrontekServiceBase.GPSPKNAME);
                }
                if (!checkSystemMcuAutoUpdate(path)) {
                    checkTouchUpdate(path);
                    updateDmcuExtCfg(path);
                    checkAutoInstallApk(path);
                    updateHctExtCfg(path);
                    saveCustomerLogo(path);
                    if (!MicrontekServiceBase.btLock && !MicrontekServiceBase.mDeviceLock && autoPlayEnabled != 0) {
                        clearMusicClock();
                        String customer = SystemProperties.get("ro.product.customer", "HCT");
                        if (!TextUtils.isEmpty(devString) && !devString.equals("FLASH")) {
                            if ((devString.equals("GPS") && !"YH".equals(customer)) ||
                                Constant.MUSICPACKAGE.equals(HctUtil.getTopActivityPackageName(context))) {
                                return;
                            }
                            String[] versionParts = Build.VERSION.RELEASE.split("\\.");
                            int majorVersion = Integer.parseInt(versionParts[0]);
                            if (majorVersion > 9) {
                                durationTime = System.currentTimeMillis();
                                if ((durationTime - initialTime) / 1000 > 15) {
                                    startMusic(path, 0);
                                    return;
                                }
                                return;
                            }
                            startMusic(path, 0);
                        }
                    }
                }
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
                handler.removeMessages(MSG_TOUCH_UPDATE);
            }
        }
    };

    private final BroadcastReceiver MTCAPPProc = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pkgName;
            String volume;
            String action = intent.getAction();
            if (action.equals(Constant.MSG_BEEP)) {
                setParameters("ctl_beep=1");
                clearMusicClock();
            } else if (action.equals(Constant.MSG_BEEP_CLEAR_SCREEN)) {
                clearMusicClock();
            } else if (action.equals(Constant.MSG_MTC_PROMPT)) {
                if (intent.hasExtra("package")) {
                    String pkName = intent.getStringExtra("package");
                    setParameters("av_voiceprompt_package=" + pkName);
                }
                if (intent.hasExtra("state")) {
                    String state = intent.getStringExtra("state");
                    if (state.equals("on")) {
                        setParameters("av_voiceprompt_on=true");
                        handler.removeMessages(5);
                        handler.sendEmptyMessageDelayed(5, 3_000L);
                        return;
                    }
                    setParameters("av_voiceprompt_on=false");
                }
            } else if (action.equals(Constant.MSG_MTC_VOLUME_SET)) {
                int vol = MicrontekServiceBase.mCurVolume;
                if (intent.hasExtra("type")) {
                    String type = intent.getStringExtra("type");
                    if (type.equals("add")) {
                        vol = MicrontekServiceBase.mCurVolume + (MicrontekServiceBase.KEY_VOLMAX / 10);
                        if (vol > MicrontekServiceBase.KEY_VOLMAX) {
                            vol = MicrontekServiceBase.KEY_VOLMAX;
                        }
                    } else if (type.equals("sub")) {
                        vol = MicrontekServiceBase.mCurVolume - (MicrontekServiceBase.KEY_VOLMAX / 10);
                        if (vol < 0) {
                            vol = 0;
                        }
                    }
                } else if (intent.hasExtra("volume")) {
                    int bili = intent.getIntExtra("volume", MicrontekServiceBase.KEY_VOLMAX);
                    vol = (MicrontekServiceBase.KEY_VOLMAX * bili) / 100;
                } else {
                    return;
                }
                onChangeVolume(vol);
            } else if (action.equals(AudioManager.VOLUME_CHANGED_ACTION)) {
                if (intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1) == AudioManager.STREAM_MUSIC && mMcuVersion.contains("HXD")) {
                    int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (max == MicrontekServiceBase.KEY_VOLMAX && current <= MicrontekServiceBase.KEY_VOLMAX && current != volumeTemp) {
                        Log.i(TAG, "--mtc AudioManagerVOL current:" + current + "volumeTemp:" + volumeTemp);
                        onChangeVolume(current);
                    }
                }
            } else if (action.equals(Constant.MSG_MTC_BLIGHT_SET)) {
                int level = intent.getIntExtra("level", 100);
                setHctBacklight(level);
            } else if (action.equals(Constant.MSG_MTC_APP)) {
                if (intent.hasExtra(Constant.HCT_APP_KEY)) {
                    String app = intent.getStringExtra(Constant.HCT_APP_KEY);
                    if (app.equals("music")) {
                        startMusic(null, 0);
                    } else if (app.equals("movie")) {
                        startMovie(0);
                    } else if (app.equals("radio")) {
                        int freq = -1;
                        if (intent.hasExtra(Constant.HCT_APP_KEY2)) {
                            freq = intent.getIntExtra(Constant.HCT_APP_KEY2, -1);
                        }
                        if (freq != -1) {
                            startRadio(0, "" + freq);
                            return;
                        }
                        startRadio(0);
                    } else if (app.equals("dvd")) {
                        startDVD(0);
                    } else {
                        if (TextUtils.isEmpty(MicrontekServiceBase.GPSPKNAME) || !HctUtil.isPackageApplicationEnabled(mContext, MicrontekServiceBase.GPSPKNAME)) {
                            runApp(Constant.NAVIPACKAGE);
                        } else {
                            runApp(MicrontekServiceBase.GPSPKNAME);
                        }
                    }
                }
            } else if (action.equals(Constant.MSG_SHOW_VOLUME)) {
                if (MicrontekServiceBase.btLock || MicrontekServiceBase.simPhoneLock) {
                    volume = Constant.PHONEVOLUME;
                } else {
                    volume = Constant.MTCVOLUME;
                }
                MicrontekServiceBase.mCurVolume = Settings.System.getInt(getContentResolver(), volume, MicrontekServiceBase.KEY_VOLMAX / 2);
                showVolumeDialog(MicrontekServiceBase.mCurVolume);
            } else if (!action.equals(Constant.MSG_ACTIVE)) {
                switch (action) {
                    case Constant.MSG_MTC_CLEAR -> {
                        if (!ClearProcess.getInstance(context).getBusy()) {
                            int mode = intent.getIntExtra("mode", 1);
                            if (mode == 0) {
                                ClearProcess.getInstance(context).clearManage(0, null);
                                return;
                            }
                            context.sendBroadcastAsUser(new Intent(Constant.MSG_MTC_SPEEDSTART),
                                UserHandle.CURRENT_OR_SELF);
                            ClearProcess.getInstance(context)
                                .clearManage(1, MicrontekServiceBase.GPSPKNAME);
                            context.sendBroadcastAsUser(new Intent(Constant.MSG_MTC_SPEEDEND),
                                UserHandle.CURRENT_OR_SELF);
                        }
                    }
                    case Constant.MSG_MTC_CLOSEPACKAGE -> {
                        if (!intent.hasExtra("package")
                            || (pkgName = intent.getStringExtra("package")) == null
                            || pkgName.isEmpty()) {
                            return;
                        }
                        if (pkgName.equals(Constant.RADIOPACKAGE) || pkgName.equals(
                            Constant.DVDPACKAGE) || pkgName.equals(Constant.MUSICPACKAGE)
                            || pkgName.equals(Constant.IPODPACKAGE) || pkgName.equals(
                            Constant.USBIPODPACKAGE) || pkgName.equals(Constant.TVPACKAGE)
                            || pkgName.equals(Constant.PHOTOPACKAGE) || pkgName.equals(
                            Constant.MOVIEPACKAGE) || pkgName.equals(Constant.BTPACKAGE)
                            || pkgName.equals(Constant.BTMUSICPACKAGE) || pkgName.equals(
                            Constant.RECPACKAGE) || pkgName.equals(Constant.WEATHERPACKAGE)) {
                            if (!HctUtil.isAppRunning(context, pkgName)) {
                                return;
                            }
                            if (pkgName.equals(Constant.BTPACKAGE)
                                && !HctUtil.getTopActivityClassName(context)
                                .equals(Constant.BTMUSICCLASS2)) {
                                startHome();
                            }
                            sendBootCheck(getApplicationContext(), "android.microntek.service");
                        } else if (pkgName.equals(AppManager.packageNameCARPLAY[0])) {
                            getApplicationContext().sendBroadcastAsUser(
                                new Intent("carplay.apk.close"), UserHandle.ALL);
                        } else {
                            ClearProcess.getInstance(context).closePackage(pkgName);
                        }
                    }
                    case Constant.MSG_START_RADIO -> {
                        if (!HctUtil.isAppRunning(context, Constant.RADIOPACKAGE)) {
                            startRadio(2);
                        }
                    }
                    case Constant.MSG_START_MUSIC -> {
                        if (getMediaAppflag() != 1) {
                            startMusic(null, 2);
                        }
                    }
                    case Constant.MSG_START_IPOD -> {
                        if (mUsbIpodSupport) {
                            if (mUsbIpod && !HctUtil.isAppRunning(context,
                                Constant.USBIPODPACKAGE)) {
                                startUsbIpod(2);
                            }
                        } else if (!HctUtil.isAppRunning(context, Constant.IPODPACKAGE)) {
                            startIpod(2);
                        }
                    }
                    default -> action.equals(Constant.MSG_START_BTMUSIC);
                }
            }
        }
    };

    private final BroadcastReceiver MTCploy = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd;
            String action = intent.getAction();
            if (action.equals(Constant.BROADCAST_BT_REPORT)) {
                if (intent.hasExtra(Constant.MSG_BT_SRV_CONNECT_STATE)) {
                    int btState = intent.getIntExtra(Constant.MSG_BT_SRV_CONNECT_STATE, 0);
                    if (btState == 2 || btState == 3 || btState == 5) {
                        if (btState == 2) {
                            setParameters("av_phone=out");
                        } else if (btState == 3) {
                            setParameters("av_phone=in");
                        } else {
                            setParameters("av_phone=answer");
                        }
                        if (!MicrontekServiceBase.btLock) {
                            MicrontekServiceBase.btLock = true;
                            sendBootCheck(context, "phonecallin");
                            MTCAdjVolume(2);
                            context.sendBroadcastAsUser(new Intent(Constant.MSG_ACTIVE), UserHandle.CURRENT_OR_SELF);
                        }
                    } else if (MicrontekServiceBase.btLock) {
                        MicrontekServiceBase.btLock = false;
                        setParameters("av_phone=hangup");
                        sendBootCheck(context, "phonecallout");
                        MTCAdjVolume(2);
                        if (!MicrontekServiceBase.btLock && !backviewState && isPowerScreen) {
                            updatePowerScreen(true);
                        }
                    }
                }
            } else if (action.equals(Constant.MSG_ACTION_APP_TITLE)) {
                String packageName = intent.getStringExtra("pkname");
                if (packageName.equals(Constant.BACKVIEWPACKAGE) || packageName.equals(Constant.FRONTVIEWPACKAGE)) {
                    isYoutube = false;
                }
                MicrontekServiceBase.GPSPKNAME = Settings.System.getString(getContentResolver(), "gpspkname");
                if ("0".equals(SystemProperties.get("ro.product.rotatemode")) && "true".equals(SystemProperties.get("ro.product.rotate"))) {
                    if (TextUtils.isEmpty(packageName) || !packageName.startsWith("com.android.launcher")) {
                        launcherFlag = false;
                    } else {
                        launcherFlag = true;
                        if (updateLauncher) {
                            setDefaultLauncher(mPackageName, mClassName);
                            startHome();
                            updateLauncher = false;
                        }
                    }
                }
                if (!TextUtils.isEmpty(MicrontekServiceBase.GPSPKNAME) && MicrontekServiceBase.GPSPKNAME.equals(packageName)) {
                    MicrontekServiceBase.gpsOpen = true;
                    MicrontekServiceBase.gpsIsFront = true;
                    handler.sendEmptyMessage(MSG_GPS_TOP);
                } else if (packageName.equals("com.google.android.youtube")) {
                    Intent it1 = new Intent(Constant.MSG_MTC_BOOTCHECK);
                    it1.putExtra("class", "toutube");
                    context.sendBroadcastAsUser(it1, UserHandle.CURRENT_OR_SELF);
                    setParameters("av_channel_enter=sys");
                } else if (packageName.equals("com.netflix.mediaclient")) {
                    netflixState = true;
                } else if (packageName.equals("com.youku.phone")) {
                    focusRequest();
                } else if (packageName.equals(Constant.CLOCKSCREENPACKAGE)) {
                    if (!TextUtils.isEmpty(customerSub) && "SYCH".equals(customer)) {
                        netflixState = true;
                    }
                } else if ((!packageName.equals(Constant.BACKVIEWPACKAGE) && !packageName.equals(Constant.FRONTVIEWPACKAGE)) || !MicrontekServiceBase.gpsIsFront) {
                    appMode = -1;
                    MicrontekServiceBase.gpsIsFront = false;
                    netflixState = false;
                    handler.removeMessages(MSG_GPS_BACK);
                    handler.sendEmptyMessageDelayed(MSG_GPS_BACK, 1_000L);
                }
                isScreenlock = packageName.equals(Constant.CLOCKSCREENPACKAGE);
                if (backviewState && !packageName.equals(Constant.BACKVIEWPACKAGE)) {
                    startBackView();
                }
                if (!MicrontekServiceBase.btLock && !backviewState && isPowerScreen) {
                    updatePowerScreen(true);
                }
                handler.removeMessages(MSG_AV_REFRESH);
                handler.sendEmptyMessageDelayed(MSG_AV_REFRESH, 800L);
                handler.sendEmptyMessageDelayed(MSG_AV_REFRESH, 2_000L);
                if (isCarBox) {
                    saveCarBoxData(packageName);
                }
            } else if (action.equals("com.microntek.request.event")) {
                String type = intent.getStringExtra("type");
                if (!TextUtils.isEmpty(type)) {
                    if (type.contains("handbrake")) {
                        updateHandbrake();
                    }
                    if (type.contains("headlight")) {
                        updateHeadlight();
                    }
                    if (type.contains("backview")) {
                        updateBackview();
                    }
                    if (type.contains("power")) {
                        reportEvent("power", powerState);
                    }
                    if (type.contains("volume")) {
                        sendVolStatus(MicrontekServiceBase.mCurVolume);
                    }
                    if (type.contains("reardiaplay")) {
                        HCTApi.switchDualScreen();
                    }
                }
            } else if (action.equals(Constant.MSG_MTC_DARKLIGHT)) {
                String state = getParameters("sta_ill=");
                Intent intent2 = new Intent(Constant.STATECAR_LIGHT);
                intent2.putExtra("state", state);
                sendBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF);
            } else if (action.equals(Constant.MSG_ACTION_HCTREBOOT)) {
                powerReboot();
            } else if (action.equals(Constant.GPSCHANGE)) {
                String pkgName = intent.getStringExtra("pkname");
                if (pkgName == null) {
                    return;
                }
                carManager.putState("navi_package", MicrontekServiceBase.GPSPKNAME);
                Settings.System.putString(context.getContentResolver(), "gpspkname", pkgName);
            } else if (action.equals(AppManager.packageNameCARPLAY[0]) || action.equals(AppManager.packageNameCARPLAY[1]) || action.equals(AppManager.packageNameHICAR[0])) {
                if (intent.hasExtra("status")) {
                    String status = intent.getStringExtra("status");
                    if (status == null) {
                        return;
                    }
                    Log.i("carplay", "Status:" + status);
                    if (status.equals("MAIN_PAGE_SHOW")) {
                        MicrontekServiceBase.carPlayShow = true;
                    } else if (status.equals("MAIN_PAGE_HIDDEN")) {
                        MicrontekServiceBase.carPlayShow = false;
                    }
                } else if (!intent.hasExtra("command") || (cmd = intent.getStringExtra("command")) == null) {
                } else {
                    Log.i("carplay", "command:" + cmd);
                    if (cmd.equals("RES_APK_INFO") && intent.getStringExtra("regmode")
                        .contains("w")) {
                        AppManager.getInstance(context).setUsbIpodEnabled(false);
                    } else if (cmd.equals("RES_APK_INFO") && intent.getStringExtra("regmode")
                        .contains("l") && "HZC".equals(
                        customer) && intent.getStringExtra("regmode").contains("a")) {
                        AppManager.getInstance(context).setEasyConnEnabled(false);
                    }
                }
            } else if (action.equals(Constant.BROADCAST_EASYCONN_REGISTED)) {
                AppManager.getInstance(context).setUsbIpodEnabled(false);
            } else if (action.equals("com.microntek.CarManager.event")) {
                String parameter = intent.getStringExtra("parameter");
                carManager.setParameters(parameter);
            }
        }
    };

    private AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() { // from class: android.microntek.service.MicrontekServer.21
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public void onAudioFocusChange(int focusChange) {
        }
    };

    static /* synthetic */ int access$1008(MicrontekServer x0) {
        int i = x0.mCurInstallApk;
        x0.mCurInstallApk = i + 1;
        return i;
    }

    static /* synthetic */ int access$408(MicrontekServer x0) {
        int i = x0.mModeDoulbe;
        x0.mModeDoulbe = i + 1;
        return i;
    }

    static /* synthetic */ int access$508(MicrontekServer x0) {
        int i = x0.mHomeDoulbe;
        x0.mHomeDoulbe = i + 1;
        return i;
    }

    private void s_onStatusChanged(String type, Bundle bundle) {
        if (type.equals("CarEvent")) {
            DoCarEvent(bundle);
        } else if (type.equals("CarPower")) {
            String state = bundle.getString("type");
            this.durationTime = System.currentTimeMillis();
            DoCarPower(state);
        } else if (type.equals("KeyDown")) {
            DoCarKeyDown(bundle);
        } else if (type.equals("CarApp")) {
            DoCarApp(bundle);
        } else if (type.equals("CarBox")) {
            DoCarBox(bundle);
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager pm) {
        List<PackageInfo> installedList = pm.getInstalledPackages(8192);
        int installedListSize = installedList.size();
        for (int i = 0; i < installedListSize; i++) {
            PackageInfo tmp = installedList.get(i);
            if (packageName.equalsIgnoreCase(tmp.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setApplicationEnabled(String[] packageName, boolean enable, PackageManager pm) {
        int i;
        int j = packageName.length;
        for (int i2 = 0; i2 < j; i2++) {
            if (isPackageInstalled(packageName[i2], pm)) {
                String str = packageName[i2];
                if (enable) {
                    i = 1;
                } else {
                    i = 2;
                }
                pm.setApplicationEnabledSetting(str, i, 1);
            }
        }
    }

    @Override // android.microntek.service.MicrontekServiceBase, android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mPm = getPackageManager();
        this.carManager.attach(new Handler() { // from class: android.microntek.service.MicrontekServer.3
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                s_onStatusChanged((String) msg.obj, msg.getData());
            }
        }, "CarEvent,CarPower,KeyDown,CarApp,CarBox");
        this.onCraeteTime = System.currentTimeMillis();
        InitData();
        InitSystemData();
        InitIntentFilter();
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        if (1 == Settings.Global.getInt(this.getApplicationContext().getContentResolver(), "airplane_mode_on", 0)) {
            setAirplaneModeOn(false);
        }
        if ("XLY".equals(HctUtil.getCustomerSub()) || "HZC29".equals(HctUtil.getCustomerSub())) {
            Settings.System.putInt(getContentResolver(), "XLYsetLanguage", 1);
        }
        if ("XLY".equals(HctUtil.getCustomerSub()) && Settings.System.getInt(getContentResolver(), "XLYPersianCale", 2) == 2) {
            Settings.System.putInt(getContentResolver(), "XLYPersianCale", 1);
        }
        this.handler.removeMessages(21);
        this.handler.sendEmptyMessageDelayed(21, 30000L);
        if (this.isCarBox) {
            List<ComponentName> appList = fetchAutoApps();
            for (ComponentName componentName : appList) {
                if (!TextUtils.isEmpty(componentName.getClassName())) {
                    this.mPm.setComponentEnabledSetting(componentName, 2, 1);
                }
            }
        }
    }

    private List<ComponentName> fetchAutoApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
        List<ComponentName> appList = new ArrayList<>();
        List<ResolveInfo> resolveInfoList = pm.queryBroadcastReceivers(intent, 512);
        if (resolveInfoList.size() > 0) {
            for (int i = 0; i < resolveInfoList.size(); i++) {
                if ((resolveInfoList.get(i).activityInfo.applicationInfo.flags & 1) <= 0) {
                    ComponentName mComponentName = new ComponentName(resolveInfoList.get(i).activityInfo.packageName, resolveInfoList.get(i).activityInfo.name);
                    appList.add(mComponentName);
                }
            }
        }
        return appList;
    }

    private void startPkg(String PACKAGE, String CLASS) {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(PACKAGE, CLASS));
            intent.addFlags(807600128);
            startActivity(intent);
            collapsingNotification(getApplicationContext());
        } catch (Exception e) {
            this.handler.removeMessages(21);
            this.handler.sendEmptyMessageDelayed(21, 30000L);
        }
    }

    public static void collapsingNotification(Context context) {
        Method collapse;
        try {
            Object service = context.getSystemService("statusbar");
            if (service == null) {
                return;
            }
            int sdkVersion = Build.VERSION.SDK_INT;
            if (sdkVersion <= 16) {
                collapse = service.getClass().getMethod("collapse", new Class[0]);
            } else {
                collapse = service.getClass().getMethod("collapsePanels", new Class[0]);
            }
            collapse.setAccessible(true);
            collapse.invoke(service, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void InitData() {
        if (this.carManager != null) {
            String powerstate = this.carManager.getStringState("carpower");
            DoCarPower(powerstate);
            this.backviewState = this.carManager.getBooleanState("backview");
            updateBackview();
            this.mHandbrake = this.carManager.getBooleanState("handbrake");
            updateHandbrake();
            this.mHeadlight = this.carManager.getBooleanState("headlight");
            updateHeadlight();
            this.mAjx = this.carManager.getBooleanState("ajx");
            UpdataAjx();
            this.mOrientation = this.carManager.getIntState("orientation");
            UpdataOrientation(false);
            updatePowerScreen(false);
            boolean startApp = this.carManager.getBooleanState("carstartapp");
            if (startApp) {
                NeedStartApp();
            }
        }
        this.mMfiManager.registerListener(this.mfiListener);
    }

    private void NeedStartApp() {
        if (btLock) {
            return;
        }
        this.isBoxStartApp = true;
        if (this.backviewState) {
            mNeedStartApp = true;
        } else {
            MtcStartApp();
        }
    }

    private void InitSystemData() {
        this.handler.sendEmptyMessageDelayed(3, 15000L);
        String enscreenclock = GetSystemProperties("ro.product.screenclock");
        if (enscreenclock.equals("true") || getParameters("sta_function=18").equals("1") || Settings.System.getInt(getContentResolver(), "screenState", 0) == 1) {
            this.screensaverEnableLocal = true;
            this.screensaverEnable = true;
            this.screensaverTimeout = Settings.System.getInt(getContentResolver(), "musicscreen_timeout", 30);
        }
        this.handler.sendEmptyMessageDelayed(0, 1000L);
        this.noDVD = getParameters("cfg_dvd=").equals("0");
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(phoneListener, 32);
        initGps();
        if ("HZC".equals(HctUtil.getCustomerSub()) || "HZC24".equals(HctUtil.getCustomerSub())) {
            Settings.System.putInt(getContentResolver(), "PowerOnIsShowPasswordView", 1);
        }
        if ("HZC41".equals(HctUtil.getCustomerSub())) {
            Settings.System.putInt(this.getApplicationContext().getContentResolver(), "LauncherPauseState", 0);
        }
    }

    private void initGps() {
        GPSPKNAME = Settings.System.getString(getContentResolver(), "gpspkname");
        if (GPSPKNAME == null) {
            GPSPKNAME = SystemProperties.get("ro.product.gpspkname");
        }
        if (TextUtils.isEmpty(GPSPKNAME)) {
            GPSPKNAME = "";
        }
        this.carManager.putState("navi_package", GPSPKNAME);
        String gpsstrString = Settings.System.getString(getContentResolver(), Constant.MTCGPSMONITOR);
        if (TextUtils.isEmpty(gpsstrString)) {
            gpsstrString = "on";
        }
        setParameters(Constant.MTCGPSMONITOR + gpsstrString);
        String gpsstrString2 = Settings.System.getString(getContentResolver(), Constant.MTCGPSSWITCH);
        if (TextUtils.isEmpty(gpsstrString2)) {
            gpsstrString2 = "139";
        }
        setParameters(Constant.MTCGPSSWITCH + gpsstrString2);
        String gpsstrString3 = Settings.System.getString(getContentResolver(), Constant.MTCGPSGAIN);
        if (TextUtils.isEmpty(gpsstrString3)) {
            gpsstrString3 = "on";
        }
        setParameters(Constant.MTCGPSGAIN + gpsstrString3);
        String gpsstrString4 = Settings.System.getString(getContentResolver(), Constant.MTCGPSBACKVOL);
        if (TextUtils.isEmpty(gpsstrString4)) {
            gpsstrString4 = "0";
        }
        setParameters(Constant.MTCGPSBACKVOL + gpsstrString4);
    }

    private void TickTask() {
        if (this.screensaverEnableLocal) {
            this.screensaverTimeout = Settings.System.getInt(getContentResolver(), "musicscreen_timeout", 30);
        }
        if (btLock || gpsIsFront
            || this.backviewState || this.powerState != 2 || this.screensaverTimeout <= 0 || !this.screensaverEnable) {
            this.screensaverTimer = 0;
        }
        int i = this.screensaverTimer;
        if (i == 0) {
            if (this.screensaverTimeout > 0) {
                sendBroadcastAsUser(new Intent(Constant.CLOCKEND), UserHandle.CURRENT_OR_SELF);
            }
            this.ScreenSaverOn = false;
        } else if (!this.ScreenSaverOn && i >= this.screensaverTimeout) {
            this.ScreenSaverOn = true;
            if (this.screensaverEnableLocal) {
                startMusicClock();
            } else {
                Intent it1 = new Intent(Constant.MSG_MTC_SCREENSAVER);
                it1.putExtra("timer", this.screensaverTimer);
                sendBroadcastAsUser(it1, UserHandle.CURRENT_OR_SELF);
            }
        }
        this.screensaverTimer++;
        this.handler.sendEmptyMessageDelayed(0, 1000L);
        Intent it = new Intent(Constant.MSG_MTC_TIME_FRESH);
        it.addFlags(268435456);
        sendBroadcastAsUser(it, UserHandle.CURRENT_OR_SELF);
    }

    private void clearMusicClock() {
        int musicTimeout = Settings.System.getInt(getContentResolver(), "musicscreen_timeout", 30);
        handler.removeMessages(MSG_MUSIC_CLOCK);
        if (musicTimeout != -1) {
            screensaverTimer = 0;
        }
    }

    private void InitIntentFilter() {
        IntentFilter itfl = new IntentFilter();
        itfl.addAction("android.intent.action.MEDIA_MOUNTED");
        itfl.addAction("android.intent.action.MEDIA_EJECT");
        itfl.addAction("android.intent.action.MEDIA_UNMOUNTED");
        itfl.addDataScheme("file");
        registerReceiver(this.mediaDetectReceiver, itfl);
        IntentFilter itfl2 = new IntentFilter();
        itfl2.addAction(Constant.MSG_BEEP);
        itfl2.addAction(Constant.MSG_SHOW_VOLUME);
        itfl2.addAction(Constant.MSG_ACTIVE);
        itfl2.addAction(Constant.MSG_MTC_CLEAR);
        itfl2.addAction(Constant.MSG_START_RADIO);
        itfl2.addAction(Constant.MSG_START_MUSIC);
        itfl2.addAction(Constant.MSG_START_IPOD);
        itfl2.addAction(Constant.MSG_START_BTMUSIC);
        itfl2.addAction(Constant.MSG_MTC_PROMPT);
        itfl2.addAction(Constant.MSG_MTC_APP);
        itfl2.addAction(Constant.MSG_MTC_VOLUME_SET);
        itfl2.addAction(Constant.MSG_MTC_BLIGHT_SET);
        itfl2.addAction(Constant.MSG_MTC_CLOSEPACKAGE);
        itfl2.addAction(Constant.MSG_BEEP_CLEAR_SCREEN);
        itfl2.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(this.MTCAPPProc, itfl2);
        IntentFilter itfl3 = new IntentFilter();
        itfl3.addAction(Constant.GPSCHANGE);
        itfl3.addAction(Constant.MSG_MTC_DARKLIGHT);
        itfl3.addAction(Constant.BROADCAST_BT_REPORT);
        itfl3.addAction(Constant.MSG_ACTION_HCTREBOOT);
        itfl3.addAction(Constant.MSG_ACTION_APP_TITLE);
        itfl3.addAction("com.microntek.request.event");
        itfl3.addAction(Constant.MSG_MTC_POWER_OFFDONE);
        itfl3.addAction(AppManager.packageNameCARPLAY[0]);
        itfl3.addAction(AppManager.packageNameHICAR[0]);
        itfl3.addAction(AppManager.packageNameCARPLAY[1]);
        itfl3.addAction(Constant.BROADCAST_EASYCONN_REGISTED);
        itfl3.addAction("com.microntek.CarManager.event");
        registerReceiver(this.MTCploy, itfl3);
        IntentFilter itfl4 = new IntentFilter();
        itfl4.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        itfl4.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        itfl4.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        registerReceiver(this.wifiReceiver, itfl4);
        IntentFilter itfl5 = new IntentFilter();
        itfl5.addAction(Constant.MSG_INSTALL_XRROSS);
        registerReceiver(this.installApkReceiver, itfl5);
        IntentFilter itfl6 = new IntentFilter();
        itfl6.addAction(Constant.ACTION_PLUGGED);
        itfl6.addAction("com.microntek.extshow.start");
        registerReceiver(this.hdmiReceiver, itfl6);
        IntentFilter itfl7 = new IntentFilter();
        itfl7.addAction("android.intent.action.PACKAGE_ADDED");
        itfl7.addAction("android.intent.action.PACKAGE_REMOVED");
        itfl7.addAction("android.intent.action.PACKAGE_CHANGED");
        itfl7.addDataScheme("package");
        registerReceiver(this.installReceiver, itfl7);
        IntentFilter itfl8 = new IntentFilter();
        itfl8.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(this.phoneReceiver, itfl8);
        IntentFilter itfl9 = new IntentFilter();
        itfl9.addAction("android.intent.action.LOCALE_CHANGED");
        registerReceiver(this.localeReceiver, itfl9);
        IntentFilter myFilter = new IntentFilter();
        myFilter.addAction("changescreenclock");
        registerReceiver(this.screenClockBroadcast, myFilter);
    }

    private void setInstallPackagesPermissions(String packageName) {
        try {
            AppOpsManager mAppOpsManager = (AppOpsManager) getSystemService("appops");
            PackageManager mPm = getPackageManager();
            PackageInfo mPackageInfo = mPm.getPackageInfo(packageName, 4198976);
            mAppOpsManager.setMode(66, mPackageInfo.applicationInfo.uid, packageName, 0);
        } catch (Exception e) {
            Log.e("wuwq", "Exception when retrieving package:" + packageName, e);
        }
    }

    private void setCanInstallApps(boolean newState, String mPackageName) {
        this.mAppOpsManager.setMode(66, this.mPackageInfo.applicationInfo.uid, mPackageName, newState ? 0 : 2);
    }

    @Override // android.microntek.service.MicrontekServiceBase, android.app.Service
    public void onDestroy() {
        this.mMfiManager.unregisterListener(this.mfiListener);
        this.carManager.detach();
        this.handler.removeCallbacksAndMessages(null);
        unregisterReceiver(this.mediaDetectReceiver);
        unregisterReceiver(this.MTCAPPProc);
        unregisterReceiver(this.MTCploy);
        unregisterReceiver(this.wifiReceiver);
        unregisterReceiver(this.installApkReceiver);
        unregisterReceiver(this.hdmiReceiver);
        unregisterReceiver(this.installReceiver);
        unregisterReceiver(this.phoneReceiver);
        unregisterReceiver(this.localeReceiver);
        unregisterReceiver(this.screenClockBroadcast);
        super.onDestroy();
    }

    private void DoCarEvent(Bundle bundle) {
        String type = bundle.getString("type");
        if ("handbrake".equals(type)) {
            this.mHandbrake = bundle.getBoolean(MicrontekServiceBase.VALUE);
            UpdataDrivingState();
            updateHandbrake();
        } else if ("headlight".equals(type)) {
            this.mHeadlight = bundle.getBoolean(MicrontekServiceBase.VALUE);
            updateHeadlight();
        } else if ("backview".equals(type)) {
            this.backviewState = bundle.getBoolean(MicrontekServiceBase.VALUE);
            updateBackview();
        } else if ("ajx".equals(type)) {
            this.mAjx = bundle.getBoolean(MicrontekServiceBase.VALUE);
            UpdataAjx();
        } else if ("ipod".equals(type)) {
            boolean ipod = bundle.getBoolean(MicrontekServiceBase.VALUE);
            if (!this.mIpod && ipod && this.powerState == 2 && !btLock && !this.backviewState) {
                startIpod(0);
            }
            this.mIpod = ipod;
        } else if ("tp_not_found".equals(type)) {
            startTouchKeyStudy();
        } else if ("touch_down".equals(type)) {
            if (this.isPowerScreen) {
                return;
            }
            clearMusicClock();
            if ("YH".equals(this.customer)) {
                this.handler.removeMessages(21);
            }
        } else if ("touch_up".equals(type)) {
            if (this.isPowerScreen) {
                return;
            }
            clearMusicClock();
            if (this.isScreenlock) {
                sendBroadcastAsUser(new Intent(Constant.CLOCKEND), UserHandle.CURRENT_OR_SELF);
                this.isScreenlock = false;
            }
            if ("YH".equals(this.customer)) {
                this.handler.removeMessages(21);
                if (Constant.BTPACKAGE.equals(HctUtil.getTopActivityPackageName(this.mContext))) {
                    this.handler.sendEmptyMessageDelayed(21, 30000L);
                } else {
                    this.handler.sendEmptyMessageDelayed(21, 30000L);
                }
            }
        } else if ("mute".equals(type)) {
            MuteShow(bundle.getBoolean(MicrontekServiceBase.VALUE));
        } else if ("firststart".equals(type)) {
            Settings.System.putInt(this.getApplicationContext().getContentResolver(), "hasStartApp", 1);
            if (!this.isCarBox) {
                Settings.System.putString(getContentResolver(), Constant.BKPACKAGE_STRING, "");
            }
            Settings.System.putInt(this.getApplicationContext().getContentResolver(), "canbus_updata", 1);
            if (this.mMcuVersion != null && this.mMcuVersion.contains("_GS_")) {
                Settings.System.putInt(getContentResolver(), Constant.FIRSTBOOT_STRING, 128);
            }
            if ("HZC27".equals(this.customerSub)) {
                Settings.System.putInt(getContentResolver(), "isPowerOn", 0);
                Settings.System.putInt(getContentResolver(), "isLock", 1);
            }
        } else if ("overheat".equals(type)) {
            showFloatView(true);
        } else if ("screen_onoff".equals(type)) {
            reportEvent(type, bundle.getBoolean(MicrontekServiceBase.VALUE));
        } else if ("car_info".equals(type)) {
            int flag = bundle.getInt(MicrontekServiceBase.VALUE);
            if ((flag & 3) > 0) {
                startCarCD(flag);
            }
        } else if ("orientation".equals(type)) {
            this.mOrientation = bundle.getInt(MicrontekServiceBase.VALUE);
            UpdataOrientation(true);
        } else if ("user_touch_study".equals(type)) {
            int act = bundle.getInt(MicrontekServiceBase.VALUE);
            if (act == 1) {
                this.handler.postDelayed(new Runnable() { // from class: android.microntek.service.MicrontekServer.10
                    @Override // java.lang.Runnable
                    public void run() {
                        startTouchKeyStudy();
                    }
                }, 2000L);
            } else {
                showToastMsg("Enter touch study", -1);
            }
        } else if ("power_screen".equals(type)) {
            updatePowerScreen(false);
        }
    }

    private void DoCarPower(String state) {
        if (state == null) {
            return;
        }
        if (state.equals("power_on")) {
            if (this.powerState != 2) {
                powerOn();
            }
            this.wifiFirstRevFlag = false;
            this.isFirstUpdateWifiAPState = true;
            if (1 != this.powerState) {
                this.handler.removeMessages(12);
                Message msg = this.handler.obtainMessage();
                msg.what = 12;
                if (getWifiDriverState()) {
                    this.handler.sendMessageDelayed(msg, this.mlPwerOnWifiApCheckDelayMillis);
                } else {
                    this.handler.sendMessageDelayed(msg, 5000L);
                }
                this.updatingWifiAPState = true;
            }
            this.powerState = 2;
            setParameters("rpt_power=true");
            this.getApplicationContext().sendBroadcastAsUser(new Intent("android.intent.action.SCREEN_ON"), UserHandle.ALL);
            if ("YH".equals(this.customer) && (this.durationTime - this.onCraeteTime) / 1000 > 8) {
                showYHLogoView(true);
                this.handler.removeMessages(18);
                this.handler.sendEmptyMessageDelayed(18, 2000L);
            }
            if ("XHWSBOX".equals(this.customer)) {
                Settings.System.putInt(getContentResolver(), CARTOUCH_SHOW, 0);
            }
            showBlackView(true);
        } else if (state.equals("power_off")) {
            powerOff();
            if (this.powerState == 0 || -1 == this.powerState) {
                this.handler.removeMessages(12);
                Message msg2 = this.handler.obtainMessage();
                msg2.what = 12;
                if (getWifiDriverState()) {
                    this.handler.sendMessageDelayed(msg2, this.mlPwerOnWifiApCheckDelayMillis);
                } else {
                    this.handler.sendMessageDelayed(msg2, 5000L);
                }
                this.updatingWifiAPState = true;
            }
            this.powerState = 1;
            this.mlPwerOnWifiApCheckDelayMillis = 100L;
            if (this.customerSub.equals("HZC4")) {
                return;
            }
        } else if (state.equals("acc_off")) {
            powerOff();
            if (this.powerState != 0) {
                this.mlPwerOnWifiApCheckDelayMillis = 100L;
                saveWifiAPState();
            }
            this.powerState = 0;
            if (this.customerSub.equals("HZC4")) {
                return;
            }
        } else if (state.equals("sleep")) {
            this.mlPwerOnWifiApCheckDelayMillis = 3000L;
            this.getApplicationContext().sendBroadcastAsUser(new Intent("android.intent.action.SCREEN_OFF"), UserHandle.ALL);
            deviceunMountAndSleep();
            this.powerState = -1;
            if (this.customerSub.equals("HZC4")) {
                return;
            }
        } else {
            return;
        }
        reportEvent("power", this.powerState);
    }

    private void saveWifiAPState() {
        this.handler.removeMessages(12);
        this.wifiApCheckCount = 20;
        if (this.updatingWifiAPState) {
            Log.d("wuwq", "saveWifiAPState: ####### mUpdataingWifiAPState = true");
        } else if (getWifiDriverState()) {
            boolean apstate = getWifiApState() == 13;
            boolean wifistate = this.mWifiManager.isWifiEnabled();
            Settings.System.putInt(getContentResolver(), "status_acc_off_wifi_opened", wifistate ? 1 : 0);
            Settings.System.putInt(getContentResolver(), "status_acc_off_ap_opened", apstate ? 1 : 0);
            if (apstate) {
                setWifiApEnabled(false);
            }
            if (apstate && this.mGtPlatform) {
                setWifiOn(false);
            }
            if (wifistate) {
                setWifiOn(false);
            }
        }
    }

    private void updataWifiAPState() {
        boolean apstate = Settings.System.getInt(getContentResolver(), "status_acc_off_ap_opened", 0) != 0;
        boolean wifistate = Settings.System.getInt(getContentResolver(), "status_acc_off_wifi_opened", 0) != 0;
        int i = this.wifiApCheckCount;
        if (i > 0) {
            this.wifiApCheckCount = i - 1;
            if (!apstate && !wifistate) {
                this.isFirstUpdateWifiAPState = false;
                this.updatingWifiAPState = false;
                return;
            }
            if (getWifiDriverState()) {
                if (this.isFirstUpdateWifiAPState && !this.mGtPlatform) {
                    if (apstate) {
                        setWifiApEnabled(false);
                    } else if (wifistate) {
                        setWifiOn(false);
                    }
                    this.isFirstUpdateWifiAPState = false;
                } else {
                    boolean apstate2 = getWifiApState() == 13;
                    boolean wifistate2 = this.mWifiManager.isWifiEnabled();
                    if (apstate2 || wifistate2) {
                        this.wifiApCheckCount = 0;
                        if (wifistate) {
                            this.mWifiManager.startScan();
                        }
                        this.updatingWifiAPState = false;
                        return;
                    }
                }
            }
            if (apstate) {
                setWifiApEnabled(true);
            } else if (wifistate) {
                setWifiOn(true);
            }
            this.handler.removeMessages(12);
            Message msg = this.handler.obtainMessage();
            msg.what = 12;
            this.handler.sendMessageDelayed(msg, 1000L);
            return;
        }
        Log.i("wuwq", "updataWifiAPState is timeout");
        this.isFirstUpdateWifiAPState = false;
        this.updatingWifiAPState = false;
    }

    private void DoCarKeyDown(Bundle bundle) {
        String type = bundle.getString("type");
        if (type.equals("key")) {
            int keycode = bundle.getInt(MicrontekServiceBase.VALUE);
            reportEvent("key", keycode);
            if (keycode != 273 || keycode != 281 || keycode != 519) {
                clearMusicClock();
            }
            DoPressKeyTask(keycode);
            ChangeKeyTab(keycode);
        }
    }

    private void DoCarApp(Bundle bundle) {
        String type = bundle.getString("type");
        if (type.equals("start_app") && !this.isPowerScreen) {
            NeedStartApp();
        } else if (type.equals("start_ipod")) {
            if (this.powerState == 2 && !btLock && !this.backviewState && this.mIpod) {
                startIpod(0);
            }
        } else if (type.equals("start_dvd")) {
            startDVD(0);
        }
    }

    private void DoCarBox(Bundle bundle) {
        String type = bundle.getString("type");
        if (type.equals("start_app")) {
            boolean is = bundle.getBoolean(MicrontekServiceBase.VALUE);
            if (!this.isBoxStartApp && is) {
                NeedStartApp();
                Log.i(TAG, "DoCarBox start_app ");
            }
            this.isBoxStartApp = true;
        }
    }

    private void updateBackview() {
        Intent it = new Intent(MicrontekServiceBase.REPORT_EVENT);
        it.putExtra("type", "backview");
        it.putExtra(MicrontekServiceBase.VALUE, this.backviewState);
        sendBroadcastAsUser(it, UserHandle.ALL);
        if (this.backviewState) {
            startBackView();
        } else if (needRunNavi) {
            if (isGpsCardMounted()) {
                needRunNavi = false;
                runApp(GPSPKNAME);
            }
        } else if (mNeedStartApp) {
            mNeedStartApp = false;
            needRunNavi = false;
            MtcStartApp();
        }
    }

    private void DoPressKeyTask(int key) {
        if ("HZC27".equals(this.customerSub) && Settings.System.getInt(getContentResolver(), "isLock", 0) > 0) {
            return;
        }
        switch (key) {
            case 13:
                setBackOnoff();
                return;
            case 256:
                if (!"TELENAV".equals(this.customer) || !SystemProperties.get("sys.telenav.keycode.mode.isIntercepted", "").equals("true")) {
                    this.handler.sendEmptyMessage(14);
                    return;
                }
                return;
            case 258:
                MuteSwitch();
                return;
            case 273:
                this.handler.sendEmptyMessage(11);
                return;
            case 277:
                if (checkLastSwitchTime()) {
                    startGFsel();
                    return;
                }
                return;
            case 281:
                this.handler.sendEmptyMessage(10);
                return;
            case 296:
                if (checkLastSwitchTime()) {
                    startDVD(0);
                    return;
                }
                return;
            case 297:
            case 353:
                if (checkLastSwitchTime()) {
                    startRadio(0);
                    return;
                }
                return;
            case 298:
                LoudSwitch();
                return;
            case 303:
                EQSwitch();
                return;
            case 304:
                if (checkLastSwitchTime() && !btLock) {
                    startBT(1);
                    return;
                }
                return;
            case 305:
                if (checkLastSwitchTime()) {
                    startGPS();
                    return;
                }
                return;
            case 308:
                SystemKey(4, 0);
                return;
            case 309:
                SystemKey(82, 0);
                return;
            case 310:
                String customer = SystemProperties.get("ro.product.customer", "HCT");
                if (customer.equals("YH")) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setFlags(268435456);
                    intent.addCategory("android.intent.category.HOME");
                    startActivity(intent);
                    return;
                } else if (this.mDualHomeMode == 1) {
                    this.handler.sendEmptyMessage(16);
                    return;
                } else {
                    this.handler.sendEmptyMessage(17);
                    return;
                }
            case 316:
                if (!btLock && !carPlayShow && IsSwitchToBT()) {
                    startBT(1);
                    return;
                }
                return;
            case 319:
                if (checkLastSwitchTime()) {
                    startAux(0);
                    return;
                }
                return;
            case 320:
                if (checkLastSwitchTime()) {
                    startDTV(0);
                    return;
                }
                return;
            case 321:
                startSettings();
                return;
            case 327:
                if (!btLock && !carPlayShow && IsSwitchToBT()) {
                    startBT(1);
                    return;
                }
                return;
            case 331:
                if ("WCX".equals(HctUtil.getCustomer()) || "WE".equals(HctUtil.getCustomerSub())) {
                    String musicName = Settings.System.getString(getContentResolver(), "music_name");
                    if (!TextUtils.isEmpty(musicName)) {
                        runApp(musicName);
                        return;
                    }
                    return;
                } else if ("CHSS".equals(this.customerSub) || "CHS8".equals(this.customerSub)) {
                    runApp(getCHSSAppPkName(1));
                    return;
                } else {
                    startMusic(null, 0);
                    return;
                }
            case 332:
                IRsetBrightness();
                return;
            case 333:
                if (checkLastSwitchTime()) {
                    startFrontView(0);
                    return;
                }
                return;
            case 336:
                if ("WE".equals(HctUtil.getCustomerSub())) {
                    String movieName = Settings.System.getString(getContentResolver(), "movie_name");
                    if (!TextUtils.isEmpty(movieName)) {
                        runApp(movieName);
                        return;
                    }
                    return;
                } else if (checkLastSwitchTime()) {
                    startMovie(0);
                    return;
                } else {
                    return;
                }
            case 337:
                if (checkLastSwitchTime()) {
                    if (this.mUsbIpodSupport) {
                        if (this.mUsbIpod) {
                            startUsbIpod(0);
                            return;
                        }
                        return;
                    }
                    startIpod(0);
                    return;
                }
                return;
            case 339:
                if (checkLastSwitchTime()) {
                    startScreenShot();
                    return;
                }
                return;
            case 359:
                if (checkLastSwitchTime()) {
                    startDeskClock();
                    return;
                }
                return;
            case 362:
                if (checkLastSwitchTime()) {
                    startSYNC();
                    return;
                }
                return;
            case 365:
                if (checkLastSwitchTime()) {
                    sendBroadcastAsUser(new Intent(Constant.MSG_MTC_RECENT), UserHandle.CURRENT_OR_SELF);
                    return;
                }
                return;
            case 384:
                if (checkLastSwitchTime() && !btLock && !HctUtil.getTopActivityClassName(this.mContext).startsWith(AppManager.packageNameCARPLAY[0]) && !HctUtil.getTopActivityClassName(this.mContext).startsWith("com.zjinnova.android.zlink") && !HctUtil.getTopActivityClassName(this.mContext).startsWith(AppManager.packageNameCARPLAY[1]) && !HctUtil.getTopActivityClassName(this.mContext).startsWith(AppManager.packageNameHICAR[0]) && !startTxzVoice()) {
                    startGoogleVoice();
                    return;
                }
                return;
            case 514:
                if (checkLastSwitchTime() && !btLock) {
                    startFrontView(1);
                    return;
                }
                return;
            case 516:
                if (checkLastSwitchTime() && !btLock) {
                    startSoundexpress();
                    return;
                }
                return;
            case 517:
                startTouchKeyStudy();
                return;
            case 519:
                if (!this.isPowerScreen) {
                    if (this.customerSub.equals("zst25") && this.screenOn) {
                        MuteSwitch();
                        this.screenOn = false;
                        startHome();
                        return;
                    } else if (this.customerSub.equals("zst25") && !this.screenOn) {
                        MuteSwitch();
                        this.screenOn = true;
                        startscreenlock();
                        return;
                    } else if (checkLastSwitchTime() && !btLock && !Constant.CLOCKSCREENPACKAGE.equals(HctUtil.getTopActivityPackageName(this.mContext))) {
                        this.screensaverTimer = this.screensaverTimeout;
                        startMusicClock();
                        return;
                    } else {
                        clearMusicClock();
                        sendBroadcastAsUser(new Intent(Constant.CLOCKEND), UserHandle.CURRENT_OR_SELF);
                        return;
                    }
                }
                return;
            case 520:
                if (checkLastSwitchTime()) {
                    startRadio(0, 0);
                    return;
                }
                return;
            case 521:
                if (checkLastSwitchTime()) {
                    startRadio(0, 3);
                    return;
                }
                return;
            case 786:
                if (checkLastSwitchTime() && !btLock) {
                    startEasyconected();
                    return;
                }
                return;
            case 787:
                if (checkLastSwitchTime() && !btLock) {
                    startZlink();
                    return;
                }
                return;
            case 788:
                if (checkLastSwitchTime() && !btLock) {
                    startBtMusic();
                    return;
                }
                return;
            case 791:
                if (checkLastSwitchTime() && !btLock) {
                    startAskAlexa();
                    return;
                }
                return;
            case 792:
                startAVM();
                return;
            case 793:
                startTMPS();
                return;
            case 800:
                Log.e("Microntk", "-------rotate----");
                if ("true".equals(SystemProperties.get("ro.product.rotate"))) {
                    setParameters("ctl_key=800");
                    return;
                }
                return;
            default:
                return;
        }
    }

    private String getCHSSAppPkName(int type) {
        ContentResolver contentResolver = getContentResolver();
        String pk = Settings.System.getString(contentResolver, "chss_launcher_app_" + type);
        if (TextUtils.isEmpty(pk) || !HctUtil.isPackageInstalled(this.mContext, pk)) {
            return type == 1 ? Constant.MUSICPACKAGE : Constant.MOVIEPACKAGE;
        }
        return pk;
    }

    private void powerOn() {
        String VOL;
        this.initialTime = System.currentTimeMillis();
        showFloatView(false);
        if (this.isPowerScreen) {
            updatePowerScreen(true);
        }
        handler.removeCallbacks(PowerLongPress);
        handler.removeCallbacks(powerOffRunnable);
        this.handler.removeMessages(8);
        this.handler.sendEmptyMessage(8);
        this.handler.removeMessages(21);
        this.handler.sendEmptyMessageDelayed(21, 30000L);
        GetSystemProperties("ro.product.customer");
        if (this.powerState == 0 && !isGpsCardMounted()) {
            mDeviceLock = true;
            this.handler.removeMessages(3);
            this.handler.sendEmptyMessageDelayed(3, 15000L);
        }
        if (btLock || simPhoneLock) {
            VOL = Constant.PHONEVOLUME;
        } else {
            VOL = Constant.MTCVOLUME;
        }
        int current = Settings.System.getInt(getContentResolver(), VOL, KEY_VOLMAX / 2);
        if (this.isOrgPanel) {
            if (mVolMaxDefault != 0) {
                Settings.System.putInt(getContentResolver(), VOL, mVolMaxDefault);
            }
        } else if (mVolMaxDefault != 0) {
            Settings.System.putInt(getContentResolver(), VOL, mVolMaxDefault);
        } else if (current > (KEY_VOLMAX * 2) / 5) {
            if (!"YH".equals(this.customer)) {
                current = (KEY_VOLMAX * 2) / 5;
            }
            Settings.System.putInt(getContentResolver(), VOL, current);
        }
        MTCAdjVolume(2);
        ReportCanBusDisPlay("type", "on");
        if (!isGpsOn()) {
            openGps();
        }
        if (!this.mMfi.equals("2")) {
            if (!this.mGtPlatform) {
                return;
            }
            if (!this.mMfi.equals("1") && !this.mMfi.equals("2")) {
                return;
            }
        }
        if ("1".equals(this.mCarPlayType)) {
            SystemProperties.set(AppManager.packageNameCARPLAY[0], "enable");
            Intent it = new Intent(AppManager.packageNameCARPLAY[0]);
            it.addFlags(16777216);
            it.putExtra("command", "ACTION_ENTER");
            this.getApplicationContext().sendBroadcastAsUser(it, UserHandle.ALL);
            it.setPackage(AppManager.packageNameCARPLAY[0]);
            this.getApplicationContext().sendBroadcastAsUser(it, UserHandle.ALL);
        } else if ("2".equals(this.mCarPlayType)) {
            SystemProperties.set(AppManager.packageNameCARPLAY[1], "enable");
            Intent it2 = new Intent(AppManager.packageNameCARPLAY[1]);
            it2.addFlags(16777216);
            it2.putExtra("command", "ACTION_ENTER");
            this.getApplicationContext().sendBroadcastAsUser(it2, UserHandle.ALL);
            it2.setPackage(AppManager.packageNameCARPLAY[1]);
            this.getApplicationContext().sendBroadcastAsUser(it2, UserHandle.ALL);
        }
    }

    private void powerOff() {
        this.handler.removeMessages(3);
        needRunNavi = false;
        mNeedStartApp = false;
        mLastHasGpsCard = isGpsCardMounted();
        if (this.powerState == 2 && !this.customerSub.equals("HZC4")) {
            this.handler.removeCallbacks(this.PowerLongPress);
            this.handler.post(this.PowerLongPress);
        }
        savePoweroffData();
        ReportCanBusDisPlay("type", "off");
        this.handler.removeCallbacks(powerOffRunnable);
        this.handler.postDelayed(powerOffRunnable, 1000L);
        showBlackView(false);
    }

    private void savePoweroffData() {
        if (this.powerState == 2) {
            int state = Settings.System.getInt(this.getApplicationContext().getContentResolver(), "hasStartApp", 1);
            if (state == 1) {
                Settings.System.putInt(this.getApplicationContext().getContentResolver(), "hasStartApp", 0);
                String[] savepackage = new String[3];
                int i = 0;
                if (HctUtil.isAppRunning(this.mContext, Constant.RECPACKAGE)) {
                    int i2 = 0 + 1;
                    savepackage[0] = Constant.RECPACKAGE;
                    i = i2;
                }
                String mtcpackagename = getRunMtcAppPackageName();
                if (mtcpackagename == null) {
                    String toppackagename = HctUtil.getTopActivityPackageName(this.mContext);
                    if (Constant.ZLINKPACKAGE.equals(toppackagename)) {
                        int i3 = i + 1;
                        savepackage[i] = toppackagename;
                        String className = HctUtil.getTopActivityClassName(this.mContext);
                        Settings.System.putString(this.getApplicationContext().getContentResolver(), Constant.ZLINKCLASS_STRING, className);
                    } else if (!checkPKFilter(toppackagename)) {
                        int i4 = i + 1;
                        savepackage[i] = toppackagename;
                    }
                } else {
                    if (mtcpackagename.equals(Constant.BTPACKAGE) && HctUtil.isAppRunning(this.mContext, Constant.BTMUSICPACKAGE)) {
                        mtcpackagename = Constant.BTMUSICPACKAGE;
                    }
                    int i5 = i + 1;
                    savepackage[i] = mtcpackagename;
                    if (gpsIsFront && gpsOpen) {
                        int i6 = i5 + 1;
                        savepackage[i5] = GPSPKNAME;
                    }
                }
                ContentResolver contentResolver = this.getApplicationContext().getContentResolver();
                Settings.System.putString(contentResolver, Constant.BKPACKAGE_STRING, savepackage[0] + "," + savepackage[1] + "," + savepackage[2]);
            }
        }
        sendBootCheck(this.mContext, "poweroff");
    }

    private void powerOffAction() {
        startHome();
        if (this.mMfi.equals("2") || (this.mGtPlatform && (this.mMfi.equals("1") || this.mMfi.equals("2")))) {
            if ("1".equals(this.mCarPlayType)) {
                SystemProperties.set(AppManager.packageNameCARPLAY[0], "disable");
                Intent it = new Intent(AppManager.packageNameCARPLAY[0]);
                it.putExtra("command", "ACTION_EXIT");
                this.getApplicationContext().sendBroadcastAsUser(it, UserHandle.CURRENT_OR_SELF);
                it.setPackage(AppManager.packageNameCARPLAY[0]);
                this.getApplicationContext().sendBroadcastAsUser(it, UserHandle.CURRENT_OR_SELF);
            } else if ("2".equals(this.mCarPlayType)) {
                SystemProperties.set(AppManager.packageNameCARPLAY[1], "disable");
                Intent it2 = new Intent(AppManager.packageNameCARPLAY[1]);
                it2.putExtra("command", "ACTION_EXIT");
                this.getApplicationContext().sendBroadcastAsUser(it2, UserHandle.CURRENT_OR_SELF);
                it2.setPackage(AppManager.packageNameCARPLAY[1]);
                this.getApplicationContext().sendBroadcastAsUser(it2, UserHandle.CURRENT_OR_SELF);
            }
        }
        this.getApplicationContext().sendBroadcastAsUser(new Intent("android.intent.action.SYNC"), UserHandle.CURRENT_OR_SELF);
        this.appMode = -1;
        if (this.customerSub.equals("HZC4")) {
            setParameters("rpt_power=wait");
            reportEvent("power", this.powerState);
        } else {
            new Thread(new Runnable() { // from class: android.microntek.service.MicrontekServer.12
                @Override // java.lang.Runnable
                public void run() {
                    ClearProcess.getInstance(mContext).clearManage(0, null);
                }
            }).start();
            setParameters("rpt_power=false");
        }
        this.handler.sendEmptyMessageDelayed(8, 2000L);
    }

    private void SendTouchUpdate(List<String> list) {
        if (list.size() <= 0) {
            return;
        }
        String path = "";
        StringBuffer sBuf = new StringBuffer();
        sBuf.append("----update----");
        sBuf.append("\r\n");
        for (int i = 0; i < list.size(); i++) {
            String path2 = list.get(i);
            path = path2;
            sBuf.append(path.substring(path.lastIndexOf("/") + 1, path.length()));
            sBuf.append("\r\n");
        }
        sBuf.append("------" + this.touchCount + "----");
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            showToastMsg(sBuf.toString(), 0);
            int i2 = this.touchCount;
            if (i2 <= 0) {
                this.handler.removeMessages(6);
                TouchUpdateAsyncTask updateTextTask = new TouchUpdateAsyncTask(this, list);
                updateTextTask.execute(new Void[0]);
                return;
            }
            this.touchCount = i2 - 1;
            this.handler.removeMessages(6);
            Message msg1 = this.handler.obtainMessage();
            msg1.what = 6;
            msg1.obj = list;
            this.handler.sendMessageDelayed(msg1, 1000L);
        }
    }

    private void checkTouchUpdate(String path) {
        List<String> updateFilePaths = Arrays.stream(Constant.updateCfgFileName)
            .map(fileName -> path + File.separator + fileName).filter(pathName -> {
                File file = new File(pathName);
                return file.isFile() && file.exists();
            }).collect(Collectors.toList());
        if (!updateFilePaths.isEmpty()) {
            touchCount = 7;
            handler.removeMessages(MSG_TOUCH_UPDATE);
            Message msg = handler.obtainMessage();
            msg.what = MSG_TOUCH_UPDATE;
            msg.obj = updateFilePaths;
            handler.sendMessageDelayed(msg, 1_500L);
        }
    }

    private void updateDmcuExtCfg(String path) {
        String result = HctUtil.getTxtFile(path + "/dmcu.ext");
        if (!TextUtils.isEmpty(result)) {
            try {
                showToastMsg(result, 0);
                String[] token = Objects.requireNonNull(result).split("\n");
                for (String str : token) {
                    String line = str.trim();
                    if (!TextUtils.isEmpty(line)) {
                        if (line.startsWith("screen:") && line.length() > 7) {
                            int par = Integer.parseInt(line.substring(7));
                            if (par >= 0 && par <= 63) {
                                setParameters("ctl_tmode=" + (par + 500));
                            } else if (par >= 64 && par <= 127) {
                                setParameters("ctl_tmode=" + (par + 536));
                            }
                        } else if (line.startsWith("backlight:") && line.length() > 10) {
                            int par = Integer.parseInt(line.substring(10));
                            setParameters("ctl_tmode=" + (par + 700));
                        }
                        Log.i(TAG, "updateDmcuExtCfg:" + line);
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, "updateDmcuExtCfgException" + e.getMessage());
            }
        }
    }

    private void updateHctExtCfg(String path) {
        String result = HctUtil.getTxtFile(path + "/hct.ext");
        if (!TextUtils.isEmpty(result)) {
            try {
                showToastMsg(result, 0);
                String from = null;
                String dest = null;
                String[] token = Objects.requireNonNull(result).split("\n");
                for (String str : token) {
                    String line = str.trim();
                    if (!TextUtils.isEmpty(line)) {
                        if (line.startsWith("copy:") && line.contains(",")) {
                            line = line.substring(line.indexOf(":") + 1);
                            from = line.substring(0, line.indexOf(","));
                            dest = line.substring(line.indexOf(",") + 1);
                            if (TextUtils.isEmpty(dest)) {
                                dest = "/";
                            }
                            if (!dest.startsWith("/")) {
                                dest = "/" + dest;
                            }
                        }
                        Log.i(TAG, "updateHctExtCfg:" + line);
                    }
                }
                if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(dest)) {
                    copyFile(path + "/" + from, "sdcard" + dest);
                }
            } catch (Exception e) {
                Log.i(TAG, "updateHctExtCfgException" + e.getMessage());
            }
        }
    }

    private void saveCustomerLogo(String path) {
        if (!"false".equals(SystemProperties.get("ro.product.wipe.data", ""))) {
            return;
        }
        String pathName = path + File.separator + "customer.png";
        if (new File(pathName).exists()) {
            Log.i(TAG, "SaveLogo:" + pathName);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, options);
            int scale = 1;
            int destWidth = dm.widthPixels;
            int destHeight = dm.heightPixels;
            while ((options.outWidth / scale) / 2 > destWidth
                || (options.outHeight / scale) / 2 > destHeight) {
                scale *= 2;
            }
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            options2.inPurgeable = true;
            options2.inInputShareable = true;
            Bitmap bt = BitmapFactory.decodeFile(pathName, options2);
            int w = options2.outWidth;
            int h = options2.outHeight;
            float scaleWidth = (float) destWidth / w;
            float scaleHeight = (float) destHeight / h;
            Matrix matrix = new Matrix();
            if (scaleWidth < scaleHeight) {
                matrix.postScale(scaleWidth, scaleWidth);
            } else {
                matrix.postScale(scaleHeight, scaleHeight);
            }
            Bitmap bitmap = Bitmap.createBitmap(bt, 0, 0, w, h, matrix, false);
            File dirFile = new File(logoDirPath);
            if (dirFile.mkdirs()) {
                Log.i(TAG, "creat dirFile");
            } else {
                Log.i(TAG, "creat dirFile fail!");
            }
            try {
                Runtime.getRuntime().exec("chmod 777 " + customerLogoPath);
                File bitmapFile = new File(customerLogoPath);
                try {
                    bitmapFile.createNewFile();
                    try (FileOutputStream bitmapWriter = new FileOutputStream(bitmapFile)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 99, bitmapWriter);
                        try {
                            bitmapWriter.flush();
                            bitmapWriter.getFD().sync();
                            bitmapWriter.close();
                            Runtime.getRuntime().exec("chmod 777 " + customerLogoPath);
                            Toast.makeText(getApplicationContext(), "Save Logo OK!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e(TAG, "save fail!", e);
                        }
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "FileOutputStream fail!", e);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "createNewFile fail!  >>> ", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "chmod 777 " + customerLogoPath + " fail!");
            }
        }
    }

    private void copyFile(String from, String dest) {
        AlertDialog alertDialog = copyDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        if (!new File(from).exists()) {
            showToastMsg(from + " no exists !!!!", 0);
            return;
        }
        Log.i(TAG, "copy from:" + from + "  dest:" + dest);
        SystemProperties.set("sys.hct.copy.path.from", from);
        SystemProperties.set("sys.hct.copy.path.dest", dest);
        SystemProperties.set("sys.hct.copy.result", "");
        SystemProperties.set("service.hctcopy.start", "true");
        msgIndex = 0;
        this.handler.removeMessages(24);
        this.handler.sendEmptyMessageDelayed(24, 100L);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("Copy");
        builder.setMessage("Copy.....");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setCancelable(false);
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() { // from class: android.microntek.service.MicrontekServer.13
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog create = builder.create();
        copyDialog = create;
        create.getWindow().setType(2003);
        copyDialog.show();
    }

    private boolean checkSystemMcuAutoUpdate(String path) {
        long currentTimeMillis = System.currentTimeMillis();
        this.durationTime = currentTimeMillis;
        if ((currentTimeMillis - this.initialTime) / 1000 < 10) {
            return false;
        }
        String filePath = path +
                File.separator +
                (this.isCarBox ? "box.auto" : "hct.auto");
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            Intent serviceIntent = new Intent();
            File file2 = new File(path + File.separator + "update.zip");
            if (file2.isFile() && file2.exists()) {
                serviceIntent.setComponent(new ComponentName("android.rockchip.update.service", "android.rockchip.update.service.RKUpdateService"));
                serviceIntent.putExtra("command", 1);
                serviceIntent.putExtra("delay", 0);
                serviceIntent.putExtra("hctAutoUpdate", 1);
                startService(serviceIntent);
                return true;
            }
            File gmcuFile = new File(path + File.separator + "gmcu.img");
            File hmcuFile = new File(path + File.separator + "hmcu.img");
            File imcuFile = new File(path + File.separator + "imcu.img");
            File dmcuFile = new File(path + File.separator + "dmcu.img");
            if ((dmcuFile.isFile() && dmcuFile.exists()) ||
                    ((gmcuFile.isFile() && gmcuFile.exists()) ||
                            ((hmcuFile.isFile() && hmcuFile.exists()) ||
                                    (imcuFile.isFile() && imcuFile.exists())))) {
                Intent serviceIntent2 = new Intent();
                serviceIntent2.setComponent(new ComponentName(Constant.SETTINGSPACKAGE, "com.android.settings.hct.McuUpdate"));
                serviceIntent2.putExtra("command", 1);
                serviceIntent2.putExtra("delay", 0);
                serviceIntent2.putExtra("hctAutoUpdate", 1);
                startService(serviceIntent2);
                return true;
            }
        }
        return false;
    }

    private void checkAutoInstallApk(String path) {
        long currentTimeMillis = System.currentTimeMillis();
        this.durationTime = currentTimeMillis;
        if ((currentTimeMillis - this.initialTime) / 1000 < 10) {
            return;
        }
        AlertDialog alertDialog = this.mApkDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        File dir = new File(path + "/hct/apk");
        if (dir.exists() && !"com.android.provision".equals(HctUtil.getTopActivityClassName(this.mContext))) {
            String[] list = dir.list(new FilenameFilter() { // from class: android.microntek.service.MicrontekServer.14
                @Override // java.io.FilenameFilter
                public boolean accept(File dir2, String filename) {
                    if (filename.endsWith(".apk")) {
                        return true;
                    }
                    return false;
                }
            });
            this.mApkFileNames = list;
            if (list != null && list.length > 0) {
                int i = 0;
                while (true) {
                    String[] strArr = this.mApkFileNames;
                    if (i < strArr.length) {
                        strArr[i] = path + "/hct/apk/" + this.mApkFileNames[i];
                        i++;
                    } else {
                        this.isInstallClear = new File(path + "/hct/apk/clear.ext").exists();
                        SystemProperties.set("sys.hct.install.clear", "false");
                        this.mCurInstallApk = 0;
                        instatllBatch(this.mApkFileNames[0]);
                        return;
                    }
                }
            }
        }
    }

    private void instatllBatch(String path) {
        File apkFile = new File(path);
        if (!apkFile.isFile()) {
            return;
        }
        PackageManager mPm = getPackageManager();
        PackageInfo info = mPm.getPackageArchiveInfo(path, 1);
        Drawable icon = null;
        String name = null;
        if (info != null) {
            try {
                ApplicationInfo appInfo = info.applicationInfo;
                appInfo.sourceDir = path;
                appInfo.publicSourceDir = path;
                name = appInfo.loadLabel(mPm).toString();
                icon = appInfo.loadIcon(mPm);
            } catch (Exception e) {
                name = null;
                icon = null;
            }
        }
        if (TextUtils.isEmpty(name)) {
            return;
        }
        if (this.isInstallClear && this.mCurInstallApk >= this.mApkFileNames.length - 1) {
            SystemProperties.set("sys.hct.install.clear", "true");
        }
        new InstallUtil(this, path, this.handler);
        AlertDialog alertDialog = this.mApkDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mApkDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(name);
        builder.setMessage("Installing.....");
        if (icon == null) {
            builder.setIcon(R.drawable.ic_launcher);
        } else {
            builder.setIcon(icon);
        }
        builder.setCancelable(false);
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() { // from class: android.microntek.service.MicrontekServer.15
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MicrontekServer microntekServer = MicrontekServer.this;
                microntekServer.mCurInstallApk = microntekServer.mApkFileNames.length;
            }
        });
        AlertDialog create = builder.create();
        this.mApkDialog = create;
        create.getWindow().setType(2003);
        this.mApkDialog.show();
    }

    private void focusRequest() {
        audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, 2);
        audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, 1);
    }

    private void saveCarBoxData(String packageName) {
        if (this.powerState == 2 && this.isBoxStartApp) {
            String[] savepackage = new String[3];
            int i = 0;
            if (HctUtil.isAppRunning(this.mContext, Constant.RECPACKAGE)) {
                int i2 = 0 + 1;
                savepackage[0] = Constant.RECPACKAGE;
                i = i2;
            }
            String mtcpackagename = getRunMtcAppPackageName();
            if (mtcpackagename == null) {
                if (Constant.ZLINKPACKAGE.equals(packageName)) {
                    int i3 = i + 1;
                    savepackage[i] = packageName;
                    String className = HctUtil.getTopActivityClassName(this.mContext);
                    Settings.System.putString(this.getApplicationContext().getContentResolver(), Constant.ZLINKCLASS_STRING, className);
                } else if (!checkPKFilter(packageName) && !packageName.startsWith("com.android.launcher")) {
                    int i4 = i + 1;
                    savepackage[i] = packageName;
                }
            } else {
                if (mtcpackagename.equals(Constant.BTPACKAGE) && HctUtil.isAppRunning(this.mContext, Constant.BTMUSICPACKAGE)) {
                    mtcpackagename = Constant.BTMUSICPACKAGE;
                }
                int i5 = i + 1;
                savepackage[i] = mtcpackagename;
                if (gpsIsFront && gpsOpen) {
                    int i6 = i5 + 1;
                    savepackage[i5] = GPSPKNAME;
                }
            }
            ContentResolver contentResolver = this.getApplicationContext().getContentResolver();
            Settings.System.putString(contentResolver, Constant.BKPACKAGE_STRING, savepackage[0] + "," + savepackage[1] + "," + savepackage[2]);
        }
    }

    private void updatePowerScreen(boolean is) {
        this.isPowerScreen = this.carManager.getBooleanState("power_screen");
        if (this.isPowerScreenLast != this.isPowerScreen || is) {
            if (this.isPowerScreen) {
                Log.i(TAG, "-----Enter Power Screen!");
                if (!is && !this.handler.hasMessages(MSG_PWR_SCREEN)) {
                    savePoweroffData();
                }
                this.handler.removeMessages(MSG_PWR_SCREEN);
                if (!this.backviewState) {
                    if (this.isPowerScreenLast != this.isPowerScreen && !SystemProperties.get("sys.ship.package", "").contains("com.android.launcher")) {
                        startHome();
                    }
                    this.screensaverTimer = this.screensaverTimeout;
                    startMusicClock();
                }
            } else {
                clearMusicClock();
                sendBroadcastAsUser(new Intent(Constant.CLOCKEND), UserHandle.CURRENT_OR_SELF);
                this.handler.removeMessages(MSG_PWR_SCREEN);
                this.handler.sendEmptyMessageDelayed(MSG_PWR_SCREEN, 1000L);
            }
            this.isPowerScreenLast = this.isPowerScreen;
        }
    }

    private void powerReboot() {
        this.handler.removeCallbacks(this.PowerLongPress);
        this.handler.post(this.PowerLongPress);
        this.handler.removeCallbacks(this.PowerRebootRunnable);
        this.handler.postDelayed(this.PowerRebootRunnable, 3000L);
    }

    private void UpdataOrientation(boolean flag) {
        if ("true".equals(SystemProperties.get("ro.product.rotate"))) {
            if (this.mOrientation == 0) {
                Settings.System.putInt(getContentResolver(), "user_rotation", 0);
                if ("launcher2".equals(SystemProperties.get("ro.product.launcher"))) {
                    this.mPackageName = "com.android.launcher";
                    this.mClassName = "com.android.launcher2.Launcher";
                } else {
                    this.mPackageName = "com.android.launcher3";
                    this.mClassName = "com.android.launcher3.Launcher";
                }
            } else if (this.mOrientation == 1) {
                Settings.System.putInt(getContentResolver(), "user_rotation", 3);
                if ("launcher2".equals(SystemProperties.get("ro.product.launcher"))) {
                    this.mPackageName = "com.android.launcher2p";
                    this.mClassName = "com.android.launcher2p2.Launcher";
                } else {
                    this.mPackageName = "com.android.launcher3p";
                    this.mClassName = "com.android.launcher3p.Launcher";
                }
            } else {
                Settings.System.putInt(getContentResolver(), "user_rotation", 1);
                if ("launcher2".equals(SystemProperties.get("ro.product.launcher"))) {
                    this.mPackageName = "com.android.launcher2p";
                    this.mClassName = "com.android.launcher2p2.Launcher";
                } else {
                    this.mPackageName = "com.android.launcher3p";
                    this.mClassName = "com.android.launcher3p.Launcher";
                }
            }
            if ("0".equals(SystemProperties.get("ro.product.rotatemode"))) {
                if (this.launcherFlag || "com.android.launcher".equals(HctUtil.getTopActivityPackageName(this.mContext)) || "com.android.launcher2p".equals(HctUtil.getTopActivityPackageName(this.mContext)) || "com.android.launcher3".equals(HctUtil.getTopActivityPackageName(this.mContext)) || "com.android.launcher3p".equals(HctUtil.getTopActivityPackageName(this.mContext)) || !flag) {
                    startDefaultLanuncher(flag);
                }
                if (flag) {
                    this.handler.removeMessages(13);
                    this.handler.sendEmptyMessageDelayed(13, 0L);
                }
                this.updateLauncher = true;
            }
        }
    }

    private Boolean setDefaultLauncher(String PackageName, String ClassName) {
        IntentFilter mHomeFilter;
        PackageManager pm;
        List<ResolveInfo> resolveInfoList;
        int size;
        int defaultMatch;
        ComponentName[] set;
        try {
            mHomeFilter = new IntentFilter("android.intent.action.MAIN");
            mHomeFilter.addCategory("android.intent.category.HOME");
            mHomeFilter.addCategory("android.intent.category.DEFAULT");
            pm = getPackageManager();
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            resolveInfoList = pm.queryIntentActivities(intent, 0);
            size = resolveInfoList.size();
            ComponentName[] mHomeComponent = new ComponentName[resolveInfoList.size()];
            defaultMatch = 0;
            for (int i = 0; i < resolveInfoList.size(); i++) {
                ActivityInfo info = resolveInfoList.get(i).activityInfo;
                ComponentName activityName = new ComponentName(info.packageName, info.name);
                mHomeComponent[i] = activityName;
            }
            set = new ComponentName[size];

            ComponentName defaultLauncher = new ComponentName(PackageName, ClassName);
            for (int i2 = 0; i2 < size; i2++) {
                ResolveInfo resolveInfo = resolveInfoList.get(i2);
                set[i2] = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                if (defaultLauncher.getClassName().equals(resolveInfo.activityInfo.name)) {
                    defaultMatch = resolveInfo.match;
                }
            }
            Class pmre = Class.forName("android.content.pm.PackageManager");
            Method m = pmre.getDeclaredMethod("replacePreferredActivity", IntentFilter.class, Integer.TYPE, ComponentName[].class, ComponentName.class);
            m.invoke(pm, mHomeFilter, Integer.valueOf(defaultMatch), set, defaultLauncher);
            return true;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private void startDefaultLanuncher(boolean flag) {
        if (!this.mPackageName.equals(HctUtil.getTopActivityPackageName(this.mContext))) {
            Intent mIntent = new Intent("android.intent.action.MAIN");
            mIntent.setComponent(new ComponentName(this.mPackageName, this.mClassName));
            mIntent.addFlags(268468224);
            try {
                startActivityAsUser(mIntent, UserHandle.CURRENT_OR_SELF);
                if (flag && !this.firstflag) {
                    String clearpkg = "";
                    if (this.mPackageName == "com.android.launcher3") {
                        clearpkg = "com.android.launcher3p";
                    } else if (this.mPackageName == "com.android.launcher3p") {
                        clearpkg = "com.android.launcher3";
                    } else if (this.mPackageName == "com.android.launcher2p") {
                        clearpkg = "com.android.launcher";
                    } else if (this.mPackageName == "com.android.launcher") {
                        clearpkg = "com.android.launcher2p";
                    }
                    ClearProcess.getInstance(this.mContext).closePackage(clearpkg);
                    this.firstflag = true;
                }
            } catch (Exception e) {
                startDefaultLanuncher(true);
            }
        }
    }

    private void startExtShow() {
        ActivityOptions options = ActivityOptions.makeBasic();
        MediaRouter mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        if (route != null) {
            Display presentationDisplay = route.getPresentationDisplay();
            options.setLaunchDisplayId(presentationDisplay.getDisplayId());
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName("com.microntek.externshow", "com.microntek.externshow.MainActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent, options.toBundle());
        }
    }

    private void sendKeyCode(int code) {
        Intent intent = new Intent(Constant.MSG_MTC_IRKEY_DOWN);
        intent.putExtra(Constant.KEY_CODE, code);
        getApplicationContext().sendBroadcast(intent);
    }

}
