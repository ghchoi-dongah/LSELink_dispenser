package com.dongah.dispenser.basefunction;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.pages.FaultFragment;
import com.dongah.dispenser.rfcard.RfCardReaderListener;
import com.dongah.dispenser.rfcard.RfCardReaderReceive;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.ocpp.core.ResetType;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;
import com.dongah.dispenser.websocket.socket.handler.handlersend.ChargingAlarmReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.MeterValuesReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.ProcessHandler;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StartTransactionReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotificationReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StopTransactionReq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ClassUiProcess implements RfCardReaderListener {

    private static final Logger logger = LoggerFactory.getLogger(ClassUiProcess.class);

    int ch;
    UiSeq uiSeq;
    UiSeq oSeq;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;

    FragmentChange fragmentChange;
    ControlBoard controlBoard;
    NotifyFaultCheck notifyFaultCheck;
    RfCardReaderReceive rfCardReaderReceive;
    SocketReceiveMessage socketReceiveMessage;
    ProcessHandler processHandler;
    Timer eventTimer;

    ZonedDateTimeConvert zonedDateTimeConvert;

    int powerMeterCheck = 0;
    boolean chargingAlarm = true;
    MeterValuesReq meterValuesReq;

    public int getCh() {
        return ch;
    }

    public UiSeq getUiSeq() {
        return uiSeq;
    }

    public void setUiSeq(UiSeq uiSeq) {
        this.uiSeq = uiSeq;
    }

    public UiSeq getoSeq() {
        return oSeq;
    }

    public void setoSeq(UiSeq oSeq) {
        this.oSeq = oSeq;
    }

    public int getPowerMeterCheck() {
        return powerMeterCheck;
    }

    public void setPowerMeterCheck(int powerMeterCheck) {
        this.powerMeterCheck = powerMeterCheck;
    }


    public ClassUiProcess(int ch) {
        this.ch = ch;
        try {
            setUiSeq(UiSeq.INIT);
            zonedDateTimeConvert = new ZonedDateTimeConvert();

            // rf card
            rfCardReaderReceive = ((MainActivity) MainActivity.mContext).getRfCardReaderReceive();
            // configuration
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            // fragment change
            fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
            // control board
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            // alarm check
            notifyFaultCheck = new NotifyFaultCheck(ch);
            // process handler
            processHandler = ((MainActivity) MainActivity.mContext).getProcessHandler();
            // loop
            eventTimer = new Timer();
            eventTimer.schedule(new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    onEventAction();
                }
            }, 3000, 1000);
        } catch (Exception e) {
            Log.e("ClassUiProcess", "construct error", e);
            logger.error("ClassUiProcess - construct error : {}", e.getMessage());
        }
    }

    int getId = 0;
    int channel;
    boolean check;

    /**
     * charging sequence loop
     * server data send : 서버와 연결이 안된 경우 ProcessHandler dump data save
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onEventAction() {
        try {
            channel = getCh();
            RxData rxData = controlBoard.getRxData(getCh());
            check = rxData.isCsFault();
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(channel);
            chargingCurrentData.setIntegratedPower(rxData.getPowerMeter());
            getId = ((MainActivity) MainActivity.mContext).getFragmentSeq(getCh()).getValue();
            if (((MainActivity) MainActivity.mContext).getFragmentSeq(getCh()).getValue() < 18) onFaultCheck(rxData);

            // sequence check
            switch (getUiSeq()) {
                case NONE:
                case INIT:
                    setoSeq(UiSeq.INIT);
                    setPowerMeterCheck(0);
                    chargingCurrentData.setChgWait(false);
                    if (Objects.equals(controlBoard.getTxData(channel).getChargerPointMode(), 0)) {
                        controlBoard.getTxData(getCh()).setUiSequence((short) 1);
                        controlBoard.getTxData(getCh()).setStart(false);
                        controlBoard.getTxData(getCh()).setStop(false);
                    }
                    if (chargingCurrentData.isReBoot() && onRebootCheck()) {
                        setUiSeq(UiSeq.REBOOTING);
                    }
                    chargingAlarm = true;
//                    meterValuesReq.stopMeterValues();; // MeterValue Stop
                    onMeterValueStop();
                    break;
                case REBOOTING:
                    if (!(getCurrentFragment() instanceof FaultFragment)) {
                        fragmentChange.onFragmentChange(
                                getCh(),
                                UiSeq.REBOOTING,
                                "REBOOTING",
                                chargingCurrentData.getStopReason() == Reason.HardReset ? "Hard" : "Soft"
                        );
                    }
                    break;
                case MEMBER_CARD:
                case MEMBER_CHECK_WAIT:
                    break;
                case PLUG_CHECK:
                    if (rxData.isCsPilot()) {
                        controlBoard.getTxData(getCh()).setStart(true);
                        controlBoard.getTxData(getCh()).setStop(false);
                        setUiSeq(UiSeq.CONNECT_CHECK);
                    }
                    break;
                case CHARGING_WAIT:
                    break;
                case CONNECT_CHECK:
                    if (rxData.isCsStart()) {
                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Charging);
                        chargingCurrentData.setPowerMeterStart(rxData.getPowerMeter()*10);
                        chargingCurrentData.setPowerMeterCalculate(rxData.getPowerMeter());
                        chargingCurrentData.setChargingStartTime(zonedDateTimeConvert.getStringCurrentTimeZone());

                        // Auto 및 Test mode
                        // socket receive message get instance
                        socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                        if (!Objects.equals(chargerConfiguration.getOpMode(), 1) ||
                                (SocketState.OPEN != socketReceiveMessage.getSocket().getState() && !GlobalVariables.isStopTransactionOnInvalidId())) {
                            setUiSeq(UiSeq.CHARGING);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(getCh(), UiSeq.CHARGING, "CHARGING", null);
                        }
                        // server mode
                        if (Objects.equals(chargerConfiguration.getOpMode(), 1)) {

                            // start transaction send to server
                            // Accepted → Charging Fragment
                            StartTransactionReq startTransactionReq = new StartTransactionReq(chargingCurrentData.getConnectorId());
                            startTransactionReq.sendStartTransactionReq();
                        }
                    } else if (rxData.isCsStop() || rxData.getCsmSeccStatusCode() == (byte) 0x10) {
                        controlBoard.getTxData(getCh()).setStop(true);
                        controlBoard.getTxData(getCh()).setStart(false);
                        onHome();
                    }
                    break;
                case CHARGING:
                    try {
                        // 충전 사용량 계산
                        onUsePowerMeter(getCh(), rxData);
                        controlBoard.getTxData(getCh()).setUiSequence((short) 2);
                        // target soc
                        boolean isSocReached = (chargingCurrentData.getSoc() != 0
                                && chargingCurrentData.getSoc() >= chargerConfiguration.getTargetSoc());

                        // 충전율 90%
                        if (Objects.equals(chargingCurrentData.getSoc(), 90) && chargingAlarm) {
                            ChargingAlarmReq chargingAlarmReq = new ChargingAlarmReq(chargingCurrentData.getConnectorId());
                            chargingAlarmReq.sendChargingAlarmReq(2);
                            chargingAlarm = false;
                        }

                        // stop 조건
                        if (!GlobalVariables.isStopTransactionOnEVSideDisconnect() &&
                                !GlobalVariables.isUnlockConnectorOnEVSideDisconnect()) {
                            if (rxData.isCsStop() || !rxData.isCsPilot() || isSocReached) {
                                if (chargingCurrentData.getStopReason() == Reason.Remote || chargingCurrentData.isUserStop()) {
                                    controlBoard.getTxData(getCh()).setStop(true);
                                    controlBoard.getTxData(getCh()).setStop(false);
                                    if (!rxData.isCsPilot()) {
                                        // status notification send to server : ChargePointStatus.SuspendedEV
                                        // 2.4.5. EV Side Disconnected
                                        chargingCurrentData.setStopReason(Reason.EVDisconnected);
                                    }
                                    setUiSeq(UiSeq.FINISH_WAIT);
                                    fragmentChange.onFragmentChange(getCh(), UiSeq.FINISH_WAIT, "FINISH_WAIT", null);
                                }
                            }
                        } else {
                            if (rxData.isCsStop() || !rxData.isCsPilot() || chargingCurrentData.isUserStop() || isSocReached) {
                                controlBoard.getTxData(getCh()).setStop(true);
                                controlBoard.getTxData(getCh()).setStart(false);
                                if (!rxData.isCsPilot()) {
                                    // status notification send to server : ChargePointStatus.SuspendedEV
                                    // 2.4.5. EV Side Disconnected
                                    chargingCurrentData.setStopReason(Reason.EVDisconnected);
                                }
                                setUiSeq(UiSeq.FINISH_WAIT);
                                fragmentChange.onFragmentChange(getCh(), UiSeq.FINISH_WAIT, "FINISH_WAIT", null);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("ClassUiProcess - CHARGING error : {}", e.getMessage());
                    }
                    break;
                case FINISH_WAIT:
                    try {
                        controlBoard.getTxData(getCh()).setStop(true);
                        controlBoard.getTxData(getCh()).setUiSequence((short) 3);
                        onMeterValueStop();
                        //사용자 user stop
                        chargingCurrentData.setStopReason(chargingCurrentData.isUserStop() ? Reason.Local : chargingCurrentData.getStopReason());
                        // 충전 사용량 정리
                        chargingCurrentData.setPowerMeterStop(rxData.getPowerMeter()*10);
                        chargingCurrentData.setChargingEndTime(zonedDateTimeConvert.getStringCurrentTimeZone());
                        //stop transaction send to server
                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);


                        //socket receive message get instance
//                        socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                        if (Objects.equals(chargerConfiguration.getOpMode(), 1)) {
                            StatusNotificationReq req1 = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                            req1.sendStatusNotification();

                            // StopTransaction
                            StopTransactionReq stopTransactionReq = new StopTransactionReq(chargingCurrentData.getConnectorId());
                            stopTransactionReq.sendStopTransactionReq();

//                            StatusNotificationReq req2 = new StatusNotificationReq(chargingCurrentData.getConnectorId());
//                            req2.sendStatusNotification();
                        }
                        setUiSeq(UiSeq.FINISH);
                        fragmentChange.onFragmentChange(getCh(), UiSeq.FINISH, "FINISH", null);
                    } catch (Exception e) {
                        logger.error("ClassUiProcess - FINISH_WAIT error : {} ", e.getMessage());
                    }
                    break;
                case FINISH:
                    onFinish();
                    break;
                case FAULT:
                    UiSeq currentViewSeq = ((MainActivity) MainActivity.mContext).getFragmentSeq(getCh());
                    if (currentViewSeq.getValue() < 16) {
                        if (!(getCurrentFragment() instanceof FaultFragment)) {
                            // server mode 및 charging
                            if (Objects.equals(chargerConfiguration.getOpMode(), 1) &&
                                    Objects.equals(getoSeq(), UiSeq.CHARGING)) {
                                // meter values stop
                                meterValuesReq.stopMeterValues();
                                chargingCurrentData.setStopReason(rxData.isCsEmergency() ? Reason.EmergencyStop : Reason.Other);
                                controlBoard.getTxData(getCh()).setStop(true);
                                controlBoard.getTxData(getCh()).setStart(false);
                                chargingCurrentData.setUserStop(false);
                                chargingCurrentData.setPowerMeterStop(rxData.getPowerMeter()*10);
                                chargingCurrentData.setChargingEndTime(zonedDateTimeConvert.getStringCurrentTimeZone());
                                chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);

                                // socket receive message get instance
                                socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                                SocketState state = socketReceiveMessage.getSocket().getState();
                                if (Objects.equals(state.getValue(), 7) && Objects.equals(chargerConfiguration.getOpMode(), 1)) {
                                    StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                                    statusNotificationReq.sendStatusNotification();

                                    // server send
                                    StopTransactionReq stopTransactionReq = new StopTransactionReq(chargingCurrentData.getConnectorId());
                                    stopTransactionReq.sendStopTransactionReq();
                                }
                            }
                            fragmentChange.onFragmentChange(getCh(), UiSeq.FAULT, "FAULT", null);;
                        }
                    }

                    // fault 해제
                    if (!controlBoard.isDisconnected() && !rxData.isCsFault()) {
                        if (Objects.equals(getoSeq(), UiSeq.CHARGING)) {
                            chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);
                            chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.NoError);
                            StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                            statusNotificationReq.sendStatusNotification();
                            setUiSeq(UiSeq.FINISH);
                            fragmentChange.onFragmentChange(getCh(), UiSeq.FINISH, "FINISH", null);
                        } else {
                            if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                    !rxData.isCsPilot()) {
                                chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                                // socket receive message get instance
                                socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                                SocketState state = socketReceiveMessage.getSocket().getState();
                                if (Objects.equals(state.getValue(), 7) && Objects.equals(chargerConfiguration.getOpMode(), 1)) {
                                    StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                                    statusNotificationReq.sendStatusNotification();
                                }
                            }
                            onHome();
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e("ClassUiProcess", "onEventAction error ", e);
            logger.error("ClassUiProcess onEventAction error : {}", e.getMessage());
        }
    }

    public void onHome() {
        try {
            setUiSeq(UiSeq.INIT);
            fragmentChange.onFragmentChange(getCh(), UiSeq.INIT, "INIT", null);
        } catch (Exception e) {
            logger.error("ClassUiProcess onHome error : {}", e.getMessage());
        }
    }

    /** 충전 완료 */
    private void onFinish() {
        if (chargingCurrentData.isReBoot()) {
            setUiSeq(UiSeq.INIT);
        }
    }


    /**
     * 현재 Fragment 찾기
     *
     * @return fragment
     * */
    private Fragment getCurrentFragment() {
        return ((MainActivity) MainActivity.mContext).getSupportFragmentManager().findFragmentById(getCh() == 0 ? R.id.ch0 : R.id.ch1);
    }

    /**
     * Remote Transaction stop
     * */
    public void onRemoteTransactionStop(int channel, Reason reason) {
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getUiSeq();
            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
                chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(channel);

                controlBoard.getTxData(channel).setStop(true);
                controlBoard.getTxData(channel).setStart(false);
                chargingCurrentData.setUserStop(false);
                chargingCurrentData.setStopReason(reason);
            }
        } catch (Exception e) {
            logger.error("remote stop error : {}", e.getMessage());
        }
    }

