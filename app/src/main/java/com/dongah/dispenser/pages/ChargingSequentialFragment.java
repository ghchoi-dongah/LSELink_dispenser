package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.UiSeq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingSequentialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingSequentialFragment extends Fragment implements View.OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(ChargingSequentialFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    View viewCircle;
    ImageView imageViewBus;
    TextView textViewConnector;
    ChargingCurrentData chargingCurrentData;

    public ChargingSequentialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingSequentialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingSequentialFragment newInstance(String param1, String param2) {
        ChargingSequentialFragment fragment = new ChargingSequentialFragment();
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
        View view = inflater.inflate(R.layout.fragment_charging_sequential, container, false);
        view.setOnClickListener(this);
        textViewConnector = view.findViewById(R.id.textViewConnector);
        imageViewBus = view.findViewById(R.id.imageViewBus);
        viewCircle = view.findViewById(R.id.viewCircle);
        viewCircle.setOnClickListener(this);

        try {
            // ch0, ch1 구분 => 이미지 위치 조절
            if (mChannel == 0) {
                imageViewBus.setScaleX(1f);
                textViewConnector.setText("1" + R.string.connectorSeq);
            } else {
                imageViewBus.setScaleX(-1f);
                textViewConnector.setText("2" + R.string.connectorSeq);
            }
        } catch (Exception e) {
            logger.error("ChargingSequentialFragment onCreateView error : {}", e.getMessage());
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
        } catch (Exception e) {
            logger.error("ChargingSequentialFragment onViewCreated error : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (!Objects.equals(v.getId(), R.id.viewCircle)) return;

        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.MEMBER_CHECK_WAIT);
        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.MEMBER_CHECK_WAIT, "MEMBER_CHECK_WAIT", null);
    }
}