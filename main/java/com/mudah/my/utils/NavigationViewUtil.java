package com.mudah.my.utils;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chatcafe.sdk.core.Cafe;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.BetaUsersActivity;
import com.mudah.my.activities.EmailSupportActivity;
import com.mudah.my.activities.HomepageActivity;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.activities.LaunchLocationActivity;
import com.mudah.my.activities.ListAdViewFavouritesActivity;
import com.mudah.my.activities.ListBookmarksActivity;
import com.mudah.my.activities.MudahBaseActivity;
import com.mudah.my.activities.PrivacyPolicyActivity;
import com.mudah.my.activities.SignInActivity;
import com.mudah.my.activities.SignUpActivity;
import com.mudah.my.activities.TermsActivity;
import com.mudah.my.activities.UserLiveAdsActivity;
import com.mudah.my.activities.UserProfileActivity;
import com.mudah.my.activities.WebViewActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.ChatCafe;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pin on 12/5/16.
 */
public class NavigationViewUtil {
    private static final int LOADER_LOGOUT = 0x04;
    private static final String ZERO = "0";
    private AppCompatActivity hostActivity;
    private boolean currentLoginStatus = false;

    private final View.OnClickListener MenuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.iv_user_image:
                    if (Config.userAccount.isLogin()) {
                        Intent intentUserImage = new Intent(hostActivity, UserProfileActivity.class);
                        intentUserImage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        hostActivity.startActivity(intentUserImage);
                    }
                    break;
                case R.id.iv_home:
                    Intent intent = new Intent(hostActivity, HomepageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    hostActivity.startActivity(intent);
                    break;
                case R.id.tv_sign_up_label:
                    userSignUp();
                    EventTrackingUtils.sendUAClick(TealiumHelper.DRAWER, TealiumHelper.EVENT_SIGNUP);
                    break;
                case R.id.tv_sign_in_label:
                    userSignIn();
                    EventTrackingUtils.sendUAClick(TealiumHelper.DRAWER, TealiumHelper.EVENT_SIGNIN);
                    break;
                case R.id.ll_user_layout:
                    if (Config.userAccount.isLogin()) {
                        Intent intentUserAdsList = new Intent(hostActivity, UserLiveAdsActivity.class);
                        intentUserAdsList.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        hostActivity.startActivity(intentUserAdsList);
                        EventTrackingUtils.sendUAClick(TealiumHelper.DRAWER, TealiumHelper.EVENT_LIVEADS);
                    }
                    break;
            }
        }
    };
    private ImageView userImage;
    private LinearLayout llMenuSignUp;
    private LinearLayout llUserLayout;
    private MenuItem llMenuBetaUsers;
    private MenuItem llMenuLogout;
    private TextView tvSignIn;
    private TextView tvSignUp;
    private View view = null;
    private TextView tvBookmark;
    private TextView tvAdWatch;
    private ProgressBar pbLoading;
    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {

        // This method will trigger on item Click of navigation menu
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            String level2Menu = XitiUtils.LEVEL2_MENU_ID;
            //Checking if the item is in checked state or not, if not make it in checked state
            if (menuItem.isChecked())
                menuItem.setChecked(false);
            else
                menuItem.setChecked(true);

            //Check to see which item was being clicked and perform appropriate action
            switch (menuItem.getItemId()) {
                case R.id.ll_menu_chat:
                    Intent intent = ChatCafe.redirectIntentToInBox(hostActivity);
                    hostActivity.startActivity(intent);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_ICON_NAVIGATION_DRAWER, XitiUtils.NAVIGATION);
                    break;
                case R.id.ll_menu_find:
                    EventTrackingUtils.sendClick(level2Menu, XitiUtils.MENU_FIND, XitiUtils.NAVIGATION);
                    Intent findIntent;

                    if ((Config.firstTimeUser && !Config.firstTimeUserAndChooseRegion) || !MudahUtil.isValidSession()) {
                        findIntent = new Intent(hostActivity, LaunchLocationActivity.class);
                    } else {
                        findIntent = new Intent(hostActivity, AdsListActivity.class);
                        findIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    }
                    hostActivity.startActivity(findIntent);
                    break;
                case R.id.ll_menu_insertad:
                    EventTrackingUtils.sendClick(level2Menu, XitiUtils.MENU_INSERTAD, XitiUtils.NAVIGATION);
                    Intent intentInsert = new Intent(hostActivity, InsertAdActivity.class);
                    intentInsert.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    hostActivity.startActivity(intentInsert);
                    break;
                case R.id.ll_menu_favorite:
                    EventTrackingUtils.sendClick(level2Menu, XitiUtils.MENU_FAV, XitiUtils.NAVIGATION);
                    Intent intentFavourite = new Intent(hostActivity, ListAdViewFavouritesActivity.class);
                    intentFavourite.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    hostActivity.startActivity(intentFavourite);
                    break;
                case R.id.ll_menu_bookmark:
                    EventTrackingUtils.sendClick(level2Menu, XitiUtils.MENU_BOOKMARK, XitiUtils.NAVIGATION);
                    Intent intentBookmarks = new Intent(hostActivity, ListBookmarksActivity.class);
                    intentBookmarks.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    hostActivity.startActivity(intentBookmarks);
                    break;
                case R.id.ll_menu_privacy:
                    Intent intentPrivacy = new Intent(hostActivity, PrivacyPolicyActivity.class);
                    intentPrivacy.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    hostActivity.startActivity(intentPrivacy);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CUST_SERVICE, XitiUtils.MENU_PRIVACY, XitiUtils.NAVIGATION);
                    break;
                case R.id.ll_menu_support:
                    Intent intentSupport = new Intent(hostActivity, EmailSupportActivity.class);
                    intentSupport.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    hostActivity.startActivity(intentSupport);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CUST_SERVICE, XitiUtils.MENU_SUPPORT, XitiUtils.NAVIGATION);
                    break;
                case R.id.ll_menu_terms:
                    Intent intentTerms = new Intent(hostActivity, TermsActivity.class);
                    intentTerms.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    hostActivity.startActivity(intentTerms);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CUST_SERVICE, XitiUtils.MENU_TERMS, XitiUtils.NAVIGATION);
                    break;
                case R.id.ll_menu_safely:
                    Intent intentSafely = new Intent(hostActivity, WebViewActivity.class);
                    intentSafely.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentSafely.putExtra(WebViewActivity.EXTERNAL_URL, Config.shareHost + Config.SHOP_SAFELY);
                    hostActivity.startActivity(intentSafely);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CUST_SERVICE, XitiUtils.MENU_SHOP_SAFE, XitiUtils.NAVIGATION);
                    break;
                case R.id.ll_menu_betauser:
                    Intent intentBetaUsers = new Intent(hostActivity, BetaUsersActivity.class);
                    intentBetaUsers.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    hostActivity.startActivity(intentBetaUsers);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_BETA_USER_SIGNUP, XitiUtils.MENU_BETA_USER, XitiUtils.NAVIGATION);
                    break;
                case R.id.ll_menu_logout:
                    signOut();
                    EventTrackingUtils.sendUAClick(TealiumHelper.DRAWER, TealiumHelper.EVENT_LOGOUT);
                    break;
            }
            return true;
        }
    };
    private View headerLayout;
    private TextView tvUnreadChat;

    public NavigationViewUtil(NavigationView navigationView, AppCompatActivity host) {
        hostActivity = host;

        //Initializing NavigationView
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
        navigationView.setItemIconTintList(null);
        navigationView.setItemTextColor(null);

        Menu menu = navigationView.getMenu();
        if (menu != null) {
            tvBookmark = (TextView) menu.findItem(R.id.ll_menu_bookmark).getActionView().findViewById(R.id.tv_counter);
            tvAdWatch = (TextView) menu.findItem(R.id.ll_menu_favorite).getActionView().findViewById(R.id.tv_counter);
            tvUnreadChat = (TextView) menu.findItem(R.id.ll_menu_chat).getActionView().findViewById(R.id.tv_counter);
        }

        headerLayout = navigationView.getHeaderView(0);
        tvSignIn = (TextView) headerLayout.findViewById(R.id.tv_sign_in_label);
        tvSignIn.setOnClickListener(MenuOnClickListener);

        tvSignUp = (TextView) headerLayout.findViewById(R.id.tv_sign_up_label);
        tvSignUp.setText(ACUtils.getHtmlFromString(hostActivity.getResources().getString(R.string.menu_item_signup_superscript)));
        tvSignUp.setOnClickListener(MenuOnClickListener);

        llMenuSignUp = (LinearLayout) headerLayout.findViewById(R.id.ll_signin_layout);
        llUserLayout = (LinearLayout) headerLayout.findViewById(R.id.ll_user_layout);
        llUserLayout.setOnClickListener(MenuOnClickListener);

        pbLoading = (ProgressBar) navigationView.findViewById(R.id.pb_logout_loading);
        userImage = (ImageView) headerLayout.findViewById(R.id.iv_user_image);
        userImage.setOnClickListener(MenuOnClickListener);
        ImageView homeImage = (ImageView) headerLayout.findViewById(R.id.iv_home);
        if (host instanceof HomepageActivity) {
            homeImage.setVisibility(View.GONE);
        } else {
            homeImage.setOnClickListener(MenuOnClickListener);
        }

        MenuItem chatMenu = menu.findItem(R.id.ll_menu_chat);
        llMenuLogout = menu.findItem(R.id.ll_menu_logout);
        llMenuBetaUsers = menu.findItem(R.id.ll_menu_betauser);
        if (!Config.enableChat) {
            chatMenu.setVisible(false);
        } else {
            chatMenu.setTitle(ACUtils.getHtmlFromString(hostActivity.getResources().getString(R.string.menu_item_chat)));
            chatMenu.setVisible(true);
        }
        checkUserMenuVisibility();
    }

    public void updateProfilePicture() {
        if (Config.userAccount.isLogin() && !(ACUtils.isEmpty(Config.userAccount.getAvatar()))) {
            Picasso.with(hostActivity.getApplicationContext()).load(Config.userAccount.getImageUrl()).into(userImage);
        } else {
            Picasso.with(hostActivity.getApplicationContext()).load(R.drawable.signin_usericon).into(userImage);
        }
    }

    public void updateUnreadChatMessage() {
        if (tvUnreadChat != null && hostActivity instanceof MudahBaseActivity) {
            if (Config.badgeUnreadChatNumber > 0) {
                tvUnreadChat.setText(Config.badgeUnreadChatNumber + Constants.EMPTY_STRING);
                tvUnreadChat.setVisibility(View.VISIBLE);
            } else {
                tvUnreadChat.setVisibility(View.GONE);
            }
        }
    }

    public void updateListNumber() {
        if (Config.userAccount.isLogin() && Config.enableChat) {
            updateUnreadChatMessage();
        }

        if (Config.allFavouritAdIds.size() > 0) {
            tvAdWatch.setText(String.format("%s", Config.allFavouritAdIds.size()));
            tvAdWatch.setVisibility(View.VISIBLE);
        } else {
            tvAdWatch.setVisibility(View.GONE);
        }

        if (Config.bookmarkTotal > 0) {
            tvBookmark.setText(String.format("%s", Config.bookmarkTotal));
            tvBookmark.setVisibility(View.VISIBLE);
        } else {
            tvBookmark.setVisibility(View.GONE);
        }
    }

    public void checkBetaUserSignUpVisibility() {
        if (Config.betaUserSignUp)
            llMenuBetaUsers.setVisible(true);
        else
            llMenuBetaUsers.setVisible(false);
    }

    public void checkUserMenuVisibility() {
        currentLoginStatus = Config.userAccount.isLogin();
        if (currentLoginStatus) {
            llMenuSignUp.setVisibility(View.GONE);
            llUserLayout.setVisibility(View.VISIBLE);
            llMenuLogout.setVisible(true);
            if (hostActivity != null && userImage != null)
                updateProfilePicture();
            if (!ACUtils.isEmpty(Config.userAccount.getEmail())) {
                TextView tvUserEmail = (TextView) headerLayout.findViewById(R.id.tv_user_email);
                tvUserEmail.setText(Config.userAccount.getEmail());
            }

            updateUserTotalAds();
        } else {
            llMenuSignUp.setVisibility(View.VISIBLE);
            llUserLayout.setVisibility(View.GONE);
            llMenuLogout.setVisible(false);
            if (hostActivity != null && userImage != null)
                updateProfilePicture();
        }
    }

    public boolean getCurrentLoginStatus() {
        return currentLoginStatus;
    }

    public void setCurrentLoginStatus(boolean status) {
        currentLoginStatus = status;
    }

    public void updateUserTotalAds() {
        TextView tvUserTotalAds = (TextView) headerLayout.findViewById(R.id.tv_total_ads);
        if (tvUserTotalAds != null) {
            String totalAds = ZERO;
            if (!ACUtils.isEmpty(Config.userAccount.getTotalAds())) {
                totalAds = Config.userAccount.getTotalAds();
            }
            tvUserTotalAds.setText(hostActivity.getString(R.string.menu_item_user_total_ads, totalAds));
        }
    }

    private void userSignUp() {
        Intent intentSignUp = new Intent(hostActivity, SignUpActivity.class);
        intentSignUp.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        hostActivity.startActivity(intentSignUp);
    }

    private void userSignIn() {
        Intent intentSignIn = new Intent(hostActivity, SignInActivity.class);
        intentSignIn.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        hostActivity.startActivity(intentSignIn);
    }

    public void signOut() {
        hostActivity.getSupportLoaderManager().initLoader(LOADER_LOGOUT, null, asyncSignOut());
    }

    private APILoader.Callbacks asyncSignOut() {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.GET, "logout/" + Config.userAccount.getToken(), params, hostActivity) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d();
                Log.d("Success to request API, data: " + data);
                onUserLogout(data);
                hostActivity.getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(hostActivity,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                hostActivity.getLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onUserLogout(JSONObject data) {
        String status = data.optString(Constants.STATUS);
        String message = data.optString(Constants.MSG);

        if (status.equals(Constants.OK)) {
            clearLoginData();

            Toast.makeText(hostActivity,
                    R.string.user_account_sign_out,
                    Toast.LENGTH_LONG).show();

            Intent intentHomepage = new Intent(hostActivity, HomepageActivity.class);
            intentHomepage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            hostActivity.startActivity(intentHomepage);
        } else {
            Toast.makeText(hostActivity,
                    message,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void clearLoginData() {
        Log.d();
        Config.userAccount.clearDataOnLogout(hostActivity.getApplicationContext());
        KahunaHelper.tagAttributes(KahunaHelper.PAGE_USER_ACCOUNT, KahunaHelper.LOGIN_STATUS, KahunaHelper.NO);

        if (hostActivity instanceof MudahBaseActivity) {
            ((MudahBaseActivity) hostActivity).resetUnreadNumber();
            updateUnreadChatMessage();
        }

        if (Config.enableChat) {
            Cafe.logOut();
            ServerUtils.deleteDevice(Config.deviceId);
            Config.badgeUnreadChatNumber = -1;
        }

        checkUserMenuVisibility();
    }
}
