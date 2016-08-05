package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.lib701.datasets.ACAd;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.DialogUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.PDPNHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.AppsFlyerUtils;
import com.mudah.my.utils.AppsFlyerUtils.AppsFlyerTags;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.GravityUtils;
import com.mudah.my.utils.MudahPreferencesUtils;
import com.mudah.my.utils.MudahUtil;
import com.tealium.library.Tealium;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.mudah.my.helpers.PDPNHelper.getPDPNoptinValue;

public class EmailAdvertiserActivity extends MudahBaseActivity {
    private static final int LOADER_SENDEMAIL = 0x02;
    private static final String SAVED_DATA = "data";
    private static final String SENT_BY_ANDROID = "sent_by_android_app";
    private static boolean pdpnEnable = false;
    Bundle bundle;
    int listId;
    int categoryId;
    EditText name;
    EditText email;
    EditText phone;
    CheckBox sendCopyToMe;
    CheckBox optInInternal;
    CheckBox optInInternalExternal;
    EditText message;
    LinearLayout llPdpn;
    LinearLayout llPdpnCheckboxes;
    protected OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasfocus) {
            if (!hasfocus) {
                try {
                    PDPNHelper.hidePDPNmessages(getApplicationContext(), email.getText().toString(), llPdpnCheckboxes);
                } catch (JSONException e) {
                    ACUtils.debug(e);
                }
            }
        }
    };
    CheckBox.OnCheckedChangeListener checkbox1Listener = new CheckBox.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (optInInternal.isChecked()) {
                optInInternalExternal.setChecked(false);
            }

        }
    };
    CheckBox.OnCheckedChangeListener checkbox2Listener = new CheckBox.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (optInInternalExternal.isChecked()) {
                optInInternal.setChecked(false);
            }

        }
    };
    private AppEventsLogger logger;
    private AdViewAd acAd;
    protected OnClickListener sendListener = new OnClickListener() {
        public void onClick(View v) {
            Log.d();
            if (!clientValidationError()) {
                getSupportLoaderManager().initLoader(LOADER_SENDEMAIL, null,
                        asyncSendEmail());
            }
        }
    };
    private JSONObject data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        setContentView(R.layout.activity_email_advertiser);
        logger = AppEventsLogger.newLogger(this);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);

        Log.d("savedInstanceState here : " + savedInstanceState);
        // if phone is rotate, we can lost categoryId,
        // which we need for xiti
        if (savedInstanceState != null) {
            categoryId = savedInstanceState.getInt("savedCategoryId",
                    XitiUtils.CATEGORY_OTHERS);
            listId = savedInstanceState.getInt("listId");
            acAd = (AdViewAd) savedInstanceState.getSerializable("tagging_params");
        } else {
            Intent intent = this.getIntent();
            bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey("list_id"))
                    listId = bundle.getInt("list_id");
                if (bundle.containsKey(AdViewActivity.EXTRA_CATEGORY_ID))
                    categoryId = bundle.getInt(AdViewActivity.EXTRA_CATEGORY_ID);
                if (bundle.containsKey("tagging_params"))
                    acAd = (AdViewAd) bundle.getSerializable("tagging_params");
            }
        }

        Log.d("list_id:" + listId + ", categoryId: " + categoryId);

        name = (EditText) findViewById(R.id.et_name);
        email = (EditText) findViewById(R.id.et_email);
        phone = (EditText) findViewById(R.id.et_phone);
        sendCopyToMe = (CheckBox) findViewById(R.id.cb_send_copy_to_me);
        optInInternal = (CheckBox) findViewById(R.id.cb_pdpa_internal_checkbox);
        optInInternalExternal = (CheckBox) findViewById(R.id.cb_pdpa_internal_external_checkbox);
        message = (EditText) findViewById(R.id.et_message);
        llPdpn = (LinearLayout) findViewById(R.id.ll_pdpn);
        llPdpnCheckboxes = (LinearLayout) findViewById(R.id.ll_pdpn_checkboxes);
        Button send = (Button) findViewById(R.id.b_send);
        send.setOnClickListener(sendListener);

        optInInternal.setOnCheckedChangeListener(checkbox1Listener);
        optInInternalExternal.setOnCheckedChangeListener(checkbox2Listener);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVED_DATA)) {
            try {
                data = new JSONObject(savedInstanceState.getString(SAVED_DATA));
            } catch (JSONException e) {
                ACUtils.debug(e, "EmailAd_restoreData", savedInstanceState.getString(SAVED_DATA));
            }
        }

        try {
            recoverFieldsValue();
            if (PDPNHelper.getPDPNEnable(EmailAdvertiserActivity.this)) {
                pdpnEnable = true;
                PDPNHelper.hidePDPNmessages(this, email.getText().toString(), llPdpnCheckboxes);
                llPdpn.setVisibility(View.VISIBLE);
                email.setOnFocusChangeListener(focusChangeListener);
                PDPNHelper.setPDPNmessage(this.findViewById(android.R.id.content), this);
            } else {
                pdpnEnable = false;
                llPdpn.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            ACUtils.debug(e);
        }

    }

    /**
     * set the values of the field previously successfully submitted into the form
     *
     * @throws org.json.JSONException
     */
    private void recoverFieldsValue() throws JSONException {

        String contactAdvertiserInfo = PreferencesUtils
                .getSharedPreferences(this)
                .getString(MudahPreferencesUtils.CONTACT_ADVERTISER_INFO, "{}");
        JSONObject contactAdvertiserJsonObject = new JSONObject(contactAdvertiserInfo);

        if (contactAdvertiserJsonObject.length() > 0) {
            String contactAdvertiserName = contactAdvertiserJsonObject.getString("name");
            String contactAdvertiserEmail = contactAdvertiserJsonObject.getString("email");
            String contactAdvertiserPhone = contactAdvertiserJsonObject.getString("phone");
            String contactAdvertiserSendCopy = contactAdvertiserJsonObject.getString("cc");

            name.setText(contactAdvertiserName);
            email.setText(contactAdvertiserEmail);
            phone.setText(contactAdvertiserPhone);
            if (contactAdvertiserSendCopy.equalsIgnoreCase("1") || contactAdvertiserSendCopy.equalsIgnoreCase("true")) {
                sendCopyToMe.setChecked(true);
            } else {
                sendCopyToMe.setChecked(false);
            }
        }
    }

    /**
     * save the values of the field into shared pref
     *
     * @param params params of the contact advertiser form
     * @throws org.json.JSONException
     */
    private void saveFieldsValue(Map<String, Object> params) throws JSONException {

        JSONObject contactAdvertiserJsonObject = new JSONObject();
        contactAdvertiserJsonObject.put("name", (String) params.get("name"));
        contactAdvertiserJsonObject.put("email", (String) params.get("email"));
        contactAdvertiserJsonObject.put("phone", (String) params.get("phone"));
        contactAdvertiserJsonObject.put("cc", (String) params.get("cc"));
        PreferencesUtils
                .getSharedPreferences(this).edit()
                .putString(MudahPreferencesUtils.CONTACT_ADVERTISER_INFO, contactAdvertiserJsonObject.toString())
                .apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d();
        if (data != null) {
            outState.putString(SAVED_DATA, data.toString());
        }
        outState.putInt("listId", listId);
        outState.putInt("savedCategoryId", categoryId);
        outState.putSerializable("tagging_params", acAd);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();
        if (getSupportLoaderManager().getLoader(LOADER_SENDEMAIL) != null) {
            getSupportLoaderManager().initLoader(LOADER_SENDEMAIL, null,
                    asyncSendEmail());
        }
    }

    /**
     * If user rotates the phone before the loader_sendmail has started. Hide
     * the loading screen. Otherwise, this screen will prevent user from
     * interacting with a screen
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d();
        if (getSupportLoaderManager().getLoader(LOADER_SENDEMAIL) != null
                && !getSupportLoaderManager().getLoader(LOADER_SENDEMAIL)
                .isStarted()) {
            Log.d("isstarted "
                    + getSupportLoaderManager().getLoader(LOADER_SENDEMAIL)
                    .isStarted());
            DialogUtils.hideProgressDialog(EmailAdvertiserActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                //return to Adview
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BlocketLoader.Callbacks asyncSendEmail() {
        Log.d();
        DialogUtils.showProgressDialog(this, getString(R.string.loading));
        final Map<String, Object> p = new HashMap<String, Object>();

        p.put("list_id", listId);
        p.put("name", name.getText().toString());
        p.put("phone", phone.getText().toString());
        if (sendCopyToMe.isChecked()) {
            p.put("cc", "1");
        } else {
            p.put("cc", "0");
        }
        if (!ACUtils.isEmpty(message.getText().toString()))
            p.put("body", message.getText().toString() + SENT_BY_ANDROID);
        p.put("email", email.getText().toString().toLowerCase());
        try {
            if (optInInternal.isChecked() || optInInternalExternal.isChecked()) {

                String optInValue = getPDPNoptinValue(this);
                p.put("opt_in", optInValue);
                p.put("opt_out", "0");

                //top is checked
                if (optInInternal.isChecked() && !optInInternalExternal.isChecked()) {

                    Map<String, String> pdpnQueues = PDPNHelper.getPDPNQueues(EmailAdvertiserActivity.this);

                    int totalQueueValues = 0;
                    for (String key : pdpnQueues.keySet()) {
                        totalQueueValues += Integer.parseInt(key);
                    }
                    p.put("queues", String.valueOf(totalQueueValues));
                }

                //bottom is checked
                else if (!optInInternal.isChecked() && optInInternalExternal.isChecked()) {
                    p.put("queues", "0");//TO DO
                }
            }

        } catch (JSONException e) {
            ACUtils.debug(e);
        }

        return new BlocketLoader.Callbacks(Method.POST, "mail", p, this) {

            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                Log.d();
                onEmailSent(data, p);
                getSupportLoaderManager().destroyLoader(loader.getId());
                DialogUtils.hideProgressDialog(EmailAdvertiserActivity.this);
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                // general failure
                DialogUtils.hideProgressDialog(EmailAdvertiserActivity.this);
                DialogUtils.showGeneralErrorAlert(EmailAdvertiserActivity.this);
                getSupportLoaderManager().destroyLoader(loader.getId());
            }
        };
    }

    private void sendTagging(ACCategory category) {
        if (acAd != null) {
            String categoryName = category.getName();
            String categoryParentName = category.getParentName();
            acAd.setCategoryName(categoryName);
            acAd.setParentCategoryName(categoryParentName);
            GravityUtils.sendEventAsyncWithEventTypeAndItemId(GravityUtils.EVENT_TYPE_LETTER_SEND, acAd.getListId() + Constants.EMPTY_STRING);
            tagEvent(categoryParentName, categoryName);
            tagFBEvent(categoryParentName);
            tagAppsFlyerEmailEvent(categoryId, acAd);
            AmplitudeUtils.tagReply(acAd, Constants.EMAIL);

            KahunaHelper.tagEvent(KahunaHelper.AD_REPLY_EVENT, acAd);
            KahunaHelper.tagAttributes(KahunaHelper.PAGE_AD_REPLIED, KahunaHelper.LAST_TITLE_REPLIED, acAd.getSubject());
        }
    }

    private void tagAppsFlyerEmailEvent(int catId, ACAd acAd) {
        if (Constants.CARS == catId) {
            AppsFlyerUtils.sendConversionTag(EmailAdvertiserActivity.this, AppsFlyerTags.AR_CARS_APP, acAd);
        } else {
            AppsFlyerUtils.sendConversionTag(EmailAdvertiserActivity.this, AppsFlyerTags.AR_MARKETPLACE_APP, acAd);
        }
    }

    private void tagEvent(String categoryParentName, String categoryName) {
        String fullTagName = Constants.EMAIL + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(categoryParentName) + XitiUtils.CHAPTER_SIGN + ACUtils.encodeSign(categoryName);
        EventTrackingUtils.sendAdReply(fullTagName, acAd);
        tagTealium(fullTagName);
    }

    private void tagFBEvent(String categoryParentName) {
        Bundle parameters = fbAdreply(categoryParentName, Constants.EMAIL, acAd);
        logger.logEvent(AdViewActivity.ADREPLY, parameters);
    }

    protected void onEmailSent(JSONObject data, Map<String, Object> params_received) {
        String status = data.optString("status");

        if (status.equals("OK") || status.equals("TRANS_OK")) {

            // Main Category
            ACCategory category = ACSettings.getACSettings().getCategory(categoryId + Constants.EMPTY_STRING);
            if (category != null && !ACUtils.isEmpty(category.getParentName())) {
                sendTagging(category);
            }

            Toast.makeText(EmailAdvertiserActivity.this,
                    R.string.mail_dialog_toast_hint,
                    Toast.LENGTH_LONG).show();
            try {
                if (pdpnEnable) {
                    saveFieldsValue(params_received);
                    PDPNHelper.saveOpttedInEmails(params_received, this);
                }
            } catch (JSONException e) {
                ACUtils.debug(e);
            }
            finish();
        } else {
            JSONObject error;
            String params;
            JSONArray errParams;

            ErrorHandler.needsToRequestFocus = true;
            try {
                error = data.getJSONObject("error");
                // resulted in array of parameters from error output
                errParams = error.getJSONArray("parameters");
                for (int i = 0; i < errParams.length(); i++) {
                    EditText errorView = null;
                    String errorMessage;

                    // set red exclamation mark for each particular field
                    // Prod format: {"message":"MISSING_PARAMETER","description":"MISSING_PARAMETER","name":"body"}
                    // Regress format: {"phone":"INVALID_PHONE"}
                    params = errParams.getJSONObject(i).optString("name");
                    Log.d("error field: " + params + ", jsonObj: " + errParams.getJSONObject(i));
                    if (params != null) {
                        if (params.equals("name")) {
                            errorView = name;
                            setFocusToErrorField(name);
                        } else if (params.equals("phone")) {
                            errorView = phone;
                            setFocusToErrorField(phone);
                        } else if (params.equals("email")) {
                            // In UI, email is before phone. So,the focus should be on 'email' field
                            // Because mapy returns an error in phone first, the focus is there and set 'needToRequestFocus' to false
                            // Have to set needToRequestFocus to true back so that the focus can be on 'email' field
                            ErrorHandler.needsToRequestFocus = true;
                            errorView = email;
                            setFocusToErrorField(email);
                        } else {
                            params = "message";
                            errorView = message;
                            setFocusToErrorField(message);
                        }

                    }

                    // set the content of error message for each field
                    if (errParams.optString(i).contains("MISSING")) {
                        errorMessage = getResources().getString(
                                R.string.contact_empty, params);
                    } else if (errParams.optString(i).contains("SHORT")) {
                        errorMessage = getResources().getString(
                                R.string.contact_short, params);
                    } else {
                        errorMessage = getResources().getString(
                                R.string.contact_invalid, params);
                    }
                    ErrorHandler.setError(errorMessage, errorView);

                }
            } catch (JSONException e) {
                ACUtils.debug(e);
            }

        }

    }

    private void tagTealium(String fullNameWithChapter) {
        String regionName = "";
        String messageByWhatsapp = !ACUtils.isEmpty(acAd.getWhatsApp()) ? "1" : Constants.EMPTY_STRING;
        String hidePhoneNumber = ACUtils.isEmpty(acAd.getPhone()) ? "1" : Constants.EMPTY_STRING;
        String numberOfPhotos = acAd.getImageCount() + Constants.EMPTY_STRING;
        String sellerType = MudahUtil.getSellerTypeStr(acAd.getCompanyAd());

        if (acAd.getRegionId() != null) {
            regionName = ACSettings.getACSettings().getRegionName(acAd.getRegionId());
        }

        Map<String, String> tealiumData = Tealium.map(
                TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_SEND_MAIL,
                TealiumHelper.PAGE_NAME, Constants.EMAIL,
                TealiumHelper.REGION_ID, acAd.getRegionId(),
                TealiumHelper.REGION_NAME, regionName,
                TealiumHelper.SUBREGION_ID, acAd.getSubRegionId(),
                TealiumHelper.SUBREGION_NAME, acAd.getSubRegionName(),
                TealiumHelper.CATEGORY_ID, categoryId + Constants.EMPTY_STRING,
                TealiumHelper.CATEGORY_NAME, acAd.getCategoryName(),
                TealiumHelper.PARENT_CATEGORY_ID, acAd.getParentCategoryId(),
                TealiumHelper.PARENT_CATEGORY_NAME, acAd.getParentCategoryName(),
                TealiumHelper.AD_TYPE, acAd.getTypeAd(),
                TealiumHelper.LIST_ID, acAd.getListId() + Constants.EMPTY_STRING,
                TealiumHelper.AD_ID, acAd.getAdId(),
                TealiumHelper.AD_REPLY_TYPE, XitiUtils.AD_REPLY_EMAIL,
                TealiumHelper.AD_TITLE, MudahUtil.removeUnwantedSign(acAd.getSubject()),
                TealiumHelper.SELLER_TYPE, sellerType,
                TealiumHelper.AD_SELLER_TYPE, acAd.getAdSellerType(),
                TealiumHelper.MESSAGE_BY_WHATSAPP, messageByWhatsapp,
                TealiumHelper.HIDE_PHONE_NUMBER, hidePhoneNumber,
                TealiumHelper.NUMBER_OF_PHOTOS, numberOfPhotos,
                TealiumHelper.STORE_ID, acAd.getStoreId(),
                TealiumHelper.XTN2, XitiUtils.LEVEL2_AD_REPLY_ID);

        TealiumHelper.prepareParamsTagging(tealiumData, acAd.getParameters());

        TealiumHelper.track(this, tealiumData, Tealium.VIEW);
        //click
        tealiumData.put(TealiumHelper.PAGE_NAME, fullNameWithChapter);
        tealiumData.put(TealiumHelper.CLICK_TYPE, TealiumHelper.CLICK_TYPE_ACTION);
        tealiumData.put(TealiumHelper.XTN2, XitiUtils.LEVEL2_AD_REPLY_ID);
        TealiumHelper.track(this, tealiumData, Tealium.EVENT);
    }

    private void setFocusToErrorField(EditText field) {
        ErrorHandler.tryToFocus(field);
    }

    private boolean clientValidationError() {
        String errorMessage;
        boolean errorExist = false;
        ErrorHandler.needsToRequestFocus = true;

        if (ACUtils.isEmpty(name.getText().toString())) {
            setFocusToErrorField(name);
            errorMessage = getResources().getString(
                    R.string.contact_empty, "name");
            ErrorHandler.setError(errorMessage, name);
            errorExist = true;
        }
        if (ACUtils.isEmpty(email.getText().toString())) {
            setFocusToErrorField(email);
            errorMessage = getResources().getString(
                    R.string.contact_empty, "email");
            ErrorHandler.setError(errorMessage, email);
            errorExist = true;
        }
        if (ACUtils.isEmpty(message.getText().toString())) {
            setFocusToErrorField(message);
            errorMessage = getResources().getString(R.string.contact_body_empty);
            ErrorHandler.setError(errorMessage, message);
            errorExist = true;
        }


        return errorExist;
    }

}
