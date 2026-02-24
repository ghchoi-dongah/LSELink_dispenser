package com.dongah.dispenser.basefunction;

import android.os.Environment;

import com.dongah.dispenser.websocket.ocpp.localauthlist.UpdateStatus;
import com.dongah.dispenser.websocket.ocpp.security.HashAlgorithm;

import java.io.File;

public class GlobalVariables {

    //storage/emulated/0/download
    public static String ROOT_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";

    public static String VERSION = "1.0.0";
    public static String FW_VERSION = "1.0.1";

    public static final String UNIT_FILE_NAME = "unitPrice.dongah";

    /**
     * Max plug count
     */
    public static int maxChannel = 2;
    public static int maxPlugCount = 3;
    public static boolean CONNECT_RETRY = false;
    public static boolean[] ChargerOperation = new boolean[maxPlugCount];
    public static UpdateStatus updateStatus;

    /**
     * ocpp configuration key
     */
    public static boolean notSupportedKey = false;
    public static int ConnectionTimeOut = 60;
    public static int MinimunStatusDuration = 300;
    public static int MeterValueSampleInterval = 60;
    public static int HeartBeatInterval = 60;
    public static boolean AuthorizeRemoteTxRequests = false;
    public static boolean ReserveConnectorZeroSupported = true;
    public static boolean LocalPreAuthorize = false;
    public static boolean AllowOfflineTxForUnknownId = false;
    public static boolean StopTransactionOnInvalidId = false;
    public static boolean StopTransactionOnEVSideDisconnect = true;
    public static boolean UnlockConnectorOnEVSideDisconnect = true;
    public static int ClockAlignedDataInterval = 0;
    public static boolean LocalAuthorizeOffline = false;
    public static String SecurityProfile = "0";
    public static String AuthorizationKey = "dongahpassword1234";
    public static boolean LocalAuthListEnabled = true;

    public static int FullRechgAmt = 40000;
    public static  String PersonUtztnLmtYn = "N";
    public static int PersonUtztnLmtHr = 30;

    public static int dumpTransactionId = 0;


    public static String getRootPath() {
        return ROOT_PATH;
    }

    public static void setRootPath(String rootPath) {
        ROOT_PATH = rootPath;
    }

    public static boolean isConnectRetry() {
        return CONNECT_RETRY;
    }

    public static void setConnectRetry(boolean connectRetry) {
        CONNECT_RETRY = connectRetry;
    }

    public static boolean isNotSupportedKey() {
        return notSupportedKey;
    }

    public static void setNotSupportedKey(boolean notSupportedKey) {
        GlobalVariables.notSupportedKey = notSupportedKey;
    }

    public static String getAuthorizationKey() {
        return AuthorizationKey;
    }

    public static void setAuthorizationKey(String authorizationKey) {
        AuthorizationKey = authorizationKey;
    }

    public static boolean isLocalAuthListEnabled() {
        return LocalAuthListEnabled;
    }

    public static void setLocalAuthListEnabled(boolean localAuthListEnabled) {
        LocalAuthListEnabled = localAuthListEnabled;
    }


    public static int getConnectionTimeOut() {
        return ConnectionTimeOut;
    }

    public static void setConnectionTimeOut(int connectionTimeOut) {
        ConnectionTimeOut = connectionTimeOut;
    }

    public static int getMinimunStatusDuration() {
        return MinimunStatusDuration;
    }

    public static void setMinimunStatusDuration(int minimunStatusDuration) {
        MinimunStatusDuration = minimunStatusDuration;
    }

    public static int getMeterValueSampleInterval() {
        return MeterValueSampleInterval;
    }

    public static void setMeterValueSampleInterval(int meterValueSampleInterval) {
        MeterValueSampleInterval = meterValueSampleInterval;
    }

    public static int getHeartBeatInterval() {
        return HeartBeatInterval;
    }

