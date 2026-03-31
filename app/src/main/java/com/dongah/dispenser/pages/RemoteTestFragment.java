package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.core.AvailabilityType;
import com.dongah.dispenser.websocket.ocpp.core.Reason;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RemoteTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RemoteTestFragment extends Fragment implements View.OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(RemoteTestFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    Button btnResetHard, btnResetSoft, btnInoperative, btnOperative, btnInoperativeAll, btnOperativeAll;
    Button btnExit;

    MainActivity activity;
    ChargingCurrentData chargingCurrentData;
    UiSeq uiSeq;

    public RemoteTestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RemoteTestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RemoteTestFragment newInstance(String param1, String param2) {
        RemoteTestFragment fragment = new RemoteTestFragment();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_test, container, false);
        activity = (MainActivity) MainActivity.mContext;
        chargingCurrentData = activity.getChargingCurrentData(mChannel);

        btnExit = view.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(this);
        btnResetHard = view.findViewById(R.id.btnResetHard);
        btnResetHard.setOnClickListener(this);
        btnResetSoft = view.findViewById(R.id.btnResetSoft);
        btnResetSoft.setOnClickListener(this);
        btnInoperative = view.findViewById(R.id.btnInoperative);
        btnInoperative.setOnClickListener(this);
        btnOperative = view.findViewById(R.id.btnOperative);
        btnOperative.setOnClickListener(this);
        btnInoperativeAll = view.findViewById(R.id.btnInoperativeAll);
        btnInoperativeAll.setOnClickListener(this);
        btnOperativeAll = view.findViewById(R.id.btnOperativeAll);
        btnOperativeAll.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        if (Objects.equals(getId, R.id.btnExit)) {
            FragmentTransaction transaction = ((MainActivity) MainActivity.mContext).getSupportFragmentManager().beginTransaction();
            EnvironmentFragment environmentFragment = new EnvironmentFragment();
            transaction.replace(R.id.frameFull, environmentFragment);
            transaction.commit();
        } else if (Objects.equals(getId, R.id.btnResetHard) || Objects.equals(getId, R.id.btnResetSoft)) {
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                activity.getChargingCurrentData(i).setStopReason(Objects.equals(getId, R.id.btnResetHard) ? Reason.HardReset : Reason.SoftReset);
                activity.getChargingCurrentData(i).setReBoot(true);
            }
        } else if (Objects.equals(getId, R.id.btnInoperative) || Objects.equals(getId, R.id.btnOperative)) {
            boolean checkType = !Objects.equals(getId, R.id.btnInoperative);
            GlobalVariables.ChargerOperation[1] = checkType;    // ch0
            onChargerOperateSave();
        } else if (Objects.equals(getId, R.id.btnInoperativeAll) || Objects.equals(getId, R.id.btnOperativeAll)) {
            boolean checkType = !Objects.equals(getId, R.id.btnInoperativeAll);
            Arrays.fill(GlobalVariables.ChargerOperation, checkType);
            onChargerOperateSave();
        }
    }

    // save ChargerOperate
    private void onChargerOperateSave() {
        try {
            boolean chk;
            FileManagement fileManagement = new FileManagement();
            String rootPath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
            String fileName = "ChargerOperate";
            File file = new File(rootPath + File.separator + fileName);
            if (file.exists()) chk = file.delete();

            for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                String statusContent = String.valueOf(GlobalVariables.ChargerOperation[i]);
                fileManagement.stringToFileSave(rootPath, fileName, statusContent, true);
            }
        } catch (Exception e) {
            logger.error("onChargerOperateSave {}", e.getMessage());
        }
    }
}