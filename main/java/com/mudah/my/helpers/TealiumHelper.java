package com.mudah.my.helpers;

import android.app.Activity;
import android.app.Application;

import com.chatcafe.sdk.core.CCMessage;
import com.chatcafe.sdk.core.CCRoom;
import com.lib701.datasets.ACAd;
import com.lib701.datasets.ACAdParameter;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.activities.ChatActivity;
import com.mudah.my.adapters.InsertAdCategoryParamsAdapter.PostedBy;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.UserAccountModel;
import com.mudah.my.utils.MudahUtil;
import com.tealium.library.Tealium;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pin on 31/8/15.
 */
public class TealiumHelper {
    public static final String ANDROID = "android";
    public static final String ACCOUNT_NAME = "schibsted";
    public static final String PROFILE_NAME = "mudah-mobile";
    public static final String ENVIRONMENT_DEV_NAME = "dev";
    public static final String ENVIRONMENT_PROD_NAME = "prod";
    public static final String DEVELOPMENT = "development";
    public static final String PRODUCTION = "production";
    //Tealium UDOs
    public static final String USER_AGENT = "user_agent";
    public static final String PLATFORM = "platform";
    public static final String ENVIRONMENT = "environment";
    public static final String APPLICATION = "application";
    public static final String PAGE_NAME = "page_name";
    public static final String REGION_ID = "region_id";
    public static final String REGION_NAME = "region_name";
    public static final String REGION_SCOPE = "region_scope";
    public static final String SUBREGION_ID = "subregion_id";
    public static final String SUBREGION_NAME = "subregion_name";
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String AD_TYPE = "ad_type";
    public static final String LIST_ID = "list_id";
    public static final String ITEM_ID = "item_id"; // same as list id
    public static final String AD_ID = "ad_id";
    public static final String MEMBER_ID = "member_id";
    public static final String TOKEN = "private_token";
    public static final String AD_REPLY_TYPE = "ad_reply_type";
    public static final String MESSAGE_BY_WHATSAPP = "message_by_whatsapp";
    public static final String HIDE_PHONE_NUMBER = "hide_phone_number";
    public static final String MUDAH_MAILING_LIST = "mudah_mailing_list";
    public static final String MUDAH_AND_PARTNERS_MAILING_LIST = "mudah_and_partners_mailing_list";
    public static final String NUMBER_OF_PHOTOS = "number_of_photos";
    public static final String AD_SELLER_TYPE = "ad_seller_type";
    public static final String AD_TITLE = "ad_title";
    public static final String STORE_ID = "store_id";
    public static final String KEYWORD = "keyword";
    public static final String SEARCH_TITLE_ONLY = "search_title_only";
    public static final String PAGE_NUMBER = "page_number";
    public static final String SELLER_TYPE = "seller_type";
    public static final String SORT_TYPE = "sort_type";
    public static final String VIEW_TYPE = "view_type";
    public static final String TIMESTAMP = "timestamp";
    public static final String XTN2 = "xtn2";
    public static final String USER_ID = "user_id";
    public static final String USER_ACCOUNT_ID = "user_account_id";
    public static final String USER_TYPE = "user_type";
    public static final String USER_TYPE_NEW = "New";
    public static final String USER_TYPE_RETURN = "Return";
    public static final String AD_ACTION_TYPE = "ad_action_type";
    public static final String CERTIFIED = "certified";
    public static final String COMPANY_ROC = "company_roc";
    public static final String CLICK_TYPE = "xiti_click_type";
    public static final String CLICK_TYPE_ACTION = "A";
    public static final String EVENT_NAME = "event_name";
    public static final String CAMPAIGN_ID = "campaign_id";
    public static final String IMPRESSION_OR_CLICK = "impression_or_click";
    public static final String AI_ATC = "ai_atc";
    public static final String AI_CATEGORY = "ai_category";
    public static final String AI_LOCATION = "ai_location";
    public static final String AI_HEADING_DESCRIPTION_PRICE = "ai_heading_description_price";
    public static final String AI_PHOTO = "ai_photo";
    public static final String AI_CATEGORY_PARAMS_FILLED = "ai_category_params_filled";
    public static final String AI_AD_TYPE_CATEGORY_PARAMS_FILLED_FIELDS = "ai_ad_type_category_params_filled_fields";
    public static final String AI_PAGE_NAME = "ai_page_name";
    public static final String XITI_FORM = "xiti_f";
    public static final String NUMBER_OF_ADS = "number_of_ads";
    public static final String PARENT_CATEGORY_ID = "parent_category_id";
    public static final String PARENT_CATEGORY_NAME = "parent_category_name";
    public static final String DEVICE_ID = "device_id";
    public static final String UUID = "uuid";
    public static final String FAVOURITES_TYPE = "favourites_type";
    public static final String FAVOURITES_ADS = "ads";
    public static final String FAVOURITES_SEARCHES = "searches";
    public static final String YES = "1";
    public static final String NO = "";
    public static final String ROOM_ID = "room_id";
    public static final String TRANSACTION_TYPE = "transaction_type";
    public static final String CHAT_CREATE_ROOM = "Chat_create_room";
    public static final String CHAT_SEND_FIRST_MESSAGE = "Chat_send_first_message";
    public static final String CHAT_SEND_FIRST_IMAGE = "Chat_send_first_image";
    public static final String CHAT_SEND_MESSAGE = "Chat_send_message";
    public static final String CHAT_SEND_IMAGE = "Chat_send_image";
    public static final String CHAT_RECEIVE_MESSAGE = "Chat_receive_message";
    public static final String CHAT_RECEIVE_IMAGE = "Chat_receive_image";
    public static final String DELETE_ROOM = "Delete_room";
    public static final String CHAT_MUTE_NOTIFICATION = "Chat_mute_notification";
    public static final String CHAT_UNMUTE_NOTIFICATION = "Chat_unmute_notification";
    public static final String CHAT_BLOCK_USER = "Chat_block_user";
    public static final String CHAT_UNBLOCK_USER = "Chat_unblock_user";
    public static final String CHAT_REPORT_USER = "Chat_report_user";
    public static final String USER_STATUS = "chat_user_type";
    public static final String USER_STATUS_BUYER = "buyer";
    public static final String USER_STATUS_SELLER = "seller";
    public static final String CLICK_TYPE_NAVIGATION = "N";
    public static final String CLICK_NAME = "click_name";
    public static final String RECEIVER_ID = "receiver_id";
    public static final String SENDER_ID = "sender_id";
    public static final String OBJECT_ID = "object_id";
    public static final String MESSAGE = "chat_message";
    public static final String IMAGE_URL = "image_url";
    public static final String ROOMS_DELETED = "rooms_deleted";
    public static final String DELETED_USER_ID = "deleted_user_id";
    public static final String SELLER_ID = "seller_id";
    public static final String BUYER_ID = "buyer_id";
    public static final String TEXT = "text";
    public static final String ATTACHMENTS = "attachments";
    public static final String DATA = "data";
    public static final String THUMBNAIL_URL = "thumbnail_url";
    public static final String UNIQUE_ID = "unique_id";
    public static final String MUTED_USER_ID = "muted_user_id";
    public static final String UNMUTED_USER_ID = "unmuted_user_id";
    public static final String BLOCKED_USER_ID = "blocked_user_id";
    public static final String UNBLOCKED_USER_ID = "unblocked_user_id";
    public static final String REPORT_REASON = "report_reason";
    public static final String REPORTED_USER_ID = "reported_user_id";
    public static final String CHAT_ROOM = "Chat Room";
    public static final String INBOX = "Inbox";
    public static final String CHAT_ICON_ACTION_BAR = "Chat_inbox_action_bar_icon";
    public static final String CHAT_ICON_NAVIGATION_DRAWER = "Chat_inbox_navigation_drawer";
    public static final String CHAT_DELETE_ICON = "Chat_delete_icon";
    public static final String CHAT_INAPP_NOTIFICATION = "Chat_inapp_notification";
    public static final String CHAT_WITH_BUYER = "Chat_with_buyer";
    public static final String CHAT_WITH_SELLER = "Chat_with_seller";
    public static final String CHAT_WITH_ALL = "Chat_with_all";
    public static final String MUTE = "mute";
    public static final String UNMUTE = "unmute";
    public static final String BLOCK = "block";
    public static final String UNBLOCK = "unblock";
    public static final String REPORT = "report";

