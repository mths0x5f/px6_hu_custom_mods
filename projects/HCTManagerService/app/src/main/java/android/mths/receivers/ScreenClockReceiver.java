package android.mths.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mths.Constants;

public class ScreenClockReceiver extends BaseReceiver<ScreenClockReceiver.ScreenClockListener> {

    public static final IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(Constants.Intent.ACTION_CHANGE_SCREEN_CLOCK);
    }

    public ScreenClockReceiver(Context context, ScreenClockListener listener) {
        super(context, listener, INTENT_FILTER);
    }

    public interface ScreenClockListener extends Listener {
        void onScreenClockChange(boolean value);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ScreenClockListener listener = getListener();
        String action = intent.getAction();
        if (action.equals(Constants.Intent.ACTION_CHANGE_SCREEN_CLOCK)) {
            int value = intent.getIntExtra(Constants.Intent.EXTRA_MY_SCREEN_CLOCK, 0);
            listener.onScreenClockChange(value == 1);
        }
    }

}
