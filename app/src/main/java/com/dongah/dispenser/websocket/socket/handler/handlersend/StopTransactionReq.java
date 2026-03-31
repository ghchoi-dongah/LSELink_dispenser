package com.dongah.dispenser.websocket.socket.handler.handlersend;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.utils.LogDataSave;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.Location;
import com.dongah.dispenser.websocket.ocpp.core.MeterValue;
import com.dongah.dispenser.websocket.ocpp.core.SampledValue;
import com.dongah.dispenser.websocket.ocpp.core.StopTransactionRequest;
import com.dongah.dispenser.websocket.ocpp.core.ValueFormat;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StopTransactionReq {
    private static final Logger logger = LoggerFactory.getLogger(StopTransactionReq.class);

    private final int connectorId ;
    public int getConnectorId() {
        return connectorId;
    }

    final ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();

    public StopTransactionReq(int connectorId) {
        this.connectorId = connectorId;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendStopTransactionReq() {
        try {
            MainActivity activity = (MainActivity) MainActivity.mContext;
            ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(getConnectorId()-1);
            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime(chargingCurrentData.getChargingEndTime());
//            ZonedDateTime timestamp = zonedDateTimeConvert.doGetCurrentTime(chargingCurrentData.getChargingEndTime());

            MeterValuesReq req = new MeterValuesReq(getConnectorId());
            req.sendMeterValues(getConnectorId());
            activity.getClassUiProcess(getConnectorId()-1).onMeterValueStop();

            StopTransactionRequest stopTransactionRequest = new StopTransactionRequest(
                    chargingCurrentData.getPowerMeterStop(),
                    timestamp,
                    chargingCurrentData.getTransactionId(),
                    chargingCurrentData.getStopReason()
            );
            stopTransactionRequest.setIdTag(chargingCurrentData.getIdTag());


            // 충전 사용량
            SampledValue energy = new SampledValue();
            energy.setValue(String.valueOf(chargingCurrentData.getPowerMeterUse() * 0.01));
            energy.setContext("Transaction.End");
            energy.setFormat(ValueFormat.Raw);
            energy.setMeasurand("Current.Export");
            energy.setPhase("L1");
            energy.setLocation(Location.Outlet);
            energy.setUnit("kWh");

            //SoC
            SampledValue soc = new SampledValue();
            soc.setValue(String.valueOf(chargingCurrentData.getSoc()));
            soc.setContext("Transaction.End");
            soc.setFormat(ValueFormat.Raw);
            soc.setMeasurand("SoC");
            soc.setPhase("L2");
            soc.setLocation(Location.EV);
            soc.setUnit("Percent");

            List<SampledValue> list = new ArrayList<>();
            list.add(energy);
            list.add(soc);
            SampledValue[] sampledArray = list.toArray(new SampledValue[0]);

            ZonedDateTime now = zonedDateTimeConvert.doGetCurrentTime();

            MeterValue meterValue = new MeterValue(now, sampledArray);
            stopTransactionRequest.setTransactionData(new MeterValue[] {meterValue});


            SocketState socketState = activity.getSocketReceiveMessage().getSocket().getState();
            if (socketState.equals(SocketState.OPEN)) {
                //send
                activity.getSocketReceiveMessage().onSend(
                        getConnectorId(),
                        stopTransactionRequest.getActionName(),
                        stopTransactionRequest);
            } else {
                String uuid = UUID.randomUUID().toString();
                saveFullStartTransaction(getConnectorId(), uuid, stopTransactionRequest);
            }
        } catch (Exception e) {
            Log.e("StopTransactionReq", "sendStopTransactionReq error", e);
            logger.error(" sendStopTransactionReq error : {}", e.getMessage());
        }
    }

    private void saveFullStartTransaction(
            int connectorId,
            String uniqueId,
            StopTransactionRequest req) {
        try {
            JSONArray frame = new JSONArray();

            frame.put(2); // CALL
            frame.put(uniqueId);
            frame.put(req.getActionName());

            JSONObject payload = new JSONObject();
            payload.put("idTag", req.getIdTag());
            payload.put("meterStop", req.getMeterStop());
            payload.put("timestamp", req.getTimestamp().toString());
            payload.put("transactionId", req.getTransactionId());
            payload.put("reason", req.getReason());

            frame.put(payload);

            LogDataSave logDataSave = new LogDataSave();
            logDataSave.makeDump(frame.toString()); // TODO : 커넥터별 dump 분리

        } catch (Exception e) {
            logger.error(" saveFullStartTransaction error : {}", e.getMessage());
        }
    }

}
