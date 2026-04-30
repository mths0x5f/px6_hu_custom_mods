package android.mths.receivers;

import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseReceiver<L extends BaseReceiver.Listener> extends BroadcastReceiver {

    private boolean isReceiverRegistered;
    private final Context appContext;
    @NonNull
    private final L listener;
    @NonNull
    private final IntentFilter intentFilter;

    protected interface Listener {
    }

    protected BaseReceiver(Context context,
                           @NonNull L listener,
                           @NonNull IntentFilter intentFilter) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
        this.intentFilter = intentFilter;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void register() {
        if (!isReceiverRegistered) {
            appContext.registerReceiver(this, intentFilter);
            isReceiverRegistered = true;
        }
    }

    public void unregister() {
        if (isReceiverRegistered) {
            try {
                appContext.unregisterReceiver(this);
                isReceiverRegistered = false;
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

}
