package android.mths.util;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

import android.annotation.NonNull;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;
import java.util.Objects;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppUtils {

    /**
     * Retrieves the class name of the top (foreground) activity.
     *
     * @param context The application context.
     * @return The class name of the top activity, or an empty String if it cannot be determined.
     */
    public static String getTopActivityClassName(Context context) {
        ActivityManager am = context.getSystemService(ActivityManager.class);
        try {
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            return cn.getClassName();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Retrieves the package name of the top (foreground) activity.
     *
     * @param context The application context.
     * @return The package name of the top activity, or an empty String if it cannot be determined.
     */
    public static String getTopActivityPackageName(Context context) {
        ActivityManager am = context.getSystemService(ActivityManager.class);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                Objects.requireNonNullElse(am.getRunningAppProcesses(), List.of());
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == IMPORTANCE_FOREGROUND) {
                return appProcess.processName;
            }
        }
        return "";
    }

    /**
     * Checks if a specific application is currently running in the foreground.
     *
     * @param context     The application context.
     * @param packageName The package name of the application to check.
     * @return true if the application is running in the foreground, false otherwise.
     */
    public static boolean isAppRunning(@NonNull Context context, @NonNull String packageName) {
        ActivityManager am = context.getSystemService(ActivityManager.class);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();

        if (appProcesses == null) {
            return false;
        }

        return appProcesses.stream()
                .anyMatch(processInfo ->
                        processInfo.importance ==
                                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                                processInfo.processName.equals(packageName)
                );
    }

}
