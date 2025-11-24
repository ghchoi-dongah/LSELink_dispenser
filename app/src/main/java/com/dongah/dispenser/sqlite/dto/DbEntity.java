package com.dongah.dispenser.sqlite.dto;

import android.content.ContentValues;

public interface DbEntity {
    String getTableName();
    ContentValues toContentValues();
}