    //application
    public static final String APPLICATION_CHAT = "chat";
    public static final String APPLICATION_VIEW = "view";
    public static final String APPLICATION_LIST = "list";
    public static final String APPLICATION_BETA_USR = "beta_users";
    public static final String APPLICATION_SEND_MAIL = "sendmail";
    public static final String APPLICATION_REPORT = "report";
    public static final String APPLICATION_SUPPORT = "support";
    public static final String APPLICATION_FAV = "favourites";
    public static final String APPLICATION_SAVED_SEARCHES = "saved_searches";
    public static final String APPLICATION_AI = "ai";
    public static final String APPLICATION_UA = "user_account";
    public static final String UA = "UserAccount";
    //User Account PageNames
    public static final String PAGE_UA_SIGNIN = "ua_sign_in";
    public static final String PAGE_UA_SIGNUP_INTRO = "ua_sign_up_intro";
    public static final String PAGE_UA_SIGNUP = "ua_sign_up";
    public static final String PAGE_UA_SIGNUP_EMAIL_SENT = "ua_sign_up_email_sent";
    public static final String PAGE_UA_SIGNUP_ACTIVATED = "ua_sign_up_activated";
    public static final String PAGE_UA_TERMS_CONDITIONS = "ua_terms_and_conditions";
    public static final String PAGE_UA_PRIVACY_POLICY = "ua_privacy_policy";
    public static final String PAGE_UA_FORGOT_PASSWORD = "ua_forgot_password";
    public static final String PAGE_UA_FORGOT_PASSWORD_EMAIL_SENT = "ua_forgot_password_email_sent";
    public static final String PAGE_UA_RESET_PASSWORD = "ua_reset_password";
    public static final String PAGE_UA_RESET_PASSWORD_EMAIL_SENT = "ua_reset_password_email_sent";
    public static final String PAGE_UA_LIVE_ADS = "ua_live_ads";
    public static final String ACTIVITY_HOMEPAGE = "Homepage";
    public static final String ACTIVITY_ADVIEW = "AdView";
    public static final String ACTIVITY_LIVEADS = "LiveAds";
    public static final String ACTIVITY_SIGNIN = "SignIn";
    public static final String ACTIVITY_SIGNUP = "SignUp";
    public static final String ACTIVITY_SIGNUP_INTRO = "SignUp_Intro";
    public static final String DRAWER = "Drawer";
    public static final String EVENT_SIGNIN = "SignIn";
    public static final String EVENT_SIGNUP = "SignUp";
    public static final String EVENT_LOGOUT = "LogOut";
    public static final String EVENT_CHAT = "Chat";
    public static final String EVENT_VIEW = "View";
    public static final String EVENT_LIVEADS = "LiveAds";
    public static final String EVENT_SIGNUP_LINK = "SignUp_link";
    public static final String EVENT_SIGNUP_MENU = "SignUp_menu";
    public static final String EVENT_SIGNIN_LINK = "SignIn_link";
    public static final String EVENT_NO_THX = "No_thanks";
    public static final String EVENT_SIGNIN_MENU = "SignIn_menu";
    public static final String EVENT_FORGOT_PASSWORD_LINK = "Forgot_password_link";
    public static final String USER_EMAIL = "ua_email";
    public static final String UNIVERSAL = "universal";
    public static final String ADDITIONAL = "additional";
    private static final String AD_SELLER_PRIVATE = "1";
    private static final String AD_SELLER_PROFESSIONAL = "2";
    private static final String AD_SELLER_COMPANY = "3";
    private static final String LABEL_SWAP = "swap";
    private static final String LABEL_INFO = "_info";
    private static final String LABEL_TYPE = "_type";
    private static final String LABEL_AGENT_INFO = "agent_info";
    private static final String LABEL_AGENT_IDENTIFY = "agent_identity";
    private static final String LABEL_AGENT_NUMBER = "agent_number";
    private static final String SPACE = " ";
    private static final String LABEL_MANUFACTURED_DATE = "manufactured_date";
    private static final String DASH = "-";
    private static final String LABEL_MILEAGE = "mileage";
    private static final String LABEL_PRICELIST = "pricelist";
    private static final String LABEL_SIZELIST = "sizelist";
    private static final String LABEL_ROOMS = "rooms";
    private static final String PRIVATE_SELLER = "Private_Seller";
    private static final String COMPANY_SELLER = "Company_Seller";
    private static final String ALL_SELLER = "All_Seller";
    private static final String EXTRA = "extra";
    private static final String ISO_8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
    //click name
    public static final String EVENT_SIGNUP_SUCCESS = "Sign_up_success";
    public static final String EVENT_SIGNUP_POST_AD_SUCCESS = "Sign_up_post_ad_success";
    public static final String EVENT_SIGNIN_SUCCESS = "Sign_in_success";
    public static final String EVENT_PHONE_ACCOUNT_SIGNUP_SUCCESS = "Phone_account_sign_up_success";

