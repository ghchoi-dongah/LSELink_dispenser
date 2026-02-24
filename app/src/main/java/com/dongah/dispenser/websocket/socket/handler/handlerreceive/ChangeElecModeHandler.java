package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.DataTransferStatus;
import com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink.ChangeElecModeConfirm;
import com.dongah.dispenser.websocket.socket.OcppHandler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeElecModeHandler implements OcppHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChangeElecModeHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        String vendorId = payload.getString("vendorId");
        String msgId = payload.getString("messageId");  //changeelecmode.req
        String dataStr = payload.getString("data");

        JSONObject dataJson = new JSONObject(dataStr);
        //파일 저장
        FileManagement fileManagement = new FileManagement();
        fileManagement.stringToFileSave(
                GlobalVariables.getRootPath(),
                "changeElecMode",
                dataStr, false);

        // 응답
        sendResponse(connectorId, messageId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendResponse(int connectorId, String messageId) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;

            ChangeElecModeConfirm changeElecModeConfirm = new ChangeElecModeConfirm();
            changeElecModeConfirm.setStatus(DataTransferStatus.Accepted);

            activity.getSocketReceiveMessage().onResultSend(
                    changeElecModeConfirm.getActionName(),
                    messageId,
                    changeElecModeConfirm);
        } catch (Exception e) {
            logger.error(" sendResponse error : {}", e.getMessage());
        }
    }
}
