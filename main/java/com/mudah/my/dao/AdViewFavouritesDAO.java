package com.mudah.my.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.lib701.datasets.ACAd;
import com.lib701.datasets.ACAdParameter;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.helpers.DatabaseHelper;
import com.mudah.my.models.ListAdModel;

import java.util.ArrayList;

/**
 * No need to call for db.close()
 * Ref: http://stackoverflow.com/questions/6608498/best-place-to-close-database-connection
 * Android made a deliberate design decision that is can seem surprising, to just give up on the whole idea of applications cleanly exiting and instead let the kernel clean up their resources.
 * After all, the kernel needs to be able to do this anyway. Given that design, keeping anything open for the entire duration of a process's life and never closing it is simply not a leak.
 * It will be cleaned up when the process is cleaned up.
 */
public class AdViewFavouritesDAO {

    // Database fields
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public AdViewFavouritesDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public SparseBooleanArray getAllFavouritesID() throws SQLiteException {
        SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_ADVIEW_FAV,
                    new String[]{
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID,
                    }, null, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                sparseBooleanArray.put(cursor.getInt(0), true);
                cursor.moveToNext();
            }
            // Make sure to close the cursor
            cursor.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "AdViewFavDAO", "getAllFavouritesID()");
        }
        return sparseBooleanArray;
    }

    public ArrayList<ListAdModel> getAllFavourites() throws SQLiteException {

        ArrayList<ListAdModel> items = new ArrayList<ListAdModel>();
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_ADVIEW_FAV,
                    new String[]{
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_ID,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_SUBJECT,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_PRICE,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_TIME,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_IMG_URL,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_CATEGORY_ID,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_CREATED_TIME,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_COMPANY_AD,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_IMG_COUNT,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_BODY,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_NAME,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_REGION,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_PHONE,

                    }, null, null, null, null, DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_ID + " desc", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                items.add(cursorToAdViewFav(cursor));
                cursor.moveToNext();
            }
            // Make sure to close the cursor
            cursor.close();
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "AdViewFavDAO", "getAllFavourites()");
        }
        return items;
    }

    // Deleting single favourite
    public int deleteFavourite(long adId) {
        db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PARAMETER,
                DatabaseHelper.TABLE_PARAMETER_COLUMN_AD_ID + " = ?",
                new String[]{String.valueOf(adId)});
        int result = db.delete(DatabaseHelper.TABLE_ADVIEW_FAV,
                DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID + " = ?",
                new String[]{String.valueOf(adId)});
        //db.close();
        Log.d("deleted row(s): " + result);
        return result;
    }

    // Deleting Multiple favourites
    public int deleteMultipleFavourites(ArrayList<String> adIds) {
        String allAdIds = adIds.toString();//e.g. [1,2]
        allAdIds = allAdIds.substring(1, allAdIds.length() - 1);//Remove brackets []
        int result = 0;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete(DatabaseHelper.TABLE_PARAMETER,
                    String.format(DatabaseHelper.TABLE_PARAMETER_COLUMN_AD_ID + " IN (%s)", allAdIds),
                    new String[]{});
            result = db.delete(DatabaseHelper.TABLE_ADVIEW_FAV,
                    String.format(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID + " IN (%s)", allAdIds),
                    new String[]{});
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "AdViewFavDAO", "deleteMultipleFavourites() " + allAdIds);
        }
        Log.d("deleted row(s): " + result);
        return result;
    }

    // Deleting All favourites
    public int deleteAllFavourites() {
        int result = 0;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete(DatabaseHelper.TABLE_PARAMETER, null, new String[]{});
            result = db.delete(DatabaseHelper.TABLE_ADVIEW_FAV, null, new String[]{});
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "AdViewFavDAO", "deleteAllFavourites()");
        }
        Log.d("deleted row(s): " + result);
        return result;
    }

    //Insert multiple favourites
    public void insertMultipleAdViewFavourites(SparseArray<ACAd> acAdList) {
        int dataSize = acAdList.size();
        for (int i = 0; i < dataSize; i++) {
            insertAdViewFavourite(acAdList.valueAt(i));
        }
    }

    // Insert a single favourite
    public long insertAdViewFavourite(ACAd acAd) {
        if (acAd == null) {
            return 0l;
        }
        long result = 0l;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID, acAd.getListId());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_SUBJECT, acAd.getSubject());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_PRICE, acAd.getPrice());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_TIME, acAd.getRawDate());
            if (acAd.getImages() != null && acAd.getImages().size() > 0) {
                String imgUrl = acAd.getImages().get(0);
                imgUrl = imgUrl.replace("wm_images", "mob_thumbs_app");
                values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_IMG_URL, imgUrl);
            }

            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_CATEGORY_ID, acAd.getCategoryId());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_BODY, acAd.getBody());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_NAME, acAd.getName());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_REGION, acAd.getRegion());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_PHONE, acAd.getPhone());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_CREATED_TIME, System.currentTimeMillis());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_COMPANY_AD, acAd.getCompanyAd());
            values.put(DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_IMG_COUNT, acAd.getImageCount());
            try {
                result = db.insert(DatabaseHelper.TABLE_ADVIEW_FAV, null, values);
                if (result > 0) {
                    for (ACAdParameter acParam : acAd.getParameters()) {
                        ContentValues params = new ContentValues();
                        params.put(DatabaseHelper.TABLE_PARAMETER_COLUMN_AD_ID, acAd.getListId());
                        params.put(DatabaseHelper.TABLE_PARAMETER_COLUMN_PARAM_ID, acParam.getId());
                        params.put(DatabaseHelper.TABLE_PARAMETER_COLUMN_PARAM_VALUE, acParam.getValue());
                        params.put(DatabaseHelper.TABLE_PARAMETER_COLUMN_PARAM_LABEL, acParam.getLabel());
                        db.insert(DatabaseHelper.TABLE_PARAMETER, null, params);
                    }
                }

            } catch (SQLiteConstraintException e) {
                ACUtils.debug(e);
            }

            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "AdViewFavDAO", "insertAdViewFavourite()");
        }
        Log.d("result = " + result);
        return result;
    }

    // Detect an ad is existed in database
    public boolean isInserted(long adId) {
        boolean result = false;
        try {
            db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(DatabaseHelper.TABLE_ADVIEW_FAV,
                    new String[]{DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_ID},
                    DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID + "='" + adId + "'",
                    null, null, null, null, null);
            cursor.moveToFirst();
            result = !cursor.isAfterLast();
            // Make sure to close the cursor
            cursor.close();
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "AdViewFavDAO", "isInserted " + adId);
        }
        return result;
    }

    public int total() {
        int result = 0;
        try {
            db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(DatabaseHelper.TABLE_ADVIEW_FAV,
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
            ACUtils.debug(exception, "AdViewFavDAO", "total()");
        }
        return result;
    }

    /**
     * Get Ad from database from it's id
     *
     * @param adId
     * @return ACAd Object
     */
    public ACAd getAd(int adId) {
        ACAd adItem = null;
        try {
            db = dbHelper.getReadableDatabase();

            Cursor adCursor = db.query(DatabaseHelper.TABLE_ADVIEW_FAV,
                    new String[]{
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_SUBJECT,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_PRICE,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_TIME,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_CATEGORY_ID,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_BODY,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_NAME,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_REGION,
                            DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_PHONE
                    }, DatabaseHelper.TABLE_ADVIEW_FAV_COLUMN_AD_ID + "='" + adId + "'",
                    null, null, null, null, null);
            adCursor.moveToFirst();
            if (!adCursor.isAfterLast()) {
                int i = 0;
                adItem = new ACAd();
                adItem.setListId(adCursor.getInt(i++));
                adItem.setSubject(adCursor.getString(i++));
                adItem.setPrice(adCursor.getString(i++));
                adItem.setDate(adCursor.getString(i++));
                adItem.setCategoryId(adCursor.getString(i++));
                adItem.setBody(adCursor.getString(i++));
                adItem.setName(adCursor.getString(i++));
                adItem.setRegion(adCursor.getString(i++));
                adItem.setPhone(adCursor.getString(i++));

                Cursor paramCursor = db.query(DatabaseHelper.TABLE_PARAMETER,
                        new String[]{
                                DatabaseHelper.TABLE_PARAMETER_COLUMN_PARAM_ID,
                                DatabaseHelper.TABLE_PARAMETER_COLUMN_PARAM_VALUE,
                                DatabaseHelper.TABLE_PARAMETER_COLUMN_PARAM_LABEL
                        }, DatabaseHelper.TABLE_PARAMETER_COLUMN_AD_ID + "='" + adId + "'",
                        null, null, null, null, null);
                ArrayList<ACAdParameter> params = new ArrayList<ACAdParameter>();
                paramCursor.moveToFirst();
                int j;
                while (!paramCursor.isAfterLast()) {
                    ACAdParameter param = new ACAdParameter();
                    j = 0;
                    param.setId(paramCursor.getString(j++));
                    param.setValue(paramCursor.getString(j++));
                    param.setLabel(paramCursor.getString(j++));

                    params.add(param);
                }
                paramCursor.close();
                adItem.setParameters(params);
            }
            // Make sure to close the adCursor
            adCursor.close();
            //db.close();
        } catch (SQLiteException exception) {
            ACUtils.debug(exception, "AdViewFavDAO", "getAd() " + adId);
        }
        return adItem;
    }

    private ListAdModel cursorToAdViewFav(Cursor cursor) {

        int i = 0;
        ListAdModel adViewFavourite = new ListAdModel();
        adViewFavourite.setId(cursor.getLong(i++));
        adViewFavourite.setAdId(cursor.getString(i++));
        adViewFavourite.setAdSubject(cursor.getString(i++));
        adViewFavourite.setAdPrice(cursor.getString(i++));
        adViewFavourite.setAdTime(cursor.getString(i++));
        adViewFavourite.setAdImgUrl(cursor.getString(i++));
        adViewFavourite.setAdCategoryId(cursor.getInt(i++));
        adViewFavourite.setCreatedTime(cursor.getLong(i++));
        adViewFavourite.setCompanyAd(cursor.getString(i++));
        adViewFavourite.setImgCount(cursor.getInt(i++));
        return adViewFavourite;

    }
}
