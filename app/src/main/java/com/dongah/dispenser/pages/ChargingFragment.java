package com.dongah.dispenser.pages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.UiSeq;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingFragment extends Fragment {

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


    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean blinkVisible = true;
    private boolean running = false;

    int soc = 75;
    private static final int MAX_SOC = 90;
    private static final int BLINK_INTERVAL = 500;  // 깜빡임 간격(ms)

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
        imageViewBattery = view.findViewById(R.id.imageViewBattery);
        imageViewBatteryValue = view.findViewById(R.id.imageViewBatteryValue);

        updateBatteryUI();   // soc에 따른 이미지 갱신
        startBlink();        // 깜빡임 시작

        btnChargingStop.setOnClickListener(v -> {
            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.FINISH_WAIT, "FINISH_WAIT", null);
        });

        return view;
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
}