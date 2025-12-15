package com.dongah.dispenser.pages;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingWaitFragment extends Fragment implements View.OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(ChargingWaitFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    Handler handler;
    View[] dots;
    Drawable[] dotDrawables;
    LinearLayout linearLayoutLoadingContainer;
    final String[] colors = { "#B4C7E7", "#8FA3C6", "#6A8EA6", "#455C85", "#203864" };
    final int[] dotIds = { R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4, R.id.dot5 };
    int currentStep = -1;
    boolean running = false;

    private static final int STEP_DELAY_MS  = 600;  // 점 하나씩 표시 간격
    private static final int CYCLE_PAUSE_MS = 1000;  // 한 사이클 끝난 뒤 쉬는 시간

    int cnt = 0;
    RxData rxData;
    Handler countHandler;
    Runnable countRunnable;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;


    public ChargingWaitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingWaitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingWaitFragment newInstance(String param1, String param2) {
        ChargingWaitFragment fragment = new ChargingWaitFragment();
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
        View view = inflater.inflate(R.layout.fragment_charging_wait, container, false);
//        view.setOnClickListener(this);
        linearLayoutLoadingContainer = view.findViewById(R.id.linearLayoutLoadingContainer);
        handler = new Handler(Looper.getMainLooper());

        // 점 뷰/드로어블 캐싱
        dots = new View[dotIds.length];
        dotDrawables = new Drawable[dotIds.length];
        for (int i = 0; i < dotIds.length; i++) {
            dots[i] = view.findViewById(dotIds[i]);
            dots[i].setVisibility(View.INVISIBLE);

            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.parseColor(colors[i]));
            dotDrawables[i] = gd;
        }

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

            cnt = 0;
            rxData = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel);

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
                                countHandler.removeCallbacks(countRunnable);
                                countHandler.removeCallbacksAndMessages(null);
                                countHandler.removeMessages(0);
                                // 충전기 종료
                                ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(mChannel).setStart(false);
                                ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(mChannel).setStop(false);

                                // preparing
                                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                        Objects.equals(chargerConfiguration.getOpMode(), "1") &&
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
                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
                            } else {
                                countHandler.postDelayed(countRunnable, 1000);
                            }

                            // TODO: connecting wait
                            if (rxData.isCsPilot()) {
                                cnt = 0;
                            }
                        }
                    };
                    countHandler.postDelayed(countRunnable, 1000);
                }
            });
        } catch (Exception e) {
            Log.e("ChargingWaitFragment", "onViewCreated error", e);
            logger.error("ChargingWaitFragment onViewCreated error : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (!isAdded()) return;

//        stopDotLoop(); // 애니메이션 중지
        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.CHARGING);
        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.CHARGING, "CHARGING", null);
    }

//    private final Runnable loop = new Runnable() {
//        @Override public void run() {
//            if (!isAdded() || getView() == null) return;
//            if (!running || dots == null) return;
//
//            currentStep++;
//
//            if (currentStep < dots.length) {
//                // 현재 단계까지 누적해서 표시
//                for (int i = 0; i <= currentStep; i++) {
//                    dots[i].setVisibility(View.VISIBLE);
//                    dots[i].setBackground(dotDrawables[i]);
//                }
//                handler.postDelayed(this, STEP_DELAY_MS);
//            } else {
//                // 사이클 종료: 전부 숨기고 다시 시작
//                for (View d : dots) d.setVisibility(View.INVISIBLE);
//                currentStep = -1;
//                handler.postDelayed(this, CYCLE_PAUSE_MS);
//            }
//        }
//    };

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (handler != null) handler.removeCallbacksAndMessages(null);
            if (countHandler != null) {
                countHandler.removeCallbacks(countRunnable);
                countHandler.removeCallbacksAndMessages(null);
                countHandler.removeMessages(0);
            }
        } catch (Exception e) {
            Log.e("ChargingWaitFragment", "onDetach error", e);
            logger.error("ChargingWaitFragment onDetach error : {}", e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startDotLoop();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDotLoop();
    }

    private void startDotLoop() {
        if (running) return;
        running = true;
        currentStep = -1;
//        handler.post(loop);
    }

    private void stopDotLoop() {
        running = false;
//        handler.removeCallbacks(loop);
        if (dots != null) for (View d : dots) d.setVisibility(View.INVISIBLE);
    }
}