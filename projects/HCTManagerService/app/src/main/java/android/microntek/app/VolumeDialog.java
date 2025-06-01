package android.microntek.app;

import android.app.Dialog;
import android.content.Context;
import android.microntek.CarManager;
import android.microntek.Constant;
import android.microntek.HctUtil;
import android.microntek.common.ToggleSlider;
import android.microntek.common.VolumeController;
import android.microntek.common.VolumeInterface;
import android.microntek.service.R;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
/* loaded from: classes.dex */
public class VolumeDialog extends Dialog implements VolumeController.VolStateChangeCallback {
    private CarManager mCarManager;
    private Context mContext;
    private final Runnable mDismissDialogRunnable;
    private VolumeInterface mFun;
    protected Handler mHandler;
    private VolumeController mVolumeController;
    private final int mVolumeDialogLongTimeout;
    private final int mVolumeDialogShortTimeout;

    public VolumeDialog(Context context) {
        super(context, R.style.HctDialog);
        this.mHandler = new Handler();
        this.mFun = null;
        this.mDismissDialogRunnable = new Runnable() { // from class: android.microntek.app.VolumeDialog.1
            @Override // java.lang.Runnable
            public void run() {
                if (VolumeDialog.this.isShowing()) {
                    VolumeDialog.this.dismiss();
                }
            }
        };
        this.mContext = context;
        this.mVolumeDialogLongTimeout = 5000;
        this.mVolumeDialogShortTimeout = 2000;
        try {
            this.mFun = (VolumeInterface) context;
        } catch (Exception e) {
        }
    }

    @Override // android.app.Dialog
    public void onCreate(Bundle savedInstanceState) {
        int mVolumeMax;
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setType(2020);
        window.clearFlags(2);
        window.requestFeature(1);
        String mCustomerSub = HctUtil.getCustomerSub();
        if (mCustomerSub.startsWith("ASUKA")) {
            setContentView(R.layout.asuka_volume_dialog);
        } else if ("HZC25".equals(mCustomerSub) || "HZC25_1".equals(mCustomerSub) || "HZC25_2".equals(mCustomerSub)) {
            setContentView(R.layout.hzc25_volume_dialog);
        } else {
            setContentView(R.layout.volume_dialog);
        }
        setCanceledOnTouchOutside(true);
        this.mCarManager = new CarManager();
        int intvaul = 0;
        VolumeInterface volumeInterface = this.mFun;
        if (volumeInterface != null) {
            intvaul = volumeInterface.GetVolume();
        }
        String maxvol = this.mCarManager.getParameters("cfg_maxvolume=");
        try {
            mVolumeMax = Integer.parseInt(maxvol);
        } catch (Exception e) {
            mVolumeMax = 30;
        }
        if (this.mCarManager.getParameters(Constant.VOLUMEMUTE).equals("true")) {
            intvaul = 0;
        }
        this.mVolumeController = new VolumeController(getContext(), (ToggleSlider) findViewById(R.id.volume_slider), intvaul, mVolumeMax);
    }

    @Override // android.app.Dialog
    protected void onStart() {
        super.onStart();
        dismissVolumeDialog(this.mVolumeDialogLongTimeout);
        this.mVolumeController.addStateChangedCallback(this);
    }

    @Override // android.app.Dialog
    protected void onStop() {
        super.onStop();
        this.mVolumeController.unregisterCallbacks();
        removeAllVolumeDialogCallbacks();
    }

    private void dismissVolumeDialog(int timeout) {
        removeAllVolumeDialogCallbacks();
        this.mHandler.postDelayed(this.mDismissDialogRunnable, timeout);
    }

    private void removeAllVolumeDialogCallbacks() {
        this.mHandler.removeCallbacks(this.mDismissDialogRunnable);
    }

    @Override // android.microntek.common.VolumeController.VolStateChangeCallback
    public void onVolLevelChanged(int vol) {
        dismissVolumeDialog(this.mVolumeDialogShortTimeout);
        VolumeInterface volumeInterface = this.mFun;
        if (volumeInterface != null) {
            volumeInterface.OnChangeVolume(vol);
        }
    }

    public void SetVolumeDialog(int vol) {
        VolumeController volumeController = this.mVolumeController;
        if (volumeController != null) {
            volumeController.setVolume(vol);
            dismissVolumeDialog(this.mVolumeDialogShortTimeout);
        }
    }
}
