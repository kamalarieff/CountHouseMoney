package com.mudah.my.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.lib701.connection.ACRESTClientAuth;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.ListBookmarksActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.dao.BookmarksDAO;
import com.mudah.my.helpers.NotificationBuilderHelper;
import com.mudah.my.models.BookmarkNotificationModel;
import com.mudah.my.models.BookmarksModel;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class BookmarkNotificationReceiver extends BroadcastReceiver {

    Context context;

    public BookmarkNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();
        this.context = context;
        CallBookmarkAPITask task = new CallBookmarkAPITask();
        task.execute();
        return;
    }

    private void createNotification(Context context, String bookmarkName) {

        Fabric.with(context, new Crashlytics());

        if (XitiUtils.initFromLastConfig(context) == null) {
            ACUtils.sendFalseCrashLog("<BookmarkNotificationReceiver> Xiti initialization is wrong");
        }

        Log.d("BookmarkNotification Wakeup time!             " + MudahUtil.formatDate(Calendar.getInstance()));

        // Construct notification message
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(context);
        String name = pref.getString(PreferencesUtils.USER_NAME, null);
        BookmarkNotificationModel bookmarkConfig = BookmarkNotificationModel.newInstance(context.getApplicationContext());
        String bookmarkText = "";
        String bookmarkTextShort = bookmarkConfig.getMsgTextShort();
        String bookmarkTitle = bookmarkConfig.getMsgTitle();
        if (!ACUtils.isEmpty(name)) {
            if (!ACUtils.isEmpty(bookmarkName)) {
                bookmarkText = bookmarkConfig.getMsgTextWithNameBookmarkName().replace("{name}", name.toUpperCase()).replace("{bookmarkname}", bookmarkName);
            } else {
                bookmarkText = bookmarkConfig.getMsgTextWithName().replace("{name}", name.toUpperCase());
            }
        } else {
            if (!ACUtils.isEmpty(bookmarkName)) {
                bookmarkText = bookmarkConfig.getMsgTextWithBookmarkName().replace("{bookmarkname}", bookmarkName);
            } else {
                bookmarkText = bookmarkConfig.getMsgText();
            }
        }

        // Construct intent for click notification
        Intent notificationClickIntent = new Intent(context, ListBookmarksActivity.class);
        notificationClickIntent.setAction(Intent.ACTION_MAIN);
        notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationClickIntent.putExtra(Config.BOOKMARK_MAIN_NOTIFICATION, true);
        PendingIntent notificationClickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct intent for dismiss notification
        Intent notificationDeleteIntent = new Intent(context, BookmarkDismissNotificationReceiver.class);
        notificationDeleteIntent.setAction(Intent.ACTION_MAIN);
        notificationDeleteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent notificationDeletePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                notificationDeleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct notification
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        NotificationCompat.Builder notify = new NotificationCompat
                .Builder(context)
                .setWhen(Calendar.getInstance().getTimeInMillis())
                .setContentTitle(bookmarkTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bookmarkText))
                .setContentText(bookmarkTextShort)
                .setSmallIcon(R.drawable.notification_ticker)
                .setLargeIcon(largeIcon)
                .setTicker(bookmarkTitle)
                .setAutoCancel(true)
                .setContentIntent(notificationClickPendingIntent)
                .setDeleteIntent(notificationDeletePendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_BOOKMARK_ID, notify.build());

        EventTrackingUtils.sendCampaign(context, XitiUtils.CAMPAIGN_NOTIFICATION_BOOKMARK, XitiUtils.CAMPAIGN_IMPRESSION);
    }

    private class CallBookmarkAPITask extends AsyncTask<Void, Void, JSONObject> {
        JSONObject responseJSON;
        HashMap<Integer, Long> apiToBookmarkIdMapping = new HashMap<>();
        ArrayList<BookmarksModel> bookmarkArrayList;

        @Override
        protected JSONObject doInBackground(Void... params) {
            BookmarksDAO bookmarksDAO = new BookmarksDAO(context);
            bookmarkArrayList = bookmarksDAO.getAllBookmarks();
            boolean isFirstBookmark = true;
            String listLatestId = "";
            String listFilterQuery = "";
            for (int i = 0; i < bookmarkArrayList.size(); i++) {
                BookmarksModel bookmark = bookmarkArrayList.get(i);

                String latestId = "";
                String filterQuery = "";

                apiToBookmarkIdMapping.put(i, bookmark.getId());

                if (!ACUtils.isEmpty(bookmark.getListIds()) && !bookmark.getListIds().equalsIgnoreCase("null")) {
                    latestId = bookmark.getListIds();
                }
                if (!ACUtils.isEmpty(bookmark.getQuery()) && !bookmark.getQuery().equalsIgnoreCase("null")) {
                    filterQuery = bookmark.getQuery();
                }

                if (isFirstBookmark) {
                    listLatestId = latestId;
                    listFilterQuery = filterQuery;
                } else {
                    listLatestId = listLatestId + "|#" + latestId;
                    listFilterQuery = listFilterQuery + "|#" + filterQuery;
                }

                isFirstBookmark = false;
            }

            ACRESTClientAuth acRESTClientAuth = new ACRESTClientAuth();
            HashMap<String, Object> postParameters = new HashMap<String, Object>();
            Map<String, String> headers = new HashMap<String, String>();

            headers.put("Content-Type", "application/x-www-form-urlencoded");

            postParameters.put("list_latest_id", listLatestId);
            postParameters.put("list_filter_query", listFilterQuery);

            acRESTClientAuth.setMethod("POST");
            acRESTClientAuth.setResource("bookmark");
            acRESTClientAuth.setPostParameters(postParameters);
            acRESTClientAuth.setHeaders(headers);

            responseJSON = acRESTClientAuth.makeSynchronousRESTCallWithError();

            ACUtils.logCrashlytics("BookmarkNotification postParameters:" + postParameters);
            ACUtils.logCrashlytics("BookmarkNotification responseJSON:" + responseJSON);

            return responseJSON;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            boolean isNewAdsAvailable = false;
            boolean isOnlyOneBookmarkHaveUpdate = false;
            long theOnlyResultBookmarkId = -1;

            try {

                JSONObject totalAdsJSON = responseJSON.optJSONObject("total_ads");
                if (totalAdsJSON != null) {
                    Iterator<?> keys = totalAdsJSON.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        String value = totalAdsJSON.optString(key);
                        if (Integer.parseInt(value) > 0) {
                            isNewAdsAvailable = true;
                            if (!isOnlyOneBookmarkHaveUpdate && theOnlyResultBookmarkId == -1) {
                                isOnlyOneBookmarkHaveUpdate = true;
                                theOnlyResultBookmarkId = apiToBookmarkIdMapping.get(Integer.parseInt(key));
                            } else {
                                isOnlyOneBookmarkHaveUpdate = false;
                            }
                        }
                    }
                }

                String bookmarkName = "";
                if (isOnlyOneBookmarkHaveUpdate) {
                    for (BookmarksModel bookmark : bookmarkArrayList) {
                        if (bookmark.getId() == theOnlyResultBookmarkId) {
                            bookmarkName = bookmark.getName();
                        }
                    }
                }

                if (isNewAdsAvailable) {
                    createNotification(context, bookmarkName);
                    BookmarkNotificationModel bookmarkNotificationModel = BookmarkNotificationModel.newInstance(context);
                    NotificationBuilderHelper notificationBuilderHelper = new NotificationBuilderHelper();
                    notificationBuilderHelper.createBookmarkNotificationReminder(context, bookmarkNotificationModel);
                }
            }
            catch (Throwable e) {
                ACUtils.debug(e, "responseJSON", responseJSON+"");
            }
        }

    }

}
