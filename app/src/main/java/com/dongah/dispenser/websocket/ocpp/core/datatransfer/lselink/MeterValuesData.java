package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

public class MeterValuesData {

    private String chargeBoxSerialNumber;   // 충전소ID
    private String chargePointSerialNumber; // 충전기ID
    private int connectorId;                // Connector ID
    private int transactionId;              // Transaction ID
    private String idTag;                   // ID Tag
    private String timestamp;               // (ex:2023-05-30T11:16:12.899Z)
    public double power;
    public int eps;                         // 전압
    public int ecu;                         // 전류
    public float accWh;                     // 충전기 계량기에서 올려준 적산 적정량(kWh, 소수점 3자리)
    public float accTickWh;                 // 이전 미터 벨류와 현재 미터벨류 사이의 사용전력량(kWh, 소수점 3자리)
    public int accTickTime;                 // 이전 미터벨류의 현재 미터벨류 사이의 충전 시간(초) ex: 60
    public int rechgHr;                     // 충전시간(초)
    public int remnHr;                      // 잔여시간(초)
    public String btrRm;                    // 배터리잔량
    public String slprcUpc;                 // 매출단가(소비자에 부가되는 단가)
    public String crtrUpc;                  // 매입단가(시간별 한전 요금)
}
