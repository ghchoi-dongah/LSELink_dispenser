package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.DataTransferStatus;
import com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink.RechgrsocscheduleConfirm;
import com.dongah.dispenser.websocket.socket.OcppHandler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class RechgrsocscheduleHandler implements OcppHandler {
    private static final Logger logger = LoggerFactory.getLogger(RechgrsocscheduleHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        String vendorId = payload.getString("vendorId");
        String msgId = payload.getString("messageId");
        String dataStr = payload.getString("data");

        try {
            // file save
            saveRechgrsocscheduleToFile(dataStr);

            // response
            sendResponse(connectorId, messageId);
        } catch (Exception e) {
            logger.error("RechgrsocscheduleHandler error : {}", e.getMessage(), e);
        }
    }

    private void saveRechgrsocscheduleToFile(String newData) {
        try {
            FileManagement fileManagement = new FileManagement();
            JSONObject rootJson;

            File file = new File(GlobalVariables.getRootPath() + File.separator + "rechgrsocschedule");

            if (!file.exists()) {
                rootJson = new JSONObject();
            } else {
                String oldText = readFile(file);
                if (oldText == null || oldText.isEmpty()) {
                    rootJson = new JSONObject();
                } else {
                    rootJson = new JSONObject(oldText);
                }
            }

            JSONObject newJson = new JSONObject(newData);
            int connectorId = newJson.getInt("connectorId");
            rootJson.put(String.valueOf(connectorId), newJson);

            fileManagement.stringToFileSave(
                    GlobalVariables.getRootPath(),
                    "rechgrsocschedule",
                    rootJson.toString(),
                    false);
        } catch (Exception e) {
            logger.error("saveRechgrsocscheduleToFile error : {}", e.getMessage(), e);
        }
    }

    private String readFile(File file) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendResponse(int connectorId, String messageId) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;

            RechgrsocscheduleConfirm rechgrsocscheduleConfirm = new RechgrsocscheduleConfirm();
            rechgrsocscheduleConfirm.setStatus(DataTransferStatus.Accepted);

            activity.getSocketReceiveMessage().onResultSend(
                    connectorId,
                    rechgrsocscheduleConfirm.getActionName(),
                    messageId,
                    rechgrsocscheduleConfirm);
        } catch (Exception e) {
            logger.error("sendResponse error : {}", e.getMessage(), e);
        }
    }
}
