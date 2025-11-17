package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

import com.dongah.dispenser.websocket.ocpp.common.model.Validatable;

import java.util.Objects;

public class KeyInRechgInfoData implements Validatable {

    private String chargeBoxSerialNumber;   // 충전소ID
    private String chargePointSerialNumber; // 충전기ID
    private int connectorId;                // Connector ID
    private String idTag;                   // ID Tag
    private float rechgKw;
    private int rechgAmt;
    private String timestamp;

    public KeyInRechgInfoData() {}

    public String getChargeBoxSerialNumber() {
        return chargeBoxSerialNumber;
    }

    public void setChargeBoxSerialNumber(String chargeBoxSerialNumber) {
        this.chargeBoxSerialNumber = chargeBoxSerialNumber;
    }

    public String getChargePointSerialNumber() {
        return chargePointSerialNumber;
    }

    public void setChargePointSerialNumber(String chargePointSerialNumber) {
        this.chargePointSerialNumber = chargePointSerialNumber;
    }

    public int getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public String getIdTag() {
        return idTag;
    }

    public void setIdTag(String idTag) {
        this.idTag = idTag;
    }

    public float getRechgKw() {
        return rechgKw;
    }

    public void setRechgKw(float rechgKw) {
        this.rechgKw = rechgKw;
    }

    public int getRechgAmt() {
        return rechgAmt;
    }

    public void setRechgAmt(int rechgAmt) {
        this.rechgAmt = rechgAmt;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean validate() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyInRechgInfoData that = (KeyInRechgInfoData) o;
        return Objects.equals(chargeBoxSerialNumber, that.chargeBoxSerialNumber) &&
                Objects.equals(chargePointSerialNumber, that.chargePointSerialNumber) &&
                connectorId == that.connectorId &&
                Objects.equals(idTag, that.idTag) &&
                rechgKw == that.rechgKw &&
                rechgAmt == that.rechgAmt &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeBoxSerialNumber, chargePointSerialNumber, connectorId,
                idTag, rechgKw, rechgAmt, timestamp);
    }
}
