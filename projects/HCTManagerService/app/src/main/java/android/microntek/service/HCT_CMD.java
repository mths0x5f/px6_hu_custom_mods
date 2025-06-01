package android.microntek.service;

public class HCT_CMD {

    public static final int CHAR_CONV = 251;
    public static final int CHAR_CONV_CONV = 235;
    public static final int CHAR_CONV_START = 234;
    public static final int CHAR_START = 250;
    public static final int CMD_AV = 16;
    public static final int CMD_CANBUS = 21;
    public static final int CMD_CAR = 22;
    public static final int CMD_DBG = 255;
    public static final int CMD_DTV = 18;
    public static final int CMD_DVD = 19;
    public static final int CMD_INT = 31;
    public static final int CMD_RADIO = 17;

    public static class AvCmd {
        public static final int AV_A_CMD_ACTIVE = 13;
        public static final int AV_A_CMD_BALANCE = 12;
        public static final int AV_A_CMD_DEINIT = 1;
        public static final int AV_A_CMD_ENTER_CHANNEL = 2;
        public static final int AV_A_CMD_EQ = 10;
        public static final int AV_A_CMD_EXIT_CHANNEL = 3;
        public static final int AV_A_CMD_GPS_GAIN = 17;
        public static final int AV_A_CMD_GPS_MONITOR = 15;
        public static final int AV_A_CMD_GPS_ONTOP = 14;
        public static final int AV_A_CMD_GPS_SWITCH = 16;
        public static final int AV_A_CMD_INIT = 0;
        public static final int AV_A_CMD_LUD = 11;
        public static final int AV_A_CMD_MUTE = 8;
        public static final int AV_A_CMD_PHONE_MODE = 4;
        public static final int AV_A_CMD_UNMUTE = 9;
        public static final int AV_A_CMD_VOLUME = 5;
        public static final int AV_A_CMD_VOLUME_BACKVIEW = 7;
        public static final int AV_A_CMD_VOLUME_PHONE = 6;
        public static final int AV_MSG_AJX_ENTER = 196;
        public static final int AV_MSG_AJX_EXIT = 197;
        public static final int AV_MSG_BACKVIEW_ENTER = 198;
        public static final int AV_MSG_BACKVIEW_EXIT = 199;
        public static final int AV_MSG_DEINIT = 193;
        public static final int AV_MSG_DTV_MUTE = 204;
        public static final int AV_MSG_DTV_UNMUTE = 205;
        public static final int AV_MSG_DVD_MUTE = 202;
        public static final int AV_MSG_DVD_UNMUTE = 203;
        public static final int AV_MSG_INIT = 192;
        public static final int AV_MSG_RADIO_MUTE = 200;
        public static final int AV_MSG_RADIO_UNMUTE = 201;
        public static final int AV_MSG_TA_ENTER = 194;
        public static final int AV_MSG_TA_EXIT = 195;
        public static final int AV_MSG_TIMEOUT = 207;
        public static final int AV_MSG_VIDEO_CHANNEL = 206;
        public static final int AV_M_CMD_AUDIO_OFF = 19;
        public static final int AV_M_CMD_AUDIO_ON = 18;
        public static final int AV_M_RPT_AUDIO_MUTE = 20;
    }

    public static class CanBusCmd {
        public static final int CANBUS_A_REQ_LIST = 33;
        public static final int CANBUS_A_REQ_MENU_LIST = 34;
        public static final int CANBUS_A_RSP = 32;
        public static final int CANBUS_M_CMD = 0;
        public static final int CANBUS_M_RPT_LIST = 1;
        public static final int CANBUS_M_RPT_MENU_END = 24;
    }

