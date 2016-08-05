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
 * Created by w_ongkl on 12/26/14.
 */
public class InactiveUserNotificationModel {

    public static final String DEFAULT_CONFIG = "" +
            "{" +
            "main_enable: false," +
            "secondary_enable: false," +
            "main_timer: \"1814000\"," +
            "secondary_timer: \"604800\"," +
            "notification_timing_hour: \"19\"," +
            "notification_timing_minute: \"00\"," +
            "notification_timing_second: \"00\"" +
            "}";

    public static final int UNSET = -1;
    @SerializedName("mainInactiveUserEnable")
    private boolean mainInactiveUserEnable = false;
    @SerializedName("secondaryInactiveUserEnable")
    private boolean secondaryInactiveUserEnable = false;
    @SerializedName("mainInactiveUserTimer")
    private long mainInactiveUserTimer = 1814000; //21 days
    @SerializedName("secondaryInactiveUserTimer")
    private long secondaryInactiveUserTimer = 604800; // 7 days
    @SerializedName("hourInactiveUserNotification")
    private int hourInactiveUserNotification = 19; // In 24 hrs notation
    @SerializedName("minuteInactiveUserNotification")
    private int minuteInactiveUserNotification = 00;
    @SerializedName("secondInactiveUserNotification")
    private int secondInactiveUserNotification = 00;
    @SerializedName("dismissCount")
    private int dismissCount = 0;
    @SerializedName("reminderMsgTitle")
    private String reminderMsgTitle = "We miss you!";
    @SerializedName("reminderMsgTitleWithName")
    private String reminderMsgTitleWithName = "We miss you, {name}!";
    @SerializedName("reminderMsgTextWithCategory")
    private String reminderMsgTextWithCategory = "Many {category} posted in Mudah everyday";
    @SerializedName("reminderMsgText")
    private String reminderMsgText = "Many items posted in Mudah everyday";
    @SerializedName("shortReminderMsgTextWithCategory")
    private String shortReminderMsgTextWithCategory = "Check for new {category}";
    @SerializedName("shortReminderMsgText")
    private String shortReminderMsgText = "Check for new items";

    public InactiveUserNotificationModel() {
        // Do nothing
    }

    public static InactiveUserNotificationModel newInstance(Context context) {
        InactiveUserNotificationModel inactiveUserConf = new InactiveUserNotificationModel();
        try {
            inactiveUserConf = MudahUtil
                    .retrieveClassInSharedPreferences(
                            context,
                            MudahPreferencesUtils.INACTIVE_USER_NOTIFICATION_CONFIG,
                            InactiveUserNotificationModel.class,
                            DEFAULT_CONFIG);

        } catch (MalformedJsonException e) {
            ACUtils.debug(e);
        } catch (JsonSyntaxException e) {
            ACUtils.debug(e);
        }
        return inactiveUserConf;
    }

    public boolean isMainInactiveUserEnable() {
        return mainInactiveUserEnable;
    }

    public boolean isSecondaryInactiveUserEnable() {
        return secondaryInactiveUserEnable;
    }

    public long getMainInactiveUserTimer() {
        return mainInactiveUserTimer;
    }

    public long getSecondaryInactiveUserTimer() {
        return secondaryInactiveUserTimer;
    }

    public int getHourInactiveUserNotification() {
        return hourInactiveUserNotification;
    }

    public int getMinuteInactiveUserNotification() {
        return minuteInactiveUserNotification;
    }

    public int getSecondInactiveUserNotification() {
        return secondInactiveUserNotification;
    }

    public int getDismissCount() {
        return dismissCount;
    }

    public String getReminderMsgTitle() {
        return reminderMsgTitle;
    }

    public String getReminderMsgTitleWithName() {
        return reminderMsgTitleWithName;
    }

    public String getReminderMsgTextWithCategory() {
        return reminderMsgTextWithCategory;
    }

    public String getReminderMsgText() {
        return reminderMsgText;
    }

    public String getShortReminderMsgTextWithCategory() {
        return shortReminderMsgTextWithCategory;
    }

    public String getShortReminderMsgText() {
        return shortReminderMsgText;
    }

