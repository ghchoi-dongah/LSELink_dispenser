package com.dongah.dispenser.pages;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.UiSeq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingFinishWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingFinishWaitFragment extends Fragment implements View.OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(ChargingFinishWaitFragment.class);

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
    final String[] colors = { "#FFD6BA", "#FABD8C", "#F5A55D", "#EF8C2F", "#EA7300" };
    final int[] dotIds = { R.id.dot1, R.id.dot2, R.id.dot3, R.id.dot4, R.id.dot5 };
    int currentStep = -1;
    boolean running = false;

    private static final int STEP_DELAY_MS  = 600;  // 점 하나씩 표시 간격
    private static final int CYCLE_PAUSE_MS = 1000;  // 한 사이클 끝난 뒤 쉬는 시간

    int cnt;
    Handler countHandler;
    Runnable countRunnable;

    public ChargingFinishWaitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingFinishWaitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingFinishWaitFragment newInstance(String param1, String param2) {
        ChargingFinishWaitFragment fragment = new ChargingFinishWaitFragment();
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
        View view = inflater.inflate(R.layout.fragment_charging_finish_wait, container, false);
        view.setOnClickListener(this);

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            cnt = 0;

//            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    countHandler = new Handler();
//                    countRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            cnt++;
//                            if (Objects.equals(cnt, 10)) {
//                                countHandler.removeCallbacks(countRunnable);
//                                countHandler.removeCallbacksAndMessages(null);
//                                countHandler.removeMessages(0);
//
////                                stopDotLoop();  // animation stop
//                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.FINISH_WAIT);
//                            } else {
//                                countHandler.postDelayed(countRunnable, 1000);
//                            }
//                        }
//                    };
//                    countHandler.postDelayed(countRunnable, 1000);
//                }
//            });
        } catch (Exception e) {
            logger.error("ChargingFinishWaitFragment onViewCreated error : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {

    }

//    private final Runnable loop = new Runnable() {
//        @Override public void run() {
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