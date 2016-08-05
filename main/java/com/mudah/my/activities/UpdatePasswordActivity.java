package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
 * Created by ondo on 26/11/15.
 */
public class UpdatePasswordActivity extends MudahBaseActivity {
    private static final String PASSWORD = "password";
    private static final String CONFIRM_PASSWORD = "confirm_password";
    private static final int LOADER_UPDATE_PASSWORD = 0x02;
    private EditText password = null;
    private EditText passwordConfirmed = null;
    private String token = null;
    private ProgressBar pbLoading;
    private final View.OnClickListener UpdatePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit:
                    if (!clientValidationError()) {
                        resetPassword();
                    }
                    break;
                case R.id.tv_forgot_password:
                    Intent intentForgotPasswd = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                    intentForgotPasswd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentForgotPasswd);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.strictMode();
        Log.d();

        setContentView(R.layout.activity_update_password);
        Log.d("savedInstanceState here : " + savedInstanceState);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.update_password_title));
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        password = (EditText) findViewById(R.id.password);
        passwordConfirmed = (EditText) findViewById(R.id.confirm_password);

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(UpdatePasswordOnClickListener);

        TextView forgotPasswd = (TextView) findViewById(R.id.tv_forgot_password);
        forgotPasswd.setOnClickListener(UpdatePasswordOnClickListener);

        if (!ACUtils.isEmpty(Config.userAccount.getEmail())) {
            TextView updatePasswdMsg = (TextView) findViewById(R.id.update_password_top_message);
            updatePasswdMsg.setText(ACUtils.getHtmlFromString(getString(R.string.update_password_top_message, Config.userAccount.getEmail())));
            sendTagUserAccount(TealiumHelper.PAGE_UA_SIGNUP_EMAIL_SENT);
        }

        Intent intent = getIntent();
        token = intent.getStringExtra(Constants.TOKEN);
        sendTagUserAccount(TealiumHelper.PAGE_UA_RESET_PASSWORD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();

        if (getSupportLoaderManager().getLoader(LOADER_UPDATE_PASSWORD) != null) {
            resetPassword();
        }
    }

    protected void resetPassword() {
        String userPassword = password.getText().toString();
        String userToken = token;
        getSupportLoaderManager().initLoader(LOADER_UPDATE_PASSWORD, null, asyncResetPassword(userPassword, userToken));
    }

    private APILoader.Callbacks asyncResetPassword(String password, String token) {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(Constants.TOKEN, token);
        params.put(PASSWORD, password);

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "resetpassword", params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onPasswordReset(data, params);
                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(UpdatePasswordActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onPasswordReset(JSONObject data, Map<String, Object> paramsReceived) {
        String status = data.optString(Constants.STATUS);
        String message = data.optString(Constants.MSG);
        if (status.equals(Constants.OK)) {
            Config.userAccount.setUserDataPreferences(getApplicationContext(), paramsReceived);
            sendTagUserAccount(TealiumHelper.PAGE_UA_RESET_PASSWORD_EMAIL_SENT);
            Toast.makeText(UpdatePasswordActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
            Intent intentSignIn = new Intent(getApplicationContext(), SignInActivity.class);
            intentSignIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentSignIn);

            finish();
        } else {
            ErrorHandler.needsToRequestFocus = true;

            Toast.makeText(UpdatePasswordActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setFocusToErrorField(EditText field) {
        ErrorHandler.tryToFocus(field);
    }

    private boolean clientValidationError() {
        String errorMessage;
        boolean errorExist = false;
        ErrorHandler.needsToRequestFocus = true;

        if (ACUtils.isEmpty(password.getText().toString())) {
            setFocusToErrorField(password);
            errorMessage = getResources().getString(
                    R.string.contact_empty, PASSWORD);
            ErrorHandler.setError(errorMessage, password);
            errorExist = true;
        }

        String userPassword = password.getText().toString();
        String userPasswordConfirmed = passwordConfirmed.getText().toString();

        if (!userPassword.equals(userPasswordConfirmed)) {
            setFocusToErrorField(passwordConfirmed);
            errorMessage = getResources().getString(
                    R.string.contact_invalid, CONFIRM_PASSWORD);
            ErrorHandler.setError(errorMessage, passwordConfirmed);
            errorExist = true;
        }

        return errorExist;
    }
}
