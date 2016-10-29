package io.vphone.vphonedispatcher;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

public class VPhoneDao {
    private static VPhoneDao sInstance;
    // Database fields
    private SQLiteDatabase database;
    private VPhoneSQLiteHelper dbHelper;
    private String[] allColumns = {VPhoneSQLiteHelper.COLUMN_ID,
            VPhoneSQLiteHelper.COLUMN_BODY, VPhoneSQLiteHelper.COLUMN_FROM, VPhoneSQLiteHelper.COLUMN_TIMESTAMP};

    public static synchronized VPhoneDao getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new VPhoneDao(context.getApplicationContext());
            sInstance.open();
        }
        return sInstance;
    }
    private VPhoneDao(Context context) {
        dbHelper = new VPhoneSQLiteHelper(context);
    }

    private void open() throws SQLException {
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
        Log.v("vphone", "SMS deleted with id: " + id);
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
}
