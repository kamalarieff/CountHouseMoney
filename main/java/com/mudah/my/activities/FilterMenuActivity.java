package com.mudah.my.activities;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lib701.datasets.ACCategoriesDataSet;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACCategoryGroup;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.datasets.AdsType;
import com.lib701.utils.ACUtils;
import com.lib701.utils.DialogUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.adapters.CategoryParamsAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.dao.BookmarksDAO;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.BookmarkUtil;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by moehninhtwee on 26/12/14.
 */

public class FilterMenuActivity extends MudahBaseActivity {

    public static final int POST_BY_BUTTON = 3;
    public static final int SORT_BY_BUTTON = 4;
    public static final int TYPE_BUTTON = 5;

    public static final String REQUEST_FILTER_QUERY = "filter_query";
    public static final String REQUEST_FILTER_FILTER = "filter_filter";
    public static final String REQUEST_FILTER_LABEL_FILTER = "filter_filter_label";

    public static final String DFP_PARAMS = "dfp_banner";
    public static final String CALLER_NAME = "filter";
    private static final int LOADER_FILTER_PARAMS = 1;
    private BookmarksDAO bookmarksDAO;
    private HashMap<String, String> searchParams = new HashMap<>();
    private HashMap<String, String> filterParams = new HashMap<>();
    private HashMap<String, String> filterLabelParams = new HashMap<>();
    private int searchTotal = Integer.MIN_VALUE;
    private int xitiCategory;
    private final View.OnTouchListener filterOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.s_post_by:
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        xitiCategory = searchParams.get(Constants.CATEGORY) != null ? Integer.parseInt(searchParams.get(Constants.CATEGORY)) : 0;
                        XitiUtils.sendClickByCategoryId(xitiCategory, XitiUtils.LISTING, "Listing_filterPostedby", XitiUtils.NAVIGATION);
                    }
                    break;
                case R.id.s_sort_by:
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        xitiCategory = searchParams.get(Constants.CATEGORY) != null ? Integer.parseInt(searchParams.get(Constants.CATEGORY)) : 0;
                        XitiUtils.sendClickByCategoryId(xitiCategory, XitiUtils.LISTING, "Listing_filterSortby", XitiUtils.NAVIGATION);
                    }
                    break;
                case R.id.s_type:
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        xitiCategory = searchParams.get(Constants.CATEGORY) != null ? Integer.parseInt(searchParams.get(Constants.CATEGORY)) : 0;
                        XitiUtils.sendClickByCategoryId(xitiCategory, XitiUtils.LISTING, "Listing_filterType", XitiUtils.NAVIGATION);
                    }
                    break;
                case R.id.scroll_view:
                    MudahUtil.hideSoftKeyboard(FilterMenuActivity.this);
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    /* filter params */
    private CategoryParamsAdapter categoryParamsAdapter;
    private RelativeLayout menuType;
    private TextView typeSelected;
    private RelativeLayout menuPostBy;
    private TextView postbySelected;
    private RelativeLayout menuSortBy;
    private TextView sortbySelected;
    private LinearLayout llFilterInfo;
    private View vFilterInfoSeparator;
    private boolean hasFilter = false;
    private TextView ibSearch;
    private Spinner sPostBy;
    private Spinner sSortBy;
    private Spinner sType;
    private String categoryName;
    private String keyword;
    private String regionName;
    private String subregionName;
    private CheckBox cbSearchTitleOnly;
    private ScrollView scrollView;
    private boolean redirectToBookmarkPage = false;
    private String filter = Constants.EMPTY_STRING;
    private final View.OnClickListener filterOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            xitiCategory = searchParams.get(Constants.CATEGORY) != null ? Integer.parseInt(searchParams.get(Constants.CATEGORY)) : 0;
            switch (v.getId()) {
                case R.id.menu_post_by:
                    if (sPostBy != null && sPostBy.getAdapter() != null)
                        sPostBy.performClick();
                    EventTrackingUtils.sendClickByCategoryId(null, xitiCategory, XitiUtils.LISTING, "Listing_filterPostedby", XitiUtils.NAVIGATION);
                    break;
                case R.id.menu_sort_by:
                    if (sSortBy != null && sSortBy.getAdapter() != null)
                        sSortBy.performClick();
                    EventTrackingUtils.sendClickByCategoryId(null, xitiCategory, XitiUtils.LISTING, "Listing_filterSortby", XitiUtils.NAVIGATION);
                    break;
                case R.id.menu_type:
                    if (sType != null && sType.getAdapter() != null) {
                        sType.performClick();
                    }
                    EventTrackingUtils.sendClickByCategoryId(null, xitiCategory, XitiUtils.LISTING, "Listing_filterType", XitiUtils.NAVIGATION);
                    break;
                case R.id.ll_bookmark_save:
                    checkAndSaveBookmark();
                    break;
                case R.id.btn_search:
                    try {
                        directToListingPage();
                        EventTrackingUtils.sendClickByCategoryId(null, xitiCategory, XitiUtils.LISTING, "Listing_searchNow", XitiUtils.NAVIGATION);
                    } catch (Throwable e) {
                        ACUtils.debug(e, "FilterMenu_search_catId", Constants.EMPTY_STRING + xitiCategory);
                    }
                    break;
                case R.id.cb_search_title_only:
                    boolean isChecked = cbSearchTitleOnly.isChecked();
                    if (isChecked) {
                        searchParams.put(Constants.TITLE_ONLY, Constants.TITLE_ONLY_ENABLED);
                    } else {
                        searchParams.put(Constants.TITLE_ONLY, Constants.TITLE_ONLY_DISABLED);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void checkAndSaveBookmark() {
        CheckAndSaveBookmarkAysncTask checkAndSaveBookmarkAysncTask = new CheckAndSaveBookmarkAysncTask(this);
        checkAndSaveBookmarkAysncTask.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.init();
        setContentView(R.layout.search_menu);
        regionName = getString(R.string.ads_search_region_all);
        bookmarksDAO = new BookmarksDAO(this);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getResources().getString(R.string.filter_page_title));

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (savedInstanceState != null) {
            searchParams = (HashMap<String, String>) savedInstanceState.getSerializable(AdsListActivity.SEARCH_PARAMS);
            filterParams = (HashMap<String, String>) savedInstanceState.getSerializable(AdsListActivity.FILTER_PARAMS);
            filterLabelParams = (HashMap<String, String>) savedInstanceState.getSerializable(AdsListActivity.FILTER_LABEL_PARAMS);
        } else if (bundle != null) {
            //prevent loader from restarting while device is rotated
            if (savedInstanceState == null && searchTotal == Integer.MIN_VALUE) {
                if (bundle.containsKey("add_new")) {
                    redirectToBookmarkPage = true;
                }
            }

            if (bundle.containsKey(AdsListActivity.SEARCH_PARAMS)) {
                searchParams = (HashMap<String, String>) bundle.getSerializable(AdsListActivity.SEARCH_PARAMS);
            }
            if (bundle.containsKey(AdsListActivity.FILTER_PARAMS)) {
                filterParams = (HashMap<String, String>) bundle.getSerializable(AdsListActivity.FILTER_PARAMS);
            }

            //get keyword for bookmark auto-generated title
            if (bundle.containsKey(BookmarkUtil.SEARCH_QUERY))
                keyword = (String) bundle.get(BookmarkUtil.SEARCH_QUERY);
            if (bundle.containsKey(BookmarkUtil.REGION))
                regionName = (String) bundle.get(BookmarkUtil.REGION);
            if (bundle.containsKey(BookmarkUtil.SUBREGION))
                subregionName = (String) bundle.get(BookmarkUtil.SUBREGION);

        }
        scrollView = (ScrollView) this.findViewById(R.id.scroll_view);
        scrollView.setOnTouchListener(filterOnTouchListener);

        /* filter params */
        categoryParamsAdapter = new CategoryParamsAdapter(CALLER_NAME);
        categoryParamsAdapter.setOnCallerActionListener(new CategoryParamsListener());
        categoryParamsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                redrawCategoryParams();
            }
        });

        ibSearch = (TextView) findViewById(R.id.btn_search);
        ibSearch.setOnClickListener(filterOnClickListener);


        menuType = (RelativeLayout) findViewById(R.id.menu_type);
        menuType.setOnClickListener(filterOnClickListener);
        typeSelected = (TextView) findViewById(R.id.tv_type_selected);

        menuPostBy = (RelativeLayout) findViewById(R.id.menu_post_by);
        menuPostBy.setOnClickListener(filterOnClickListener);
        postbySelected = (TextView) findViewById(R.id.tv_postby_selected);

        menuSortBy = (RelativeLayout) findViewById(R.id.menu_sort_by);
        menuSortBy.setOnClickListener(filterOnClickListener);
        sortbySelected = (TextView) findViewById(R.id.tv_sortby_selected);

        sPostBy = (Spinner) findViewById(R.id.s_post_by);
        sPostBy.setOnTouchListener(filterOnTouchListener);

        sSortBy = (Spinner) findViewById(R.id.s_sort_by);
        sSortBy.setOnTouchListener(filterOnTouchListener);

        sType = (Spinner) findViewById(R.id.s_type);
        sType.setOnTouchListener(filterOnTouchListener);

        llFilterInfo = (LinearLayout) findViewById(R.id.ll_filter_info);
        vFilterInfoSeparator = (View) findViewById(R.id.v_filter_info_separator);

        LinearLayout llBookmarkSearch = (LinearLayout) findViewById(R.id.ll_bookmark_save);
        llBookmarkSearch.setOnClickListener(filterOnClickListener);

        cbSearchTitleOnly = (CheckBox) findViewById(R.id.cb_search_title_only);
        cbSearchTitleOnly.setOnClickListener(filterOnClickListener);

        //default view
        updateMenuVisibility(TYPE_BUTTON, View.GONE);

        loadLastSearch();
    }

    private void loadLastSearch() {
        boolean updateFilterParams = false;
        ACReferences ref = ACReferences.getACReferences();

        if (!searchParams.isEmpty()) {
            hasFilter = ref.hasFilter;
            updateFilterParams = !MudahUtil.isEmptyOrAllAdType(searchParams);
        }
        updatePostBy();
        updateTypeFilter();//After setting type, the "sort by" option will be triggered.
        updateQueryFields();
        updateSearchTitleOnlyFilter();

        Log.d("hasfilter=" + hasFilter + ", updateFilter=" + updateFilterParams);

        if (hasFilter && updateFilterParams) {
            updateFilterParams();
        }
    }

    private void updateSearchTitleOnlyFilter() {
        if (searchParams.get(Constants.TITLE_ONLY) == null || (Constants.TITLE_ONLY_ENABLED).equals(searchParams.get(Constants.TITLE_ONLY))) {
            cbSearchTitleOnly.setChecked(true);
            //if null, default to enable
            if (searchParams.get(Constants.TITLE_ONLY) == null) {
                searchParams.put(Constants.TITLE_ONLY, Constants.TITLE_ONLY_ENABLED);
            }
        } else {
            cbSearchTitleOnly.setChecked(false);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d();
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(AdsListActivity.SEARCH_PARAMS, searchParams);
        savedInstanceState.putSerializable(AdsListActivity.FILTER_PARAMS, filterParams);
        savedInstanceState.putSerializable(AdsListActivity.FILTER_LABEL_PARAMS, filterLabelParams);
    }

    private void updateSortBy() {
        ArrayAdapter<Item<Integer>> sortByAdapter = new ArrayAdapter<>(FilterMenuActivity.this, R.layout.simple_spinner_item, android.R.id.text1);
        sortByAdapter.add(new Item<>(getString(R.string.ads_search_sort_by_date), Constants.SORT_BY_DATE));
        String type = searchParams.get(Constants.TYPE);
        if (!(Constants.TYPE_WANTED).equalsIgnoreCase(type) && !(Constants.TYPE_WANTED_TO_RENT).equalsIgnoreCase(type)) {
            sortByAdapter.add(new Item<>(getString(R.string.ads_search_sort_by_price), Constants.SORT_BY_PRICE));
        } else {//set default to sort by date
            searchParams.remove(Constants.SORT_BY);
        }
        sortByAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        sSortBy.setAdapter(sortByAdapter);
        if (searchParams.get(Constants.SORT_BY) != null) {
            for (int i = 0; i < sortByAdapter.getCount(); i++) {
                if (searchParams.get(Constants.SORT_BY).equals(sortByAdapter.getItem(i).value.toString())) {
                    sSortBy.setSelection(i);
                    setMenuLabel(SORT_BY_BUTTON, sortByAdapter.getItem(i).name);
                    break;
                }
            }
        } else {//set default to '0' for the first time user
            setMenuLabel(SORT_BY_BUTTON, getString(R.string.ads_search_sort_by_date));
        }
        sSortBy.setOnItemSelectedListener(new IgnoreFirstSelectedListener(Constants.SORT_BY, SORT_BY_BUTTON));

        ACReferences ref = ACReferences.getACReferences();
        final String categoryId = ref.categoryId;
        updateMenuVisibility(SORT_BY_BUTTON, View.VISIBLE);
        if ((Constants.CAT_ID_NOT_SORTED).equals(categoryId)) {
            updateMenuVisibility(SORT_BY_BUTTON, View.GONE);
        }
    }

    private void updatePostBy() {
        ArrayAdapter<Item<String>> postByAdapter = new ArrayAdapter<Item<String>>(FilterMenuActivity.this, R.layout.simple_spinner_item, android.R.id.text1);
        postByAdapter.add(new Item<String>(getString(R.string.ads_search_post_by_anyone), null));
        postByAdapter.add(new Item<>(getString(R.string.ads_search_post_by_private), Constants.POSTED_BY_PRIVATE));
        postByAdapter.add(new Item<>(getString(R.string.ads_search_post_by_company), Constants.POSTED_BY_COMPANY));
        postByAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        sPostBy.setAdapter(postByAdapter);
        if (searchParams.get(Constants.POSTED_BY) != null) {
            for (int i = 0; i < postByAdapter.getCount(); i++) {
                if (searchParams.get(Constants.POSTED_BY).equals(postByAdapter.getItem(i).value)) {
                    sPostBy.setSelection(i);
                    setMenuLabel(POST_BY_BUTTON, postByAdapter.getItem(i).name);
                    break;
                }
            }
        } else {//set default to '0' for the first time user
            setMenuLabel(POST_BY_BUTTON, getString(R.string.ads_search_post_by_anyone));
        }
        sPostBy.setOnItemSelectedListener(new IgnoreFirstSelectedListener(Constants.POSTED_BY, POST_BY_BUTTON));

        ACReferences ref = ACReferences.getACReferences();
        final String categoryId = ref.categoryId;
        updateMenuVisibility(POST_BY_BUTTON, View.VISIBLE);
        if ((Constants.CAT_ID_NOT_POSTED).equals(categoryId)) {
            updateMenuVisibility(POST_BY_BUTTON, View.GONE);
        }
    }

    private void updateFilterParams() {
        /**
         * 1. Hide previous filter params
         * 2. Send request to API for new filtering params if ref.filterParams is null(category is changed)
         * 3. Display the filter params
         */
        clearFilter();
        ACReferences ref = ACReferences.getACReferences();

        String categoryId = "0";
        if (!ACUtils.isEmpty(ref.categoryId))
            categoryId = ref.categoryId;
        else if (!ACUtils.isEmpty(ref.categoryGroupId))
            categoryId = ref.categoryGroupId;

        if (categoryId != null && ref.filterParams == null) {
            getSupportLoaderManager().restartLoader(LOADER_FILTER_PARAMS, null, newFilterParamsCallback(categoryId));
        } else if (ref.filterParams != null) {
            displayFilterParams(ref.filterParams);
        }
    }

    private void displayFilterParams(JSONObject data) {
        categoryParamsAdapter.setTypes(data);

        Map<String, String> state = new HashMap<>(filterParams);
        state.put("type", searchParams.get(Constants.TYPE));
        categoryParamsAdapter.setState(state);

        redrawCategoryParams();
    }

    private void clearFilter() {
        if (llFilterInfo != null) {
            llFilterInfo.removeAllViews();
            vFilterInfoSeparator.setVisibility(View.GONE);
        }
        categoryParamsAdapter.clearTypes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void redrawCategoryParams() {
        int viewCount = 0;
        int numView = 0;
        llFilterInfo.removeAllViews();
        if (categoryParamsAdapter != null)
            numView = categoryParamsAdapter.getCount();
        Log.d("numView=" + numView);
        List<View> viewList;
        LayoutInflater inflater;
        View paramSeparator;
        for (int i = 0; i < numView; i++) {
            try {
                viewList = categoryParamsAdapter.getViewList(i, llFilterInfo);
                // if the view is null, do not create view
                if (viewList != null) {
                    for (View view : viewList) {
                        view.setSaveEnabled(false);
                        llFilterInfo.addView(view);
                        viewCount++;
                        if (i < numView - 1) {
                            inflater = LayoutInflater.from(this);
                            paramSeparator = (View) inflater.inflate(R.layout.filter_param_separator, null, false);
                            llFilterInfo.addView(paramSeparator);
                        }
                    }
                }
            } catch (RuntimeException e) {
                ACUtils.debug(e);
            }
        }

        if (viewCount > 0) {
            llFilterInfo.setVisibility(View.VISIBLE);
            vFilterInfoSeparator.setVisibility(View.VISIBLE);
        } else {
            llFilterInfo.setVisibility(View.GONE);
            vFilterInfoSeparator.setVisibility(View.GONE);
        }
    }

    private BlocketLoader.Callbacks newFilterParamsCallback(
            final String categoryId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(Constants.CATEGORY, categoryId);
        return new BlocketLoader.Callbacks(Method.GET, "filter",
                params, FilterMenuActivity.this) {
            @Override
            public void onLoadFinished(Loader<JSONObject> loader,
                                       JSONObject data) {
                super.onLoadFinished(loader, data);
            }

            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                try {
                    if (!data.has("error")) {
                        ACReferences ref = ACReferences.getACReferences();
                        ref.filterParams = data;
                        displayFilterParams(data);
                    }
                } catch (Exception e) {//JSONException
                    Log.e(e);
                    onLoadError(loader, data);
                }
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                Log.d();
                DialogUtils.showGeneralErrorAlert(FilterMenuActivity.this);
                getSupportLoaderManager().destroyLoader(LOADER_FILTER_PARAMS);
                resetCategory();
            }
        };
    }

    private void resetCategory() {
        Log.d();
        ACReferences ref = ACReferences.getACReferences();
        ref.filterParams = null;
        searchParams.remove(Constants.TYPE);
        filterParams.clear();    // Reset all category search filters
        filterLabelParams.clear();
    }

    private void updateTypeFilter() {
        updateMenuVisibility(TYPE_BUTTON, View.GONE);
        clearFilter();

        ACReferences ref = ACReferences.getACReferences();
        String categoryId = null;
        String categoryGroupId = null; //all categories as default
        if (!ACUtils.isEmpty(ref.categoryId)) {
            categoryId = ref.categoryId;
        } else if (!ACUtils.isEmpty(ref.categoryGroupId)) {
            categoryGroupId = ref.categoryGroupId;
        }
        Log.d("categoryId:" + categoryId + ", categoryGroupId:" + categoryGroupId);

        ACSettings acSettings = ACSettings.getACSettings();
        ACCategoriesDataSet acCategoriesDataSet = acSettings.getAcCategoriesForSearchDataSet();

        ACCategoryGroup catGrp = null;
        if (categoryGroupId == null) {
            int catGroupsSize = acCategoriesDataSet.getCategoryGroups().size() - 1;
            catGrp = acCategoriesDataSet.getCategoryGroups().get(catGroupsSize);
        } else {
            catGrp = acCategoriesDataSet.findCategoryGroupById(categoryGroupId);
        }

        ACCategory category = null;
        if (categoryId != null) {
            category = acCategoriesDataSet.findCategoryById(categoryId);
        }

        ArrayList<AdsType> adsTypes = null;
        if (category != null) {
            adsTypes = category.getAdsType();
        } else if (catGrp != null) {
            adsTypes = catGrp.getAdsType();
        }
        if (adsTypes != null && adsTypes.size() != 0) {
            ArrayAdapter<Item<String>> adapter = new ArrayAdapter<>(
                    FilterMenuActivity.this, R.layout.simple_spinner_item,
                    android.R.id.text1);
            adapter.add(new Item<String>(getString(R.string.ads_search_type_all), Constants.TYPE_ALL));

            int selectionIndex = 0;
            for (int i = 0; i < adsTypes.size(); i++) {
                AdsType adsType = adsTypes.get(i);
                String typeId = adsType.getId();
                adapter.add(new Item<>(adsType.getName(), typeId));
                if (searchParams.get(Constants.TYPE) != null && typeId.equals(searchParams.get(Constants.TYPE))) {
                    selectionIndex = i + 1; // 1st item in adapter is not 1st item in jsonTypes
                    setMenuLabel(TYPE_BUTTON, adsType.getName());
                }
            }
            //set default to '0' for the first time user
            if (selectionIndex == 0)
                setMenuLabel(TYPE_BUTTON, getString(R.string.ads_search_type_all));

            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            sType.setAdapter(adapter);
            sType.setSelection(selectionIndex);
            sType.setOnItemSelectedListener(new IgnoreFirstSelectedListener(Constants.TYPE, TYPE_BUTTON));
            updateMenuVisibility(TYPE_BUTTON, View.VISIBLE);
        }
    }

    private void updateQueryFields() {
        ACReferences ref = ACReferences.getACReferences();
        ACSettings set = ACSettings.getACSettings();

        if (!ACUtils.isEmpty(ref.categoryGroupId)) {
            if (!ACUtils.isEmpty(ref.categoryId)) {
                categoryName = set.getCategoryName(ref.categoryGroupId, ref.categoryId);
            } else {
                categoryName = set.getCategoryGroupName(ref.categoryGroupId);
            }
        } else {
            categoryName = getString(R.string.ads_search_category_all);
        }

        if ((XitiUtils.CAT_OTHERS).equals(categoryName))
            categoryName = XitiUtils.OTHERS_NAME;

    }

    public void saveBookmark() {
        String query = MudahUtil.getSearchUri(searchParams).getEncodedQuery();

        String filter = Constants.EMPTY_STRING;
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
        if (redirectToBookmarkPage) {
            values.put(BookmarkUtil.REDIRECT_TO_BOOKMARK, "true");
        }

        BookmarkUtil bookmarkUtil = new BookmarkUtil(this, values);
        bookmarkUtil.showSaveBookmarksDialog(false);

    }

    public void directToListingPage() {
        directToListingPage(AdsListActivity.REQUEST_FROM_FILTER, null, null);
    }

    public void directToListingPage(int functionRequest, String putExtraKey, String putExtraValue) {
        try {
            String query = MudahUtil.getSearchUri(searchParams).getEncodedQuery();
            filter = MudahUtil.getSearchUri(filterParams).getEncodedQuery();
            MudahUtil.hideSoftKeyboard(this);

            searchTotal = Integer.MIN_VALUE;

            Intent intent = new Intent(FilterMenuActivity.this, AdsListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(AdsListActivity.FUNCTION_REQUEST, functionRequest);
            //get the value of labels' keys for DFP banner
            if (Config.IS_GOOGLEAD_ENABLE) {
                HashMap<String, String> paramsLabel = getParamLabelForDFP();
                intent.putExtra(DFP_PARAMS, paramsLabel);
            }

            if (!ACUtils.isEmpty(filter)) {
                intent.putExtra(REQUEST_FILTER_FILTER, filter);
                intent.putExtra(REQUEST_FILTER_LABEL_FILTER, filterLabelParams);
            }
            //always send this value regardless of having value or not to make this 'FLAG_ACTIVITY_REORDER_TO_FRONT' work
            //AdsListActivity.onNewIntent will be called!
            intent.putExtra(REQUEST_FILTER_QUERY, query);
            if (!ACUtils.isEmpty(putExtraKey) && !ACUtils.isEmpty(putExtraValue)) {
                intent.putExtra(putExtraKey, putExtraValue);
            }

            startActivity(intent);
            finish();
        } catch (Throwable throwableErr) {
            ACUtils.debug(throwableErr, "FilterMenu_SaveWithData", "ExtraKey= " + putExtraKey + ", ExtraValue= " + putExtraValue);
        }
    }

    private HashMap<String, String> getParamLabelForDFP() {
        HashMap<String, String> paramsLabel = new HashMap<>();
        if (!MudahUtil.isEmptyOrAllAdType(searchParams)) {
            String genderTypeKey = null;
            String makeKey = null;
            String carTypeKey = null;

            if (searchParams.get("gender_type") != null) {
                genderTypeKey = searchParams.get("gender_type");
            }

            if (searchParams.get("make") != null) {
                makeKey = searchParams.get("make");
            }

            if (searchParams.get("car_type") != null) {
                carTypeKey = searchParams.get("car_type");
            }

            if (categoryParamsAdapter.getParam("gender_type") != null) {
                JSONArray genderTypeArray = categoryParamsAdapter.getParam("gender_type").optJSONArray("options");
                for (int i = 0; i < genderTypeArray.length(); i++) {
                    JSONObject genderObj = genderTypeArray.optJSONObject(i);
                    String genderType = genderObj.optString(genderTypeKey);
                    if (!ACUtils.isEmpty(genderType) && !genderType.equalsIgnoreCase("Unisex")) {
                        paramsLabel.put("gender", genderType);
                        break;
                    }

                }
            }

            if (categoryParamsAdapter.getParam("make") != null) {
                JSONArray makeArray = categoryParamsAdapter.getParam("make").optJSONArray("options");
                for (int i = 0; i < makeArray.length(); i++) {
                    JSONObject makeObj = makeArray.optJSONObject(i);
                    String makeName = makeObj.optString(makeKey);
                    if (makeName != null && makeName != Constants.EMPTY_STRING) {
                        paramsLabel.put("make", makeName);
                        break;
                    }
                }
            }

            if (categoryParamsAdapter.getParam("car_type") != null) {
                JSONArray carTypeArray = categoryParamsAdapter.getParam("car_type").optJSONArray("options");
                for (int i = 0; i < carTypeArray.length(); i++) {
                    JSONObject carTypeObj = carTypeArray.optJSONObject(i);
                    String carTypeName = carTypeObj.optString(carTypeKey);
                    if (carTypeName != null && carTypeName != Constants.EMPTY_STRING) {
                        paramsLabel.put("cartype", carTypeName);
                        break;
                    }
                }

            }
        }
        return paramsLabel;
    }

    public void updateMenuVisibility(int type, int visibility) {
        switch (type) {
            case TYPE_BUTTON:
                menuType.setVisibility(visibility);
                break;
            case SORT_BY_BUTTON:
                menuSortBy.setVisibility(visibility);
                break;
            case POST_BY_BUTTON:
                menuPostBy.setVisibility(visibility);
                break;
        }
    }

    public void setMenuLabel(int type, String label) {
        switch (type) {
            case SORT_BY_BUTTON:
                sortbySelected.setText(label);
                break;
            case POST_BY_BUTTON:
                postbySelected.setText(label);
                break;
            case TYPE_BUTTON:
                typeSelected.setText(label);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void sendXitiTaggingForSortByBtn(String name) {
        String[] sortLabel = name.split(" ");
        EventTrackingUtils.sendClickByCategoryId(null, xitiCategory, XitiUtils.LISTING, "Listing_filter" + sortLabel[0] + "Sortby", XitiUtils.NAVIGATION);
    }

    private static class CheckAndSaveBookmarkAysncTask extends AsyncTask<Void, Void, Void> {
        boolean isLessThanMax = false;
        private WeakReference<FilterMenuActivity> activityWeakReference;

        private CheckAndSaveBookmarkAysncTask(FilterMenuActivity filterMenuActivity) {
            this.activityWeakReference = new WeakReference<>(filterMenuActivity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (activityWeakReference == null || activityWeakReference.get() == null)
                return null;
            FilterMenuActivity filterMenuActivity = activityWeakReference.get();
            isLessThanMax = (filterMenuActivity.bookmarksDAO.total() < Config.maxBookmarksTotal);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (activityWeakReference == null || activityWeakReference.get() == null)
                return;

            FilterMenuActivity filterMenuActivity = activityWeakReference.get();

            if (isLessThanMax) {
                filterMenuActivity.saveBookmark();
                //tagging will be done after redirecting into AdsListActivity, REQUEST_SAVE_BOOKMARKS
            } else {
                MudahUtil.showExceedMaxBookmarkResult(filterMenuActivity);
            }
        }
    }

    private static class Item<T> {
        private final String name;
        private final T value;

        public Item(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class CategoryParamsListener implements CategoryParamsAdapter.OnCallerActionListener {

        public void onCallerAction(Map<String, String> state, Map<String, String> stateLabel) {
            state.remove("type");
            filterParams.clear();
            filterLabelParams.clear();
            filterParams.putAll(state);
            filterLabelParams.putAll(stateLabel);
        }

        @Override
        public void onRequireLoginAction() {
            //Do Nothing
        }
    }

    private class IgnoreFirstSelectedListener implements AdapterView.OnItemSelectedListener {
        private String key;
        private int type;

        public IgnoreFirstSelectedListener(String key, int type) {
            this.key = key;
            this.type = type;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Item<?> item = ((Item<?>) parent.getSelectedItem());
            String name = item.name;

            if (position == 0 && !Constants.TYPE.equals(key)) {
                searchParams.remove(key);

                if (type == SORT_BY_BUTTON) {
                    sendXitiTaggingForSortByBtn(name);
                }
            } else {
                String value = item.value.toString();
                if (!value.equals(searchParams.get(key))) {
                    if ((Constants.TYPE).equals(key)) {
                        filterParams.clear();    // Reset all category search filters
                        filterLabelParams.clear();
                    }
                    if (type == SORT_BY_BUTTON && searchParams.get(key) == null) {
                        sendXitiTaggingForSortByBtn(name);
                    }

                    searchParams.put(key, value);
                }
            }
            if ((Constants.TYPE).equals(key)) {
                updateSortBy();
                if (hasFilter) {
                    updateFilterParams();
                } else {
                    clearFilter();
                }
            }
            setMenuLabel(type, name);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

}
