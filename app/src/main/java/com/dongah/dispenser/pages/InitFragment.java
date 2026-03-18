package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargerPointType;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.utils.BitUtilities;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitFragment extends Fragment implements View.OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(InitFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    Animation animBlink;
    View viewCircle;
    TextView textViewInitMessage, textViewConnector;
    ImageView imageViewBus, imageViewFault;

    MainActivity activity;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];

    Handler handler;
    Runnable runnable;
    RxData rxData;

    public InitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InitFragment newInstance(String param1, String param2) {
        InitFragment fragment = new InitFragment();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        view.setOnClickListener(this);
        animBlink = AnimationUtils.loadAnimation(getActivity(), R.anim.blink_animation);
        activity = ((MainActivity) MainActivity.mContext);
        chargerConfiguration = activity.getChargerConfiguration();
        chargingCurrentData = activity.getChargingCurrentData(mChannel);
        textViewInitMessage = view.findViewById(R.id.textViewInitMessage);
        textViewInitMessage.startAnimation(animBlink);
        textViewConnector = view.findViewById(R.id.textViewConnector);
        imageViewBus = view.findViewById(R.id.imageViewBus);
        viewCircle = view.findViewById(R.id.viewCircle);
        viewCircle.setOnClickListener(this);
        imageViewFault = view.findViewById(R.id.imageViewFault);
        rxData = activity.getControlBoard().getRxData(mChannel);

        try {
            if (chargingCurrentData.isConnectUse()) {
                textViewInitMessage.setText(R.string.initMessage);
                imageViewFault.setVisibility(View.INVISIBLE);
            } else {
                textViewInitMessage.setText(R.string.changeModeMessage);
                imageViewFault.setVisibility(View.VISIBLE);
            }

            boolean isCsReady = rxData.isCsReady() && chargingCurrentData.isConnectUse();
            boolean isChannel = (mChannel == 0);

            viewCircle.setBackgroundResource(
                    isCsReady ? R.drawable.layer_list_oval_yellow : R.drawable.layer_list_oval_blue
            );

            imageViewBus.setBackgroundResource(
                    isCsReady ? R.drawable.bus_yellow : R.drawable.bus_blue
            );

            imageViewBus.setScaleX(isChannel ? 1f : -1f);

            String connectorText = isChannel ? "1 커넥터" : "2 커넥터";
            if (isCsReady) {
                connectorText += "(순차모드)";
            }
            textViewConnector.setText(connectorText);

        } catch (Exception e) {
            Log.e("InitFragment", "onCreateView error", e);
            logger.error("InitFragment onCreateView error : {}", e.getMessage());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            requestStrings[0] = String.valueOf(0);
            sharedModel.setMutableLiveData(requestStrings);
        } catch (Exception e) {
            Log.e("InitFragment", "onViewCreated error", e);
            logger.error("InitFragment onViewCreated : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (!chargingCurrentData.isConnectUse()) return;
        if (!Objects.equals(v.getId(), R.id.viewCircle) && !rxData.isCsPilot()) return;
        changeFragment();
    }

    private void changeFragment() {
        try {
            chargingCurrentData.onCurrentDataClear();   // clear
            chargingCurrentData.setConnectorId(mChannel + 1);

            activity.getChargingCurrentData(mChannel).setChargerPointType(ChargerPointType.COMBO);
            activity.getChargingCurrentData(mChannel).setConnectorId(mChannel + 1);

            if (Objects.equals(chargerConfiguration.getOpMode(), 0)) {
                // test mode
                double testPrice = Double.parseDouble(activity.getChargerConfiguration().getTestPrice());
                activity.getChargingCurrentData(mChannel).setPowerUnitPrice(testPrice);
                activity.getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                activity.getFragmentChange().onFragmentChange(mChannel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
            } else if (Objects.equals(chargerConfiguration.getOpMode(), 1)) {
                // server mode
                try {
                    SocketState socketState = activity.getSocketReceiveMessage().getSocket().getState();
                    if (Objects.equals(socketState, SocketState.OPEN)) {
                        switch (chargerConfiguration.getAuthMode()) {
                            case 0:
                            case 2:
                                String evccId = BitUtilities.toHexString(rxData.getCsmVehicleEvccId());
                                activity.getChargingCurrentData(mChannel).setAuthType("M");
//                                chargingCurrentData.setIdTag(evccId);
                                chargingCurrentData.setIdTag("1364747EE708");
                                activity.getClassUiProcess(mChannel).setUiSeq(UiSeq.MEMBER_CHECK_WAIT);
                                activity.getFragmentChange().onFragmentChange(mChannel, UiSeq.MEMBER_CHECK_WAIT, "MEMBER_CHECK_WAIT", null);
                                break;
                            case 1:
                                activity.getChargingCurrentData(mChannel).setAuthType("C");
                                activity.getClassUiProcess(mChannel).setUiSeq(UiSeq.MEMBER_CARD);
                                activity.getFragmentChange().onFragmentChange(mChannel, UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
                                break;
                            default:
                                logger.error("InitFragment changeFragment error >> Invalid value");
                                break;
                        }
                    } else {
                        activity.getToastPositionMake().onShowToast(mChannel, "서버 연결 DISCONNECT.\n충전을 할 수 없습니다.");
                    }
                } catch (Exception e) {
                    activity.getToastPositionMake().onShowToast(mChannel, "서버 연결 DISCONNECT.\n충전을 할 수 없습니다.");
                    Log.e("InitFragment", "server disconnect error", e);
                    logger.error("InitFragment server disconnect error : {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e("InitFragment", "changeFragment error", e);
            logger.error("InitFragment changeFragment error : {}", e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);

            if (handler != null) {
                handler.removeCallbacks(runnable);
                handler.removeCallbacksAndMessages(null);
                handler.removeMessages(0);
            }
        } catch (Exception e) {
            Log.e("InitFragment", "onDetach error", e);
            logger.error("InitFragment onDetach error : {}", e.getMessage());
        }
    }
}