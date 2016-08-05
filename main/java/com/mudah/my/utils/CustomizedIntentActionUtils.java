package com.mudah.my.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.lib701.adapters.ShareAdapter;
import com.lib701.datasets.Share;
import com.lib701.utils.ACUtils;
import com.lib701.utils.IntentActionUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.configs.Config;

import java.util.List;

public class CustomizedIntentActionUtils extends IntentActionUtils {
    /**
     * Workaround InsertAd for Android 4.4 by redirecting users to browser for now.
     * This is because <input type="file"/> doesn't work
     */
    public static void InsertAdRedirectToBrowser(boolean redirect, Context context) {
        try {
            if (redirect) {
                if (!Log.isDebug) {
                    Crashlytics.getInstance().core.setString("startActivity", "InsertAd Web View");
                }
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.insert_ad_redirection_message), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.insertAdURL));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, InsertAdActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(intent);
            }

        } catch (ActivityNotFoundException activityNotFound) {
            //Tablet will throw out this error
            ACUtils.debug(activityNotFound);
        }
    }

    public static void customShare(final int categoryId, final String page, final String subject, final String body, final Context context) {
        // Custom Share: Inflate popup layout
        View shareView = ((Activity) context).getLayoutInflater().inflate(com.android701.R.layout.popup_share, null);

        List<Share> shareData = getListOfShareApp(context);
        if (shareData.size() > 0) {
            final ShareAdapter adapter = new ShareAdapter(context, com.android701.R.layout.share, shareData);

            ListView listView = (ListView) shareView.findViewById(com.android701.R.id.lv_share);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View item,
                                        int position, long id) {
                    Share share = adapter.getItem(position);

                    Intent targetedShareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    targetedShareIntent.setType("text/plain");
                    //add a subject
                    targetedShareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                    //build the body of the body to be shared
                    targetedShareIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
                    targetedShareIntent.setPackage(share.packageName);

                    Log.d("Share via: " + share.name + ", from: " + page);
                    EventTrackingUtils.sendClickByCategoryId(null, categoryId, page, "Share"+XitiUtils.CHAPTER_SIGN + page + XitiUtils.CHAPTER_SIGN + share.name, XitiUtils.NAVIGATION);
                    context.startActivity(targetedShareIntent);
                }
            });

            // Custom Share: Create popup
            createPopup(shareView, context);
        } else {
            Toast.makeText(context, com.android701.R.string.no_app_for_share, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * To handle buttons in the real Android ActionBar.
     */
    public static void returnHome(Activity activity) {
        Intent intent = new Intent(activity, AdsListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
        EventTrackingUtils.sendClick(XitiUtils.getLevel2Map(XitiUtils.LEVEL2_OTHERS), "Actionbar_homeLogo", XitiUtils.NAVIGATION);
    }
}
