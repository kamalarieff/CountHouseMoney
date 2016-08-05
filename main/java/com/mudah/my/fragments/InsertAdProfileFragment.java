package com.mudah.my.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.adapters.CategoryParamsAdapter;
import com.mudah.my.adapters.InsertAdCategoryParamsAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class InsertAdProfileFragment extends Fragment implements InsertAdActivity.Form, CategoryParamsAdapter.OnCallerActionListener {

    private static final int MIN_NAME_LENGTH = 1;
    private final String NAME_ERROR_LABEL = "ERROR_NAME_MISSING";
    private final String ERROR_NAME_TOO_SHORT = "ERROR_NAME_TOO_SHORT";
    private final String EMAIL_ERROR_LABEL = "ERROR_EMAIL_MISSING";
    private final String ERROR_EMAIL_INVALID = "ERROR_EMAIL_INVALID";
    private final String PHONE_ERROR_LABEL = "ERROR_PHONE_MISSING";
    private final String ERROR_STORE_USER_EMAIL = "ERROR_STORE_USER_EMAIL";
    private final String ERROR_PHONE_AREACODE = "ERROR_PHONE_AREACODE";
    private final String EMAIL_ERROR_XITI_KEY = "1";
    private final String PHONE_ERROR_XITI_KEY = "3";
    private final String NAME = "name";
    private final String EMAIL = "email";
    private final String PHONE = "phone";
    private final String PHONE_HIDDEN = "phone_hidden";
    private final String WHATS_APP = "whats_app";
    private final View.OnTouchListener scrollOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (getActivity() != null) {
                MudahUtil.hideSoftKeyboard(getActivity());
            }
            return false;
        }
    };
    public RadioGroup rgPostAs;
    protected String userId;
    protected String firstName;
    protected String lastName;
    private String strPostAs = null;
    private Button bPrev;
    private Button bNext;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etName;
    private JSONObject errors;
    private CheckBox cbPhoneHide;
    private CheckBox cbWhatsApp;
    private Map<String, String> oneTimeUseState;
    private JSONObject data;
    private View view;
    private LinearLayout llProfileParams1;
    private LinearLayout llAdtype;
    private RadioButton rbPrivate;
    private RadioButton rbCompany;
    private RadioButton rbProfessional;
    private InsertAdCategoryParamsAdapter categoryProfileParamsAdapter;
    private JSONObject paramsObj;
    private int errorCountProfilePage = 0;
    private ArrayList<String> paramsKeyList = new ArrayList<>();
    RadioGroup.OnCheckedChangeListener radioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup buttonView, int checkedId) {
            strPostAs = null;
            boolean isChecked = false;
            switch (checkedId) {
                case R.id.rb_private:
                    if (rbPrivate.isChecked()) {
                        isChecked = true;
                        strPostAs = InsertAdCategoryParamsAdapter.PostedBy.PRIVATE.toString();
                    }
                    break;
                case R.id.rb_company:
                    if (rbCompany.isChecked()) {
                        isChecked = true;
                        strPostAs = InsertAdCategoryParamsAdapter.PostedBy.COMPANY.toString();
                    }
                    break;
                case R.id.rb_professional:
                    if (rbProfessional.isChecked()) {
                        isChecked = true;
                        strPostAs = InsertAdCategoryParamsAdapter.PostedBy.PROFESSIONAL.toString();
                    }
                    break;
            }
            if (isChecked) {

                if (getActivity() instanceof InsertAdActivity && strPostAs != null) {
                    ((InsertAdActivity) getActivity()).updatePostedByOption(strPostAs.toLowerCase());
                    boolean clearData = !strPostAs.toLowerCase().equalsIgnoreCase(categoryProfileParamsAdapter.getPostedBy());
                    //Clear old params from hash map before drawing new ones
                    if (clearData) {
                        clearAdParamsData();
                    }
                    categoryProfileParamsAdapter.setPostedBy(strPostAs.toLowerCase());

                    redrawCategoryParams();
                }

            }
        }
    };
    private Map<String, String> restoreParams;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public InsertAdProfileFragment() {
    }

    public static InsertAdProfileFragment newInstance() {
        InsertAdProfileFragment f = new InsertAdProfileFragment();
        return f;
    }

    private void clearAdParamsData() {
        for (String name : paramsKeyList) {
            categoryProfileParamsAdapter.removeParam(name);
        }
    }

    public String updatePostedByOption(String selectedOption) {
        String strPostedBy;
        rgPostAs.clearCheck();
        if (InsertAdCategoryParamsAdapter.PostedBy.COMPANY.toString().equalsIgnoreCase(selectedOption)) {
            rgPostAs.check(R.id.rb_company);
            strPostedBy = InsertAdCategoryParamsAdapter.PostedBy.COMPANY.toString();
        } else if (InsertAdCategoryParamsAdapter.PostedBy.PROFESSIONAL.toString().equalsIgnoreCase(selectedOption)) {
            rgPostAs.check(R.id.rb_professional);
            strPostedBy = InsertAdCategoryParamsAdapter.PostedBy.PROFESSIONAL.toString();
        } else {
            rgPostAs.check(R.id.rb_private);
            strPostedBy = InsertAdCategoryParamsAdapter.PostedBy.PRIVATE.toString();
        }

        return strPostedBy;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("isVisibleToUser: " + isVisibleToUser);
        if (isVisibleToUser) {
            if (restoreParams != null && restoreParams.size() > 0) {
                updateCategoryParams(restoreParams);
            } else {
                updateCategoryParams(new HashMap<String, String>());
            }
        }
    }

    public void updatePostAsLabelsAndSelectedOption() {
        Log.d();
        JSONObject labels = categoryProfileParamsAdapter.getPostAsLabel();
        if (labels == null) {
            llAdtype.setVisibility(View.GONE);
        } else {
            llAdtype.setVisibility(View.VISIBLE);
            String selectedPostAs = categoryProfileParamsAdapter.getPostedBy();
            updatePostAsLabels(labels);
            updatePostedByOption(selectedPostAs);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryProfileParamsAdapter = new InsertAdCategoryParamsAdapter();

        categoryProfileParamsAdapter.setOnCallerActionListener(this);
        categoryProfileParamsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d();
                //Initialise the AdsView flag to differentiate ads listing and other operations
                redrawCategoryParams();
            }
        });
        setRetainInstance(true);
    }

    private void preRedrawCategoryParams() {
        llProfileParams1.removeAllViews();
        categoryProfileParamsAdapter.setTextColor(Color.BLACK);
    }

    private void updateCategoryParams(Map<String, String> categoryParamsState) {
        if (getActivity() instanceof InsertAdActivity)
            paramsObj = ((InsertAdActivity) getActivity()).getParamsObj();
        if (paramsObj != null) {
            Map<String, String> oneTimeUseCategoryProfileParamsState = new HashMap<>();
            if (categoryParamsState != null && categoryParamsState.size() > 0) {
                oneTimeUseCategoryProfileParamsState.putAll(categoryParamsState);
            } else if (categoryProfileParamsAdapter.getState().size() > 0) {
                oneTimeUseCategoryProfileParamsState.putAll(categoryProfileParamsAdapter.getState());
            }
            Log.d("oneTimeUseCategoryProfileParamsState: " + oneTimeUseCategoryProfileParamsState.toString());
            if (resetPostByIfCategoryChange(oneTimeUseCategoryProfileParamsState)) {
                oneTimeUseCategoryProfileParamsState.clear();
            }
            oneTimeUseCategoryProfileParamsState.put(CategoryParamsAdapter.STR_TYPE, ((InsertAdActivity) getActivity()).getSelectedAdType());
            categoryProfileParamsAdapter.setTypes(paramsObj, oneTimeUseCategoryProfileParamsState);
            updatePostAsLabelsAndSelectedOption();
            if (oneTimeUseCategoryProfileParamsState.size() > 0 && restoreParams != null) {
                restoreParams = null;//reset
            }
        }
    }

    //In case users change to a category that does not have the same post as option as the previous one
    private boolean resetPostByIfCategoryChange(Map<String, String> oneTimeUseCategoryProfileParamsState) {
        boolean reset = false;
        String selectedPostAs = oneTimeUseCategoryProfileParamsState.get(CategoryParamsAdapter.STR_AD_TYPE);
        String postAsFromActivity = selectedPostAs;
        if (getActivity() instanceof InsertAdActivity) {
            postAsFromActivity = ((InsertAdActivity) getActivity()).getPostedByOption();
        }
        Log.d("postby old/new: " + selectedPostAs + "/ " + postAsFromActivity);
        if (!ACUtils.isEmpty(selectedPostAs) && !selectedPostAs.equalsIgnoreCase(postAsFromActivity)) {
            reset = true;
        }
        return reset;
    }

    private void redrawCategoryParams() {
        Log.d(Constants.EMPTY_STRING);
        if (categoryProfileParamsAdapter != null && categoryProfileParamsAdapter.getAdTypeLabels().length() > 0) {
            //remove all views
            preRedrawCategoryParams();
            if (getActivity() instanceof InsertAdActivity)
                ((InsertAdActivity) getActivity()).redrawCategoryParams(categoryProfileParamsAdapter, llProfileParams1, InsertAdActivity.PAGE_2, errors);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.insert_ad_profile, null);
        initializeTag();

        ScrollView svForm = (ScrollView) view.findViewById(R.id.sv_profile_form);
        svForm.setOnTouchListener(scrollOnTouchListener);

        etName = (EditText) view.findViewById(R.id.et_name);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etPhone = (EditText) view.findViewById(R.id.et_phone);

        rgPostAs = (RadioGroup) view.findViewById(R.id.rg_post_as);
        rgPostAs.setOnCheckedChangeListener(radioListener);

        rbPrivate = (RadioButton) view.findViewById(R.id.rb_private);
        rbCompany = (RadioButton) view.findViewById(R.id.rb_company);
        rbProfessional = (RadioButton) view.findViewById(R.id.rb_professional);

        cbWhatsApp = (CheckBox) view.findViewById(R.id.cb_whats_app);
        cbPhoneHide = (CheckBox) view.findViewById(R.id.cb_phone_hide);

        bPrev = (Button) view.findViewById(R.id.b_prev);
        bPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearExistingErrors();
                closeSoftKeyboard();
                if (getActivity() instanceof InsertAdActivity) {
                    ((InsertAdActivity) getActivity()).prev();
                }
            }
        });

        bNext = (Button) view.findViewById(R.id.b_next);
        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearExistingErrors();
                closeSoftKeyboard();
                if (isClientValidationSuccess()) {
                    next();
                }
            }
        });

        cbWhatsApp.setOnCheckedChangeListener(new checkBoxListener());
        cbPhoneHide.setOnCheckedChangeListener(new checkBoxListener());

        llProfileParams1 = (LinearLayout) view.findViewById(R.id.ll_profile_params_1);
        llAdtype = (LinearLayout) view.findViewById(R.id.ll_adtype);

        return view;
    }

    /**
     * Does the basic validation check for mandatory fields
     *
     * @return {@code true} if the validation passes for all the mandatory fields,
     * {@code false} otherwise.
     */
    private boolean isClientValidationSuccess() {
        boolean flag = true;
        String errorMessage;
        String errorPageName = Constants.EMPTY_STRING;
        ViewGroup baseViewGroup = null;
        Map<String, String> mapXitiErrorFormParams = new HashMap<>();
        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            baseViewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(i).findViewById(R.id.ll_profile_holder);
        }

        //Empty check for Name
        if (ACUtils.isEmpty(etName.getText().toString().trim())) {
            InsertAdActivity.tryToFocus(etName);
            //tag it as a page
            if (XitiUtils.getInsertAdFormErrorMap().containsKey(NAME)) {
                errorPageName += InsertAdActivity.UNDERSCORE + NAME_ERROR_LABEL;
            }
            errorMessage = getString(R.string.insert_ad_validation_name_error);
            flag = false;
        }
        //Valid name check
        else if (etName.getText().toString().trim().length() < MIN_NAME_LENGTH) {
            InsertAdActivity.tryToFocus(etName);
            String errorKey = XitiUtils.getInsertAdFormErrorMap().get(NAME);
            if (errorKey != null)
                mapXitiErrorFormParams.put(errorKey, ERROR_NAME_TOO_SHORT);
            errorMessage = getString(R.string.insert_ad_validation_minimum_length_error);
            flag = false;
        }//No error. Reset Error message UI
        else {
            errorMessage = null;
        }
        //Display error for name
        displayError(
                baseViewGroup,
                R.id.rl_param_wrapper_name,
                R.id.ll_item_info_name,
                R.id.ll_item_error_name,
                R.id.tv_info_msg_name,
                R.id.tv_error_msg_name,
                errorMessage);

        //Empty check for email
        if (ACUtils.isEmpty(etEmail.getText().toString().trim())) {
            InsertAdActivity.tryToFocus(etEmail);
            if (XitiUtils.getInsertAdFormErrorMap().containsKey(EMAIL)) {
                errorPageName += InsertAdActivity.UNDERSCORE + EMAIL_ERROR_LABEL;
            }
            errorMessage = getString(R.string.insert_ad_validation_email_error);
            flag = false;
        }
        //Valid email check
        else if (!etEmail.getText().toString().trim().matches(Constants.EMAIL_VALIDATOR)) {
            InsertAdActivity.tryToFocus(etEmail);
            if (XitiUtils.getInsertAdFormErrorMap().containsKey(EMAIL)) {
                errorPageName += InsertAdActivity.UNDERSCORE + ERROR_EMAIL_INVALID;
            }
            errorMessage = getString(R.string.insert_ad_validation_email_invalid_error);
            flag = false;
        } //No error. Reset Error message UI
        else {
            errorMessage = null;
        }
        //Display error for email
        displayError(
                baseViewGroup,
                R.id.rl_param_wrapper_email,
                R.id.ll_item_info_email,
                R.id.ll_item_error_email,
                R.id.tv_info_msg_email,
                R.id.tv_error_msg_email,
                errorMessage);

        //Empty check for phone
        if (ACUtils.isEmpty(etPhone.getText().toString())) {
            InsertAdActivity.tryToFocus(etPhone);
            String errorKey = XitiUtils.getInsertAdFormErrorMap().get(PHONE);
            if (errorKey != null)
                mapXitiErrorFormParams.put(errorKey, PHONE_ERROR_LABEL);
            errorMessage = getString(R.string.insert_ad_validation_phone_error);
            flag = false;
        }
        //No error. Reset Error message UI
        else {
            errorMessage = null;
        }
        //Display error for phone
        displayError(
                baseViewGroup,
                R.id.rl_param_wrapper_phone,
                R.id.ll_item_info_phone,
                R.id.ll_item_error_phone,
                R.id.tv_info_msg_phone,
                R.id.tv_error_msg_phone,
                errorMessage);

        if (!ACUtils.isEmpty(errorPageName)) {
            errorPageName = InsertAdActivity.INSERT_AD_PAGE[InsertAdActivity.AD_PROFILE_FRAGMENT] + errorPageName;
            XitiUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getActivity(), errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID, null);
            TealiumHelper.tagTealiumPage(getActivity(), TealiumHelper.APPLICATION_AI, errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID);
        }
        if (mapXitiErrorFormParams.size() > 0) {
            EventTrackingUtils.sendLevel2CustomVariable(XitiUtils.LEVEL2_INSERT_AD, InsertAdActivity.INSERT_AD_PAGE[InsertAdActivity.AD_PROFILE_FRAGMENT] + "_Error", mapXitiErrorFormParams);//send page and error
        }
        return flag;
    }

    public void displayError(ViewGroup baseViewGroup, int rlParamWrapperRes, int llItemInfoRes, int llItemErrorRes, int tvInfoMsgRes, int tvErrorMsgRes, String errorMsg) {

        RelativeLayout rlParamWrapper = (RelativeLayout) baseViewGroup.findViewById(rlParamWrapperRes);

        if (!ACUtils.isEmpty(errorMsg)) {
            rlParamWrapper.findViewById(llItemInfoRes).setVisibility(View.GONE);
            rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.VISIBLE);
            ((TextView) rlParamWrapper.findViewById(llItemErrorRes)
                    .findViewById(tvErrorMsgRes))
                    .setMovementMethod(LinkMovementMethod.getInstance()); // To handle a link in message to be linkable
            ((TextView) rlParamWrapper.findViewById(llItemErrorRes)
                    .findViewById(tvErrorMsgRes))
                    .setText(ACUtils.getHtmlFromString(errorMsg));
        } else {
            TextView tvInfoMsg = ((TextView) rlParamWrapper.findViewById(llItemInfoRes).findViewById(tvInfoMsgRes));
            //Check if there are existing static info message
            if (!ACUtils.isEmpty(tvInfoMsg.getText().toString())) {
                rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.GONE);
                rlParamWrapper.findViewById(llItemInfoRes).setVisibility(View.VISIBLE);
            } else {
                rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.GONE);
                rlParamWrapper.findViewById(llItemInfoRes).setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (errors != null) {
            setErrors(errors);
        }
        if (oneTimeUseState != null) {
            oneTimeUseState = null;
            setState(oneTimeUseState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveLocalProfile();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode = " + requestCode + ", resultCode: " + resultCode);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
    }

    @Override
    public Map<String, String> getState() {
        Map<String, String> params = new HashMap<>();
        HashMap<String, String> allParamsState = (HashMap) categoryProfileParamsAdapter.getState();
        params.put(NAME, etName.getText().toString().trim());
        params.put(EMAIL, etEmail.getText().toString().toLowerCase().trim());
        params.put(PHONE, etPhone.getText().toString());
        params.put(PHONE_HIDDEN, (cbPhoneHide.isChecked()) ? "1" : "0");
        params.put(WHATS_APP, (cbWhatsApp.isChecked()) ? "1" : "0");
        //when user click saved on the first page and the category param in the second page is not ready yet
        // we will loose the saved data. Need to prevent this.
        if (allParamsState.size() == 0 && restoreParams != null && restoreParams.size() > 0) {
            if (!paramsKeyList.contains(CategoryParamsAdapter.STR_AD_TYPE)) {
                paramsKeyList.add(CategoryParamsAdapter.STR_AD_TYPE);
            }
            //choose only params in profile page
            for (int i = 0; i < paramsKeyList.size(); i++) {
                params.put(paramsKeyList.get(i), restoreParams.get(paramsKeyList.get(i)));
            }

            String companyAd = categoryProfileParamsAdapter.getCompanyAd(allParamsState.get(restoreParams.get(CategoryParamsAdapter.STR_AD_TYPE)));
            if (!ACUtils.isEmpty(companyAd))
                params.put("company_ad", companyAd);
        } else {
            if (allParamsState.size() > 0) {
                for (int i = 0; i < paramsKeyList.size(); i++) {
                    params.put(paramsKeyList.get(i), allParamsState.get(paramsKeyList.get(i)));
                }
            }
            String companyAd = categoryProfileParamsAdapter.getCompanyAd(allParamsState.get(CategoryParamsAdapter.STR_AD_TYPE));
            if (!ACUtils.isEmpty(companyAd))
                params.put("company_ad", companyAd);
            Log.d("params " + params.toString());

        }
        return params;
    }

    @Override
    public void setState(Map<String, String> params) {
        if (getView() != null) {
            if (params == null) {
                params = new HashMap<>();
            }
            restoreParams = params;
            loadLocalProfile(params);
            cbPhoneHide.setChecked("1".equals(params.get("phone_hidden")));
            cbWhatsApp.setChecked("1".equals(params.get("whats_app")));
        } else {
            oneTimeUseState = params;
        }
    }

    public void setParamsKeyList(ArrayList<String> params) {
        paramsKeyList = params;
    }

    @Override
    public int setErrors(JSONObject errorObj) {
        this.errors = errorObj;
        String errorPageName = Constants.EMPTY_STRING;
        if (errors == null) return 0;
        Map<String, String> mapXitiErrorFormParams = new HashMap<String, String>();
        int errorCount = 0;
        String errorMessage;
        ViewGroup baseViewGroup = null;
        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            baseViewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(i).findViewById(R.id.ll_profile_holder);
        }

        if (errorObj != null && errorObj.length() > 0) {

            if (!ACUtils.isEmpty(errorObj.optString("name_error_label"))) {
                errorMessage = errorObj.optString("name_error_label");
                String errorKey = XitiUtils.getInsertAdFormErrorMap().get(NAME);
                if (errorKey != null && errorObj.has(NAME)) {
                    mapXitiErrorFormParams.put(errorKey, errorObj.optString(NAME));
                }
                InsertAdActivity.tryToFocus(etName);
                errorCount++;
            }
            //No error. Reset Error message UI
            else {
                errorMessage = null;
            }
            //Display error for phone
            displayError(
                    baseViewGroup,
                    R.id.rl_param_wrapper_name,
                    R.id.ll_item_info_name,
                    R.id.ll_item_error_name,
                    R.id.tv_info_msg_name,
                    R.id.tv_error_msg_name,
                    errorMessage);

            if (!ACUtils.isEmpty(errorObj.optString("email_error_label"))) {
                errorMessage = errorObj.optString("email_error_label");
                String errorKey = XitiUtils.getInsertAdFormErrorMap().get(EMAIL);
                if (errorKey != null && errorObj.has(EMAIL)) {
                    mapXitiErrorFormParams.put(errorKey, errorObj.optString(EMAIL));
                }
                InsertAdActivity.tryToFocus(etEmail);
                errorCount++;
            }
            //No error. Reset Error message UI
            else {
                errorMessage = null;
            }
            //Display error for phone
            displayError(
                    baseViewGroup,
                    R.id.rl_param_wrapper_email,
                    R.id.ll_item_info_email,
                    R.id.ll_item_error_email,
                    R.id.tv_info_msg_email,
                    R.id.tv_error_msg_email,
                    errorMessage);

            if (errorObj.optString(EMAIL).equalsIgnoreCase(ERROR_STORE_USER_EMAIL)) {
                String errorKey = XitiUtils.getInsertAdFormErrorMap().get("email");
                if (errorKey != null) {
                    mapXitiErrorFormParams.put(errorKey, "ERROR_STORE_USER_EMAIL");
                }
                Activity activity = getActivity();
                if (activity instanceof InsertAdActivity) {
                    ((InsertAdActivity) activity).showCustomDialog(errorObj.optString("email_error_label"), R.layout.dialog_proniaga_redirect, getActivity());
                }
            }

            if (!ACUtils.isEmpty(errorObj.optString("phone_error_label"))) {
                errorMessage = errorObj.optString("phone_error_label");
                String errorKey = XitiUtils.getInsertAdFormErrorMap().get(PHONE);
                if (errorKey != null && errorObj.has(PHONE)) {
                    mapXitiErrorFormParams.put(errorKey, errorObj.optString(PHONE));
                }
                InsertAdActivity.tryToFocus(etPhone);
                errorCount++;
            }
            //No error. Reset Error message UI
            else {
                errorMessage = null;
            }
            //Display error for phone
            displayError(
                    baseViewGroup,
                    R.id.rl_param_wrapper_phone,
                    R.id.ll_item_info_phone,
                    R.id.ll_item_error_phone,
                    R.id.tv_info_msg_phone,
                    R.id.tv_error_msg_phone,
                    errorMessage);
        }

        if (mapXitiErrorFormParams.size() > 0) {
            //if there is error for Subject or Body, tag it as a page
            if (mapXitiErrorFormParams.containsValue(ERROR_STORE_USER_EMAIL) || mapXitiErrorFormParams.containsValue(ERROR_PHONE_AREACODE)) {
                sendErrorTagAsPage(mapXitiErrorFormParams);
                mapXitiErrorFormParams.remove(EMAIL_ERROR_XITI_KEY);
                mapXitiErrorFormParams.remove(PHONE_ERROR_XITI_KEY);
            }
        }

        int errorParamsCount = categoryProfileParamsAdapter.setErrors(errorObj, InsertAdActivity.PAGE_2);
        if (errorParamsCount > 0) {
            JSONObject errorCode = categoryProfileParamsAdapter.getErrorsLabel();
            Map<String, String> mapFormParams = XitiUtils.getErrorFormParamsMap(errorCode);
            if (mapFormParams.size() > 0) {
                mapXitiErrorFormParams.putAll(mapFormParams);
            }
            errorCount += errorParamsCount;

        }
        if (mapXitiErrorFormParams.size() > 0) {
            EventTrackingUtils.sendLevel2CustomVariable(XitiUtils.LEVEL2_INSERT_AD, InsertAdActivity.INSERT_AD_PAGE[InsertAdActivity.AD_PROFILE_FRAGMENT] + "_Error", mapXitiErrorFormParams);//send page and error
        }

        setErrorCount(errorCount);
        return errorCount;
    }

    private void sendErrorTagAsPage(Map<String, String> mapFormParams) {
        String errorPageName = InsertAdActivity.INSERT_AD_PAGE[InsertAdActivity.AD_PROFILE_FRAGMENT];
        if (mapFormParams.containsValue(ERROR_STORE_USER_EMAIL)) {
            errorPageName += InsertAdActivity.UNDERSCORE + ERROR_STORE_USER_EMAIL;
        }
        if (mapFormParams.containsValue(ERROR_PHONE_AREACODE)) {
            errorPageName += InsertAdActivity.UNDERSCORE + ERROR_PHONE_AREACODE;
        }
        XitiUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getActivity(), errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID, null);
        TealiumHelper.tagTealiumPage(getActivity(), TealiumHelper.APPLICATION_AI, errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID);
    }

    /**
     * This method clears the existing errors if its been set before.
     */
    private void clearExistingErrors() {
        InsertAdActivity.setError(null, etName);
        InsertAdActivity.setError(null, etEmail);
        InsertAdActivity.setError(null, etPhone);
    }

    private void closeSoftKeyboard() {
        //Trying to close the soft keyboard if it opens
        //We can safely ignore this if there is any error happen
        try {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        } catch (Exception e) {
            ACUtils.debug(e);
        }

    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
        if (data != null && !ACUtils.isEmpty(data.optString(EMAIL))) {
            try {
                userId = data.getString("user_id");
                firstName = data.getString("first_name");
                lastName = data.getString("last_name");
                updateForm(
                        data.getString(EMAIL),
                        data.getString(NAME),
                        data.getString(PHONE)
                );
            } catch (JSONException e) {
                ACUtils.debug(e, "InsertAdProfile_setData", data.toString());
            }
        } else {
            loadLocalProfile();
        }
    }

    public void loadLocalProfile() {
        loadLocalProfile(new HashMap<String, String>());
    }

    public void loadLocalProfile(Map<String, String> overwrite) {
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(getActivity());
        String loadedEmail;
        if (!ACUtils.isEmpty(overwrite.get(EMAIL))) {
            loadedEmail = overwrite.get(EMAIL);
        } else {
            loadedEmail = pref.getString(PreferencesUtils.USER_EMAIL, null);
        }

        String loadedPhone;
        if (!ACUtils.isEmpty(overwrite.get(PHONE))) {
            loadedPhone = overwrite.get(PHONE);
        } else {
            loadedPhone = pref.getString(PreferencesUtils.USER_PHONE, null);
        }

        String loadedName;
        if (!ACUtils.isEmpty(overwrite.get(NAME))) {
            loadedName = overwrite.get(NAME);
        } else {
            loadedName = pref.getString(PreferencesUtils.USER_NAME, null);
        }

        if (Config.userAccount.isLogin()) {
            if (ACUtils.isEmpty(loadedEmail)) {
                loadedEmail = Config.userAccount.getEmail();
            }

            if (ACUtils.isEmpty(loadedPhone)) {
                loadedPhone = Config.userAccount.getPhone();
            }

            if (ACUtils.isEmpty(loadedName)) {
                loadedName = Config.userAccount.getFullName();
            }
        }

        updateForm(loadedEmail, loadedName, loadedPhone);
    }

    private void saveLocalProfile() {
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(getActivity());
        pref.edit()
                .putString(PreferencesUtils.USER_EMAIL, etEmail.getText().toString().trim())
                .putString(PreferencesUtils.USER_NAME, etName.getText().toString().trim())
                .putString(PreferencesUtils.USER_PHONE, etPhone.getText().toString())
                .apply();
    }

    private void next() {
        if (getActivity() instanceof InsertAdActivity) {
            ((InsertAdActivity) getActivity()).next();
        }
    }

    private void updateForm(String email, String name, String phone) {
        etEmail.setText(email);
        etName.setText(name);
        etPhone.setText(phone);

        saveLocalProfile();
    }

    private void initializeTag() {
        String myTag = getTag();
        ((InsertAdActivity) getActivity()).setInsertAdProfileFragmentTag(myTag);
    }

    public void updatePostAsLabels(JSONObject labels) {

        if (labels != null && !ACUtils.isEmpty(labels.optString("private"))) {
            rbPrivate.setText(labels.optString("private"));
            rbPrivate.setVisibility(View.VISIBLE);
        } else {
            rbPrivate.setVisibility(View.GONE);
        }

        if (labels != null && !ACUtils.isEmpty(labels.optString("professional"))) {
            rbProfessional.setText(labels.optString("professional"));
            rbProfessional.setVisibility(View.VISIBLE);
        } else {
            rbProfessional.setVisibility(View.GONE);
        }

        if (labels != null && !ACUtils.isEmpty(labels.optString("company"))) {
            rbCompany.setText(labels.optString("company"));
            rbCompany.setVisibility(View.VISIBLE);
        } else {
            rbCompany.setVisibility(View.GONE);
        }

    }

    public void setErrorCount(int errCount) {
        this.errorCountProfilePage = errCount;
    }

    @Override
    public void onCallerAction(Map<String, String> state, Map<String, String> stateLabel) {

    }

    @Override
    public void onRequireLoginAction() {

    }

    class checkBoxListener implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView == cbWhatsApp) {
                    cbPhoneHide.setChecked(false);
                } else if (buttonView == cbPhoneHide) {
                    cbWhatsApp.setChecked(false);
                }

            }

        }

    }

}