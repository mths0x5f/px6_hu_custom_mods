package android.microntek.service;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.microntek.app.SystemSettingsAccessor.SET_SYS_UPDATE_ACC_DELAY_MODE;
import static android.microntek.app.SystemSettingsAccessor.SET_SYS_UPDATE_RIGHT_VIEW_MODE;
import static android.microntek.app.SystemSettingsAccessor.SET_SYS_VIDEO_WHILE_DRIVING;
import static android.mths.Constants.App.REC_DVR_APP;
import static android.mths.Constants.App.VIDEO_APP;
import static android.mths.Constants.App.ZLINK_APP;
import static android.mths.Constants.*;
import static android.mths.Constants.Intent.ACTION_CANBUS_TELEPHONY;
import static android.mths.Constants.Message.*;
import static android.mths.Constants.Message.MSG_GPS_BACK;
import static android.mths.Constants.Message.MSG_GPS_TOP;
import static android.mths.Constants.Message.MSG_TIME_FRESH;
import static android.mths.Constants.SystemProperty.SYS_PROP_STARTUP_APPS_ENABLE;
import static android.mths.Constants.SystemProperty.SYS_PROP_THEME_MODE_DEFAULT;
import static android.mths.Constants.SystemProperty.SYS_PROP_VIDEO_WHILE_DRIVING;
import static android.mths.Constants.SystemSetting.SET_SYS_LAUNCHED_STARTUP_APPS;
import static android.mths.Constants.SystemSetting.SET_SYS_NAVI_APP_AUTORUN;
import static android.mths.Constants.SystemSetting.SET_SYS_STARTUP_APPS;
import static android.mths.Constants.SystemSetting.SET_SYS_THEME_MODE;
import static android.mths.Constants.SystemSetting.SET_SYS_UPDATE_THEME;
import static android.mths.Constants.SystemSetting.SET_SYS_ZLINK_LAST_CLASSNAME;

