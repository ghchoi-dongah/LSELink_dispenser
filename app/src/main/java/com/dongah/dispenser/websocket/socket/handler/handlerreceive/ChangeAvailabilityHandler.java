package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.AvailabilityStatus;
import com.dongah.dispenser.websocket.ocpp.core.AvailabilityType;
import com.dongah.dispenser.websocket.ocpp.core.ChangeAvailabilityConfirmation;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class ChangeAvailabilityHandler implements OcppHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChangeAvailabilityHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
//        int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);
        AvailabilityType type = AvailabilityType.valueOf(payload.getString("type"));

        // Operative → 충전기 사용 가능
        boolean checkType = type == AvailabilityType.Operative;

        // 충전 중 상태 확인(true: 충전 중)
        boolean isCharging = Objects.equals(
                activity.getClassUiProcess(connectorId).getUiSeq(),
                UiSeq.CHARGING
        );

        AvailabilityStatus result =
                ((type == AvailabilityType.Inoperative) || (type == AvailabilityType.Maintenance) && isCharging)
                        ? AvailabilityStatus.Scheduled
                        : AvailabilityStatus.Accepted;

        // change availability response
        ChangeAvailabilityConfirmation changeAvailabilityConfirmation = new ChangeAvailabilityConfirmation(result);
        activity.getSocketReceiveMessage().onResultSend(
                changeAvailabilityConfirmation.getActionName(),
                messageId,
                changeAvailabilityConfirmation);

        ChargePointStatus status = type.equals(AvailabilityType.Operative) ?
                ChargePointStatus.Available : ChargePointStatus.Maintenance;
        chargingCurrentData.setChargePointStatus(status);

        // StatusNotification send
        StatusNotificationReq statusNotificationReq = new StatusNotificationReq(connectorId);
        statusNotificationReq.sendStatusNotification(connectorId, chargingCurrentData.getChargePointStatus());

        // ChargerOperate
        // connectorId == 0 → 전체 업데이트
        if (connectorId == 0) {
            Arrays.fill(GlobalVariables.ChargerOperation, checkType);
        } else {
            GlobalVariables.ChargerOperation[connectorId] = checkType;
        }

        onChargerOperateSave(checkType);
    }


    private void onChargerOperateSave(boolean checkType) {
        try {
            boolean chk;
            FileManagement fileManagement = new FileManagement();
            String rootPath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
            String fileName = "ChargerOperate";
            File file = new File(rootPath + File.separator + fileName);
            if (file.exists()) chk = file.delete();

            for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                String statusContent = String.valueOf(GlobalVariables.ChargerOperation[i]);
                fileManagement.stringToFileSave(rootPath, fileName, statusContent, true);
            }
        } catch (Exception e) {
            logger.error("onChargerOperateSave {}", e.getMessage());
        }
    }
}
