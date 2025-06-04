package android.microntek;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class HctUtil {

    public static int getInt(byte[] serialBuff, int start, int size) {
        int dat = 0;
        for (int i = 0; i < size; i++) {
            dat = (dat << 8) + (serialBuff[start + i] & 255);
        }
        return dat;
    }

    public static int[] getIntArray(byte[] serialBuff, int start, int size) {
        int len = size / 3;
        int[] result = new int[len];
        int dat = 0;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < 3; j++) {
                dat = (dat << 8) + (serialBuff[(i * 3) + start + j] & 255);
            }
            result[i] = dat;
        }
        return result;
    }

    public static int getInt2(byte[] serialBuff, int start, int size) {
        int dat = 0;
        for (int i = 0; i < size; i++) {
            dat = (dat << 8) + (serialBuff[((start + size) - 1) - i] & 255);
        }
        return dat;
    }

    public static int getFramePos(byte[] buf, int start, int size) {
        for (int i = start; i < size; i++) {
            if (buf[i] == -6) {
                final int frameStart = i;
                if (size - i >= 5) {
                    int len = getInt2(buf, i + 3, 2) + 5;
                    int frameEnd = (len + frameStart) - 1;
                    if (size - i >= len) {
                        return (frameEnd << 16) | frameStart;
                    }
                }
                return -1;
            }
        }
        return -1;
    }

    public static byte[] getAsciiByteArray(byte[] buffer, int start, int length) {
        int i = 0;
        int size = start;
        while (i < length) {
            int size2 = size + 1;
            if (buffer[size] == 0) {
                break;
            }
            i++;
            size = size2;
        }
        int size3 = i;
        byte[] bytebuffer = new byte[size3];
        System.arraycopy(buffer, start, bytebuffer, 0, size3);
        return bytebuffer;
    }

    public static String getAsciiString(byte[] buffer, int start, int length) {
        int i = 0;
        int size = start;
        while (i < length) {
            int size2 = size + 1;
            if (buffer[size] == 0) {
                break;
            }
            i++;
            size = size2;
        }
        int size3 = i;
        byte[] bytebuffer = new byte[size3];
        System.arraycopy(buffer, start, bytebuffer, 0, size3);
        try {
            return new String(bytebuffer, "gb2312");
        } catch (Exception e) {
            return "null";
        }
    }

    public static int mtcGetRealVolume(int v, int max) {
        float vol;
        float vol2 = (100.0f * v) / max;
        if (vol2 < 20.0f) {
            vol = (3.0f * vol2) / 2.0f;
        } else if (vol2 >= 50.0f) {
            vol = 20.0f + ((4.0f * vol2) / 5.0f);
        } else {
            vol = 10.0f + vol2;
        }
        return (int) vol;
    }

    public static boolean isAppRunning(Context ct, String pkgName) {
        boolean is = false;
        boolean is2 = false;
        ActivityManager am = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.processName.equals(pkgName)) {
                is = true;
                break;
            }
        }
        if (is) {
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
            Iterator<ActivityManager.RunningTaskInfo> it2 = list.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                ActivityManager.RunningTaskInfo info = it2.next();
                if (info.topActivity.getPackageName().equals(pkgName) && info.baseActivity.getPackageName().equals(pkgName)) {
                    is2 = true;
                    break;
                }
            }
        }
        return is && is2;
    }

    public static boolean CheckIsFront(Context ct, String pkname) {
        ActivityManager am = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(pkname) && appProcess.importance == 100) {
                break;
            }
        }
        return false;
    }

    public static String getTopActivityClassName(Context ct) {
        ActivityManager am = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            return cn.getClassName();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTopActivityPackageName(Context ct) {
        ActivityManager am = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == 100) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static boolean isPackageApplicationEnabled(Context ct, String packageName) {
        int state = 2;
        PackageManager pm = ct.getPackageManager();
        if (isPackageInstalled(ct, packageName)) {
            if (packageName.startsWith("com.microntek.")) {
                int temp = pm.getComponentEnabledSetting(new ComponentName(packageName, packageName + ".MainActivity"));
                if (temp == 2 || temp == 3) {
                    return false;
                }
            }
            state = pm.getApplicationEnabledSetting(packageName);
        }
        return state != 2 && state != 3;
    }

    public static boolean isPackageInstalled(Context ct, String packageName) {
        PackageManager pm = ct.getPackageManager();
        List<PackageInfo> installedList = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        int installedListSize = installedList.size();
        for (int i = 0; i < installedListSize; i++) {
            PackageInfo tmp = installedList.get(i);
            if (packageName.equalsIgnoreCase(tmp.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static void setApplicationEnabled(Context ct, String[] packageName, boolean enable) {
        int i;
        PackageManager pm = ct.getPackageManager();
        for (String s : packageName) {
            if (isPackageInstalled(ct, s)) {
                if (enable) {
                    i = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                } else {
                    i = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                }
                pm.setApplicationEnabledSetting(s, i, PackageManager.DONT_KILL_APP);
            }
        }
    }

    public static void setApplicationEnabled(Context ct, String packageName, boolean enable) {
        int i;
        PackageManager pm = ct.getPackageManager();
        if (isPackageInstalled(ct, packageName)) {
            if (enable) {
                i = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            } else {
                i = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            }
            pm.setApplicationEnabledSetting(packageName, i, PackageManager.DONT_KILL_APP);
        }
    }

    public static String getTxtFile(String path) {
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            StringBuilder sb = new StringBuilder();
            InputStreamReader read = null;
            try {
                read = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(read);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\r\n");
                }
                read.close();
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                if (read != null) {
                    try {
                        read.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static String getCustomer() {
        return SystemProperties.get("ro.product.customer", "HCT");
    }

    public static String getCustomerSub() {
        return SystemProperties.get("ro.product.customer.sub", "HCT");
    }

    public static void execCmd(String cmd) {
        OutputStream outputStream;
        DataOutputStream dataOutputStream;
        try {
            Process p = Runtime.getRuntime().exec("sh");
            outputStream = p.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();

            dataOutputStream.close();

            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
