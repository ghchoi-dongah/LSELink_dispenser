package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargerPointType;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    View viewCircle;
    TextView textViewInitMessage, textViewConnector;
    ImageView imageViewBus;

    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];

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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        view.setOnClickListener(this);

        chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
        textViewInitMessage = view.findViewById(R.id.textViewInitMessage);
        textViewConnector = view.findViewById(R.id.textViewConnector);
        imageViewBus = view.findViewById(R.id.imageViewBus);
        viewCircle = view.findViewById(R.id.viewCircle);
        viewCircle.setOnClickListener(this);

        try {
            // ch0, ch1 구분 => 이미지 위치 조절
            if (mChannel == 0) {
                imageViewBus.setScaleX(1f);
                textViewConnector.setText("1 커넥터");
            } else {
                imageViewBus.setScaleX(-1f);
                textViewConnector.setText("2 커넥터");
            }
        } catch (Exception e) {
            Log.e("InitFragment", "onCreateView error", e);
            logger.error("InitFragment onCreateView error : {}", e.getMessage());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            requestStrings[0] = String.valueOf(0);
            sharedModel.setMutableLiveData(requestStrings);
        } catch (Exception e) {
            Log.e("InitFragment", "onViewCreated error", e);
            logger.error("InitFragment onViewCreated : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            chargingCurrentData.onCurrentDataClear();   // clear
            chargingCurrentData.setConnectorId(mChannel + 1);

            if (!Objects.equals(v.getId(), R.id.viewCircle)) return;

            ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel).setChargerPointType(ChargerPointType.COMBO);
            ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel).setConnectorId(mChannel + 1);


            if (Objects.equals(chargerConfiguration.getOpMode(), 0)) {
                // test mode
                Log.d("InitFragment", "getOpMode(): test mode");
                double testPrice = Double.parseDouble(((MainActivity) MainActivity.mContext).getChargerConfiguration().getTestPrice());
                ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel).setPowerUnitPrice(testPrice);
                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
            } else if (Objects.equals(chargerConfiguration.getOpMode(), 1)) {
                // server mode
                Log.d("InitFragment", "getOpMode(): server mode");
                if (!onUnitPrice()) {
                    Toast.makeText(getActivity(), "단가 정보가 없습니다.\n잠시 후, 충전하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    SocketState socketState = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().getSocket().getState();
                    if (Objects.equals(socketState, SocketState.OPEN)) {
                        switch (chargerConfiguration.getAuthMode()) {
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
                    } else {
                        ((MainActivity) MainActivity.mContext).getToastPositionMake().onShowToast(mChannel, "서버 연결 DISCONNECT.\n충전을 할 수 없습니다.");
                    }
                } catch (Exception e) {
                    ((MainActivity) MainActivity.mContext).getToastPositionMake().onShowToast(mChannel, "서버 연결 DISCONNECT.\n충전을 할 수 없습니다.");
                    Log.e("InitFragment", "server disconnect error", e);
                    logger.error("InitFragment server disconnect error : {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e("InitFragment", "onClick error", e);
            logger.error("InitFragment onClick error : {}", e.getMessage());
        }
    }

    private boolean onUnitPrice() {
        boolean result = false;
        try {
            File file = new File(GlobalVariables.getRootPath() + File.separator + GlobalVariables.UNIT_FILE_NAME);
            result = file.exists() || !Objects.equals(chargerConfiguration.getOpMode(), 1);
        } catch (Exception e) {
            logger.error("InitFragment onUnitPrice error : {}" ,e.getMessage());
        }
        return result;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);
        } catch (Exception e) {
            Log.e("InitFragment", "onDetach error", e);
            logger.error("InitFragment onDetach error : {}", e.getMessage());
        }
    }
}