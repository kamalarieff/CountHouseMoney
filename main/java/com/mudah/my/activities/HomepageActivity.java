package com.mudah.my.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.lib701.datasets.ACReferences;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.adapters.BannerFragmentAdapter;
import com.mudah.my.adapters.CategoryGridAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.ShowcaseHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.ChatCafe;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.utils.NavigationViewUtil;
import com.mudah.my.viewholders.CategoryGridHolder;
import com.mudah.my.widgets.CirclePageIndicator;
import com.mudah.my.widgets.MarginDecoration;
import com.optimizely.Optimizely;
import com.optimizely.Variable.LiveVariable;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ondo on 23/9/15.
 */
public class HomepageActivity extends MudahBaseActivity implements CategoryGridHolder.OnItemClickListener {
    public static final String REQUEST_FROM_HOMEPAGE = "request_from_homepage";
    public static final String BANNER_LINK = "banner_link";
    private static final int LOADER_USER_SIGNIN = 0x01;
    private static final String CATEGORY_MODE = "CategoryMode";
    private static final String PREFIX_HOMEPAGE = "hp_";
    private static final String HOMEPAGE_MORE = "hp_more";
    private static final String HOMEPAGE_APPLICATION = "index";
    private static final String HOMEPAGE_AI = "Homepage_to_insertAd";
    private static final String HOMEPAGE_FIND = "Homepage_to_find";
    private static final String HOMEPAGE_SKIP_TUTORIAL = "Homepage_skip_tutorial";
    private static final String HOMEPAGE_RECOMMEND = "Homepage_to_recommended_ad";
    private static final String HOMEPAGE_PAGE_NAME = "homepage";
    private static final String CUSTOM_FONT_OPEN = "<font color=\"#96BA00\">";
    private static final String CUSTOM_FONT_END = "&nbsp;</font>";
    private static int currentTutorialStep;
    private static LiveVariable<String> categoryModeVariable = Optimizely.stringForKey(CATEGORY_MODE, CategoryGridAdapter.ICON_MODE);
    private ShowcaseHelper showcaseHelper;
    private int[] skipTutorialArea = new int[2];
    private int currentPage;
    private Timer swipeTimer;
    private ViewPager mPager;
    private NavigationViewUtil navigationViewUtil;

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_sell:
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_HOMEPAGE_ID, HOMEPAGE_AI, XitiUtils.NAVIGATION);
                    Intent intentInsert = new Intent(HomepageActivity.this, InsertAdActivity.class);
                    intentInsert.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentInsert);
                    break;
                case R.id.btn_find:
                    if (showcaseHelper != null) {
                        showcaseHelper.closeAllTutorials(false);
                    }
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_HOMEPAGE_ID, HOMEPAGE_FIND, XitiUtils.NAVIGATION);
                    Intent intent;
                    if ((Config.firstTimeUser && !Config.firstTimeUserAndChooseRegion) || !MudahUtil.isValidSession()) {
                        intent = new Intent(HomepageActivity.this, LaunchLocationActivity.class);
                    } else {
                        intent = new Intent(HomepageActivity.this, AdsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    }

                    startActivity(intent);
                    break;
                case R.id.recommended_ads:
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_HOMEPAGE_ID, HOMEPAGE_RECOMMEND, XitiUtils.NAVIGATION);
                    tagR4U();
                    Intent intentRecommendedAds = new Intent(HomepageActivity.this, WebViewActivity.class);
                    intentRecommendedAds.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentRecommendedAds.putExtra(WebViewActivity.EXTERNAL_URL, Config.mShareHost + Config.RECOMMEND_PAGE);
                    startActivity(intentRecommendedAds);
                    break;
                case R.id.skip_tutorial:
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_HOMEPAGE_ID, HOMEPAGE_SKIP_TUTORIAL, XitiUtils.NAVIGATION);
                    Config.skipAllTutorial = true;
                    hideSkipTutorialOption();
                    //hide Tutorial
                    if (showcaseHelper != null) {
                        showcaseHelper.skipAndClearAllTutorial();
                    }
                    break;
            }
        }
    };
    private Button buttonSell;

    private void tagR4U() {
        KahunaHelper.tagEvent(KahunaHelper.LAST_VIEW_R4U_PAGE);
        KahunaHelper.tagDateAttributes(KahunaHelper.VIEWING_R4U_PAGE);
        AmplitudeUtils.tagEvent(AmplitudeUtils.R4U_PV_EVENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();
        setContentView(R.layout.activity_homepage);
        setupActionBar();

        buttonSell = (Button) findViewById(R.id.btn_sell);
        buttonSell.setOnClickListener(clickListener);

        Button buttonFind = (Button) findViewById(R.id.btn_find);
        buttonFind.setOnClickListener(clickListener);

        skipTutorial = (TextView) findViewById(R.id.skip_tutorial);
        skipTutorial.setOnClickListener(clickListener);

        TextView subHeadline = (TextView) findViewById(R.id.sub_headline);
        subHeadline.setText(ACUtils.getHtmlFromString(CUSTOM_FONT_OPEN + Config.totalAdsCount + CUSTOM_FONT_END + getString(R.string.sub_headline)));

        if (Config.homepageRecommendedAds) {
            TextView recommendedAds = (TextView) findViewById(R.id.recommended_ads);
            recommendedAds.setVisibility(View.VISIBLE);
            recommendedAds.setOnClickListener(clickListener);
        }
        //show tutorial
        prepareTutorial();

        setupBanner();
        setupCategoryGrid();
        sendTag(HOMEPAGE_PAGE_NAME + Constants.UNDERSCORE + categoryModeVariable.get());
        if (Config.userAccount.isLogin() && !Config.userAccount.isSessionValid()) {
            Log.d("Token has expired, request a new one");
            getSupportLoaderManager().initLoader(LOADER_USER_SIGNIN, null, asyncAutoSignIn());
        }
    }

    private APILoader.Callbacks asyncAutoSignIn() {
        Log.d();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("email", Config.userAccount.getEmail());
        params.put("password", Config.userAccount.getPassword());
        params.put("source", SignInActivity.SIGNIN_SOURCE);

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "login", params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onUserAutoSignIn(data);
                getSupportLoaderManager().destroyLoader(loader.getId());
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);
                if (navigationViewUtil != null) {
                    navigationViewUtil.clearLoginData();
                }
                getSupportLoaderManager().destroyLoader(loader.getId());
            }
        };
    }

    protected void onUserAutoSignIn(JSONObject data) {
        String status = data.optString(Constants.STATUS);
        if (status.equals(Constants.OK)) {
            Config.userAccount.setLastSignIn((new Date()).getTime());
            Config.userAccount.setToken(data.optString("token"));
            Config.userAccount.saveUserDataPreferences(getApplicationContext());
            KahunaHelper.tagAttributes(KahunaHelper.PAGE_USER_ACCOUNT, KahunaHelper.LOGIN_STATUS, KahunaHelper.YES);

            if (Config.enableChat) {
                ChatCafe.logInChatCafe();
            }
        } else if (navigationViewUtil != null) {
            navigationViewUtil.clearLoginData();
        }
    }

    private void prepareTutorial() {
        if (Config.tutorialPagesAndSteps.containsKey(Config.TutorialPages.HOMEPAGE.toString())) {
            currentTutorialStep = Config.tutorialPagesAndSteps.get(Config.TutorialPages.HOMEPAGE.toString());
        } else {
            currentTutorialStep = 1;
        }

        if (!isSkipTutorial() && currentTutorialStep <= ShowcaseHelper.HOMEPAGE_TUTORIAL_STEPS) {
            listenForWhenButtonBecomesVisible();
        }
    }

    private void listenForWhenButtonBecomesVisible() {
        final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View findButton = findViewById(R.id.btn_chat);
                // This could be called when the button is not there yet, so we must test for null
                if (findButton != null) {
                    // Found it! Do what you need with the button
                    showTutorial();
                    // Now you can get rid of this listener
                    try {
                        if (Build.VERSION.SDK_INT < 16) {
                            getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    } catch (IllegalStateException illegal) {
                        ACUtils.debug(illegal);
                    }
                }
            }
        });
    }

    private void showTutorial() {
        int resourceId = 0;
        int tipTitle = 0;
        int tipDesc = 0;
        boolean isActionBarIcon = false;
        showcaseHelper = new ShowcaseHelper(HomepageActivity.this);

        while (currentTutorialStep <= ShowcaseHelper.HOMEPAGE_TUTORIAL_STEPS) {
            resourceId = 0;
            switch (currentTutorialStep) {
                case 1:
                    resourceId = R.id.btn_sell;
                    tipTitle = R.string.sell_title_tip;
                    tipDesc = R.string.sell_desc_tip;
                    break;
                case 2:
                    resourceId = R.id.btn_find;
                    tipTitle = R.string.buy_title_tip;
                    tipDesc = R.string.buy_desc_tip;
                    break;
                case 3:
                    resourceId = R.id.category_grid;
                    tipTitle = R.string.category_grid_title_tip;
                    tipDesc = R.string.category_grid_desc_tip;
                    break;
                case 4:
                    if (Config.homepageRecommendedAds) {
                        resourceId = R.id.recommended_ads;
                        tipTitle = R.string.recommend_title_tip;
                        tipDesc = R.string.recommend_desc_tip;
                    }
                    break;
                case 5:
                    if (!(getResources().getBoolean(R.bool.isTablet))) {
                        resourceId = R.id.material_menu_button;
                        tipTitle = R.string.menu_title_tip;
                        tipDesc = R.string.menu_desc_tip;
                        isActionBarIcon = true;
                    }
                    break;
                case 6:
                    resourceId = R.id.btn_chat;
                    tipTitle = R.string.chat_title_tip;
                    tipDesc = R.string.chat_desc_tip;
                    isActionBarIcon = true;
                    break;
            }

            if (resourceId != 0) {
                ShowcaseHelper.TutorialStepAndResource tutorialStepAndResource = showcaseHelper.new TutorialStepAndResource(Config.TutorialPages.HOMEPAGE, resourceId, isActionBarIcon);
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
            checkScrollToSellButtonBottom();
        }
    }

    //For small screen in landscape mode
    private void checkScrollToSellButtonBottom() {
        int location[] = new int[2];
        buttonSell.getLocationOnScreen(location);
        int btnSellBottomY = (buttonSell.getHeight() + location[1]);
        // btnSellBottomY > skipTutorialArea Y means the sell button is blocked by the 'skip all' button
        if (btnSellBottomY > 0 && btnSellBottomY > skipTutorialArea[1]) {
            NestedScrollView svHomepage = (NestedScrollView) findViewById(R.id.sv_homepage);
            if (svHomepage != null) {
                svHomepage.smoothScrollTo(0, (int) (buttonSell.getHeight() + buttonSell.getY()));
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Block all touch unless users click on "skip tutorial"
        if (showcaseHelper != null && showcaseHelper.getRemainingTutorialStep() >= 0
                && ev.getRawX() > skipTutorialArea[0]
                && ev.getRawY() > skipTutorialArea[1]) {
            skipTutorial.performClick();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setupBanner() {
        BannerFragmentAdapter mAdapter = new BannerFragmentAdapter(getSupportFragmentManager());
        mAdapter.setItems(Config.bannerImages);
        mPager = (ViewPager) findViewById(R.id.banner_pager);
        mPager.setAdapter(mAdapter);
        CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }

    private void swipeTimer(final ViewPager viewPager, final int totalImages) {
        currentPage = viewPager.getCurrentItem();
        //need to create a new instance, in case onPause() already canceled it.
        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentPage == totalImages) {
                            currentPage = 0;
                        }
                        viewPager.setCurrentItem(currentPage++, true);
                    }
                });
            }
        }, 500, 3000);
    }

    private void setupActionBar() {
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);
        showActionBarRedBorder(true);

        View view = findViewById(R.id.drawer_layout);
        if (view != null) {
            DrawerLayoutUtils drawerLayoutUtils = actionBar.createSlideInMenu(R.id.drawer_layout, R.id.left_drawer, ActionBarHelper.SHOW_MENU_BUTTON_ON_ACTIONBAR);
            setDrawerLayoutUtils(drawerLayoutUtils);
            navigationViewUtil = drawerLayoutUtils.getNavigationViewUtil();
        } else {
            actionBar.setDisplayHomeAsUpEnabled(false);
            navigationViewUtil = new NavigationViewUtil((NavigationView) findViewById(R.id.left_drawer), this);
        }
    }

    private void setupCategoryGrid() {
        RecyclerView categoryGrid = (RecyclerView) findViewById(R.id.category_grid);
        CategoryGridAdapter recyclerViewAdapter = new CategoryGridAdapter(getApplicationContext(), categoryModeVariable.get());
        int spanCount = getResources().getInteger(R.integer.category_grid_column_count); // 3 columns
        int spacing = MudahUtil.dpToPx(getResources().getInteger(R.integer.category_grid_column_spacing), this);
        boolean includeEdge = true;
        categoryGrid.addItemDecoration(new MarginDecoration(spanCount, spacing, includeEdge));
        categoryGrid.setHasFixedSize(true);
        categoryGrid.setLayoutManager(new GridLayoutManager(this, spanCount));
        categoryGrid.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d();
        if (Config.enableChat) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.home_menu, menu);
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
                    Intent intent = ChatCafe.redirectIntentToInBox(HomepageActivity.this);
                    startActivity(intent);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_ICON_ACTION_BAR, XitiUtils.NAVIGATION);
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void sendTag(String pageName) {
        XitiUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, this, pageName, XitiUtils.LEVEL2_HOMEPAGE_ID, null);
        TealiumHelper.tagTealiumPage(this, HOMEPAGE_APPLICATION, pageName, XitiUtils.LEVEL2_HOMEPAGE_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();
        if (mPager != null && Config.bannerImages != null && Config.bannerImages.length() > 1) {
            swipeTimer(mPager, Config.bannerImages.length());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d();
    }

    @Override
    public void onBackPressed() {
        // Do not inherit from super. Handle this separately
        if (getDrawerLayoutUtils() != null && getDrawerLayoutUtils().isMenuOpen()) {
            getDrawerLayoutUtils().setMenuClose();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (findViewById(R.id.content_layout) != null) {
            ACUtils.unbindDrawables(findViewById(R.id.content_layout));
        }
        System.gc();
        if (showcaseHelper != null) {
            showcaseHelper.releaseHostActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (swipeTimer != null) {
            swipeTimer.cancel();
        }
    }

    // handle when item in category grid is clicked
    @Override
    public void onItemClick(int position) {
        String categoryId = CategoryGridAdapter.aCatId[position];
        if (ACUtils.isEmpty(categoryId)) {
            Intent intent = new Intent(this, ACAdsSearchCategoryGroupChooser.class);
            intent.putExtra(REQUEST_FROM_HOMEPAGE, true);
            startActivity(intent);
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_HOMEPAGE_ID, HOMEPAGE_MORE, XitiUtils.NAVIGATION);
        } else {
            Intent intent = new Intent(this, AdsListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(AdsListActivity.FUNCTION_REQUEST, AdsListActivity.REQUEST_CATEGORY);
            String categoryName = CategoryGridAdapter.aCatName[position];

            ACReferences ref = ACReferences.getACReferences();
            ref.categoryId = categoryId;
            //send Tagging
            if (!ACUtils.isEmpty(categoryName)) {
                EventTrackingUtils.sendClick(XitiUtils.LEVEL2_HOMEPAGE_ID, PREFIX_HOMEPAGE + categoryName, XitiUtils.NAVIGATION);
            }
            startActivity(intent);
        }
    }
}
