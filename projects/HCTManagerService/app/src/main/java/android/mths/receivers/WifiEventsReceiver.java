package android.mths.receivers;

import static android.net.wifi.WifiManager.EXTRA_NEW_STATE;
import static android.net.wifi.WifiManager.EXTRA_PREVIOUS_WIFI_STATE;
import static android.net.wifi.WifiManager.EXTRA_WIFI_STATE;
import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_AP_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.util.Log;

public class WifiEventsReceiver extends BaseReceiver<WifiEventsReceiver.WifiEventsListener> {

    private static final String TAG = "WifiEventsReceiver";

    public static final IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(WIFI_STATE_CHANGED_ACTION);
        INTENT_FILTER.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        INTENT_FILTER.addAction(SUPPLICANT_STATE_CHANGED_ACTION);
    }

    public WifiEventsReceiver(Context context, WifiEventsListener listener) {
        super(context, listener, INTENT_FILTER);
    }

    public interface WifiEventsListener extends Listener {
        default void onWifiStateChanged(int newState, int oldState) {}
        default void onWifiApStateChanged() {}
        default void onSupplicantStateChanged(SupplicantState newState) {}
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiEventsListener listener = getListener();
        String action = intent.getAction();
        switch (action) {
            case WIFI_STATE_CHANGED_ACTION -> {
                int newState = intent.getIntExtra(EXTRA_WIFI_STATE, WIFI_STATE_UNKNOWN);
                int oldState = intent.getIntExtra(EXTRA_PREVIOUS_WIFI_STATE, WIFI_STATE_UNKNOWN);
                Log.d(TAG, "Wi-Fi state changed to " + newState + " from " + oldState);
                listener.onWifiStateChanged(newState, oldState);
            }
            case WIFI_AP_STATE_CHANGED_ACTION -> listener.onWifiApStateChanged();
            case SUPPLICANT_STATE_CHANGED_ACTION -> {
                SupplicantState newState = intent.getParcelableExtra(EXTRA_NEW_STATE);
                Log.d(TAG, "Supplicant state changed to " + newState);
                listener.onSupplicantStateChanged(newState);
            }
        }
    }

}