    public static String ENVIRONMENT_NAME = ENVIRONMENT_PROD_NAME;
    public static String ENVIRONMENT_TYPE = PRODUCTION;
    private static DateFormat iso8601Format = new SimpleDateFormat(ISO_8601_DATE_PATTERN);

    public static void initialize(Application application) {
        if (!Config.enableTealiumTagging)
            return;//if disable, do nothing

        if (MudahUtil.isProduction()) {
            ENVIRONMENT_NAME = ENVIRONMENT_PROD_NAME;
        } else {
            ENVIRONMENT_NAME = ENVIRONMENT_DEV_NAME;
        }

        Tealium.Config config = Tealium.Config.create(application, ACCOUNT_NAME, PROFILE_NAME, ENVIRONMENT_NAME);

        if (!MudahUtil.isProduction()) {
            config.setLibraryLogLevel(Tealium.LogLevel.VERBOSE).setJavaScriptLogLevel(Tealium.LogLevel.VERBOSE).setHTTPSEnabled(false);
            config.setHTTPSEnabled(false).setLibraryLogLevel(Tealium.LogLevel.VERBOSE).setJavaScriptLogLevel(Tealium.LogLevel.VERBOSE);
        }

        Tealium.initialize(config);

        setGlobalCommonData();
        setGlobalCommonDataUser();
    }

    private static void setGlobalCommonData() {
        if (MudahUtil.isProduction()) {
            ENVIRONMENT_TYPE = PRODUCTION;
        } else {
            ENVIRONMENT_TYPE = DEVELOPMENT;
        }

        Tealium.getGlobalCustomData().edit()
                .putString(PLATFORM, ANDROID)
                .putString(ENVIRONMENT, ENVIRONMENT_TYPE)
                .putString(DEVICE_ID, Config.deviceId)
                .putString(UUID, Config.androidUUID)
                .putString(USER_AGENT, Config.androidUA)
                .apply();
    }

    public static void setGlobalCommonDataUser() {
        if (Config.userAccount.isLogin()) {
            Tealium.getGlobalCustomData().edit()
                    .putString(USER_ID, Config.userAccount.getUserId())
                    .putString(USER_EMAIL, Config.userAccount.getEmail())
                    .putString(USER_ACCOUNT_ID, Config.userAccount.getUserAccountId())
                    .apply();
        }
    }

    public static HashMap<String, String> getGlobalData() {
        return (HashMap<String, String>) Tealium.getGlobalCustomData().getAll();
    }

    //eventName - preferably Tealium.EVENT or Tealium.VIEW
    public static void track(Object object, Map<String, ?> optionalData, String eventName) {
        if (!Config.enableTealiumTagging)
            return;
        if (optionalData != null)
            Log.d(optionalData.toString());
        Tealium.track(object, optionalData, eventName);
    }

