package android.microntek.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.microntek.CarManager;
import android.microntek.Constant;
import android.microntek.HctUtil;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
/* loaded from: classes.dex */
public class AppManager {
    private static CarManager mCarManager;
    private static Context mContext;
    private static AppManager sInstance;
    private static final String[] packageNameDialer = {"com.android.dialer", "org.codeaurora.dialer"};
    private static final String[] packageNameMMS = {"com.android.mms", "com.android.messaging"};
    private static final String[] packageNameContacts = {"com.android.contacts"};
    private static final String[] packageNameDVD = {Constant.DVDPACKAGE};
    private static final String[] packageNameAVIN = {Constant.AVINPACKAGE};
    private static final String[] packageNameRearPanel = {"com.microntek.externshow"};
    private static final String[] packageNameRGBKEY = {"com.microntek.rgbkey"};
    private static final String[] packageNameTV = {Constant.TVPACKAGE};
    private static final String[] packageNameBT = {Constant.BTPACKAGE, Constant.BTMUSICPACKAGE, "com.goodocom.gocsdk"};
    private static final String[] packageNameIPOD = {Constant.IPODPACKAGE};
    private static final String[] packageNameUSBIPOD = {Constant.USBIPODPACKAGE};
    private static final String[] packageNameSYNC = {"com.microntek.sync"};
    private static final String[] packageNameFrontView = {Constant.FRONTVIEWPACKAGE};
    private static final String[] packageNameWheelStudy = {"com.hct.wheelstudy"};
    private static final String[] packageNameRadio = {Constant.RADIOPACKAGE};
    public static final String[] packageNameDVR = {Constant.RECPACKAGE};
    public static final String[] packageNameCvbsDVR = {"com.microntek.dvr"};
    public static final String[] packageNameTpms = {"com.microntek.tpms"};
    public static final String[] packageNameHdmi = {"com.microntek.hdmi"};
    public static final String[] packageNameEASYCONN = {Constant.EASYCONNECTEDPACKAGE};
    public static final String[] packageNameCARPLAY = {Constant.ZLINKPACKAGE, Constant.SPEEDPLAYPACKAGE};
    public static final String[] packageNameHICAR = {"com.huawei.hicar"};
    public static final String[] packageNameDAB = {"com.microntek.dab"};
    public static final String[] packageNameDAB_YHS_USB = {"com.ex.dabplayer.pad"};
    public static final String[] packageNameDAB_YHS_SERIAL_PORT = {"com.ex.yhxdabw8.pad"};
    public static final String[] packageNameHdmiIn = {"com.microntek.hdmiin"};
    public static final String[] packageNameLedSettings = {"com.microntek.ledsettings"};
    public static final String[] packageNameCursorToggle = {"com.microntek.cursortoggle"};
    private static Object sGlobalLock = new Object();

    private AppManager(Context context) {
        mContext = context;
        mCarManager = new CarManager();
    }

