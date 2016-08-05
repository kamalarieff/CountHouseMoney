package com.mudah.my.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AppsFlyerLib;
import com.lib701.datasets.ACAd;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moehninhtwee on 4/12/14.
 */
public class AppsFlyerUtils extends Application {
    public static final String APPS_FLYER_DEV_KEY = "yr2k3ZZT5bxPzU5a69Yfng";
    public static final String APPS_FLYER_CURRENCY = "SGD";
    public static final String DELIMITER = ";";

    public static void init(Application context) {
        if (!Config.enableAppsFlyer)
            return;
        AppsFlyerLib.getInstance().setAndroidIdData(Config.androidID);
        AppsFlyerLib.getInstance().setGCMProjectNumber(context.getString(R.string.gcm_sender_id));
        AppsFlyerLib.getInstance().startTracking(context, APPS_FLYER_DEV_KEY);
        AppsFlyerLib.getInstance().setCurrencyCode(APPS_FLYER_CURRENCY);
        if (Log.isDebug) {
            AppsFlyerLib.getInstance().setDebugLog(true);
        }
    }

    public static void trackEvent(Context context, String eventName, Map<String, Object> eventValues) {
        if (!Config.enableAppsFlyer)
            return;
        AppsFlyerLib.getInstance().trackEvent(context, eventName, eventValues);
    }

    public static void sendConversionTag(Context context, AppsFlyerTags tag, ACAd acAd) {
        if (!Config.enableAppsFlyer)
            return;
        Map<String, Object> appsFlyerParams = new HashMap<>();
        appsFlyerParams.put(AFInAppEventParameterName.CONTENT_TYPE, acAd.getCategoryName());
        appsFlyerParams.put(AFInAppEventParameterName.DESTINATION_A, acAd.getRegion());
        sendConversionTag(context, tag, appsFlyerParams);
    }

    public static void sendConversionTag(Context context, AppsFlyerTags tag, String categoryName, String regionName) {
        if (!Config.enableAppsFlyer)
            return;
        Map<String, Object> appsFlyerParams = new HashMap<>();
        appsFlyerParams.put(AFInAppEventParameterName.CONTENT_TYPE, categoryName);
        appsFlyerParams.put(AFInAppEventParameterName.DESTINATION_A, regionName);
        sendConversionTag(context, tag, appsFlyerParams);
    }

    public static void sendConversionTag(Context context, AppsFlyerTags tag, Map<String, Object> appsFLyerParams) {
        if (!Config.enableAppsFlyer)
            return;
        String tagName = "";
        String revenue = "0";

        switch (tag) {
            case PROD_VIEW:
                tagName = "Product view" + DELIMITER + "Ad view";
                revenue = "0.02";
                break;
            case AR_MARKETPLACE_APP:
                tagName = "AR_Marketplace_App";
                revenue = "0.04";
                break;
            case AR_CARS_APP:
                tagName = "AR_Cars_App";
                revenue = "0.04";
                break;
            case CHAT_MARKETPLACE_APP:
                tagName = "Chat_Marketplace_App";
                revenue = "0.04";
                break;
            case CHAT_CARS_APP:
                tagName = "Chat_Cars_App";
                revenue = "0.04";
                break;
            case AI_MARKETPLACE_APP:
                tagName = "AI_Marketplace_App";
                revenue = "0.38";
                break;
            case AI_CARS_APP:
                tagName = "AI_Cars_App";
                revenue = "0.38";
                break;
        }
        if (!ACUtils.isEmpty(tagName)) {
            if (appsFLyerParams == null) {
                appsFLyerParams = new HashMap<>();
            }
            appsFLyerParams.put(AFInAppEventParameterName.REVENUE, revenue);
            Log.d("tagName: " + tagName + ", value: " + appsFLyerParams.toString());
            trackEvent(context, tagName, appsFLyerParams);
        }
    }

    public static void sendDeepLinkData(Activity activity) {
        AppsFlyerLib.getInstance().sendDeepLinkData(activity);
    }

    public enum AppsFlyerTags {
        PROD_VIEW, AR_MARKETPLACE_APP, AR_CARS_APP, CHAT_MARKETPLACE_APP,
        CHAT_CARS_APP, AI_MARKETPLACE_APP, AI_CARS_APP
    }

}
