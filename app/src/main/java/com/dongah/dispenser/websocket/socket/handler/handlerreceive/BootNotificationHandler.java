package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.UnitPriceThread;

import org.json.JSONObject;

import java.util.Objects;

public class BootNotificationHandler implements OcppHandler {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        String status = payload.getString("status");
        int interval = payload.getInt("interval");
        MainActivity activity = (MainActivity) MainActivity.mContext;
        activity.getProcessHandler().onBootNotificationStop();

        if ("Accepted".equals(status)) {
            GlobalVariables.setConnectRetry(true);
            // Heartbeat 간격 설정 등 기존 로직 이동
            activity.getProcessHandler().onHeartBeatStart(interval);

            // StatusNotification
            // connectorId = 0 : all
            activity.getProcessHandler().onStatusNotificationStart(300);

            //Unit Price
            UnitPriceThread unitPriceThread = new UnitPriceThread(3600);
            unitPriceThread.start();

            // DataTransfer ChangeMode
            activity.getProcessHandler().onChangeModeStart();

            if (!Objects.equals(activity.getChargerConfiguration().getOpMode(), 0)) {
                // DataTransfer ChangeElecMode
                activity.getProcessHandler().onChangeElecModeStart();
            }
        } else {
            activity.getProcessHandler().onBootNotificationStart(5);
        }
    }
}
