package com.mudah.my.utils;

import android.app.Activity;
import android.content.Context;

import com.lib701.datasets.ACAd;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.models.UserAccountModel;
import com.tealium.library.Tealium;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Siripin K. on 23/7/15.
 */
public class EventTrackingUtils {

    public static void sendTagWithMode(int mode, Context context, String page, String level, Map<String, String> mapX) {
        XitiUtils.sendTagWithMode(mode, context, page, level, mapX);
    }

    public static void sendTagWithMode(int mode, Context context, String page, int level, Map<String, String> mapX) {
        XitiUtils.sendTagWithMode(mode, context, page, level, mapX);
    }

    public static void sendUATagWithMode(int mode, Activity activity, String page, String level, UserAccountModel userAccountModel) {
        Map mapX = prepareMapUserAccountPage(page, userAccountModel);
        XitiUtils.sendTagWithMode(mode, activity.getApplicationContext(), page, level, mapX);
        TealiumHelper.tagTealiumUserAccountPage(activity, page, userAccountModel);
    }

    public static void sendClickByCategoryId(Map<String, String> moreDataTealium, int categoryId, String page, String name, String type) {
        XitiUtils.sendClickByCategoryId(categoryId, page, name, type);

        int level = XitiUtils.getLevel2ByCategoryIDAndPage(categoryId, page);
        // prevent data in "moreDataTealium" got modified, in case it needs to be reused in the caller
        Map<String, String> copyOfMoreDataTealium;
        if (moreDataTealium == null) {
            copyOfMoreDataTealium = new HashMap<>();
        } else {
            copyOfMoreDataTealium = new HashMap<>(moreDataTealium);
        }
        copyOfMoreDataTealium.put(TealiumHelper.CATEGORY_ID, categoryId + Constants.EMPTY_STRING);
        tagTealiumEvent(copyOfMoreDataTealium, level + Constants.EMPTY_STRING, name, type);
    }

    public static void sendUAClick(String activityName, String event) {
        String eventName = prepareUserAccountEventName(activityName, event);
        sendClick(XitiUtils.LEVEL2_UA_SITE_ID, eventName, XitiUtils.NAVIGATION);
    }

    public static void sendClick(String level, String name, String type) {
        XitiUtils.sendClick(level, name, type);
        tagTealiumEvent(null, level, name, type);
    }

    private static void tagTealiumEvent(Map<String, String> moreDataTealium, String level, String eventName, String type) {
        String clickType = type.substring(0, 1).toUpperCase();//send (A)ction or (N)avigation
        Map<String, String> dataTealium = Tealium.map(
                TealiumHelper.PAGE_NAME, eventName,
                TealiumHelper.CLICK_TYPE, clickType,
                TealiumHelper.XTN2, level);

        if (moreDataTealium != null && moreDataTealium.size() > 0) {
            moreDataTealium.putAll(dataTealium); //make sure dataTealium overwrite data in moreDataTealium
        } else {
            moreDataTealium = dataTealium;
        }

        TealiumHelper.track(null, moreDataTealium, Tealium.EVENT);
    }

    public static void sendAdReply(String name, ACAd acAd) {
        XitiUtils.sendAdReply(name, acAd);
    }

    public static void sendCampaign(Context context, int campaignKey, boolean impressionOrClick) {
        String campaignId = XitiUtils.getCampaignId(campaignKey);
        if (!ACUtils.isEmpty(campaignId)) {
            XitiUtils.sendCampaign(context, campaignId, impressionOrClick);
            Map<String, String> dataTealium = Tealium.map(
                    TealiumHelper.CAMPAIGN_ID, campaignId,
                    TealiumHelper.IMPRESSION_OR_CLICK, impressionOrClick + Constants.EMPTY_STRING);

            TealiumHelper.track(null, dataTealium, Tealium.EVENT);
        }
    }

