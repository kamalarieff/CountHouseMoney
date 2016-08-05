package com.mudah.my.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lib701.utils.ACUtils;
import com.lib701.utils.DialogUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.PDPNHelper;
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

import static com.mudah.my.helpers.PDPNHelper.getPDPNoptinValue;

public class BetaUsersActivity extends MudahBaseActivity {
    private static final String BETA_USERS_PAGE = "Beta User Submit Form";
    private static final int LOADER_BETAUSER_SIGNUP = 0x02;
    private static boolean pdpnEnable = false;
    Bundle bundle;
    EditText name;
    EditText email;
    EditText phone;
    CheckBox optInInternal;
    CheckBox optInterview;
    protected OnClickListener sendListener = new OnClickListener() {
        public void onClick(View v) {
            Log.d();
            if (!clientValidationError()) {
                getSupportLoaderManager().initLoader(LOADER_BETAUSER_SIGNUP, null,
                        asyncBetaUserSignUp()
                );
            }
        }
    };
    LinearLayout llPdpn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.strictMode();
        Log.d();

        setContentView(R.layout.activity_beta_users);
        Log.d("savedInstanceState here : " + savedInstanceState);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBar(R.id.actionbar);

        name = (EditText) findViewById(R.id.et_name);
        email = (EditText) findViewById(R.id.et_email);
        phone = (EditText) findViewById(R.id.et_phone);
        optInInternal = (CheckBox) findViewById(R.id.cb_pdpa_internal_checkbox);
        optInterview = (CheckBox) findViewById(R.id.cb_beta_user_interview);
        llPdpn = (LinearLayout) findViewById(R.id.ll_pdpn);
        Button send = (Button) findViewById(R.id.b_send);
        send.setOnClickListener(sendListener);

