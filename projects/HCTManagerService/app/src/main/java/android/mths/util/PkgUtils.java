package android.mths.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

import lombok.experimental.UtilityClass;

/**
 * Utility class for package management tasks.
 *
 * @author Matheus Santos (mths95)
 */
@UtilityClass
public class PkgUtils {

    /**
     * Checks if a package is installed on the device.
     *
     * @param context     The application context.
     * @param packageName The package name to check.
     * @return true if the package is installed, false otherwise.
     */
    public boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> installedList = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        return installedList.stream().anyMatch(pkg -> packageName.equalsIgnoreCase(pkg.packageName));
    }

    /**
     * Enables or disables an application based on the provided package name.
     *
     * @param context     The application context.
     * @param packageName The package name of the application to enable/disable.
     * @param enable      true to enable the application, false to disable it.
     */
    public void setApplicationEnabled(Context context, String packageName, boolean enable) {
        PackageManager pm = context.getPackageManager();
        final int state = enable ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (isPackageInstalled(context, packageName)) {
            pm.setApplicationEnabledSetting(packageName, state, PackageManager.DONT_KILL_APP);
        }
    }

    /**
     * Enables or disables multiple applications based on the provided package names.
     *
     * @param context      The application context.
     * @param packageNames An array of package names of the applications to enable/disable.
     * @param enable       true to enable the applications, false to disable them.
     */
    public void setApplicationEnabled(Context context, String[] packageNames, boolean enable) {
        for (String packageName : packageNames) {
            setApplicationEnabled(context, packageName, enable);
        }
    }

    /**
     * Checks if an application is enabled based on the provided package name.
     *
     * @param context     The application context.
     * @param packageName The package name of the application to check.
     * @return true if the application is enabled, false otherwise.
     */
    public static boolean isApplicationEnabled(Context context, String packageName) {
        int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        PackageManager pm = context.getPackageManager();
        if (isPackageInstalled(context, packageName)) {
            if (packageName.startsWith("com.microntek.")) {
                var componentName = new ComponentName(packageName, packageName + ".MainActivity");
                state = pm.getComponentEnabledSetting(componentName);
                return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                       state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
            }
            state = pm.getApplicationEnabledSetting(packageName);
        }
        return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
               state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
    }

}
