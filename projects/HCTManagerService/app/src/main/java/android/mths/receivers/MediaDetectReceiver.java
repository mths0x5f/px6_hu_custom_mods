package android.mths.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MediaDetectReceiver extends BaseReceiver<MediaDetectReceiver.MediaDetectListener> {

    public static final IntentFilter INTENT_FILTER;

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
        default void onMediaEject() {}
        default void onMediaUnmounted() {}
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaDetectListener listener = getListener();
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_MEDIA_MOUNTED -> {
                String path = intent.getData().getPath();
                listener.onMediaMounted(path);
            }
            case Intent.ACTION_MEDIA_EJECT -> listener.onMediaEject();
            case Intent.ACTION_MEDIA_UNMOUNTED -> listener.onMediaUnmounted();
        }
    }

}
