package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.utils.SharedModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberCardNoMacFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberCardNoMacFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(MemberCardNoMacFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    int timer = 10;
    TextView textViewTagTimer;
    ImageView imageViewMemberCard;
    AnimationDrawable animationDrawable;
    Handler countHandler;
    Runnable countRunnable;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];
    MediaPlayer mediaPlayer;

    public MemberCardNoMacFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberCardNoMacFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberCardNoMacFragment newInstance(String param1, String param2) {
        MemberCardNoMacFragment fragment = new MemberCardNoMacFragment();
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
        View view = inflater.inflate(R.layout.fragment_member_card_no_mac, container, false);
        textViewTagTimer = view.findViewById(R.id.textViewTagTimer);
        imageViewMemberCard = view.findViewById(R.id.imageViewMemberCard);
        imageViewMemberCard.setBackgroundResource(R.drawable.membercardtagging);
        animationDrawable = (AnimationDrawable) imageViewMemberCard.getBackground();
        sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
        requestStrings[0] = String.valueOf(mChannel);

        ((MainActivity) MainActivity.mContext).getRfCardReaderReceive().rfCardReadRequest();
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mediaPlayer();   // media player

            animationDrawable.start();
            textViewTagTimer.setText(timer + "초");

            countHandler = new Handler();
            countRunnable = new Runnable() {
                @Override
                public void run() {
                    timer--;
                    if (Objects.equals(timer, 0)) {
                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
                    } else {
                        countHandler.postDelayed(countRunnable, 1000);
                        textViewTagTimer.setText(timer + "초");
                    }
                }
            };
            countHandler.postDelayed(countRunnable, 1000);
        } catch (Exception e) {
            logger.error("MemberCardNoMacFragment error: {}", e.getMessage());
        }
    }

    private void mediaPlayer() {
        releasePlayer();

        try {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.membercard);
            mediaPlayer.setOnCompletionListener(me -> releasePlayer());
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("MemberCardNoMacFragment", "mediaPlayer error", e);
            logger.error("MemberCardNoMacFragment mediaPlayer error : {}", e.getMessage());
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e("MemberCardNoMacFragment", "releasePlayer error", e);
                logger.error("MemberCardNoMacFragment releasePlayer error : {}", e.getMessage());
            }
            mediaPlayer = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            animationDrawable.stop();
            ((AnimationDrawable) imageViewMemberCard.getBackground()).stop();
            imageViewMemberCard.setBackground(null);
            countHandler.removeCallbacks(countRunnable);
            countHandler.removeCallbacksAndMessages(null);
            countHandler.removeMessages(0);
        } catch (Exception e) {
            logger.error("MemberCardNoMacFragment onDetach error : {}", e.getMessage());
        }
    }
}