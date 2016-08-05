package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lib701.utils.ACUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.lib701.widgets.CustomEditTextState;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.ChatCafe;
import com.mudah.my.models.UserAccountModel;
import com.mudah.my.utils.AmplitudeUtils;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.utils.ServerUtils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ondo on 24/11/15.
 */
public class SignInActivity extends MudahBaseActivity {
    public static final String FROM_SIGNIN = "from_signin";
    public static final String SIGNIN_SOURCE = "android_mudah";
    private static final String CODE_LOGIN_USER_NOT_ACTIVE = "ERROR_USER_ACCOUNT_LOGIN_USER_NOT_ACTIVE";
    private static final int LOADER_USER_SIGNIN = 0x02;
    private final String SOURCE = "source";
    private CustomEditTextState email = null;
    private CustomEditTextState password = null;
    private ProgressBar pbLoading;
    private String chatRoomId;
    private boolean isGoToInbox = false;
    private boolean isGoToChat = false;
    private boolean showReloginMsg = false;
    private AdViewAd acAd;
    private final View.OnClickListener signInOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit:
                    MudahUtil.hideSoftKeyboard(SignInActivity.this);
                    if (!clientValidationError()) {
                        signIn();
                    }
                    break;
                case R.id.tv_sign_up:
                    Intent intentSignUp = new Intent();
                    intentSignUp.setClass(getApplicationContext(), SignUpActivity.class);
                    intentSignUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentSignUp);
                    EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNIN, TealiumHelper.EVENT_SIGNUP_LINK);
                    break;
                case R.id.tv_forgot_password:
                    Intent intentForgotPassword = new Intent();
                    intentForgotPassword.setClass(getApplicationContext(), ForgotPasswordActivity.class);
                    intentForgotPassword.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentForgotPassword);
                    EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNIN, TealiumHelper.EVENT_FORGOT_PASSWORD_LINK);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.strictMode();
        Log.d();

        setContentView(R.layout.activity_sign_in);
        Log.d("savedInstanceState here : " + savedInstanceState);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.sign_in_title));

        email = (CustomEditTextState) findViewById(R.id.email);
        password = (CustomEditTextState) findViewById(R.id.password);

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(signInOnClickListener);

        TextView signUp = (TextView) findViewById(R.id.tv_sign_up);
        signUp.setOnClickListener(signInOnClickListener);

        TextView forgotPassword = (TextView) findViewById(R.id.tv_forgot_password);
        forgotPassword.setOnClickListener(signInOnClickListener);

        TextView proniagaInfo = (TextView) findViewById(R.id.proniaga_info);
        proniagaInfo.setText(ACUtils.getHtmlFromString(getResources().getString(R.string.proniaga_notification)));

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        chatRoomId = null;
        if (getIntent() != null) {
            if (getIntent().hasExtra(Constants.PUSH_CHAT_ROOM_ID)) {
                chatRoomId = getIntent().getStringExtra(Constants.PUSH_CHAT_ROOM_ID);
                isGoToChat = true;
            } else if (getIntent().hasExtra(Constants.CHAT_INBOX)) {
                isGoToInbox = getIntent().getBooleanExtra(Constants.CHAT_INBOX, false);
            } else if (getIntent().hasExtra(Constants.CHAT_PAGE)) {
                isGoToChat = getIntent().getBooleanExtra(Constants.CHAT_PAGE, false);
                acAd = (AdViewAd) getIntent().getSerializableExtra(Constants.CHAT_PRODUCT);
            } else if (getIntent().hasExtra(Constants.RELOGIN_MSG)) {
                showReloginMsg = getIntent().getBooleanExtra(Constants.RELOGIN_MSG, false);
                if (showReloginMsg) {
                    TextView textMsg = (TextView) findViewById(R.id.intro_txt);
                    textMsg.setText(R.string.msg_relogin);

                    if (!ACUtils.isEmpty(Config.userAccount.getEmail())) {
                        email.setText(Config.userAccount.getEmail());
                        password.requestFocus();
                    }
                }
            }

            if (getIntent().hasExtra(Constants.SOURCE)) {
                TextView activatedHeader = (TextView) findViewById(R.id.activated_header);
                activatedHeader.setVisibility(View.VISIBLE);

                TextView activatedtext = (TextView) findViewById(R.id.activated_text);
                activatedtext.setVisibility(View.VISIBLE);
                if (!ACUtils.isEmpty(Config.userAccount.getEmail())) {
                    email.setText(Config.userAccount.getEmail());
                    password.requestFocus();
                }
            }
        }

        sendTagUserAccount(TealiumHelper.PAGE_UA_SIGNIN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();

        if (getSupportLoaderManager().getLoader(LOADER_USER_SIGNIN) != null) {
            signIn();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_up:
                Intent intentSignUp = new Intent();
                intentSignUp.setClass(getApplicationContext(), SignUpActivity.class);
                intentSignUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSignUp);
                EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNIN, TealiumHelper.EVENT_SIGNUP_MENU);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void signIn() {
        String userEmail = email.getText().toString().toLowerCase();
        String userPassword = password.getText().toString();

        getSupportLoaderManager().initLoader(LOADER_USER_SIGNIN, null, asyncSignIn(userEmail, userPassword));
    }

    private APILoader.Callbacks asyncSignIn(String email, String password) {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(UserAccountModel.EMAIL, email);
        params.put(UserAccountModel.PASSWORD, password);
        params.put(SOURCE, SIGNIN_SOURCE);

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "login", params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onUserSignIn(data, params);
                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(SignInActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onUserSignIn(JSONObject data, Map<String, Object> paramsReceived) {
        String status = data.optString(Constants.STATUS);
        String code = data.optString(Constants.CODE);

        if (status.equals(Constants.OK)) {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, Object> map = gson.fromJson(data.toString(), type);

            if (!ACUtils.isEmpty(data.optString(UserAccountModel.BIRTHDAY))) {
                map.put(UserAccountModel.BIRTHMONTH, data.optString(UserAccountModel.BIRTHDAY).split(Constants.DATE_SEPERATOR)[1]);
                map.put(UserAccountModel.BIRTHYEAR, data.optString(UserAccountModel.BIRTHDAY).split(Constants.DATE_SEPERATOR)[0]);
            }

            if (map != null) {
                KahunaHelper.tagProfileAttributes(map);
            }

            map.putAll(paramsReceived);

            map.put("userAccountId", data.optString("user_acct_id"));
            map.put(UserAccountModel.USER_ID, data.optString("member_id"));
            map.put(UserAccountModel.USERNAME, data.optString("member_name"));
            map.put(UserAccountModel.REGISTERED, Config.userRegistered);

            Crashlytics.getInstance().core.setString(UserAccountModel.USER_ID, data.optString("member_id"));
            Config.userAccount.setLastSignIn((new Date()).getTime());
            Config.userAccount.setUserDataPreferences(getApplicationContext(), map);
            AmplitudeUtils.tagEvent(AmplitudeUtils.SIGN_IN);
            AmplitudeUtils.trackAllUserProperties();
            TealiumHelper.setGlobalCommonDataUser();
            KahunaHelper.tagAttributes(KahunaHelper.PAGE_USER_ACCOUNT, KahunaHelper.LOGIN_STATUS, KahunaHelper.YES);
            Toast.makeText(SignInActivity.this,
                    R.string.user_account_sign_in,
                    Toast.LENGTH_SHORT).show();

            if (Config.enableChat) {
                ChatCafe.logInChatCafe();
                registerGcmToHydra();
                if (isGoToInbox) {
                    Intent intent = new Intent(getApplicationContext(), InboxActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else if (isGoToChat && !ACUtils.isEmpty(chatRoomId)) {
                    ChatActivity.buildStackChatRoomListActivity(SignInActivity.this, chatRoomId);
                } else if (isGoToChat && acAd != null) {
                    //disable chat if users click on their own ad
                    if (!acAd.getUserId().equals(Config.userAccount.getUserId())) {
                        ChatActivity.start(getApplicationContext(), acAd);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.own_chat_msg, Toast.LENGTH_LONG).show();
                        goToLiveAds();
                    }
                }
            }

            EventTrackingUtils.sendUAClick(TealiumHelper.ACTIVITY_SIGNIN, TealiumHelper.EVENT_SIGNIN_SUCCESS);
            //If users click on Push Noti, chatRoomId is not empty, redirect to Chat page
            //If users click on Chat Menu, redirect to Inbox page
            //Otherwise, redirect to User Live Ads List page
            if (!isGoToInbox && !isGoToChat) {
                goToLiveAds();
            }
            finish();

        } else if (CODE_LOGIN_USER_NOT_ACTIVE.equalsIgnoreCase(code)) {
            Config.userAccount.setEmail((String) paramsReceived.get(UserAccountModel.EMAIL));
            Intent intentSignUpActivation = new Intent(getApplicationContext(), SignUpActivationActivity.class);
            intentSignUpActivation.putExtra(SignUpActivationActivity.REDIRECT_FROM_SIGNIN, true);
            startActivity(intentSignUpActivation);
        } else {
            ErrorHandler.needsToRequestFocus = true;
            String message = getString(R.string.invalid_sign_in);

            if (password != null) {
                setFocusToErrorField(password);
                ErrorHandler.setError(message, password);
            }
        }
    }

    private void goToLiveAds() {
        Intent intentLiveAds = new Intent(getApplicationContext(), UserLiveAdsActivity.class);
        intentLiveAds.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLiveAds.putExtra(FROM_SIGNIN, true);
        startActivity(intentLiveAds);
    }

    public void registerGcmToHydra() {
        try {
            ServerUtils.postDevice(MudahUtil.getDeviceId(this));
        } catch (Exception exp) {
            Log.e(exp);
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
        } else if (!email.getText().toString().trim().matches(Constants.EMAIL_VALIDATOR)) {
            setFocusToErrorField(email);
            errorMessage = getResources().getString(R.string.contact_email_invalid);
            ErrorHandler.setError(errorMessage, email);
            errorExist = true;
        } else {
            //clear the error state
            ErrorHandler.setError(null, email);
        }

        if (ACUtils.isEmpty(password.getText().toString())) {
            setFocusToErrorField(password);
            errorMessage = getResources().getString(
                    R.string.contact_invalid, "password");
            ErrorHandler.setError(errorMessage, password);
            errorExist = true;
        } else {
            //clear the error state
            ErrorHandler.setError(null, password);
        }

        return errorExist;
    }
}
