package com.dongah.dispenser.pages;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.controlboard.TxData;
import com.dongah.dispenser.utils.BitUtilities;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;
import com.dongah.dispenser.websocket.socket.handler.handlersend.AuthorizeReq;
import com.dongah.dispenser.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectorCheckFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectorCheckFragment extends Fragment implements View.OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorCheckFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;


    int cnt = 0;
    boolean isFlag = false, isFlagAuthorize = true;
    TextView textViewConnectorCheckMessage, textViewFailed, textViewConnector;
    ImageView imageViewLoading, imageViewConnectionFailed;
    AnimationDrawable animationDrawable;
    ObjectAnimator fadeAnimator;
    RxData rxData;
    TxData txData;
    Handler countHandler;
    Runnable countRunnable;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];
    ClassUiProcess classUiProcess;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;

    public ConnectorCheckFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectorCheckFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectorCheckFragment newInstance(String param1, String param2) {
        ConnectorCheckFragment fragment = new ConnectorCheckFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mChannel = getArguments().getInt(CHANNEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connector_check, container, false);
        view.setOnClickListener(this);
        textViewConnectorCheckMessage = view.findViewById(R.id.textViewConnectorCheckMessage);
        imageViewLoading = view.findViewById(R.id.imageViewLoading);
        imageViewLoading.setBackgroundResource(R.drawable.ani_loading);
        animationDrawable = (AnimationDrawable) imageViewLoading.getBackground();
        imageViewConnectionFailed = view.findViewById(R.id.imageViewConnectionFailed);
        textViewFailed = view.findViewById(R.id.textViewFailed);

        // textViewFailed animation
        fadeAnimator = ObjectAnimator.ofFloat(textViewFailed, "alpha", 1f, 0.2f);
        fadeAnimator.setDuration(1000);
        fadeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        fadeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        fadeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        textViewConnector = view.findViewById(R.id.textViewConnector);
        classUiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel);
        chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
        chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);
            rxData = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel);
            txData = ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(mChannel);
            cnt = 0;
            isFlag = false;
            isFlagAuthorize = true;
            animationDrawable.start();

            // connection time out
            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countHandler = new Handler();
                    countRunnable = new Runnable() {
                        @Override
                        public void run() {
                            cnt++;
                            if (Objects.equals(cnt, GlobalVariables.getConnectionTimeOut())) {
                                // 충전기 종료
                                txData.setStart(false);
                                txData.setStop(true);

                                // preparing
                                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                        Objects.equals(chargerConfiguration.getOpMode(), 1) &&
                                        !((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel).isCsPilot()) {
                                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                                    chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.NoError);

                                    // StatusNotification
                                    StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                                    statusNotificationReq.sendStatusNotification();
                                }

                                // 통신 실패 처리
                                authorizeFailed();
                            } else {
                                countHandler.postDelayed(countRunnable, 1000);
                            }

                            // connecting wait
                            if (rxData.isCsPilot()) {
                                if (textViewConnectorCheckMessage.getTag() == null || !(boolean) textViewConnectorCheckMessage.getTag()) {
                                    textViewConnectorCheckMessage.setText(R.string.EVCheckMessage);
                                    textViewConnectorCheckMessage.setTag(true);

                                    // auto mode는 바로 CONNECT_CHECK
//                                    switch (chargerConfiguration.getOpMode()) {
//                                        case 0:
//                                            // test mode
//                                            classUiProcess.setUiSeq(UiSeq.CONNECT_CHECK);
//                                            break;
//                                        case 1:
//                                            // server mode
//                                            // auth type = 'M'면 authorize 진행. 'C'면 CONNECT_CHECk
//                                            if (Objects.equals(chargingCurrentData.getAuthType(), "M")) {
//                                                authorized();
//                                            } else if (Objects.equals(chargingCurrentData.getAuthType(), "C")) {
//                                                classUiProcess.setUiSeq(UiSeq.CONNECT_CHECK);
//                                            }
//                                            break;
//                                    }
                                    // start가 진행되어야 함.
                                    // server mode는 회원 인증 완료 후 CONNECT_CHECK
                                    if (Objects.equals(chargerConfiguration.getOpMode(), 1) &&
                                            Objects.equals(chargingCurrentData.getAuthType(), "M") &&
                                            txData.isStart() && isFlagAuthorize) {
                                        authorized();
                                        isFlagAuthorize = false;
                                    }
                                }
                            }
                        }
                    };
                    countHandler.postDelayed(countRunnable, 1000);
                }
            });
        } catch (Exception e) {
            Log.e("ConnectorCheckFragment", "onViewCreated error", e);
            logger.error("ConnectorCheckFragment onViewCreated error : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (!isAdded() && !isFlag) return;
        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void authorized() {
        String[] idTagInfo;
        UiSeq uiSeq = classUiProcess.getUiSeq();
        SocketReceiveMessage socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();

        String evccId = BitUtilities.toHexString(rxData.getCsmVehicleEvccId());
        chargingCurrentData.setIdTag(evccId);
        Log.d("ConnectorCheckFragment", "mac address : " + evccId);

        // isLocalPreAuthorize == true : local authorization list 에서 사용자 인증
        // isLocalPreAuthorize: 사전 로컬 인증 모드
        if (GlobalVariables.isLocalPreAuthorize()) {
            // local authorization enabled --> local 인증
            idTagInfo = socketReceiveMessage.getLocalAuthorizationListStrings(uiSeq == UiSeq.CHARGING ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag());
            if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
                if (Objects.equals(chargingCurrentData.getParentIdTag(), idTagInfo[1]) ||
                        Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop())) {
                    classUiProcess.setUiSeq(UiSeq.FINISH_WAIT);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.FINISH_WAIT, "FINISH_WAIT", null);
                } else  {
                    classUiProcess.setUiSeq(UiSeq.CHARGING);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.CHARGING, "CHARGING", null);
                }
            } else {
                if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                        Objects.equals(chargerConfiguration.getOpMode(), 1)) {
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                    StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                    statusNotificationReq.sendStatusNotification();
                }

                if (Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag())) {
                    chargingCurrentData.setAuthorizeResult(true);
                    chargingCurrentData.setParentIdTag(idTagInfo[1]);
                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                } else if (Objects.equals(idTagInfo[0], "notFound")) {
                    AuthorizeReq authorizeReq = new AuthorizeReq(chargingCurrentData.getConnectorId());
                    authorizeReq.sendAuthorize("C" + chargingCurrentData.getIdTag());
                } else {
                    // 인증 실패
                    ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel).setAuthorizeResult(false);
                    authorizeFailed();
                    RxData rxData = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel);
                    if (!rxData.isCsPilot() && Objects.equals(chargerConfiguration.getOpMode(), 1)) {
                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                        StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                        statusNotificationReq.sendStatusNotification();
                    }
                }
            }
        } else {
            // central system send
            SocketState state = socketReceiveMessage.getSocket().getState();
            if (state == SocketState.OPEN) {
                if (Objects.equals(UiSeq.CHARGING, uiSeq) && Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop())) {
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.FINISH_WAIT, "FINISH_WAIT", null);
                } else {
                    if (chargingCurrentData.getChargePointStatus() == ChargePointStatus.Reserved) {
                        if (!Objects.equals(chargingCurrentData.getResIdTag(), chargingCurrentData.getIdTag())) {
                            Toast.makeText(getActivity(), "예약한 IdTag가 틀립니다. ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    AuthorizeReq authorizeReq = new AuthorizeReq(chargingCurrentData.getConnectorId());
                    authorizeReq.sendAuthorize("C" + chargingCurrentData.getIdTag());
                }
            } else {
                // 서버와 연결이 안된 경우
                // isLocalAuthorizeOffline: 서버 연결이 끊겼을 때 오프라인 로컬 인증 허용 여부
                if (GlobalVariables.isLocalAuthorizeOffline()) {
                    // local authorization enabled --> local 인증
                    idTagInfo = socketReceiveMessage.getLocalAuthorizationListStrings(uiSeq == UiSeq.CHARGING ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag());
                    if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
                        if (Objects.equals(chargingCurrentData.getParentIdTag(), idTagInfo[1]) ||
                                Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop())) {
                            classUiProcess.setUiSeq(UiSeq.FINISH_WAIT);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.FINISH_WAIT, "FINISH_WAIT", null);
                        } else {
                            classUiProcess.setUiSeq(UiSeq.CHARGING);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.CHARGING, "CHARGING", null);
                        }
                    } else {
                        // isAllowOfflineTxForUnknownId: 오프라인에서 미등록 IdTag도 거래 허용
                        if (Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag()) || GlobalVariables.isAllowOfflineTxForUnknownId() ||
                                GlobalVariables.isStopTransactionOnInvalidId()) {
                            chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                            StatusNotificationReq statusNotificationReq = new StatusNotificationReq(chargingCurrentData.getConnectorId());
                            statusNotificationReq.sendStatusNotification();

                            // isStopTransactionOnInvalidId: 미등록 IdTag로 시작했으면 나중에 중단 사유 세팅
                            chargingCurrentData.setStopReason(!Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag()) &&
                                    GlobalVariables.isStopTransactionOnInvalidId() ? Reason.DeAuthorized : chargingCurrentData.getStopReason());
                            ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                        } else {
                            // 인증 실패
                            authorizeFailed();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "서버와 통신 DISCONNECT!!! 인증 실패. ", Toast.LENGTH_SHORT).show();
                    if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.CHARGING);
                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING, "CHARGING", null);
                    } else {
                        authorizeFailed();
                    }
                }
            }
        }
    }

    private void authorizeFailed() {
        try {
            textViewConnectorCheckMessage.setText(R.string.connectorRetryMessage);
            imageViewLoading.setVisibility(View.INVISIBLE);
            imageViewConnectionFailed.setVisibility(View.VISIBLE);
            textViewFailed.setVisibility(View.VISIBLE);
            textViewConnector.setVisibility(View.VISIBLE);
            animationDrawable.stop();
            fadeAnimator.start();
            isFlag = true;
        } catch (Exception e) {
            logger.error("ConnectorCheckFragment authorizeFailed error : {}", e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        try {
            if (fadeAnimator != null) {
                fadeAnimator.cancel();
                fadeAnimator = null;
            }

            if (animationDrawable != null) {
                animationDrawable.stop();
            }

            if (imageViewLoading != null) {
                Drawable bg = imageViewLoading.getBackground();
                if (bg instanceof AnimationDrawable) {
                    ((AnimationDrawable) bg).stop();
                }
                imageViewLoading.setBackground(null);
            }

            if (countHandler != null) {
                countHandler.removeCallbacksAndMessages(null);
                countHandler = null;
            }
            countRunnable = null;

        } catch (Exception e) {
            Log.e("ConnectorCheckFragment", "onDestroyView error", e);
            logger.error("ConnectorCheckFragment onDestroyView error : {}", e.getMessage());
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (countHandler != null) {
                countHandler.removeCallbacks(countRunnable);
                countHandler.removeCallbacksAndMessages(null);
                countHandler.removeMessages(0);
            }
        } catch (Exception e) {
            Log.e("ConnectorCheckFragment", "onDetach error", e);
            logger.error("ConnectorCheckFragment onDetach error : {}", e.getMessage());
        }
    }
}