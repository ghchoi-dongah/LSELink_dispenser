package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

import com.dongah.dispenser.websocket.ocpp.common.model.Validatable;

import java.util.Objects;

public class PaymentInfoData implements Validatable {

    private String tid;         // 결제승인관리번호
    private String trantype;    // 요청코드(승인 0210, 부분취소 0431, 전체취소 0430)
    private String errcode;     // 0000(정상승인), 실패시 PG에서 받은 코드 입력
    private String cardno;      // 카드번호(마스킹처리)
    private Integer halbu;          // 할부개월
    private Integer tamt;           // 결제금액
    private String trandate;    // 승인일자
    private String trantime;    // 승인시간
    private String authno;      // 승인번호
    private String merno;       // 가맹점번호
    private String tran_serial; // 가맹점일련번호
    private String stlinst;     // 발급사명
    private String reqinst;     // 매입사명
    private String signpath;    // 서명
    private String msg1;        // 승인 메시지
    private String msg2;
    private String msg3;
    private String msg4;        // 실패내역

    public PaymentInfoData() {}

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTrantype() {
        return trantype;
    }

    public void setTrantype(String trantype) {
        this.trantype = trantype;
    }

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getCardno() {
        return cardno;
    }

    public void setCardno(String cardno) {
        this.cardno = cardno;
    }

    public Integer getHalbu() {
        return halbu;
    }

    public void setHalbu(Integer halbu) {
        this.halbu = halbu;
    }

    public Integer getTamt() {
        return tamt;
    }

    public void setTamt(Integer tamt) {
        this.tamt = tamt;
    }

    public String getTrandate() {
        return trandate;
    }

    public void setTrandate(String trandate) {
        this.trandate = trandate;
    }

    public String getTrantime() {
        return trantime;
    }

    public void setTrantime(String trantime) {
        this.trantime = trantime;
    }

    public String getAuthno() {
        return authno;
    }

    public void setAuthno(String authno) {
        this.authno = authno;
    }

    public String getMerno() {
        return merno;
    }

    public void setMerno(String merno) {
        this.merno = merno;
    }

    public String getTran_serial() {
        return tran_serial;
    }

    public void setTran_serial(String tran_serial) {
        this.tran_serial = tran_serial;
    }

    public String getStlinst() {
        return stlinst;
    }

    public void setStlinst(String stlinst) {
        this.stlinst = stlinst;
    }

    public String getReqinst() {
        return reqinst;
    }

    public void setReqinst(String reqinst) {
        this.reqinst = reqinst;
    }

    public String getSignpath() {
        return signpath;
    }

    public void setSignpath(String signpath) {
        this.signpath = signpath;
    }

    public String getMsg1() {
        return msg1;
    }

    public void setMsg1(String msg1) {
        this.msg1 = msg1;
    }

    public String getMsg2() {
        return msg2;
    }

    public void setMsg2(String msg2) {
        this.msg2 = msg2;
    }

    public String getMsg3() {
        return msg3;
    }

    public void setMsg3(String msg3) {
        this.msg3 = msg3;
    }

    public String getMsg4() {
        return msg4;
    }

    public void setMsg4(String msg4) {
        this.msg4 = msg4;
    }

    @Override
    public boolean validate() {
        return tid != null && trantype != null && errcode != null &&
                cardno != null && halbu != null && tamt != null &&
                trandate != null && trantime != null && authno != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentInfoData that = (PaymentInfoData) o;
        return Objects.equals(tid, that.tid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid, trantype, errcode, cardno, halbu,
                tamt, trandate, trantime, authno, merno, tran_serial,
                stlinst, reqinst, signpath, msg1, msg2, msg3, msg4);
    }
}
