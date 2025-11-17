package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

import com.dongah.dispenser.websocket.ocpp.common.model.Validatable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitPriceConfirmData implements Validatable {

    private static final Logger logger = LoggerFactory.getLogger(UnitPriceConfirmData.class);

    private float unitPrice;       // 판매단가[소수점 2자리까지]
    private String userTypeCd;      // 회원 구분(C:법인, K:환경부, M:회원, N:비회원)
    private float crtrUnitPrice;   // 한전 계약단가[소수점 2자리까지]
    private String rechgType;       // 충전기 구분(DC:급속, AC:완속)

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUserTypeCd() {
        return userTypeCd;
    }

    public void setUserTypeCd(String userTypeCd) {
        this.userTypeCd = userTypeCd;
    }

    public float getCrtrUnitPrice() {
        return crtrUnitPrice;
    }

    public void setCrtrUnitPrice(float crtrUnitPrice) {
        this.crtrUnitPrice = crtrUnitPrice;
    }

    public String getRechgType() {
        return rechgType;
    }

    public void setRechgType(String rechgType) {
        this.rechgType = rechgType;
    }

    @Override
    public boolean validate() {
        return true;
    }
}
