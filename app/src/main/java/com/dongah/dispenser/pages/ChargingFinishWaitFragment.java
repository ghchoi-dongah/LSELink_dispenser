package com.dongah.dispenser.pages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.wang.avi.AVLoadingIndicatorView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingFinishWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingFinishWaitFragment extends Fragment {
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

    private static final int TIME_OUT = 10;
    int cnt;
    AVLoadingIndicatorView aviCheck;
    Handler countHandler;
    Runnable countRunnable;
    ChargingCurrentData chargingCurrentData;

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
        chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
        aviCheck = view.findViewById(R.id.avi);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            cnt = 0;
            startAviAnim();

            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countHandler = new Handler();
                    countRunnable = new Runnable() {
                        @Override
                        public void run() {
                            cnt++;
                            if (Objects.equals(cnt, TIME_OUT)) {
                                countHandler.removeCallbacks(countRunnable);
                                countHandler.removeCallbacksAndMessages(null);
                                countHandler.removeMessages(0);
                                chargingCurrentData.setChgFinishWait(true);
                            } else {
                                countHandler.postDelayed(countRunnable, 1000);
                            }
                        }
                    };
                    countHandler.postDelayed(countRunnable, 1000);
                }
            });
        } catch (Exception e) {
            Log.e("ChargingFinishWaitFragment", "onViewCreated error", e);
            logger.error("ChargingFinishWaitFragment onViewCreated error : {}", e.getMessage());
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
            Log.e("ChargingFinishWaitFragment", "onDetach error", e);
            logger.error("ChargingFinishWaitFragment onDetach error : {}", e.getMessage());
        }
    }
}