//    public void onResetStop(int channel, ResetType resetType) {
//        try {
//            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getUiSeq();
//            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
//                controlBoard.getTxData(getCh()).setStop(true);
//                controlBoard.getTxData(getCh()).setStart(false);
//                chargingCurrentData.setUserStop(false);
//                chargingCurrentData.setStopReason(resetType == ResetType.Hard ? Reason.HardReset : Reason.SoftReset);
//                setUiSeq(UiSeq.FINISH_WAIT);
//            }
//        } catch (Exception e) {
//            logger.error("reset stop error : {}", e.getMessage());
//        }
//    }

    public void onResetStop(ResetType resetType) {
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(getCh()).getUiSeq();
            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                chargingCurrentData.setUserStop(false);
                chargingCurrentData.setStopReason(resetType == ResetType.Hard ? Reason.HardReset : Reason.SoftReset);
                setUiSeq(UiSeq.FINISH_WAIT);
            }
        } catch (Exception e) {
            logger.error("reset stop error : {} ", e.getMessage());
        }
    }

    private boolean onRebootCheck() {
        boolean result = false;
        try {
            UiSeq uiSeq1 = ((MainActivity) MainActivity.mContext).getClassUiProcess(0).getUiSeq();
            UiSeq uiSeq2 = ((MainActivity) MainActivity.mContext).getClassUiProcess(1).getUiSeq();
            result = Objects.equals(UiSeq.REBOOTING, uiSeq1) || Objects.equals(UiSeq.INIT, uiSeq1);
            result = result && Objects.equals(UiSeq.REBOOTING, uiSeq2) || Objects.equals(UiSeq.INIT, uiSeq2);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    /**
     * Meter value
     *
     * @param connectorId connector id
     */
    public void onMeterValueStart(int connectorId) {
        onMeterValueStop();
        meterValuesReq = new MeterValuesReq(connectorId);
        meterValuesReq.startMeterValues();
    }

    public void onMeterValueStop() {
        if (meterValuesReq != null) {
            meterValuesReq.stopMeterValues();
            meterValuesReq = null;
        }
    }

    private void onFaultCheck(RxData rxData) {
        try {
            //충전중 일 때 fault 가 발생한 경우
            if (controlBoard.isDisconnected() || rxData.csFault) {
                if (Objects.equals(getUiSeq(), UiSeq.CHARGING)) {
                    controlBoard.getTxData(getCh()).setStop(true);
                    controlBoard.getTxData(getCh()).setStart(false);
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Faulted);
                    chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.OtherError);
                    //비회원 충전 요금 단가 조정을 한다.
                    if (Objects.equals(chargingCurrentData.getPaymentType().value(), 2) &&
                            chargingCurrentData.getPrePayment() <= chargingCurrentData.getPowerMeterUsePay()) {
                        chargingCurrentData.setPowerMeterUsePay(chargingCurrentData.getPrePayment());
                    }
                }
                // fault 발생하기 전에 충전 스퀀스 저장
                if (getUiSeq() != UiSeq.FAULT) setoSeq(getUiSeq());
                setUiSeq(UiSeq.FAULT);
            }
            notifyFaultCheck.onErrorMessageMake(rxData);
        } catch (Exception e) {
            logger.error("onFaultCheck error.... : {}", e.toString());
        }
    }

    /**
     * 충전 사용량 계산
     *
     * @param rxData power meter raw data pick
     */
    private void onUsePowerMeter(int ch, RxData rxData) {
        try {
            long gapPower = 0;
            double gapPay = 0;
            if (rxData.getPowerMeter() > 0) {
                // current power meter --> charging CurrentData.powerKwh
                // 전력량 변화 여부 체크
                chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(ch);
                gapPower = rxData.getPowerMeter() - chargingCurrentData.getPowerMeterCalculate();
                gapPower = (gapPower <= 0) ? 0 : (gapPower > 10) ? 1 : gapPower;
                // 전력량 변화 여부 체크 892 = 8.92kW
                powerMeterCheck = gapPower == 0 ? powerMeterCheck + 1 : 0;

                chargingCurrentData.setPowerMeterUse(chargingCurrentData.getPowerMeterUse() + gapPower);
                chargingCurrentData.setPowerMeterCalculate(rxData.getPowerMeter());

                chargingCurrentData.setRemaintime(rxData.getRemainTime());
            }
            chargingCurrentData.setOutPutCurrent(rxData.getOutCurrent());   // 출력전류
            chargingCurrentData.setOutPutVoltage(rxData.getOutVoltage());   // 출력전압
            chargingCurrentData.setPowerMeter(rxData.getPowerMeter());      // 전력량
            chargingCurrentData.setTargetCurrent(rxData.getCsmEVTargetCurrent());   // 요청전류
            chargingCurrentData.setFrequency(60);                           // 주파수
            chargingCurrentData.setChargingRemainTime(rxData.getRemainTime());  // 충전 남은 시간
            chargingCurrentData.setSoc(rxData.getSoc());
        } catch (Exception e) {
            logger.error("power meter calculate error : {}", e.getMessage());
        }
    }

    /**
     * Rf CARD reader
     * @param cardNum card number
     * @param value boolean
     */
    @Override
    public void onRfCardDataReceive(String cardNum, boolean value) {
        try {
            if (cardNum.isEmpty() || Objects.equals(cardNum,"0000000000000000")) {
                setUiSeq(UiSeq.INIT);
                fragmentChange.onFragmentChange(getCh(), UiSeq.INIT,"INIT",null);
                Toast.makeText(((MainActivity) MainActivity.mContext), "카드 리더기에서 응답이 없습니다.",Toast.LENGTH_SHORT).show();
            } else {
                onRfCardDataReceiveEvent(cardNum, true);
            }
        } catch (Exception e) {
            logger.error("onRfCardDataReceive error : {} ", e.getMessage());
        }
    }

    private void onRfCardDataReceiveEvent(String cardNum, boolean b) {
        if (b) {
            try {
                if (Objects.equals(cardNum,"0000000000000000")) {
                    rfCardReaderReceive.rfCardReadRequest();
                } else if (!cardNum.isEmpty()) {

                    int authMode = ((MainActivity) MainActivity.mContext).getChargerConfiguration().getOpMode();
                    if (Objects.equals(authMode, 1)) {
                        //서버에 회원카드 정보를 보내여 인증을 취득하면 전역변수에 저장한다.
                        chargingCurrentData.setIdTag(cardNum);
                        setUiSeq(UiSeq.MEMBER_CHECK_WAIT);
                        fragmentChange.onFragmentChange(getCh(), UiSeq.MEMBER_CHECK_WAIT,"MEMBER_CHECK_WAIT",null);
                    }
//                    if (Objects.equals(authMode, "4") && !Objects.equals(getUiSeq(), UiSeq.MEMBER_CARD)) {
//                        // member save
//                        ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                FileManagement fileManagement = new FileManagement();
//                                boolean chk = fileManagement.stringToFileSave(GlobalVariables.ROOT_PATH, "memberList.dongah", cardNum, true);
//                                Toast.makeText((MainActivity.mContext), chk ? "저장 성공" : " 저장 실패 ",Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } else {
//                        //서버에 회원카드 정보를 보내여 인증을 취득하면 전역변수에 저장한다.
//                        chargingCurrentData.setIdTag(cardNum);
//                        setUiSeq(UiSeq.MEMBER_CHECK_WAIT);
//                        fragmentChange.onFragmentChange(getCh(), UiSeq.MEMBER_CHECK_WAIT,"MEMBER_CHECK_WAIT",null);
//                    }
                    rfCardReaderReceive.rfCardReadRelease();
                }
            } catch (Exception e) {
                logger.error("onRfCardDataReceiveEvent error : {} ", e.getMessage());
            }
        }
    }
}