    public static void sendPublisherTag(Context context, int pageNo, String pageName, Map<String, String> mapX) {
        XitiUtils.sendPublisherTag(context, pageNo, pageName, mapX);

        Map<String, String> dataTealium = Tealium.map(
                TealiumHelper.AI_ATC, "true",
                TealiumHelper.AI_CATEGORY, XitiUtils.OPEN_TAG + mapX.get(XitiUtils.CATEGORY) + XitiUtils.CLOSE_TAG,
                TealiumHelper.AI_LOCATION, XitiUtils.OPEN_TAG + mapX.get(XitiUtils.LOCATION) + XitiUtils.CLOSE_TAG,
                TealiumHelper.AI_HEADING_DESCRIPTION_PRICE, XitiUtils.OPEN_TAG + mapX.get(XitiUtils.COMMON_FIELDS) + XitiUtils.CLOSE_TAG,
                TealiumHelper.AI_PHOTO, XitiUtils.OPEN_TAG + mapX.get(XitiUtils.PHOTO) + XitiUtils.CLOSE_TAG,
                TealiumHelper.AI_CATEGORY_PARAMS_FILLED, XitiUtils.OPEN_TAG + mapX.get(XitiUtils.CATEGORY_PARAMS) + XitiUtils.CLOSE_TAG,
                TealiumHelper.AI_AD_TYPE_CATEGORY_PARAMS_FILLED_FIELDS, XitiUtils.OPEN_TAG + mapX.get(XitiUtils.CATEGORY_PARAMS_FIELDS) + XitiUtils.CLOSE_TAG,
                TealiumHelper.AI_PAGE_NAME, pageNo + XitiUtils.OPEN_TAG + pageName + XitiUtils.CLOSE_TAG,
                TealiumHelper.IMPRESSION_OR_CLICK, "false");

        TealiumHelper.track(null, dataTealium, Tealium.EVENT);
    }

    public static void sendLevel2CustomVariableByName(String level2siteName, String page, String key, String message) {
        XitiUtils.sendLevel2CustomVariableByName(level2siteName, page, key, message);

        Map<String, String> mapFormParams = new HashMap<>();
        String mapKey = XitiUtils.getFormKeyByVariableName(level2siteName, key);

        if (!ACUtils.isEmpty(mapKey)) {
            mapFormParams.put(mapKey, message);
            sendLevel2CustomVariable(level2siteName, page, mapFormParams);
        }
    }

    public static void sendLevel2CustomVariable(String level2siteName, String page, Map<String, String> mapFormParams) {
        XitiUtils.sendLevel2CustomVariable(level2siteName, page, mapFormParams);

        String insertAdLevel2 = XitiUtils.getLevel2Map(level2siteName);
        Map<String, String> dataTealium = Tealium.map(
                TealiumHelper.PAGE_NAME, page);
        if (!ACUtils.isEmpty(insertAdLevel2) && mapFormParams != null && mapFormParams.size() > 0) {
            dataTealium.put(TealiumHelper.XTN2, insertAdLevel2);
            Log.d("page: " + page);
            for (Map.Entry<String, String> entry : mapFormParams.entrySet()) {
                Log.d("          with f" + entry.getKey() + ": " + entry.getValue());
                dataTealium.put(TealiumHelper.XITI_FORM + entry.getKey(), entry.getValue());
            }

            TealiumHelper.track(null, dataTealium, Tealium.VIEW);
        }
    }

    public static Map prepareMapUserAccountPage(String pageName, UserAccountModel userAccount) {
        Map<String, String> xitiMapX = new HashMap<>();
        if (userAccount != null) {
            if (userAccount.isLogin()) {
                if (!ACUtils.isEmpty(userAccount.getUserId())) {
                    xitiMapX.put(XitiUtils.CUST_VAR_USER_ID, userAccount.getUserId());
                }
                if (!ACUtils.isEmpty(userAccount.getEmail())) {
                    xitiMapX.put(XitiUtils.CUST_VAR_USER_EMAIL, userAccount.getEmail());
                }
            } else if ((TealiumHelper.PAGE_UA_SIGNUP_EMAIL_SENT).equalsIgnoreCase(pageName)
                    || (TealiumHelper.PAGE_UA_SIGNUP_ACTIVATED).equalsIgnoreCase(pageName)
                    || (TealiumHelper.PAGE_UA_RESET_PASSWORD_EMAIL_SENT).equalsIgnoreCase(pageName)
                    || (TealiumHelper.PAGE_UA_FORGOT_PASSWORD_EMAIL_SENT).equalsIgnoreCase(pageName)) {
                if (!ACUtils.isEmpty(userAccount.getEmail())) {
                    xitiMapX.put(XitiUtils.CUST_VAR_USER_EMAIL, userAccount.getEmail());
                }
            }
        }

        Log.d("UserAccount xitiMapX " + xitiMapX);
        return xitiMapX;
    }

    public static String prepareUserAccountEventName(String activityName, String event) {
        return TealiumHelper.UA + XitiUtils.CHAPTER_SIGN + activityName + XitiUtils.CHAPTER_SIGN + event;
    }
}
