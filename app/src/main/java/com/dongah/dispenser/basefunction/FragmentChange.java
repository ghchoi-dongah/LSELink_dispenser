package com.dongah.dispenser.basefunction;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.pages.AdminPasswordFragment;
import com.dongah.dispenser.pages.ChargingFinishFragment;
import com.dongah.dispenser.pages.ChargingFinishWaitFragment;
import com.dongah.dispenser.pages.ChargingFragment;
import com.dongah.dispenser.pages.ChargingSequentialFragment;
import com.dongah.dispenser.pages.ChargingWaitFragment;
import com.dongah.dispenser.pages.ConfigSettingFragment;
import com.dongah.dispenser.pages.ConnectionFailedFragment;
import com.dongah.dispenser.pages.ControlDebugFragment;
import com.dongah.dispenser.pages.EnvironmentFragment;
import com.dongah.dispenser.pages.FaultFragment;
import com.dongah.dispenser.pages.FooterFragment;
import com.dongah.dispenser.pages.InitFragment;
import com.dongah.dispenser.pages.MemberCardFragment;
import com.dongah.dispenser.pages.MemberCardNoMacFragment;
import com.dongah.dispenser.pages.MemberCheckFailedFragment;
import com.dongah.dispenser.pages.MemberCheckWaitFragment;
import com.dongah.dispenser.pages.OperationStopFragment;
import com.dongah.dispenser.pages.ScreenSaverFragment;
import com.dongah.dispenser.pages.WebSocketDebugFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FragmentChange {

    public static final Logger logger = LoggerFactory.getLogger(FragmentChange.class);

    FragmentCurrent fragmentCurrent;

    public FragmentChange() {}

    public void onFragmentChange(int channel, UiSeq uiSeq, String sendText, String type) {
        Bundle bundle = new Bundle();
        bundle.putInt("CHANNEL", channel);
        ((MainActivity) MainActivity.mContext).setFragmentSeq(channel, uiSeq);
        int frameLayoutId = channel == 0 ? R.id.ch0 : R.id.ch1;
        // full = 1024*768, small = 512*692
        FragmentTransaction transaction = ((MainActivity) MainActivity.mContext).getSupportFragmentManager().beginTransaction();
        switch (uiSeq) {
            case INIT:
                try {
                    onFrameLayoutChange(false);
                    if (channel == 0) {
                        bundle.putInt("CHANNEL", 0);
                    } else {
                        bundle.putInt("CHANNEL", 1);
                    }
                    InitFragment initFragment = new InitFragment();
                    initFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, initFragment, sendText);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : INIT {}", e.getMessage());
                }
                break;
            case MEMBER_CHECK_WAIT:
                try {
                    onFrameLayoutChange(false);
                    MemberCheckWaitFragment memberCheckWaitFragment = new MemberCheckWaitFragment();
                    memberCheckWaitFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, memberCheckWaitFragment, "MEMBER_CHECK_WAIT");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : MEMBER_CHECK_WAIT {}", e.getMessage());
                }
                break;
            case CONNECTION_FAILED:
                try {
                    onFrameLayoutChange(false);
                    ConnectionFailedFragment connectionFailedFragment = new ConnectionFailedFragment();
                    connectionFailedFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, connectionFailedFragment, "CONNECTION_FAILED");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : CONNECTION_FAILED {}", e.getMessage());
                }
                break;
            case MEMBER_CARD:
                try {
                    onFrameLayoutChange(false);
                    MemberCardFragment memberCardFragment = new MemberCardFragment();
                    memberCardFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, memberCardFragment, "MEMBER_CARD");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : MEMBER_CARD {}", e.getMessage());
                }
            case MEMBER_CARD_NO_MAC:
                try {
                    onFrameLayoutChange(false);
                    MemberCardNoMacFragment memberCardNoMacFragment = new MemberCardNoMacFragment();
                    memberCardNoMacFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, memberCardNoMacFragment, "MEMBER_CARD_NO_MAC");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : MEMBER_CARD_NO_MAC {}", e.getMessage());
                }
                break;
            case MEMBER_CHECK_FAILED:
                try {
                    onFrameLayoutChange(false);
                    MemberCheckFailedFragment memberCheckFailedFragment = new MemberCheckFailedFragment();
                    memberCheckFailedFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, memberCheckFailedFragment, "MEMBER_CHECK_FAILED");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : MEMBER_CHECK_FAILED {}", e.getMessage());
                }
                break;
            case CHARGING_WAIT:
                try {
                    onFrameLayoutChange(false);
                    ChargingWaitFragment chargingWaitFragment = new ChargingWaitFragment();
                    chargingWaitFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, chargingWaitFragment, "CHARGING_WAIT");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : CHARGING_WAIT {}", e.getMessage());
                }
                break;
            case CHARGING:
                try {
                    onFrameLayoutChange(false);
                    ChargingFragment chargingFragment = new ChargingFragment();
                    chargingFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, chargingFragment, "CHARGING");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : CHARGING {}", e.getMessage());
                }
                break;
            case FINISH_WAIT:
                try {
                    onFrameLayoutChange(false);
                    ChargingFinishWaitFragment chargingFinishWaitFragment = new ChargingFinishWaitFragment();
                    chargingFinishWaitFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, chargingFinishWaitFragment, "FINISH_WAIT");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : FINISH_WAIT {}", e.getMessage());
                }
                break;
            case FINISH:
                try {
                    onFrameLayoutChange(false);
                    ChargingFinishFragment chargingFinishFragment = new ChargingFinishFragment();
                    chargingFinishFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, chargingFinishFragment, "FINISH");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : FINISH {}", e.getMessage());
                }
                break;
            case FAULT:
                try {
                    onFrameLayoutChange(false);
                    FaultFragment faultFragment = new FaultFragment();
                    faultFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, faultFragment, "FAULT");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : FAULT {}", e.getMessage());
                }
                break;
            case SEQUENTIAL_CHARGING:
                try {
                    onFrameLayoutChange(false);
                    ChargingSequentialFragment chargingSequentialFragment = new ChargingSequentialFragment();
                    chargingSequentialFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, chargingSequentialFragment, "SEQUENTIAL_CHARGING");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : SEQUENTIAL_CHARGING {}", e.getMessage());
                }
                break;
            case OP_STOP:
                try {
                    onFrameLayoutChange(false);
                    OperationStopFragment operationStopFragment = new OperationStopFragment();
                    operationStopFragment.setArguments(bundle);
                    transaction.replace(frameLayoutId, operationStopFragment, "OP_STOP");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : OP_STOP {}", e.getMessage());
                }
                break;
            case ADMIN_PASS:
                try {
                    onFrameLayoutChange(true);
                    AdminPasswordFragment adminPasswordFragment = new AdminPasswordFragment();
                    adminPasswordFragment.setArguments(bundle);
                    transaction.replace(R.id.frameFull, adminPasswordFragment, "ADMIN_PASS");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : ADMIN_PASS {}", e.getMessage());
                }
                break;
            case ENVIRONMENT:
                try {
                    onFrameLayoutChange(true);
                    EnvironmentFragment environmentFragment = new EnvironmentFragment();
                    environmentFragment.setArguments(bundle);
                    transaction.replace(R.id.frameFull, environmentFragment, "ENVIRONMENT");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : ENVIRONMENT {}", e.getMessage());
                }
                break;
            case CONFIG_SETTING:
                try {
                    onFrameLayoutChange(true);
                    ConfigSettingFragment configSettingFragment = new ConfigSettingFragment();
                    configSettingFragment.setArguments(bundle);
                    transaction.replace(R.id.frameFull, configSettingFragment, "CONFIG_SETTING");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : CONFIG_SETTING {}", e.getMessage());
                }
                break;
            case CONTROL_BOARD_DEBUGGING:
                try {
                    onFrameLayoutChange(true);
                    ControlDebugFragment controlDebugFragment = new ControlDebugFragment();
                    controlDebugFragment.setArguments(bundle);
                    transaction.replace(R.id.frameFull, controlDebugFragment, "CONTROL_BOARD_DEBUGGING");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : CONTROL_BOARD_DEBUGGING {}", e.getMessage());
                }
                break;
            case WEB_SOCKET:
                try {
                    onFrameLayoutChange(true);
                    WebSocketDebugFragment webSocketDebugFragment = new WebSocketDebugFragment();
                    webSocketDebugFragment.setArguments(bundle);
                    transaction.replace(R.id.frameFull, webSocketDebugFragment, "WEB_SOCKET");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : WEB_SOCKET {}", e.getMessage());
                }
                break;
            case SCREEN_SAVER:
                try {
                    onFrameLayoutChange(true);
                    ScreenSaverFragment screenSaverFragment = new ScreenSaverFragment();
                    screenSaverFragment.setArguments(bundle);
                    transaction.replace(R.id.frameFull, screenSaverFragment, "SCREEN_SAVER");
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : SCREEN_SAVER {}", e.getMessage());
                }
                break;
            default:
                logger.error("onFragmentChange error : default");
                break;
        }
    }

    public void onFrameLayoutChange(boolean hidden) {
        //main activity layout fullScreen change
        try {
            FrameLayout frameLayout0 = ((MainActivity) MainActivity.mContext).findViewById(R.id.ch0);
            FrameLayout frameLayout1 = ((MainActivity) MainActivity.mContext).findViewById(R.id.ch1);
            FrameLayout fullScreen = ((MainActivity) MainActivity.mContext).findViewById(R.id.frameFull);
            FrameLayout frameFooter = ((MainActivity) MainActivity.mContext).findViewById(R.id.frameFooter);
            View viewLine = ((MainActivity) MainActivity.mContext).findViewById(R.id.viewLine);

            /**
             * true: full screen
             * false: ch0, ch1
             * */
            if (hidden) {
                fullScreen.setVisibility(View.VISIBLE);
                frameLayout0.setVisibility(View.INVISIBLE);
                frameLayout1.setVisibility(View.INVISIBLE);
                frameFooter.setVisibility(View.INVISIBLE);
                viewLine.setVisibility(View.INVISIBLE);
            } else {
                onFrameLayoutRemove();
                fullScreen.setVisibility(View.INVISIBLE);
                frameLayout0.setVisibility(View.VISIBLE);
                frameLayout1.setVisibility(View.VISIBLE);
                frameFooter.setVisibility(View.VISIBLE);
                viewLine.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            logger.error("onFrameLayoutChange error : {}", e.getMessage());
        }
    }

    public void onFragmentFooterChange(int channel, String sendText) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("CHANNEL", channel);
            int frameLayoutId = R.id.frameFooter;
            FragmentTransaction transaction = ((MainActivity) MainActivity.mContext).getSupportFragmentManager().beginTransaction();
            FooterFragment footerFragment = new FooterFragment();
            transaction.replace(frameLayoutId, footerFragment, sendText);
            footerFragment.setArguments(bundle);
            transaction.commit();
        } catch (Exception e) {
            logger.error("onFragmentFooterChange error : {}", e.getMessage());
        }
    }

    public void onFrameLayoutRemove(){
        try {
            fragmentCurrent = new FragmentCurrent();
            FragmentTransaction transaction = ((MainActivity) MainActivity.mContext).getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragmentCurrent.getCurrentFragment();
            if (fragment != null) {
                transaction.remove(fragment); // 제거
                transaction.commit(); // UI 반영
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
