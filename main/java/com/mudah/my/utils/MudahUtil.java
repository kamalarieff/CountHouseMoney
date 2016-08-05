package com.mudah.my.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chatcafe.sdk.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import com.lib701.datasets.ACAd;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.BaseApplication;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.dao.AdViewFavouritesDAO;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.models.LabelParamsModel;
import com.squareup.leakcanary.RefWatcher;

import org.json.JSONException;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by pchin87 on 9/11/14.
 */
public class MudahUtil extends Utility {
    private static final String URL_FORMAT = "xxx://xxx.xxx/?";
    private static final String STR_TO_BE_REPLACED = "::|&|#|\'";
    private static String EQUAL = "=";
    private static String AND = "&";
    private static Gson gson = new Gson();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static boolean isValidMD5(String s) {
        return s.matches("[a-fA-F0-9]{32}");
    }

    /**
     * Function to perform the pop up dialog box containing html formatted messages
     *
     * @throws org.json.JSONException
     */
    public static void popUpMessage(Context context, String message) throws JSONException {

        final Dialog dialog = new Dialog(context, R.style.TranslucentFadeDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_popup_message);

        WebView webViewEn = (WebView) dialog.findViewById(R.id.wv_messages);
        webViewEn.loadData(message, "text/html", "UTF-8");

        RelativeLayout rlOutside = (RelativeLayout) dialog.findViewById(R.id.rl_outside);
        rlOutside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_close);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int pxToDp(int px, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public static String formatDate(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }

    public static Calendar getCalendarFromString(String strCalendar) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(strCalendar));// all done
        } catch (ParseException ignore) {
        }
        return cal;
    }

    public static boolean isValidSession() {
        boolean result = false;
        Calendar lastSearchTime = getCalendarFromString(Config.lastSearchTs);
        Calendar now = Calendar.getInstance();
        long timeDiff = now.getTimeInMillis() - lastSearchTime.getTimeInMillis();
        //shorter session for development
        if ((Log.isDebug && timeDiff < Config.SESSION_LENGTH_FOR_DEV_USERS) || (!Log.isDebug && timeDiff < Config.SESSION_LENGTH_FOR_USERS)) {
            result = true;
        }
        Log.d("isValidSession: " + result + ", timeDiff= " + timeDiff + " ms, lastSearchTime: " + Config.lastSearchTs);
        return result;
    }

    public static void saveClassInSharedPreferences(Context context, String sharedPrefsKey, Object object) {
        String json = gson.toJson(object);
        MudahPreferencesUtils.getSharedPreferences(context).edit()
                .putString(sharedPrefsKey, json)
                .apply();
        Log.d("saveClassInSharedPreferences sharedPrefsKey: " + sharedPrefsKey + ", json: " + json);
    }

    public static <T> T retrieveClassInSharedPreferences(Context context, String sharedPrefsKey, Class<T> type, String defaultValue) throws JsonSyntaxException, MalformedJsonException {
        String json = PreferencesUtils.getSharedPreferences(context).getString(sharedPrefsKey, defaultValue);
        Log.d("retrieveClassInSharedPreferencesClass<T> sharedPrefsKey: " + sharedPrefsKey + ", json: " + json);
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, type);
    }

    public static <T> T retrieveClassInSharedPreferences(Context context, String sharedPrefsKey, Type type, String defaultValue) throws JsonSyntaxException, MalformedJsonException {
        String json = PreferencesUtils.getSharedPreferences(context).getString(sharedPrefsKey, defaultValue);
        Log.d("retrieveClassInSharedPreferences sharedPrefsKey: " + sharedPrefsKey + ", json: " + json);
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, type);
    }

    public static void clearNotificationsByID(Context context, int notificationId) {
        NotificationManager nMgr = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(notificationId);
        Log.d("Notification CANCELED with id: " + notificationId);
    }

    public static void cancelAlarmManagerWithPendingIntent(PendingIntent pendingIntent, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.d("Notification AlarmManager CANCELED");
    }

    public static HashMap<String, String> convertStringToHashMap(String queryString) {
        HashMap<String, String> newHashMap = new HashMap<>();
        Uri uriSearch = Uri.parse(URL_FORMAT + queryString);
        String query = uriSearch.getQuery();//this will decode "q=samsung%20s5" to "q=samsung s5"
        String[] params = query.split(AND);
        String[] keyValueArr;
        for (String keyValue : params) {
            keyValueArr = keyValue.split(EQUAL);
            if (keyValueArr.length != 2) {
                continue;
            }
            newHashMap.put(keyValueArr[0], keyValueArr[1]);
        }
        return newHashMap;
    }

    public static HashMap<String, Integer> convertStringToHashMapInt(String queryString) {
        HashMap<String, Integer> newHashMap = new HashMap<>();
        Uri uriSearch = Uri.parse(URL_FORMAT + queryString);
        String query = uriSearch.getQuery();//this will decode "q=samsung%20s5" to "q=samsung s5"
        String[] params = query.split(AND);
        String[] keyValueArr;
        for (String keyValue : params) {
            keyValueArr = keyValue.split(EQUAL);
            if (keyValueArr.length != 2) {
                continue;
            }
            newHashMap.put(keyValueArr[0], Integer.parseInt(keyValueArr[1]));
        }
        return newHashMap;
    }

    public static Map<String, String> copySearchParamsForApi(Map<String, String> searchParams) {
        Map<String, String> params = new HashMap<>(searchParams);
        String q = params.get(Constants.KEYWORD);
        if (ACUtils.isEmpty(q))
            params.remove(Constants.KEYWORD);

        //when region is selected but area is not selected, set the default for w=1
        String w = params.get(Constants.AREA);
        String region = params.get(Constants.REGION);
        if (Constants.ALL_REGION.equals(region) || ACUtils.isEmpty(region)) {
            params.remove(Constants.REGION);
            params.remove(Constants.AREA);
        } else if (region != null && ACUtils.isEmpty(w)) {
            params.put(Constants.AREA, Constants.AREA_SPECIFIC);
        } else if (!ACUtils.isEmpty(w)) {
            params.put(Constants.AREA, w.substring(0, 1));
        }
        //Search Title only refinement
        String so = params.get(Constants.TITLE_ONLY);
        if (ACUtils.isEmpty(so)) {
            params.put(Constants.TITLE_ONLY, Constants.TITLE_ONLY_ENABLED);
        } else if ((Constants.TITLE_ONLY_DISABLED).equalsIgnoreCase(so)) {
            params.remove(Constants.TITLE_ONLY);
        }
        //Remove empty pricerange
        String priceRange = params.get(Constants.PRICERANGE);
        if (Constants.EMPTY_RANGE.equals(priceRange)) {
            params.remove(Constants.PRICERANGE);
        }
        //temporary: remove ad_id
        //To Do: find out where ad_id comes from
        params.remove(Constants.AD_ID);

        return params;
    }

    public static Uri getSearchUri(Map<String, String> params) {
        Uri.Builder builder = Uri.parse(Config.shareHost).buildUpon().appendPath("list");
        if (params != null) {
            for (String key : params.keySet()) {
                builder.appendQueryParameter(key, params.get(key));
            }
        }

        return builder.build();
    }

    public static HashMap<String, String> saveLastSearch(Context context, Map<String, String> query, Map<String, String> filter, String lastSearchedCategoryName, HashMap<String, String> filterLabel) {
        String queryString = getSearchUri(query).getEncodedQuery();
        Log.d(queryString);
        Log.d("######### SAVE queries:" + query + "  " + filter);
        String filterString = Constants.EMPTY_STRING;
        if (filter != null && !filter.isEmpty()) {
            filterString = getSearchUri(filter).getEncodedQuery();
        }
        if (ACUtils.isEmpty(lastSearchedCategoryName)) {
            lastSearchedCategoryName = Constants.EMPTY_STRING;
        }

        LabelParamsModel labelParamsModel = new LabelParamsModel();
        labelParamsModel.setParamsHashMap(filterLabel);
        String labelParamsModelJson = gson.toJson(labelParamsModel);
        Config.lastSearchTs = formatDate(Calendar.getInstance());
        SharedPreferences.Editor editor = PreferencesUtils.getSharedPreferences(context).edit();
        editor.putString(PreferencesUtils.LAST_SEARCH_QUERY, queryString)
                .putString(PreferencesUtils.LAST_FILTER_PARAMS, filterString)
                .putString(PreferencesUtils.LAST_FILTER_LABEL_PARAMS, labelParamsModelJson)
                .putString(PreferencesUtils.LAST_SEARCH_TIMESTAMP, Config.lastSearchTs);
        if (!ACUtils.isEmpty(lastSearchedCategoryName)) {
            editor.putString(MudahPreferencesUtils.LAST_SEARCHED_CATEGORY_NAME, lastSearchedCategoryName);
        }

        editor.apply();

        HashMap<String, String> savedValue = new HashMap<String, String>();
        savedValue.put(AdsListActivity.SEARCH_PARAMS, queryString);
        savedValue.put(AdsListActivity.FILTER_PARAMS, filterString);

        return savedValue;
    }

    public static HashMap<String, String> getLastQuerySearch(Context context) {
        String lastQuery = PreferencesUtils.getSharedPreferences(context)
                .getString(PreferencesUtils.LAST_SEARCH_QUERY, "");
        return convertStringToHashMap(lastQuery);
    }

    public static void saveLastQuerySearch(Context context, Map<String, String> query) {
        String queryString = getSearchUri(query).getEncodedQuery();
        Log.d(queryString);
        Log.d("######### SAVE queries search:" + query);

        PreferencesUtils.getSharedPreferences(context).edit()
                .putString(PreferencesUtils.LAST_SEARCH_QUERY, queryString)
                .apply();
    }

    public static String bundleToString(Bundle bundle) {
        StringBuffer strBundle = new StringBuffer();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                strBundle.append("\n " + key + " => " + bundle.get(key));
            }
            return strBundle.toString();
        } else {
            return "NULL";
        }
    }

    //Tool to generate OutOfMemoryError exception
    public static void generateOOM() throws Exception {
        if (Log.isDebug) {
            int iteratorValue = 20;
            Log.d("\n=================> OOM test started..\n");
            for (int outerIterator = 1; outerIterator < 20; outerIterator++) {
                Log.d("========= Iteration " + outerIterator + " Free Mem: " + Runtime.getRuntime().freeMemory());
                int loop1 = 2;
                int[] memoryFillIntVar = new int[iteratorValue];
                // feel memoryFillIntVar array in loop..
                do {
                    memoryFillIntVar[loop1] = 0;
                    loop1--;
                } while (loop1 > 0);
                iteratorValue = iteratorValue * 5;
                Log.d("\n========= Required Memory for next loop: " + iteratorValue);
                Thread.sleep(1000);
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (!GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.d("This device is not supported.");
                return false;
            }
        }
        return true;
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            if (activity == null)
                return;
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && activity.getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException nullError) {
            ACUtils.debug(nullError);
        } catch (Exception ignore) {
            ACUtils.debug(ignore);
        }
    }

    public static boolean isEmptyOrAllAdType(Map<String, String> params) {
        return ACUtils.isEmpty(params.get(Constants.STR_TYPE)) || Constants.TYPE_ALL.equalsIgnoreCase(params.get(Constants.STR_TYPE));
    }

    public static String getSellerTypeStr(String strSellerType) {
        // Ad source (Private(0), Company(1), ..)
        String sellerType = Constants.POSTED_BY_PRIVATE;
        if (!ACUtils.isEmpty(strSellerType) && (Constants.POSTED_BY_COMPANY_ID).equals(strSellerType)) {
            sellerType = Constants.POSTED_BY_COMPANY;
        }
        return sellerType;
    }

    public static boolean prepareParentCategoryInfo(String categoryId, AdViewAd ad) {
        boolean result = false;
        // Main Category
        ACCategory category = ACSettings.getACSettings().getCategory(categoryId);
        if (category != null && ad != null) {
            ad.setParentCategoryId(category.getParent());
            ad.setParentCategoryName(category.getParentName());
            result = true;
            Log.d("categoryId= " + categoryId + ",categoryParent = " + ad.getParentCategoryId() + " - " + ad.getParentCategoryName());
        }
        return result;
    }

    public static String getDeviceId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception ignore) {
        }
        return Constants.UNKNOWN;
    }

    public static String getUUID(Context context) {
        try {
            String androidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            byte[] theByteArray = androidId.getBytes();
            return UUID.nameUUIDFromBytes(theByteArray).toString();
        } catch (Exception ignore) {
        }
        return Constants.UNKNOWN;
    }

    public static boolean isProduction() {
        if (Config.RootType.PRODUCTION == Config.ROOT) {
            return true;
        } else {
            return false;
        }
    }

    public static void showExceedMaxFavouriteResult(Context context) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.favourite_error_title)
                .setMessage(context.getString(R.string.favourite_limit_error_title, Config.maxAdviewFavTotal))
                .setPositiveButton(R.string.favourite_button_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();

        dialog.show();
    }

    public static void showExceedMaxBookmarkResult(Context context) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.bookmark_error_title)
                .setMessage(context.getString(R.string.bookmark_limit_error_title, Config.maxBookmarksTotal))
                .setPositiveButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();

        dialog.show();
    }

    public static void showFavouriteResult(Context context, boolean isFavourited) {
        if (isFavourited) {
            Toast.makeText(context,
                    context.getString(R.string.favourite_notify_save_success),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,
                    context.getString(R.string.favourite_delete_success),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static String getSellerType(Map<String, String> searchParams) {
        String sellerType = searchParams.get(Constants.POSTED_BY) + Constants.EMPTY_STRING;
        if (ACUtils.isEmpty(sellerType)) {
            sellerType = Constants.POSTED_BY_ALL;
        }
        return sellerType;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context
                .getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context
                    .getResources()
                    .getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void detectMemoryLeakForFragment(Activity activity, Object object) {
        RefWatcher refWatcher = BaseApplication.getRefWatcher(activity);
        if (refWatcher != null) {
            refWatcher.watch(object);
        }
    }

    private static String generateUniqueId() {
        return String.valueOf(Long.toString((long) Math.floor((1 + Math.random()) * 0x10000), 16)).substring(1);
    }

    public static String getChatUniqueId() {
        StringBuilder sb = new StringBuilder();
        for (int count = 0; count < 8; count++) {
            sb.append(generateUniqueId());
        }
        return Constants.CHAT_UNIQUE_ID_PREFIX + sb.toString();
    }

    /****
     * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView
     ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewPager.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static String removeUnwantedSign(String original) {
        if (ACUtils.isEmpty(original))
            return Constants.EMPTY_STRING;
        else
            return original.replaceAll(STR_TO_BE_REPLACED, Constants.EMPTY_STRING);
    }

    public static void hideDialogDivider(Context context, Dialog dialog) {
        //hide divider layout
        int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setVisibility(View.GONE);
    }

    public static String getCountryCode(Context context) {
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            if (mTelephonyManager != null) {
                if (ACUtils.isEmpty(mTelephonyManager.getNetworkCountryIso())) {
                    Log.d("getSimCountryIso: " + mTelephonyManager.getSimCountryIso());
                    return mTelephonyManager.getSimCountryIso();
                } else {
                    Log.d("getNetworkCountryIso: " + mTelephonyManager.getNetworkCountryIso());
                    return mTelephonyManager.getNetworkCountryIso();
                }
            }
        } catch (Exception ignore) {
        }
        return Constants.EMPTY_STRING;
    }

    public static boolean isWhitelistedCountryCode(String countryCode) {
        if (ACUtils.isEmpty(countryCode))
            return false;
        else
            return Config.COUNTRY_CODE_WHITELIST_IST.contains(countryCode.toLowerCase(Locale.US));
    }

    public static boolean isFavouritedAd(int listId) {
        if (Config.allFavouritAdIds == null) {
            return false;
        } else {
            return Config.allFavouritAdIds.get(listId);
        }
    }

    public static void updateAdViewDAOThread(final AdViewFavouritesDAO adViewFavouritesDAO,
                                             final SparseBooleanArray toBeRemovedFromFav,
                                             final SparseArray<ACAd> toBeAddedToFav) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //remove favourite ads
                if (adViewFavouritesDAO != null && toBeRemovedFromFav != null && toBeRemovedFromFav.size() > 0) {
                    ArrayList<String> selections = new ArrayList<>();
                    int dataSize = toBeRemovedFromFav.size();
                    for (int i = 0; i < dataSize; i++) {
                        selections.add(toBeRemovedFromFav.keyAt(i) + Constants.EMPTY_STRING);
                    }
                    adViewFavouritesDAO.deleteMultipleFavourites(selections);
                }
                //add favourite add
                if (adViewFavouritesDAO != null && toBeAddedToFav != null && toBeAddedToFav.size() > 0) {
                    adViewFavouritesDAO.insertMultipleAdViewFavourites(toBeAddedToFav);
                }
            }
        }).start();
    }

    public static Point getWindowSize(Activity activity) {
        try {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Log.d("window W x H:  " + size.x + " x " + size.y);
            return size;
        } catch (Exception ignore) {
        }
        return null;
    }

    public static int daysBetween(Calendar dayOne, Calendar dayTwo) {

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
        }
    }

    public static void saveClickTime(Context context) {
        PreferencesUtils.getSharedPreferences(context)
                .edit()
                .putLong(PreferencesUtils.ONBOARD_LAST_VISIT, System.currentTimeMillis())
                .apply();
    }

    public static String convertDateToMonthAndYear(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy");
            String formattedDate = outputFormat.format(date);
            Log.d("Got the date: " + formattedDate);
            return formattedDate;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return Constants.EMPTY_STRING;
    }

    public static String getRegionName(String regionId) {
        return ACSettings.getACSettings().getRegionName(regionId);
    }

    public static String getSubAreaName(String areaId) {
        return ACSettings.getACSettings().getMunicipalityName(areaId);
    }

    public static void saveData(Context context, String name, String data) {
        if (context != null) {
            PreferencesUtils.getSharedPreferences(context).edit()
                    .putString(name, data)
                    .apply();
        }
    }

    public static class IntentDataID {
        public static final String INTENT_REDIRECT_INSERT_AD = "intent_redirect_insert_ad";
    }
}
