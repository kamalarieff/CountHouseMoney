package com.mudah.my.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.utils.ACUtils;
import com.lib701.utils.DialogUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahPreferencesUtils;
import com.tealium.library.Tealium;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moehninhtwee on 15/12/14.
 */
public class EmailReportActivity extends MudahBaseActivity {
    private static final String REPORT_PAGE = "Report_form_page";
    private static final int LOADER_SENDEMAIL = 0x02;
    private static final String SAVED_DATA = "data";
    private static final String TRANS_OPTIONS = "report";
    int listId;
    EditText etName;
    EditText etEmail;
    EditText etPhone;
    EditText etMessage;
    String advertiserName;
    String adTitle;
    String adEmail;
    protected View.OnClickListener sendListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d();
            if (!clientValidationError()) {
                getSupportLoaderManager().initLoader(LOADER_SENDEMAIL, null,
                        asyncSendEmail());
            }
        }
    };
    TextView tvReportName;
    TextView tvAdTitle;

    private JSONObject data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        setContentView(R.layout.activity_email_report);
        Log.d("savedInstanceState here : " + savedInstanceState);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            advertiserName = bundle.getString(AdViewActivity.ADVERTISER_NAME);
            adTitle = bundle.getString(AdViewActivity.AD_TITLE);
            adEmail = bundle.getString(AdViewActivity.AD_EMAIL);
            listId = bundle.getInt(AdViewActivity.LIST_ID);
        }

        tvReportName = (TextView) findViewById(R.id.tv_report_title);
        tvAdTitle = (TextView) findViewById(R.id.tv_report_info);
        tvReportName.setText(getResources().getString(
                R.string.mail_report_title, advertiserName));
        tvAdTitle.setText(getResources().getString(
                R.string.mail_report_info));
        tvAdTitle.append(ACUtils.getHtmlFromString(Constants.BOLD_OPEN_TAG + Constants.SPACE + adTitle + Constants.BOLD_CLOSE_TAG));

        etName = (EditText) findViewById(R.id.et_report_name);
        etEmail = (EditText) findViewById(R.id.et_report_email);
        etPhone = (EditText) findViewById(R.id.et_report_phone);
        etMessage = (EditText) findViewById(R.id.et_report_message);
        Button send = (Button) findViewById(R.id.b_report_send);
        send.setOnClickListener(sendListener);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVED_DATA)) {
            try {
                data = new JSONObject(savedInstanceState.getString(SAVED_DATA));
            } catch (JSONException e) {
                ACUtils.debug(e, "EmailReport_restoreData", savedInstanceState.getString(SAVED_DATA));
            }
        }

        try {
            recoverFieldsValue();
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

        String contact_report_info = PreferencesUtils
                .getSharedPreferences(this)
                .getString(MudahPreferencesUtils.CONTACT_REPORT_INFO, "{}");
        JSONObject contact_report_jsonobject = new JSONObject(contact_report_info);

        if (contact_report_jsonobject.length() > 0) {
            String contact_report_name = contact_report_jsonobject.optString("name");
            String contact_report_email = contact_report_jsonobject.optString("email");
            String contact_report_phone = contact_report_jsonobject.optString("phone");

            etName.setText(contact_report_name);
            etEmail.setText(contact_report_email);
            etPhone.setText(contact_report_phone);
        }
    }

    /**
     * save the values of the field into shared pref
     *
     * @param p_sent params of the report form
     * @throws org.json.JSONException
     */
    private void saveFieldsValue(Map<String, Object> p_sent) throws JSONException {

        JSONObject contactReportJsonObject = new JSONObject();
        contactReportJsonObject.put("name", p_sent.get("name"));
        contactReportJsonObject.put("email", p_sent.get("email"));
        contactReportJsonObject.put("phone", p_sent.get("phone"));
        contactReportJsonObject.put("support_body", p_sent.get("support_body"));
        PreferencesUtils
                .getSharedPreferences(this).edit()
                .putString(MudahPreferencesUtils.CONTACT_REPORT_INFO, contactReportJsonObject.toString())
                .apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d();
        if (data != null) {
            outState.putString(SAVED_DATA, data.toString());
        }
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
     * If user rotates the etPhone before the loader_sendmail has started. Hide
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
            DialogUtils.hideProgressDialog(EmailReportActivity.this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
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
        p.put("name", etName.getText().toString());
        p.put("phone", etPhone.getText().toString());
        p.put("trans_option", TRANS_OPTIONS);
        p.put("support_body", etMessage.getText().toString());
        p.put("email", etEmail.getText().toString().toLowerCase());
        p.put("adv_email", adEmail);

        return new BlocketLoader.Callbacks(Method.POST, "mail", p, this) {

            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                Log.d();
                final Map<String, Object> params = new HashMap<String, Object>();
                params.putAll(p);
                onEmailSent(data, params);
                getSupportLoaderManager().destroyLoader(loader.getId());
                DialogUtils.hideProgressDialog(EmailReportActivity.this);
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                // general failure
                DialogUtils.hideProgressDialog(EmailReportActivity.this);
                DialogUtils.showGeneralErrorAlert(EmailReportActivity.this);
                getSupportLoaderManager().destroyLoader(loader.getId());
            }
        };
    }

    private void sendTagging() {
        EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getApplicationContext(), REPORT_PAGE, XitiUtils.LEVEL2_CUST_SERVICE, null);
        tagTealium(XitiUtils.LEVEL2_CUST_SERVICE);
    }

    private void tagTealium(String level2) {
        Map<String, String> tealiumData = Tealium.map(
                TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_REPORT,
                TealiumHelper.PAGE_NAME, REPORT_PAGE,
                TealiumHelper.XTN2, level2);

        TealiumHelper.track(this, tealiumData, Tealium.VIEW);
    }

    protected void onEmailSent(JSONObject data, Map<String, Object> params_received) {
        String status = data.optString("status");
        if (status.equals("OK") || status.equals("TRANS_OK")) {
            Toast.makeText(EmailReportActivity.this,
                    R.string.mail_dialog_toast_hint,
                    Toast.LENGTH_LONG).show();
            try {
                saveFieldsValue(params_received);
            } catch (JSONException e) {
                ACUtils.debug(e);
            }
            sendTagging();
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
                    // Prod format: {"etMessage":"MISSING_PARAMETER","description":"MISSING_PARAMETER","name":"body"}
                    // Regress format: {"etPhone":"INVALID_PHONE"}
                    params = errParams.getJSONObject(i).optString("name");
                    Log.d("error field: " + params + ", jsonObj: " + errParams.getJSONObject(i));
                    if (params != null) {
                        if (params.equals("name")) {
                            errorView = etName;
                            setFocusToErrorField(etName);
                        } else if (params.equals("phone")) {
                            errorView = etPhone;
                            setFocusToErrorField(etPhone);
                        } else if (params.equals("email")) {
                            errorView = etEmail;
                            setFocusToErrorField(etEmail);
                        } else {
                            params = "message";
                            errorView = etMessage;
                            setFocusToErrorField(etMessage);
                        }

                    }

                    // set the content of error etMessage for each field
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

    private void setFocusToErrorField(EditText field) {
        ErrorHandler.tryToFocus(field);
    }

    private boolean clientValidationError() {
        String errorMessage;
        boolean errorExist = false;
        ErrorHandler.needsToRequestFocus = true;

        if (ACUtils.isEmpty(etName.getText().toString())) {
            setFocusToErrorField(etName);
            errorMessage = getResources().getString(
                    R.string.contact_empty, "name");
            ErrorHandler.setError(errorMessage, etName);
            errorExist = true;
        }
        if (ACUtils.isEmpty(etEmail.getText().toString())) {
            setFocusToErrorField(etEmail);
            errorMessage = getResources().getString(R.string.contact_email_empty);
            ErrorHandler.setError(errorMessage, etEmail);
            errorExist = true;
        }
        if (ACUtils.isEmpty(etMessage.getText().toString())) {
            setFocusToErrorField(etMessage);
            errorMessage = getResources().getString(
                    R.string.contact_body_empty);
            ErrorHandler.setError(errorMessage, etMessage);
            errorExist = true;
        }

        return errorExist;
    }

}
