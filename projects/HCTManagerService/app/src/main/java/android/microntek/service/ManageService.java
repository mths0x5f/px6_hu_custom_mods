package android.microntek.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

public class ManageService extends Service {

    private static final String TAG = "ManageService";

    private static final ComponentName CAR_SERVICE_COMPONENT =
            new ComponentName("android.microntek.service", "android.microntek.service.MicrontekServer");
    private static final ComponentName CANBUS_SERVICE_COMPONENT =
            new ComponentName("android.microntek.canbus", "android.microntek.canbus.CanBusServer");
    private static final ComponentName RADIO_SERVICE_COMPONENT =
            new ComponentName("com.microntek.radio", "com.microntek.radio.RadioService");
    private static final ComponentName BLUETOOTH_SERVICE_COMPONENT =
            new ComponentName("android.microntek.mtcser", "android.microntek.mtcser.BlueToothService");
    private static final ComponentName GPSTIME_SERVICE_COMPONENT =
            new ComponentName("android.microntek.service", "android.microntek.service.MicrontekTimeUpdateServer");

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void startCarService() {
        try {
            Intent carServiceIntent = new Intent();
            carServiceIntent.setComponent(CAR_SERVICE_COMPONENT);
            startServiceAsUser(carServiceIntent, UserHandle.SYSTEM);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Car service", e);
        }
    }

    private void startCanBusService() {
        try {
            Intent canBusServiceIntent = new Intent();
            canBusServiceIntent.setComponent(CANBUS_SERVICE_COMPONENT);
            startServiceAsUser(canBusServiceIntent, UserHandle.SYSTEM);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start CanBus service", e);
        }
    }

    private void startRadioService() {
        try {
            Intent radioServiceIntent = new Intent();
            radioServiceIntent.setComponent(RADIO_SERVICE_COMPONENT);
            startServiceAsUser(radioServiceIntent, UserHandle.SYSTEM);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Radio service", e);
        }
    }

    private void startBluetoothService() {
        try {
            Intent bluetoothServiceIntent = new Intent();
            bluetoothServiceIntent.setComponent(BLUETOOTH_SERVICE_COMPONENT);
            startServiceAsUser(bluetoothServiceIntent, UserHandle.SYSTEM);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Bluetooth service", e);
        }
    }

    private void startGpsTimeService() {
        try {
            Intent gpsTimeServiceIntent = new Intent();
            gpsTimeServiceIntent.setComponent(GPSTIME_SERVICE_COMPONENT);
            startServiceAsUser(gpsTimeServiceIntent, UserHandle.SYSTEM);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start GPS Time service", e);
        }
    }

    private void turnOffShowTouches() {
        Settings.System.putInt(getContentResolver(), "show_touches", 0);
    }

    private void startAdbOn() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "start");
        startCarService();
        startBluetoothService();
        startRadioService();
        startCanBusService();
        startGpsTimeService();
        turnOffShowTouches();
        startAdbOn();
    }

}
