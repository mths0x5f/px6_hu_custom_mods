package android.mths.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class InstallReceiver extends BaseReceiver<InstallReceiver.InstallListener> {

    public static final IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(Intent.ACTION_PACKAGE_ADDED);
        INTENT_FILTER.addAction(Intent.ACTION_PACKAGE_REMOVED);
        INTENT_FILTER.addAction(Intent.ACTION_PACKAGE_CHANGED);
        INTENT_FILTER.addDataScheme("package");
    }

    public InstallReceiver(Context context, InstallListener listener) {
        super(context, listener, INTENT_FILTER);
    }

    public interface InstallListener extends Listener {
        default void onPackageAdded(String packageName) {}
        default void onPackageRemoved(String packageName) {}
        default void onPackageChanged(String packageName) {}
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        InstallListener listener = getListener();
        String action = intent.getAction();
        // code replicate original, not ideal
        String packageName = intent.getData().getSchemeSpecificPart();
        switch (action) {
            case Intent.ACTION_PACKAGE_ADDED -> listener.onPackageAdded(packageName);
            case Intent.ACTION_PACKAGE_REMOVED -> listener.onPackageRemoved(packageName);
            case Intent.ACTION_PACKAGE_CHANGED -> listener.onPackageChanged(packageName);
        }
    }

}
