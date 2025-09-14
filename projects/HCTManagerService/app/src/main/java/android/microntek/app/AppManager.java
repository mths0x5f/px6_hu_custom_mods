package android.microntek.app;

import static android.mths95.Constants.Cfg.*;

import android.annotation.NonNull;
import android.content.Context;
import android.microntek.CarManager;
import android.microntek.Constant;
import android.mths95.util.PackageUtils;
import android.os.Build;
import android.os.SystemProperties;

/**
 * AppManager is a singleton class responsible for managing the visibility and state of various
 * applications on the head unit based on configuration parameters.
 */
public final class AppManager {

    private static volatile AppManager INSTANCE;

    private final CarManager carManager;
    private final Context context;

    private AppManager(Context context) {
        this.carManager = new CarManager();
        this.context = context;
    }

    /**
     * Get the singleton instance of AppManager.
     *
     * @param context The application context.
     * @return The singleton instance of AppManager.
     * @throws IllegalStateException if the instance is already initialized with a different context.
     */
    public static AppManager getInstance(Context context) {
        AppManager appManager;
        synchronized (AppManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new AppManager(context.getApplicationContext());
            } else if (INSTANCE.context != context.getApplicationContext()) {
                throw new IllegalStateException("AppManager already initialized with a different context");
            }
            appManager = INSTANCE;
        }
        return appManager;
    }

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
    private static final String[] packageNameDVR = {Constant.RECPACKAGE};
    private static final String[] packageNameCvbsDVR = {"com.microntek.dvr"};
    private static final String[] packageNameTpms = {"com.microntek.tpms"};
    private static final String[] packageNameHdmi = {"com.microntek.hdmi"};
    private static final String[] packageNameEASYCONN = {Constant.EASYCONNECTEDPACKAGE};
    private static final String[] packageNameCARPLAY = {Constant.ZLINKPACKAGE, Constant.SPEEDPLAYPACKAGE};
    private static final String[] packageNameHICAR = {"com.huawei.hicar"};
    private static final String[] packageNameDAB = {"com.microntek.dab"};
    private static final String[] packageNameDAB_YHS_USB = {"com.ex.dabplayer.pad"};
    private static final String[] packageNameDAB_YHS_SERIAL_PORT = {"com.ex.yhxdabw8.pad"};
    private static final String[] packageNameHdmiIn = {"com.microntek.hdmiin"};
    private static final String[] packageNameLedSettings = {"com.microntek.ledsettings"};
    private static final String[] packageNameCursorToggle = {"com.microntek.cursortoggle"};

    private int getIntStateParameter(@NonNull String param) {
        if (!param.endsWith("=") && !param.contains("=")) param += "=";
        return carManager.getIntState(param);
    }

    /**
     * Update the visibility of head unit applications based on configuration parameters.
     * This method checks various configuration settings and enables or disables
     * corresponding applications accordingly.
     */
    public void updateHeadUnitAppVisibility() {

        disableTelephonyApps();
        updateWheelKeysStudyAppEnabledState();
        updateTVAppEnabledState();
        updateIpodAppEnabledState();
        updateDvdAppEnabledState();
        updatePanelLedAppEnabledState();
        updateBluetoothAppsEnabledState();
        updateFrontCameraAppEnabledState();
        updateRadioAppEnabledState();
        updateDvrAppEnabledState();
        updateTpmsAppEnabledState();
        updateSyncAppEnabledState();
        updateHdmiAppEnabledState();
        updateAvInAppEnabledState();
        updateDabAppEnabledState();
        updateRearPanelAppEnabledState();
        updateHdmiInAppEnabledState();
        updateLedSettingsAppEnabledState();
        updateHdmiCursorAppEnabledState();

    }

    /**
     * Disable telephony-related applications such as Contacts, MMS, and Dialer.
     * This is typically done when the head unit is not configured for telephony features.
     */
    private void disableTelephonyApps() {
        PackageUtils.setApplicationEnabled(context, packageNameContacts, false);
        PackageUtils.setApplicationEnabled(context, packageNameMMS, false);
        PackageUtils.setApplicationEnabled(context, packageNameDialer, false);
    }

    /**
     * Update the enabled state of the Wheel Keys Study application based on configuration.
     * The application is enabled if the CFG_WHEELSTUDY_TYPE parameter is non-zero.
     */
    private void updateWheelKeysStudyAppEnabledState() {
        final var value = getIntStateParameter(CFG_WHEELSTUDY_TYPE);
        PackageUtils.setApplicationEnabled(context, packageNameWheelStudy, value != 0);
    }

    /**
     * Update the enabled state of the TV application based on configuration.
     * The application is enabled if the CFG_DTV parameter is non-zero.
     */
    private void updateTVAppEnabledState() {
        final var value = getIntStateParameter(CFG_DTV);
        PackageUtils.setApplicationEnabled(context, packageNameTV, value != 0);
    }

    /**
     * Update the enabled state of the iPod applications based on configuration.
     * Depending on the STA_MFI and CFG_IPOD parameters, either the USB iPod app or the standard iPod app is enabled.
     */
    private void updateIpodAppEnabledState() {
        var value = getIntStateParameter(STA_MFI);
        if (value != 1 && value != 2) {
            value = getIntStateParameter(CFG_IPOD);
            PackageUtils.setApplicationEnabled(context, packageNameUSBIPOD, false);
            PackageUtils.setApplicationEnabled(context, packageNameIPOD, value != 0);
        } else {
            PackageUtils.setApplicationEnabled(context, packageNameUSBIPOD, true);
            PackageUtils.setApplicationEnabled(context, packageNameIPOD, false);
        }
    }

    /**
     * Update the enabled state of the DVD application based on configuration.
     * The application is enabled if the CFG_DVD parameter is non-zero.
     */
    private void updateDvdAppEnabledState() {
        final var value = getIntStateParameter(CFG_DVD);
        PackageUtils.setApplicationEnabled(context, packageNameDVD, value != 0);
    }

    /**
     * Update the enabled state of the Panel LED application based on configuration.
     * The application is enabled if the CFG_LED_TYPE parameter is non-zero and less than 3.
     */
    private void updatePanelLedAppEnabledState() {
        final var value = getIntStateParameter(CFG_LED_TYPE);
        PackageUtils.setApplicationEnabled(context, packageNameRGBKEY, value != 0 && value < 3);
    }

    /**
     * Update the enabled state of Bluetooth-related applications based on configuration.
     * The applications are enabled if the CFG_BT parameter is non-zero.
     */
    private void updateBluetoothAppsEnabledState() {
        final var value = getIntStateParameter(CFG_BT);
        PackageUtils.setApplicationEnabled(context, packageNameBT, value != 0);
    }

    /**
     * Update the enabled state of the Front Camera application based on configuration.
     * The application is enabled if the CFG_FRONTVIEW parameter is non-zero.
     */
    private void updateFrontCameraAppEnabledState() {
        final var value = getIntStateParameter(CFG_FRONTVIEW);
        PackageUtils.setApplicationEnabled(context, packageNameFrontView, value != 0);
    }

    /**
     * Update the enabled state of the Radio application based on configuration.
     * The application is disabled if the CFG_RADIO parameter equals 1.
     */
    private void updateRadioAppEnabledState() {
        final var value = getIntStateParameter(CFG_RADIO);
        PackageUtils.setApplicationEnabled(context, packageNameRadio, value != 1);
    }

    /**
     * Update the enabled state of the DVR applications based on configuration.
     * The state is determined by the CFG_DVR parameter, which can enable or disable
     * different combinations of the DVR and CVBS DVR applications.
     */
    private void updateDvrAppEnabledState() {
        final var value = getIntStateParameter(CFG_DVR);
        if (value == 0) {
            PackageUtils.setApplicationEnabled(context, packageNameDVR, true);
            PackageUtils.setApplicationEnabled(context, packageNameCvbsDVR, false);
        } else if (value == 1) {
            PackageUtils.setApplicationEnabled(context, packageNameDVR, false);
            PackageUtils.setApplicationEnabled(context, packageNameCvbsDVR, true);
        } else if (value != 2) {
            PackageUtils.setApplicationEnabled(context, packageNameDVR, false);
            PackageUtils.setApplicationEnabled(context, packageNameCvbsDVR, false);
        } else {
            PackageUtils.setApplicationEnabled(context, packageNameDVR, true);
            PackageUtils.setApplicationEnabled(context, packageNameCvbsDVR, true);
        }
    }

    /**
     * Update the enabled state of the TPMS application based on configuration.
     * The application is enabled if the CFG_TPMS parameter is non-zero.
     */
    private void updateTpmsAppEnabledState() {
        final var value = getIntStateParameter(CFG_TPMS);
        PackageUtils.setApplicationEnabled(context, packageNameTpms, value != 0);
    }

    /**
     * Update the enabled state of the SYNC application based on configuration.
     * The application is enabled if the CFG_FACTORY_PART_98_1 parameter is non-zero.
     */
    private void updateSyncAppEnabledState() {
        final var value = getIntStateParameter(CFG_FACTORY_PART_98_1);
        PackageUtils.setApplicationEnabled(context, packageNameSYNC, value != 0);
    }

    /**
     * Update the enabled state of the HDMI application based on configuration.
     * The application is enabled if the STA_FUNCTION_30 parameter equals 1.
     */
    private void updateHdmiAppEnabledState() {
        final var value = getIntStateParameter(STA_FUNCTION_30);
        PackageUtils.setApplicationEnabled(context, packageNameHdmi, value == 1);
    }

    /**
     * Update the enabled state of the AV-IN application based on configuration.
     * The application is disabled if the CFG_FACTORY_PART_104_1 parameter equals 1.
     */
    private void updateAvInAppEnabledState() {
        final var value = getIntStateParameter(CFG_FACTORY_PART_104_1);
        PackageUtils.setApplicationEnabled(context, packageNameAVIN, value != 1);
    }

    /**
     * Update the enabled state of the DAB applications based on configuration.
     * The state is determined by the CFG_DAB parameter, which can enable or disable
     * different combinations of the main DAB application and its variants.
     */
    private void updateDabAppEnabledState() {
        final var value = getIntStateParameter(CFG_DAB);
        PackageUtils.setApplicationEnabled(context, packageNameDAB, value == 2);
        if (value == 3) {
            PackageUtils.setApplicationEnabled(context, packageNameDAB_YHS_SERIAL_PORT, true);
            PackageUtils.setApplicationEnabled(context, packageNameDAB_YHS_USB, false);
        } else {
            PackageUtils.setApplicationEnabled(context, packageNameDAB_YHS_USB, true);
            PackageUtils.setApplicationEnabled(context, packageNameDAB_YHS_SERIAL_PORT, false);
        }
    }

    /**
     * Update the enabled state of the Rear Panel application based on configuration.
     * The application is enabled if the CFG_REAR_PANEL parameter is non-zero,
     * except on Android 10 and above where it is always disabled.
     */
    private void updateRearPanelAppEnabledState() {
        final var value = getIntStateParameter(CFG_REAR_PANEL);
        String[] versionParts = Build.VERSION.RELEASE.split("\\.");
        int majorVersion = Integer.parseInt(versionParts[0]);
        if (majorVersion >= 10) {
            PackageUtils.setApplicationEnabled(context, packageNameRearPanel, false);
        } else PackageUtils.setApplicationEnabled(context, packageNameRearPanel, value != 0);
    }

    /**
     * Update the enabled state of the HDMI-IN application based on configuration.
     * The application is enabled if the STA_HDMI_IN parameter equals 1.
     */
    private void updateHdmiInAppEnabledState() {
        final var value = getIntStateParameter(STA_HDMI_IN);
        PackageUtils.setApplicationEnabled(context, packageNameHdmiIn, value == 1);
    }

    /**
     * Update the enabled state of the LED Settings application based on configuration.
     * The application is enabled if the STA_FUNCTION_41 parameter equals 1.
     */
    private void updateLedSettingsAppEnabledState() {
        final var value = getIntStateParameter(STA_FUNCTION_41);
        PackageUtils.setApplicationEnabled(context, packageNameLedSettings, value == 1);
    }

    /**
     * Update the enabled state of the HDMI Cursor Toggle application based on configuration.
     * The application is enabled if the CFG_CARBOX_HDMI parameter equals 1.
     */
    private void updateHdmiCursorAppEnabledState() {
        final var value = getIntStateParameter(CFG_CARBOX_HDMI);
        PackageUtils.setApplicationEnabled(context, packageNameCursorToggle, value == 1);
    }

    /**
     * Initialize the MFi device application based on configuration parameters.
     * This method checks the STA_MFI and CFG_FACTORY_PART_100_1 parameters to determine
     * which applications to enable or disable, including USB iPod and CarPlay apps.
     *
     * @return true if the USB iPod application is enabled, false otherwise.
     */
    public boolean initMfiDeviceApp() {
        boolean isUSBIpodEnabled = false;
        String carplayRegMode = SystemProperties.get(Constant.ZLINK_REGISTED_MODE, "");
        final int mfiState = getIntStateParameter(STA_MFI);
        final int carPlayType = getIntStateParameter(CFG_FACTORY_PART_100_1);

        if (carPlayType != 0 && carplayRegMode.contains("w")) {
            PackageUtils.setApplicationEnabled(context, packageNameUSBIPOD, false);
        } else if (mfiState == 1 || mfiState == 2) {
            isUSBIpodEnabled = true;
            PackageUtils.setApplicationEnabled(context, packageNameUSBIPOD, true);
        } else {
            PackageUtils.setApplicationEnabled(context, packageNameUSBIPOD, false);
        }
        if (carPlayType == 1) {
            PackageUtils.setApplicationEnabled(context, packageNameCARPLAY[0], true);
            PackageUtils.setApplicationEnabled(context, packageNameHICAR[0], false);
            PackageUtils.setApplicationEnabled(context, packageNameCARPLAY[1], false);
            SystemProperties.set("carplay.type", "1");
        } else if (carPlayType == 2) {
            PackageUtils.setApplicationEnabled(context, packageNameCARPLAY[0], false);
            PackageUtils.setApplicationEnabled(context, packageNameHICAR[0], false);
            PackageUtils.setApplicationEnabled(context, packageNameCARPLAY[1], true);
            SystemProperties.set("carplay.type", "2");
        } else {
            PackageUtils.setApplicationEnabled(context, packageNameCARPLAY, false);
            PackageUtils.setApplicationEnabled(context, packageNameHICAR, false);
        }
        PackageUtils.setApplicationEnabled(context, packageNameEASYCONN, false);
        return isUSBIpodEnabled;
    }

    /**
     * Enable or disable the USB iPod application.
     *
     * @param enabled true to enable, false to disable
     */
    public void setUsbIpodEnabled(boolean enabled) {
        PackageUtils.setApplicationEnabled(context, packageNameUSBIPOD, enabled);
    }

}
