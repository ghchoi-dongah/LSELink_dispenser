package com.dongah.dispenser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.ConfigurationKeyRead;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.FragmentCurrent;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    TextView textViewTime, textViewVersionValue, textViewChargerIdValue;
    ImageView imageViewNetwork;
    ImageButton btnLogo;
    int clickedCnt = 0;


//    int mChannel;

    Handler handler = new Handler();
    Runnable runnable;


    UiSeq[] fragmentSeq;
    ClassUiProcess[] classUiProcess;
    ConfigurationKeyRead configurationKeyRead;
    ChargerConfiguration chargerConfiguration;
    FragmentChange fragmentChange;
    FragmentCurrent fragmentCurrent;



//    public void setChannel(int channel) {
//        this.mChannel = channel;
//    }
//
//    public int getChannel() {
//        return mChannel;
//    }

    public UiSeq getFragmentSeq(int ch)  {
        return fragmentSeq[ch];
    }

    public void setFragmentSeq(int ch, UiSeq fragmentSeq) {
        this.fragmentSeq[ch] = fragmentSeq;
    }

    public ClassUiProcess[] getClassUiProcess() {
        return classUiProcess;
    }

    public ClassUiProcess getClassUiProcess(int ch) {
        return classUiProcess[ch];
    }


    public ConfigurationKeyRead getConfigurationKeyRead() {
        return configurationKeyRead;
    }

    public ChargerConfiguration getChargerConfiguration() {
        return chargerConfiguration;
    }

    public FragmentChange getFragmentChange() {
        return fragmentChange;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hideNavigationBar();

        mContext = this;

        /* 슬립 모드 방지*/
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /* 세로 고정 */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

//        imageViewNetwork = findViewById(R.id.imageViewNetwork);
//        textViewVersionValue = findViewById(R.id.textViewVersionValue);
//        textViewVersionValue.setText(" | VER: " + GlobalVariables.VERSION);
//        textViewTime = findViewById(R.id.textViewTime);
//        textViewChargerIdValue = findViewById(R.id.textViewChargerIdValue);
//        btnLogo = findViewById(R.id.btnLogo);
//        btnLogo.setOnClickListener(v -> {
//            if (clickedCnt > 8) {
//                try {
//                    boolean chkUiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(0).getUiSeq() == UiSeq.INIT &&
//                            ((MainActivity) MainActivity.mContext).getClassUiProcess(1).getUiSeq() == UiSeq.INIT;
//                    if (chkUiSeq) {
//                        ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.ADMIN_PASS);
//                                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.ADMIN_PASS,"ADMIN_PASS",null);
//                            }
//                        });
//                    }
//                } catch (Exception e) {
//                    logger.error("btnLogo error: {}", e.getMessage());
//                }
//            }
//            clickedCnt++;
//        });

        // fragment current
        fragmentCurrent = new FragmentCurrent();

        // ConfigurationKey read
        configurationKeyRead = new ConfigurationKeyRead();
        configurationKeyRead.onRead();

        // 1. charger configuration, ConfigurationKey read
        chargerConfiguration = new ChargerConfiguration();
        chargerConfiguration.onLoadConfiguration();

        // 2. fragment change management
        fragmentChange = new FragmentChange();
        fragmentSeq = new UiSeq[GlobalVariables.maxChannel];
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            fragmentChange.onFragmentChange(i, UiSeq.INIT, "INIT", "");
            fragmentChange.onFragmentFooterChange(i, "Footer");
        }

        // 3. control board
        // 4. rf card reader : MID = terminal ID
        // 5. web socket

        // modem data

        // server mode
        // 6. classUrProcess
        classUiProcess = new ClassUiProcess[GlobalVariables.maxChannel];
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            classUiProcess[i] = new ClassUiProcess(i);
            classUiProcess[i].setUiSeq(UiSeq.INIT);
        }

        // 7. PLC modem
        // 8. ChargerOperate read
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                // 1초마다 실행
                handler.postDelayed(this, 1000);

                // TODO: network connection check
            }
        };
        runnable.run();
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