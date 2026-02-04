package com.dongah.dispenser.pages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.wang.avi.AVLoadingIndicatorView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectorCheckFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectorCheckFragment extends Fragment {
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

    AVLoadingIndicatorView aviCheck;
    TextView textViewConnectorCheckMessage;
    int cnt = 0;
    RxData rxData;
    Handler countHandler;
    Runnable countRunnable;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];
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
        textViewConnectorCheckMessage = view.findViewById(R.id.textViewConnectorCheckMessage);
        aviCheck = view.findViewById(R.id.avi);
        chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
        chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);
            rxData = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel);
            cnt = 0;
            startAviAnim();

            // connection time out
            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countHandler = new Handler();
                    countRunnable = new Runnable() {
                        @Override
                        public void run() {
                            cnt++;

//                            if (Objects.equals(chargerConfiguration.getAuthMode(), "0") && Objects.equals(cnt, TIME_OUT)) {
//                                chargingCurrentData.setChgWait(true);
//                            }

                            if (Objects.equals(cnt, GlobalVariables.getConnectionTimeOut())) {
                                countHandler.removeCallbacks(countRunnable);
                                countHandler.removeCallbacksAndMessages(null);
                                countHandler.removeMessages(0);
                                // 충전기 종료
                                ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(mChannel).setStart(false);
                                ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(mChannel).setStop(false);

                                // preparing
                                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                        Objects.equals(chargerConfiguration.getOpMode(), 1) &&
                                        !((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel).isCsPilot()) {
                                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                                    chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.NoError);
                                    ((MainActivity) MainActivity.mContext).getProcessHandler().sendMessage(((MainActivity) MainActivity.mContext).getSocketReceiveMessage()
                                            .onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                    chargingCurrentData.getConnectorId(),
                                                    0,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                }
//                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
                                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.CONNECTION_FAILED, "CONNECTION_FAILED", null);
                            } else {
                                countHandler.postDelayed(countRunnable, 1000);
                            }

                            // connecting wait
                            if (rxData.isCsPilot()) {
                                if (textViewConnectorCheckMessage.getTag() == null || !(boolean) textViewConnectorCheckMessage.getTag()) {
                                    textViewConnectorCheckMessage.setText(R.string.EVCheckMessage);
                                    textViewConnectorCheckMessage.setTag(true);
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

    void startAviAnim() {
        aviCheck.show();
    }

    void stopAviAnim() {
        aviCheck.hide();
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
            stopAviAnim();
        } catch (Exception e) {
            Log.e("ConnectorCheckFragment", "onDetach error", e);
            logger.error("ConnectorCheckFragment onDetach error : {}", e.getMessage());
        }
    }
}