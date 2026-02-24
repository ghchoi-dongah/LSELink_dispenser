package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.websocket.ocpp.core.AuthorizationStatus;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.ChargingAlarmReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;

import java.util.Objects;

public class StopTransactionHandler implements OcppHandler  {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);

        JSONObject idTagInfo = payload.getJSONObject("idTagInfo");

        AuthorizationStatus status = AuthorizationStatus.valueOf(idTagInfo.getString("status"));
        String parentIdTag = idTagInfo.has("parentIdTag") ? idTagInfo.getString("parentIdTag") : null;


        // DataTransfer ChargingAlarm
        ChargingAlarmReq chargingAlarmReq = new ChargingAlarmReq(connectorId);
        chargingAlarmReq.sendChargingAlarmReq(3);

        //accept continue
//        if (Objects.equals(status, AuthorizationStatus.Accepted)) {
//            chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
//        }


        chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
        StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
        statusNotificationReq.sendStatusNotification();

//        activity.getClassUiProcess(connectorId-1).setUiSeq(UiSeq.FINISH);
//        FragmentChange fragmentChange = new FragmentChange();
//        fragmentChange.onFragmentChange(connectorId-1, UiSeq.FINISH, "FINISH", null);
    }
}
