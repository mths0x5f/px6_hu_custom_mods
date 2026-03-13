package android.microntek.service;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.microntek.CarManager;
import android.microntek.ClearProcess;
import android.microntek.Constant;
import android.microntek.HCTApi;
import android.microntek.HctUtil;
import android.microntek.app.AppManager;
import android.microntek.app.VolumeDialog;
import android.microntek.common.VolumeInterface;
import android.microntek.mfi.MfiManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/* loaded from: classes.dex */
public class MicrontekServiceBase extends Service implements VolumeInterface {
    private static final String BRIGHTNESSSTATUS = "brightnessStatus";
    protected static final String HCT_CARMANAGER_EVENT = "com.microntek.CarManager.event";
    protected static final String HCT_REPORT_EVENT = "com.microntek.report.event";
    protected static final String HCT_REQUEST_EVENT = "com.microntek.request.event";
    private static final String MODE_CHANGING_ACTION = "com.android.settings.location.MODE_CHANGING";
    protected static final String MSG_LAUNCHER2P_CLS = "com.android.launcher2p2.Launcher";
    protected static final String MSG_LAUNCHER2P_PKG = "com.android.launcher2p";
    protected static final String MSG_LAUNCHER2_CLS = "com.android.launcher2.Launcher";
    protected static final String MSG_LAUNCHER2_PKG = "com.android.launcher";
    protected static final String MSG_LAUNCHER3P_CLS = "com.android.launcher3p.Launcher";
    protected static final String MSG_LAUNCHER3P_PKG = "com.android.launcher3p";
    protected static final String MSG_LAUNCHER3_CLS = "com.android.launcher3.Launcher";
    protected static final String MSG_LAUNCHER3_PKG = "com.android.launcher3";
    private static final String NEW_MODE_KEY = "NEW_MODE";
    public static final int POWER_STA_ACC_OFF = 0;
    public static final int POWER_STA_INVALID = -1;
    public static final int POWER_STA_OFF = 1;
    public static final int POWER_STA_ON = 2;
    public static final String REPORT_EVENT = "com.microntek.report.event";
    protected static final String TAG = "MicrontekServer";
    public static final String TYPE = "type";
    private static final String UPDATEACCDELAYMODE = "updateAccDelayMode";
    private static final String UPDATERIGHTVIEWMODE = "updateRightViewMode";
    private static final String UPDATETHEME = "updateTheme";
    public static final String VALUE = "value";
    private static final String VOICE_NAME = "voice_name";
    public static final int VOL_NAP = 10;
    private boolean addView;
    protected volatile boolean dvdsafeflag;
    protected boolean isRunUsbIpod;
    protected AccDelayObserver mAccDelayObserver;
    protected AudioManager mAudioManager;
    private LinearLayout mBackLayout;
    private View mBoxFloatUnMatchLayout;
    private WindowManager mBoxWindowManager;
    protected ConnectivityManager mConnectivityManager;
    protected Context mContext;
    protected String mCustomer;
    protected String mCustomerSub;
    protected int mDualHomeMode;
    private View mFloatLayout;
    private LinearLayout mFloatUnMatchLayout;
    protected String mMcuVersion;
    protected RightViewObserver mRightViewObserver;
    protected SettingsObserver mSettingsObserver;
    public boolean mWaitingForTerminalState;
    protected WifiManager mWifiManager;
    private WindowManager mWindowManager;
    private View mYHLogoLayout;
    private String[] zst25app;
    private List zst25list;
    private String[] zstapp;
    private List zstlist;
    protected static boolean mDeviceLock = true;
    protected static final boolean EnLog = false;
    protected static boolean btLock = EnLog;
    protected static boolean simPhoneLock = EnLog;
    protected static int KEY_VOLMAX = 30;
    protected static int mVolMaxDefault = 0;
    protected static int mCurVolume = -1;
    protected static String GPSPKNAME = null;
    protected static boolean gps_open = EnLog;
    protected static boolean gps_isfront = EnLog;
    protected static boolean bCarPlayShow = EnLog;
    protected static boolean needrunnavi = EnLog;
    protected static boolean mNeedStartApp = EnLog;
    protected static boolean mLastHasGpsCard = EnLog;
    protected static long runtime = 0;
    protected CarManager mCarManager = null;
    protected MfiManager mMfiManager = null;
    protected ProgressDialog mProgressDialog = null;
    protected int mVolumeTemp = -1;
    protected VolumeDialog mVolumeDialog = null;
    protected boolean mBackviewState = EnLog;
    protected boolean mHandbrake = EnLog;
    protected boolean mHeadlight = EnLog;
    protected int mOrientation = 0;
    protected boolean mAjx = EnLog;
    protected boolean mIpod = EnLog;
    protected String mPackageName = null;
    protected String mClassName = null;
    protected boolean updateLauncher = EnLog;
    protected boolean mUsbIpodSupport = EnLog;
    protected boolean mUsbIpod = EnLog;
    protected boolean mNetflixState = EnLog;
    protected boolean isScreenlock = EnLog;
    protected int mPowerState = 0;
    protected Toast mToast = null;
    protected boolean noDVD = EnLog;
    protected int mAppMode = -1;
    public boolean isHandbrake = EnLog;
    public String mMfi = "";
    public boolean mGtPlatform = EnLog;
    public String mCarPlayType = "";
    public boolean isOrgPanel = EnLog;
    public boolean isCarBox = EnLog;
    public boolean isPowerScreenLast = EnLog;
    public boolean isPowerScreen = EnLog;
    public boolean isStartApp = true;
    private String mCustomerMcuID = "";
    public Toast toast = null;
    public String customerLogoPath = "/cache/logo/customer.png";
    public String logoDirPath = "/cache/logo";
    private ContentObserver apkUpdataContentObserver = new ContentObserver(new Handler()) { // from class: android.microntek.service.MicrontekServiceBase.2
        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            MicrontekServiceBase.this.checkAppUpdata(MicrontekServiceBase.EnLog);
        }
    };
    private INetworkManagementService mNMService = null;
    private List<Integer> mUidList = new ArrayList();
    protected Runnable PowerRebootRunnable = new Runnable() { // from class: android.microntek.service.MicrontekServiceBase.3
        @Override // java.lang.Runnable
        public void run() {
            MicrontekServiceBase.this.setParameters(Constant.MSG_MTC_RBOOT);
        }
    };
    protected Runnable PowerLongPress = new Runnable() { // from class: android.microntek.service.MicrontekServiceBase.4
        @Override // java.lang.Runnable
        public void run() {
            MicrontekServiceBase.this.PowerOffDialog();
        }
    };
    private Handler mVolHandler = new Handler() { // from class: android.microntek.service.MicrontekServiceBase.5
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (MicrontekServiceBase.mCurVolume > MicrontekServiceBase.this.mVolumeTemp) {
                    MicrontekServiceBase microntekServiceBase = MicrontekServiceBase.this;
                    int i = MicrontekServiceBase.mCurVolume;
                    MicrontekServiceBase.mCurVolume = i - 1;
                    microntekServiceBase.setVolume(i, MicrontekServiceBase.EnLog);
                    MicrontekServiceBase.this.mVolHandler.removeCallbacksAndMessages(null);
                    MicrontekServiceBase.this.mVolHandler.sendEmptyMessageDelayed(0, 15L);
                    return;
                }
                MicrontekServiceBase.this.setVolume(MicrontekServiceBase.mCurVolume, true);
                MicrontekServiceBase.this.mVolHandler.removeCallbacksAndMessages(null);
                Message msgvol = MicrontekServiceBase.this.mVolHandler.obtainMessage(3, MicrontekServiceBase.mCurVolume, MicrontekServiceBase.mCurVolume);
                MicrontekServiceBase.this.mVolHandler.sendMessageDelayed(msgvol, 10L);
            } else if (msg.what == 1) {
                if (MicrontekServiceBase.mCurVolume < MicrontekServiceBase.this.mVolumeTemp) {
                    MicrontekServiceBase microntekServiceBase2 = MicrontekServiceBase.this;
                    int i2 = MicrontekServiceBase.mCurVolume;
                    MicrontekServiceBase.mCurVolume = i2 + 1;
                    microntekServiceBase2.setVolume(i2, MicrontekServiceBase.EnLog);
                    MicrontekServiceBase.this.mVolHandler.removeCallbacksAndMessages(null);
                    MicrontekServiceBase.this.mVolHandler.sendEmptyMessageDelayed(1, 15L);
                    return;
                }
                MicrontekServiceBase.this.setVolume(MicrontekServiceBase.mCurVolume, true);
                MicrontekServiceBase.this.mVolHandler.removeCallbacksAndMessages(null);
                Message msgvol2 = MicrontekServiceBase.this.mVolHandler.obtainMessage(3, MicrontekServiceBase.mCurVolume, MicrontekServiceBase.mCurVolume);
                MicrontekServiceBase.this.mVolHandler.sendMessageDelayed(msgvol2, 10L);
            } else if (msg.what == 3) {
                MicrontekServiceBase.this.SendVolStatus(msg.arg1);
            }
        }
    };

    public MicrontekServiceBase() {
        String[] strArr = {Constant.MOVIEPACKAGE, "com.mxtech.videoplayer.ad", Constant.AVINPACKAGE, Constant.DVDPACKAGE, "com.microntek.FileBrowser", "com.android.gallery3d", "com.google.android.gms"};
        this.zstapp = strArr;
        this.zstlist = Arrays.asList(strArr);
        String[] strArr2 = {Constant.MOVIEPACKAGE, Constant.AVINPACKAGE, Constant.DVDPACKAGE, "com.android.chrome", "com.google.android.apps.maps", "com.waze", "com.google.android.youtube", "com.microntek.FileBrowser", "com.android.gallery3d"};
        this.zst25app = strArr2;
        this.zst25list = Arrays.asList(strArr2);
        this.isRunUsbIpod = EnLog;
        this.dvdsafeflag = true;
        this.addView = true;
    }

    protected boolean checkLastSwitchTime() {
        long nowtime = SystemClock.uptimeMillis();
        if (nowtime - runtime > 1000) {
            runtime = nowtime;
            return true;
        }
        return EnLog;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        String platform = SystemProperties.get("ro.board.platform");
        if (platform != null && platform.startsWith("trinket")) {
            this.customerLogoPath = "/data/cache/logo/customer.png";
        }
        this.mCarManager = new CarManager();
        this.mMfiManager = new MfiManager();
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mConnectivityManager = (ConnectivityManager) getSystemService("connectivity");
        this.mAudioManager = (AudioManager) getSystemService("audio");
        Toast makeText = Toast.makeText(this, "", 0);
        this.mToast = makeText;
        makeText.setGravity(17, 0, 0);
        init();
    }

    @Override // android.app.Service
    public void onStart(Intent intent, int startId) {
        runtime = SystemClock.uptimeMillis();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mSettingsObserver.unobserve();
        this.mAccDelayObserver.unobserve();
        this.mRightViewObserver.unobserve();
        super.onDestroy();
    }

    private void BackViewCheck() {
        new Thread(new Runnable() { // from class: android.microntek.service.MicrontekServiceBase.1
            @Override // java.lang.Runnable
            public void run() {
                String status = SystemProperties.get("boot.car.reverse", "0");
                while (status.equals("1")) {
                    status = SystemProperties.get("boot.car.reverse", "0");
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                SystemProperties.set("sys.car.reverse", "2");
            }
        }).start();
    }

    private void init() {
        int brightness;
        String VOL;
        this.mCustomer = SystemProperties.get("ro.product.customer", "");
        this.mCustomerSub = SystemProperties.get("ro.product.customer.sub", "");
        this.mMcuVersion = this.mCarManager.getParameters("sta_mcu_version=");
        this.mDualHomeMode = Integer.parseInt(SystemProperties.get("ro.product.dualhome.mode", "0"));
        SystemProperties.set("persist.product.mcuversion", this.mMcuVersion);
        this.isOrgPanel = "yes".equals(getParameters("sta_mcu_o="));
        this.isCarBox = "BOX".equals(SystemProperties.get("ro.product.project", ""));
        this.isStartApp = "true".equals(SystemProperties.get("ro.product.startapp", "true"));
        BackViewCheck();
        String maxvol = getParameters("cfg_maxvolume=");
        try {
            KEY_VOLMAX = Integer.parseInt(maxvol);
        } catch (Exception e) {
            KEY_VOLMAX = 30;
        }
        String strMaxDefaultVol = getParameters("cfg_vol_max_default=");
        try {
            mVolMaxDefault = Integer.parseInt(strMaxDefaultVol);
        } catch (Exception e2) {
            mVolMaxDefault = 0;
        }
        String backlight = getParameters("cfg_backlight=");
        try {
            brightness = Integer.parseInt(backlight);
        } catch (Exception e3) {
            brightness = Settings.System.getInt(getContentResolver(), "screen_brightness", 100);
        }
        setBrightness(brightness);
        String backviewvol = Settings.System.getString(getContentResolver(), "BackViewVolume");
        if (backviewvol == null) {
            backviewvol = "11";
        }
        setParameters(Constant.MTCBACKVIEWVOL + backviewvol);
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
        String gpsphonevol = Settings.System.getString(getContentResolver(), Constant.MTCGPSPHONE);
        int gpsphonevolume = 0;
        try {
            if (TextUtils.isEmpty(gpsphonevol)) {
                gpsphonevol = GetSystemProperties("ro.product.gpsphone");
                if (gpsphonevol.equals("null")) {
                    gpsphonevol = getParameters("sta_function=24");
                }
            }
            gpsphonevolume = Integer.parseInt(gpsphonevol);
        } catch (Exception e4) {
        }
        setParameters(Constant.MTCGPSPHONE + gpsphonevolume);
        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        this.mSettingsObserver = settingsObserver;
        settingsObserver.observe();
        AccDelayObserver accDelayObserver = new AccDelayObserver(new Handler());
        this.mAccDelayObserver = accDelayObserver;
        accDelayObserver.observe();
        RightViewObserver rightViewObserver = new RightViewObserver(new Handler());
        this.mRightViewObserver = rightViewObserver;
        rightViewObserver.observe();
        UpdataDrivingState();
        this.mCarPlayType = this.mCarManager.getParameters("cfg_factory_part=100,1");
        this.mMfi = this.mCarManager.getParameters("sta_mfi=");
        this.mUsbIpod = this.mMfiManager.isConnected();
        String platform = SystemProperties.get("ro.board.platform");
        if (platform != null && (platform.startsWith("msm") || platform.startsWith("trinket"))) {
            this.mGtPlatform = true;
        }
        getContentResolver().registerContentObserver(Settings.System.getUriFor("hctapkupdata"), true, this.apkUpdataContentObserver, -1);
        checkAppUpdata(true);
        if (this.isCarBox) {
            checkBoxUnMatch();
        }
    }

    private void checkAppUpdata(boolean isUpdata) {
        int apkupdata = Settings.System.getInt(getContentResolver(), "hctapkupdata", -1);
        if (apkupdata != 0 || isUpdata) {
            Log.i("chun", "--mtc >>>> apkUpdataContentObserver");
            Settings.System.putInt(getContentResolver(), "hctapkupdata", 0);
            AppManager.getInstance(this.mContext).disable_APP();
        }
        this.mUsbIpodSupport = AppManager.getInstance(this.mContext).initMfiDeviceApp();
    }

    private INetworkManagementService getNetworkManagementService() {
        IBinder b = ServiceManager.getService("network_management");
        return INetworkManagementService.Stub.asInterface(b);
    }

    private List<String> getWhiteList(String key) {
        List<String> list = new ArrayList<>();
        String s = Settings.System.getString(getContentResolver(), key);
        if (s != null) {
            String[] token = s.split("\n");
            for (int i = 0; i < token.length; i++) {
                if (token[i].length() > 0) {
                    list.add(token[i]);
                }
            }
        }
        return list;
    }

    private boolean isWhiteNet(String name) {
        int netappmode = Settings.System.getInt(getContentResolver(), "hct_net_app_mode", 0);
        List<String> whiteAppList = getWhiteList("hct_net_app_white");
        if (netappmode == 3) {
            if (whiteAppList.contains(name)) {
                return true;
            }
        } else if (netappmode == 2) {
            if (name.equals(GPSPKNAME) || name.contains(Constant.EASYCONNECTEDPACKAGE) || name.contains("weather") || name.equals(AppManager.packageNameCARPLAY[0]) || name.equals(AppManager.packageNameCARPLAY[1]) || name.equals(AppManager.packageNameHICAR[0]) || name.indexOf("navi.") != -1 || name.indexOf(".igo.") != -1 || name.indexOf("map") != -1 || name.indexOf("Map") != -1 || name.equals("com.tencent.wecarnavi") || name.equals("com.waze") || name.equals("fm.qingting.qtradio") || name.equals("com.ximalaya.ting.android") || name.equals("com.baidu.BaiduMap") || name.equals("com.google.android.app.maps")) {
                return true;
            }
        } else if (netappmode == 0) {
            return true;
        }
        return EnLog;
    }

    private void setAppRule(List<Integer> uidList, boolean state) {
        if (this.mNMService == null) {
            this.mNMService = getNetworkManagementService();
        }
        if (state) {
            for (Integer uid : uidList) {
                if (!this.mUidList.contains(uid)) {
                    this.mUidList.add(uid);
                }
                try {
//                    this.mNMService.setDataUidRule(uid.intValue(), state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        for (Integer uid2 : uidList) {
            if (this.mUidList.contains(uid2)) {
                this.mUidList.remove(uid2);
                try {
//                    this.mNMService.setDataUidRule(uid2.intValue(), state);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    protected void UpdataNetForApp() {
        boolean state = this.mCarManager.getBooleanState("handbrake");
        List<Integer> uidList = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packinfos = pm.getInstalledPackages(12288);
        Iterator<PackageInfo> it = packinfos.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PackageInfo info = it.next();
            String[] premissions = info.requestedPermissions;
            if (premissions != null && premissions.length > 0) {
                for (String premission : premissions) {
                    if ("android.permission.INTERNET".equals(premission)) {
                        int uid = info.applicationInfo.uid;
                        if (!uidList.contains(Integer.valueOf(uid))) {
                            uidList.add(Integer.valueOf(uid));
                        }
                    }
                }
            }
        }
        setAppRule(uidList, EnLog);
        if (state) {
            uidList.clear();
            for (PackageInfo info2 : packinfos) {
                String[] premissions2 = info2.requestedPermissions;
                if (premissions2 != null && premissions2.length > 0) {
                    for (String premission2 : premissions2) {
                        if ("android.permission.INTERNET".equals(premission2)) {
                            int uid2 = info2.applicationInfo.uid;
                            String pkname = info2.applicationInfo.packageName;
                            if (!isWhiteNet(pkname) && !uidList.contains(Integer.valueOf(uid2))) {
                                uidList.add(Integer.valueOf(uid2));
                            }
                        }
                    }
                }
            }
            setAppRule(uidList, true);
        }
    }

    protected void UpdataDrivingState() {
        Log.e("UpdataDrivingState", "in2" + HctUtil.getTopActivityPackageName(this));
        int videoEnable = Settings.System.getInt(getContentResolver(), Constant.DVEN_STRING, 0);
        boolean state = this.mCarManager.getBooleanState("handbrake");
        if (state && videoEnable == 0) {
            if ("HST".equals(this.mCustomerSub) && isApplicationBroughtTop()) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(new ComponentName(Constant.HSTBLINKPACKAGE, Constant.HSTBLINKCLASS));
                startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }
            if (isHZC(this.mCustomerSub) && isApplicationBroughtTop()) {
                String asd = getResources().getString(R.string.vedio_warning);
                ClearProcess.getInstance(this.mContext).closePackage("com.microntek.instructionsvideo");
                MyToast(asd);
            }
            this.isHandbrake = true;
            SystemProperties.set("sys.ship.video", "1");
        } else {
            this.isHandbrake = EnLog;
            SystemProperties.set("sys.ship.video", "0");
        }
        UpdataNetForApp();
    }

    public void MyToast(String str) {
        Toast toast = this.toast;
        if (toast != null) {
            toast.setText("" + str);
            this.toast.setDuration(1);
            this.toast.setGravity(17, 0, 0);
            this.toast.show();
            return;
        }
        Context context = this.mContext;
        Toast makeText = Toast.makeText(context, "" + str, 1);
        this.toast = makeText;
        makeText.setGravity(17, 0, 0);
        this.toast.show();
    }

    public boolean isHZC(String str) {
        if (!TextUtils.isEmpty(str)) {
            if ("HZC9".equals(this.mCustomerSub) || "HZC8".equals(this.mCustomerSub) || "HZC7".equals(this.mCustomerSub) || "HZC6".equals(this.mCustomerSub) || "HZC21".equals(this.mCustomerSub)) {
                return true;
            }
            return EnLog;
        }
        return EnLog;
    }

    public boolean isHZC27() {
        return "HZC27".equals(this.mCustomerSub);
    }

    public boolean isApplicationBroughtTop() {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals("com.google.android.youtube") || topActivity.getPackageName().equals("com.microntek.instructionsvideo")) {
                return true;
            }
        }
        return EnLog;
    }

    protected void ReportEvent(String report, boolean state) {
        Intent it = new Intent("com.microntek.report.event");
        it.putExtra("type", report);
        it.putExtra(VALUE, state);
        this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
    }

    protected void ReportEvent(String report, int dat) {
        Intent it = new Intent("com.microntek.report.event");
        it.putExtra("type", report);
        it.putExtra(VALUE, dat);
        this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
    }

    protected void ReportEvent(String report, String state) {
        Intent it = new Intent("com.microntek.report.event");
        it.putExtra("type", report);
        it.putExtra(VALUE, state);
        this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
    }

    protected void ReportCanBusDisPlay(String type, String state) {
        Intent it1 = new Intent(Constant.MSG_MTC_CANBUS_DISPLAY);
        it1.putExtra(type, state);
        this.mContext.sendBroadcastAsUser(it1, UserHandle.CURRENT_OR_SELF);
    }

    protected void setParameters(String para) {
        CarManager carManager = this.mCarManager;
        if (carManager != null) {
            carManager.setParameters(para);
        }
    }

    protected String getParameters(String para) {
        CarManager carManager = this.mCarManager;
        if (carManager != null) {
            return carManager.getParameters(para);
        }
        return null;
    }

    protected void UpdataHandBrake() {
        Intent it = new Intent("com.microntek.report.event");
        it.putExtra("type", "handbrake");
        it.putExtra(VALUE, this.mHandbrake);
        sendBroadcastAsUser(it, UserHandle.ALL);
    }

    protected void UpdataHeadLight() {
        Intent it = new Intent("com.microntek.report.event");
        it.putExtra("type", "headlight");
        it.putExtra(VALUE, this.mHeadlight);
        int defaultHctNightMode = SystemProperties.getInt("ro.product.brightnessmode", 2);
        int nightMode = Settings.System.getInt(getContentResolver(), BRIGHTNESSSTATUS, defaultHctNightMode);
        if (nightMode == 0) {
            Settings.System.putInt(getContentResolver(), UPDATETHEME, 1 ^ (this.mHeadlight ? 1 : 0));
        } else if (nightMode == 1) {
            Settings.System.putInt(getContentResolver(), UPDATETHEME, 1);
        }
        sendBroadcastAsUser(it, UserHandle.ALL);
    }

    protected void UpdataAjx() {
        Intent it = new Intent("com.microntek.report.event");
        it.putExtra("type", "ajx");
        it.putExtra(VALUE, this.mAjx);
        sendBroadcastAsUser(it, UserHandle.ALL);
        if (this.mAjx) {
            Intent canserviceintent = new Intent();
            canserviceintent.setComponent(new ComponentName("android.microntek.canbus", "android.microntek.canbus.Ajxserver"));
            startServiceAsUser(canserviceintent, UserHandle.OWNER);
            return;
        }
        sendBroadcastAsUser(new Intent("com.microntek.ajx"), UserHandle.CURRENT_OR_SELF);
    }

    protected void startIVIApps() {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName("com.transiot.kardiobd2", "com.polstar.obd.LaunchActivity"));
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            Intent intent2 = new Intent("android.intent.action.MAIN");
            intent2.setComponent(new ComponentName("com.transiot.kardiivinavi", "com.transiot.kardiivinavi.activity.LoginActivity"));
            startActivityAsUser(intent2, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void PowerOffDialog() {
        if (this.mProgressDialog == null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            this.mProgressDialog = progressDialog;
            progressDialog.setProgressStyle(0);
            this.mProgressDialog.setTitle(getText(R.string.power_off));
            this.mProgressDialog.setMessage(getText(R.string.shutdown_progress));
            this.mProgressDialog.setIndeterminate(true);
            this.mProgressDialog.setCancelable(EnLog);
            this.mProgressDialog.setCanceledOnTouchOutside(EnLog);
            WindowManager.LayoutParams attrs = this.mProgressDialog.getWindow().getAttributes();
            this.mProgressDialog.getWindow().setAttributes(attrs);
            this.mProgressDialog.getWindow().setType(2009);
        }
        this.mProgressDialog.show();
    }

    protected void MTCAdjVolume(int dir) {
        int current;
        String VOL;
        this.mVolHandler.removeCallbacksAndMessages(null);
        if (dir != 2) {
            if (btLock || simPhoneLock) {
                VOL = Constant.PHONEVOLUME;
            } else {
                VOL = Constant.MTCVOLUME;
            }
            current = Settings.System.getInt(getContentResolver(), VOL, KEY_VOLMAX / 2);
            int i = mCurVolume;
            if (i != current && i > 0) {
                current = this.mVolumeTemp;
            }
            if (dir == 0) {
                if (current > 0) {
                    current--;
                    setVolume(current, true);
                }
            } else if (dir == 1) {
                int i2 = KEY_VOLMAX;
                if (current < i2) {
                    current++;
                    setVolume(current, true);
                } else if (current >= i2) {
                    current = KEY_VOLMAX;
                    setParameters("av_mute=false");
                    setVolume(current, true);
                }
            }
            mCurVolume = current;
            this.mVolumeTemp = current;
            ShowVolumeDalog(current);
        } else {
            VolumeDialog volumeDialog = this.mVolumeDialog;
            if (volumeDialog != null && volumeDialog.isShowing()) {
                this.mVolumeDialog.dismiss();
            }
            int vol = Settings.System.getInt(getContentResolver(), Constant.MTCVOLUME, KEY_VOLMAX / 2);
            setParameters(Constant.MTCVOLUME + HctUtil.mtcGetRealVolume(vol, KEY_VOLMAX));
            int vol1 = Settings.System.getInt(getContentResolver(), Constant.PHONEVOLUME, KEY_VOLMAX / 2);
            setParameters(Constant.PHONEVOLUME + HctUtil.mtcGetRealVolume(vol1, KEY_VOLMAX));
            if (btLock || simPhoneLock) {
                current = vol1;
            } else {
                current = vol;
            }
            mCurVolume = current;
            this.mVolumeTemp = current;
        }
        if (dir == 2 && "true".equals(getParameters(Constant.VOLUMEMUTE))) {
            current = 0;
        }
        Message msgvol = this.mVolHandler.obtainMessage(3, current, current);
        this.mVolHandler.sendMessageDelayed(msgvol, 10L);
    }

    protected void SendVolStatus(int vol) {
        if (this.mMcuVersion.contains("HXD")) {
            this.mAudioManager.setStreamVolume(3, mCurVolume, 4);
        }
        Intent intent = new Intent(Constant.MSG_MTC_VOLUME_CHANGED);
        intent.putExtra("volume", vol);
        intent.putExtra("volumemax", KEY_VOLMAX);
        intent.putExtra("curvolume", mCurVolume);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    protected void setVolume(int vol, boolean write) {
        String VOL;
        if (btLock || simPhoneLock) {
            VOL = Constant.PHONEVOLUME;
        } else {
            VOL = Constant.MTCVOLUME;
        }
        if (write) {
            Settings.System.putInt(getContentResolver(), VOL, vol);
        }
        setParameters(VOL + HctUtil.mtcGetRealVolume(vol, KEY_VOLMAX));
    }

    @Override // android.microntek.common.VolumeInterface
    public void OnChangeVolume(int vol) {
        int mode;
        String VOL;
        this.mVolHandler.removeCallbacksAndMessages(null);
        this.mVolumeTemp = vol;
        int i = mCurVolume;
        if (vol == i) {
            if (btLock || simPhoneLock) {
                VOL = Constant.PHONEVOLUME;
            } else {
                VOL = Constant.MTCVOLUME;
            }
            Settings.System.putInt(getContentResolver(), VOL, vol);
            return;
        }
        if (vol < i) {
            mode = 0;
        } else {
            mode = 1;
        }
        this.mVolHandler.sendEmptyMessageDelayed(mode, 15L);
    }

    @Override // android.microntek.common.VolumeInterface
    public int GetVolume() {
        String VOL;
        if (btLock || simPhoneLock) {
            VOL = Constant.PHONEVOLUME;
        } else {
            VOL = Constant.MTCVOLUME;
        }
        int i = Settings.System.getInt(getContentResolver(), VOL, KEY_VOLMAX / 2);
        mCurVolume = i;
        return i;
    }

    protected void ShowVolumeDalog(int vol) {
        Window dialogWindow;
        VolumeDialog volumeDialog = this.mVolumeDialog;
        if (volumeDialog == null) {
            VolumeDialog volumeDialog2 = new VolumeDialog(this);
            this.mVolumeDialog = volumeDialog2;
            volumeDialog2.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: android.microntek.service.MicrontekServiceBase.6
                @Override // android.content.DialogInterface.OnDismissListener
                public void onDismiss(DialogInterface dialog) {
                    MicrontekServiceBase.this.mVolumeDialog = null;
                }
            });
        } else {
            volumeDialog.SetVolumeDialog(vol);
        }
        VolumeDialog volumeDialog3 = this.mVolumeDialog;
        if (volumeDialog3 != null && !volumeDialog3.isShowing() && !this.mBackviewState && !this.mNetflixState && ((HCTApi.getHctwmPloy() != 1 || "HZCS_2".equals(this.mCustomerSub)) && !"wcxs".equals(this.mCustomerSub))) {
            if ("ASUKA".equals(this.mCustomer) && (dialogWindow = this.mVolumeDialog.getWindow()) != null) {
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(49);
                dialogWindow.setAttributes(lp);
            }
            this.mVolumeDialog.show();
        }
        if ("wcxs".equals(this.mCustomerSub)) {
            this.mVolumeDialog.show();
        }
    }

    protected String getRunMtcAppPackageName() {
        ActivityManager am = (ActivityManager) getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> runtask = am.getRunningTasks(50);
        for (ActivityManager.RunningTaskInfo info : runtask) {
            WindowConfiguration winConfig = info.configuration.windowConfiguration;
            int activityType = winConfig.getActivityType();
            int windowingMode = winConfig.getWindowingMode();
            if (activityType != 2 && activityType != 3 && windowingMode != 4) {
                String strtemp = info.topActivity.getPackageName();
                if (checkPKName(strtemp)) {
                    return strtemp;
                }
            }
        }
        return null;
    }

    protected boolean checkPKName(String pkname) {
        String customer = GetSystemProperties("ro.product.customer");
        if (pkname.equals("com.microntek.dvr") && customer.equals("YH")) {
            return EnLog;
        }
        for (int i = 0; i < Constant.pknameList.length; i++) {
            if (pkname.equals(Constant.pknameList[i])) {
                return true;
            }
        }
        return EnLog;
    }

    protected boolean checkAudioName(String pkname) {
        for (int i = 0; i < Constant.pkAudioList.length; i++) {
            if (pkname.equals(Constant.pkAudioList[i])) {
                return true;
            }
        }
        return EnLog;
    }

    protected boolean checkAudioName2(String pkname) {
        for (int i = 0; i < Constant.pkAudioList2.length; i++) {
            if (pkname.equals(Constant.pkAudioList2[i])) {
                return true;
            }
        }
        return EnLog;
    }

    protected boolean checkCarPlayCall(String pkname, int key) {
        if ((AppManager.packageNameCARPLAY[0].equals(pkname) || AppManager.packageNameCARPLAY[1].equals(pkname) || AppManager.packageNameHICAR[0].equals(pkname)) && (316 == key || 317 == key || 384 == key || 327 == key)) {
            return true;
        }
        return EnLog;
    }

    protected int checkModeAPPName(String pkname) {
        for (int i = 0; i < Constant.ModeAppList.length; i++) {
            if (pkname.equals(Constant.ModeAppList[i])) {
                return i;
            }
        }
        return -1;
    }

    protected boolean checkPKFilter(String pkname) {
        for (int i = 0; i < Constant.pkFilterList.length; i++) {
            if (pkname.equals(Constant.pkFilterList[i])) {
                return true;
            }
        }
        return EnLog;
    }

    protected void SystemKey(int code, int flags) {
        long downtime = SystemClock.uptimeMillis();
        int repeatCount = (flags & 128) != 0 ? 1 : 0;
        KeyEvent ev1 = new KeyEvent(downtime, SystemClock.uptimeMillis(), 0, code, repeatCount, 0, -1, 0, flags | 8 | 64, 257);
        InputManager.getInstance().injectInputEvent(ev1, 0);
        KeyEvent ev2 = new KeyEvent(downtime, SystemClock.uptimeMillis(), 1, code, repeatCount, 0, -1, 0, flags | 8 | 64, 257);
        InputManager.getInstance().injectInputEvent(ev2, 0);
    }

    protected void startHome() {
        Intent mHomeIntent = new Intent("android.intent.action.MAIN");
        mHomeIntent.addCategory("android.intent.category.HOME");
        mHomeIntent.addFlags(270532608);
        try {
            startActivityAsUser(mHomeIntent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void setBrightness(int brightness) {
        if (brightness < 10) {
            brightness = 10;
        }
        Settings.System.putInt(getContentResolver(), "screen_brightness", brightness);
    }

    protected String GetSystemProperties(String key) {
        return SystemProperties.get(key);
    }

    protected void sendBootCheck(Context ct, String cmd) {
        Intent it = new Intent(Constant.MSG_MTC_BOOTCHECK);
        it.putExtra("class", cmd);
        ct.sendBroadcastAsUser(it, UserHandle.ALL);
    }

    protected void startMusicClock() {
        GetSystemProperties("ro.product.customer");
        String customersub = GetSystemProperties("ro.product.customer.sub");
        if (!"HZC25".equals(this.mCustomerSub) && !"HZC25_1".equals(this.mCustomerSub) && !"HZC25_2".equals(this.mCustomerSub) && !"HZC27".equals(customersub) && !"XLY".equals(this.mCustomerSub) && (btLock || gps_isfront)) {
            sendBroadcastAsUser(new Intent(Constant.MSG_ACTIVE), UserHandle.CURRENT_OR_SELF);
            return;
        }
        String packagename = HctUtil.getTopActivityPackageName(this.mContext);
        Log.e("packagename", "" + packagename);
        if (customersub.equals("zst_jac")) {
            if (!this.zstlist.contains(packagename)) {
                startscreenlock();
            }
        } else if (customersub.equals("zst25")) {
            if (!this.zst25list.contains(packagename)) {
                startscreenlock();
            }
        } else if ("HZC25".equals(customersub) || "HZC25_1".equals(customersub) || "HZC25_2".equals(customersub) || "XLY".equals(customersub) || "HZC27".equals(customersub)) {
            startscreenlock();
        } else if (packagename.equals(Constant.MUSICPACKAGE) || packagename.equals(Constant.RADIOPACKAGE) || packagename.equals(Constant.IPODPACKAGE) || packagename.equals(Constant.BTPACKAGE) || packagename.equals(Constant.BTMUSICPACKAGE) || packagename.equals(Constant.SETTINGSPACKAGE) || packagename.startsWith(MSG_LAUNCHER2_PKG)) {
            startscreenlock();
        }
    }

    protected void startscreenlock() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.CLOCKSCREENPACKAGE, Constant.CLOCKSCREENCLASS));
        intent.addFlags(807600128);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void RunApp(String packageName) {
        if (packageName == null) {
            return;
        }
        if (mDeviceLock && packageName.equals(GPSPKNAME) && !isGpsCardMounted() && mLastHasGpsCard) {
            needrunnavi = true;
        } else if (this.mBackviewState) {
            if (packageName.equals(GPSPKNAME)) {
                needrunnavi = true;
            }
        } else if (packageName.equals(Constant.DVDPACKAGE)) {
            startDVD(0);
        } else if (packageName.equals(Constant.TVPACKAGE)) {
            startDTV(0);
        } else if (packageName.equals(Constant.BTPACKAGE)) {
            startBT(0);
        } else if (packageName.equals(Constant.IPODPACKAGE)) {
            startIpod(0);
        } else if (packageName.equals(Constant.USBIPODPACKAGE)) {
            if (this.mUsbIpod) {
                startUsbIpod(0);
            } else {
                this.isRunUsbIpod = true;
            }
        } else {
            try {
                Intent it = getPackageManager().getLaunchIntentForPackage(packageName);
                if (it != null && !packageName.startsWith(MSG_LAUNCHER2_PKG)) {
                    if (packageName.equals(GPSPKNAME) && it.hasCategory("android.intent.category.LAUNCHER")) {
                        it.removeCategory("android.intent.category.LAUNCHER");
                    }
                    it.addFlags(807534592);
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLaunchDisplayId(0);
                    if (isHdmiSwitchSet()) {
                        startActivity(it, options.toBundle());
                    } else {
                        startActivityAsUser(it, UserHandle.CURRENT_OR_SELF);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isHdmiSwitchSet() {
        String carbox_hdmi = getParameters("cfg_carbox_hdmi=");
        return "1".equals(carbox_hdmi);
    }

    protected void startBackView() {
        String cameraoem = SystemProperties.get("hct.camera.ploy", "0");
        if (!TextUtils.isEmpty(cameraoem) && !cameraoem.equals("0")) {
            return;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.BACKVIEWPACKAGE, Constant.BACKVIEWCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    private void counttime() {
        Log.e("counttimedvdsafeflag", "dvdsafeflag" + this.dvdsafeflag);
        final Timer timer = new Timer();
        this.dvdsafeflag = EnLog;
        TimerTask task = new TimerTask() { // from class: android.microntek.service.MicrontekServiceBase.7
            int count = 0;

            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                if (this.count > 5) {
                    MicrontekServiceBase.this.dvdsafeflag = true;
                    timer.cancel();
                }
                this.count++;
            }
        };
        timer.schedule(task, 0L, 1000L);
    }

    protected int startDVD(int flag) {
        if (this.noDVD) {
            startCarCD(0);
            return 0;
        }
        int res = 0;
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.DVDCLASS)) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.DVDPACKAGE, Constant.DVDCLASS));
        if (flag == 1 && gps_isfront) {
            if (!isHZC27()) {
                intent.putExtra("start", 1);
            }
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        String customer = GetSystemProperties("ro.product.customer");
        if (customer.equalsIgnoreCase("zst")) {
            counttime();
        }
        return res;
    }

    protected int startDTV(int flag) {
        int res = 0;
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.TVCLASS)) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.TVPACKAGE, Constant.TVCLASS));
        if (flag == 1 && gps_isfront) {
            if (!isHZC27()) {
                intent.putExtra("start", 1);
            }
            res = 1;
        }
        intent.addFlags(807534592);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected int startBT(int flag) {
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.BTCLASS)) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.BTPACKAGE, Constant.BTCLASS));
        if (flag != 0) {
            ActivityManager am = (ActivityManager) getSystemService("activity");
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            intent.putExtra("nowapplication", cn.getPackageName());
        }
        intent.addFlags(807534592);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return 1;
    }

    protected int startBTMusic(int flag) {
        int res = 0;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.BTMUSICPACKAGE, Constant.BTMUSICCLASS));
        if (flag == 1 && gps_isfront) {
            if (!isHZC27()) {
                intent.putExtra("start", 1);
            }
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected void startNavi() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.NAVIPACKAGE, Constant.NAVICLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected int startAux(int flag) {
        int res = 0;
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.AVINCLASS)) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.AVINPACKAGE, Constant.AVINCLASS));
        if (flag == 1 && gps_isfront) {
            if (!isHZC27()) {
                intent.putExtra("start", 1);
            }
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected int startRadio(int flag) {
        int res = 0;
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.RADIOCLASS)) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.RADIOPACKAGE, Constant.RADIOCLASS));
        if ((flag == 1 && gps_isfront) || flag == 2) {
            if (!isHZC27()) {
                intent.putExtra("start", 1);
            }
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected int startRadio(int flag, int band) {
        int res = 0;
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.RADIOCLASS)) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.RADIOPACKAGE, Constant.RADIOCLASS));
        if ((flag == 1 && gps_isfront) || flag == 2) {
            intent.putExtra("start", 1);
            res = 1;
        }
        intent.putExtra("band", band);
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected int startRadio(int flag, String freq) {
        int res = 0;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.RADIOPACKAGE, Constant.RADIOCLASS));
        if ((flag == 1 && gps_isfront) || flag == 2) {
            intent.putExtra("start", 1);
            res = 1;
        }
        intent.putExtra("freq", freq);
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected void startFrontView(int flag) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.FRONTVIEWPACKAGE, Constant.FRONTVIEWCLASS));
        intent.addFlags(807600128);
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.FRONTVIEWCLASS)) {
            intent.putExtra("start", 1);
        }
        if (flag == 1) {
            intent.putExtra("status", "right");
        }
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startSYNC() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.microntek.sync", Constant.SYNCCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startCarCD(int flag) {
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.CARCDCLASS)) {
            return;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.CARCDPACKAGE, Constant.CARCDCLASS));
        intent.addFlags(807600128);
        intent.putExtra("flag", flag);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected int startIpod(int flag) {
        int res = 0;
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.IPODCLASS)) {
            return 0;
        }
        String ipodbook = this.mCarManager.getParameters("cfg_ipod=");
        if (ipodbook.equals("0")) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.IPODPACKAGE, Constant.IPODCLASS));
        if ((flag == 1 && gps_isfront) || flag == 2) {
            intent.putExtra("start", 1);
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected int startUsbIpod(int flag) {
        int res = 0;
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.USBIPODCLASS)) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.USBIPODPACKAGE, Constant.USBIPODCLASS));
        if ((flag == 1 && gps_isfront) || flag == 2) {
            intent.putExtra("start", 1);
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected int startMusic(String devString, int flag) {
        int res = 0;
        if (!this.dvdsafeflag) {
            return 0;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.MUSICPACKAGE, Constant.MUSICCLASS));
        if (devString != null) {
            intent.putExtra("dev", devString);
        }
        if ((flag == 1 && gps_isfront) || flag == 2) {
            if (!isHZC27()) {
                intent.putExtra("start", 1);
            }
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected int startMovie(int flag) {
        int res = 0;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.MOVIEPACKAGE, Constant.MOVIECLASS));
        if (flag == 1 && gps_isfront) {
            if (!isHZC27()) {
                intent.putExtra("start", 1);
            }
            res = 1;
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
        return res;
    }

    protected void startGoogleVoice() {
        Intent intent;
        String voiceName;
        if ("TELENAV".equals(this.mCustomer) && SystemProperties.get("sys.telenav.keycode.voice", "false").equals("true")) {
            return;
        }
        try {
            PackageManager pm = this.mContext.getPackageManager();
            ComponentName componentName = new ComponentName(Constant.GOOGLEVOICESEARCHPACKAGE, Constant.GOOGLEVOICESEARCHCLASS);
            int temp = -1;
            if (HctUtil.isPackageInstalled(this.mContext, Constant.GOOGLEVOICESEARCHPACKAGE)) {
                temp = pm.getComponentEnabledSetting(componentName);
            }
            if (temp != 2 && temp != 3) {
                intent = new Intent();
                intent.setComponent(componentName);
                voiceName = Settings.System.getString(getContentResolver(), VOICE_NAME);
                if ((TextUtils.isEmpty(voiceName) && !Constant.GOOGLEVOICESEARCHPACKAGE.equals(voiceName) && ((intent = pm.getLaunchIntentForPackage(voiceName)) == null || !HctUtil.isPackageInstalled(this.mContext, voiceName))) || intent == null) {
                    return;
                }
                intent.addFlags(807600128);
                startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }
            intent = new Intent("android.speech.action.WEB_SEARCH");
            voiceName = Settings.System.getString(getContentResolver(), VOICE_NAME);
            if (TextUtils.isEmpty(voiceName)) {
            }
            intent.addFlags(807600128);
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected boolean startTxzVoice() {
        boolean bResult = EnLog;
        String txzAsr = SystemProperties.get("ro.product.txz.asr");
        if (txzAsr == null || "".equals(txzAsr) || "null".equals(txzAsr)) {
            return EnLog;
        }
        Intent txzVoice = new Intent();
        if ("Chinese".equals(txzAsr)) {
            txzVoice.setAction("com.txznet.adapter.recv");
            txzVoice.putExtra("key_type", 2400);
            txzVoice.putExtra("action", "txz.window.auto");
            bResult = true;
        } else if ("English".equals(txzAsr)) {
            txzVoice.setAction("txz.intent.action.smartwakeup.triggerRecordButton");
            bResult = true;
        }
        this.mContext.sendBroadcastAsUser(txzVoice, UserHandle.ALL);
        return bResult;
    }

    protected void startDeskClock() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.GOOGLEDESKCLOCKPACKAGE, Constant.GOOGLEDESKCLOCKCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startEasyconected() {
        Intent intent = new Intent("android.intent.action.MAIN");
        if (Build.VERSION.RELEASE.startsWith("9")) {
            intent.setComponent(new ComponentName(Constant.EASYCONNECTEDPACKAGE, Constant.EASYCONNECTEDCLASS_9));
        } else {
            intent.setComponent(new ComponentName(Constant.EASYCONNECTEDPACKAGE, Constant.EASYCONNECTEDCLASS));
        }
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startSmartLink() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.SAIPAPACKAGE, Constant.SAIPACLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
            Toast.makeText(this, "Please check if the app is installed!", 0).show();
        }
    }

    protected void startBtMusic() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.BTMUSICPACKAGE, Constant.BTMUSICCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startAskAlexa() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.ASKALEXAPACKAGE, Constant.ASKALEXACLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startZlink() {
        if (HctUtil.getTopActivityClassName(this.mContext).startsWith(Constant.ZLINKPACKAGE)) {
            Intent intentSiri = new Intent(Constant.BROADCAST_ASR_REQ_PHONE_SIRI);
            sendBroadcastAsUser(intentSiri, UserHandle.CURRENT_OR_SELF);
            return;
        }
        boolean bZlink3 = "true".equals(SystemProperties.get("ro.product.zlink3", "false"));
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.ZLINKPACKAGE, bZlink3 ? Constant.ZLINK3CLASS : Constant.ZLINKCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startZlink(String className) {
        if (HctUtil.getTopActivityClassName(this.mContext).startsWith(Constant.ZLINKPACKAGE)) {
            Intent intentSiri = new Intent(Constant.BROADCAST_ASR_REQ_PHONE_SIRI);
            sendBroadcastAsUser(intentSiri, UserHandle.CURRENT_OR_SELF);
        } else if (className == null || "".equals(className)) {
            startZlink();
        } else {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(Constant.ZLINKPACKAGE, className));
            intent.addFlags(807600128);
            try {
                startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            } catch (Exception e) {
            }
        }
    }

    protected void startSoundexpress() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.SOUNDEXPRESSPACKAGE, Constant.SOUNDEXPRESSCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void _setHctBacklight(int level) {
        if (level > 255) {
            level = HCT_CMD.CMD_DBG;
        }
        if (level < 0) {
            level = 0;
        }
        setParameters("cfg_backlight=" + level);
        setBrightness(level);
    }

    protected int getmediaAppflag() {
        ActivityManager am = (ActivityManager) getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> runtask = am.getRunningTasks(10);
        for (ActivityManager.RunningTaskInfo info : runtask) {
            if (info.topActivity.getPackageName().equals(Constant.MUSICPACKAGE)) {
                return 1;
            }
            if (info.topActivity.getPackageName().equals(Constant.MOVIEPACKAGE)) {
                return 2;
            }
        }
        return 0;
    }

    protected void startRec(int flag) {
        if (HctUtil.CheckIsRun(this.mContext, Constant.RECPACKAGE)) {
            return;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.putExtra("start", flag);
        ComponentName cn = new ComponentName(Constant.RECPACKAGE, Constant.RECCLASS);
        intent.setComponent(cn);
        intent.addFlags(807534592);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startSettings() {
        ComponentName cn;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        if (this.isOrgPanel) {
            cn = new ComponentName(Constant.ORGSETTINGSPACKAGE, Constant.ORGSETTINGCLASS);
        } else {
            cn = new ComponentName(Constant.SETTINGSPACKAGE, Constant.SETTINGCLASS);
        }
        intent.setComponent(cn);
        intent.addFlags(807534592);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startAVM() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.AVMPACKAGE, Constant.AVMCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startTMPS() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(Constant.TMPSPACKAGE, Constant.TMPSCLASS));
        intent.addFlags(807600128);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00b1  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x00d1  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00e8  */
    /* JADX WARN: Removed duplicated region for block: B:60:0x0108  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0124  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x0132  */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0140  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void startGPS() {
        String gpsnameString = Settings.System.getString(getContentResolver(), "gpspkname");
        if (!TextUtils.isEmpty(GPSPKNAME) && GPSPKNAME.equals(gpsnameString)) {
            if (gps_isfront) {
                if (!gps_open) {
                    RunApp(GPSPKNAME);
                    return;
                }
                ActivityManager am = (ActivityManager) getSystemService("activity");
                List<ActivityManager.RunningTaskInfo> runtask = am.getRunningTasks(40);
                int appindex = -1;
                for (ActivityManager.RunningTaskInfo info : runtask) {
                    WindowConfiguration winConfig = info.configuration.windowConfiguration;
                    int activityType = winConfig.getActivityType();
                    int windowingMode = winConfig.getWindowingMode();
                    if (activityType != 2 && activityType != 3 && windowingMode != 4 && (appindex = checkModeAPPName(info.topActivity.getPackageName())) >= 0) {
                        break;
                    }
                }
                switch (appindex) {
                    case 0:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                            startRadio(0);
                            return;
                        }
                        if (getParameters("sta_dvd=").equals("diskin") && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.DVDPACKAGE)) {
                            startDVD(0);
                            return;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MUSICPACKAGE) && !HCTApi.isSecondScreenApp(Constant.MUSICPACKAGE) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.MUSICPACKAGE)) {
                            startMusic(null, 0);
                            return;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE) && !HCTApi.isSecondScreenApp(Constant.MOVIEPACKAGE) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.MOVIEPACKAGE)) {
                            startMovie(0);
                            return;
                        }
                        if (!this.mUsbIpodSupport && getParameters(Constant.MTCIPOD).equals("true") && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.IPODPACKAGE)) {
                            startIpod(0);
                            return;
                        }
                        if (this.mUsbIpodSupport && this.mUsbIpod && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.USBIPODPACKAGE)) {
                            startUsbIpod(0);
                            return;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                            startAux(0);
                            return;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                            startDTV(0);
                            return;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE) && SystemProperties.get("ro.product.customer", "HCT").equals("XLY")) {
                            startBTMusic(0);
                            return;
                        }
                        backotherappTop();
                        return;
                    case 1:
                        if (getParameters("sta_dvd=").equals("diskin")) {
                            startDVD(0);
                            return;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MUSICPACKAGE)) {
                            startMusic(null, 0);
                            return;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE)) {
                            startMovie(0);
                            return;
                        }
                        if (!this.mUsbIpodSupport) {
                            startIpod(0);
                            return;
                        }
                        if (this.mUsbIpodSupport) {
                            startUsbIpod(0);
                            return;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                            startBTMusic(0);
                            return;
                        }
                        backotherappTop();
                        return;
                    case 2:
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MUSICPACKAGE)) {
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE)) {
                        }
                        if (!this.mUsbIpodSupport) {
                        }
                        if (this.mUsbIpodSupport) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        backotherappTop();
                        return;
                    case 3:
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE)) {
                        }
                        if (!this.mUsbIpodSupport) {
                        }
                        if (this.mUsbIpodSupport) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        backotherappTop();
                        return;
                    case 4:
                        if (!this.mUsbIpodSupport) {
                        }
                        if (this.mUsbIpodSupport) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        backotherappTop();
                        return;
                    case 5:
                        if (this.mUsbIpodSupport) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        backotherappTop();
                        return;
                    case 6:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        backotherappTop();
                        return;
                    case 7:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        backotherappTop();
                        return;
                    case 8:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        backotherappTop();
                        return;
                    default:
                        backotherappTop();
                        return;
                }
            } else if (TextUtils.isEmpty(GPSPKNAME) || !HctUtil.isPackageApplicationEnabled(this.mContext, GPSPKNAME)) {
                RunApp(Constant.NAVIPACKAGE);
                return;
            } else {
                RunApp(GPSPKNAME);
                return;
            }
        }
        if (TextUtils.isEmpty(GPSPKNAME) || !HctUtil.isPackageApplicationEnabled(this.mContext, GPSPKNAME)) {
            RunApp(Constant.NAVIPACKAGE);
        } else {
            RunApp(gpsnameString);
        }
        gps_isfront = true;
        gps_open = true;
        GPSPKNAME = gpsnameString;
    }

    private void backotherappTop() {
        ActivityManager am = (ActivityManager) getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> runtask = am.getRunningTasks(3);
        if (runtask.size() < 2) {
            startHome();
            return;
        }
        ComponentName cnlaunch = runtask.get(1).topActivity;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(cnlaunch);
        if (cnlaunch.getPackageName().contains("launcher")) {
            intent.addCategory("android.intent.category.HOME");
            intent.addFlags(270532608);
        } else {
            intent.addFlags(807534592);
        }
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
            startHome();
        }
    }

    protected void LoudSwitch() {
        String msg;
        if (this.mCarManager.getLud() == 0) {
            this.mCarManager.setLud(true);
            msg = getResources().getString(R.string.loudon);
        } else {
            this.mCarManager.setLud((boolean) EnLog);
            msg = getResources().getString(R.string.loudoff);
        }
        showToastMsg(msg, 0);
    }

    protected void MuteSwitch() {
        String VOL;
        String msg;
        if (getParameters(Constant.VOLUMEMUTE).equals("false")) {
            setParameters("av_mute=true");
            SendVolStatus(0);
            msg = getResources().getString(R.string.muteon);
        } else {
            if (btLock || simPhoneLock) {
                VOL = Constant.PHONEVOLUME;
            } else {
                VOL = Constant.MTCVOLUME;
            }
            int current = Settings.System.getInt(getContentResolver(), VOL, KEY_VOLMAX / 2);
            setParameters("av_mute=false");
            SendVolStatus(current);
            msg = getResources().getString(R.string.muteoff);
        }
        showToastMsg(msg, 0);
    }

    protected void MuteShow(boolean mute) {
        String VOL;
        if (mute) {
            SendVolStatus(0);
            return;
        }
        if (btLock || simPhoneLock) {
            VOL = Constant.PHONEVOLUME;
        } else {
            VOL = Constant.MTCVOLUME;
        }
        int current = Settings.System.getInt(getContentResolver(), VOL, KEY_VOLMAX / 2);
        SendVolStatus(current);
    }

    protected void EQSwitch() {
        int eqmode = this.mCarManager.getEqIdx();
        int eqmode2 = (((eqmode > 6 || eqmode < 0) ? 0 : 0) + 1) % 7;
        this.mCarManager.setEqIdx(eqmode2);
        String toaststring = getResources().getString(R.string.music_style0 + eqmode2).toString();
        showToastMsg(toaststring, 0);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Removed duplicated region for block: B:103:0x021a  */
    /* JADX WARN: Removed duplicated region for block: B:107:0x022a  */
    /* JADX WARN: Removed duplicated region for block: B:110:0x0248  */
    /* JADX WARN: Removed duplicated region for block: B:123:0x0289  */
    /* JADX WARN: Removed duplicated region for block: B:129:0x02b8  */
    /* JADX WARN: Removed duplicated region for block: B:132:0x02d1  */
    /* JADX WARN: Removed duplicated region for block: B:174:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:180:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:182:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:186:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:51:0x00fb  */
    /* JADX WARN: Removed duplicated region for block: B:54:0x0118  */
    /* JADX WARN: Removed duplicated region for block: B:65:0x0151  */
    /* JADX WARN: Removed duplicated region for block: B:76:0x0183  */
    /* JADX WARN: Removed duplicated region for block: B:82:0x01a1  */
    /* JADX WARN: Removed duplicated region for block: B:85:0x01b9  */
    /* JADX WARN: Removed duplicated region for block: B:91:0x01cd  */
    /* JADX WARN: Removed duplicated region for block: B:94:0x01e9  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void ModeSwitch() {
        char c;
        char c2;
        char c3;
        String customer = GetSystemProperties("ro.product.customer");
        ActivityManager am = (ActivityManager) getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> runtask = am.getRunningTasks(40);
        int appindex = -1;
        Iterator<ActivityManager.RunningTaskInfo> it = runtask.iterator();
        while (true) {
            c = 4;
            c2 = 3;
            c3 = 2;
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningTaskInfo info = it.next();
            WindowConfiguration winConfig = info.configuration.windowConfiguration;
            int activityType = winConfig.getActivityType();
            int windowingMode = winConfig.getWindowingMode();
            if (activityType != 2 && activityType != 3 && windowingMode != 4) {
                appindex = checkModeAPPName(info.topActivity.getPackageName());
                if ("com.android.settings.hct.GpsAppChoose".equals(info.topActivity.getClassName()) && isHZC27()) {
                    appindex = 9;
                }
                if (appindex >= 0) {
                    break;
                }
            }
        }
        char c4 = 65535;
        if (appindex == -1) {
            appindex = this.mAppMode;
        }
        int cnt = 0;
        while (true) {
            int cnt2 = cnt + 1;
            if (cnt > Constant.ModeAppList.length) {
                return;
            }
            appindex = (appindex + 1) % Constant.ModeAppList.length;
            String tmp = Constant.ModeAppList[appindex];
            if (!HctUtil.isPackageApplicationEnabled(this.mContext, tmp) || SystemProperties.get("hct.extra.app", "null").equals(tmp) || HCTApi.isSecondScreenApp(tmp) || appindex == this.mAppMode) {
                char c5 = c2;
                char c6 = c4;
                cnt = cnt2;
                c = c;
                c4 = c6;
                c2 = c5;
                c3 = c3;
            } else {
                switch (appindex) {
                    case 0:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                            if (startRadio(1) == 1 || isHZC27()) {
                                showToastMsg(getString(R.string.radio).toString(), -1);
                                this.mAppMode = 0;
                                return;
                            }
                            return;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.DVDPACKAGE) && getParameters("sta_dvd=").equals("diskin")) {
                            if (startDVD(1) != 1) {
                                showToastMsg(getString(R.string.dvd).toString(), -1);
                                this.mAppMode = 1;
                                return;
                            }
                            return;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MUSICPACKAGE) && !HCTApi.isSecondScreenApp(Constant.MUSICPACKAGE) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.MUSICPACKAGE)) {
                            if (startMusic(null, 1) != 1 || isHZC27()) {
                                showToastMsg(getString(R.string.music).toString(), -1);
                                this.mAppMode = 2;
                                return;
                            }
                            return;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE) && !HCTApi.isSecondScreenApp(Constant.MOVIEPACKAGE) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.MOVIEPACKAGE)) {
                            if (startMovie(1) != 1 || isHZC27()) {
                                showToastMsg(getString(R.string.media).toString(), -1);
                                this.mAppMode = 3;
                                return;
                            }
                            return;
                        }
                        if (!this.mUsbIpodSupport && getParameters(Constant.MTCIPOD).equals("true") && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.IPODPACKAGE)) {
                            if (startIpod(1) != 1) {
                                showToastMsg(getString(R.string.ipod).toString(), -1);
                                this.mAppMode = 4;
                                return;
                            }
                            return;
                        }
                        if (this.mUsbIpodSupport && this.mUsbIpod && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.USBIPODPACKAGE)) {
                            if (startUsbIpod(1) != 1) {
                                showToastMsg(getString(R.string.ipod).toString(), -1);
                                this.mAppMode = 5;
                                return;
                            }
                            return;
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.AVINPACKAGE)) {
                            if (startAux(1) != 1 || isHZC27()) {
                                showToastMsg(getString(R.string.avin).toString(), -1);
                                this.mAppMode = 6;
                                return;
                            }
                            return;
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.TVPACKAGE)) {
                            if (startDTV(1) != 1) {
                                showToastMsg(getString(R.string.tv).toString(), -1);
                                this.mAppMode = 7;
                                return;
                            }
                            return;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE) && (customer.equals("XLY") || "HZC25_1".equals(this.mCustomerSub) || "HZC27".equals(this.mCustomerSub))) {
                            if (startBTMusic(1) != 1 || isHZC27()) {
                                showToastMsg(getString(R.string.btmusic).toString(), -1);
                                this.mAppMode = 8;
                                return;
                            }
                            return;
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                            if (!TextUtils.isEmpty(GPSPKNAME)) {
                                startGPS();
                                this.mAppMode = 9;
                                return;
                            }
                            startNavi();
                            showToastMsg(getString(R.string.naviset).toString(), -1);
                            this.mAppMode = 9;
                            return;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                            if (startRadio(1) == 1) {
                                showToastMsg(getString(R.string.radio).toString(), -1);
                                this.mAppMode = 0;
                                return;
                            }
                            return;
                        } else if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MUSICPACKAGE) && !HCTApi.isSecondScreenApp(Constant.MUSICPACKAGE) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.MUSICPACKAGE)) {
                            if (startMusic(null, 1) == 1) {
                                showToastMsg(getString(R.string.music).toString(), -1);
                                this.mAppMode = 2;
                                return;
                            }
                            return;
                        } else if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE) && !HCTApi.isSecondScreenApp(Constant.MOVIEPACKAGE) && HctUtil.isPackageApplicationEnabled(this.mContext, Constant.MOVIEPACKAGE) && startMovie(1) == 1) {
                            showToastMsg(getString(R.string.media).toString(), -1);
                            this.mAppMode = 3;
                            return;
                        } else {
                            return;
                        }
                    case 1:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.DVDPACKAGE)) {
                            if (startDVD(1) != 1) {
                            }
                            break;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MUSICPACKAGE)) {
                            if (startMusic(null, 1) != 1) {
                                break;
                            }
                            showToastMsg(getString(R.string.music).toString(), -1);
                            this.mAppMode = 2;
                            return;
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE)) {
                            if (startMovie(1) != 1) {
                                break;
                            }
                            showToastMsg(getString(R.string.media).toString(), -1);
                            this.mAppMode = 3;
                            return;
                        }
                        if (!this.mUsbIpodSupport) {
                            if (startIpod(1) != 1) {
                            }
                            break;
                        }
                        if (this.mUsbIpodSupport) {
                            if (startUsbIpod(1) != 1) {
                            }
                            break;
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                            if (startAux(1) != 1) {
                                break;
                            }
                            showToastMsg(getString(R.string.avin).toString(), -1);
                            this.mAppMode = 6;
                            return;
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                            if (startDTV(1) != 1) {
                            }
                            break;
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                            if (startBTMusic(1) != 1) {
                                break;
                            }
                            showToastMsg(getString(R.string.btmusic).toString(), -1);
                            this.mAppMode = 8;
                            return;
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 2:
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MUSICPACKAGE)) {
                        }
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE)) {
                        }
                        if (!this.mUsbIpodSupport) {
                        }
                        if (this.mUsbIpodSupport) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 3:
                        if (!SystemProperties.get("hct.extra.app", "null").equals(Constant.MOVIEPACKAGE)) {
                        }
                        if (!this.mUsbIpodSupport) {
                        }
                        if (this.mUsbIpodSupport) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 4:
                        if (!this.mUsbIpodSupport) {
                        }
                        if (this.mUsbIpodSupport) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 5:
                        if (this.mUsbIpodSupport) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 6:
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 7:
                        if (!"HZC25_1".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 8:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.BTMUSICPACKAGE)) {
                        }
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    case 9:
                        if ("HZC27".equals(this.mCustomerSub)) {
                        }
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                    default:
                        if (HctUtil.isPackageApplicationEnabled(this.mContext, Constant.RADIOPACKAGE)) {
                        }
                        break;
                }
            }
        }
    }

    protected void startTouchKeyStudy() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.hct.factory", "com.hct.factory.TouchKeyStudy"));
        intent.putExtra("common", "hcttouch");
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    protected void startGFsel() {
        if (HctUtil.getTopActivityClassName(this.mContext).equals(Constant.GFCLASS)) {
            sendBroadcastAsUser(new Intent(Constant.MSG_MTC_AMP_CLOSE), UserHandle.CURRENT_OR_SELF);
            return;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        ComponentName cn = new ComponentName(Constant.GFPACKAGE, Constant.GFCLASS);
        intent.setComponent(cn);
        intent.addFlags(807534592);
        try {
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
        }
    }

    public void showToastMsg(String msg, int flag) {
        Toast toast = this.mToast;
        if (toast != null) {
            toast.cancel();
        }
        Toast makeText = Toast.makeText(this, msg, 0);
        this.mToast = makeText;
        if (flag != 0) {
            makeText.setGravity(17, 0, -100);
        } else {
            makeText.setGravity(17, 0, 0);
        }
        this.mToast.show();
    }

    protected boolean ChangeKeyTab(int key) {
        ActivityManager am = (ActivityManager) getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> runtask = am.getRunningTasks(40);
        if (runtask == null || runtask.size() == 0) {
            return EnLog;
        }
        runtask.get(0);
        if (this.isOrgPanel && (key == 789 || key == 790 || key == 260 || key == 268 || key == 263 || key == 265 || key == 264)) {
            if (key == 789 || key == 790) {
                String topPkName = HctUtil.getTopActivityPackageName(this.mContext);
                if (!btLock) {
                    if (Arrays.toString(Constant.filterFocusPkList).contains(topPkName)) {
                        return EnLog;
                    }
                    if (topPkName != null && topPkName.contains("com.autonavi.amapauto")) {
                        if (key == 790) {
                            sendAmapAutoKeyEvent(0);
                        }
                        if (key == 789) {
                            sendAmapAutoKeyEvent(1);
                        }
                        return EnLog;
                    } else if (topPkName != null && !topPkName.contains(MSG_LAUNCHER2_PKG)) {
                        if (key == 790) {
                            key = 268;
                        }
                        if (key == 789) {
                            key = 260;
                        }
                    }
                }
            }
        } else if (this.mCustomerSub.equals("RM9")) {
            if (checkAudioName2(HctUtil.getTopActivityPackageName(this.mContext))) {
                return EnLog;
            }
        } else if ("ZF".equals(this.mCustomerSub)) {
            String topPkName2 = HctUtil.getTopActivityPackageName(this.mContext);
            if (topPkName2 != null && !topPkName2.contains(MSG_LAUNCHER2_PKG)) {
                for (ActivityManager.RunningTaskInfo infos : runtask) {
                    String strtemp = infos.topActivity.getPackageName();
                    if (!checkCarPlayCall(HctUtil.getTopActivityPackageName(this.mContext), key) && checkAudioName(strtemp)) {
                        return EnLog;
                    }
                }
            }
        } else if ("HCT7B_LY".equals(this.mCustomerSub)) {
            String topPkName3 = HctUtil.getTopActivityPackageName(this.mContext);
            if (topPkName3 != null && !topPkName3.contains(MSG_LAUNCHER2_PKG)) {
                for (ActivityManager.RunningTaskInfo infos2 : runtask) {
                    String strtemp2 = infos2.topActivity.getPackageName();
                    if (!checkCarPlayCall(HctUtil.getTopActivityPackageName(this.mContext), key) && checkAudioName(strtemp2)) {
                        return EnLog;
                    }
                }
            }
        } else if ("TELENAV".equals(this.mCustomer)) {
            if (SystemProperties.get("sys.telenav.keycode.isIntercepted", "").equals("true")) {
                return EnLog;
            }
            for (ActivityManager.RunningTaskInfo infos3 : runtask) {
                String strtemp3 = infos3.topActivity.getPackageName();
                if (!checkCarPlayCall(HctUtil.getTopActivityPackageName(this.mContext), key) && checkAudioName(strtemp3)) {
                    return EnLog;
                }
            }
        } else {
            for (ActivityManager.RunningTaskInfo infos4 : runtask) {
                String strtemp4 = infos4.topActivity.getPackageName();
                if (!checkCarPlayCall(HctUtil.getTopActivityPackageName(this.mContext), key) && checkAudioName(strtemp4) && !this.mCustomerSub.equals("AKM2") && !this.mCustomerSub.equals("zst25")) {
                    return EnLog;
                }
            }
        }
        if (key == 257) {
            SystemKey(85, 0);
        } else if (key == 260) {
            SystemKey(19, 0);
        } else if (key == 327) {
            SystemKey(777, 0);
        } else if (key == 384) {
            SystemKey(1500, 0);
        } else if (key == 267) {
            SystemKey(86, 0);
        } else if (key == 268) {
            SystemKey(20, 0);
        } else if (key == 316) {
            SystemKey(555, 0);
        } else if (key != 317) {
            if (key != 789) {
                if (key != 790) {
                    switch (key) {
                        case 263:
                            break;
                        case 264:
                            SystemKey(66, 0);
                            break;
                        case 265:
                            break;
                        default:
                            switch (key) {
                                case 299:
                                    SystemKey(88, 0);
                                    break;
                                case 300:
                                    SystemKey(87, 0);
                                    break;
                                case 301:
                                    SystemKey(89, 0);
                                    break;
                                case 302:
                                    SystemKey(90, 0);
                                    break;
                                default:
                                    return EnLog;
                            }
                    }
                }
                SystemKey(22, 0);
            }
            SystemKey(21, 0);
        } else {
            SystemKey(666, 0);
        }
        return true;
    }

    protected void sendAmapAutoKeyEvent(int extra_opera) {
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.setComponent(new ComponentName("com.autonavi.amapauto", "com.autonavi.amapauto.adapter.internal.AmapAutoBroadcastReceiver"));
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE", 1);
        intent.putExtra("EXTRA_OPERA", extra_opera);
        sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    protected void startScreenShot() {
        Intent intent = new Intent();
        intent.setAction("rk.android.screenshot.ACTION");
        sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    protected void setBackOnoff() {
        setParameters("ctl_key=13");
    }

    protected void IRsetBrightness() {
        int brightness;
        int level;
        int[] bri = {10, 51, HCT_CMD.CarCmd.CAR_A_RPT_TOUCH_PRESS, armkeytable.MSG_RADIO_RDS_PSN, HCT_CMD.AvCmd.AV_MSG_DTV_MUTE, HCT_CMD.CMD_DBG};
        String backlight = getParameters("cfg_backlight=");
        try {
            brightness = Integer.parseInt(backlight);
        } catch (Exception e) {
            brightness = Settings.System.getInt(getContentResolver(), "screen_brightness", 100);
        }
        int level2 = brightness / 51;
        if (!"ASUKA".equals(this.mCustomer)) {
            if (level2 < 5) {
                level = level2 + 1;
            } else {
                level = 0;
            }
            brightness = bri[level];
        }
        setParameters("cfg_backlight=" + brightness);
        setBrightness(brightness);
        Intent intent = new Intent("com.android.intent.action.SHOW_BRIGHTNESS_DIALOG");
        sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    protected boolean IsSwitchToBT() {
        boolean en1 = HctUtil.getTopActivityPackageName(this.mContext).equals("com.microntek.sync");
        if (en1) {
            return EnLog;
        }
        return true;
    }

    protected void MtcStartApp() {
        if (!this.isStartApp || "false".equals(SystemProperties.get("ro.product.startapp", "true"))) {
            return;
        }
        if ("WE".equals(HctUtil.getCustomerSub())) {
            int bootRunAppStatu = Settings.System.getInt(getContentResolver(), Constant.BOOTRUNAPP_STATU, 0);
            String bootRunAppPkg = Settings.System.getString(getContentResolver(), Constant.BOOTRUNAPP_PKG);
            if (bootRunAppStatu == 1 && !TextUtils.isEmpty(bootRunAppPkg) && HctUtil.isPackageInstalled(getApplicationContext(), bootRunAppPkg)) {
                RunApp(bootRunAppPkg);
                return;
            }
            return;
        }
        final String result = Settings.System.getString(getContentResolver(), Constant.BKPACKAGE_STRING);
        Settings.System.putInt(getContentResolver(), "hasStartApp", 1);
        if (result == null || result.length() == 0) {
            MtcPowerOnStartGps();
        } else {
            new Thread(new Runnable() { // from class: android.microntek.service.MicrontekServiceBase.8
                @Override // java.lang.Runnable
                public void run() {
                    boolean isGpsRun = MicrontekServiceBase.EnLog;
                    String[] token = result.split(",");
                    String gpsautorun = Settings.System.getString(MicrontekServiceBase.this.getContentResolver(), Constant.MTCGPSAUTORUN);
                    for (int i = 0; i < token.length && token[i] != null; i++) {
                        if (token[i].equals(Constant.RECPACKAGE)) {
                            try {
                                Thread.sleep(1000L);
                            } catch (Exception e) {
                            }
                            MicrontekServiceBase.this.startRec(1);
                            try {
                                Thread.sleep(1000L);
                            } catch (Exception e2) {
                            }
                        } else if (token[i].equals(Constant.ZLINKPACKAGE)) {
                            MicrontekServiceBase microntekServiceBase = MicrontekServiceBase.this;
                            microntekServiceBase.startZlink(Settings.System.getString(microntekServiceBase.getContentResolver(), Constant.ZLINKCLASS_STRING));
                            try {
                                Thread.sleep(500L);
                            } catch (Exception e3) {
                            }
                        } else if (!token[i].equals(MicrontekServiceBase.GPSPKNAME)) {
                            if (token[i].equals(Constant.MOVIEPACKAGE) && Build.VERSION.SDK_INT >= 30) {
                                try {
                                    Thread.sleep(1500L);
                                } catch (Exception e4) {
                                }
                            }
                            MicrontekServiceBase.this.RunApp(token[i]);
                            try {
                                Thread.sleep(500L);
                            } catch (Exception e5) {
                            }
                        } else if (token[i].equals(MicrontekServiceBase.GPSPKNAME)) {
                            isGpsRun = true;
                        }
                    }
                    if (!TextUtils.isEmpty(gpsautorun) && "on".equals(gpsautorun)) {
                        isGpsRun = true;
                    }
                    if (!isGpsRun) {
                        MicrontekServiceBase.this.MtcPowerOnStartGps();
                    } else {
                        MicrontekServiceBase.this.RunApp(MicrontekServiceBase.GPSPKNAME);
                    }
                    if (MicrontekServiceBase.this.mBackviewState) {
                        MicrontekServiceBase.mNeedStartApp = true;
                    }
                }
            }).start();
        }
    }

    protected void MtcPowerOnStartGps() {
        String gpsautorun = Settings.System.getString(getContentResolver(), Constant.MTCGPSAUTORUN);
        if (gpsautorun != null && "on".equals(gpsautorun) && !TextUtils.isEmpty(GPSPKNAME)) {
            RunApp(GPSPKNAME);
        }
    }

    public boolean isGpsOn() {
        int mode = Settings.Secure.getInt(getContentResolver(), "location_mode", 0);
        if (mode == 3) {
            return true;
        }
        return EnLog;
    }

    protected void deviceunMountAndSleep() {
        String powerstate = this.mCarManager.getStringState("carpower");
        if (!powerstate.equals("sleep")) {
            return;
        }
        this.mCarManager.setParameters("rpt_media_safemounted=start");
        IStorageManager storageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        try {
            storageManager.unmount("hctall");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mCarManager.setParameters("rpt_media_safemounted=end");
        String powerstate2 = this.mCarManager.getStringState("carpower");
        if (!powerstate2.equals("sleep")) {
            return;
        }
        gotoSleep();
    }

    protected void closeGps() {
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(NEW_MODE_KEY, 0);
        sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
        Settings.Secure.putInt(getContentResolver(), "location_mode", 0);
    }

    protected void openGps() {
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(NEW_MODE_KEY, 3);
        sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
        Settings.Secure.putInt(getContentResolver(), "location_mode", 3);
    }

    protected boolean isAirplaneModeOn() {
        if (Settings.Global.getInt(getContentResolver(), "airplane_mode_on", 0) != 0) {
            return true;
        }
        return EnLog;
    }

    protected void setAirplaneModeOn(boolean enabled) {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService("connectivity");
        mgr.setAirplaneMode(enabled);
    }

    protected void gotoSleep() {
        Log.i("chun", "goToSleep >>>>>>>>>>");
        PowerManager pm = (PowerManager) getSystemService("power");
        pm.goToSleep(SystemClock.uptimeMillis());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Type inference failed for: r0v0, types: [android.microntek.service.MicrontekServiceBase$9] */
    public void setWifiOn(final boolean enabled) {
        new AsyncTask<Void, Void, Void>() { // from class: android.microntek.service.MicrontekServiceBase.9
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... args) {
                MicrontekServiceBase.this.mWifiManager.setWifiEnabled(enabled);
                return null;
            }
        }.execute(new Void[0]);
    }

    protected void setWifiApEnabled(boolean enabled) {
        if (this.mWaitingForTerminalState) {
            return;
        }
        if (!enabled) {
            this.mConnectivityManager.stopTethering(0);
            return;
        }
        this.mWaitingForTerminalState = true;
        this.mConnectivityManager.startTethering(0, EnLog, new ConnectivityManager.OnStartTetheringCallback() { // from class: android.microntek.service.MicrontekServiceBase.10
            public void onTetheringFailed() {
                if (!MicrontekServiceBase.this.mWaitingForTerminalState) {
                    return;
                }
                int wifiApState = MicrontekServiceBase.this.getWifiApState();
                if (wifiApState != 11 && wifiApState != 13) {
                    if (wifiApState == 14) {
                        MicrontekServiceBase.this.mConnectivityManager.stopTethering(0);
                    } else {
                        return;
                    }
                }
                MicrontekServiceBase.this.mWaitingForTerminalState = MicrontekServiceBase.EnLog;
            }
        });
    }

    protected int getWifiApState() {
        try {
            return this.mWifiManager.getWifiApState();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected boolean getWifiDriverState() {
        boolean wlanState = EnLog;
        if (this.mGtPlatform) {
            return true;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/wireless"));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                Log.d(TAG, "line:" + line);
                if (line.contains("wlan0") || line.contains("p2p0")) {
                    wlanState = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wlanState;
    }

    protected boolean isGpsCardMounted() {
        StorageManager storageManager = (StorageManager) getSystemService(StorageManager.class);
        List<VolumeInfo> volumes = storageManager.getVolumes();
        for (VolumeInfo vol : volumes) {
            if (vol.getType() == 0 && vol.isMountedReadable()) {
                DiskInfo disk = vol.getDisk();
                if (disk.isSd() && storageManager.getBestVolumeDescription(vol).equals("GPS")) {
                    return true;
                }
            }
        }
        return EnLog;
    }

    protected String getDeviceType(Context context, String path) {
        StorageManager storageManager = (StorageManager) context.getSystemService(StorageManager.class);
        List<VolumeInfo> volumes = storageManager.getVolumes();
        for (VolumeInfo vol : volumes) {
            if (vol.getType() == 0 && vol.isMountedReadable() && vol.getPath().getPath().equals(path)) {
                DiskInfo disk = vol.getDisk();
                if (disk.isSd()) {
                    String type = storageManager.getBestVolumeDescription(vol);
                    return type;
                } else if (!disk.isUsb()) {
                    return "";
                } else {
                    return "USB";
                }
            }
        }
        return "";
    }

    protected void showFloatView(boolean visable) {
        if (visable) {
            if (this.mWindowManager == null) {
                Application application = getApplication();
                getApplication();
                this.mWindowManager = (WindowManager) application.getSystemService("window");
            }
            if (this.mFloatLayout == null) {
                LayoutInflater inflater = LayoutInflater.from(getApplication());
                this.mFloatLayout = inflater.inflate(R.layout.heatover, (ViewGroup) null);
                WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
                wmParams.type = 2007;
                wmParams.format = 1;
                wmParams.flags = 56;
                wmParams.gravity = 81;
                wmParams.x = 0;
                wmParams.y = 0;
                wmParams.width = -1;
                wmParams.height = -1;
                this.mWindowManager.addView(this.mFloatLayout, wmParams);
                this.mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
                return;
            }
            return;
        }
        View view = this.mFloatLayout;
        if (view != null) {
            this.mWindowManager.removeView(view);
            this.mFloatLayout = null;
        }
    }

    protected void showBlackView(boolean visable) {
        boolean isShowTop = EnLog;
        String mShowBlackView = SystemProperties.get("ro.product.showblackview", "");
        if (TextUtils.isEmpty(mShowBlackView)) {
            return;
        }
        String mShowBlackViewMode = SystemProperties.get("ro.product.showblackviewmode", "0");
        if ("0".equals(mShowBlackViewMode)) {
            isShowTop = true;
        }
        if (visable) {
            if (this.mWindowManager == null) {
                Application application = getApplication();
                getApplication();
                this.mWindowManager = (WindowManager) application.getSystemService("window");
            }
            if (this.mBackLayout == null) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
                LinearLayout linearLayout = new LinearLayout(this);
                this.mBackLayout = linearLayout;
                linearLayout.setLayoutParams(lp);
                this.mBackLayout.setBackgroundColor(getResources().getColor(17170444));
                WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
                wmParams.gravity = isShowTop ? 48 : 80;
                wmParams.x = 0;
                wmParams.y = 0;
                wmParams.width = -1;
                wmParams.height = 1;
                wmParams.type = 2006;
                wmParams.flags = 1280;
                wmParams.systemUiVisibility = 4102;
                this.mWindowManager.addView(this.mBackLayout, wmParams);
                this.mBackLayout.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
                return;
            }
            return;
        }
        LinearLayout linearLayout2 = this.mBackLayout;
        if (linearLayout2 != null) {
            this.mWindowManager.removeView(linearLayout2);
            this.mBackLayout = null;
        }
    }

    public Bitmap getLocalBitmap(File file) {
        if (!file.exists()) {
            return null;
        }
        try {
            Bitmap bt = BitmapFactory.decodeFile(file.getPath());
            return bt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void showUnMatch(String msg) {
        if (this.mWindowManager == null) {
            Application application = getApplication();
            getApplication();
            this.mWindowManager = (WindowManager) application.getSystemService("window");
        }
        LinearLayout linearLayout = this.mFloatUnMatchLayout;
        if (linearLayout == null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            LinearLayout linearLayout2 = new LinearLayout(this);
            this.mFloatUnMatchLayout = linearLayout2;
            linearLayout2.setLayoutParams(lp);
            this.mFloatUnMatchLayout.setBackgroundColor(Integer.MIN_VALUE);
            this.mFloatUnMatchLayout.setOrientation(0);
            ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(-2, -2);
            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(vlp);
            tv1.setText(msg);
            tv1.setTextColor(-65536);
            this.mFloatUnMatchLayout.addView(tv1);
            tv1.setTag("unmatch");
            WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
            wmParams.type = 2007;
            wmParams.format = 1;
            wmParams.flags = 56;
            wmParams.gravity = 81;
            wmParams.x = 0;
            wmParams.y = 0;
            wmParams.width = -2;
            wmParams.height = -2;
            this.mWindowManager.addView(this.mFloatUnMatchLayout, wmParams);
        } else {
            TextView tv12 = (TextView) linearLayout.findViewWithTag("unmatch");
            if (tv12 != null) {
                tv12.setText(msg);
            }
        }
        this.mFloatUnMatchLayout.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
    }

    protected void checkBoxUnMatch() {
        String androidCid = SystemProperties.get("ro.product.android.cid", "0");
        if (TextUtils.isEmpty(androidCid) || "0".equals(androidCid) || TextUtils.isEmpty(getCustomerMcuID()) || getCustomerMcuID().equals(androidCid)) {
            return;
        }
        if (this.mBoxWindowManager == null) {
            Application application = getApplication();
            getApplication();
            this.mBoxWindowManager = (WindowManager) application.getSystemService("window");
        }
        if (this.mBoxFloatUnMatchLayout == null) {
            LayoutInflater inflater = LayoutInflater.from(getApplication());
            this.mBoxFloatUnMatchLayout = inflater.inflate(R.layout.unmath_layout, (ViewGroup) null);
            WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
            wmParams.type = 2010;
            wmParams.format = 1;
            wmParams.flags = 1336;
            wmParams.gravity = 81;
            wmParams.x = 0;
            wmParams.y = 0;
            TextView tv1 = (TextView) this.mBoxFloatUnMatchLayout.findViewById(R.id.unmatch);
            if (tv1 != null) {
                tv1.setText(getCustomerMcuID() + " " + androidCid);
            }
            wmParams.width = -1;
            wmParams.height = -1;
            this.mBoxWindowManager.addView(this.mBoxFloatUnMatchLayout, wmParams);
        }
        this.mBoxFloatUnMatchLayout.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
    }

    protected String getCustomerMcuID() {
        if (!TextUtils.isEmpty(this.mCustomerMcuID)) {
            return this.mCustomerMcuID;
        }
        String strID = this.mCarManager.getParameters("cfg_factory_part=4,4");
        if (!strID.contains(",")) {
            return this.mCustomerMcuID;
        }
        String[] strArray = strID.split(",");
        for (int i = strArray.length - 1; i >= 0; i--) {
            this.mCustomerMcuID += "" + Integer.toHexString(Integer.valueOf(strArray[i]).intValue());
        }
        String upperCase = this.mCustomerMcuID.toUpperCase();
        this.mCustomerMcuID = upperCase;
        return upperCase;
    }

    /* loaded from: classes.dex */
    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = MicrontekServiceBase.this.mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(Constant.DVEN_STRING), MicrontekServiceBase.EnLog, this);
            resolver.registerContentObserver(Settings.System.getUriFor("hct_net_app_mode"), MicrontekServiceBase.EnLog, this);
            resolver.registerContentObserver(Settings.System.getUriFor("hct_net_app_white"), MicrontekServiceBase.EnLog, this);
            MicrontekServiceBase.this.updateSettings();
        }

        void unobserve() {
            MicrontekServiceBase.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            MicrontekServiceBase.this.updateSettings();
        }
    }

    public void updateSettings() {
        boolean state = this.mCarManager.getBooleanState("handbrake");
        if (state) {
            UpdataDrivingState();
        }
    }

    /* loaded from: classes.dex */
    private class AccDelayObserver extends ContentObserver {
        public AccDelayObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = MicrontekServiceBase.this.mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(MicrontekServiceBase.UPDATEACCDELAYMODE), MicrontekServiceBase.EnLog, this);
            MicrontekServiceBase.this.updateAccDelayMode();
        }

        void unobserve() {
            MicrontekServiceBase.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            MicrontekServiceBase.this.updateAccDelayMode();
        }
    }

    private void updateAccDelayMode() {
        int def = 0;
        if ("YH".equals(this.mCustomer)) {
            def = 3;
        }
        int accDelayMode = Settings.System.getInt(this.mContext.getContentResolver(), UPDATEACCDELAYMODE, def);
        setParameters(Constant.MTC_CFG_ACC_DELAY + accDelayMode);
    }

    /* loaded from: classes.dex */
    private class RightViewObserver extends ContentObserver {
        public RightViewObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = MicrontekServiceBase.this.mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(MicrontekServiceBase.UPDATERIGHTVIEWMODE), MicrontekServiceBase.EnLog, this);
            MicrontekServiceBase.this.updateRightViewMode();
        }

        void unobserve() {
            MicrontekServiceBase.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            MicrontekServiceBase.this.updateRightViewMode();
        }
    }

    private void updateRightViewMode() {
        int rightViewMode = Settings.System.getInt(getContentResolver(), UPDATERIGHTVIEWMODE, 0);
        setParameters(Constant.MTC_CFG_RIGHT_VIEW + rightViewMode);
    }

    private String getTopActivity() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService("activity");
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos == null || runningTaskInfos.size() == 0) {
                return "";
            }
            return runningTaskInfos.get(0).baseActivity.getPackageName();
        } catch (Exception e) {
            return "";
        }
    }

}
