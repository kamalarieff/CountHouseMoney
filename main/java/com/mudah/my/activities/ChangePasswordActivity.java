package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
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
import com.mudah.my.helpers.KahunaHelper;
import com.mudah.my.loaders.APILoader;
import com.mudah.my.loaders.Method;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kalpana on 5/18/16.
 */
public class ChangePasswordActivity extends FragmentActivity {
    private static final int LOADER_UPDATE_PASSWORD = 0x02;
    private static final int LOADER_LOGOUT = 0x04;
    private static final String CURRENT_PASSWORD = "current_password";
    private static final String PASSWORD = "password";
    private static final String CONFIRM_PASSWORD = "confirm_password";
    private static final String NEW_PASSWORD = "new_password";
    private static final String TRANS_ERROR = "TRANS_ERROR";
    private EditText password = null;
    private EditText passwordConfirmed = null;
    private EditText currentPassword = null;
    private ProgressBar pbLoading;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cancel_dialog:
                    finish();
                    break;
                case R.id.set:
                    if (!clientValidationError()) {
                        changePassword();
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_password);
        getWindow().setBackgroundDrawable(null);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        currentPassword = (EditText) findViewById(R.id.current_password);
        password = (EditText) findViewById(R.id.password);
        passwordConfirmed = (EditText) findViewById(R.id.confirm_password);

        TextView cancel = (TextView) findViewById(R.id.cancel_dialog);
        cancel.setOnClickListener(onClickListener);
        TextView set = (TextView) findViewById(R.id.set);
        set.setOnClickListener(onClickListener);
    }

    protected void changePassword() {
        getSupportLoaderManager().initLoader(LOADER_UPDATE_PASSWORD, null, asyncChangePassword());
    }

    private APILoader.Callbacks asyncChangePassword() {
        Log.d();
        String userToken = Config.userAccount.getToken();
        String oldPassword = currentPassword.getText().toString();
        String newPassword = password.getText().toString();
        pbLoading.setVisibility(View.VISIBLE);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(Constants.TOKEN, userToken);
        params.put(CURRENT_PASSWORD, oldPassword);
        params.put(NEW_PASSWORD, newPassword);

        return new APILoader.Callbacks(Config.userAccountApiUrl, Method.POST, "changepassword/" + userToken, params, this) {
            @Override
            public void onLoadComplete(APILoader loader, JSONObject data) {
                Log.d("Success to request API, data: " + data);
                onPasswordChange(data);
                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadError(APILoader loader, JSONObject data) {
                Log.d("Failed to request API, result: " + data);

                Toast.makeText(ChangePasswordActivity.this,
                        R.string.api_request_failed,
                        Toast.LENGTH_LONG).show();

                getSupportLoaderManager().destroyLoader(loader.getId());
                pbLoading.setVisibility(View.INVISIBLE);
            }
        };
    }

    protected void onPasswordChange(JSONObject data) {
        String status = data.optString(Constants.STATUS);
        String message = data.optString(Constants.MSG);
        if (status.equals(Constants.OK)) {
            Config.userAccount.setPasswordInPreferences(getApplicationContext(), password.getText().toString());
            Toast.makeText(ChangePasswordActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
            signOut();
        } else {
            ErrorHandler.needsToRequestFocus = true;
            if (ACUtils.isEmpty(message) || TRANS_ERROR.equalsIgnoreCase(message)) {
                message = getString(R.string.connection_lost);
            }
            Toast.makeText(ChangePasswordActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void signOut() {
        Config.userAccount.clearDataOnLogout(getApplicationContext());
        KahunaHelper.tagAttributes(KahunaHelper.PAGE_USER_ACCOUNT, KahunaHelper.LOGIN_STATUS, KahunaHelper.NO);

        Intent intentSignIn = new Intent(ChangePasswordActivity.this, SignInActivity.class);
        intentSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intentSignIn.putExtra(Constants.RELOGIN_MSG, true);
        startActivity(intentSignIn);
        finish();
    }

    private void setFocusToErrorField(EditText field) {
        ErrorHandler.tryToFocus(field);
    }

    private boolean clientValidationError() {
        String errorMessage;
        boolean errorExist = false;
        ErrorHandler.needsToRequestFocus = true;

        if (ACUtils.isEmpty(currentPassword.getText().toString())) {
            setFocusToErrorField(currentPassword);
            errorMessage = getResources().getString(
                    R.string.contact_empty, CURRENT_PASSWORD);
            ErrorHandler.setError(errorMessage, currentPassword);
            errorExist = true;
        } else if (!ACUtils.isEmpty(Config.userAccount.getPassword()) && !Config.userAccount.getPassword().equalsIgnoreCase(currentPassword.getText().toString())) {
            setFocusToErrorField(currentPassword);
            errorMessage = getResources().getString(R.string.contact_current_passw_invalid);
            ErrorHandler.setError(errorMessage, currentPassword);
            errorExist = true;
        }

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

        if (userPassword.equals(currentPassword.getText().toString())) {
            setFocusToErrorField(password);
            errorMessage = getResources().getString(
                    R.string.contact_invalid_same_password);
            ErrorHandler.setError(errorMessage, password);
            errorExist = true;
        }

        return errorExist;
    }
}
