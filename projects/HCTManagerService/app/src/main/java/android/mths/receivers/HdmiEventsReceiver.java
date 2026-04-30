package android.mths.receivers;

import static android.mths.Constants.Intent.EXTRA_STATE;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mths.Constants;

public class HdmiEventsReceiver extends BaseReceiver<HdmiEventsReceiver.HdmiEventsListener> {

    public static final IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(Constants.Intent.ACTION_HDMI_PLUGGED);
        INTENT_FILTER.addAction(Constants.Intent.ACTION_EXTERNAL_SHOW_START);
    }

    public HdmiEventsReceiver(Context context, HdmiEventsListener listener) {
        super(context, listener, INTENT_FILTER);
    }

    public interface HdmiEventsListener extends Listener {
        void onHdmiPlugged(final boolean state);
        void onStartExtShowCmd();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        HdmiEventsListener listener = getListener();
        String action = intent.getAction();
        if (action.equals(Constants.Intent.ACTION_HDMI_PLUGGED)) {
            boolean state = intent.getBooleanExtra(EXTRA_STATE, false);
            listener.onHdmiPlugged(state);
        } else if (action.equals(Constants.Intent.ACTION_EXTERNAL_SHOW_START)) {
            listener.onStartExtShowCmd();
        }
    }

}
