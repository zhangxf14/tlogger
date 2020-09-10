/*
 * Copyright 2020 NXP.
 * This software is owned or controlled by NXP and may only be used
 * strictly in accordance with the applicable license terms.  By expressly
 * accepting such terms or by downloading, installing, activating and/or
 * otherwise using the software, you are agreeing that you have read, and
 * that you agree to comply with and are bound by, such license terms.  If
 * you do not agree to be bound by the applicable license terms, then you
 * may not retain, install, activate or otherwise use the software.
 */


package com.nxp.nhs31xx.demo.tlogger.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

public class ConfigStorage extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "tlogger";
    private static final String TABLE_NAME = "Config";
    private static final String KEY_ID = "id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_DATE = "date";
    private static final String KEY_COUNT = "count";

    public ConfigStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addConfig(String tag, Date date, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            try {
                createTable(db);
                ContentValues values = new ContentValues();
                values.put(KEY_TAG, tag);
                values.put(KEY_DATE, date.getTime());
                values.put(KEY_COUNT, count);
                db.insert(TABLE_NAME, null, values);
            } catch (SQLiteException e) {
                // absorb
            } finally {
                db.close();
            }
        }
    }

    public Date getDate(String tag) {
        Date date = new Date(System.currentTimeMillis());
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String sql = "SELECT " + KEY_DATE + " FROM " + TABLE_NAME + " WHERE " + KEY_TAG + "='" + tag + "'";
            try (Cursor cursor = db.rawQuery(sql, null)) {
                if (cursor.moveToFirst()) {
                    date = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATE)));
                }
            }
        } catch (SQLiteException e) {
            // absorb
        }
        return date;
    }

    public int getCount(String tag) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            try {
                String sql = "SELECT " + KEY_COUNT + " FROM " + TABLE_NAME + " WHERE " + KEY_TAG + "='" + tag + "'";
                try (Cursor cursor = db.rawQuery(sql, null)) {
                    if (cursor.moveToFirst()) {
                        count = (int) cursor.getLong(cursor.getColumnIndex(KEY_COUNT));
                    }
                }
            } catch (SQLiteException e) {
                // absorb
			}
        }
        return count;
    }

    public void deleteConfig(String tag) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            try {
                db.delete(TABLE_NAME, KEY_TAG + " = ?", new String[]{tag});
            } catch (SQLiteException e) {
                // absorb
            } finally {
                db.close();
            }
        }
    }

    private void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(" + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TAG + " TEXT,"
                + KEY_DATE + " INTEGER,"
                + KEY_COUNT + " INTEGER,"
                + "UNIQUE (" + KEY_TAG + ") ON CONFLICT REPLACE" + ")";
        db.execSQL(sql);
    }
}
