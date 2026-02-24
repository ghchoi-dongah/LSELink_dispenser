package com.dongah.dispenser.websocket.socket.handler.handlersend;


import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.StatusNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Objects;

public class StatusNotificationReq {
    private static final Logger logger = LoggerFactory.getLogger(StatusNotificationReq.class);

    private final int connectorId ;

    public StatusNotificationReq(int connectorId) {
        this.connectorId = connectorId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendStatusNotification() {
        try {
            int startConnectorId, endConnectorId;
            if (getConnectorId() == 0) {
                startConnectorId = 1;
                endConnectorId = GlobalVariables.maxPlugCount;
            } else {
                startConnectorId = getConnectorId();
                endConnectorId = getConnectorId() + 1;
            }

            //응답 대기 시간을 반영 순차적 보냄
            for (int i = startConnectorId; i < endConnectorId; i++) {
                final int rConnectorId = i;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    sendSingleStatusNotification(rConnectorId);
                }, 1000);
            }
        } catch (Exception e) {
            Log.e("StatusNotificationReq", "sendStatusNotification error", e);
            logger.error("sendStatusNotification error :  {}", e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendSingleStatusNotification(int connectorId) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();
            StatusNotificationRequest statusNotificationRequest = new StatusNotificationRequest(timestamp);

            statusNotificationRequest.setConnectorId(connectorId);
            ControlBoard controlBoard = activity.getControlBoard();
            RxData rxData = controlBoard.getRxData(connectorId-1);
            ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);
            String status = chargingCurrentData.getChargePointStatus().name();
            ChargePointErrorCode errorCode = (controlBoard.isDisconnected() ? ChargePointErrorCode.EVCommunicationError :
                    rxData.isCsEmergency() ? ChargePointErrorCode.OtherError : ChargePointErrorCode.NoError);
            statusNotificationRequest.setErrorCode(errorCode);
            statusNotificationRequest.setStatus(rxData.isCsFault() ? ChargePointStatus.Faulted :
                    !GlobalVariables.ChargerOperation[connectorId-1]  ? ChargePointStatus.Unavailable :
                            ChargePointStatus.valueOf(status));

            activity.getSocketReceiveMessage().onSend(
                    connectorId,
                    statusNotificationRequest.getActionName(),
                    statusNotificationRequest
            );

            // DataTransfer statusnoti
            StatusNotiReq statusNotiReq = new StatusNotiReq(connectorId);
            statusNotiReq.sendStatusNotification();
        } catch (Exception e) {
            Log.e("StatusNotificationReq", "sendSingleStatusNotification error", e);
            logger.error("sendSingleStatusNotification {}", e.getMessage());
        }
    }

    public int getConnectorId() {
        return connectorId;
    }
}
