package android.microntek;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.microntek.service.R;
import android.os.Debug;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClearProcess {

    private static final String TAG = "ClearProcess";
    static String gpsPackage;
    static Context mContext;
    static Toast mToast;
    static ClearProcess sInstance;
    private static final boolean EnLog = false;
    private static boolean mBusy = EnLog;
    static final Object sGlobalLock = new Object();
    private static int mMode = -1;
    private static final String[] serverList = {"com.percherry.roundadas", "com.android.externalstorage", "android.microntek.", "com.murtas.", "com.microntek.", "com.goodocom.gocsdk", "android.rockchip.update.service", "com.android.systemui", "com.hct.obdservice.OBDService", "com.unisound", "com.intel.thermal", "com.dpadnavi.assist", "cn.manstep.phonemirror", "com.android.bluetooth", "com.hiworld.", Constant.EASYCONNECTEDPACKAGE, "com.txznet.txz", "com.txznet.adapter", "com.txznet.smartadapter", "com.google.android", "com.vayosoft.carsystem", "com.ituran.driveusagemonitor", "com.android.chrome", "com.hct.", "com.vn.zestech.mapviet.tracking", "com.joaomgcd.", "flar2.", "com.tl.tpms", "com.icarvietnam.", "com.setting.icar", "com.icar.", "com.vanced.android.youtube", "com.aispeech.lyra.view", "com.aispeech.lyra.daemon", "com.aispeech.lyra.adapter", "com.devfill.togoinsights", "com.thlonline.camperhelp", "com.togoinsights.", "com.aispeech.lyra.", "com.aispeech.hotwords", "com.xtrons.app", "com.companyname.MessageApp", "com.telenav.launcher", "com.telenav.vivid.scout4cars.launcher", "com.thl.launcher", "com.thl.timezoneupdate", "com.tecno.unitapp", "com.suding.aibox"};
    private static final String[] packageList = {"com.percherry.roundadas", "com.android.externalstorage", "android.microntek.", "com.murtas.", "com.microntek.", "com.goodocom.gocsdk", "android.rockchip.update.service", "com.android.systemui", "com.hct.obd.OBDActivity", "com.unisound", "com.dpadnavi.assist", "com.intel.thermal", "cn.manstep.phonemirror", "com.hiworld.", "com.carboy.launch", "com.android.bluetooth", Constant.EASYCONNECTEDPACKAGE, "com.txznet.txz", "com.txznet.adapter", "com.txznet.smartadapter", "com.android.launcher", "com.google.android", "com.vayosoft.carsystem", "com.ituran.driveusagemonitor", "com.android.chrome", "com.hct.", "com.joaomgcd.", "flar2.", "com.tl.tpms", "com.icarvietnam.", "com.setting.icar", "com.icar.", "com.vanced.android.youtube", "com.aispeech.lyra.view", "com.aispeech.lyra.daemon", "com.aispeech.lyra.adapter", "com.xtrons.app", "com.thlonline.camperhelp", "nz.co.campermate", "com.devfill.togoinsights", "com.togoinsights.", "com.aispeech.lyra.", "com.aispeech.hotwords", "com.xtrons.app", "com.companyname.MessageApp", "com.telenav.launcher", "com.telenav.vivid.scout4cars.launcher", "com.thl.launcher", "com.thl.timezoneupdate", "com.tecno.unitapp", "com.suding.aibox"};

    public static ClearProcess getInstance(Context context) {
        ClearProcess clearProcess;
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new ClearProcess(context);
            }
            mMode = -1;
            clearProcess = sInstance;
        }
        return clearProcess;
    }

    private ClearProcess(Context context) {
        mContext = context;
    }

    public boolean getBusy() {
        return mBusy;
    }

    public void clearManage(int mode, String gps) {
        mMode = mode;
        mBusy = true;
        gpsPackage = gps;
        long beforeMem = getAvailMemory(mContext);
        closeRunningService(mContext);
        int count = closeRunningAppProcess(mContext);
        long afterMem = getAvailMemory(mContext);
        if (mode == 1) {
            long clearMemory = Math.abs(afterMem - beforeMem);
            String message = mContext.getString(R.string.clear_message, String.valueOf(count), Formatter.formatFileSize(mContext, clearMemory));
            Toast toast = mToast;
            if (toast == null) {
                mToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
            } else {
                toast.cancel();
                mToast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
            }
            mToast.show();
        }
        mBusy = EnLog;
    }

    private boolean shouldNotClose(String pkgName) {
        String[] strArr;
        for (String name : serverList) {
            if (pkgName.startsWith(name)) {
                return true;
            }
        }
        String str = gpsPackage;
        if (!pkgName.equals(str)) {
            return EnLog;
        }
        return true;
    }

    private boolean shouldNotClose2(String pkgName) {
        String[] strArr;
        for (String name : packageList) {
            if (pkgName.startsWith(name)) {
                return true;
            }
        }
        String str = gpsPackage;
        if (!pkgName.equals(str)) {
            return EnLog;
        }
        return true;
    }

    private void closeRunningService(Context context) {
        Iterator<ActivityManager.RunningServiceInfo> it;
        ApplicationInfo appInfo;
        RunServiceModel runService;
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runServiceList = mActivityManager.getRunningServices(100);
        System.out.println(runServiceList.size());
        List<RunServiceModel> serviceInfoList = new ArrayList<>();
        for (ActivityManager.RunningServiceInfo runServiceInfo : runServiceList) {
            int pid = runServiceInfo.pid;
            int uid = runServiceInfo.uid;
            String processName = runServiceInfo.process;
            long j = runServiceInfo.activeSince;
            int i = runServiceInfo.clientCount;
            ComponentName serviceCMP = runServiceInfo.service;
            String serviceName = serviceCMP.getShortClassName();
            String pkgName = serviceCMP.getPackageName();
            PackageManager mPackageManager = context.getPackageManager();
            try {
                appInfo = mPackageManager.getApplicationInfo(pkgName, 0);
                runService = new RunServiceModel();
                runService.setAppIcon(appInfo.loadIcon(mPackageManager));
                runService.setAppLabel(appInfo.loadLabel(mPackageManager).toString());
                runService.setServiceName(serviceName);
                runService.setPkgName(pkgName);
                Intent intent = new Intent();
                intent.setComponent(serviceCMP);
                runService.setIntent(intent);
                runService.setPid(pid);
                runService.setUid(uid);
                runService.setProcessName(processName);
                serviceInfoList.add(runService);
            } catch (PackageManager.NameNotFoundException e3) {
                e3.printStackTrace();
            }
        }
        for (RunServiceModel serviceInfo : serviceInfoList) {
            if (serviceInfo.getUid() >= 10000) {
                String pkgName2 = serviceInfo.getPkgName();
                if (!shouldNotClose(pkgName2) && !isInputServicePkgName(context, pkgName2) && !isWallpaperPkgName(context, pkgName2)) {
                    if (mMode == 0 && !shouldNotClose2(pkgName2)) {
                        closePackage(pkgName2);
                    } else {
                        Intent stopserviceIntent = serviceInfo.getIntent();
                        try {
                            context.stopService(stopserviceIntent);
                        } catch (Exception sEx) {
                            closePackage(pkgName2);
                            Log.e(TAG, "stopService pkgName:" + pkgName2 + " Exception:" + sEx);
                        }
                    }
                }
            }
        }
    }

    public void closePackage(String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return;
        }
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            mActivityManager.forceStopPackage(pkgName);
        } catch (Exception sEx) {
            Log.e(TAG, " forceStopPackage  pkgName:" + pkgName + " Exception:" + sEx);
        }
    }

    private int closeRunningAppProcess(Context context) {
        List<ProcessInfo> processInfoList = new ArrayList<>();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
            int pid = appProcessInfo.pid;
            int uid = appProcessInfo.uid;
            String processName = appProcessInfo.processName;
            int[] myMemPid = {pid};
            Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(myMemPid);
            int memSize = memoryInfo[0].dalvikPrivateDirty;
            ProcessInfo processInfo = new ProcessInfo();
            processInfo.setPid(pid);
            processInfo.setUid(uid);
            processInfo.setMemSize(memSize);
            processInfo.setProcessName(processName);
            processInfo.pkgNameList = appProcessInfo.pkgList;
            processInfoList.add(processInfo);
            String[] strArr = appProcessInfo.pkgList;
        }
        int count = 0;
        for (ProcessInfo processInfo2 : processInfoList) {
            if (processInfo2.getUid() >= 10000) {
                String processname = processInfo2.getProcessName();
                if (processname.contains(".")) {
                    int pos = processname.indexOf(":");
                    if (pos != -1) {
                        processname = processname.substring(0, pos);
                    }
                    if (!shouldNotClose2(processname) && !isInputServicePkgName(context, processname) && !isWallpaperPkgName(context, processname)) {
                        if (mMode == 0 && !shouldNotClose(processname)) {
                            closePackage(processname);
                            count++;
                        } else {
                            try {
                                mActivityManager.killBackgroundProcesses(processname);
                                count++;
                            } catch (Exception e) {
                                System.out.println(" deny the permission");
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    private long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    private boolean isInputServicePkgName(Context context, String pk) {
        if (context == null || TextUtils.isEmpty(pk)) {
            return EnLog;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputProcesses = inputMethodManager.getInputMethodList();
        for (InputMethodInfo inputMethodInfo : inputProcesses) {
            String str = inputMethodInfo.getPackageName();
            if (pk.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return EnLog;
    }

    private boolean isWallpaperPkgName(Context ct, String pk) {
        try {
            PackageManager pm = ct.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentServices(new Intent("android.service.wallpaper.WallpaperService"), 128);
            for (ResolveInfo resolveInfo : list) {
                if (pk.equalsIgnoreCase(resolveInfo.serviceInfo.packageName)) {
                    return true;
                }
            }
            return EnLog;
        } catch (Exception e) {
            e.printStackTrace();
            return EnLog;
        }
    }

}
