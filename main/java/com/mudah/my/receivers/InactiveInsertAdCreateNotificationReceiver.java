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
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.models.InactiveInsertAdNotificationModel;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class InactiveInsertAdCreateNotificationReceiver extends BroadcastReceiver {

    public InactiveInsertAdCreateNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();
        Fabric.with(context, new Crashlytics());

        if (XitiUtils.initFromLastConfig(context) == null) {
            ACUtils.sendFalseCrashLog("<InactiveInsertAdCreateNotificationReceiver> Xiti initialization is wrong");
        }

        String putExtraStr = "";
        boolean userClickedNotification = intent.getExtras().getBoolean(Config.INSERTAD_MAIN_NOTIFICATION);
        if (userClickedNotification) {
            putExtraStr = Config.INSERTAD_MAIN_NOTIFICATION;
        }

        Log.d("Wakeup time!             " + MudahUtil.formatDate(Calendar.getInstance()));

        // Construct notification message
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(context);
        String name = pref.getString(PreferencesUtils.USER_NAME, null);

        InactiveInsertAdNotificationModel inactiveInsertAdNotificationModel = InactiveInsertAdNotificationModel.newInstance(context.getApplicationContext());
        String reminderMsgTitle = inactiveInsertAdNotificationModel.getMsgTitle();
        String reminderMsgText = inactiveInsertAdNotificationModel.getMsgText();

        if (!ACUtils.isEmpty(name)) {
            reminderMsgTitle = inactiveInsertAdNotificationModel.getMsgTitleWithName().replace("{name}", name.toUpperCase());
        }

        // Construct intent for click notification
        Intent notificationClickIntent = new Intent(context, InsertAdActivity.class);
        notificationClickIntent.setAction(Intent.ACTION_MAIN);
        notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationClickIntent.putExtra(putExtraStr, true);
        PendingIntent notificationClickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct intent for dismiss notification
        Intent notificationDeleteIntent = new Intent(context, InactiveInsertAdDismissNotificationReceiver.class);
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
                .setContentText(reminderMsgText)
                .setSmallIcon(R.drawable.notification_ticker)
                .setLargeIcon(largeIcon)
                .setTicker(reminderMsgTitle)
                .setAutoCancel(true)
                .setContentIntent(notificationClickPendingIntent)
                .setDeleteIntent(notificationDeletePendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_INSERT_AD, notify.build());

        EventTrackingUtils.sendCampaign(context, XitiUtils.CAMPAIGN_NOTIFICATION_INSERT_AD, XitiUtils.CAMPAIGN_IMPRESSION);

        return;
    }

}
