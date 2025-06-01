package android.microntek;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.Objects;

public class InstallUtil {

    private static final String TAG = "InstallUtil";

    private static final int INSTALLED_FAILED = 0;
    private static final int INSTALLED_OK = 1;

    public static final int MSG_INSTALLED_PACKAGE = 65535;
    public static final String MSG_INSTALL_XRROSS_OK = "com.microntek.install.xrross.ok";
    public static final String PACKAGE = "package";

    private static InstallUtil mInstallUtil = null;

    private Context mContext;
    private Handler mHandler;

    private final Handler mInHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            InstallUtil.this.mInHandler.removeMessages(0);
            Log.i(InstallUtil.TAG, ">>>>>>>>> install.result=" + SystemProperties.get("sys.hct.install.result", ""));
            if ("install".equals(SystemProperties.get("sys.hct.install.result", "install"))) {
                InstallUtil.this.mInHandler.sendEmptyMessageDelayed(0, 1000L);
            } else if (InstallUtil.this.mHandler != null) {
                Message msg2 = InstallUtil.this.mHandler.obtainMessage();
                msg2.what = InstallUtil.MSG_INSTALLED_PACKAGE;
                msg2.arg1 = 1;
                msg2.obj = InstallUtil.this.mPackageName;
                InstallUtil.this.mHandler.removeMessages(msg2.what);
                InstallUtil.this.mHandler.sendMessageDelayed(msg2, 1000L);
            }
        }
    };

    private String mPackageName;
    private String mPath;

    private InstallUtil() {
    }

    public InstallUtil(Context c, String path, Handler h) {
        this.mPath = path;
        this.mHandler = h;
        this.mContext = c;
        startInstall2(path);
    }

    public static InstallUtil getInstance() {
        if (mInstallUtil == null) {
            mInstallUtil = new InstallUtil();
        }
        return mInstallUtil;
    }

    private String getPath() {
        return this.mPath;
    }

    public void installPackage(Context context, String path) {
        this.mPath = path;
        this.mContext = context;
    }

    private void execCmd(String cmd) {
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            try {
                try {
                    Process p = Runtime.getRuntime().exec("sh");
                    outputStream = p.getOutputStream();
                    dataOutputStream = new DataOutputStream(outputStream);
                    dataOutputStream.writeBytes(cmd);
                    dataOutputStream.flush();
                    try {
                        dataOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Throwable th) {
                    if (dataOutputStream != null) {
                        try {
                            dataOutputStream.close();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Exception e3) {
                            e3.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e4.printStackTrace();
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (Exception e5) {
                        e5.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                } else {
                    return;
                }
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e6) {
            e6.printStackTrace();
        }
    }

    private void startInstall2(String path) {
        PackageInfo info = this.mContext.getPackageManager().getPackageArchiveInfo(this.mPath, PackageManager.GET_ACTIVITIES);
        this.mPackageName = info != null ? info.packageName : "";
        if ("install".equals(SystemProperties.get("sys.hct.install.result", ""))) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = MSG_INSTALLED_PACKAGE;
            msg.arg1 = -1;
            msg.obj = this.mPackageName;
            this.mHandler.removeMessages(msg.what);
            this.mHandler.sendMessageDelayed(msg, 1000L);
            Log.i(TAG, ">>>>>>>>> apk installing ....." + this.mPackageName);
        } else if (path.length() > 90 || path.contains(" ")) {
            Message msg2 = this.mHandler.obtainMessage();
            msg2.what = MSG_INSTALLED_PACKAGE;
            msg2.arg1 = 1;
            msg2.obj = this.mPackageName;
            this.mHandler.removeMessages(msg2.what);
            this.mHandler.sendMessageDelayed(msg2, 2000L);
            Context context = this.mContext;
            Toast.makeText(context, "name error " + path.substring(path.lastIndexOf("/") + 1), Toast.LENGTH_SHORT).show();
        } else {
            Settings.Global.putInt(this.mContext.getContentResolver(), "package_verifier_user_consent", -1);
            Log.i(TAG, ">>>>>>>>> start install.sh >>path=" + path + ">>mPackageName=" + this.mPackageName);
            SystemProperties.set("sys.hct.install.path", path);
            SystemProperties.set("sys.hct.install.result", "install");
            execCmd("/system/bin/hctinstall.sh");
            this.mInHandler.removeMessages(0);
            this.mInHandler.sendEmptyMessage(0);
        }
    }
}
