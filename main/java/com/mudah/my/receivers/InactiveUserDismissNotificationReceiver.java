package com.mudah.my.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.configs.Config;
import com.mudah.my.helpers.NotificationBuilderHelper;
import com.mudah.my.models.InactiveUserNotificationModel;
import com.mudah.my.utils.EventTrackingUtils;

public class InactiveUserDismissNotificationReceiver extends BroadcastReceiver {

    public InactiveUserDismissNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();

        if (XitiUtils.initFromLastConfig(context) == null)
            Log.e("Xiti initialization is wrong");

        InactiveUserNotificationModel inactiveUserNotificationModel = InactiveUserNotificationModel.newInstance(context);
        NotificationBuilderHelper notificationBuilderHelper = new NotificationBuilderHelper();
        notificationBuilderHelper.createInactiveUserReminder(context, Config.INACTIVE_USER_SECONDARY_NOTIFICATION, inactiveUserNotificationModel);

        EventTrackingUtils.sendClick(XitiUtils.getLevel2Map(XitiUtils.LEVEL2_NOTIFICATION_REACTIVATE), "RE_notification_dismissed", XitiUtils.NAVIGATION);

    }

}
