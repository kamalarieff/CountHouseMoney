package com.mudah.my.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib701.fragments.RetainedDialogFragment;
import com.lib701.utils.ACUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.helpers.PDPNHelper;
import com.mudah.my.utils.EventTrackingUtils;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static com.mudah.my.helpers.PDPNHelper.getPDPNEnable;
import static com.mudah.my.helpers.PDPNHelper.getPDPNQueues;
import static com.mudah.my.helpers.PDPNHelper.getPDPNoptinValue;

public class InsertAdPasswordFragment extends RetainedDialogFragment {
    private final String ERROR_PASSWORD_MISMATCH = "ERROR_PASSWORD_MISMATCH";
    private final String ERROR_PASSWORD_MISSING = "ERROR_PASSWORD_MISSING";
    private final String ERROR_PASSWORD_TOO_SHORT = "ERROR_PASSWORD_TOO_SHORT";
    private final String ERROR_PASSWORD_TOO_LONG = "ERROR_PASSWORD_TOO_LONG";
    CheckBox optInInternal;
    CheckBox optInInternalExternal;
    LinearLayout llPdpn;
    LinearLayout llPdpnCheckboxes;
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
    private String password;
    private String passwordConfirm;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private Button btnSend;
    private String email;
    private View view;
    private ViewGroup vgPassword;
    private ViewGroup vgConfirmPassword;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TranslucentFadeDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.insert_ad_password);

        view = dialog.findViewById(R.id.ll_password_container);
        etPassword = (EditText) dialog.findViewById(R.id.et_password);
        etPasswordConfirm = (EditText) dialog.findViewById(R.id.et_password_confirm);
        optInInternal = (CheckBox) dialog.findViewById(R.id.cb_pdpa_internal_checkbox);
        optInInternalExternal = (CheckBox) dialog.findViewById(R.id.cb_pdpa_internal_external_checkbox);
        llPdpn = (LinearLayout) dialog.findViewById(R.id.ll_pdpn);
        llPdpnCheckboxes = (LinearLayout) dialog.findViewById(R.id.ll_pdpn_checkboxes);

        optInInternal.setOnCheckedChangeListener(checkbox1Listener);
        optInInternalExternal.setOnCheckedChangeListener(checkbox2Listener);

        vgPassword = (ViewGroup) dialog.findViewById(R.id.ll_password);
        vgConfirmPassword = (ViewGroup) dialog.findViewById(R.id.ll_confirm_password);

        btnSend = (Button) dialog.findViewById(R.id.b_send);

        etPassword.setText(password);
        etPasswordConfirm.setText(passwordConfirm);
        etPassword.requestFocus();

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordConfirm = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        try {

            if (getPDPNEnable(getActivity())) {
                PDPNHelper.hidePDPNmessages(getActivity(), email, llPdpnCheckboxes);
                llPdpn.setVisibility(View.VISIBLE);
                PDPNHelper.setPDPNmessage(view, getActivity());
            } else {
                llPdpn.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            ACUtils.debug(e);
        }

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                // FIXME: hack to catch button click, do stuff, and prevent dialog from dismissing until passwords are ok.
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Map<String, String> p = new HashMap<String, String>();

                        if (isClientValidationSuccess()) {
                            try {
                                if (optInInternal.isChecked() || optInInternalExternal.isChecked()) {

                                    String opt_in_value = getPDPNoptinValue(getActivity());
                                    p.put("opt_in", opt_in_value);
                                    p.put("opt_out", "0");

                                    //top is checked
                                    if (optInInternal.isChecked() && !optInInternalExternal.isChecked()) {
                                        Map<String, String> pdpnQueues = getPDPNQueues(getActivity());
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

                                if (getActivity() instanceof InsertAdActivity) {
                                    ((InsertAdActivity) getActivity()).submitPreview(password, p);
                                    dismiss();
                                }
                            } catch (JSONException e) {
                                ACUtils.debug(e);
                            }
                        }
                    }
                });
                if (Build.VERSION.SDK_INT < 16) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        return dialog;
    }

    /**
     * Does the basic validation check for mandatory fields
     *
     * @return {@code true} if the validation passes for all the mandatory fields,
     * {@code false} otherwise.
     */
    private boolean isClientValidationSuccess() {
        boolean flag = true;
        if (password == null) {
            sendErrorTracking(password);
            displayError(
                    vgPassword,
                    R.id.rl_password_wrapper,
                    R.id.ll_password_info,
                    R.id.ll_password_error,
                    R.id.tv_password_info_msg,
                    R.id.tv_password_error_msg,
                    getString(R.string.insert_ad_password_confirm_error));
            etPassword.requestFocus();
            flag = false;
        } else if (password.length() < 5 || password.length() > 40) {
            sendErrorTracking(password);
            displayError(
                    vgPassword,
                    R.id.rl_password_wrapper,
                    R.id.ll_password_info,
                    R.id.ll_password_error,
                    R.id.tv_password_info_msg,
                    R.id.tv_password_error_msg,
                    getString(R.string.insert_ad_password_error));
            etPassword.requestFocus();
            flag = false;
        } else if (password.equals(passwordConfirm) == false) {
            sendErrorTracking("");
            displayError(
                    vgPassword,
                    R.id.rl_password_wrapper,
                    R.id.ll_password_info,
                    R.id.ll_password_error,
                    R.id.tv_password_info_msg,
                    R.id.tv_password_error_msg,
                    null);

            displayError(
                    vgConfirmPassword,
                    R.id.rl_confirm_password_wrapper,
                    null,
                    R.id.ll_confirm_password_error,
                    null,
                    R.id.tv_confirm_password_error_msg,
                    getString(R.string.insert_ad_password_confirm_error));
            etPasswordConfirm.requestFocus();
            flag = false;
        }
        if (flag) {
            displayError(
                    vgPassword,
                    R.id.rl_password_wrapper,
                    R.id.ll_password_info,
                    R.id.ll_password_error,
                    R.id.tv_password_info_msg,
                    R.id.tv_password_error_msg,
                    null);

            displayError(
                    vgConfirmPassword,
                    R.id.rl_confirm_password_wrapper,
                    null,
                    R.id.ll_confirm_password_error,
                    null,
                    R.id.tv_confirm_password_error_msg,
                    null);
        }
        return flag;
    }

    private void sendErrorTracking(String password) {
        String error;
        if (ACUtils.isEmpty(password))
            error = ERROR_PASSWORD_MISSING;
        else if (password.length() < 5)
            error = ERROR_PASSWORD_TOO_SHORT;
        else
            error = ERROR_PASSWORD_TOO_LONG;
        EventTrackingUtils.sendLevel2CustomVariableByName(XitiUtils.LEVEL2_INSERT_AD, InsertAdActivity.INSERT_AD_PAGE[2] + "_Error", "passwd", error);
    }

    public void setEmail(String mail) {
        email = mail;
    }

    public void displayError(ViewGroup baseViewGroup, Integer rlParamWrapperRes, Integer llItemInfoRes, Integer llItemErrorRes, Integer tvInfoMsgRes, Integer tvErrorMsgRes, String errorMsg) {

        RelativeLayout rlParamWrapper = (RelativeLayout) baseViewGroup.findViewById(rlParamWrapperRes);

        if (!ACUtils.isEmpty(errorMsg)) {
            if (llItemInfoRes != null)
                rlParamWrapper.findViewById(llItemInfoRes).setVisibility(View.GONE);
            if (llItemErrorRes != null)
                rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.VISIBLE);
            ((TextView) rlParamWrapper.findViewById(llItemErrorRes).findViewById(tvErrorMsgRes)).setText(errorMsg);
        } else {
            if (llItemInfoRes != null) {
                TextView tvInfoMsg = ((TextView) rlParamWrapper.findViewById(llItemInfoRes).findViewById(tvInfoMsgRes));
                //Check if there are existing static info message
                if (!ACUtils.isEmpty(tvInfoMsg.getText().toString())) {
                    if (llItemErrorRes != null)
                        rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.GONE);
                    if (llItemErrorRes != null)
                        rlParamWrapper.findViewById(llItemInfoRes).setVisibility(View.VISIBLE);
                } else {
                    if (llItemErrorRes != null)
                        rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.GONE);
                    rlParamWrapper.findViewById(llItemInfoRes).setVisibility(View.GONE);
                }
            } else {
                rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.GONE);
            }

        }
    }


}
