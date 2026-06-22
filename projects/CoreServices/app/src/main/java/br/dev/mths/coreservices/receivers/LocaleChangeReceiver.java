package br.dev.mths.coreservices.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class LocaleChangeReceiver extends BaseReceiver<LocaleChangeReceiver.LocaleChangeListener> {

    private static final IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(Intent.ACTION_LOCALE_CHANGED);
    }

    public LocaleChangeReceiver(Context context, LocaleChangeListener listener) {
        super(context, listener, INTENT_FILTER);
    }

    public interface LocaleChangeListener extends Listener {
        void onLocaleChanged();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocaleChangeListener listener = getListener();
        String action = intent.getAction();
        if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            listener.onLocaleChanged();
        }
    }

}
