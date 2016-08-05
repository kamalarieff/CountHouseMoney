package com.mudah.my.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.models.InactiveUserNotificationModel;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahPreferencesUtils;
import com.mudah.my.utils.MudahUtil;

import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class InactiveUserCreateNotificationReceiver extends BroadcastReceiver {

    public InactiveUserCreateNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();
        Fabric.with(context, new Crashlytics());

        if (XitiUtils.initFromLastConfig(context) == null) {
            ACUtils.sendFalseCrashLog("<InactiveUserCreateNotificationReceiver> Xiti initialization is wrong");
        }

        String putExtraStr = "";
        boolean userClickedNotification = intent.getExtras().getBoolean(Config.INACTIVE_USER_MAIN_NOTIFICATION);
        boolean userDismissedNotification = intent.getExtras().getBoolean(Config.INACTIVE_USER_SECONDARY_NOTIFICATION);
        if (userClickedNotification) {
            putExtraStr = Config.INACTIVE_USER_MAIN_NOTIFICATION;
        } else if (userDismissedNotification) {
            putExtraStr = Config.INACTIVE_USER_SECONDARY_NOTIFICATION;
        }

        Log.d("InactiveUserNotification Wakeup time!             " + MudahUtil.formatDate(Calendar.getInstance()));

        // Construct notification message
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(context);
        String name = pref.getString(PreferencesUtils.USER_NAME, null);
        String category = pref.getString(MudahPreferencesUtils.LAST_SEARCHED_CATEGORY_NAME, null);
        InactiveUserNotificationModel notificationConfig = InactiveUserNotificationModel.newInstance(context.getApplicationContext());
        String reminderMsgTitle = notificationConfig.getReminderMsgTitle();
        String reminderMsgText = notificationConfig.getReminderMsgText();
        String shortReminderMsgText = notificationConfig.getShortReminderMsgText();

        if (!ACUtils.isEmpty(name)) {
            reminderMsgTitle = notificationConfig.getReminderMsgTitleWithName().replace("{name}", name.toUpperCase());
        }
        if (!ACUtils.isEmpty(category) && !(context.getString(R.string.ads_search_category_all)).equalsIgnoreCase(category)) {
            reminderMsgText = notificationConfig.getReminderMsgTextWithCategory().replace("{category}", category.toLowerCase());
            shortReminderMsgText = notificationConfig.getShortReminderMsgTextWithCategory().replace("{category}", category.toLowerCase());
        }

        // Construct intent for click notification
        Intent notificationClickIntent = new Intent(context, AdsListActivity.class);
        notificationClickIntent.setAction(Intent.ACTION_MAIN);
        notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationClickIntent.putExtra(putExtraStr, true);
        PendingIntent notificationClickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct intent for dismiss notification
        Intent notificationDeleteIntent = new Intent(context, InactiveUserDismissNotificationReceiver.class);
        notificationDeleteIntent.setAction(Intent.ACTION_MAIN);
        notificationDeleteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationDeleteIntent.putExtra(putExtraStr, true);
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
                .setContentTitle(reminderMsgTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(reminderMsgText))
                .setContentText(shortReminderMsgText)
                .setSmallIcon(R.drawable.notification_ticker)
                .setLargeIcon(largeIcon)
                .setTicker(reminderMsgTitle)
                .setAutoCancel(true)
                .setContentIntent(notificationClickPendingIntent)
                .setDeleteIntent(notificationDeletePendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_INACTIVE_USER_ID, notify.build());

        EventTrackingUtils.sendCampaign(context, XitiUtils.CAMPAIGN_NOTIFICATION_REACTIVATE, XitiUtils.CAMPAIGN_IMPRESSION);

        return;
    }

}
