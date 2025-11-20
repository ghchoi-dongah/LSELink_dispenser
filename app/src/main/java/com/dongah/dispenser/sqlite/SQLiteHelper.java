package com.dongah.dispenser.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dongah.dispenser.sqlite.dto.CpChangeMode;
import com.dongah.dispenser.sqlite.dto.CpChargingHist;
import com.dongah.dispenser.sqlite.dto.CpChgElecmode;
import com.dongah.dispenser.sqlite.dto.CpNonTransmit;
import com.dongah.dispenser.sqlite.dto.CpOcppConfigKeys;
import com.dongah.dispenser.sqlite.dto.CpSettings;
import com.dongah.dispenser.sqlite.dto.CpUnitPrice;
import com.dongah.dispenser.sqlite.dto.DbEntity;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dongah.db";
    private static final  int DATABASE_VERSION = 1;

    private static SQLiteHelper instance;

    public static SQLiteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /** SQLite Type
         * NULL: NULL값
         * INTEGER: 정수형(boolean: 1 or 0)
         * REAL: 실수형
         * TEXT: 문자열
         * BLOB: 바이너리
         * */
        sqLiteDatabase.execSQL(CpSettings.CREATE_SQL);
        sqLiteDatabase.execSQL(CpOcppConfigKeys.CREATE_SQL);
        sqLiteDatabase.execSQL(CpUnitPrice.CREATE_SQL);
        sqLiteDatabase.execSQL(CpChgElecmode.CREATE_SQL);
        sqLiteDatabase.execSQL(CpChangeMode.CREATE_SQL);
        sqLiteDatabase.execSQL(CpNonTransmit.CREATE_SQL);
        sqLiteDatabase.execSQL(CpChargingHist.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE if exists mytable";
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }

    // 공통 insert
    public long insert(DbEntity entity) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(entity.getTableName(), null, entity.toContentValues());
    }

    // delete all tables
    public void dropAllTables(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + new CpSettings().getTableName());
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + new CpOcppConfigKeys().getTableName());
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + new CpUnitPrice().getTableName());
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + new CpChgElecmode().getTableName());
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + new CpChangeMode().getTableName());
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + new CpNonTransmit().getTableName());
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + new CpChargingHist().getTableName());
    }

    // delete table
    public void dropTable(SQLiteDatabase sqLiteDatabase, String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) return;
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableName);
    }


}
