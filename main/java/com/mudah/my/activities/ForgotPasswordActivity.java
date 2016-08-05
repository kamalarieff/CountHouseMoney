package com.mudah.my.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lib701.utils.ACUtils;
import com.lib701.utils.ErrorHandler;
import com.lib701.utils.Log;
import com.lib701.widgets.CustomEditTextState;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ondo on 26/11/15.
 */
public class ForgotPasswordActivity extends MudahBaseActivity {
    private static final int LOADER_FORGOT_PASSWORD = 0x02;
    private CustomEditTextState email = null;
    private ProgressBar pbLoading;
    private final View.OnClickListener forgotPasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.submit:
                    MudahUtil.hideSoftKeyboard(ForgotPasswordActivity.this);
                    if (!clientValidationError()) {
                        requestResetPassword();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        setContentView(R.layout.activity_forgot_password);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.forgot_password_title));

        email = (CustomEditTextState) findViewById(R.id.email);

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(forgotPasswordOnClickListener);

        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        sendTagUserAccount(TealiumHelper.PAGE_UA_FORGOT_PASSWORD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d();

        if (getSupportLoaderManager().getLoader(LOADER_FORGOT_PASSWORD) != null) {
            requestResetPassword();
        }
    }

    protected void requestResetPassword() {
        String userEmail = email.getText().toString().toLowerCase();
        getSupportLoaderManager().initLoader(LOADER_FORGOT_PASSWORD, null, asyncRequestResetPassword(userEmail));
    }

    private APILoader.Callbacks asyncRequestResetPassword(String email) {
        Log.d();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("email", email);

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "forgetpassword", params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onPasswordResetRequested(data, params);
                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(ForgotPasswordActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.GONE);
            }
        };
    }

    protected void onPasswordResetRequested(JSONObject data, Map<String, Object> paramsReceived) {
        String status = data.optString(Constants.STATUS);
        String message = data.optString(Constants.MSG);
        if (status.equals(Constants.OK)) {
            email.setText(Constants.EMPTY_STRING);
            Config.userAccount.setUserDataPreferences(getApplicationContext(), paramsReceived);
            sendTagUserAccount(TealiumHelper.PAGE_UA_FORGOT_PASSWORD_EMAIL_SENT);

            Toast.makeText(ForgotPasswordActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
        } else {
            ErrorHandler.needsToRequestFocus = true;

            Toast.makeText(ForgotPasswordActivity.this,
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
        }

        return errorExist;
    }
}
