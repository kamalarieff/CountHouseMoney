/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mudah.my.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.SplashScreenActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.utils.EventTrackingUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import io.fabric.sdk.android.Fabric;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {

    public static final String MESSAGE = "message";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d("GCM " + data);
        if (!(data.containsKey(MESSAGE))) {
            return;
        }
        Fabric.with(this, new Crashlytics());
        if (XitiUtils.initFromLastConfig(this) == null) {
            Log.e("Xiti initialization is wrong");
        }

        if (data.containsKey(Constants.PUSH_CHAT_ROOM_ID)) {
            sendChatNotification(data);
        } else {
            sendGeneralNotification(data);
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     */
    private void sendGeneralNotification(Bundle data) {

        Log.d("data: " + data);
        String message = data.getString(MESSAGE);
        String query = data.getString(AdsListActivity.QUERY);
        String filter = data.getString(AdsListActivity.FILTER);

        Intent intent = new Intent(this, AdsListActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AdsListActivity.QUERY, query);
        intent.putExtra(AdsListActivity.FILTER, filter);
        intent.putExtra(Config.PUSH_NOTIFICATION, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_ticker)
                .setLargeIcon(largeIcon)
                .setContentTitle(getString(R.string.title_activity_manage_ads))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        EventTrackingUtils.sendCampaign(this, XitiUtils.CAMPAIGN_NOTIFICATION_GCM_PUSH, XitiUtils.CAMPAIGN_IMPRESSION);

    }

    /**
     * Create and show a chat notification containing the received GCM message.
     */
    private void sendChatNotification(Bundle data) {
        String message = data.getString("message");
        String roomId = data.getString(Constants.PUSH_CHAT_ROOM_ID);
        Log.d("roomId: " + roomId + ", Message: " + message);//565c1669a51af36a00926384

        if (!ACUtils.isEmpty(roomId) && !ACUtils.isEmpty(message)) {
            Intent intent = new Intent(this, SplashScreenActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.PUSH_CHAT_ROOM_ID, roomId);
            intent.putExtra(Config.PUSH_NOTIFICATION, true);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //displayed icon when pull down the notification
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notification_ticker)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(getString(R.string.title_activity_manage_ads))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message));

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(Config.NOTIFICATION_CHAT_MSG, notificationBuilder.build());
            //tagging
            try {
                JSONObject json = getJsonFromBundle(data);
                json.put(TealiumHelper.TEXT, getMessageFromFullText(message));

                TealiumHelper.tagTealiumReceiveMessage(this, json);
            } catch (JSONException skip) {
            }
        }

    }

    private String getMessageFromFullText(String message) {
        String[] msgText = message.split(": ");
        if (msgText.length == 2)
            return msgText[1];
        else
            return message;
    }

    private JSONObject getJsonFromBundle(Bundle bundle) {
        JSONObject result = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                result.put(key, (String) bundle.get(key));
            } catch (JSONException ignore) {
            }
        }
        return result;
    }
}