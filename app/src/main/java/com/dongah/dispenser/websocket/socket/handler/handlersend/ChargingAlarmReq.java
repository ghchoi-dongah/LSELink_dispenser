package com.dongah.dispenser.websocket.socket.handler.handlersend;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink.ChargingAlarmData;
import com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink.ChargingAlarmRequest;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargingAlarmReq {
    private static final Logger logger = LoggerFactory.getLogger(ChargingAlarmReq.class);

    private final int connectorId ;

    public int getConnectorId() {
        return connectorId;
    }

    public ChargingAlarmReq(int connectorId) {
        this.connectorId = connectorId;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendChargingAlarmReq(int msgType) {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            if (activity == null) return;

            ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();
            ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);

            ChargingAlarmData chargingAlarmData = createChargingAlarmData(msgType);

            ChargingAlarmRequest chargingAlarmRequest = new ChargingAlarmRequest();
            chargingAlarmRequest.setVendorId(chargerConfiguration.getChargePointVendor());
            chargingAlarmRequest.setMessageId("chargingAlarm");
            Gson gson = new Gson();
            chargingAlarmRequest.setData(gson.toJson(chargingAlarmData));

            activity.getSocketReceiveMessage().onSend(
                    getConnectorId(),
                    chargingAlarmRequest.getActionName(),
                    chargingAlarmRequest);
        } catch (Exception e) {
            logger.error("sendChargingAlarmReq error :  {}", e.getMessage());
        }
    }

    private ChargingAlarmData createChargingAlarmData(int msgType) {

        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);
        UiSeq uiSeq = activity.getClassUiProcess(connectorId-1).getUiSeq();

        ChargingAlarmData chargingAlarmData = new ChargingAlarmData();
        chargingAlarmData.setConnectorId(connectorId);
        //(1: 충전 시작, 2: 충전률 90% 도달, 3: 충전 종료
        chargingAlarmData.setMsgType(msgType);
        chargingAlarmData.setTransactionId(chargingCurrentData.getTransactionId());
        chargingAlarmData.setIdTag(chargingCurrentData.getIdTag());
        chargingAlarmData.setPhoneNum("");

        return chargingAlarmData;

    }

}
