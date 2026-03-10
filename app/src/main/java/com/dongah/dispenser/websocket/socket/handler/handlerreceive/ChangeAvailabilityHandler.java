package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.AvailabilityStatus;
import com.dongah.dispenser.websocket.ocpp.core.AvailabilityType;
import com.dongah.dispenser.websocket.ocpp.core.ChangeAvailabilityConfirmation;
import com.dongah.dispenser.websocket.socket.OcppHandler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ChangeAvailabilityHandler implements OcppHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChangeAvailabilityHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
//        int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
        MainActivity activity = (MainActivity) MainActivity.mContext;

        AvailabilityType type = AvailabilityType.valueOf(payload.getString("type"));
        //change availability response
        boolean checkType = type == AvailabilityType.Operative;

        AvailabilityStatus result = AvailabilityStatus.Accepted ;
        ChangeAvailabilityConfirmation changeAvailabilityConfirmation = new ChangeAvailabilityConfirmation(result);
        activity.getSocketReceiveMessage().onResultSend(changeAvailabilityConfirmation.getActionName(),
                messageId,
                changeAvailabilityConfirmation);

        // ChargerOperate
        GlobalVariables.ChargerOperation[connectorId-1] = checkType;
        onChargerOperateSave(checkType);

        // 충전 가능 && OP_STOP → INIT로 갱신
        if (GlobalVariables.ChargerOperation[connectorId-1] &&
                activity.getClassUiProcess(connectorId-1).getUiSeq().equals(UiSeq.OP_STOP)) {
            activity.getClassUiProcess(connectorId-1).onHome();
        }
    }


    private void onChargerOperateSave(boolean checkType) {
        try {
            boolean chk;
            FileManagement fileManagement = new FileManagement();
            String rootPath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
            String fileName = "ChargerOperate";
            File file = new File(rootPath + File.separator + fileName);
            if (file.exists()) chk = file.delete();
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                String statusContent = String.valueOf(GlobalVariables.ChargerOperation[i]);
                fileManagement.stringToFileSave(rootPath, fileName, statusContent, true);
            }
        } catch (Exception e) {
            logger.error(" onChargerOperateSave {}", e.getMessage());
        }
    }
}
