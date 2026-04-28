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
import java.util.UUID;

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
    Button btnTestChangeModeDM, btnTestChangeModeIM, btnTestChangeElecMode, btnRechgrsocschedule;
    Button btnExit;

    MainActivity activity;
    ChargingCurrentData chargingCurrentData;


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
        btnTestChangeModeDM = view.findViewById(R.id.btnTestChangeModeDM);
        btnTestChangeModeDM.setOnClickListener(this);
        btnTestChangeModeIM = view.findViewById(R.id.btnTestChangeModeIM);
        btnTestChangeModeIM.setOnClickListener(this);
        btnTestChangeElecMode = view.findViewById(R.id.btnTestChangeElecMode);
        btnTestChangeElecMode.setOnClickListener(this);
        btnRechgrsocschedule = view.findViewById(R.id.btnRechgrsocschedule);
        btnRechgrsocschedule.setOnClickListener(this);
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
            String type = Objects.equals(getId, R.id.btnInoperative) ? "Inoperative" : "Operative";
            onTestChangeAvailability(1, type);
        } else if (Objects.equals(getId, R.id.btnInoperativeAll) || Objects.equals(getId, R.id.btnOperativeAll)) {
            String type = Objects.equals(getId, R.id.btnInoperativeAll) ? "Inoperative" : "Operative";
            onTestChangeAvailability(0, type);
        } else if (Objects.equals(getId, R.id.btnTestChangeModeDM) || Objects.equals(getId, R.id.btnTestChangeModeIM)) {
            String type = Objects.equals(getId, R.id.btnTestChangeModeDM) ? "DM" : "IM";
            onTestChangeMode(type);
        } else if (Objects.equals(getId, R.id.btnTestChangeElecMode)) {
            onTestChangeElecMode();
        } else if (Objects.equals(getId, R.id.btnRechgrsocschedule)) {
            onTestRechgrsocschedule();
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

    /**
     * ChangeAvailability 전문 시뮬레이션 테스트
     * CSMS에서 수신한 것과 동일한 전문을 onGetMessage에 직접 주입
     * @param type "Inoperative" 또는 "Operative"
     */
    private void onTestChangeAvailability(int connectorId, String type) {
        try {
            int loop = connectorId == 0 ? 2 : connectorId;

            for (int i = 0; i < loop; i++) {
                String uuid = UUID.randomUUID().toString();
                String testMessage = "[2,\"" + uuid + "\",\"ChangeAvailability\","
                        + "{\"connectorId\":" + i + ",\"type\":\"" + type + "\"}]";

                logger.info("[TEST] ChangeAvailability[{}]({}) 전문 주입: {}", i, type, testMessage);
                activity.getSocketReceiveMessage().onGetMessage(null, testMessage);
                logger.info("[TEST] ChangeAvailability[{}]({}) 처리 완료", i, type);
            }
        } catch (Exception e) {
            logger.error("[TEST] ChangeAvailability({}) 테스트 실패: {}", type, e.getMessage(), e);
        }
    }

    /**
     * DataTransfer changemode.req 전문 시뮬레이션 테스트
     * CSMS에서 수신한 것과 동일한 전문을 onGetMessage에 직접 주입
     */
    private void onTestChangeMode(String type) {
        try {
            String uuid = UUID.randomUUID().toString();
            String dataJson = "{\"connectorId\":0,\"rechgAmt\":95,\"rechgElec\":0,"
                    + "\"HH00\":\"" + type + "\",\"HH01\":\"" + type + "\",\"HH02\":\"" + type + "\",\"HH03\":\"" + type + "\","
                    + "\"HH04\":\"" + type + "\",\"HH05\":\"" + type + "\",\"HH06\":\"" + type + "\",\"HH07\":\"" + type + "\","
                    + "\"HH08\":\"" + type + "\",\"HH09\":\"" + type + "\",\"HH10\":\"" + type + "\",\"HH11\":\"" + type + "\","
                    + "\"HH12\":\"" + type + "\",\"HH13\":\"" + type + "\",\"HH14\":\"" + type + "\",\"HH15\":\"" + type + "\","
                    + "\"HH16\":\"" + type + "\",\"HH17\":\"" + type + "\",\"HH18\":\"" + type + "\",\"HH19\":\"" + type + "\","
                    + "\"HH20\":\"" + type + "\",\"HH21\":\"" + type + "\",\"HH22\":\"" + type + "\",\"HH23\":\"" + type + "\"}";

            String testMessage = "[2,\"" + uuid + "\",\"DataTransfer\","
                    + "{\"vendorId\":\"DONGAH\",\"messageId\":\"changemode.req\","
                    + "\"data\":\"" + dataJson.replace("\"", "\\\"") + "\"}]";

            logger.info("[TEST] changemode.req 전문 주입: {}", testMessage);
            activity.getSocketReceiveMessage().onGetMessage(null, testMessage);
            logger.info("[TEST] changemode.req 처리 완료");
        } catch (Exception e) {
            logger.error("[TEST] changemode.req 테스트 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * DataTransfer changeelecmode.req 전문 시뮬레이션 테스트
     * CSMS에서 수신한 것과 동일한 전문을 onGetMessage에 직접 주입
     * connectorId=1, HH00~HH23 모두 "40"
     */
    private void onTestChangeElecMode() {
        try {
            String uuid = UUID.randomUUID().toString();
            String dataJson = "{\"connectorId\":1"
                    + ",\"HH00\":\"40\",\"HH01\":\"40\",\"HH02\":\"40\",\"HH03\":\"40\""
                    + ",\"HH04\":\"40\",\"HH05\":\"40\",\"HH06\":\"40\",\"HH07\":\"40\""
                    + ",\"HH08\":\"40\",\"HH09\":\"40\",\"HH10\":\"40\",\"HH11\":\"40\""
                    + ",\"HH12\":\"40\",\"HH13\":\"40\",\"HH14\":\"40\",\"HH15\":\"40\""
                    + ",\"HH16\":\"40\",\"HH17\":\"40\",\"HH18\":\"40\",\"HH19\":\"40\""
                    + ",\"HH20\":\"40\",\"HH21\":\"40\",\"HH22\":\"40\",\"HH23\":\"40\"}";

            String testMessage = "[2,\"" + uuid + "\",\"DataTransfer\","
                    + "{\"vendorId\":\"DONGAH\",\"messageId\":\"changeelecmode.req\","
                    + "\"data\":\"" + dataJson.replace("\"", "\\\"") + "\"}]";

            logger.info("[TEST] changeelecmode.req 전문 주입: {}", testMessage);
            activity.getSocketReceiveMessage().onGetMessage(null, testMessage);
            logger.info("[TEST] changeelecmode.req 처리 완료");
        } catch (Exception e) {
            logger.error("[TEST] changeelecmode.req 테스트 실패: {}", e.getMessage(), e);
        }
    }

    private void onTestRechgrsocschedule() {
        try {
            String uuid = UUID.randomUUID().toString();
            String dataJson = "{\"connectorId\":1"
                    + ",\"DH00\":\"80\",\"DH01\":\"80\",\"DH02\":\"80\",\"DH03\":\"80\",\"DH04\":\"80\",\"DH05\":\"80\""
                    + ",\"DH06\":\"80\",\"DH07\":\"80\",\"DH08\":\"80\",\"DH09\":\"80\",\"DH10\":\"80\",\"DH11\":\"80\""
                    + ",\"DH12\":\"80\",\"DH13\":\"80\",\"DH14\":\"80\",\"DH15\":\"80\",\"DH16\":\"80\",\"DH17\":\"80\""
                    + ",\"DH18\":\"80\",\"DH19\":\"80\",\"DH20\":\"80\",\"DH21\":\"80\",\"DH22\":\"80\",\"DH23\":\"80\""
                    + ",\"WH00\":\"80\",\"WH01\":\"80\",\"WH02\":\"80\",\"WH03\":\"80\",\"WH04\":\"80\",\"WH05\":\"80\""
                    + ",\"WH06\":\"80\",\"WH07\":\"80\",\"WH08\":\"80\",\"WH09\":\"80\",\"WH10\":\"80\",\"WH11\":\"80\""
                    + ",\"WH12\":\"80\",\"WH13\":\"80\",\"WH14\":\"80\",\"WH15\":\"80\",\"WH16\":\"80\",\"WH17\":\"80\""
                    + ",\"WH18\":\"80\",\"WH19\":\"80\",\"WH20\":\"80\",\"WH21\":\"80\",\"WH22\":\"80\",\"WH23\":\"80\""
                    + "}";

            String testMessage = "[2,\"" + uuid + "\",\"DataTransfer\","
                    + "{\"vendorId\":\"DONGAH\","
                    + "\"messageId\":\"rechgrsocschedule.req\","
                    + "\"data\":\"" + dataJson.replace("\"", "\\\"") + "\"}]";

            logger.info("[TEST] rechgrsocschedule.req 전문 주입: {}", testMessage);
            activity.getSocketReceiveMessage().onGetMessage(null, testMessage);
            logger.info("[TEST] rechgrsocschedule.req 처리 완료");
        } catch (Exception e) {
            logger.error("[TEST] rechgrsocschedule.req 테스트 실패: {}", e.getMessage(), e);
        }
    }
}