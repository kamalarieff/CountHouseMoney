package com.mudah.my.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.lib701.connection.ACBlocketConnection;
import com.lib701.connection.HttpClient;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.fragments.ProgressDialogFragment;
import com.lib701.fragments.RetainedDialogFragment;
import com.lib701.utils.ACUtils;
import com.lib701.utils.DialogUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.adapters.CategoryParamsAdapter;
import com.mudah.my.adapters.InsertAdCategoryParamsAdapter;
import com.mudah.my.adapters.InsertAdCategoryParamsAdapter.PostedBy;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.fragments.AlertDialogFragment;
import com.mudah.my.fragments.InsertAdDetailsFragment;
import com.mudah.my.fragments.InsertAdPasswordFragment;
import com.mudah.my.fragments.InsertAdPreviewFragment;
import com.mudah.my.fragments.InsertAdProfileFragment;
import com.mudah.my.fragments.InsertAdSuccessFragment;
import com.mudah.my.fragments.LoadingFragment;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.NotificationBuilderHelper;
import com.mudah.my.helpers.PDPNHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.DraftAdNotificationModel;
import com.mudah.my.models.InactiveInsertAdNotificationModel;
import com.mudah.my.models.InsertAdAdapterTagModel;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.AppsFlyerUtils;
import com.mudah.my.utils.CustomizedIntentActionUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;
import com.tealium.library.Tealium;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertAdActivity extends MudahBaseActivity {
    //Page Name
    public static final String[] INSERT_AD_PAGE = new String[]{"NonPN_Insert_Ad_Details_(Native)", "NonPN_Insert_Ad_Personal_Details_(Native)", "NonPN_Insert_Ad_Preview_(Native)", "NonPN_Insert_Ad_Successful_(Native)"};
    public static final String[] INSERT_AD_PAGE_ONBOARD = new String[]{"NonPN_Insert_Ad_Details_Onboard_(Native)", "NonPN_Insert_Ad_Personal_Details_Onboard_(Native)", "NonPN_Insert_Ad_Preview_Onboard_(Native)", "NonPN_Insert_Ad_Successful_Onboard_(Native)"};
    public static final String[] INSERT_AD_SAVE_DRAFT_PAGE = new String[]{"NonPN_Insert_Ad_Detail_Save_Draft_(Native)", "NonPN_Insert_Ad_Personal_Details_Save_Draft_(Native)", "NonPN_Insert_Ad_Preview_Save_Draft_(Native)"};
    public static final String PAGE_1 = "1";
    public static final String PAGE_2 = "2";
    public static final String UNDERSCORE = "_";
    public static final int AD_DETAIL_FRAGMENT = 0;
    public static final int AD_PROFILE_FRAGMENT = 1;
    public static final int AD_SUCCESS_PAGE = 3;
    public static final int AD_PREVIEW_FRAGMENT = 2;
    public static final String INSERT_AD_CONNECTION_LOST = "Insert_Ad_Connection_Lost";
    public static final String CLEAN_DRAFT = "Clean_Draft";
    public static final String SUFFIX = "suffix";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "passwd";
    protected static final String TAG_ERROR_DIALOG = "error_dialog";
    private static final String CONDITION_NEW = "2";
    private static final String CONDITION_USED = "1";
    private static final String AD_INSERT = "adInsert";
    private static final String[] PROFILE_FIELDS = new String[]{"subarea", "region", "phone", "email", "name", "whats_app", "phone_hidden", "company_ad"};
    private static final int PROGRESS_INTERVAL = 1000;//Progress bar update interval in ms
    private static final int MAX_PROGRESS_SLEEPING_TIME = HttpClient.TIMEOUT_IN_SEC;//this should match with the timeout in ACRESTClient
    //private static final int LOADER_PROFILE = 1;
    private static final int LOADER_SUBMIT = 2;
    private static final int LOADER_VALIDATE = 3;
    private static final String STATE_NAME = "name";
    private static final String STATE_IS_SUBMITTING = "is_submitting";
    private static final String TAG_CONFIRM_CONTINUE_DIALOG = "confirm_continue_dialog";
    private static final String DRAFT = "Draft_";
    private static final String SELL_ITEM = "Sell Item";
    private static final String PHOTOS_AND_DESCRIPTION = "Photos & Description";
    private static final String CONTACT_AND_LOCATION = "Contact & Location";
    private static final String PREVIEW_AND_SUBMIT = "Preview & Submit";
    public static boolean isStartActivityForResult = false; //used for tracking if onStop is called due to startActivityForResult()
    public static boolean isSellerOnBoardViewed = false;
    public static JSONObject paramsObj;
    private static boolean needsToRequestFocus;
    private static boolean isSubmitting;
    private final String PASSWORD_VERIFICATION = "passwd_ver";
    private final int SELLER_ONBOARD_COOKIE_EXPIRY = 20;
    private final int SELLER_ONBOARD_TIMESTAMP_DEFAULT = 0;
    protected JSONObject lastSavedState;
    InsertAdDetailsFragment referenceInsertAdDetailsFragment;
    InsertAdProfileFragment referenceInsertAdProfileFragment;
    Bundle parameters;
    private String insertAdEmail;
    private String insertAdSubject;
    private ViewPager vpContent;
    private ContentAdapter contentAdapter;
    private String name;
    private LoadingFragment fLoading;
    private Context context;
    private String tagInsertAdProfileFragment;
    private String tagInsertAdDetailsFragment;
    private String selectedPostAs;
    private String draftJson;
    private JSONObject previewParams;
    private String categoryName;
    private String regionName;
    private String categoryParentName = Constants.EMPTY_STRING;
    private String subRegionName = Constants.EMPTY_STRING;
    private boolean isClickedDraftAdReminder = false;
    private String selectedAdType;
    private AppEventsLogger logger;
    private String strPrefixPageName = Constants.EMPTY_STRING;
    private String userIdAfterInsertAd;
    private boolean optInMudahParterMailingList;
    private boolean optInMudahMailingList;
    private boolean isRequiredCertifiedAd;
    private AlertDialog dialog = null;
    private ActionBarHelper actionBar;
    private String INSERT_AD_DETAILS = "INSERT_AD_DETAILS";
    private String INSERT_AD_PROFILE = "INSERT_AD_PROFILE";
    private String INSERT_AD_SUBMIT = "INSERT_AD_SUBMIT";
    private String itemCondition;
    private ViewPager.OnPageChangeListener vpContentListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {

            Form form = contentAdapter.getForm(vpContent, position);

            if (form instanceof InsertAdPreviewFragment) {
                InsertAdPreviewFragment preview = (InsertAdPreviewFragment) form;
                preview.setCategoryParams(((InsertAdDetailsFragment) contentAdapter.getForm(vpContent, 0)).getCategoryParams());
                preview.setPreviewParams(previewParams);
                preview.updateUI();
            }
            if (position == AD_DETAIL_FRAGMENT) {
                setActionbar(PHOTOS_AND_DESCRIPTION);
            } else if (position == AD_PROFILE_FRAGMENT) {
                setActionbar(CONTACT_AND_LOCATION);
            } else {
                setActionbar(PREVIEW_AND_SUBMIT);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    public static void changeImageURLtoImageId(Map<String, Object> params) {
        int id = 0;
        while (params.containsKey("image_url" + id)) {
            String strUrl = (String) params.remove("image_url" + id);
            try {
                URL tmpURL = new URL(strUrl);
                //example: path = /wm_images/36/3611995133.jpg
                String[] pathName = tmpURL.getPath().split("/");
                String imageId = pathName[pathName.length - 1];
                params.put("image_id" + id, imageId);
            } catch (IOException ioException) {
                ACUtils.debug(ioException, "ioException", "invokeURL cannot be passed to URL");
            }
            id++;
        }
    }

    /**
     * Error object from Trans and API are in different format.
     * We need to transform API's error to be the same format as Trans's error
     * E.g. Trans's error { "subject":"ERROR_SUBJECT_MISSING", "subject_error_label": "Enter ad title"}
     * E.g. API's error {"message":"ERROR_SUBJECT_MISSING", "description":"Enter ad title", "name":"subject"}
     *
     * @param errors in API's format
     * @return errors in Trans's error format
     */
    public static JSONObject setErrorKeysValues(JSONObject errors) {
        JSONObject errorObj = new JSONObject();
        if (errors != null && errors.optJSONArray("parameters") != null) {

            JSONArray errorsArr = errors.optJSONArray("parameters");
            for (int k = 0; k < errorsArr.length(); k++) {
                JSONObject tmpObj = errorsArr.optJSONObject(k);
                if (tmpObj != null && !ACUtils.isEmpty(tmpObj.optString("name"))) {
                    try {
                        Log.d("errors: " + tmpObj.optString("name") + ", " + tmpObj.optString("description"));
                        errorObj.accumulate(tmpObj.optString("name"), tmpObj.optString("message"));//collect error key to be used for Xiti from error tracking,e.g. subject: ERROR_SUBJECT_MISSING
                        errorObj.accumulate(tmpObj.optString("name") + "_error_label", tmpObj.optString("description"));
                    } catch (JSONException e) {
                        ACUtils.debug(e);
                    }
                }
            }
        }
        return errorObj;
    }

    public static int setError(String errorMsg, TextView view) {
        if (!ACUtils.isEmpty(errorMsg)) {
            view.setError(ACUtils.getHtmlFromString(errorMsg).toString());
            tryToFocus(view);
            return 1;
        } else {
            view.clearFocus();
            view.setError(null);
            return 0;
        }
    }

    public static void tryToFocus(final View view) {
        //needsToRequestFocus flag helps to focus only the first error
        if (needsToRequestFocus) {
            needsToRequestFocus = false;
            if (view.isFocusableInTouchMode() == false) {
                view.setFocusableInTouchMode(true);
                view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(final View v, boolean hasFocus) {
                        v.setFocusableInTouchMode(false);
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                v.clearFocus();
                            }
                        });
                    }
                });
            }
            // post runnable in case view has not been attached to view tree yet
            view.post(new Runnable() {
                @Override
                public void run() {
                    //To Fixed: java.lang.ClassCastException: android.widget.TextView cannot be cast to android.view.ViewGroup
                    try {
                        view.requestFocus();
                    } catch (ClassCastException cc) {
                        ACUtils.debug(cc);
                    }
                }
            });
        }
    }

    public static boolean equals(JSONObject o1, JSONObject o2) {
        Log.d(o1 + Constants.EMPTY_STRING);
        Log.d(o2 + Constants.EMPTY_STRING);
        if (o1 == o2) return true;
        if (o1 == null || o2 == null) return false;
        if (o1.length() != o2.length()) {
            return false;
        }
        if (o1.length() > 0) {
            JSONArray names = o1.names();
            try {
                for (int i = 0; i < names.length(); i++) {
                    if (o1.get(names.getString(i)).equals(o2.optString(names.getString(i))) == false) {
                        return false;
                    }
                }
            } catch (JSONException e) {
                ACUtils.debug(e);
            }
        }
        return true;
    }

    public static boolean equalsIgnoreProfile(JSONObject o1, JSONObject o2) {
        Log.d(o1 + Constants.EMPTY_STRING);
        Log.d(o2 + Constants.EMPTY_STRING);
        if (o1 == o2) return true;
        if (o1 == null || o2 == null) return false;
        if (o1.length() != o2.length()) {
            return false;
        }
        if (o1.length() > 0) {
            JSONArray names = o1.names();
            List profileFields = Arrays.asList(PROFILE_FIELDS);
            try {
                for (int i = 0; i < names.length(); i++) {
                    if (!profileFields.contains(names.getString(i)) && o1.get(names.getString(i)).equals(o2.optString(names.getString(i))) == false) {
                        Log.d("Found this field not match: " + names.getString(i));
                        return false;
                    }
                }
                Log.d("Nothing Found");
            } catch (JSONException e) {
                ACUtils.debug(e);
            }
        }
        return true;
    }

    public String getSelectedAdType() {
        return selectedAdType;
    }

    public void setSelectedAdType(String selectedAdType) {
        this.selectedAdType = selectedAdType;
    }

    private void showProniagaMsg(int state) {
        Form form = contentAdapter.getForm(vpContent, 0);
        if (form instanceof InsertAdDetailsFragment) {
            InsertAdDetailsFragment adDetails = (InsertAdDetailsFragment) form;
            adDetails.saveShowProniagaMsgState(state);
        }

    }

    public void showCustomDialog(String errorMsg, int resId, final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(resId);
        TextView text = (TextView) dialog.findViewById(R.id.tv_dialog_text);
        text.setText(ACUtils.getHtmlFromString(errorMsg).toString());
        PreferencesUtils.getSharedPreferences(context).edit()
                .putInt(PreferencesUtils.INSERT_AD_PRONIAGA_MSG_STATE, 1)
                .apply();
        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.tv_dialog_cancel);
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button dialogButtonOK = (Button) dialog.findViewById(R.id.tv_dialog_ok);
        dialogButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Config.loginProNiagaUrl;
                if (!ACUtils.isEmpty(url)) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    context.startActivity(i);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        showProniagaMsg(1);
    }

    public String getPostedByOption() {
        return selectedPostAs;
    }

    public void updatePostedByOption(String selectedOption) {
        selectedPostAs = selectedOption;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove Material background, to prevent overdrawing
        getWindow().setBackgroundDrawable(null);
        AmplitudeUtils.InitializeAmplitude(this, getApplication());
        logger = AppEventsLogger.newLogger(this);

        Log.strictMode();
        Log.d();
        setContentView(R.layout.activity_insert_ad);
        setActionbar(PHOTOS_AND_DESCRIPTION);

        Intent intent = getIntent();
        if (intent != null) {
            boolean clean_draft = intent.getBooleanExtra(Form.CLEAN_DRAFT, false);
            if (clean_draft == true) {
                clearLocalDraft();
            }
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(Config.DRAFT_MAIN_NOTIFICATION)) {
                Log.d("DraftAdNotification user clicked notification");
                EventTrackingUtils.sendCampaign(this, XitiUtils.CAMPAIGN_NOTIFICATION_DRAFT_AD, XitiUtils.CAMPAIGN_CLICK);
                isClickedDraftAdReminder = true;
            } else {
                isClickedDraftAdReminder = false;
            }

            if (bundle != null && bundle.containsKey(Config.INSERTAD_MAIN_NOTIFICATION)) {
                Log.d("InsertAdNotification user clicked notification");
                EventTrackingUtils.sendCampaign(this, XitiUtils.CAMPAIGN_NOTIFICATION_INSERT_AD, XitiUtils.CAMPAIGN_CLICK);
            }

            //check if users come from deep link
            Uri uri = getIntent().getData();
            if (uri != null && Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                String strUrl = uri.toString();
                Log.d("Redirected from " + strUrl);
                AppsFlyerUtils.sendDeepLinkData(this);
                if (XitiUtils.initFromLastConfig(this) == null) {
                    Log.e("Xiti initialization is wrong");
                } else {
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_INSERT_AD_ID, "Deeplink_to_insert_ad", XitiUtils.NAVIGATION);
                }
            }
        }

        context = this;

        fLoading = (LoadingFragment) getSupportFragmentManager().findFragmentById(R.id.f_loading);
        fLoading.setOnRetryListener(new LoadingFragment.OnRetryListener() {
            @Override
            public void onRetry() {
                setDraft(null);
                updateLoading();
            }
        });

        contentAdapter = new ContentAdapter(this);
        vpContent = (ViewPager) findViewById(R.id.vp_content);
        vpContent.setAdapter(contentAdapter);
        vpContent.addOnPageChangeListener(vpContentListener);

        if (savedInstanceState != null) {
            name = savedInstanceState.getString(STATE_NAME);
            isSubmitting = savedInstanceState.getBoolean(STATE_IS_SUBMITTING);
        }

        // just to receive any callbacks, not to start loading
        if (getSupportLoaderManager().getLoader(LOADER_SUBMIT) != null) {
            getSupportLoaderManager().initLoader(LOADER_SUBMIT, null, newSubmitLoaderCallbacks(null));
        }
        if (getSupportLoaderManager().getLoader(LOADER_VALIDATE) != null) {
            ValidationSuccessCallback callback = (isSubmitting) ? newSubmitValidationCallback() : null;
            getSupportLoaderManager().initLoader(LOADER_VALIDATE, null, newValidateLoaderCallbacks(null, callback));
        }

        SharedPreferences sharedPreferences = PreferencesUtils.getSharedPreferences(InsertAdActivity.this);
        draftJson = sharedPreferences.getString(PreferencesUtils.INSERT_AD_DRAFT, null);
        if (draftJson == null) {
            sendTracking(AD_DETAIL_FRAGMENT);//send Xiti tracking for Ad Detail page
            KahunaHelper.tagEvent(KahunaHelper.CHECKED_OUT_FORM_INSERT_EVENT, new AdViewAd());
            AmplitudeUtils.tagEvent(AmplitudeUtils.CHECKED_OUT_FORM_INSERT_EVENT);
        }

        //Dismiss draft ad notification from the action bar
        MudahUtil.clearNotificationsByID(this, Config.NOTIFICATION_DRAFT_AD);

        //Dismiss insert ad notification from the action bar
        MudahUtil.clearNotificationsByID(this, Config.NOTIFICATION_INSERT_AD);

        isSellerOnBoardViewed(sharedPreferences.getLong(PreferencesUtils.ONBOARD_LAST_VISIT, 0));
    }

    private void isSellerOnBoardViewed(long savedTs) {
        if (savedTs == 0) {
            isSellerOnBoardViewed = false;
        } else {
            Calendar now = Calendar.getInstance();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(savedTs);

            int daysCount = MudahUtil.daysBetween(now, calendar);
            Log.d("daysCount " + daysCount);

            if (daysCount > SELLER_ONBOARD_COOKIE_EXPIRY) {
                isSellerOnBoardViewed = false;
            } else {
                isSellerOnBoardViewed = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.insert_ad_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                prev();
                return true;
            case R.id.b_save:
                saveLocalDraft();
                tagSavedDraft();
                EventTrackingUtils.sendClick(XitiUtils.LEVEL2_INSERT_AD_ID, INSERT_AD_SAVE_DRAFT_PAGE[getCurrentPage()], XitiUtils.NAVIGATION);
                Toast.makeText(InsertAdActivity.this, R.string.insert_ad_draft_save_updated, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_NAME, name);
        outState.putBoolean(STATE_IS_SUBMITTING, isSubmitting);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoading();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(SUFFIX)) {
            isSellerOnBoardViewed = bundle.getBoolean(SUFFIX);
            sendTracking(getCurrentPage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d();
        ACReferences ref = ACReferences.getACReferences();
        // Before users navigate to other pages, automatically save a draft
        // either the draft exists (because users have clicked "Save Draft" before)
        // or at least users have already chosen something like a category
        if (ref.getInsertAdCategoryId() != null || ref.isUploadedImage()) {
            saveLocalDraft();
            ref.setLoadDraft(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("isStartActivityForResult: " + isStartActivityForResult);
        InsertAdDetailsFragment details = (InsertAdDetailsFragment) contentAdapter.getForm(vpContent, AD_DETAIL_FRAGMENT);

        //Track when page got abandon, only for the detail page
        if (vpContent != null && vpContent.getCurrentItem() == AD_DETAIL_FRAGMENT && !isStartActivityForResult && details != null) {
            Map<String, String> params = details.getFilledFieldsParams();
            if (params.size() > 0) {
                String pageName = strPrefixPageName + InsertAdActivity.INSERT_AD_PAGE[AD_DETAIL_FRAGMENT] + "_Abandon";
                //some random Id for tagging purpose
                int pageTagId = 4;
                if (ACUtils.isEmpty(strPrefixPageName))
                    pageTagId = 6;
                EventTrackingUtils.sendPublisherTag(this, pageTagId, pageName, params);//send page and error
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (getDrawerLayoutUtils() != null) {
            if (getDrawerLayoutUtils().isMenuOpen()) {
                getDrawerLayoutUtils().setMenuClose();
            } else {
                prev();
            }
        } else {
            prev();
        }
    }

    public void next() {
        validate();
    }

    public JSONObject getParamsObj() {
        return paramsObj;
    }

    public void setParamsObj(JSONObject params) {
        paramsObj = params;
    }

    public void prev() {
        int prevItem = vpContent.getCurrentItem() - 1;
        if (prevItem >= 0) {
            vpContent.setCurrentItem(prevItem);
        } else if (isTaskRoot()) {
            // To make sure that Homepage is always the last view that user see before exiting app
            Intent intent = new Intent(InsertAdActivity.this, HomepageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            finish();
        }

    }

    public void validate() {
        isSubmitting = false;
        validate(null);
    }

    public void submit() {
        isSubmitting = true;
        validate(newSubmitValidationCallback());
    }

    public void submitPreview(String password, Map<String, String> p) {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> resultParams = new HashMap<String, Object>(getState());
        params.put(PASSWORD, password);
        params.put(PASSWORD_VERIFICATION, password);
        resultParams.putAll(params);
        resultParams.putAll(p);

        getSupportLoaderManager().restartLoader(LOADER_SUBMIT, null, newSubmitLoaderCallbacks(resultParams));
    }

    public Map<String, String> getState() {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < contentAdapter.getCount(); i++) {
            params.putAll(contentAdapter.getForm(vpContent, i).getState());
        }
        return params;
    }

    public void setState(Map<String, String> params) {
        for (int i = 0; i < contentAdapter.getCount(); i++) {
            contentAdapter.getForm(vpContent, i).setState(params);
        }
    }

    public void setState(JSONObject draft) throws JSONException {
        if (draft == null) {
            clearState();
            return;
        }

        JSONArray draftKeys = draft.names();
        Map<String, String> state = new HashMap<>();
        for (int i = 0; i < draft.length(); i++) {
            String key = draftKeys.getString(i);
            state.put(key, draft.getString(key));
        }
        setState(state);
    }

    public void clearState() {
        HashMap<String, String> emptyHash = new HashMap<>();
        //don't want to clear a user profile so that we can do the autocomplete for subsequent ad insertions
        for (int i = 0; i < contentAdapter.getCount(); i++) {
            if (i != AD_PROFILE_FRAGMENT)
                contentAdapter.getForm(vpContent, i).setState(emptyHash);
        }
        ACReferences ref = ACReferences.getACReferences();
        ref.clearACReferences();
        optInMudahMailingList = false;
        optInMudahParterMailingList = false;
        isRequiredCertifiedAd = false;
    }

    public int getCurrentPage() {
        return vpContent.getCurrentItem();
    }

    public void redrawCategoryParams(InsertAdCategoryParamsAdapter categoryParamsAdapter, LinearLayout llayout, String pageNo, JSONObject errors) {
        Log.d();
        if (categoryParamsAdapter != null && categoryParamsAdapter.getAdTypeLabels().length() > 0) {
            //remove all views
            int totalParamsCount;
            ArrayList<String> profileParamsKeyList = new ArrayList<>();
            referenceInsertAdDetailsFragment = (InsertAdDetailsFragment) getSupportFragmentManager()
                    .findFragmentByTag(getInsertAdDetailsFragmentTag());
            referenceInsertAdProfileFragment = (InsertAdProfileFragment) getSupportFragmentManager()
                    .findFragmentByTag(getInsertAdProfileFragmentTag());
            //page 2
            if (PAGE_2.equals(pageNo)) {
                profileParamsKeyList.add(CategoryParamsAdapter.STR_AD_TYPE);
                totalParamsCount = categoryParamsAdapter.getCount();
            } else { //page 1
                totalParamsCount = categoryParamsAdapter.getCount() + 1;
            }
            List<View> viewList;
            for (int i = 0; i < totalParamsCount; i++) {
                try {
                    if (i == 0 && PAGE_1.equals(pageNo)) {
                        if (referenceInsertAdDetailsFragment != null)
                            referenceInsertAdDetailsFragment.addTypeView();
                    } else {
                        if (PAGE_2.equals(pageNo))
                            viewList = categoryParamsAdapter.getViewList((i), llayout, pageNo);
                        else
                            viewList = categoryParamsAdapter.getViewList((i - 1), llayout);
                        // if the view is null, do not create view
                        if (viewList != null && viewList.size() > 0) {
                            for (View view : viewList) {
                                view.setSaveEnabled(false);
                                showError(view, errors);
                                llayout.addView(view);
                                //accumulating params keys belong to profile page
                                if (profileParamsKeyList != null && PAGE_2.equals(pageNo))
                                    profileParamsKeyList.add(getNameTag(view));
                            }
                        }

                    }
                } catch (RuntimeException e) {
                    Log.e(e);
                    startOverWithCleanDraft();
                }
            }
            if (profileParamsKeyList != null && profileParamsKeyList.size() > 0)
                referenceInsertAdProfileFragment.setParamsKeyList(profileParamsKeyList);
        }
    }


    private void startOverWithCleanDraft() {
        Intent intent = new Intent();
        intent.putExtra(CLEAN_DRAFT, true);
        intent.setClass(getApplicationContext(), InsertAdActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public String getNameTag(View view) {
        InsertAdAdapterTagModel tagObj = (InsertAdAdapterTagModel) view.getTag();
        if (tagObj != null) {
            return tagObj.getName();
        }
        return null;
    }

    private int showError(View view, JSONObject errors) {
        int errCount = 0;
        ViewGroup vg = ((ViewGroup) view);
        for (int i = 0; i < vg.getChildCount(); i++) {

            String name = ((InsertAdAdapterTagModel) vg.getTag()).getName();
            String errorMsg = getError(name, errors);

            if (!errorMsg.equalsIgnoreCase(Constants.EMPTY_STRING)) {

                if (vg.getChildAt(i).getId() == R.id.rl_param_wrapper) {
                    RelativeLayout rlParamWrapper = (RelativeLayout) vg.findViewById(R.id.rl_param_wrapper);
                    rlParamWrapper.findViewById(R.id.ll_item_info).setVisibility(View.GONE);
                    rlParamWrapper.findViewById(R.id.ll_item_error).setVisibility(View.VISIBLE);
                    ((TextView) rlParamWrapper.findViewById(R.id.ll_item_error)
                            .findViewById(R.id.tv_error_msg))
                            .setMovementMethod(LinkMovementMethod.getInstance()); // To handle a link in message to be linkable
                    ((TextView) rlParamWrapper.findViewById(R.id.ll_item_error)
                            .findViewById(R.id.tv_error_msg))
                            .setText(ACUtils.getHtmlFromString(errorMsg));
                    InsertAdActivity.tryToFocus(vg.getChildAt(i));
                    errCount++;
                }
            }
        }
        return errCount;
    }

    private String getError(String name, JSONObject errors) {
        String errorMsg = Constants.EMPTY_STRING;
        if (errors != null && errors.has(name + "_error_label")) {
            try {
                errorMsg = errors.getString(name + "_error_label");
            } catch (JSONException e) {
                ACUtils.debug(e);
            }
        }
        return errorMsg;
    }

    private int setErrors(JSONObject errors, boolean changePage) {
        needsToRequestFocus = true;
        int errorCount = 0;
        for (int i = 0; i <= vpContent.getCurrentItem(); i++) {
            errorCount += contentAdapter.getForm(vpContent, i).setErrors(errors);
            Log.d("Errors on fragment page -> " + contentAdapter.getForm(vpContent, i).getClass().getName() + " error:" + errorCount);
            if (changePage && errorCount > 0) {
                vpContent.setCurrentItem(i);
                break;
            }
        }

        int currPage = vpContent.getCurrentItem();
        updateCategoryAndRegionName();
        //send taging even there is an error
        tagSubmitRegardlessError(currPage);
        if (changePage && errorCount == 0 && currPage < contentAdapter.getCount() - 1) {
            boolean nextPage = false;
            if (currPage != AD_DETAIL_FRAGMENT) {
                if (errors.has(Constants.MESSAGE)) {
                    AlertDialogFragment
                            .instantiate(getString(R.string.general_error_title), errors.optString(Constants.MESSAGE), null)
                            .show(getSupportFragmentManager(), TAG_ERROR_DIALOG);
                } else if (errors.has(Constants.ERROR)) {
                    AlertDialogFragment
                            .instantiate(getString(R.string.general_error_title), errors.optString("error_error_label"), null)
                            .show(getSupportFragmentManager(), TAG_ERROR_DIALOG);
                    EventTrackingUtils.sendLevel2CustomVariableByName(XitiUtils.LEVEL2_INSERT_AD, InsertAdActivity.INSERT_AD_PAGE[1] + "_Error", "trans_error", errors.optString(Constants.ERROR));
                } else {
                    nextPage = true;
                }
            } else {
                nextPage = true;
            }
            if (nextPage) {
                sendTracking(currPage + 1);
                vpContent.setCurrentItem(currPage + 1);
            }

            if (currPage == AD_DETAIL_FRAGMENT) {
                tagFBEvent(INSERT_AD_DETAILS);
            } else if (currPage == AD_PROFILE_FRAGMENT) {
                tagFBEvent(INSERT_AD_PROFILE);
            }
        }
        return errorCount;
    }

    private void setActionbar(String subTitle) {
        actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar, SELL_ITEM, subTitle);
        showActionBarRedBorder(false);
    }

    private void sendTracking(int currPage) {
        sendTracking(Constants.EMPTY_STRING, currPage);
    }

    private void sendTracking(String prefixPage, int currPage) {
        String pageName;
        if (isSellerOnBoardViewed) {
            pageName = prefixPage + INSERT_AD_PAGE_ONBOARD[currPage];
        } else {
            pageName = prefixPage + INSERT_AD_PAGE[currPage];
        }

        tagXiti(prefixPage, pageName, XitiUtils.LEVEL2_INSERT_AD_ID);
        tagTealium(prefixPage, currPage, pageName, XitiUtils.LEVEL2_INSERT_AD_ID);
    }

    private void tagSubmitRegardlessError(int currPage) {
        AdViewAd acAd = getAdDetailForTag();

        if (currPage == AD_DETAIL_FRAGMENT) {
            KahunaHelper.tagEvent(KahunaHelper.SUBMITTED_DETAIL_INSERT_EVENT, acAd);
            KahunaHelper.tagInsertAdAttributes(KahunaHelper.LAST_TITLE_IN_PROGRESS, acAd);
            AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.AD_DETAILS_INSERT_EVENT, acAd);
        } else if (currPage == AD_PROFILE_FRAGMENT) {
            KahunaHelper.tagEvent(KahunaHelper.SUBMITTED_PROFILE_INSERT_EVENT, acAd);
            KahunaHelper.tagInsertAdAttributes(KahunaHelper.LAST_TITLE_IN_PROGRESS, acAd);
            AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.PROFILE_INSERT_EVENT, acAd);
        } else if (currPage == AD_PREVIEW_FRAGMENT) {
            KahunaHelper.tagEvent(KahunaHelper.PREVIEWED_INSERT_EVENT, acAd);
            KahunaHelper.tagInsertAdAttributes(KahunaHelper.LAST_TITLE_IN_PROGRESS, acAd);
            AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.AD_PREVIEW_EVENT, acAd);
        } else if (currPage == AD_SUCCESS_PAGE) {
            KahunaHelper.tagEventWithAttribute(KahunaHelper.LAST_POSTED_INSERT_EVENT, acAd, KahunaHelper.ACTION_POSTED);
            KahunaHelper.tagInsertAdAttributes(KahunaHelper.LAST_TITLE_POSTED, acAd);

            AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.INSERT_AD, acAd);
            //tag user id if user did not log in
            if (!Config.userAccount.isLogin()) {
                AmplitudeUtils.tagUserId(userIdAfterInsertAd);
                AmplitudeUtils.trackUserProperties(AmplitudeUtils.EMAIL, acAd.getEmail());
            }
        }
    }

    private void tagSavedDraft() {
        updateCategoryAndRegionName();
        AdViewAd acAd = getAdDetailForTag();
        tagSavedDraftKahuna(acAd, KahunaHelper.LAST_SAVED_DRAFT_INSERT_EVENT);
        AmplitudeUtils.tagEventWithAdDetails(AmplitudeUtils.SAVED_DRAFT_EVENT, acAd);
    }

    private void tagSavedDraftKahuna(AdViewAd acAd, String event) {
        KahunaHelper.tagEventWithAttribute(event, acAd, KahunaHelper.ACTION_DRAFT);
        KahunaHelper.tagInsertAdAttributes(KahunaHelper.LAST_TITLE_SAVED_DRAFT, acAd);
    }

    private AdViewAd getAdDetailForTag() {
        AdViewAd acAd = null;
        if (previewParams != null) {
            acAd = new AdViewAd(previewParams);
            acAd.setParentCategoryName(categoryParentName);
        } else {
            acAd = new AdViewAd();
            acAd.setName(name);
            acAd.setSubject(insertAdSubject);
            acAd.setParentCategoryName(categoryParentName);
            acAd.setCategoryName(categoryName);
            acAd.setRegion(regionName);
            acAd.setSubRegionName(subRegionName);
        }
        return acAd;
    }

    private void tagXiti(String prefixPage, String pageName, String level2) {
        strPrefixPageName = prefixPage;
        Map<String, String> mapX = new HashMap<String, String>();
        ACReferences ref = ACReferences.getACReferences();
        if (ref.getInsertAdRegionId() != null) {
            mapX.put(XitiUtils.CUST_VAR_REGION, ref.getInsertAdRegionId());
        }
        if (ref.getInsertAdCategoryId() != null) {
            String categoryParent = ACSettings.getACSettings().getCategoryParent(ref.getInsertAdCategoryId());
            if (!ACUtils.isEmpty(categoryParent))
                mapX.put(XitiUtils.CUST_VAR_MAIN_CATEGORY, categoryParent);
        }
        //User Account
        if (Config.userAccount.isLogin()) {
            mapX.put(XitiUtils.CUST_VAR_USER_ID, Config.userAccount.getUserId());
            mapX.put(XitiUtils.CUST_VAR_USER_EMAIL, Config.userAccount.getEmail());
        }

        EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getApplicationContext(), pageName, level2, mapX);
    }

    private void updateLoading() {
        fLoading.updateMaintenanceMode(Config.maintenanceInsertAd, true);
        if (Config.maintenanceInsertAd) {
            return; //stop doing anything
        }
        Log.d("categoriesFetched: " + ACReferences.categoriesFetched + ", regionsFetched: " + ACReferences.regionsFetched);
        if (ACReferences.categoriesFetched == false || ACReferences.regionsFetched == false) {
            // make sure regions are available before anything starts (so restoring item location from setState() and loadProfile() works)
            Handler handler = new MyHandler(this);
            if (!ACReferences.categoriesFetched) {
                ACBlocketConnection.fetchCategories(this, handler);
            } else if (!ACReferences.regionsFetched) {
                ACBlocketConnection.fetchRegions(this, handler);
            }
            return;
        } else {
            loadDraft();
        }
    }

    private void loadDraft() {
        Log.d();
        if (getDraft() == null) {
            // load local draft
            loadLocalDraft();
        } else {
            fLoading.hide();
        }
    }

    private JSONObject getDraftJsonFromString() {
        JSONObject draft = null;
        try {
            draft = new JSONObject(draftJson);
        } catch (JSONException e) {
            ACUtils.debug(e, "InsertAd_LoadLocalDraft", draftJson, false);
        }
        return draft;
    }

    private void loadLocalDraft() {
        JSONObject empty = new JSONObject(getState());
        final ACReferences ref = ACReferences.getACReferences();
        Log.d("isLoadDraft(): " + ref.isLoadDraft());
        if (!ref.isLoadDraft() && draftJson != null) {
            final JSONObject draft = getDraftJsonFromString();

            if (!equalsIgnoreProfile(empty, draft)) {
                Log.d("Got draft");
                //if users open Insert Ad page from a reminder, no need to show a dialog box just load draft right away
                if (isClickedDraftAdReminder) {
                    restoreLocalDraft(draft);
                    ref.setLoadDraft(true);
                    sendTracking(DRAFT, AD_DETAIL_FRAGMENT);
                    isClickedDraftAdReminder = false;
                } else {
                    new LoadDraftFragment().show(getSupportFragmentManager(), TAG_CONFIRM_CONTINUE_DIALOG);
                }
            } else {
                Log.d("Empty local draft found");
                // empty local draft found
                clearLocalDraft();
                loadLocalDraft();
            }
        } else {
            // no local draft found
            restoreUserProfile();

            setDraft(null);
            fLoading.hide();
        }
    }

    private void restoreLocalDraft(JSONObject draft) {
        // continue offline draft
        try {
            setDraft(draft);
            setState(draft);
            fLoading.hide();
        } catch (JSONException e) {
            if (draft != null) {
                ACUtils.debug(e, "InsertAd_resotreLocalDraft", draft.toString());
            } else {
                ACUtils.debug(e);
            }
        }
    }

    private void restoreUserProfile() {
        // for subsequent ad insertions, check if the user profile exists so that we can load it for autocomplete.
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(this);
        if (!ACUtils.isEmpty(pref.getString(PreferencesUtils.ITEM_REGION, null))) {
            String region = pref.getString(PreferencesUtils.ITEM_REGION, null);
            String municipality = pref.getString(PreferencesUtils.ITEM_MUNICIPALITY, null);
            Log.d("restore region: " + region + ", municipality: " + municipality);
            if (!ACUtils.isEmpty(region) && !ACUtils.isEmpty(municipality)) {
                InsertAdDetailsFragment details = (InsertAdDetailsFragment) contentAdapter.getForm(vpContent, AD_DETAIL_FRAGMENT);
                if (details != null)
                    details.setLocation(region, municipality);
            }

            InsertAdProfileFragment profile = (InsertAdProfileFragment) contentAdapter.getForm(vpContent, AD_PROFILE_FRAGMENT);
            if (profile != null)
                profile.loadLocalProfile();

        }
    }

    public void saveLocalDraft() {
        Log.d();
        Map<String, String> allParams = getState();
        insertAdSubject = allParams.get(Constants.SUBJECT_KEY);//will be used for tagging
        name = allParams.get(Constants.NAME_KEY);//will be used for tagging
        String draft = new JSONObject(allParams).toString();
        ACReferences ref = ACReferences.getACReferences();
        Log.d(draft);
        ref.setLoadDraft(true);
        PreferencesUtils.getSharedPreferences(InsertAdActivity.this)
                .edit()
                .putString(PreferencesUtils.INSERT_AD_DRAFT, draft)
                .apply();

        (new NotificationBuilderHelper()).cancelInactiveInsertAdNotification(this);
        createDraftNotification();
    }

    private void createDraftNotification() {
        DraftAdNotificationModel draftAdNotificationModel = DraftAdNotificationModel.newInstance(getBaseContext());
        NotificationBuilderHelper notificationBuilderHelper = new NotificationBuilderHelper();
        notificationBuilderHelper.createDraftAdReminder(this, draftAdNotificationModel);
    }

    public String formatURLToInvokeInAppActivity(String url) {
        return Config.SCHEME + url;
    }

    private void clearLocalDraft() {
        Log.d();
        setDraft(null);
        PreferencesUtils.getSharedPreferences(InsertAdActivity.this)
                .edit()
                .remove(PreferencesUtils.INSERT_AD_DRAFT)
                .apply();
        draftJson = null;
    }

    private void validate(ValidationSuccessCallback callback) {
        Map<String, Object> params = new HashMap<String, Object>(getState());
        if (params.containsKey(Constants.EMAIL_KEY))
            insertAdEmail = params.get(Constants.EMAIL_KEY) + Constants.EMPTY_STRING;
        else
            insertAdEmail = Constants.EMPTY_STRING;

        if (params.containsKey(Constants.SUBJECT_KEY))
            insertAdSubject = params.get(Constants.SUBJECT_KEY) + Constants.EMPTY_STRING;
        else
            insertAdSubject = Constants.EMPTY_STRING;

        getSupportLoaderManager().restartLoader(LOADER_VALIDATE, null, newValidateLoaderCallbacks(params, callback));
    }

    private ValidationSuccessCallback newSubmitValidationCallback() {
        return new ValidationSuccessCallback() {
            @Override
            public void onValidationSuccess(JSONObject data) {
                InsertAdPasswordFragment insertAdPasswordFragment = new InsertAdPasswordFragment();
                insertAdPasswordFragment.setEmail(insertAdEmail);
                insertAdPasswordFragment.show(getSupportFragmentManager(), "dialog");
            }
        };
    }

    private BlocketLoader.Callbacks newValidateLoaderCallbacks(Map<String, Object> params, final ValidationSuccessCallback callback) {
        if (params != null) {
            Log.d(params.toString());
            params.put("action", "validate_ad");
            changeImageURLtoImageId(params);
            isRequiredCertifiedAd = params.containsKey("certified");
        }

        DialogUtils.showProgressDialog(this, getString(R.string.loading));
        return new BlocketLoader.Callbacks(Method.POST, "newad", params, this) {
            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                super.onLoadFinished(loader, data);
                getSupportLoaderManager().destroyLoader(loader.getId());
                DialogUtils.hideProgressDialog(InsertAdActivity.this);
            }

            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                if (!ACUtils.isEmpty(data.optString("newad")) || !ACUtils.isEmpty(data.optString(Constants.ERROR))) {
                    //Trans error
                    JSONObject errors = data.optJSONObject("newad");
                    previewParams = data.optJSONObject("preview");
                    if (errors == null) {
                        //API validation error
                        errors = data.optJSONObject(Constants.ERROR);
                        if (errors != null && errors.optJSONArray("parameters") != null) {
                            errors = setErrorKeysValues(errors);
                        }
                    }
                    int errorCount = setErrors(errors, true);
                    if (errorCount == 0 && callback != null) {
                        callback.onValidationSuccess(data);
                    }
                } else // has mod_sec error
                    onLoadError(loader, data);
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                DialogUtils.showUnexpectedErrorAlert(InsertAdActivity.this);
                tagConnectionLost(INSERT_AD_CONNECTION_LOST, data);
            }
        };
    }

    private BlocketLoader.Callbacks newSubmitLoaderCallbacks(Map<String, Object> params) {
        if (params != null) {
            // a new submit, not a init/restoring on reinitializing activity
            Log.d(params.toString());
            params.put("action", "insert_ad");
            params.put("pay_type", "free"); // no premium ads
            params.put("tpa", "4");//source
            changeImageURLtoImageId(params);
            name = params.get("name").toString();
        }
        final Map<String, Object> p = new HashMap<String, Object>(params);

        final ProgressDialogFragment progressDialogFragment = DialogUtils.showProgressBar(this, getString(R.string.insert_ad_loading));
        final Thread progressIncrement = new Thread() {

            @Override
            public void run() {
                int jumpTime = 0;
                int sleepingTime = 0;

                while (sleepingTime < MAX_PROGRESS_SLEEPING_TIME) {
                    try {
                        if (Thread.interrupted()) {
                            return;
                        }

                        sleep(PROGRESS_INTERVAL);
                        sleepingTime++;
                        jumpTime += 100 / MAX_PROGRESS_SLEEPING_TIME;
                        if (progressDialogFragment != null) {
                            progressDialogFragment.setProgress(jumpTime);
                        }
                    } catch (InterruptedException e) {
                        return;
                    }

                }

            }
        };
        progressIncrement.start();

        return new BlocketLoader.Callbacks(Method.POST, "newad", params, this) {
            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                super.onLoadFinished(loader, data);
                getSupportLoaderManager().destroyLoader(loader.getId());
                if (progressDialogFragment != null) {
                    progressDialogFragment.dismiss();
                }
                progressIncrement.interrupt();
            }

            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                try {
                    if (progressDialogFragment != null) {
                        progressDialogFragment.setProgress(100);
                    }
                    boolean emailExist = PDPNHelper.saveOpttedInEmails(p, context);

                    if (data.has("newad") || !ACUtils.isEmpty(data.optString(Constants.ERROR))) {
                        JSONObject newAd = data.optJSONObject("newad");
                        if (!data.has(Constants.ERROR) && newAd != null && "TRANS_OK".equals(newAd.optString("status"))) {
                            String pdpnOption = (String) p.get("queues");
                            userIdAfterInsertAd = newAd.optString("user_id");
                            //if empty, check from the save data
                            if (!emailExist && ACUtils.isEmpty(pdpnOption)) {
                                pdpnOption = PDPNHelper.getPdpnOptionType(context);
                            }

                            if (!ACUtils.isEmpty(pdpnOption)) {
                                if (("0").equals(pdpnOption)) {
                                    optInMudahParterMailingList = true;
                                } else
                                    optInMudahMailingList = true;
                            }
                            previewParams.optJSONObject("ad").put("ad_id", newAd.optString("ad_id"));
                            onSubmitSuccess(p);
                        } else {
                            //Trans error
                            JSONObject errors = data.optJSONObject("newad");
                            if (errors == null) {
                                //API validation error
                                errors = data.optJSONObject(Constants.ERROR);
                                if (errors != null && errors.optJSONArray(Constants.PARAMETERS) != null)
                                    errors = setErrorKeysValues(errors);
                            }
                            int errorCount = setErrors(errors, true);
                            if (errorCount == 0) {
                                //could not find error in the forms, must be some other technical errors
                                if (errors.has(Constants.MESSAGE)) {
                                    AlertDialogFragment
                                            .instantiate(getString(R.string.general_error_title), errors.getString(Constants.MESSAGE), null)
                                            .show(getSupportFragmentManager(), TAG_ERROR_DIALOG);
                                } else {
                                    onLoadError(loader, data); // unknown error
                                }
                            }
                        }
                    } else // has mod_sec error
                        onLoadError(loader, data);

                } catch (JSONException e) {
                    Log.e(e);
                    onLoadError(loader, data);
                }
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                DialogUtils.showUnexpectedErrorAlert(InsertAdActivity.this);
            }
        };
    }

    private void onSubmitSuccess(Map<String, Object> sentParams) {
        Log.d();
        updateCategoryAndRegionName();
        tagAppsFlyerInsertAdEvent();
        tagFBEvent(INSERT_AD_SUBMIT);
        sendTracking(AD_SUCCESS_PAGE);
        tagSubmitRegardlessError(AD_SUCCESS_PAGE);

        clearState();
        clearLocalDraft();
        InsertAdSuccessFragment.instantiate(name, sentParams).show(getSupportFragmentManager(), "dialog");

        //Remove draft ad notification
        (new NotificationBuilderHelper()).cancelDraftNotification(this);
        //Create inactive insert ad notification
        createInactiveInsertAdNotification();
    }

    private void tagAppsFlyerInsertAdEvent() {
        if (Constants.CAR_CATEGORY.equalsIgnoreCase(categoryName)) {
            AppsFlyerUtils.sendConversionTag(InsertAdActivity.this, AppsFlyerUtils.AppsFlyerTags.AI_CARS_APP, categoryName, regionName);
        } else {
            AppsFlyerUtils.sendConversionTag(InsertAdActivity.this, AppsFlyerUtils.AppsFlyerTags.AI_MARKETPLACE_APP, categoryName, regionName);
        }
    }

    private void updateCategoryAndRegionName() {
        ACReferences ref = ACReferences.getACReferences();
        if (ref.insertAdCategoryId != null) {
            ACCategory category = ACSettings.getACSettings().getCategory(ref.insertAdCategoryId);
            if (category != null) {
                categoryName = category.getName();
                categoryParentName = category.getParentName();
            }
        }
        if (ref.insertAdRegionId != null) {
            regionName = ACSettings.getACSettings().getRegionName(ref.insertAdRegionId);
            subRegionName = ACSettings.getACSettings().getMunicipalityName(ref.insertAdMunicipalityId);
        }

    }

    private void tagFBEvent(String funnelStage) {
        //Facebook Param Tracking
        String postAs = selectedPostAs;
        //In Ad Detils, we do not know the "post as" yet. Default is Private but we should not send this.
        //So, reset this value to empty
        if ((INSERT_AD_DETAILS).equalsIgnoreCase(funnelStage)) {
            postAs = Constants.EMPTY_STRING;
        }
        parameters = fbInsertAd(categoryParentName, categoryName, regionName, subRegionName, funnelStage, postAs);
        logger.logEvent(AD_INSERT, parameters);
    }

    private void createInactiveInsertAdNotification() {
        InactiveInsertAdNotificationModel inactiveInsertAdNotificationModel = InactiveInsertAdNotificationModel.newInstance(this);
        NotificationBuilderHelper notificationBuilderHelper = new NotificationBuilderHelper();
        notificationBuilderHelper.createInactiveInsertAdReminder(this, inactiveInsertAdNotificationModel);
    }

    public String getInsertAdProfileFragmentTag() {
        return tagInsertAdProfileFragment;
    }

    public void setInsertAdProfileFragmentTag(String t) {
        tagInsertAdProfileFragment = t;
    }

    public String getInsertAdDetailsFragmentTag() {
        return tagInsertAdDetailsFragment;
    }

    public void setInsertAdDetailsFragmentTag(String t) {
        tagInsertAdDetailsFragment = t;
    }

    private String getSellerType(String strSellerType) {
        // Ad source (Private(0), Company(1), ..)
        String sellerType = Constants.POSTED_BY_PRIVATE;
        if (!ACUtils.isEmpty(strSellerType) && ("1").equals(strSellerType)) {
            sellerType = Constants.POSTED_BY_COMPANY;
        }
        return sellerType;
    }

    private void tagTealium(String prefixPage, int currPage, String pageName, String level2) {

        //Common Tealium tagging data
        Map<String, String> dataTealium = Tealium.map(
                TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_AI,
                TealiumHelper.PAGE_NAME, pageName,
                TealiumHelper.XTN2, level2,
                TealiumHelper.AD_ACTION_TYPE, "New");

        if (currPage == AD_DETAIL_FRAGMENT || currPage == AD_PROFILE_FRAGMENT) {
            TealiumHelper.track(this, dataTealium, Tealium.VIEW);
        } else if (currPage == AD_PREVIEW_FRAGMENT || currPage == AD_SUCCESS_PAGE) {
            //Ad details
            ACReferences ref = ACReferences.getACReferences();
            AdViewAd ad = new AdViewAd(previewParams);
            MudahUtil.prepareParentCategoryInfo(ad.getCategoryId(), ad);
            Map<String, String> adData = Tealium.map(
                    TealiumHelper.REGION_ID, ad.getRegionId(),
                    TealiumHelper.REGION_NAME, ad.getRegion(),
                    TealiumHelper.SUBREGION_ID, ref.getInsertAdMunicipalityId(),
                    TealiumHelper.SUBREGION_NAME, ad.getSubRegionName(),
                    TealiumHelper.CATEGORY_NAME, ad.getCategoryName(),
                    TealiumHelper.PARENT_CATEGORY_ID, ad.getParentCategoryId(),
                    TealiumHelper.PARENT_CATEGORY_NAME, ad.getParentCategoryName(),
                    TealiumHelper.AD_TYPE, ad.getTypeAd(),
                    TealiumHelper.MESSAGE_BY_WHATSAPP, !ACUtils.isEmpty(ad.getWhatsApp()) ? TealiumHelper.YES : TealiumHelper.NO,
                    TealiumHelper.HIDE_PHONE_NUMBER, ACUtils.isEmpty(ad.getPhone()) ? TealiumHelper.YES : TealiumHelper.NO,
                    TealiumHelper.NUMBER_OF_PHOTOS, ad.getImageCount() + Constants.EMPTY_STRING,
                    TealiumHelper.AD_TITLE, MudahUtil.removeUnwantedSign(ad.getSubject()),
                    TealiumHelper.STORE_ID, ad.getStoreId(),
                    TealiumHelper.SELLER_TYPE, getSellerType(ad.getCompanyAd())
            );

            if (isRequiredCertifiedAd) {
                adData.put(TealiumHelper.CERTIFIED, TealiumHelper.YES);
            }

            String postedBy = getPostedByOption();
            if (!ACUtils.isEmpty(postedBy)) {
                adData.put(TealiumHelper.AD_SELLER_TYPE, TealiumHelper.getAdSellerTypeIdFromLabel(PostedBy.valueOf(postedBy.toUpperCase())));
            }

            dataTealium.putAll(adData);
            TealiumHelper.prepareParamsTagging(dataTealium, ad.getParameters());

            if (currPage == AD_PREVIEW_FRAGMENT) {
                TealiumHelper.track(this, dataTealium, Tealium.VIEW);

            } else if (currPage == AD_SUCCESS_PAGE) {
                //PDPN details
                Map<String, String> pdpaData = Tealium.map(
                        TealiumHelper.MUDAH_MAILING_LIST, optInMudahMailingList ? TealiumHelper.YES : TealiumHelper.NO,
                        TealiumHelper.MUDAH_AND_PARTNERS_MAILING_LIST, optInMudahParterMailingList ? TealiumHelper.YES : TealiumHelper.NO,
                        TealiumHelper.USER_ID, userIdAfterInsertAd,
                        TealiumHelper.USER_TYPE, Config.firstTimeUser ? TealiumHelper.USER_TYPE_NEW : TealiumHelper.USER_TYPE_RETURN
                );
                dataTealium.putAll(pdpaData);
                TealiumHelper.track(this, dataTealium, Tealium.VIEW);
            }
        }
    }

    public JSONObject getDraft() {
        return lastSavedState;
    }

    public void setDraft(JSONObject state) {
        this.lastSavedState = state;
    }

    public interface Form {
        String CLEAN_DRAFT = "Clean_Draft";

        Map<String, String> getState();

        void setState(Map<String, String> state);

        int setErrors(JSONObject errors);
    }

    private interface ValidationSuccessCallback {
        void onValidationSuccess(JSONObject data);
    }

    public static class LoadDraftFragment extends RetainedDialogFragment {

        public LoadDraftFragment() {
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final InsertAdActivity insertAdActivity = (InsertAdActivity) getActivity();
            final JSONObject draft = insertAdActivity.getDraftJsonFromString();
            final ACReferences ref = ACReferences.getACReferences();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MudahDialogStyle)
                    .setTitle(R.string.insert_ad_continue_confirm)
                    .setMessage(R.string.insert_ad_continue_confirm_message)
                    .setPositiveButton(R.string.insert_ad_continue_confirm_continue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (insertAdActivity == null)
                                dialog.dismiss();
                            insertAdActivity.restoreLocalDraft(draft);
                            ref.setLoadDraft(true);
                            insertAdActivity.sendTracking(DRAFT, AD_DETAIL_FRAGMENT);
                        }
                    })
                    .setNegativeButton(R.string.insert_ad_continue_confirm_discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (insertAdActivity == null)
                                dialog.dismiss();
                            insertAdActivity.clearLocalDraft();
                            insertAdActivity.loadLocalDraft();
                            insertAdActivity.sendTracking(AD_DETAIL_FRAGMENT);
                            //Remove draft ad notification
                            (new NotificationBuilderHelper()).cancelDraftNotification(insertAdActivity);
                        }
                    })
                    .setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                insertAdActivity.finish();
                                dialog.dismiss();
                            }
                            return true;
                        }
                    });

            AlertDialog dialog = builder.show();
            dialog.setCanceledOnTouchOutside(false);
            MudahUtil.hideDialogDivider(getContext(), dialog);

            return dialog;
        }

        public void onCancel(DialogInterface dialog) {
            CustomizedIntentActionUtils.returnHome(getActivity());
        }
    }

    static class MyHandler extends Handler {
        private final WeakReference<InsertAdActivity> mTarget;

        public MyHandler(InsertAdActivity target) {
            mTarget = new WeakReference<InsertAdActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            InsertAdActivity target = mTarget.get();
            if (target == null) {
                return;
            }
            if (msg.arg1 == ACReferences.RESULT_ERROR && target != null) {
                target.fLoading.setConnectionLostShown(true);
                target.tagConnectionLost(INSERT_AD_CONNECTION_LOST, null);
            } else if (!target.isFinishing()) {
                target.updateLoading();
            }
        }
    }

    private static class ContentAdapter extends FragmentPagerAdapter {
        private FragmentActivity activity;

        public ContentAdapter(FragmentActivity activity) {
            super(activity.getSupportFragmentManager());

            this.activity = activity;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(position + Constants.EMPTY_STRING);
            switch (position) {
                case AD_DETAIL_FRAGMENT:
                    return InsertAdDetailsFragment.newInstance();
                case AD_PROFILE_FRAGMENT:
                    return InsertAdProfileFragment.newInstance();
                case AD_PREVIEW_FRAGMENT:
                    return InsertAdPreviewFragment.newInstance();
                default:
                    return null;
            }
        }

        public Form getForm(ViewGroup container, int position) {
            Form form = (Form) instantiateItem(container, position); // instantiateItem() does not commit FragmentTransaction
            finishUpdate(container); // force commit of FragmentTransaction to make sure fragment returned is reused
            return form;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return activity.getText(R.string.insert_ad_details);
                case 1:
                    return activity.getText(R.string.insert_ad_profile);
                case 2:
                    return activity.getText(R.string.insert_ad_preview);
                default:
                    return null;
            }
        }
    }
}


