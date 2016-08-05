package com.mudah.my.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lib701.datasets.ACReferences;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.RangeSeekBar;
import com.lib701.widgets.ErrorDisplayableArrayAdapter;
import com.lib701.widgets.ErrorDisplayableCustomArrayAdapter;
import com.lib701.widgets.MultiSpinner;
import com.mudah.my.R;
import com.mudah.my.activities.FilterMenuActivity;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.ChoiceModel;
import com.mudah.my.models.InsertAdAdapterTagModel;
import com.mudah.my.utils.MudahUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryParamsAdapter extends BaseAdapter {

    public static final String THOUSAND = "1000";
    public static final String CAR_CATEGORY_ID = "1020";
    public static final String STR_NAME = "name";
    public static final String STR_TYPE = "type";
    public static final String STR_AD_TYPE = "adtype";
    public static final String STR_LABEL = "label";
    public static final String STR_PLACEHOLDER = "placeholder";
    public static final String STR_HINT = "hint";
    public static final String STR_TEXT = "text";

    public static final String STR_CUSTOMBEHAVIOR = "custom_behavior";
    public static final String STR_CUSTOMBEHAVIOR_INPUT = "input";
    public static final String STR_CUSTOMBEHAVIOR_INPUT_INTEGER = "integer";
    public static final String STR_CUSTOMBEHAVIOR_INPUT_EMAIL = "email";
    public static final String STR_CUSTOMBEHAVIOR_INPUT_PHONE = "phone";
    public static final String STR_CUSTOMBEHAVIOR_INPUT_PASSWORD = "password";
    public static final String STR_CUSTOMBEHAVIOR_INPUT_URL = "url";

    public static final String STR_CUSTOMBEHAVIOR_MAXLENGTH = "max_length";
    public static final String DEFAULT_PAGE_NO = "1";
    static final String SPACE = " ";
    static final String EMPTY = Constants.EMPTY_STRING;
    private static final String PARAM_TYPE_STRING = "string";
    private static final String PARAM_TYPE_TEXT = "text";
    private static final String PARAM_TYPE_INTEGER_TEXT = "integer_text";
    private static final String PARAM_TYPE_TEXTAREA = "textarea";
    private static final String PARAM_TYPE_BOOLEAN = "boolean";
    private static final String PARAM_TYPE_CHECKBOX = "checkbox";
    private static final String PARAM_TYPE_CHECKLIST = "checklist";
    private static final String PARAM_TYPE_INTEGER = "integer";
    private static final String PARAM_TYPE_INT = "int";
    private static final String PARAM_TYPE_COLOR_INT = "color_integer";
    private static final String PARAM_TYPE_RADIO = "radio";
    private static final String PARAM_TYPE_RANGE = "range";
    private static final String PARAM_TYPE_CHAIN = "chainselect";
    private static final String PARAM_TYPE_INTEGER_CHAIN = "integer_chainselect";
    private static final String PARAM_TYPE_TEXTRANGE = "textrange";
    private static final String RANGE_MIN = "firstItem";
    private static final String RANGE_MAX = "lastItem";
    private static final String RANGE_DELIMETER = "-";
    private static final String REQUIRED_LOGIN = "login";
    private static final int MANUFACTURED_DATE = 0;
    private static final int MAKE = 1;
    private static final int MODEL = 2;
    private static final int ENGINE_CAPACITY = 3;
    private static final int TRANSMISSION = 4;
    public JSONObject errors;
    protected Map<String, JSONArray> types = new HashMap<String, JSONArray>();
    /**
     * param name -> param value
     */
    protected Map<String, String> savedState = new HashMap<>();
    protected Map<String, String> savedStateLabel = new HashMap<>();
    protected OnCallerActionListener onCallerActionListener;
    BigDecimal engineCapacityConverted, divider;
    private String callerName = Constants.EMPTY_STRING;
    private int textColor = Color.WHITE;
    private boolean clearToggleChildElements = false;
    private String paramName;
    /**
     * Auto generate subject for category 1020
     */
    private TextView tvSubject;
    private String[] strSubjectGenerated = new String[(Config.AUTO_GENERATED_SUBJECT_PARAMS_LIST).size()];
    private String STR_MAKE = Constants.EMPTY_STRING;
    /**
     * For textrange input
     */
    private String rangeMinValue = Constants.EMPTY_STRING;
    private String rangeMaxValue = Constants.EMPTY_STRING;

    /**
     * chainselect param -> parent param
     */
    private Map<String, Boolean> chainselectParents = new HashMap<String, Boolean>();

    public CategoryParamsAdapter(String name) {
        super();
        callerName = name;
    }

    public CategoryParamsAdapter() {
        super();
    }

    private void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public void setOnCallerActionListener(OnCallerActionListener listener) {
        onCallerActionListener = listener;
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    @Override
    public int getCount() {
        if (types != null && getType() != null) {
            return getParams().length();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (types != null) {
            return getParams();
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public List<View> getViewList(int position, ViewGroup parent) {
        return getViewList(position, parent, null);
    }

    public List<View> getViewList(int position, ViewGroup parent, String selectedPage) {
        List<View> viewList = new ArrayList<View>();
        Context context = parent.getContext();
        try {
            int paramIndex = position;
            JSONArray params = getParams();
            JSONObject param = params.getJSONObject(paramIndex);
            String page = Constants.EMPTY_STRING;
            String name = Constants.EMPTY_STRING;
            String label = Constants.EMPTY_STRING;
            String errorMsg = Constants.EMPTY_STRING;
            if (param.has("page") && !param.getString("page").equalsIgnoreCase("false")) {
                page = param.getString("page");
                if ("1.1".equals(page))
                    page = DEFAULT_PAGE_NO;
            } else if (ACUtils.isEmpty(page)) {
                page = DEFAULT_PAGE_NO;
            }
            if (ACUtils.isEmpty(selectedPage)) {
                selectedPage = DEFAULT_PAGE_NO;
            }
            // if a selected page is exists, only proceed if view matches with the pageTag
            if (selectedPage.equalsIgnoreCase(page)) {

                if (param.has("name") && param.has("label")) {
                    name = param.getString("name");
                    label = param.getString("label");

                    if (errors != null && errors.has(name)) {
                        errorMsg = errors.getString(name);
                    }
                }
                InsertAdAdapterTagModel insertAdAdapterTagModel = new InsertAdAdapterTagModel(name, page, label, errorMsg);

                String paramType = param.getString(STR_TYPE);
                if (param.optJSONArray("toggles") != null) {
                    paramType = PARAM_TYPE_CHAIN;
                } else if (!ACUtils.isEmpty(param.optString("integer_toggles"))) {
                    paramType = PARAM_TYPE_INTEGER_CHAIN;
                }
                //For API validation purpose, need to send an empty value when users does not enter.
                //Hence, initialise the saved state for each params (only if it is not from filter page)
                if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName) && !savedState.containsKey(param.getString(STR_NAME))) {
                    saveStateWithLabel(param.getString(STR_NAME), Constants.EMPTY_STRING, Constants.EMPTY_STRING);
                }
                View newView = null;
                if (PARAM_TYPE_STRING.equalsIgnoreCase(paramType)) {
                    newView = newTextView(param, context);
                } else if (PARAM_TYPE_TEXT.equalsIgnoreCase(paramType)) {
                    newView = newPlainTextView(param, context);
                } else if (PARAM_TYPE_TEXTAREA.equalsIgnoreCase(paramType)) {
                    newView = newTextAreaView(param, context);
                } else if (PARAM_TYPE_BOOLEAN.equalsIgnoreCase(paramType) || PARAM_TYPE_CHECKBOX.equalsIgnoreCase(paramType)) {
                    newView = newCheckboxView(param, context);
                } else if (PARAM_TYPE_CHECKLIST.equalsIgnoreCase(paramType)) {
                    newView = newMultiChoiceView(param, context);
                } else if (PARAM_TYPE_INTEGER.equalsIgnoreCase(paramType) || PARAM_TYPE_INT.equalsIgnoreCase(paramType)) {
                    newView = newSingleChoiceView(param, context);
                } else if (PARAM_TYPE_COLOR_INT.equalsIgnoreCase(paramType)) {
                    newView = newSingleChoiceCustomView(param, context);
                } else if (PARAM_TYPE_RADIO.equalsIgnoreCase(paramType)) {
                    newView = newRadioView(param, context);
                } else if (PARAM_TYPE_RANGE.equalsIgnoreCase(paramType)) {
                    newView = newRangeView(param, context);
                } else if (PARAM_TYPE_TEXTRANGE.equalsIgnoreCase(paramType)) {
                    newView = newTextRangeView(param, context);
                }

                if (newView != null) {
                    newView.setTag(insertAdAdapterTagModel);
                    viewList.add(newView);
                }

                //Integer Chain Select
                if (newView == null && PARAM_TYPE_INTEGER_CHAIN.equalsIgnoreCase(paramType)) {
                    String parentName = param.getString(STR_NAME);
                    String childName = param.optString("integer_toggles");
                    String choiceId = savedState.get(parentName);
                    View parentView;
                    if (ACUtils.isEmpty(choiceId)) {
                        clearToggleChildElements = true;
                        chainselectParents.put(parentName, clearToggleChildElements);
                    }
                    parentView = newChainselectView(param, context);
                    if (parentView != null) {
                        parentView.setTag(insertAdAdapterTagModel);
                        viewList.add(parentView);
                    }

                    //display child
                    try {

                        if (choiceId != null && choiceId.length() > 0) {
                            if (chainselectParents.get(parentName) != null && chainselectParents.get(parentName) == true) {
                                //reset child
                                removeStateAndLabel(childName);
                                //remove parent from the map, otherwise the child will be removed again when other chainselect get selected
                                chainselectParents.remove(parentName);
                            }
                            JSONObject childObj = param.getJSONObject(childName);
                            if (childObj != null) {
                                if (ACUtils.isEmpty(childObj.optString(STR_NAME)))
                                    childObj.accumulate(STR_NAME, childName);
                                String childLabel = param.getString("label");
                                String childErrorMsg = null;
                                View childView;
                                if (errors != null && errors.has(childName)) {
                                    childErrorMsg = errors.getString(childName);
                                }
                                InsertAdAdapterTagModel childInsertAdAdapterTagModel = new InsertAdAdapterTagModel(childName, page, childLabel, childErrorMsg);
                                //Initialising the saved state for chain select params for API validation purpose only if it is not from Listing page
                                if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName) && !savedState.containsKey(childObj.getString(STR_NAME))) {
                                    saveStateWithLabel(childObj.getString(STR_NAME), Constants.EMPTY_STRING, Constants.EMPTY_STRING);
                                }

                                childView = newSingleChoiceIntegerChildView(childObj, context, choiceId);

                                if (childView != null) {
                                    childView.setTag(childInsertAdAdapterTagModel);
                                    viewList.add(childView);
                                }
                            }

                        } else {
                            removeStateAndLabel(childName);
                        }
                    } catch (JSONException e) {
                        ACUtils.debug(e);
                    }

                }
                //Chain Select
                else if (newView == null && PARAM_TYPE_CHAIN.equalsIgnoreCase(paramType)) {
                    String type = param.getString(STR_TYPE);
                    String parentName = param.getString(STR_NAME);
                    String choiceId = savedState.get(parentName);
                    View parentView = null;
                    if (ACUtils.isEmpty(choiceId)) {
                        clearToggleChildElements = true;
                        chainselectParents.put(parentName, clearToggleChildElements);
                    }
                    if ((PARAM_TYPE_CHECKBOX.equalsIgnoreCase(type))) {
                        parentView = newToggleCheckboxView(param, context);
                        if (ACUtils.isEmpty(choiceId))
                            choiceId = "0";//default
                    } else {
                        parentView = newChainselectView(param, context);
                    }
                    if (parentView != null) {
                        parentView.setTag(insertAdAdapterTagModel);
                        viewList.add(parentView);
                    }
                    //display child
                    try {
                        if (choiceId != null && choiceId.length() > 0) {
                            for (int i = 0; i < param.getJSONArray("toggles").length(); i++) {
                                String childName = param.getJSONArray("toggles").getString(i);
                                if (chainselectParents.get(parentName) != null && chainselectParents.get(parentName) == true) {
                                    //reset child
                                    removeStateAndLabel(childName);
                                    //remove parent from the map, otherwise the child will be removed again when other chainselect get selected
                                    chainselectParents.remove(parentName);
                                }
                                JSONObject childObj = param.getJSONObject(childName).optJSONObject(choiceId);
                                if (childObj == null) {
                                    childObj = param.getJSONObject(childName);
                                }
                                if (childObj != null && childObj.has(STR_TYPE)) {
                                    if (ACUtils.isEmpty(childObj.optString(STR_NAME)))
                                        childObj.accumulate(STR_NAME, childName);
                                    String childLabel = param.getString("label");
                                    String childErrorMsg = null;

                                    if (errors != null && errors.has(childName)) {
                                        childErrorMsg = errors.getString(childName);
                                    }
                                    InsertAdAdapterTagModel childInsertAdAdapterTagModel = new InsertAdAdapterTagModel(childName, page, childLabel, childErrorMsg);
                                    //Initialising the saved state for chain select params for API validation purpose only if it is not from Listing page
                                    if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName) && !savedState.containsKey(childObj.getString(STR_NAME))) {
                                        saveStateWithLabel(childObj.getString(STR_NAME), Constants.EMPTY_STRING, Constants.EMPTY_STRING);
                                    }
                                    View childView = getChildView(childObj, context);
                                    if (childView != null) {
                                        childView.setTag(childInsertAdAdapterTagModel);
                                        viewList.add(childView);
                                    }
                                } else {
                                    removeStateAndLabel(childName);
                                }
                            }
                        } else {
                            for (int i = 0; i < param.getJSONArray("toggles").length(); i++) {
                                removeStateAndLabel(param.getJSONArray("toggles").getString(i));
                            }
                        }
                    } catch (JSONException e) {
                        ACUtils.debug(e);
                    }
                }
            }
        } catch (JSONException e) {
            ACUtils.debug(e);
        }
        return viewList;
    }

    private View getChildView(JSONObject childObj, Context context) {
        View childView = null;
        try {

            if ((PARAM_TYPE_STRING.equalsIgnoreCase(childObj.getString(STR_TYPE)))) {
                childView = newTextView(childObj, context);
            } else if ((PARAM_TYPE_TEXTAREA.equalsIgnoreCase(childObj.getString(STR_TYPE)))) {
                childView = newTextAreaView(childObj, context);
            } else if ((PARAM_TYPE_INTEGER_TEXT.equalsIgnoreCase(childObj.getString(STR_TYPE)))) {
                childView = newIntegerTextView(childObj, context);
            } else {
                childView = newSingleChoiceView(childObj, context);
            }
        } catch (JSONException e) {
            ACUtils.debug(e);
        }
        return childView;
    }

    public String getParamType(int position, ViewGroup parent) {
        int paramIndex = position;
        JSONArray params = getParams();
        JSONObject param;
        String paramType = null;
        try {
            param = params.getJSONObject(paramIndex);
            paramType = param.getString(STR_TYPE);
        } catch (JSONException e) {
            ACUtils.debug(e);
        }

        return paramType;
    }

    public Map<String, String> getState() {
        //Need to return a new instance.
        //If the 'savedState' in this class is modified later on, it won't affect the variable inside the caller
        return new HashMap<>(savedState);
    }

    public void setState(Map<String, String> inParams) {
        if (types != null) {
            clearState();
            if (inParams != null && !MudahUtil.isEmptyOrAllAdType(inParams)) {
                savedState.put(STR_TYPE, inParams.get(STR_TYPE));
                if (inParams.containsKey(STR_AD_TYPE))
                    savedState.put(STR_AD_TYPE, inParams.get(STR_AD_TYPE));
                Set<String> paramNames = new HashSet<String>();
                try {
                    JSONArray params = getParams();
                    for (int i = 0; i < params.length(); i++) {
                        JSONObject jsonObj = params.getJSONObject(i);
                        paramNames.add(jsonObj.getString(STR_NAME));
                        //Check if this is a chain select
                        if (jsonObj.optJSONArray("toggles") != null) {
                            JSONArray toggleArr = jsonObj.getJSONArray("toggles");
                            int toggleArrSize = toggleArr.length();
                            for (int k = 0; k < toggleArrSize; k++) {
                                paramNames.add(toggleArr.getString(k));
                            }
                        }

                        if (jsonObj.has("integer_toggles")) {
                            paramNames.add(jsonObj.getString("integer_toggles"));
                        }

                    }
                    for (String key : inParams.keySet()) {
                        String rootKey = key.replaceFirst("[0-9]+$", Constants.EMPTY_STRING);
                        if (paramNames.contains(rootKey)) {
                            String value = inParams.get(key);
                            if (ACUtils.isEmpty(value))
                                value = Constants.EMPTY_STRING;
                            Log.d("key: " + key + ", value: " + value);
                            savedState.put(key, value);
                        }
                    }
                } catch (JSONException e) {
                    ACUtils.debug(e);
                    throw new RuntimeException(e);
                }

            }
            notifyDataSetChanged();
        }
    }

    public Map<String, String> getStateLabel() {
        //Need to return a new instance.
        //If the 'savedStateLabel' in this class is modified later on, it won't affect the variable inside the caller
        return new HashMap<>(savedStateLabel);
    }

    public void removeParam(String param) {
        if (!ACUtils.isEmpty(param)) {
            removeStateAndLabel(param);
        }
    }

    public String getParamLabel(String paramName) {
        return getParam(paramName).optString(STR_LABEL);
    }

    public String getParamSuffix(String paramName) {
        return getParam(paramName).optString("suffix");
    }

    public JSONObject getParam(String paramName) {
        try {
            JSONArray params = getParams();
            for (int i = 0; i < params.length(); i++) {
                JSONObject param = params.getJSONObject(i);
                if (param.getString(STR_NAME).equals(paramName)) {
                    return param;
                }
            }
            return null;
        } catch (JSONException e) {
            ACUtils.debug(e);
            throw new RuntimeException(e);
        }
    }

    public void clearTypes() {
        setTypes(null);
    }

    public Map<String, JSONArray> getTypes() {
        return types;
    }

    public void setTypes(JSONObject typesObj) {
        if (typesObj != null) {
            types = getMapJSONObj(typesObj);
        }
        if (typesObj == null) {
            if (this.types != null) {
                // not null -> null
                this.types = new HashMap<String, JSONArray>();
                clearState();
                notifyDataSetChanged();
            }
        }

    }

    protected void clearState() {
        savedState.clear();
        savedStateLabel.clear();
        chainselectParents.clear();
        errors = null;
    }

    public String getType() {
        return savedState.get(STR_TYPE);
    }

    private Map<String, JSONArray> getMapJSONObj(JSONObject filters) {
        Map<String, JSONArray> result = new HashMap<String, JSONArray>();
        Iterator<String> nameAdType = filters.keys(); //e.g. ["s","k","u","h"] or ["pricelist","regdate"]
        while (nameAdType.hasNext()) {
            String key = nameAdType.next();//s
            JSONObject tmpObj;
            try {
                tmpObj = filters.getJSONObject(key);
                if (tmpObj != null) {
                    JSONArray arrayResult = new JSONArray();
                    Iterator<String> namefilters = tmpObj.keys();
                    while (namefilters.hasNext()) {
                        String keyFilter = namefilters.next();// pricelist, regdate
                        try {
                            JSONObject filterObj = tmpObj.getJSONObject(keyFilter);
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
                    result.put(key, arrayResult);
                }
            } catch (JSONException e) {
                ACUtils.debug(e, "CategoryParamsAdapter_data", filters.toString());
            }
        }

        Log.d(" result size: " + result.size());
        return result;
    }

    private Map<String, String> getStringMapJSONObj(JSONArray filters) {
        Map<String, String> result = new HashMap<String, String>();
        int arrSize = filters.length();
        for (int k = 0; k < arrSize; k++) {
            JSONObject optObj = filters.optJSONObject(k);

            if (optObj != null) {
                Iterator<String> nameAdType = optObj.keys();
                if (nameAdType.hasNext()) {
                    String key = nameAdType.next();
                    result.put(key, optObj.optString(key));
                    if (k == 0)
                        result.put(RANGE_MIN, key + Constants.EMPTY_STRING);
                    else if (arrSize == (k + 1))
                        result.put(RANGE_MAX, key + Constants.EMPTY_STRING);
                }
            }
        }
        return result;
    }

    public JSONArray getParams() {
        String adType = getType();
        JSONArray resultJson = new JSONArray();
        if (types != null && adType != null) {
            resultJson = types.get(adType);
        }
        if (resultJson == null)
            resultJson = new JSONArray();
        return resultJson;
    }

    private View newPlainTextView(final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        final String label = param.getString(STR_LABEL);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_plain_text, null);

        TextView tvLabel = (TextView) view.findViewById(R.id.tv_label);
        tvLabel.setText(ACUtils.getHtmlFromString(label));
        // If textView has links specified by putting <a> tags in the string
        // resource.  By default these links will appear but not
        // respond to user input.  To make them active, you need to
        // call setMovementMethod() on the TextView object.
        tvLabel.setMovementMethod(LinkMovementMethod.getInstance());
        if (onCallerActionListener != null && REQUIRED_LOGIN.equalsIgnoreCase(name)) {
            onCallerActionListener.onRequireLoginAction();
        }
        return view;
    }

    private View newRangeView(final JSONObject param, final Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        final String label = param.getString(STR_LABEL);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_range, null);
        view.setTag(name);

        final TextView tvLabel = (TextView) view.findViewById(R.id.range_label);
        final TextView tvLabelValue = (TextView) view.findViewById(R.id.range_label_value);

        // create RangeSeekBar as Integer range between min and max
        final Map<String, String> result = getStringMapJSONObj(param.getJSONArray("options"));
        String firstItem = result.get(RANGE_MIN);
        String lastItem = result.get(RANGE_MAX);
        RangeSeekBar<Integer> seekBar = new RangeSeekBar<>(Integer.parseInt(firstItem), Integer.parseInt(lastItem), context);
        seekBar.setId(Integer.parseInt(lastItem));
        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue, boolean callAPI) {
                tvLabel.setText(label);
                String minLabel = result.get(Constants.EMPTY_STRING + minValue);
                String maxLabel = result.get(Constants.EMPTY_STRING + maxValue);
                tvLabelValue.setText(minLabel + " to " + maxLabel);
                saveStateWithLabel(name, minValue + RANGE_DELIMETER + maxValue, minLabel + RANGE_DELIMETER + maxLabel);

                if (onCallerActionListener != null && callAPI) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
                // handle changed range values
            }
        });

        // add RangeSeekBar to pre-defined banner_image
        ViewGroup layout = (ViewGroup) view.findViewById(R.id.range_bar);
        layout.addView(seekBar);

        //restore value
        if (!ACUtils.isEmpty(savedState.get(name))) {
            String rangeValue = savedState.get(name);
            String[] arrRange = rangeValue.split(RANGE_DELIMETER);
            String minLabel = Constants.EMPTY_STRING;
            String maxLabel = Constants.EMPTY_STRING;
            String minValue = Constants.EMPTY_STRING;
            String maxValue = Constants.EMPTY_STRING;
            if (arrRange.length == 2) {
                minLabel = result.get(arrRange[0]);
                maxLabel = result.get(arrRange[1]);
                minValue = arrRange[0];
                maxValue = arrRange[1];
            } else {
                minValue = arrRange[0];
                if (ACUtils.isEmpty(arrRange[0])) {
                    minLabel = result.get(firstItem);
                    minValue = firstItem;
                } else {
                    minLabel = result.get(arrRange[0]);
                }

                if (arrRange.length == 1 || ACUtils.isEmpty(arrRange[1])) {
                    maxLabel = result.get(lastItem);
                    maxValue = lastItem;
                }
            }
            tvLabel.setText(label);
            tvLabelValue.setText(minLabel + " to " + maxLabel);
            savedStateLabel.put(name + Constants.LABEL_VALUE, minLabel + RANGE_DELIMETER + maxLabel);
            seekBar.setSelectedMinValue(Integer.parseInt(minValue));
            seekBar.setSelectedMaxValue(Integer.parseInt(maxValue));
        } else {
            tvLabel.setText(label);
            tvLabelValue.setText(result.get(firstItem) + " to " + result.get(lastItem));
        }
        return view;
    }

    public View newTextView(final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        final String label = param.getString(STR_LABEL);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_text, null);

        handleInfoMsg(view, param);

        TextView tvPrefix = (TextView) view.findViewById(R.id.tv_prefix);
        if (param.optString("prefix").length() > 0) {
            tvPrefix.setText(param.optString("prefix"));
        } else {
            tvPrefix.setVisibility(View.GONE);
        }
        TextView tvSuffix = (TextView) view.findViewById(R.id.tv_suffix);
        if (param.optString("suffix").length() > 0) {
            tvSuffix.setText(param.optString("suffix"));
        } else {
            tvSuffix.setVisibility(View.GONE);
        }

        final String placeholder;
        if (param.has(STR_PLACEHOLDER)) {
            placeholder = param.optString(STR_PLACEHOLDER);
        } else if (param.has(STR_HINT)) {
            placeholder = param.optString(STR_HINT);
        } else {
            placeholder = label;
        }

        EditText etField = (EditText) view.findViewById(R.id.et_field);
        etField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        etField.setHint(placeholder);
        etField.setText(savedState.get(name));
        etField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                savedState.put(name, s.toString());
                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
                if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName) && Config.AUTO_GENERATED_SUBJECT_PARAMS_LIST.contains(name)) {
                    setAutoGeneratedSubject(name, s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });


        setEditTextCustomBehavior(param, etField);
        //Provide quick access to subject adparam
        if (name.equalsIgnoreCase("subject")) {
            tvSubject = etField;
        }

        return view;
    }

    public View newIntegerTextView(final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        final String label = param.getString(STR_LABEL);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_text, null);
        handleInfoMsg(view, param);

        TextView tvPrefix = (TextView) view.findViewById(R.id.tv_prefix);
        if (param.optString("prefix").length() > 0) {
            tvPrefix.setText(param.optString("prefix"));
        } else {
            tvPrefix.setVisibility(View.GONE);
        }
        TextView tvSuffix = (TextView) view.findViewById(R.id.tv_suffix);
        if (param.optString("suffix").length() > 0) {
            tvSuffix.setText(param.optString("suffix"));
        } else {
            tvSuffix.setVisibility(View.GONE);
        }

        final String placeholder;
        if (param.has(STR_PLACEHOLDER)) {
            placeholder = param.optString(STR_PLACEHOLDER);
        } else if (param.has(STR_HINT)) {
            placeholder = param.optString(STR_HINT);
        } else {
            placeholder = label;
        }

        EditText etField = (EditText) view.findViewById(R.id.et_field);
        etField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        etField.setHint(placeholder);
        etField.setInputType(InputType.TYPE_CLASS_NUMBER);
        etField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                savedState.put(name, s.toString());
                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etField.setText(savedState.get(name));
        setEditTextCustomBehavior(param, etField);

        //Provide quick access to subject adparam
        if (name.equalsIgnoreCase("subject")) {
            tvSubject = etField;
        }

        return view;
    }

    private void setEditTextCustomBehavior(JSONObject param, EditText etField) throws JSONException {
        if (param.has(STR_CUSTOMBEHAVIOR)) {
            JSONObject customBehaviors = param.getJSONObject(STR_CUSTOMBEHAVIOR);

            if (customBehaviors.has(STR_CUSTOMBEHAVIOR_INPUT)) {
                String input = customBehaviors.getString(STR_CUSTOMBEHAVIOR_INPUT);
                if (input.equalsIgnoreCase(STR_CUSTOMBEHAVIOR_INPUT_INTEGER))
                    etField.setInputType(InputType.TYPE_CLASS_NUMBER);
                else if (input.equalsIgnoreCase(STR_CUSTOMBEHAVIOR_INPUT_PHONE))
                    etField.setInputType(InputType.TYPE_CLASS_PHONE);
                else if (input.equalsIgnoreCase(STR_CUSTOMBEHAVIOR_INPUT_EMAIL))
                    etField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                else if (input.equalsIgnoreCase(STR_CUSTOMBEHAVIOR_INPUT_URL))
                    etField.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                else if (input.equalsIgnoreCase(STR_CUSTOMBEHAVIOR_INPUT_PASSWORD))
                    etField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }

            if (customBehaviors.has(STR_CUSTOMBEHAVIOR_MAXLENGTH)) {
                int maxLength = Integer.parseInt(customBehaviors.getString(STR_CUSTOMBEHAVIOR_MAXLENGTH));
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                etField.setFilters(FilterArray);
            }

        }
    }

    private View newTextAreaView(final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        final String label = param.getString(STR_LABEL);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_text_area, null);
        view.setTag(name);

        handleInfoMsg(view, param);

        final String placeholder;
        if (param.has(STR_PLACEHOLDER)) {
            placeholder = param.optString(STR_PLACEHOLDER);
        } else if (param.has(STR_HINT)) {
            placeholder = param.optString(STR_HINT);
        } else {
            placeholder = label;
        }

        EditText etField = (EditText) view.findViewById(R.id.et_box);
        etField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        etField.setHint(placeholder);
        etField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                savedState.put(name, s.toString());

                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etField.setText(savedState.get(name));
        setEditTextCustomBehavior(param, etField);

        return view;
    }

    private View newRadioView(JSONObject param, Context context) throws JSONException {
        final String[][] values = ACUtils.getKeyValueArrayFromJSON(param.getJSONArray("options"));
        final String label = param.getString(STR_LABEL);
        final String name = param.getString(STR_NAME);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_radio, null);
        TextView tvLabel = (TextView) view.findViewById(R.id.tv_label);
        tvLabel.setText(label);
        tvLabel.setTextColor(textColor);

        handleInfoMsg(view, param);

        final RadioGroup myRadioGroup = (RadioGroup) view.findViewById(R.id.rg_item_type);
        String savedId = savedState.get(name);

        int arrSize = values.length;
        LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT,
                1f);
        for (int k = 0; k < arrSize; k++) {
            String key = values[k][0];
            String value = values[k][1];

            RadioButton myRadioButton = new RadioButton(context);

            myRadioButton.setText(value);
            myRadioButton.setId(Integer.parseInt(key));
            //by default, select the first option if there is no saved data
            if (k == 0 && (savedId == null || savedId.length() == 0)) {
                myRadioButton.setChecked(true);
                saveStateWithLabel(name, key, value);
            } else {
                //restore the checked value
                myRadioButton.setChecked(((k + 1) + Constants.EMPTY_STRING).equals(savedId));
                savedStateLabel.put(name + Constants.LABEL_VALUE, value);
            }

            myRadioGroup.addView(myRadioButton, k, layoutParams);
        }

        myRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                String label = Constants.EMPTY_STRING;
                for (int k = 0; k < values.length; k++) {
                    if (values[k][0].equalsIgnoreCase(checkedId + Constants.EMPTY_STRING)) {
                        label = values[k][1];
                    }
                }
                saveStateWithLabel(name, checkedId + Constants.EMPTY_STRING, label);

                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
            }
        });

        return view;
    }

    public View newCheckboxView(JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        String label = param.getString(STR_LABEL);
        if (ACUtils.isEmpty(label))
            label = param.optString(STR_TEXT);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_checkbox, null);
        handleInfoMsg(view, param);

        final CheckBox cbField = (CheckBox) view.findViewById(R.id.cb_field);
        cbField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        cbField.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savedState.put(name, isChecked ? "1" : "0");

                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
            }
        });
        cbField.setChecked("1".equals(savedState.get(name)));

        TextView tvCheckbox = (TextView) view.findViewById(R.id.tv_checkbox);
        tvCheckbox.setText(label);
        tvCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbField.toggle();
            }
        });

        return view;
    }

    public View newDropDownBoxView(final JSONObject param, Context context) throws JSONException {
        return null;
    }

    public View newMultiChoiceView(final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        final String label = param.getString(STR_LABEL);
        final View view;
        int multiSpinnerResource;

        if (!ACUtils.isEmpty(callerName) && callerName.equalsIgnoreCase(FilterMenuActivity.CALLER_NAME)) {
            view = LayoutInflater.from(context).inflate(R.layout.filter_param_multi_choice, null);
            TextView tvLabel = (TextView) view.findViewById(R.id.tv_spinner_label);
            tvLabel.setText(label);
            RelativeLayout rlParamWrapper = (RelativeLayout) view.findViewById(R.id.rl_param_wrapper);
            rlParamWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Spinner spinner = (Spinner) view.findViewById(R.id.ms_field);
                    spinner.performClick();
                }
            });
            multiSpinnerResource = R.layout.filter_multiline_spinner_item;
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.ad_param_multi_choice, null);
            handleInfoMsg(view, param);
            multiSpinnerResource = R.layout.multiline_spinner_item;
        }

        // restore selection
        final String[][] values = ACUtils.getKeyValueArrayFromJSON(param.getJSONArray("options"));
        String[] items = new String[values.length];
        boolean[] selected = new boolean[items.length];

        String selectedStr = savedState.get(name);
        List<String> selectedList = new ArrayList<String>();
        //Convert comma separated String to ArrayList
        if (selectedStr != null)
            selectedList = Arrays.asList(selectedStr.split("\\s*,\\s*"));
        int arrSize = values.length;
        for (int k = 0; k < arrSize; k++) {
            selected[k] = selectedList.contains(values[k][0]);
            items[k] = values[k][1];
        }

        MultiSpinner msField = (MultiSpinner) view.findViewById(R.id.ms_field);
        msField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        msField.setPrompt(label);
        msField.setItems(
                items,
                selected,
                label,
                multiSpinnerResource,
                android.R.id.text1,
                new MultiSpinner.MultiSpinnerListener() {

                    @Override
                    public void onItemsSelected(boolean[] selected) {
                        List<String> selectedList = new ArrayList<>();
                        List<String> selectedLabelList = new ArrayList<>();
                        // save selection
                        for (int i = 0; i < selected.length; i++) {
                            if (selected[i]) {
                                selectedList.add(values[i][0] + Constants.EMPTY_STRING);//add id
                                selectedLabelList.add(values[i][1]);
                            }
                        }
                        removeStateAndLabel(name);
                        saveState(selectedList, selectedLabelList);

                        if (onCallerActionListener != null) {
                            onCallerActionListener.onCallerAction(getState(), getStateLabel());
                        }
                        notifyDataSetChanged();

                    }

                    private void saveState(List<String> selectedList, List<String> selectedLabelList) {
                        if (selectedList != null && selectedList.size() > 0) {
                            String selectedStr = listToString(selectedList);
                            String selectedStrLabel = listToString(selectedLabelList);
                            saveStateWithLabel(name, selectedStr, selectedStrLabel);
                            setParamName(name);
                        }
                    }
                }
        );

        return view;
    }

    private String listToString(List<String> selectedList) {
        //convert a list to a string with comma separated
        //selectedList.toString() will return [1, 2]. Need to remove [ and ]
        String tmp = selectedList.toString();
        return tmp.substring(1, tmp.length() - 1).replace(", ", ",");
    }

    public View newChainSelectChoiceView(ErrorDisplayableArrayAdapter<ChoiceModel> adapter, final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        String label = param.getString(STR_LABEL);
        final View view;
        if (!ACUtils.isEmpty(callerName) && callerName.equalsIgnoreCase(FilterMenuActivity.CALLER_NAME)) {
            view = LayoutInflater.from(context).inflate(R.layout.filter_param_chain_single_choice, null);
            TextView tvLabel = (TextView) view.findViewById(R.id.tv_spinner_label);
            tvLabel.setText(label);
            RelativeLayout rlParamWrapper = (RelativeLayout) view.findViewById(R.id.rl_param_wrapper);
            rlParamWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Spinner spinner = (Spinner) view.findViewById(R.id.s_field);
                    spinner.performClick();
                }
            });
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.ad_param_single_choice, null);
            handleInfoMsg(view, param);
        }

        // init spinner
        Spinner sField = (Spinner) view.findViewById(R.id.s_field);
        sField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        sField.setPrompt(label);
        sField.setPadding(15, 0, 0, 0);//padding does not work in xml when using app-compat. Need to set it programmatically
        sField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                boolean updated = false;
                // check if saved value is different to prevent feedback loop
                ChoiceModel choice = (ChoiceModel) parent.getItemAtPosition(position);
                String choiceId = choice.getValue();
                if (choiceId != null) {
                    if (!choiceId.equals(savedState.get(name))) {
                        // this is a new selection
                        saveStateWithLabel(name, choiceId, choice.getName());
                        if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName) && Config.AUTO_GENERATED_SUBJECT_PARAMS_LIST.contains(name)) {
                            setAutoGeneratedSubject(name, choice.getName());
                        }
                        updated = true;
                    } else {
                        //For restore back data, only need to put the label
                        savedStateLabel.put(name + Constants.LABEL_VALUE, choice.getName());
                    }
                } else if (savedState.get(name) != null) {
                    // remove old selection
                    if (!ACUtils.isEmpty(savedState.get(name)))
                        updated = true;
                    //Set the saved state default to empty for API validation purpose only if it is not from Listing page
                    saveStateWithLabel(name, Constants.EMPTY_STRING, Constants.EMPTY_STRING);
                    removeStateAndLabel("agent_number");
                    if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName) && Config.AUTO_GENERATED_SUBJECT_PARAMS_LIST.contains(name)) {
                        setAutoGeneratedSubject(name, Constants.EMPTY_STRING);
                    }
                }


                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
                //update child, if any
                if (updated) {
                    clearToggleChildElements = true;
                    chainselectParents.put(name, clearToggleChildElements);
                    notifyDataSetChanged();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // cannot happen
            }
        });
        sField.setAdapter(adapter);
        if (adapter.getCount() == 1 && adapter.getItem(0).getValue() == null) {
            sField.setEnabled(false);
        } else {
            sField.setEnabled(true);
        }
        // restore selection
        if (savedState.get(name) != null && !ACUtils.isEmpty((savedState.get(name)))) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String id = adapter.getItem(i).getValue();
                if (id != null && id.equals(savedState.get(name))) {
                    sField.setSelection(i, true);
                    break;
                }
            }
        } else {
            saveStateWithLabel(name, Constants.EMPTY_STRING, Constants.EMPTY_STRING);
        }

        return view;
    }


    public View newSingleChoiceView(ErrorDisplayableArrayAdapter<ChoiceModel> adapter, final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        String label = param.getString(STR_LABEL);
        final View view;

        if (!ACUtils.isEmpty(callerName) && callerName.equalsIgnoreCase(FilterMenuActivity.CALLER_NAME)) {
            view = LayoutInflater.from(context).inflate(R.layout.filter_param_single_choice, null);
            TextView tvLabel = (TextView) view.findViewById(R.id.tv_spinner_label);
            tvLabel.setText(label);
            RelativeLayout rlParamWrapper = (RelativeLayout) view.findViewById(R.id.rl_param_wrapper);
            rlParamWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Spinner spinner = (Spinner) view.findViewById(R.id.s_field);
                    spinner.performClick();
                }
            });
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.ad_param_single_choice, null);
            handleInfoMsg(view, param);
        }

        // init spinner
        Spinner sField = prepareSpinner(view, label, name);
        sField.setAdapter(adapter);
        if (adapter.getCount() == 1 && adapter.getItem(0).getValue() == null) {
            sField.setEnabled(false);
        } else {
            sField.setEnabled(true);
        }
        // restore selection
        if (savedState.get(name) != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String id = adapter.getItem(i).getValue();
                if (id != null && id.equals(savedState.get(name))) {
                    sField.setSelection(i, true);
                    break;
                }
            }
        }

        return view;
    }

    public View newSingleCustomChoiceView(ErrorDisplayableCustomArrayAdapter<ChoiceModel> adapter, final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        String label = param.getString(STR_LABEL);

        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_single_choice, null);
        handleInfoMsg(view, param);

        // init spinner
        Spinner sField = prepareSpinner(view, label, name);

        sField.setAdapter(adapter);
        if (adapter.getCount() == 1 && adapter.getItem(0).getValue() == null) {
            sField.setEnabled(false);
        } else {
            sField.setEnabled(true);
        }
        // restore selection
        if (savedState.get(name) != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String id = adapter.getItem(i).getValue();
                if (id != null && id.equals(savedState.get(name))) {
                    sField.setSelection(i, true);
                    break;
                }
            }
        }

        return view;
    }

    private Spinner prepareSpinner(View view, String title, final String name) {

        Spinner sField = (Spinner) view.findViewById(R.id.s_field);
        sField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        sField.setPrompt(title);
        sField.setPadding(15, 0, 0, 0);//padding does not work in xml when using app-compat. Need to set it programmatically
        sField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // check if saved value is different to prevent feedback loop
                ChoiceModel choice = (ChoiceModel) parent.getItemAtPosition(position);
                String choiceId = choice.getValue();
                if (choiceId != null) {
                    if (!choiceId.equals(savedState.get(name))) {
                        // this is a new selection
                        saveStateWithLabel(name, choiceId, choice.getName());
                        if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName)) {
                            setAutoGeneratedSubject(name, choice.getName());
                        }
                    } else {
                        //this is a restore case, only need to put the label
                        savedStateLabel.put(name + Constants.LABEL_VALUE, choice.getName());
                    }
                } else {
                    if (savedState.get(name) != null) {
                        // remove old selection
                        //Set the saved state default to empty for API validation purpose only if it is not from Listing page
                        saveStateWithLabel(name, Constants.EMPTY_STRING, Constants.EMPTY_STRING);
                        if (!(FilterMenuActivity.CALLER_NAME).equalsIgnoreCase(callerName)) {
                            setAutoGeneratedSubject(name, Constants.EMPTY_STRING);
                        }
                    }
                }

                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {// cannot happen
            }
        });
        return sField;
    }

    private View newSingleChoiceIntegerChildView(final JSONObject param, Context context, String parentChoiceId) throws JSONException {
        String label = param.getString(STR_LABEL);
        String[][] values = ACUtils.getKeyValueArrayFromJSON(param.getJSONArray("options"));
        // setup adapter for spinner
        ErrorDisplayableArrayAdapter<ChoiceModel> adapter = new ErrorDisplayableArrayAdapter<>(context, R.layout.simple_spinner_item, android.R.id.text1);
        adapter.setDropDownViewResource(R.layout.select_dialog_custom_singlechoice);
        adapter.add(new ChoiceModel(label, null));

        int arrSize = values.length;
        try {
            int manufacturedDate = Integer.parseInt(parentChoiceId);
            for (int k = 0; k < arrSize; k++) {
                String key = values[k][0];
                if (key.equals("1") || Integer.parseInt(key) >= manufacturedDate) {
                    adapter.add(new ChoiceModel(values[k][1], key)); //put value,key
                }
            }
        } catch (NumberFormatException e) {
            ACUtils.debug(e);
        }

        return newSingleChoiceView(adapter, param, context);
    }

    private View newSingleChoiceView(final JSONObject param, Context context) throws JSONException {
        String label = param.getString(STR_LABEL);
        String[][] values = ACUtils.getKeyValueArrayFromJSON(param.getJSONArray("options"));
        ErrorDisplayableArrayAdapter<ChoiceModel> adapter;
        // setup adapter for spinner
        if (!ACUtils.isEmpty(callerName) && callerName.equalsIgnoreCase(FilterMenuActivity.CALLER_NAME)) {
            adapter = new ErrorDisplayableArrayAdapter<>(context, R.layout.filter_spinner_item, android.R.id.text1);
            adapter.add(new ChoiceModel("All " + label, null));

        } else {
            adapter = new ErrorDisplayableArrayAdapter<>(context, R.layout.simple_spinner_item, android.R.id.text1);
            adapter.add(new ChoiceModel(label, null));
        }
        adapter.setDropDownViewResource(R.layout.select_dialog_custom_singlechoice);

        int arrSize = values.length;
        for (int k = 0; k < arrSize; k++) {
            adapter.add(new ChoiceModel(values[k][1], values[k][0]));//put value,key
        }

        return newSingleChoiceView(adapter, param, context);
    }

    private View newSingleChoiceCustomView(final JSONObject param, Context context) throws JSONException {
        String label = param.getString(STR_LABEL);
        String[][] values = ACUtils.getKeyValueArrayFromJSON(param.getJSONArray("options"));
        // setup adapter for spinner
        ErrorDisplayableCustomArrayAdapter<ChoiceModel> adapter = new ErrorDisplayableCustomArrayAdapter<ChoiceModel>(context, R.layout.simple_spinner_item, android.R.id.text1);
        adapter.setDropDownViewResource(com.android701.R.layout.custom_spinner_item);
        adapter.add(new ChoiceModel(label, null));

        int arrSize = values.length;
        for (int k = 0; k < arrSize; k++) {
            adapter.add(new ChoiceModel(values[k][1], values[k][0]));//put value,key
        }

        return newSingleCustomChoiceView(adapter, param, context);
    }

    private View newChainselectView(JSONObject param, Context context) throws JSONException {
        String label = param.getString(STR_LABEL);
        String[][] values = ACUtils.getKeyValueArrayFromJSON(param.getJSONArray("options"));
        // setup adapter for spinner
        ErrorDisplayableArrayAdapter<ChoiceModel> adapter;
        // setup adapter for spinner
        if (!ACUtils.isEmpty(callerName) && callerName.equalsIgnoreCase(FilterMenuActivity.CALLER_NAME)) {
            adapter = new ErrorDisplayableArrayAdapter<>(context, R.layout.filter_chain_select_spinner_item, android.R.id.text1);
            adapter.add(new ChoiceModel("All " + label, null));
        } else {
            adapter = new ErrorDisplayableArrayAdapter<>(context, R.layout.simple_spinner_item, android.R.id.text1);
            adapter.add(new ChoiceModel(label, null));
        }
        adapter.setDropDownViewResource(R.layout.select_dialog_custom_singlechoice);
        int arrSize = values.length;
        for (int k = 0; k < arrSize; k++) {
            adapter.add(new ChoiceModel(values[k][1], values[k][0]));//put value,key
        }

        return newChainSelectChoiceView(adapter, param, context);

    }

    private View newToggleCheckboxView(JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        String label = param.getString(STR_LABEL);
        if (ACUtils.isEmpty(label))
            label = param.optString(STR_TEXT);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_checkbox, null);
        view.setTag(name);
        handleInfoMsg(view, param);

        final CheckBox cbField = (CheckBox) view.findViewById(R.id.cb_field);
        cbField.setSaveEnabled(false); // Need to stop Android from auto-saving for manual saving of dynamically-generated fields to work
        cbField.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String checked = isChecked ? "1" : "0";
                boolean updated = false;

                if (!checked.equals(savedState.get(name))) {
                    // save new selection
                    savedState.put(name, checked);
                    updated = true;
                }
                //update child, if any
                if (updated) {
                    clearToggleChildElements = true;
                    chainselectParents.put(name, clearToggleChildElements);
                    notifyDataSetChanged();
                }
            }
        });
        cbField.setChecked("1".equals(savedState.get(name)));

        TextView tvCheckbox = (TextView) view.findViewById(R.id.tv_checkbox);
        tvCheckbox.setText(label);
        tvCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbField.toggle();
            }
        });
        return view;
    }

    private View newTextRangeView(final JSONObject param, Context context) throws JSONException {
        final String name = param.getString(STR_NAME);
        final String label = param.getString(STR_LABEL);
        View view = LayoutInflater.from(context).inflate(R.layout.ad_param_textrange, null);

        String labelMin = context.getResources().getString(R.string.textrange_minimum);
        TextView tvLabelMin = (TextView) view.findViewById(R.id.tv_label_min);
        tvLabelMin.setText(ACUtils.getHtmlFromString(label + SPACE + labelMin));
        tvLabelMin.setMovementMethod(LinkMovementMethod.getInstance());

        String labelMax = context.getResources().getString(R.string.textrange_maximum);
        TextView tvLabelMax = (TextView) view.findViewById(R.id.tv_label_max);
        tvLabelMax.setText(ACUtils.getHtmlFromString(labelMax));
        tvLabelMax.setMovementMethod(LinkMovementMethod.getInstance());

        final String textrangeValue = savedState.get(name);
        if (textrangeValue != null && !RANGE_DELIMETER.equalsIgnoreCase(textrangeValue)) {
            final String[] rangeValue = textrangeValue.split(RANGE_DELIMETER);
            if (rangeValue.length > 0) {
                rangeMinValue = rangeValue[0];
                if (rangeValue.length > 1) {
                    rangeMaxValue = rangeValue[1];
                }
            }
        }

        EditText etMin = (EditText) view.findViewById(R.id.et_min);
        etMin.setSaveEnabled(false);
        etMin.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rangeMinValue = s.toString();
                savedState.put(name, rangeMinValue + RANGE_DELIMETER + rangeMaxValue);
                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etMin.setText(rangeMinValue);
        setEditTextCustomBehavior(param, etMin);

        EditText etMax = (EditText) view.findViewById(R.id.et_max);
        etMax.setSaveEnabled(false);
        etMax.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMax.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rangeMaxValue = s.toString();
                savedState.put(name, rangeMinValue + RANGE_DELIMETER + rangeMaxValue);
                if (onCallerActionListener != null) {
                    onCallerActionListener.onCallerAction(getState(), getStateLabel());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etMax.setText(rangeMaxValue);
        setEditTextCustomBehavior(param, etMax);

        return view;
    }

    public void handleInfoMsg(View view, final JSONObject param) {

        try {
            TextView tvInfo = (TextView) view.findViewById(R.id.tv_info_msg);
            LinearLayout llItemInfo = (LinearLayout) view.findViewById(R.id.ll_item_info);
            if (param.has(STR_TEXT)) {
                try {
                    tvInfo.setText(ACUtils.getHtmlFromString(param.getString(STR_TEXT)).toString());
                    llItemInfo.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    ACUtils.debug(e, "param", param.toString());
                }
            } else {
                llItemInfo.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            ACUtils.debug(e, "param", param.toString());
            //Some view do not have R.id.tv_info_msg and R.id.ll_item_info
            //Do not do anything
        }
    }

    private void setAutoGeneratedSubject(String name, String label) {
        String categoryId = ACReferences.getACReferences().getInsertAdCategoryId();
        String adType = savedState.get(STR_TYPE);
        String strSubject = Constants.EMPTY_STRING;
        if (adType != null && categoryId != null && categoryId.equalsIgnoreCase(CAR_CATEGORY_ID) && (adType.equalsIgnoreCase(Constants.TYPE_SALE) || adType.equalsIgnoreCase(Constants.TYPE_RENT))) {
            ACUtils.logCrashlytics("AutoGeneratedSubject: " + name + ", label: " + label);

            if (name.equalsIgnoreCase("make")) {
                if (ACUtils.isEmpty(label)) {
                    //if label is empty, clear the subject
                    strSubjectGenerated = new String[(Config.AUTO_GENERATED_SUBJECT_PARAMS_LIST).size()];
                } else {
                    strSubjectGenerated[MAKE] = label;
                }
            } else if (name.equalsIgnoreCase("model")) {
                if (ACUtils.isEmpty(label)) {
                    strSubjectGenerated[MODEL] = EMPTY;
                } else {
                    strSubjectGenerated[MODEL] = label;
                }
            } else if (name.equalsIgnoreCase("transmission")) {
                if (ACUtils.isEmpty(label)) {
                    strSubjectGenerated[TRANSMISSION] = EMPTY;
                } else {
                    strSubjectGenerated[TRANSMISSION] = "(" + label.toUpperCase().charAt(0) + ")";
                }
            } else if (name.equalsIgnoreCase("manufactured_date")) {
                if (ACUtils.isEmpty(label)) {
                    strSubjectGenerated[MANUFACTURED_DATE] = EMPTY;
                } else {
                    strSubjectGenerated[MANUFACTURED_DATE] = label;
                }
            } else if (name.equalsIgnoreCase("engine_capacity")) {
                if (ACUtils.isEmpty(label)) {
                    strSubjectGenerated[ENGINE_CAPACITY] = EMPTY;
                } else {
                    if (label.length() > 3) {
                        engineCapacityConverted = new BigDecimal(label);
                        divider = new BigDecimal(THOUSAND);
                        BigDecimal convertedValue = engineCapacityConverted.divide(divider, 1, RoundingMode.HALF_UP);
                        strSubjectGenerated[ENGINE_CAPACITY] = convertedValue.toString();
                    } else {
                        strSubjectGenerated[ENGINE_CAPACITY] = label + SPACE + "cc";
                    }
                }
            }

            for (int i = 0; i < (Config.AUTO_GENERATED_SUBJECT_PARAMS_LIST).size(); i++) {
                if (!ACUtils.isEmpty(strSubjectGenerated[i])) {
                    if (strSubject.isEmpty()) {
                        strSubject = strSubjectGenerated[i];
                    } else {
                        strSubject = strSubject + SPACE + strSubjectGenerated[i];
                    }
                }
            }

            if (tvSubject != null && !ACUtils.isEmpty(strSubject.replaceAll("\\s+", Constants.EMPTY_STRING))) {
                tvSubject.setText(strSubject);
            }
        }
    }

    public void saveStateWithLabel(String key, String value, String label) {
        savedState.put(key, value);
        savedStateLabel.put(key + Constants.LABEL_VALUE, label);
    }

    public void removeStateAndLabel(String key) {
        savedState.remove(key);
        savedStateLabel.remove(key + Constants.LABEL_VALUE);
    }

    public static interface OnCallerActionListener {
        public void onCallerAction(Map<String, String> state, Map<String, String> stateLabel);

        public void onRequireLoginAction();
    }

}
