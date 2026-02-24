package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.FirmwareDownload;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.dispenser.websocket.ocpp.firmware.FirmwareStatus;
import com.dongah.dispenser.websocket.ocpp.firmware.FirmwareStatusNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.firmware.UpdateFirmwareConfirmation;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

public class UpdateFirmwareHandler implements OcppHandler  {
    private static final Logger logger = LoggerFactory.getLogger(UpdateFirmwareHandler.class);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {

        String location = payload.has("location") ? payload.getString("location") : "";
        int retries = payload.has("retries") ? payload.getInt("retries") : 1;

        MainActivity activity = ((MainActivity) MainActivity.mContext);
        //응답
        UpdateFirmwareConfirmation updateFirmwareConfirmation = new UpdateFirmwareConfirmation();
        activity.getSocketReceiveMessage().onResultSend(
                updateFirmwareConfirmation.getActionName(),
                messageId,
                updateFirmwareConfirmation
        );

        // 1. firmware status : Downloading
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();
        FirmwareStatusNotificationRequest firmwareStatusNotificationRequest =
                new FirmwareStatusNotificationRequest(FirmwareStatus.Downloading);
        chargerConfiguration.setFirmwareStatus(FirmwareStatus.Downloading);
        activity.getSocketReceiveMessage().onSend(
                connectorId,
                firmwareStatusNotificationRequest.getActionName(),
                firmwareStatusNotificationRequest
        );

        // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = false ==> Unavailable
        Arrays.fill(GlobalVariables.ChargerOperation, false);
        onChargerOperateSave();

        // Status Notification - all
        StatusNotificationReq statusNotificationReq = new StatusNotificationReq(0);
        statusNotificationReq.sendStatusNotification();

        //https
        String fileName = location.substring(location.lastIndexOf("/") + 1);
        FirmwareDownload firmwareDownload = new FirmwareDownload(
                location,
                fileName,
                retries,
                new FirmwareDownload.Callback() {
                    @Override
                    public void onSuccess(File file) {
                        sendFirmwareStatus(FirmwareStatus.Downloaded);
                    }

                    @Override
                    public void onFail(String message) {
                        sendFirmwareStatus(FirmwareStatus.DownloadFailed);
                    }

                    private void sendFirmwareStatus(FirmwareStatus firmwareStatus) {
                        FirmwareStatusNotificationRequest request =
                                new FirmwareStatusNotificationRequest(firmwareStatus);
                        try {
                            activity.getSocketReceiveMessage().onSend(
                                    100,
                                    request.getActionName(),
                                    request
                            );
                        } catch (OccurenceConstraintException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
        firmwareDownload.start();
    }

    private void onChargerOperateSave() {
        try {
            boolean check;
            String rootPath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
            File file = new File(rootPath + File.separator + "ChargerOperate");
            if (file.exists()) check = file.delete();

            FileManagement fileManagement = new FileManagement();
            for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                String statusContent = String.valueOf(GlobalVariables.ChargerOperation[i]);
                fileManagement.stringToFileSave(rootPath, "ChargerOperate", statusContent, true);
            }

        } catch (Exception e) {
            logger.error("onChargerOperateSave error : {}", e.getMessage());
        }
    }
}
