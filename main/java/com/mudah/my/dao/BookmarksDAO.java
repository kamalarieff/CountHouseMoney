package com.mudah.my.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.DatabaseHelper;
import com.mudah.my.models.BookmarksModel;

import java.util.ArrayList;

/**
 * No need to call for db.close()
 * Ref: http://stackoverflow.com/questions/6608498/best-place-to-close-database-connection
 * Android made a deliberate design decision that is can seem surprising, to just give up on the whole idea of applications cleanly exiting and instead let the kernel clean up their resources.
 * After all, the kernel needs to be able to do this anyway. Given that design, keeping anything open for the entire duration of a process's life and never closing it is simply not a leak.
 * It will be cleaned up when the process is cleaned up.
 */
public class BookmarksDAO {
    public static final String BOOKMARK_ID = "bookmark_id";

    // Database fields
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    /**
     *
     * @param context
     */
    public BookmarksDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public ArrayList<BookmarksModel> getAllBookmarks() throws SQLiteException {

        ArrayList<BookmarksModel> items = new ArrayList<BookmarksModel>();
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKMARKS,
                    new String[]{
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_ID,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_NAME,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_QUERY,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_FILTER,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_CREATED_TIME,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_UPDATED_TIME,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_LIST_IDS
                    }, null, null, null, null, DatabaseHelper.TABLE_BOOKMARKS_COLUMN_ID + " desc", null);

            cursor.moveToFirst();
            BookmarksModel tmpBookmarks;
            while (!cursor.isAfterLast()) {
                tmpBookmarks = cursorToBookmarks(cursor);
                items.add(tmpBookmarks);
                cursor.moveToNext();
            }
            // Make sure to close the cursor
            cursor.close();
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "getAllBookmarks()");
        }
        return items;
    }

    // Deleting single Bookmark
    public int deleteBookmarks(long id) {
        int result = 0;
        try {
            db = dbHelper.getWritableDatabase();
            result = db.delete(DatabaseHelper.TABLE_BOOKMARKS,
                    DatabaseHelper.TABLE_BOOKMARKS_COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});

            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "deleteBookmarks() " + id);
        }
        Log.d("deleted row(s): " + result);
        return result;
    }

    // Deleting multiple Bookmarks
    public int deleteMultipleBookmarks(ArrayList<Long> adIds) {
        String allAdIds = adIds.toString();//e.g. [1,2]
        allAdIds = allAdIds.substring(1, allAdIds.length() - 1);//Remove brackets []
        int result = 0;
        try {
            db = dbHelper.getWritableDatabase();
            result = db.delete(DatabaseHelper.TABLE_BOOKMARKS,
                    String.format(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_ID + " IN (%s)", allAdIds),
                    new String[]{});

            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "deleteMultipleBookmarks()");
        }
        Log.d("deleted row(s): " + result);
        return result;
    }

    // Deleting All Bookmarks
    public int deleteAllBookmarks() {
        int result = 0;
        try {
            db = dbHelper.getWritableDatabase();
            result = db.delete(DatabaseHelper.TABLE_BOOKMARKS, null, new String[]{});

            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "deleteAllBookmarks()");
        }
        Log.d("deleted row(s): " + result);
        return result;
    }

    // Insert a partial single ad_watch
    public long insertBookmarks(String name, String query, String filter) {
        return insertBookmarks(name, query, filter, Constants.EMPTY_STRING);
    }

    // Insert a single ad_watch
    public long insertBookmarks(String name, String query, String filter, String listIds) {

        long result = 0l;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_NAME, name);
            values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_QUERY, query);
            values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_FILTER, filter);
            values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_CREATED_TIME, System.currentTimeMillis());
            if (!ACUtils.isEmpty(listIds)) {
                values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_LIST_IDS, listIds);
                values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_UPDATED_TIME, System.currentTimeMillis());
            }

            try {
                result = db.insert(DatabaseHelper.TABLE_BOOKMARKS, null, values);
            } catch (SQLiteConstraintException e) {
                ACUtils.debug(e);
            }

            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "insertBookmarks() " + query + "," + filter);
        }
        Log.d("insert result = " + result);
        return result;
    }

    // Update an ad_watch's latest ad_ids
    public long updateAdIdOfBookmark(long id, String arrayListId[]) {

        String strAdIds = Constants.EMPTY_STRING;
        if (arrayListId.length > 0) {
            strAdIds = TextUtils.join(",", arrayListId);
        }

        long result = 0l;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_LIST_IDS, strAdIds);
            values.put(DatabaseHelper.TABLE_BOOKMARKS_COLUMN_UPDATED_TIME, System.currentTimeMillis());

            try {
                result = db.update(DatabaseHelper.TABLE_BOOKMARKS, values, DatabaseHelper.TABLE_BOOKMARKS_COLUMN_ID + "=" + id, null);
            } catch (SQLiteConstraintException e) {
                ACUtils.debug(e);
            }

            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "updateAdIdOfBookmark()");
        }
        Log.d("update result = " + result);
        return result;
    }

    public int total() {
        int result = 0;
        try {
            db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKMARKS,
                    new String[]{"count(*)"},
                    null, null, null, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                result = cursor.getInt(0);
            }
            // Make sure to close the cursor
            cursor.close();
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "total()");
        }
        return result;
    }

    public BookmarksModel getAd(int adId) {
        BookmarksModel bookmarks = null;
        try {
            db = dbHelper.getReadableDatabase();

            Cursor adCursor = db.query(DatabaseHelper.TABLE_BOOKMARKS,
                    new String[]{
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_ID,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_NAME,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_QUERY,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_FILTER,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_CREATED_TIME,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_UPDATED_TIME,
                            DatabaseHelper.TABLE_BOOKMARKS_COLUMN_LIST_IDS
                    }, DatabaseHelper.TABLE_BOOKMARKS_COLUMN_ID + "='" + adId + "'",
                    null, null, null, null, null);
            adCursor.moveToFirst();
            if (!adCursor.isAfterLast()) {
                int i = 0;
                bookmarks = new BookmarksModel(adCursor.getLong(i++),
                        adCursor.getString(i++),
                        adCursor.getString(i++),
                        adCursor.getString(i++),
                        adCursor.getLong(i++)
                );
            }
            // Make sure to close the adCursor
            adCursor.close();
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "BookmarksDAO", "getAd() " + adId);
        }
        return bookmarks;
    }

    private BookmarksModel cursorToBookmarks(Cursor cursor) {

        int i = 0;
        BookmarksModel bookmarks = new BookmarksModel(
                cursor.getLong(i++),
                cursor.getString(i++),
                cursor.getString(i++),
                cursor.getString(i++),
                cursor.getLong(i++),
                cursor.getLong(i++),
                cursor.getString(i++)
        );
        return bookmarks;

    }
}
