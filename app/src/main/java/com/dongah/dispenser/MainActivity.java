package com.dongah.dispenser;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.dongah.dispenser.TECH3800.TLS3800;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.ConfigurationKeyRead;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.handler.ProcessHandler;
import com.dongah.dispenser.pages.ScreenSaverFragment;
import com.dongah.dispenser.rfcard.RfCardReaderReceive;
import com.dongah.dispenser.sqlite.SQLiteHelper;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.FragmentCurrent;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.sqlite.dto.CpSettings;
import com.dongah.dispenser.utils.ToastPositionMake;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.socket.HttpClientHelper;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.TripleDES;
import com.dongah.dispenser.websocket.tcpsocket.ClientSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    Handler handler = new Handler();
    Runnable runnable;

    SQLiteHelper sqLiteHelper;
    SQLiteDatabase sqLiteDatabase;


    UiSeq[] fragmentSeq;
    ClassUiProcess[] classUiProcess;
    ChargingCurrentData[] chargingCurrentData;
    ConfigurationKeyRead configurationKeyRead;
    ChargerConfiguration chargerConfiguration;
    SocketReceiveMessage socketReceiveMessage;
    FragmentChange fragmentChange;
    FragmentCurrent fragmentCurrent;
    ProcessHandler processHandler;

    ControlBoard controlBoard;
    RfCardReaderReceive rfCardReaderReceive;
    TLS3800 tls3800;
    ToastPositionMake toastPositionMake;
    ClientSocket clientSocket;



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

    public ChargingCurrentData getChargingCurrentData(int ch) {
        return chargingCurrentData[ch];
    }

    public ConfigurationKeyRead getConfigurationKeyRead() {
        return configurationKeyRead;
    }

    public ChargerConfiguration getChargerConfiguration() {
        return chargerConfiguration;
    }

    public SocketReceiveMessage getSocketReceiveMessage() {
        return socketReceiveMessage;
    }

    public ControlBoard getControlBoard() {
        return controlBoard;
    }

    public RfCardReaderReceive getRfCardReaderReceive() {
        return rfCardReaderReceive;
    }

    public TLS3800 getTls3800() {
        return tls3800;
    }

    public ToastPositionMake getToastPositionMake() {
        return toastPositionMake;
    }

    public FragmentChange getFragmentChange() {
        return fragmentChange;
    }

    public ProcessHandler getProcessHandler() {
        return processHandler;
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
        sqLiteHelper.dropAllTables(sqLiteDatabase);   // delete all tables
        sqLiteHelper.onCreate(sqLiteDatabase);          // create all tables
        testCrud(); // test data insert

        // fragment current
        fragmentCurrent = new FragmentCurrent();

        // ConfigurationKey read
        configurationKeyRead = new ConfigurationKeyRead();
        configurationKeyRead.onRead();
        toastPositionMake = new ToastPositionMake(this);

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
        controlBoard = new ControlBoard(GlobalVariables.maxChannel, chargerConfiguration.getControlCom());

        // 4. rf card reader : MID = terminal ID
        rfCardReaderReceive = new RfCardReaderReceive(chargerConfiguration.getRfCom());

        // 5. handler
        processHandler = new ProcessHandler(chargerConfiguration);

        // 6. classUiProcess
        chargingCurrentData = new ChargingCurrentData[GlobalVariables.maxChannel];
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            chargingCurrentData[i] = new ChargingCurrentData();
            chargingCurrentData[i].onCurrentDataClear();
        }

        classUiProcess = new ClassUiProcess[GlobalVariables.maxChannel];
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            classUiProcess[i] = new ClassUiProcess(i);
            classUiProcess[i].setUiSeq(UiSeq.INIT);
        }

        // 7. PLC modem
//        clientSocket = new ClientSocket("192.168.39.1", 9999, new ClientSocket.TcpClientListener() {
//            @Override
//            public void onConnected() {
//                logger.debug("connected");
//            }
//
//            @Override
//            public void onDisconnected() {
//
//            }
//
//            @Override
//            public void onError(Exception e) {
//
//            }
//
//            @Override
//            public void onMessageReceived(String message) {
//                Log.d("TCP", "General recv: "+ message);
//            }
//        });

