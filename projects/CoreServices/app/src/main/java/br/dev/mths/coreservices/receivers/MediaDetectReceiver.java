package br.dev.mths.coreservices.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MediaDetectReceiver extends BaseReceiver<MediaDetectReceiver.MediaDetectListener> {

    private static final IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(Intent.ACTION_MEDIA_MOUNTED);
        INTENT_FILTER.addAction(Intent.ACTION_MEDIA_EJECT);
        INTENT_FILTER.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        INTENT_FILTER.addDataScheme("file");
    }

    public MediaDetectReceiver(Context context, MediaDetectListener listener) {
        super(context, listener, INTENT_FILTER);
    }

    public interface MediaDetectListener extends Listener {
        default void onMediaMounted(String path) {}
        default void onMediaEject(String path) {}
        default void onMediaUnmounted(String path) {}
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaDetectListener listener = getListener();
        String action = intent.getAction();

        if (action == null || intent.getData() == null) {
            return;
        }

        String path = intent.getData().getPath();
        switch (action) {
            case Intent.ACTION_MEDIA_MOUNTED -> listener.onMediaMounted(path);
            case Intent.ACTION_MEDIA_EJECT -> listener.onMediaEject(path);
            case Intent.ACTION_MEDIA_UNMOUNTED -> listener.onMediaUnmounted(path);
        }
    }

}
