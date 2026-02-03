package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

import com.dongah.dispenser.websocket.ocpp.common.model.Validatable;

import java.util.Objects;

public class FullrechgsocData implements Validatable {

    private String chargeBoxSerialNumber;   // 충전소ID
    private String chargePointSerialNumber; // 충전기ID
    private int connectorId;                // Connector ID
    private String idTag;                   // ID Tag
    private String timestamp;               // 수집시간(ex: 2025-07-24T10:09:49.832Z)

    public FullrechgsocData() {}

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
        FullrechgsocData that = (FullrechgsocData) o;
        return Objects.equals(chargeBoxSerialNumber, that.chargeBoxSerialNumber) &&
                Objects.equals(chargePointSerialNumber, that.chargePointSerialNumber) &&
                connectorId == that.connectorId && Objects.equals(idTag, that.idTag) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeBoxSerialNumber, chargePointSerialNumber, connectorId,
                idTag, timestamp);
    }
}
