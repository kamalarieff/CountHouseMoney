package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.UserAdsAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.mudah.my.utils.NavigationViewUtil;
import com.mudah.my.viewholders.AdsListViewHolders;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kalpana on 12/2/15.
 */
public class UserLiveAdsActivity extends MudahBaseActivity implements AdsListViewHolders.OnItemClickListener {
    private static final int ADS_COUNT_PER_PAGE = 10;
    private static final int LOADER_USER_ADS = 1;
    private static final int VISIBLE_THRESHOLD = 10;
    private boolean isFromSignIn = false;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManagerVertical;
    private UserAdsAdapter recyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar pbLoading;
    private int lastVisibleItem;
    private int resultTotal;
    private int currentPage = 1;
    private boolean isLoading;
    private int currentTotal;
    private View footerLoading;
    private View vConnectionLost;
    private TextView emptyView;
    private NavigationViewUtil navigationViewUtil;
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int distanceY) {
            if (distanceY >= 0) { //check for scroll down
                lastVisibleItem = getLastVisibleItem();
                loadMore();
            }
        }
    };

    private int getLastVisibleItem() {
        int lastVisibleItem = 0;
        if (linearLayoutManagerVertical != null) {
            lastVisibleItem = linearLayoutManagerVertical.findLastVisibleItemPosition();
        }
        return lastVisibleItem + 1;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        setContentView(R.layout.activity_user_live_ads);
        setupActionBar();

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        vConnectionLost = findViewById(R.id.user_ads_connection_lost);
        setUpConnectionLostView();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_ads);
        setUpAdRecycleView();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_ads_Layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.red);
        setUpSwipeRefresh();

        footerLoading = findViewById(R.id.loading_ads_footer);
        emptyView = (TextView) findViewById(R.id.empty_ads_list);

        Bundle bundle = getIntent().getExtras();
        if (savedInstanceState != null) {
            isFromSignIn = savedInstanceState.getBoolean(SignInActivity.FROM_SIGNIN);
        } else if (bundle != null) {
            isFromSignIn = bundle.getBoolean(SignInActivity.FROM_SIGNIN);
        }

        refreshData();
        sendTag();
    }

    private void setupActionBar() {
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar, getString(R.string.live_ads));

        DrawerLayoutUtils drawerLayoutUtils = actionBar.createSlideInMenu(R.id.drawer_layout, R.id.left_drawer, ActionBarHelper.SHOW_MENU_BUTTON_ON_ACTIONBAR);
        setDrawerLayoutUtils(drawerLayoutUtils);
        navigationViewUtil = drawerLayoutUtils.getNavigationViewUtil();
    }

    private void sendTag() {
        sendTagUserAccount(TealiumHelper.PAGE_UA_LIVE_ADS);
        KahunaHelper.tagEvent(KahunaHelper.LAST_VIEWED_PERSONAL_DASHBOARD);
        KahunaHelper.tagDateAttributes(KahunaHelper.LAST_DASHBOARD_VIEWED);
        AmplitudeUtils.tagEvent(AmplitudeUtils.VIEW_DASHBOARD_EVENT);
    }

    private void setUpSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                resetAll();
                refreshData();
            }
        });
    }

    private void setUpAdRecycleView() {
        linearLayoutManagerVertical =
                new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        recyclerViewAdapter = new UserAdsAdapter(getApplicationContext());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickListener(this);
        recyclerView.addOnScrollListener(onScrollListener);
        recyclerView.setLayoutManager(linearLayoutManagerVertical);
    }

    private void setUpConnectionLostView() {
        ImageView connectionLostImg = (ImageView) findViewById(R.id.imgv_connection_lost);
        Picasso.with(this).load(R.drawable.loader_connection_lost).fit().centerInside().into(connectionLostImg);
        vConnectionLost.setVisibility(View.GONE);
        vConnectionLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vConnectionLost.setVisibility(View.GONE);
                resetAll();
                refreshData();
            }
        });
    }

    private void resetAll() {
        lastVisibleItem = 0;
        resultTotal = 0;
        currentPage = 1;
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, AdViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(AdViewActivity.EXTRA_LIST_ITEM_POSITION, position);
        intent.putExtra(AdViewActivity.EXTRA_GRAND_TOTAL, resultTotal);
        intent.putStringArrayListExtra(AdViewActivity.EXTRA_ALL_LIST_ID, recyclerViewAdapter.getAllListId());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        goBack();
        finish();//to prevent looping
    }

    private void goBack() {
        if (isFromSignIn) {
            Intent intent = new Intent(this, HomepageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(SignInActivity.FROM_SIGNIN, isFromSignIn);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void loadMore() {
        if (!isLoading() && shouldLoadMore()) {
            Log.d("load page: " + (getCurrentPage() + 1));
            getSupportLoaderManager().restartLoader(LOADER_USER_ADS, null, asyncLoadUserAds(getCurrentPage() + 1));
        }
    }

    public void refreshData() {
        //only show the center progress bar on first load, page 1
        //page 2 is a buffer load for fast access
        // if it's refreshing, there is a progress circle already. No need to add another loading sign
        if (!swipeRefreshLayout.isRefreshing()) {
            if (getCurrentPage() <= 2) {
                pbLoading.setVisibility(View.VISIBLE);
            } else {
                //add progress item in footer instead of the middle of the page
                footerLoading.setVisibility(View.VISIBLE);
            }
        }
        getSupportLoaderManager().restartLoader(LOADER_USER_ADS, null, asyncLoadUserAds(getCurrentPage()));
    }

    private APILoader.Callbacks asyncLoadUserAds(final int page) {
        Log.d();
        isLoading = true;
        // api/user/myads/lgn_9a8ae08ed479b14068bfb35a56e4f622ad374f84?page=1&limit=2&sp=1
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", page);
        params.put("limit", ADS_COUNT_PER_PAGE);
        Log.d("params: " + params);
        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.GET, "myads/" + Config.userAccount.getToken(), params, this) {
            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                super.onLoadFinished(loader, data);
                Log.d();
                isLoading = false;
                pbLoading.setVisibility(View.GONE);
                // Stop refresh animation
                swipeRefreshLayout.setRefreshing(false);
                //remove loadding bar
                footerLoading.setVisibility(View.GONE);
                getSupportLoaderManager().destroyLoader(loader.getId());
            }

            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                try {
                    setCurrentPage(page);
                    currentTotal = 0;
                    resultTotal = 0;

                    if (data.optJSONObject("result") == null) {
                        onLoadError(loader, data);
                    } else {
                        currentTotal = calculateCurrentTotal(data.optJSONObject("result").optInt("lines"));
                        resultTotal = data.optJSONObject("result").optInt("filtered");

                        Log.d("current " + currentTotal + "/" + resultTotal);
                        if (resultTotal == 0) {
                            setViewEmptyResultShown(true);
                        } else {
                            setViewEmptyResultShown(false);
                            //handle swipe refresh
                            if (getCurrentPage() == 1) {
                                recyclerViewAdapter.clearItems();
                            }
                            JSONArray items = data.optJSONObject("result").getJSONArray("ads");
                            if (items.length() > 0) {
                                recyclerViewAdapter.addItems(items);
                            }
                        }
                        vConnectionLost.setVisibility(View.GONE);
                        Config.userAccount.setTotalAds(Integer.toString(resultTotal));
                        Config.userAccount.saveUserDataPreferences(getApplicationContext());
                    }
                } catch (JSONException e) {
                    Log.e("error: ", e);
                    onLoadError(loader, data);
                }
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);
                vConnectionLost.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();
    }

    private int calculateCurrentTotal(int currentPageTotal) {
        if (getCurrentPage() > 1) {
            return (getCurrentPage() - 1) * ADS_COUNT_PER_PAGE + currentPageTotal;
        } else {
            return currentPageTotal;
        }
    }

    private void setViewEmptyResultShown(boolean show) {
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    private boolean shouldLoadMore() {
        int count = lastVisibleItem + VISIBLE_THRESHOLD;
        return (currentTotal <= count && currentTotal < resultTotal);
    }

    public boolean isLoading() {
        return isLoading;
    }
}
