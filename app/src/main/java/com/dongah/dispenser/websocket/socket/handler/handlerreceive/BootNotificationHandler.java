package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotiReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.UnitPriceThread;

import org.json.JSONObject;

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

            // dataTransfer statusnoti
            // connectorId = 0 : all
//            StatusNotiReq statusNotiReq = new StatusNotiReq(0);
//            statusNotiReq.sendStatusNotification();

            //Unit Price
            UnitPriceThread unitPriceThread = new UnitPriceThread(3600);
            unitPriceThread.start();

//            //1. Authorize test
//            AuthorizeReq authorizeReq = new AuthorizeReq(1);
//            authorizeReq.sendAuthorize("C1010010341009611");
//
//            //2. startTransaction
//            StartTransactionReq startTransactionReq = new StartTransactionReq(1);
//            startTransactionReq.sendStartTransactionReq();
//
//            //3. FullRechgSoc
//            FullRechgSocReq fullRechgSocReq = new FullRechgSocReq(1);
//            fullRechgSocReq.sendFullRechSoc();
        } else {
            activity.getProcessHandler().onBootNotificationStart(5);
        }
    }
}
