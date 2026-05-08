package com.dongah.dispenser.websocket.socket.handler.handlersend;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Objects;

public class RechgrsocscheduleThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RechgrsocscheduleThread.class);

    private volatile boolean stopped = false;

    public void stopThread() {
        stopped = true;
        interrupt();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        logger.info("RechgrsocscheduleThread start");
        processRechgrsoc(0);
        while (!stopped && !isInterrupted()) {
            try {
                Thread.sleep(1000);

                ZonedDateTimeConvert convert = new ZonedDateTimeConvert();
                ZonedDateTime now = convert.doGetCurrentTime();

                int minute = now.getMinute();
                int second = now.getSecond();

                // 정각일 때 실행
                if (minute == 0 && second == 0) {
                    processRechgrsoc(0);
                }
            } catch (InterruptedException e) {
                // interrupt()로 인해 발생한 예외이므로 종료를 위해 루프를 빠져나가도록 유도
                logger.info("RechgrsocscheduleThread is interrupted. Stopping...");
                Thread.currentThread().interrupt(); // Interrupt 플래그를 다시 세팅
                break;
            } catch (Exception e) {
                logger.info("RechgrsocscheduleThread error : {}", e.getMessage(), e);
            }
        }
        logger.info("RechgrsocscheduleThread terminated");
    }

    // soc 제한
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void processRechgrsoc(int connectorId) {
        try {
            // 1. rechgrsocschedule 파일 유무 확인
            File file = new File(GlobalVariables.getRootPath() + File.separator + GlobalVariables.FILE_RECHGR_SOC_SCHEDULE);

            int startIndex, endIndex;
            if (connectorId == 0) {
                startIndex = 1;
                endIndex = GlobalVariables.maxChannel;
            } else {
                startIndex = connectorId;
                endIndex = connectorId;
            }

            if (!file.exists()) {
                logger.info("processRechgrsoc file doesn't exist. changemode start");
                // 2. 파일이 없으면 DT(changemode) rechgAmt 확인
                for (int i = startIndex; i <= endIndex; i++) {
                    ChangeModeThread.setRechgAmt(i);
                }
            } else {
                // 3. 파일이 있는 경우(채널별 충전량 제한)
                for (int i = startIndex; i <= endIndex; i++) {
                    String content = readFile(file, i);
                    logger.info("processRechgrsoc connectorId[{}] content : {}", i, content);

                    if (content == null) {
                        logger.info("processRechgrsoc file content is null. changemode start");
                        // DT(changemode) rechgAmt 조회
                        ChangeModeThread.setRechgAmt(i);
                    } else {
                        setRechgrsoc(i, content);
                    }
                }
            }
        } catch (Exception  e) {
            logger.error("processRechgrsoc error : {}", e.getMessage(), e);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void setRechgrsoc(int connectorId, String content) {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        try {
            JSONObject rootJson = new JSONObject(content);
            ZonedDateTime now = new ZonedDateTimeConvert().doGetCurrentTime();
            if (now == null) return;

            DayOfWeek dayOfWeek = now.getDayOfWeek();

            // DH: 평일, WH: 주말
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

            int hour = now.getHour();
            @SuppressLint("DefaultLocale") String hourKey = String.format("%s%02d", isWeekend ? "WH" : "DH", hour);

            String value = rootJson.optString(hourKey, String.valueOf(chargerConfiguration.getTargetSoc()));
            activity.getChargingCurrentData(connectorId-1).setLimitSoc(Integer.parseInt(value));

            logger.info("setRechgrsoc connectorId[{}] soc >> {} : {}", connectorId, hourKey, value);

            if (Objects.equals(activity.getClassUiProcess(connectorId-1).getUiSeq(), UiSeq.INIT)) {
                activity.getClassUiProcess(connectorId-1).onHome();
            }
        } catch (Exception e) {
            logger.error("setRechgrsoc error : {}", e.getMessage(), e);
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
}
