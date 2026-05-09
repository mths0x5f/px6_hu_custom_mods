package android.mths.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.microntek.service.R;
import android.mths.services.OverlayManager;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;

public class SystemOverlay {

    private final Context context;
    private final int layoutRes;

    private static final Bundle layoutParams = new Bundle();

    static {
        layoutParams.putInt("type", WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
        layoutParams.putInt("width", WindowManager.LayoutParams.MATCH_PARENT);
        layoutParams.putInt("height", WindowManager.LayoutParams.MATCH_PARENT);
        layoutParams.putInt("format", PixelFormat.RGBA_8888);
        layoutParams.putInt("windowAnimations", R.style.system_overlay_anim);
        layoutParams.putInt("flags", WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                   | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                   | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                   | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                                   | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
    }

    private SystemOverlay(Context context, int layoutRes) {
        this.context = context;
        this.layoutRes = layoutRes;
    }

    public static SystemOverlay create(Context context, @LayoutRes int layoutRes) {
        return new SystemOverlay(context, layoutRes);
    }

    public void show() {
        final var intent = new Intent();
        intent.setAction(OverlayManager.ACTION_SHOW_OVERLAY);
        intent.putExtra("layoutRes", layoutRes);
        intent.putExtra("layoutParams", layoutParams);
        context.sendBroadcast(intent);
    }

    public void dismiss() {
        final var intent = new Intent();
        intent.setAction(OverlayManager.ACTION_DISMISS_OVERLAY);
        context.sendBroadcast(intent);
    }

}
