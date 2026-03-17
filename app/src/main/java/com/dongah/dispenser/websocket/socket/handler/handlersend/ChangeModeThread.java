package com.dongah.dispenser.websocket.socket.handler.handlersend;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
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
import java.time.ZonedDateTime;
import java.util.Objects;

public class ChangeModeThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ChangeModeThread.class);

    private volatile boolean stopped = false;

    public void stopThread() {
        stopped = true;
        interrupt();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        logger.info("ChangeModeThread start");
        processChangeMode();    // 충전기 부팅 후 1회 실행
        while (!stopped && !isInterrupted()) {
            try {
                Thread.sleep(1000);

                ZonedDateTimeConvert convert = new ZonedDateTimeConvert();
                ZonedDateTime now = convert.doGetCurrentTime();

                int minute = now.getMinute();
                int second = now.getSecond();

                // 정각일 때 충전 모드 변경
                if (minute == 0 && second == 0) {
                    processChangeMode();
                }
            } catch (Exception e) {
                logger.error("ChangeModeThread error : {}", e.getMessage());
            }
        }
        logger.info("ChangeModeThread terminated");
    }

    // 커넥터 모드 변경
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void processChangeMode() {
        MainActivity activity = (MainActivity) MainActivity.mContext;

        try {
            // 1. changeMode 파일 유무 확인
            File file = new File(GlobalVariables.getRootPath() + File.separator + "changeMode");

            if (!file.exists()) {
                // 2. 파일이 없으면 DM(양구) 처리
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    activity.getChargingCurrentData(i).setChangeMode("DM");
                }
            } else {
                // 3. 파일이 있는 경우(커넥터 별 모드 갱신)
                // connectorId 없으면, connectorId 0 상태값 설정
                // DM(양구), NM(1구), WM(충전대기), IM(충전불가)
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    String content = readFile(file, i+1);
                    Log.d("ChangeModeThread", "content" + (i+1) + ": " + content);

                    // content == null 이면 connectorId에 해당하는 changeMode이 없음
                    if (content == null) {
                        // connectorId: 0으로 대체
                        String content0 = readFile(file, 0);
                        Log.d("ChangeModeThread", "content0" + content0);
                        // connectorId: 0에 대한 content가 없으면 "DM"으로 처리
                        if (content0 == null) {
                            activity.getChargingCurrentData(i).setChangeMode("DM");
                        }
                        // connectorId: 0에 대한 content가 있으면 커넥터 모드 갱신
                        else {
                            setChangeMode(i, content0);
                        }
                    } else {
                        setChangeMode(i, content);
                    }
                }

                // 4. 커넥터 모드에 따른 커넥터 사용 유무 설정 및 화면에 상태 반영
                setConnectUse();
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    // INIT 화면일 경우만 화면 refresh
                    if (Objects.equals(activity.getClassUiProcess(i).getUiSeq(), UiSeq.INIT)) {
                        activity.getClassUiProcess(i).onHome();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("processChangeMode error : {}", e.getMessage());
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
    private static void setChangeMode(int connectorId, String content) {
        MainActivity activity = (MainActivity) MainActivity.mContext;

        try {
            JSONObject rootJson = new JSONObject(content);
            ZonedDateTime now = new ZonedDateTimeConvert().doGetCurrentTime();
            if (now == null) return;

            int hour = now.getHour();
            @SuppressLint("DefaultLocale") String hourKey = String.format("HH%02d", hour);

            // DM(양구), NM(1구), WM(충전대기), IM(충전불가)
            String value = rootJson.optString(hourKey, "DM");
            activity.getChargingCurrentData(connectorId).setChangeMode(value);
        } catch (Exception e) {
            Log.e("setChangeMode", "error >> ", e);
            logger.error("setChangeMode error : {}", e.getMessage());
        }
    }

    private static void setConnectUse() {
       try {
           MainActivity activity = ((MainActivity) MainActivity.mContext);
           String chMode0 = activity.getChargingCurrentData(0).getChangeMode();
           String chMode1 = activity.getChargingCurrentData(1).getChangeMode();

           /** 커넥터 모드에 따른 커넥터 사용 유무 설정
            * 1ch / 2ch
            * DM / DM : 전체 충전
            * DM / NM, NM / DM[NM] : 1구 충전(1ch > 2ch 우선 순위, 2ch 미사용)
            * WM / DM[NM], IM / DM[NM] : 1구 충전
            * */
           boolean isMode0Valid = "DM".equals(chMode0) || "NM".equals(chMode0);
           boolean isMode1Valid = "DM".equals(chMode1) || "NM".equals(chMode1);

           boolean conUse0, conUse1;

           if (isMode0Valid && isMode1Valid) {
               conUse0 = true;
               conUse1 = "DM".equals(chMode0) && "DM".equals(chMode1);
           } else {
               conUse0 = isMode0Valid;
               conUse1 = isMode1Valid;
           }

           activity.getChargingCurrentData(0).setConnectUse(conUse0);
           activity.getChargingCurrentData(1).setConnectUse(conUse1);
       } catch (Exception e) {
           logger.error("setConnectUse error : {}", e.getMessage());
       }
    }
}
