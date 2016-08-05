package com.mudah.my.helpers;

import com.kahuna.sdk.EventBuilder;
import com.kahuna.sdk.IKahuna;
import com.kahuna.sdk.Kahuna;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.models.UserAccountModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pin on 14/3/16.
 */
public class KahunaHelper {
    public static final String CATEGORY = "category";
    public static final String SUB_CATEGORY = "sub-category";
    public static final String REGION = "region";
    public static final String AREA = "area";
    public static final String SPACE = " ";

    //Event name
    public static final String ADVIEW_EVENT = "Last viewed ad";
    public static final String FAV_EVENT = "Last favourited ad";
    public static final String SAVED_SEARCH_EVENT = "Last saved search";
    public static final String SEARCHED_KEYWORD_EVENT = "Last searched keyword";
    public static final String AD_REPLY_EVENT = "Last replied ad";
    public static final String CHECKED_OUT_FORM_INSERT_EVENT = "Last checked out insert ad form";
    public static final String SUBMITTED_DETAIL_INSERT_EVENT = "Last submitted insert ad detail";
    public static final String SUBMITTED_PROFILE_INSERT_EVENT = "Last submitted user profile";
    public static final String PREVIEWED_INSERT_EVENT = "Last previewed ad";
    public static final String LAST_POSTED_INSERT_EVENT = "Last posted ad";
    public static final String LAST_SAVED_DRAFT_INSERT_EVENT = "Last saved ad draft";
    public static final String REGISTERED_ACCOUNT = "Register account";
    public static final String LAST_VIEWED_PERSONAL_DASHBOARD = "Last viewed personal dashboard";
    public static final String LAST_CHATTED = "Last chatted";
    public static final String LAST_SAVED_PROFILE = "Last saved profile";
    public static final String LAST_VIEW_R4U_PAGE = "Last viewed R4U page";
    public static final String LAST_VIEW_R4U_AD = "Last viewed R4U ad";
    //Attribute
    public static final String APP_VERSION = "App version";
    public static final String LISTER_NAME = "Lister name";
    public static final String LAST_TITLE_SAVED_DRAFT = "Last ad title saved as draft";
    public static final String LAST_TITLE_IN_PROGRESS = "Last ad title WIP";
    public static final String LAST_TITLE_POSTED = "Last ad title posted";
    public static final String LAST_TITLE_VIEWED = "Last ad title viewed";
    public static final String LAST_TITLE_REPLIED = "Last ad title replied";
    public static final String LAST_TITLE_FAV = "Last ad title favourited";
    public static final String LAST_TITLE_SEARCH = "Last keyword searched";
    public static final String LAST_DATE_AD_POSTED = "Last date of ad posted";
    public static final String PAGE_AD_VIEWED = " ad viewed";
    public static final String PAGE_AD_REPLIED = " ad replied";
    public static final String PAGE_AD_FAV = " ad favourited";
    public static final String PAGE_SEARCH_SAVED = " search saved";
    public static final String PAGE_KW_SEARCHED = " keyword searched";
    public static final String PAGE_USER_ACCOUNT = " user account";
    public static final String LAST_DATE = "Last date of";
    public static final String LOGIN_STATUS = "Logged In Status";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String USER_NAME = "User Name";
    public static final String BIRTH_MONTH = "Birth Month";
    public static final String BIRTH_YEAR = "Birth Year";
    public static final String USER_REGION = "User Region";
    public static final String USER_AREA = "User Area";
    public static final String USER_GENDER = "User Gender";

    public static final String LAST_CATEGORY = "Last category";
    public static final String LAST_SUB_CATEGORY = "Last sub-category";
    public static final String LAST_REGION = "Last region";
    public static final String LAST_AREA = "Last area";
    public static final String LAST_DASHBOARD_VIEWED = " Personal Dashboard Viewed";
    public static final String LAST_CHAT = " Chat";
    public static final String VIEWING_R4U_PAGE = " Viewing R4U page";

    public static final String ACTION_VIEW = " viewed";
    public static final String ACTION_DRAFT = " draft";
    public static final String ACTION_POSTED = " posted";
    public static final String ACTION_FAV = " favourited";

    private static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");

    public static void tagInsertAdAttributes(String titleKey, AdViewAd acAd) {
        if (acAd == null)
            return;

        Map<String, String> attributes = new HashMap<>();
        if (!ACUtils.isEmpty(acAd.getName()))
            attributes.put(LISTER_NAME, acAd.getName());
        if (!ACUtils.isEmpty(acAd.getSubject()))
            attributes.put(titleKey, acAd.getSubject());

        if (LAST_TITLE_POSTED.equals(titleKey)) {
            attributes.put(LAST_DATE_AD_POSTED, iso8601Format.format(new Date()));
        }

        Log.d(" { " + LISTER_NAME + ": " + attributes + "}");
        Kahuna.getInstance().setUserAttributes(attributes);
    }

    public static void tagAttributes(String page, String keyAttribute, String valueAttribute) {

        Map<String, String> attributes = new HashMap<>();
        attributes.put(keyAttribute, valueAttribute);

        attributes.put(LAST_DATE + page, iso8601Format.format(new Date()));

        Log.d(page + ": " + attributes);
        Kahuna.getInstance().setUserAttributes(attributes);
    }

