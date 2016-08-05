package com.mudah.my.models;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.MalformedJsonException;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.utils.MudahPreferencesUtils;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONObject;

/**
 * Created by w_ongkl on 02/10/15.
 */
public class BookmarkNotificationModel {

    public static final int UNSET = -1;
    public static final String DEFAULT_CONFIG = "" +
            "{" +
            "main_enable: false," +
            "main_timer: \"604800\"," +
            "notification_timing_hour: \"20\"," +
            "notification_timing_minute: \"00\"," +
            "notification_timing_second: \"00\"," +
            "msg_title: \"Check your Bookmarked Searches!\"," +
            "msg_text_short: \"You have new stuff in some of your Bookmarked Searches!\"," +
            "msg_text: \"Check your Bookmarked Searches. People are selling what you want!\"," +
            "msg_text_with_name: \"Hi {name}, check your Bookmarked Searches. People are selling what you want!\"," +
            "msg_text_with_bookmarkname: \"Check your {bookmarkname} bookmark. People are selling what you want!\"," +
            "msg_text_with_name_bookmarkname: \"Hi {name}, check your {bookmarkname} bookmark. People are selling what you want!\"" +
            "}";
    @SerializedName("bookmarkNotificationEnable")
    private static boolean bookmarkNotificationEnable = false;
    @SerializedName("bookmarkNotificationTimer")
    private static long bookmarkNotificationTimer = 604800;
    @SerializedName("hourBookmarkNotification")
    private int hourBookmarkNotification = UNSET; // In 24 hrs notation
    @SerializedName("minuteBookmarkNotification")
    private int minuteBookmarkNotification = UNSET;
    @SerializedName("secondBookmarkNotification")
    private int secondBookmarkNotification = UNSET;
    @SerializedName("msgTitle")
    private String msgTitle = "Check your Bookmarked Searches!";
    @SerializedName("msgTextShort")
    private String msgTextShort = "You have new stuff in some of your Bookmarked Searches!";
    @SerializedName("msgText")
    private String msgText = "Check your Bookmarked Searches. People are selling what you want!";
    @SerializedName("msgTextWithName")
    private String msgTextWithName = "Hi {name}, check your Bookmarked Searches. People are selling what you want!";
    @SerializedName("msgTextWithBookmarkName")
    private String msgTextWithBookmarkName = "Check your {bookmarkname} bookmark. People are selling what you want!";
    @SerializedName("msgTextWithNameBookmarkName")
    private String msgTextWithNameBookmarkName = "Hi {name}, check your {bookmarkname} bookmark. People are selling what you want!";

    public BookmarkNotificationModel() {

    }

    public static BookmarkNotificationModel newInstance(Context context) {
        BookmarkNotificationModel bookmarkNotificationConf = new BookmarkNotificationModel();
        try {
            bookmarkNotificationConf = MudahUtil
                    .retrieveClassInSharedPreferences(
                            context,
                            MudahPreferencesUtils.BOOKMARK_NOTIFICATION_CONFIG,
                            BookmarkNotificationModel.class,
                            DEFAULT_CONFIG);

        } catch (MalformedJsonException e) {
            ACUtils.debug(e);
        } catch (JsonSyntaxException e) {
            ACUtils.debug(e);
        }
        return bookmarkNotificationConf;
    }

    public void saveBookmarkNotificationConfig(JSONObject jsonObjBookmark, Context context) {

        if (jsonObjBookmark.has("main_enable")) {
            bookmarkNotificationEnable = jsonObjBookmark.optBoolean("main_enable");
        }

        if (jsonObjBookmark.has("main_timer")) {
            bookmarkNotificationTimer = jsonObjBookmark.optLong("main_timer");
        }

        if (jsonObjBookmark.has("notification_timing_hour") && !ACUtils.isEmpty(jsonObjBookmark.optString("notification_timing_hour"))) {
            hourBookmarkNotification = jsonObjBookmark.optInt("notification_timing_hour");
        } else {
            hourBookmarkNotification = UNSET;
        }

        if (jsonObjBookmark.has("notification_timing_minute") && !ACUtils.isEmpty(jsonObjBookmark.optString("notification_timing_minute"))) {
            minuteBookmarkNotification = jsonObjBookmark.optInt("notification_timing_minute");
        } else {
            minuteBookmarkNotification = UNSET;
        }

        if (jsonObjBookmark.has("notification_timing_second") && !ACUtils.isEmpty(jsonObjBookmark.optString("notification_timing_second"))) {
            secondBookmarkNotification = jsonObjBookmark.optInt("notification_timing_second");
        } else {
            secondBookmarkNotification = UNSET;
        }

        if (jsonObjBookmark.has("msg_title")) {
            msgTitle = jsonObjBookmark.optString("msg_title");
        }

        if (jsonObjBookmark.has("msg_text_short")) {
            msgTextShort = jsonObjBookmark.optString("msg_text_short");
        }

        if (jsonObjBookmark.has("msg_text")) {
            msgText = jsonObjBookmark.optString("msg_text");
        }

        if (jsonObjBookmark.has("msg_text_with_name")) {
            msgTextWithName = jsonObjBookmark.optString("msg_text_with_name");
        }

        if (jsonObjBookmark.has("msg_text_with_bookmarkname")) {
            msgTextWithBookmarkName = jsonObjBookmark.optString("msg_text_with_bookmarkname");
        }

        if (jsonObjBookmark.has("msg_text_with_name_bookmarkname")) {
            msgTextWithNameBookmarkName = jsonObjBookmark.optString("msg_text_with_name_bookmarkname");
        }

        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.BOOKMARK_NOTIFICATION_CONFIG, this);
    }

    public boolean isBookmarkNotificationEnable() {
        return bookmarkNotificationEnable;
    }

    public long getBookmarkNotificationTimer() {
        return bookmarkNotificationTimer;
    }

    public int getHourBookmarkNotification() {
        return hourBookmarkNotification;
    }

    public int getMinuteBookmarkNotification() {
        return minuteBookmarkNotification;
    }

    public int getSecondBookmarkNotification() {
        return secondBookmarkNotification;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public String getMsgTextShort() {
        return msgTextShort;
    }

    public String getMsgText() {
        return msgText;
    }

    public String getMsgTextWithName() {
        return msgTextWithName;
    }

    public String getMsgTextWithBookmarkName() {
        return msgTextWithBookmarkName;
    }

    public String getMsgTextWithNameBookmarkName() {
        return msgTextWithNameBookmarkName;
    }

    public void listConfig() {
        Log.d("BookmarkNotification  bookmarkNotificationEnable:" + isBookmarkNotificationEnable());
        Log.d("BookmarkNotification          bookmarkCheckTimer:" + getBookmarkNotificationTimer());
        Log.d("BookmarkNotification    hourBookmarkNotification:" + getHourBookmarkNotification());
        Log.d("BookmarkNotification  minuteBookmarkNotification:" + getMinuteBookmarkNotification());
        Log.d("BookmarkNotification  secondBookmarkNotification:" + getSecondBookmarkNotification());
        Log.d("BookmarkNotification                    msgTitle:" + getMsgTitle());
        Log.d("BookmarkNotification                msgTextShort:" + getMsgTextShort());
        Log.d("BookmarkNotification                     msgText:" + getMsgText());
        Log.d("BookmarkNotification             msgTextWithName:" + getMsgTextWithName());
        Log.d("BookmarkNotification     msgTextWithBookmarkName:" + getMsgTextWithBookmarkName());
        Log.d("BookmarkNotification msgTextWithNameBookmarkName:" + getMsgTextWithNameBookmarkName());
    }

}
