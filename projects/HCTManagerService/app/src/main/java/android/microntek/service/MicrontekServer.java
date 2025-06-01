package android.microntek.service;

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
import java.util.List;
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
    private boolean launcherflag = false;
    private boolean firstflag = false;
    private boolean isYoutube = false;
    private int ScreenSaverTimeOut = -1;
    private boolean ScreenSaverEnableLocal = false;
    private boolean ScreenSaverEnable = false;
    private int ScreenSaverTimer = 0;
    private boolean ScreenSaverOn = false;
    private int mTouchCount = 5;
    private int mModeDoulbe = 0;
    private int mHomeDoulbe = 0;
    private AlertDialog mApkDialog = null;
    private String[] mApkFileNames = null;
    private int mCurInstallApk = 0;
    private boolean isInstallClear = false;
    private boolean isBoxStartApp = false;
    private Handler mHandler = new Handler() { // from class: android.microntek.service.MicrontekServer.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i != 65535) {
                switch (i) {
                    case 0:
                        MicrontekServer.this.TickTask();
                        return;
                    case 1:
                        if (MicrontekServiceBase.gps_open && MicrontekServiceBase.gps_isfront) {
                            MicrontekServer.this.initGps();
                            MicrontekServer.this.setParameters("av_gps_ontop=true");
                            return;
                        }
                        return;
                    case 2:
                        if (!MicrontekServiceBase.gps_open || !MicrontekServiceBase.gps_isfront) {
                            MicrontekServer.this.setParameters("av_gps_ontop=false");
                            return;
                        }
                        return;
                    case 3:
                        MicrontekServiceBase.mDeviceLock = false;
                        if (MicrontekServiceBase.needrunnavi) {
                            MicrontekServiceBase.needrunnavi = false;
                            MicrontekServer.this.RunApp(MicrontekServiceBase.GPSPKNAME);
                            return;
                        }
                        return;
                    case 4:
                        MicrontekServer.this.startMusicClock();
                        return;
                    case 5:
                        MicrontekServer.this.setParameters("av_voiceprompt_on=false");
                        return;
                    case 6:
                        MicrontekServer.this.SendTouchUpdate((List) msg.obj);
                        return;
                    case 7:
                        MicrontekServer.this.setParameters("rpt_power=false");
                        return;
                    case 8:
                        if (MicrontekServer.this.mProgressDialog != null && MicrontekServer.this.mProgressDialog.isShowing()) {
                            MicrontekServer.this.mProgressDialog.dismiss();
                            return;
                        }
                        return;
                    case 9:
                        SystemProperties.set("service.vending.enable", "1");
                        return;
                    case 10:
                        MicrontekServer.this.MTCAdjVolume(0);
                        return;
                    case 11:
                        MicrontekServer.this.MTCAdjVolume(1);
                        return;
                    case 12:
                        MicrontekServer.this.updataWifiAPState();
                        return;
                    case 13:
                        MicrontekServer microntekServer = MicrontekServer.this;
                        microntekServer.setDefaultLauncher(microntekServer.mPackageName, MicrontekServer.this.mClassName);
                        return;
                    case 14:
                        MicrontekServer.this.mHandler.removeMessages(15);
                        MicrontekServer.access$408(MicrontekServer.this);
                        if (MicrontekServer.this.mModeDoulbe >= 2) {
                            MicrontekServer.this.mModeDoulbe = 0;
                            HCTApi.switchDualScreen();
                            return;
                        }
                        MicrontekServer.this.mHandler.sendEmptyMessageDelayed(15, 600L);
                        return;
                    case 15:
                        MicrontekServer.this.mModeDoulbe = 0;
                        MicrontekServer.this.ModeSwitch();
                        return;
                    case 16:
                        MicrontekServer.this.mHandler.removeMessages(17);
                        MicrontekServer.access$508(MicrontekServer.this);
                        if (MicrontekServer.this.mHomeDoulbe >= 2) {
                            MicrontekServer.this.mHomeDoulbe = 0;
                            HCTApi.switchDualScreen();
                            return;
                        }
                        MicrontekServer.this.mHandler.sendEmptyMessageDelayed(17, 600L);
                        return;
                    case 17:
                        MicrontekServer.this.mHomeDoulbe = 0;
                        MicrontekServer.this.SystemKey(3, 0);
                        return;
                    case 18:
                        MicrontekServer.this.showYHLogoView(false);
                        return;
                    default:
                        switch (i) {
                            case 21:
                                if ("YH".equals(MicrontekServer.this.mCustomer) && !MicrontekServer.this.mBackviewState && !"com.microntek.dvr".equals(HctUtil.getTopActivityPackageName(MicrontekServer.this.mContext))) {
                                    MicrontekServer.this.startPkg("com.microntek.dvr", "com.microntek.dvr.MainActivity");
                                    return;
                                }
                                return;
                            case 22:
                                MicrontekServer.this.mAudioManager.setParameters("av_refresh=true");
                                return;
                            case MicrontekServer.MSG_PWR_SCREEN /* 23 */:
                                MicrontekServer.this.NeedStartApp();
                                return;
                            case 24:
                                MicrontekServer.this.mHandler.removeMessages(24);
                                String result = SystemProperties.get("sys.hct.copy.result", "");
                                if (TextUtils.isEmpty(result)) {
                                    MicrontekServer microntekServer2 = MicrontekServer.this;
                                    microntekServer2.msg_index = (microntekServer2.msg_index + 1) % 6;
                                    MicrontekServer.this.mCopyDialog.setMessage(MicrontekServer.DIALOG_MESSAGE[MicrontekServer.this.msg_index]);
                                    MicrontekServer.this.mHandler.sendEmptyMessageDelayed(24, 800L);
                                    return;
                                } else if ("0".equals(result)) {
                                    MicrontekServer.this.mCopyDialog.setMessage("Copy error !!!!");
                                    MicrontekServer.this.mHandler.sendEmptyMessageDelayed(MicrontekServer.MSG_COPY_OK, 3000L);
                                    return;
                                } else if ("1".equals(result)) {
                                    MicrontekServer.this.mCopyDialog.setMessage("Copy success");
                                    MicrontekServer.this.mHandler.sendEmptyMessageDelayed(MicrontekServer.MSG_COPY_OK, 3000L);
                                    return;
                                } else {
                                    return;
                                }
                            case MicrontekServer.MSG_COPY_OK /* 25 */:
                                if (MicrontekServer.this.mCopyDialog != null) {
                                    MicrontekServer.this.mCopyDialog.dismiss();
                                    return;
                                }
                                return;
                            default:
                                switch (i) {
                                    case 65296:
                                        MicrontekServer.this.durationTime = System.currentTimeMillis();
                                        if (((MicrontekServer.this.durationTime - MicrontekServer.this.initialTime) / 1000 >= 10 || MicrontekServer.this.isRunUsbIpod) && !MicrontekServer.this.mUsbIpod && MicrontekServer.this.mPowerState == 2 && !MicrontekServiceBase.btLock && !MicrontekServer.this.mBackviewState) {
                                            MicrontekServer.this.startUsbIpod(0);
                                        }
                                        MicrontekServer.this.mUsbIpod = true;
                                        MicrontekServer.this.isRunUsbIpod = false;
                                        return;
                                    case 65297:
                                        MicrontekServer.this.mUsbIpod = false;
                                        return;
                                    default:
                                        return;
                                }
                        }
                }
            }
            MicrontekServer.access$1008(MicrontekServer.this);
            if (MicrontekServer.this.mCurInstallApk >= MicrontekServer.this.mApkFileNames.length || msg.arg1 == -1) {
                if (MicrontekServer.this.mApkDialog != null && MicrontekServer.this.mApkDialog.isShowing()) {
                    MicrontekServer.this.mApkDialog.dismiss();
                    return;
                }
                return;
            }
            MicrontekServer microntekServer3 = MicrontekServer.this;
            microntekServer3.instatllBatch(microntekServer3.mApkFileNames[MicrontekServer.this.mCurInstallApk]);
        }
    };
    private MfiListener mListener = new MfiListener() { // from class: android.microntek.service.MicrontekServer.2
        public void onConnected() {
            Message msg = MicrontekServer.this.mHandler.obtainMessage();
            msg.what = 65296;
            MicrontekServer.this.mHandler.sendMessage(msg);
        }

        public void onDisconnected() {
            Message msg = MicrontekServer.this.mHandler.obtainMessage();
            msg.what = 65297;
            MicrontekServer.this.mHandler.sendMessage(msg);
        }
    };
    private BroadcastReceiver screenClockBroadcast = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.4
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("changescreenclock".equals(action)) {
                int vaule = intent.getIntExtra("myscreenclock", 0);
                if (1 == vaule) {
                    MicrontekServer.this.ScreenSaverEnableLocal = true;
                    MicrontekServer.this.ScreenSaverEnable = true;
                    MicrontekServer microntekServer = MicrontekServer.this;
                    microntekServer.ScreenSaverTimeOut = Settings.System.getInt(microntekServer.getContentResolver(), "musicscreen_timeout", 30);
                } else if (vaule == 0) {
                    MicrontekServer.this.ScreenSaverEnableLocal = false;
                    MicrontekServer.this.ScreenSaverEnable = false;
                    MicrontekServer.this.ScreenSaverTimeOut = -1;
                }
                MicrontekServer.this.ScreenSaverTimer = 0;
            }
        }
    };
    private BroadcastReceiver mInstallApkReceiver = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String path;
            String action = intent.getAction();
            if (action.equals(Constant.MSG_INSTALL_XRROSS) && (path = intent.getExtras().getString(MicrontekServiceBase.VALUE)) != null && new File(path).exists()) {
                new InstallUtil(MicrontekServer.this.mContext, path, new Handler() { // from class: android.microntek.service.MicrontekServer.5.1
                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Intent intent2 = new Intent(InstallUtil.MSG_INSTALL_XRROSS_OK);
                        intent2.putExtra("package", (String) msg.obj);
                        MicrontekServer.this.sendBroadcastAsUser(intent2, UserHandle.ALL);
                    }
                });
            }
        }
    };
    private BroadcastReceiver mInstallReceiver = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("1".equals(SystemProperties.get("ro.product.market.mode", "0")) && action.equals("android.intent.action.PACKAGE_ADDED")) {
                String packageName = intent.getData().getSchemeSpecificPart();
                try {
                    MicrontekServer.this.mPackageInfo = MicrontekServer.this.getPackageManager().getPackageInfo(packageName, 4198976);
                    String[] permissons = MicrontekServer.this.mPackageInfo.requestedPermissions;
                    boolean needSetCanInstallApps = false;
                    for (String permisson : permissons) {
                        if ("android.permission.INSTALL_PACKAGES".equals(permisson) || "android.permission.REQUEST_INSTALL_PACKAGES".equals(permisson)) {
                            needSetCanInstallApps = true;
                        }
                    }
                    if (needSetCanInstallApps) {
                        MicrontekServer.this.setCanInstallApps(true, packageName);
                    }
                } catch (Exception e) {
                }
            }
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                try {
                    String packageName2 = intent.getData().getSchemeSpecificPart();
                    if ("android.microntek.canbus".equals(packageName2)) {
                        Intent canserviceintent = new Intent();
                        canserviceintent.setComponent(new ComponentName("android.microntek.canbus", "android.microntek.canbus.CanBusServer"));
                        MicrontekServer.this.startServiceAsUser(canserviceintent, UserHandle.OWNER);
                    }
                    if (MicrontekServer.this.mCustomerSub.equals("GS9")) {
                        if (TextUtils.isEmpty(packageName2)) {
                            return;
                        }
                        String packageName3 = packageName2.substring(packageName2.indexOf(":") + 1);
                        Log.i("wuwq", "mInstallReceiver: " + packageName3);
                        if (!TextUtils.isEmpty(packageName3) && "com.xtrons.app".equals(packageName3)) {
                            HctUtil.execCmd("dpm set-device-owner com.xtrons.app/.MainActivity");
                        } else if (!TextUtils.isEmpty(packageName3) && "com.togoinsights.deviceadmin".equals(packageName3)) {
                            HctUtil.execCmd("dpm set-device-owner com.togoinsights.deviceadmin/.AdminActivity");
                        }
                    } else if ((MicrontekServer.this.mCustomerSub.equals("TELENAV_SCOUT") || MicrontekServer.this.mCustomerSub.equals("TELENAV_S4C")) && !TextUtils.isEmpty(packageName2)) {
                        String packageName4 = packageName2.substring(packageName2.indexOf(":") + 1);
                        Log.i("wuwq", "mInstallReceiver: " + packageName4);
                        if (TextUtils.isEmpty(packageName4)) {
                            return;
                        }
                        if ("com.telenav.launcher".equals(packageName4) || "com.telenav.vivid.scout4cars.launcher".equals(packageName4)) {
                            MicrontekServer.this.setInstallPackagesPermissions(packageName4);
                            HctUtil.execCmd("dpm set-device-owner " + packageName4 + "/" + packageName4 + ".home.LauncherActivity");
                        }
                    }
                } catch (Exception e2) {
                }
            }
        }
    };
    private BroadcastReceiver mLocaleReceiver = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.7
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.LOCALE_CHANGED")) {
                MicrontekServer.this.mProgressDialog = null;
            }
        }
    };
    private BroadcastReceiver mHdmiReceiver = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.8
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constant.ACTION_PLUGGED)) {
                boolean state = intent.getBooleanExtra("state", false);
                String boot_completed = SystemProperties.get("sys.boot_completed", "0");
                if (!boot_completed.equals("0") && state) {
                    MicrontekServer.this.mHandler.postDelayed(new Runnable() { // from class: android.microntek.service.MicrontekServer.8.1
                        @Override // java.lang.Runnable
                        public void run() {
                            MicrontekServer.this.startExtShow();
                        }
                    }, 10000L);
                } else if (!state) {
                    MicrontekServer.this.sendKeyCode(1026);
                }
            } else if (action.equals("com.microntek.extshow.start")) {
                MicrontekServer.this.startExtShow();
            }
        }
    };
    private boolean mWifiFirstRevFlag = true;
    private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.9
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            WifiManager wifiManager = (WifiManager) MicrontekServer.this.mContext.getSystemService("wifi");
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                if (2 == MicrontekServer.this.mPowerState || MicrontekServer.this.mWifiFirstRevFlag) {
                    int nWifistate = wifiManager.getWifiState();
                    if ((3 == nWifistate || 1 == nWifistate) && MicrontekServer.this.getWifiDriverState() && (!MicrontekServer.this.mWifiFirstRevFlag || 1 != Settings.System.getInt(MicrontekServer.this.getContentResolver(), "status_acc_off_ap_opened", 0))) {
                        boolean apstate = MicrontekServer.this.mWifiManager.getWifiApState() == 13;
                        boolean wifistate = MicrontekServer.this.mWifiManager.isWifiEnabled();
                        Settings.System.putInt(MicrontekServer.this.getContentResolver(), "status_acc_off_wifi_opened", wifistate ? 1 : 0);
                        Settings.System.putInt(MicrontekServer.this.getContentResolver(), "status_acc_off_ap_opened", apstate ? 1 : 0);
                    }
                } else if (MicrontekServer.this.mPowerState == 0) {
                    if (3 == wifiManager.getWifiState()) {
                        MicrontekServer.this.setWifiOn(false);
                    }
                }
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                if (2 == MicrontekServer.this.mPowerState || MicrontekServer.this.mWifiFirstRevFlag) {
                    int wifiApState = MicrontekServer.this.getWifiApState();
                    if (13 == wifiApState || 11 == wifiApState) {
                        MicrontekServer.this.mWaitingForTerminalState = false;
                        if (MicrontekServer.this.getWifiDriverState()) {
                            boolean apstate2 = MicrontekServer.this.getWifiApState() == 13;
                            boolean wifistate2 = MicrontekServer.this.mWifiManager.isWifiEnabled();
                            Settings.System.putInt(MicrontekServer.this.getContentResolver(), "status_acc_off_wifi_opened", wifistate2 ? 1 : 0);
                            Settings.System.putInt(MicrontekServer.this.getContentResolver(), "status_acc_off_ap_opened", apstate2 ? 1 : 0);
                        }
                    } else if (14 == wifiApState) {
                        Log.i("wuwq", "mWifiReceiver *** WIFI_AP_STATE_FAILED state is Failed. ");
                        MicrontekServer.this.wifiapcheck_cnt = 20;
                        MicrontekServer.this.mIsFirstUpdataWifiAPState = true;
                        MicrontekServer.this.mHandler.removeMessages(12);
                        Message msg = MicrontekServer.this.mHandler.obtainMessage();
                        msg.what = 12;
                        MicrontekServer.this.mHandler.sendMessageDelayed(msg, 3000L);
                    }
                } else if (MicrontekServer.this.mPowerState == 0) {
                    if (13 == MicrontekServer.this.getWifiApState()) {
                        MicrontekServer.this.setWifiApEnabled(false);
                    }
                }
            } else {
                if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action) && 2 == MicrontekServer.this.mPowerState) {
                    SupplicantState supplicantState = (SupplicantState) intent.getParcelableExtra("newState");
                    if (SupplicantState.INTERFACE_DISABLED.equals(supplicantState)) {
                        Log.i("wuwq", "mWifiReceiver supplicant state is disabled. ");
                        MicrontekServer.this.wifiapcheck_cnt = 20;
                        MicrontekServer.this.mIsFirstUpdataWifiAPState = true;
                        MicrontekServer.this.mHandler.removeMessages(12);
                        Message msg2 = MicrontekServer.this.mHandler.obtainMessage();
                        msg2.what = 12;
                        MicrontekServer.this.mHandler.sendMessageDelayed(msg2, 3000L);
                    }
                }
            }
        }
    };
    private int wifiapcheck_cnt = 20;
    private boolean mIsFirstUpdataWifiAPState = true;
    private boolean mUpdataingWifiAPState = false;
    private Runnable PowerOffRunnable = new Runnable() { // from class: android.microntek.service.MicrontekServer.11
        @Override // java.lang.Runnable
        public void run() {
            MicrontekServer.this.PowerOffAction();
        }
    };
    private int msg_index = 0;
    private AlertDialog mCopyDialog = null;
    private PhoneStateListener phoneListener = new PhoneStateListener() { // from class: android.microntek.service.MicrontekServer.16
        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (state == 0) {
                MicrontekServer.this.setParameters("av_phone_sim=hangup");
                MicrontekServiceBase.simPhoneLock = false;
                MicrontekServer.this.MTCAdjVolume(2);
            } else if (state != 1) {
                if (state == 2) {
                    MicrontekServer.this.setParameters("av_phone_sim=answer");
                    MicrontekServiceBase.simPhoneLock = true;
                    MicrontekServer.this.MTCAdjVolume(2);
                }
            } else {
                MicrontekServer.this.setParameters("av_phone_sim=in");
                MicrontekServiceBase.simPhoneLock = true;
                MicrontekServer.this.MTCAdjVolume(2);
            }
        }
    };
    private BroadcastReceiver phoneReceiver = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.17
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
                MicrontekServer.this.setParameters("av_phone_sim=out");
                MicrontekServiceBase.simPhoneLock = true;
                MicrontekServer.this.MTCAdjVolume(2);
            }
        }
    };
    private BroadcastReceiver MediaDetectReceiver = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.18
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int autoPlayEN = Settings.System.getInt(MicrontekServer.this.getContentResolver(), Constant.MEDIAAUTOEN_STRING, 0);
            String action = intent.getAction();
            if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                if (MicrontekServer.this.mPowerState != 2 || MicrontekServer.this.mBackviewState) {
                    return;
                }
                String path = MicrontekServer.this.convertStorageToMnt(intent.getData().getPath());
                MicrontekServer microntekServer = MicrontekServer.this;
                String devString = microntekServer.getDeviceType(context, microntekServer.convertMntToStorage(path));
                if (devString.equals("GPS") && MicrontekServiceBase.needrunnavi) {
                    MicrontekServiceBase.needrunnavi = false;
                    MicrontekServer.this.RunApp(MicrontekServiceBase.GPSPKNAME);
                }
                if (!MicrontekServer.this.checkSystemMcuAutoUpdate(path)) {
                    MicrontekServer.this.checkTouchUpdate(path);
                    MicrontekServer.this.updateDmcuExtCfg(path);
                    MicrontekServer.this.checkAutoInstallApk(path);
                    MicrontekServer.this.updateHctExtCfg(path);
                    MicrontekServer.this.saveCustomerLogo(path);
                    if (!MicrontekServiceBase.btLock && !MicrontekServiceBase.mDeviceLock && autoPlayEN != 0) {
                        MicrontekServer.this.clearMusicClock();
                        String customer = SystemProperties.get("ro.product.customer", "HCT");
                        if (!TextUtils.isEmpty(devString) && !devString.equals("FLASH")) {
                            if ((devString.equals("GPS") && !"YH".equals(customer)) || Constant.MUSICPACKAGE.equals(HctUtil.getTopActivityPackageName(context))) {
                                return;
                            }
                            String[] versionParts = Build.VERSION.RELEASE.split("\\.");
                            int majorVersion = Integer.valueOf(versionParts[0]).intValue();
                            if (majorVersion > 9) {
                                MicrontekServer.this.durationTime = System.currentTimeMillis();
                                if ((MicrontekServer.this.durationTime - MicrontekServer.this.initialTime) / 1000 > 15) {
                                    MicrontekServer.this.startMusic(path, 0);
                                    return;
                                }
                                return;
                            }
                            MicrontekServer.this.startMusic(path, 0);
                        }
                    }
                }
            } else if (action.equals("android.intent.action.MEDIA_UNMOUNTED") || action.equals("android.intent.action.MEDIA_EJECT")) {
                MicrontekServer.this.mHandler.removeMessages(6);
            }
        }
    };
    private BroadcastReceiver MTCAPPProc = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.19
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String pkname;
            String VOL;
            String action = intent.getAction();
            if (action.equals(Constant.MSG_BEEP)) {
                MicrontekServer.this.setParameters("ctl_beep=1");
                MicrontekServer.this.clearMusicClock();
            } else if (action.equals(Constant.MSG_BEEP_CLEAR_SCREEN)) {
                MicrontekServer.this.clearMusicClock();
            } else if (action.equals(Constant.MSG_MTC_PROMPT)) {
                if (intent.hasExtra("package")) {
                    String pgname = intent.getStringExtra("package");
                    MicrontekServer microntekServer = MicrontekServer.this;
                    microntekServer.setParameters("av_voiceprompt_package=" + pgname);
                }
                if (intent.hasExtra("state")) {
                    String state = intent.getStringExtra("state");
                    if (state.equals("on")) {
                        MicrontekServer.this.setParameters("av_voiceprompt_on=true");
                        MicrontekServer.this.mHandler.removeMessages(5);
                        MicrontekServer.this.mHandler.sendEmptyMessageDelayed(5, 3000L);
                        return;
                    }
                    MicrontekServer.this.setParameters("av_voiceprompt_on=false");
                }
            } else if (action.equals(Constant.MSG_MTC_VOLUME_SET)) {
                int vol = MicrontekServiceBase.mCurVolume;
                if (intent.hasExtra("type")) {
                    String type = intent.getStringExtra("type");
                    if (type.equals("add")) {
                        if ("HZC23".equals(MicrontekServer.this.mCustomerSub) || "HZC39".equals(MicrontekServer.this.mCustomerSub) || "HZC40".equals(MicrontekServer.this.mCustomerSub)) {
                            int vol2 = MicrontekServiceBase.mCurVolume;
                            vol = vol2 + 1;
                        } else {
                            vol = MicrontekServiceBase.mCurVolume + (MicrontekServiceBase.KEY_VOLMAX / 10);
                        }
                        if (vol > MicrontekServiceBase.KEY_VOLMAX) {
                            vol = MicrontekServiceBase.KEY_VOLMAX;
                        }
                    } else if (type.equals("sub")) {
                        if ("HZC23".equals(MicrontekServer.this.mCustomerSub) || "HZC39".equals(MicrontekServer.this.mCustomerSub) || "HZC40".equals(MicrontekServer.this.mCustomerSub)) {
                            int vol3 = MicrontekServiceBase.mCurVolume;
                            vol = vol3 - 1;
                        } else {
                            vol = MicrontekServiceBase.mCurVolume - (MicrontekServiceBase.KEY_VOLMAX / 10);
                        }
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
                MicrontekServer.this.OnChangeVolume(vol);
            } else if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                if (intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1) == 3 && MicrontekServer.this.mMcuVersion.contains("HXD")) {
                    int max = MicrontekServer.this.mAudioManager.getStreamMaxVolume(3);
                    int current = MicrontekServer.this.mAudioManager.getStreamVolume(3);
                    if (max == MicrontekServiceBase.KEY_VOLMAX && current <= MicrontekServiceBase.KEY_VOLMAX && current != MicrontekServer.this.mVolumeTemp) {
                        Log.i("MicrontekServer", "--mtc AudioManagerVOL current:" + current + "mVolumeTemp:" + MicrontekServer.this.mVolumeTemp);
                        MicrontekServer.this.OnChangeVolume(current);
                    }
                }
            } else if (action.equals(Constant.MSG_MTC_BLIGHT_SET)) {
                int level = intent.getIntExtra("level", 100);
                MicrontekServer.this._setHctBacklight(level);
            } else if (action.equals(Constant.MSG_MTC_APP)) {
                if (intent.hasExtra(Constant.HCT_APP_KEY)) {
                    String app = intent.getStringExtra(Constant.HCT_APP_KEY);
                    if (app.equals("music")) {
                        MicrontekServer.this.startMusic(null, 0);
                    } else if (app.equals("movie")) {
                        MicrontekServer.this.startMovie(0);
                    } else if (app.equals("radio")) {
                        int freq = -1;
                        if (intent.hasExtra(Constant.HCT_APP_KEY2)) {
                            freq = intent.getIntExtra(Constant.HCT_APP_KEY2, -1);
                        }
                        if (freq != -1) {
                            MicrontekServer microntekServer2 = MicrontekServer.this;
                            microntekServer2.startRadio(0, "" + freq);
                            return;
                        }
                        MicrontekServer.this.startRadio(0);
                    } else if (app.equals("dvd")) {
                        MicrontekServer.this.startDVD(0);
                    } else if (!app.equals("navi") || MicrontekServiceBase.gps_isfront) {
                    } else {
                        if (TextUtils.isEmpty(MicrontekServiceBase.GPSPKNAME) || !HctUtil.isPackageApplicationEnabled(MicrontekServer.this.mContext, MicrontekServiceBase.GPSPKNAME)) {
                            MicrontekServer.this.RunApp(Constant.NAVIPACKAGE);
                        } else {
                            MicrontekServer.this.RunApp(MicrontekServiceBase.GPSPKNAME);
                        }
                    }
                }
            } else if (action.equals(Constant.MSG_SHOW_VOLUME)) {
                if (MicrontekServiceBase.btLock || MicrontekServiceBase.simPhoneLock) {
                    VOL = Constant.PHONEVOLUME;
                } else {
                    VOL = Constant.MTCVOLUME;
                }
                MicrontekServiceBase.mCurVolume = Settings.System.getInt(MicrontekServer.this.getContentResolver(), VOL, MicrontekServiceBase.KEY_VOLMAX / 2);
                MicrontekServer.this.ShowVolumeDalog(MicrontekServiceBase.mCurVolume);
            } else if (!action.equals(Constant.MSG_ACTIVE)) {
                if (action.equals(Constant.MSG_MTC_CLEAR)) {
                    if (!ClearProcess.getInstance(context).getBusy()) {
                        int mode = intent.getIntExtra("mode", 1);
                        if (mode == 0) {
                            ClearProcess.getInstance(context).clearManage(0, null);
                            return;
                        }
                        context.sendBroadcastAsUser(new Intent(Constant.MSG_MTC_SPEEDSTART), UserHandle.CURRENT_OR_SELF);
                        ClearProcess.getInstance(context).clearManage(1, MicrontekServiceBase.GPSPKNAME);
                        context.sendBroadcastAsUser(new Intent(Constant.MSG_MTC_SPEEDEND), UserHandle.CURRENT_OR_SELF);
                    }
                } else if (action.equals(Constant.MSG_MTC_CLOSEPACKAGE)) {
                    if (!intent.hasExtra("package") || (pkname = intent.getStringExtra("package")) == null || "".equals(pkname)) {
                        return;
                    }
                    if (pkname.equals(Constant.RADIOPACKAGE) || pkname.equals(Constant.DVDPACKAGE) || pkname.equals(Constant.MUSICPACKAGE) || pkname.equals(Constant.IPODPACKAGE) || pkname.equals(Constant.USBIPODPACKAGE) || pkname.equals(Constant.TVPACKAGE) || pkname.equals(Constant.PHOTOPACKAGE) || pkname.equals(Constant.MOVIEPACKAGE) || pkname.equals(Constant.BTPACKAGE) || pkname.equals(Constant.BTMUSICPACKAGE) || pkname.equals(Constant.RECPACKAGE) || pkname.equals(Constant.WEATHERPACKAGE)) {
                        if (!HctUtil.CheckIsRun(context, pkname)) {
                            return;
                        }
                        if (pkname.equals(Constant.BTPACKAGE) && !HctUtil.getTopActivityClassName(context).equals(Constant.BTMUSICCLASS2)) {
                            MicrontekServer.this.startHome();
                        }
                        MicrontekServer microntekServer3 = MicrontekServer.this;
                        microntekServer3.sendBootCheck(microntekServer3.mContext, "android.microntek.service");
                    } else if (pkname.equals(AppManager.packageNameCARPLAY[0])) {
                        MicrontekServer.this.mContext.sendBroadcastAsUser(new Intent("carplay.apk.close"), UserHandle.ALL);
                    } else {
                        ClearProcess.getInstance(context).closePackage(pkname);
                    }
                } else if (action.equals(Constant.MSG_START_RADIO)) {
                    if (!HctUtil.CheckIsRun(context, Constant.RADIOPACKAGE)) {
                        MicrontekServer.this.startRadio(2);
                    }
                } else if (action.equals(Constant.MSG_START_MUSIC)) {
                    int mediaapp = MicrontekServer.this.getmediaAppflag();
                    if (mediaapp != 1) {
                        MicrontekServer.this.startMusic(null, 2);
                    }
                } else if (action.equals(Constant.MSG_START_IPOD)) {
                    if (MicrontekServer.this.mUsbIpodSupport) {
                        if (MicrontekServer.this.mUsbIpod && !HctUtil.CheckIsRun(context, Constant.USBIPODPACKAGE)) {
                            MicrontekServer.this.startUsbIpod(2);
                        }
                    } else if (!HctUtil.CheckIsRun(context, Constant.IPODPACKAGE)) {
                        MicrontekServer.this.startIpod(2);
                    }
                } else {
                    action.equals(Constant.MSG_START_BTMUSIC);
                }
            }
        }
    };
    private BroadcastReceiver MTCploy = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekServer.20
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String cmd;
            String action = intent.getAction();
            if (action.equals(Constant.BROADCAST_BT_REPORT)) {
                if (intent.hasExtra(Constant.MSG_BT_SRV_CONNECT_STATE)) {
                    int btstate = intent.getIntExtra(Constant.MSG_BT_SRV_CONNECT_STATE, 0);
                    if (btstate == 2 || btstate == 3 || btstate == 5) {
                        if (btstate == 2) {
                            MicrontekServer.this.setParameters("av_phone=out");
                        } else if (btstate == 3) {
                            MicrontekServer.this.setParameters("av_phone=in");
                        } else {
                            MicrontekServer.this.setParameters("av_phone=answer");
                        }
                        if (!MicrontekServiceBase.btLock) {
                            MicrontekServiceBase.btLock = true;
                            MicrontekServer.this.sendBootCheck(context, "phonecallin");
                            MicrontekServer.this.MTCAdjVolume(2);
                            context.sendBroadcastAsUser(new Intent(Constant.MSG_ACTIVE), UserHandle.CURRENT_OR_SELF);
                        }
                    } else if (MicrontekServiceBase.btLock) {
                        MicrontekServiceBase.btLock = false;
                        MicrontekServer.this.setParameters("av_phone=hangup");
                        MicrontekServer.this.sendBootCheck(context, "phonecallout");
                        MicrontekServer.this.MTCAdjVolume(2);
                        if (!MicrontekServiceBase.btLock && !MicrontekServer.this.mBackviewState && MicrontekServer.this.isPowerScreen) {
                            MicrontekServer.this.updataPowerScreen(true);
                        }
                    }
                }
            } else if (action.equals(Constant.MSG_ACTION_APP_TITLE)) {
                String packageName = intent.getStringExtra("pkname");
                if ("YH".equals(MicrontekServer.this.mCustomer)) {
                    MicrontekServer.this.mHandler.removeMessages(21);
                    if (Constant.BTPACKAGE.equals(packageName)) {
                        MicrontekServer.this.mHandler.sendEmptyMessageDelayed(21, 30000L);
                    } else {
                        MicrontekServer.this.mHandler.sendEmptyMessageDelayed(21, 30000L);
                    }
                }
                if (packageName.equals(Constant.BACKVIEWPACKAGE) || packageName.equals(Constant.FRONTVIEWPACKAGE)) {
                    MicrontekServer.this.isYoutube = false;
                }
                MicrontekServiceBase.GPSPKNAME = Settings.System.getString(MicrontekServer.this.getContentResolver(), "gpspkname");
                if ("0".equals(SystemProperties.get("ro.product.rotatemode")) && "true".equals(SystemProperties.get("ro.product.rotate"))) {
                    if (TextUtils.isEmpty(packageName) || !packageName.startsWith("com.android.launcher")) {
                        MicrontekServer.this.launcherflag = false;
                    } else {
                        MicrontekServer.this.launcherflag = true;
                        if (MicrontekServer.this.updateLauncher) {
                            MicrontekServer microntekServer = MicrontekServer.this;
                            microntekServer.setDefaultLauncher(microntekServer.mPackageName, MicrontekServer.this.mClassName);
                            MicrontekServer.this.startHome();
                            MicrontekServer.this.updateLauncher = false;
                        }
                    }
                }
                if (!TextUtils.isEmpty(MicrontekServiceBase.GPSPKNAME) && MicrontekServiceBase.GPSPKNAME.equals(packageName)) {
                    MicrontekServiceBase.gps_open = true;
                    MicrontekServiceBase.gps_isfront = true;
                    MicrontekServer.this.mHandler.sendEmptyMessage(1);
                } else if (packageName.equals("com.google.android.youtube")) {
                    if (MicrontekServer.this.mCustomerSub.startsWith("ASUKA")) {
                        if (!MicrontekServer.this.isYoutube) {
                            MicrontekServer.this.mCarManager.setParameters("ctl_key=257");
                        }
                        MicrontekServer.this.isYoutube = true;
                    }
                    Intent it1 = new Intent(Constant.MSG_MTC_BOOTCHECK);
                    it1.putExtra("class", "toutube");
                    context.sendBroadcastAsUser(it1, UserHandle.CURRENT_OR_SELF);
                    MicrontekServer.this.setParameters("av_channel_enter=sys");
                    MicrontekServer microntekServer2 = MicrontekServer.this;
                    if (microntekServer2.isHZC(microntekServer2.mCustomerSub) && MicrontekServer.this.isHandbrake) {
                        String asd = MicrontekServer.this.getResources().getString(R.string.vedio_warning);
                        MicrontekServer.this.MyToast(asd);
                    }
                } else if (packageName.equals("com.microntek.instructionsvideo")) {
                    MicrontekServer microntekServer3 = MicrontekServer.this;
                    if (microntekServer3.isHZC(microntekServer3.mCustomerSub) && MicrontekServer.this.isHandbrake) {
                        String asd2 = MicrontekServer.this.getResources().getString(R.string.vedio_warning);
                        ClearProcess.getInstance(MicrontekServer.this.mContext).closePackage("com.microntek.instructionsvideo");
                        MicrontekServer.this.MyToast(asd2);
                    }
                } else if (packageName.equals("com.netflix.mediaclient")) {
                    MicrontekServer.this.mNetflixState = true;
                } else if (packageName.equals("com.youku.phone")) {
                    MicrontekServer.this.focusRequest();
                } else if (packageName.equals(Constant.CLOCKSCREENPACKAGE)) {
                    if (!TextUtils.isEmpty(MicrontekServer.this.mCustomerSub) && "SYCH".equals(MicrontekServer.this.mCustomer)) {
                        MicrontekServer.this.mNetflixState = true;
                    }
                } else if ((!packageName.equals(Constant.BACKVIEWPACKAGE) && !packageName.equals(Constant.FRONTVIEWPACKAGE)) || !MicrontekServiceBase.gps_isfront) {
                    MicrontekServer.this.mAppMode = -1;
                    MicrontekServiceBase.gps_isfront = false;
                    MicrontekServer.this.mNetflixState = false;
                    MicrontekServer.this.mHandler.removeMessages(2);
                    MicrontekServer.this.mHandler.sendEmptyMessageDelayed(2, 1000L);
                }
                MicrontekServer.this.isScreenlock = packageName.equals(Constant.CLOCKSCREENPACKAGE);
                if (MicrontekServer.this.mBackviewState && !packageName.equals(Constant.BACKVIEWPACKAGE)) {
                    MicrontekServer.this.startBackView();
                }
                if (!MicrontekServiceBase.btLock && !MicrontekServer.this.mBackviewState && MicrontekServer.this.isPowerScreen) {
                    MicrontekServer.this.updataPowerScreen(true);
                }
                MicrontekServer.this.mHandler.removeMessages(22);
                MicrontekServer.this.mHandler.sendEmptyMessageDelayed(22, 800L);
                MicrontekServer.this.mHandler.sendEmptyMessageDelayed(22, 2000L);
                if (MicrontekServer.this.isCarBox) {
                    MicrontekServer.this.saveCarBoxData(packageName);
                }
            } else if (action.equals(Constant.MSG_MTC_POWER_OFFDONE)) {
                if (MicrontekServer.this.mCustomerSub.equals("HZC4")) {
                    MicrontekServer.this.setParameters("rpt_power=false");
                    ClearProcess.getInstance(MicrontekServer.this.mContext).clearManage(0, null);
                }
            } else if (action.equals("com.microntek.request.event")) {
                String type = intent.getStringExtra("type");
                if (!TextUtils.isEmpty(type)) {
                    if (type.contains("handbrake")) {
                        MicrontekServer.this.UpdataHandBrake();
                    }
                    if (type.contains("headlight")) {
                        MicrontekServer.this.UpdataHeadLight();
                    }
                    if (type.contains("backview")) {
                        MicrontekServer.this.UpdataBackView();
                    }
                    if (type.contains("power")) {
                        MicrontekServer microntekServer4 = MicrontekServer.this;
                        microntekServer4.ReportEvent("power", microntekServer4.mPowerState);
                    }
                    if (type.contains("volume")) {
                        MicrontekServer.this.SendVolStatus(MicrontekServiceBase.mCurVolume);
                    }
                    if (type.contains("reardiaplay")) {
                        HCTApi.switchDualScreen();
                    }
                }
            } else if (action.equals(Constant.MSG_MTC_DARKLIGHT)) {
                String state = MicrontekServer.this.getParameters("sta_ill=");
                Intent intent2 = new Intent(Constant.STATECAR_LIGHT);
                intent2.putExtra("state", state);
                MicrontekServer.this.sendBroadcastAsUser(intent2, UserHandle.CURRENT_OR_SELF);
            } else if (action.equals(Constant.MSG_ACTION_HCTREBOOT)) {
                MicrontekServer.this.powerReboot();
            } else if (action.equals(Constant.GPSCHANGE)) {
                String pkname = intent.getStringExtra("pkname");
                if (pkname == null) {
                    return;
                }
                MicrontekServer.this.mCarManager.putState("navi_package", MicrontekServiceBase.GPSPKNAME);
                Settings.System.putString(context.getContentResolver(), "gpspkname", pkname);
            } else if (action.equals(AppManager.packageNameCARPLAY[0]) || action.equals(AppManager.packageNameCARPLAY[1]) || action.equals(AppManager.packageNameHICAR[0])) {
                if (intent.hasExtra("status")) {
                    String status = intent.getStringExtra("status");
                    if (status == null) {
                        return;
                    }
                    Log.i("carplay", "Status:" + status);
                    if (status.equals("MAIN_PAGE_SHOW")) {
                        MicrontekServiceBase.bCarPlayShow = true;
                    } else if (status.equals("MAIN_PAGE_HIDDEN")) {
                        MicrontekServiceBase.bCarPlayShow = false;
                    }
                } else if (!intent.hasExtra("command") || (cmd = intent.getStringExtra("command")) == null) {
                } else {
                    Log.i("carplay", "command:" + cmd);
                    if (cmd != null && cmd.equals("RES_APK_INFO") && intent.getStringExtra("regmode").contains("w")) {
                        AppManager.getInstance(context).setUsbIpodEnabled(false);
                        if ("HZC".equals(MicrontekServer.this.mCustomer) && intent.getStringExtra("regmode").contains("a")) {
                            AppManager.getInstance(context).setEasyConnEnabled(false);
                        }
                    } else if (cmd != null && cmd.equals("RES_APK_INFO") && intent.getStringExtra("regmode").contains("l") && "HZC".equals(MicrontekServer.this.mCustomer) && intent.getStringExtra("regmode").contains("a")) {
                        AppManager.getInstance(context).setEasyConnEnabled(false);
                    }
                }
            } else if (action.equals(Constant.BROADCAST_EASYCONN_REGISTED)) {
                AppManager.getInstance(context).setUsbIpodEnabled(false);
            } else if (action.equals("com.microntek.CarManager.event")) {
                String parameter = intent.getStringExtra("parameter");
                MicrontekServer.this.mCarManager.setParameters(parameter);
            }
        }
    };
    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() { // from class: android.microntek.service.MicrontekServer.21
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
        this.mCarManager.attach(new Handler() { // from class: android.microntek.service.MicrontekServer.3
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                MicrontekServer.this.s_onStatusChanged((String) msg.obj, msg.getData());
            }
        }, "CarEvent,CarPower,KeyDown,CarApp,CarBox");
        this.onCraeteTime = System.currentTimeMillis();
        InitData();
        InitSystemData();
        InitIntentFilter();
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        if (1 == Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0)) {
            setAirplaneModeOn(false);
        }
        if ("XLY".equals(HctUtil.getCustomerSub()) || "HZC29".equals(HctUtil.getCustomerSub())) {
            Settings.System.putInt(getContentResolver(), "XLYsetLanguage", 1);
        }
        if ("XLY".equals(HctUtil.getCustomerSub()) && Settings.System.getInt(getContentResolver(), "XLYPersianCale", 2) == 2) {
            Settings.System.putInt(getContentResolver(), "XLYPersianCale", 1);
        }
        this.mHandler.removeMessages(21);
        this.mHandler.sendEmptyMessageDelayed(21, 30000L);
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
            this.mHandler.removeMessages(21);
            this.mHandler.sendEmptyMessageDelayed(21, 30000L);
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
        if (this.mCarManager != null) {
            String powerstate = this.mCarManager.getStringState("carpower");
            DoCarPower(powerstate);
            this.mBackviewState = this.mCarManager.getBooleanState("backview");
            UpdataBackView();
            this.mHandbrake = this.mCarManager.getBooleanState("handbrake");
            UpdataHandBrake();
            this.mHeadlight = this.mCarManager.getBooleanState("headlight");
            UpdataHeadLight();
            this.mAjx = this.mCarManager.getBooleanState("ajx");
            UpdataAjx();
            this.mOrientation = this.mCarManager.getIntState("orientation");
            UpdataOrientation(false);
            updataPowerScreen(false);
            boolean startApp = this.mCarManager.getBooleanState("carstartapp");
            if (startApp) {
                NeedStartApp();
            }
        }
        this.mMfiManager.registerListener(this.mListener);
    }

    private void NeedStartApp() {
        if (btLock) {
            return;
        }
        this.isBoxStartApp = true;
        if (this.mBackviewState) {
            mNeedStartApp = true;
        } else {
            MtcStartApp();
        }
    }

    private void InitSystemData() {
        this.mHandler.sendEmptyMessageDelayed(3, 15000L);
        String enscreenclock = GetSystemProperties("ro.product.screenclock");
        if (enscreenclock.equals("true") || getParameters("sta_function=18").equals("1") || Settings.System.getInt(getContentResolver(), "screenState", 0) == 1) {
            this.ScreenSaverEnableLocal = true;
            this.ScreenSaverEnable = true;
            this.ScreenSaverTimeOut = Settings.System.getInt(getContentResolver(), "musicscreen_timeout", 30);
        }
        this.mHandler.sendEmptyMessageDelayed(0, 1000L);
        this.noDVD = getParameters("cfg_dvd=").equals("0");
        TelephonyManager tm = (TelephonyManager) getSystemService("phone");
        tm.listen(this.phoneListener, 32);
        initGps();
        if ("HZC".equals(HctUtil.getCustomerSub()) || "HZC24".equals(HctUtil.getCustomerSub())) {
            Settings.System.putInt(getContentResolver(), "PowerOnIsShowPasswordView", 1);
        }
        if ("HZC41".equals(HctUtil.getCustomerSub())) {
            Settings.System.putInt(this.mContext.getContentResolver(), "LauncherPauseState", 0);
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
        this.mCarManager.putState("navi_package", GPSPKNAME);
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
        if (this.ScreenSaverEnableLocal) {
            this.ScreenSaverTimeOut = Settings.System.getInt(getContentResolver(), "musicscreen_timeout", 30);
        }
        if (btLock || gps_isfront || this.mBackviewState || this.mPowerState != 2 || this.ScreenSaverTimeOut <= 0 || !this.ScreenSaverEnable) {
            this.ScreenSaverTimer = 0;
        }
        int i = this.ScreenSaverTimer;
        if (i == 0) {
            if (this.ScreenSaverTimeOut > 0) {
                sendBroadcastAsUser(new Intent(Constant.CLOCKEND), UserHandle.CURRENT_OR_SELF);
            }
            this.ScreenSaverOn = false;
        } else if (!this.ScreenSaverOn && i >= this.ScreenSaverTimeOut) {
            this.ScreenSaverOn = true;
            if (this.ScreenSaverEnableLocal) {
                startMusicClock();
            } else {
                Intent it1 = new Intent(Constant.MSG_MTC_SCREENSAVER);
                it1.putExtra("timer", this.ScreenSaverTimer);
                sendBroadcastAsUser(it1, UserHandle.CURRENT_OR_SELF);
            }
        }
        this.ScreenSaverTimer++;
        this.mHandler.sendEmptyMessageDelayed(0, 1000L);
        Intent it = new Intent(Constant.MSG_MTC_TIME_FRESH);
        it.addFlags(268435456);
        sendBroadcastAsUser(it, UserHandle.CURRENT_OR_SELF);
    }

    private void clearMusicClock() {
        int musicTimeout = Settings.System.getInt(getContentResolver(), "musicscreen_timeout", 30);
        this.mHandler.removeMessages(4);
        if (musicTimeout != -1) {
            this.ScreenSaverTimer = 0;
        }
    }

    private void InitIntentFilter() {
        IntentFilter itfl = new IntentFilter();
        itfl.addAction("android.intent.action.MEDIA_MOUNTED");
        itfl.addAction("android.intent.action.MEDIA_EJECT");
        itfl.addAction("android.intent.action.MEDIA_UNMOUNTED");
        itfl.addDataScheme("file");
        registerReceiver(this.MediaDetectReceiver, itfl);
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
        registerReceiver(this.mWifiReceiver, itfl4);
        IntentFilter itfl5 = new IntentFilter();
        itfl5.addAction(Constant.MSG_INSTALL_XRROSS);
        registerReceiver(this.mInstallApkReceiver, itfl5);
        IntentFilter itfl6 = new IntentFilter();
        itfl6.addAction(Constant.ACTION_PLUGGED);
        itfl6.addAction("com.microntek.extshow.start");
        registerReceiver(this.mHdmiReceiver, itfl6);
        IntentFilter itfl7 = new IntentFilter();
        itfl7.addAction("android.intent.action.PACKAGE_ADDED");
        itfl7.addAction("android.intent.action.PACKAGE_REMOVED");
        itfl7.addAction("android.intent.action.PACKAGE_CHANGED");
        itfl7.addDataScheme("package");
        registerReceiver(this.mInstallReceiver, itfl7);
        IntentFilter itfl8 = new IntentFilter();
        itfl8.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(this.phoneReceiver, itfl8);
        IntentFilter itfl9 = new IntentFilter();
        itfl9.addAction("android.intent.action.LOCALE_CHANGED");
        registerReceiver(this.mLocaleReceiver, itfl9);
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
        this.mMfiManager.unregisterListener(this.mListener);
        this.mCarManager.detach();
        this.mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(this.MediaDetectReceiver);
        unregisterReceiver(this.MTCAPPProc);
        unregisterReceiver(this.MTCploy);
        unregisterReceiver(this.mWifiReceiver);
        unregisterReceiver(this.mInstallApkReceiver);
        unregisterReceiver(this.mHdmiReceiver);
        unregisterReceiver(this.mInstallReceiver);
        unregisterReceiver(this.phoneReceiver);
        unregisterReceiver(this.mLocaleReceiver);
        unregisterReceiver(this.screenClockBroadcast);
        super.onDestroy();
    }

    private void DoCarEvent(Bundle bundle) {
        String type = bundle.getString("type");
        if ("handbrake".equals(type)) {
            this.mHandbrake = bundle.getBoolean(MicrontekServiceBase.VALUE);
            UpdataDrivingState();
            UpdataHandBrake();
        } else if ("headlight".equals(type)) {
            this.mHeadlight = bundle.getBoolean(MicrontekServiceBase.VALUE);
            UpdataHeadLight();
        } else if ("backview".equals(type)) {
            this.mBackviewState = bundle.getBoolean(MicrontekServiceBase.VALUE);
            UpdataBackView();
        } else if ("ajx".equals(type)) {
            this.mAjx = bundle.getBoolean(MicrontekServiceBase.VALUE);
            UpdataAjx();
        } else if ("ipod".equals(type)) {
            boolean ipod = bundle.getBoolean(MicrontekServiceBase.VALUE);
            if (!this.mIpod && ipod && this.mPowerState == 2 && !btLock && !this.mBackviewState) {
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
            if ("YH".equals(this.mCustomer)) {
                this.mHandler.removeMessages(21);
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
            if ("YH".equals(this.mCustomer)) {
                this.mHandler.removeMessages(21);
                if (Constant.BTPACKAGE.equals(HctUtil.getTopActivityPackageName(this.mContext))) {
                    this.mHandler.sendEmptyMessageDelayed(21, 30000L);
                } else {
                    this.mHandler.sendEmptyMessageDelayed(21, 30000L);
                }
            }
        } else if ("mute".equals(type)) {
            MuteShow(bundle.getBoolean(MicrontekServiceBase.VALUE));
        } else if ("firststart".equals(type)) {
            Settings.System.putInt(this.mContext.getContentResolver(), "hasStartApp", 1);
            if (!this.isCarBox) {
                Settings.System.putString(getContentResolver(), Constant.BKPACKAGE_STRING, "");
            }
            Settings.System.putInt(this.mContext.getContentResolver(), "canbus_updata", 1);
            if (this.mMcuVersion != null && this.mMcuVersion.contains("_GS_")) {
                Settings.System.putInt(getContentResolver(), Constant.FIRSTBOOT_STRING, 128);
            }
            if ("HZC27".equals(this.mCustomerSub)) {
                Settings.System.putInt(getContentResolver(), "isPowerOn", 0);
                Settings.System.putInt(getContentResolver(), "isLock", 1);
            }
        } else if ("overheat".equals(type)) {
            showFloatView(true);
        } else if ("unmatch".equals(type)) {
            byte[] b = bundle.getByteArray(MicrontekServiceBase.VALUE);
            try {
                String unmatch = new String(b, "gb2312").trim();
                showUnMatch(unmatch);
            } catch (Exception e) {
            }
        } else if ("screen_onoff".equals(type)) {
            ReportEvent(type, bundle.getBoolean(MicrontekServiceBase.VALUE));
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
                this.mHandler.postDelayed(new Runnable() { // from class: android.microntek.service.MicrontekServer.10
                    @Override // java.lang.Runnable
                    public void run() {
                        MicrontekServer.this.startTouchKeyStudy();
                    }
                }, 2000L);
            } else {
                showToastMsg("Enter touch study", -1);
            }
        } else if ("power_screen".equals(type)) {
            updataPowerScreen(false);
        }
    }

    private void DoCarPower(String state) {
        if (state == null) {
            return;
        }
        if (state.equals("power_on")) {
            if (this.mPowerState != 2) {
                powerOn();
            }
            this.mWifiFirstRevFlag = false;
            this.mIsFirstUpdataWifiAPState = true;
            if (1 != this.mPowerState) {
                this.mHandler.removeMessages(12);
                Message msg = this.mHandler.obtainMessage();
                msg.what = 12;
                if (getWifiDriverState()) {
                    this.mHandler.sendMessageDelayed(msg, this.mlPwerOnWifiApCheckDelayMillis);
                } else {
                    this.mHandler.sendMessageDelayed(msg, 5000L);
                }
                this.mUpdataingWifiAPState = true;
            }
            this.mPowerState = 2;
            setParameters("rpt_power=true");
            this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.SCREEN_ON"), UserHandle.ALL);
            if ("YH".equals(this.mCustomer) && (this.durationTime - this.onCraeteTime) / 1000 > 8) {
                showYHLogoView(true);
                this.mHandler.removeMessages(18);
                this.mHandler.sendEmptyMessageDelayed(18, 2000L);
            }
            if ("XHWSBOX".equals(this.mCustomer)) {
                Settings.System.putInt(getContentResolver(), CARTOUCH_SHOW, 0);
            }
            showBlackView(true);
        } else if (state.equals("power_off")) {
            powerOff();
            if (this.mPowerState == 0 || -1 == this.mPowerState) {
                this.mHandler.removeMessages(12);
                Message msg2 = this.mHandler.obtainMessage();
                msg2.what = 12;
                if (getWifiDriverState()) {
                    this.mHandler.sendMessageDelayed(msg2, this.mlPwerOnWifiApCheckDelayMillis);
                } else {
                    this.mHandler.sendMessageDelayed(msg2, 5000L);
                }
                this.mUpdataingWifiAPState = true;
            }
            this.mPowerState = 1;
            this.mlPwerOnWifiApCheckDelayMillis = 100L;
            if (this.mCustomerSub.equals("HZC4")) {
                return;
            }
        } else if (state.equals("acc_off")) {
            powerOff();
            if (this.mPowerState != 0) {
                this.mlPwerOnWifiApCheckDelayMillis = 100L;
                saveWifiAPState();
            }
            this.mPowerState = 0;
            if (this.mCustomerSub.equals("HZC4")) {
                return;
            }
        } else if (state.equals("sleep")) {
            this.mlPwerOnWifiApCheckDelayMillis = 3000L;
            this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.SCREEN_OFF"), UserHandle.ALL);
            deviceunMountAndSleep();
            this.mPowerState = -1;
            if (this.mCustomerSub.equals("HZC4")) {
                return;
            }
        } else {
            return;
        }
        ReportEvent("power", this.mPowerState);
    }

    private void saveWifiAPState() {
        this.mHandler.removeMessages(12);
        this.wifiapcheck_cnt = 20;
        if (this.mUpdataingWifiAPState) {
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
        int i = this.wifiapcheck_cnt;
        if (i > 0) {
            this.wifiapcheck_cnt = i - 1;
            if (!apstate && !wifistate) {
                this.mIsFirstUpdataWifiAPState = false;
                this.mUpdataingWifiAPState = false;
                return;
            }
            if (getWifiDriverState()) {
                if (this.mIsFirstUpdataWifiAPState && !this.mGtPlatform) {
                    if (apstate) {
                        setWifiApEnabled(false);
                    } else if (wifistate) {
                        setWifiOn(false);
                    }
                    this.mIsFirstUpdataWifiAPState = false;
                } else {
                    boolean apstate2 = getWifiApState() == 13;
                    boolean wifistate2 = this.mWifiManager.isWifiEnabled();
                    if (apstate2 || wifistate2) {
                        this.wifiapcheck_cnt = 0;
                        if (wifistate) {
                            this.mWifiManager.startScan();
                        }
                        this.mUpdataingWifiAPState = false;
                        return;
                    }
                }
            }
            if (apstate) {
                setWifiApEnabled(true);
            } else if (wifistate) {
                setWifiOn(true);
            }
            this.mHandler.removeMessages(12);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 12;
            this.mHandler.sendMessageDelayed(msg, 1000L);
            return;
        }
        Log.i("wuwq", "updataWifiAPState is timeout");
        this.mIsFirstUpdataWifiAPState = false;
        this.mUpdataingWifiAPState = false;
    }

    private void DoCarKeyDown(Bundle bundle) {
        String type = bundle.getString("type");
        if (type.equals("key")) {
            int keycode = bundle.getInt(MicrontekServiceBase.VALUE);
            ReportEvent("key", keycode);
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
            if (this.mPowerState == 2 && !btLock && !this.mBackviewState && this.mIpod) {
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
                Log.i("MicrontekServer", "DoCarBox start_app ");
            }
            this.isBoxStartApp = true;
        }
    }

    private void UpdataBackView() {
        Intent it = new Intent(MicrontekServiceBase.REPORT_EVENT);
        it.putExtra("type", "backview");
        it.putExtra(MicrontekServiceBase.VALUE, this.mBackviewState);
        sendBroadcastAsUser(it, UserHandle.ALL);
        if (this.mBackviewState) {
            startBackView();
        } else if (needrunnavi) {
            if (isGpsCardMounted()) {
                needrunnavi = false;
                RunApp(GPSPKNAME);
            }
        } else if (mNeedStartApp) {
            mNeedStartApp = false;
            needrunnavi = false;
            MtcStartApp();
        }
    }

    private void DoPressKeyTask(int key) {
        if ("HZC27".equals(this.mCustomerSub) && Settings.System.getInt(getContentResolver(), "isLock", 0) > 0) {
            return;
        }
        switch (key) {
            case 13:
                setBackOnoff();
                return;
            case 256:
                if (!"TELENAV".equals(this.mCustomer) || !SystemProperties.get("sys.telenav.keycode.mode.isIntercepted", "").equals("true")) {
                    this.mHandler.sendEmptyMessage(14);
                    return;
                }
                return;
            case 258:
                MuteSwitch();
                return;
            case 273:
                this.mHandler.sendEmptyMessage(11);
                return;
            case 277:
                if (checkLastSwitchTime()) {
                    startGFsel();
                    return;
                }
                return;
            case 281:
                this.mHandler.sendEmptyMessage(10);
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
                    this.mHandler.sendEmptyMessage(16);
                    return;
                } else {
                    this.mHandler.sendEmptyMessage(17);
                    return;
                }
            case 316:
                if (!btLock && !bCarPlayShow && IsSwitchToBT()) {
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
                if (!btLock && !bCarPlayShow && IsSwitchToBT()) {
                    startBT(1);
                    return;
                }
                return;
            case 331:
                if ("WCX".equals(HctUtil.getCustomer()) || "WE".equals(HctUtil.getCustomerSub())) {
                    String musicName = Settings.System.getString(getContentResolver(), "music_name");
                    if (!TextUtils.isEmpty(musicName)) {
                        RunApp(musicName);
                        return;
                    }
                    return;
                } else if ("CHSS".equals(this.mCustomerSub) || "CHS8".equals(this.mCustomerSub)) {
                    RunApp(getCHSSAppPkName(1));
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
                        RunApp(movieName);
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
                    if (this.mCustomerSub.equals("zst25") && this.screenOn) {
                        MuteSwitch();
                        this.screenOn = false;
                        startHome();
                        return;
                    } else if (this.mCustomerSub.equals("zst25") && !this.screenOn) {
                        MuteSwitch();
                        this.screenOn = true;
                        startscreenlock();
                        return;
                    } else if (checkLastSwitchTime() && !btLock && !Constant.CLOCKSCREENPACKAGE.equals(HctUtil.getTopActivityPackageName(this.mContext))) {
                        this.ScreenSaverTimer = this.ScreenSaverTimeOut;
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
            updataPowerScreen(true);
        }
        this.mHandler.removeCallbacks(this.PowerLongPress);
        this.mHandler.removeCallbacks(this.PowerOffRunnable);
        this.mHandler.removeMessages(8);
        this.mHandler.sendEmptyMessage(8);
        this.mHandler.removeMessages(21);
        this.mHandler.sendEmptyMessageDelayed(21, 30000L);
        GetSystemProperties("ro.product.customer");
        if (this.mPowerState == 0 && !isGpsCardMounted()) {
            mDeviceLock = true;
            this.mHandler.removeMessages(3);
            this.mHandler.sendEmptyMessageDelayed(3, 15000L);
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
            if (!"YH".equals(this.mCustomer)) {
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
            this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
            it.setPackage(AppManager.packageNameCARPLAY[0]);
            this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
        } else if ("2".equals(this.mCarPlayType)) {
            SystemProperties.set(AppManager.packageNameCARPLAY[1], "enable");
            Intent it2 = new Intent(AppManager.packageNameCARPLAY[1]);
            it2.addFlags(16777216);
            it2.putExtra("command", "ACTION_ENTER");
            this.mContext.sendBroadcastAsUser(it2, UserHandle.ALL);
            it2.setPackage(AppManager.packageNameCARPLAY[1]);
            this.mContext.sendBroadcastAsUser(it2, UserHandle.ALL);
        }
    }

    private void powerOff() {
        this.mHandler.removeMessages(3);
        needrunnavi = false;
        mNeedStartApp = false;
        mLastHasGpsCard = isGpsCardMounted();
        if (this.mPowerState == 2 && !this.mCustomerSub.equals("HZC4")) {
            this.mHandler.removeCallbacks(this.PowerLongPress);
            this.mHandler.post(this.PowerLongPress);
        }
        savePoweroffData();
        ReportCanBusDisPlay("type", "off");
        this.mHandler.removeCallbacks(this.PowerOffRunnable);
        this.mHandler.postDelayed(this.PowerOffRunnable, 1000L);
        showBlackView(false);
    }

    private void savePoweroffData() {
        if (this.mPowerState == 2) {
            int state = Settings.System.getInt(this.mContext.getContentResolver(), "hasStartApp", 1);
            if (state == 1) {
                Settings.System.putInt(this.mContext.getContentResolver(), "hasStartApp", 0);
                String[] savepackage = new String[3];
                int i = 0;
                if (HctUtil.CheckIsRun(this.mContext, Constant.RECPACKAGE)) {
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
                        Settings.System.putString(this.mContext.getContentResolver(), Constant.ZLINKCLASS_STRING, className);
                    } else if (!checkPKFilter(toppackagename)) {
                        int i4 = i + 1;
                        savepackage[i] = toppackagename;
                    }
                } else {
                    if (mtcpackagename.equals(Constant.BTPACKAGE) && HctUtil.CheckIsRun(this.mContext, Constant.BTMUSICPACKAGE)) {
                        mtcpackagename = Constant.BTMUSICPACKAGE;
                    }
                    int i5 = i + 1;
                    savepackage[i] = mtcpackagename;
                    if (gps_isfront && gps_open) {
                        int i6 = i5 + 1;
                        savepackage[i5] = GPSPKNAME;
                    }
                }
                ContentResolver contentResolver = this.mContext.getContentResolver();
                Settings.System.putString(contentResolver, Constant.BKPACKAGE_STRING, savepackage[0] + "," + savepackage[1] + "," + savepackage[2]);
            }
        }
        sendBootCheck(this.mContext, "poweroff");
    }

    private void PowerOffAction() {
        startHome();
        if (this.mMfi.equals("2") || (this.mGtPlatform && (this.mMfi.equals("1") || this.mMfi.equals("2")))) {
            if ("1".equals(this.mCarPlayType)) {
                SystemProperties.set(AppManager.packageNameCARPLAY[0], "disable");
                Intent it = new Intent(AppManager.packageNameCARPLAY[0]);
                it.putExtra("command", "ACTION_EXIT");
                this.mContext.sendBroadcastAsUser(it, UserHandle.CURRENT_OR_SELF);
                it.setPackage(AppManager.packageNameCARPLAY[0]);
                this.mContext.sendBroadcastAsUser(it, UserHandle.CURRENT_OR_SELF);
            } else if ("2".equals(this.mCarPlayType)) {
                SystemProperties.set(AppManager.packageNameCARPLAY[1], "disable");
                Intent it2 = new Intent(AppManager.packageNameCARPLAY[1]);
                it2.putExtra("command", "ACTION_EXIT");
                this.mContext.sendBroadcastAsUser(it2, UserHandle.CURRENT_OR_SELF);
                it2.setPackage(AppManager.packageNameCARPLAY[1]);
                this.mContext.sendBroadcastAsUser(it2, UserHandle.CURRENT_OR_SELF);
            }
        }
        this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.SYNC"), UserHandle.CURRENT_OR_SELF);
        this.mAppMode = -1;
        if (this.mCustomerSub.equals("HZC4")) {
            setParameters("rpt_power=wait");
            ReportEvent("power", this.mPowerState);
        } else {
            new Thread(new Runnable() { // from class: android.microntek.service.MicrontekServer.12
                @Override // java.lang.Runnable
                public void run() {
                    ClearProcess.getInstance(MicrontekServer.this.mContext).clearManage(0, null);
                }
            }).start();
            setParameters("rpt_power=false");
        }
        this.mHandler.sendEmptyMessageDelayed(8, 2000L);
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
        sBuf.append("------" + this.mTouchCount + "----");
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            showToastMsg(sBuf.toString(), 0);
            int i2 = this.mTouchCount;
            if (i2 <= 0) {
                this.mHandler.removeMessages(6);
                TouchUpdateAsyncTask updateTextTask = new TouchUpdateAsyncTask(this, list);
                updateTextTask.execute(new Void[0]);
                return;
            }
            this.mTouchCount = i2 - 1;
            this.mHandler.removeMessages(6);
            Message msg1 = this.mHandler.obtainMessage();
            msg1.what = 6;
            msg1.obj = list;
            this.mHandler.sendMessageDelayed(msg1, 1000L);
        }
    }

    private void checkTouchUpdate(String path) {
        List<String> mList = new ArrayList<>();
        for (int i = 0; i < Constant.updateCfgFileName.length; i++) {
            String pathNeme = path + File.separator + Constant.updateCfgFileName[i];
            File file = new File(pathNeme);
            if (file.isFile() && file.exists()) {
                mList.add(pathNeme);
            }
        }
        int i2 = mList.size();
        if (i2 > 0) {
            this.mTouchCount = 7;
            this.mHandler.removeMessages(6);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 6;
            msg.obj = mList;
            this.mHandler.sendMessageDelayed(msg, 1500L);
        }
    }

    private void updateDmcuExtCfg(String path) {
        String result = HctUtil.getTxtFile(path + "/dmcu.ext");
        if (!TextUtils.isEmpty(result)) {
            try {
                showToastMsg(result, 0);
                String[] token = result.split("\n");
                for (String str : token) {
                    String line = str.trim();
                    if (!TextUtils.isEmpty(line)) {
                        if (line.startsWith("screen:") && line.length() > 7) {
                            int par = Integer.parseInt(line.substring(7, line.length()));
                            if (par >= 0 && par <= 63) {
                                setParameters("ctl_tmode=" + (par + 500));
                            } else if (par >= 64 && par <= 127) {
                                setParameters("ctl_tmode=" + (par + 536));
                            }
                        } else if (line.startsWith("backlight:") && line.length() > 10) {
                            int par2 = Integer.parseInt(line.substring(10, line.length()));
                            setParameters("ctl_tmode=" + (par2 + 700));
                        }
                        Log.i("MicrontekServer", "updateDmcuExtCfg:" + line);
                    }
                }
            } catch (Exception e) {
                Log.i("MicrontekServer", "updateDmcuExtCfgException" + e.getMessage());
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
                String[] token = result.split("\n");
                for (String str : token) {
                    String line = str.trim();
                    if (!TextUtils.isEmpty(line)) {
                        if (line.startsWith("copy:") && line.contains(",")) {
                            line = line.substring(line.indexOf(":") + 1, line.length());
                            from = line.substring(0, line.indexOf(","));
                            dest = line.substring(line.indexOf(",") + 1, line.length());
                            if (TextUtils.isEmpty(dest)) {
                                dest = "/";
                            }
                            if (!dest.startsWith("/")) {
                                dest = "/" + dest;
                            }
                        }
                        Log.i("MicrontekServer", "updateHctExtCfg:" + line);
                    }
                }
                if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(dest)) {
                    copyFile(path + "/" + from, "sdcard" + dest);
                }
            } catch (Exception e) {
                Log.i("MicrontekServer", "updateHctExtCfgException" + e.getMessage());
            }
        }
    }

    private void saveCustomerLogo(String path) {
        int h;
        Matrix matrix;
        if (!"false".equals(SystemProperties.get("ro.product.wipe.data", ""))) {
            return;
        }
        String pathName = path + File.separator + "customer.png";
        if (new File(pathName).exists()) {
            Log.i("MicrontekServer", "SaveLogo:" + pathName);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, options);
            int scale = 1;
            int dest_w = dm.widthPixels;
            int dest_h = dm.heightPixels;
            while (true) {
                if ((options.outWidth / scale) / 2 <= dest_w && (options.outHeight / scale) / 2 <= dest_h) {
                    break;
                }
                scale *= 2;
            }
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            options2.inPurgeable = true;
            options2.inInputShareable = true;
            Bitmap bt = BitmapFactory.decodeFile(pathName, options2);
            int w = options2.outWidth;
            int h2 = options2.outHeight;
            float scaleWidth = dest_w / w;
            float scaleHeight = dest_h / h2;
            Matrix matrix2 = new Matrix();
            if (scaleWidth < scaleHeight) {
                h = h2;
                matrix = matrix2;
                matrix.postScale(scaleWidth, scaleWidth);
            } else {
                h = h2;
                matrix = matrix2;
                matrix.postScale(scaleHeight, scaleHeight);
            }
            Bitmap bitmap = Bitmap.createBitmap(bt, 0, 0, w, h, matrix, false);
            File dirFile = new File(this.logoDirPath);
            if (dirFile.mkdirs()) {
                Log.i("MicrontekServer", "creat dirFile");
            } else {
                Log.i("MicrontekServer", "creat dirFile fail!");
            }
            try {
                Runtime.getRuntime().exec("chmod 777 " + this.customerLogoPath);
                File bitmapFile = new File(this.customerLogoPath);
                try {
                    bitmapFile.createNewFile();
                    try {
                        FileOutputStream bitmapWtriter = new FileOutputStream(bitmapFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 99, bitmapWtriter);
                        try {
                            bitmapWtriter.flush();
                            bitmapWtriter.getFD().sync();
                            bitmapWtriter.close();
                            Runtime.getRuntime().exec("chmod 777 " + this.customerLogoPath);
                            Toast.makeText(this.mContext, "Save Logo OK!", 0).show();
                        } catch (IOException e) {
                            Log.i("MicrontekServer", "save fail!");
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e2) {
                        Log.i("MicrontekServer", "FileOutputStream fail!");
                        e2.printStackTrace();
                    }
                } catch (IOException e3) {
                    Log.i("MicrontekServer", "createNewFile fail!  >>> " + e3.toString());
                }
            } catch (Exception e4) {
                Log.i("MicrontekServer", "chmod 777 " + this.customerLogoPath + " fail!");
            }
        }
    }

    private void copyFile(String from, String dest) {
        AlertDialog alertDialog = this.mCopyDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        if (!new File(from).exists()) {
            showToastMsg(from + " no exists !!!!", 0);
            return;
        }
        Log.i("MicrontekServer", "copy from:" + from + "  dest:" + dest);
        SystemProperties.set("sys.hct.copy.path.from", from);
        SystemProperties.set("sys.hct.copy.path.dest", dest);
        SystemProperties.set("sys.hct.copy.result", "");
        SystemProperties.set("service.hctcopy.start", "true");
        this.msg_index = 0;
        this.mHandler.removeMessages(24);
        this.mHandler.sendEmptyMessageDelayed(24, 100L);
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
        this.mCopyDialog = create;
        create.getWindow().setType(2003);
        this.mCopyDialog.show();
    }

    private boolean checkSystemMcuAutoUpdate(String path) {
        long currentTimeMillis = System.currentTimeMillis();
        this.durationTime = currentTimeMillis;
        if ((currentTimeMillis - this.initialTime) / 1000 < 10) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append(File.separator);
        sb.append(this.isCarBox ? "box.auto" : "hct.auto");
        File file = new File(sb.toString());
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
            File file3 = new File(path + File.separator + "dmcu.img");
            if ((file3.isFile() && file3.exists()) || ((gmcuFile.isFile() && gmcuFile.exists()) || ((hmcuFile.isFile() && hmcuFile.exists()) || (imcuFile.isFile() && imcuFile.exists())))) {
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
        new InstallUtil(this, path, this.mHandler);
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

    private boolean focusRequest() {
        this.mAudioManager.requestAudioFocus(this.mAudioFocusListener, 3, 2);
        return 1 == this.mAudioManager.requestAudioFocus(this.mAudioFocusListener, 3, 1);
    }

    private void saveCarBoxData(String packageName) {
        if (this.mPowerState == 2 && this.isBoxStartApp) {
            String[] savepackage = new String[3];
            int i = 0;
            if (HctUtil.CheckIsRun(this.mContext, Constant.RECPACKAGE)) {
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
                    Settings.System.putString(this.mContext.getContentResolver(), Constant.ZLINKCLASS_STRING, className);
                } else if (!checkPKFilter(packageName) && !packageName.startsWith("com.android.launcher")) {
                    int i4 = i + 1;
                    savepackage[i] = packageName;
                }
            } else {
                if (mtcpackagename.equals(Constant.BTPACKAGE) && HctUtil.CheckIsRun(this.mContext, Constant.BTMUSICPACKAGE)) {
                    mtcpackagename = Constant.BTMUSICPACKAGE;
                }
                int i5 = i + 1;
                savepackage[i] = mtcpackagename;
                if (gps_isfront && gps_open) {
                    int i6 = i5 + 1;
                    savepackage[i5] = GPSPKNAME;
                }
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Settings.System.putString(contentResolver, Constant.BKPACKAGE_STRING, savepackage[0] + "," + savepackage[1] + "," + savepackage[2]);
        }
    }

    private void updataPowerScreen(boolean is) {
        this.isPowerScreen = this.mCarManager.getBooleanState("power_screen");
        if (this.isPowerScreenLast != this.isPowerScreen || is) {
            if (this.isPowerScreen) {
                Log.i("MicrontekServer", "-----Enter Power Screen!");
                if (!is && !this.mHandler.hasMessages(MSG_PWR_SCREEN)) {
                    savePoweroffData();
                }
                this.mHandler.removeMessages(MSG_PWR_SCREEN);
                if (!this.mBackviewState) {
                    if (this.isPowerScreenLast != this.isPowerScreen && !SystemProperties.get("sys.ship.package", "").contains("com.android.launcher")) {
                        startHome();
                    }
                    this.ScreenSaverTimer = this.ScreenSaverTimeOut;
                    startMusicClock();
                }
            } else {
                clearMusicClock();
                sendBroadcastAsUser(new Intent(Constant.CLOCKEND), UserHandle.CURRENT_OR_SELF);
                this.mHandler.removeMessages(MSG_PWR_SCREEN);
                this.mHandler.sendEmptyMessageDelayed(MSG_PWR_SCREEN, 1000L);
            }
            this.isPowerScreenLast = this.isPowerScreen;
        }
    }

    private void powerReboot() {
        this.mHandler.removeCallbacks(this.PowerLongPress);
        this.mHandler.post(this.PowerLongPress);
        this.mHandler.removeCallbacks(this.PowerRebootRunnable);
        this.mHandler.postDelayed(this.PowerRebootRunnable, 3000L);
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
                if (this.launcherflag || "com.android.launcher".equals(HctUtil.getTopActivityPackageName(this.mContext)) || "com.android.launcher2p".equals(HctUtil.getTopActivityPackageName(this.mContext)) || "com.android.launcher3".equals(HctUtil.getTopActivityPackageName(this.mContext)) || "com.android.launcher3p".equals(HctUtil.getTopActivityPackageName(this.mContext)) || !flag) {
                    startDefaultLanuncher(flag);
                }
                if (flag) {
                    this.mHandler.removeMessages(13);
                    this.mHandler.sendEmptyMessageDelayed(13, 0L);
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
        MediaRouter mediaRouter = (MediaRouter) getSystemService("media_router");
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(2);
        if (route != null) {
            Display presentationDisplay = route.getPresentationDisplay();
            try {
                options.setLaunchDisplayId(presentationDisplay.getDisplayId());
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(new ComponentName("com.microntek.externshow", "com.microntek.externshow.MainActivity"));
                intent.addFlags(268435456);
                startActivity(intent, options.toBundle());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendKeyCode(int code) {
        Intent intent = new Intent(Constant.MSG_MTC_IRKEY_DOWN);
        intent.putExtra(Constant.KEY_CODE, code);
        this.mContext.sendBroadcast(intent);
    }
}
