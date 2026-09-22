package com.dongah.dispenser.pages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.controlboard.ControlBoardListener;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.controlboard.TxData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductTestIoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductTestIoFragment extends Fragment implements View.OnClickListener, ControlBoardListener {
    private static final Logger logger = LoggerFactory.getLogger(ProductTestIoFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    EditText editRy1, editRy2, editRy3, editRy4, editRy5, editRy6;
    ToggleButton btnRy1, btnRy2, btnRy3, btnRy4, btnRy5, btnRy6;

    ControlBoard controlBoard;
    RxData rxData1;
    TxData txData1;

    public ProductTestIoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProductTestIoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductTestIoFragment newInstance(String param1, String param2) {
        ProductTestIoFragment fragment = new ProductTestIoFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_test_io, container, false);

        try {
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            rxData1 = controlBoard.getRxData(0);
            txData1 = controlBoard.getTxData(0);

            editRy1 = view.findViewById(R.id.editRy1);
            editRy2 = view.findViewById(R.id.editRy2);
            editRy3 = view.findViewById(R.id.editRy3);
            editRy4 = view.findViewById(R.id.editRy4);
            editRy5 = view.findViewById(R.id.editRy5);
            editRy6 = view.findViewById(R.id.editRy6);

            // btnRy 초기화
            btnRy1  = view.findViewById(R.id.btnRy1);
            btnRy1.setOnClickListener(this);
            btnRy2  = view.findViewById(R.id.btnRy2);
            btnRy2.setOnClickListener(this);
            btnRy3  = view.findViewById(R.id.btnRy3);
            btnRy3.setOnClickListener(this);
            btnRy4  = view.findViewById(R.id.btnRy4);
            btnRy4.setOnClickListener(this);
            btnRy5  = view.findViewById(R.id.btnRy5);
            btnRy5.setOnClickListener(this);
            btnRy6  = view.findViewById(R.id.btnRy6);
            btnRy6.setOnClickListener(this);
        } catch (Exception e) {
            logger.error("onCreateView error : {}", e.getMessage(), e);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                controlBoard.getTxData(i).setChargerPointMode((short) 2);
            }

            // btnRy 초기값 설정
            btnRy1.setChecked(txData1.isRelay1());
            btnRy2.setChecked(txData1.isRelay2());
            btnRy3.setChecked(txData1.isRelay3());
            btnRy4.setChecked(txData1.isRelay4());
            btnRy5.setChecked(txData1.isRelay5());
            btnRy6.setChecked(txData1.isRelay6());

            editRy1.setText(rxData1.isCsRY1Status() ? "ON" : "OFF");
            editRy2.setText(rxData1.isCsRY2Status() ? "ON" : "OFF");
            editRy3.setText(rxData1.isCsRY3Status() ? "ON" : "OFF");
            editRy4.setText(rxData1.isCsRY4Status() ? "ON" : "OFF");
            editRy5.setText(rxData1.isCsRY5Status() ? "ON" : "OFF");
            editRy6.setText(rxData1.isCsRY6Status() ? "ON" : "OFF");
        } catch (Exception e) {
            logger.error("onViewCreated error : {}", e.getMessage(), e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (controlBoard != null) {
            controlBoard.addControlBoardListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (controlBoard != null) {
            controlBoard.removeControlBoardListener(this);
        }
    }

    @Override
    public void onControlBoardReceive(RxData[] rxData) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) return;
            editRy1.setText(rxData[0].isCsRY1Status() ? "ON" : "OFF");
            editRy2.setText(rxData[0].isCsRY2Status() ? "ON" : "OFF");
            editRy3.setText(rxData[0].isCsRY3Status() ? "ON" : "OFF");
            editRy4.setText(rxData[0].isCsRY4Status() ? "ON" : "OFF");
            editRy5.setText(rxData[0].isCsRY5Status() ? "ON" : "OFF");
            editRy6.setText(rxData[0].isCsRY6Status() ? "ON" : "OFF");
        });
    }

    @Override
    public void onControlBoardSend(TxData[] txData) {
        // not used
    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        try {
            if (Objects.equals(getId, R.id.btnRy1)) {
                txData1.setRelay1(btnRy1.isChecked());
            } else if (Objects.equals(getId, R.id.btnRy2)) {
                txData1.setRelay2(btnRy2.isChecked());
            } else if (Objects.equals(getId, R.id.btnRy3)) {
                txData1.setRelay3(btnRy3.isChecked());
            } else if (Objects.equals(getId, R.id.btnRy4)) {
                txData1.setRelay4(btnRy4.isChecked());
            } else if (Objects.equals(getId, R.id.btnRy5)) {
                txData1.setRelay5(btnRy5.isChecked());
            } else if (Objects.equals(getId, R.id.btnRy6)) {
                txData1.setRelay6(btnRy6.isChecked());
            }
        } catch (Exception e) {
            logger.error("onClick error : {}", e.getMessage(), e);
        }
    }
}