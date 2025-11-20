package com.dongah.dispenser.sqlite.dto;

import android.content.ContentValues;

public class CpNonTransmit implements DbEntity {
    private static final String tableName = "CP_NON_TRANSMIT";
    private static final String ID = "ID";
    private static final String UUID = "UUID";
    private static final String ACTIONS = "ACTIONS";
    private static final String PAYLOAD = "PAYLOAD";
    private static final String REG_DT = "REG_DT";
    private static final String RETRANSMITT_YN = "RETRANSMITT_YN";
    public static final String CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    UUID + " TEXT NOT NULL," +
                    ACTIONS + " TEXT NOT NULL," +
                    PAYLOAD + " TEXT NOT NULL," +
                    REG_DT + " TEXT NOT NULL," +
                    RETRANSMITT_YN  + " TEXT NOT NULL" +
                    ");";

    public String uuid;
    public String actions;
    public String payload;
    public String regDt;
    public String retransmitYn;

    public CpNonTransmit() {}

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(UUID, uuid);
        values.put(ACTIONS, actions);
        values.put(PAYLOAD, payload);
        values.put(REG_DT, regDt);
        values.put(RETRANSMITT_YN, retransmitYn);
        return values;
    }
}