    public static void setHeartBeatInterval(int heartBeatInterval) {
        HeartBeatInterval = heartBeatInterval;
    }

    public static boolean isAuthorizeRemoteTxRequests() {
        return AuthorizeRemoteTxRequests;
    }

    public static void setAuthorizeRemoteTxRequests(boolean authorizeRemoteTxRequests) {
        AuthorizeRemoteTxRequests = authorizeRemoteTxRequests;
    }

    public static boolean isReserveConnectorZeroSupported() {
        return ReserveConnectorZeroSupported;
    }

    public static void setReserveConnectorZeroSupported(boolean reserveConnectorZeroSupported) {
        ReserveConnectorZeroSupported = reserveConnectorZeroSupported;
    }

    public static boolean isLocalPreAuthorize() {
        return LocalPreAuthorize;
    }

    public static void setLocalPreAuthorize(boolean localPreAuthorize) {
        LocalPreAuthorize = localPreAuthorize;
    }

    public static boolean isAllowOfflineTxForUnknownId() {
        return AllowOfflineTxForUnknownId;
    }

    public static void setAllowOfflineTxForUnknownId(boolean allowOfflineTxForUnknownId) {
        AllowOfflineTxForUnknownId = allowOfflineTxForUnknownId;
    }

    public static boolean isStopTransactionOnInvalidId() {
        return StopTransactionOnInvalidId;
    }

    public static void setStopTransactionOnInvalidId(boolean stopTransactionOnInvalidId) {
        StopTransactionOnInvalidId = stopTransactionOnInvalidId;
    }

    public static boolean isStopTransactionOnEVSideDisconnect() {
        return StopTransactionOnEVSideDisconnect;
    }

    public static void setStopTransactionOnEVSideDisconnect(boolean stopTransactionOnEVSideDisconnect) {
        StopTransactionOnEVSideDisconnect = stopTransactionOnEVSideDisconnect;
    }

    public static boolean isUnlockConnectorOnEVSideDisconnect() {
        return UnlockConnectorOnEVSideDisconnect;
    }

    public static void setUnlockConnectorOnEVSideDisconnect(boolean unlockConnectorOnEVSideDisconnect) {
        UnlockConnectorOnEVSideDisconnect = unlockConnectorOnEVSideDisconnect;
    }

    public static int getClockAlignedDataInterval() {
        return ClockAlignedDataInterval;
    }

    public static void setClockAlignedDataInterval(int clockAlignedDataInterval) {
        ClockAlignedDataInterval = clockAlignedDataInterval;
    }

    public static boolean isLocalAuthorizeOffline() {
        return LocalAuthorizeOffline;
    }

    public static void setLocalAuthorizeOffline(boolean localAuthorizeOffline) {
        LocalAuthorizeOffline = localAuthorizeOffline;
    }

    public static String getSecurityProfile() {
        return SecurityProfile;
    }

    public static void setSecurityProfile(String securityProfile) {
        SecurityProfile = securityProfile;
    }

    public static int getFullRechgAmt() {
        return FullRechgAmt;
    }

    public static void setFullRechgAmt(int fullRechgAmt) {
        FullRechgAmt = fullRechgAmt;
    }

    public static String getPersonUtztnLmtYn() {
        return PersonUtztnLmtYn;
    }

    public static void setPersonUtztnLmtYn(String personUtztnLmtYn) {
        PersonUtztnLmtYn = personUtztnLmtYn;
    }

    public static int getPersonUtztnLmtHr() {
        return PersonUtztnLmtHr;
    }

    public static void setPersonUtztnLmtHr(int personUtztnLmtHr) {
        PersonUtztnLmtHr = personUtztnLmtHr;
    }

    public static int getDumpTransactionId() {
        return dumpTransactionId;
    }

    public static void setDumpTransactionId(int dumpTransactionId) {
        GlobalVariables.dumpTransactionId = dumpTransactionId;
    }
}
