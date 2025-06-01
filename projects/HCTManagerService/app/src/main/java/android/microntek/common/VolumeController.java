package android.microntek.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.microntek.Constant;
import android.microntek.common.ToggleSlider;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import java.util.ArrayList;
import java.util.Iterator;
/* loaded from: classes.dex */
public class VolumeController implements ToggleSlider.Listener {
    private final Context mContext;
    private final ToggleSlider mControl;
    private int mVolLevel;
    private Handler mHandler = new Handler() { // from class: android.microntek.common.VolumeController.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0 && VolumeController.this.mControl != null) {
                VolumeController.this.mControl.setValue(msg.arg1);
            }
        }
    };
    private ArrayList<VolStateChangeCallback> mChangeCallbacks = new ArrayList<>();

    /* loaded from: classes.dex */
    public interface VolStateChangeCallback {
        void onVolLevelChanged(int i);
    }

    public VolumeController(Context context, ToggleSlider control, int level, int max) {
        this.mContext = context;
        this.mControl = control;
        this.mVolLevel = level;
        control.setMax(max);
        this.mControl.setValue(level);
        control.setOnChangedListener(this);
        IntentFilter intentFilter = new IntentFilter(Constant.MSG_MTC_VOLUME_CHANGED);
        VolumeReceiver volumeReceiver = new VolumeReceiver();
        this.mContext.registerReceiver(volumeReceiver, intentFilter);
    }

    /* loaded from: classes.dex */
    class VolumeReceiver extends BroadcastReceiver {
        VolumeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.MSG_MTC_VOLUME_CHANGED.equals(action) && intent.hasExtra("volume")) {
                int vol = intent.getIntExtra("volume", 0);
                VolumeController.this.mHandler.obtainMessage(0, vol, vol).sendToTarget();
            }
        }
    }

    public void setVolume(int vol) {
        this.mVolLevel = vol;
        this.mControl.setValue(vol);
    }

    public void addStateChangedCallback(VolStateChangeCallback cb) {
        this.mChangeCallbacks.add(cb);
    }

    public boolean removeStateChangedCallback(VolStateChangeCallback cb) {
        return this.mChangeCallbacks.remove(cb);
    }

    public void unregisterCallbacks() {
        this.mChangeCallbacks.clear();
    }

    @Override // android.microntek.common.ToggleSlider.Listener
    public void onInit() {
    }

    @Override // android.microntek.common.ToggleSlider.Listener
    public void onChanged(int value) {
        this.mVolLevel = value;
        ToggleSlider toggleSlider = this.mControl;
        if (toggleSlider != null) {
            toggleSlider.setValue(value);
        }
        Iterator<VolStateChangeCallback> it = this.mChangeCallbacks.iterator();
        while (it.hasNext()) {
            VolStateChangeCallback cb = it.next();
            cb.onVolLevelChanged(value);
        }
    }

    @Override // android.microntek.common.ToggleSlider.Listener
    public void onVolClick() {
        Intent intent = new Intent(Constant.MSG_MTC_IRKEY_DOWN);
        intent.putExtra(Constant.KEY_CODE, 258);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    @Override // android.microntek.common.ToggleSlider.Listener
    public void onUpVolClick() {
        Intent intent = new Intent(Constant.MSG_MTC_IRKEY_DOWN);
        intent.putExtra(Constant.KEY_CODE, 273);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    @Override // android.microntek.common.ToggleSlider.Listener
    public void onDownVolClick() {
        Intent intent = new Intent(Constant.MSG_MTC_IRKEY_DOWN);
        intent.putExtra(Constant.KEY_CODE, 281);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }
}