    public static class CarCmd {
        public static final int CAR_A_CFG_BACKLIGHT = 40;
        public static final int CAR_A_CFG_BEEP = 39;
        public static final int CAR_A_CFG_BL_MODE = 45;
        public static final int CAR_A_CFG_CANBUS_AMP = 48;
        public static final int CAR_A_CFG_CANBUS_MODEL = 47;
        public static final int CAR_A_CFG_CANBUS_SWAP = 49;
        public static final int CAR_A_CFG_COLOR = 37;
        public static final int CAR_A_CFG_DIM = 42;
        public static final int CAR_A_CFG_FACTORY = 32;
        public static final int CAR_A_CFG_KEY = 34;
        public static final int CAR_A_CFG_MIRROR = 43;
        public static final int CAR_A_CFG_MULTI_COLOR = 44;
        public static final int CAR_A_CFG_POWER_DELAY = 41;
        public static final int CAR_A_CFG_RGB = 38;
        public static final int CAR_A_CFG_SWKEY = 35;
        public static final int CAR_A_CFG_TOUCH = 36;
        public static final int CAR_A_CFG_USER = 33;
        public static final int CAR_A_CFG_USER_PART = 104;
        public static final int CAR_A_CFG_VIDEO_SAFE = 46;
        public static final int CAR_A_CMD_BACKVIEW_START = 15;
        public static final int CAR_A_CMD_BACKVIEW_STOP = 16;
        public static final int CAR_A_CMD_BEEP = 14;
        public static final int CAR_A_CMD_CAPTURE_OFF = 106;
        public static final int CAR_A_CMD_CAPTURE_ON = 105;
        public static final int CAR_A_CMD_KEY_MODE = 7;
        public static final int CAR_A_CMD_KEY_PRESS = 6;
        public static final int CAR_A_CMD_PHONE_MODE = 109;
        public static final int CAR_A_CMD_RESET = 181;
        public static final int CAR_A_REQ_TOUCH = 56;
        public static final int CAR_A_RPT_ACC_OFF = 11;
        public static final int CAR_A_RPT_ACC_ON = 10;
        public static final int CAR_A_RPT_ANDROID = 3;
        public static final int CAR_A_RPT_BACKVIEW_START = 12;
        public static final int CAR_A_RPT_BACKVIEW_STOP = 13;
        public static final int CAR_A_RPT_BOOTCOMPLETE = 5;
        public static final int CAR_A_RPT_BOOTMODE = 0;
        public static final int CAR_A_RPT_KERNEL = 1;
        public static final int CAR_A_RPT_LOGO = 2;
        public static final int CAR_A_RPT_OFF = 9;
        public static final int CAR_A_RPT_ON = 8;
        public static final int CAR_A_RPT_RECOVERY = 4;
        public static final int CAR_A_RPT_RESTART = 182;
        public static final int CAR_A_RPT_TOUCH_PRESS = 102;
        public static final int CAR_A_RPT_TOUCH_RELEASE = 103;
        public static final int CAR_MSG_K0_LONG = 195;
        public static final int CAR_MSG_K0_SHORT = 194;
        public static final int CAR_MSG_KEY = 192;
        public static final int CAR_MSG_RESET = 193;
        public static final int CAR_M_CFG_FACTORY = 88;
        public static final int CAR_M_CFG_KEY = 90;
        public static final int CAR_M_CFG_SWKEY = 91;
        public static final int CAR_M_CFG_TOUCH = 92;
        public static final int CAR_M_CFG_USER = 89;
        public static final int CAR_M_CMD_ACC_OFF = 73;
        public static final int CAR_M_CMD_AJX_START = 69;
        public static final int CAR_M_CMD_AJX_STOP = 70;
        public static final int CAR_M_CMD_BACKLIGHT = 77;
        public static final int CAR_M_CMD_BACKVIEW_START = 67;
        public static final int CAR_M_CMD_BACKVIEW_STOP = 68;
        public static final int CAR_M_CMD_BRAKE = 75;
        public static final int CAR_M_CMD_BRIGHT_ADJ = 83;
        public static final int CAR_M_CMD_CAPTURE_OFF = 108;
        public static final int CAR_M_CMD_CAPTURE_ON = 107;
        public static final int CAR_M_CMD_FIRST_START = 110;
        public static final int CAR_M_CMD_ILL = 74;
        public static final int CAR_M_CMD_IPOD = 76;
        public static final int CAR_M_CMD_KEY_PRESS = 78;
        public static final int CAR_M_CMD_OFF = 65;
        public static final int CAR_M_CMD_ON = 64;
        public static final int CAR_M_CMD_SHUTDOWN = 72;
        public static final int CAR_M_CMD_SLEEP = 66;
        public static final int CAR_M_CMD_START_APP = 81;
        public static final int CAR_M_CMD_START_DVD = 79;
        public static final int CAR_M_CMD_START_IPOD = 80;
        public static final int CAR_M_CMD_TOUCH_MODE = 82;
        public static final int CAR_M_REQ_TIME = 152;
        public static final int CAR_M_RPT_CUSTOMER_ID = 85;
        public static final int CAR_M_RPT_KEY_STUDY_LONG = 97;
        public static final int CAR_M_RPT_KEY_STUDY_SHORT = 96;
        public static final int CAR_M_RPT_MAX_VOLUME = 101;
        public static final int CAR_M_RPT_PASSWORD = 84;
        public static final int CAR_M_RPT_SW_STUDY_LONG = 99;
        public static final int CAR_M_RPT_SW_STUDY_SHORT = 98;
        public static final int CAR_M_RPT_TOUCH_NOT_FOUND = 100;
        public static final int CAR_M_RPT_VERSION = 86;
    }

    public static class DVD_Key {
        public static final int DVD_A_CMD_MUTE = 19;
        public static final int DVD_A_CMD_UNMUTE = 20;
        public static final int DVD_A_INT_MSG = 18;
        public static final int DVD_A_RPT_DVD_TYPE = 17;
        public static final int DVD_A_RSP = 16;
        public static final int DVD_MSG_ACC_OFF = 133;
        public static final int DVD_MSG_ACC_ON = 132;
        public static final int DVD_MSG_EJECT = 130;
        public static final int DVD_MSG_EJECT_FORCE = 131;
        public static final int DVD_MSG_OFF = 128;
        public static final int DVD_MSG_ON = 129;
        public static final int DVD_M_CMD = 3;
        public static final int DVD_M_CMD_EJECT_REQUEST = 2;
        public static final int DVD_M_RPT_DOOR = 4;
        public static final int DVD_M_RPT_DVD_TYPE = 5;
        public static final int DVD_M_RPT_OFF = 7;
        public static final int DVD_M_RPT_ON = 6;
    }

