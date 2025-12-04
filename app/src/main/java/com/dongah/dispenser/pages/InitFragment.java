package com.dongah.dispenser.pages;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.UiSeq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    TextView textViewInitMessage;
    ImageView imageViewConnector, imageViewConnectorBg;
    ObjectAnimator fadeAnimator;

    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        view.setOnClickListener(this);

        chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
        textViewInitMessage = view.findViewById(R.id.textViewInitMessage);
        imageViewConnector = view.findViewById(R.id.imageViewConnector);
        imageViewConnectorBg = view.findViewById(R.id.imageViewConnectorBg);

        try {
            // ch0, ch1 구분 => 이미지 위치 조절
            if (mChannel == 0) {
                imageViewConnector.setScaleX(-1f);
                imageViewConnectorBg.setScaleX(-1f);
            } else {
                imageViewConnector.setScaleX(1f);
                imageViewConnectorBg.setScaleX(1f);
            }

            // imageViewConnectorBg animation
            fadeAnimator = ObjectAnimator.ofFloat(imageViewConnectorBg, "alpha", 1f, 0.2f);
            fadeAnimator.setDuration(1000);
            fadeAnimator.setRepeatCount(ValueAnimator.INFINITE);
            fadeAnimator.setRepeatMode(ValueAnimator.REVERSE);
            fadeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            fadeAnimator.start();
        } catch (Exception e) {
            Log.e("InitFragment", "onCreateView error", e);
            logger.error("InitFragment onCreateView error : {}", e.getMessage());
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        try {
            // chargingCurrentData.onCurrentDataClear();   // clear

            if (!isAdded()) return;

            switch (Integer.parseInt(chargerConfiguration.getAuthMode())) {
                case 0:
                case 2:
                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.MEMBER_CHECK_WAIT);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.MEMBER_CHECK_WAIT, "MEMBER_CHECK_WAIT", null);
                    break;
                case 1:
                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.MEMBER_CARD);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
                    break;
                default:
                    logger.error("InitFragment onClick error >> Invalid value");
                    break;
            }

        } catch (Exception e) {
            Log.e("InitFragment", "onClick error", e);
            logger.error("InitFragment onClick error : {}", e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (fadeAnimator != null) {
                fadeAnimator.cancel();  // animation stop
                fadeAnimator.removeAllListeners();  // listener remove
                fadeAnimator = null;
            }

            if (imageViewConnectorBg != null) {
                imageViewConnectorBg.clearAnimation();
            }
        } catch (Exception e) {
            Log.e("InitFragment", "onDetach error", e);
            logger.error("InitFragment onDetach error : {}", e.getMessage());
        }
    }
}