    public void incrementDismissCount(Context context) {
        dismissCount++;
        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.INACTIVE_USER_NOTIFICATION_CONFIG, this);
    }

    public void resetDismissCount(Context context) {
        dismissCount = 0;
        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.INACTIVE_USER_NOTIFICATION_CONFIG, this);
    }

    public void saveInactiveUserNotificationConfig(JSONObject jsonObjInactiveUser, Context context) {

        if (jsonObjInactiveUser.has("main_enable")) {
            mainInactiveUserEnable = jsonObjInactiveUser.optBoolean("main_enable");
            if (!mainInactiveUserEnable) {
                secondaryInactiveUserEnable = false;
            } else {
                if (jsonObjInactiveUser.has("secondary_enable")) {
                    secondaryInactiveUserEnable = jsonObjInactiveUser.optBoolean("secondary_enable");
                }
            }
        }

        if (jsonObjInactiveUser.has("main_timer")) {
            mainInactiveUserTimer = jsonObjInactiveUser.optLong("main_timer");
        }

        if (jsonObjInactiveUser.has("secondary_timer")) {
            secondaryInactiveUserTimer = jsonObjInactiveUser.optLong("secondary_timer");
        }

        if (jsonObjInactiveUser.has("notification_timing_hour") && !ACUtils.isEmpty(jsonObjInactiveUser.optString("notification_timing_hour"))) {
            hourInactiveUserNotification = jsonObjInactiveUser.optInt("notification_timing_hour");
        } else {
            hourInactiveUserNotification = UNSET;
        }

        if (jsonObjInactiveUser.has("notification_timing_minute") && !ACUtils.isEmpty(jsonObjInactiveUser.optString("notification_timing_minute"))) {
            minuteInactiveUserNotification = jsonObjInactiveUser.optInt("notification_timing_minute");
        } else {
            minuteInactiveUserNotification = UNSET;
        }

        if (jsonObjInactiveUser.has("notification_timing_second") && !ACUtils.isEmpty(jsonObjInactiveUser.optString("notification_timing_second"))) {
            secondInactiveUserNotification = jsonObjInactiveUser.optInt("notification_timing_second");
        } else {
            secondInactiveUserNotification = UNSET;
        }

        if (jsonObjInactiveUser.has("msg_title_with_name")) {
            reminderMsgTitleWithName = jsonObjInactiveUser.optString("msg_title_with_name");
        }

        if (jsonObjInactiveUser.has("msg_title")) {
            reminderMsgTitle = jsonObjInactiveUser.optString("msg_title");
        }

        if (jsonObjInactiveUser.has("msg_text_with_category")) {
            reminderMsgTextWithCategory = jsonObjInactiveUser.optString("msg_text_with_category");
        }

        if (jsonObjInactiveUser.has("msg_text")) {
            reminderMsgText = jsonObjInactiveUser.optString("msg_text");
        }

        if (jsonObjInactiveUser.has("short_msg_text_with_category")) {
            shortReminderMsgTextWithCategory = jsonObjInactiveUser.optString("short_msg_text_with_category");
        }

        if (jsonObjInactiveUser.has("short_msg_text")) {
            shortReminderMsgText = jsonObjInactiveUser.optString("short_msg_text");
        }
        // User opens the app. Reset dismiss count counter
        dismissCount = 0;

        MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.INACTIVE_USER_NOTIFICATION_CONFIG, this);
    }

    public void listConfig() {
        StringBuilder strLog = new StringBuilder(); // Using default 16 character size
        strLog.append("InactiveUserNotification         mainInactiveUserEnable:" + isMainInactiveUserEnable());
        strLog.append("\n    secondaryInactiveUserEnable:" + isSecondaryInactiveUserEnable());
        strLog.append("\n          mainInactiveUserTimer:" + getMainInactiveUserTimer());
        strLog.append("\n     secondaryInactiveUserTimer:" + getSecondaryInactiveUserTimer());
        strLog.append("\n   hourInactiveUserNotification:" + getHourInactiveUserNotification());
        strLog.append("\n minuteInactiveUserNotification:" + getMinuteInactiveUserNotification());
        strLog.append("\n secondInactiveUserNotification:" + getSecondInactiveUserNotification());
        strLog.append("\n               msgTitleWithName:" + getReminderMsgTitleWithName());
        strLog.append("\n                       msgTitle:" + getReminderMsgTitle());
        strLog.append("\n            msgTextWithCategory:" + getReminderMsgTextWithCategory());
        strLog.append("\n                        msgText:" + getReminderMsgText());
        strLog.append("\n       shortMsgTextWithCategory:" + getShortReminderMsgTextWithCategory());
        strLog.append("\n                   shortMsgText:" + getShortReminderMsgText());
        strLog.append("\n                   dismissCount:" + getDismissCount());
        Log.d(strLog.toString());
    }


}
