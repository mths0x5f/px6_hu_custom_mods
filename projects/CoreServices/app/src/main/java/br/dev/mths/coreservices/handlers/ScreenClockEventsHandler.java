package br.dev.mths.coreservices.handlers;

import android.util.Log;

import java.text.MessageFormat;

import br.dev.mths.coreservices.receivers.ScreenClockReceiver;
import br.dev.mths.coreservices.system.SettingsAccessor;
import br.dev.mths.coreservices.system.state.State;

public class ScreenClockEventsHandler implements ScreenClockReceiver.ScreenClockListener {

    private static final String TAG = "ScreenClockEventsHandler";

    private final State state;
    private final SettingsAccessor settings;

    public ScreenClockEventsHandler(State state, SettingsAccessor settings) {
        this.state = state;
        this.settings = settings;
    }

    @Override
    public void onScreenClockChange(boolean value) {
        Log.d(TAG, MessageFormat.format("onScreenClockChange({0})", value));
        if (value) {
            state.getFlags().setScreensaverEnableLocal(true);
            state.getFlags().setScreensaverEnable(true);
            state.getValues().setScreensaverTimeout(settings.getMusicScreenTimeout());
        } else {
            state.getFlags().setScreensaverEnableLocal(false);
            state.getFlags().setScreensaverEnable(false);
            state.getValues().setScreensaverTimeout(-1);
        }
        state.getValues().setScreensaverTimer(0);
    }

}