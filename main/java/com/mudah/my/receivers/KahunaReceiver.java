package com.mudah.my.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.kahuna.sdk.Kahuna;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.activities.ListAdViewFavouritesActivity;
import com.mudah.my.activities.SignUpActivity;
import com.mudah.my.activities.SignUpIntroActivity;
import com.mudah.my.activities.SplashScreenActivity;
import com.mudah.my.activities.WebViewActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.utils.MudahUtil;

public class KahunaReceiver extends BroadcastReceiver {
    private static final String DEEP_LINK = "deeplink";
    private static final String DEEP_DOWNLOAD = "download";
    private static final String DEEP_LINK_AI = "ai";
    private static final String DEEP_LINK_DRAFT = "draft";
    private static final String DEEP_LINK_FAV = "favourite";
    private static final String DEEP_LINK_LIST = "list";
    private static final String DEEP_LINK_SIGNUP = "signup";
    private static final String DEEP_LINK_SIGNUP_INTRO = "signup_intro";
    private static final String DEEP_LINK_SELLER_ONBOARD = "seller_onboard";

    public KahunaReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();
        String action = intent.getAction();
        // If the incoming intent is a Kahuna push notification
        if (Kahuna.ACTION_PUSH_RECEIVED.equals(action)) {

            // Get the notification message
            String message = intent.getStringExtra(Kahuna.EXTRA_PUSH_MESSAGE);
            // Get the value of the "k" key ("k" is an extra provided by Kahuna)
            String kahunaMessageId = intent.getStringExtra("k");
            Log.d("Received Kahuna push message:" + message + ", kahunaMessageId: " + kahunaMessageId);
            // If the notification isn't empty, and it's a Kahuna notification
            if (!ACUtils.isEmpty(message)) {

                // Get the "params" from the incoming notification. These are "deep-linking"
                // attached to the notification
                Bundle extras = intent.getBundleExtra(Kahuna.EXTRA_LANDING_DICTIONARY_ID);
                Intent notificationClickIntent = new Intent(context, SplashScreenActivity.class);
                if (extras != null) {
                    // Make sure to check all values in case you receive pushes
                    // without any parameters.
                    String deepLink = extras.getString(DEEP_LINK);
                    Log.d("Received Kahuna push with deepLink:" + deepLink);
                    String putExtraStr = Constants.EMPTY_STRING;
                    if (!ACUtils.isEmpty(deepLink)) {
                        switch (deepLink.toLowerCase()) {
                            case DEEP_LINK_DRAFT:
                                putExtraStr = Config.DRAFT_MAIN_NOTIFICATION;
                            case DEEP_LINK_AI:
                                // Construct intent for click notification
                                notificationClickIntent = new Intent(context, InsertAdActivity.class);
                                notificationClickIntent.putExtra(putExtraStr, true);
                                break;
                            case DEEP_LINK_SIGNUP:
                                notificationClickIntent = new Intent(context, SignUpActivity.class);
                                break;
                            case DEEP_LINK_SIGNUP_INTRO:
                                notificationClickIntent = new Intent(context, SignUpIntroActivity.class);
                                break;
                            case DEEP_LINK_FAV:
                                notificationClickIntent = new Intent(context, ListAdViewFavouritesActivity.class);
                                break;
                            case DEEP_LINK_LIST:
                                notificationClickIntent = new Intent(context, AdsListActivity.class);
                                notificationClickIntent.putExtra(AdsListActivity.QUERY, extras.getString(AdsListActivity.QUERY));
                                notificationClickIntent.putExtra(AdsListActivity.FILTER, extras.getString(AdsListActivity.FILTER));
                                notificationClickIntent.putExtra(Config.PUSH_NOTIFICATION, true);
                                break;
                            case DEEP_LINK_SELLER_ONBOARD:
                                notificationClickIntent = new Intent(context, WebViewActivity.class);
                                notificationClickIntent.putExtra(WebViewActivity.EXTERNAL_URL, Config.shareHost + Config.SELLER_ONBOARD);
                                MudahUtil.saveClickTime(context);
                                break;
                            case DEEP_DOWNLOAD:
                                notificationClickIntent = new Intent(Intent.ACTION_VIEW);
                                notificationClickIntent.setData(Uri.parse(Config.MUDAH_APP_GOOGLE));
                                break;
                        }
                    }
                }

                if (ACUtils.isEmpty(notificationClickIntent.getAction())) {
                    notificationClickIntent.setAction(Intent.ACTION_MAIN);
                }
                notificationClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                PendingIntent notificationClickPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        notificationClickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
                //Prepare to generate our own notification.
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                //Construct the Builder object.
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        // The app's small icon
                        .setSmallIcon(R.drawable.notification_ticker)
                        .setLargeIcon(largeIcon)
                        // The app's icon label
                        .setContentTitle(context.getString(R.string.title_activity_manage_ads))
                        // The incoming notification message
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                        // The text displayed in the notification area
                        .setTicker(message)
                        // Make Android "big text" for the message
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
                // Prepare to send the notification
                builder.setContentIntent(notificationClickPendingIntent);
                builder.setAutoCancel(true);
                // Send the notification
                notificationManager.notify(Config.NOTIFICATION_KAHUNA, builder.build());
            }

        }
    }

}