    public static Map<String, String> prepareParamsTagging(Map<String, String> data, ArrayList<ACAdParameter> params) {
        if (!Config.enableTealiumTagging)
            return new HashMap<>();//if disable, do nothing

        if (params != null) {
            HashMap<String, String> mapParams = new HashMap<>();
            HashMap<String, String> mapLabelParams = new HashMap<>();
            for (int i = 0; i < params.size(); i++) {
                ACAdParameter param = params.get(i);
                if (!ACUtils.isEmpty(param.getRealValue())) {
                    mapParams.put(param.getId(), param.getRealValue());
                    mapLabelParams.put(param.getId() + Constants.LABEL_VALUE, param.getValue());

                    if (Config.RED_ADVIEW_PARAMS_LIST.contains(param.getLabel()) && !ACUtils.isEmpty(param.getPriceType())) {
                        String key = param.getId().toLowerCase();
                        mapParams.put(key + LABEL_TYPE, param.getPriceType());
                        if (!ACUtils.isEmpty(param.getPriceTypeLabel())) {
                            mapLabelParams.put(key + LABEL_TYPE + Constants.LABEL_VALUE, param.getPriceTypeLabel());
                        }
                    } else if (LABEL_SWAP.equalsIgnoreCase(param.getId())) {
                        mapParams.put(param.getId() + LABEL_INFO, param.getValue());
                    }
                }
            }
            data.putAll(customizeParams(mapParams));
            data.putAll(customizeParams(mapLabelParams, Constants.LABEL_VALUE));
        }

        return data;
    }

    public static HashMap<String, String> customizeParams(HashMap<String, String> originalParams) {
        return customizeParams(originalParams, Constants.EMPTY_STRING);
    }

    public static HashMap<String, String> customizeParams(HashMap<String, String> originalParams, String keySuffix) {
        if (!Config.enableTealiumTagging)
            return new HashMap<>();//if disable, do nothing

        HashMap<String, String> params = new HashMap<>(originalParams);

        getSplitedMap(params, LABEL_AGENT_INFO + keySuffix, SPACE, LABEL_AGENT_IDENTIFY, LABEL_AGENT_NUMBER);
        getSplitedMap(params, LABEL_MANUFACTURED_DATE + keySuffix, DASH, "mds" + keySuffix, "mde" + keySuffix);
        getSplitedMap(params, LABEL_MILEAGE + keySuffix, DASH, "mileage_start" + keySuffix, "mileage_end" + keySuffix);
        getSplitedMap(params, LABEL_PRICELIST + keySuffix, DASH, "ps" + keySuffix, "pe" + keySuffix);
        //rooms: used as range for Filter but used as value in Adview.
        //We only need to update this name for Filter
        getSplitedMap(params, LABEL_ROOMS + keySuffix, DASH, "ros" + keySuffix, "roe" + keySuffix);

        getSplitedMap(params, LABEL_SIZELIST + keySuffix, DASH, "ss" + keySuffix, "se" + keySuffix);

        Log.d("result: " + params.toString());
        return params;
    }

    public static Map<String, String> getSplitedMap(Map<String, String> params, String key, String splitString, String keyStart, String keyEnd) {
        if (params.containsKey(key) && !ACUtils.isEmpty(params.get(key))) {
            String[] sizeList = params.remove(key).split(splitString);
            if (sizeList.length == 2) {
                params.put(keyStart, sizeList[0]);
                params.put(keyEnd, sizeList[1]);
            }
        }
        return params;
    }

    public static String getAdSellerTypeIdFromLabel(PostedBy adSellerType) {
        String sellerTypeId;
        switch (adSellerType) {
            case PRIVATE:
                sellerTypeId = AD_SELLER_PRIVATE;
                break;
            case COMPANY:
                sellerTypeId = AD_SELLER_COMPANY;
                break;
            case PROFESSIONAL:
                sellerTypeId = AD_SELLER_PROFESSIONAL;
                break;
            default:
                sellerTypeId = AD_SELLER_PRIVATE;
        }

        return sellerTypeId;

    }

    public static void tagTealiumPage(Activity activity, String application, String pageName, String level2) {
        Map<String, String> tealiumData = Tealium.map(
                APPLICATION, application,
                PAGE_NAME, pageName,
                XTN2, level2);
        Log.d("tagTealiumPage " + tealiumData);

        track(activity, tealiumData, Tealium.VIEW);
    }

    public static String getSellerTypeText(String sellerType) {
        String sellerTypeText = sellerType;
        if (Constants.POSTED_BY_PRIVATE.equalsIgnoreCase(sellerType)) {
            sellerTypeText = PRIVATE_SELLER;
        } else if (Constants.POSTED_BY_COMPANY.equalsIgnoreCase(sellerType)) {
            sellerTypeText = COMPANY_SELLER;
        } else {
            sellerTypeText = ALL_SELLER;
        }
        return sellerTypeText;
    }

    public static String getSellerTypeTextFromId(String sellerType) {
        String sellerTypeText = PRIVATE_SELLER;
        if (Constants.AD_SRC_COMPANY.equals(sellerType)) {
            sellerTypeText = COMPANY_SELLER;
        }
        return sellerTypeText;
    }

