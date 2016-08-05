package com.mudah.my;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.chatcafe.sdk.core.Cafe;
import com.comscore.analytics.comScore;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.kahuna.sdk.IKahuna;
import com.kahuna.sdk.Kahuna;
import com.lib701.connection.ACBlocketConnection;
import com.lib701.datasets.ACReferences;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Config.ListViewMode;
import com.mudah.my.configs.Constants;
import com.mudah.my.dao.AdViewFavouritesDAO;
import com.mudah.my.dao.BookmarksDAO;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.models.UserAccountModel;
import com.mudah.my.receivers.KahunaReceiver;
import com.mudah.my.utils.MudahUtil;
import com.newrelic.agent.android.NewRelic;
import com.squareup.leakcanary.RefWatcher;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Pin on 7/1/15.
 */
public class BaseApplication extends Application {

    private static final String BASE_APP_SCHEME = "android-app://";
    private static final String BASE_APP_WEB_SCHEME = "/http/www.";
    private static final String BASE_APP_WEB_VIEW_PAGE = "/vi/";

    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
            // Cafe logout to disconnect socket, msg will now be sent via Push
            if (Config.enableChat) {
                Cafe.logOut();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };
    //use the RefWatcher to watch for fragment leaks:
    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        BaseApplication application = (BaseApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    //override the attachBaseContext() method and call MultiDex.install(this) to enable multidex
    //Ref: https://developer.android.com/tools/building/multidex.html
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (RuntimeException ignore) {
            //observe the MultiDex error
            ACUtils.debug(ignore);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Config.init();
        //refWatcher = LeakCanary.install(this);
        Fabric.with(this, new Crashlytics());
        initFacebook();
        //UA for InsertAd and Email Advertiser
        initUserAgent();
        //Initialize comscore id and parameters
        initializeComscore();
        initConfigValues();
        initUserAccount();
        initKahuna();
        //Restore categories and regions data into ACSettings
        ACBlocketConnection.getCategoriesFromSavedData(this);
        ACBlocketConnection.getRegionsFromSavedData(this);
        //Initialize Xiti
        XitiUtils.initFromLastConfig(this);

        getTotalFavouriteTotal();
        try {
            TealiumHelper.initialize(this);
            initNewRelic();
        } catch (Exception ignore) {
            //Ignore this when could not create ParseKeyValueCache directory
        }
        printStatus();
        configureChat();
    }

    private void initFacebook(){
        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    private void initKahuna() {
        try {
            IKahuna kahuna = Kahuna.getInstance();
            kahuna.onAppCreate(this, Config.KAHUNA_KEY, getString(R.string.gcm_sender_id));
            kahuna.setPushNotificationLargeIconResourceId(R.drawable.icon);
            kahuna.setPushNotificationSmallIconResourceId(R.drawable.notification_ticker);
            Kahuna.getInstance().setPushReceiver(KahunaReceiver.class);
            Kahuna.getInstance().disableKahunaGenerateNotifications();
            KahunaHelper.tagUserAttributes(Config.getAppVersion() + Constants.EMPTY_STRING);
        } catch (Exception ignore) {
            Log.e("Kahuna exception: ", ignore);
        }
    }

    private void configureChat() {
        Log.d("Cafe= Config.chatSocketPort: " + Config.chatSocketPort);
        com.chatcafe.sdk.core.Config chatConfig = new com.chatcafe.sdk.core.Config.Builder()
                .setBaseUrl(Config.chatAPIUrl)
                .setSocketPort(Config.chatSocketPort)
                .setServicePort(Config.chatAPIPort)
                .build();
        Cafe.initialize(getApplicationContext(), chatConfig);
        Cafe.logLevel(Arrays.asList(Cafe.DB, Cafe.SOCKET, Cafe.WS));
        if (Log.isDebug) {
            Cafe.enableLog();
        }
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    private void initNewRelic() {
        String token = Config.NEWRELIC_APPLICATION_ID;
        if (!MudahUtil.isProduction()) {
            token = Config.NEWRELIC_STAGING_APPLICATION_ID;
        }

        NewRelic.withApplicationToken(token).start(this.getApplicationContext());
    }

    private void initConfigValues() {
        if (getApplicationContext() != null) {
            Config.deviceId = MudahUtil.getDeviceId(getApplicationContext());
            Config.androidUUID = MudahUtil.getUUID(getApplicationContext());
            Config.BASE_APP_URI = Uri.parse(BASE_APP_SCHEME + getApplicationContext().getPackageName() + BASE_APP_WEB_SCHEME + Config.host + BASE_APP_WEB_VIEW_PAGE);
        }
        SharedPreferences sharedPreferences = PreferencesUtils.getSharedPreferences(this);

        Config.listViewMode = ListViewMode.valueOf(sharedPreferences.getString(PreferencesUtils.LIST_VIEW_MODE, ListViewMode.GRID_VIEW.toString()));
        //check preferences value to handle backward compatibility for LIST_VIEW_NO_THUMB
        if (Config.listViewMode == ListViewMode.LIST_VIEW_NO_THUMB) {
            Config.listViewMode = ListViewMode.valueOf(ListViewMode.GRID_VIEW.toString());
        }

        Config.firstLaunchDate = sharedPreferences.getString(PreferencesUtils.FIRST_LAUNCH_DATE, Constants.EMPTY_STRING);
        if (ACUtils.isEmpty(Config.firstLaunchDate)) {
            Config.firstTimeUser = true;
            initFirstTimeUser();
        } else
            Config.firstTimeUser = false;

        Config.imageMode = sharedPreferences.getBoolean(PreferencesUtils.IMAGE_MODE, true);

        initTutorialPagesAndSteps(sharedPreferences);

        Config.FIRST_TIME_RATING = sharedPreferences.getInt(PreferencesUtils.FIRST_TIME_USE, 0);
        Config.splashUrl = sharedPreferences.getString(PreferencesUtils.SPLASH_IMG_URL, Constants.EMPTY_STRING);
        Config.splashBgColor = sharedPreferences.getString(PreferencesUtils.SPLASH_BG_COLOR, getResources().getString(R.string.default_splash_bg));
        Config.lastSearchTs = sharedPreferences.getString(PreferencesUtils.LAST_SEARCH_TIMESTAMP, Constants.EMPTY_STRING);
        Config.splashUpdatedTs = sharedPreferences.getString(PreferencesUtils.SPLASH_UPDATED_TIMESTAMP, Constants.EMPTY_STRING);
        Config.categoryUpdatedTs = sharedPreferences.getString(PreferencesUtils.CAT_UPDATED_TIMESTAMP, Constants.EMPTY_STRING);
        Config.regionUpdatedTs = sharedPreferences.getString(PreferencesUtils.REGION_UPDATED_TIMESTAMP, Constants.EMPTY_STRING);
        Config.preferLang = sharedPreferences.getString(PreferencesUtils.PREFER_LANG, Constants.EMPTY_STRING);
        clearLastSearch(sharedPreferences);
    }

    private void clearLastSearch(SharedPreferences sharedPreferences) {
        //clear last search except location
        String globalQuery = sharedPreferences.getString(PreferencesUtils.LAST_SEARCH_QUERY, Constants.EMPTY_STRING);
        if (ACUtils.isEmpty(globalQuery))
            return;

        String queryString = Constants.EMPTY_STRING;
        Map<String, String> searchParams = MudahUtil.convertStringToHashMap(globalQuery);
        if (searchParams != null && searchParams.containsKey(Constants.REGION)) {
            Map<String, String> newQuery = new HashMap<>();
            newQuery.put(Constants.REGION, searchParams.get(Constants.REGION));
            if (searchParams.containsKey(Constants.SUBAREA)) {
                newQuery.put(Constants.SUBAREA, searchParams.get(Constants.SUBAREA));
            }
            queryString = MudahUtil.getSearchUri(newQuery).getEncodedQuery();
        }

        sharedPreferences.edit()
                .putString(PreferencesUtils.LAST_SEARCH_QUERY, queryString)
                .remove(PreferencesUtils.LAST_FILTER_PARAMS).apply();
    }


    private void initTutorialPagesAndSteps(SharedPreferences sharedPreferences) {
        String tutorialStepsString = sharedPreferences.getString(PreferencesUtils.TUTORIAL_PAGES_AND_STEPS, Constants.EMPTY_STRING);
        Log.d("tutorialStepsString: " + tutorialStepsString);
        Config.tutorialPagesAndSteps = MudahUtil.convertStringToHashMapInt(tutorialStepsString);

    }

    private void initFirstTimeUser() {
        Log.d();
        Calendar now = Calendar.getInstance();
        Config.firstLaunchDate = MudahUtil.formatDate(now);
        PreferencesUtils.getSharedPreferences(this).edit()
                .putString(PreferencesUtils.FIRST_LAUNCH_DATE, Config.firstLaunchDate)
                .apply();
    }

    private void initUserAgent() {
        int version;
        try {
            version = (getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
            Config.setAppVersion(version);
        } catch (PackageManager.NameNotFoundException e) {
            ACUtils.debug(e);
        }
        //UA for InsertAd and Email Advertiser
        Config.setUserAgent(getApplicationContext());
        Crashlytics.getInstance().core.setString("UserAgent", Config.androidUA);
    }

    private void printStatus() {
        StringBuilder status = new StringBuilder();
        status.append(" firstLaunchDate: " + Config.firstLaunchDate)
                .append("\n imageMode= " + Config.imageMode)
                .append("\n ACReferences.regionsFetched = " + ACReferences.regionsFetched)
                .append("\n ACReferences.categoriesFetched = " + ACReferences.categoriesFetched)
                .append("\n listViewMode= " + Config.listViewMode)
                .append("\n splashImgUrl= " + Config.splashUrl)
                .append("\n user agent= " + Config.androidUA)
                .append("\n user device_id= " + Config.deviceId)
                .append("\n user uuid= " + Config.androidUUID)
                .append("\n user country= " + Config.userAccount.getCountryCode());
        Log.d(status.toString());
    }

    private void initializeComscore() {
        // Initialize comScore Application Tag library
        comScore.setAppContext(this.getApplicationContext());
        // Include any of the comScore Application Tag library initialization settings here
        comScore.setCustomerC2(Config.COMSCORE_CLIENT_ID);
        comScore.setPublisherSecret(Config.COMSCORE_SECRET_KEY);
        comScore.setAppName("App Mudah");
        comScore.enableAutoUpdate(60, false);
        comScore.onUxActive();
    }

    private void initUserAccount() {
        if (Config.enableUserAccount) {
            Config.userAccount = UserAccountModel.newInstance(getApplicationContext());
            if (Config.userAccount.isLogin()) {
                Crashlytics.getInstance().core.setString("userId", Config.userAccount.getUserId());
                tagKahuna(KahunaHelper.YES);
            }
        } else {
            //if disable, no need to load anything from sharePref.
            //simple initial to avoid NullPointer
            Config.userAccount = new UserAccountModel();
            tagKahuna(KahunaHelper.NO);
        }
        Config.userAccount.setCountryCode(MudahUtil.getCountryCode(getApplicationContext()));
    }

    private void tagKahuna(String value) {
        Log.d();
        KahunaHelper.tagAttributes(KahunaHelper.PAGE_USER_ACCOUNT, KahunaHelper.LOGIN_STATUS, value);
    }

    private void getTotalFavouriteTotal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AdViewFavouritesDAO adViewFavouritesDAO = new AdViewFavouritesDAO(getApplicationContext());
                BookmarksDAO bookmarkDAO = new BookmarksDAO(getApplicationContext());
                Config.allFavouritAdIds = adViewFavouritesDAO.getAllFavouritesID();
                Config.bookmarkTotal = bookmarkDAO.total();
            }
        }).start();
    }

}
