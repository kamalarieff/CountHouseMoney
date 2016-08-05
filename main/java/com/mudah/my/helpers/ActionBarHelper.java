package com.mudah.my.helpers;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuView;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.mudah.my.utils.NavigationViewUtil;
import com.squareup.picasso.Picasso;

/**
 * Created by w_ongkl on 4/20/15.
 */
public class ActionBarHelper {

    public static final boolean HIDE_MENU_BUTTON_ON_ACTIONBAR = true;
    public static final boolean SHOW_MENU_BUTTON_ON_ACTIONBAR = false;

    private DrawerLayoutUtils drawerLayoutUtils;
    private AppCompatActivity actionBarActivity;
    private View rootView;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private FrameLayout flMaterialMenuContainer;
    private MaterialMenuView materialMenu;
    private ImageView ivLogo;
    private TextView tvMainTitle;
    private LinearLayout toolbarTextContainer;
    private TextView tvTitle;
    private TextView tvHeadTitle;
    private TextView tvSubtitle;
    private TextView tvFirstTitle;// used in multiple line titles
    private TextView tvSecondTitle;// used in multiple line titles
    private TextView tvThirdTitle;
    private ImageView imgThumb;
    private ActionBarClickListener listener;
    private boolean showBack;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.material_menu_button:
                    listener.onMenuClickListener(showBack);
                    break;
                case R.id.toolbar_ll_text_container:
                    listener.onContainerClickListener();
                    break;
                case R.id.toolbar_tv_title:
                    listener.onTitleClickListener();
                    break;
                case R.id.toolbar_tv_subtitle:
                    listener.onSubtitleClickListener();
                    break;
            }
        }
    };

    public ActionBarHelper(AppCompatActivity activity) {
        actionBarActivity = activity;
    }

    public void createActionBar(int actionbarRes, String title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (Toolbar) actionBarActivity.findViewById(actionbarRes);
        } else {
            rootView = actionBarActivity.findViewById(actionbarRes);
            toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        }

        flMaterialMenuContainer = (FrameLayout) toolbar.findViewById(R.id.fl_material_menu_container);
        toolbarTextContainer = (LinearLayout) toolbar.findViewById(R.id.toolbar_ll_text_container);
        tvMainTitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_main_title);
        // Activate 3 bars menu
        // put try/catch to prevent crash by NoClassDefFoundError
        // in case the fix that we put in the proguard-rules.pro does not work
        try {
            actionBarActivity.setSupportActionBar(toolbar);
            actionBar = actionBarActivity.getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

            flMaterialMenuContainer.setVisibility(View.GONE);
            toolbarTextContainer.setVisibility(View.GONE);
            tvMainTitle.setVisibility(View.GONE);

            if (ACUtils.isEmpty(title)) {
                ivLogo = (ImageView) toolbar.findViewById(R.id.toolbar_iv_logo);
                ivLogo.setVisibility(View.VISIBLE);
            } else {
                tvMainTitle.setText(title);
                tvMainTitle.setVisibility(View.VISIBLE);
            }
        } catch (Throwable throwable) {
            ACUtils.debug(throwable);
        }

    }

    public void createActionBar(int actionbarRes, String title, String subTitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (Toolbar) actionBarActivity.findViewById(actionbarRes);
        } else {
            rootView = actionBarActivity.findViewById(actionbarRes);
            toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        }

        flMaterialMenuContainer = (FrameLayout) toolbar.findViewById(R.id.fl_material_menu_container);
        toolbarTextContainer = (LinearLayout) toolbar.findViewById(R.id.toolbar_ll_text_container);
        tvHeadTitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_title);
        tvSubtitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_subtitle);


        // Activate 3 bars menu
        // put try/catch to prevent crash by NoClassDefFoundError
        // in case the fix that we put in the proguard-rules.pro does not work
        try {
            actionBarActivity.setSupportActionBar(toolbar);
            actionBar = actionBarActivity.getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            flMaterialMenuContainer.setVisibility(View.GONE);
            toolbarTextContainer.setVisibility(View.VISIBLE);
            tvHeadTitle.setText(title);
            tvSubtitle.setText(subTitle);

        } catch (Exception exception) {
            Log.e(exception);
        }

    }

    public void createChatActionBar(int actionbarRes, View.OnClickListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (Toolbar) actionBarActivity.findViewById(actionbarRes);
        } else {
            rootView = actionBarActivity.findViewById(actionbarRes);
            toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        }

        RelativeLayout multipleLinesContainer = (RelativeLayout) toolbar.findViewById(R.id.toolbar_ll_multiple_lines);
        tvFirstTitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_top_title);
        tvSecondTitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_second_title);
        tvThirdTitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_third_title);
        imgThumb = (ImageView) toolbar.findViewById(R.id.toolbar_thumb);
        // Activate 3 bars menu
        // put try/catch to prevent crash by NoClassDefFoundError
        // in case the fix that we put in the proguard-rules.pro does not work
        try {
            actionBarActivity.setSupportActionBar(toolbar);
            actionBar = actionBarActivity.getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            multipleLinesContainer.setOnClickListener(listener);

        } catch (Throwable throwable) {
            ACUtils.debug(throwable);
        }

    }

    public void setMainTitle(String title) {
        tvMainTitle.setText(title);
    }

    public void configureActionbarForSearch(boolean initial) {
        actionBar.setDisplayHomeAsUpEnabled(false);
        toolbar.setLogo(null);

        if (initial && toolbar != null) {

            showBack = false;

            tvMainTitle.setVisibility(View.GONE);

            toolbarTextContainer.setVisibility(View.VISIBLE);
            toolbarTextContainer.setOnClickListener(onClickListener);
            flMaterialMenuContainer.setVisibility(View.VISIBLE);

            ivLogo = (ImageView) toolbar.findViewById(R.id.toolbar_iv_logo);
            ivLogo.setVisibility(View.VISIBLE);

            materialMenu = (MaterialMenuView) toolbar.findViewById(R.id.material_menu_button);
            materialMenu.setVisibility(View.VISIBLE);
            materialMenu.setOnClickListener(onClickListener);

            tvTitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_title);
            tvTitle.setOnClickListener(onClickListener);

            tvSubtitle = (TextView) toolbar.findViewById(R.id.toolbar_tv_subtitle);
            tvSubtitle.setOnClickListener(onClickListener);

        }

    }

    public void setHamBurgerVisible(int actionbarRes) {
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayOptions(0, actionBar.DISPLAY_USE_LOGO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (Toolbar) actionBarActivity.findViewById(actionbarRes);
        } else {
            rootView = actionBarActivity.findViewById(actionbarRes);
            toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        }

        ivLogo = (ImageView) toolbar.findViewById(R.id.toolbar_iv_logo);
        ivLogo.setVisibility(View.VISIBLE);
        flMaterialMenuContainer = (FrameLayout) toolbar.findViewById(R.id.fl_material_menu_container);
        flMaterialMenuContainer.setVisibility(View.VISIBLE);
        materialMenu = (MaterialMenuView) toolbar.findViewById(R.id.material_menu_button);
        materialMenu.setVisibility(View.VISIBLE);
        materialMenu.setOnClickListener(onClickListener);
    }

    public void setMenuButton(MaterialMenuDrawable.IconState iconState, boolean animate) {
        if (animate) {
            materialMenu.setTransformationDuration(500);
            materialMenu.animateState(iconState);
        } else {
            materialMenu.setTransformationDuration(0);
            materialMenu.setState(iconState);
        }
        if (iconState == MaterialMenuDrawable.IconState.BURGER) {
            showBack = false;
        } else if (iconState == MaterialMenuDrawable.IconState.ARROW) {
            showBack = true;
        }
    }

    public void setProductTitles(Context context, String imgUrl, String title, String subTitle, String thirdTitle) {
        tvFirstTitle.setText(title);
        tvSecondTitle.setText(subTitle);
        tvThirdTitle.setText(thirdTitle);

        if (ACUtils.isEmpty(imgUrl)) {
            Picasso.with(context).load(R.drawable.cat_others).fit().centerCrop().into(imgThumb);
        } else {
            Picasso.with(context).load(imgUrl).fit().centerCrop().into(imgThumb);
        }
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
        toolbar.setTitle(Constants.EMPTY_STRING);
        toolbar.setSubtitle(Constants.EMPTY_STRING);
    }

    public void setSubtitle(String subtitle) {
        tvSubtitle.setText(subtitle);
        toolbar.setTitle(Constants.EMPTY_STRING);
        toolbar.setSubtitle(Constants.EMPTY_STRING);
    }

    public void clearTitle() {
        toolbar.setTitle(Constants.EMPTY_STRING);
        tvTitle.setText(Constants.EMPTY_STRING);
    }

    public void clearSubtitle() {
        toolbar.setSubtitle(Constants.EMPTY_STRING);
        tvSubtitle.setText(Constants.EMPTY_STRING);
    }

    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
    }

    public void enableLogo(boolean showLogo) {
        if (showLogo) {
            ivLogo.setVisibility(View.VISIBLE);
            ivLogo.setImageDrawable(actionBarActivity.getResources().getDrawable(R.drawable.logo_white));
        } else {
            ivLogo.setVisibility(View.GONE);
        }
    }

    public boolean isLogoEnable() {
        return ivLogo.isShown();
    }

    public void setActionBarClickListener(ActionBarClickListener listener) {
        this.listener = listener;
    }

    public void createActionBar(int actionbarRes) {
        createActionBar(actionbarRes, null);
    }

    public void createActionBarWithTitle(int actionbarRes, String title) {
        createActionBar(actionbarRes, title);
    }

    public DrawerLayoutUtils createSlideInMenu(int drawerLayoutRes, int leftDrawerRes, boolean hideMenuButtonOnActionbar) {

        DrawerLayout mDrawerLayout = (DrawerLayout) actionBarActivity.findViewById(drawerLayoutRes);
        NavigationViewUtil navigationViewUtil = new NavigationViewUtil((NavigationView) actionBarActivity.findViewById(leftDrawerRes), actionBarActivity);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayoutUtils = new DrawerLayoutUtils(navigationViewUtil,
                actionBarActivity,     /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        if (hideMenuButtonOnActionbar) {
            actionBar.setHomeButtonEnabled(false);
            drawerLayoutUtils.setDrawerIndicatorEnabled(false);
        }

        return drawerLayoutUtils;
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public interface ActionBarClickListener {
        void onMenuClickListener(boolean backEnabled);

        void onTitleClickListener();

        void onSubtitleClickListener();

        void onContainerClickListener();
    }
}
