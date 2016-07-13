package io.vphone.vphonedispatcher;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by FerasWilson on 2016-07-13.
 */
public class VPhoneDao {
    // Database fields
    private SQLiteDatabase database;
    private VPhoneSQLiteHelper dbHelper;
    private String[] allColumns = { VPhoneSQLiteHelper.COLUMN_ID,
            VPhoneSQLiteHelper.COLUMN_BODY, VPhoneSQLiteHelper.COLUMN_FROM, VPhoneSQLiteHelper.COLUMN_TIMESTAMP };

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

        long insertId = database.insert(VPhoneSQLiteHelper.TABLE_COMMENTS, null,
                values);
        Cursor cursor = database.query(VPhoneSQLiteHelper.TABLE_COMMENTS,
                allColumns, VPhoneSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        VPhoneSMS newComment = cursorToComment(cursor);
        cursor.close();
        return newComment;
    }

    public void deleteComment(VPhoneSMS comment) {
        long id = comment.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(VPhoneSQLiteHelper.TABLE_COMMENTS, VPhoneSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<VPhoneSMS> getAllComments() {
        List<VPhoneSMS> comments = new ArrayList<VPhoneSMS>();

        Cursor cursor = database.query(VPhoneSQLiteHelper.TABLE_COMMENTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            VPhoneSMS comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    private VPhoneSMS cursorToComment(Cursor cursor) {
        VPhoneSMS comment = new VPhoneSMS();
        comment.setId(cursor.getLong(0));
        comment.setSmsbody(cursor.getString(1));
        comment.setSmsfrom(cursor.getString(2));
        comment.setSmstimestamp(cursor.getString(3));
        return comment;
    }
}
