package com.mudah.my.configs;

import android.net.Uri;
import android.util.SparseBooleanArray;

import com.lib701.utils.Log;
import com.mudah.my.models.UserAccountModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config extends com.lib701.connection.Config {

    public static final String DEFAULT_SAFTY_LINK = "http://www.mudah.my/security/index.htm";
    public static final String OPTIMIZELY_KEY = "AAM7hIkA5je6lPEsW4B3sgsRZb3pPzuH~3160310800";
    public static final String NEWRELIC_APPLICATION_ID = "AAe92564bde2ec2410cf4b347d0e76f1eddb5f4198";
    public static final String NEWRELIC_STAGING_APPLICATION_ID = "AA2ba8606208f0724623a3ce9402f434d41eed510b";
    public static final String PARSE_APPLICATION_ID = "fxfwXEfU5Lui1Q2YwgfdJKoENAkxYiIRbGGEUyOZ";
    public static final String PARSE_CLIENT_KEY = "ebnvKahTHPMIrqMuVui2wBYAjdv0kqTPac9CHIB7";
    public static final String DEFAULT_AD_COUNT = "over 1.7 million";
    public static final String UPLOAD_IMAGES_DIR = "MudahApp";
    public static final String CHAT_IMG_NAME = "chat.jpg";
    public static final String INSERT_AD_IMG_NAME = "insert_ad.jpg";
    public static final String CROP_IMG_NAME = "crop_insert_ad.jpg";
    public static final int GOOGLE_ADS_PAGE_THRESHOLD = 2;
    public static final String[] SPECIAL_ADVIEW_PARAMS = {"Price", "Category", "Location", "Condition", "Salary", "Monthly Rent", "Rent", "Max Rent"};
    public static final List<String> SPECIAL_ADVIEW_PARAMS_LIST = Arrays.asList(SPECIAL_ADVIEW_PARAMS);
    public static final String[] RED_ADVIEW_PARAMS = {"Price", "Salary", "Monthly Rent", "Rent", "Max Rent"};
    public static final List<String> RED_ADVIEW_PARAMS_LIST = Arrays.asList(RED_ADVIEW_PARAMS);
    public static final String[] AUTO_GENERATED_SUBJECT_PARAMS = {"make", "model", "transmission", "manufactured_date", "engine_capacity"};
    public static final List<String> AUTO_GENERATED_SUBJECT_PARAMS_LIST = Arrays.asList(AUTO_GENERATED_SUBJECT_PARAMS);
    public static final String[] COUNTRY_CODE_WHITELIST = {"sg", "my", "id", "th", "vn"};
    public static final List<String> COUNTRY_CODE_WHITELIST_IST = Arrays.asList(COUNTRY_CODE_WHITELIST);
    //Search filter keyword
    public static final int MAX_FREE_TEXT_LENGTH = 100;
    public static final int DATABASE_VERSION = 2;
    //Notification ID (must be unique for us to identify notification item)
    public static final int NOTIFICATION_INACTIVE_USER_ID = 1;
    public static final int NOTIFICATION_INSERT_AD = 2;
    public static final int NOTIFICATION_DRAFT_AD = 3;
    public static final int NOTIFICATION_BOOKMARK_ID = 4;
    public static final int NOTIFICATION_CHAT_MSG = 5;
    public static final int NOTIFICATION_KAHUNA = 6;
    //Notification ID (must be unique for us to identify notification extra string passed in bundle)
    public static final String INACTIVE_USER_MAIN_NOTIFICATION = "inactive_user_main_notification";
    public static final String INACTIVE_USER_SECONDARY_NOTIFICATION = "inactive_user_secondary_notification";
    public static final String DRAFT_MAIN_NOTIFICATION = "draft_main_notification";
    public static final String INSERTAD_MAIN_NOTIFICATION = "insertad_main_notification";
    public static final String BOOKMARK_MAIN_NOTIFICATION = "bookmark_main_notification";
    public static final String SCHEME = "com.mudah.my://android.intent.action.VIEW/";
    public static final String DEFAULT_PRONIAGA_LOGIN = "http://www2.mudah.my/store/main_login/0";
    public static final String DEFAULT_TIPS_URL = "http://www.mudah.my/security/tips.htm?ca=9_s";
    public static final String DEFAULT_RULES_URL = "http://www.mudah.my/about/index.htm?ca=9_s&page=rules";
    public static final long SESSION_LENGTH_FOR_USERS = 3 * 24 * 60 * 60000;//24 hour x 3 days
    public static final long SESSION_LENGTH_FOR_DEV_USERS = 30000;//30 secs
    public static final String[] SHARE_PACKAGES = {Constants.FACEBOOK, Constants.INSTAGRAM, Constants.WHATSAPP};
    public static final String PUSH_NOTIFICATION = "push_notification";
    public static final String KAHUNA_KEY = "5cbff80574f04cd091e323d4b90f982e";
    public static final String MUDAH_APP_GOOGLE = "market://details?id=com.mudah.my";
    private static final String FACEBOOK_PACKAGE = "com.facebook.katana";
    private static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    public static String loginProNiagaUrl = DEFAULT_PRONIAGA_LOGIN;
    public static String tipsUrl = DEFAULT_TIPS_URL;
    //URI for AppIndex: (android-app://<package_name>/<scheme>/[host_path])
    public static Uri BASE_APP_URI = Uri.parse("android-app://com.mudah.my/http/www.mudah.my/vi/");
    public static boolean googleAppIndexing = false;
    public static boolean firstTimeUser = false;
    public static boolean firstTimeUserAndChooseRegion = false;
    public static String firstLaunchDate;
    public static boolean enableAmplitudeTracking;
    public static boolean isGoogleAppCrawler = false;
    public static int MAXIMUM_STORED_HISTORY_COUNT = 30;
    public static int TrackAmplitude;
    public static JSONObject amplitudeTrackingStep;
    //maintenance mode
    public static boolean maintenanceListing = false;
    public static boolean maintenanceInsertAd = false;
    public static String maintenanceListingText;
    public static String maintenanceListingSubText;
    public static String maintenanceInsertAdText;
    public static String maintenanceInsertAdSubText;
    public static String apiPath = "/api/";
    public static String userApiPath = "/api/user/";
    public static ArrayList<String> apiList = new ArrayList<String>();
    public static String rulesUrl = DEFAULT_RULES_URL;
    public static String lastSearchTs = "";
    public static Map<String, Integer> tutorialPagesAndSteps = new HashMap<>();
    public static int maxAdviewFavTotal = 50;
    public static int maxBookmarksTotal = 50;
    public static boolean betaUserSignUp = false;
    public static boolean upgrade = false;
    public static boolean imageMode = true;
    public static ListViewMode listViewMode = ListViewMode.GRID_VIEW;
    public static int FIRST_TIME_RATING = 0;
    public static int NO_MORE_RATING = 1;
    public static int UPGRADE_PREFERENCES = 0;
    public static boolean IS_GOOGLEAD_ENABLE = false;
    public static String totalAdsCount = DEFAULT_AD_COUNT;
    public static JSONArray bannerImages;
    public static boolean enableTealiumTagging = true;
    public static boolean enableOptimizely = false;
    public static boolean enableAppsFlyer = true;
    public static boolean enableGravity = true;
    public static boolean enableUserAccount = true;
    public static boolean enableChat = true;
    public static boolean enableSellerOnboard = false;
    public static String deviceId = Constants.UNKNOWN;
    public static String androidUUID = Constants.UNKNOWN;
    public static String COMSCORE_CLIENT_ID = "6296496";
    public static String COMSCORE_SECRET_KEY = "0339b9a1e2cfd33eb684d27f5f157570";
    public static String AMPLITUDE_API_KEY = "b867623acc91664292e1eeb48d849d04";
    public static String AMPLITUDE_API_DEV_KEY = "ecef3b6a579b6395dab2cc7c2f671173";
    public static String userRegistered = "1";
    public static String SHOP_SAFELY = "/security/index.htm";
    public static String SELLER_ONBOARD = "/onboard.htm?lang=";
    public static String RECOMMEND_PAGE = "/recommended?f=p";
    public static UserAccountModel userAccount;
    public static String preferLang = "my"; //en or my
    public static boolean isRegisterWithGcm = false;
    public static int TIME_OUT = 30;
    public static String hydraPushNotificationRoot;
    public static String chatAPIUrl;
    public static int chatAPIPort;
    public static int chatSocketPort;
    public static String gcmToken;
    public static String userIP;
    public static SparseBooleanArray allFavouritAdIds = new SparseBooleanArray();
    public static int bookmarkTotal = 0;
    public static String splashUrl;
    public static String splashBgColor;
    public static String splashUpdatedTs;
    public static String regionUpdatedTs;
    public static String categoryUpdatedTs;
    public static boolean needRegionUpdate = false;
    public static boolean needCategoryUpdate = false;
    public static boolean homepageRecommendedAds = false;
    public static boolean skipAllTutorial = false;
    public static int badgeUnreadChatNumber = -1;
    public static int validSignInDays = 30;

    static {
        setROOT(RootType.PRODUCTION);
    }

    static {
        setSupportedAPIVersion("v2");
        setAppId("mudah_android");
        switch (ROOT) {
            case REGRESS:
                Log.isDebug = true;
                Log.sendCrashlytics = false;
                Log.forceCrash = true;
                setAPIScheme("http://");
                setHost("172.16.20.211:21830");
                setMHost("10.40.3.188:54321");
                setInsertAdHost("172.16.20.211:21804");
                setAPIRoot(apiScheme + host + apiPath);
                setShareHost("http://" + mhost);
                setMShareHost("http://" + mhost);
                setInsertAdURL("http://" + insertAdHost + "/ai");
                setManageAdURL("http://www2." + host + "/ai");
                setUserAccountAPIUrl("http://172.16.20.211:21870" + userApiPath);
                googleAppIndexing = false;
                MAXIMUM_STORED_HISTORY_COUNT = 3;
                hydraPushNotificationRoot = "https://hydra1-staging.mudah.my";
                chatAPIUrl = "https://chat-preprod.mudah.my";
                chatAPIPort = 443;
                chatSocketPort = 41337;
                break;
            case STAGING:
                Log.isDebug = true;
                Log.sendCrashlytics = true;
                Log.forceCrash = false;
//                setHost("selamat.my");
//                setMHost("selamat.my");
                setHost("schibstedclassifiedmedia.my");
                setMHost("schibstedclassifiedmedia.my");
                setInsertAdHost("www2." + host);
                setAPIScheme("https://");
                setAPIRoot(apiScheme + "api." + host + apiPath);
                setShareHost("http://www." + host);
                setMShareHost("http://" + mhost);
                setInsertAdURL("http://" + insertAdHost + "/ai");
                setManageAdURL("http://www2." + host + "/ai");
                setUserAccountAPIUrl(apiScheme + "uapi." + host + userApiPath);
                // Terbaik Staging
                //setUserAccountAPIUrl(apiScheme + "uapi.terbaik.com.my" + userApiPath);
                MAXIMUM_STORED_HISTORY_COUNT = 10;
                googleAppIndexing = false;
                hydraPushNotificationRoot = "https://hydra1-staging.mudah.my";
                //Selemat

//                chatAPIUrl = "https://chat-preprod.mudah.my";
//                chatAPIPort = 443;
//                chatSocketPort = 41337;
                //SCM
                chatAPIUrl = "http://54.169.165.141";//"https://chat-preprod.mudah.my";
                chatAPIPort = 5000;//443;
                chatSocketPort = 1337; //41337;
                break;
            case PRODUCTION:
            default:
                Log.isDebug = false;
                Log.sendCrashlytics = true;
                Log.forceCrash = false;
                setHost("mudah.my");
                setMHost("m.mudah.my");
                setInsertAdHost("www2." + host);
                setAPIScheme("https://");
                setAPIRoot(apiScheme + "api." + host + apiPath);
                apiList.add(apiScheme + "api1." + host + apiPath);
                apiList.add(apiScheme + "api2." + host + apiPath);
                setShareHost("http://www." + host);
                setMShareHost("http://" + mhost);
                setInsertAdURL("http://" + insertAdHost + "/ai");
                setManageAdURL("http://www2." + host + "/ai");
                setUserAccountAPIUrl(apiScheme + "uapi.mudah.my" + userApiPath);
                MAXIMUM_STORED_HISTORY_COUNT = 30;
                googleAppIndexing = true;
                hydraPushNotificationRoot = "https://hydra.mudah.my";
                chatAPIUrl = "https://chat.mudah.my";
                chatAPIPort = 443;
                chatSocketPort = 41337;
                break;
        }
    }

    public static void init() {
        //dummy method for other class to call so that the static block got initialized
        Log.TAG = "mudah_android";
    }

    public static ListViewMode getNextViewMode(ListViewMode currentMode) {
        ListViewMode nextMode = ListViewMode.GRID_VIEW;
        switch (currentMode) {
            case GRID_VIEW:
                nextMode = ListViewMode.LIST_VIEW;
                break;
            case LIST_VIEW:
                nextMode = ListViewMode.GRID_VIEW;
                break;
        }
        Log.d("currentMode: " + currentMode + " => nextMode: " + nextMode);
        return nextMode;
    }

    public static String getPackage(String label) {
        String name = null;
        switch (label) {
            case Constants.FACEBOOK:
                name = FACEBOOK_PACKAGE;
                break;
            case Constants.INSTAGRAM:
                name = INSTAGRAM_PACKAGE;
                break;
            case Constants.WHATSAPP:
                name = WHATSAPP_PACKAGE;
                break;
        }

        return name;
    }

    public enum TutorialPages {LISTING, ADVIEW, VERIFIED, HOMEPAGE, NAVIGATION}

    public enum ListViewMode {LIST_VIEW, LIST_VIEW_NO_THUMB, GRID_VIEW}
}
