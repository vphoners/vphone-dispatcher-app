package io.vphone.vphonedispatcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VPhoneSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_SMSES = "vphone_sms";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BODY = "smsbody";
    public static final String COLUMN_FROM = "smsfrom";
    public static final String COLUMN_TIMESTAMP = "smstimestamp";

    public static final String TABLE_SETTINGS = "vphone_settings";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VALUE = "value";


    private static final String DATABASE_NAME = "vphone.db";
    private static final int DATABASE_VERSION = 6;

    // Database creation sql statement
    private static final String SMS_TABLE_CREATE = "create table IF NOT EXISTS "
            + TABLE_SMSES + "( " + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_BODY + " text, "
            + COLUMN_FROM + " varchar(255),"
            + COLUMN_TIMESTAMP + " varchar(255)"
            + ");";
    private static final String SETTINGS_TABLE_CREATE = "create table "
            + TABLE_SETTINGS + "("
            + COLUMN_NAME + " varchar(255) primary key,"
            + COLUMN_VALUE + " varchar(255)"
            + ");";

    public VPhoneSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SMS_TABLE_CREATE);
        database.execSQL(SETTINGS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(VPhoneSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

}