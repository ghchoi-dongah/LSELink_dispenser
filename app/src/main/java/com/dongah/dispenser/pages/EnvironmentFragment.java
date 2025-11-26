package com.dongah.dispenser.pages;

import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.UiSeq;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EnvironmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnvironmentFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    Button btnConfig, btnWebSocket, btnControl, btnUi, btnExit;

    public EnvironmentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EnvironmentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnvironmentFragment newInstance(String param1, String param2) {
        EnvironmentFragment fragment = new EnvironmentFragment();
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
        View view = inflater.inflate(R.layout.fragment_environment, container, false);
        btnConfig = view.findViewById(R.id.btnConfig);
        btnWebSocket = view.findViewById(R.id.btnWebSocket);
        btnControl = view.findViewById(R.id.btnControl);
        btnUi = view.findViewById(R.id.btnUi);
        btnExit = view.findViewById(R.id.btnExit);
        btnConfig.setOnClickListener(this);
        btnWebSocket.setOnClickListener(this);
        btnControl.setOnClickListener(this);
        btnUi.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        return  view;
    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        if (Objects.equals(getId, R.id.btnConfig)) {
            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.CONFIG_SETTING, "CONFIG_SETTING", null);
        } else if (Objects.equals(getId, R.id.btnWebSocket)) {
            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.WEB_SOCKET, "WEB_SOCKET", null);
        } else if (Objects.equals(getId, R.id.btnControl)) {
            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CONTROL_BOARD_DEBUGGING, "CONTROL_BOARD_DEBUGGING", null);
        } else if (Objects.equals(getId, R.id.btnUi)) {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).getUiSeq();
            switch (uiSeq) {
                case CHARGING:
                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.CHARGING);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING, "CHARGING", null);
                    break;
                case FAULT:
                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.FAULT);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.FAULT, "FAULT", null);
                    break;
                default:
                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.INIT);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.INIT, "INIT", null);
                    break;
            }
        } else if (Objects.equals(getId, R.id.btnExit)) {
            ActivityCompat.finishAffinity((MainActivity) MainActivity.mContext);
            System.exit(0);
        }
    }
}