package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.DataTransferStatus;
import com.dongah.dispenser.websocket.socket.OcppHandler;

import org.json.JSONObject;

public class UnitPriceHandler implements OcppHandler  {

    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        DataTransferStatus status = DataTransferStatus.valueOf(payload.getString("status"));
        String dataStr = payload.getString("data");

        if (status.equals(DataTransferStatus.Accepted)) {
            // 저장
            FileManagement fileManagement = new FileManagement();
            fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "unitPrice", dataStr, false);
        }
    }
}