    public static class INT_Cmd {
        public static final int INT_A_TOUCHDOWN = 3;
        public static final int INT_A_TOUCHUP = 4;
        public static final int INT_A_TOUCH_KEY = 1;
        public static final int INT_A_TOUCH_XY = 0;
        public static final int INT_A_VIDEO_SIGNAL = 2;
    }

    public static class RadioCmd {
        public static final int RADIO_A_CMD_AF_EN = 7;
        public static final int RADIO_A_CMD_FREQ = 12;
        public static final int RADIO_A_CMD_GET_NAME = 13;
        public static final int RADIO_A_CMD_LOC_EN = 10;
        public static final int RADIO_A_CMD_PTY_EN = 9;
        public static final int RADIO_A_CMD_RDS_EN = 6;
        public static final int RADIO_A_CMD_SEEK_AUTO = 2;
        public static final int RADIO_A_CMD_SEEK_DOWN = 1;
        public static final int RADIO_A_CMD_SEEK_STOP = 3;
        public static final int RADIO_A_CMD_SEEK_UP = 0;
        public static final int RADIO_A_CMD_ST_EN = 11;
        public static final int RADIO_A_CMD_TA_EN = 8;
        public static final int RADIO_A_CMD_TUNE_DOWN = 5;
        public static final int RADIO_A_CMD_TUNE_UP = 4;
        public static final int RADIO_A_REQ_INFO = 43;
        public static final int RADIO_MSG_AF_FREG = 192;
        public static final int RADIO_MSG_DEINIT = 194;
        public static final int RADIO_MSG_INIT = 193;
        public static final int RADIO_MSG_OFF = 196;
        public static final int RADIO_MSG_ON = 195;
        public static final int RADIO_MSG_RDS_SEM = 197;
        public static final int RADIO_M_RPT_FREQ = 37;
        public static final int RADIO_M_RPT_FREQ_AM_MAX = 45;
        public static final int RADIO_M_RPT_FREQ_AM_MIN = 46;
        public static final int RADIO_M_RPT_FREQ_FM_MAX = 47;
        public static final int RADIO_M_RPT_FREQ_FM_MIN = 48;
        public static final int RADIO_M_RPT_FREQ_TABLE = 44;
        public static final int RADIO_M_RPT_NAME = 42;
        public static final int RADIO_M_RPT_PI = 40;
        public static final int RADIO_M_RPT_PSN = 38;
        public static final int RADIO_M_RPT_PTY = 39;
        public static final int RADIO_M_RPT_RT = 35;
        public static final int RADIO_M_RPT_SEEK_END = 32;
        public static final int RADIO_M_RPT_SEEK_END_AUTO = 34;
        public static final int RADIO_M_RPT_SEEK_FOUND = 33;
        public static final int RADIO_M_RPT_SEEK_FOUND_AUTO = 53;
        public static final int RADIO_M_RPT_SEEK_START = 51;
        public static final int RADIO_M_RPT_SEEK_START_AUTO = 52;
        public static final int RADIO_M_RPT_ST = 41;
        public static final int RADIO_M_RPT_TA_ENTER = 49;
        public static final int RADIO_M_RPT_TA_EXIT = 50;
        public static final int RADIO_M_RPT_TP = 36;
    }

    public static class TV_CMD {
        public static final int ATV_A_CMD_FREQ = 7;
        public static final int ATV_A_CMD_MODE = 6;
        public static final int ATV_A_CMD_SEEK_AUTO = 2;
        public static final int ATV_A_CMD_SEEK_DOWN = 1;
        public static final int ATV_A_CMD_SEEK_STOP = 3;
        public static final int ATV_A_CMD_SEEK_UP = 0;
        public static final int ATV_A_CMD_TUNE_DOWN = 5;
        public static final int ATV_A_CMD_TUNE_UP = 4;
        public static final int ATV_M_RPT_FREQ = 18;
        public static final int ATV_M_RPT_SEEK_END = 15;
        public static final int ATV_M_RPT_SEEK_END_AUTO = 17;
        public static final int ATV_M_RPT_SEEK_FOUND = 16;
        public static final int ATV_M_RPT_SEEK_FOUND_AUTO = 21;
        public static final int ATV_M_RPT_SEEK_START = 19;
        public static final int ATV_M_RPT_SEEK_START_AUTO = 20;
        public static final int DTV_A_CMD_CMD = 13;
        public static final int DTV_A_CMD_IR = 9;
        public static final int DTV_A_CMD_XY = 14;
        public static final int TV_A_CMD_GET_NAME = 8;
        public static final int TV_A_REQ_LIST = 10;
        public static final int TV_MSG_OFF = 128;
        public static final int TV_MSG_ON = 129;
        public static final int TV_M_RPT_LIST = 12;
        public static final int TV_M_RPT_TYPE = 11;
    }

}
