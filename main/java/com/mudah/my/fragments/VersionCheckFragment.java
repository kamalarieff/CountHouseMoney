package com.mudah.my.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseIntArray;

import com.lib701.fragments.RetainedDialogFragment;
import com.lib701.fragments.RetainedDialogFragment.OnDismissListener;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.ApiConfigs;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.DfpConfig;
import com.mudah.my.helpers.PDPNHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.BookmarkNotificationModel;
import com.mudah.my.models.DraftAdNotificationModel;
import com.mudah.my.models.InactiveInsertAdNotificationModel;
import com.mudah.my.models.InactiveUserNotificationModel;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class VersionCheckFragment extends Fragment {

    private static final int LOADER_VERSION_CHECK = VersionCheckFragment.class.hashCode();
    private static final String TAG_UPGRADE_DIALOG = "upgrade_dialog";
    private static boolean checked = false;
    private static int LATEST_APP_VERSION;
    //upper limits of screen size
    private final int MEDIUM_SIZE = 320;
    private final int LARGE_SIZE = 480;
    private final int XLARGE_SIZE = 720;
    private final int XXLARGE_SIZE = 1080;
    private NotificationsFragment fNotifications;
    private Context context;
    private String splashBgColor;
    private boolean isWaitUntilFinish;
    private int windowWidth;
    private int windowHeight;

    /**
     * Allow config to be reloaded to get the latest config
     */
    public static void resetCheckStatus() {
        Log.d();
        checked = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();
        Config.init();
        Log.d("checked: " + checked);
        //proceed if check is true
        if (!checked) {
            check();
        } else {
            sendBroadcastLoadFinish();
        }
    }

    private void check() {
        getLoaderManager().initLoader(LOADER_VERSION_CHECK, null,
                new BlocketLoader.Callbacks(Method.GET, "conf",
                        new HashMap<String, Object>(), getActivity()) {
                    @Override
                    public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                        checked = true;
                        try {
                            //Init Amplitude Tracking
                            Config.TrackAmplitude = data.optInt("track_amplitude", 0);
                            if (Config.TrackAmplitude > 0) {
                                Config.amplitudeTrackingStep = data.optJSONObject("amplitude");
                                if (Config.amplitudeTrackingStep == null) {
                                    Config.amplitudeTrackingStep = new JSONObject();
                                }
                            }
                            Log.d("enable TrackAmplitude: " + Config.TrackAmplitude);
                        } catch (Exception ignore) {
                            ACUtils.debug(ignore);
                        }
                        try {
                            setMaintenanceMode(data.optJSONObject("maintenance"));
                            Config.validSignInDays = data.optInt("valid_sign_in_days", 30);
                            Config.userIP = data.optString("user_ip");
                            Config.enableChat = data.optBoolean("enable_chat", false);
                            Config.enableOptimizely = data.optBoolean("enable_optimizely", false);
                            Config.enableAppsFlyer = data.optBoolean("appsflyer_enabled", true);
                            Config.totalAdsCount = data.optString("total_item_count", Config.DEFAULT_AD_COUNT);
                            Config.bannerImages = data.optJSONArray("banner_images");
                            Config.enableSellerOnboard = data.optBoolean("enable_seller_onboard", false);
                            Config.homepageRecommendedAds = data.optBoolean("enable_recommendation", false);
                            if (!data.optBoolean("enable_failed_over", true)) {
                                Config.apiList = null;
                            } else {
                                Config.setApiList(Config.apiList);
                            }
                            if (Config.isGoogleAppCrawler) {
                                Config.enableXitiTagging = false;
                                Config.enableTealiumTagging = false;
                            } else {
                                Config.enableXitiTagging = data.optBoolean("enable_xiti", true);
                                Config.enableTealiumTagging = data.optBoolean("enable_tealium", true);
                            }
                            Config.supportedAPIVersion = data.optString("api_version", Config.supportedAPIVersion);
                            boolean forcedUpgrade = data.optBoolean("upgrade", false);
                            Config.maxAdviewFavTotal = data.optInt("max_adview_favourite", Config.maxAdviewFavTotal);
                            Config.maxBookmarksTotal = data.optInt("max_bookmark", Config.maxBookmarksTotal);
                            Config.betaUserSignUp = data.optString("beta_user_sign_up").equalsIgnoreCase("1");
                            ApiConfigs.saveConfig(context, ApiConfigs.config_insertAdRedirect, data.optString("insert_ad_redirect_browser", "false"));
                            Config.apiRoot = data.getString("url");
                            JSONObject webLinkUrls = data.optJSONObject("weblink_urls");
                            if (webLinkUrls != null) {
                                Config.rulesUrl = webLinkUrls.optString("rules", Config.DEFAULT_RULES_URL);
                                Config.tipsUrl = webLinkUrls.optString("tips", Config.DEFAULT_TIPS_URL);
                                Config.loginProNiagaUrl = webLinkUrls.optString("proniaga_login", Config.DEFAULT_PRONIAGA_LOGIN);
                            }

                            setDFPConf(data.optJSONObject("dfp"));
                            // notification for non-force upgrade case
                            setXitiConf(data.optJSONObject("xiti"));
                            int version = Config.getAppVersion();
                            setApiConf(data.optJSONArray("pdpn"));
                            setInactiveUserConf(data.optJSONObject("inactive_user_notification"));
                            setDraftAdConf(data.optJSONObject("draft_ad_notification"));
                            setInactiveInsertAdConf(data.optJSONObject("inactive_insertad_notification"));
                            setBookmarkConf(data.optJSONObject("bookmark_notification"));
                            LATEST_APP_VERSION = data.optInt("app_version", version);
                            Log.d("app version: " + version + ", app version(api): " + LATEST_APP_VERSION + ", forcedUpgrade: " + forcedUpgrade);

                            //get upload image config
                            if (data.has("image_config")) {
                                ApiConfigs.setImageConfig(getActivity(), data.getJSONObject("image_config"));
                            }

                            Config.needRegionUpdate = isUpdateData(PreferencesUtils.REGION_UPDATED_TIMESTAMP, data.optString("region_updated_ts"), Config.regionUpdatedTs);
                            Config.needCategoryUpdate = isUpdateData(PreferencesUtils.CAT_UPDATED_TIMESTAMP, data.optString("category_updated_ts"), Config.categoryUpdatedTs);

                            if (version != 0 && version < LATEST_APP_VERSION) {
                                if (forcedUpgrade) {
                                    upgrade();
                                }
                                //for testing
                                //if (Integer.parseInt(CURRENT_APP_VERSION) < 2) {
                                Config.upgrade = forcedUpgrade;
                                // this helps to show the Notification fragment when the app first created, when checked is changed from false to true
                                Config.UPGRADE_PREFERENCES = PreferencesUtils.getSharedPreferences(getActivity()).getInt("never_ask_for_upgrade", 0);
                                if (Config.upgrade && Config.UPGRADE_PREFERENCES == 0) {
                                    fNotifications = (NotificationsFragment) getFragmentManager().findFragmentById(R.id.f_notifications);
                                    if (fNotifications != null)
                                        fNotifications.show();
                                }
                            }
                            //Check for the new splash image
                            checkSplashPage(data);

                        } catch (JSONException e) {
                            Log.e(e);
                            checked = false;
                            onLoadError(loader, data);
                        }

                        sendBroadcastLoadFinish();
                    }

                    @Override
                    public void onLoadError(BlocketLoader loader,
                                            JSONObject data) {
                        checked = false;
                        sendBroadcastLoadFinish();
                    }
                }
        );
    }

    public void setWindowScreenSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public void setWaitUntilFinish(boolean wait) {
        isWaitUntilFinish = wait;
    }

    private void sendBroadcastLoadFinish() {
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        if (isWaitUntilFinish) {
            Log.d();
            Intent loadComplete = new Intent(PreferencesUtils.LOAD_CONFIG_COMPLETE);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(loadComplete);
        }
    }

    private void setMaintenanceMode(JSONObject maintenance) {
        if (maintenance != null) {
            Config.maintenanceListing = maintenance.optBoolean("listing", false);
            Config.maintenanceInsertAd = maintenance.optBoolean("insert_ad", false);

            if (Config.maintenanceListing) {
                Config.maintenanceListingText = maintenance.optString("listing_ad_txt");
                Config.maintenanceListingSubText = maintenance.optString("listing_ad_sub_txt");

                if (ACUtils.isEmpty(Config.maintenanceListingText)) {
                    Config.maintenanceListingText = getString(R.string.maintenance_title);
                }
                if (ACUtils.isEmpty(Config.maintenanceListingSubText)) {
                    Config.maintenanceListingSubText = getString(R.string.maintenance_subtitle);
                }
            }

            if (Config.maintenanceInsertAd) {
                Config.maintenanceInsertAdText = maintenance.optString("insert_ad_txt");
                Config.maintenanceInsertAdSubText = maintenance.optString("insert_ad_sub_txt");

                if (ACUtils.isEmpty(Config.maintenanceInsertAdText)) {
                    Config.maintenanceInsertAdText = getString(R.string.maintenance_title);
                }
                if (ACUtils.isEmpty(Config.maintenanceInsertAdSubText)) {
                    Config.maintenanceInsertAdSubText = getString(R.string.maintenance_subtitle);
                }
            }
        }
        Log.d("maintenanceListing: " + Config.maintenanceListing + ", maintenanceInsertAd: " + Config.maintenanceInsertAd);
    }

    private void checkSplashPage(JSONObject data) {
        JSONObject splashURLs = data.optJSONObject("splashURLs");
        splashBgColor = data.optString("splashBgColor");

        if (splashURLs != null && isUpdateData(PreferencesUtils.SPLASH_UPDATED_TIMESTAMP, data.optString("splashUpdatedTs"), Config.splashUpdatedTs)) {
            saveSplashInfo(getSplashURLByScreenSize(splashURLs), splashBgColor);
        }
    }

    private String getSplashURLByScreenSize(JSONObject splashURLs) {
        String selectedSize;

        if (windowWidth >= XXLARGE_SIZE) {
            selectedSize = "1080p";
        } else if ((XLARGE_SIZE <= windowWidth) && (windowWidth <= XXLARGE_SIZE)) {
            selectedSize = "720p";
        } else if ((LARGE_SIZE <= windowWidth) && (windowWidth <= XLARGE_SIZE)) {
            selectedSize = "large";
        } else {
            selectedSize = "medium";
        }

        Log.d("screenSize = " + windowWidth + " x " + windowHeight + ", selectedSize = " + selectedSize);
        return splashURLs.optString(selectedSize);
    }

    private void saveSplashInfo(String splashUrl, String splashBgColor) {
        if (context != null) {
            PreferencesUtils.getSharedPreferences(context).edit()
                    .putString(PreferencesUtils.SPLASH_IMG_URL, splashUrl)
                    .putString(PreferencesUtils.SPLASH_BG_COLOR, splashBgColor)
                    .apply();
        }
    }

    private boolean isUpdateData(String dataName, String apiUpdatedTs, String localUpdatedTs) {
        if (ACUtils.isEmpty(localUpdatedTs)) {
            MudahUtil.saveData(context, dataName, apiUpdatedTs);
            return true;
        }
        boolean isUpdate = false;
        Calendar orgCal = Calendar.getInstance();
        Calendar newCal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {

            if (!ACUtils.isEmpty(apiUpdatedTs)) {
                //Default, try with dd/MM/yy
                orgCal.setTime(dateFormat.parse(localUpdatedTs));
                newCal.setTime(dateFormat.parse(apiUpdatedTs));
                if (orgCal.before(newCal)) {
                    isUpdate = true;
                    MudahUtil.saveData(context, dataName, apiUpdatedTs);
                } else {
                    isUpdate = false;
                }
            }
        } catch (Exception e) {
            ACUtils.debug(e, "FailedParseDate", "Failed to parse date format; dd/MM/yyyy. apiUpdatedTs = " + apiUpdatedTs + ", " + e);
        }

        Log.d(dataName + ": need Update? " + isUpdate);
        return isUpdate;
    }

    private void setDFPConf(JSONObject dfpData) {
        if (dfpData != null) {
            String dfp_enable = dfpData.optString("dfp_enable");
            if (dfp_enable.equalsIgnoreCase("1")) {
                Config.IS_GOOGLEAD_ENABLE = true;
            } else if (dfp_enable.equalsIgnoreCase("0")) {
                Config.IS_GOOGLEAD_ENABLE = false;
            }
            if (!ACUtils.isEmpty(dfp_enable) && dfp_enable.equalsIgnoreCase("1")) {
                JSONArray dfpArray = dfpData.optJSONArray("dfp_id");
                Map<Integer, String> dfpMap = setDfpIntMap(dfpArray);
                DfpConfig.setDfpMapBanner(dfpMap);

                JSONArray shortcatArray = dfpData.optJSONArray("short_cat_names");
                Map<Integer, String> shortCatMap = setDfpIntMap(shortcatArray);
                DfpConfig.setShortCatMapBanner(shortCatMap);

                JSONArray priceSetArray = dfpData.optJSONArray("price_set");
                Map<Integer, JSONObject> dfpPriceSetMap = setDfpPriceMap(priceSetArray);
                DfpConfig.setDfpPriceSet(dfpPriceSetMap);
            }

        }
    }

    public Map<Integer, JSONObject> setDfpPriceMap(JSONArray listArray) {
        Map<Integer, JSONObject> resultList = new HashMap<Integer, JSONObject>();
        if (listArray != null) {
            int arraySize = listArray.length();
            for (int i = 0; i < arraySize; i++) {
                JSONObject data = listArray.optJSONObject(i);
                if (data != null) {
                    Iterator<String> dfpMap = data.keys();
                    if (dfpMap.hasNext()) {
                        String key = dfpMap.next();
                        JSONObject value;
                        try {
                            value = data.getJSONObject(key);
                            resultList.put(Integer.parseInt(key), value);
                        } catch (JSONException e) {
                            ACUtils.debug(e);
                        }
                    }
                }
            }
        }
        return resultList;
    }

    public Map<Integer, String> setDfpIntMap(JSONArray listArray) {
        Map<Integer, String> resultList = new HashMap<Integer, String>();
        if (listArray != null) {
            int arraySize = listArray.length();
            for (int i = 0; i < arraySize; i++) {
                JSONObject data = listArray.optJSONObject(i);
                if (data != null) {
                    Iterator<String> dfpMap = data.keys();
                    if (dfpMap.hasNext()) {
                        try {
                            String key = dfpMap.next();
                            String value = data.optString(key);
                            resultList.put(Integer.parseInt(key), value);
                        } catch (NumberFormatException n) {
                            ACUtils.debug(n);
                        }
                    }
                }
            }
        }
        return resultList;
    }

    private void setXitiConf(JSONObject xitiData) {
        if (xitiData != null) {

            JSONArray listArray = xitiData.optJSONArray("list");
            SparseIntArray mList = ACUtils.getIntToIntMapByJson(listArray);

            JSONArray adviewArray = xitiData.optJSONArray("adview");
            SparseIntArray mAd = ACUtils.getIntToIntMapByJson(adviewArray);

            JSONArray adtypeArray = xitiData.optJSONArray("adtype");
            Map<String, Integer> mType = ACUtils.getStringToIntMapBuJson(adtypeArray);

            JSONArray level2Array = xitiData.optJSONArray("level2");
            Map<String, String> level2Map = ACUtils.getStringToStringMapByJson(level2Array);

            JSONArray insertAdformErrorArray = xitiData.optJSONArray("insert_ad_form_error");
            Map<String, String> insertAdformErrorMap = ACUtils.getStringToStringMapByJson(insertAdformErrorArray);

            JSONArray conditionArray = xitiData.optJSONArray("condition");
            Map<String, String> conditionMap = ACUtils.getStringToStringMapByJson(conditionArray);

            JSONArray inactiveUserNotificationArray = xitiData.optJSONArray("notification_reactivate");
            if (inactiveUserNotificationArray == null) {
                inactiveUserNotificationArray = new JSONArray();
            }
            Map<String, String> inactiveUserNotificationMap = ACUtils.getStringToStringMapByJson(inactiveUserNotificationArray);

            JSONArray draftAdNotificationArray = xitiData.optJSONArray("notification_draft_ad");
            if (draftAdNotificationArray == null) {
                draftAdNotificationArray = new JSONArray();
            }
            Map<String, String> draftAdNotificationMap = ACUtils.getStringToStringMapByJson(draftAdNotificationArray);

            JSONArray insertAdNotificationArray = xitiData.optJSONArray(XitiUtils.LEVEL2_NOTIFICATION_INSERT_AD);
            if (insertAdNotificationArray == null) {
                insertAdNotificationArray = new JSONArray();
            }
            Map<String, String> insertAdNotificationMap = ACUtils.getStringToStringMapByJson(insertAdNotificationArray);

            JSONArray bookmarkNotificationArray = xitiData.optJSONArray(XitiUtils.LEVEL2_NOTIFICATION_BOOKMARK);
            if (bookmarkNotificationArray == null) {
                bookmarkNotificationArray = new JSONArray();
            }
            Map<String, String> bookmarkNotificationMap = ACUtils.getStringToStringMapByJson(bookmarkNotificationArray);

            String domain = xitiData.optString("domain");
            String siteId = xitiData.optString("site_id");
            String site = xitiData.optString("site");

            PreferencesUtils.getSharedPreferences(getActivity())
                    .edit()
                    .putString(PreferencesUtils.XITI_DOMAIN, domain)
                    .putString(PreferencesUtils.XITI_SITE_ID, siteId)
                    .putString(PreferencesUtils.XITI_SITE, site)
                    .putString(PreferencesUtils.XITI_NOTIFICATION_REACTIVATE, inactiveUserNotificationArray.toString())
                    .putString(PreferencesUtils.XITI_NOTIFICATION_DRAFT_AD, draftAdNotificationArray.toString())
                    .putString(PreferencesUtils.XITI_NOTIFICATION_INSERT_AD, insertAdNotificationArray.toString())
                    .putString(PreferencesUtils.XITI_NOTIFICATION_BOOKMARK, bookmarkNotificationArray.toString())
                    .putString(PreferencesUtils.XITI_LEVEL2_MAP, level2Array.toString())
                    .putString(PreferencesUtils.XITI_ADVIEW_MAP, adviewArray.toString())
                    .putString(PreferencesUtils.XITI_LISTING_MAP, listArray.toString())
                    .putString(PreferencesUtils.XITI_ADTYPE_MAP, adtypeArray.toString())
                    .putString(PreferencesUtils.XITI_INSERT_FORM_ERR_MAP, insertAdformErrorArray.toString())
                    .apply();
            XitiUtils.setDomain(domain);
            XitiUtils.setSiteId(siteId);
            XitiUtils.setSite(site);

            XitiUtils.setMList(mList);
            XitiUtils.setMAd(mAd);
            XitiUtils.setMType(mType);
            XitiUtils.setLevel2Map(level2Map);
            XitiUtils.setConditionMap(conditionMap);
            XitiUtils.setInsertAdFormErrorMap(insertAdformErrorMap);
            XitiUtils.setInactiveUserNotificationMap(inactiveUserNotificationMap);
            XitiUtils.setDraftAdNotificationMap(draftAdNotificationMap);
            XitiUtils.setInsertAdNotificationMap(insertAdNotificationMap);
            XitiUtils.setBookmarkNotificationMap(bookmarkNotificationMap);

            //initialize after finish loading
            if (getActivity() != null) {
                XitiUtils.init(getActivity().getApplicationContext());
            }
        }
    }

    /**
     * Set the values of any configs in the api which will be stored into PDPNHelper
     *
     * @param apiData data retrieved from api
     * @throws org.json.JSONException
     */
    private void setApiConf(JSONArray apiData) throws JSONException {
        if (apiData != null) {
            PDPNHelper.setPDPNconf(apiData, getActivity());
        }
    }

    private void setInactiveUserConf(JSONObject apiData) throws JSONException {
        if (apiData == null) {
            apiData = new JSONObject(InactiveUserNotificationModel.DEFAULT_CONFIG);
        }
        InactiveUserNotificationModel inactiveUserNotificationModel = new InactiveUserNotificationModel();
        inactiveUserNotificationModel.saveInactiveUserNotificationConfig(apiData, getActivity());
    }

    private void setDraftAdConf(JSONObject apiData) throws JSONException {
        if (apiData == null) {
            apiData = new JSONObject(DraftAdNotificationModel.DEFAULT_CONFIG);
        }
        DraftAdNotificationModel draftAdNotificationModel = new DraftAdNotificationModel();
        draftAdNotificationModel.saveDraftAdNotificationConfig(apiData, getActivity());
    }

    private void setInactiveInsertAdConf(JSONObject apiData) throws JSONException {
        if (apiData == null) {
            apiData = new JSONObject(InactiveInsertAdNotificationModel.DEFAULT_CONFIG);
        }

        InactiveInsertAdNotificationModel inactiveInsertAdNotificationModel = new InactiveInsertAdNotificationModel();
        inactiveInsertAdNotificationModel.saveInsertAdNotificationConfig(apiData, getActivity());
    }

    private void setBookmarkConf(JSONObject apiData) throws JSONException {
        if (apiData == null) {
            apiData = new JSONObject(BookmarkNotificationModel.DEFAULT_CONFIG);
        }
        BookmarkNotificationModel bookmarkNotificationModel = new BookmarkNotificationModel();
        bookmarkNotificationModel.saveBookmarkNotificationConfig(apiData, getActivity());
    }

    public void upgrade() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        AlertDialogFragment.instantiate(
                getActivity().getString(R.string.version_upgrade),
                getActivity().getString(R.string.version_upgrade_message),
                new OnDismissListener() {
                    @Override
                    public void onDismiss(RetainedDialogFragment fragment) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(Config.MUDAH_APP_GOOGLE));
                        //If no applications match, Android displays a system message without crashing
                        startActivity(Intent.createChooser(intent, getResources().getText(R.string.version_upgrade_by)));
                        getActivity().finish();
                    }
                }
        ).show(getFragmentManager(), TAG_UPGRADE_DIALOG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("");
    }
}
