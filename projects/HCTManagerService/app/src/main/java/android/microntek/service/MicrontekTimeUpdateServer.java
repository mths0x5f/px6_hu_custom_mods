package android.microntek.service;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * MicrontekTimeUpdateServer is a service that updates the system time based on GPS data.
 * It listens for location updates and adjusts the system time accordingly.
 * It also manages GPS state based on car power states and user settings.
 */
public class MicrontekTimeUpdateServer extends Service {

    private static final String TAG = "MicrontekTimeUpdateServer";

    private static final String MODE_CHANGING_ACTION = "com.android.settings.location.MODE_CHANGING";
    private static final String NEW_MODE_KEY = "NEW_MODE";

    public static final int POWER_STA_ACC_OFF = 0;
    public static final int POWER_STA_INVALID = -1;
    public static final int POWER_STA_OFF = 1;
    public static final int POWER_STA_ON = 2;

    private static final int SETUP_LOCATION_UPDATES = 0;
    private static final String GPSUPDATETIME_SETTING = "gpsupdatetime";

    public static final String ACTION_GPSAUTOUPDATE = "com.microntek.gpsautoupdate";
    public static final String ACTION_FRESHTIME = "com.microntek.freshtime";

    private boolean videoSpeedEnable;
    private boolean videoSpeedChanged;
    private static int timeUpdateCounter = 0;
    private static int currentPowerState = POWER_STA_ACC_OFF;
    private LocationManager locationManager;
    private boolean isFirstFix = true;
    private CarManager carManager;