        try {
            recoverFieldsValue();
            if (PDPNHelper.getPDPNEnable(BetaUsersActivity.this)) {
                pdpnEnable = true;
                llPdpn.setVisibility(View.VISIBLE);
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

        String beta_user_info = PreferencesUtils
                .getSharedPreferences(this)
                .getString(MudahPreferencesUtils.BETA_USER_INFO, "{}");
        JSONObject betaUserJsonObject = new JSONObject(beta_user_info);

        if (betaUserJsonObject.length() > 0) {
            String betaUserName = betaUserJsonObject.getString("name");
            String betaUserEmail = betaUserJsonObject.getString("email");
            String betaUserPhone = betaUserJsonObject.getString("phone");

            name.setText(betaUserName);
            email.setText(betaUserEmail);
            phone.setText(betaUserPhone);
        }
    }

    /**
     * save the values of the field into shared pref
     *
     * @param pSent params of the beta user form
     * @throws org.json.JSONException
     */
    private void saveFieldsValue(Map<String, Object> pSent) throws JSONException {

        JSONObject betaUserJsonObject = new JSONObject();
        betaUserJsonObject.put("name", (String) pSent.get("name"));
        betaUserJsonObject.put("email", (String) pSent.get("email"));
        betaUserJsonObject.put("phone", (String) pSent.get("phone"));
        PreferencesUtils
                .getSharedPreferences(this).edit()
                .putString(MudahPreferencesUtils.BETA_USER_INFO, betaUserJsonObject.toString())
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();

        if (getSupportLoaderManager().getLoader(LOADER_BETAUSER_SIGNUP) != null) {
            getSupportLoaderManager().initLoader(LOADER_BETAUSER_SIGNUP, null,

                    asyncBetaUserSignUp()
            );
        }
    }

    /**
     * If user rotates the phone before the loader_betauser_signup has started. Hide
     * the loading screen. Otherwise, this screen will prevent user from
     * interacting with a screen
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d();
        if (getSupportLoaderManager().getLoader(LOADER_BETAUSER_SIGNUP) != null
                && !getSupportLoaderManager().getLoader(LOADER_BETAUSER_SIGNUP)
                .isStarted()) {
            Log.d("isstarted "
                    + getSupportLoaderManager().getLoader(LOADER_BETAUSER_SIGNUP)
                    .isStarted());
            DialogUtils.hideProgressDialog(BetaUsersActivity.this);
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

    private BlocketLoader.Callbacks asyncBetaUserSignUp() {
        Log.d();
        DialogUtils.showProgressDialog(this, getString(R.string.loading));
        final Map<String, Object> p = new HashMap<String, Object>();

        p.put("action", "add_beta_user");
        p.put("name", name.getText().toString());
        p.put("phone", phone.getText().toString());
        p.put("email", email.getText().toString().toLowerCase());
        try {
            if (optInInternal.isChecked()) {

                String optInValue = getPDPNoptinValue(this);
                p.put("opt_in", optInValue);
                p.put("opt_out", "0");

                //top is checked
                if (optInInternal.isChecked()) {

                    Map<String, String> pdpnQueues = new HashMap<String, String>();
                    pdpnQueues = PDPNHelper.getPDPNQueues(BetaUsersActivity.this);

                    int totalQueueValues = 0;
                    for (String key : pdpnQueues.keySet()) {
                        totalQueueValues += Integer.parseInt(key);
                    }
                    p.put("queues", String.valueOf(totalQueueValues));
                }

                //bottom is checked
                else if (!optInInternal.isChecked()) {
                    p.put("queues", "0");//TO DO
                }

                if (optInterview.isChecked()) {
                    p.put("opt_interview", "1");
                } else {
                    p.put("opt_interview", "0");
                }
            }

        } catch (JSONException e) {
            ACUtils.debug(e);
        }

        return new BlocketLoader.Callbacks(Method.POST, "users", p, this) {

            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                Log.d();
                onUserSignUp(data, p);
                getSupportLoaderManager().destroyLoader(loader.getId());
                DialogUtils.hideProgressDialog(BetaUsersActivity.this);
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                // general failure
                DialogUtils.hideProgressDialog(BetaUsersActivity.this);
                DialogUtils.showGeneralErrorAlert(BetaUsersActivity.this);
                getSupportLoaderManager().destroyLoader(loader.getId());
            }
        };
    }

    private void sendTagging() {
        String level2Menu = XitiUtils.getLevel2Map(XitiUtils.LEVEL2_BETA_USER_SIGNUP);
        if (!ACUtils.isEmpty(level2Menu)) {
            EventTrackingUtils.sendClick(level2Menu, BETA_USERS_PAGE, XitiUtils.NAVIGATION);
            tagTealium(level2Menu);
        }
    }

    private void tagTealium(String level2) {
        Map<String, String> tealiumData = Tealium.map(
                TealiumHelper.APPLICATION, TealiumHelper.APPLICATION_BETA_USR,
                TealiumHelper.PAGE_NAME, BETA_USERS_PAGE,
                TealiumHelper.XTN2, level2);

        TealiumHelper.track(this, tealiumData, Tealium.VIEW);
    }

    protected void onUserSignUp(JSONObject data, Map<String, Object> paramsReceived) {
        String status = data.optString("status");

        if (status.equals("OK") || status.equals("API_OK")) {
            sendTagging();

            Toast.makeText(BetaUsersActivity.this,
                    R.string.beta_user_dialog_toast_hint,
                    Toast.LENGTH_LONG).show();
            try {
                if (pdpnEnable) {
                    saveFieldsValue(paramsReceived);
                    PDPNHelper.saveOpttedInEmails(paramsReceived, this);
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
                    // Prod format: {"message":"MISSING_PARAMETER","description":"MISSING_PARAMETER","name":"email"}
                    // Regress format: {"phone":"INVALID_PHONE"}
                    params = errParams.getJSONObject(i).optString("name");
                    Log.d("error field: " + params + ", jsonObj: " + errParams.getJSONObject(i));
                    if (params != null) {
                        if (params.equals("email")) {
                            // In UI, email is before phone. So,the focus should be on 'email' field
                            // Because mapy returns an error in phone first, the focus is there and set 'needToRequestFocus' to false
                            // Have to set needToRequestFocus to true back so that the focus can be on 'email' field
                            ErrorHandler.needsToRequestFocus = true;
                            errorView = email;
                            setFocusToErrorField(email);
                        } else if (params.equals("name")) {
                            errorView = name;
                            setFocusToErrorField(name);
                        } else if (params.equals("phone")) {
                            errorView = phone;
                            setFocusToErrorField(phone);
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

    private void setFocusToErrorField(EditText field) {
        ErrorHandler.tryToFocus(field);
    }

    private boolean clientValidationError() {
        String errorMessage;
        boolean errorExist = false;
        ErrorHandler.needsToRequestFocus = true;

        if (ACUtils.isEmpty(email.getText().toString())) {
            setFocusToErrorField(email);
            errorMessage = getResources().getString(
                    R.string.contact_empty, "email");
            ErrorHandler.setError(errorMessage, email);
            errorExist = true;
        }

        if (!optInInternal.isChecked()) {
            Toast.makeText(BetaUsersActivity.this,
                    R.string.beta_user_cb_internal_unchecked,
                    Toast.LENGTH_LONG).show();
            errorExist = true;
        }

        return errorExist;
    }

}
