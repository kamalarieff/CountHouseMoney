package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.utils.ACUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.EventTrackingUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpIntroActivity extends MudahBaseActivity {
    public static final String EXTRA_SIGN_UP_EMAIL = "extra_sign_up_email";
    public static final String EXTRA_SIGN_UP_PASSW = "extra_sign_up_passw";
    private static final String ACTION = "signup";
    private static final String LOGIN_TYPE = "email";
    private static final int LOADER_USER_SIGNUP = 0x01;
    private String email;
    private String password;
    private ProgressBar pbLoading;

    private final View.OnClickListener signUpOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_no_thx:
                    Intent intent = new Intent(SignUpIntroActivity.this, AdsListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNUP_INTRO, TealiumHelper.EVENT_NO_THX);
                    finish();
                    break;
                case R.id.btn_sign_now:
                    if (ACUtils.isEmpty(email) || ACUtils.isEmpty(password)) {
                        Intent intentSignIn = new Intent(SignUpIntroActivity.this, SignUpActivity.class);
                        intentSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentSignIn);
                    } else {
                        signUp();
                    }
                    break;
                case R.id.tv_sign_in:
                    Intent intentSignIn = new Intent(SignUpIntroActivity.this, SignInActivity.class);
                    intentSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentSignIn);
                    EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNUP_INTRO, TealiumHelper.EVENT_SIGNIN_LINK);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_intro);

        Button btnNoThx = (Button) findViewById(R.id.btn_no_thx);
        btnNoThx.setOnClickListener(signUpOnClickListener);

        Button btnSignNow = (Button) findViewById(R.id.btn_sign_now);
        btnSignNow.setOnClickListener(signUpOnClickListener);

        TextView signIn = (TextView) findViewById(R.id.tv_sign_in);
        signIn.setOnClickListener(signUpOnClickListener);

        TextView introDesc = (TextView) findViewById(R.id.intro_desc);
        introDesc.setText(ACUtils.getHtmlFromString(getResources().getString(R.string.intro_desc)));

        TextView proniagaInfo = (TextView) findViewById(R.id.proniaga_info);
        proniagaInfo.setText(ACUtils.getHtmlFromString(getResources().getString(R.string.intro_proniaga_notification)));

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        Bundle bundle = getIntent().getExtras();
        if (savedInstanceState != null) {
            email = savedInstanceState.getString(EXTRA_SIGN_UP_EMAIL);
            password = savedInstanceState.getString(EXTRA_SIGN_UP_PASSW);
        }
        if (bundle != null) {
            email = bundle.getString(EXTRA_SIGN_UP_EMAIL);
            password = bundle.getString(EXTRA_SIGN_UP_PASSW);
        }
        sendTagUserAccount(TealiumHelper.PAGE_UA_SIGNUP_INTRO);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(EXTRA_SIGN_UP_EMAIL, email);
        savedInstanceState.putString(EXTRA_SIGN_UP_PASSW, password);
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void signUp() {
        getSupportLoaderManager().initLoader(LOADER_USER_SIGNUP, null, asyncSignUp(email, password));
    }

    private APILoader.Callbacks asyncSignUp(String email, String password) {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", ACTION);
        params.put("login_type", LOGIN_TYPE);
        params.put("plainpass", "1");
        params.put("email", email);
        params.put("password", password);
        params.put("verification_password", password);

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "signup", params, this) {
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

                Toast.makeText(SignUpIntroActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onUserSignUp(JSONObject data, Map<String, Object> paramsSent) {
        String status = data.optString(Constants.STATUS);
        String message = data.optString(Constants.MSG);

        if (status.equals(Constants.OK)) {
            paramsSent.put("token", data.optString("token"));
            paramsSent.put("userId", data.optString("member_id"));
            paramsSent.put("registered", Config.userRegistered);
            Config.userAccount.setUserDataPreferences(getApplicationContext(), paramsSent);

            EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNUP, TealiumHelper.EVENT_SIGNUP_POST_AD_SUCCESS);
            KahunaHelper.tagEvent(KahunaHelper.REGISTERED_ACCOUNT);

            Intent intentSignUpActivation = new Intent(getApplicationContext(), SignUpActivationActivity.class);
            startActivity(intentSignUpActivation);

            finish();
        } else {
            ErrorHandler.needsToRequestFocus = true;

            Toast.makeText(SignUpIntroActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
            if (SignUpActivity.ERROR_USER_ACCOUNT_UNVERIFIED.equalsIgnoreCase(data.optString(Constants.CODE))) {
                Config.userAccount.setEmail((String) paramsSent.get("email"));
                Intent intentSignUpActivation = new Intent(getApplicationContext(), SignUpActivationActivity.class);
                intentSignUpActivation.putExtra(SignUpActivationActivity.RESEND_LINK, true);
                startActivity(intentSignUpActivation);
                finish();
            }
        }
    }
}
