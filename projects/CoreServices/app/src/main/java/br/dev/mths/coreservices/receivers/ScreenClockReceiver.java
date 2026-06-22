package br.dev.mths.coreservices.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenClockReceiver extends BaseReceiver<ScreenClockReceiver.ScreenClockListener> {

    private static final IntentFilter INTENT_FILTER;

    private static final String ACTION_CHANGE_SCREEN_CLOCK = "changescreenclock";
    private static final String EXTRA_MY_SCREEN_CLOCK = "myscreenclock";

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(ACTION_CHANGE_SCREEN_CLOCK);
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
        if (action.equals(ACTION_CHANGE_SCREEN_CLOCK)) {
            boolean value = intent.getIntExtra(EXTRA_MY_SCREEN_CLOCK, 0) == 1;
            listener.onScreenClockChange(value);
        }
    }

}
