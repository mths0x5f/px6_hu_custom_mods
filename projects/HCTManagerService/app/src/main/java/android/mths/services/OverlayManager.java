package android.mths.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public class OverlayManager extends Service {

    private static final String TAG = "OverlayManager";

    public static final String ACTION_SHOW_OVERLAY = "OverlayManager.ACTION_SHOW_OVERLAY";
    public static final String ACTION_DISMISS_OVERLAY = "OverlayManager.ACTION_DISMISS_OVERLAY";
    public static final String ACTION_CLEAR_QUEUE = "OverlayManager.ACTION_CLEAR_QUEUE";

    private WindowManager windowManager;
    private View currentView;
    private final Queue<OverlayRequest> overlayQueue = new ArrayDeque<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private record OverlayRequest(int layoutRes, WindowManager.LayoutParams params) {}

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case ACTION_SHOW_OVERLAY -> {
                    int layoutRes = intent.getIntExtra("layoutRes", 0);
                    Bundle bundle = intent.getBundleExtra("layoutParams");
                    if (layoutRes == 0) return;
                    handleShowRequest(layoutRes, bundle);
                }
                case ACTION_DISMISS_OVERLAY -> dismissOverlay();
                case ACTION_CLEAR_QUEUE -> {
                    overlayQueue.clear();
                    dismissOverlay();
                }
            }
        }
    };

    private void handleShowRequest(@LayoutRes int layoutRes, Bundle bundle) {
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "Cannot show overlay: Permission denied");
            return;
        }

        var params = createLayoutParams(bundle);
        enqueueOverlay(layoutRes, params);
    }

    private WindowManager.LayoutParams createLayoutParams(Bundle bundle) {
        var params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        if (bundle != null) {
            params.type = bundle.getInt("type", params.type);
            params.width = bundle.getInt("width", params.width);
            params.height = bundle.getInt("height", params.height);
            params.format = bundle.getInt("format", params.format);
            params.gravity = bundle.getInt("gravity", params.gravity);
            params.x = bundle.getInt("x", params.x);
            params.y = bundle.getInt("y", params.y);
            params.windowAnimations = bundle.getInt("windowAnimations", params.windowAnimations);
            params.flags = bundle.getInt("flags", params.flags);
        }

        return params;
    }

    private void enqueueOverlay(int layoutRes, WindowManager.LayoutParams params) {
        overlayQueue.add(new OverlayRequest(layoutRes, params));
        if (currentView == null) {
            showNextOverlay();
        }
    }

    private void showNextOverlay() {
        var nextRequest = overlayQueue.poll();
        if (nextRequest == null) return;

        currentView = LayoutInflater.from(this).inflate(nextRequest.layoutRes, null);
        windowManager.addView(currentView, nextRequest.params);
    }

    private void dismissOverlay() {
        if (currentView != null) {
            if (currentView.isAttachedToWindow()) {
                windowManager.removeView(currentView);
            }
            currentView = null;
            handler.postDelayed(this::showNextOverlay, 100);
        } else {
            showNextOverlay();
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        var filter = new IntentFilter();
        filter.addAction(ACTION_SHOW_OVERLAY);
        filter.addAction(ACTION_DISMISS_OVERLAY);
        filter.addAction(ACTION_CLEAR_QUEUE);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (currentView != null && currentView.isAttachedToWindow()) {
            windowManager.removeView(currentView);
        }
        overlayQueue.clear();
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
