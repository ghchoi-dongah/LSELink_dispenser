package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

import com.dongah.dispenser.websocket.ocpp.common.model.Validatable;

import java.util.Objects;

public class ChangeElecModeData implements Validatable {

    private int connectorId;    // Connector ID
    private int HH00;
    private int HH01;
    private int HH02;
    private int HH03;
    private int HH04;
    private int HH05;
    private int HH06;
    private int HH07;
    private int HH08;
    private int HH09;
    private int HH10;
    private int HH11;
    private int HH12;
    private int HH13;
    private int HH14;
    private int HH15;
    private int HH16;
    private int HH17;
    private int HH18;
    private int HH19;
    private int HH20;
    private int HH21;
    private int HH22;
    private int HH23;

    public ChangeElecModeData() {}

    public int getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public int getHH00() {
        return HH00;
    }

    public void setHH00(int HH00) {
        this.HH00 = HH00;
    }

    public int getHH01() {
        return HH01;
    }

    public void setHH01(int HH01) {
        this.HH01 = HH01;
    }

    public int getHH02() {
        return HH02;
    }

    public void setHH02(int HH02) {
        this.HH02 = HH02;
    }

    public int getHH03() {
        return HH03;
    }

    public void setHH03(int HH03) {
        this.HH03 = HH03;
    }

    public int getHH04() {
        return HH04;
    }

    public void setHH04(int HH04) {
        this.HH04 = HH04;
    }

    public int getHH05() {
        return HH05;
    }

    public void setHH05(int HH05) {
        this.HH05 = HH05;
    }

    public int getHH06() {
        return HH06;
    }

    public void setHH06(int HH06) {
        this.HH06 = HH06;
    }

    public int getHH07() {
        return HH07;
    }

    public void setHH07(int HH07) {
        this.HH07 = HH07;
    }

    public int getHH08() {
        return HH08;
    }

    public void setHH08(int HH08) {
        this.HH08 = HH08;
    }

    public int getHH09() {
        return HH09;
    }

    public void setHH09(int HH09) {
        this.HH09 = HH09;
    }

    public int getHH10() {
        return HH10;
    }

    public void setHH10(int HH10) {
        this.HH10 = HH10;
    }

    public int getHH11() {
        return HH11;
    }

    public void setHH11(int HH11) {
        this.HH11 = HH11;
    }

    public int getHH12() {
        return HH12;
    }

    public void setHH12(int HH12) {
        this.HH12 = HH12;
    }

    public int getHH13() {
        return HH13;
    }

    public void setHH13(int HH13) {
        this.HH13 = HH13;
    }

    public int getHH14() {
        return HH14;
    }

    public void setHH14(int HH14) {
        this.HH14 = HH14;
    }

    public int getHH15() {
        return HH15;
    }

    public void setHH15(int HH15) {
        this.HH15 = HH15;
    }

    public int getHH16() {
        return HH16;
    }

    public void setHH16(int HH16) {
        this.HH16 = HH16;
    }

    public int getHH17() {
        return HH17;
    }

    public void setHH17(int HH17) {
        this.HH17 = HH17;
    }

    public int getHH18() {
        return HH18;
    }

    public void setHH18(int HH18) {
        this.HH18 = HH18;
    }

    public int getHH19() {
        return HH19;
    }

    public void setHH19(int HH19) {
        this.HH19 = HH19;
    }

    public int getHH20() {
        return HH20;
    }

    public void setHH20(int HH20) {
        this.HH20 = HH20;
    }

    public int getHH21() {
        return HH21;
    }

    public void setHH21(int HH21) {
        this.HH21 = HH21;
    }

    public int getHH22() {
        return HH22;
    }

    public void setHH22(int HH22) {
        this.HH22 = HH22;
    }

    public int getHH23() {
        return HH23;
    }

    public void setHH23(int HH23) {
        this.HH23 = HH23;
    }

    @Override
    public boolean validate() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeElecModeData that = (ChangeElecModeData) o;
        return connectorId == that.connectorId &&
                HH00 == that.HH00 && HH01 == that.HH01 &&
                HH02 == that.HH02 && HH03 == that.HH03 &&
                HH02 == that.HH04 && HH03 == that.HH05 &&
                HH02 == that.HH06 && HH03 == that.HH07 &&
                HH02 == that.HH08 && HH03 == that.HH09 &&
                HH02 == that.HH10 && HH03 == that.HH11 &&
                HH02 == that.HH12 && HH03 == that.HH13 &&
                HH02 == that.HH14 && HH03 == that.HH15 &&
                HH02 == that.HH16 && HH03 == that.HH17 &&
                HH02 == that.HH18 && HH03 == that.HH19 &&
                HH02 == that.HH20 && HH03 == that.HH21 &&
                HH02 == that.HH22 && HH03 == that.HH23;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorId,
                HH00, HH01, HH02, HH03, HH04, HH05, HH06, HH07, HH08, HH09,
                HH10, HH11, HH12, HH13, HH14, HH15, HH16, HH17, HH18, HH19,
                HH20, HH21, HH22, HH23);
    }
}
