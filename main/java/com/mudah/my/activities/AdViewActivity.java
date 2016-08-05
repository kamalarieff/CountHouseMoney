package com.mudah.my.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.gravityrd.receng.web.webshop.jsondto.GravityEvent;
import com.gravityrd.receng.web.webshop.jsondto.GravityNameValue;
import com.lib701.datasets.ACAd;
import com.lib701.utils.ACUtils;
import com.lib701.utils.AppRater;
import com.lib701.utils.DialogUtils;
import com.lib701.utils.FileUtils;
import com.lib701.utils.IntentActionUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.dao.AdViewFavouritesDAO;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.fragments.AdViewFragment;
import com.mudah.my.fragments.AlertDialogFragment;
import com.mudah.my.fragments.DeleteAdEmailFragment;
import com.mudah.my.fragments.PagingBlocketLoaderFragment;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.ShowcaseHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.AppsFlyerUtils;
import com.mudah.my.utils.CustomizedIntentActionUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.GravityUtils;
import com.mudah.my.utils.MudahUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tealium.library.Tealium;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdViewActivity extends MudahBaseActivity {
    public static final String EXTRA_LIST_ITEM_POSITION = "list_item_position";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_REGION_ID = "region";
    public static final String EXTRA_ALL_LIST_ID = "all_list_id";
    public static final String EXTRA_GRAND_TOTAL = "grand_total";
    public static final String EXTRA_SEARCH_PARAM = "search_param";
    public static final String VIEW_TYPE = "view_type";
    public static final int OFF_PAGE_LIMIT = 1;
    /**
     * The minimum amount of ads to have after the current view page, before loading more.
     */
    public static final int MIN_REMAINING_ADS = 5;
    public static final String ADVERTISER_NAME = "advertiser_name";
    public static final String AD_TITLE = "ad_title";
    public static final String AD_EMAIL = "adv_email";
    public static final String LIST_ID = "list_id";
    public static final String ADREPLY = "adreply";
    private static final String PREFIX_MOBILE_PHONE = "01";
    private static final String CHAT_DISABLE = "Chat_disable_";
    private static final String CHAT_NO_COUNTRY_CODE = "Chat_no_country_code";
    private static final String TAG_LOAD_LIST_ID = "load_list_id";
    private static final String ADVIEW = "adview";
    private static final int LOADER_MORE_AD = 6;
    private static final int LOADER_SENDDELETEMAIL = 0x04;
    private static final String TAG_ERROR_DIALOG = "error_dialog";
    private static final int CAR_CATEGORY = 1020;
    private static final String VERIFIED_STATUS = "verified";
    private static final String ADVIEW_TUTORIAL = "ADVIEW";
    private static final String VERIFIED_TUTORIAL = "VERIFIED";
    private static int currentTutorialStep;
    private static int verifiedTutorialStep;
    public int totalAds;
    public boolean facebook = true;
    public boolean instagram = true;
    public boolean whatsapp = true;
    public boolean sms = true;
    public Menu menu;
    String imageUrlToShare;
    ShareDialog shareDialog;
    Bundle parameters;
    private boolean isMyAds = false;
    private ViewPager mPager;
    private boolean updateUIDone = false;
    private int categoryId;
    private String storeVerified;
    private MenuItem miFavourite;
    private AdViewFavouritesDAO adViewFavouritesDAO;
    private boolean isFavourited = false;
    private ShowcaseHelper showcaseHelper;
    private int regionId;
    private AdViewAd acAd;
    private LinearLayout llEmail;
    private LinearLayout llPhone;
    private LinearLayout llSms;
    private LinearLayout llChat;
    private int currentPosition;
    private int grandTotal;
    private List<String> allListIDList = new ArrayList<String>();
    private HashMap<String, Object> searchParam = new HashMap<String, Object>();
    private boolean isLoading = false;
    private int currentPage;
    private AlertDialog rateDialog;
    private AppRater appRater;
    //to differentiate between clicking or swiping and prevent sending tracking to Xiti twice.
    // When click to navigate, it will trigger onPageChange which is similar to swiping
    private boolean clickToNavigate = false;
    private boolean menuStateOfEnabled = true;
    private GoogleApiClient mClient;
    private AppEventsLogger logger;
    private Map<String, String> dataTealium;
    private String parentCategoryName;
    private Map<String, String> xitiMapX = new HashMap<>();
    private SparseBooleanArray toBeRemovedFromFav = new SparseBooleanArray();
    private SparseArray<ACAd> toBeAddedToFav = new SparseArray<>();
    private boolean tutorialFavouriteOpen = false;
    private boolean tutorialVerifiedOpen = false;
    private String viewType;
    private OnClickListener adviewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_chat:
                    if (!Log.isDebug && !isChatEnable())
                        return;
                    //if user has not logged in - > Go to Sign In page
                    //If user has logged in, go to chat
                    if (!Config.userAccount.isLogin() && acAd != null) {
                        Intent intent = new Intent(AdViewActivity.this, SignInActivity.class);
                        intent.putExtra(Constants.CHAT_PAGE, true);
                        intent.putExtra(Constants.CHAT_PRODUCT, acAd);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    } else if (acAd != null) {
                        //disable chat if users click on their own ad
                        if (!acAd.getUserId().equals(Config.userAccount.getUserId())) {
                            ChatActivity.start(AdViewActivity.this, acAd);
                        } else {
                            Toast.makeText(AdViewActivity.this, R.string.own_chat_msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_ADVIEW, TealiumHelper.EVENT_CHAT);
                    break;
                case R.id.ll_phone:
                    if (acAd != null) {
                        IntentActionUtils.call(acAd.getPhone(), AdViewActivity.this);
                        tagCallEvent();
                    }
                    break;
                case R.id.ll_sms:
                    if (acAd != null) {
                        IntentActionUtils.message(acAd.getPhone(), acAd.getSubject(), AdViewActivity.this);
                        tagSMSEvent();
                    }
                    break;
                case R.id.ll_email:
                    if (acAd != null) {
                        Intent intent = new Intent(AdViewActivity.this, EmailAdvertiserActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
                        intent.putExtra("list_id", acAd.getListId());
                        intent.putExtra("tagging_params", acAd);
                        startActivity(intent);
                    }
                    EventTrackingUtils.sendClickByCategoryId(dataTealium, categoryId, XitiUtils.AD_VIEW, "Ad_detail_emailForm", XitiUtils.NAVIGATION);
                    break;
                default:
                    break;
            }
        }
    };
    private Target InstagramImageTarget = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String filename = imageUrlToShare.substring(imageUrlToShare.lastIndexOf('/') + 1);
                    String message = acAd.getSubject() + "\n\n" + acAd.getUrl();
                    String imgFilePath = Constants.EMPTY_STRING;
                    try {
                        File imgFileToShare = FileUtils.getPublicTempImageFile(Config.UPLOAD_IMAGES_DIR, filename);
                        imgFilePath = imgFileToShare.getPath();
                        imgFileToShare.createNewFile();
                        FileOutputStream outputStream = new FileOutputStream(imgFileToShare);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                        outputStream.close();

                        if (imgFileToShare.exists()) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType(FileUtils.MIME_TYPE_IMAGE);

                            // Create the URI from the media
                            Uri uri = Uri.fromFile(imgFileToShare);
                            Log.d("uri:" + uri);

                            i.putExtra(Intent.EXTRA_STREAM, uri);
                            i.putExtra(Intent.EXTRA_TEXT, message);

                            i.setPackage(Config.getPackage("instagram"));
                            startActivity(i);
                        }
                    } catch (Exception e) {
                        ACUtils.debug(e, "shareToInstagram", imgFilePath);
                    }

                }
            }).start();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private boolean isChatEnable() {
        if (ACUtils.isEmpty(Config.userAccount.getCountryCode())) {
            AlertDialogFragment
                    .instantiate(getString(R.string.general_error_title), getString(R.string.no_country_code_found), null)
                    .show(getSupportFragmentManager(), TAG_ERROR_DIALOG);
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, CHAT_NO_COUNTRY_CODE, XitiUtils.NAVIGATION);
            return false;
        } else if (!MudahUtil.isWhitelistedCountryCode(Config.userAccount.getCountryCode())) {
            AlertDialogFragment
                    .instantiate(getString(R.string.general_error_title), getString(R.string.chat_not_allow), null)
                    .show(getSupportFragmentManager(), TAG_ERROR_DIALOG);
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_CHAT_ID, CHAT_DISABLE + Config.userAccount.getCountryCode(), XitiUtils.NAVIGATION);
            return false;
        }
        return true;
    }

    private void tagSMSEvent() {
        tagAdReplyEvent(Constants.SMS, XitiUtils.AD_REPLY_SMS);
        tagAppsFlyerEvent(categoryId);

        tagFBAdReplyEvent(Constants.SMS);
        tagGravityEventExtra(GravityUtils.EVENT_TYPE_PHONE_CLICK, GravityUtils.METHOD_SMS);
        AmplitudeUtils.tagReply(acAd, Constants.SMS);

        KahunaHelper.tagEvent(KahunaHelper.AD_REPLY_EVENT, acAd);
        KahunaHelper.tagAttributes(KahunaHelper.PAGE_AD_REPLIED, KahunaHelper.LAST_TITLE_REPLIED, acAd.getSubject());
    }

    private void tagCallEvent() {
        Log.d(" parentCategoryName=" + parentCategoryName);
        tagAdReplyEvent(Constants.CALL, XitiUtils.AD_REPLY_CALL);
        tagAppsFlyerEvent(categoryId);

        tagFBAdReplyEvent(Constants.PHONE);
        tagGravityEventExtra(GravityUtils.EVENT_TYPE_PHONE_CLICK, GravityUtils.METHOD_PHONE);
        AmplitudeUtils.tagReply(acAd, Constants.PHONE);
        KahunaHelper.tagEvent(KahunaHelper.AD_REPLY_EVENT, acAd);
        KahunaHelper.tagAttributes(KahunaHelper.PAGE_AD_REPLIED, KahunaHelper.LAST_TITLE_REPLIED, acAd.getSubject());
    }

    private void tagAdReplyEvent(String adReplyType, String adReplyTealiumId) {
        if (!ACUtils.isEmpty(parentCategoryName) && !ACUtils.isEmpty(acAd.getCategoryName())) {
            String fullTagName = adReplyType + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(parentCategoryName) + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(acAd.getCategoryName());
            EventTrackingUtils.sendAdReply(fullTagName, acAd);
            tagTealiumEvent(adReplyType, adReplyTealiumId, fullTagName);
        }
    }

    private void tagFBAdReplyEvent(String adReplyType) {
        parameters = fbAdreply(parentCategoryName, adReplyType, acAd);
        logger.logEvent(ADREPLY, parameters);
    }

    public void tagAppsFlyerEvent(int categoryId) {
        if (Constants.CARS == categoryId) {
            AppsFlyerUtils.sendConversionTag(AdViewActivity.this, AppsFlyerUtils.AppsFlyerTags.AR_CARS_APP, acAd);
        } else {
            AppsFlyerUtils.sendConversionTag(AdViewActivity.this, AppsFlyerUtils.AppsFlyerTags.AR_MARKETPLACE_APP, acAd);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove Material background, to prevent overdrawing
        getWindow().setBackgroundDrawable(null);
        AmplitudeUtils.InitializeAmplitude(this, getApplication());
        logger = AppEventsLogger.newLogger(this);
        adViewFavouritesDAO = new AdViewFavouritesDAO(this);
        shareDialog = new ShareDialog(this);

        Log.strictMode();

        setContentView(R.layout.activity_ad_view);

        mClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(AppIndex.API).build();

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);

        Bundle bundle = getIntent().getExtras();
        Uri uri = getIntent().getData();
        if (savedInstanceState != null) {
            int savedListViewPosition = savedInstanceState.getInt("savedListViewPosition", 0);
            categoryId = savedInstanceState.getInt("savedCategoryId", XitiUtils.CATEGORY_OTHERS);
            regionId = savedInstanceState.getInt("savedRegionId", 0);
            grandTotal = savedInstanceState.getInt("savedGrandTotal", 0);
            searchParam = (HashMap<String, Object>) savedInstanceState.getSerializable("savedSearchParam");
            allListIDList = savedInstanceState.getStringArrayList("savedAllListsId");
            if (savedListViewPosition > 0) {
                currentPosition = savedListViewPosition;
            }
        } else if (uri != null && Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            String strUrl = uri.toString();
            Log.d("Redirected from " + strUrl);
            try {
                String listId = extractListIdFromUrl(strUrl);
                if (ACUtils.isEmpty(listId)) {
                    //Throw exception so that it will be caught and the app will be redirected to listing page
                    throw new IllegalArgumentException("URL format unexpected");
                } else {
                    // Run parseInt to check if listId obtained is convertible to int.
                    // If not convertible, it will raise exception
                    Integer.parseInt(listId.trim());
                    allListIDList.add(listId);
                    regionId = 0;
                    currentPosition = 0;
                    grandTotal = 1;
                    AppsFlyerUtils.sendDeepLinkData(this);
                    if (XitiUtils.initFromLastConfig(getApplicationContext()) == null) {
                        Log.e("Xiti initialization is wrong");
                    } else {
                        String segments[] = strUrl.split("/");
                        String host = segments[2].trim();
                        if (host.equalsIgnoreCase("www.mudah.my")) {
                            EventTrackingUtils.sendClick(XitiUtils.getLevel2Map(XitiUtils.LEVEL2_OTHERS), "Redirect_from_website", XitiUtils.NAVIGATION);
                        } else if (host.equalsIgnoreCase("m.mudah.my")) {
                            EventTrackingUtils.sendClick(XitiUtils.getLevel2Map(XitiUtils.LEVEL2_OTHERS), "Redirect_from_msite", XitiUtils.NAVIGATION);
                        }
                    }
                }
            } catch (Exception e) {
                ACUtils.debug(e, "AdView_url", strUrl, false);
                Intent intent = new Intent(this, AdsListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        } else if (bundle != null) {
            currentPosition = bundle.getInt(EXTRA_LIST_ITEM_POSITION);
            grandTotal = bundle.getInt(EXTRA_GRAND_TOTAL);
            searchParam = (HashMap<String, Object>) this.getIntent().getSerializableExtra(EXTRA_SEARCH_PARAM);
            allListIDList = this.getIntent().getStringArrayListExtra(EXTRA_ALL_LIST_ID);
            viewType = AmplitudeUtils.getViewType(bundle.getString(VIEW_TYPE));
        }

        if (allListIDList != null) {
            totalAds = allListIDList.size();
        } else
            totalAds = 0;

        MyAdapter mAdapter = new MyAdapter(getSupportFragmentManager(), allListIDList);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(OFF_PAGE_LIMIT);
        mPager.setAdapter(mAdapter);
        PageListener pageListener = new PageListener();
        mPager.addOnPageChangeListener(pageListener);

        llEmail = (LinearLayout) findViewById(R.id.ll_email);
        llPhone = (LinearLayout) findViewById(R.id.ll_phone);
        llSms = (LinearLayout) findViewById(R.id.ll_sms);
        llChat = (LinearLayout) findViewById(R.id.ll_chat);
        if (Config.enableChat) {
            TextView tvChat = (TextView) findViewById(R.id.tv_chat);
            tvChat.setText(ACUtils.getHtmlFromString(getResources().getString(R.string.ad_view_menu_chat)));

            llChat.setOnClickListener(adviewOnClickListener);
            llChat.setVisibility(View.VISIBLE);
        } else {
            llChat.setVisibility(View.GONE);
        }

        if (getSupportFragmentManager().findFragmentByTag(TAG_LOAD_LIST_ID) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(new LoadMoreListIDFragment(), TAG_LOAD_LIST_ID)
                    .commit();
        }
        //To reduce the number of disk reading, check first
        if (Config.FIRST_TIME_RATING != Config.NO_MORE_RATING) {
            //ask user for Ratings in Play Store
            appRater = new AppRater(this, this.getPackageName());
            appRater.setDaysBeforePrompt(2);
            appRater.setLaunchesBeforePrompt(5);
            appRater.setPhrases(R.string.rate_title, R.string.rate_explanation, R.string.rate_now, R.string.rate_later, R.string.rate_never);
            rateDialog = appRater.show();
        }

        //show tutorial
        showcaseHelper = new ShowcaseHelper(this);
        if (!isSkipTutorial() && !isFavoriteTutorialDone()) {
            prepareTutorial();
        }

        checkSharePackagesInstalled();
    }

    private boolean isVerifiedTutorialDone() {
        if (Config.tutorialPagesAndSteps.containsKey(Config.TutorialPages.VERIFIED.toString())) {
            verifiedTutorialStep = Config.tutorialPagesAndSteps.get(Config.TutorialPages.VERIFIED.toString());
        } else {
            verifiedTutorialStep = 1;
        }

        if (verifiedTutorialStep > ShowcaseHelper.VERIFIED_TUTORIAL_STEPS) {
            return true;
        }
        return false;
    }

    private boolean isVerifiedSeller() {
        return (CAR_CATEGORY == categoryId && VERIFIED_STATUS.equals(storeVerified));
    }

    private boolean isFavoriteTutorialDone() {
        if (Config.tutorialPagesAndSteps.containsKey(Config.TutorialPages.ADVIEW.toString())) {
            currentTutorialStep = Config.tutorialPagesAndSteps.get(Config.TutorialPages.ADVIEW.toString());
        } else {
            currentTutorialStep = 1;
        }

        if (currentTutorialStep > ShowcaseHelper.ADVIEW_TUTORIAL_STEPS) {
            return true;
        }
        return false;
    }

    /*
    *   This function should be able to extract the list_id of 33945642 from these different versions of URLs
    *
    *      1) http://www.mudah.my/Toyota+Corolla+M+-33945642.htm?last=1
    *      2) http://www.mudah.my/Toyota+Corolla+M+-33945642.htm
    *
    *      3) http://www.mudah.my/vi/33945642.htm
    *      4) http://www.mudah.my/vi/33945642.htm?ca=9_s&st=s&cg=1020&w=3
    *
    *      5) http://m.mudah.my/view?&f=p&ad_id=33945642
    */
    private String extractListIdFromUrl(String strUrl) {

        String listId = Constants.EMPTY_STRING;
        try {
            Uri uri = Uri.parse(strUrl);
            String path = uri.getPath();
            Log.d("path= " + path);
            if (("/view").equalsIgnoreCase(path)) {
                // http://m.mudah.my/view?&f=p&ad_id=33945642
                listId = uri.getQueryParameter("ad_id");
            } else {
                int indexStartA = path.lastIndexOf("-");
                int indexStartB = path.lastIndexOf("/vi/");
                int indexEnd = path.indexOf(".htm");
                if (indexStartA > 0 && indexEnd > 0) {
                    // http://www.mudah.my/Toyota+Corolla+M+-33945642.htm?last=1
                    // http://www.mudah.my/Toyota+Corolla+M+-33945642.htm
                    listId = path.substring(indexStartA + 1, indexEnd);
                } else if (indexStartB >= 0 && indexEnd > 0) {
                    // http://www.mudah.my/vi/33945642.htm
                    // http://www.mudah.my/vi/33945642.htm?ca=9_s&st=s&cg=1020&w=3
                    listId = path.substring(indexStartB + 4, indexEnd);
                }
            }
        } catch (NullPointerException e) {
            ACUtils.debug(e, "AdView_extractListIdFromUrl", strUrl);
        }

        return listId;
    }

    private void prepareTutorial() {
        //need 1 listener for both tutorial, if using 2 listener when removing listener, both will be removed
        final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (!tutorialFavouriteOpen && !isFavoriteTutorialDone()) {
                    //check favorite tutorial first
                    View resourceID = findViewById(R.id.menu_favourite);
                    if (resourceID != null) {
                        tutorialFavouriteOpen = true;
                        showTutorial(ADVIEW_TUTORIAL);
                    }
                } else if (!tutorialVerifiedOpen && !isVerifiedTutorialDone() && isVerifiedSeller()) {
                    //after favorite tutorial finished, check verified tutorial
                    View resourceID = findViewById(R.id.ad_view_verified_text);
                    if (resourceID != null) {
                        tutorialVerifiedOpen = true;//prevent this tutorial pops up again when swipe
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showTutorial(VERIFIED_TUTORIAL);
                            }
                        }, 500);//set some delay to wait for the view to be draw on UI first
                    }
                }

                if (updateUIDone) {
                    //remove listener if favorite tutorial done, and not verified seller
                    removeListener();
                }
            }

            private void removeListener() {
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
        });
    }

    private void showTutorial(String tutorial) {

        if (VERIFIED_TUTORIAL.equals(tutorial)) {
            ShowcaseHelper.TutorialStepAndResource tutorialStepAndResource = showcaseHelper.new TutorialStepAndResource(Config.TutorialPages.VERIFIED, R.id.ad_view_verified_text, false);
            tutorialStepAndResource.setStep(verifiedTutorialStep);
            tutorialStepAndResource.setTitleResource(R.string.verified_title_tip);
            tutorialStepAndResource.setDescriptionResource(R.string.verified_desc_tip);
            showcaseHelper.declareSupportedTutorial(tutorialStepAndResource);
            verifiedTutorialStep++;

            if (isFavoriteTutorialDone()) {
                showcaseHelper.showTutorial();
            }

        } else if (ADVIEW_TUTORIAL.equals(tutorial)) {
            ShowcaseHelper.TutorialStepAndResource tutorialStepAndResource = showcaseHelper.new TutorialStepAndResource(Config.TutorialPages.ADVIEW, R.id.menu_favourite, true);
            tutorialStepAndResource.setStep(currentTutorialStep);
            tutorialStepAndResource.setTitleResource(R.string.favorite_title_tip);
            tutorialStepAndResource.setDescriptionResource(R.string.favorite_desc_tip);
            showcaseHelper.declareSupportedTutorial(tutorialStepAndResource);
            showcaseHelper.showTutorial();
            currentTutorialStep++;
        }
    }

    public void clickToRate(View v) {
        if (appRater != null && rateDialog != null) {
            if (!Log.isDebug) {
                Crashlytics.getInstance().core.setString("AdViewActivity_clickToRate", "App Rating: " + v.getId());
            }
            switch (v.getId()) {
                case R.id.rate_later:
                    appRater.rateLater(rateDialog);
                    break;
                case R.id.rate_now:
                    appRater.rateNow(rateDialog);
                    saveFirstTimeFlag(Config.NO_MORE_RATING);
                    break;
                case R.id.rate_never:
                    appRater.rateNever(rateDialog);
                    saveFirstTimeFlag(Config.NO_MORE_RATING);
                    break;
            }
        }
    }

    public void shareAd(View v) {
        String url = null;
        Integer currentAdListId;
        if (acAd != null) {
            currentAdListId = acAd.getListId();

            if (acAd.getUrl() != null) {
                url = acAd.getUrl();
            } else {
                url = Config.shareHost + "/vi/" + currentAdListId + ".htm";
                acAd.setUrl(url);
            }
        }

        if (!ACUtils.isEmpty(url)) {
            String taggingLabel = "Ad_detail_share";
            switch (v.getId()) {
                case R.id.share_to_facebook_bottom:
                    shareToFacebook(url);
                    taggingLabel = "Ad_detail_share_facebook";
                    break;
                case R.id.share_to_whatsapp_bottom:
                    shareToWhatsapp(url);
                    taggingLabel = "Ad_detail_share_whatsapp";
                    break;
                case R.id.share_to_sms_bottom:
                    shareToMessage(url);
                    taggingLabel = "Ad_detail_share_sms";
                    break;
                case R.id.share_to_instagram_bottom:
                    shareToInstagram(url);
                    taggingLabel = "Ad_detail_share_instagram";
                    break;
                default:
                    break;
            }

            EventTrackingUtils.sendClickByCategoryId(dataTealium, categoryId, XitiUtils.AD_VIEW, taggingLabel, XitiUtils.NAVIGATION);
        }
    }

    public void shareToFacebook(String url) {
        if (url != null) {
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle(acAd.getSubject())
                        .setContentDescription(ACUtils.getHtmlFromString(acAd.getBody()).toString())
                        .setContentUrl(Uri.parse(url))
                        .build();

                shareDialog.show(linkContent);
            }
        }
    }

    public void shareToWhatsapp(String url) {
        String message = acAd.getSubject() + "\n\n" + url;
        if (url != null) {
            final ComponentName name = new ComponentName(Config.getPackage("whatsapp"), "com.whatsapp.ContactPicker");
            Intent oShareIntent = new Intent();
            oShareIntent.setComponent(name);
            oShareIntent.setType("text/plain");
            oShareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
            startActivity(oShareIntent);
        }
    }

    public void shareToMessage(String url) {
        String message = acAd.getSubject() + "\n\n" + url;
        if (url != null) {
            try {
                Intent sms = new Intent(Intent.ACTION_VIEW);
                sms.putExtra("sms_body", message.toString());
                sms.setType("vnd.android-dir/mms-sms");
                startActivity(sms);
            } catch (ActivityNotFoundException activityNotFound) {
                //Tablet will throw out this error
                Log.e(activityNotFound);
            }
        }
    }

    public void shareToInstagram(String url) {
        ArrayList images = acAd.getImages();
        imageUrlToShare = images.get(0).toString();
        Picasso.with(this).load(imageUrlToShare).into(InstagramImageTarget);
    }

    private void saveFirstTimeFlag(int flag) {
        Config.FIRST_TIME_RATING = flag;
        PreferencesUtils.getSharedPreferences(getApplicationContext())
                .edit()
                .putInt(PreferencesUtils.FIRST_TIME_USE, flag)
                .apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ad_view, menu);
        miFavourite = menu.findItem(R.id.menu_favourite);
        if (acAd != null && updateUIDone) {
            updateFavouriteStatus(acAd.getListId());
        }

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d("menuStateOfEnabled: " + menuStateOfEnabled);

        menu.setGroupVisible(R.id.menu_adview_group, menuStateOfEnabled);
        MenuItem favItem = menu.findItem(R.id.menu_favourite);
        if (favItem != null) {
            favItem.setVisible(menuStateOfEnabled);
        }
        if (isMyAds) {
            hideNonUserAdMenu(menu);
        }
        this.menu = menu;
        return true;
    }

    private void hideNonUserAdMenu(Menu menu) {
        MenuItem reportMenu = menu.findItem(R.id.menu_report);
        if (reportMenu != null) {
            reportMenu.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //no need to check if task is root, if users come from M-site, they should be able to go back to browser when click back
                finish();
                return true;
            case R.id.menu_favourite:
                if (acAd != null) {
                    toggleFavourite();
                }
                //hide Tutorial
                if (showcaseHelper != null) {
                    showcaseHelper.closeAllTutorials(false);
                }
                return true;
            case R.id.menu_delete:
                EventTrackingUtils.sendClickByCategoryId(dataTealium, categoryId, XitiUtils.AD_VIEW, "Ad_detail_delete", XitiUtils.NAVIGATION);
                //To delete, we need listID. Check if acAd is not null first.
                if (acAd != null) {
                    delete();
                }
                return true;
            case R.id.menu_report:
                EventTrackingUtils.sendClickByCategoryId(dataTealium, categoryId, XitiUtils.AD_VIEW, "Ad_detail_report", XitiUtils.NAVIGATION);
                if (acAd != null) {
                    Intent intent = new Intent(AdViewActivity.this, EmailReportActivity.class);
                    intent.putExtra(ADVERTISER_NAME, acAd.getName());
                    intent.putExtra(AD_TITLE, acAd.getSubject());
                    intent.putExtra(AD_EMAIL, acAd.getEmail());
                    intent.putExtra(LIST_ID, acAd.getListId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
            case R.id.menu_share:
                if (acAd != null) {
                    String shareUrl;
                    if (!ACUtils.isEmpty(acAd.getUrl())) {
                        shareUrl = acAd.getUrl();
                    } else {
                        shareUrl = Config.shareHost + "/vi/" + acAd.getListId() + ".htm";
                    }
                    CustomizedIntentActionUtils.customShare(categoryId, XitiUtils.AD_VIEW, acAd.getSubject(), shareUrl, this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleFavourite() {
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
            MudahUtil.showExceedMaxFavouriteResult(this);
        }

        if (!isExceedMax) {
            miFavourite.setIcon(isFavourited ?
                    R.drawable.ic_favorite_saved
                    : R.drawable.ic_favorite_white);
            MudahUtil.showFavouriteResult(this, isFavourited);
            sendTagFavourite();
        }
    }

    private void sendTagFavourite() {
        if (!ACUtils.isEmpty(parentCategoryName) && acAd != null && !ACUtils.isEmpty(acAd.getCategoryName())) {
            String pageAndClickName = isFavourited ? getString(R.string.saved_to_favourites) : getString(R.string.removed_my_favourites);
            if (isFavourited) {
                tagGravityEvent(GravityUtils.EVENT_TYPE_ADD_TO_FAVORITES);
                KahunaHelper.tagEventWithAttribute(KahunaHelper.FAV_EVENT, acAd, KahunaHelper.ACTION_FAV);
                KahunaHelper.tagAttributes(KahunaHelper.PAGE_AD_FAV, KahunaHelper.LAST_TITLE_FAV, acAd.getSubject());
                AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.FAV_AD_EVENT, acAd);
            }

            tagTealiumViewWithCustomPageNameAndL2(pageAndClickName, XitiUtils.LEVEL2_FAVOURITE_ID);
            //Xiti
            EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getApplicationContext(), pageAndClickName, XitiUtils.LEVEL2_FAVOURITE_ID, xitiMapX);
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_FAVOURITE_ID, pageAndClickName + XitiUtils.CHAPTER_SIGN + TealiumHelper.getSellerTypeText(dataTealium.get(TealiumHelper.SELLER_TYPE)) + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(acAd.getCategoryName()) + XitiUtils.CHAPTER_SIGN + dataTealium.get(TealiumHelper.REGION_NAME), XitiUtils.NAVIGATION);
        }
    }

    private void delete() {
        new DeleteAdEmailFragment().show(getSupportFragmentManager(), "dialog");
    }

    public int getCurrentPosition() {
        int currPos = 0;
        if (mPager != null)
            currPos = mPager.getCurrentItem();
        return currPos;
    }

    public GoogleApiClient getmClient() {
        return mClient;
    }

    public void disableContactButtons() {
        Log.d();
        clearActionBar();
        menuStateOfEnabled = false;

        updateUIDone = true;
        rebuildMenu();
    }

    private void rebuildMenu() {
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            invalidateOptionsMenu();
        } else if (this.menu != null) {
            this.onPrepareOptionsMenu(this.menu);
        }
    }

    public void resetUpdateUIFlag() {
        updateUIDone = false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void updateUI() {
        Log.d("currentPosition: " + mPager.getCurrentItem() + ", updateUIDone: " + updateUIDone);
        //to prevent double UI update
        if (!updateUIDone) {
            MyAdapter adapter = (MyAdapter) mPager.getAdapter();
            AdViewFragment adViewFragment = ((AdViewFragment) adapter.instantiateItem(mPager, mPager.getCurrentItem()));
            acAd = adViewFragment.getData();
            if (acAd == null || acAd.isDeletedAd()) {
                ACUtils.logCrashlytics("AdView deletedAd");
                disableContactButtons();
            } else if (acAd != null) {
                adViewFragment.getSimilarRecommendations();
                checkIsMyAd();

                //re-enable the menu
                if (!menuStateOfEnabled) {
                    menuStateOfEnabled = true;
                    rebuildMenu();
                }
                updateFavouriteStatus(acAd.getListId());
                ACUtils.logCrashlytics("AdView listId: " + acAd.getListId());

                //update categoryId
                if (!ACUtils.isEmpty(acAd.getCategoryId())) {
                    try {
                        categoryId = Integer.parseInt(acAd.getCategoryId());
                    } catch (NumberFormatException e) {
                        categoryId = 0;
                    }
                }
                //update regionId
                if (!ACUtils.isEmpty(acAd.getRegionId()))
                    regionId = Integer.parseInt(acAd.getRegionId());
                if (acAd.getPhone().trim().equals(Constants.EMPTY_STRING)) {
                    setEnabled(llPhone, false);
                    setEnabled(llSms, false);
                } else {
                    setEnabled(llPhone, true);
                    //only enable SMS for mobile phone
                    if (acAd.getPhone().trim().startsWith(PREFIX_MOBILE_PHONE)) {
                        setEnabled(llSms, true);
                    } else {
                        setEnabled(llSms, false);
                    }
                    llPhone.setOnClickListener(adviewOnClickListener);
                    llSms.setOnClickListener(adviewOnClickListener);
                }
                //update storeVerified
                if (!ACUtils.isEmpty(acAd.getStoreVerified())) {
                    storeVerified = acAd.getStoreVerified();
                }

                setEnabled(llChat, acAd.isChatEnable());
                setEnabled(llEmail, true);
                llEmail.setOnClickListener(adviewOnClickListener);
                // load more Ads
                if (!isLoading() && (totalAds < grandTotal) && (totalAds - currentPosition) < MIN_REMAINING_ADS) {
                    loadAndAppend();
                }
                sendAppIndexing();
                sendTagging();

                parameters = fbAdview(parentCategoryName, acAd);
                logger.logEvent(ADVIEW, parameters);
                updateUIDone = true;
                if (!isSkipTutorial() && !tutorialVerifiedOpen && !isVerifiedTutorialDone() && isVerifiedSeller()) {
                    prepareTutorial();
                }

            }
        }
    }

    private void checkIsMyAd() {
        if (Config.userAccount.isLogin() && Config.userAccount.getUserId().equals(acAd.getUserId())) {
            isMyAds = true;
        } else {
            isMyAds = false;
        }
    }

    private void updateChatButton(boolean isEnable) {
        llChat.setEnabled(isEnable);
    }

    private void loadAndAppend() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_LOAD_LIST_ID) != null) {
            LoadMoreListIDFragment loadFragment = (LoadMoreListIDFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_LOAD_LIST_ID);
            isLoading = true;
            loadFragment.loadAndAppend();
            Log.d("called loadAndAppend()");
        }
    }

    private void sendAppIndexing() {
        if (Config.googleAppIndexing && mClient != null && acAd != null) {
            // Connect your client
            mClient.connect();

            // Define a title for your current page, shown in autocompletion UI
            final String title = acAd.getSubject();
            final Uri appUri = Config.BASE_APP_URI.buildUpon().appendPath(acAd.getListId() + ".htm").build();
            Log.d("API title=" + title + ", URI=" + appUri);

            Action viewAction = Action.newAction(Action.TYPE_VIEW, title, appUri);

            // Call the App Indexing API view method
            AppIndex.AppIndexApi.start(mClient, viewAction);
        }
    }

    protected void onPause() {
        super.onPause();
        Log.d();
        if (toBeRemovedFromFav.size() > 0 || toBeAddedToFav.size() > 0) {
            MudahUtil.updateAdViewDAOThread(adViewFavouritesDAO, toBeRemovedFromFav, toBeAddedToFav);
        }
    }

    @Override
    public void onStop() {
        Log.d();
        if (Config.googleAppIndexing && acAd != null && !ACUtils.isEmpty(acAd.getSubject())) {
            final String title = acAd.getSubject();
            final Uri appUri = Config.BASE_APP_URI.buildUpon().appendPath(acAd.getListId() + ".htm").build();

            Action viewAction = Action.newAction(Action.TYPE_VIEW, title, appUri);
            AppIndex.AppIndexApi.end(mClient, viewAction);

            mClient.disconnect();
        }
        super.onStop();
    }

    public void updateFavouriteStatus(int listId) {
        if (miFavourite != null) {
            isFavourited = MudahUtil.isFavouritedAd(listId);
            miFavourite.setIcon(isFavourited ?
                    R.drawable.ic_favorite_saved
                    : R.drawable.ic_favorite_white);
        }
    }

    private void checkSharePackagesInstalled() {
        for (int i = 0; i < Config.SHARE_PACKAGES.length; i++) {
            if (!appInstalledOrNot(Config.getPackage(Config.SHARE_PACKAGES[i]))) {
                switch (Config.SHARE_PACKAGES[i]) {
                    case Constants.FACEBOOK:
                        facebook = false;
                        break;
                    case Constants.INSTAGRAM:
                        instagram = false;
                        break;
                    case Constants.WHATSAPP:
                        whatsapp = false;
                        break;
                }
            }
        }

        if (!canSendSMS()) {
            sms = false;
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = this.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private boolean canSendSMS() {
        PackageManager pm = this.getPackageManager();
        return (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) || pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA));
    }

    private void setEnabled(ViewGroup layout, boolean enable) {
        layout.setEnabled(enable);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            view.setEnabled(enable); // Or whatever you want to do with the view.
            if (enable) {
                view.setAlpha(1f);
            } else {
                view.setAlpha(.25f);
            }
        }
    }

    private void clearActionBar() {
        setEnabled(llPhone, false);
        setEnabled(llEmail, false);
        setEnabled(llChat, false);
        setEnabled(llSms, false);
    }

    private int getSellerTypeId() {
        // Ad source (Private(0), Company(1), ..)
        int sellerType = 1;
        if (!ACUtils.isEmpty(acAd.getCompanyAd())) {
            sellerType = Integer.valueOf(acAd.getCompanyAd()) + 1;
        }
        return sellerType;
    }

    private void xiTiUpdate() {
        // ---------------------- XiTi --------------------------------
        if (acAd != null) {
            xitiMapX = prepareXitiMap();
            EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_ADVIEW, getApplicationContext(), acAd.getListId() + Constants.UNDERSCORE
                            + MudahUtil.removeUnwantedSign(acAd.getSubject()),
                    Integer.valueOf(categoryId), xitiMapX);
        }
        // ------------------------------------------------------------
    }

    private Map<String, String> prepareXitiMap() {
        Map<String, String> xitiMapX = new HashMap<>();
        if (acAd != null) {
            if (!ACUtils.isEmpty(acAd.getParentCategoryId())) {
                xitiMapX.put(XitiUtils.CUST_VAR_MAIN_CATEGORY, acAd.getParentCategoryId());
            }
            xitiMapX.put(XitiUtils.CUST_VAR_REGION, regionId + Constants.EMPTY_STRING);
            xitiMapX.put(XitiUtils.CUST_VAR_SUB_REGION, acAd.getSubRegionName());
            xitiMapX.put(XitiUtils.CUST_VAR_AD_TITLE, MudahUtil.removeUnwantedSign(acAd.getSubject()));
            xitiMapX.put(XitiUtils.CUST_VAR_AD_SRC, getSellerTypeId() + Constants.EMPTY_STRING);
            // mapX.put("5", ) TODO: postcode is not available in API output
            xitiMapX.put(XitiUtils.CUST_VAR_AD_ID, acAd.getAdId());
            xitiMapX.put(XitiUtils.CUST_VAR_LIST_ID, acAd.getListId() + Constants.EMPTY_STRING);
            if (!ACUtils.isEmpty(acAd.getTypeAd()))
                xitiMapX.put(XitiUtils.CUST_VAR_AD_TYPE, XitiUtils.getAdTypeValue(acAd.getTypeAd()) + Constants.EMPTY_STRING);
            //Track the ad that users click after using keyword
            if (searchParam != null && searchParam.get(Constants.KEYWORD) != null && !ACUtils.isEmpty(searchParam.get(Constants.KEYWORD).toString())) {
                int pageNo = currentPosition + 1; //currentPosition starts from 0 but Xiti expects a page no from 1
                xitiMapX.put(XitiUtils.SEARCH_KEYWORD_PAGE_NO, searchParam.get(Constants.KEYWORD) + XitiUtils.SEARCH_KEYWORD_PAGE_NO + pageNo);//e.g. toyota+delimiter+10
            }
            //User Account
            if (Config.userAccount.isLogin()) {
                xitiMapX.put(XitiUtils.CUST_VAR_USER_ID, Config.userAccount.getUserId());
                xitiMapX.put(XitiUtils.CUST_VAR_USER_EMAIL, Config.userAccount.getEmail());
            }
        }
        return xitiMapX;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("savedCategoryId", categoryId);
        savedInstanceState.putInt("savedRegionId", regionId);
        if (null != mPager)
            currentPosition = mPager.getCurrentItem();
        savedInstanceState.putInt("savedListViewPosition", currentPosition);
        savedInstanceState.putInt("savedGrandTotal", grandTotal);
        savedInstanceState.putStringArrayList("savedAllListsId", (ArrayList<String>) allListIDList);
        savedInstanceState.putSerializable("savedSearchParam", searchParam);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toBeRemovedFromFav.clear();
        toBeAddedToFav.clear();
        if (mPager != null) {
            mPager.setCurrentItem(currentPosition);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        miFavourite = null;
        if (findViewById(R.id.ll_adview_root_view) != null) {
            ACUtils.unbindDrawables(findViewById(R.id.ll_adview_root_view));
        }
        System.gc();
        if (rateDialog != null && rateDialog.isShowing()) {
            rateDialog.dismiss();
        }
        if (showcaseHelper != null) {
            showcaseHelper.releaseHostActivity();
        }
    }

    private boolean isLoading() {
        return isLoading;
    }

    public void sendDeleteEmailTmp(String email) {
        if (acAd != null)
            getSupportLoaderManager().initLoader(LOADER_SENDDELETEMAIL, null, sendDeleteEmail(email));
    }

    public BlocketLoader.Callbacks sendDeleteEmail(String email) {
        DialogUtils.showProgressDialog(this, getString(R.string.loading));
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("list_id", acAd.getListId());
        params.put("email", email);
        return new BlocketLoader.Callbacks(Method.POST, "delete", params, this) {
            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                Log.d();
                onDeleteEmailSent(data);
                DialogUtils.hideProgressDialog(AdViewActivity.this);
                getSupportLoaderManager().destroyLoader(loader.getId());
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                // general failure
                DialogUtils.hideProgressDialog(AdViewActivity.this);
                DialogUtils.showGeneralErrorAlert(AdViewActivity.this);
                getSupportLoaderManager().destroyLoader(loader.getId());
            }
        };
    }

    protected void onDeleteEmailSent(JSONObject data) {
        Log.d();
        String status = data.optString("status");
        StringBuilder stringBuilder = new StringBuilder();
        if (status != null) {
            if (status.equals("OK") || status.equals("TRANS_OK")) {
                Toast.makeText(AdViewActivity.this, R.string.delete_ad_toast, Toast.LENGTH_LONG).show();
            } else {
                JSONObject errors;

                try {
                    errors = data.getJSONObject("error");
                    JSONArray errParams;
                    errParams = errors.getJSONArray("parameters");
                    int errParamsLength = errParams.length();
                    for (int i = 0; i < errParamsLength; i++) {

                        if (errParams.optString(i).contains("MISSING")) {
                            stringBuilder.append(getResources().getString(R.string.delete_ad_empty_email)).append("\n");
                        } else if (errParams.optString(i).contains("WRONG")) {
                            stringBuilder.append(getResources().getString(R.string.delete_ad_wrong_email)).append("\n");
                        } else {
                            stringBuilder.append(getResources().getString(R.string.delete_ad_invalid_email)).append("\n");
                        }
                    }

                    Dialog dialog = new AlertDialog.Builder(AdViewActivity.this)
                            .setIcon(null)
                            .setTitle(R.string.mail_advertiser_alert_dialog_title)
                            .setMessage(stringBuilder.toString())
                            .setPositiveButton(
                                    R.string.mail_advertiser_alert_dialog_button_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            new DeleteAdEmailFragment().show(getSupportFragmentManager(), "dialog");
                                        }
                                    }
                            ).create();

                    dialog.show();

                } catch (JSONException e) {
                    ACUtils.debug(e);
                }
            }
        }
    }

    private void sendTagging() {
        MudahUtil.prepareParentCategoryInfo(categoryId + Constants.EMPTY_STRING, acAd);
        parentCategoryName = acAd.getParentCategoryName();
        xiTiUpdate();
        tagTealiumView();
        KahunaHelper.tagEventWithAttribute(KahunaHelper.ADVIEW_EVENT, acAd, KahunaHelper.ACTION_VIEW);
        KahunaHelper.tagAttributes(KahunaHelper.PAGE_AD_VIEWED, KahunaHelper.LAST_TITLE_VIEWED, acAd.getSubject());
        tagGravityEvent(GravityUtils.EVENT_TYPE_VIEW);
        AppsFlyerUtils.sendConversionTag(this, AppsFlyerUtils.AppsFlyerTags.PROD_VIEW, acAd);
        sendTagAd(categoryId);
    }

    private void tagGravityEvent(String eventType) {
        if (acAd != null && acAd.getListId() != 0) {
            GravityUtils.sendEventAsyncWithEventTypeAndItemId(eventType, acAd.getListId() + Constants.EMPTY_STRING);
        }
    }

    private void tagGravityEventExtra(String eventType, String methodType) {
        GravityEvent event = new GravityEvent();
        event.eventType = eventType;
        event.itemId = acAd.getListId() + Constants.EMPTY_STRING;
        GravityNameValue method = new GravityNameValue(GravityUtils.FIELD_METHOD, methodType);
        event.nameValues = new GravityNameValue[]{method};

        GravityUtils.sendEventAsync(event);
    }

    private Map<String, String> prepareTealiumTag() {
        String level2 = XitiUtils.getCustomLevel2Tag(Integer.valueOf(categoryId), XitiUtils.MODE_ADVIEW);
        Map<String, String> dataTealium = null;

        if (acAd != null) {
            String listId = acAd.getListId() + Constants.EMPTY_STRING;
            String title = MudahUtil.removeUnwantedSign(acAd.getSubject());
            String pageName = listId + Constants.UNDERSCORE + title;

            String sellerType = MudahUtil.getSellerTypeStr(acAd.getCompanyAd());

            dataTealium = Tealium.map(
                    TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_VIEW,
                    TealiumHelper.PAGE_NAME, pageName,
                    TealiumHelper.REGION_ID, acAd.getRegionId(),
                    TealiumHelper.REGION_NAME, acAd.getRegion(),
                    TealiumHelper.SUBREGION_ID, acAd.getSubRegionId(),
                    TealiumHelper.SUBREGION_NAME, acAd.getSubRegionName(),
                    TealiumHelper.CATEGORY_NAME, acAd.getCategoryName(),
                    TealiumHelper.PARENT_CATEGORY_ID, acAd.getParentCategoryId(),
                    TealiumHelper.PARENT_CATEGORY_NAME, acAd.getParentCategoryName(),
                    TealiumHelper.AD_TYPE, acAd.getTypeAd(),
                    TealiumHelper.SELLER_TYPE, sellerType,
                    TealiumHelper.AD_SELLER_TYPE, acAd.getAdSellerType(),
                    TealiumHelper.LIST_ID, listId,
                    TealiumHelper.AD_ID, acAd.getAdId(),
                    TealiumHelper.AD_TITLE, title,
                    TealiumHelper.STORE_ID, acAd.getStoreId(),
                    TealiumHelper.XTN2, level2);

            if (!ACUtils.isEmpty(acAd.getWhatsApp())) {
                dataTealium.put(TealiumHelper.MESSAGE_BY_WHATSAPP, TealiumHelper.YES);
            }

            if (ACUtils.isEmpty(acAd.getPhone())) {
                dataTealium.put(TealiumHelper.HIDE_PHONE_NUMBER, TealiumHelper.YES);
            }

            if (!ACUtils.isEmpty(acAd.getCertified())) {
                dataTealium.put(TealiumHelper.CERTIFIED, TealiumHelper.YES);
            }

            dataTealium.put(TealiumHelper.NUMBER_OF_PHOTOS, acAd.getImageCount() + Constants.EMPTY_STRING);

            if (!ACUtils.isEmpty(acAd.getCompanyRoc())) {
                dataTealium.put(TealiumHelper.COMPANY_ROC, acAd.getCompanyRoc());
            }

            TealiumHelper.prepareParamsTagging(dataTealium, acAd.getParameters());

        }

        return dataTealium;
    }

    private void tagTealiumView() {
        dataTealium = prepareTealiumTag();
        if (dataTealium != null) {
            TealiumHelper.track(this, dataTealium, Tealium.VIEW);
        }
    }

    private void tagTealiumViewWithCustomPageNameAndL2(String newPageName, String level2) {
        if (dataTealium == null || dataTealium.size() == 0) {
            dataTealium = prepareTealiumTag();
        }

        if (dataTealium != null) {
            dataTealium.put(TealiumHelper.PAGE_NAME, newPageName);
            dataTealium.put(TealiumHelper.XTN2, level2);
            TealiumHelper.track(this, dataTealium, Tealium.VIEW);
        }
    }

    private void tagTealiumEvent(String adReplyType, String adReplyTypeId, String eventName) {
        if (dataTealium == null) {
            dataTealium = prepareTealiumTag();
        }

        if (dataTealium != null) {
            // prevent data in "dataTealium" got modified
            Map<String, String> copyOfData = new HashMap<>(dataTealium);
            //page
            copyOfData.put(TealiumHelper.PAGE_NAME, adReplyType);
            copyOfData.put(TealiumHelper.XTN2, XitiUtils.LEVEL2_AD_REPLY_ID);
            TealiumHelper.track(this, copyOfData, Tealium.VIEW);

            //click
            copyOfData.put(TealiumHelper.AD_REPLY_TYPE, adReplyTypeId);
            copyOfData.put(TealiumHelper.PAGE_NAME, eventName);
            copyOfData.put(TealiumHelper.CLICK_TYPE, TealiumHelper.CLICK_TYPE_ACTION);
            TealiumHelper.track(this, copyOfData, Tealium.EVENT);

        }
    }

    @Override
    public void onBackPressed() {
        //Overwrite this method, so that deep linking users can click back to return to browser
        finish();
    }

    //Tracking on every fifth ad for every sub category
    public void sendTagAd(int categoryId) {
        AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.TRACK_AD, acAd, viewType);
    }

    @Override
    public void sendFalseCrashLog(String message) {
        ACUtils.logCrashlytics(message);
        try {
            throw new RuntimeException(message);
        } catch (RuntimeException ignore) {
            ACUtils.debug(ignore, false);
        }
    }

    public static class MyAdapter extends FragmentStatePagerAdapter {

        WeakReference<List<String>> list = new WeakReference<List<String>>(new ArrayList<String>());

        public MyAdapter(FragmentManager fm, List<String> newListIDList) {
            super(fm);
            list = new WeakReference<>(newListIDList);
        }

        public void setItem(List<String> newListIDList) {
            list = new WeakReference<>(newListIDList);
        }

        @Override
        public int getCount() {

            List<String> listIDList = list.get();
            return listIDList.size();
        }

        @Override
        public Fragment getItem(int position) {
            int listId = 0;
            List<String> listIDList = list.get();
            if (null != listIDList && getCount() > position) {
                listId = Integer.parseInt(listIDList.get(position));
            }
            return AdViewFragment.newInstance(listId, position);
        }
    }

    public static class LoadMoreListIDFragment extends Fragment {
        WeakReference<AdViewActivity> localActivity;

        public LoadMoreListIDFragment() {

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getActivity() instanceof AdViewActivity) {
                localActivity = new WeakReference(getActivity());
            }
        }

        private void loadAndAppend() {
            BlocketLoader.Callbacks callback = moreAdViewCallback();
            if (callback != null) {
                getActivity().getSupportLoaderManager().restartLoader(LOADER_MORE_AD, null, callback);
            }
        }

        private BlocketLoader.Callbacks moreAdViewCallback() {
            final AdViewActivity localAdView = localActivity.get();
            if (localAdView == null)
                return null;
            // calculate for next offset(e.g.If limit is 10, page 1 => offset 0
            // page 2 => offset 10, page 3 => offset 20)
            localAdView.currentPage = (((Integer) localAdView.searchParam.get("o")) / PagingBlocketLoaderFragment.VISIBLE_THRESHOLD) + 1;
            int offset = (localAdView.currentPage) * PagingBlocketLoaderFragment.VISIBLE_THRESHOLD;
            localAdView.searchParam.put("o", offset); // starts from 1
            Log.d("currentPage= " + localAdView.currentPage + ", searchParam= " + localAdView.searchParam.toString());
            final AdViewActivity adViewActivity = ((AdViewActivity) getActivity());
            return new BlocketLoader.Callbacks(Method.GET, "list_id", localAdView.searchParam, adViewActivity) {
                @Override
                public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                    super.onLoadFinished(loader, data);
                }

                @Override
                public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                    // Add more Ad ID
                    try {
                        int page = (((Integer) loader.getParams().get("o")) / PagingBlocketLoaderFragment.VISIBLE_THRESHOLD) + 1;
                        Log.d(" search page " + page);
                        if (localAdView.currentPage < page) {
                            localAdView.currentPage = page;

                            String strGrandTotal = data.getString("filtered");
                            if (!ACUtils.isEmpty(strGrandTotal))
                                localAdView.grandTotal = Integer.parseInt(strGrandTotal);
                            else
                                localAdView.grandTotal = 0;

                            JSONArray items = data.optJSONArray("ads");
                            if (items != null) {
                                List<String> newList = new ArrayList<String>();
                                for (int i = 0; i < items.length(); i++) {
                                    newList.add(items.getJSONObject(i).getString("list_id"));
                                }
                                if (newList.size() > 0) {
                                    localAdView.allListIDList.addAll(newList);
                                    localAdView.totalAds = localAdView.allListIDList.size();
                                    if (localAdView.mPager != null) {
                                        ((MyAdapter) localAdView.mPager.getAdapter()).setItem(localAdView.allListIDList);
                                        localAdView.mPager.getAdapter().notifyDataSetChanged();
                                    }
                                    Crashlytics.getInstance().core.setInt("total_adview", localAdView.totalAds);
                                    Log.d(" new allListIDList size= " + localAdView.totalAds);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        ACUtils.debug(e, "AdView_moreAdView", data.toString());
                    }
                    localAdView.isLoading = false;
                }

                @Override
                public void onLoadError(BlocketLoader loader, JSONObject data) {
                    localAdView.isLoading = false;
                    Log.d(Constants.EMPTY_STRING);
                }
            };
        }
    }

    private class PageListener extends SimpleOnPageChangeListener {

        //1 when you start dragging, 2 when you let go and 0 when the ViewPager has stopped scrolling
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            if (state == 0) {//finish scrolling, then update the bottom action bar
                int previousPosition = currentPosition;
                currentPosition = mPager.getCurrentItem();
                //reset the updateUIDone to false, and set it to true after successfully update.
                //if the fragment data is null, updateUIDone will remain 'false'.
                //During Fragmemt.onResume, the data will be loaded and updateUI() will be called again.
                updateUIDone = false;
                clearActionBar();
                updateUI();
                if (!clickToNavigate) {
                    if (previousPosition > currentPosition)
                        EventTrackingUtils.sendClickByCategoryId(dataTealium, categoryId, XitiUtils.AD_VIEW,
                                "Ad_detail_swipe_previousAd", XitiUtils.NAVIGATION);
                    else if (previousPosition < currentPosition)
                        EventTrackingUtils.sendClickByCategoryId(dataTealium, categoryId, XitiUtils.AD_VIEW,
                                "Ad_detail_swipe_nextAd", XitiUtils.NAVIGATION);
                } else
                    clickToNavigate = false;//clear the flag

                Log.d("remaining ads " + (totalAds - currentPosition)
                        + ", currentPosition=" + currentPosition + ",loading="
                        + isLoading + ",totalAds= " + totalAds + " of " + grandTotal);
            }
        }
    }
}