import android.annotation.Nullable;
import android.app.ActivityOptions;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.microntek.Constant;
import android.microntek.actions.CarEventActions;
import android.microntek.app.SystemSettingsAccessor;
import android.microntek.broadcast.SystemEventBroadcaster;
import android.microntek.handlers.CarManagerEventHandler;
import android.microntek.handlers.processors.CarEventProcessor;
import android.microntek.launcher.AppLauncher;
import android.microntek.listeners.CarEventListener;
import android.microntek.mfi.MfiManager;
import android.microntek.app.SystemPropertiesAccessor;
import android.microntek.receivers.ScreenClockReceiver;
import android.microntek.state.State;
import android.mths.Constants;
import android.mths.util.MicrontekCarManager;
import android.mths.util.SystemUtils;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewMicrontekServer extends MicrontekServer
        implements CarManagerEventHandler.ServiceCallback, CarEventListener {

    private State state;
    private SystemEventBroadcaster systemEventBroadcaster;
    private AppLauncher appLauncher;

    private MicrontekCarManager carManager;
    private CarManagerEventHandler carManagerEventHandler;

    private CarEventActions carEventActions;
    private CarEventProcessor carEventProcessor;

    private MfiManager mfiManager;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private AudioManager audioManager;
    private AppOpsManager appOpsManager;

    private SystemSettingsAccessor settings;
    private SystemPropertiesAccessor properties;

    private ContentObserver drivingVideoSettingsObserver =
            new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    updateDrivingVideoRestrictions();
                }
            };

    private ContentObserver y =
            new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    //MicrontekServiceBase.this.updateAccDelayMode();
                }
            };

    private ContentObserver z =
            new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    //MicrontekServiceBase.this.updateRightViewMode();
                }
            };

    private ContentObserver a =
            new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    //MicrontekServiceBase.this.checkAppUpdata(false);
                }
            };

    // DONE!
    private final ScreenClockReceiver screenClockReceiver =
            new ScreenClockReceiver(this, (value) -> {
                if (value) {
                    state.getFlags().setScreensaverEnableLocal(true);
                    state.getFlags().setScreensaverEnable(true);
                } else {
                    state.getFlags().setScreensaverEnableLocal(false);
                    state.getFlags().setScreensaverEnable(false);
                }
                state.getValues().setScreensaverTimer(0);
            });

    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME_FRESH -> { tickTask(); }
                case MSG_GPS_TOP -> {
                    initGps();
                }
                case MSG_GPS_BACK -> {}
                case MSG_DEVICE_LOCK -> {}
                case MSG_MUSIC_CLOCK -> {}
                case MSG_PROMPT_OFF -> {}
                case MSG_TOUCH_UPDATE -> {}
                case MSG_POWEROFF_CMD -> {}
                case MSG_POWER_DIALOG -> {}
                case MSG_VENDING_ENABLE -> {}
                case MSG_VOL_DOWN -> {}
                case MSG_VOL_UP -> {}
                case MSG_WIFI_AP_CHECK -> {}
                case MSG_UPDATE_LAUNCHER -> {}
                case MSG_DUAL_MODE -> {}
                case MSG_DUAL_CNTCLEAR -> {}
                case MSG_DUAL_HOME -> {}
                case MSG_SINGLE_HOME -> {}
                case MSG_HIDE_YH_LOGO -> { /*noop*/ }
                case MSG_GO_DRIVING_ASSISTANCE -> { /*noop*/ }
                case MSG_AV_REFRESH -> {}
                case MSG_PWR_SCREEN -> {}
                case MSG_COPY_STATE -> {}
                case MSG_COPY_OK -> {}
            }
        }
    };


    @Override
    public void onCreate() {
        state = State.getInstance();
        systemEventBroadcaster = new SystemEventBroadcaster(this, state);
        appLauncher = new AppLauncher(this, state);

        carManager = new MicrontekCarManager();
        carManagerEventHandler = new CarManagerEventHandler(getMainLooper(), this);
        carManager.attach(carManagerEventHandler, CarManagerEventHandler.ALL_EVENT_TYPES);

        carEventActions = new CarEventActions(this, state);
        carEventActions.setListener(this);
        carEventProcessor = new CarEventProcessor(carEventActions);

        mfiManager = new MfiManager();
        wifiManager = getSystemService(WifiManager.class);
        connectivityManager = getSystemService(ConnectivityManager.class);
        audioManager = getSystemService(AudioManager.class);
        appOpsManager = getSystemService(AppOpsManager.class);

        init();
        /*InitData();
        InitSystemData();
        registerBroadcastReceivers();*/

        if (settings.isAirplaneModeEnabled()) {
            settings.setAirplaneModeEnabled(false);
        }
    }

    private void init() {
        settings = new SystemSettingsAccessor(this, carManager);
        properties = new SystemPropertiesAccessor(this, carManager);

        reverseCheckOnServiceStart();

        var backlightLevel = properties.getScreenBacklightLevel();
        settings.setScreenBrightnessLevel(backlightLevel);

        var rearviewVolume = settings.getRearviewVolume();
        properties.setRearviewVolume(rearviewVolume);

        var maxBootVolume = properties.getMaxVolumeOnBoot();
        var phoneCallVolume = settings.getPhoneCallVolumeOr(maxBootVolume);
        var generalVolume = settings.getGeneralVolumeOr(maxBootVolume);
        settings.setPhoneCallVolume(Math.min(phoneCallVolume, maxBootVolume));
        settings.setGeneralVolume(Math.min(generalVolume, maxBootVolume));

        // MTCAdjVolume(2);

        var defaultGpsPhoneVolume = properties.getGpsPhoneVolume();
        var gpsPhoneVolume = settings.getGpsPhoneVolumeOr(defaultGpsPhoneVolume);
        properties.setGpsPhoneVolume(gpsPhoneVolume);

        ContentResolver cr = getContentResolver();
        cr.registerContentObserver(Settings.System.getUriFor(SET_SYS_VIDEO_WHILE_DRIVING),
                false, drivingVideoSettingsObserver);
        updateDrivingVideoRestrictions();

        cr.registerContentObserver(Settings.System.getUriFor(SET_SYS_UPDATE_ACC_DELAY_MODE),
                false, y);
        //MicrontekServiceBase.this.updateAccDelayMode();

        cr.registerContentObserver(Settings.System.getUriFor(SET_SYS_UPDATE_RIGHT_VIEW_MODE),
                false, z);
        // MicrontekServiceBase.this.updateRightViewMode();

        cr.registerContentObserver(Settings.System.getUriFor("hctapkupdata"),
                true, a, UserHandle.USER_ALL);
        // MicrontekServiceBase.this.checkAppUpdata(true);
    }

    /**
     * Checks if the vehicle is in reverse gear upon service startup.
     * <p>
     * Some head units boot up while still reporting the reverse gear as engaged,
     * even if it is not. This method starts a background thread that continuously
     * polls the reverse gear state. It waits until the system no longer reports
     * being in reverse. Once the state is cleared, it sets a system property
     * to reflect that the reverse check is complete. This prevents premature
     * actions that might be triggered by a false positive reverse state during boot.
     */
    private void reverseCheckOnServiceStart() {
        new Thread(() -> {
            while (properties.isInReverseOnBoot()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignored) {}
            }
            properties.setSysPropOnReverse();
        }).start();
    }

    private boolean isUsbIpodConnected() {
        if (mfiManager == null) return false;
        return mfiManager.isConnected();
    }

    @Override
    public void onDestroy() {
//        this.mfiManager.unregisterListener(this.mfiListener);
        carManager.detach();
        carManagerEventHandler.removeCallbacksAndMessages(null);
        //unregisterBroadcastReceivers();
        getContentResolver().unregisterContentObserver(drivingVideoSettingsObserver);
        getContentResolver().unregisterContentObserver(y);
        getContentResolver().unregisterContentObserver(z);
        getContentResolver().unregisterContentObserver(a);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* Handler methods ========================================================================== */
    public void tickTask() {
        var screensaverTimeout = -1;
        if (state.getFlags().isScreensaverEnableLocal()) {
            screensaverTimeout = settings.getScreensaverTimeout();
        }
        if (/*btLock ||
            gpsIsFront ||
            this.backviewState ||
            this.powerState != 2 ||
           */
            screensaverTimeout <= 0 ||
            !state.getFlags().isScreensaverEnable()) {
            state.getValues().setScreensaverTimer(0);
        }
        if (state.getValues().getScreensaverTimer() == 0) {
            if (screensaverTimeout > 0) {
                //sendBroadcastAsUser(new Intent(Constant.CLOCKEND), UserHandle.CURRENT_OR_SELF);
            }
            state.getFlags().setScreensaverOn(false);
        } else if(!state.getFlags().isScreensaverOn() &&
                  state.getValues().getScreensaverTimer() >= screensaverTimeout) {
            state.getFlags().setScreensaverOn(true);
            if (state.getFlags().isScreensaverEnableLocal()) {
                //startMusicClock();
            } else {
                Intent it1 = new Intent(Constant.MSG_MTC_SCREENSAVER);
                it1.putExtra("timer", state.getValues().getScreensaverTimer());
                sendBroadcastAsUser(it1, UserHandle.CURRENT_OR_SELF);
            }
        }
        state.getValues().incrementScreensaverTimer();
        mainHandler.sendEmptyMessageDelayed(0, 1000L);

        Intent intent = new Intent(Constant.MSG_MTC_TIME_FRESH);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    public void initGps() { // TODO revisar estes valores com o comportamento observado, está estranho
        var defaultNavAppPackageName = properties.getDefaultNavAppPackageName();
        var navAppPackageName = settings.getNavAppPackageName();
        properties.setNavAppPackage(navAppPackageName.isEmpty() ?
                                    defaultNavAppPackageName :
                                    navAppPackageName);

        carManager.setParameter(Cfg.MTCGPSMONITOR, settings.getGpsMonitorState());
        carManager.setParameter(Cfg.MTCGPSSWITCH, settings.getGpsSwitchState());
        carManager.setParameter(Cfg.MTCGPSGAIN, settings.getGpsGainState());
        carManager.setParameter(Cfg.MTCGPSBACKVOL, settings.getGpsBackVolume());
    }

    /* ========================================================================================== */

    @Override
    public void handleCarEvent(Bundle bundle) {
        Log.d(TAG, "CarEvent received, delegating to CarEventProcessor.");
        carEventProcessor.process(bundle);
    }

    @Override
    public void handleCarPower(String state) {

    }

    @Override
    public void handleKeyDown(Bundle bundle) {

    }

    @Override
    public void handleCarApp(Bundle bundle) {

    }

    @Override
    public void handleCarBox(Bundle bundle) {

    }

    @Override
    public void setDurationTimeInternal(long time) {

    }

    /* ============= Listening car events ========================================== */

    @Override
    public void onHandbrakeStateChanged(boolean isActive) {
        systemEventBroadcaster.broadcastHandbrakeState();
    }

    public void updateDrivingVideoRestrictions() {
        boolean isDriving = state.getVehicleSignals().isDrivingState();
        boolean isVideoPlaybackAllowedBySetting =
                Settings.System.getInt(getContentResolver(), SET_SYS_VIDEO_WHILE_DRIVING, 0) == 1;

        boolean restrictVideoPlayback = isDriving && !isVideoPlaybackAllowedBySetting;

        SystemProperties.set(SYS_PROP_VIDEO_WHILE_DRIVING, restrictVideoPlayback ? "1" : "0");
    }

    @Override
    public void onDrivingStateChanged(boolean isDriving) {
        updateDrivingVideoRestrictions();
    }

    private void updateThemeBasedOnHeadlightStateAndUserPreference() { // todo review
        int defaultThemeMode = SystemProperties.getInt(SYS_PROP_THEME_MODE_DEFAULT, 2);
        int themeMode = Settings.System.getInt(getContentResolver(), SET_SYS_THEME_MODE, defaultThemeMode);
        if (themeMode == 0) { // automatic
            Settings.System.putInt(getContentResolver(), SET_SYS_UPDATE_THEME, state.getVehicleSignals().isHeadlightOn() ? 0 : 1);
        } else if (themeMode == 1) { // light theme
            Settings.System.putInt(getContentResolver(), SET_SYS_UPDATE_THEME, 1);
        }
    }

    @Override
    public void onHeadlightStateChanged(boolean isOn) {
        this.updateThemeBasedOnHeadlightStateAndUserPreference();
        this.systemEventBroadcaster.broadcastHeadlightState();
    }

    @Override
    public void onReverseGearStateChanged(boolean isEngaged) {
        this.systemEventBroadcaster.broadcastReverseGearState();

        if (state.getVehicleSignals().isReverseGearEngaged()) {
            appLauncher.launchRearCameraApp();
        } else {
            if (state.getFlags().isNavigationLaunchPending()) {
                if (SystemUtils.isGpsCardMounted(this)) {
                    state.getFlags().setNavigationLaunchPending(false);
                    launchApp(settings.getNavAppPackageName());
                }
            } else if (state.getFlags().isAppLaunchPending()) {
                state.getFlags().setAppLaunchPending(false);
                launchStartupApps();
            }
        }
    }

    // todo decidir se deve estar aqui
    private void launchApp(@Nullable String packageName) {
        if (packageName == null) return;

        String navAppPackageName = settings.getNavAppPackageName();

        if (packageName.equals(navAppPackageName)) {
            boolean isDeviceLockedAndGpsCardMissing =
                    state.getFlags().isDeviceLocked() &&
                    !SystemUtils.isGpsCardMounted(this) &&
                    mLastHasGpsCard;

            if (isDeviceLockedAndGpsCardMissing || backviewState) {
                state.getFlags().setNavigationLaunchPending(true);
            }
        }

        switch (packageName) {
            case Constants.Package.DVD_APP -> appLauncher.launchDvdApp(0);
            case Constants.Package.TV_APP -> appLauncher.launchTvApp(0);
            case Constants.Package.BLUETOOTH_APP -> appLauncher.launchBluetoothApp(0);
            case Constants.Package.IPOD_APP -> appLauncher.launchIpodApp(0);
            case Constants.Package.USB_IPOD_APP -> {
                if (this.mUsbIpod) {
                    appLauncher.launchUsbIpodApp(0);
                } else {
                    this.isRunUsbIpod = true;
                }
            }
            default -> {
                try {
                    Intent it = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (it != null && !packageName.startsWith(Constants.Package.LAUNCHER_APP)) {
                        if (packageName.equals(navAppPackageName) &&
                                it.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                            it.removeCategory(Intent.CATEGORY_LAUNCHER);
                        }
                        it.addFlags(FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        ActivityOptions options = ActivityOptions.makeBasic();
                        options.setLaunchDisplayId(0);
                        if ("1".equals(carManager.getParameter(Cfg.CFG_CARBOX_HDMI))) {
                            startActivity(it, options.toBundle());
                        } else {
                            startActivityAsUser(it, UserHandle.CURRENT_OR_SELF);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "App " + packageName + " could not be started", e);
                }
            }
        }
    }

    // todo decidir se deve estar aqui
    private void launchStartupApps() {
        if (SystemProperties.get(SYS_PROP_STARTUP_APPS_ENABLE, "true").equals("false")) return;

        final var appsToLaunch = Settings.System.getString(getContentResolver(), SET_SYS_STARTUP_APPS);
        Settings.System.putInt(getContentResolver(), SET_SYS_LAUNCHED_STARTUP_APPS, 1);

        final var gpsAutoRun = Settings.System.getString(getContentResolver(), SET_SYS_NAVI_APP_AUTORUN);

        final var navAppPackageName = settings.getNavAppPackageName();

        if (TextUtils.isEmpty(appsToLaunch)) {
            if ("on".equals(gpsAutoRun) && !TextUtils.isEmpty(navAppPackageName)) {
                launchApp(navAppPackageName);
            }
        } else {
            String[] packageTokens = appsToLaunch.split(",");
            long launchDelay = 0L;
            boolean shouldLaunchNaviApp = "on".equals(gpsAutoRun);
            for (String token : packageTokens) {
                if (token == null || "null".equalsIgnoreCase(token)) continue;

                if (REC_DVR_APP.getPackageName().equals(token)) {
                    launchDelay += 1_000L;
                    mainHandler.postDelayed(() -> appLauncher.launchedRecDvrApp(1), launchDelay);
                    launchDelay = 1_000L;
                } else if (ZLINK_APP.getPackageName().equals(token)) {
                    String zlinkClassName = Settings.System.getString(getContentResolver(), SET_SYS_ZLINK_LAST_CLASSNAME);
                    mainHandler.postDelayed(() -> appLauncher.startZlink(zlinkClassName), launchDelay);
                    launchDelay = 500L;
                } else if (token.equals(navAppPackageName)) {
                    shouldLaunchNaviApp = true;
                } else {
                    if (token.equals(VIDEO_APP.getPackageName())) {
                        launchDelay += 1_500L;
                    }
                    mainHandler.postDelayed(() -> launchApp(token), launchDelay);
                    launchDelay = 500L;
                }
            }
            if (shouldLaunchNaviApp && !TextUtils.isEmpty(navAppPackageName)) {
                mainHandler.postDelayed(() -> launchApp(navAppPackageName), launchDelay);
            }
            if (backviewState) { // todo
                state.getFlags().setAppLaunchPending(true);
            }
        }
    }

    @Override
    public void onCanbusTelephonyStateChanged(boolean isActive) {
        this.systemEventBroadcaster.broadcastCanbusTelephonyState();
        if (isActive) {
            Intent intent = new Intent();
            intent.setComponent(App.CANBUS_TELEPHONY_VIEW.getComponentName());
            startServiceAsUser(intent, UserHandle.OWNER);
        } else {
            // this broadcast notify the canbus service to stop telephony integration
            sendBroadcastAsUser(new Intent(ACTION_CANBUS_TELEPHONY), UserHandle.CURRENT_OR_SELF);
        }
    }
}
