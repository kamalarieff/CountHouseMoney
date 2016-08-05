package com.mudah.my.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.utils.EventTrackingUtils;

public class DraftAdDismissNotificationReceiver extends BroadcastReceiver {

    public DraftAdDismissNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d();

        if (XitiUtils.initFromLastConfig(context) == null)
            Log.e("Xiti initialization is wrong");
        else
            EventTrackingUtils.sendClick(XitiUtils.getLevel2Map(XitiUtils.LEVEL2_NOTIFICATION_DRAFT_AD), "AI_notification_dismissed", XitiUtils.NAVIGATION);

    }

}
