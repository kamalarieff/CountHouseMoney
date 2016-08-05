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
 * Created by Siripin on 12/26/14.
 */
public class DraftAdNotificationModel {

    public static final String DEFAULT_CONFIG = "" +
            "{" +
            "main_enable: false," +
            "main_timer: \"1814000\"," +
            "notification_timing_hour: \"19\"," +
            "notification_timing_minute: \"00\"," +
            "notification_timing_second: \"00\"" +
            "}";

    public static final int UNSET = -1;
    @SerializedName("mainDraftAdEnable")
    private boolean mainDraftAdEnable = false;
    @SerializedName("mainDraftAdTimer")
    private long mainDraftAdTimer = 604800; // 7 days
    @SerializedName("hourDraftAdNotification")
    private int hourDraftAdNotification = 19; // In 24 hrs notation
    @SerializedName("minuteDraftAdNotification")
    private int minuteDraftAdNotification = 00;
    @SerializedName("secondDraftAdNotification")
    private int secondDraftAdNotification = 00;
    @SerializedName("dismissCount")
    private int dismissCount = 0;
    @SerializedName("msgTitle")
    private String msgTitle = "Your draft is pending.";
    @SerializedName("msgTitleWithName")
    private String msgTitleWithName = "Hi {name}, your draft is pending.";
    @SerializedName("msgText")
    private String msgText = "Post your ad for FREE! Thousands buyers are waiting for it.";

    public DraftAdNotificationModel() {

    }

    public static DraftAdNotificationModel newInstance(Context context) {
        DraftAdNotificationModel draftAdConf = new DraftAdNotificationModel();
        try {
            draftAdConf = MudahUtil
                    .retrieveClassInSharedPreferences(
                            context,
                            MudahPreferencesUtils.DRAFT_AD_NOTIFICATION_CONFIG,
                            DraftAdNotificationModel.class,
                            DEFAULT_CONFIG);

        } catch (MalformedJsonException e) {
            ACUtils.debug(e);
        } catch (JsonSyntaxException e) {
            ACUtils.debug(e);
        }
        return  draftAdConf;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }

    public String getMsgTitleWithName() {
        return msgTitleWithName;
    }

    public void setMsgTitleWithName(String msgTitleWithName) {
        this.msgTitleWithName = msgTitleWithName;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public void saveDraftAdNotificationConfig(JSONObject jsonObjDraftAd, Context context) {

        if (jsonObjDraftAd.has("main_enable")) {
            mainDraftAdEnable = jsonObjDraftAd.optBoolean("main_enable");
        }

        if (jsonObjDraftAd.has("main_timer")) {
            mainDraftAdTimer = jsonObjDraftAd.optLong("main_timer");
        }

        if (jsonObjDraftAd.has("notification_timing_hour") && !ACUtils.isEmpty(jsonObjDraftAd.optString("notification_timing_hour"))) {
            hourDraftAdNotification = jsonObjDraftAd.optInt("notification_timing_hour");
        } else {
            hourDraftAdNotification = UNSET;
        }

        if (jsonObjDraftAd.has("notification_timing_minute") && !ACUtils.isEmpty(jsonObjDraftAd.optString("notification_timing_minute"))) {
            minuteDraftAdNotification = jsonObjDraftAd.optInt("notification_timing_minute");
        } else {
            minuteDraftAdNotification = UNSET;
        }

        if (jsonObjDraftAd.has("notification_timing_second") && !ACUtils.isEmpty(jsonObjDraftAd.optString("notification_timing_second"))) {
            secondDraftAdNotification = jsonObjDraftAd.optInt("notification_timing_second");
        } else {
            secondDraftAdNotification = UNSET;
        }

        if (jsonObjDraftAd.has("msg_title_with_name")) {
            msgTitleWithName = jsonObjDraftAd.optString("msg_title_with_name");
        }

        if (jsonObjDraftAd.has("msg_title")) {
            msgTitle = jsonObjDraftAd.optString("msg_title");
        }
        if (jsonObjDraftAd.has("msg_text")) {
            msgText = jsonObjDraftAd.optString("msg_text");
        }
        // User opens the app. Reset dismiss count counter
        dismissCount = 0;

        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.DRAFT_AD_NOTIFICATION_CONFIG, this);
    }

    public void listConfig() {
        StringBuilder strLog = new StringBuilder(); // Using default 16 character size
        strLog.append("DraftAdNotification  mainDraftAdEnable:" + isMainDraftAdEnable());
        strLog.append("\n         mainDraftAdEnable:" + isMainDraftAdEnable());
        strLog.append("\n          mainDraftAdTimer:" + getMainDraftAdTimer());
        strLog.append("\n   hourDraftAdNotification:" + getHourDraftAdNotification());
        strLog.append("\n minuteDraftAdNotification:" + getMinuteDraftAdNotification());
        strLog.append("\n secondDraftAdNotification:" + getSecondDraftAdNotification());
        strLog.append("\n          msgTitleWithName:" + getMsgTitleWithName());
        strLog.append("\n                  msgTitle:" + getMsgTitle());
        strLog.append("\n                   msgText:" + getMsgText());
        Log.d(strLog.toString());
    }

    public boolean isMainDraftAdEnable() {
        return mainDraftAdEnable;
    }

    public long getMainDraftAdTimer() {
        return mainDraftAdTimer;
    }

    public int getHourDraftAdNotification() {
        return hourDraftAdNotification;
    }

    public int getMinuteDraftAdNotification() {
        return minuteDraftAdNotification;
    }

    public int getSecondDraftAdNotification() {
        return secondDraftAdNotification;
    }

    public int getDismissCount() {
        return dismissCount;
    }

    public void incrementDismissCount(Context context) {
        dismissCount++;
        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.DRAFT_AD_NOTIFICATION_CONFIG, this);
    }

    public void resetDismissCount(Context context) {
        dismissCount = 0;
        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.DRAFT_AD_NOTIFICATION_CONFIG, this);
    }

}
