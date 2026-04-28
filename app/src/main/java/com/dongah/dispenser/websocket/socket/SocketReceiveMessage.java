package com.dongah.dispenser.websocket.socket;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.utils.LogDataSave;
import com.dongah.dispenser.websocket.ocpp.common.JSONCommunicator;
import com.dongah.dispenser.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.dispenser.websocket.ocpp.common.model.Confirmation;
import com.dongah.dispenser.websocket.ocpp.common.model.Message;
import com.dongah.dispenser.websocket.ocpp.common.model.Request;
import com.dongah.dispenser.websocket.ocpp.utilities.Stopwatch;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.AuthorizeHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.BootNotificationHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.ChangeAvailabilityHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.ChangeConfigurationHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.ChangeElecModeHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.ChangeModeHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.ChargingAlarmHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.ClearCacheHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.DTAuthorizeHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.FirmwareStatusNotificationHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.FullRechgSocHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.GetLocalListVersionHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.HeartbeatHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.MeterValuesHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.RechgrsocscheduleHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.RemoteStartTransactionHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.RemoteStopTransactionHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.ResetHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.SendLocalListHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.StartTransactionHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.StatusNotiHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.StatusNotificationHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.StopTransactionHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.TriggerMessageHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.UnitPriceHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.UpdateFirmwareHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.UserSetSocHandler;
import com.dongah.dispenser.websocket.socket.handler.handlerreceive.VehicleInfoHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import okhttp3.WebSocket;

public class SocketReceiveMessage extends JSONCommunicator implements SocketInterface {

    private static final Logger logger = LoggerFactory.getLogger(SocketReceiveMessage.class);

    private final Map<String, OcppHandler> handlerMap = new HashMap<>();
    private final Map<String, OcppHandler> dataTransferHandlerMap = new HashMap<>();

    private int connectorId = 0;

    /**
     * Okhttp3 web-socket
     */
    WebSocket webSocket = null;
    /**
     * user define socket blue-networks socket
     */
    Socket socket = null;
    String url;
    String actionName;
    Message message = null;
    SendHashMapObject sendHashMapObject;
    /**
     * message send/receive list (UUID,Action)
     */
    HashMap<String, String> hashMapUuid = null;
    HashMap<String, Object> newHashMapUuid = null;
    HashMap<Integer, Integer> getConnectorIdHashMap;

    /**
     * LogData save class
     */
    LogDataSave logDataSave = new LogDataSave("log");
    /**
     * dump data save actions
     */
    String[] actionNames = { "Authorize", "StartTransaction", "StopTransaction", "MeterValues", "chargingAlarm" };
    ArrayList<String> actionList = new ArrayList<>();
    LogDataSave logDataSaveDump = new LogDataSave("dump");

    FileManagement fileManagement;

