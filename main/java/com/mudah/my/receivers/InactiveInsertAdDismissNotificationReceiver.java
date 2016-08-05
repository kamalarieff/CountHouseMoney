package com.mudah.my.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.helpers.NotificationBuilderHelper;
import com.mudah.my.models.InactiveInsertAdNotificationModel;
import com.mudah.my.utils.EventTrackingUtils;

public class InactiveInsertAdDismissNotificationReceiver extends BroadcastReceiver {

    public InactiveInsertAdDismissNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();

        InactiveInsertAdNotificationModel inactiveInsertAdNotificationModel = InactiveInsertAdNotificationModel.newInstance(context);
        NotificationBuilderHelper notificationBuilderHelper = new NotificationBuilderHelper();
        notificationBuilderHelper.createInactiveInsertAdReminder(context, inactiveInsertAdNotificationModel);

        if (XitiUtils.initFromLastConfig(context) == null)
            Log.e("Xiti initialization is wrong");
        else
            EventTrackingUtils.sendClick(XitiUtils.getLevel2Map(XitiUtils.LEVEL2_NOTIFICATION_INSERT_AD), "AI_Insertad_notification_dismissed", XitiUtils.NAVIGATION);
    }

}
