package com.mudah.my.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.fragments.ListAdViewFavouritesFragment;
import com.mudah.my.fragments.ListAdViewFavouritesFragment.ListMode;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.tealium.library.Tealium;

import java.util.Map;

public class ListAdViewFavouritesActivity extends MudahBaseActivity {
    private static final String FAVOURITE_PAGE = "My Favourites Listing";
    private Button actionButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_adview_favorites);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);
        DrawerLayoutUtils drawerLayoutUtils = actionBar.createSlideInMenu(R.id.drawer_layout, R.id.left_drawer, ActionBarHelper.SHOW_MENU_BUTTON_ON_ACTIONBAR);
        setDrawerLayoutUtils(drawerLayoutUtils);

        actionButton = (Button) findViewById(R.id.btn_adview_fav_action);
        cancelButton = (Button) findViewById(R.id.btn_fav_cancel);
        sendTagging();

    }

    private void sendTagging() {
        XitiUtils.sendTag(this, FAVOURITE_PAGE, XitiUtils.LEVEL2_FAVOURITE_ID, null);
        tagTealium(XitiUtils.LEVEL2_FAVOURITE_ID);
    }

    private void tagTealium(String level2) {
        Map<String, String> tealiumData = Tealium.map(
                TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_FAV,
                TealiumHelper.PAGE_NAME, FAVOURITE_PAGE,
                TealiumHelper.FAVOURITES_TYPE, TealiumHelper.FAVOURITES_ADS,
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

    public void cancelFavClickHandle(View v) {
        ListAdViewFavouritesFragment listAdViewFavouritesFragment = (ListAdViewFavouritesFragment) getSupportFragmentManager().findFragmentById(R.id.f_adview_favourites);
        if (listAdViewFavouritesFragment != null)
            listAdViewFavouritesFragment.updateViewMode(ListMode.VIEW);
    }

    public void actionClickHandle(View v) {
        ListAdViewFavouritesFragment listAdViewFavouritesFragment = (ListAdViewFavouritesFragment) getSupportFragmentManager().findFragmentById(R.id.f_adview_favourites);
        if (listAdViewFavouritesFragment != null) {
            if (listAdViewFavouritesFragment.getViewMode() == ListMode.VIEW)
                finish();
            else
                listAdViewFavouritesFragment.deleteSelectedItem();
        }
    }

}
