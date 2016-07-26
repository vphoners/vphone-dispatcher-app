package io.vphone.vphonedispatcher;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.json.JSONObject;

/**
 * Created by FerasWilson on 2016-07-13.
 */
public class VPhoneDao {
    public static final String DEVICE_KEY = "device";
    public static final String START_SERVICE = "start_service";
    // Database fields
    private SQLiteDatabase database;
    private VPhoneSQLiteHelper dbHelper;
    private String[] allColumns = {VPhoneSQLiteHelper.COLUMN_ID,
            VPhoneSQLiteHelper.COLUMN_BODY, VPhoneSQLiteHelper.COLUMN_FROM, VPhoneSQLiteHelper.COLUMN_TIMESTAMP};

    public VPhoneDao(Context context) {
        dbHelper = new VPhoneSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public VPhoneSMS createSms(String body, String from, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(VPhoneSQLiteHelper.COLUMN_BODY, body);
        values.put(VPhoneSQLiteHelper.COLUMN_FROM, from);
        values.put(VPhoneSQLiteHelper.COLUMN_TIMESTAMP, timestamp);

        long insertId = database.insert(VPhoneSQLiteHelper.TABLE_SMSES, null,
                values);
        Cursor cursor = database.query(VPhoneSQLiteHelper.TABLE_SMSES,
                allColumns, VPhoneSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        VPhoneSMS newSMS = cursorToSMS(cursor);
        cursor.close();
        return newSMS;
    }

    public void deleteSMS(VPhoneSMS SMS) {
        long id = SMS.getId();
        System.out.println("SMS deleted with id: " + id);
        database.delete(VPhoneSQLiteHelper.TABLE_SMSES, VPhoneSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<VPhoneSMS> getAllSMSs() {
        List<VPhoneSMS> SMSs = new ArrayList<VPhoneSMS>();

        Cursor cursor = database.query(VPhoneSQLiteHelper.TABLE_SMSES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            VPhoneSMS SMS = cursorToSMS(cursor);
            SMSs.add(SMS);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return SMSs;
    }

    private VPhoneSMS cursorToSMS(Cursor cursor) {
        VPhoneSMS SMS = new VPhoneSMS();
        SMS.setId(cursor.getLong(0));
        SMS.setSmsbody(cursor.getString(1));
        SMS.setSmsfrom(cursor.getString(2));
        SMS.setSmstimestamp(cursor.getString(3));
        return SMS;
    }

    public void updateSetting(String name, String value) {
        ContentValues cv = new ContentValues();
        SQLiteStatement stmt = database.compileStatement("REPLACE INTO " + VPhoneSQLiteHelper.TABLE_SETTINGS
                +" (" + VPhoneSQLiteHelper.COLUMN_NAME + " ,"
                + VPhoneSQLiteHelper.COLUMN_VALUE + ") VALUES (?,?)");
        stmt.bindString(1, name);
        stmt.bindString(2, value);
        stmt.execute();
    }

    public String getSetting(String name) {
        Cursor cursor = database.query(VPhoneSQLiteHelper.TABLE_SETTINGS,
                new String[]{VPhoneSQLiteHelper.COLUMN_VALUE},
                VPhoneSQLiteHelper.COLUMN_NAME + " = ?",
                new String[]{name},
                null,
                null,
                null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                return cursor.getString(0);
            }
            return null;
        }finally {
            // make sure to close the cursor
            cursor.close();
        }
    }

}
