package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

import com.dongah.dispenser.websocket.ocpp.common.model.Validatable;

import java.util.Objects;

public class PaymentData implements Validatable {

    private String chargeBoxSerialNumber;   // 충전소ID
    private String chargePointSerialNumber; // 충전기ID
    private int connectorId;                // Connector ID
    private int transactionId;              // Transaction ID
    private String idTag;                   // ID Tag
    private String timestamp;               // (ex: 2023-01-04T13:07:48.554Z)
    private String paymentInfo;

    public PaymentData() {}

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

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
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

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    @Override
    public boolean validate() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentData that = (PaymentData) o;
        return Objects.equals(chargeBoxSerialNumber, that.chargeBoxSerialNumber) &&
                Objects.equals(chargePointSerialNumber, that.chargePointSerialNumber) &&
                connectorId == that.connectorId &&
                transactionId == that.transactionId &&
                Objects.equals(idTag, that.idTag) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(paymentInfo, that.paymentInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeBoxSerialNumber, chargePointSerialNumber, connectorId,
                transactionId, idTag, timestamp, paymentInfo);
    }
}