    public static void tagDateAttributes(String page) {

        Map<String, String> attributes = new HashMap<>();
        attributes.put(LAST_DATE + page, iso8601Format.format(new Date()));

        Log.d(page + ": " + attributes);
        Kahuna.getInstance().setUserAttributes(attributes);
    }

    public static void tagUserAttributes(String data) {

        Map<String, String> attributes = new HashMap<>();
        attributes.put(APP_VERSION, data);

        Log.d(data + ": " + attributes);
        Kahuna.getInstance().setUserAttributes(attributes);
    }

    public static void tagProfileAttributes(Map data) {

        ACSettings acSettings = ACSettings.getACSettings();

        Map<String, String> attributes = new HashMap<>();
        String fullname = "";
        if (data.containsKey(UserAccountModel.FIRST_NAME)) {
            fullname += data.get(UserAccountModel.FIRST_NAME).toString();
        }
        if (data.containsKey(UserAccountModel.LAST_NAME)) {
            if (!(ACUtils.isEmpty(fullname))) {
                fullname += SPACE;
            }
            fullname += data.get(UserAccountModel.LAST_NAME).toString();
        }

        if (!(ACUtils.isEmpty(fullname))) {
            attributes.put(USER_NAME, fullname);
        }
        if (data.containsKey(UserAccountModel.BIRTHMONTH) && data.get(UserAccountModel.BIRTHMONTH) != null) {
            attributes.put(BIRTH_MONTH, data.get(UserAccountModel.BIRTHMONTH).toString());
        }
        if (data.containsKey(UserAccountModel.BIRTHYEAR) && data.get(UserAccountModel.BIRTHYEAR) != null) {
            attributes.put(BIRTH_YEAR, data.get(UserAccountModel.BIRTHYEAR).toString());
        }
        if (data.containsKey(UserAccountModel.REGION) && data.get(UserAccountModel.REGION) != null) {
            attributes.put(USER_REGION, acSettings.getRegionName(data.get(UserAccountModel.REGION).toString()));
        }
        if (data.containsKey(UserAccountModel.SUBAREA) && data.get(UserAccountModel.SUBAREA) != null) {
            attributes.put(USER_AREA, acSettings.getMunicipalityName(data.get(UserAccountModel.SUBAREA).toString()));
        }
        if (data.containsKey(UserAccountModel.GENDER) && data.get(UserAccountModel.GENDER) != null) {
            attributes.put(USER_GENDER, data.get(UserAccountModel.GENDER).toString());
        }
        if (attributes != null) {
            Kahuna.getInstance().setUserAttributes(attributes);
        }
    }

    public static void tagEventWithAttribute(String event, AdViewAd acAd, String action) {
        if (acAd == null)
            return;
        IKahuna kahunaObject = Kahuna.getInstance();
        EventBuilder eventBuilder = getCommonMap(event, acAd);

        kahunaObject.track(eventBuilder.build());

        //Attribute
        Map<String, String> attributes = getCommonAttributesMap(acAd, action);
        Kahuna.getInstance().setUserAttributes(attributes);
    }

    public static void tagEvent(String event) {
        IKahuna kahunaObject = Kahuna.getInstance();
        kahunaObject.trackEvent(event);
        Log.d(" eventName " + event);
    }

    public static void tagEvent(String event, AdViewAd acAd) {
        if (acAd == null)
            return;
        IKahuna kahunaObject = Kahuna.getInstance();
        EventBuilder eventBuilder = getCommonMap(event, acAd);

        kahunaObject.track(eventBuilder.build());
    }

    private static EventBuilder getCommonMap(String event, AdViewAd acAd) {
        EventBuilder eventBuilder = new EventBuilder(event);

        if (!ACUtils.isEmpty(acAd.getParentCategoryName()))
            eventBuilder.addProperty(CATEGORY, acAd.getParentCategoryName());
        if (!ACUtils.isEmpty(acAd.getCategoryName()))
            eventBuilder.addProperty(SUB_CATEGORY, acAd.getCategoryName());
        if (!ACUtils.isEmpty(acAd.getRegion()))
            eventBuilder.addProperty(REGION, acAd.getRegion());
        if (!ACUtils.isEmpty(acAd.getSubRegionName()))
            eventBuilder.addProperty(AREA, acAd.getSubRegionName());

        Log.d(event + " {category: " + acAd.getParentCategoryName() + ", sub-cat: " + acAd.getCategoryName() + ", region: " + acAd.getRegion() + ", area: " + acAd.getSubRegionName() + "}");
        return eventBuilder;
    }

    private static Map<String, String> getCommonAttributesMap(AdViewAd acAd, String action) {
        Map<String, String> attributes = new HashMap<>();

        if (!ACUtils.isEmpty(acAd.getParentCategoryName()))
            attributes.put(LAST_CATEGORY + action, acAd.getParentCategoryName());
        if (!ACUtils.isEmpty(acAd.getCategoryName()))
            attributes.put(LAST_SUB_CATEGORY + action, acAd.getCategoryName());
        if (!ACUtils.isEmpty(acAd.getRegion()))
            attributes.put(LAST_REGION + action, acAd.getRegion());
        if (!ACUtils.isEmpty(acAd.getSubRegionName()))
            attributes.put(LAST_AREA + action, acAd.getSubRegionName());

        Log.d("Last viewed {category: " + acAd.getParentCategoryName() + ", sub-cat: " + acAd.getCategoryName() + ", region: " + acAd.getRegion() + ", area: " + acAd.getSubRegionName() + "}");
        return attributes;
    }
}
