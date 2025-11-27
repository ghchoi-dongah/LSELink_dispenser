package com.dongah.dispenser.sqlite.dto;

import android.content.ContentValues;

public class CpChangeMode implements DbEntity {
    private static final String tableName = "CP_CHANGE_MODE";
    private static final String ID = "ID";
    private static final String CHARGER_MODE = "CHARGER_MODE";
    private static final String TIME = "TIME";
    private static final String CONNECTOR_ID = "CONNECTOR_ID";
    private static final String REG_DT = "REG_DT";
    public static final String CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CONNECTOR_ID + " INTEGER NOT NULL," +
                    CHARGER_MODE + " TEXT," +
                    TIME + " TEXT NOT NULL," +
                    REG_DT  + " TEXT NOT NULL" +
                    ");";

    public String chargerMode;
    public String time;
    public Integer connectorId;
    public String regDt;

    public CpChangeMode() {}

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(CHARGER_MODE, chargerMode);
        values.put(TIME, time);
        values.put(CONNECTOR_ID, connectorId);
        values.put(REG_DT, regDt);
        return values;
    }
}