    public static void tagTealiumUserAccountPage(Activity activity, String pageName, UserAccountModel userAccount) {

        Map<String, String> tealiumData = Tealium.map(
                APPLICATION, APPLICATION_UA,
                PAGE_NAME, pageName,
                XTN2, XitiUtils.LEVEL2_UA_SITE_ID
        );

        if (userAccount != null
                && ((PAGE_UA_SIGNUP_EMAIL_SENT).equalsIgnoreCase(pageName)
                || (PAGE_UA_SIGNUP_ACTIVATED).equalsIgnoreCase(pageName)
                || (PAGE_UA_RESET_PASSWORD_EMAIL_SENT).equalsIgnoreCase(pageName)
                || (PAGE_UA_FORGOT_PASSWORD_EMAIL_SENT).equalsIgnoreCase(pageName))) {
            if (!ACUtils.isEmpty(userAccount.getEmail())) {
                tealiumData.put(USER_EMAIL, userAccount.getEmail());
            }
        }

        Log.d("UserAccount tealiumData " + tealiumData);
        track(activity, tealiumData, Tealium.VIEW);
    }

    //Chat Create Room
    public static void tagTealiumCreateRoom(Activity activity, ACAd dfAdsDO, CCRoom ccRoom) {
        Map<String, String> dataTealium = Tealium.map(
                APPLICATION, APPLICATION_CHAT,
                ROOM_ID, ccRoom.getObjectId(),
                TRANSACTION_TYPE, CHAT_CREATE_ROOM,
                USER_STATUS, USER_STATUS_BUYER,
                CLICK_NAME, CHAT_CREATE_ROOM,
                CLICK_TYPE, CLICK_TYPE_NAVIGATION,
                XTN2, XitiUtils.LEVEL2_CHAT_ID);

        if (dfAdsDO != null) {
            Map<String, String> adDetailTealium = Tealium.map(
                    REGION_ID, dfAdsDO.getRegionId(),
                    REGION_NAME, dfAdsDO.getRegion(),
                    CATEGORY_ID, dfAdsDO.getCategoryId(),
                    CATEGORY_NAME, dfAdsDO.getCategoryName(),
                    PARENT_CATEGORY_ID, dfAdsDO.getParentCategoryId(),
                    PARENT_CATEGORY_NAME, dfAdsDO.getParentCategoryName(),
                    AD_ID, dfAdsDO.getAdId(),
                    LIST_ID, Integer.toString(dfAdsDO.getListId()));
            dataTealium.putAll(adDetailTealium);
        }

        if (ccRoom != null && ccRoom.getRoomDetail() != null) {
            dataTealium.put(ROOM_ID, ccRoom.getObjectId());
            if (!ACUtils.isEmpty(ccRoom.getSellerId())) {
                dataTealium.put(RECEIVER_ID, ccRoom.getSellerId());
            }

            if (!ACUtils.isEmpty(ccRoom.getBuyerId())) {
                dataTealium.put(SENDER_ID, ccRoom.getBuyerId());
            }
        }

        track(activity, dataTealium, Tealium.EVENT);
        Log.d("tagTealiumCreateRoom " + dataTealium);
    }

    //Prepare tealium Map with common params
    public static Map<String, String> tagTealiumEvent(ACAd dfAdsDO, CCMessage ccMessage, CCRoom ccRoom) {
        Map<String, String> dataTealium = Tealium.map(
                APPLICATION, APPLICATION_CHAT,
                XTN2, XitiUtils.LEVEL2_CHAT_ID,
                OBJECT_ID, ccMessage.getUniqueId(),
                ROOM_ID, ccRoom.getObjectId(),
                USER_ID, Config.userAccount.getUserId());

        if (dfAdsDO != null) {
            dataTealium.put(AD_ID, dfAdsDO.getAdId());
            dataTealium.put(LIST_ID, Integer.toString(dfAdsDO.getListId()));
        } else if (ccRoom != null) {
            dataTealium.put(LIST_ID, ccRoom.getProductId());
            dataTealium.put(AD_ID, getAdIdFromExtraParam(ccRoom.getRoomDetail().getExtraParams()));
        }

        if (ccRoom != null) {
            dataTealium.put(ROOM_ID, ccRoom.getObjectId());
            if (Config.userAccount.getUserId().equalsIgnoreCase(ccRoom.getSellerId())) {
                dataTealium.put(USER_STATUS, USER_STATUS_SELLER);
                dataTealium.put(RECEIVER_ID, ccRoom.getBuyerId());
                dataTealium.put(SENDER_ID, ccRoom.getSellerId());
            } else {
                dataTealium.put(USER_STATUS, USER_STATUS_BUYER);
                dataTealium.put(RECEIVER_ID, ccRoom.getSellerId());
                dataTealium.put(SENDER_ID, ccRoom.getBuyerId());
            }
        }
        return dataTealium;
    }

