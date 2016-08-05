package com.mudah.my.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.configs.Constants;
import com.mudah.my.fragments.InsertAdPreviewFragment;
import com.mudah.my.models.InsertAdAdapterTagModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InsertAdCategoryParamsAdapter extends CategoryParamsAdapter implements InsertAdActivity.Form, InsertAdPreviewFragment.CategoryParams {
    public static final String STR_TITLE_LABEL = "subject_label";
    public static final String STR_DESC_LABEL = "body_label";
    public static final String STR_ADTYPE_LABEL = "adtype_label";
    public static final String STR_MIN_REQD_IMAGES = "min_images";
    private static final String STR_ERROR_CODE = "_error_code";
    private static final String PRIVATE_AD = "0";
    private static final String COMPANY_AD = "1";

    public String defaultAdType;
    private JSONArray adtypeLabels = new JSONArray();
    private JSONObject rawTypesObj;
    private String photoTipsImg;
    private JSONArray photoTipsMsg;

    public JSONArray getPhotoTipsMsg() {
        return photoTipsMsg;
    }

    public void setPhotoTipsMsg(JSONArray photoTipsMsg) {
        this.photoTipsMsg = photoTipsMsg;
    }

    public String getPhotoTipsImg() {
        return photoTipsImg;
    }

    public void setPhotoTipsImg(String photoTipsImg) {
        this.photoTipsImg = photoTipsImg;
    }

    public int setErrors(JSONObject errorObj) {
        return setErrors(errorObj, DEFAULT_PAGE_NO);
    }

    public int setErrors(JSONObject errorObj, String selectedPage) {
        if (errorObj == null) return 0;
        if (types == null) return 0;
        int errorCount = 0;
        JSONArray params = getParams();
        JSONObject errorsLabel = new JSONObject();
        try {
            if (errorObj.length() > 0) {
                for (int i = 0; i < params.length(); i++) {
                    JSONObject param = params.getJSONObject(i);
                    String paramName = param.getString(STR_NAME);
                    String page = Constants.EMPTY_STRING;
                    if (param.has("page") && !param.getString("page").equalsIgnoreCase("false")) {
                        page = param.getString("page");
                    } else if (ACUtils.isEmpty(page)) {
                        page = DEFAULT_PAGE_NO;
                    }
                    if (page.equalsIgnoreCase(selectedPage)) {
                        if (errorObj.has(paramName + "_error_label")) {
                            errorCount++;
                            errorsLabel.accumulate(paramName, errorObj.get(paramName + "_error_label"));
                            errorsLabel.accumulate(paramName + STR_ERROR_CODE, errorObj.get(paramName));
                        }
                        //chain select
                        if (param.optJSONArray("toggles") != null) {
                            for (int k = 0; k < param.getJSONArray("toggles").length(); k++) {
                                String childName = param.getJSONArray("toggles").getString(k);
                                if (errorObj.has(childName + "_error_label")) {
                                    errorCount++;
                                    errorsLabel.accumulate(childName, errorObj.get(childName + "_error_label"));
                                    errorsLabel.accumulate(childName + STR_ERROR_CODE, errorObj.get(childName));
                                }
                            }
                        }
                        if (param.has("integer_toggles")) {
                            String childName = param.getString("integer_toggles");
                            if (errorObj.has(childName + "_error_label")) {
                                errorCount++;
                                errorsLabel.accumulate(childName, errorObj.get(childName + "_error_label"));
                                errorsLabel.accumulate(childName + STR_ERROR_CODE, errorObj.get(childName));
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            ACUtils.debug(e);
        }
        if (errors != errorsLabel) {
            errors = errorsLabel;
            if (errorCount > 0 || errors.length() > 0)
                notifyDataSetChanged();
        }
        return errorCount;
    }

    public JSONObject getErrorsLabel() {
        return errors;
    }

    @Override
    protected void clearState() {
        Log.d();
        setErrors(null);
        super.clearState();
    }

    @Override
    public void setState(Map<String, String> inParams) {
        if (types != null) {
            setErrors(null);
        }
        super.setState(inParams);
    }

    @Override
    public String getTypeName() {
        return getAdTypeLabels().optString(STR_LABEL);
    }

    public JSONObject getPostAsLabel() {
        return getAdTypeLabels().optJSONObject(STR_ADTYPE_LABEL);
    }

    public String getPostedBy() {
        try {
            if (!ACUtils.isEmpty(savedState.get(STR_AD_TYPE))) {
                return savedState.get(STR_AD_TYPE);
            } else {
                Log.e("Posted By is null !!!!");
                String defaultPostedByOption = getDefaultPostedByOption();
                setPostedBy(defaultPostedByOption);
                return defaultPostedByOption;
            }
        } catch (NumberFormatException numberFormat) {
            ACUtils.debug(numberFormat);
        }
        return null;
    }

    public void setPostedBy(String postedBy) {
        savedState.put(STR_AD_TYPE, postedBy);
    }

    public void setTypes(JSONObject typesObj, Map<String, String> restoreState) {
        if (typesObj != null) {
            if (typesObj.equals(this.rawTypesObj) == false) {
                this.rawTypesObj = typesObj;
                clearState();
                types = getMapJSONObj(typesObj);
                if (restoreState != null) {
                    setState(restoreState);
                } else {
                    setSelectedType(defaultAdType, null);
                }
            }
        } else {
            if (this.types != null) {
                // not null -> null
                this.types = new HashMap<>();
                this.rawTypesObj = null;
                clearState();
                notifyDataSetChanged();
            }
        }

    }

    @Override
    public JSONArray getParams() {
        String postedBy = getPostedBy();
        if ((PostedBy.PROFESSIONAL.toString()).equalsIgnoreCase(postedBy))
            postedBy = PostedBy.PRIVATE.toString().toLowerCase();

        String adType = getType() + postedBy;
        JSONArray resultJson = new JSONArray();
        if (getTypes() != null && adType != null) {
            resultJson = getTypes().get(adType);
        }
        if (resultJson == null)
            resultJson = new JSONArray();
        return resultJson;
    }

    public void setSelectedType(String type, String postedBy) {
        clearState();
        savedState.put("type", type);
        if (ACUtils.isEmpty(postedBy))
            postedBy = getDefaultPostedByOption();
        savedState.put(STR_AD_TYPE, postedBy);
        notifyDataSetChanged();
    }

    public String getDefaultPostedByOption() {
        JSONObject postAsLabel = getPostAsLabel();
        String defaultPostedBy = null; //e.g. private, professional, or company
        //if some category (e.g. Job) wants to hide Post As option, postAsLabel will be null from API
        if (postAsLabel != null) {
            if (!ACUtils.isEmpty(postAsLabel.optString("private")))
                defaultPostedBy = PostedBy.PRIVATE.toString().toLowerCase();
            else if (!ACUtils.isEmpty(postAsLabel.optString("professional")))
                defaultPostedBy = PostedBy.PROFESSIONAL.toString().toLowerCase();
            else
                defaultPostedBy = PostedBy.COMPANY.toString().toLowerCase();
        }
        //need to return something, otherwise category params cannot be displayed
        if (ACUtils.isEmpty(defaultPostedBy))
            defaultPostedBy = PostedBy.PRIVATE.toString().toLowerCase();
        return defaultPostedBy;
    }

    public View newTypesView(Context context) throws JSONException {

        int total_type = adtypeLabels.length();
        String[][] values = new String[total_type][2];
        if (adtypeLabels != null) {
            int KEY = 0;
            int VALUE = 1;
            for (int i = 0; i < total_type; i++) {
                JSONObject item = adtypeLabels.optJSONObject(i);
                Iterator<String> nameFilters = item.keys();
                if (nameFilters.hasNext()) {
                    String keyFilter = nameFilters.next();
                    values[i][KEY] = keyFilter;
                    values[i][VALUE] = item.getJSONObject(keyFilter).optString(STR_LABEL);
                }
            }
        }
        return newAdTypeRadioView(values, context);
    }

    private View newAdTypeRadioView(String[][] values, Context context) throws JSONException {

        final View view = LayoutInflater.from(context).inflate(R.layout.ad_param_radio, null);

        TextView tvLabel = (TextView) view.findViewById(R.id.tv_label);
        TextView tvInfo = (TextView) view.findViewById(R.id.tv_info_msg);
        LinearLayout llItemInfo = (LinearLayout) view.findViewById(R.id.ll_item_info);

        tvLabel.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        llItemInfo.setVisibility(View.GONE);

        final RadioGroup myRadioGroup = (RadioGroup) view.findViewById(R.id.rg_item_type);
        myRadioGroup.setOrientation(RadioGroup.VERTICAL);
        String savedId = savedState.get("type");

        int arrSize = values.length;
        int KEY = 0;
        int VALUE = 1;
        for (int k = 0; k < arrSize; k++) {
            String key = values[k][KEY];
            String value = values[k][VALUE];

            RadioButton myRadioButton = new RadioButton(context);
            myRadioButton.setText(value);
            myRadioButton.setTag(key);
            myRadioButton.setId(k);
            //by default, select the first option if there is no saved data
            if (k == 0 && (savedId == null || savedId.length() == 0)) {
                myRadioButton.setChecked(true);
                savedState.put("type", key);
                setSelectedType(key, savedState.get(STR_AD_TYPE));
            } else {
                //restore the checked value
                if (myRadioButton.getTag().equals(savedId)) {
                    myRadioButton.setChecked(true);
                }
            }

            myRadioButton.setOnClickListener(new RadioGroup.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RadioGroup group = (RadioGroup) view.findViewById(R.id.rg_item_type);
                    int checked = group.getCheckedRadioButtonId();
                    RadioButton checkedRadioButton = (RadioButton) group.findViewById(checked);
                    String selectedType = checkedRadioButton.getTag().toString();
                    savedState.put("type", selectedType);
                    setSelectedType(selectedType, savedState.get(STR_AD_TYPE));

                    if (onCallerActionListener != null) {
                        onCallerActionListener.onCallerAction(getState(), getStateLabel());
                    }
                }
            });

            //Hide all seller type radio button if there are only one selection
            if (arrSize == 1) {
                myRadioButton.setVisibility(View.GONE);
            } else {
                myRadioButton.setVisibility(View.VISIBLE);
            }

            LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    1f);
            myRadioGroup.addView(myRadioButton, k, layoutParams);

        }

        InsertAdAdapterTagModel insertAdAdapterTagModel = new InsertAdAdapterTagModel("type", "1", "type", Constants.EMPTY_STRING);
        view.setTag(insertAdAdapterTagModel);

        return view;
    }

    public Map<String, JSONArray> getMapJSONObj(JSONObject filters) {
        Map<String, JSONArray> result = new HashMap<String, JSONArray>();

        setPhotoTipsImg(filters.optString("photo_tips_img"));
        setPhotoTipsMsg(filters.optJSONArray("photo_tips_msg"));

        JSONArray adParamsArray = filters.optJSONArray("ad_params");
        adtypeLabels = new JSONArray();

        if (adParamsArray != null) {
            int adParamArraySize = adParamsArray.length();
            for (int k = 0; k < adParamArraySize; k++) {
                JSONObject adParam = (JSONObject) adParamsArray.opt(k);
                Iterator<String> keys = adParam.keys();
                String key = Constants.EMPTY_STRING;
                if (keys != null && keys.hasNext()) {
                    key = keys.next();
                }

                if (k == 0) {
                    defaultAdType = key;
                }
                //company or private type
                for (PostedBy postedBy : PostedBy.values()) {
                    JSONArray tmpObj;
                    JSONArray arrayResult = new JSONArray();
                    String strPostedBy = postedBy.toString().toLowerCase();
                    try {
                        tmpObj = adParam.getJSONObject(key).optJSONArray(strPostedBy);
                        if (tmpObj != null) {
                            for (int i = 0; i < tmpObj.length(); i++) {
                                JSONObject item = tmpObj.getJSONObject(i);
                                Iterator<String> namefilters = item.keys();
                                while (namefilters.hasNext()) {
                                    String keyFilter = namefilters.next();// pricelist, regdate
                                    try {
                                        JSONObject filterObj = item.getJSONObject(keyFilter);
                                        //If there is no name object, manually create one
                                        if (!filterObj.has(STR_NAME))
                                            filterObj.accumulate(STR_NAME, keyFilter);
                                        if (filterObj != null) {
                                            arrayResult.put(filterObj);
                                        }
                                    } catch (JSONException e) {
                                        ACUtils.debug(e);
                                    }
                                }
                            }//for
                            //e.g. key: sprivate or scompany or kprivate etc.
                            result.put(key + strPostedBy, arrayResult);
                        }
                    } catch (JSONException e) {
                        ACUtils.debug(e);
                    }
                }//for

                //set params for ad type, s,k,..
                JSONObject adTypeParamObj = new JSONObject();
                String label = Constants.EMPTY_STRING;
                try {

                    if (adParam.optJSONObject(key) != null) {
                        JSONObject keyJsonObj = adParam.optJSONObject(key);
                        label = keyJsonObj.optString(STR_LABEL);
                        adTypeParamObj.accumulate(STR_LABEL, label);
                        adTypeParamObj.accumulate(STR_ADTYPE_LABEL, keyJsonObj.optJSONObject(STR_ADTYPE_LABEL));
                        adTypeParamObj.accumulate(STR_TITLE_LABEL, keyJsonObj.optString(STR_TITLE_LABEL));
                        adTypeParamObj.accumulate(STR_DESC_LABEL, keyJsonObj.optString(STR_DESC_LABEL));
                        adTypeParamObj.accumulate(STR_MIN_REQD_IMAGES, keyJsonObj.optString(STR_MIN_REQD_IMAGES));

                        JSONObject adTypeLabel = new JSONObject();
                        adtypeLabels.put(k, adTypeLabel.accumulate(key, adTypeParamObj));// {"s": { label: "For sale", subject_label: "Title"} }
                    }

                } catch (JSONException e) {
                    ACUtils.debug(e);
                }

            }//for
        }
        return result;
    }

    public JSONObject getAdTypeLabels() {
        if (adtypeLabels != null && getType() != null) {
            for (int i = 0; i < adtypeLabels.length(); i++) {
                JSONObject item = adtypeLabels.optJSONObject(i);
                if (item != null && item.has(getType()))
                    return item.optJSONObject(getType());
            }
        }
        //if not found, return empty
        return new JSONObject();
    }

    public String getCompanyAd(String adType) {
        if (PostedBy.PRIVATE.toString().toLowerCase().equals(adType))
            return PRIVATE_AD;
        else if (!ACUtils.isEmpty(adType))
            return COMPANY_AD;
        else
            return null;
    }

    public enum PostedBy {
        PRIVATE, COMPANY, PROFESSIONAL;
    }
}
