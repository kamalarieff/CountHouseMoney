package com.mudah.my.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.utils.ACUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kalpana on 11/24/15.
 */
public class SignUpActivationActivity extends MudahBaseActivity {
    public static final String RESEND_LINK = "resend_link";
    public static final String REDIRECT_FROM_SIGNIN = "REDIRECT_FROM_SIGNIN";
    private static final int LOADER_USER_ACTIVATION = 0x02;
    private static final String ACTION_PARAM = "c";
    private static final String RESET_PASSW_PARAM = "resetpasswd";
    private static final String ERROR_USER_ACCOUNT_ALREADY_ACTIVATED = "ERROR_USER_ACCOUNT_ALREADY_ACTIVATED";
    private String userToken = null;
    private ProgressBar pbLoading;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up_activation);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        if (getIntent() != null && getIntent().getBooleanExtra(REDIRECT_FROM_SIGNIN, false)) {
            actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.sign_in_title));
        } else {
            actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.sign_up_title));
        }

        Intent intentSignUp = new Intent(getApplicationContext(), SignUpActivity.class);
        intentSignUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setLinkInText(this.findViewById(android.R.id.content));
        setSpanClickable(this.findViewById(android.R.id.content), 30, 43, R.string.wrong_email, R.id.wrong_email, intentSignUp);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        Uri uri = getIntent().getData();

        if (uri != null && Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            String strUrl = uri.toString();
            Log.d("Redirected from " + strUrl);
            try {

                if (!ACUtils.isEmpty(uri.getQueryParameter(Constants.TOKEN))) {
                    userToken = uri.getQueryParameter(Constants.TOKEN);
                }

                if (!ACUtils.isEmpty(uri.getQueryParameter(ACTION_PARAM))) {
                    activateUser(uri.getQueryParameter(ACTION_PARAM), userToken);
                } else if (!ACUtils.isEmpty(uri.getQueryParameter(RESET_PASSW_PARAM))) {

                    Intent intent = new Intent(this, UpdatePasswordActivity.class);
                    intent.putExtra(Constants.TOKEN, userToken);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                ACUtils.debug(e, "link_url", strUrl, false);
                Intent intent = new Intent(this, AdsListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        }

        if (getIntent() != null && (getIntent().getBooleanExtra(RESEND_LINK, false) || getIntent().getBooleanExtra(REDIRECT_FROM_SIGNIN, false))) {
            TextView tvSendingMail = (TextView) findViewById(R.id.sending_email);
            tvSendingMail.setVisibility(View.GONE);
        }

        if (!ACUtils.isEmpty(Config.userAccount.getEmail())) {
            TextView tvUserEmail = (TextView) findViewById(R.id.tv_sign_up_activation_msg);
            tvUserEmail.setText(ACUtils.getHtmlFromString(getString(R.string.activation_2, Config.userAccount.getEmail())));
            sendTagUserAccount(TealiumHelper.PAGE_UA_SIGNUP_EMAIL_SENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();
    }

    public void setLinkInText(View view) {
        final int START_ACTIVATION_LINK = 22;
        final int END_ACTIVATION_LINK = 44;

        String activationMsg = getResources().getString(R.string.activation_4);

        SpannableString spannableString = new SpannableString(activationMsg);

        ClickableSpan activateLink = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                onActivateEmailSent();
            }

            @Override
            public void updateDrawState(TextPaint ds) {// override updateDrawState
                ds.setUnderlineText(false); // set to false to remove underline
            }
        };

        if (END_ACTIVATION_LINK > activationMsg.length()) {
            spannableString.setSpan(activateLink, 0, END_ACTIVATION_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(activateLink, START_ACTIVATION_LINK, END_ACTIVATION_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.sign_form_focus_text)), START_ACTIVATION_LINK, END_ACTIVATION_LINK, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        TextView textView = (TextView) view.findViewById(R.id.act_4);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    protected void onActivateEmailSent() {
        Log.d();
        getSupportLoaderManager().initLoader(LOADER_USER_ACTIVATION, null, asyncResendActivationLink(Config.userAccount.getEmail(), userToken));
    }

    protected void activateUser(String action, String token) {
        if (Constants.ACTIVATION.equals(action)) {
            getSupportLoaderManager().initLoader(LOADER_USER_ACTIVATION, null, asyncUserActivation(token));
        } else {
            Toast.makeText(SignUpActivationActivity.this,
                    R.string.user_deeplink_invalid,
                    Toast.LENGTH_LONG).show();
        }
    }

    private APILoader.Callbacks asyncResendActivationLink(String email, String token) {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("email", email);

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "resendactivation", params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onEmailSent(data, params);
                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(SignUpActivationActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onEmailSent(JSONObject data, Map<String, Object> paramsReceived) {
        String status = data.optString(Constants.STATUS);
        String message = data.optString(Constants.MSG);
        if (status.equals(Constants.OK)) {
            Toast.makeText(SignUpActivationActivity.this,
                    R.string.activate_toast,
                    Toast.LENGTH_LONG).show();
        } else {
            ErrorHandler.needsToRequestFocus = true;

            Toast.makeText(SignUpActivationActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
        }
    }

    private APILoader.Callbacks asyncUserActivation(String token) {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("token", token);
        params.put("email", Config.userAccount.getEmail());

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "activate", params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onUserActivated(data, params);
                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(SignUpActivationActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onUserActivated(JSONObject data, Map<String, Object> paramsReceived) {
        String status = data.optString(Constants.STATUS);
        String code = data.optString(Constants.CODE);
        String message = data.optString(Constants.MSG);
        //status OK or ERROR_USER_ACCOUNT_TOKEN_USED in case users click it multiple times
        if (status.equals(Constants.OK) || (ERROR_USER_ACCOUNT_ALREADY_ACTIVATED).equalsIgnoreCase(code)) {
            sendTagUserAccount(TealiumHelper.PAGE_UA_SIGNUP_ACTIVATED);

            Toast.makeText(SignUpActivationActivity.this,
                    R.string.user_account_sign_in,
                    Toast.LENGTH_LONG).show();

            Intent intentSignIn = new Intent(getApplicationContext(), SignInActivity.class);
            intentSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentSignIn.putExtra(Constants.SOURCE, Constants.ACTIVATION);
            startActivity(intentSignIn);

            finish();
        } else {
            ErrorHandler.needsToRequestFocus = true;

            Toast.makeText(SignUpActivationActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
        }
    }
}
