package com.mudah.my.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.google.gson.stream.MalformedJsonException;
import com.gravityrd.receng.web.webshop.jsondto.GravityEvent;
import com.gravityrd.receng.web.webshop.jsondto.GravityNameValue;
import com.lib701.connection.ACBlocketConnection;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACCategoryGroup;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACRegion;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.dao.BookmarksDAO;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.fragments.AdsFragment;
import com.mudah.my.fragments.ListBookmarksFragment;
import com.mudah.my.fragments.LoadingFragment;
import com.mudah.my.fragments.NotificationsFragment;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.KeywordHelper;
import com.mudah.my.helpers.NotificationBuilderHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.BookmarksModel;
import com.mudah.my.models.ChatCafe;
import com.mudah.my.models.InactiveUserNotificationModel;
import com.mudah.my.models.LabelParamsModel;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.AppsFlyerUtils;
import com.mudah.my.utils.BookmarkUtil;
import com.mudah.my.utils.CustomizedIntentActionUtils;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.GravityUtils;
import com.mudah.my.utils.MudahUtil;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AdsListActivity extends MudahBaseActivity {
    public static final String QUERY = "query";
    public static final String FILTER = "filter";
    public static final String FUNCTION_REQUEST = "function_request";
    public static final int REQUEST_CATEGORY = 1;
    public static final int REQUEST_BOOKMARKS = 2;
    public static final int REQUEST_SAVE_BOOKMARKS = 7;
    public static final int REQUEST_FROM_FILTER = 4;
    public static final int REQUEST_LOCATION = 5;
    public static final int REQUEST_CATEGORY_LOCATION = 6;

    public static final String SALE = "sale";
    public static final String SEARCH_PARAMS = "search_params";
    public static final String FILTER_PARAMS = "filter_params";
    public static final String FILTER_LABEL_PARAMS = "filter_label_params";
    public static final String VIEW_TYPE = "view_type";
    private static final String OTHERS_NAME = "OTHERS";
    private static final String INVALID_QUERY = "INVALID_QUERY";
    private static final String SEASON_CATEGORY_PREFIX = "89";
    private static int xitiCategory;
    private static boolean isNewInstance = true;
    protected LoadingFragment fLoading;
    private BookmarksDAO bookmarksDAO;
    private Context context;
    private String categoryName;
    private AdsFragment adsFragment;
    private String regionName;
    private String subregionName;
    private NotificationsFragment fNotifications;
    private int searchTotal = Integer.MIN_VALUE;
    private HashMap<String, String> searchParams = new HashMap<>();
    private HashMap<String, String> filterParams = new HashMap<>();
    private HashMap<String, String> filterLabelParams = new HashMap<>();
    private boolean isBookmarkedLatestListIdUpdated = false;
    private long bookmarkId = -1;
    private String globalQuery = null;
    private String globalFilter = null;
    private HashMap<String, String> paramsLabel;
    private boolean isCreateInactiveUserReminderDone = false;
    private String arrayListId[];
    private View semitransparentOverlay;
    private ActionBarHelper actionBarHelper;
    private SearchBox search;
    private boolean setMenuItemSearchVisible = true;
    private boolean isKeywordAvailableBeforeThis = false;
    private boolean isFromPushNotification = false;
    private String viewType = AmplitudeUtils.AD_TYPE_NORMAL;
    private KeywordHelper keywordHistoryHelper;
    private View.OnClickListener adsListOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.v_semitransparent_overlay:
                    if (search.isOpen()) {
                        search.performBackClick();
                    }
                    break;
            }
        }
    };

    private ActionBarHelper.ActionBarClickListener actionBarClickListener = new ActionBarHelper.ActionBarClickListener() {
        @Override
        public void onMenuClickListener(boolean showBack) {
            if (showBack) {
                formatActionBarWithKeyword(Constants.EMPTY_STRING);
                HashMap<String, String> newParamsMap = new HashMap<String, String>();
                String[] affectedParams = new String[]{Constants.KEYWORD};
                newParamsMap.put(Constants.KEYWORD, Constants.EMPTY_STRING);
                searchParams = getNewSearchParams(affectedParams, newParamsMap);
                search.setSearchString(Constants.EMPTY_STRING);
                updateLoading();
            } else {
                setMenuOpen();
            }
        }

        @Override
        public void onTitleClickListener() {
            search.bringToFront();
            openSearch();
        }

        @Override
        public void onSubtitleClickListener() {
            search.bringToFront();
            openSearch();
        }

        @Override
        public void onContainerClickListener() {
            search.bringToFront();
            openSearch();
        }
    };
    private AdsFragment.FloatingActionBarListener floatingActionBarListener = new AdsFragment.FloatingActionBarListener() {
        @Override
        public void onLeftSegmentClick() {
            Intent locationIntent = new Intent(context, LocationActivity.class);
            locationIntent.putExtra(SEARCH_PARAMS, searchParams);
            locationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(locationIntent);
        }

        @Override
        public void onMidSegmentClick() {
            Intent categoryIntent = new Intent(context, ACAdsSearchCategoryGroupChooser.class);
            startActivityForResult(categoryIntent, REQUEST_CATEGORY);
        }

        @Override
        public void onRightSegmentClick() {
            Intent filterIntent = new Intent(context, FilterMenuActivity.class);
            //if both category and type (s/k/u/h) are not exist, default it to 's' (e.g. All Category)
            if (!searchParams.containsKey(Constants.CATEGORY)) {
                ACReferences ref = ACReferences.getACReferences();
                ref.hasFilter = true;
            }
            if (!searchParams.containsKey(Constants.TYPE)) {
                searchParams.put(Constants.TYPE, Constants.TYPE_SALE);
            }
            filterIntent.putExtra(SEARCH_PARAMS, searchParams);
            filterIntent.putExtra(FILTER_PARAMS, filterParams);
            filterIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //For BookmarkUtil
            String keyword = searchParams.get(Constants.KEYWORD);
            if (keyword != null)
                filterIntent.putExtra(BookmarkUtil.SEARCH_QUERY, keyword);
            filterIntent.putExtra(BookmarkUtil.REGION, regionName);
            filterIntent.putExtra(BookmarkUtil.SUBREGION, subregionName);

            startActivity(filterIntent);

        }
    };

    private SearchBox.SearchListener searchListener = new SearchBox.SearchListener() {

        @Override
        public void onSearchOpened() {
            semitransparentOverlay.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSearchClosed() {
            semitransparentOverlay.setVisibility(View.GONE);
            search.hideCircularly(AdsListActivity.this);
        }

        @Override
        public void onSearchTermChanged() {
            // React to the search term changing
            // Called after it has updated results
        }

        @Override
        public void onSearch(String searchTerm) {
            if (isFinishing())
                return;
            HashMap<String, String> newParamsMap = new HashMap<String, String>();
            String[] affectedParams = new String[]{Constants.KEYWORD};
            if (ACUtils.isEmpty(searchTerm)) {
                search.setMaterialMenuAnimationDuration(500);
                formatActionBarWithKeyword(null);
                newParamsMap.put(Constants.KEYWORD, Constants.EMPTY_STRING);
            } else {
                //Strip off html code like %20 when users copy text from web
                searchTerm = ACUtils.getHtmlFromString(searchTerm).toString();
                search.setMaterialMenuAnimationDuration(0);
                formatActionBarWithKeyword(searchTerm);
                newParamsMap.put(Constants.KEYWORD, searchTerm);
                tagSearchOrSave(KahunaHelper.SEARCHED_KEYWORD_EVENT, searchTerm);
            }

            searchParams = getNewSearchParams(affectedParams, newParamsMap);
            updateLoading();
        }

        @Override
        public void onSearchCleared() {
        }

    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String keyword = searchParams.get(Constants.KEYWORD);
        if (!ACUtils.isEmpty(keyword) && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            actionBarClickListener.onMenuClickListener(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void createActionBar() {
        actionBarHelper = new ActionBarHelper(this);
        actionBarHelper.createActionBar(R.id.actionbar);
        actionBarHelper.configureActionbarForSearch(true);
        actionBarHelper.setActionBarClickListener(actionBarClickListener);
        showActionBarRedBorder(true);

        DrawerLayoutUtils drawerLayoutUtils = actionBarHelper.createSlideInMenu(R.id.drawer_layout, R.id.left_drawer, ActionBarHelper.SHOW_MENU_BUTTON_ON_ACTIONBAR);
        setDrawerLayoutUtils(drawerLayoutUtils);
    }


    private void initKeywordHistory() {
        keywordHistoryHelper = new KeywordHelper(getApplicationContext());
        keywordHistoryHelper.init();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        try {
            setContentView(R.layout.activity_adslist);
            this.context = this;
            //remove Material background, to prevent overdrawing
            getWindow().setBackgroundDrawable(null);
            bookmarksDAO = new BookmarksDAO(this);
            createActionBar();

            semitransparentOverlay = (View) findViewById(R.id.v_semitransparent_overlay);
            semitransparentOverlay.setOnClickListener(adsListOnClickListener);

            search = (SearchBox) findViewById(R.id.searchbox);

            search.setHint(getResources().getString(R.string.searchbox_hint));
            search.setSearchListener(searchListener);

            //hide the optional upgrade notification first
            fNotifications = (NotificationsFragment) getSupportFragmentManager().findFragmentById(R.id.f_notifications);
            fNotifications.hide();

            adsFragment = (AdsFragment) getSupportFragmentManager().findFragmentById(R.id.f_ads);
            adsFragment.setFloatingActionBarListener(floatingActionBarListener);

            fLoading = (LoadingFragment) getSupportFragmentManager().findFragmentById(R.id.f_loading);
            fLoading.setOnRetryListener(new LoadingFragment.OnRetryListener() {
                @Override
                public void onRetry() {
                    updateLoading();
                }
            });

            //prevent loader from restarting while device is rotated
            if (savedInstanceState != null && searchTotal != Integer.MIN_VALUE) {
                isNewInstance = savedInstanceState.getBoolean("isNewInstance");
                Log.d("savedInstanceState=" + isNewInstance);
            } else
                isNewInstance = true;

            Intent intent = getIntent();
            if (setValuesFromIntent(intent)) {
                getQuery(true);
                getFilter(true);
            }

            boolean isExecuteSearchByRequest = false;
            if (!isCreateInactiveUserReminderDone && intent != null && intent.getExtras() != null) {
                Bundle bundle = intent.getExtras();
                Uri uri = getIntent().getData();
                viewType = AmplitudeUtils.getViewType(bundle.getString(VIEW_TYPE));

                if (bundle.containsKey(FUNCTION_REQUEST)) {
                    isExecuteSearchByRequest = true;
                    int requestCode = bundle.getInt(FUNCTION_REQUEST);
                    executeSearchByRequest(requestCode, intent);
                } else if (uri != null && Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                    deeplinkListingPage(uri);
                }
            }

            if (isFromPushNotification || isCreateInactiveUserReminderDone || !isExecuteSearchByRequest) {
                // First time load or Come from inactive notification
                updateLoading();
            }

            setInitialActionbarUI();

        } catch (Throwable throwable) {
            ACUtils.debug(throwable, "AdsList_onCreate", "query: " + globalQuery + " ==filter:" + globalFilter);
        }
        initKeywordHistory();
        configureInactiveUserNotification();
    }

    private void deeplinkListingPage(Uri uri) {
        AppsFlyerUtils.sendDeepLinkData(this);
        String strUrl = uri.toString();

        Log.d("Redirected from " + strUrl);
        try {
            String catID = extractCategoryIdFromUrl(strUrl);
            if (ACUtils.isEmpty(catID)) {
                //Throw exception so that it will be caught and the app will be redirected to listing page
                throw new IllegalArgumentException("URL format unexpected");
            } else {
                if (!(SALE).equalsIgnoreCase(catID)) {
                    ACReferences ref = ACReferences.getACReferences();
                    ref.setCategoryId(catID);
                    ref.setRegionId(Constants.ALL_CATEGORY);
                    int requestCode = AdsListActivity.REQUEST_CATEGORY_LOCATION;
                    executeSearchByRequest(requestCode, null);

                    if (XitiUtils.initFromLastConfig(getApplicationContext()) == null) {
                        Log.e("Xiti initialization is wrong");
                    } else {
                        int level2 = XitiUtils.getLevel2ByCategoryIDAndPage(Integer.parseInt(catID), XitiUtils.LISTING);
                        EventTrackingUtils.sendClick(Integer.toString(level2), "Redirect_from_website", XitiUtils.NAVIGATION);
                    }
                }
            }
        } catch (Exception e) {
            ACUtils.debug(e, "List_url", strUrl, false);
        }
    }

    private String extractCategoryIdFromUrl(String strUrl) {

        String catId = Constants.EMPTY_STRING;
        try {
            Uri uri = Uri.parse(strUrl);
            String path = uri.getPath();
            String[] arrayPath = path.split(Constants.SLASH);
            if ((Constants.MALAYSIA).equalsIgnoreCase(arrayPath[1])) {
                catId = uri.getQueryParameter(Constants.CATEGORY);
                if (ACUtils.isEmpty(catId)) {
                    int indexStart = arrayPath[2].lastIndexOf(Constants.EMPTY_RANGE);
                    if (indexStart >= 0) {
                        //http://www.mudah.my/Malaysia/Pets-for-sale-5080
                        catId = arrayPath[2].substring(indexStart + 1, indexStart + 5);
                    }
                }
            }
            //Test if catId is a number, if not default it to 0
            Integer.parseInt(catId);
        } catch (NullPointerException e) {
            ACUtils.debug(e, "AdsList_extractCatIdFromUrl", strUrl);
        } catch (NumberFormatException numberError){
            catId = Constants.ALL_CATEGORY;
        }

        return catId;
    }

    private void configureInactiveUserNotification() {
        final Context context = this;
        final Handler threadHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Check for notifications setting
                        InactiveUserNotificationModel inactiveUserNotificationModel = InactiveUserNotificationModel.newInstance(getBaseContext());
                        NotificationBuilderHelper notificationBuilderHelper = new NotificationBuilderHelper();
                        notificationBuilderHelper.createInactiveUserReminder(context, Config.INACTIVE_USER_MAIN_NOTIFICATION, inactiveUserNotificationModel);
                    }
                });
            }
        }).start();
    }

    private boolean setValuesFromIntent(Intent intent) {
        boolean loadFromLastSearch = true;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {

                // Check for DFP values
                if (bundle.containsKey(FilterMenuActivity.DFP_PARAMS))
                    paramsLabel = (HashMap<String, String>) bundle.getSerializable(FilterMenuActivity.DFP_PARAMS);

                // Check if there are settings to redirect to browser insert ad instead of native insert ad
                if (bundle.containsKey(MudahUtil.IntentDataID.INTENT_REDIRECT_INSERT_AD)) {
                    boolean insertAdRedirect = bundle.getBoolean(MudahUtil.IntentDataID.INTENT_REDIRECT_INSERT_AD);
                    CustomizedIntentActionUtils.InsertAdRedirectToBrowser(insertAdRedirect, this);
                }

                isCreateInactiveUserReminderDone = false;
                if (bundle.containsKey(Config.INACTIVE_USER_MAIN_NOTIFICATION) || bundle.containsKey(Config.INACTIVE_USER_SECONDARY_NOTIFICATION)) {
                    isCreateInactiveUserReminderDone = true;
                }

                Log.d("isCreateInactiveUserReminderDone:" + isCreateInactiveUserReminderDone);

                if (bundle.containsKey(Config.PUSH_NOTIFICATION) || bundle.containsKey(HomepageActivity.BANNER_LINK)) {
                    if (bundle.containsKey(Config.PUSH_NOTIFICATION)) {
                        isFromPushNotification = true;
                    }
                    globalQuery = bundle.getString(QUERY);
                    globalFilter = bundle.getString(FILTER);
                    searchParams = MudahUtil.convertStringToHashMap(globalQuery);
                    if (searchParams.containsKey(Constants.TYPE)) {
                        searchParams.put(Constants.TYPE, Constants.TYPE_SALE);
                    }

                    filterParams = MudahUtil.convertStringToHashMap(globalFilter);
                    Log.d(" isPushNotification: " + isFromPushNotification + ", globalQuery: " + globalQuery
                            + ", globalFilter: " + globalFilter);
                    loadFromLastSearch = false;
                }
            }

        }
        return loadFromLastSearch;
    }

    private void setInitialActionbarUI() {
        if (!ACUtils.isEmpty(searchParams.get(Constants.KEYWORD))) {
            formatActionBarWithKeyword(searchParams.get(Constants.KEYWORD));
        } else {
            formatActionBarWithKeyword(null);
        }
    }

    public void openSearch() {
        search.revealFromMenuItem(this);
        search.clearSearchable();
        ArrayList<KeywordHelper.KeywordProperty> keywordProperties = keywordHistoryHelper.getKeywordProperties();
        if (keywordProperties != null) {
            for (int i = keywordProperties.size() - 1; i >= 0; i--) {
                KeywordHelper.KeywordProperty keywordProperty = keywordProperties.get(i);
                //Add history and skip the word that is already in the search box
                if ((KeywordHelper.KeywordProperty.HISTORY).equalsIgnoreCase(keywordProperty.getType()) && !search.getSearchText().equalsIgnoreCase(keywordProperty.getKeyword())) {
                    SearchResult option = new SearchResult(keywordProperty.getKeyword(), ContextCompat.getDrawable(this, R.drawable.ic_history));
                    search.addSearchable(option);
                }
            }
        }
    }

    private String extractParams(String paramName) {
        if (searchParams.containsKey(paramName)) {
            if (searchParams.get(paramName) != null) {
                return searchParams.get(paramName);
            }
        }
        return Constants.EMPTY_STRING;
    }

    private void setSortByInActionbar() {
        String sortBy = extractParams(Constants.SORT_BY);
        String subtitle = getResources().getString(R.string.actionbar_sort_by_date);
        if (!ACUtils.isEmpty(sortBy)) {
            if (Integer.valueOf(sortBy) == Constants.SORT_BY_PRICE) {
                subtitle = getResources().getString(R.string.actionbar_sort_by_price);
            }
        }
        actionBarHelper.setSubtitle(subtitle);
    }

    private void formatActionBarWithKeyword(String searchTerm) {
        //NO KEYWORD
        if (ACUtils.isEmpty(searchTerm)) {
            actionBarHelper.setMenuButton(MaterialMenuDrawable.IconState.BURGER, isKeywordAvailableBeforeThis);
            actionBarHelper.clearTitle();
            actionBarHelper.clearSubtitle();
            actionBarHelper.enableLogo(true);
            setMenuItemSearchVisible = true;
            isKeywordAvailableBeforeThis = false;
        }
        //GOT KEYWORD
        else {
            actionBarHelper.setMenuButton(MaterialMenuDrawable.IconState.ARROW, !isKeywordAvailableBeforeThis);
            actionBarHelper.setTitle(searchTerm);
            actionBarHelper.enableLogo(false);
            search.setSearchString(searchTerm);
            setSortByInActionbar();
            setMenuItemSearchVisible = false;
            isKeywordAvailableBeforeThis = true;
        }
        this.invalidateOptionsMenu();
    }

    private void getQuery(boolean fromLastSearch) {
        if (globalQuery == null || fromLastSearch) {
            globalQuery = PreferencesUtils.getSharedPreferences(AdsListActivity.this)
                    .getString(PreferencesUtils.LAST_SEARCH_QUERY, Constants.EMPTY_STRING);
            searchParams = MudahUtil.convertStringToHashMap(globalQuery);
        }
    }

    private void getFilter(boolean fromLastSearch) {
        if (globalFilter == null || fromLastSearch) {
            SharedPreferences sharedPreferences = PreferencesUtils.getSharedPreferences(AdsListActivity.this);
            globalFilter = sharedPreferences.getString(PreferencesUtils.LAST_FILTER_PARAMS, Constants.EMPTY_STRING);
            restoreFilterLabelfromSharedPref();
        }
    }

    //Restore the values from the shared Preference to the hash map
    private void restoreFilterLabelfromSharedPref() {
        try {
            LabelParamsModel labelParamsModel = MudahUtil.retrieveClassInSharedPreferences(getApplicationContext(), PreferencesUtils.LAST_FILTER_LABEL_PARAMS, LabelParamsModel.class, Constants.EMPTY_STRING);
            if (labelParamsModel != null) {
                filterLabelParams = labelParamsModel.getParamsHashMap();
            }
        } catch (MalformedJsonException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, String> getNewSearchParams(String[] affectedParams, HashMap<String, String> newParamsMap) {

        HashMap<String, String> newSearchParams = new HashMap<>();
        getQuery(false);

        if (!ACUtils.isEmpty(globalQuery)) {
            newSearchParams = MudahUtil.convertStringToHashMap(globalQuery);
        }
        // remove existing param before adding them
        if (affectedParams != null) {
            for (String param : affectedParams) {
                newSearchParams.remove(param);
            }
        }

        // add new param
        Iterator iterator = newParamsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry keyValue = (Map.Entry) iterator.next();
            String key = keyValue.getKey().toString();
            if (keyValue.getValue() != null) {
                String value = keyValue.getValue().toString();
                if (ACUtils.isEmpty(value)) {
                    if (newSearchParams.containsKey(key)) {
                        newSearchParams.remove(key);
                    }
                } else {
                    newSearchParams.put(keyValue.getKey().toString(), keyValue.getValue().toString());
                }
            }
        }

        return newSearchParams;
    }

    //Need to implement this method for launchMode="singleInstance" since onCreate will not be called
    @Override
    protected void onNewIntent(Intent intent) {
        try {
            boolean isExecuteSearchByRequest = false;
            if (intent != null && intent.getExtras() != null) {
                Bundle bundle = intent.getExtras();
                viewType = AmplitudeUtils.getViewType(bundle.getString(VIEW_TYPE));

                if (bundle.containsKey(FUNCTION_REQUEST)) {
                    isExecuteSearchByRequest = true;
                    int requestCode = bundle.getInt(FUNCTION_REQUEST);
                    executeSearchByRequest(requestCode, intent);
                }
            }
            if (!isExecuteSearchByRequest) {
                // First time load
                updateLoading();
            }
            setValuesFromIntent(intent);
        } catch (Throwable error) {
            ACUtils.debug(error, "AdsList_onNewIntent", "query:" + globalQuery + ", filter:" + globalFilter);
        }
    }

    private boolean initLoadingData() {
        if (fLoading == null)
            return false;//stop doing anything
        fLoading.updateMaintenanceMode(Config.maintenanceListing, false);
        if (Config.maintenanceListing) {
            return false;//stop doing anything
        }
        Log.d("categoriesFetched: " + ACReferences.categoriesFetched + ", regionsFetched: " + ACReferences.regionsFetched);
        // make sure regions and categories are fetched before updating UI and conducting first search
        if (!ACReferences.categoriesFetched || !ACReferences.regionsFetched) {
            fLoading.show();
            Handler handler = new MyHandler(this);

            // fetch sequentially instead of in parallel to prevent FirstTimeDialog from popping up twice
            if (!ACReferences.categoriesFetched) {
                ACBlocketConnection.fetchCategories(this, handler);
            } else if (!ACReferences.regionsFetched) {
                ACBlocketConnection.fetchRegions(this, handler);
            }
            return false;
        }

        fLoading.hide();
        return true;
    }

    protected void updateLoading() {
        if (!initLoadingData()) {
            return;
        }

        if ((!ACUtils.isEmpty(globalQuery) || (!searchParams.isEmpty()))) {
            loadSearch();
        } else {
            loadLastSearch();
        }
        //send tagging for Notification Click
        xitiTag();
    }


    private void loadLastSearch() {

        getQuery(true);
        getFilter(true);

        Log.d("query= " + globalQuery + ", filter= " + globalFilter);
        loadSearch();
    }


    public void saveBookmark() {
        String query = MudahUtil.getSearchUri(searchParams).getEncodedQuery();
        String filter = Constants.EMPTY_STRING;
        String keyword = searchParams.get(Constants.KEYWORD);

        if (!filterParams.isEmpty())
            filter = MudahUtil.getSearchUri(filterParams).getEncodedQuery();

        HashMap<String, String> values = new HashMap<>();
        values.put(BookmarkUtil.CATEGORY, categoryName);
        values.put(BookmarkUtil.REGION, regionName);
        values.put(BookmarkUtil.SUBREGION, subregionName);
        values.put(BookmarkUtil.QUERY, query);
        values.put(BookmarkUtil.FILTER, filter);
        if (keyword != null)
            values.put(BookmarkUtil.SEARCH_QUERY, keyword);
        if (arrayListId != null && arrayListId.length > 0) {
            String strAdIds = TextUtils.join(",", arrayListId);
            values.put(BookmarkUtil.LIST_IDS, strAdIds);
        }

        BookmarkUtil bookmarkUtil = new BookmarkUtil(this, values, arrayListId);
        bookmarkUtil.showSaveBookmarksDialog(true);
    }

    private void tagSearchOrSave(String event, String keyword) {
        AdViewAd acAd = getAdDetailForTagging();
        KahunaHelper.tagEvent(event, acAd);
        if (!ACUtils.isEmpty(keyword)) {
            KahunaHelper.tagAttributes(KahunaHelper.PAGE_KW_SEARCHED, KahunaHelper.LAST_TITLE_SEARCH, keyword);
        } else {
            KahunaHelper.tagDateAttributes(KahunaHelper.PAGE_SEARCH_SAVED);
            AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.SAVED_SEARCH_EVENT, acAd);
        }
    }

    private AdViewAd getAdDetailForTagging() {
        AdViewAd acAd = new AdViewAd();
        ACReferences ref = ACReferences.getACReferences();
        if (MudahUtil.prepareParentCategoryInfo(ref.categoryId, acAd)) {
            acAd.setCategoryName(categoryName);
        } else {
            acAd.setParentCategoryName(getString(R.string.ads_search_category_all));//all category
        }

        acAd.setRegion(regionName);
        acAd.setSubRegionName(subregionName);
        return acAd;
    }

    private boolean isSeasonCategory(String categoryId) {
        return (!ACUtils.isEmpty(categoryId) && SEASON_CATEGORY_PREFIX.equals(categoryId.substring(0, 2)));
    }

    private void setTagAsParentGroupForSeasonCategory(String currentGroupId) {
        if (!ACUtils.isEmpty(currentGroupId) && isSeasonCategory(currentGroupId) && adsFragment != null) {
            adsFragment.setCateogryGroupForSeasonCategory(currentGroupId);
        }
    }

    private void loadSearch() {
        filterParams = new HashMap<>();
        ACReferences currentRef = ACReferences.getACReferences();
        String currentGroupId = currentRef.categoryGroupId;
        adsFragment.setCateogryGroupForSeasonCategory(Constants.EMPTY_STRING);
        ACReferences.clearACReferences();
        if (searchParams.size() != 0) {
            ACReferences ref = ACReferences.getACReferences();
            ACSettings settings = ACSettings.getACSettings();

            try {
                if (searchParams.containsKey(Constants.CATEGORY)) {
                    String paramValue = searchParams.get(Constants.CATEGORY);
                    String categoryParent = settings.getCategoryParent(paramValue);
                    if (ACUtils.isEmpty(categoryParent))
                        categoryParent = paramValue;
                    Log.d("category id= " + paramValue + ", categoryParent = " + categoryParent);
                    xitiCategory = Integer.parseInt(paramValue);
                    // category
                    ACCategoryGroup categoryGroup = settings
                            .getAcCategoriesForSearchDataSet()
                            .findCategoryGroupById(categoryParent);
                    if (categoryGroup != null) {
                        ref.categoryGroupId = categoryGroup.getId();
                        setTagAsParentGroupForSeasonCategory(currentGroupId);
                        if (!paramValue.equals(categoryGroup.getId())) {
                            // not a category group => is a category
                            ACCategory category = categoryGroup.findCategoryById(paramValue);
                            ref.categoryId = category.getId();
                            ref.hasFilter = category.hasFilter();
                            ref.defaultAdType = category.getDefaultAdType();
                        } else {
                            ref.hasFilter = categoryGroup.hasFilter();
                            ref.defaultAdType = categoryGroup.getDefaultAdType();
                        }
                    }

                    if (ACUtils.isEmpty(ref.defaultAdType)) {
                        ref.defaultAdType = Constants.TYPE_SALE;
                    }

                    if (!searchParams.containsKey(Constants.TYPE)) {
                        searchParams.put(Constants.TYPE, ref.defaultAdType);
                    }
                } else {
                    xitiCategory = Integer.parseInt(Constants.ALL_CATEGORY);// All Category
                }

                if (searchParams.containsKey(Constants.REGION)) {
                    // region
                    ACRegion region = settings.getAcRegionsDataSet().findRegionById(searchParams.get(Constants.REGION));
                    String subAreaValue = searchParams.get(Constants.SUBAREA);
                    boolean hasSubArea = false;
                    if (region != null) {
                        ref.setRegionId(region.getId());
                        if (!ACUtils.isEmpty(subAreaValue)) {
                            hasSubArea = region.checkMunicipalityInRegion(subAreaValue);
                        }
                    }

                    Log.d("hasSubArea: " + hasSubArea + ", subAreaValue: " + subAreaValue);
                    if (hasSubArea) {
                        ref.setMunicipalityId(subAreaValue);
                    } else {
                        ref.setMunicipalityId(null);
                    }
                }
                if (!ACUtils.isEmpty(globalFilter)) {
                    filterParams.putAll(MudahUtil.convertStringToHashMap(globalFilter));
                }
            } catch (Exception e) {
                ACUtils.debug(e, "AdsList_loadSearch", "query: " + globalQuery + ", filter: " + globalFilter);
            }

            if (isNewInstance) {
                isNewInstance = false;
            }
        } else {
            searchParams = new HashMap<>();
        }

        if (!searchParams.containsKey(Constants.TYPE) || ACUtils.isEmpty(searchParams.get(Constants.TYPE))) {
            searchParams.put(Constants.TYPE, Constants.TYPE_SALE);
        }

        search();
    }

    private void tagGravityEvent(String eventType, Map<String, String> allParams) {
        GravityEvent event = new GravityEvent();
        event.eventType = eventType;

        ArrayList<GravityNameValue> nameValues = new ArrayList<>();

        String[] nameFields = {GravityUtils.FIELD_KEYWORD, GravityUtils.FIELD_REGION, GravityUtils.FIELD_AREA, GravityUtils.FIELD_CATEGORY_ID};
        String[] valueFields = {Constants.KEYWORD, Constants.REGION, Constants.SUBAREA, Constants.CATEGORY};

        int idx = 0;
        for (String valueField : valueFields) {
            if (allParams.containsKey(valueField)) {
                GravityNameValue keyword = new GravityNameValue(nameFields[idx], allParams.get(valueField));
                nameValues.add(keyword);
            }
            idx++;
        }

        event.nameValues = nameValues.toArray(new GravityNameValue[nameValues.size()]);

        GravityUtils.sendEventAsync(event);
    }

    private void storeKeyword() {
        if (!ACUtils.isEmpty(searchParams.get(Constants.KEYWORD))) {
            final Handler threadHandler;
            threadHandler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    threadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (searchParams == null)
                                return;
                            KeywordHelper.KeywordProperty keywordProperty =
                                    keywordHistoryHelper.new KeywordProperty(
                                            searchParams.get(Constants.KEYWORD),
                                            Calendar.getInstance(),
                                            KeywordHelper.KeywordProperty.HISTORY
                                    );

                            keywordHistoryHelper.addKeywordProperty(keywordProperty);
                            keywordHistoryHelper.save();
                        }
                    });
                }
            }).start();
        }
    }

    public void search() {
        Log.d();
        fLoading.show();
        searchTotal = Integer.MIN_VALUE;
        updateQueryFields();
        final Map<String, String> allParams = new HashMap<>();
        if (searchParams != null)
            allParams.putAll(searchParams);
        if (filterParams != null)
            allParams.putAll(filterParams);

        tagGravityEvent(GravityUtils.EVENT_TYPE_SEARCH, allParams);
        storeKeyword();

        final Map<String, Object> params = new HashMap<String, Object>(MudahUtil.copySearchParamsForApi(allParams));
        adsFragment.setOnLoadCompleteListener(new BlocketLoader.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                try {
                    fLoading.hide();
                    if (data.has("error")) {
                        handleErrorResponse(data.optJSONObject("error"));
                    } else if (data.getJSONArray("ads") != null) {

                        String strTotal = data.optString("filtered");
                        if (!ACUtils.isEmpty(strTotal))
                            searchTotal = Integer.parseInt(strTotal);
                        else
                            searchTotal = 0;
                        if (params.get(Constants.OFFSET) == null || (int) params.get(Constants.OFFSET) == 0) {
                            ACUtils.logCrashlytics("query: " + params.toString() + ", searchTotal: " + searchTotal + ", viewMode: " + Config.listViewMode);
                            AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.LIST_EVENT, getAdDetailForTagging(), Constants.EMPTY_STRING + params.get(Constants.KEYWORD), viewType);
                        }

                        if (searchTotal > Integer.MIN_VALUE) {
                            ACUtils.logToast(getApplicationContext(), "# of ads " + searchTotal);
                        }

                        if (adsFragment != null)
                            adsFragment.setResultTotal(searchTotal);

                        JSONArray jsonArrayItems = data.getJSONArray("ads");
                        if (jsonArrayItems.length() > 0) {
                            boolean isAdsAvailable = false;

                            if (jsonArrayItems.length() > BookmarksModel.numberOfListIdSaved) {
                                arrayListId = new String[BookmarksModel.numberOfListIdSaved];
                            } else {
                                arrayListId = new String[jsonArrayItems.length()];
                            }

                            for (int i = 0; i < jsonArrayItems.length(); i++) {
                                if (i < BookmarksModel.numberOfListIdSaved) {
                                    JSONObject adsObj = jsonArrayItems.optJSONObject(i);
                                    String list_id = adsObj.optString("list_id");
                                    arrayListId[i] = list_id;
                                    isAdsAvailable = true;
                                } else {
                                    break;
                                }
                            }
                            //This case is use when users save search from Filter page and got redirect here
                            //because we need the list ID
                            if (isAdsAvailable && bookmarkId >= 0 && !isBookmarkedLatestListIdUpdated) {
                                isBookmarkedLatestListIdUpdated = true;
                                BookmarkUtil bookmarkUtil = new BookmarkUtil(AdsListActivity.this, arrayListId);
                                bookmarkUtil.createBookmarkNotification(bookmarkId);
                            }

                        }
                    }
                } catch (JSONException e) {
                    onLoadError(loader, data);
                }
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                fLoading.setConnectionLostShown(true);
                searchTotal = 0;
                tagConnectionLost(AdsFragment.LISTING_CONNECTION_LOST, data);
            }
        });
        adsFragment.setCustomTargetingParams(paramsLabel);
        adsFragment.setFilterParamForTagging(filterParams, filterLabelParams);
        adsFragment.setApi(Method.GET, "list", params);
        adsFragment.restartLoader();
    }

    @Override
    public void sendFalseCrashLog(String message) {
        ACUtils.logCrashlytics(message);
        try {
            throw new RuntimeException(message);
        } catch (RuntimeException ignore) {
            ACUtils.debug(ignore, false);
        }
    }

    private void handleErrorResponse(JSONObject error) {
        try {
            JSONArray parameters = error.optJSONArray(Constants.PARAMETERS);
            if (parameters != null && parameters.length() > 0) {
                JSONObject errorMsg = (JSONObject) parameters.get(0);
                if (errorMsg != null && INVALID_QUERY.equalsIgnoreCase(errorMsg.optString(Constants.MESSAGE))) {
                    Toast.makeText(context, getString(R.string.ads_list_toast_invalid_query), Toast.LENGTH_LONG).show();
                }
            }
        } catch (JSONException ignore) {
        }
    }

    private void updateQueryFields() {
        ACSettings set = ACSettings.getACSettings();

        categoryName = getString(R.string.ads_search_category_all);
        regionName = Constants.MALAYSIA;
        subregionName = Constants.EMPTY_STRING;

        if (searchParams != null && searchParams.containsKey(Constants.CATEGORY)) {
            String categoryId = searchParams.get(Constants.CATEGORY);
            categoryName = set.getCategoryName(categoryId);
            //parent category
            if (ACUtils.isEmpty(categoryName)) {
                categoryName = set.getCategoryGroupName(categoryId);
            }
        }
        // failed over in case no category name found (should not happen)
        if (ACUtils.isEmpty(categoryName)) {
            categoryName = OTHERS_NAME;
        }

        adsFragment.setCategoryText(categoryName);

        if (searchParams != null && searchParams.containsKey(Constants.REGION)) {
            regionName = set.getRegionName(searchParams.get(Constants.REGION));
            String subAreaValue = searchParams.get(Constants.SUBAREA);
            if (!ACUtils.isEmpty(subAreaValue)) {
                subregionName = set.getMunicipalityName(subAreaValue);
            }
        }

        if (!ACUtils.isEmpty(subregionName)) {
            adsFragment.setLocationText(subregionName);
        } else {
            adsFragment.setLocationText(regionName);
        }

        //Regdate and Transmission 'both' is removed on API side
        if (filterParams != null && filterParams.containsKey(Constants.REG_DATE)) {
            filterParams.remove(Constants.REG_DATE);
        }

        if (filterParams != null && (Constants.TRANSMISSION_BOTH_TYPE).equalsIgnoreCase(filterParams.get(Constants.TRANSMISSION))) {
            filterParams.remove(Constants.TRANSMISSION);
        }

        //update Hash Map for Latest Search
        HashMap<String, String> savedMap = MudahUtil.saveLastSearch(getBaseContext(), searchParams, filterParams, categoryName, filterLabelParams);
        globalQuery = savedMap.get(SEARCH_PARAMS);
        globalFilter = savedMap.get(FILTER_PARAMS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ads_list, menu);

        actionBarHelper.configureActionbarForSearch(false);

        MenuItem itemSearch = menu.findItem(R.id.menu_search);
        itemSearch.setVisible(setMenuItemSearchVisible);

        if (Config.enableChat) {
            final View badge = menu.findItem(R.id.badge).getActionView();
            badgeTotalUnreadChatMsg = (TextView) badge.findViewById(R.id.badgeUnreadChat);
            if (!Config.userAccount.isLogin()) {
                badgeTotalUnreadChatMsg.setVisibility(View.GONE);
            } else if (getUnreadNumber() > 0) {
                badgeTotalUnreadChatMsg.setVisibility(View.VISIBLE);
                threadRunBadge(getUnreadNumber());
            }
            badge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = ChatCafe.redirectIntentToInBox(AdsListActivity.this);
                    startActivity(intent);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_ICON_ACTION_BAR, XitiUtils.NAVIGATION);
                }
            });
        }

        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (adsFragment != null) {
            adsFragment.isClickSkipTutorial(ev.getRawY());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void hideSkipTutorialOption() {
        if (adsFragment != null) {
            adsFragment.hideSkipTutorialOption();
        }
    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
        } catch (Throwable e) {
            ACUtils.forceCrash(e);
        }
    }

    @Override
    protected void onStop() {
        try {
            super.onStop();
        } catch (Throwable e) {
            ACUtils.forceCrash(e);
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            if (Config.upgrade && Config.UPGRADE_PREFERENCES == 0) {
                fNotifications = (NotificationsFragment) getSupportFragmentManager().findFragmentById(R.id.f_notifications);
                if (fNotifications != null)
                    fNotifications.show();
            }

            // Hide the keyboard every time this window is on top(active)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } catch (Throwable e) {
            ACUtils.forceCrash(e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            Log.d();
            ACUtils.unbindDrawables(findViewById(R.id.drawer_layout));
            System.gc();
            //release fragment
            adsFragment = null;
            fLoading = null;
        } catch (Throwable e) {
            ACUtils.forceCrash(e);
        }
    }

    private HashMap<String, String> getSearchParams(String[] effectedParamsName, HashMap<String, String> effectedSearchParamsMap, String existingQueries) {

        HashMap<String, String> newSearchParams;

        if (!ACUtils.isEmpty(existingQueries)) {
            newSearchParams = MudahUtil.convertStringToHashMap(existingQueries);
        } else {
            newSearchParams = new HashMap<>();
        }

        // remove existing param before adding them
        if (effectedParamsName != null) {
            for (String param : effectedParamsName) {
                newSearchParams.remove(param);
            }
        }

        // add new param
        Iterator iterator = effectedSearchParamsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry keyValue = (Map.Entry) iterator.next();
            String key = keyValue.getKey().toString();
            if (keyValue.getValue() != null) {
                String value = keyValue.getValue().toString();
                if (ACUtils.isEmpty(value)) {
                    newSearchParams.remove(key);
                } else {
                    newSearchParams.put(key, value);
                }
            }
        }

        // force include search title only
        newSearchParams.put(Constants.TITLE_ONLY, Constants.TITLE_ONLY_ENABLED);

        return newSearchParams;
    }

    private void executeSearchByRequest(int requestCode, Intent intent) {
        Log.d("requestCode: " + requestCode);
        boolean isSaveSearchFromFilter = false;
        if (!initLoadingData()) {
            return;
        }
        if (fLoading != null) {
            fLoading.hide();
        }
        getQuery(false);

        switch (requestCode) {
            case REQUEST_CATEGORY_LOCATION:
                loadNewCategory(globalQuery);
                String query = MudahUtil.getSearchUri(searchParams).getEncodedQuery();
                loadNewLocationFromReferences(query);
                loadSearch();
                break;

            case REQUEST_CATEGORY:
                loadNewCategory(globalQuery);
                loadSearch();
                break;

            case REQUEST_LOCATION:
                loadNewLocationFromIntent(intent, globalQuery);
                loadSearch();
                break;

            case REQUEST_FROM_FILTER:
                loadFilterFromIntent(intent);
                loadSearch();
                formatActionBarWithKeyword(extractParams(Constants.KEYWORD));
                break;
            case REQUEST_SAVE_BOOKMARKS:
                isSaveSearchFromFilter = true;
            case REQUEST_BOOKMARKS:
                loadBookmarkFromIntent(intent);
                loadSearch();
                formatActionBarWithKeyword(extractParams(Constants.KEYWORD));
                if (isSaveSearchFromFilter) {
                    sendTagSavedSearch();
                }
                break;
        }
    }

    private void loadFilterFromIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            if (bundle.containsKey(FilterMenuActivity.REQUEST_FILTER_QUERY)) {
                String queryFromFilter = bundle.getString(FilterMenuActivity.REQUEST_FILTER_QUERY);
                searchParams = new HashMap<>();
                if (!ACUtils.isEmpty(queryFromFilter)) {
                    searchParams = MudahUtil.convertStringToHashMap(queryFromFilter);
                }
            }

            globalFilter = Constants.EMPTY_STRING;
            if (bundle.containsKey(FilterMenuActivity.REQUEST_FILTER_FILTER)) {
                globalFilter = bundle.getString(FilterMenuActivity.REQUEST_FILTER_FILTER);
            }

            filterLabelParams = new HashMap<>();
            if (bundle.containsKey(FilterMenuActivity.REQUEST_FILTER_LABEL_FILTER)) {
                filterLabelParams = (HashMap<String, String>) bundle.getSerializable(FilterMenuActivity.REQUEST_FILTER_LABEL_FILTER);
            }
        }
    }

    private void loadBookmarkFromIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            String requestQuery = Constants.EMPTY_STRING;
            globalFilter = Constants.EMPTY_STRING;
            if (bundle.containsKey(FilterMenuActivity.REQUEST_FILTER_QUERY)) {
                requestQuery = bundle.getString(FilterMenuActivity.REQUEST_FILTER_QUERY);
                globalFilter = bundle.getString(FilterMenuActivity.REQUEST_FILTER_FILTER);
            } else if (bundle.containsKey(ListBookmarksFragment.REQUEST_BOOKMARKS_QUERY)) {
                requestQuery = bundle.getString(ListBookmarksFragment.REQUEST_BOOKMARKS_QUERY);
                globalFilter = bundle.getString(ListBookmarksFragment.REQUEST_BOOKMARKS_FILTER);
            }
            if (!ACUtils.isEmpty(requestQuery))
                searchParams = MudahUtil.convertStringToHashMap(requestQuery);
            else
                searchParams = new HashMap<>();

            // Check for attached bookmark ID
            if (bundle.containsKey(BookmarksDAO.BOOKMARK_ID)) {
                String strBookmarkId = bundle.getString(BookmarksDAO.BOOKMARK_ID);
                try {
                    bookmarkId = Long.parseLong(strBookmarkId);
                } catch (NumberFormatException e) {
                    bookmarkId = -1;
                    ACUtils.debug(e, "AdsList_onNewIntent", MudahUtil.bundleToString(bundle), false);
                }
                Log.d("Selected bookmark with bookmarkedId:" + bookmarkId + " filter:" + globalFilter);
                isBookmarkedLatestListIdUpdated = false;
            }
        }
    }

    private void loadNewLocationFromIntent(Intent intent, String query) {
        String[] effectedParamsName = LocationActivity.DECLARED_PARAMS;
        HashMap<String, String> effectedSearchParamsMap = new HashMap<>();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            effectedSearchParamsMap = (HashMap<String, String>) bundle.getSerializable(LocationActivity.REQUEST_LOCATION_QUERY);
        }

        searchParams = getSearchParams(effectedParamsName, effectedSearchParamsMap, query);
    }

    private void loadNewLocationFromReferences(String query) {
        String[] effectedParamsName = LocationActivity.DECLARED_PARAMS;
        HashMap<String, String> effectedSearchParamsMap = new HashMap<>();
        ACReferences ref = ACReferences.getACReferences();
        if (!ACUtils.isEmpty(ref.getRegionId())) {
            if (Constants.ALL_REGION.equals(ref.getRegionId())) {
                searchParams.put(Constants.REGION, null);
                searchParams.put(Constants.AREA, null);
                searchParams.put(Constants.SUBAREA, null);
            } else {
                effectedSearchParamsMap.put(Constants.REGION, ref.getRegionId());
                searchParams.put(Constants.AREA, null);
                if (!ACUtils.isEmpty(ref.getMunicipalityId())) {
                    searchParams.put(Constants.SUBAREA, ref.getMunicipalityId());
                    effectedSearchParamsMap.put(Constants.SUBAREA, ref.getMunicipalityId());
                } else {
                    searchParams.put(Constants.SUBAREA, null);
                }
            }
        }

        searchParams = getSearchParams(effectedParamsName, effectedSearchParamsMap, query);
    }

    private void loadNewCategory(String query) {
        ACReferences ref = ACReferences.getACReferences();
        String cg = (ref.categoryId != null) ? ref.categoryId : ref.categoryGroupId;

        String[] effectedParamsName = new String[]{Constants.CATEGORY, Constants.TYPE};
        HashMap<String, String> effectedSearchParamsMap = new HashMap<>();

        if (!ACUtils.isEmpty(cg)) {
            effectedSearchParamsMap.put(Constants.CATEGORY, cg);
        }

        //clear param label
        filterLabelParams = new HashMap<>();

        searchParams = getSearchParams(effectedParamsName, effectedSearchParamsMap, query);

        if (!ACUtils.isEmpty(ref.extraQuery)) {
            if (ref.extraQuery.contains(Constants.TYPE)) {
                searchParams.put(Constants.TYPE, MudahUtil.convertStringToHashMap(ref.extraQuery).get(Constants.TYPE));
            }
            globalFilter = ref.extraQuery;
        } else {
            globalFilter = Constants.EMPTY_STRING;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        executeSearchByRequest(requestCode, data);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                EventTrackingUtils.sendClickByCategoryId(null, xitiCategory, XitiUtils.LISTING, "ActionBar_Listing_to_ShowFilter", XitiUtils.NAVIGATION);
                search.bringToFront();
                openSearch();
                return true;
            case R.id.menu_save_bookmarks:
                checkAndSaveBookmark();
                return true;
            case android.R.id.home:
                if (!actionBarHelper.isLogoEnable()) {
                    Log.d("close keyword panel");
                    actionBarClickListener.onMenuClickListener(true);
                } else {
                    // To make sure that AdsListActivity is always the last view that user see before exiting app
                    Intent intent = new Intent(this, HomepageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();//to prevent looping and users cannot exit the app
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkAndSaveBookmark() {
        SaveBookmarkAyncTask saveBookmarkAyncTask = new SaveBookmarkAyncTask(this);
        saveBookmarkAyncTask.execute();
    }

    public void sendTagSavedSearch() {
        Log.d();
        // Add_Favourite_Search::Private_Seller::Cars::Kedah
        String level2Bookmark = XitiUtils.getLevel2Map(XitiUtils.LEVEL2_BOOKMARK);
        String pageName = "Add_Favourite_Search";
        EventTrackingUtils.sendClick(level2Bookmark, pageName + XitiUtils.CHAPTER_SIGN + TealiumHelper.getSellerTypeText(MudahUtil.getSellerType(searchParams)) + XitiUtils.CHAPTER_SIGN + categoryName + XitiUtils.CHAPTER_SIGN + regionName, XitiUtils.NAVIGATION);
        if (adsFragment != null) {
            adsFragment.tagTealium(pageName, level2Bookmark, 0);
            adsFragment.tagXitiWithPageNameAndLevel2(pageName, level2Bookmark);
        }
        tagSearchOrSave(KahunaHelper.SAVED_SEARCH_EVENT, null);
    }

    public void xitiTag() {
        if (isCreateInactiveUserReminderDone) {
            EventTrackingUtils.sendCampaign(this, XitiUtils.CAMPAIGN_NOTIFICATION_REACTIVATE, XitiUtils.CAMPAIGN_CLICK);
            isCreateInactiveUserReminderDone = false;//reset it to false so that we don't double tag
        }

        if (isFromPushNotification) {
            EventTrackingUtils.sendCampaign(this, XitiUtils.CAMPAIGN_NOTIFICATION_GCM_PUSH, XitiUtils.CAMPAIGN_CLICK);
            isFromPushNotification = false;//reset it to false so that we don't double tag
        }
    }

    @Override
    public void onBackPressed() {
        if (search != null && search.isOpen()) {
            search.performBackClick();
        } else {
            super.onBackPressed();
        }
    }

    private static class SaveBookmarkAyncTask extends AsyncTask<Void, Void, Void> {
        boolean isLessThanMax = false;
        private WeakReference<AdsListActivity> activityWeakReference;

        private SaveBookmarkAyncTask(AdsListActivity adsListActivity) {
            this.activityWeakReference = new WeakReference<>(adsListActivity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (activityWeakReference == null || activityWeakReference.get() == null)
                return null;
            AdsListActivity adsListActivity = activityWeakReference.get();
            isLessThanMax = (adsListActivity.bookmarksDAO.total() < Config.maxBookmarksTotal);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (activityWeakReference == null || activityWeakReference.get() == null)
                return;

            AdsListActivity adsListActivity = activityWeakReference.get();

            if (isLessThanMax) {
                adsListActivity.saveBookmark();
            } else {
                MudahUtil.showExceedMaxBookmarkResult(adsListActivity);
            }
        }
    }

    static class MyHandler extends Handler {
        private final WeakReference<AdsListActivity> mTarget;

        public MyHandler(AdsListActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AdsListActivity target = mTarget.get();
            if (target == null) {
                return;
            }
            if (msg.arg1 == ACReferences.RESULT_ERROR && !target.isFinishing()) {
                if (target.fLoading != null)
                    target.fLoading.setConnectionLostShown(true);
                else
                    return;//do nothing;
            } else if (!target.isFinishing()) {
                target.updateLoading();
            }
        }
    }
}