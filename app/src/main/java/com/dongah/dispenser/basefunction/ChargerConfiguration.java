package com.dongah.dispenser.basefunction;

import android.os.Environment;
import android.text.TextUtils;

import com.dongah.dispenser.utils.FileManagement;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ChargerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ChargerConfiguration.class);

    public static final String CONFIG_FILE_NAME = "config";
    FileManagement fileManagement;

    public String rootPath = "";

    public String MID = "";

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

    // TODO: 충전기 타입

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

    /** test price */
    public String testPrice = "313.0";

    /** charging point configuration setting */
    // TODO
    public String vendorId = "DONGAH";
    public String chargerPointModel = "DEVD240";

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
            }
        } catch (Exception e) {
            logger.error("configuration load fail: {}", e.getMessage());
        }
    }

    public void onSaveConfiguration() {
        try {
            JSONObject obj = new JSONObject();
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
}
