package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.fragments.ListBookmarksFragment;
import com.mudah.my.fragments.ListBookmarksFragment.ListMode;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;
import com.tealium.library.Tealium;

import java.util.Map;

public class ListBookmarksActivity extends MudahBaseActivity {
    private Button actionButton;
    private Button cancelButton;

    private boolean fromBookmarkNotification = false;
    private static final String BOOKMARK_PAGE = "Bookmark Search Listing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_bookmarks);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);
        DrawerLayoutUtils drawerLayoutUtils = actionBar.createSlideInMenu(R.id.drawer_layout, R.id.left_drawer, ActionBarHelper.SHOW_MENU_BUTTON_ON_ACTIONBAR);
        setDrawerLayoutUtils(drawerLayoutUtils);

        actionButton = (Button) findViewById(R.id.btn_bookmark_action);
        cancelButton = (Button) findViewById(R.id.btn_bookmark_cancel);
        // to enable 'Back' function
        sendTagging();

        //Remove bookmark notification
        MudahUtil.clearNotificationsByID(getBaseContext(), Config.NOTIFICATION_BOOKMARK_ID);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                if (bundle.containsKey(Config.BOOKMARK_MAIN_NOTIFICATION)) {
                    fromBookmarkNotification = bundle.getBoolean(Config.BOOKMARK_MAIN_NOTIFICATION);
                    if (XitiUtils.initFromLastConfig(getApplicationContext()) == null) {
                        Log.e("Xiti initialization is wrong");
                    } else {
                        EventTrackingUtils.sendCampaign(this, XitiUtils.CAMPAIGN_NOTIFICATION_BOOKMARK, XitiUtils.CAMPAIGN_CLICK);

                    }
                    Log.d("BookmarkNotification redirected from BookmarkNotification");
                }
            }
        }

    }

    private void sendTagging() {
        String level2Bookmark = XitiUtils.getLevel2Map(XitiUtils.LEVEL2_BOOKMARK);
        if (!ACUtils.isEmpty(level2Bookmark)) {
            XitiUtils.sendTag(this, BOOKMARK_PAGE, level2Bookmark, null);
            tagTealium(level2Bookmark);
        }
    }

    private void tagTealium(String level2){
        Map<String, String> tealiumData = Tealium.map(
                TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_SAVED_SEARCHES,
                TealiumHelper.PAGE_NAME, BOOKMARK_PAGE,
                TealiumHelper.FAVOURITES_TYPE, TealiumHelper.FAVOURITES_SEARCHES,
                TealiumHelper.XTN2, level2);

        TealiumHelper.track(this, tealiumData, Tealium.VIEW);
    }

    public void updateActionButtonText(ListMode listMode) {
        if (actionButton != null) {
            switch (listMode) {
                case VIEW:
                    actionButton.setText(getString(R.string.close));
                    cancelButton.setVisibility(View.GONE);
                    break;
                default:
                    actionButton.setText(getString(R.string.delete));
                    cancelButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void cancelClickHandle(View v) {
        ListBookmarksFragment listBookmarksFragment = (ListBookmarksFragment) getSupportFragmentManager().findFragmentById(R.id.f_bookmark);
        if (listBookmarksFragment != null)
            listBookmarksFragment.updateViewMode(ListMode.VIEW);
    }

    public void actionClickHandle(View v) {
        ListBookmarksFragment listBookmarksFragment = (ListBookmarksFragment) getSupportFragmentManager().findFragmentById(R.id.f_bookmark);
        if (listBookmarksFragment != null) {
            if (listBookmarksFragment.getViewMode() == ListMode.VIEW)
                finish();
            else
                listBookmarksFragment.deleteSelectedItem();
        }
    }

    @Override
    public void onBackPressed() {
        if (getDrawerLayoutUtils() != null) {
            if (getDrawerLayoutUtils().isMenuOpen()) {
                getDrawerLayoutUtils().setMenuClose();
            }
            else {
                if (isTaskRoot() | fromBookmarkNotification) {
                    // To make sure that AdsListActivity is always the last view that user see before exiting app
                    Intent intent = new Intent(ListBookmarksActivity.this, AdsListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                else {
                    finish();
                }
            }
        } else
            finish();
    }
    
}