    public static AppManager getInstance(Context context) {
        AppManager appManager;
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new AppManager(context);
            }
            appManager = sInstance;
        }
        return appManager;
    }

    private void setParameters(String cmd) {
        CarManager carManager = mCarManager;
        if (carManager != null) {
            carManager.setParameters(cmd);
        }
    }

    private String getParameters(String cmd) {
        CarManager carManager = mCarManager;
        if (carManager != null) {
            return carManager.getParameters(cmd);
        }
        return null;
    }

    public void disable_APP() {
        int appstate;
        int appstate2;
        int appstate3;
        int appstate4;
        int appstate5;
        int appstate6;
        int appstate7;
        int appstate8;
        int appstate9;
        int appstate10;
        int appstate11;
        int appstate12;
        int appstate13;
        int appstate14;
        int appstate15;
        int appstate16;
        int appstate17;
        String appalive = getParameters("cfg_wheelstudy_type=");
        try {
            appstate = Integer.parseInt(appalive);
        } catch (Exception e) {
            appstate = 0;
        }
        if (appstate == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameWheelStudy, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameWheelStudy, true);
        }
        String appalive2 = getParameters("cfg_dtv=");
        try {
            appstate2 = Integer.parseInt(appalive2);
        } catch (Exception e2) {
            appstate2 = 0;
        }
        if (appstate2 == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameTV, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameTV, true);
        }
        String appalive3 = getParameters("sta_mfi=");
        try {
            appstate3 = Integer.parseInt(appalive3);
        } catch (Exception e3) {
            appstate3 = 0;
        }
        if (appstate3 != 1 && appstate3 != 2) {
            HctUtil.setApplicationEnabled(mContext, packageNameUSBIPOD, false);
            String appalive4 = getParameters("cfg_ipod=");
            try {
                appstate17 = Integer.parseInt(appalive4);
            } catch (Exception e4) {
                appstate17 = 0;
            }
            if (appstate17 == 0) {
                HctUtil.setApplicationEnabled(mContext, packageNameIPOD, false);
            } else {
                HctUtil.setApplicationEnabled(mContext, packageNameIPOD, true);
            }
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameUSBIPOD, true);
            HctUtil.setApplicationEnabled(mContext, packageNameIPOD, false);
        }
        String appalive5 = getParameters("cfg_dvd=");
        try {
            appstate4 = Integer.parseInt(appalive5);
        } catch (Exception e5) {
            appstate4 = 0;
        }
        if (appstate4 == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameDVD, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameDVD, true);
        }
        String appalive6 = getParameters("cfg_led_type=");
        try {
            appstate5 = Integer.parseInt(appalive6);
        } catch (Exception e6) {
            appstate5 = 0;
        }
        if (appstate5 == 0 || appstate5 >= 3) {
            HctUtil.setApplicationEnabled(mContext, packageNameRGBKEY, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameRGBKEY, true);
        }
        String appalive7 = getParameters("cfg_bt=");
        try {
            appstate6 = Integer.parseInt(appalive7);
        } catch (Exception e7) {
            appstate6 = 0;
        }
        if (appstate6 == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameBT, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameBT, true);
        }
        String appalive8 = getParameters("cfg_frontview=");
        try {
            appstate7 = Integer.parseInt(appalive8);
        } catch (Exception e8) {
            appstate7 = 0;
        }
        if (appstate7 == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameFrontView, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameFrontView, true);
        }
        String appalive9 = getParameters("cfg_radio=");
        try {
            appstate8 = Integer.parseInt(appalive9);
        } catch (Exception e9) {
            appstate8 = 0;
        }
        if (appstate8 == 1) {
            HctUtil.setApplicationEnabled(mContext, packageNameRadio, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameRadio, true);
        }
        String appalive10 = getParameters("cfg_dvr=");
        try {
            appstate9 = Integer.parseInt(appalive10);
        } catch (Exception e10) {
            appstate9 = 0;
        }
        if (appstate9 == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameDVR, true);
            HctUtil.setApplicationEnabled(mContext, packageNameCvbsDVR, false);
        } else if (appstate9 == 1) {
            HctUtil.setApplicationEnabled(mContext, packageNameDVR, false);
            HctUtil.setApplicationEnabled(mContext, packageNameCvbsDVR, true);
        } else if (appstate9 != 2) {
            HctUtil.setApplicationEnabled(mContext, packageNameDVR, false);
            HctUtil.setApplicationEnabled(mContext, packageNameCvbsDVR, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameDVR, true);
            HctUtil.setApplicationEnabled(mContext, packageNameCvbsDVR, true);
        }
        String appalive11 = getParameters("cfg_tpms=");
        try {
            appstate10 = Integer.parseInt(appalive11);
        } catch (Exception e11) {
            appstate10 = 0;
        }
        if (appstate10 == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameTpms, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameTpms, true);
        }
        String appalive12 = getParameters("cfg_factory_part=98,1");
        try {
            Integer.parseInt(appalive12);
        } catch (Exception e12) {
        }
        String appalive13 = getParameters("sta_function=30");
        try {
            appstate11 = Integer.parseInt(appalive13);
        } catch (Exception e13) {
            appstate11 = 0;
        }
        if (appstate11 != 1) {
            HctUtil.setApplicationEnabled(mContext, packageNameHdmi, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameHdmi, true);
        }
        String appalive14 = getParameters("cfg_factory_part=104,1");
        try {
            appstate12 = Integer.parseInt(appalive14);
        } catch (Exception e14) {
            appstate12 = 0;
        }
        if (appstate12 == 1) {
            HctUtil.setApplicationEnabled(mContext, packageNameAVIN, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameAVIN, true);
        }
        String appalive15 = getParameters("cfg_dab=");
        try {
            appstate13 = Integer.parseInt(appalive15);
        } catch (Exception e15) {
            appstate13 = 0;
        }
        String customer = SystemProperties.get("ro.product.customer");
        if ("HT".equals(customer) || "WCX".equals(customer)) {
            initAppForHT(appstate13);
        } else {
            if (appstate13 != 2) {
                HctUtil.setApplicationEnabled(mContext, packageNameDAB, false);
            } else {
                HctUtil.setApplicationEnabled(mContext, packageNameDAB, true);
            }
            if (appstate13 == 3) {
                HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_SERIAL_PORT, true);
                HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_USB, false);
            } else {
                HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_USB, true);
                HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_SERIAL_PORT, false);
            }
        }
        String appalive16 = getParameters("cfg_rear_panel=");
        try {
            appstate14 = Integer.parseInt(appalive16);
        } catch (Exception e16) {
            appstate14 = 0;
        }
        String[] versionParts = Build.VERSION.RELEASE.split("\\.");
        int majorVersion = Integer.valueOf(versionParts[0]).intValue();
        if (majorVersion >= 10) {
            HctUtil.setApplicationEnabled(mContext, packageNameRearPanel, false);
        } else if (appstate14 == 0) {
            HctUtil.setApplicationEnabled(mContext, packageNameRearPanel, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameRearPanel, true);
        }
        String platform = SystemProperties.get("ro.board.platform");
        if (platform == null || (!platform.startsWith("msm") && !platform.startsWith("trinket"))) {
            HctUtil.setApplicationEnabled(mContext, packageNameContacts, false);
            HctUtil.setApplicationEnabled(mContext, packageNameMMS, SystemProperties.getBoolean("ro.sms.capable", false));
            HctUtil.setApplicationEnabled(mContext, packageNameDialer, SystemProperties.getBoolean("ro.voice.capable", false));
        } else {
            mContext.getResources();
            if (!Resources.getSystem().getBoolean(17891585)) {
                HctUtil.setApplicationEnabled(mContext, packageNameContacts, false);
                HctUtil.setApplicationEnabled(mContext, packageNameMMS, SystemProperties.getBoolean("ro.sms.capable", false));
                HctUtil.setApplicationEnabled(mContext, packageNameDialer, SystemProperties.getBoolean("ro.voice.capable", false));
            } else {
                HctUtil.setApplicationEnabled(mContext, packageNameContacts, true);
                HctUtil.setApplicationEnabled(mContext, packageNameMMS, SystemProperties.getBoolean("ro.sms.capable", true));
                HctUtil.setApplicationEnabled(mContext, packageNameDialer, SystemProperties.getBoolean("ro.voice.capable", true));
            }
        }
        String appalive17 = getParameters("sta_hdmi_in=");
        try {
            appstate15 = Integer.parseInt(appalive17);
        } catch (Exception e17) {
            appstate15 = 0;
        }
        if (appstate15 != 1) {
            HctUtil.setApplicationEnabled(mContext, packageNameHdmiIn, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameHdmiIn, true);
        }
        String appalive18 = getParameters("sta_function=41");
        try {
            appstate16 = Integer.parseInt(appalive18);
        } catch (Exception e18) {
            appstate16 = 0;
        }
        if (appstate16 != 1) {
            HctUtil.setApplicationEnabled(mContext, packageNameLedSettings, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameLedSettings, true);
        }
        if (!isHdmiSwitchSet()) {
            HctUtil.setApplicationEnabled(mContext, packageNameCursorToggle, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameCursorToggle, true);
        }
    }

    private boolean isHdmiSwitchSet() {
        String carbox_hdmi = getParameters("cfg_carbox_hdmi=");
        return "1".equals(carbox_hdmi);
    }

    public boolean initMfiDeviceApp() {
        boolean usbipod = false;
        String carplayRegMode = SystemProperties.get(Constant.ZLINK_REGISTED_MODE, "");
        String easyconnRegisted = SystemProperties.get(Constant.EASYCONN_REGISTED, "");
        String mKMode = getParameters("sta_kmode=");
        String mMfi = getParameters("sta_mfi=");
        String mCarPlayType = getParameters("cfg_factory_part=100,1");
        String mHicar = getParameters("cfg_factory_ext_part=0,1");
        String mEasyConnection = getParameters("cfg_factory_ext_part=1,1");
        if (TextUtils.isEmpty(mKMode)) {
            mKMode = "";
        }
        if (TextUtils.isEmpty(mMfi)) {
            mMfi = "";
        }
        if (TextUtils.isEmpty(mCarPlayType)) {
            mCarPlayType = "";
        }
        if (!mCarPlayType.equals("0") && carplayRegMode.contains("w")) {
            HctUtil.setApplicationEnabled(mContext, packageNameUSBIPOD, false);
        } else if (mEasyConnection.equals("1") && "true".equals(easyconnRegisted)) {
            HctUtil.setApplicationEnabled(mContext, packageNameUSBIPOD, false);
        } else if (mMfi.equals("1") || mMfi.equals("2")) {
            usbipod = true;
            HctUtil.setApplicationEnabled(mContext, packageNameUSBIPOD, true);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameUSBIPOD, false);
        }
        if (mCarPlayType.equals("1")) {
            HctUtil.setApplicationEnabled(mContext, packageNameCARPLAY[0], true);
            if (mHicar.equals("1")) {
                HctUtil.setApplicationEnabled(mContext, packageNameHICAR[0], true);
            } else {
                HctUtil.setApplicationEnabled(mContext, packageNameHICAR[0], false);
            }
            HctUtil.setApplicationEnabled(mContext, packageNameCARPLAY[1], false);
            SystemProperties.set("carplay.type", "1");
        } else if (mCarPlayType.equals("2")) {
            HctUtil.setApplicationEnabled(mContext, packageNameCARPLAY[0], false);
            HctUtil.setApplicationEnabled(mContext, packageNameHICAR[0], false);
            HctUtil.setApplicationEnabled(mContext, packageNameCARPLAY[1], true);
            SystemProperties.set("carplay.type", "2");
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameCARPLAY, false);
            HctUtil.setApplicationEnabled(mContext, packageNameHICAR, false);
        }
        if (mEasyConnection.equals("1")) {
            HctUtil.setApplicationEnabled(mContext, packageNameEASYCONN, true);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameEASYCONN, false);
        }
        String customer = SystemProperties.get("ro.product.customer");
        if ("HZC".equals(customer) && "carplay".equals(mKMode) && "1".equals(mEasyConnection) && ((carplayRegMode.contains("w") || carplayRegMode.contains("l")) && carplayRegMode.contains("a"))) {
            HctUtil.setApplicationEnabled(mContext, packageNameEASYCONN, false);
        }
        return usbipod;
    }

    public void setUsbIpodEnabled(boolean bEnabled) {
        HctUtil.setApplicationEnabled(mContext, packageNameUSBIPOD, bEnabled);
    }

    public void setEasyConnEnabled(boolean bEnabled) {
        HctUtil.setApplicationEnabled(mContext, packageNameEASYCONN, bEnabled);
    }

    private void initAppForHT(int appstate) {
        if (appstate == 2) {
            HctUtil.setApplicationEnabled(mContext, packageNameDAB, true);
            HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_SERIAL_PORT, false);
            HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_USB, false);
        } else if (appstate == 3) {
            HctUtil.setApplicationEnabled(mContext, packageNameDAB, false);
            HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_SERIAL_PORT, true);
            HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_USB, false);
        } else {
            HctUtil.setApplicationEnabled(mContext, packageNameDAB, false);
            HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_SERIAL_PORT, false);
            HctUtil.setApplicationEnabled(mContext, packageNameDAB_YHS_USB, true);
        }
    }
}
