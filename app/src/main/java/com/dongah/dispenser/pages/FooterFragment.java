package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FooterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FooterFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(FooterFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    private final Handler handler = new Handler(Looper.getMainLooper());

    int clickedCnt = 0;
    ImageButton btnLogo;
    ImageView imageViewNetwork;
    TextView textViewTime, textViewChargerIdValue, textViewVersionValue;

    Runnable runnable;

    // 1초마다 실행되는 Runnable
    private final Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            updateTime();
            handler.postDelayed(this, 1000); // 1초마다 반복
        }
    };

    public FooterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FooterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FooterFragment newInstance(String param1, String param2) {
        FooterFragment fragment = new FooterFragment();
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
        View view = inflater.inflate(R.layout.fragment_footer, container, false);
        imageViewNetwork = view.findViewById(R.id.imageViewNetwork);
        textViewVersionValue = view.findViewById(R.id.textViewVersionValue);
        textViewVersionValue.setText(" | VER: " + GlobalVariables.VERSION);
        textViewChargerIdValue = view.findViewById(R.id.textViewChargerIdValue);
        textViewTime = view.findViewById(R.id.textViewTime);
        btnLogo = view.findViewById(R.id.btnLogo);
        btnLogo.setOnClickListener(v -> {
            System.out.println("btnLogo click: " + clickedCnt);

//            logger.info("FooterFragment btnLogo click clickedCnt: {}", clickedCnt);
            if (clickedCnt > 8) {
                try {
//                    boolean chkUiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(0).getUiSeq() == UiSeq.INIT &&
//                            ((MainActivity) MainActivity.mContext).getClassUiProcess(1).getUiSeq() == UiSeq.INIT;

                    MainActivity activity = (MainActivity) MainActivity.mContext;
                    if (activity == null) {
                        logger.error("btnLogo error: MainActivity.mContext is null");
                        return;
                    }

                    UiSeq ui0 = activity.getClassUiProcess(0) != null
                            ? activity.getClassUiProcess(0).getUiSeq()
                            : null;
                    UiSeq ui1 = activity.getClassUiProcess(1) != null
                            ? activity.getClassUiProcess(1).getUiSeq()
                            : null;

                    boolean chkUiSeq = ui0 == UiSeq.INIT && ui1 == UiSeq.INIT;

//                    logger.info("btnLogo clickedCnt: {}, ui0: {}, ui1: {}, chkUiSeq: {}",
//                            clickedCnt, ui0, ui1, chkUiSeq);

                    System.out.println("clickedCnt > 8, ui0: " + ui0 + ", ui1: " + ui1 + ", chkUiSeq: " + chkUiSeq + ", mChannel:" + mChannel);


                    if (chkUiSeq) {
                        ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.ADMIN_PASS);
                                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.ADMIN_PASS,"ADMIN_PASS",null);
                            }
                        });
                    }
                    clickedCnt = 0;
                } catch (Exception e) {
                    logger.error("btnLogo error: {}", e.getMessage());
                }
            }
            clickedCnt++;
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(timeUpdater); // 타이머 시작
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(timeUpdater); // 타이머 중단 (메모리 누수 방지)
    }

    private void updateTime() {
        try {
            if (textViewTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String currentTime = sdf.format(new Date());
                textViewTime.setText(currentTime);
            }
        } catch (Exception e) {
            logger.error("updateTime error: {}", e.getMessage());
        }
    }
}