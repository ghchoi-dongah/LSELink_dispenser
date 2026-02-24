package com.dongah.dispenser.websocket.socket.handler.handlersend;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink.UserSetSocData;
import com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink.UserSetSocRequest;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSetSocReq {
    private static final Logger logger = LoggerFactory.getLogger(UserSetSocReq.class);


    private final int connectorId ;

    public UserSetSocReq(int connectorId) {
        this.connectorId = connectorId;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendUserSetSoc() {

        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

            UserSetSocData userSetSocData = createUserSocData();

            UserSetSocRequest userSetSocRequest = new UserSetSocRequest();
            userSetSocRequest.setVendorId(chargerConfiguration.getChargePointVendor());
            userSetSocRequest.setMessageId("userSetSoc");
            Gson gson = new Gson();
            userSetSocRequest.setData(gson.toJson(userSetSocData));

            activity.getSocketReceiveMessage().onSend(
                    connectorId,
                    userSetSocRequest.getActionName(),
                    userSetSocRequest);

        } catch (Exception e) {
            logger.error(" sendUserSetSoc error : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private UserSetSocData createUserSocData() {
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();
        ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);

        UserSetSocData userSetSocData = new UserSetSocData();
        userSetSocData.setChargeBoxSerialNumber(chargerConfiguration.getChargeBoxSerialNumber());
        userSetSocData.setChargePointSerialNumber(chargerConfiguration.getChargerId());
        userSetSocData.setConnectorId(connectorId);
        userSetSocData.setIdTag(chargingCurrentData.getIdTag());
        userSetSocData.setTransactionId(chargingCurrentData.getTransactionId());
        userSetSocData.setSetSoc(chargerConfiguration.getTargetSoc());
        ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
        userSetSocData.setTimestamp(zonedDateTimeConvert.doGetUtcDatetimeAsString());

        return userSetSocData;
    }
}