    //Tag only Chat first message
    public static void tagTealiumSendFirstMessage(Activity activity, ACAd dfAdsDO, CCMessage ccMessage, CCRoom ccRoom, String imageUrl) {
        Map<String, String> dataTealium = tagTealiumEvent(dfAdsDO, ccMessage, ccRoom);
        String clickName = Constants.EMPTY_STRING;
        String pageName = Constants.EMPTY_STRING;
        //Check if the first message is an image
        if (ACUtils.isEmpty(imageUrl)) {
            dataTealium.put(TRANSACTION_TYPE, CHAT_SEND_FIRST_MESSAGE);
            dataTealium.put(MESSAGE, ccMessage.getText());
            pageName = CHAT_SEND_FIRST_MESSAGE;
        } else {
            dataTealium.put(TRANSACTION_TYPE, CHAT_SEND_FIRST_IMAGE);
            dataTealium.put(IMAGE_URL, imageUrl);
            pageName = CHAT_SEND_FIRST_IMAGE;
        }

        if (dfAdsDO != null) {
            dataTealium.put(REGION_ID, dfAdsDO.getRegionId());
            dataTealium.put(REGION_NAME, dfAdsDO.getRegion());
            dataTealium.put(CATEGORY_ID, dfAdsDO.getCategoryId());
            dataTealium.put(CATEGORY_NAME, dfAdsDO.getCategoryName());
            dataTealium.put(PARENT_CATEGORY_ID, dfAdsDO.getParentCategoryId());
            dataTealium.put(PARENT_CATEGORY_NAME, dfAdsDO.getParentCategoryName());
            clickName = pageName + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(dfAdsDO.getParentCategoryName()) + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(dfAdsDO.getCategoryName());
        }

        dataTealium.put(PAGE_NAME, pageName);
        dataTealium.put(AD_REPLY_TYPE, XitiUtils.AD_REPLY_CHAT);
        dataTealium.put(XTN2, XitiUtils.LEVEL2_AD_REPLY_ID);
        Log.d("tagTealiumSendFirstMessage  view " + dataTealium);
        track(activity, dataTealium, Tealium.VIEW);

        dataTealium.put(CLICK_TYPE, CLICK_TYPE_ACTION);
        dataTealium.put(CLICK_NAME, clickName);
        Log.d("tagTealiumSendFirstMessage  event " + dataTealium);
        track(activity, dataTealium, Tealium.EVENT);
    }

    //Tag Chat messages except the first message
    public static void tagTealiumSendMessage(Activity activity, ACAd dfAdsDO, CCMessage ccMessage, CCRoom ccRoom, String imageUrl) {
        Map<String, String> dataTealium = tagTealiumEvent(dfAdsDO, ccMessage, ccRoom);

        if (ACUtils.isEmpty(imageUrl)) {
            dataTealium.put(TRANSACTION_TYPE, CHAT_SEND_MESSAGE);
            dataTealium.put(CLICK_NAME, CHAT_SEND_MESSAGE);
            dataTealium.put(MESSAGE, ccMessage.getText());
        } else {
            dataTealium.put(TRANSACTION_TYPE, CHAT_SEND_IMAGE);
            dataTealium.put(CLICK_NAME, CHAT_SEND_IMAGE);
            dataTealium.put(IMAGE_URL, imageUrl);
        }
        dataTealium.put(CLICK_TYPE, CLICK_TYPE_NAVIGATION);
        Log.d("tagTealiumSendMessage " + dataTealium);

        track(activity, dataTealium, Tealium.EVENT);
    }

    //Tag Receive message
    public static void tagTealiumReceiveMessage(Object object, JSONObject result) {
        if (result == null)
            return;
        Map<String, String> dataTealium = Tealium.map(
                APPLICATION, APPLICATION_CHAT,
                XTN2, XitiUtils.LEVEL2_CHAT_ID,
                OBJECT_ID, result.optString(UNIQUE_ID),
                USER_ID, Config.userAccount.getUserId(),
                ROOM_ID, result.optString(ROOM_ID),
                AD_ID, result.optString(AD_ID),
                LIST_ID, result.optString(ITEM_ID));

        if (ACUtils.isEmpty(dataTealium.get(AD_ID)) && result.has(EXTRA)) {
            String adId = Constants.EMPTY_STRING;
            //a workaround, if API sends with a backslash, need to manually convert this to Json
            // {"extra": \"{"additional\":...
            if (result.optJSONObject(EXTRA) == null && !ACUtils.isEmpty(result.optString(EXTRA))) {
                try {
                    JSONObject extraJSON = new JSONObject(result.optString(EXTRA));
                    adId = extraJSON.optJSONObject(ADDITIONAL).optString(AD_ID);
                } catch (JSONException ignore) {
                }
            } else {
                adId = result.optJSONObject(EXTRA).optJSONObject(ADDITIONAL).optString(AD_ID);
            }

            dataTealium.put(AD_ID, adId);
        }

        if (result.has(RECEIVER_ID)) {
            dataTealium.put(USER_STATUS, result.optString(USER_STATUS));
            dataTealium.put(RECEIVER_ID, result.optString(RECEIVER_ID));
            dataTealium.put(SENDER_ID, result.optString(SENDER_ID));
        } else if (Config.userAccount.getUserId().equalsIgnoreCase(result.optString(SELLER_ID))) {
            dataTealium.put(USER_STATUS, USER_STATUS_BUYER);
            dataTealium.put(RECEIVER_ID, result.optString(SELLER_ID));
            dataTealium.put(SENDER_ID, result.optString(BUYER_ID));
        } else {
            dataTealium.put(USER_STATUS, USER_STATUS_SELLER);
            dataTealium.put(RECEIVER_ID, result.optString(BUYER_ID));
            dataTealium.put(SENDER_ID, result.optString(SELLER_ID));
        }

        if (result.has(IMAGE_URL) && !ACUtils.isEmpty(result.optString(IMAGE_URL))) {
            dataTealium.put(IMAGE_URL, result.optString(IMAGE_URL));
            dataTealium.put(TRANSACTION_TYPE, CHAT_RECEIVE_IMAGE);
        } else if (result.has(TEXT)) {
            dataTealium.put(MESSAGE, result.optString(TEXT));
            dataTealium.put(TRANSACTION_TYPE, CHAT_RECEIVE_MESSAGE);
        } else if (result.has(DATA)) {
            dataTealium.put(IMAGE_URL, result.optJSONObject(DATA).optJSONArray(ATTACHMENTS).optJSONObject(0).optString(THUMBNAIL_URL));
            dataTealium.put(TRANSACTION_TYPE, CHAT_RECEIVE_IMAGE);
        }

        Log.d("tagTealiumReceiveMessage " + dataTealium);

        track(object, dataTealium, Tealium.EVENT);
    }

