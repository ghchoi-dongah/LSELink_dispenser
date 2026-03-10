package com.dongah.dispenser.websocket.socket.handler.handlerreceive;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.ToastPositionMake;
import com.dongah.dispenser.websocket.ocpp.core.AuthorizationStatus;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.socket.OcppHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.DtAuthorizeReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotificationReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.VehicleInfoReq;

import org.json.JSONObject;

import java.util.Objects;

public class AuthorizeHandler implements OcppHandler {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handle(JSONObject payload, int connectorId, String messageId) throws Exception {
        FragmentChange fragmentChange = new FragmentChange();
        MainActivity activity = (MainActivity) MainActivity.mContext;
        ChargingCurrentData chargingCurrentData = activity.getChargingCurrentData(connectorId-1);
        UiSeq uiSeq = activity.getClassUiProcess(connectorId-1).getUiSeq();
        ChargerConfiguration chargerConfiguration = activity.getChargerConfiguration();

        JSONObject idTagInfo = payload.getJSONObject("idTagInfo");
        AuthorizationStatus status = AuthorizationStatus.valueOf(idTagInfo.getString("status"));
        String parentIdTag = idTagInfo.has("parentIdTag") ? idTagInfo.getString("parentIdTag") : "";
        String expiryDate = idTagInfo.has("expiryDate") ? idTagInfo.getString("expiryDate") : "";

        // 차량 번호
        chargingCurrentData.setParentIdTagStop(parentIdTag);

        if (AuthorizationStatus.Accepted.equals(status)) {
            if (UiSeq.CHARGING.equals(uiSeq)) {
                boolean stopConfirm = activity.getChargerConfiguration().isStopConfirm();
            } else {
                chargingCurrentData.setParentIdTag(parentIdTag);

                // test mode
                if (Objects.equals(activity.getChargerConfiguration().getOpMode(), 0)) {
                    chargingCurrentData.setPowerUnitPrice(Double.parseDouble(activity.getChargerConfiguration().getTestPrice()));

                    //test 용
                    chargingCurrentData.setIdTag("C1010010341009611");
                }

                // DataTransfer (Authorize)
                DtAuthorizeReq dtAuthorizeReq = new DtAuthorizeReq(
                        messageId,
                        connectorId,
                        chargingCurrentData.getIdTag()
                );
                dtAuthorizeReq.sendDtAuthorize();

                // DataTransfer vehicleInfo
                VehicleInfoReq vehicleInfoReq = new VehicleInfoReq(connectorId);
                vehicleInfoReq.sendVehicleInfo();

                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Available)) {
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                    StatusNotificationReq statusNotificationReq = new StatusNotificationReq(connectorId);
                    statusNotificationReq.sendStatusNotification();
                }

                activity.getClassUiProcess(connectorId-1).setUiSeq(UiSeq.PLUG_CHECK);
                fragmentChange.onFragmentChange(connectorId-1, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
            }
        } else {
            String certificationReason = status.name();
            ToastPositionMake toastPositionMake = new ToastPositionMake(activity);
            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                activity.getClassUiProcess(connectorId-1).setUiSeq(UiSeq.CHARGING);
                fragmentChange.onFragmentChange(connectorId-1, UiSeq.CHARGING, "CHARGING", null);
                toastPositionMake.onShowToast(connectorId-1, "충전 중지 인증 실패 : " + certificationReason);
            } else {
                if (Objects.equals(chargingCurrentData.authType, "M")) {
                    activity.getClassUiProcess(connectorId-1).setUiSeq(UiSeq.MEMBER_CARD);
                    fragmentChange.onFragmentChange(connectorId-1, UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
                } else {
                    activity.getChargingCurrentData(connectorId-1).setAuthorizeResult(false);
                    activity.getClassUiProcess(connectorId-1).onHome();
                    toastPositionMake.onShowToast(connectorId-1, "인증 실패 : " + certificationReason);
                }
            }
        }
    }
}
