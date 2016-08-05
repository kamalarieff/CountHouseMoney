package com.mudah.my.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mudah.my.configs.Config;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_ADVIEW_FAV = "adview_favourite";
    public static final String TABLE_ADVIEW_FAV_COLUMN_ID = "_id";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_ID = "ad_id";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_SUBJECT = "ad_subject";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_PRICE = "ad_price";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_TIME = "ad_time";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_IMG_URL = "ad_img_url";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_CATEGORY_ID = "ad_category_id";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_BODY = "ad_body";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_NAME = "ad_name";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_REGION = "ad_region";
    public static final String TABLE_ADVIEW_FAV_COLUMN_AD_PHONE = "ad_phone";
    public static final String TABLE_ADVIEW_FAV_COLUMN_CREATED_TIME = "created_time";
    public static final String TABLE_ADVIEW_FAV_COLUMN_COMPANY_AD = "company_ad";
    public static final String TABLE_ADVIEW_FAV_COLUMN_IMG_COUNT = "image_count";
    public static final String TABLE_PARAMETER = "parameters";
    public static final String TABLE_PARAMETER_COLUMN_AD_ID = "ad_id";
    public static final String TABLE_PARAMETER_COLUMN_PARAM_ID = "param_id";
    public static final String TABLE_PARAMETER_COLUMN_PARAM_VALUE = "param_value";
    public static final String TABLE_PARAMETER_COLUMN_PARAM_LABEL = "param_label";
    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String TABLE_BOOKMARKS_COLUMN_ID = "_id";
    public static final String TABLE_BOOKMARKS_COLUMN_NAME = "name";
    public static final String TABLE_BOOKMARKS_COLUMN_QUERY = "query";
    public static final String TABLE_BOOKMARKS_COLUMN_FILTER = "filter";
    public static final String TABLE_BOOKMARKS_COLUMN_CREATED_TIME = "created_time";
    public static final String TABLE_BOOKMARKS_COLUMN_UPDATED_TIME = "updated_time";
    public static final String TABLE_BOOKMARKS_COLUMN_LIST_IDS = "list_ids";
    private static final String DATABASE_NAME = "mudah.db";

    private static DatabaseHelper sInstance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, Config.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Database creation sql statement
        final String sqlCreateTableAdViewFav = "create table if not exists "
                + TABLE_ADVIEW_FAV + "("
                + TABLE_ADVIEW_FAV_COLUMN_ID + " integer primary key autoincrement, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_ID + " integer unique not null, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_SUBJECT + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_PRICE + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_TIME + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_IMG_URL + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_CATEGORY_ID + " integer, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_BODY + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_NAME + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_REGION + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_AD_PHONE + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_CREATED_TIME + " integer not null, "
                + TABLE_ADVIEW_FAV_COLUMN_COMPANY_AD + " text, "
                + TABLE_ADVIEW_FAV_COLUMN_IMG_COUNT + " integer "
                + ");";
        final String sqlCreateTableParameters = "create table if not exists "
                + TABLE_PARAMETER + "("
                + TABLE_PARAMETER_COLUMN_AD_ID + " integer, "
                + TABLE_PARAMETER_COLUMN_PARAM_ID + " text, "
                + TABLE_PARAMETER_COLUMN_PARAM_LABEL + " text, "
                + TABLE_PARAMETER_COLUMN_PARAM_VALUE + " text"
                + ");";
        final String sqlCreateTableBookmarks = "create table if not exists "
                + TABLE_BOOKMARKS + "("
                + TABLE_BOOKMARKS_COLUMN_ID + " integer primary key autoincrement, "
                + TABLE_BOOKMARKS_COLUMN_NAME + " text, "
                + TABLE_BOOKMARKS_COLUMN_QUERY + " text, "
                + TABLE_BOOKMARKS_COLUMN_FILTER + " text, "
                + TABLE_BOOKMARKS_COLUMN_CREATED_TIME + " integer, "
                + TABLE_BOOKMARKS_COLUMN_UPDATED_TIME + " integer, "
                + TABLE_BOOKMARKS_COLUMN_LIST_IDS + " text "
                + ");";
        db.execSQL(sqlCreateTableAdViewFav);
        db.execSQL(sqlCreateTableParameters);
        db.execSQL(sqlCreateTableBookmarks);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion);

        //Alter table rather than droping table to prevent data losing. Need to customize the code case by case
        switch (newVersion) {
            case 1:
                //Do nothing
            case 2:
                db.execSQL("ALTER TABLE " + TABLE_BOOKMARKS + " ADD COLUMN " + TABLE_BOOKMARKS_COLUMN_UPDATED_TIME + " integer ");
                db.execSQL("ALTER TABLE " + TABLE_BOOKMARKS + " ADD COLUMN " + TABLE_BOOKMARKS_COLUMN_LIST_IDS + " text ");
            default:
                break;
        }

        onCreate(db);

    }

}
