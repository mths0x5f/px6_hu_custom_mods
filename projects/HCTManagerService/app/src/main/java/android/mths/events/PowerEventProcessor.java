package android.mths.events;

import android.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PowerEventProcessor {

    private static final String TAG = "PowerEventProcessor";

    private static final String EVENT_TYPE_POWER_ON = "power_on";
    private static final String EVENT_TYPE_POWER_OFF = "power_off";
    private static final String EVENT_TYPE_ACC_OFF = "acc_off";
    private static final String EVENT_TYPE_SLEEP = "sleep";

    private final PowerEventActions eventActions;

    public void process(@NonNull String type) {
        switch (type) {
            case EVENT_TYPE_POWER_ON -> eventActions.onPowerOn();
            case EVENT_TYPE_POWER_OFF -> eventActions.onPowerOff();
            case EVENT_TYPE_ACC_OFF -> eventActions.onAccOff();
            case EVENT_TYPE_SLEEP -> eventActions.onSleep();
        }
    }

    public void process(@NonNull Bundle eventDetails) {
        String type = eventDetails.getString("type");
        if (type == null) return;
        Log.d(TAG, "Processing PowerEvent type: " + type + ", Details: " + eventDetails);
        process(type);
    }

    public interface PowerEventActions {
        void onPowerOn();
        void onPowerOff();
        void onAccOff();
        void onSleep();
    }

}
