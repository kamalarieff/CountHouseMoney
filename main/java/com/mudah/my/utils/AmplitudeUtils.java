package com.mudah.my.utils;

import android.app.Activity;
import android.app.Application;

import com.amplitude.api.Amplitude;
import com.amplitude.api.Identify;
import com.google.gson.Gson;
import com.lib701.datasets.ACAd;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.models.UserAccountModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kalpana on 11/6/15.
 */
public class AmplitudeUtils {
    public static final String STEP1 = "step1";
    public static final String STEP2 = "step2";
    public static final String STEP3 = "step3";
    public static final String STEP4 = "step4";
    public static final String STEP5 = "step5";
    //Property
    public static final String REGION = "region";
    public static final String AREA = "area";
    public static final String CONDITION = "condition";
    public static final String CATEGORY = "Category";
    public static final String SUB_CATEGORY = "Sub Category";
    public static final String REPLY_TYPE = "Reply Type";
    public static final String AD_ID = "Ad ID";
    public static final String LIST_ID = "List ID";
    public static final String EMAIL = "email";
    public static final String SUBAREA = "subarea";
    public static final String AD_TYPE_NORMAL = "normal";
    public static final String AD_TYPE_R4U = "R4U";
    public static final String KEYWORD = "keyword";
    public static final String AD_TYPE = "type";
    //Event name
    //step 0
    public static final String SIGN_IN = "Sign In";
    public static final String START_APP = "Start App";
    public static final String TRACK_AD = "Ad View";
    public static final String INSERT_AD = "Ad Insert";
    public static final String REPLY_AD = "Ad Reply";
    //step 1
    public static final String LIST_EVENT = "List View";
    public static final String R4U_PV_EVENT = "R4U Page View";
    //step 2
    public static final String FAV_AD_EVENT = "Favourited Ad";
    public static final String SAVED_SEARCH_EVENT = "Saved Search";
    //step 3
    public static final String SAVED_DRAFT_EVENT = "Saved Ad Draft";
    public static final String CHECKED_OUT_FORM_INSERT_EVENT = "AI Form Open";
    //step 4
    public static final String VIEW_DASHBOARD_EVENT = "View Personal Dashboard";
    public static final String REGISTER_EVENT = "Register Account";
    public static final String CHAT_EVENT = "Initialized Chat";
    //step 5
    public static final String AD_DETAILS_INSERT_EVENT = "Ad Details Submitted in Ad Insert";
    public static final String PROFILE_INSERT_EVENT = "User Profile Submitted in Ad Insert";
    public static final String AD_PREVIEW_EVENT = "Ad Preview";

    public static final int TRACKING_ALL = 1;
    private static final long SESSION_TIME = 10 * 60 * 1000; //10 minutes

    //Initialize Amplitude
    public static void InitializeAmplitude(Activity activity, Application application) {
        if (Config.TrackAmplitude == TRACKING_ALL) {
            if (Log.isDebug)
                Config.AMPLITUDE_API_KEY = Config.AMPLITUDE_API_DEV_KEY;
            Amplitude.getInstance().initialize(activity, Config.AMPLITUDE_API_KEY, Config.deviceId)
                    .enableForegroundTracking(application)
                    .setMinTimeBetweenSessionsMillis(SESSION_TIME);
            Config.enableAmplitudeTracking = true;
        } else {
            Config.enableAmplitudeTracking = false;
        }
        Log.d("Config.enableAmplitudeTracking " + Config.enableAmplitudeTracking);
    }

    //Event Tracking for Reply Type
    public static void tagReply(ACAd ad, String replyType) {
        if (!Config.enableAmplitudeTracking)
            return;
        JSONObject eventProperties = getAdProperty(ad);
        try {
            eventProperties.put(REPLY_TYPE, replyType);
            Log.d(REPLY_AD + ", eventProperties: " + eventProperties);
        } catch (JSONException exception) {
        }
        Amplitude.getInstance().logEvent(REPLY_AD, eventProperties);
    }

    //Event Tracking
    public static void tagEventWithAdDetails(String eventName, AdViewAd ad) {
        if (!Config.enableAmplitudeTracking)
            return;
        boolean track = false;
        switch (eventName) {
            case FAV_AD_EVENT:
            case SAVED_SEARCH_EVENT:
                if (Config.amplitudeTrackingStep.optBoolean(STEP2))
                    track = true;
                break;
            case SAVED_DRAFT_EVENT:
                if (Config.amplitudeTrackingStep.optBoolean(STEP3))
                    track = true;
                break;
            case AD_DETAILS_INSERT_EVENT:
            case PROFILE_INSERT_EVENT:
            case AD_PREVIEW_EVENT:
                if (Config.amplitudeTrackingStep.optBoolean(STEP5))
                    track = true;
                break;
            default:
                track = true;

        }
        if (track) {
            JSONObject eventProperties = getAdProperty(ad);
            Log.d(eventName + ", eventProperties: " + eventProperties);
            Amplitude.getInstance().logEvent(eventName, eventProperties);
        }
    }

