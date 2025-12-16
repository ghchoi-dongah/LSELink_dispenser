package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingFragment extends Fragment implements View.OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(ChargingFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    Button btnChargingStop;
    ImageView imageViewBattery, imageViewBatteryValue;
    TextView textViewSocValue;
    TextView textViewChargingAmtValue, textViewChargingTimeRemainValue, textViewChargingTimeValue;
    TextView textViewChargingVoltageValue, textViewChargingPowerValue, textViewChargingCurrentValue, textViewRequestCurrentValue;


    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean blinkVisible = true;
    private boolean running = false;

    int soc = 0;
    private static final int MAX_SOC = 90;
    private static final int BLINK_INTERVAL = 500;  // 깜빡임 간격(ms)

    MediaPlayer mediaPlayer;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];
    Handler uiUpdateHandler;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;

    Date startTime = null, useTime = null;
    DecimalFormat powerFormatter = new DecimalFormat("#,###,##0.00");
    DecimalFormat voltageFormatter = new DecimalFormat("#,###,##0.0");
    ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();

    public ChargingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingFragment newInstance(String param1, String param2) {
        ChargingFragment fragment = new ChargingFragment();
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
        View view = inflater.inflate(R.layout.fragment_charging, container, false);
        btnChargingStop = view.findViewById(R.id.btnChargingStop);
        btnChargingStop.setOnClickListener(this);
        imageViewBattery = view.findViewById(R.id.imageViewBattery);
        imageViewBatteryValue = view.findViewById(R.id.imageViewBatteryValue);
        textViewSocValue = view.findViewById(R.id.textViewSocValue);
        textViewChargingAmtValue = view.findViewById(R.id.textViewChargingAmtValue);
        textViewChargingTimeRemainValue = view.findViewById(R.id.textViewChargingTimeRemainValue);
        textViewChargingTimeValue = view.findViewById(R.id.textViewChargingTimeValue);
        textViewChargingVoltageValue = view.findViewById(R.id.textViewChargingVoltageValue);
        textViewChargingPowerValue = view.findViewById(R.id.textViewChargingPowerValue);
        textViewChargingCurrentValue = view.findViewById(R.id.textViewChargingCurrentValue);
        textViewRequestCurrentValue = view.findViewById(R.id.textViewRequestCurrentValue);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
            setSoc(chargingCurrentData.getSoc());

            updateBatteryUI();  // soc에 따른 이미지 갱신
            startBlink();       // 깜빡임 시작
            mediaPlayer();      // media player

            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);

            try {
                startTime = zonedDateTimeConvert.doStringDateToDate(chargingCurrentData.getChargingStartTime());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            onCharging();
        } catch (Exception e) {
            logger.error("ChargingFragment onViewCreated error : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (Objects.equals(v.getId(), R.id.btnChargingStop)) {
            ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel).setUserStop(true);
            ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(mChannel).setStop(true);
            ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(mChannel).setStart(false);
        }
    }
    
    private void onCharging() {
        uiUpdateHandler = new Handler();
        uiUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                     @SuppressLint({"SetTextI18n", "DefaultLocale"})
                     @RequiresApi(api = Build.VERSION_CODES.O)
                     @Override
                     public void run() {
                         try {
                             long diffTime = 0;
                             useTime = zonedDateTimeConvert.doStringDateToDate(zonedDateTimeConvert.getStringCurrentTimeZone());

                             if (useTime != null) {
                                 diffTime = (useTime.getTime() - startTime.getTime()) / 1000;
                                 int hour = (int) diffTime / 3600;
                                 int minute = (int) (diffTime % 3600) / 60;
                                 int second = (int) diffTime % 60;
                                 chargingCurrentData.setChargingTime((int) diffTime);
                                 textViewChargingTimeValue.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second));
                                 chargingCurrentData.setChargingUseTime(textViewChargingTimeValue.getText().toString());

                                 textViewChargingAmtValue.setText(powerFormatter.format(chargingCurrentData.getPowerMeterUse() * 0.01) + "kWh");

                                 int rHour = chargingCurrentData.getRemaintime() / 3600;
                                 int rMinute = (chargingCurrentData.getRemaintime() % 3600) / 60;
                                 int rSecond = chargingCurrentData.getRemaintime() % 60;

                                 textViewChargingTimeRemainValue.setText(String.format("%02d", rHour) + ":" + String.format("%02d", rMinute) + ":" + String.format("%02d", rSecond));

                                 textViewSocValue.setText(chargingCurrentData.getSoc() + "%");
                                 setSoc(chargingCurrentData.getSoc());
                                 updateBatteryUI();

                                 textViewChargingVoltageValue.setText(voltageFormatter.format(chargingCurrentData.getOutPutVoltage() * 0.1) + " V");
                                 textViewChargingCurrentValue.setText(powerFormatter.format(chargingCurrentData.getOutPutCurrent() * 0.1) + " A");
                                 textViewChargingPowerValue.setText(powerFormatter.format(chargingCurrentData.getOutPutVoltage() * chargingCurrentData.getOutPutCurrent() * 0.00001) + " kW");
                                 // TODO: 요청전류
                             }
                         } catch (Exception e) {
                             Log.e("ChargingFragment", "onCharging error", e);
                             logger.error("ChargingFragment onCharging error : {}", e.getMessage());
                         }
                     }
                 });
                uiUpdateHandler.postDelayed(this, 1000);
            }
        }, 50);
    }
    
    private void mediaPlayer() {
        releasePlayer();
        
        try {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.charging);
            mediaPlayer.setOnCompletionListener(me -> releasePlayer());
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("ChargingFragment", "mediaPlayer error", e);
            logger.error("ChargingFragment mediaPlayer error : {}", e.getMessage());
        }
    }
    
    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e("ChargingFragment", "releasePlayer error", e);
                logger.error("ChargingFragment releasePlayer error : {}", e.getMessage());
            }
            mediaPlayer = null;
        }
    }

    // 깜빡임 루프
    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            imageViewBatteryValue.setVisibility(blinkVisible ? View.VISIBLE : View.INVISIBLE);
            blinkVisible = !blinkVisible;
            handler.postDelayed(this, BLINK_INTERVAL);
        }
    };

    private void updateBatteryUI() {
        // SOC 구간별 이미지 설정
        if (soc < 25) {
            imageViewBattery.setBackgroundResource(R.drawable.battery0);
            imageViewBatteryValue.setBackgroundResource(R.drawable.battery1);
        } else if (soc < 50) {
            imageViewBattery.setBackgroundResource(R.drawable.battery1);
            imageViewBatteryValue.setBackgroundResource(R.drawable.battery2);
        } else if (soc < 75) {
            imageViewBattery.setBackgroundResource(R.drawable.battery2);
            imageViewBatteryValue.setBackgroundResource(R.drawable.battery3);
        } else if (soc <= MAX_SOC) {
            imageViewBattery.setBackgroundResource(R.drawable.battery3);
            imageViewBatteryValue.setBackgroundResource(R.drawable.battery4);
        }
    }

    private void startBlink() {
        if (running) return;
        running = true;
        handler.post(blinkRunnable);
    }

    private void stopBlink() {
        running = false;
        handler.removeCallbacks(blinkRunnable);
        imageViewBatteryValue.setVisibility(View.VISIBLE); // 마지막엔 보이게 유지
    }

    @Override
    public void onResume() {
        super.onResume();
        startBlink();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBlink();
    }

    // SOC 값 변경 메서드
    public void setSoc(int socValue) {
        this.soc = Math.min(socValue, MAX_SOC);
        updateBatteryUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);
            uiUpdateHandler.removeCallbacksAndMessages(null);
            uiUpdateHandler.removeMessages(0);
            if (uiUpdateHandler != null) uiUpdateHandler = null;
        } catch (Exception e) {
            Log.e("ChargingFragment", "onDetach error", e);
            logger.error("ChargingFragment onDetach error : {}", e.getMessage());
        }
    }
}