    //Tag Delete Room
    public static void tagTealiumDeleteRoom(Activity activity, String roomsDeleted, String deletedUserId, String roomIds, String deletedListId, String deletedAdId) {

        Map<String, String> dataTealium = Tealium.map(
                APPLICATION, APPLICATION_CHAT,
                XTN2, XitiUtils.LEVEL2_CHAT_ID,
                ROOM_ID, roomIds,
                USER_ID, Config.userAccount.getUserId(),
                TRANSACTION_TYPE, DELETE_ROOM,
                CLICK_NAME, DELETE_ROOM,
                CLICK_TYPE, CLICK_TYPE_NAVIGATION,
                ROOMS_DELETED, roomsDeleted,
                DELETED_USER_ID, deletedUserId,
                AD_ID, deletedAdId,
                LIST_ID, deletedListId);

        Log.d("tagTealiumDeleteRoom " + dataTealium);
        track(activity, dataTealium, Tealium.EVENT);
    }

    //Tag Mute Room
    public static void tagTealiumChatOverFlowAction(Activity activity, CCRoom ccRoom, String action, CharSequence reportReason) {

        Map<String, String> dataTealium = Tealium.map(
                APPLICATION, APPLICATION_CHAT,
                XTN2, XitiUtils.LEVEL2_CHAT_ID,
                CLICK_TYPE, CLICK_TYPE_NAVIGATION,
                ROOM_ID, ccRoom.getObjectId(),
                USER_ID, Config.userAccount.getUserId());

        if (ccRoom != null) {
            dataTealium.put(LIST_ID, ccRoom.getProductId());
            dataTealium.put(AD_ID, getAdIdFromExtraParam(ccRoom.getRoomDetail().getExtraParams()));
        }

        if (Config.userAccount.getUserId().equalsIgnoreCase(ccRoom.getSellerId())) {
            dataTealium.put(USER_STATUS, USER_STATUS_SELLER);
            dataTealium.put(RECEIVER_ID, ccRoom.getBuyerId());
            dataTealium.put(SENDER_ID, ccRoom.getSellerId());
        } else {
            dataTealium.put(USER_STATUS, USER_STATUS_BUYER);
            dataTealium.put(RECEIVER_ID, ccRoom.getSellerId());
            dataTealium.put(SENDER_ID, ccRoom.getBuyerId());
        }

        if ((MUTE).equalsIgnoreCase(action)) {
            prepareMapOverFlowAction(ccRoom, CHAT_MUTE_NOTIFICATION, MUTED_USER_ID, dataTealium);
        } else if ((UNMUTE).equalsIgnoreCase(action)) {
            prepareMapOverFlowAction(ccRoom, CHAT_UNMUTE_NOTIFICATION, UNMUTED_USER_ID, dataTealium);
        } else if ((BLOCK).equalsIgnoreCase(action)) {
            prepareMapOverFlowAction(ccRoom, CHAT_BLOCK_USER, BLOCKED_USER_ID, dataTealium);
        } else if ((UNBLOCK).equalsIgnoreCase(action)) {
            prepareMapOverFlowAction(ccRoom, CHAT_UNBLOCK_USER, UNBLOCKED_USER_ID, dataTealium);
        } else {
            prepareMapOverFlowAction(ccRoom, CHAT_REPORT_USER, REPORTED_USER_ID, dataTealium);
            dataTealium.put(REPORT_REASON, reportReason.toString());
        }

        Log.d("tagTealiumChatOverFlowAction " + dataTealium);
        track(activity, dataTealium, Tealium.EVENT);
    }

    public static void prepareMapOverFlowAction(CCRoom ccRoom, String transactionType, String titleUserId, Map<String, String> dataTealium) {
        dataTealium.put(TRANSACTION_TYPE, transactionType);
        dataTealium.put(CLICK_NAME, transactionType);
        if (Config.userAccount.getUserId().equalsIgnoreCase(ccRoom.getSellerId())) {
            dataTealium.put(titleUserId, ccRoom.getBuyerId());
        } else {
            dataTealium.put(titleUserId, ccRoom.getSellerId());
        }
    }

