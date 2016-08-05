package com.mudah.my.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.configs.Config;
import com.mudah.my.models.BookmarkNotificationModel;
import com.mudah.my.models.DraftAdNotificationModel;
import com.mudah.my.models.InactiveInsertAdNotificationModel;
import com.mudah.my.models.InactiveUserNotificationModel;
import com.mudah.my.receivers.BookmarkNotificationReceiver;
import com.mudah.my.receivers.DraftAdCreateNotificationReceiver;
import com.mudah.my.receivers.InactiveInsertAdCreateNotificationReceiver;
import com.mudah.my.receivers.InactiveUserCreateNotificationReceiver;
import com.mudah.my.utils.MudahPreferencesUtils;
import com.mudah.my.utils.MudahUtil;

import java.util.Calendar;

/**
 * Created by w_ongkl on 2/11/15.
 */
public class NotificationBuilderHelper {

    public void createInactiveUserReminder(Context context, String options, InactiveUserNotificationModel inactiveUserNotificationModel) {

        //Remove inactive user notification
        MudahUtil.clearNotificationsByID(context, Config.NOTIFICATION_INACTIVE_USER_ID);

        inactiveUserNotificationModel.listConfig();

        // Cancel all alarm
        Log.d("InactiveUserNotification AlarmManager CANCELED");
        Intent updateServiceIntent = new Intent(context, InactiveUserCreateNotificationReceiver.class);
        PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 0, updateServiceIntent, 0);
        MudahUtil.cancelAlarmManagerWithPendingIntent(pendingUpdateIntent, context);

