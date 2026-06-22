package br.dev.mths.coreservices.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class HdmiEventsReceiver extends BaseReceiver<HdmiEventsReceiver.HdmiEventsListener> {

    private static final IntentFilter INTENT_FILTER;

    private static final String ACTION_HDMI_PLUGGED = "android.intent.action.HDMI_PLUGGED";
    private static final String ACTION_EXTERNAL_SHOW_START_CMD = "com.microntek.extshow.start";

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(ACTION_HDMI_PLUGGED);
        INTENT_FILTER.addAction(ACTION_EXTERNAL_SHOW_START_CMD);
    }

    public HdmiEventsReceiver(Context context, HdmiEventsListener listener) {
        super(context, listener, INTENT_FILTER);
    }

    public interface HdmiEventsListener extends Listener {
        void onHdmiPlugged();
        void onHdmiUnplugged();
        void onExternalShowStartCommand();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        HdmiEventsListener listener = getListener();
        String action = intent.getAction();
        switch (action) {
            case ACTION_HDMI_PLUGGED -> {
                if (intent.getBooleanExtra("state", false)) {
                    listener.onHdmiPlugged();
                } else {
                    listener.onHdmiUnplugged();
                }
            }
            case ACTION_EXTERNAL_SHOW_START_CMD -> listener.onExternalShowStartCommand();
        }
    }

}
