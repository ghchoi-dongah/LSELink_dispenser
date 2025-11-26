package com.dongah.dispenser.controlboard;

import com.dongah.dispenser.utils.BitUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RxData {
    private static final Logger logger = LoggerFactory.getLogger(RxData.class);

    private static final int RX_DATA_CNT = 46;

    // 400 address
    public boolean csPilot = false;         // 1bit
    public boolean csStart = false;         // 4bit
    public boolean csStop = false;          // 5bit
    public boolean csFault = false;         // 6bit
    public boolean csReady = false;         // 7bit
    public boolean csRY1Status = false;     // 8bit
    public boolean csRY2Status = false;     // 9bit
    public boolean csRY3Status = false;     // 10bit
    public boolean csRY4Status = false;     // 11bit
    public boolean csRY5Status = false;     // 12bit
    public boolean csRY6Status = false;     // 13bit
    public boolean csMC1Status = false;     // 14bit
    public boolean csMC2Status = false;     // 15bit

    // 402 address
    public short cpVoltage = 0;             // cp 전압 (ex. 1198 ==> 119.8)
    // 403 address
    public short firmwareVersion = 0;       // firmware version (ex. 121 ==> 1.2.1)
    // 404 address
    public short remainTime = 0;            // 충전 남은 시간
    // 405 address
    public short soc = 0;                   // soc
    // 406 address
    public boolean csMc1Fault = false;      // 406[0] 0: 정상, 1: 비정상(오픈 또는 융착)
    public boolean csMc2Fault = false;      // 406[1] 0: 정상, 1: 비정상(오픈 또는 융착)
    public boolean csRelay1 = false;        // 406[2] 0: 정상, 1: 비정상(오픈 또는 융착)
    public boolean csRelay2 = false;        // 406[3] 0: 정상, 1: 비정상(오픈 또는 융착)
    public boolean csRelay3 = false;        // 406[4] 0: 정상, 1: 비정상(오픈 또는 융착)
    public boolean csRelay4 = false;        // 406[5] 0: 정상, 1: 비정상(오픈 또는 융착)
    public boolean csRelay5 = false;        // 406[6] 0: 정상, 1: 비정상(오픈 또는 융착)
    public boolean csRelay6 = false;        // 406[7] 0: 정상, 1: 비정상(오픈 또는 융착)
    // 407 ~ 408 address
    public long powerMeter = 0;     // kWh
    // 409 address
    public short outVoltage = 0;    // 0.1V
    // 410 address
    public short outCurrent = 0;    // 0.1A
    // 411 address
    public boolean csEmergency = false;         // 411[0] Emergency
    public boolean csPLCComm = false;           // 411[1] PLC 통신 오류
    public boolean csPowerMeterComm = false;    // 411[2] 전력량계 통신 오류
    public boolean csModule1Comm = false;  // 411[3] 파워모듈1 통신 오류
    public boolean csModule2Comm = false;  // 411[4] 파워모듈2 통신 오류
    public boolean csModule3Comm = false;  // 411[5] 파워모듈3 통신 오류
    public boolean csModule4Comm = false;  // 411[6] 파워모듈4 통신 오류
    public boolean csChargerLeak = false;       // 411[7] 충전기 누설 감지
    public boolean csCarLeak = false;           // 411[8] 차량 누설 감지
    public boolean csOutOVR = false;            // 411[9] 출력 과전압(정격의 110%)
    public boolean csOutOCR = false;            // 411[10] 출력 과전류(정격의 110%)
    public boolean csCouplerTempSensor = false; // 411[11] 커플러 온도 센서 이상
    public boolean csCouplerOVT = false;        // 411[12] 커플러 과온도
    // 412 address
    public boolean csModule1Error = false;      // 412[0] 파워모듈1 에러
    public boolean csModule2Error = false;      // 412[1] 파워모듈2 에러
    public boolean csModule3Error = false;      // 412[2] 파워모듈3 에러
    public boolean csModule4Error = false;      // 412[3] 파워모듈4 에러
    // 414 address
    public short couplerTemp = 0;               // 커플러 온도 1 ==> 1℃

    /**
     * 415 ~ 418 (CSM_Status)
     */
    public byte csmSeccReady = 0x00;
    public byte csmSeccStatusCode = 0x00;
    // CSM_STATUS_NONE = 0             CSM_STATUS_READY = 1
    // CSM_STATUS_WAIT_HANDSHAKE = 2   CSM_STATUS_SESSION_READY = 3
    // CSM_STATUS_AUTH_CHECK = 4       CSM_STATUS_CHARGE_PARAM_CHECK = 5
    // CSM_STATUS_CABLE_CHECK = 6      CSM_STATUS_PRE_CHARGE = 7
    // CSM_STATUS_CHARGING = 8         CSM_STATUS_STOP_CHARGING = 9
    // CSM_STATUS_FAULT_STOPPED = 10   CSM_STATUS_NORMAL_STOPPED = 11
    public byte csmSeccErrorCode = 0x00;
    public byte csmSeccSWVersion = 0x00;
    public byte csmProximityVoltage = 0x00;
    public short csmPwmDutyCycle = 0;
    public byte csmSeccProtocol = 0x0;
    public byte csmSeccPwmVoltage = 0x00;

    /**
     * 419 ~ 422 (Vehicle_EvccId)
     */
    public long csmVehicleEvccId = 0L;

    /**
     * 423 ~ 426 (Vehicle_Charging_Service)
     * */
    public byte csmSelectedPaymentOption = 0x00;
    public byte csmIso20ControlMode = 0x00;
    public byte csmBPTChannelSelection = 0x00;
    public byte csmRequestedEnergyTransferType = 0x00;
    //0x00: AC_SINGLE_PHASE_CORE  0x01: AC_THREE_PHASE_CORE
    //0x02: DC_CORE               0x03: DC_EXTENDED
    //0x04: DC_COMBO_CORE         0x05: DC_DUAL
    public short csmMaxSupportPoint = 0;
    public int csmDepartureTime = 0;

    /**
     * 427 ~ 430 (Vehicle_DC_Charging_Status)
     * */
    public boolean csmBulkChargingComplete = false;
    public boolean csmFullChargingComplete = false;
    public boolean csmEvReady = false;
    public boolean csmEvCabinConditioning = false;
    public boolean csmEvRessConditioning = false;
    public byte csmEvErrorCode = 0x00;
    public byte csmRessSoc = 0x00;
    public short csmRemainingTimeFullSoc = 0;
    public short csmRemainingTimeBulkSoc = 0;

    /**
     * 431 ~ 434 (Vehicle_DC_Charging_Variable)
     * */
    public short csmEVTargetCurrent = 0;
    public short csmEVSMaximumCurrentLimit = 0;
    public short csmEVSMaximumVoltageLimit = 0;

    /**
     * 435 ~ 438 (Vehicle_DC_Charge_Parameter)
     * */
    public short csmEVEnergyCapacity = 0;
    public short csmEVEnergyRequest = 0;
    public short csmEVMaximumPowerLimit = 0;
    public byte csmFullSOC = 0x00;
    public byte csmBulkSOC = 0x00;

    /**
     * 439 ~ 442 (Vehicle_AC_Charge_Parameter)
     * */

    // 443 address
    public short csmReserved0 = 0;
    // 444 address
    public short csmEVTargetVoltage = 0;
    public short csmReserved1 = 0;
    // 445 address
    public short csmReserved2 = 0;


    public void Decode(short[] data) {
        try {
            // 400 address
            csPilot = BitUtilities.getBitBoolean(data[0], 1);
            csStart = BitUtilities.getBitBoolean(data[0], 4);
            csStop = BitUtilities.getBitBoolean(data[0], 5);
            csFault = BitUtilities.getBitBoolean(data[0], 6);
            csReady = BitUtilities.getBitBoolean(data[0], 7);
            csRY1Status = BitUtilities.getBitBoolean(data[0], 8);
            csRY2Status = BitUtilities.getBitBoolean(data[0], 9);
            csRY3Status = BitUtilities.getBitBoolean(data[0], 10);
            csRY4Status = BitUtilities.getBitBoolean(data[0], 11);
            csRY5Status = BitUtilities.getBitBoolean(data[0], 12);
            csRY6Status = BitUtilities.getBitBoolean(data[0], 13);
            csMC1Status = BitUtilities.getBitBoolean(data[0], 14);
            csMC2Status = BitUtilities.getBitBoolean(data[0], 15);

            cpVoltage = data[2];
            firmwareVersion = data[3];
            remainTime = data[4];
            soc = data[5];

            // 406
            csMc1Fault = BitUtilities.getBitBoolean(data[6], 0);
            csMc2Fault = BitUtilities.getBitBoolean(data[6], 1);
            csRelay1 = BitUtilities.getBitBoolean(data[6], 2);
            csRelay2 = BitUtilities.getBitBoolean(data[6], 3);
            csRelay3 = BitUtilities.getBitBoolean(data[6], 4);
            csRelay4 = BitUtilities.getBitBoolean(data[6], 5);
            csRelay5 = BitUtilities.getBitBoolean(data[6], 6);
            csRelay6 = BitUtilities.getBitBoolean(data[6], 7);

            powerMeter = (data[7] << 16) | (data[8] & 0xffff);      //1w
            outVoltage = data[9];                                   //0.1V
            outCurrent = data[10];                                  //0.1A

            // 411
            csEmergency = BitUtilities.getBitBoolean(data[11], 0);
            csPLCComm = BitUtilities.getBitBoolean(data[11], 1);
            csPowerMeterComm = BitUtilities.getBitBoolean(data[11], 2);
            csModule1Comm = BitUtilities.getBitBoolean(data[11], 3);
            csModule2Comm = BitUtilities.getBitBoolean(data[11], 4);
            csModule3Comm = BitUtilities.getBitBoolean(data[11], 5);
            csModule4Comm = BitUtilities.getBitBoolean(data[11], 6);
            csChargerLeak = BitUtilities.getBitBoolean(data[11], 7);
            csCarLeak = BitUtilities.getBitBoolean(data[11], 8);
            csOutOVR = BitUtilities.getBitBoolean(data[11], 9);
            csOutOCR = BitUtilities.getBitBoolean(data[11], 10);
            csCouplerTempSensor = BitUtilities.getBitBoolean(data[11], 11);
            csCouplerOVT = BitUtilities.getBitBoolean(data[11], 12);

            // 412
            csModule1Error = BitUtilities.getBitBoolean(data[12], 0);
            csModule2Error = BitUtilities.getBitBoolean(data[12], 1);
            csModule3Error = BitUtilities.getBitBoolean(data[12], 2);
            csModule4Error = BitUtilities.getBitBoolean(data[12], 3);

            couplerTemp = data[14];                 //커플러 온도 1 => 1℃

            /** plc model information */
            byte[] parseData, parseNext;
            // CSM_Status
            parseData = BitUtilities.ShortToByteArray(data[15]);
            csmSeccReady = parseData[0];
            csmSeccStatusCode = parseData[1];

            parseNext = BitUtilities.ShortToByteArray(data[17]);

            // Vehicle_EvccId
            csmVehicleEvccId = (((long) data[19] << 32) | ((long) data[20] << 24) | (data[21] << 16) | (data[22] & 0xffff));

            // Vehicle_Charging_Service

            // Vehicle_DC_Charging_Status
            // Vehicle_DC_Charging_Variable
            // Vehicle_DC_Charge_Parameter
            // Vehicle_AC_Charge_Parameter

        } catch (Exception e) {
            logger.error("rx data decode error : {}", e.getMessage());
        }
    }

    public boolean isCsPilot() {
        return csPilot;
    }

    public void setCsPilot(boolean csPilot) {
        this.csPilot = csPilot;
    }

    public boolean isCsStart() {
        return csStart;
    }

    public void setCsStart(boolean csStart) {
        this.csStart = csStart;
    }

    public boolean isCsStop() {
        return csStop;
    }

    public void setCsStop(boolean csStop) {
        this.csStop = csStop;
    }

    public boolean isCsFault() {
        return csFault;
    }

    public void setCsFault(boolean csFault) {
        this.csFault = csFault;
    }

    public boolean isCsReady() {
        return csReady;
    }

    public void setCsReady(boolean csReady) {
        this.csReady = csReady;
    }

    public boolean isCsRY1Status() {
        return csRY1Status;
    }

    public void setCsRY1Status(boolean csRY1Status) {
        this.csRY1Status = csRY1Status;
    }

    public boolean isCsRY2Status() {
        return csRY2Status;
    }

    public void setCsRY2Status(boolean csRY2Status) {
        this.csRY2Status = csRY2Status;
    }

    public boolean isCsRY3Status() {
        return csRY3Status;
    }

    public void setCsRY3Status(boolean csRY3Status) {
        this.csRY3Status = csRY3Status;
    }

    public boolean isCsRY4Status() {
        return csRY4Status;
    }

    public void setCsRY4Status(boolean csRY4Status) {
        this.csRY4Status = csRY4Status;
    }

    public boolean isCsRY5Status() {
        return csRY5Status;
    }

    public void setCsRY5Status(boolean csRY5Status) {
        this.csRY5Status = csRY5Status;
    }

    public boolean isCsRY6Status() {
        return csRY6Status;
    }

    public void setCsRY6Status(boolean csRY6Status) {
        this.csRY6Status = csRY6Status;
    }

    public boolean isCsMC1Status() {
        return csMC1Status;
    }

    public void setCsMC1Status(boolean csMC1Status) {
        this.csMC1Status = csMC1Status;
    }

    public boolean isCsMC2Status() {
        return csMC2Status;
    }

    public void setCsMC2Status(boolean csMC2Status) {
        this.csMC2Status = csMC2Status;
    }

    public short getCpVoltage() {
        return cpVoltage;
    }

    public void setCpVoltage(short cpVoltage) {
        this.cpVoltage = cpVoltage;
    }

    public short getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(short firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public short getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(short remainTime) {
        this.remainTime = remainTime;
    }

    public short getSoc() {
        return soc;
    }

    public void setSoc(short soc) {
        this.soc = soc;
    }

    public boolean isCsMc1Fault() {
        return csMc1Fault;
    }

    public void setCsMc1Fault(boolean csMc1Fault) {
        this.csMc1Fault = csMc1Fault;
    }

    public boolean isCsMc2Fault() {
        return csMc2Fault;
    }

    public void setCsMc2Fault(boolean csMc2Fault) {
        this.csMc2Fault = csMc2Fault;
    }

    public boolean isCsRelay1() {
        return csRelay1;
    }

    public void setCsRelay1(boolean csRelay1) {
        this.csRelay1 = csRelay1;
    }

    public boolean isCsRelay2() {
        return csRelay2;
    }

    public void setCsRelay2(boolean csRelay2) {
        this.csRelay2 = csRelay2;
    }

    public boolean isCsRelay3() {
        return csRelay3;
    }

    public void setCsRelay3(boolean csRelay3) {
        this.csRelay3 = csRelay3;
    }

    public boolean isCsRelay4() {
        return csRelay4;
    }

    public void setCsRelay4(boolean csRelay4) {
        this.csRelay4 = csRelay4;
    }

    public boolean isCsRelay5() {
        return csRelay5;
    }

    public void setCsRelay5(boolean csRelay5) {
        this.csRelay5 = csRelay5;
    }

    public boolean isCsRelay6() {
        return csRelay6;
    }

    public void setCsRelay6(boolean csRelay6) {
        this.csRelay6 = csRelay6;
    }

    public long getPowerMeter() {
        return powerMeter;
    }

    public void setPowerMeter(long powerMeter) {
        this.powerMeter = powerMeter;
    }

    public short getOutVoltage() {
        return outVoltage;
    }

    public void setOutVoltage(short outVoltage) {
        this.outVoltage = outVoltage;
    }

    public short getOutCurrent() {
        return outCurrent;
    }

    public void setOutCurrent(short outCurrent) {
        this.outCurrent = outCurrent;
    }

    public boolean isCsEmergency() {
        return csEmergency;
    }

    public void setCsEmergency(boolean csEmergency) {
        this.csEmergency = csEmergency;
    }

    public boolean isCsPLCComm() {
        return csPLCComm;
    }

    public void setCsPLCComm(boolean csPLCComm) {
        this.csPLCComm = csPLCComm;
    }

    public boolean isCsPowerMeterComm() {
        return csPowerMeterComm;
    }

    public void setCsPowerMeterComm(boolean csPowerMeterComm) {
        this.csPowerMeterComm = csPowerMeterComm;
    }

    public boolean isCsModule1Comm() {
        return csModule1Comm;
    }

    public void setCsModule1Comm(boolean csModule1Comm) {
        this.csModule1Comm = csModule1Comm;
    }

    public boolean isCsModule2Comm() {
        return csModule2Comm;
    }

    public void setCsModule2Comm(boolean csModule2Comm) {
        this.csModule2Comm = csModule2Comm;
    }

    public boolean isCsModule3Comm() {
        return csModule3Comm;
    }

    public void setCsModule3Comm(boolean csModule3Comm) {
        this.csModule3Comm = csModule3Comm;
    }

    public boolean isCsModule4Comm() {
        return csModule4Comm;
    }

    public void setCsModule4Comm(boolean csModule4Comm) {
        this.csModule4Comm = csModule4Comm;
    }

    public boolean isCsChargerLeak() {
        return csChargerLeak;
    }

    public void setCsChargerLeak(boolean csChargerLeak) {
        this.csChargerLeak = csChargerLeak;
    }

    public boolean isCsCarLeak() {
        return csCarLeak;
    }

    public void setCsCarLeak(boolean csCarLeak) {
        this.csCarLeak = csCarLeak;
    }

    public boolean isCsOutOVR() {
        return csOutOVR;
    }

    public void setCsOutOVR(boolean csOutOVR) {
        this.csOutOVR = csOutOVR;
    }

    public boolean isCsOutOCR() {
        return csOutOCR;
    }

    public void setCsOutOCR(boolean csOutOCR) {
        this.csOutOCR = csOutOCR;
    }

    public boolean isCsCouplerTempSensor() {
        return csCouplerTempSensor;
    }

    public void setCsCouplerTempSensor(boolean csCouplerTempSensor) {
        this.csCouplerTempSensor = csCouplerTempSensor;
    }

    public boolean isCsCouplerOVT() {
        return csCouplerOVT;
    }

    public void setCsCouplerOVT(boolean csCouplerOVT) {
        this.csCouplerOVT = csCouplerOVT;
    }

    public boolean isCsModule1Error() {
        return csModule1Error;
    }

    public void setCsModule1Error(boolean csModule1Error) {
        this.csModule1Error = csModule1Error;
    }

    public boolean isCsModule2Error() {
        return csModule2Error;
    }

    public void setCsModule2Error(boolean csModule2Error) {
        this.csModule2Error = csModule2Error;
    }

    public boolean isCsModule3Error() {
        return csModule3Error;
    }

    public void setCsModule3Error(boolean csModule3Error) {
        this.csModule3Error = csModule3Error;
    }

    public boolean isCsModule4Error() {
        return csModule4Error;
    }

    public void setCsModule4Error(boolean csModule4Error) {
        this.csModule4Error = csModule4Error;
    }

    public short getCouplerTemp() {
        return couplerTemp;
    }

    public void setCouplerTemp(short couplerTemp) {
        this.couplerTemp = couplerTemp;
    }
}