    public static HashMap<String, Object> prepareExtraParamForAPI(ACAd dfAdsDO) {
        HashMap<String, Object> extraParams = new HashMap<>();
        extraParams.put(TealiumHelper.UNIVERSAL, getChatCommonData());

        HashMap<String, String> additional = new HashMap<>();
        additional.put(TealiumHelper.USER_STATUS, TealiumHelper.USER_STATUS_BUYER);
        additional.put(TealiumHelper.AD_ID, dfAdsDO.getAdId());
        additional.put(TealiumHelper.SENDER_ID, Config.userAccount.getUserId());
        additional.put(TealiumHelper.RECEIVER_ID, dfAdsDO.getUserId());

        extraParams.put(TealiumHelper.ADDITIONAL, additional);
        return extraParams;
    }

    public static HashMap<String, Object> getChatExtraParamForAPI(CCRoom ccRoom) {
        HashMap<String, Object> extraParams = new HashMap<>();
        extraParams.put(TealiumHelper.UNIVERSAL, getChatCommonData());

        HashMap<String, String> additional = new HashMap<>();
        additional.put(TealiumHelper.SENDER_ID, Config.userAccount.getUserId());

        if (ccRoom != null && ccRoom.getRoomDetail() != null) {
            if (Config.userAccount.getUserId().equalsIgnoreCase(ccRoom.getSellerId())) {
                additional.put(TealiumHelper.USER_STATUS, USER_STATUS_SELLER);
                additional.put(TealiumHelper.RECEIVER_ID, ccRoom.getBuyerId());
            } else {
                additional.put(TealiumHelper.USER_STATUS, USER_STATUS_BUYER);
                additional.put(TealiumHelper.RECEIVER_ID, ccRoom.getSellerId());
            }
        }

        additional.put(TealiumHelper.LIST_ID, ccRoom.getProductId());
        additional.put(TealiumHelper.ROOM_ID, ccRoom.getObjectId());

        String adId = getAdIdFromAPIExtraParam(ccRoom.getRoomDetail().getExtraParams());
        additional.put(TealiumHelper.AD_ID, adId);

        additional.put(TealiumHelper.MEMBER_ID, Config.userAccount.getUserId());
        additional.put(TealiumHelper.TOKEN, Config.userAccount.getToken());
        extraParams.put(TealiumHelper.ADDITIONAL, additional);

        return extraParams;
    }

    public static HashMap<String, String> getChatCommonData() {
        HashMap<String, String> universal = TealiumHelper.getGlobalData();
        universal.put(TealiumHelper.TIMESTAMP, iso8601Format.format(new Date()));
        universal.put(TealiumHelper.XTN2, XitiUtils.LEVEL2_CHAT_ID);
        universal.put(TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_CHAT);
        return universal;
    }

    //Tag chat room page view
    public static void tagTealiumViewChatRoom(Activity activity, ACAd dfAdsDO, CCRoom ccRoom) {
        //HAVE TO TAG AD_ID and LIST_ID yet

        Map<String, String> dataTealium = Tealium.map(
                PAGE_NAME, CHAT_ROOM,
                USER_ID, Config.userAccount.getUserId());

        if (ccRoom != null) {
            dataTealium.put(ROOM_ID, ccRoom.getObjectId());
            dataTealium.put(BUYER_ID, ccRoom.getBuyerId());
            dataTealium.put(SELLER_ID, ccRoom.getSellerId());
            if (Config.userAccount.getUserId().equalsIgnoreCase(ccRoom.getSellerId())) {
                dataTealium.put(USER_STATUS, USER_STATUS_SELLER);
            } else {
                dataTealium.put(USER_STATUS, USER_STATUS_BUYER);
            }
        }
        if (dfAdsDO != null) {
            dataTealium.put(AD_ID, dfAdsDO.getAdId());
            dataTealium.put(LIST_ID, Integer.toString(dfAdsDO.getListId()));
        } else if (ccRoom != null) {
            dataTealium.put(LIST_ID, ccRoom.getProductId());
            dataTealium.put(AD_ID, getAdIdFromExtraParam(ccRoom.getRoomDetail().getExtraParams()));
        }
        Log.d("tagTealiumViewChatRoom " + dataTealium);
        track(activity, dataTealium, Tealium.VIEW);
    }

    public static String getAdIdFromAPIExtraParam(Map<String, Object> extraParamsFromAPI) {
        Log.d("extraParamsFromAPI: " + extraParamsFromAPI);
        try {
            /* Example response from API
            extra: {
                universal: {}
                additional: {}
            }
          */
            if (extraParamsFromAPI != null && extraParamsFromAPI.containsKey(ChatActivity.EXTRA_PARAMS)) {
                JSONObject jsonExtra = new JSONObject((String) extraParamsFromAPI.get(ChatActivity.EXTRA_PARAMS));
                if (jsonExtra.has(ChatActivity.ADDITIONAL)) {
                    return jsonExtra.optJSONObject(ChatActivity.ADDITIONAL).optString(TealiumHelper.AD_ID);
                }
            }
        } catch (JSONException ignore) {
        }
        return Constants.EMPTY_STRING;
    }

    public static String getAdIdFromExtraParam(Map<String, Object> extraParams) {
        Log.d("extraParams: " + extraParams);
        /* The extra Hashmap <String, Object> contains
            universal: Hashmap <String, String>
            additional: Hashmap <String, String>
        }
       */
        if (extraParams != null && extraParams.containsKey(ChatActivity.ADDITIONAL)) {
            HashMap<String, String> additional = (HashMap<String, String>) extraParams.get(ChatActivity.ADDITIONAL);
            return additional.get(TealiumHelper.AD_ID);
        }

        return Constants.EMPTY_STRING;
    }
}
