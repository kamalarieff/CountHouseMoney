package com.mudah.my.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.utils.ACUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.lib701.widgets.CustomEditTextState;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kalpana on 11/23/15.
 */
public class SignUpActivity extends MudahBaseActivity {
    public static final String ERROR_USER_ACCOUNT_UNVERIFIED = "ERROR_USER_ACCOUNT_UNVERIFIED";
    private static final String TERMS_PAGE = "file:///android_asset/terms.html";
    private static final String PRIVACY_PAGE = "file:///android_asset/privacy.html";
    private static final String GOOGLE_TYPE = "com.google";
    private static final int SELECT_ACCOUNT = 1;
    private static final int LOADER_USER_SIGNUP = 0x02;
    private static final String ACTION = "action";
    private static final String LOGIN_TYPE = "login_type";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String VERIFY_PASSWORD = "verification_password";
    private static final String APP_ID = "app_id";
    private static final String HASH = "hash";
    private static final String TOKEN = "token";
    private static final String USER_ID = "userId";
    private static final String MEMBER_ID = "member_id";
    private static final String REGISTERED = "registered";
    private static String SIGNUP = "signup";
    private static String EMAIL_LOGIN_TYPE = "email";
    private AutoCompleteTextView email = null;
    private CustomEditTextState password = null;
    private CustomEditTextState passwordConfirmed = null;
    private CheckBox agreeTC = null;
    private ProgressBar pbLoading;
    private String selectedEmail;
    private boolean isSkipActivation;

