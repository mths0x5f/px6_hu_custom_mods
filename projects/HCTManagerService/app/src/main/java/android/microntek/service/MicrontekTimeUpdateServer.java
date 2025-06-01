package android.microntek.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.microntek.CarManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Date;

public class MicrontekTimeUpdateServer extends Service {
    private static final String MODE_CHANGING_ACTION = "com.android.settings.location.MODE_CHANGING";
    private static final String NEW_MODE_KEY = "NEW_MODE";
    public static final int POWER_STA_ACC_OFF = 0;
    public static final int POWER_STA_INVALID = -1;
    public static final int POWER_STA_OFF = 1;
    public static final int POWER_STA_ON = 2;
    private static Context mContext;
    private boolean videoSpeedChanged;
    private boolean videoSpeedEnable;
    private static int timeUpdateCounter = 0;
    private static int mPowerState = 0;
    private LocationManager locationManager = null;
    private boolean isFirstFix = true;
    protected CarManager mCarManager = null;
    private Toast mToast = null;
    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0 && MicrontekTimeUpdateServer.this.locationManager == null) {
                MicrontekTimeUpdateServer.this.InitLoc();
            }
        }
    };
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            MicrontekTimeUpdateServer.this.updateLocation(location);
        }

        @Override
        public void onProviderDisabled(String arg0) {
            MicrontekTimeUpdateServer.this.updateLocation(null);
        }

        @Override
        public void onProviderEnabled(String arg0) {
            MicrontekTimeUpdateServer.this.mHandler.removeMessages(0);
            MicrontekTimeUpdateServer.this.mHandler.sendEmptyMessageDelayed(0, 3000L);
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    };
    private int mSatelliteCount = 0;
    private final GnssStatus.Callback gnssStatusCallBack = new GnssStatus.Callback() { // from class: android.microntek.service.MicrontekTimeUpdateServer.3
        @Override // android.location.GnssStatus.Callback
        public void onSatelliteStatusChanged(GnssStatus status) {
            int satelliteCount = status.getSatelliteCount();
            if (satelliteCount > 0 && Math.abs(MicrontekTimeUpdateServer.this.mSatelliteCount - satelliteCount) > 2) {
                int unused = MicrontekTimeUpdateServer.timeUpdateCounter = 8;
                MicrontekTimeUpdateServer.this.mSatelliteCount = satelliteCount;
            }
        }
    };
    private final ContentObserver gpsAutoContentObserver = new ContentObserver(new Handler(Looper.myLooper())) { // from class: android.microntek.service.MicrontekTimeUpdateServer.4
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int gpsUpdateTime = Settings.System.getInt(MicrontekTimeUpdateServer.mContext.getContentResolver(), "gpsupdatetime", 0);
            if (gpsUpdateTime == 1) {
                if (MicrontekTimeUpdateServer.this.locationManager == null) {
                    MicrontekTimeUpdateServer.this.isFirstFix = true;
                    MicrontekTimeUpdateServer.this.InitLoc();
                }
            } else if (MicrontekTimeUpdateServer.this.locationManager != null) {
                MicrontekTimeUpdateServer.this.locationManager.unregisterGnssStatusCallback(MicrontekTimeUpdateServer.this.gnssStatusCallBack);
                MicrontekTimeUpdateServer.this.locationManager.removeUpdates(MicrontekTimeUpdateServer.this.locationListener);
                MicrontekTimeUpdateServer.this.locationManager = null;
            }
        }
    };
    private final BroadcastReceiver updateTime = new BroadcastReceiver() { // from class: android.microntek.service.MicrontekTimeUpdateServer.6
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
            if (action.equals("com.microntek.gpsautoupdate")) {
                boolean en = arg1.getBooleanExtra("en", false);
                if (en || MicrontekTimeUpdateServer.this.videoSpeedEnable) {
                    if (MicrontekTimeUpdateServer.this.locationManager == null) {
                        MicrontekTimeUpdateServer.this.isFirstFix = true;
                        MicrontekTimeUpdateServer.this.InitLoc();
                    }
                } else if (MicrontekTimeUpdateServer.this.locationManager != null && !MicrontekTimeUpdateServer.this.videoSpeedEnable) {
                    MicrontekTimeUpdateServer.this.locationManager.unregisterGnssStatusCallback(MicrontekTimeUpdateServer.this.gnssStatusCallBack);
                    MicrontekTimeUpdateServer.this.locationManager.removeUpdates(MicrontekTimeUpdateServer.this.locationListener);
                    MicrontekTimeUpdateServer.this.locationManager = null;
                }
            } else if (action.equals("com.microntek.freshtime")) {
                arg0.sendBroadcast(new Intent("android.intent.action.TIME_SET"));
            }
        }
    };

    private void updateLocation(Location location) {
        LocationManager locationManager;
        int gpsupdatetime = Settings.System.getInt(getContentResolver(), "gpsupdatetime", 0);
        if (location != null) {
            if (this.videoSpeedEnable) {
                float speed = location.getSpeed() * 3.6f;
                int videoSpeed = Settings.System.getInt(mContext.getContentResolver(), "DrivingVideoOverSpeed", 0);
                if (videoSpeed > 0 && speed > videoSpeed) {
                    if (!this.videoSpeedChanged) {
                        this.videoSpeedChanged = true;
                        this.mCarManager.setVideoOverSpeed(true);
                    }
                } else if (this.videoSpeedChanged) {
                    this.videoSpeedChanged = false;
                    this.mCarManager.setVideoOverSpeed(false);
                }
            }
            int i = timeUpdateCounter;
            if (i > 0) {
                int i2 = i - 1;
                timeUpdateCounter = i2;
                if (i2 == 0) {
                    long gpsTime = location.getTime();
                    if (gpsupdatetime == 1) {
                        Date date = new Date(gpsTime);
                        SystemClock.setCurrentTimeMillis(gpsTime);
                        Calendar.getInstance().setTime(date);
                        mContext.sendBroadcast(new Intent("android.intent.action.TIME_SET"));
                        if (this.isFirstFix && (locationManager = this.locationManager) != null) {
                            locationManager.unregisterGnssStatusCallback(this.gnssStatusCallBack);
                            this.locationManager.removeUpdates(this.locationListener);
                            this.locationManager = null;
                            this.mHandler.removeMessages(0);
                            this.mHandler.sendEmptyMessageDelayed(0, 20L);
                            this.isFirstFix = false;
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            return;
        }
        timeUpdateCounter = 8;
    }

    private boolean isGpsOn() {
        int mode = Settings.Secure.getInt(getContentResolver(), "location_mode", 0);
        return mode == 3;
    }

    private void openGps() {
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(NEW_MODE_KEY, 3);
        sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
        Settings.Secure.putInt(getContentResolver(), "location_mode", 3);
    }

    private void closeGps() {
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(NEW_MODE_KEY, 0);
        sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
        Settings.Secure.putInt(getContentResolver(), "location_mode", 0);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        mContext = this;
        this.mCarManager = new CarManager();
        this.videoSpeedEnable = SystemProperties.get("ro.product.drivingoverspeed", "false").equals("true");
        if (!isGpsOn()) {
            openGps();
            this.mHandler.sendEmptyMessageDelayed(0, 3000L);
        } else {
            InitLoc();
        }
        this.mCarManager.attach(new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String type = (String) msg.obj;
                Bundle bundle = msg.getData();
                if (type.equals("CarPower")) {
                    String state = bundle.getString("type");
                    MicrontekTimeUpdateServer.this.DoCarPower(state);
                }
            }
        }, "CarPower");
        String powerstate = this.mCarManager.getStringState("carpower");
        DoCarPower(powerstate);
        getContentResolver().registerContentObserver(Settings.System.getUriFor("gpsupdatetime"), true, this.gpsAutoContentObserver, -1);
        IntentFilter itfl = new IntentFilter();
        itfl.addAction("com.microntek.gpsautoupdate");
        itfl.addAction("com.microntek.freshtime");
        registerReceiver(this.updateTime, itfl);
    }

    private void setAirplaneModeOn(boolean enabled) {
        if (SystemProperties.get("ro.board.platform", "rkXXXX").startsWith("rk") && SystemProperties.get("ro.momdem.chip", "false").equals("false")) {
            return;
        }
        ConnectivityManager mgr = (ConnectivityManager) getSystemService("connectivity");
        mgr.setAirplaneMode(enabled);
    }

    private void DoCarPower(String state) {
        if (state == null) {
            return;
        }
        if (state.equals("sleep")) {
            this.mHandler.removeMessages(0);
            LocationManager locationManager = this.locationManager;
            if (locationManager != null) {
                locationManager.unregisterGnssStatusCallback(this.gnssStatusCallBack);
                this.locationManager.removeUpdates(this.locationListener);
                this.locationManager = null;
            }
            if (isGpsOn()) {
                closeGps();
            }
            setAirplaneModeOn(true);
            mPowerState = -1;
        } else if (state.equals("power_on")) {
            if (1 != mPowerState) {
                setAirplaneModeOn(false);
            }
            mPowerState = 2;
            if (!isGpsOn()) {
                openGps();
            }
            this.mHandler.removeMessages(0);
            this.mHandler.sendEmptyMessageDelayed(0, 3000L);
        } else if (state.equals("power_off")) {
            if (-1 == mPowerState) {
                setAirplaneModeOn(false);
            }
            mPowerState = 1;
        } else if (state.equals("acc_off")) {
            mPowerState = 0;
            this.mHandler.removeMessages(0);
            LocationManager locationManager2 = this.locationManager;
            if (locationManager2 != null) {
                locationManager2.unregisterGnssStatusCallback(this.gnssStatusCallBack);
                this.locationManager.removeUpdates(this.locationListener);
                this.locationManager = null;
            }
            if (isGpsOn()) {
                closeGps();
            }
        }
    }

    private void InitLoc() {
        boolean en = Settings.Secure.isLocationProviderEnabled(getContentResolver(), "gps");
        if (!en) {
            Settings.Secure.setLocationProviderEnabled(getContentResolver(), "gps", true);
        }
        int gpsupdatetime = Settings.System.getInt(getContentResolver(), "gpsupdatetime", 0);
        if (gpsupdatetime == 1) {
            try {
                LocationManager locationManager = (LocationManager) getSystemService("location");
                this.locationManager = locationManager;
                String bestProvider = locationManager.getBestProvider(getCriteria(), true);
                this.locationManager.requestLocationUpdates("gps", 1000L, 0.0f, this.locationListener);
                this.locationManager.registerGnssStatusCallback(this.gnssStatusCallBack);
                Location location = this.locationManager.getLastKnownLocation(bestProvider);
                updateLocation(location);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        timeUpdateCounter = 8;
    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(1);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(1);
        return criteria;
    }

    @Override // android.app.Service
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mCarManager.detach();
        LocationManager locationManager = this.locationManager;
        if (locationManager != null) {
            locationManager.unregisterGnssStatusCallback(this.gnssStatusCallBack);
            this.locationManager.removeUpdates(this.locationListener);
            this.locationManager = null;
        }
        unregisterReceiver(this.updateTime);
        super.onDestroy();
    }
}
