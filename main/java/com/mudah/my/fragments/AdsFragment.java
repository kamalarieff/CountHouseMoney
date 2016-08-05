package com.mudah.my.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;
import com.lib701.datasets.ACAd;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdViewActivity;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.adapters.AdsRecyclerViewAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Config.ListViewMode;
import com.mudah.my.configs.Constants;
import com.mudah.my.configs.DfpConfig;
import com.mudah.my.dao.AdViewFavouritesDAO;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.ShowcaseHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.GravityUtils;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.viewholders.AdsListViewHolders;
import com.squareup.picasso.Picasso;
import com.tealium.library.Tealium;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AdsFragment extends PagingBlocketLoaderFragment implements AdsListViewHolders.OnItemClickListener, AdsRecyclerViewAdapter.OnFavItemClickListener {
    public static final String LISTING_CONNECTION_LOST = "Listing_Connection_Lost";
    private static final String LISTING_SKIP_TUTORIAL = "Listing_skip_tutorial";
    private static final String LISTING_FILTER = "Listing_filter";
    private static final String LISTING_FILTER_CATEGORY = "Listing_filterCategory";
    private static final String LISTING_INSERT_AD = "Listing_to_insertAd";
    private static final String PRICE_REGEX = "[^\\d.]";
    private static final int ADS_COUNT_PER_XITI_PAGE = 10;
    private static final int MAX_PREV_SCROLL_REQUIREMENT = 20;//max scroll to previous ad, to keep the list id list short
    private static final String RESULT_PAGE = "Result_page";
    private static int currentTutorialStep;
    private int resultTotal = 0;
    private AdsRecyclerViewAdapter recyclerViewAdapter;
    private LinearLayoutManager linearLayoutManagerVertical;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private Queue<Pair<Integer, Map<String, String>>> xitiBacklog = new LinkedList<Pair<Integer, Map<String, String>>>();
    private int seenItemCount = 0;
    private View actionBarFloating;
    private RelativeLayout leftSegment;
    private RelativeLayout rlMidSegment;
    private RelativeLayout rlRightSegment;
    private TextView tvLocationSelected;
    private TextView tvCategorySelected;
    private FloatingActionBarListener listener;
    private HashMap<String, Object> searchParam = new HashMap<>();
    private HashMap<String, String> filterLabelParamsForTagging = new HashMap<>();
    private HashMap<String, String> filterParamForTagging = new HashMap<>();
    private HashMap<String, String> customTargetingParams = new HashMap<>();
    private int category;
    private ShowcaseHelper showcaseHelper;
    private MenuItem miSwitchListMode;
    private Map<String, String> dataTealium = new HashMap<>();
    private AdViewFavouritesDAO adViewFavouritesDAO;
    private Map<String, String> xitiMapX = new HashMap<String, String>();
    private RecyclerView recyclerView;
    private int lastVisibleItem;
    private View footerLoading;
    private AdViewAd ad;
    private boolean isFirstPageTag;
    private TextView skipTutorial;
    private int[] skipTutorialArea = new int[2];
    private int currentFavouriteAdSize;

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int distanceY) {
            if (distanceY >= 0) { //check for scroll down
                lastVisibleItem = getLastVisibleItem();

                // Sends a tag to Xiti only when the modulus equals to zero;
                // e.g. seenItemCount is 0, 10, 20...
                // Added visibleItems > 0 to make sure that we got result back from onLoadComplete and got data
                // Added isFirstPageTag flag because when there is only 1 or 2 ads result, onScrolled is called before the resultTotal is set in onLoadComplete
                if (resultTotal > 0 && lastVisibleItem > seenItemCount && seenItemCount >= 0) {
                    for (int i = seenItemCount; i < lastVisibleItem; i++) {
                        if ((i == 0 && !isFirstPageTag) || (i > 0 && i % AdsFragment.ADS_COUNT_PER_XITI_PAGE == 0)) {
                            isFirstPageTag = true;
                            sendTagging((i / AdsFragment.ADS_COUNT_PER_XITI_PAGE) + 1);//start from 0, need to plus 1
                        }
                    }
                    seenItemCount = lastVisibleItem;
                }
                loadMore();
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
            super.onScrollStateChanged(recyclerView, scrollState);
            final Picasso picasso = Picasso.with(getContext());
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                picasso.resumeTag(getContext());
            } else {
                picasso.pauseTag(getContext());
            }
        }
    };
    private SparseBooleanArray toBeRemovedFromFav = new SparseBooleanArray();
    private SparseArray<ACAd> toBeAddedToFav = new SparseArray<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private View.OnClickListener AdsFragmentOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int xitiCategory = 0;
            if (!ACUtils.isEmpty((String) params.get(Constants.CATEGORY))) {
                xitiCategory = Integer.parseInt((String) params.get(Constants.CATEGORY));
            }
            switch (v.getId()) {
                case R.id.floating_action_btn:
                    EventTrackingUtils.sendClickByCategoryId(dataTealium, xitiCategory, XitiUtils.LISTING, LISTING_INSERT_AD, XitiUtils.NAVIGATION);
                    Intent intentInsert = new Intent(getActivity(), InsertAdActivity.class);
                    intentInsert.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentInsert);
                    break;

                case R.id.rl_left_segment:
                    listener.onLeftSegmentClick();
                    break;

                case R.id.rl_mid_segment:
                    listener.onMidSegmentClick();
                    EventTrackingUtils.sendClickByCategoryId(dataTealium, xitiCategory, XitiUtils.LISTING, LISTING_FILTER_CATEGORY, XitiUtils.NAVIGATION);
                    break;

                case R.id.rl_right_segment:
                    if (showcaseHelper != null) {
                        showcaseHelper.closeAllTutorials(false);
                    }
                    listener.onRightSegmentClick();
                    EventTrackingUtils.sendClickByCategoryId(dataTealium, xitiCategory, XitiUtils.LISTING, LISTING_FILTER, XitiUtils.NAVIGATION);
                    break;
                case R.id.skip_tutorial:
                    EventTrackingUtils.sendClickByCategoryId(dataTealium, xitiCategory, XitiUtils.LISTING, LISTING_SKIP_TUTORIAL, XitiUtils.NAVIGATION);
                    Config.skipAllTutorial = true;
                    skipTutorial.setVisibility(View.GONE);
                    Log.d("tutorial " + showcaseHelper);
                    //hide Tutorial
                    if (showcaseHelper != null) {
                        showcaseHelper.skipAndClearAllTutorial();
                    }
                    break;
            }
        }
    };

    private String categorySeasonGroupId;

    public void setCateogryGroupForSeasonCategory(String catagoryGroupId) {
        categorySeasonGroupId = catagoryGroupId;
    }

    private int getLastVisibleItem() {
        int lastVisibleItem = 0;
        if (Config.listViewMode == ListViewMode.GRID_VIEW && staggeredGridLayoutManager != null) {
            int visibleItems[] = staggeredGridLayoutManager.findLastVisibleItemPositions(null);
            lastVisibleItem = getLastVisibleItem(visibleItems);
        } else if (linearLayoutManagerVertical != null) {
            lastVisibleItem = linearLayoutManagerVertical.findLastVisibleItemPosition();
        }
        return lastVisibleItem + 1;
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    public void setResultTotal(int resultTotal) {
        this.resultTotal = resultTotal;
    }

    public void setXitiCategory(String category) {
        if (!ACUtils.isEmpty(category)) {
            try {
                this.category = Integer.parseInt(category);
            } catch (NumberFormatException numberError){
                this.category = Integer.parseInt(Constants.ALL_CATEGORY);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().supportInvalidateOptionsMenu();
        adViewFavouritesDAO = new AdViewFavouritesDAO(getActivity());
    }

    public void setFloatingActionBarListener(FloatingActionBarListener listener) {
        this.listener = listener;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ads_list, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        setListView(recyclerView);
        int gridColumn = getActivity().getResources().getInteger(R.integer.grid_column_count);
        //for grid view
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(gridColumn, 1);
        //to avoid item moving position when scroll to top
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        //for list view
        linearLayoutManagerVertical =
                new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);


        recyclerViewAdapter = new AdsRecyclerViewAdapter(getActivity().getApplicationContext());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickListener(this);
        recyclerViewAdapter.setOnFavItemClickListener(this);
        setViewMode(false);
        recyclerView.addOnScrollListener(onScrollListener);

        CoordinatorLayout rlListContainer = (CoordinatorLayout) rootView.findViewById(R.id.rl_list_container);

        View resultView = super.onCreateView(inflater, container, savedInstanceState);
        resultView.setId(R.id.result_view);
        RelativeLayout.LayoutParams resultViewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        rlListContainer.addView(resultView, resultViewParams);
        recyclerView.bringToFront();

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_Layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                if (getActivity() instanceof AdsListActivity) {
                    ((AdsListActivity) getActivity()).search();
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.red);
        swipeRefreshLayout.bringToFront();

        actionBarFloating = (View) rootView.findViewById(R.id.action_bar_floating_list);
        //need to bring this view to front so that the view will be in front of the programatically generated list view
        actionBarFloating.bringToFront();

        leftSegment = (RelativeLayout) actionBarFloating.findViewById(R.id.rl_left_segment);
        leftSegment.setOnClickListener(AdsFragmentOnClickListener);

        rlMidSegment = (RelativeLayout) actionBarFloating.findViewById(R.id.rl_mid_segment);
        rlMidSegment.setOnClickListener(AdsFragmentOnClickListener);

        rlRightSegment = (RelativeLayout) actionBarFloating.findViewById(R.id.rl_right_segment);
        rlRightSegment.setOnClickListener(AdsFragmentOnClickListener);

        tvLocationSelected = (TextView) actionBarFloating.findViewById(R.id.tv_location_selected);

        tvCategorySelected = (TextView) actionBarFloating.findViewById(R.id.tv_category_selected);

        footerLoading = rootView.findViewById(R.id.loading_footer);
        footerLoading.bringToFront();

        skipTutorial = (TextView) rootView.findViewById(R.id.skip_tutorial);
        skipTutorial.setOnClickListener(AdsFragmentOnClickListener);

        FloatingActionButton floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floating_action_btn);
        //need to bring this view to front so that the view will be in front of the programatically generated list view
        floatingActionButton.bringToFront();
        floatingActionButton.setOnClickListener(AdsFragmentOnClickListener);

        return rootView;
    }

    public void setLocationText(String location) {
        tvLocationSelected.setText(location);
    }

    public void setCategoryText(String category) {
        tvCategorySelected.setText(category);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (result) return result;
        switch (item.getItemId()) {
            case R.id.action_switch:
                toggleViewMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        while (xitiBacklog.size() > 0) {
            Pair<Integer, Map<String, String>> item = xitiBacklog.poll();
            EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_LISTING, getActivity().getApplicationContext(), RESULT_PAGE, item.first,
                    item.second);
            Log.d("xitiBacklog-sendTag");
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (toBeRemovedFromFav.size() > 0 || toBeAddedToFav.size() > 0) {
            MudahUtil.updateAdViewDAOThread(adViewFavouritesDAO, toBeRemovedFromFav, toBeAddedToFav);
        }
        currentFavouriteAdSize = Config.allFavouritAdIds.size();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (showcaseHelper != null) {
            showcaseHelper.releaseHostActivity();
        }
        //A memory leak detection
        MudahUtil.detectMemoryLeakForFragment(getActivity(), this);
    }

    private void showTutorial() {
        int resourceId = 0;
        int tipTitle = 0;
        int tipDesc = 0;
        showcaseHelper = new ShowcaseHelper(getActivity());
        while (currentTutorialStep <= ShowcaseHelper.LISTING_TUTORIAL_STEPS) {
            switch (currentTutorialStep) {
                case 1:
                    resourceId = R.id.v_center_mark_region;
                    tipTitle = R.string.location_title_tip;
                    tipDesc = R.string.location_desc_tip;
                    break;
                case 2:
                    resourceId = R.id.v_center_mark_category;
                    tipTitle = R.string.category_title_tip;
                    tipDesc = R.string.category_desc_tip;
                    break;

                case 3:
                    resourceId = R.id.tv_filter;
                    tipTitle = R.string.filter_title_tip;
                    tipDesc = R.string.filter_desc_tip;
                    break;
            }

            if (resourceId != 0) {
                ShowcaseHelper.TutorialStepAndResource tutorialStepAndResource = showcaseHelper.new TutorialStepAndResource(Config.TutorialPages.LISTING, resourceId);
                tutorialStepAndResource.setStep(currentTutorialStep);
                tutorialStepAndResource.setTitleResource(tipTitle);
                tutorialStepAndResource.setDescriptionResource(tipDesc);
                showcaseHelper.declareSupportedTutorial(tutorialStepAndResource);
            }
            currentTutorialStep++;
        }
        showcaseHelper.showTutorial();

        if (currentTutorialStep > 0) {
            skipTutorialArea = showcaseHelper.showSkipTutorialOption(skipTutorial);
            skipTutorial.bringToFront();
        }
    }

    public void isClickSkipTutorial(float clickY) {
        // Block all touch unless users click on "skip tutorial"
        if (showcaseHelper != null && showcaseHelper.getRemainingTutorialStep() >= 0
                && clickY > skipTutorialArea[1]) {
            skipTutorial.performClick();
        }
    }

    public void hideSkipTutorialOption() {
        if (skipTutorial != null) {
            skipTutorial.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        miSwitchListMode = menu.findItem(R.id.action_switch);
        updateListModeIcon();
    }

    public void updateListModeIcon() {
        if (miSwitchListMode != null) {
            miSwitchListMode.setIcon(Config.listViewMode == ListViewMode.GRID_VIEW ?
                    R.drawable.ic_action_list : R.drawable.ic_action_grid);
            miSwitchListMode.setTitle(Config.listViewMode == ListViewMode.GRID_VIEW ?
                    R.string.action_ads_list_mode : R.string.action_ads_grid_mode);
        }
    }

    private void toggleViewMode() {
        Config.listViewMode = Config.getNextViewMode(Config.listViewMode);
        saveViewMode();
        updateListModeIcon();
        ACUtils.logCrashlytics("switch view mode to: " + Config.listViewMode);

        refreshOptionsMenu();
        setViewMode(true);
    }

    private void setViewMode(boolean isTag) {
        if (Config.listViewMode == ListViewMode.GRID_VIEW) {
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            updateFavouriteIconsInList();
            if (isTag) {
                Toast.makeText(getActivity(), R.string.grid_view, Toast.LENGTH_SHORT).show();
                EventTrackingUtils.sendClickByCategoryId(dataTealium, this.category, XitiUtils.LISTING, "Actionbar_gridModeOn", XitiUtils.NAVIGATION);
            }
        } else if (Config.listViewMode == ListViewMode.LIST_VIEW) {
            recyclerView.setLayoutManager(linearLayoutManagerVertical);
            if (isTag) {
                Toast.makeText(getActivity(), R.string.list_view, Toast.LENGTH_SHORT).show();
                EventTrackingUtils.sendClickByCategoryId(dataTealium, this.category, XitiUtils.LISTING, "Actionbar_pictureModeOn", XitiUtils.NAVIGATION);
            }
        }
    }

    public void saveViewMode() {
        if (Config.listViewMode != null && getActivity() != null) {
            PreferencesUtils.getSharedPreferences(getActivity().getApplicationContext()).edit()
                    .putString(PreferencesUtils.LIST_VIEW_MODE, Config.listViewMode.toString())
                    .apply();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void refreshOptionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActivity().invalidateOptionsMenu();
        } else {
            getActivity().supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), AdViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        int baseOffsetPosition = 0;
        //max scroll to previous ad, to keep the list id list short
        //Ex. if users are at page 40, there will be 400 list Id. Too big to pass everything
        if (position > MAX_PREV_SCROLL_REQUIREMENT) {
            baseOffsetPosition = position - MAX_PREV_SCROLL_REQUIREMENT;
            intent.putExtra(AdViewActivity.EXTRA_LIST_ITEM_POSITION, MAX_PREV_SCROLL_REQUIREMENT);
        } else {
            intent.putExtra(AdViewActivity.EXTRA_LIST_ITEM_POSITION, position);
        }

        intent.putExtra(AdViewActivity.EXTRA_SEARCH_PARAM, searchParam);
        intent.putExtra(AdViewActivity.EXTRA_GRAND_TOTAL, getGrandTotal());
        intent.putStringArrayListExtra(AdViewActivity.EXTRA_ALL_LIST_ID, recyclerViewAdapter.getAllListIdFromOffset(baseOffsetPosition));
        startActivity(intent);
    }

    public void loadMore() {
        if (!isLoading() && shouldLoadMore()) {
            //add progress item
            footerLoading.setVisibility(View.VISIBLE);
            // calculate for next offset(e.g.If limit is 10, page 1 => offset 0
            // page 2 => offset 10, page 3 => offset 20

            int offset = (getCurrentPage()) * VISIBLE_THRESHOLD;
            Log.d("load more offset: " + offset);
            setApi(method, resource, params, offset);
            //check if the fragment is currently added to its activity.
            if (isAdded())
                restartLoader(false);

        }
    }

    private boolean shouldLoadMore() {
        int count = lastVisibleItem + VISIBLE_THRESHOLD;
        return (currentTotal <= count && currentTotal < grandTotal);
    }

    public void setFilterParamForTagging(HashMap<String, String> params, HashMap<String, String> labelParams) {
        filterParamForTagging = TealiumHelper.customizeParams(params);
        filterLabelParamsForTagging = TealiumHelper.customizeParams(labelParams, Constants.LABEL_VALUE);
    }

    @Override
    public void setApi(Method method, String resource, Map<String, Object> params) {
        super.setApi(method, resource, params);
        searchParam = (HashMap<String, Object>) params;
        seenItemCount = 0;
        resultTotal = 0;
        isFirstPageTag = false;
        recyclerViewAdapter.clearItems();

        if (params != null && params.containsKey(Constants.CATEGORY))
            setXitiCategory((String) params.get(Constants.CATEGORY));
    }

    public void setCustomTargetingParams(HashMap<String, String> params) {
        customTargetingParams = params;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d();
        toBeRemovedFromFav.clear();
        toBeAddedToFav.clear();
        updateFavouriteIconsInList();
        updateUnreadMsg();
    }

    private void updateUnreadMsg(){
        if (Config.userAccount.isLogin() && Config.enableChat) {
            getActivity().invalidateOptionsMenu();
        }
    }

    public void updateFavouriteIconsInList() {
        //Update the fav icons in list, in case users has favourited ad in Ad Detail
        if (Config.listViewMode == Config.ListViewMode.GRID_VIEW && recyclerViewAdapter != null) {
            // if Config.allFavouritAdIds.size() is empty but currentFavouriteAdSize is more than 0,
            // meaning users deleted all favourite ads and return to listing. Need to update the heart icon
            if ((Config.allFavouritAdIds.size() > 0 && recyclerViewAdapter.getItemCount() > 0)
                    || (Config.allFavouritAdIds.size() == 0 && currentFavouriteAdSize > 0)) {
                //update the fav icon in ad if there are ads
                recyclerViewAdapter.notifyDataSetChanged();
            } else if (Config.allFavouritAdIds == null || Config.allFavouritAdIds.size() == 0) {
                UpdateFavouriteIconsInListAsyncTask updateFavouriteIconsInListAsyncTask = new UpdateFavouriteIconsInListAsyncTask(this);
                updateFavouriteIconsInListAsyncTask.execute();
            }
        }
    }

    private void sendTagForNoResult() {
        Log.d();
        // When result is 0, onScroll() is not called and tryXiti() is never called too.
        // Need to manually call tryXiti()
        sendTagging(1);
        seenItemCount = -1;// prevent double tracking
    }

    @Override
    protected void onLoadError(BlocketLoader loader, JSONObject data) {
        super.onLoadError(loader, data);
        //remove loadding bar
        footerLoading.setVisibility(View.GONE);

        if (getActivity() instanceof AdsListActivity) {
            ((AdsListActivity) getActivity()).tagConnectionLost(LISTING_CONNECTION_LOST, data);
        }
    }

    @Override
    protected void onLoadComplete(BlocketLoader loader, JSONObject data)
            throws LoadException {
        int page = getCurrentPage();
        super.onLoadComplete(loader, data);
        // Stop refresh animation
        swipeRefreshLayout.setRefreshing(false);

        Crashlytics.getInstance().core.setInt("current_page", page);

        // onScroll() is not called on the first load
        if (seenItemCount == 0 && resultTotal == 0) {
            sendTagForNoResult();
            swipeRefreshLayout.setEnabled(false);
        } else {
            swipeRefreshLayout.setEnabled(true);
            // Added isFirstPageTag flag because when there is only 1 or 2 ads result, onScrolled is called before the resultTotal is set in onLoadComplete
            if (getCurrentPage() == 1 && !isFirstPageTag) {
                isFirstPageTag = true;
                sendTagging(getCurrentPage());
            }
        }

        ACUtils.logCrashlytics("currentPage: " + getCurrentPage());
        if (page < getCurrentPage()) {
            try {
                JSONArray items = data.getJSONArray("ads");
                //remove loadding bar
                footerLoading.setVisibility(View.GONE);
                if (items.length() > 0) {
                    recyclerViewAdapter.addItems(items);
                    setLoading(false);
                    //To do: Temporary comment out for now until OOM is solved
//                    if (Config.listViewMode == ListViewMode.GRID_VIEW) {
//                        preFetchImage(items);
//                    }
                }

            } catch (JSONException e) {
                ACUtils.logCrashlytics("query: " + searchParam);
                ACUtils.debug(e, "AdsFragment_onLoadComplete", data.toString(), false);
                //TO DO: Check if this will be catched in BlocketLoaderFragment or it will cause the app to crash?
                throw new LoadException(e);
            }

            if (resultTotal == 0) {
                setViewEmptyResultShown(true);
            }
        }
        //Hide loading
        vLoading.setVisibility(View.GONE);

        //show tutorial
        if (getCurrentPage() == 1) {
            prepareTutorial();
        }
    }

    private void prepareTutorial() {
        if (Config.tutorialPagesAndSteps.containsKey(Config.TutorialPages.LISTING.toString())) {
            currentTutorialStep = Config.tutorialPagesAndSteps.get(Config.TutorialPages.LISTING.toString());
        } else {
            currentTutorialStep = 1;
        }
        Log.d("currentTutorialStep: " + currentTutorialStep);

        if (!((AdsListActivity) getActivity()).isSkipTutorial() && currentTutorialStep <= ShowcaseHelper.LISTING_TUTORIAL_STEPS) {
            listenForWhenButtonBecomesVisible();
        }
    }

    private void listenForWhenButtonBecomesVisible() {
        final ViewTreeObserver viewTreeObserver = getActivity().getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View menuButton = getActivity().findViewById(R.id.tv_location);
                // This could be called when the button is not there yet, so we must test for null
                if (menuButton != null) {
                    // Found it! Do what you need with the button
                    showTutorial();
                    // Now you can get rid of this listener
                    try {
                        if (Build.VERSION.SDK_INT < 16) {
                            getActivity().getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    } catch (IllegalStateException illegal) {
                        ACUtils.debug(illegal);
                    }
                }
            }
        });
    }

    private void preFetchImage(final JSONArray items) {
        if (items != null) {
            Thread imgThread = new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject itemObj = items.optJSONObject(i);
                        String imgURL = itemObj.optString("grid_image");
                        if (ACUtils.isEmpty(imgURL))
                            imgURL = itemObj.optString("image");
                        if (!ACUtils.isEmpty(imgURL) && getActivity() != null) {
                            Picasso.with(getActivity().getApplicationContext())
                                    .load(imgURL)
                                    .fetch();
                        }
                    }
                }
            };

            imgThread.start();
        }
    }

    public int getActualPrice(String priceRange) {
        int actualPrice = -1;
        String price = Constants.EMPTY_STRING;
        ACReferences ref = ACReferences.getACReferences();
        try {
            if (ref != null) {
                if (ref.filterParams != null) {
                    JSONObject filterPrice = (JSONObject) ref.filterParams.get(Constants.TYPE_SALE);
                    JSONObject priceList = (JSONObject) filterPrice.get(Constants.PRICELIST);
                    JSONArray priceMapping = (JSONArray) priceList.get(Constants.OPTIONS);
                    JSONObject priceMap;
                    Iterator<String> priceMapKey;
                    String key;
                    for (int i = 0; i < priceMapping.length(); i++) {
                        priceMap = priceMapping.getJSONObject(i);
                        priceMapKey = priceMap.keys();
                        if (priceMapKey.hasNext()) {
                            key = priceMapKey.next();
                            if (key.equalsIgnoreCase(priceRange)) {
                                price = priceMap.getString(key).toString();
                                price = price.replaceAll(PRICE_REGEX, Constants.EMPTY_STRING);
                            }
                        }
                    }
                }
            }

        } catch (JSONException e1) {
            ACUtils.debug(e1);
        }
        if (!ACUtils.isEmpty(price)) {
            actualPrice = Integer.parseInt(price);
        }
        return actualPrice;
    }

    public PublisherAdRequest createAdRequest(HashMap<String, Object> params) {
        PublisherAdRequest request;
        if (params != null) {
            Bundle extras = new Bundle();
            String key;
            String value;
            Map.Entry pair;
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                pair = (Map.Entry) iterator.next();
                key = pair.getKey().toString();

                if (key.equalsIgnoreCase(Constants.REGION) || key.equalsIgnoreCase(Constants.CATEGORY) || key.equalsIgnoreCase(Constants.KEYWORD) || key.equalsIgnoreCase("pricelist") || key.equalsIgnoreCase(Constants.TYPE)) {
                    value = pair.getValue().toString();
                    if (key.equalsIgnoreCase("pricelist")) {
                        boolean threshold_android_listing = false;
                        int minPrice = 0;
                        int maxPrice = 0;
                        int actualMinPrice;
                        int actualMaxPrice;
                        int category = -1;

                        int index = value.indexOf("-", 0);
                        String minPriceParam = value.substring(0, index);
                        index = index + 1;
                        String maxPriceParam = value.substring(index, value.length());

                        actualMinPrice = getActualPrice(minPriceParam);
                        actualMaxPrice = getActualPrice(maxPriceParam);

                        if (params.get(Constants.CATEGORY) != null && params.get(Constants.CATEGORY) != Constants.EMPTY_STRING) {
                            category = Integer.parseInt(params.get(Constants.CATEGORY).toString());
                        }

                        JSONObject priceSet = DfpConfig.getDfpPriceByCategory(category);
                        if (priceSet != null) {
                            minPrice = priceSet.optInt("min_price");
                            maxPrice = priceSet.optInt("max_price");

                            if (maxPrice != 0 && actualMaxPrice <= maxPrice) {
                                threshold_android_listing = !(minPrice != 0 && actualMinPrice < minPrice);
                            }
                        }

                        if (threshold_android_listing) {
                            extras.putBoolean("threshold_android_listing", threshold_android_listing);
                        }
                    } else {
                        if (key.equalsIgnoreCase(Constants.KEYWORD)) {
                            key = "kw";
                        } else if (key.equalsIgnoreCase(Constants.TYPE)) {
                            key = "adtype";
                        }
                        extras.putString(key, value);
                    }
                }

            }
            if (customTargetingParams != null) {
                Iterator paramIterator = customTargetingParams.entrySet().iterator();
                while (paramIterator.hasNext()) {
                    Map.Entry paramPair = (Map.Entry) paramIterator.next();
                    extras.putString(paramPair.getKey().toString(), paramPair.getValue().toString());
                }
            }

            request = new PublisherAdRequest.Builder().addNetworkExtras(new AdMobExtras(extras)).build();
        } else {
            request = new PublisherAdRequest.Builder().build();
        }
        return request;
    }

    private void sendTagging(int pageNumber) {
        tryXiti();
        tagTealium(RESULT_PAGE, Constants.EMPTY_STRING, pageNumber);
        GravityUtils.sendEventAsyncWithEventType(GravityUtils.EVENT_TYPE_BROWSE);
    }

    // Region(1), Neighboring(2), Entire Malaysia(3)
    private String getRegionScope() {
        String regionScope = Constants.EMPTY_STRING;
        // Region(1), Neighboring(2), Entire Malaysia(3)
        if (null != params.get(Constants.AREA)) {
            String w = params.get(Constants.AREA).toString();
            regionScope = w.charAt(0) + Constants.EMPTY_STRING;
        } else if (params.get("ca") != null) {
            regionScope = Constants.AREA_SPECIFIC;
        } else {
            regionScope = Constants.AREA_ENTIRE_COUNTRY;
        }
        return regionScope;
    }

    private String getSellerType() {
        String sellerType = params.get(Constants.POSTED_BY) + Constants.EMPTY_STRING;

        if (ACUtils.isEmpty(sellerType)) {
            sellerType = Constants.POSTED_BY_ALL;
        }

        return sellerType;
    }

    public void tagTealium(String pageName, String level2, int pageNumber) {
        ACReferences ref = ACReferences.getACReferences();
        ACSettings set = ACSettings.getACSettings();
        String regionScope = getRegionScope();
        String regionId = ref.getRegionId() + Constants.EMPTY_STRING;
        String st = (null != params.get(Constants.TYPE)) ? (String) params.get(Constants.TYPE) : Constants.TYPE_ALL;
        String titleOnly = params.get(Constants.TITLE_ONLY) + Constants.EMPTY_STRING;
        String keyword = Constants.EMPTY_STRING;
        String sellerType = getSellerType();
        String subRegionId = Constants.EMPTY_STRING;
        String regionName = Constants.MALAYSIA;
        String subRegionName = Constants.EMPTY_STRING;
        String categoryId = Constants.ALL_CATEGORY;// All Category
        String categoryName = tvCategorySelected.getText().toString();
        String sortBy = (String) params.get(Constants.SORT_BY);
        String parentCategoryId = Constants.EMPTY_STRING;
        String parentCategoryName = Constants.EMPTY_STRING;

        if (params.containsKey(Constants.KEYWORD)) {
            keyword = params.get(Constants.KEYWORD) + Constants.EMPTY_STRING;
        }

        if (ACUtils.isEmpty(regionId)) {
            regionId = Constants.ALL_REGION;
        }

        if (ref.getRegionId() != null) {
            regionName = ACSettings.getACSettings().getRegionName(ref.getRegionId());
        }

        if (ref.getMunicipalityId() != null) {
            subRegionId = ref.getMunicipalityId();
            subRegionName = set.getMunicipalityName(ref.getMunicipalityId());
        }

        if (params.containsKey(Constants.CATEGORY)) {
            categoryId = (String) params.get(Constants.CATEGORY);
            ad = new AdViewAd();
            if (MudahUtil.prepareParentCategoryInfo(categoryId, ad)) {
                parentCategoryId = ad.getParentCategoryId();
                parentCategoryName = ad.getParentCategoryName();
            } else { //this is a parent category
                parentCategoryId = categoryId;
                parentCategoryName = categoryName;
            }
        } else {//no selected category
            parentCategoryId = Constants.EMPTY_STRING;
            parentCategoryName = Constants.EMPTY_STRING;
        }

        if (ACUtils.isEmpty(level2)) {
            String tmpCategory = categoryId;
            if (!ACUtils.isEmpty(categorySeasonGroupId)) {
                tmpCategory = categorySeasonGroupId;
            }
            level2 = XitiUtils.getCustomLevel2Tag(Integer.valueOf(tmpCategory), XitiUtils.MODE_LISTING);
        }
        if (ACUtils.isEmpty(sortBy)) {
            sortBy = Constants.SORT_BY_DATE + Constants.EMPTY_STRING;
        }

        dataTealium = Tealium.map(
                TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_LIST,
                TealiumHelper.PAGE_NAME, pageName,
                TealiumHelper.REGION_ID, regionId,
                TealiumHelper.REGION_NAME, regionName,
                TealiumHelper.REGION_SCOPE, regionScope,
                TealiumHelper.SUBREGION_ID, subRegionId,
                TealiumHelper.SUBREGION_NAME, subRegionName,
                TealiumHelper.CATEGORY_ID, categoryId,
                TealiumHelper.PARENT_CATEGORY_ID, parentCategoryId,
                TealiumHelper.PARENT_CATEGORY_NAME, parentCategoryName,
                TealiumHelper.CATEGORY_NAME, categoryName,
                TealiumHelper.AD_TYPE, st,
                TealiumHelper.KEYWORD, keyword,
                TealiumHelper.SEARCH_TITLE_ONLY, (!ACUtils.isEmpty(keyword) && (Constants.TITLE_ONLY_ENABLED).equals(titleOnly)) ? titleOnly : Constants.EMPTY_STRING,
                TealiumHelper.PAGE_NUMBER, pageNumber + Constants.EMPTY_STRING,
                TealiumHelper.NUMBER_OF_ADS, getGrandTotal() + Constants.EMPTY_STRING,
                TealiumHelper.SELLER_TYPE, sellerType,
                TealiumHelper.SORT_TYPE, sortBy,
                TealiumHelper.VIEW_TYPE, Config.listViewMode.toString(),
                TealiumHelper.XTN2, level2);

        if (filterParamForTagging.size() > 0) {
            dataTealium.putAll(filterParamForTagging);
        }
        if (filterLabelParamsForTagging.size() > 0) {
            dataTealium.putAll(filterLabelParamsForTagging);
        }

        TealiumHelper.track(this, dataTealium, Tealium.VIEW);
    }

    /**
     * Send a tag to Xiti for the listing page when this fragment is attached.
     * Otherwise, store the tag in a xitiBacklog and send when this fragment is
     * attached later on.
     */
    public void tryXiti() {
        prepareXitiMap();
        // Category
        String cg = Constants.ALL_CATEGORY;// All Category
        if (params.containsKey(Constants.CATEGORY)) {
            cg = params.get(Constants.CATEGORY).toString();
            if (!ACUtils.isEmpty(categorySeasonGroupId)) {
                cg = categorySeasonGroupId;
            }
        }
        if (!isDetached() && xitiMapX.size() > 0) {
            EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_LISTING, getActivity().getApplicationContext(), RESULT_PAGE,
                    Integer.valueOf(cg), xitiMapX);
            Log.d("sendTag");
            // ------------------------------------------------------------
        } else if (xitiMapX.size() > 0) {
            xitiBacklog.add(new Pair<>(Integer.valueOf(cg), xitiMapX));
            Log.d("xitiBacklog");
        }
    }

    public void tagXitiWithPageNameAndLevel2(String pageName, String level2) {
        if (xitiMapX == null || xitiMapX.size() == 0) {
            prepareXitiMap();
        }
        EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getActivity().getApplicationContext(), pageName, level2, xitiMapX);
    }

    public void prepareXitiMap() {
        // ---------------------- XiTi --------------------------------
        xitiMapX = new HashMap<>();

        if (null != params.get(Constants.REGION)) {
            xitiMapX.put(XitiUtils.CUST_VAR_REGION, params.get(Constants.REGION).toString());
            ACReferences ref = ACReferences.getACReferences();
            if (ref != null && !ACUtils.isEmpty(ref.getMunicipalityId())) {
                ACSettings set = ACSettings.getACSettings();
                String subRegionName = set.getMunicipalityName(ref.getMunicipalityId());
                if (!ACUtils.isEmpty(subRegionName)) {
                    xitiMapX.put(XitiUtils.CUST_VAR_SUB_REGION, subRegionName);
                }
            }
        }

        // is keyword entered?
        if (null != params.get(Constants.KEYWORD) && !ACUtils.isEmpty(params.get(Constants.KEYWORD).toString())) {
            xitiMapX.put(XitiUtils.CUST_VAR_KEYWORD_ENTER, XitiUtils.KEYWORD_ENTER);
            xitiMapX.put(XitiUtils.SEARCH_KEYWORD_RESULT, params.get(Constants.KEYWORD) + XitiUtils.SEARCH_KEYWORD_RESULT + resultTotal);//e.g. toyota+delimiter+1000
        } else {
            xitiMapX.put(XitiUtils.CUST_VAR_KEYWORD_ENTER, XitiUtils.NO_KEYWORD);
        }
        //ad type choosen?
        String st = (null != params.get(Constants.TYPE)) ? params.get(Constants.TYPE).toString() : Constants.TYPE_ALL;
        xitiMapX.put(XitiUtils.CUST_VAR_AD_TYPE, XitiUtils.getAdTypeValue(st) + Constants.EMPTY_STRING);

        String regionScope = getRegionScope();
        if (!ACUtils.isEmpty(regionScope)) {
            xitiMapX.put(XitiUtils.CUST_VAR_RESULT_SCOPE, regionScope);
        }

        // Image mode, image(1), no image(2) and grid view (3)
        if (Config.listViewMode == ListViewMode.LIST_VIEW)
            xitiMapX.put(XitiUtils.CUST_VAR_IMG_MODE, XitiUtils.IMAGE_MODE);
        else if (Config.listViewMode == ListViewMode.GRID_VIEW)
            xitiMapX.put(XitiUtils.CUST_VAR_IMG_MODE, XitiUtils.GRID_MODE);

        // Result Ad Resources, all(1), private(2), company(3)
        String sellerType = getSellerType();
        if (Constants.POSTED_BY_PRIVATE.equals(sellerType)) {
            xitiMapX.put(XitiUtils.CUST_VAR_RESULT_AD_SRC, XitiUtils.AD_SRC_PRIVATE);
        } else if (Constants.POSTED_BY_COMPANY.equals(sellerType)) {
            xitiMapX.put(XitiUtils.CUST_VAR_RESULT_AD_SRC, XitiUtils.AD_SRC_COMPANY);
        } else {
            xitiMapX.put(XitiUtils.CUST_VAR_RESULT_AD_SRC, XitiUtils.AD_SRC_ALL);
        }

        //User Account
        if (Config.userAccount.isLogin()) {
            xitiMapX.put(XitiUtils.CUST_VAR_USER_ID, Config.userAccount.getUserId());
            xitiMapX.put(XitiUtils.CUST_VAR_USER_EMAIL, Config.userAccount.getEmail());
        }
    }

    //on favourite click
    @Override
    public void onClick(View view) {
        final int position = recyclerView.getChildAdapterPosition(((AdsListViewHolders) view.getTag()).itemView);
        saveFavourite(recyclerViewAdapter.getItem(position), (ImageView) view);
    }

    private void saveFavourite(JSONObject data, ImageView imvFavourite) {
        String categoryName = null;

        ACCategory category = ACSettings.getACSettings().getCategory(data.optString(Constants.CATEGORY_TXT) + Constants.EMPTY_STRING);
        if (category != null) {
            categoryName = category.getName();
        }
        AdViewAd acAd = new AdViewAd(data);
        acAd.setListId(data.optInt(Constants.LIST_ID_KEY));
        acAd.setCategoryName(categoryName);
        acAd.setCondition(data.optString(AmplitudeUtils.CONDITION));
        toggleFavourite(acAd, imvFavourite);

    }

    private void toggleFavourite(AdViewAd acAd, ImageView imvFavourite) {
        boolean isFavourited = false;
        boolean isExceedMax = false;
        if (MudahUtil.isFavouritedAd(acAd.getListId())) {
            Log.d("remove fav: " + acAd.getListId());
            isFavourited = false;
            toBeRemovedFromFav.put(acAd.getListId(), true);
            toBeAddedToFav.remove(acAd.getListId());
            Config.allFavouritAdIds.delete(acAd.getListId());

        } else if (Config.allFavouritAdIds.size() < Config.maxAdviewFavTotal) {
            Log.d("add fav: " + acAd.getListId());
            isFavourited = true;
            toBeAddedToFav.put(acAd.getListId(), acAd);
            toBeRemovedFromFav.delete(acAd.getListId());
            Config.allFavouritAdIds.put(acAd.getListId(), true);

        } else if (Config.allFavouritAdIds.size() == Config.maxAdviewFavTotal) {
            isFavourited = false;
            isExceedMax = true;
            MudahUtil.showExceedMaxFavouriteResult(getActivity());
        }

        if (!isExceedMax) {
            Picasso.with(getActivity()).load(isFavourited ?
                    R.drawable.ic_action_heart_red_grid
                    : R.drawable.ic_action_heart_grid).into(imvFavourite);
            MudahUtil.showFavouriteResult(getContext(), isFavourited);
            sendTagFavourite(acAd, isFavourited);
        }

    }

    private void sendTagFavourite(AdViewAd acAd, boolean isFavourited) {
        if (acAd != null && !ACUtils.isEmpty(acAd.getCategoryName())) {
            String pageAndClickName = isFavourited ? getString(R.string.saved_to_favourites) : getString(R.string.removed_my_favourites);
            tagTealiumFavourite(acAd, pageAndClickName, XitiUtils.LEVEL2_FAVOURITE_ID);
            tagXitiFavourite(acAd, pageAndClickName, XitiUtils.LEVEL2_FAVOURITE_ID);
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_FAVOURITE_ID, pageAndClickName + XitiUtils.CHAPTER_SIGN + TealiumHelper.getSellerTypeTextFromId(acAd.getCompanyAd()) + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(acAd.getCategoryName()) + XitiUtils.CHAPTER_SIGN + acAd.getRegion(), XitiUtils.NAVIGATION);
            if (isFavourited) {
                MudahUtil.prepareParentCategoryInfo(acAd.getCategoryId(), acAd);
                KahunaHelper.tagEventWithAttribute(KahunaHelper.FAV_EVENT, acAd, KahunaHelper.ACTION_FAV);
                KahunaHelper.tagAttributes(KahunaHelper.PAGE_AD_FAV, KahunaHelper.LAST_TITLE_FAV, acAd.getSubject());
                AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.FAV_AD_EVENT, acAd);
            }
        }
    }

    private void tagTealiumFavourite(AdViewAd acAd, String pageName, String level2Fav) {
        Map<String, String> dataTealium = null;

        if (acAd != null) {
            String listId = acAd.getListId() + Constants.EMPTY_STRING;
            String title = MudahUtil.removeUnwantedSign(acAd.getSubject());

            String sellerType = MudahUtil.getSellerTypeStr(acAd.getCompanyAd());

            dataTealium = Tealium.map(
                    TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_LIST,
                    TealiumHelper.PAGE_NAME, pageName,
                    TealiumHelper.REGION_ID, acAd.getRegionId(),
                    TealiumHelper.REGION_NAME, acAd.getRegion(),
                    TealiumHelper.SUBREGION_ID, acAd.getSubRegionId(),
                    TealiumHelper.SUBREGION_NAME, acAd.getSubRegionName(),
                    TealiumHelper.CATEGORY_ID, acAd.getCategoryId(),
                    TealiumHelper.CATEGORY_NAME, acAd.getCategoryName(),
                    TealiumHelper.AD_TYPE, acAd.getTypeAd(),
                    TealiumHelper.SELLER_TYPE, sellerType,
                    TealiumHelper.AD_SELLER_TYPE, acAd.getAdSellerType(),
                    TealiumHelper.LIST_ID, listId,
                    TealiumHelper.AD_TITLE, title,
                    TealiumHelper.XTN2, level2Fav);

            TealiumHelper.track(this, dataTealium, Tealium.EVENT);
        }
    }

    private void tagXitiFavourite(AdViewAd acAd, String pageName, String level2Fav) {
        // ---------------------- XiTi --------------------------------
        Map<String, String> mapX = new HashMap<>();
        mapX.put(XitiUtils.CUST_VAR_REGION, acAd.getRegionId());
        mapX.put(XitiUtils.CUST_VAR_AD_TYPE, XitiUtils.getAdTypeValue(acAd.getTypeAd()) + Constants.EMPTY_STRING);

        // Result Ad Resources, all(1), private(2), company(3)
        String sellerType = MudahUtil.getSellerTypeStr(acAd.getCompanyAd());
        if (Constants.POSTED_BY_PRIVATE.equals(sellerType)) {
            mapX.put(XitiUtils.CUST_VAR_RESULT_AD_SRC, XitiUtils.AD_SRC_PRIVATE);
        } else if (Constants.POSTED_BY_COMPANY.equals(sellerType)) {
            mapX.put(XitiUtils.CUST_VAR_RESULT_AD_SRC, XitiUtils.AD_SRC_COMPANY);
        }

        mapX.put(XitiUtils.CUST_VAR_CATEGORY_NAME, acAd.getCategoryName());
        //User Account
        if (Config.userAccount.isLogin()) {
            mapX.put(XitiUtils.CUST_VAR_USER_ID, Config.userAccount.getUserId());
            mapX.put(XitiUtils.CUST_VAR_USER_EMAIL, Config.userAccount.getEmail());
        }

        EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getActivity().getApplicationContext(), pageName, level2Fav, mapX);
    }

    public interface FloatingActionBarListener {
        /**
         * Called when the left segment of floating actionbar is clicked
         */
        public void onLeftSegmentClick();

        /**
         * Called when the mid segment of floating actionbar is clicked
         */
        public void onMidSegmentClick();

        /**
         * Called when the right segment of floating actionbar is clicked
         */
        public void onRightSegmentClick();

    }

    private static class UpdateFavouriteIconsInListAsyncTask extends AsyncTask<Void, Void, SparseBooleanArray> {
        private WeakReference<AdsFragment> fragmentWeakReference;

        private UpdateFavouriteIconsInListAsyncTask(AdsFragment adsFragment) {
            this.fragmentWeakReference = new WeakReference<>(adsFragment);
        }

        @Override
        protected SparseBooleanArray doInBackground(Void... voids) {
            if (fragmentWeakReference == null || fragmentWeakReference.get() == null)
                return null;
            AdsFragment adsFragment = fragmentWeakReference.get();
            Config.allFavouritAdIds = adsFragment.adViewFavouritesDAO.getAllFavouritesID();
            return Config.allFavouritAdIds;
        }

        @Override
        protected void onPostExecute(SparseBooleanArray result) {
            if (fragmentWeakReference == null || result == null || Config.listViewMode != Config.ListViewMode.GRID_VIEW)
                return;
            AdsFragment adsFragment = fragmentWeakReference.get();
            if (adsFragment == null || adsFragment.recyclerViewAdapter == null)
                return;

            Log.d("Updated allFavouritAdIds: " + Config.allFavouritAdIds);
            //update the fav icon in ad if there are ads
            if (Config.allFavouritAdIds.size() > 0 && adsFragment.recyclerViewAdapter.getItemCount() > 0) {
                adsFragment.recyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

}
