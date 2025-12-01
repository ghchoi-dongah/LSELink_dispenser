package com.dongah.dispenser.basefunction;

import android.os.Environment;
import android.text.TextUtils;

import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.firmware.DiagnosticsStatus;
import com.dongah.dispenser.websocket.ocpp.firmware.FirmwareStatus;
import com.dongah.dispenser.websocket.ocpp.security.SignedFirmwareStatus;
import com.dongah.dispenser.websocket.ocpp.security.UploadLogStatus;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ChargerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ChargerConfiguration.class);

    public static final String CONFIG_FILE_NAME = "config";
    FileManagement fileManagement;

    public String rootPath = "";

    public String MID = "KIOSK1114915545";      // 결제MID

    /** MAX Channel Count */
    public int maxChannel = 2;

    /** 충전기ID, 충전기NO */
    public String chargerId = "";

    /**
     * server connection string
     * test server: ws://dev-connect.lselink.com/ocpp/00000026
     * ws://dev-connect.lselink.com/ocpp/{충전소ID}{충전기ID}
     * 충전소ID : 000000
     * 충전기ID : 26
     * */
    public String serverConnectingString = "ws://dev-connect.lselink.com/ocpp";
    public int serverPort = 4000;

    /** 회원 인증 모드
     * 0: mac
     * 1: member
     * 2: mac + member
     * */
    public String selectAuth = "2";
    public int selectAuthId;

    /** 운영모드
     * 0: test
     * 1: server
     * */
    public String opMode = "0";
    public int opModeId;

    /** device serial port */
    public String controlCom = "/dev/tty20";
    public String rfCom = "/dev/ttyS5";
    public String creditCom = "/dev/ttyS2";

    /** test price */
    public String testPrice = "313.0";

    /** charging point configuration setting */
    public int chargerPointType = 0;                    // 충전기 타입
    public String chargePointVendor = "DONGAH";         // 제조사 코드
    public String chargerPointModel = "DEVD240";        // 충전기 모델ID
    public String chargePointModelName = "";            // 충전기모델명
    public String chargeBoxSerialNumber = "";           // 충전소ID
    public String chargePointSerialNumber = "";         // 충전기 시리얼 번호
    public String firmwareVersion = "";                 // 펌웨어 버전
    public String iccid = "";                           // 모뎀 SIM 카드의 ICCID
    public String imsi = "";                            // 모뎀 SIM 카드의 IMSI
    public String meterSerialNumber = "";               // 충전기의 주전력량계의 시리얼 번호
    public String meterType = "";                       // 충전기의 주전력량계의 타입 포함


    public boolean StopConfirm;
    public boolean signed = true ;
    public FirmwareStatus firmwareStatus = FirmwareStatus.Idle;
    public SignedFirmwareStatus signedFirmwareStatus = SignedFirmwareStatus.Idle;
    public DiagnosticsStatus diagnosticsStatus = DiagnosticsStatus.Idle;
    public UploadLogStatus uploadLogStatus = UploadLogStatus.Idle;

    public ChargerConfiguration() {
        setRootPath(Environment.getExternalStorageDirectory().toString() + File.separator + "Download");
        fileManagement = new FileManagement();
    }

    public void onLoadConfiguration() {
        try {
            File targetFile = new File(GlobalVariables.ROOT_PATH + File.separator + CONFIG_FILE_NAME);
            String configurationString;
            if (!targetFile.exists()) onSaveConfiguration();

            // get file context json string
            configurationString = fileManagement.getStringFromFile(GlobalVariables.ROOT_PATH  + File.separator + CONFIG_FILE_NAME);
            if (!TextUtils.isEmpty(configurationString)) {
                JSONObject obj = new JSONObject(configurationString);

                // TODO
            }
        } catch (Exception e) {
            logger.error("configuration load fail: {}", e.getMessage());
        }
    }

    public void onSaveConfiguration() {
        try {
            JSONObject obj = new JSONObject();
            // TODO
        } catch (Exception e) {
            logger.error("configuration save fail: {}", e.getMessage());
        }
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getMID() {
        return MID;
    }

    public void setMID(String MID) {
        this.MID = MID;
    }

    public int getMaxChannel() {
        return maxChannel;
    }

    public void setMaxChannel(int maxChannel) {
        this.maxChannel = maxChannel;
    }

    public String getChargerId() {
        return chargerId;
    }

    public void setChargerId(String chargerId) {
        this.chargerId = chargerId;
    }

    public String getServerConnectingString() {
        return serverConnectingString;
    }

    public void setServerConnectingString(String serverConnectingString) {
        this.serverConnectingString = serverConnectingString;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getSelectAuth() {
        return selectAuth;
    }

    public void setSelectAuth(String selectAuth) {
        this.selectAuth = selectAuth;
    }

    public int getSelectAuthId() {
        return selectAuthId;
    }

    public void setSelectAuthId(int selectAuthId) {
        this.selectAuthId = selectAuthId;
    }

    public String getOpMode() {
        return opMode;
    }

    public void setOpMode(String opMode) {
        this.opMode = opMode;
    }

    public int getOpModeId() {
        return opModeId;
    }

    public void setOpModeId(int opModeId) {
        this.opModeId = opModeId;
    }

    public String getControlCom() {
        return controlCom;
    }

    public void setControlCom(String controlCom) {
        this.controlCom = controlCom;
    }

    public String getRfCom() {
        return rfCom;
    }

    public void setRfCom(String rfCom) {
        this.rfCom = rfCom;
    }

    public String getCreditCom() {
        return creditCom;
    }

    public void setCreditCom(String creditCom) {
        this.creditCom = creditCom;
    }

    public String getTestPrice() {
        return testPrice;
    }

    public void setTestPrice(String testPrice) {
        this.testPrice = testPrice;
    }

    public int getChargerPointType() {
        return chargerPointType;
    }

    public void setChargerPointType(int chargerPointType) {
        this.chargerPointType = chargerPointType;
    }

    public String getChargePointVendor() {
        return chargePointVendor;
    }

    public void setChargePointVendor(String chargePointVendor) {
        this.chargePointVendor = chargePointVendor;
    }

    public String getChargerPointModel() {
        return chargerPointModel;
    }

    public void setChargerPointModel(String chargerPointModel) {
        this.chargerPointModel = chargerPointModel;
    }

    public String getChargePointModelName() {
        return chargePointModelName;
    }

    public void setChargePointModelName(String chargePointModelName) {
        this.chargePointModelName = chargePointModelName;
    }

    public String getChargeBoxSerialNumber() {
        return chargeBoxSerialNumber;
    }

    public void setChargeBoxSerialNumber(String chargeBoxSerialNumber) {
        this.chargeBoxSerialNumber = chargeBoxSerialNumber;
    }

    public String getChargePointSerialNumber() {
        return chargePointSerialNumber;
    }

    public void setChargePointSerialNumber(String chargePointSerialNumber) {
        this.chargePointSerialNumber = chargePointSerialNumber;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public String getMeterType() {
        return meterType;
    }

    public void setMeterType(String meterType) {
        this.meterType = meterType;
    }

    public boolean isStopConfirm() {
        return StopConfirm;
    }

    public void setStopConfirm(boolean stopConfirm) {
        this.StopConfirm = stopConfirm;
    }

    public FirmwareStatus getFirmwareStatus() {
        return firmwareStatus;
    }

    public void setFirmwareStatus(FirmwareStatus firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    public DiagnosticsStatus getDiagnosticsStatus() {
        return diagnosticsStatus;
    }

    public void setDiagnosticsStatus(DiagnosticsStatus diagnosticsStatus) {
        this.diagnosticsStatus = diagnosticsStatus;
    }

    public UploadLogStatus getUploadLogStatus() {
        return uploadLogStatus;
    }

    public void setUploadLogStatus(UploadLogStatus uploadLogStatus) {
        this.uploadLogStatus = uploadLogStatus;
    }

    public SignedFirmwareStatus getSignedFirmwareStatus() {
        return signedFirmwareStatus;
    }

    public void setSignedFirmwareStatus(SignedFirmwareStatus signedFirmwareStatus) {
        this.signedFirmwareStatus = signedFirmwareStatus;
    }
}
