package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.chatcafe.sdk.tool.RoomListBaseAdapter;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.fragments.DeleteRoomDialogFragment;
import com.mudah.my.fragments.InboxFragment;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.models.ChatCafe;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.mudah.my.utils.EventTrackingUtils;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends MudahBaseActivity {
    private static String ALL = "ALL";
    private static String BUY_FROM = "BUY FROM";
    private ViewPager viewPager;
    private PagerAdapter fragmentPagerAdapter;
    private ViewPager.OnPageChangeListener setOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            InboxFragment inboxFragment = (InboxFragment) fragmentPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
            if (inboxFragment != null) {
                inboxFragment.forceHiddenDeleteRoom();
                inboxFragment.refreshRoom();
                String pageTitle = viewPager.getAdapter().getPageTitle(position).toString();
                if (ALL.equalsIgnoreCase(pageTitle)) {
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_WITH_ALL, XitiUtils.NAVIGATION);
                } else if (BUY_FROM.equalsIgnoreCase(pageTitle)) {
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_WITH_SELLER, XitiUtils.NAVIGATION);
                } else {
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_WITH_BUYER, XitiUtils.NAVIGATION);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public void onRetrieveDelete(DeleteRoomDialogFragment.StatusDelete statusDelete) {
        try {
            InboxFragment inboxFragment = (InboxFragment) fragmentPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
            if (inboxFragment != null) {
                inboxFragment.onDelete(statusDelete.isStatusDelete());
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Config.userAccount.isLogin()) {
            Intent intent = ChatCafe.redirectIntentToInBox(this);
            startActivity(intent);
        }
        //In case, users click on in-app bar or delete the chat, unread number will not be updated correctly
        //Reset this so that the number can be synced with API again.
        resetBadgeUnreadChatNumber();
        setContentView(R.layout.activity_inbox);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar, getApplicationContext().getString(R.string.chat_title));

        DrawerLayoutUtils drawerLayoutUtils = actionBar.createSlideInMenu(R.id.drawer_layout, R.id.left_drawer, ActionBarHelper.SHOW_MENU_BUTTON_ON_ACTIONBAR);
        setDrawerLayoutUtils(drawerLayoutUtils);
        showActionBarRedBorder(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(setOnPageChangeListener);
        TealiumHelper.tagTealiumPage(InboxActivity.this, TealiumHelper.APPLICATION_CHAT, TealiumHelper.INBOX, XitiUtils.LEVEL2_CHAT_ID);
        EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getApplicationContext(), TealiumHelper.INBOX, XitiUtils.LEVEL2_CHAT_ID, null);
    }

    private void setupViewPager(ViewPager viewPager) {
        fragmentPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        fragmentPagerAdapter.addFragment(InboxFragment.newInstance(RoomListBaseAdapter.FILTER_ALL), getString(R.string.chat_room_tab_all));
        fragmentPagerAdapter.addFragment(InboxFragment.newInstance(RoomListBaseAdapter.FILTER_BUY), getString(R.string.chat_room_tab_buyer));
        fragmentPagerAdapter.addFragment(InboxFragment.newInstance(RoomListBaseAdapter.FILTER_SELL), getString(R.string.chat_room_tab_seller));
        viewPager.setAdapter(fragmentPagerAdapter);
    }

    public void setDisplayDialogDelete() {
        FragmentManager fm = getSupportFragmentManager();
        DeleteRoomDialogFragment dialog = new DeleteRoomDialogFragment();
        dialog.setRetainInstance(true);
        dialog.show(fm, getApplicationContext().getString(R.string.chat_room_delete));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inbox_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_delete:
                EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_DELETE_ICON, XitiUtils.NAVIGATION);
                InboxFragment inboxFragment = (InboxFragment) fragmentPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
                if (inboxFragment != null && !inboxFragment.isDeleteChatRoom() && inboxFragment.getNumberOfRooms() > 0) {
                    inboxFragment.setShowDeleteChatRoom();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static class PagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<Fragment>();
        private final List<String> mFragmentTitles = new ArrayList<String>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
