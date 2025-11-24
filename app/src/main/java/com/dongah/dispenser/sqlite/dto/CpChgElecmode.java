package com.dongah.dispenser.sqlite.dto;

import android.content.ContentValues;

public class CpChgElecmode implements DbEntity {
    private static final String tableName = "CP_CHG_ELECMODE";
    private static final String ID = "ID";
    private static final String TIME = "TIME";
    private static final String RECHG_ELEC = "RECHG_ELEC";
    private static final String CONNECTOR_ID = "CONNECTOR_ID";
    private static final String REG_DT = "REG_DT";
    public static final String CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TIME + " TEXT NOT NULL," +
                    RECHG_ELEC + " TEXT NOT NULL," +
                    CONNECTOR_ID + " INTEGER NOT NULL," +
                    REG_DT  + " TEXT NOT NULL" +
                    ");";

    public String time;
    public String rechgElec;
    public Integer connectorId;
    public String regDt;

    public CpChgElecmode() {}

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(TIME, time);
        values.put(RECHG_ELEC, rechgElec);
        values.put(CONNECTOR_ID, connectorId);
        values.put(REG_DT, regDt);
        return values;
    }
}