    /**
     * socket getter
     */
    public Socket getSocket() {
        return socket;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SocketReceiveMessage(String url) {
        this.url = url;
        this.fileManagement = new FileManagement();
        initHandlers(); // 핸들러 등록
        Collections.addAll(actionList, actionNames);
        onSocketInitialize();
    }

    public void onSocketInitialize() {
        try {
            if (socket != null) {
                socket.fullClose(); // 아래에서 정의
                socket = null;
            }

            if (hashMapUuid != null)
                hashMapUuid = null;
            hashMapUuid = new HashMap<String, String>();
            if (newHashMapUuid != null)
                newHashMapUuid = null;
            newHashMapUuid = new HashMap<String, Object>();
            // connectorId to channel (remoteStart ==> remoteStop)
            if (getConnectorIdHashMap != null)
                getConnectorIdHashMap = null;
            getConnectorIdHashMap = new HashMap<Integer, Integer>();

            socket = new Socket(url);
            socket.setState(SocketState.OPENING);
            socket.getInstance(this);

        } catch (Exception e) {
            logger.error("onSocketInitialize error  {}", e.getMessage());
        }
    }

    private void initHandlers() {
        // 신규 기능 추가 시 핸들러 클래스만 만들어서 여기에 한 줄 추가하면 끝입니다.
        handlerMap.put("BootNotification", new BootNotificationHandler());
        handlerMap.put("Heartbeat", new HeartbeatHandler());

        handlerMap.put("GetLocalListVersion", new GetLocalListVersionHandler());
        handlerMap.put("SendLocalList", new SendLocalListHandler());
        handlerMap.put("ChangeConfiguration", new ChangeConfigurationHandler());
        handlerMap.put("StatusNotification", new StatusNotificationHandler());
        handlerMap.put("ChangeAvailability", new ChangeAvailabilityHandler());
        handlerMap.put("ClearCache", new ClearCacheHandler());

        handlerMap.put("Authorize", new AuthorizeHandler());
        handlerMap.put("StartTransaction", new StartTransactionHandler());
        handlerMap.put("StopTransaction", new StopTransactionHandler());
        handlerMap.put("RemoteStartTransaction", new RemoteStartTransactionHandler());
        handlerMap.put("RemoteStopTransaction", new RemoteStopTransactionHandler());
        handlerMap.put("Reset", new ResetHandler());
        handlerMap.put("UpdateFirmware", new UpdateFirmwareHandler());
        handlerMap.put("FirmwareStatusNotification", new FirmwareStatusNotificationHandler());
        handlerMap.put("TriggerMessage", new TriggerMessageHandler());

        // DataTransfer messageId별 핸들러
        handlerMap.put("unitprice.req", new UnitPriceHandler());
        handlerMap.put("statusnoti", new StatusNotiHandler());
        handlerMap.put("chargingAlarm", new ChargingAlarmHandler());
        handlerMap.put("DTAuthorize", new DTAuthorizeHandler());    // Authorize 중복명 → DTAuthorize
        handlerMap.put("vehicleInfo", new VehicleInfoHandler());
        handlerMap.put("changeelecmode.req", new ChangeElecModeHandler());
        handlerMap.put("fullrechgsoc.req", new FullRechgSocHandler());
        handlerMap.put("userSetSoc", new UserSetSocHandler());
        handlerMap.put("MeterValues", new MeterValuesHandler());
        handlerMap.put("changemode.req", new ChangeModeHandler());
        handlerMap.put("rechgrsocschedule.req", new RechgrsocscheduleHandler());
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        socket.setState(SocketState.OPEN);
    }

    @Override
    public void onGetMessage(WebSocket webSocket, String text) throws JSONException {
        try {
            message = parse(text);
            if (message == null || message.getResultType() == null) {
                logger.warn("Failed to parse message (message or resultType is null): {}", text);
                return;
            }
            String actionName = "";
            int connectorIdForLog = 0;
            int resultType = message.getResultType();
            logger.info("onGetMessage received: resultType={}, id={}, text={}", resultType, message.getId(), text);

            /*
             * 1. Call(Type=2) 인 경우 → message.action 사용
             * 2. CallResult(Type=3) 인 경우 → UUID로 action 조회
             */
            if (resultType == 2) {
                actionName = message.getAction();
                JSONObject payload = new JSONObject((String) message.getPayload());

                if (payload.has("connectorId")) {
                    this.connectorId = payload.getInt("connectorId");
                    connectorIdForLog = this.connectorId;
                } else if ("DataTransfer".equals(actionName) && payload.has("data")) {
                    // DataTransfer의 경우 connectorId가 data JSON 문자열 내부에 있음
                    try {
                        JSONObject dataObj = new JSONObject(payload.getString("data"));
                        if (dataObj.has("connectorId")) {
                            this.connectorId = dataObj.getInt("connectorId");
                            connectorIdForLog = this.connectorId;
                        }
                    } catch (JSONException e) {
                        logger.warn("DataTransfer data parse error: {}", e.getMessage());
                    }
                }
            } else if (resultType == 3) {
                SendHashMapObject obj = (SendHashMapObject) newHashMapUuid.get(message.getId());
                if (obj != null) {
                    actionName = obj.getActionName();
                    this.connectorId = obj.getConnectorId();
                    connectorIdForLog = obj.getConnectorId();

                    // "Authorize" → "DTAuthorize" → DTAuthorizeHandler로 라우팅
                    if (obj.isDataTransfer() && actionName.equals("Authorize")) {
                        actionName = "DT" + actionName;
                    }
                } else {
                    logger.warn("No stored request for uuid={}", message.getId());
                    return;
                }
            } else {
                // Type=4 (CallError) 또는 알 수 없는 타입은 무시
                // 상대방이 요청을 처리하지 못했을 때 보내는 오류 응답 프레임
                logger.warn("Unknown resultType={}, ignoring message: {}", resultType, text);
                return;
            }

            // actionName이 비어 있으면 처리 불가
            if (actionName == null || actionName.isEmpty()) {
                logger.warn("actionName is empty, ignoring message: {}", text);
                return;
            }

            // log data save
            logDataSave.makeLogDate(connectorIdForLog, actionName, text);

            // payload null 체크
            if (message.getPayload() == null) {
                logger.warn("Payload is null for action={}, uuid={}", actionName, message.getId());
                return;
            }
            JSONObject payload = new JSONObject(message.getPayload().toString());
            OcppHandler handler = null;

            /**
             * DataTransfer 분기 처리
             */
            if ("DataTransfer".equals(actionName)) {
                // payload 안의 messageId 추출
                String messageId = payload.optString("messageId", "");
                if (messageId.isEmpty()) {
                    logger.warn("No messageId in DataTransfer message");
                    return;
                }

                // handlerMap에서 messageId로 핸들러 조회
                handler = handlerMap.get(messageId);
                if (handler == null) {
                    logger.warn("No handler for messageId={}", messageId);
                    return;
                }
                // DataTransfer handler 호출
                handler.handle(payload, connectorId, message.getId());
            } else {
                /**
                 * 일반 OCPP Action 처리
                 */
                handler = handlerMap.get(actionName);
                if (handler == null) {
                    logger.warn("No handler for action={}", actionName);
                    return;
                }
                handler.handle(payload, connectorId, message.getId());
            }

            // 공통 처리 (ID 삭제 등)
            if (message.getId() != null) {
                newHashMapUuid.remove(message.getId());
            }
        } catch (Exception e) {
            logger.error(" onGetMessage error : {}", e.getMessage(), e);
        }
    }

    @Override
    public void onGetFailure(WebSocket webSocket, Throwable t) {
        this.webSocket = webSocket;
        socket.setState(SocketState.RECONNECT_ATTEMPT);
        logger.error(t.toString());
    }

    // never used
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSend(String actionName, Request request) throws OccurenceConstraintException {
        if (!request.validate()) {
            logger.error("Can't send request: not validated. Payload {}: ", request);
            throw new OccurenceConstraintException();
        }
        try {
            String id = store(request);
            Object payload = packPayload(request);
            Object call = makeCall(id, actionName, payload);
            String actionNameCompare = null;
            if (call != null) {
                try {
                    this.webSocket.send(call.toString());
                    if (Objects.equals(actionName, "DataTransfer")) {
                        // DataTransfer 종류가 많아 ACTION_NAME 대신 MESSAGE_ID 를 Key 값으로 정의
                        // message_id 별로 parsing 해야 하는 부분이 있음.
                        JSONObject jsonObject = new JSONObject(payload.toString());
                        actionNameCompare = jsonObject.getString("messageId");
                        hashMapUuid.put(id, jsonObject.getString("messageId"));
                        logDataSave.makeLogDate(jsonObject.getString("messageId"), call.toString());
                    } else {
                        actionNameCompare = actionName;
                        hashMapUuid.put(id, actionName);
                        logDataSave.makeLogDate(actionName, call.toString());
                    }

                    logger.trace("Send a message : {}", call);
                } catch (Exception e) {
                    if (actionNameCompare == null) {
                        if (Objects.equals(actionName, "DataTransfer")) {
                            try {
                                org.json.JSONArray reqArray = new org.json.JSONArray(call.toString());
                                org.json.JSONObject payloadObj = reqArray.getJSONObject(3);
                                actionNameCompare = payloadObj.optString("messageId", "");
                            } catch (Exception ex) {
                                actionNameCompare = actionName;
                            }
                        } else {
                            actionNameCompare = actionName;
                        }
                    }

                    // dump data
                    if (actionNameCompare != null && actionList.contains(actionNameCompare)) {
                        int cId = 0;
                        try {
                            org.json.JSONArray reqArray = new org.json.JSONArray(call.toString());
                            org.json.JSONObject payloadObj = reqArray.getJSONObject(3);
                            if (payloadObj.has("connectorId")) {
                                cId = payloadObj.getInt("connectorId");
                            } else if (payloadObj.has("data")) { // Try to get from data string for DataTransfer
                                String dataStr = payloadObj.optString("data", "{}");
                                org.json.JSONObject dataObj = new org.json.JSONObject(dataStr);
                                if (dataObj.has("connectorId")) {
                                    cId = dataObj.getInt("connectorId");
                                }
                            }
                        } catch (Exception ex) {
                            logger.error("dump data error : {}", ex.getMessage());
                        }
                        logDataSaveDump.makeDump(cId, call.toString());
                    }

                    logDataSave.makeLogDate("<<send fail>>" + actionName, call.toString());
                    logger.error("send error  : {} ", e.toString());

                }
            }
        } catch (Exception e) {
            logger.error("onSend error  : {} ", e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSend(int connectorId, String actionName, Request request) throws OccurenceConstraintException {
        if (!request.validate()) {
            logger.error("multi Can't send request: not validated. Payload {}: ", request);
            throw new OccurenceConstraintException();
        }
        try {
            String id = store(request);
            Object payload = packPayload(request);
            Object call = makeCall(id, actionName, payload);
            String actionNameCompare = null;
            if (call != null) {
                try {
                    // this.webSocket.send(call.toString());
                    boolean isSent = false;
                    if (this.webSocket != null && socket != null && socket.getState() == SocketState.OPEN) {
                        isSent = this.webSocket.send(call.toString());
                    }
                    if (!isSent) {
                        throw new IllegalStateException("WebSocket is offline or send failed");
                    }
                    SendHashMapObject sendHashMapObject = new SendHashMapObject();
                    sendHashMapObject.setConnectorId(connectorId);
                    if (Objects.equals(actionName, "DataTransfer")) {
                        // DataTransfer 종류가 많아 ACTION_NAME 대신 MESSAGE_ID 를 Key 값으로 정의
                        // message_id 별로 parsing 해야 하는 부분이 있음.
                        JSONObject jsonObject = new JSONObject(payload.toString());
                        actionNameCompare = jsonObject.getString("messageId");
                        sendHashMapObject.setActionName(jsonObject.getString("messageId"));
                        sendHashMapObject.setDataTransfer(true);
                        newHashMapUuid.put(id, sendHashMapObject);
                        logDataSave.makeLogDate(connectorId, jsonObject.getString("messageId"), call.toString());
                    } else {
                        actionNameCompare = actionName;
                        sendHashMapObject.setActionName(actionName);
                        sendHashMapObject.setDataTransfer(false);
                        newHashMapUuid.put(id, sendHashMapObject);
                        logDataSave.makeLogDate(connectorId, actionName, call.toString());
                    }

                    logger.trace("Send a message: {}", call);
                } catch (Exception e) {
                    if (actionNameCompare == null) {
                        if (Objects.equals(actionName, "DataTransfer")) {
                            try {
                                org.json.JSONArray reqArray = new org.json.JSONArray(call.toString());
                                org.json.JSONObject payloadObj = reqArray.getJSONObject(3);
                                actionNameCompare = payloadObj.optString("messageId", "");
                            } catch (Exception ex) {
                                actionNameCompare = actionName;
                            }
                        } else {
                            actionNameCompare = actionName;
                        }
                    }

                    // dump data
                    if (actionNameCompare != null && actionList.contains(actionNameCompare)) {
                        logDataSaveDump.makeDump(connectorId, call.toString());
                    }

                    logDataSave.makeLogDate(connectorId, "<<send fail>>", call.toString());
                    logger.error("send error : {} ", e.toString());
                }
            }
        } catch (Exception e) {
            logger.error("onSend error : {} ", e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResultSend(String actionName, String uuid, Confirmation confirmation)
            throws OccurenceConstraintException {
        if (!confirmation.validate()) {
            logger.error("Can't send request:  not validated. Payload {}: ", confirmation);
            throw new OccurenceConstraintException();
        }
        try {
            Object call = makeCallResult(uuid, actionName, packPayload(confirmation));
            if (call != null) {
                this.webSocket.send(call.toString());
                logDataSave.makeLogDate(actionName, call.toString());
                logger.trace(" Send a message: {}", call);
            }
        } catch (Exception e) {
            logger.error("onResultSend : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResultSend(int connectorId, String actionName, String uuid, Confirmation confirmation)
            throws OccurenceConstraintException {
        if (!confirmation.validate()) {
            logger.error("[onResultSend2] Can't send request:  not validated. Payload {}: ", confirmation);
            throw new OccurenceConstraintException();
        }
        try {
            Object call = makeCallResult(uuid, actionName, packPayload(confirmation));
            if (call != null) {
                this.webSocket.send(call.toString());
                logDataSave.makeLogDate(connectorId, actionName, call.toString());
                logger.trace("[onResultSend2] Send a message: {}", call);
            }
        } catch (Exception e) {
            logger.error("onResultSend2 : {}", e.getMessage());
        }
    }

    @Override
    public void onCall(String id, String action, Object payload) {
        logger.trace("Send a message: id : {}, action : {}, payload : {}", id, action, payload.toString());
    }

    /**
     * Dump data send (미전송 데이터)
     *
     * @param text json string
     */
    public void onSend(int connectorId, String text) {
        try {
            this.webSocket.send(text);
            Message message = parse(text);
            String uuid = message.getId();
            String actionName = message.getAction();
            if (Objects.equals(actionName, "DataTransfer")) {
                JSONObject jsonObject = new JSONObject(message.getPayload().toString());
                actionName = jsonObject.getString("messageId");
                hashMapUuid.put(uuid, actionName);
            } else {
                hashMapUuid.put(uuid, actionName);
            }
            LogDataSave logDataSave = new LogDataSave("log");
            logDataSave.makeLogDate(connectorId, actionName, text);
            logger.trace(" Send a message : {}", message);
        } catch (Exception e) {
            logger.error(" onSend error : {} ", e.toString());
        }
    }

    public String store(Request request) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        return UUID.randomUUID().toString();
    }

    public android.os.Message onMakeHandlerMessage(
            int messageType,
            int connectorId,
            int delayTime,
            String idTag,
            String Uuid,
            String alarmCode,
            Boolean result) {
        try {
            android.os.Message msg = new android.os.Message();
            Bundle bundle = new Bundle();
            bundle.putInt("connectorId", connectorId);
            bundle.putInt("delay", delayTime);
            bundle.putString("idTag", idTag);
            bundle.putString("uuid", Uuid);
            bundle.putString("alarmCode", alarmCode);
            bundle.putBoolean("result", result);
            msg.setData(bundle);
            msg.what = messageType;
            return msg;
        } catch (Exception e) {
            logger.error("onMakeHandlerMessage error : {}", e.getMessage());
        }
        return null;
    }

    // idTag, parentIdTag return
    public String[] getLocalAuthorizationListStrings(String idTag) {
        boolean idTagCheck = false;
        String[] result = new String[2];
        try {
            String authorizationList = GlobalVariables.getRootPath() + File.separator + "localAuthorizationList";
            File targetFile = new File(authorizationList);
            if (targetFile.exists()) {
                JSONObject jsonObject = new JSONObject(fileManagement.getStringFromFile(authorizationList));
                JSONArray jsonArray = jsonObject.getJSONArray("localAuthorizationList");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject contDetail = jsonArray.getJSONObject(i);
                    if (Objects.equals(idTag, contDetail.getString("idTag"))) {
                        result[0] = contDetail.getString("idTag");
                        JSONObject idTagInfo = new JSONObject(contDetail.getString("idTagInfo"));
                        result[1] = idTagInfo.getString("parentIdTag");
                        idTagCheck = true;
                        break;
                    }
                }
            }
            // idTag 값이 없는 경우
            if (!idTagCheck) {
                result[0] = "notFound";
                result[1] = "";
            }
        } catch (Exception e) {
            logger.error("getLocalAuthorizationListStrings error : {}", e.getMessage());
        }
        return result;
    }
}
