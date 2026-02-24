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

public class RechgsocscheduleHandler implements OcppHandler  {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        MainActivity activity = ((MainActivity) MainActivity.mContext);

        DataTransferStatus status = DataTransferStatus.valueOf(payload.getString("status"));
        String vendorId = payload.getString("vendorId");
        String dataStr = payload.getString("data");

        // 저장
        FileManagement fileManagement = new FileManagement();
        fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "unitPrice", dataStr, false);

        //응답
        RechgrsocscheduleConfirm rechgrsocscheduleConfirm = new RechgrsocscheduleConfirm();
        rechgrsocscheduleConfirm.setStatus(DataTransferStatus.Accepted);
        activity.getSocketReceiveMessage().onResultSend(
                rechgrsocscheduleConfirm.getActionName(),
                messageId,
                rechgrsocscheduleConfirm
        );
    }
}
