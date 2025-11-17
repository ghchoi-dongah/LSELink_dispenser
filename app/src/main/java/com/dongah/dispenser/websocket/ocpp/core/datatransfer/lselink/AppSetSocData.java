package com.dongah.dispenser.websocket.ocpp.core.datatransfer.lselink;

import com.dongah.dispenser.websocket.ocpp.common.model.Validatable;

import java.util.Objects;

public class AppSetSocData implements Validatable {

    private int connectorId;    // Connector ID
    private String idTag;       // 인증식별자
    private int setSoc;         // 설정 soc

    public AppSetSocData() {}

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

    public int getSetSoc() {
        return setSoc;
    }

    public void setSetSoc(int setSoc) {
        this.setSoc = setSoc;
    }

    @Override
    public boolean validate() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppSetSocData that = (AppSetSocData) o;
        return connectorId == that.connectorId &&
                Objects.equals(idTag, that.idTag) &&
                setSoc == that.setSoc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorId, idTag, setSoc);
    }
}