    private final Handler gnssHandler = new Handler(Looper.myLooper());
    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SETUP_LOCATION_UPDATES && locationManager == null) {
                setupLocationUpdates();
            }
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider) {
            handler.removeMessages(SETUP_LOCATION_UPDATES);
            handler.sendEmptyMessageDelayed(SETUP_LOCATION_UPDATES, 3000L);
        }
    };

    private int prevSatelliteCount = 0;
    private final GnssStatus.Callback gnssStatusCallBack = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            int satelliteCount = status.getSatelliteCount();
            if (satelliteCount > 0 && Math.abs(prevSatelliteCount - satelliteCount) > 2) {
                timeUpdateCounter = 8;
                prevSatelliteCount = satelliteCount;
            }
        }
    };

    private final ContentObserver gpsAutoContentObserver = new ContentObserver(new Handler(Looper.myLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (shouldGpsUpdateTime()) {
                if (locationManager == null) {
                    isFirstFix = true;
                    setupLocationUpdates();
                }
            } else if (locationManager != null) {
                unregisterLocationUpdates();
            }
        }
    };

    private final BroadcastReceiver updateTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_GPSAUTOUPDATE)) {
                boolean en = intent.getBooleanExtra("en", false);
                if (en || videoSpeedEnable) {
                    if (locationManager == null) {
                        isFirstFix = true;
                        setupLocationUpdates();
                    }
                } else if (locationManager != null) {
                    unregisterLocationUpdates();
                }
            } else if (action.equals(ACTION_FRESHTIME)) {
                context.sendBroadcast(new Intent(Intent.ACTION_TIME_CHANGED));
            }
        }
    };

    private void updateLocation(Location location) {
        if (location != null) {
            if (videoSpeedEnable) {
                final float speed = location.getSpeed() * 3.6f;
                int videoSpeed = Settings.System.getInt(getContentResolver(), "DrivingVideoOverSpeed", 0);
                if (videoSpeed > 0 && speed > videoSpeed) {
                    if (!videoSpeedChanged) {
                        videoSpeedChanged = true;
                        carManager.setVideoOverSpeed(true);
                    }
                } else if (videoSpeedChanged) {
                    videoSpeedChanged = false;
                    carManager.setVideoOverSpeed(false);
                }
            }
            if (timeUpdateCounter > 0) {
                timeUpdateCounter = timeUpdateCounter - 1;
                if (timeUpdateCounter == 0) {
                    final long gpsTime = location.getTime();
                    if (shouldGpsUpdateTime()) {
                        Date date = new Date(gpsTime);
                        Log.i(TAG, "GPS Time: " + date + ", millis: " + gpsTime);
                        SystemClock.setCurrentTimeMillis(gpsTime);
                        Calendar.getInstance().setTime(date);
                        getBaseContext().sendBroadcast(new Intent(Intent.ACTION_TIME_CHANGED));
                        if (this.isFirstFix && locationManager != null) {
                            unregisterLocationUpdates();
                            handler.removeMessages(SETUP_LOCATION_UPDATES);
                            handler.sendEmptyMessageDelayed(SETUP_LOCATION_UPDATES, 20L);
                            isFirstFix = false;
                        }
                    }
                }
            }
        } else {
            timeUpdateCounter = 8;
        }
    }

    private boolean isGpsActivated() {
        return Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) == Settings.Secure.LOCATION_MODE_ON;
    }

    private void activateGps() {
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(NEW_MODE_KEY, Settings.Secure.LOCATION_MODE_ON);
        sendBroadcast(intent, Manifest.permission.WRITE_SECURE_SETTINGS);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_ON);
    }

    private void deactivateGps() {
        Intent intent = new Intent(MODE_CHANGING_ACTION);
        intent.putExtra(NEW_MODE_KEY, Settings.Secure.LOCATION_MODE_OFF);
        sendBroadcast(intent, Manifest.permission.WRITE_SECURE_SETTINGS);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
    }

    private boolean shouldGpsUpdateTime() {
        return Settings.System.getInt(getContentResolver(), GPSUPDATETIME_SETTING, 0) == 1;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        carManager = new CarManager();
        videoSpeedEnable = SystemProperties.get("ro.product.drivingoverspeed", "false").equals("true");
        if (!isGpsActivated()) {
            activateGps();
            handler.sendEmptyMessageDelayed(SETUP_LOCATION_UPDATES, 3000L);
        } else {
            setupLocationUpdates();
        }
        carManager.attach(new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String type = (String) msg.obj;
                Bundle bundle = msg.getData();
                if (type.equals("CarPower")) {
                    String state = bundle.getString("type");
                    handleCarPowerState(state);
                }
            }
        }, "CarPower");

        String powerState = carManager.getStringState("carpower");
        handleCarPowerState(powerState);

        getContentResolver()
                .registerContentObserver(Settings.System.getUriFor(GPSUPDATETIME_SETTING),
                        true, gpsAutoContentObserver, UserHandle.USER_ALL);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GPSAUTOUPDATE);
        filter.addAction(ACTION_FRESHTIME);
        registerReceiver(updateTime, filter);
    }

    private void setAirplaneModeOn(boolean enabled) {
        if (SystemProperties.get("ro.board.platform", "rkXXXX").startsWith("rk")) {
            return;
        }
        final var connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.setAirplaneMode(enabled);
    }

    private void handleCarPowerState(String state) {
        if (state == null) {
            return;
        }
        switch (state) {
            case "sleep" -> {
                handler.removeMessages(SETUP_LOCATION_UPDATES);
                if (locationManager != null) {
                    unregisterLocationUpdates();
                }
                if (isGpsActivated()) {
                    deactivateGps();
                }
                setAirplaneModeOn(true);
                currentPowerState = POWER_STA_INVALID;
            }
            case "power_on" -> {
                if (POWER_STA_OFF != currentPowerState) {
                    setAirplaneModeOn(false);
                }
                currentPowerState = POWER_STA_ON;
                if (!isGpsActivated()) {
                    activateGps();
                }
                handler.removeMessages(SETUP_LOCATION_UPDATES);
                handler.sendEmptyMessageDelayed(SETUP_LOCATION_UPDATES, 3000L);
            }
            case "power_off" -> {
                if (POWER_STA_INVALID == currentPowerState) {
                    setAirplaneModeOn(false);
                }
                currentPowerState = POWER_STA_OFF;
            }
            case "acc_off" -> {
                currentPowerState = POWER_STA_ACC_OFF;
                handler.removeMessages(SETUP_LOCATION_UPDATES);
                if (locationManager != null) {
                    unregisterLocationUpdates();
                }
                if (isGpsActivated()) {
                    deactivateGps();
                }
            }
        }
    }

    private void setupLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.setLocationEnabledForUser(true, UserHandle.CURRENT_OR_SELF);
        }
        if (shouldGpsUpdateTime()) {
            try {
                String bestProvider = locationManager.getBestProvider(getCriteria(), true);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.0f, locationListener);
                locationManager.registerGnssStatusCallback(gnssStatusCallBack, gnssHandler);
                Location location = locationManager.getLastKnownLocation(bestProvider);
                updateLocation(location);
            } catch (SecurityException ignored) {
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize location update callbacks", e);
            }
        }
        timeUpdateCounter = 8;
    }

    private void unregisterLocationUpdates() {
        locationManager.unregisterGnssStatusCallback(gnssStatusCallBack);
        locationManager.removeUpdates(locationListener);
        locationManager = null;
    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        carManager.detach();
        if (locationManager != null) {
            unregisterLocationUpdates();
        }
        unregisterReceiver(updateTime);
        super.onDestroy();
    }

}