    private final View.OnClickListener signUpOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit:
                    MudahUtil.hideSoftKeyboard(SignUpActivity.this);
                    if (!clientValidationError()) {
                        signUp();
                    }
                    break;
                case R.id.tv_sign_in:
                    Intent intentSignIn = new Intent(getApplicationContext(), SignInActivity.class);
                    intentSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentSignIn);
                    EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNUP, TealiumHelper.EVENT_SIGNIN_LINK);
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.sign_up_title));

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(signUpOnClickListener);

        setLinkInText(this.findViewById(android.R.id.content));

        email = (AutoCompleteTextView) findViewById(R.id.email);
        password = (CustomEditTextState) findViewById(R.id.password);
        passwordConfirmed = (CustomEditTextState) findViewById(R.id.confirm_password);
        agreeTC = (CheckBox) findViewById(R.id.agree_tc);

        TextView signIn = (TextView) findViewById(R.id.tv_sign_in);
        signIn.setOnClickListener(signUpOnClickListener);

        TextView proniagaInfo = (TextView) findViewById(R.id.proniaga_info);
        proniagaInfo.setText(ACUtils.getHtmlFromString(getResources().getString(R.string.proniaga_notification)));

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        sendTagUserAccount(TealiumHelper.PAGE_UA_SIGNUP);

        setUpEmailChooser();
    }

    private void setUpEmailChooser() {
        final ArrayList<String> accountList = new ArrayList<>();
        try {
            Account[] accounts = AccountManager.get(this).getAccountsByType(GOOGLE_TYPE);
            for (Account account : accounts) {
                accountList.add(account.name);
            }

            if (accountList.size() > 0) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, accountList);
                email.setAdapter(adapter);
                email.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapter, View arg1,
                                            int position, long arg3) {
                        selectedEmail = (String) adapter.getItemAtPosition(position);
                        email.setText(selectedEmail);
                        Log.d("email selected: " + selectedEmail);
                    }
                });
            }
        } catch (Exception ignore) {
            Log.e("Exception:" + ignore);
        }
    }

    public void setLinkInText(View view) {
        final int START_TERMS_LINK = 31;
        final int END_TERMS_LINK = 51;
        final int START_PRIVACY_LINK = 57;
        final int END_PRIVACY_LINK = 71;

        String tncMsg = getResources().getString(R.string.agree_tc_msg);

        SpannableString spannableString = new SpannableString(tncMsg);

        ClickableSpan terms = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    showMessage(TERMS_PAGE);
                    sendTagUserAccount(TealiumHelper.PAGE_UA_TERMS_CONDITIONS);
                } catch (JSONException e) {
                    ACUtils.debug(e);
                }
            }

            public void updateDrawState(TextPaint ds) {// override updateDrawState
                ds.setUnderlineText(false); // set to false to remove underline
            }
        };
        ClickableSpan privacy = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    showMessage(PRIVACY_PAGE);
                    sendTagUserAccount(TealiumHelper.PAGE_UA_PRIVACY_POLICY);
                } catch (JSONException e) {
                    ACUtils.debug(e);
                }
            }

            public void updateDrawState(TextPaint ds) {// override updateDrawState
                ds.setUnderlineText(false); // set to false to remove underline
            }
        };

        if (END_TERMS_LINK > tncMsg.length()) {
            spannableString.setSpan(terms, 0, END_TERMS_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(terms, START_TERMS_LINK, END_TERMS_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.sign_form_focus_text)), START_TERMS_LINK, END_TERMS_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (END_PRIVACY_LINK > tncMsg.length()) {
            spannableString.setSpan(privacy, 0, END_PRIVACY_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(privacy, START_PRIVACY_LINK, END_PRIVACY_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.sign_form_focus_text)), START_PRIVACY_LINK, END_PRIVACY_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        TextView textView = (TextView) view.findViewById(R.id.agree_tc_msg);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_in:
                Intent intentSignIn = new Intent(getApplicationContext(), SignInActivity.class);
                intentSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSignIn);
                EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNUP, TealiumHelper.EVENT_SIGNIN_MENU);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();

        if (getSupportLoaderManager().getLoader(LOADER_USER_SIGNUP) != null) {
            signUp();
        }
    }

    protected void signUp() {
        String userEmail = email.getText().toString().toLowerCase();
        String userPassword = password.getText().toString();
        String userPasswordConfirmed = passwordConfirmed.getText().toString();
        isSkipActivation = false;

        if (userEmail.equalsIgnoreCase(selectedEmail)) {
            isSkipActivation = true;
        }

        getSupportLoaderManager().initLoader(LOADER_USER_SIGNUP, null, asyncSignUp(userEmail, userPassword, userPasswordConfirmed));
    }

    private APILoader.Callbacks asyncSignUp(String email, String password, String userPasswordConfirmed) {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        String endpoint = "signup";
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ACTION, SIGNUP);
        params.put(LOGIN_TYPE, EMAIL_LOGIN_TYPE);
        params.put(EMAIL, email);
        params.put(PASSWORD, password);
        params.put(VERIFY_PASSWORD, userPasswordConfirmed);
        if (isSkipActivation) {
            endpoint = "autosignup";
            params.put(APP_ID, Config.appId);
            params.put(HASH, Constants.EMPTY_STRING);
        }

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, endpoint, params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onUserSignUp(data, params);
                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(SignUpActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onUserSignUp(JSONObject data, Map<String, Object> paramsReceived) {
        String status = data.optString(Constants.STATUS);
        String message = data.optString(Constants.MSG);

        if (status.equals(Constants.OK)) {
            paramsReceived.put(TOKEN, data.optString(TOKEN));
            paramsReceived.put(USER_ID, data.optString(MEMBER_ID));
            paramsReceived.put(REGISTERED, Config.userRegistered);
            Config.userAccount.setUserDataPreferences(getApplicationContext(), paramsReceived);
            sendTagSignUp();
            if (isSkipActivation) {
                EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNUP, TealiumHelper.EVENT_PHONE_ACCOUNT_SIGNUP_SUCCESS);

                Intent intentSignIn = new Intent(getApplicationContext(), SignInActivity.class);
                intentSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentSignIn.putExtra(Constants.SOURCE, Constants.ACTIVATION);
                startActivity(intentSignIn);
            } else {
                EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNUP, TealiumHelper.EVENT_SIGNUP_SUCCESS);

                Intent intentSignUpActivation = new Intent(getApplicationContext(), SignUpActivationActivity.class);
                startActivity(intentSignUpActivation);
            }
            finish();
        } else {
            ErrorHandler.needsToRequestFocus = true;

            Toast.makeText(SignUpActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
            if (ERROR_USER_ACCOUNT_UNVERIFIED.equalsIgnoreCase(data.optString(Constants.CODE))) {
                Config.userAccount.setEmail((String) paramsReceived.get("email"));
                Intent intentSignUpActivation = new Intent(getApplicationContext(), SignUpActivationActivity.class);
                intentSignUpActivation.putExtra(SignUpActivationActivity.RESEND_LINK, true);
                startActivity(intentSignUpActivation);
                finish();
            }
        }
    }

    private void sendTagSignUp() {
        KahunaHelper.tagEvent(KahunaHelper.REGISTERED_ACCOUNT);
        AmplitudeUtils.tagEvent(AmplitudeUtils.REGISTER_EVENT);
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
        } else if (!email.getText().toString().trim().matches(Constants.EMAIL_VALIDATOR)) {
            setFocusToErrorField(email);
            errorMessage = getResources().getString(R.string.contact_email_invalid);
            ErrorHandler.setError(errorMessage, email);
            errorExist = true;
        } else {
            //clear the error state
            ErrorHandler.setError(null, email);
        }

        String userPassword = password.getText().toString();
        String userPasswordConfirmed = passwordConfirmed.getText().toString();

        if (ACUtils.isEmpty(userPassword)) {
            setFocusToErrorField(password);
            errorMessage = getResources().getString(
                    R.string.contact_empty, "password");
            ErrorHandler.setError(errorMessage, password);
            errorExist = true;
        } else if (!userPassword.equals(userPasswordConfirmed)) {
            setFocusToErrorField(passwordConfirmed);
            errorMessage = getResources().getString(
                    R.string.contact_invalid, "confirm_password");
            ErrorHandler.setError(errorMessage, passwordConfirmed);
            errorExist = true;
        } else {
            //clear the error state
            ErrorHandler.setError(null, password);
        }
        //only show this message, when there is no other error
        if (!errorExist && !agreeTC.isChecked()) {
            Toast.makeText(SignUpActivity.this,
                    R.string.user_signup_cb_internal_unchecked,
                    Toast.LENGTH_LONG).show();
            errorExist = true;
        }

        return errorExist;
    }
}