        if ((options.equalsIgnoreCase(Config.INACTIVE_USER_MAIN_NOTIFICATION) && inactiveUserNotificationModel.isMainInactiveUserEnable()) ||
                (options.equalsIgnoreCase(Config.INACTIVE_USER_SECONDARY_NOTIFICATION) && inactiveUserNotificationModel.isSecondaryInactiveUserEnable())) {
            Log.d("InactiveUserNotification create inactive user notification for " + options);

            // Get time Now
            Calendar timeNow = Calendar.getInstance();

            Long timeWithOffset = 0L;
            String intentExtra = "";
            if (options.equalsIgnoreCase(Config.INACTIVE_USER_MAIN_NOTIFICATION)) {
                // Offset target time by 21 days and set the flag
                timeWithOffset = timeNow.getTimeInMillis() + (inactiveUserNotificationModel.getMainInactiveUserTimer() * 1000);
                intentExtra = Config.INACTIVE_USER_MAIN_NOTIFICATION;
                inactiveUserNotificationModel.resetDismissCount(context);
            } else if (options.equalsIgnoreCase(Config.INACTIVE_USER_SECONDARY_NOTIFICATION)) {
                // Offset target time by 7 days and set the flag
                timeWithOffset = timeNow.getTimeInMillis() + (inactiveUserNotificationModel.getSecondaryInactiveUserTimer() * 1000);
                intentExtra = Config.INACTIVE_USER_SECONDARY_NOTIFICATION;
                inactiveUserNotificationModel.incrementDismissCount(context);
            }

            // Set time to targeted notification time
            Calendar targetTimeCalendar = Calendar.getInstance();
            targetTimeCalendar.setTimeInMillis(timeWithOffset);
            if (inactiveUserNotificationModel.getHourInactiveUserNotification() != InactiveUserNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.HOUR_OF_DAY, inactiveUserNotificationModel.getHourInactiveUserNotification());
            }
            if (inactiveUserNotificationModel.getMinuteInactiveUserNotification() != InactiveUserNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.MINUTE, inactiveUserNotificationModel.getMinuteInactiveUserNotification());
            }
            if (inactiveUserNotificationModel.getSecondInactiveUserNotification() != InactiveUserNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.SECOND, inactiveUserNotificationModel.getSecondInactiveUserNotification());
            }
            long milisecondsUntilTargetHour = targetTimeCalendar.getTimeInMillis();

            Log.d("InactiveUserNotification Time Now!                " + MudahUtil.formatDate(timeNow));
            Log.d("InactiveUserNotification Scheduled time!          " + MudahUtil.formatDate(targetTimeCalendar));

            // Set alarm
            Intent intent = new Intent(context, InactiveUserCreateNotificationReceiver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(intentExtra, true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, milisecondsUntilTargetHour, pendingIntent);
        }
    }

    public void cancelDraftNotification(Context context) {
        Log.d();
        Intent updateServiceIntent = new Intent(context, DraftAdCreateNotificationReceiver.class);
        PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 0, updateServiceIntent, 0);
        MudahUtil.cancelAlarmManagerWithPendingIntent(pendingUpdateIntent, context);
    }

    public void createDraftAdReminder(Context context, DraftAdNotificationModel draftAdNotificationModel) {

        //Remove ad insert notification
        MudahUtil.clearNotificationsByID(context, Config.NOTIFICATION_DRAFT_AD);

        draftAdNotificationModel.listConfig();

        // Cancel all alarm
        cancelDraftNotification(context);

        if (draftAdNotificationModel.isMainDraftAdEnable()) {

            Log.d(" create notification ");

            // Get time Now
            Calendar timeNow = Calendar.getInstance();

            Long timeWithOffset;
            // Offset target time by 7 days and set the flag
            timeWithOffset = timeNow.getTimeInMillis() + (draftAdNotificationModel.getMainDraftAdTimer() * 1000);
            draftAdNotificationModel.resetDismissCount(context);

            // Set time to targeted notification time
            Calendar targetTimeCalendar = Calendar.getInstance();
            targetTimeCalendar.setTimeInMillis(timeWithOffset);
            if (draftAdNotificationModel.getHourDraftAdNotification() != DraftAdNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.HOUR_OF_DAY, draftAdNotificationModel.getHourDraftAdNotification());
            }
            if (draftAdNotificationModel.getMinuteDraftAdNotification() != DraftAdNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.MINUTE, draftAdNotificationModel.getMinuteDraftAdNotification());
            }
            if (draftAdNotificationModel.getSecondDraftAdNotification() != DraftAdNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.SECOND, draftAdNotificationModel.getSecondDraftAdNotification());
            }
            long milisecondsUntilTargetHour = targetTimeCalendar.getTimeInMillis();

            Log.d(" Time Now!                " + MudahUtil.formatDate(timeNow));
            Log.d(" Scheduled time!          " + MudahUtil.formatDate(targetTimeCalendar));
            // Set alarm
            Intent intent = new Intent(context, DraftAdCreateNotificationReceiver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Config.DRAFT_MAIN_NOTIFICATION, true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, milisecondsUntilTargetHour, pendingIntent);
        }
    }

    public void cancelInactiveInsertAdNotification(Context context) {
        Log.d();
        Intent updateServiceIntent = new Intent(context, InactiveInsertAdCreateNotificationReceiver.class);
        PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 0, updateServiceIntent, 0);
        MudahUtil.cancelAlarmManagerWithPendingIntent(pendingUpdateIntent, context);
    }

    public void createInactiveInsertAdReminder(Context context, InactiveInsertAdNotificationModel inactiveInsertAdNotificationModel) {

        //Remove ad insert notification
        MudahUtil.clearNotificationsByID(context, Config.NOTIFICATION_INSERT_AD);

        inactiveInsertAdNotificationModel.listConfig();

        // Cancel all alarm
        cancelInactiveInsertAdNotification(context);

        if (inactiveInsertAdNotificationModel.isMainInsertAdEnable()) {

            Log.d(" create notification ");

            // Get time Now
            Calendar timeNow = Calendar.getInstance();

            Long timeWithOffset;
            // Offset target time by 7 days and set the flag
            timeWithOffset = timeNow.getTimeInMillis() + (inactiveInsertAdNotificationModel.getMainInsertAdTimer() * 1000);

            // Set time to targeted notification time
            Calendar targetTimeCalendar = Calendar.getInstance();
            targetTimeCalendar.setTimeInMillis(timeWithOffset);
            if (inactiveInsertAdNotificationModel.getHourInsertAdNotification() != InactiveInsertAdNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.HOUR_OF_DAY, inactiveInsertAdNotificationModel.getHourInsertAdNotification());
            }
            if (inactiveInsertAdNotificationModel.getMinuteInsertAdNotification() != InactiveInsertAdNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.MINUTE, inactiveInsertAdNotificationModel.getMinuteInsertAdNotification());
            }
            if (inactiveInsertAdNotificationModel.getSecondInsertAdNotification() != InactiveInsertAdNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.SECOND, inactiveInsertAdNotificationModel.getSecondInsertAdNotification());
            }
            long milisecondsUntilTargetHour = targetTimeCalendar.getTimeInMillis();

            Log.d(" Time Now!                " + MudahUtil.formatDate(timeNow));
            Log.d(" Scheduled time!          " + MudahUtil.formatDate(targetTimeCalendar));
            // Set alarm
            Intent intent = new Intent(context, InactiveInsertAdCreateNotificationReceiver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Config.INSERTAD_MAIN_NOTIFICATION, true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, milisecondsUntilTargetHour, pendingIntent);
        }
    }

    public void createBookmarkNotificationReminder(Context context, BookmarkNotificationModel bookmarkNotificationModel) {

        bookmarkNotificationModel.listConfig();

        SharedPreferences sharedPreferences = PreferencesUtils.getSharedPreferences(context);
        Long previousBookmarkNotificationTimer = sharedPreferences.getLong(MudahPreferencesUtils.BOOKMARK_NOTIFICATION_TIMER, -1);

        boolean forceTriggerAlarm = false;

        if (!bookmarkNotificationModel.isBookmarkNotificationEnable() || previousBookmarkNotificationTimer != bookmarkNotificationModel.getBookmarkNotificationTimer()) {
            // Cancel all alarm
            Log.d("BookmarkCheckNotification AlarmManager CANCELED");
            Intent updateServiceIntent = new Intent(context, BookmarkNotificationReceiver.class);
            PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 0, updateServiceIntent, 0);
            MudahUtil.cancelAlarmManagerWithPendingIntent(pendingUpdateIntent, context);
            if (bookmarkNotificationModel.isBookmarkNotificationEnable() && previousBookmarkNotificationTimer != bookmarkNotificationModel.getBookmarkNotificationTimer()) {
                forceTriggerAlarm = true;
            }
        }

        Intent BookmarkNotificationReceiverIntent = new Intent(context, BookmarkNotificationReceiver.class);
        boolean isBookmarkAlarmSet = (PendingIntent.getBroadcast(context, 0, BookmarkNotificationReceiverIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (forceTriggerAlarm || (bookmarkNotificationModel.isBookmarkNotificationEnable() && !isBookmarkAlarmSet)) {

            Log.d("BookmarkNotification create bookmark notification");

            // Get time Now
            Calendar timeNow = Calendar.getInstance();

            Long timeWithOffset = timeNow.getTimeInMillis() + (bookmarkNotificationModel.getBookmarkNotificationTimer() * 1000);

            // Set time to targeted notification time
            Calendar targetTimeCalendar = Calendar.getInstance();
            targetTimeCalendar.setTimeInMillis(timeWithOffset);
            if (bookmarkNotificationModel.getHourBookmarkNotification() != BookmarkNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.HOUR_OF_DAY, bookmarkNotificationModel.getHourBookmarkNotification());
            }
            if (bookmarkNotificationModel.getMinuteBookmarkNotification() != BookmarkNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.MINUTE, bookmarkNotificationModel.getMinuteBookmarkNotification());
            }
            if (bookmarkNotificationModel.getSecondBookmarkNotification() != BookmarkNotificationModel.UNSET) {
                targetTimeCalendar.set(Calendar.SECOND, bookmarkNotificationModel.getSecondBookmarkNotification());
            }
            long milisecondsUntilTargetHour = targetTimeCalendar.getTimeInMillis();

            Log.d("BookmarkNotification Time Now!                " + MudahUtil.formatDate(timeNow));
            Log.d("BookmarkNotification Scheduled time!          " + MudahUtil.formatDate(targetTimeCalendar));

            // Set alarm
            Intent intent = new Intent(context, BookmarkNotificationReceiver.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, milisecondsUntilTargetHour, pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, milisecondsUntilTargetHour, bookmarkNotificationModel.getBookmarkNotificationTimer() * 1000, pendingIntent);

            //commit() will write data to the storage immediately whereas apply() will handle it in the background
            sharedPreferences.edit()
                    .putLong(MudahPreferencesUtils.BOOKMARK_NOTIFICATION_TIMER, bookmarkNotificationModel.getBookmarkNotificationTimer())
                    .apply();
        }
    }


}