    public static void tagEventWithAdDetails(String eventName, AdViewAd ad, String type) {
        if (!Config.enableAmplitudeTracking)
            return;
        JSONObject eventProperties = getAdProperty(ad);

        try {
            if (!ACUtils.isEmpty(type))
                eventProperties.put(AD_TYPE, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(eventName + ", eventProperties: " + eventProperties);
        Amplitude.getInstance().logEvent(eventName, eventProperties);
    }

    public static void tagEventWithAdDetails(String eventName, AdViewAd ad, String searchKeyword, String type) {
        if (!Config.enableAmplitudeTracking || !Config.amplitudeTrackingStep.optBoolean(STEP1))
            return;
        JSONObject eventProperties = getAdProperty(ad);

        try {
            if (!ACUtils.isEmpty(searchKeyword))
                eventProperties.put(KEYWORD, searchKeyword);
            if (!ACUtils.isEmpty(type))
                eventProperties.put(AD_TYPE, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(eventName + ", list eventProperties: " + eventProperties);
        Amplitude.getInstance().logEvent(eventName, eventProperties);
    }

    public static JSONObject getAdProperty(ACAd ad) {
        JSONObject eventProperties = new JSONObject();
        try {
            if (ad != null) {
                eventProperties.put(CATEGORY, ad.getParentCategoryName());
                eventProperties.put(SUB_CATEGORY, ad.getCategoryName());
                eventProperties.put(REGION, ad.getRegion());
                eventProperties.put(AREA, ad.getSubRegionName());
                if (!ACUtils.isEmpty(ad.getCondition())) {
                    eventProperties.put(CONDITION, ad.getCondition());
                }

                if (!ACUtils.isEmpty(ad.getAdId())) {
                    eventProperties.put(AD_ID, ad.getAdId());
                }

                if (ad.getListId() > 0) {
                    eventProperties.put(LIST_ID, ad.getListId());
                }
            }
        } catch (JSONException exception) {
        }
        return eventProperties;
    }

    public static void tagEvent(String eventName) {
        if (!Config.enableAmplitudeTracking)
            return;
        boolean track = false;
        switch (eventName) {
            case R4U_PV_EVENT:
                if (Config.amplitudeTrackingStep.optBoolean(STEP1))
                    track = true;
                break;
            case CHECKED_OUT_FORM_INSERT_EVENT:
                if (Config.amplitudeTrackingStep.optBoolean(STEP3))
                    track = true;
                break;
            case VIEW_DASHBOARD_EVENT:
            case REGISTER_EVENT:
                if (Config.amplitudeTrackingStep.optBoolean(STEP4))
                    track = true;
                break;
            default:
                track = true;

        }
        if (track) {
            Log.d(eventName);
            Amplitude.getInstance().logEvent(eventName);
        }
    }

    public static void trackAllUserProperties() {
        if (!Config.enableAmplitudeTracking)
            return;
        try {
            Gson gson = new Gson();
            JSONObject userData = new JSONObject(gson.toJson(Config.userAccount));
            userData.remove(UserAccountModel.TOKEN);
            userData.remove(UserAccountModel.PASSWORD);
            if (!ACUtils.isEmpty(Config.userAccount.getRegion())) {
                userData.put(REGION, MudahUtil.getRegionName(Config.userAccount.getRegion()));
            }
            if (!ACUtils.isEmpty(Config.userAccount.getSubarea())) {
                userData.put(SUBAREA, MudahUtil.getSubAreaName(Config.userAccount.getSubarea()));
            }
            Log.d("data: " + userData);
            //use userId instead of userAccountId so that this will be the same as userId in InsertAd when users no log in
            Amplitude.getInstance().setUserId(Config.userAccount.getUserId())
                    .setUserProperties(userData);
        } catch (JSONException e) {
            Log.e("Track login event hit error " + e.getMessage());
        }
    }

    public static void tagUserId(String userId) {
        if (!Config.enableAmplitudeTracking)
            return;
        Log.d("userId: " + userId);
        Amplitude.getInstance().setUserId(userId);
    }

    public static void trackUserProperties(String key, String value) {
        if (!Config.enableAmplitudeTracking)
            return;
        try {
            JSONObject userData = new JSONObject();
            userData.put(key, value);
            Log.d("data: " + userData);
            Amplitude.getInstance()
                    .setUserProperties(userData);
        } catch (JSONException e) {
            Log.e("Track login event hit error " + e.getMessage());
        }
    }

    public static void incrementProperty(String property) {
        if (!Config.enableAmplitudeTracking)
            return;
        boolean track = false;
        switch (property) {
            case CHAT_EVENT:
                if (Config.amplitudeTrackingStep.optBoolean(STEP4))
                    track = true;
                break;
            default:
                track = true;
        }

        if (track) {
            Log.d("add 1 to " + property);
            Identify identify = new Identify().add(property, 1);
            Amplitude.getInstance().identify(identify);
        }
    }

    public static String getViewType(String viewType){
        if (ACUtils.isEmpty(viewType)) {
            viewType = AD_TYPE_NORMAL;
        }

        return viewType;
    }
}
