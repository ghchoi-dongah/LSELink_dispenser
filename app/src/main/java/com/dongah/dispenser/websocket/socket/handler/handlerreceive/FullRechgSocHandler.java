package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.DataTransferStatus;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.OcppHandler;

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
        DataTransferStatus status = DataTransferStatus.valueOf(payload.getString("status"));
        String dataStr = payload.getString("data");
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        if (status.equals(DataTransferStatus.Accepted)) {
            // 저장
            FileManagement fileManagement = new FileManagement();
            fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "fullRechgSoc", dataStr, false);

           try {
               // SoC 설정
               String filePath = GlobalVariables.getRootPath() + File.separator + "fullRechgSoc";

               StringBuilder sb = new StringBuilder();
               try (FileInputStream fis = new FileInputStream(filePath);
                    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr)) {

                   String line;
                   while ((line = br.readLine()) != null) {
                       sb.append(line);
                   }
               }
               String content = sb.toString();

               JSONArray jsonArray = new JSONArray(content);

               ZonedDateTimeConvert convert = new ZonedDateTimeConvert();
               ZonedDateTime now = convert.doGetCurrentTime();

               if (now == null) {
                   return;
               }

               String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
               int hour = now.getHour();
               @SuppressLint("DefaultLocale") String hourKey = String.format("HH%02d", hour);

               for (int i = 0; i < jsonArray.length(); i++) {
                   JSONObject obj = jsonArray.getJSONObject(i);

                   if (today.equals(obj.optString("day", ""))) {

                       // 키가 없을 때 JSONException 방지 (없으면 config soc 사용)
                       String value = obj.optString(hourKey, String.valueOf(chargerConfiguration.getTargetSoc()));
                       chargerConfiguration.setTargetSoc(Integer.parseInt(value));
                       break;
                   }
               }
           } catch (Exception e) {
               logger.error("fullRechgSoc error : {}", e.getMessage());
           }
        }
    }
}
