package com.mudah.my.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import com.lib701.utils.ACUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.EditUserProfileActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.helpers.ShowcaseHelper;

/**
 * Created by moehninhtwee on 30/12/14.
 */
public class DrawerLayoutUtils extends ActionBarDrawerToggle {
    private static int currentTutorialStep;
    private DrawerLayout mDrawerLayout;
    private NavigationViewUtil navigationViewUtil;
    private Activity activity;
    private ShowcaseHelper showcaseHelper;
    //to differentiate between clicking or swiping and prevent sending tracking to Xiti twice.
    // When click to navigate, it will trigger onPageChange which is similar to swiping
    private boolean clickToOpen = false;

    public DrawerLayoutUtils(NavigationViewUtil newNavigationViewUtil, Activity a, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(a, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.addDrawerListener(this);
        navigationViewUtil = newNavigationViewUtil;
        activity = a;
    }

    public NavigationViewUtil getNavigationViewUtil() {
        return navigationViewUtil;
    }

    public void removeDrawerListener() {
        if (mDrawerLayout != null) {
            mDrawerLayout.removeDrawerListener(this);
        }
    }

    public void setMenuOpen() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            clickToOpen = true;
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_MENU_ID, XitiUtils.CLICK_MENU, XitiUtils.NAVIGATION);
        }
    }

    public void setMenuClose() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        if (showcaseHelper != null) {
            showcaseHelper.releaseHostActivity();
        }
    }

    public boolean isMenuOpen() {
        boolean drawerOpen = false;
        if (mDrawerLayout != null)
            drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.START);
        return drawerOpen;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        //Trying to close the soft keyboard if it opens while closing the menu
        //We can safely ignore this if there is any error happen
        try {
            InputMethodManager im = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            ACUtils.debug(e);
        }
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        if (showcaseHelper != null) {
            showcaseHelper.releaseHostActivity();
        }
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        if (navigationViewUtil != null) {
            navigationViewUtil.updateListNumber();
            navigationViewUtil.checkBetaUserSignUpVisibility();
            navigationViewUtil.updateUserTotalAds();
            // If users log into chat from AdView, when navigate back to Listing page
            // need to update the user header section as logged in
            if (!navigationViewUtil.getCurrentLoginStatus() && Config.userAccount.isLogin()){
                navigationViewUtil.checkUserMenuVisibility();
                navigationViewUtil.setCurrentLoginStatus(true);
            } else if (Config.userAccount.isLogin() && EditUserProfileActivity.isUpdatedProfileRefreshMenu) {
                navigationViewUtil.updateProfilePicture();
                EditUserProfileActivity.isUpdatedProfileRefreshMenu = false; //set back to false
            }
        }

        if (!clickToOpen) {
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_MENU_ID, XitiUtils.SWIPE_MENU, XitiUtils.NAVIGATION);
        } else
            clickToOpen = false;//reset
        if (Config.userAccount.isLogin())
            prepareTutorial();
    }

    public void prepareTutorial() {
        if (Config.tutorialPagesAndSteps.containsKey(Config.TutorialPages.NAVIGATION.toString())) {
            currentTutorialStep = Config.tutorialPagesAndSteps.get(Config.TutorialPages.NAVIGATION.toString());
        } else {
            currentTutorialStep = 1;
        }
        if (currentTutorialStep <= ShowcaseHelper.NAVIGATION_TUTORIAL_STEPS) {
            listenForWhenButtonBecomesVisible();
        }
    }

    private void listenForWhenButtonBecomesVisible() {
        final ViewTreeObserver viewTreeObserver = activity.getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View userProfile = activity.findViewById(R.id.iv_user_image);
                // This could be called when the button is not there yet, so we must test for null
                if (userProfile != null) {
                    // Found it! Do what you need with the button
                    showTutorial();
                    // Now you can get rid of this listener
                    try {
                        if (Build.VERSION.SDK_INT < 16) {
                            activity.getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    } catch (IllegalStateException illegal) {
                        ACUtils.debug(illegal);
                    }
                }
            }
        });
    }

    private void showTutorial() {
        showcaseHelper = new ShowcaseHelper(activity);
        while (currentTutorialStep <= ShowcaseHelper.NAVIGATION_TUTORIAL_STEPS) {
            int resourceId = R.id.iv_user_image;

            if (resourceId != 0) {
                ShowcaseHelper.TutorialStepAndResource tutorialStepAndResource = showcaseHelper.new TutorialStepAndResource(Config.TutorialPages.NAVIGATION, resourceId, true);
                tutorialStepAndResource.setStep(currentTutorialStep);
                tutorialStepAndResource.setTitleResource(R.string.profile_title_tip);
                tutorialStepAndResource.setDescriptionResource(R.string.profile_desc_tip);
                tutorialStepAndResource.setHideOnTouchOutside(true);
                showcaseHelper.declareSupportedTutorial(tutorialStepAndResource);
            }
            currentTutorialStep++;

        }
        showcaseHelper.showTutorial();
    }
}
