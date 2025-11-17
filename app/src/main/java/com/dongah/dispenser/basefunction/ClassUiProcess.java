package com.dongah.dispenser.basefunction;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.TECH3800.TLS3800Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUiProcess  {

    private static final Logger logger = LoggerFactory.getLogger(ClassUiProcess.class);

    int ch;
    UiSeq uiSeq;
    UiSeq oSeq;
    ChargerConfiguration chargerConfiguration;

    FragmentChange fragmentChange;

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

            // TODO: rf card
            // configuration
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            // fragment change
            fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
            // TODO: control board
            // TODO: alarm check
            // TODO: process handler
            // TODO: loop
        } catch (Exception e) {
            logger.error("ClassUiProcess - construct error : {}", e.getMessage());
        }
    }

    public void onHome() {
        setUiSeq(UiSeq.INIT);
        fragmentChange.onFragmentChange(getCh(), UiSeq.INIT, "INIT", null);
    }
}
