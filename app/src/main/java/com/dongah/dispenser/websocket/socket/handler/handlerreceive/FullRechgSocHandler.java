package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.DataTransferStatus;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.ChangeModeThread;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FullRechgSocHandler implements OcppHandler {
    private static final Logger logger = LoggerFactory.getLogger(FullRechgSocHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        try {
            DataTransferStatus status = DataTransferStatus.valueOf(payload.getString("status"));
            String dataStr = payload.getString("data");

            if (status.equals(DataTransferStatus.Accepted)) {

                // 저장
                FileManagement fileManagement = new FileManagement();
                fileManagement.stringToFileSave(GlobalVariables.getRootPath(), GlobalVariables.FILE_FULL_RECHG_SOC, dataStr, false);

                // SoC 설정
                setFullRechgSoc(connectorId);
            }
        } catch (Exception e) {
            logger.error("FullRechgSocHandler error : {}", e.getMessage(), e);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setFullRechgSoc(int connectorId) {

        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);

            File file = new File(GlobalVariables.getRootPath() + File.separator + GlobalVariables.FILE_FULL_RECHG_SOC);
            String content = readFile(file);
            logger.info("setFullRechgSoc connectorId[{}] content : {}", connectorId, content);

            JSONArray jsonArray = new JSONArray(content);
            ZonedDateTimeConvert convert = new ZonedDateTimeConvert();
            ZonedDateTime now = convert.doGetCurrentTime();
            if (now == null) return;

            String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int hour = now.getHour();
            @SuppressLint("DefaultLocale") String hourKey = String.format("HH%02d", hour);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                if (today.equals(obj.optString("day", ""))) {

                    // 키가 없을 때 JSONException 방지
                    String value = obj.optString(hourKey, null);
                    logger.info("setFullRechgSoc connectorId[{}] soc : {}", connectorId, value);

                    if (value == null || value.isEmpty()) {
                        // 키가 없으면 DT(changemode) rechgAmt 조회
                        logger.info("setFullRechgSoc connectorId[{}] value is null. changeMode start", connectorId);
                        ChangeModeThread.setRechgAmt(connectorId);
                    } else {
                        // 키가 존재
                        chargingCurrentData.setLimitSoc(Integer.parseInt(value));
                        logger.info("setFullRechgSoc connectorId[{}] LimitSoc : {}",
                                connectorId, chargingCurrentData.getLimitSoc());
                    }

                    break;
                }
            }
        } catch (Exception e) {
            logger.error("setFullRechgSoc error : {}", e.getMessage(), e);
        }
    }


    private static String readFile(File file) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
}
