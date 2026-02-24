package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.controlboard.TxData;
import com.dongah.dispenser.websocket.ocpp.core.AuthorizationStatus;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.ChargingAlarmReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.FullRechgSocReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.MeterValuesReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotificationReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.UserSetSocReq;

import org.json.JSONObject;

import java.util.Objects;

public class StartTransactionHandler implements OcppHandler  {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        MainActivity activity = ((MainActivity) MainActivity.mContext);
        ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);
        //서버에서 transactionId 받음 ==> stopTransaction 계속하여 사용.
        chargingCurrentData.setTransactionId(payload.getInt("transactionId"));
        GlobalVariables.setDumpTransactionId(payload.getInt("transactionId"));
        MeterValuesReq meterValuesReq = new MeterValuesReq(connectorId);

        JSONObject idTagInfo = payload.getJSONObject("idTagInfo");
        AuthorizationStatus status = AuthorizationStatus.valueOf(idTagInfo.getString("status"));
        String parentIdTag = idTagInfo.has("parentIdTag") ? idTagInfo.getString("parentIdTag") : "";

        //accept continue
        if (Objects.equals(status, AuthorizationStatus.Accepted)) {
            chargingCurrentData.setChargePointStatus(ChargePointStatus.Charging);

            // DataTransfer ChargingAlarm
            ChargingAlarmReq chargingAlarmReq = new ChargingAlarmReq(connectorId);
            chargingAlarmReq.sendChargingAlarmReq(1);

            // DataTransfer MeterValues
            if (GlobalVariables.getMeterValueSampleInterval() > 0) {
                activity.getClassUiProcess(connectorId-1).onMeterValueStart(connectorId);
            }

            // DataTransfer fullrechgsoc
            FullRechgSocReq fullRechgSocReq = new FullRechgSocReq(connectorId);
            fullRechgSocReq.sendFullRechSoc();

            // StatusNotification
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq(connectorId);
            statusNotificationReq.sendStatusNotification();

            // DataTransfer userSetSoc
            UserSetSocReq userSetSocReq = new UserSetSocReq(connectorId);
            userSetSocReq.sendUserSetSoc();

            activity.getClassUiProcess(connectorId-1).setUiSeq(UiSeq.CHARGING);
            FragmentChange fragmentChange = new FragmentChange();
            fragmentChange.onFragmentChange(connectorId-1, UiSeq.CHARGING, "CHARGING", null);
        } else {
            // stop
            TxData txData = activity.getControlBoard().getTxData(connectorId-1);
            txData.setStop(true);
            txData.setStart(false);

            // DataTransfer MeterValues
            activity.getClassUiProcess(connectorId-1).onMeterValueStop();

            // home
            activity.getClassUiProcess(connectorId-1).onHome();
        }
    }
}
