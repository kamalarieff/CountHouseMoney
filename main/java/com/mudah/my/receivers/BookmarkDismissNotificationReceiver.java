package com.mudah.my.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.utils.EventTrackingUtils;

public class BookmarkDismissNotificationReceiver extends BroadcastReceiver {

    public BookmarkDismissNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();

        if (XitiUtils.initFromLastConfig(context) == null)
            Log.e("Xiti initialization is wrong");

        EventTrackingUtils.sendClick(XitiUtils.getLevel2Map(XitiUtils.LEVEL2_NOTIFICATION_BOOKMARK), "bookmark_notification_dismissed", XitiUtils.NAVIGATION);
        Log.d("BookmarkNotification Notification Dismissed");
    }

}
