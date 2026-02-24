package com.dongah.dispenser.websocket.socket.handler.handlersend;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.LogDataSave;
import com.dongah.dispenser.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.dispenser.websocket.ocpp.core.HeartbeatRequest;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class HeartbeatThread extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatThread.class);

    private volatile boolean stopped = false;
    private final int delayTime;
    private int count = 0;

    private final HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
    private MainActivity activity;
    private SocketReceiveMessage socketReceiveMessage;
    private final LogDataSave logDataSave = new LogDataSave("log");


    @RequiresApi(api = Build.VERSION_CODES.O)
    public HeartbeatThread(int delayTime) {
        this.delayTime = delayTime;

        activity = (MainActivity) MainActivity.mContext;
        if (activity != null) {
            socketReceiveMessage = activity.getSocketReceiveMessage();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    socketReceiveMessage.onSend(
                            100,
                            heartbeatRequest.getActionName(),
                            heartbeatRequest
                    );
                } catch (OccurenceConstraintException e) {
                    throw new RuntimeException(e);
                }
            }, 200);
        }
    }

    public void stopThread() {
        stopped = true;
        interrupt(); // sleep 깨우기
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        logger.info("HeartbeatThread started");
        while (!stopped && !isInterrupted()) {
            try {
                Thread.sleep(java.time.Duration.ofMinutes(delayTime).toMillis());
                processHeartbeat();
            } catch (InterruptedException e) {
                logger.info("HeartbeatThread interrupted");
                Thread.currentThread().interrupt(); // 인터럽트 상태 복구
                break;
            } catch (Exception e) {
                logger.error("HeartbeatThread error : {}", e.getMessage(), e);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processHeartbeat() throws OccurenceConstraintException {
        if (activity == null) return;

        ClassUiProcess[] classUiProcess = activity.getClassUiProcess();
        boolean sendCheck = true;
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            if (classUiProcess[i].getUiSeq() == UiSeq.CHARGING) {
                sendCheck = false;
                break;
            }
        }
        // CHARGING 인 경우 HeartBit 안 보냄.
        if (sendCheck) {
            socketReceiveMessage.onSend(
                    100,
                    heartbeatRequest.getActionName(),
                    heartbeatRequest
            );

        }

        // 30일 이상 로그 삭제
        logDataSave.removeLogData();
        // 미전송 dump 데이터 전송
        SocketState socketState = socketReceiveMessage.getSocket().getState();
        if (socketState == SocketState.OPEN) {
            onDumpData(socketReceiveMessage);
        }
    }

    private void onDumpData(SocketReceiveMessage socketReceiveMessage) {
        File file = new File(GlobalVariables.getRootPath()
                + File.separator + "dump" + File.separator + "dump");

        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                socketReceiveMessage.onSend(line);
            }
            // 성공 시 삭제
            file.delete();

        } catch (Exception e) {
            logger.error("onDumpData error", e);
        }
    }
}
