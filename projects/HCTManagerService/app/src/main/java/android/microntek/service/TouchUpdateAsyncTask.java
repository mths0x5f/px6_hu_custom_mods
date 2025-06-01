package android.microntek.service;

import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
/* loaded from: classes.dex */
public class TouchUpdateAsyncTask extends AsyncTask<Void, Integer, Integer> {
    private List<String> mList;
    private String mMsgText = "";
    private MicrontekServer mServer;
    private StringBuffer sBuf;

    TouchUpdateAsyncTask(MicrontekServer context, List<String> list) {
        this.sBuf = null;
        this.mServer = context;
        this.mList = list;
        this.sBuf = new StringBuffer();
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
    }

    @Override // android.os.AsyncTask
    protected Integer doInBackground(Void... params) {
        List<String> list = this.mList;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < this.mList.size(); i++) {
                String mPath = this.mList.get(i);
                if (mPath != null) {
                    Log.i("meng", "start update:" + mPath);
                    if (mPath.endsWith("gt9xx_update.cfg")) {
                        int sucess = 0;
                        try {
                            sucess = gt9xxUpdate(mPath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (sucess < 0) {
                            this.sBuf.append("gt9xx_update.cfg  fail!!!");
                            this.sBuf.append("\r\n");
                        } else {
                            this.sBuf.append("gt9xx_update.cfg  sucess");
                            this.sBuf.append("\r\n");
                        }
                    } else if (mPath.endsWith("dmcu.tch")) {
                        int sucess2 = dmcuTchUpdate(mPath);
                        if (sucess2 < 0) {
                            this.sBuf.append("dmcu.tch  fail!!!");
                            this.sBuf.append(this.mMsgText);
                            this.sBuf.append("\r\n");
                        } else {
                            this.sBuf.append("dmcu.tch  sucess");
                            this.sBuf.append("\r\n");
                        }
                    } else if (mPath.endsWith("dmcu.sw")) {
                        int sucess3 = dmcuSwUpdate(mPath);
                        if (sucess3 < 0) {
                            this.sBuf.append("dmcu.sw  fail!!!");
                            this.sBuf.append("\r\n");
                        } else {
                            this.sBuf.append("dmcu.sw  sucess");
                            this.sBuf.append("\r\n");
                        }
                    } else if (mPath.endsWith("dmcu.auto.cfg")) {
                        int sucess4 = dmcuAutoCfgUpdate(mPath);
                        if (sucess4 < 0) {
                            this.sBuf.append("dmcu.auto.cfg  fail!!!");
                            this.sBuf.append("\r\n");
                        } else {
                            this.sBuf.append("dmcu.auto.cfg  sucess");
                            this.sBuf.append("\r\n");
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Override // android.os.AsyncTask
    protected void onPostExecute(Integer integer) {
        this.mServer.showToastMsg(this.sBuf.toString(), 0);
    }

    @Override // android.os.AsyncTask
    protected void onProgressUpdate(Integer... values) {
    }

    private int gt9xxUpdate(String path) throws IOException {
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            StringBuilder sb = new StringBuilder();
            InputStreamReader read = null;
            try {
                try {
                    try {
                        try {

                                read = new InputStreamReader(new FileInputStream(file), "UTF-8");
                                BufferedReader reader = new BufferedReader(read);
                                while (true) {
                                    String line = reader.readLine();
                                    if (line == null) {
                                        break;
                                    }
                                    sb.append(line);
                                }
                                read.close();
                            } catch (IOException e) {
                                e.printStackTrace();

                            if (read != null) {
                                read.close();
                            }
                        }
                    } catch (IOException e3) {
                        e3.printStackTrace();
                        if (read != null) {
                            read.close();
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e4.printStackTrace();
                    if (read != null) {
                        read.close();
                    }
                }
                String result = sb.toString();
                String[] token = result.split(",");
                byte[] sendHEX = new byte[token.length];
                for (int i = 0; i < token.length; i++) {
                    try {
                        char c1 = token[i].toUpperCase().charAt(2);
                        char c2 = token[i].toUpperCase().charAt(3);
                        int num1 = (c1 < 'A' || c1 > 'F') ? c1 - '0' : (c1 - 'A') + 10;
                        int num2 = (c2 < 'A' || c2 > 'F') ? c2 - '0' : (c2 - 'A') + 10;
                        sendHEX[i] = (byte) ((num1 * 16) + num2);
                    } catch (Exception e5) {
                        return -1;
                    }
                }
                return saveConfigPath(sendHEX, "/proc/gt9xx_config") ? 0 : -1;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        read.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        }
        return -1;
    }

    private boolean saveConfigPath(byte[] data, String path) {
        File file = new File(path);
        try {
            file.createNewFile();
            FileOutputStream fstream = null;
            try {
                try {
                    fstream = new FileOutputStream(file);
                    fstream.write(data);
                    fstream.flush();
                    try {
                        fstream.close();
                        return true;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return false;
                    }
                } catch (Throwable e12) {
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e13) {
                            e13.printStackTrace();
                            return false;
                        }
                    }
                    throw e12;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (fstream != null) {
                    try {
                        fstream.close();
                    } catch (IOException e14) {
                        e14.printStackTrace();
                        return false;
                    }
                }
                return false;
            }
        } catch (IOException e2) {
            return false;
        }
    }

    private int dmcuTchUpdate(String path) {
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            byte[] buf = new byte[90];
            int i = 0;
            FileInputStream reader = null;
            try {
                try {

                        try {
                            try {
                                reader = new FileInputStream(file);
                                while (true) {
                                    int line = reader.read();
                                    if (line == -1) {
                                        break;
                                    }
                                    if (i < 90) {
                                        buf[i] = (byte) (line & HCT_CMD.CMD_DBG);
                                    }
                                    i++;
                                }
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();

                            if (reader != null) {
                                reader.close();
                            }
                        }
                    } catch (UnsupportedEncodingException e3) {
                        e3.printStackTrace();
                        if (reader != null) {
                            reader.close();
                        }
                    }
                } catch (IOException e4) {
                    e4.printStackTrace();
                    if (reader != null) {
                        reader.close();
                    }
                }
                StringBuffer strBuf = new StringBuffer();
                for (int j = 0; j < buf.length; j++) {
                    strBuf.append(buf[j] & 255);
                    if (j != buf.length - 1) {
                        strBuf.append(",");
                    }
                }
                Log.i("meng", "touchupdate>>>cfg_touch=" + strBuf.toString());
                if (i != 90) {
                    this.mMsgText = "data length fail !!!";
                    return -1;
                }
                byte[] mTouchID = Arrays.copyOfRange(buf, 0, 6);
                if (!Arrays.equals(mTouchID, getTouchInfo())) {
                    this.mMsgText = "config id fail !!! \n";
                    return -1;
                }
                MicrontekServer microntekServer = this.mServer;
                microntekServer.setParameters("cfg_touch=" + strBuf.toString());
                return 0;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
//                throw th;
            }
        }
        return -1;
    }

    private byte[] getTouchInfo() {
        int[] touchID = new int[4];
        String touchInfo = this.mServer.getParameters("sta_touch_info=");
        Log.i("meng", "touchupdate>>>touchInfo=" + touchInfo);
        if (touchInfo != null && touchInfo.contains(",")) {
            String[] token = touchInfo.split(",");
            int len = token.length;
            for (int i = 0; i < len; i++) {
                if (i < 4) {
                    try {
                        touchID[i] = Integer.parseInt(token[i]);
                    } catch (Exception e) {
                    }
                }
            }
        }
        byte[] bTouchID = {(byte) (touchID[0] & HCT_CMD.CMD_DBG), (byte) (touchID[1] & HCT_CMD.CMD_DBG), (byte) (touchID[2] & HCT_CMD.CMD_DBG), (byte) ((touchID[2] >> 8) & HCT_CMD.CMD_DBG), (byte) (touchID[3] & HCT_CMD.CMD_DBG), (byte) ((touchID[3] >> 8) & HCT_CMD.CMD_DBG)};
        return bTouchID;
    }

    private int dmcuSwUpdate(String path) {
        File file = new File(path);
        if (!file.isFile() || !file.exists()) {
            return -1;
        }
        try {
            FileInputStream inStream = new FileInputStream(file);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int length = inStream.read(buffer);
                if (length != -1) {
                    stream.write(buffer, 0, length);
                } else {
                    String str = stream.toString();
                    stream.close();
                    inStream.close();
                    Log.i("meng", ">>>cfg_swkey=" + str);
                    MicrontekServer microntekServer = this.mServer;
                    microntekServer.setParameters("cfg_swkey=" + str);
                    return 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:100:0x02d2 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private int dmcuAutoCfgUpdate(String path) {
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            RandomAccessFile randomFile = null;
            try {
                randomFile = new RandomAccessFile(path, "r");
                int length = (int) randomFile.length();
                byte[] buf = new byte[length];
                randomFile.readFully(buf);
                try {
                    randomFile.close();
                    if (length < 525) {
                        Log.i("meng", "dmcu.auto.cfg length fail !!! ");
                        return -1;
                    }
                    if (buf[0] == 77 && buf[1] == 84) {
                        if (buf[2] == 67) {
                            StringBuffer result = new StringBuffer();
                            int start = 3 + 128;
                            byte[] mFactory = Arrays.copyOfRange(buf, 3, start);
                            for (int j = 0; j < mFactory.length; j++) {
                                result.append(mFactory[j] & 255);
                                if (j != mFactory.length - 1) {
                                    result.append(",");
                                }
                            }
                            Log.i("meng", "set cfg_factory=" + result.toString());
                            MicrontekServer microntekServer = this.mServer;
                            microntekServer.setParameters("cfg_factory=" + result.toString());
                            int start2 = start + 64;
                            byte[] mUser = Arrays.copyOfRange(buf, start, start2);
                            StringBuffer result2 = new StringBuffer();
                            for (int j2 = 0; j2 < mUser.length; j2++) {
                                result2.append(mUser[j2] & 255);
                                if (j2 != mUser.length - 1) {
                                    result2.append(",");
                                }
                            }
                            Log.i("meng", "set cfg_user=" + result2.toString());
                            MicrontekServer microntekServer2 = this.mServer;
                            microntekServer2.setParameters("cfg_user=" + result2.toString());
                            int start3 = start2 + 90;
                            byte[] mTouch = Arrays.copyOfRange(buf, start2, start3);
                            StringBuffer result3 = new StringBuffer();
                            for (int j3 = 0; j3 < mTouch.length; j3++) {
                                result3.append(mTouch[j3] & 255);
                                if (j3 != mTouch.length - 1) {
                                    result3.append(",");
                                }
                            }
                            Log.i("meng", "set cfg_touch=" + result3.toString());
                            MicrontekServer microntekServer3 = this.mServer;
                            microntekServer3.setParameters("cfg_touch=" + result3.toString());
                            int i = start3 + 240;
                            int start4 = i;
                            byte[] mKey = Arrays.copyOfRange(buf, start3, i);
                            StringBuffer result4 = new StringBuffer();
                            for (int j4 = 0; j4 < mKey.length; j4++) {
                                result4.append((int) mKey[j4]);
                                if (j4 != mKey.length - 1) {
                                    result4.append(",");
                                }
                            }
                            Log.i("meng", "set cfg_key=" + result4.toString());
                            MicrontekServer microntekServer4 = this.mServer;
                            microntekServer4.setParameters("cfg_key=" + result4.toString());
                            if (length >= 557) {
                                int start5 = start4 + 32;
                                byte[] mOrg = Arrays.copyOfRange(buf, start4, start5);
                                StringBuffer result5 = new StringBuffer();
                                for (int j5 = 0; j5 < mOrg.length; j5++) {
                                    result5.append(mOrg[j5] & 255);
                                    if (j5 != mOrg.length - 1) {
                                        result5.append(",");
                                    }
                                }
                                MicrontekServer microntekServer5 = this.mServer;
                                microntekServer5.setParameters("cfg_org=" + result5.toString());
                                start4 = start5;
                            }
                            if (length >= 605) {
                                byte[] mFactoryExt = Arrays.copyOfRange(buf, start4, start4 + 48);
                                StringBuffer result6 = new StringBuffer();
                                for (int j6 = 0; j6 < mFactoryExt.length; j6++) {
                                    result6.append(mFactoryExt[j6] & 255);
                                    if (j6 != mFactoryExt.length - 1) {
                                        result6.append(",");
                                    }
                                }
                                MicrontekServer microntekServer6 = this.mServer;
                                microntekServer6.setParameters("cfg_factory_ext=" + result6.toString());
                            }
                            Log.i("meng", "dmcu.auto.cfg mFactory:" + mFactory.length + " mUser:" + mUser.length + " mTouch:" + mTouch.length + " mKey:" + mKey.length);
                            Settings.Global.putInt(this.mServer.getContentResolver(), "install_non_market_apps", mFactory[80] & 255);
                            Settings.System.putInt(this.mServer.getContentResolver(), "hctapkupdata", 1);
                            Settings.System.putInt(this.mServer.getContentResolver(), "canbus_updata", 1);
                            return 0;
                        }
                    }
                    Log.i("meng", "dmcu.auto.cfg no M T C ");
                    return -1;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return -1;
                }
            } catch (Exception e) {
                try {
                    e.printStackTrace();
                    Log.i("meng", "dmcuCfgAu readFully fail !!! " + e.getMessage());
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (Exception e12) {
                            e12.printStackTrace();
                            return -1;
                        }
                    }
                    return -1;
                } catch (Throwable e1) {

                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (Exception e13) {
                            e13.printStackTrace();
                            return -1;
                        }
                    }
                    throw e1;
                }
            } catch (Throwable e1) {
                if (randomFile != null) {
                }
                throw e1;
            }
        }
        return -1;
    }
}
