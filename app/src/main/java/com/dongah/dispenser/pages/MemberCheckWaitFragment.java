package com.dongah.dispenser.pages;

import android.media.MediaPlayer;
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

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.UiSeq;
import com.wang.avi.AVLoadingIndicatorView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberCheckWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberCheckWaitFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(MemberCheckWaitFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    AVLoadingIndicatorView aviMemberCheck;
    int cnt = 0;

    MediaPlayer mediaPlayer;
    ClassUiProcess classUiProcess;
    ChargingCurrentData chargingCurrentData;
    ChargerConfiguration chargerConfiguration;

    Handler countHandler;
    Runnable countRunnable;

    public MemberCheckWaitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberCheckWaitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberCheckWaitFragment newInstance(String param1, String param2) {
        MemberCheckWaitFragment fragment = new MemberCheckWaitFragment();
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
        View view = inflater.inflate(R.layout.fragment_member_check_wait, container, false);
        view.setOnClickListener(this);

        aviMemberCheck = view.findViewById(R.id.aviMemberCheck);
        startAviAnim();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            classUiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel);
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
//            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);

            playMemberCardWait();

//            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    countHandler = new Handler();
//                    countRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                cnt++;
//                                if (Objects.equals(cnt, 20)) {
//                                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
//                                } else {
//                                    countHandler.postDelayed(countRunnable, 1000);
//                                }
//                                // TODO: authorize result check
//                            } catch (Exception e) {
//                                Log.e("MemberCheckWaitFragment", "runOnUiThread error", e);
//                                logger.error("MemberCheckWaitFragment runOnUiThread error : {}", e.getMessage());
//                            }
//                        }
//                    };
//                    countHandler.postDelayed(countRunnable, 1000);
//                }
//            });
        } catch (Exception e) {
            Log.e("MemberCheckWaitFragment", "onViewCreated error", e);
            logger.error("MemberCheckWaitFragment onViewCreated error : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (!isAdded()) return;
            stopAviAnim();

            if (mChannel == 0) {
                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.CHARGING_WAIT);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.CHARGING_WAIT, "CHARGING_WAIT", null);
            } else {
                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.CONNECTION_FAILED);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.CONNECTION_FAILED, "CONNECTION_FAILED", null);
            }
        } catch (Exception e) {
            Log.e("MemberCheckWaitFragment", "onClick error", e);
            logger.error("MemberCheckWaitFragment onClick error : {}", e.getMessage());
        }
    }

    private void playMemberCardWait() {
        releasePlayer();

        Log.d("MP_TEST", "playMemberCardWait() called");

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.membercardwait);
        Log.d("MP_TEST", "mediaPlayer = " + mediaPlayer);

        if (mediaPlayer == null) {
            Log.e("MemberCheckWaitFragment", "playMemberCardWait >> mediaPlayer is null");
            logger.error("MediaPlayer.create() failed for MemberCardWait");
            return;
        }

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e("MemberCheckWaitFragment", "playMemberCardWait >> setOnErrorListener error");
            logger.error("MediaPlayer error: what={}, extra={}", what, extra);
            releasePlayer();
            return true;
        });

        mediaPlayer.setOnCompletionListener(mp -> releasePlayer());
        Log.d("MP_TEST", "onCompletion");

        mediaPlayer.start();
        Log.d("MP_TEST", "start() called");
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    void startAviAnim() {
        aviMemberCheck.show();
    }

    void stopAviAnim() {
        aviMemberCheck.hide();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            countHandler.removeCallbacks(countRunnable);
            countHandler.removeCallbacksAndMessages(null);
            countHandler.removeMessages(0);
        } catch (Exception e) {
            Log.e("MemberCheckWaitFragment", "onDetach error", e);
            logger.error("MemberCheckWaitFragment onDetach error : {}", e.getMessage());
        }
    }
}