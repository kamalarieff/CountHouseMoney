package com.mudah.my.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatcafe.sdk.core.CCConstant;
import com.chatcafe.sdk.core.CCRoom;
import com.chatcafe.sdk.core.CCSocket;
import com.chatcafe.sdk.core.CCUser;
import com.comscore.analytics.comScore;
import com.devspark.appmsg.AppMsg;
import com.google.android.gms.appindexing.AndroidAppUri;
import com.kahuna.sdk.Kahuna;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.adapters.InsertAdCategoryParamsAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.helpers.AnimationHelper;
import com.mudah.my.helpers.ShowcaseHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.models.ChatCafe;
import com.mudah.my.utils.DrawerLayoutUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.GravityUtils;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.widgets.ChatMessageAlertView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by w_ongkl on 4/28/15.
 */
public class MudahBaseActivity extends AppCompatActivity {
    private static final String CHAT_UNREAD_PLUS = "99+";
    private static final String PRIVATE = "0";
    private static final String COMPANY = "1";
    private static final String WITH_DATA = "_With_Data";
    public TextView badgeTotalUnreadChatMsg;
    public TextView skipTutorial;
    protected CCRoom currentCCRoom;
    private DrawerLayoutUtils drawerLayoutUtils;
    private String activityUniqueKey = System.currentTimeMillis() + Constants.EMPTY_STRING;
    private CCConstant.CCResultCallback<JSONObject> messageCallback = new CCConstant.CCResultCallback<JSONObject>() {
        @Override
        public void onComplete(@Nullable JSONObject result, @Nullable String error) {
            try {
                if (result != null && !isMyMessage(result)) {
                    TealiumHelper.tagTealiumReceiveMessage(MudahBaseActivity.this, result);
                    if (!isOnChatRoom(result) && !result.optBoolean("mute", false)) {
                        showInAppMessage(result);
                    }
                }
            } catch (JSONException e) {
                Log.e(e.toString());
            }
        }
    };
    private CCConstant.CCResultCallback<Integer> unreadBadgeCallback = new CCConstant.CCResultCallback<Integer>() {
        @Override
        public void onComplete(@Nullable Integer result, @Nullable String error) {
            if (result != null && result > 0) {
                threadRunBadge(result);
            } else {
                threadRunBadge(0);
            }
        }
    };

    public int getUnreadNumber() {
        return Config.badgeUnreadChatNumber;
    }

