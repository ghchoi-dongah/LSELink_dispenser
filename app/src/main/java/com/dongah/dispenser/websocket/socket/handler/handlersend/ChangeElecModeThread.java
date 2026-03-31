package com.dongah.dispenser.websocket.socket.handler.handlersend;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

public class ChangeElecModeThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ChangeElecModeThread.class);

    private volatile boolean stopped = false;

    public void stopThread() {
        stopped = true;
        interrupt();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        logger.info("ChangeElecModeThread start");
        processChangeElecMode();
        while (!stopped && !isInterrupted()) {
            try {
                Thread.sleep(1000);

                ZonedDateTimeConvert convert = new ZonedDateTimeConvert();
                ZonedDateTime now = convert.doGetCurrentTime();

                int minute = now.getMinute();
                int second = now.getSecond();

                // 정각일 때 충전 모드 변경
                if (minute == 0 && second == 0) {
                    processChangeElecMode();
                }
            } catch (Exception e) {
                logger.info("ChangeElecModeThread error : {}", e.getMessage());
            }
        }
        logger.info("ChangeElecModeThread terminated");
    }

    // 시간대별 충전제한
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void processChangeElecMode() {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        try {
            // 1. changeElecMode 파일 유무 확인
            File file = new File(GlobalVariables.getRootPath() + File.separator + "changeElecMode");

            if (!file.exists()) {
                // 2. 파일이 없으면 config 기본값 설정
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    processChangeMode(i);
                }
            } else {
                // 3. 파일이 있는 경우(채널별 충전제한 설정)
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    String content = readFile(file, i+1);

                    if (content == null) {
                        processChangeMode(i);
                    } else {
                        setChangeElecMode(i, content);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("processChangeElecMode error : {}", e.getMessage());
        }
    }
    private static String readFile(File file, int connectorId) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        // 파일 전체 JSON 파싱
        JSONObject rootJson = new JSONObject(stringBuilder.toString());

        String key = String.valueOf(connectorId);

        // 해당 connectorId 존재 여부 확인
        if (!rootJson.has(key)) {
            return null;
        }

        JSONObject connectorJson = rootJson.getJSONObject(key);

        // 해당 connector 데이터만 문자열로 반환
        return connectorJson.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void setChangeElecMode(int connectorId, String content) {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        try {
            JSONObject rootJson = new JSONObject(content);
            ZonedDateTime now = new ZonedDateTimeConvert().doGetCurrentTime();
            if (now == null) return;

            int hour = now.getHour();
            @SuppressLint("DefaultLocale") String hourKey = String.format("HH%02d", hour);

            String value = rootJson.optString(hourKey, null);
            if (value == null || value.isEmpty()) {
                processChangeMode(connectorId);
            } else {
                activity.getControlBoard().getTxData(connectorId).setOutPowerLimit((short) Integer.parseInt(value));
            }

        } catch (Exception e) {
            logger.error("setChangeElecMode error : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void processChangeMode(int connectorId) {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        try {
            File file = new File(GlobalVariables.getRootPath() + File.separator + "changeMode");

            if (!file.exists()) {
                activity.getControlBoard().getTxData(connectorId).setOutPowerLimit((short) chargerConfiguration.getDr());
            } else {
                String content = readFile(file, connectorId+1);
                if (content == null) {
                    activity.getControlBoard().getTxData(connectorId).setOutPowerLimit((short) chargerConfiguration.getDr());
                } else {
                    setChangeMode(connectorId, content);
                }
            }
        } catch (Exception e) {
            logger.error("processChangeMode error : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void setChangeMode(int connectorId, String content) {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        try {
            JSONObject rootJson = new JSONObject(content);

            String value = rootJson.optString("rechgElec", String.valueOf(chargerConfiguration.getDr()));
            activity.getControlBoard().getTxData(connectorId).setOutPowerLimit((short) Integer.parseInt(value));

        } catch (Exception e) {
            logger.error("setChangeMode error : {}", e.getMessage());
        }
    }
}
