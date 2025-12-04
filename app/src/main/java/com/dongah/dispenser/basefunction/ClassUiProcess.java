package com.dongah.dispenser.basefunction;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.TECH3800.TLS3800;
import com.dongah.dispenser.TECH3800.TLS3800Listener;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.ocpp.core.ResetType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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

    public ClassUiProcess(int ch) {
        this.ch = ch;
        try {
            setUiSeq(UiSeq.INIT);

            // rf card
            tls3800 = ((MainActivity) MainActivity.mContext).getTls3800();
            tls3800.setTls3800Listener((TLS3800Listener) this);
            // configuration
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            // fragment change
            fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
            // control board
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            // TODO: alarm check
            // TODO: process handler
            // TODO: loop
        } catch (Exception e) {
            logger.error("ClassUiProcess - construct error : {}", e.getMessage());
        }
    }

    public void onHome() {
        try {
            setUiSeq(UiSeq.INIT);
            fragmentChange.onFragmentChange(getCh(), UiSeq.INIT, "INIT", null);
        } catch (Exception e) {
            Log.e("ClassUiProcess", "onHome error ", e);
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
}
