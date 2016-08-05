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
 * Created by NG PEI CHIN on 02/09/15.
 */
public class InactiveInsertAdNotificationModel {

    public static final String DEFAULT_CONFIG = "" +
            "{" +
            "main_enable: false," +
            "main_timer: \"3888000\"," +
            "notification_timing_hour: \"-1\"," +
            "notification_timing_minute: \"-1\"," +
            "notification_timing_second: \"-1\"" +
            "msgTitle:Anything else to sell?" +
            "msgTitleWithName = Hi {name},anything else to sell?" +
            "msgText = Thousands of users buy in Mudah, every day." +
            "}";

    public static final int UNSET = -1;
    @SerializedName("hourInsertAdNotification")
    private int hourInsertAdNotification = UNSET;
    @SerializedName("minuteInsertAdNotification")
    private int minuteInsertAdNotification = UNSET;
    @SerializedName("secondInsertAdNotification")
    private int secondInsertAdNotification = UNSET;
    @SerializedName("mainInsertAdEnable")
    private boolean mainInsertAdEnable = false;
    @SerializedName("mainInsertAdTimer")
    private long mainInsertAdTimer = 3888000; // 45 days
    @SerializedName("msgTitle")
    private String msgTitle = "Anything else to sell?";
    @SerializedName("msgTitleWithName")
    private String msgTitleWithName = "Hi {name},anything to sell?";
    @SerializedName("msgText")
    private String msgText = "Anything else to sell? Thousands of users buy in Mudah, every day.";

    public InactiveInsertAdNotificationModel() {

    }

    public static InactiveInsertAdNotificationModel newInstance(Context context) {
        InactiveInsertAdNotificationModel insertAdConf = new InactiveInsertAdNotificationModel();
        try {
            insertAdConf = MudahUtil
                    .retrieveClassInSharedPreferences(
                            context,
                            MudahPreferencesUtils.AD_INSERT_NOTIFICATION_CONFIG,
                            InactiveInsertAdNotificationModel.class,
                            DEFAULT_CONFIG);

        } catch (MalformedJsonException e) {
            ACUtils.debug(e);
        } catch (JsonSyntaxException e) {
            ACUtils.debug(e);
        }
        return insertAdConf;
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

    public void saveInsertAdNotificationConfig(JSONObject jsonObjInsertAd, Context context) {

        if (jsonObjInsertAd.has("main_enable")) {
            mainInsertAdEnable = jsonObjInsertAd.optBoolean("main_enable");
        }

        if (jsonObjInsertAd.has("main_timer")) {
            mainInsertAdTimer = jsonObjInsertAd.optLong("main_timer");
        }

        if (jsonObjInsertAd.has("notification_timing_hour") && !ACUtils.isEmpty(jsonObjInsertAd.optString("notification_timing_hour"))) {
            hourInsertAdNotification = jsonObjInsertAd.optInt("notification_timing_hour");
        } else {
            hourInsertAdNotification = UNSET;
        }

        if (jsonObjInsertAd.has("notification_timing_minute") && !ACUtils.isEmpty(jsonObjInsertAd.optString("notification_timing_minute"))) {
            minuteInsertAdNotification = jsonObjInsertAd.optInt("notification_timing_minute");
        } else {
            minuteInsertAdNotification = UNSET;
        }

        if (jsonObjInsertAd.has("notification_timing_second") && !ACUtils.isEmpty(jsonObjInsertAd.optString("notification_timing_second"))) {
            secondInsertAdNotification = jsonObjInsertAd.optInt("notification_timing_second");
        } else {
            secondInsertAdNotification = UNSET;
        }

        if (jsonObjInsertAd.has("msg_title_with_name")) {
            msgTitleWithName = jsonObjInsertAd.optString("msg_title_with_name");
        }

        if (jsonObjInsertAd.has("msg_title")) {
            msgTitle = jsonObjInsertAd.optString("msg_title");
        }
        if (jsonObjInsertAd.has("msg_text")) {
            msgText = jsonObjInsertAd.optString("msg_text");
        }

        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.AD_INSERT_NOTIFICATION_CONFIG, this);
    }

    public void listConfig() {
        StringBuilder strLog = new StringBuilder(); // Using default 16 character size
        strLog.append("InsertAdNotification mainInsertAdEnable:" + isMainInsertAdEnable());
        strLog.append("\n          mainInsertAdTimer:" + getMainInsertAdTimer());
        strLog.append("\n   hourInsertAdNotification:" + getHourInsertAdNotification());
        strLog.append("\n minuteInsertAdNotification:" + getMinuteInsertAdNotification());
        strLog.append("\n secondInsertAdNotification:" + getSecondInsertAdNotification());
        strLog.append("\n          msgTitleWithName:" + getMsgTitleWithName());
        strLog.append("\n                  msgTitle:" + getMsgTitle());
        strLog.append("\n                   msgText:" + getMsgText());
        Log.d(strLog.toString());
    }

    public boolean isMainInsertAdEnable() {
        return mainInsertAdEnable;
    }

    public long getMainInsertAdTimer() {
        return mainInsertAdTimer;
    }

    public int getHourInsertAdNotification() {
        return hourInsertAdNotification;
    }

    public int getMinuteInsertAdNotification() {
        return minuteInsertAdNotification;
    }

    public int getSecondInsertAdNotification() {
        return secondInsertAdNotification;
    }

}
