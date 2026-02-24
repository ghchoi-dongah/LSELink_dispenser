package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.PaymentType;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.websocket.ocpp.core.RemoteStartStopStatus;
import com.dongah.dispenser.websocket.ocpp.core.RemoteStartTransactionConfirmation;
import com.dongah.dispenser.websocket.socket.OcppHandler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RemoteStartTransactionHandler implements OcppHandler  {

    private static final Logger logger = LoggerFactory.getLogger(RemoteStartTransactionHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        MainActivity activity = ((MainActivity) MainActivity.mContext);
        ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);

        chargingCurrentData.setConnectorId(payload.getInt("connectorId"));
        chargingCurrentData.setIdTag(payload.getString("idTag"));
        chargingCurrentData.setPaymentType(PaymentType.MEMBER);

        // 응답
        sendResponse(connectorId, messageId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendResponse(int connectorId, String messageId) {
        try {
            MainActivity activity = ((MainActivity) MainActivity.mContext);
            UiSeq uiSeq = activity.getClassUiProcess(connectorId-1).getUiSeq();

            RemoteStartStopStatus status = !Objects.equals(uiSeq, UiSeq.INIT) ? RemoteStartStopStatus.Rejected
                    : connectorId == 0 ? RemoteStartStopStatus.Rejected : RemoteStartStopStatus.Accepted;
            RemoteStartTransactionConfirmation remoteStartTransactionConfirmation =
                    new RemoteStartTransactionConfirmation(status);
            activity.getSocketReceiveMessage().onResultSend(
                    remoteStartTransactionConfirmation.getActionName(),
                    messageId,
                    remoteStartTransactionConfirmation
            );
        } catch (Exception e) {
            logger.error(" sendResponse error : {}", e.getMessage());
        }
    }
}
