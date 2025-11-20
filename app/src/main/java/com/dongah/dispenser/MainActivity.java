package com.dongah.dispenser;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.ConfigurationKeyRead;
import com.dongah.dispenser.sqlite.SQLiteHelper;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.FragmentCurrent;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.sqlite.dto.CpSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainActivity extends AppCompatActivity {

    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    // screen saver
    private static final long INACTIVITY_TIMEOUT = 1 * 60 * 1000L;  // 2분 (ms 단위)
    private final Handler inactivityHandler = new Handler(Looper.getMainLooper());
    private final Runnable inactivityRunnable = new Runnable() {
        @Override
        public void run() {
            int channel = 0;
            getFragmentChange().onFragmentChange(channel, UiSeq.SCREEN_SAVER, "SCREEN_SAVER", null);
        }
    };


    Handler handler = new Handler();
    Runnable runnable;

    SQLiteHelper sqLiteHelper;
    SQLiteDatabase sqLiteDatabase;


    UiSeq[] fragmentSeq;
    ClassUiProcess[] classUiProcess;
    ConfigurationKeyRead configurationKeyRead;
    ChargerConfiguration chargerConfiguration;
    FragmentChange fragmentChange;
    FragmentCurrent fragmentCurrent;



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

        // 앱 켜질 때 타이머 시작
        resetInactivityTimer();

        mContext = this;

        /* 슬립 모드 방지*/
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /* 세로 고정 */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        // SQLite DB
        sqLiteHelper = new SQLiteHelper(this);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        sqLiteHelper.deleteAllTables(sqLiteDatabase);   // delete all tables
        sqLiteHelper.onCreate(sqLiteDatabase);          // create all tables
        testCrud(); // test data insert

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
                // 1초마다 실행
                handler.postDelayed(this, 1000);

                // TODO: network connection check
            }
        };
        runnable.run();
    }

    // 타이머 리셋 메서드 (외부에서 호출 가능)
    public void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable);
        inactivityHandler.postDelayed(inactivityRunnable, INACTIVITY_TIMEOUT);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetInactivityTimer();  // 입력 있을 때마다 타이머 리셋
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inactivityHandler.removeCallbacks(inactivityRunnable);
    }

    private void testCrud() {
        SQLiteHelper helper = SQLiteHelper.getInstance(this);

        // DTO 생성
        CpSettings settings = new CpSettings();
        settings.stationId = "ST01";
        settings.chargerId = "CH01";
        settings.modelNm = "MD01";
        settings.vendorNm = "DONGAH";
        settings.fwVersion = "1.0.0";
        settings.socLimit = "80";
        settings.availability = "Operative";

        // 공통 insert() 실행
        long rowId = helper.insert(settings);

        System.out.println("INSERT RESULT1 = " + rowId);

        settings.stationId = "ST02";
        settings.chargerId = "CH02";
        settings.modelNm = "MD02";
        settings.vendorNm = "DONGAH";
        settings.fwVersion = "1.0.1";
        settings.socLimit = "80";
        settings.availability = "Operative";

        // 공통 insert() 실행
        long rowId2 = helper.insert(settings);

        System.out.println("INSERT RESULT2 = " + rowId2);
    }
}