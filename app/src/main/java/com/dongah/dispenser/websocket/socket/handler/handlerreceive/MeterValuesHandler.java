package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.websocket.socket.OcppHandler;

import org.json.JSONObject;

public class MeterValuesHandler implements OcppHandler {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        int connectorid = connectorId;
        String uuid = messageId;
    }
}
