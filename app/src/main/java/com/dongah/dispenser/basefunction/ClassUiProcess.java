package com.dongah.dispenser.basefunction;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.TECH3800.TLS3800;
import com.dongah.dispenser.TECH3800.TLS3800Listener;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.handler.CustomStatusNotificationThread;
import com.dongah.dispenser.handler.ProcessHandler;
import com.dongah.dispenser.pages.FaultFragment;
import com.dongah.dispenser.rfcard.RfCardReaderListener;
import com.dongah.dispenser.rfcard.RfCardReaderReceive;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.ocpp.core.ResetType;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ClassUiProcess  {

    private static final Logger logger = LoggerFactory.getLogger(ClassUiProcess.class);

    int ch;
    UiSeq uiSeq;
    UiSeq oSeq;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;

    FragmentChange fragmentChange;
    TLS3800 tls3800;
    ControlBoard controlBoard;
    RfCardReaderReceive rfCardReaderReceive;
    SocketReceiveMessage socketReceiveMessage;
    ProcessHandler processHandler;
    Timer eventTimer;

    ZonedDateTimeConvert zonedDateTimeConvert;
    CustomStatusNotificationThread customStatusNotificationThread;

    int powerMeterCheck = 0;
    /**
     * MeterValue Thread
     * */
    // TODO

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
            // payment
            tls3800 = ((MainActivity) MainActivity.mContext).getTls3800();
            // configuration
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            // fragment change
            fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
            // control board
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            // TODO: alarm check
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
                    onMeterValueStop(); // MeterValue Stop
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
                case MEMBER_CARD_NO_MAC:
                case MEMBER_CHECK_WAIT:
                    break;
                case CHARGING_WAIT:
                    // Test Mode
//                    if (Objects.equals(chargerConfiguration.getOpMode(), "0") && !chargingCurrentData.isChgWait()) {
//                        break;
//                    }
                    if (!rxData.isCsPilot()) break;
                    controlBoard.getTxData(getCh()).setStart(true);
                    controlBoard.getTxData(getCh()).setStop(false);

                    if (rxData.isCsStart()) {
                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Charging);
                        chargingCurrentData.setPowerMeterStart(rxData.getPowerMeter()*10);
                        chargingCurrentData.setPowerMeterCalculate(rxData.getPowerMeter());
                        chargingCurrentData.setChargingStartTime(zonedDateTimeConvert.getStringCurrentTimeZone());

                        // Auto 및 Test mode
                        // socket receive message get instance
                        socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                        if (!Objects.equals(chargerConfiguration.getOpMode(), "1") ||
                                (SocketState.OPEN != socketReceiveMessage.getSocket().getState() && !GlobalVariables.isStopTransactionOnInvalidId())) {
                            setUiSeq(UiSeq.CHARGING);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(getCh(), UiSeq.CHARGING, "CHARGING", null);
                        }
                        // server mode
                        if (Objects.equals(chargerConfiguration.getOpMode(), "1")) {

                        }
                    }
                    break;
                case CHARGING:
                    try {
                        // 충전 사용량 계산
                        onUsePowerMeter(getCh(), rxData);
                        controlBoard.getTxData(getCh()).setUiSequence((short) 2);
                        // stop 조건
                        if (!GlobalVariables.isStopTransactionOnEVSideDisconnect() &&
                                !GlobalVariables.isUnlockConnectorOnEVSideDisconnect()) {
                            if (rxData.isCsStop() || !rxData.isCsPilot()) {
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
                            if (rxData.isCsStop() || !rxData.isCsPilot() || chargingCurrentData.isUserStop()) {
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
//                        if (!chargingCurrentData.isChgFinishWait()) break;
                        chargingCurrentData.setChgFinishWait(false);
                        //사용자 user stop
                        chargingCurrentData.setStopReason(chargingCurrentData.isUserStop() ? Reason.Local : chargingCurrentData.getStopReason());
                        // 충전 사용량 정리
                        chargingCurrentData.setPowerMeterStop(rxData.getPowerMeter()*10);
                        chargingCurrentData.setChargingEndTime(zonedDateTimeConvert.getStringCurrentTimeZone());
                        //stop transaction send to server
                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);
                        //socket receive message get instance
                        socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                        if (Objects.equals(chargerConfiguration.getOpMode(), "1")) {
                            processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                    GlobalVariables.MESSAGE_HANDLER_STOP_TRANSACTION,
                                    chargingCurrentData.getConnectorId(),
                                    0,
                                    chargingCurrentData.getIdTag(),
                                    null,
                                    null,
                                    false));
                            //status notification send to server
                            processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                    chargingCurrentData.getConnectorId(),
                                    0,
                                    null,
                                    null,
                                    null,
                                    false));
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

    public void onResetStop(int channel, ResetType resetType) {
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getUiSeq();
            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                controlBoard.getTxData(getCh()).setStop(true);
                controlBoard.getTxData(getCh()).setStart(false);
                chargingCurrentData.setUserStop(false);
                chargingCurrentData.setStopReason(resetType == ResetType.Hard ? Reason.HardReset : Reason.SoftReset);
                setUiSeq(UiSeq.FINISH_WAIT);
            }
        } catch (Exception e) {
            logger.error("reset stop error : {}", e.getMessage());
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
     * @param delay       delay time
     */
    public void onMeterValueStart(int connectorId, int delay) {
        // TODO
    }

    public void onMeterValueStop() {
        // TODO
    }

    public void onCustomStatusNotificationStart(int connectorId, int delay) {
        onCustomStatusNotificationStop();
        customStatusNotificationThread = new CustomStatusNotificationThread(connectorId,delay);
        customStatusNotificationThread.setStopped(false);
        customStatusNotificationThread.start();
    }

    public void onCustomStatusNotificationStop() {
        if (customStatusNotificationThread != null) {
            customStatusNotificationThread.interrupt();
            customStatusNotificationThread.setStopped(true);
            customStatusNotificationThread = null;
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
            chargingCurrentData.setFrequency(60);                           // 주파수
            chargingCurrentData.setChargingRemainTime(rxData.getRemainTime());  // 충전 남은 시간
            chargingCurrentData.setSoc(rxData.getSoc());
        } catch (Exception e) {
            logger.error("power meter calculate error : {}", e.getMessage());
        }
    }
}
