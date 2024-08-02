package com.hibory.uhfreaderlib.consts;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


public class Constants {
    public static final byte FRAME_BEGIN = -69;
    public static final byte FRAME_END = 126;
    public static final byte FRAME_TYPE_CMD = 0;
    public static final byte FRAME_TYPE_ANS = 1;
    public static final byte FRAME_TYPE_INFO = 2;
    public static final byte CMD_GET_MODULE_INFO = 3;
    public static final byte CMD_SET_QUERY = 14;
    public static final byte CMD_GET_QUERY = 13;
    public static final byte CMD_INVENTORY = 34;
    public static final byte CMD_READ_MULTI = 39;
    public static final byte CMD_STOP_MULTI = 40;
    public static final byte CMD_READ_DATA = 57;
    public static final byte CMD_WRITE_DATA = 73;
    public static final byte CMD_LOCK_UNLOCK = -126;
    public static final byte CMD_KILL = 101;
    public static final byte CMD_SET_REGION = 7;
    public static final byte CMD_SET_RF_CHANNEL = -85;
    public static final byte CMD_GET_RF_CHANNEL = -86;
    public static final byte CMD_SET_POWER = -74;
    public static final byte CMD_GET_POWER = -73;
    public static final byte CMD_SET_FHSS = -83;
    public static final byte CMD_SET_CW = -80;
    public static final byte CMD_SET_MODEM_PARA = -16;
    public static final byte CMD_READ_MODEM_PARA = -15;
    public static final byte CMD_SET_SELECT_PARA = 12;
    public static final byte CMD_GET_SELECT_PARA = 11;
    public static final byte CMD_SET_INVENTORY_MODE = 18;
    public static final byte CMD_SCAN_JAMMER = -14;
    public static final byte CMD_SCAN_RSSI = -13;
    public static final byte CMD_IO_CONTROL = 26;
    public static final byte CMD_RESTART = 25;
    public static final byte CMD_SET_READER_ENV_MODE = -11;
    public static final byte CMD_INSERT_FHSS_CHANNEL = -87;
    public static final byte CMD_SLEEP_MODE = 23;
    public static final byte CMD_SET_SLEEP_TIME = 29;
    public static final byte CMD_LOAD_NV_CONFIG = 10;
    public static final byte CMD_SAVE_NV_CONFIG = 9;
    public static final byte CMD_NXP_CHANGE_CONFIG = -32;
    public static final byte CMD_NXP_READPROTECT = -31;
    public static final byte CMD_NXP_RESET_READPROTECT = -30;
    public static final byte CMD_NXP_CHANGE_EAS = -29;
    public static final byte CMD_NXP_EAS_ALARM = -28;
    public static final byte CMD_EXE_FAILED = -1;
    public static final byte FAIL_INVALID_PARA = 14;
    public static final byte FAIL_INVENTORY_TAG_TIMEOUT = 21;
    public static final byte FAIL_INVALID_CMD = 23;
    public static final byte FAIL_FHSS_FAIL = 32;
    public static final byte FAIL_ACCESS_PWD_ERROR = 22;
    public static final byte FAIL_READ_MEMORY_NO_TAG = 9;
    public static final byte FAIL_READ_ERROR_CODE_BASE = -96;
    public static final byte FAIL_WRITE_MEMORY_NO_TAG = 16;
    public static final byte FAIL_WRITE_ERROR_CODE_BASE = -80;
    public static final byte FAIL_LOCK_NO_TAG = 19;
    public static final byte FAIL_LOCK_ERROR_CODE_BASE = -64;
    public static final byte FAIL_KILL_NO_TAG = 18;
    public static final byte FAIL_KILL_ERROR_CODE_BASE = -48;
    public static final byte FAIL_NXP_CHANGE_CONFIG_NO_TAG = 26;
    public static final byte FAIL_NXP_READPROTECT_NO_TAG = 42;
    public static final byte FAIL_NXP_RESET_READPROTECT_NO_TAG = 43;
    public static final byte FAIL_NXP_CHANGE_EAS_NO_TAG = 27;
    public static final byte FAIL_NXP_CHANGE_EAS_NOT_SECURE = 28;
    public static final byte FAIL_NXP_EAS_ALARM_NO_TAG = 29;
    public static final byte FAIL_CUSTOM_CMD_BASE = -32;
    public static final byte ERROR_CODE_OTHER_ERROR = 0;
    public static final byte ERROR_CODE_MEM_OVERRUN = 3;
    public static final byte ERROR_CODE_MEM_LOCKED = 4;
    public static final byte ERROR_CODE_INSUFFICIENT_POWER = 11;
    public static final byte ERROR_CODE_NON_SPEC_ERROR = 15;
    public static final byte SUCCESS_MSG_DATA = 0;
    public static final byte REGION_CODE_CHN2 = 1;
    public static final byte REGION_CODE_US = 2;
    public static final byte REGION_CODE_EUR = 3;
    public static final byte REGION_CODE_CHN1 = 4;
    public static final byte REGION_CODE_JAPAN = 5;
    public static final byte REGION_CODE_KOREA = 6;
    public static final byte SET_ON = -1;
    public static final byte SET_OFF = 0;
    public static final byte INVENTORY_MODE0 = 0;
    public static final byte INVENTORY_MODE1 = 1;
    public static final byte INVENTORY_MODE2 = 2;
    public static final byte MODULE_HARDWARE_VERSION_FIELD = 0;
    public static final byte MODULE_SOFTWARE_VERSION_FIELD = 1;
    public static final byte MODULE_MANUFACTURE_INFO_FIELD = 2;

    public Constants() {
    }
}