//        clientSocket.start();
//
//        clientSocket.sendCommandExpectPrefix("AT+CNUM", "+CNUM:", 10000)
//                .thenApply(line -> {
//                    // line 예: +CNUM: "LGU","+821222492396",145
//                    String[] parts = line.split(",");
//                    String raw = parts.length >= 2 ? parts[1].replace("\"","") : null;
//                    GlobalVariables.setIMSI(raw == null ? "" : parseToLocal(raw));
//                    return parseToLocal(raw); // 01222492396
//                })
//                .thenCompose(localNumber -> {
//                    Log.d("TCP","Parsed local number: " + localNumber);
//                    // 이어서 DSCREEN 명령
//                    return clientSocket.sendCommandExpectPrefix("AT$$DSCREEN?", "DSCREEN:", 5000);
//                })
//                .thenAccept(dscreenResp -> {
//                    GlobalVariables.setRSRP(parseToRSRP(dscreenResp));
//                    Log.d("TCP","DSCREEN response: " + dscreenResp);
//                    clientSocket.postDisconnected();
//                    clientSocket.closeSocket();
//                })
//                .exceptionally(ex -> {
//                    Log.e("TCP","Command chain error", ex);
//                    return null;
//                });

        // server mode
        if (Objects.equals(chargerConfiguration.getAuthMode(), "1")) {
            sendOcppAuthInfoRequest();
        }

        // 8. ChargerOperate read
        File file = new File(GlobalVariables.getRootPath() + File.separator + "ChargerOperate");
        File firmwareFile = new File(GlobalVariables.getRootPath() + File.separator + "FirmwareStatusNotification");
        if (!firmwareFile.exists()) {
            if (file.exists()) {
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    int count = 0;
                    while ((line = bufferedReader.readLine()) != null) {
                        GlobalVariables.ChargerOperation[count] = Objects.equals(line, "true");
                        count++;
                    }
                } catch (Exception e) {
                    logger.error("ChargerOperate read error : {}", e.getMessage());
                }
            } else {
                for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                    GlobalVariables.ChargerOperation[i] = true;
                }
            }
        }
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


    // screen saver
    private static final long INACTIVITY_TIMEOUT = 1 * 60 * 1000L;  // 1분 (ms 단위)
    private final Handler inactivityHandler = new Handler(Looper.getMainLooper());
    private final Runnable inactivityRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                boolean check = fragmentChange.onFragmentScreenSaverChange();
                if (!check) {
                    resetInactivityTimer();
                }
            } catch (Exception e) {
                logger.error("ScreenSaver inactivityRunnable error : {}", e.getMessage());
            }
        }
    };


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

    /** ui version update */
    public void onRebooting() {
        try {
            boolean result = false;
            ChargingCurrentData chargingCurrentData;
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(i);
                result = chargingCurrentData.isReBoot() && (getClassUiProcess(i).getUiSeq() == UiSeq.INIT);
            }

            if (result) {
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    getClassUiProcess(i).setUiSeq(UiSeq.REBOOTING);
                    ((MainActivity) MainActivity.mContext).getChargingCurrentData(i).setStopReason(Reason.Reboot);
                }
            }
        } catch (Exception e) {
            logger.error("MainActivity version reboot : {}", e.getMessage());
        }
    }

    @SuppressLint("ConstantConditions")
    public void onRebooting(String type) {
        try {
            ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().getSocket().disconnect();
            if (Objects.equals(type, "Soft")) {
                ActivityCompat.finishAffinity(((MainActivity) MainActivity.mContext));
                System.exit(0);
            } else {
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                powerManager.reboot("reboot");
            }
        } catch (Exception e) {
            logger.error("onRebooting : {}", e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inactivityHandler.removeCallbacks(inactivityRunnable);
        //Custom status notification stop
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            ((MainActivity) MainActivity.mContext).getClassUiProcess(i).onCustomStatusNotificationStop();
        }
    }

    /**
     * HTTPS 연결이 안 되면 다시 접속
     **/
    private static final int RETRY_DELAY_MS = 3000;     // 3초
    private static final int MAX_RETRY_COUNT = 5;       // 최대 재시도 횟수
    private int retryCount = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private void sendOcppAuthInfoRequest() {
        HttpClientHelper httpClientHelper = new HttpClientHelper();
        String url = chargerConfiguration.getServerConnectingString();

        TripleDES tripleDES = new TripleDES();

        try {
            // TODO: websocket
        } catch (Exception e) {
            logger.error("REQUEST_ERROR {}", e.getMessage());
            scheduleRetry();
        }
    }

    private void scheduleRetry() {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            Log.w("HTTP", "재시도 " + retryCount + "회 / " + MAX_RETRY_COUNT + "회");
            mainHandler.postDelayed(this::sendOcppAuthInfoRequest, RETRY_DELAY_MS);
        } else {
            Log.e("HTTP", "최대 재시도 횟수 초과 → 요청 중단");
        }
    }

    private String parseToLocal(String number) {
        if (number.startsWith("+82")) {
            return "0" + number.substring(3);
        }
        return number;
    }

    private String parseToRSRP(String resp) {
        Pattern p = Pattern.compile("RSRP:([-]?\\d+)");
        Matcher m = p.matcher(resp);
        if (m.find()) {
            int rsrp = Integer.parseInt(m.group(1));  // -71
            return String.valueOf(rsrp);
        } else {
            System.out.println("RSRP not found");
        }
        return "";
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

        // insert
        long rowId = helper.insert(settings);

        System.out.println("INSERT RESULT1 = " + rowId);

        settings.stationId = "ST02";
        settings.chargerId = "CH02";
        settings.modelNm = "MD02";
        settings.vendorNm = "DONGAH";
        settings.fwVersion = "1.0.1";
        settings.socLimit = "80";
        settings.availability = "Operative";

        // insert
        long rowId2 = helper.insert(settings);

        System.out.println("INSERT RESULT2 = " + rowId2);

        // select all
        Cursor cursor = helper.selectAll(settings.getTableName());
        while (cursor.moveToNext()) {
            System.out.println("SELECT RESULT = " +
                    "ID: " + cursor.getInt(cursor.getColumnIndexOrThrow("ID")) +
                    ", STATION_ID: " + cursor.getString(cursor.getColumnIndexOrThrow("STATION_ID")) +
                    ", CHARGER_ID: " + cursor.getString(cursor.getColumnIndexOrThrow("CHARGER_ID")) +
                    ", MODEL_NM: " + cursor.getString(cursor.getColumnIndexOrThrow("MODEL_NM")) +
                    ", VENDOR_NM: " + cursor.getString(cursor.getColumnIndexOrThrow("VENDOR_NM")) +
                    ", FW_VERSION: " + cursor.getString(cursor.getColumnIndexOrThrow("FW_VERSION")) +
                    ", SOC_LIMIT: " + cursor.getString(cursor.getColumnIndexOrThrow("SOC_LIMIT")) +
                    ", AVAILABILITY: " + cursor.getString(cursor.getColumnIndexOrThrow("AVAILABILITY"))
            );
        }
        cursor.close();

        // update
        ContentValues updateValues = new ContentValues();
        updateValues.put("MODEL_NM", "MD99");
        int updated = helper.update(settings.getTableName(), updateValues, "ID=?", new String[]{"2"});
        System.out.println("UPDATE RESULT = " + updated);

        // select all
        Cursor cursor2 = helper.selectAll(settings.getTableName());
        while (cursor2.moveToNext()) {
            System.out.println("SELECT2 RESULT = " +
                    "ID: " + cursor2.getInt(cursor2.getColumnIndexOrThrow("ID")) +
                    ", STATION_ID: " + cursor2.getString(cursor2.getColumnIndexOrThrow("STATION_ID")) +
                    ", CHARGER_ID: " + cursor2.getString(cursor2.getColumnIndexOrThrow("CHARGER_ID")) +
                    ", MODEL_NM: " + cursor2.getString(cursor2.getColumnIndexOrThrow("MODEL_NM")) +
                    ", VENDOR_NM: " + cursor2.getString(cursor2.getColumnIndexOrThrow("VENDOR_NM")) +
                    ", FW_VERSION: " + cursor2.getString(cursor2.getColumnIndexOrThrow("FW_VERSION")) +
                    ", SOC_LIMIT: " + cursor2.getString(cursor2.getColumnIndexOrThrow("SOC_LIMIT")) +
                    ", AVAILABILITY: " + cursor2.getString(cursor2.getColumnIndexOrThrow("AVAILABILITY"))
            );
        }
        cursor2.close();

        // delete
        int deleted = helper.delete(settings.getTableName(), "ID=?", new String[]{"2"});
        System.out.println("DELETE RESULT = " + deleted);

        // select with where
        Cursor cursor3 = helper.select(settings.getTableName(), "ID=?", new String[]{"1"});
        while (cursor3.moveToNext()) {
            System.out.println("SELECT3 RESULT = " +
                    "ID: " + cursor3.getInt(cursor3.getColumnIndexOrThrow("ID")) +
                    ", STATION_ID: " + cursor3.getString(cursor3.getColumnIndexOrThrow("STATION_ID")) +
                    ", CHARGER_ID: " + cursor3.getString(cursor3.getColumnIndexOrThrow("CHARGER_ID")) +
                    ", MODEL_NM: " + cursor3.getString(cursor3.getColumnIndexOrThrow("MODEL_NM")) +
                    ", VENDOR_NM: " + cursor3.getString(cursor3.getColumnIndexOrThrow("VENDOR_NM")) +
                    ", FW_VERSION: " + cursor3.getString(cursor3.getColumnIndexOrThrow("FW_VERSION")) +
                    ", SOC_LIMIT: " + cursor3.getString(cursor3.getColumnIndexOrThrow("SOC_LIMIT")) +
                    ", AVAILABILITY: " + cursor3.getString(cursor3.getColumnIndexOrThrow("AVAILABILITY"))
            );
        }
        cursor3.close();

        // delete all(테이블 삭제x)
        int deletedAll = helper.deleteAll(settings.getTableName());
        System.out.println("DELETE ALL RESULT = " + deletedAll);

        // delete table
        helper.dropTable(helper.getWritableDatabase(), settings.getTableName());
    }

    // 키보드 내리기
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();

        if (view != null) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];

            if (ev.getAction() == MotionEvent.ACTION_UP &&
                    (x < view.getLeft() || x >= view.getRight() ||
                            y < view.getTop() || y > view.getBottom())) {

                // 키보드 내리기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // EditText 포커스 제거
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}