    public void resetUnreadNumber() {
        threadRunBadge(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnimationHelper animate = new AnimationHelper(this, AnimationHelper.ACTIVITY_TRANSITION_OPEN);
        animate.run();
        GravityUtils.init();
        tagReferral();
        if (XitiUtils.init(getApplicationContext()) == null) {
            Log.e("Xiti initialization is wrong");
        }
    }

    public void showActionBarRedBorder(boolean show) {
        View redBorderView = findViewById(R.id.red_border_view);
        View gradientView = findViewById(R.id.gradient_view);
        if (show) {
            if (redBorderView != null) {
                redBorderView.setVisibility(View.VISIBLE);
            }
            if (gradientView != null) {
                gradientView.setVisibility(View.GONE);
            }
        } else {
            if (redBorderView != null) {
                redBorderView.setVisibility(View.GONE);
            }
            if (gradientView != null) {
                gradientView.setVisibility(View.GONE);
            }
        }
    }

    private void tagReferral() {
        Uri referrerUri = this.getReferrer();
        Log.d("ReferrerUri :" + referrerUri);
        Config.isGoogleAppCrawler = false;
        if (referrerUri != null) {
            String level2DeepLink = XitiUtils.getLevel2Map(XitiUtils.LEVEL2_APP_DEEPLINK);
            if (referrerUri.getScheme().equals("http") || referrerUri.getScheme().equals("https")) {
                // App was opened from a browser
                String host = referrerUri.getHost();
                // host will contain the host path (e.g. www.google.com)

                // Add analytics code below to trackForUninstall this click from web Search
                Log.d("Source from browser:" + host);
                EventTrackingUtils.sendClick(level2DeepLink, "Browser " + host, XitiUtils.NAVIGATION);

            } else if (referrerUri.getScheme().equals("android-app")) {
                // App was opened from another app
                AndroidAppUri appUri = AndroidAppUri.newAndroidAppUri(referrerUri);
                String referrerPackage = appUri.getPackageName();
                if (appUri.getDeepLinkUri() != null && "com.google.android.googlequicksearchbox".equals(referrerPackage)) {
                    // App was opened from the Google app
                    String host = appUri.getDeepLinkUri().getHost();
                    // host will contain the host path (e.g. www.google.com)

                    // Add analytics code below to trackForUninstall this click from the Google app
                    Log.d("Source from google app :" + host);
                    EventTrackingUtils.sendClick(level2DeepLink, "App Search", XitiUtils.NAVIGATION);

                } else if ("com.google.appcrawler".equals(referrerPackage)) {
                    // Make sure this is not being counted as part of app usage
                    Config.isGoogleAppCrawler = true;
                    Config.enableXitiTagging = false;
                    Config.enableTealiumTagging = false;
                    Log.d("Source from appCrawler !!!");
                }
            }
        }
    }

    /**
     * Returns the referrer who started this Activity.
     */
    @Override
    public Uri getReferrer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return super.getReferrer();
        }
        return getReferrerCompatible();
    }

    /**
     * Returns the referrer on devices running SDK versions lower than 22.
     */
    private Uri getReferrerCompatible() {
        Intent intent = this.getIntent();
        Uri referrerUri = intent.getParcelableExtra(Intent.EXTRA_REFERRER);

        if (referrerUri != null) {
            return referrerUri;
        }

        String referrer = intent.getStringExtra("android.intent.extra.REFERRER_NAME");
        if (referrer != null) {
            // Try parsing the referrer URL; if it's invalid, return null
            try {
                return Uri.parse(referrer);
            } catch (ParseException e) {
                return null;
            }
        }

        Uri referrerData = this.getIntent().getData();
        if (referrerData != null) {
            return referrerData;
        }

        return null;
    }

    public DrawerLayoutUtils getDrawerLayoutUtils() {
        return drawerLayoutUtils;
    }

    public void setDrawerLayoutUtils(DrawerLayoutUtils drawerLayoutUtils) {
        this.drawerLayoutUtils = drawerLayoutUtils;
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (drawerLayoutUtils != null) {
            drawerLayoutUtils.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (drawerLayoutUtils != null) {
            drawerLayoutUtils.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ACUtils.logCrashlytics("onPause: " + getLocalClassName());
        // Notify comScore about lifecycle usage
        comScore.onExitForeground();
        if (drawerLayoutUtils != null && drawerLayoutUtils.isMenuOpen()) {
            drawerLayoutUtils.setMenuClose();
        }
        if (Config.enableChat) {
            CCSocket.getInstance().unSubscribeMessageById(activityUniqueKey);
        }
        AnimationHelper animate = new AnimationHelper(this, AnimationHelper.ACTIVITY_TRANSITION_CLOSE);
        animate.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ACUtils.logCrashlytics("onResume: " + getLocalClassName());
        // Notify comScore about lifecycle usage
        comScore.onEnterForeground();
        if (Config.enableChat) {
            ChatCafe.logInChatCafe();
            if (Config.userAccount.isLogin()) {
                subscribeSocketMessage();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Kahuna.getInstance().start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Kahuna.getInstance().stop();
    }

    private void subscribeSocketMessage() {
        CCSocket.getInstance().subscribeMessageById(activityUniqueKey, messageCallback);
        if (CCUser.getCurrentUser() != null && CCUser.getCurrentUser().getObjectId() != null) {
            if (Config.badgeUnreadChatNumber < 0) {
                CCRoom.updateBadge(unreadBadgeCallback);
            } else {
                CCRoom.updateBadgeSkipFirstAPICall(Config.badgeUnreadChatNumber, unreadBadgeCallback);
            }
        }
    }

    public void resetBadgeUnreadChatNumber() {
        Config.badgeUnreadChatNumber = -1;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayoutUtils != null && drawerLayoutUtils.isMenuOpen()) {
            drawerLayoutUtils.setMenuClose();
        } else {
            if (isTaskRoot()) {
                // To make sure that AdsListActivity is always the last view that user see before exiting app
                Intent intent = new Intent(MudahBaseActivity.this, HomepageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();//to prevent looping and users cannot exit the app
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (drawerLayoutUtils != null && drawerLayoutUtils.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isTaskRoot()) {
                    // To make sure that HomepageActivity is always the last view that user see before exiting app
                    Intent intent = new Intent(MudahBaseActivity.this, HomepageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (findViewById(R.id.drawer_layout) != null) {
            ACUtils.unbindDrawables(findViewById(R.id.drawer_layout));
            System.gc();
        }

        if (drawerLayoutUtils != null) {
            drawerLayoutUtils.removeDrawerListener();
        }
    }

    public void setMenuClose() {
        if (drawerLayoutUtils != null) {
            drawerLayoutUtils.setMenuClose();
        }
    }

    public void setMenuOpen() {
        if (drawerLayoutUtils != null) {
            drawerLayoutUtils.setMenuOpen();
        }
    }

    public Bundle fbAppEventLogging(String category, String subCategory, String region, String subRegion, String postAs) {
        Bundle parameters = new Bundle();
        parameters.putString(Constants.CAT_NAME, category);
        parameters.putString(Constants.SUB_CAT_NAME, subCategory);
        parameters.putString(Constants.REGION_NAME, region);
        parameters.putString(Constants.SUB_REGION_NAME, subRegion);
        parameters.putString(Constants.POST_AS, postAs);
        return parameters;
    }

    public Bundle fbAdreply(String category, String replyType, AdViewAd acAd) {
        String subCategory = acAd.getCategoryName();
        String region = acAd.getRegion();
        String subRegion = acAd.getSubRegionName();
        String postAs = getPostAs(acAd.getCompanyAd());
        Bundle parameters = fbAppEventLogging(category, subCategory, region, subRegion, postAs);
        parameters.putString(Constants.REPLY_TYPE, replyType);
        Log.d(parameters.toString());
        return parameters;
    }

    public Bundle fbAdview(String category, AdViewAd acAd) {
        String adID = acAd.getAdId();
        String subCategory = acAd.getCategoryName();
        String region = acAd.getRegion();
        String subRegion = acAd.getSubRegionName();
        String postAs = getPostAs(acAd.getCompanyAd());
        Bundle parameters = fbAppEventLogging(category, subCategory, region, subRegion, postAs);
        parameters.putString(Constants.AD_ID, adID);
        Log.d(parameters.toString());
        return parameters;
    }

    public Bundle fbInsertAd(String category, String subCategory, String region, String subRegion, String funnelStage, String postAs) {
        Bundle parameters = fbAppEventLogging(category, subCategory, region, subRegion, postAs);
        parameters.putString(Constants.FUNNEL_STAGE, funnelStage);
        Log.d(parameters.toString());
        return parameters;
    }

    private String getPostAs(String postAs) {
        String postAsString = Constants.EMPTY_STRING;
        if (PRIVATE.equalsIgnoreCase(postAs)) {
            postAsString = InsertAdCategoryParamsAdapter.PostedBy.PRIVATE.toString().toLowerCase();
        } else if (COMPANY.equalsIgnoreCase(postAs)) {
            postAsString = InsertAdCategoryParamsAdapter.PostedBy.COMPANY.toString().toLowerCase();
        }
        return postAsString;
    }

    public void showMessage(String url) throws JSONException {

        final Dialog dialog = new Dialog(this, R.style.TranslucentFadeDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_terms_privacy);

        WebView webViewEn = (WebView) dialog.findViewById(R.id.wv_messages);
        webViewEn.loadUrl(url);

        RelativeLayout rlOutside = (RelativeLayout) dialog.findViewById(R.id.rl_outside);
        rlOutside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_close);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void setSpanClickable(View view, int startLink, int endLink, int strId, int tvId, final Intent intent) {

        String msgStr = getResources().getString(strId);

        SpannableString spannableString = new SpannableString(msgStr);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(intent);
                finish();
            }

            public void updateDrawState(TextPaint ds) {// override updateDrawState
                ds.setUnderlineText(false); // set to false to remove underline
            }
        };

        if (endLink > msgStr.length()) {
            spannableString.setSpan(clickableSpan, 0, endLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(clickableSpan, startLink, endLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.sign_form_focus_text)), startLink, endLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        TextView textView = (TextView) view.findViewById(tvId);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean isMyMessage(JSONObject result) throws JSONException {
        if (Config.userAccount.getUserId() != null) {
            return result.optString("sender_id", Constants.EMPTY_STRING).equals(Config.userAccount.getUserId());
        } else {
            return false;
        }
    }

    public void threadRunBadge(final int result) {
        Config.badgeUnreadChatNumber = result;
        try {
            Log.d("update menu from badge menu: " + result);
            if (badgeTotalUnreadChatMsg != null) {
                if (result > 0) {
                    badgeTotalUnreadChatMsg.setVisibility(View.VISIBLE);
                    if (result < 100) {
                        badgeTotalUnreadChatMsg.setText(result + Constants.EMPTY_STRING);
                        badgeTotalUnreadChatMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    } else {
                        badgeTotalUnreadChatMsg.setText(CHAT_UNREAD_PLUS);
                        badgeTotalUnreadChatMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
                    }
                } else {
                    badgeTotalUnreadChatMsg.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(" threadRunBadge ");
        }
    }


    private void showInAppMessage(final JSONObject messageJson) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inAppMessage(messageJson);
            }
        });
    }

    protected void inAppMessage(JSONObject ccMessage) {
        if (!ccMessage.optBoolean("mute", false)) {
            ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView().getRootView();
            AppMsg.Style style = new AppMsg.Style(AppMsg.LENGTH_SHORT, R.color.transparent);
            ChatMessageAlertView chatMessageAlertView = getChatMessageAlertView(ccMessage);
            AppMsg provided = AppMsg.makeText(MudahBaseActivity.this
                    , R.string.chat_notification_title
                    , style
                    , chatMessageAlertView
                    , true);
            provided.setAnimation(R.anim.slide_in_from_top, R.anim.slide_out_to_top);
            provided.setParent(viewGroup);
            provided.show();
        }
    }

    @NonNull
    private ChatMessageAlertView getChatMessageAlertView(final JSONObject ccMessage) {
        final ChatMessageAlertView chatMessageAlertView = new ChatMessageAlertView(MudahBaseActivity.this);
        playDefaultSound();

        chatMessageAlertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomId = ccMessage.optString(Constants.PUSH_CHAT_ROOM_ID, Constants.EMPTY_STRING);
                Log.d("roomId: " + roomId);
                ChatActivity.start(MudahBaseActivity.this, roomId);
                EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, TealiumHelper.CHAT_INAPP_NOTIFICATION, XitiUtils.NAVIGATION);
            }
        });
        chatMessageAlertView.setCCMessage(ccMessage);
        chatMessageAlertView.setPadding(0, MudahUtil.getStatusBarHeight(getApplicationContext()), 0, 0);
        return chatMessageAlertView;
    }

    private boolean isOnChatRoom(JSONObject result) throws JSONException {
        if (currentCCRoom != null) {
            return result.optString(Constants.PUSH_CHAT_ROOM_ID, Constants.EMPTY_STRING).equals(currentCCRoom.getObjectId());
        } else {
            return false;
        }
    }

    private void playDefaultSound() {
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(getApplicationContext(), defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tagConnectionLost(String clickName, JSONObject data) {
        if (data != null) {
            clickName += WITH_DATA;
            if (data.has(Constants.RESP_CODE)) {
                clickName += data.optString(Constants.RESP_CODE);
            }
        }

        EventTrackingUtils.sendClick(XitiUtils.LEVEL2_OTHERS_DEFAULT, clickName, XitiUtils.NAVIGATION);
        sendFalseCrashLog(clickName + " " + data);
    }

    public void sendTagUserAccount(String pageName) {
        EventTrackingUtils.sendUATagWithMode(XitiUtils.MODE_OTHERS, this, pageName, XitiUtils.LEVEL2_UA_SITE_ID, Config.userAccount);
    }

    public boolean isSkipTutorial() {
        return Config.tutorialPagesAndSteps.containsKey(ShowcaseHelper.SKIP_TUTORIAL);
    }

    public void hideSkipTutorialOption() {
        if (skipTutorial != null) {
            skipTutorial.setVisibility(View.GONE);
        }
    }

    public void sendFalseCrashLog(String message) {
        ACUtils.logCrashlytics(message);
        try {
            throw new RuntimeException(message);
        } catch (RuntimeException ignore) {
            ACUtils.debug(ignore, false);
        }
    }
}
