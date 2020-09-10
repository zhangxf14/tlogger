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
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataPointsStorage extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "tlogger";
    private static final String TABLE_NAME = "DataPoints";
    private static final String KEY_ID = "id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";

    public DataPointsStorage(Context context) {
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

    public void addDataPoint(String tag, DataPoint dataPoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            try {
                createTable(db);
                ContentValues values = new ContentValues();
                values.put(KEY_TAG, tag);
                values.put(KEY_X, (long) dataPoint.getX());
                values.put(KEY_Y, dataPoint.getY());
                db.insert(TABLE_NAME, null, values);
            } catch (RuntimeException e) {
                Log.e("aDP", e.toString());
            } finally {
                db.close();
            }
        }
    }

    public void addDataPoints(String tag, List<DataPoint> dataPoints) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            try {
                createTable(db);
                for (DataPoint dataPoint : dataPoints) {
                    ContentValues values = new ContentValues();
                    values.put(KEY_TAG, tag);
                    values.put(KEY_X, (long) dataPoint.getX());
                    values.put(KEY_Y, dataPoint.getY());
                    db.insert(TABLE_NAME, null, values);
                }
            } catch (RuntimeException e) {
                Log.e("aDPs l", e.toString());
            }
            finally {
                db.close();
            }
        }
    }

    public void addDataPoints(String tag, Iterator<DataPoint> dataPoints) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            try {
                createTable(db);
                while (dataPoints.hasNext()) {
                    DataPoint dataPoint = dataPoints.next();
                    ContentValues values = new ContentValues();
                    values.put(KEY_TAG, tag);
                    values.put(KEY_X, (long) dataPoint.getX());
                    values.put(KEY_Y, dataPoint.getY());
                    db.insert(TABLE_NAME, null, values);
                }
            } catch (RuntimeException e) {
                Log.e("aDPs i", e.toString());
            }
            finally {
                db.close();
            }
        }
    }

    public int getDataCount(String tag) {
        String sql = String.format("SELECT COUNT(*) as C FROM %s WHERE %s='%s'", TABLE_NAME, KEY_TAG, tag);
        return queryIntValue(sql, "C");
    }

    public List<DataPoint> getDataPoints(String tag) {
        String sql = String.format("SELECT * FROM %s WHERE %s='%s' ORDER BY %s ASC", TABLE_NAME, KEY_TAG, tag, KEY_TAG);
        return queryDataPoints(sql);
    }

    public List<DataPoint> getDataPoints(String tag, double minX) {
        String sql = String.format("SELECT * FROM %s WHERE %s='%s' AND %s>='%f' ORDER BY %s ASC", TABLE_NAME, KEY_TAG, tag, KEY_X, minX, KEY_TAG);
        return queryDataPoints(sql);
    }

    public void deleteDataPoints(String tag) {
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

    private List<DataPoint> queryDataPoints(String sql) {
        List<DataPoint> dataPoints = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            try (Cursor cursor = db.rawQuery(sql, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        double x = Double.parseDouble(cursor.getString(cursor.getColumnIndex(KEY_X)));
                        double y = Double.parseDouble(cursor.getString(cursor.getColumnIndex(KEY_Y)));
                        DataPoint dataPoint = new DataPoint(x, y);
                        dataPoints.add(dataPoint);
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLiteException e) {
            // absorb
        }
        return dataPoints;
    }

    private int queryIntValue(String sql, String columnName) {
        int value = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            try (Cursor cursor = db.rawQuery(sql, null)) {
                if (cursor.moveToFirst()) {
                    value = cursor.getInt(cursor.getColumnIndex(columnName));
                }
            }
        } catch (SQLiteException e) {
            // absorb
        }
        return value;
    }

    private void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(" + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TAG + " TEXT,"
                + KEY_X + " INTEGER,"
                + KEY_Y + " INTEGER,"
                + " UNIQUE (" + KEY_TAG + ", " + KEY_X + ") ON CONFLICT REPLACE" + ")";
        db.execSQL(sql);
    }
}
