package com.mudah.my.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.DialogUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.ACInsertAdCategoryGroupChooser;
import com.mudah.my.activities.ACInsertAdPictureChooser;
import com.mudah.my.activities.ACInsertAdRegionChooser;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.activities.WebViewActivity;
import com.mudah.my.adapters.CategoryParamsAdapter;
import com.mudah.my.adapters.InsertAdCategoryParamsAdapter;
import com.mudah.my.adapters.InsertAdPicturesAdapter;
import com.mudah.my.configs.ApiConfigs;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.TealiumHelper;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.MudahUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InsertAdDetailsFragment extends Fragment implements InsertAdActivity.Form, CategoryParamsAdapter.OnCallerActionListener {
    private static final int REQUEST_CATEGORY = 1;
    private static final int REQUEST_PICTURE = 2;
    private static final int REQUEST_LOCATION = 3;
    private static final int REQUEST_PICTURE_PERMISSION = 4;
    private static final int LOADER_CATEGORY_PARAMS = 1;
    private static final int MAX_DEFAULT_IMAGES = 6;
    private final String CATEGORY_ERROR_LABEL = "ERROR_CATEGORY_MISSING";
    private final String REGION_ERROR_LABEL = "ERROR_REGION_MISSING";
    private final String IMAGE_ERROR_LABEL = "ERROR_MIN_UPLOAD_IMAGES";
    private final String BODY_ERROR_LABEL = "ERROR_BODY_MISSING";
    private final String SUBJECT_ERROR_LABEL = "ERROR_SUBJECT_MISSING";
    private final String BODY_ERROR_XITI_KEY = "2";
    private final String SUBJECT_ERROR_XITI_KEY = "1";
    private final String AD_TYPE_HIDE_IMAGE_UPLOADER = "k"; // ad type to hide the image uploader. In this case, job seeker
    private final String CAT_ID_HIDE_IMAGE_UPLOADER = "7020"; // category id to hide the image uploader (7020 is job category)
    private final String SELLER_ONBOARD_PAGENAME = "Seller Onboard";

    private final View.OnTouchListener scrollOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (getActivity() != null) {
                MudahUtil.hideSoftKeyboard(getActivity());
            }
            return false;
        }
    };
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_subtitle:
                    Intent intentOnboard = new Intent(getActivity(), WebViewActivity.class);
                    intentOnboard.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentOnboard.putExtra(WebViewActivity.EXTERNAL_URL, Config.shareHost + Config.SELLER_ONBOARD + Config.preferLang);
                    startActivity(intentOnboard);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_INSERT_AD_ID, SELLER_ONBOARD_PAGENAME, XitiUtils.NAVIGATION);
                    EventTrackingUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getContext(), SELLER_ONBOARD_PAGENAME, XitiUtils.LEVEL2_INSERT_AD_ID, null);
                    MudahUtil.saveClickTime(getActivity());
                    break;
            }
        }
    };
    protected int lastImagePosition;
    private InsertAdPicturesAdapter picturesAdapter;
    private GridView gvPictures;
    private Button bCategory;
    private InsertAdCategoryParamsAdapter categoryParamsAdapter;
    private View loadingFooter;
    private ScrollView svForm;
    private Button bNext;
    private LinearLayout llPhotoTips;
    private JSONObject errors;
    private View view;
    private LinearLayout llCategoryParams;
    private LinearLayout llType;
    private LinearLayout llLocationPlaceholder;
    private Map<String, String> oneTimeUseState;
    private Map<String, String> oneTimeUseCategoryParamsState;
    private JSONObject paramsObj;
    private int errorCountDetailsPage = 0;
    private RelativeLayout relativeLayoutProniagaMsg;
    private int showPronigaMsgState;
    private Button tvProniagaMsgCloseBtn;
    private TextView tvProniagaMsg;
    private LinearLayout llLocation;
    private Button bLocation;
    private TextView tvLocationLabel;
    private RelativeLayout rlImageUploader;
    private View vDivider;
    private boolean isSetStateCalled = false;
    private int pictureRequestedPosition;
    private RelativeLayout insertAdBanner;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public InsertAdDetailsFragment() {
    }

    public static InsertAdDetailsFragment newInstance() {
        return new InsertAdDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        picturesAdapter = new InsertAdPicturesAdapter();
        picturesAdapter.setMaxCount(MAX_DEFAULT_IMAGES);
        categoryParamsAdapter = new InsertAdCategoryParamsAdapter();
        categoryParamsAdapter.setOnCallerActionListener(this);
        categoryParamsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d();
                //Initialise the AdsView flag to differentiate ads listing and other operations
                redrawCategoryParams();
            }
        });
        ACReferences ref = ACReferences.getACReferences();
        ref.clearInsertAdACReferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d();
        view = inflater.inflate(R.layout.insert_ad_details, container, false);
        initializeTag();

        tvLocationLabel = (TextView) view.findViewById(R.id.tv_location_label);
        tvLocationLabel.setText(ApiConfigs.getLocationLabel());

        rlImageUploader = (RelativeLayout) view.findViewById(R.id.rl_image_uploader);
        vDivider = view.findViewById(R.id.divider);

        ACReferences ref = ACReferences.getACReferences();

        bLocation = (Button) view.findViewById(R.id.b_location);
        if (!ACUtils.isEmpty(ref.getInsertAdMunicipalityId())) {
            bLocation.setText(ACSettings.getACSettings().getLocationString(ref.getInsertAdRegionId(), ref.getInsertAdMunicipalityId()));
        } else {
            bLocation.setText(getString(R.string.insert_ad_no_selection, getString(R.string.insert_ad_item_location).toLowerCase()));
        }
        bLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ACInsertAdRegionChooser.class);
                intent.putExtra(ACInsertAdRegionChooser.HIDE_ALL_COUNTRY_OPTION, true);
                InsertAdActivity.isStartActivityForResult = true;
                startActivityForResult(intent, REQUEST_LOCATION);
            }
        });
        llLocation = (LinearLayout) view.findViewById(R.id.ll_location);

        relativeLayoutProniagaMsg = (RelativeLayout) view.findViewById((R.id.proniaga_msg_box));

        if (Config.enableSellerOnboard) {
            insertAdBanner = (RelativeLayout) view.findViewById(R.id.insert_ad_banner);
            insertAdBanner.setVisibility(View.VISIBLE);
            TextView insertAdBannerRedirect = (TextView) view.findViewById(R.id.tv_subtitle);
            insertAdBannerRedirect.setOnClickListener(onClickListener);
        }

        //1 mean enable the message at the top of the insert ad form
        //0 mean disable the message
        showPronigaMsgState = PreferencesUtils.getSharedPreferences(getActivity()).getInt(PreferencesUtils.INSERT_AD_PRONIAGA_MSG_STATE, 1);
        if (showPronigaMsgState == 0) {
            relativeLayoutProniagaMsg.setVisibility(View.GONE);
        }

        tvProniagaMsg = (TextView) view.findViewById(R.id.tv_proniaga_msg);
        if (getActivity() instanceof InsertAdActivity) {
            InsertAdActivity insertAdActivity = (InsertAdActivity) getActivity();
            tvProniagaMsg.setText(ACUtils.getHtmlFromString(getString(R.string.proniaga_msg, insertAdActivity.formatURLToInvokeInAppActivity(Config.tipsUrl), insertAdActivity.formatURLToInvokeInAppActivity(Config.rulesUrl), Config.loginProNiagaUrl)));
        }
        tvProniagaMsg.setTextColor(Color.BLACK);
        tvProniagaMsg.setMovementMethod(LinkMovementMethod.getInstance());

        tvProniagaMsgCloseBtn = (Button) view.findViewById((R.id.btn_proniaga_msg_close));
        tvProniagaMsgCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveShowProniagaMsgState(0);
            }
        });
        svForm = (ScrollView) view.findViewById(R.id.sv_form);
        svForm.setOnTouchListener(scrollOnTouchListener);

        gvPictures = (GridView) view.findViewById(R.id.gv_pictures);
        gvPictures.setNumColumns(GridView.AUTO_FIT);
        gvPictures.setAdapter(picturesAdapter);
        gvPictures.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });
        gvPictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pictureRequestedPosition = position;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PICTURE_PERMISSION);

                } else {
                    callPictureChooser(position);
                }
            }
        });

        bCategory = (Button) view.findViewById(R.id.b_category);
        if (ACReferences.getACReferences().getInsertAdCategoryId() != null) {
            String categoryId = ACReferences.getACReferences().getInsertAdCategoryId();
            bCategory.setText(ACSettings.getACSettings().getCategoryName(categoryId));
        } else {
            bCategory.setText(getString(R.string.insert_ad_no_selection, getString(R.string.insert_ad_category).toLowerCase()));
        }
        bCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ACInsertAdCategoryGroupChooser.class);
                InsertAdActivity.isStartActivityForResult = true;
                startActivityForResult(intent, REQUEST_CATEGORY);
            }
        });

        bNext = (Button) view.findViewById(R.id.b_next);
        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Trying to close the soft keyboard if it opens
                //We can safely ignore this if there is any error happen
                try {
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                } catch (Exception e) {
                    ACUtils.debug(e);
                }

                if (isClientValidationSuccess()) {
                    if (getActivity() instanceof InsertAdActivity) {
                        InsertAdActivity activity = ((InsertAdActivity) getActivity());
                        activity.setParamsObj(paramsObj);
                        activity.updatePostedByOption(categoryParamsAdapter.getPostedBy());
                        activity.setSelectedAdType(categoryParamsAdapter.getType());
                        activity.next();
                    }
                }
            }
        });

        loadingFooter = inflater.inflate(R.layout.loading_footer, null);
        llCategoryParams = (LinearLayout) view.findViewById(R.id.ll_category_params);
        llType = (LinearLayout) view.findViewById(R.id.ll_type);
        llLocationPlaceholder = (LinearLayout) view.findViewById(R.id.ll_location_placeholder);

        llPhotoTips = (LinearLayout) view.findViewById(R.id.ll_photoTips);
        llPhotoTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog(R.layout.dialog_photo_tips, getActivity());
            }
        });
        return view;
    }

    private void callPictureChooser(int position) {
        Intent intent = new Intent(getActivity(), ACInsertAdPictureChooser.class);

        if (position < picturesAdapter.getRealCount()) {
            intent.putExtra(ACInsertAdPictureChooser.EXTRA_CHANGE, true);
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_INSERT_AD_ID, "Edit Image", XitiUtils.NAVIGATION);
        } else {
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_INSERT_AD_ID, "Upload Image", XitiUtils.NAVIGATION);
        }
        lastImagePosition = position;
        intent.putExtra(ACInsertAdPictureChooser.LAST_IMAGE_POSITION, position);
        InsertAdActivity.isStartActivityForResult = true;
        startActivityForResult(intent, REQUEST_PICTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PICTURE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission Granted, pictureRequestedPosition: " + pictureRequestedPosition);
                    callPictureChooser(pictureRequestedPosition);
                } else {
                    Log.d("Permission Denial");
                }
                return;
            }
        }
    }

    private void showCustomDialog(int resId, final Context context) {
        final Dialog dialog = new Dialog(context, R.style.TranslucentFadeDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(resId);

        final ImageView imgGallery = (ImageView) dialog.findViewById(R.id.photo_tips_image_view);
        final int resizeWidth = 300;

        Picasso.with(context).load(categoryParamsAdapter.getPhotoTipsImg()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {

                //whatever algorithm here to compute size
                float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
                float heightFloat = ((float) resizeWidth) * ratio;

                final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) imgGallery.getLayoutParams();

                layoutParams.height = (int) heightFloat;
                layoutParams.width = resizeWidth;
                imgGallery.setLayoutParams(layoutParams);
                imgGallery.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                //Do nothing
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //DO nothing
            }
        });

        JSONArray photoTipsMsg = categoryParamsAdapter.getPhotoTipsMsg();
        String tips_msg;
        if (photoTipsMsg == null) {
            tips_msg = "<ul> " +
                    "<li>Take photos from different angles to get a 360-degree view.</li>" +
                    "<li>Take close-up shots to show buyers your item's condition.</li>" +
                    "<li>Make your item the center of attention. Avoid collages, frames and messy backgrounds.</li>" +
                    "</ul>";
        } else {
            tips_msg = "<ul>";
            for (int i = 0; i < photoTipsMsg.length(); i++) {
                tips_msg += "<li>" + photoTipsMsg.optString(i) + "</li>";
            }
            tips_msg = tips_msg + "</ul>";
        }

        WebView wvPhotoTips = (WebView) dialog.findViewById(R.id.wv_photo_tips);
        wvPhotoTips.loadDataWithBaseURL(null, tips_msg, "text/html", "utf-8", null);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_close);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        RelativeLayout rlPhototipsBackground = (RelativeLayout) dialog.findViewById(R.id.rl_phototips_background);
        rlPhototipsBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Does the basic validation check for mandatory fields
     *
     * @return {@code true} if the validation passes for all the mandatory fields,
     * {@code false} otherwise.
     */
    private boolean isClientValidationSuccess() {
        boolean isSuccess = true;
        String errorPageName = InsertAdActivity.INSERT_AD_PAGE[InsertAdActivity.AD_DETAIL_FRAGMENT];
        ACReferences ref = ACReferences.getACReferences();
        if (ACUtils.isEmpty(ref.getInsertAdCategoryId())) {
            Toast.makeText(getActivity(), getString(R.string.category_required_error), Toast.LENGTH_SHORT).show();
            if (XitiUtils.getInsertAdFormErrorMap().containsKey(Constants.CATEGORY_TXT)) {
                errorPageName += InsertAdActivity.UNDERSCORE + CATEGORY_ERROR_LABEL;
            }
            isSuccess = false;
        } else {
            if (!clientValidationRegionMunicipality(view)) {
                if (XitiUtils.getInsertAdFormErrorMap().containsKey(Constants.REGION)) {
                    errorPageName += InsertAdActivity.UNDERSCORE + REGION_ERROR_LABEL;
                }
                isSuccess = false;
            }
            //Min Images required check
            if (!isMinImagesRequiredSuccess()) {
                //set focus to photo
                svForm.smoothScrollTo(0, 250);
                if (XitiUtils.getInsertAdFormErrorMap().containsKey("image")) {
                    errorPageName += InsertAdActivity.UNDERSCORE + IMAGE_ERROR_LABEL;
                }
                isSuccess = false;
            }
        }
        if (!isSuccess) {
            XitiUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getActivity(), errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID, null);
            TealiumHelper.tagTealiumPage(getActivity(), TealiumHelper.APPLICATION_AI, errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID);
        }
        return isSuccess;
    }

    protected boolean clientValidationRegionMunicipality(View view) {
        ACReferences ref = ACReferences.getACReferences();
        String errorMessage;
        //Empty region check
        if (ACUtils.isEmpty(ref.getInsertAdRegionId()) || ACUtils.isEmpty(ref.getInsertAdMunicipalityId())) {
            errorMessage = getString(R.string.insert_ad_validation_region_error);
        } else {//clear the error
            errorMessage = null;
        }

        displayError(
                llLocation,
                R.id.rl_param_wrapper_location,
                R.id.ll_item_info_location,
                R.id.ll_item_error_location,
                R.id.tv_info_msg_location,
                R.id.tv_error_msg_location,
                errorMessage);
        if (ACUtils.isEmpty(errorMessage))
            return true;
        else
            return false;
    }

    public void displayError(ViewGroup baseViewGroup, int rlParamWrapperRes, int llItemInfoRes, int llItemErrorRes, int tvInfoMsgRes, int tvErrorMsgRes, String errorMsg) {

        RelativeLayout rlParamWrapper = (RelativeLayout) baseViewGroup.findViewById(rlParamWrapperRes);

        if (!ACUtils.isEmpty(errorMsg)) {
            rlParamWrapper.findViewById(llItemInfoRes).setVisibility(View.GONE);
            rlParamWrapper.findViewById(llItemErrorRes).setVisibility(View.VISIBLE);
            ((TextView) rlParamWrapper.findViewById(tvErrorMsgRes))
                    .setMovementMethod(LinkMovementMethod.getInstance());
            ((TextView) rlParamWrapper.findViewById(tvErrorMsgRes))
                    .setText(ACUtils.getHtmlFromString(errorMsg));
        } else {
            TextView tvInfoMsg = ((TextView) rlParamWrapper.findViewById(tvInfoMsgRes));
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

    public void saveShowProniagaMsgState(int state) {
        if (getActivity() != null) {
            PreferencesUtils.getSharedPreferences(getActivity()).edit()
                    .putInt(PreferencesUtils.INSERT_AD_PRONIAGA_MSG_STATE, state)
                    .apply();
        }
        if (state == 0) {
            relativeLayoutProniagaMsg.setVisibility(View.GONE);
        } else {
            relativeLayoutProniagaMsg.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method checks the minimum images required for the category
     *
     * @return {@code true} if the minimum images required satisfied,
     * {@code false} otherwise.
     */
    private boolean isMinImagesRequiredSuccess() {
        if (categoryParamsAdapter != null && categoryParamsAdapter.getAdTypeLabels().length() > 0) {
            JSONObject jsonObject = categoryParamsAdapter.getAdTypeLabels();
            int minReqdImages = Integer.parseInt(jsonObject.optString(InsertAdCategoryParamsAdapter.STR_MIN_REQD_IMAGES, "0"));
            if (picturesAdapter != null && picturesAdapter.getRealCount() < minReqdImages) {
                Toast.makeText(getActivity(), getString(R.string.picture_min_reqd_error, String.valueOf(minReqdImages)), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (oneTimeUseState != null) {
            setErrors(null);
            setState(oneTimeUseState);
            oneTimeUseState = null;
        } else {
            if (errors != null) {
                setErrors(errors);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d();
        // To avoid OutOfMemory, removes the reference to the activity, so that it can be garbage collected.
        // Ref: http://stackoverflow.com/questions/9536521/outofmemoryerror-when-loading-activities
        if (getActivity() != null) {
            ACUtils.unbindDrawables(getActivity().findViewById(R.id.sv_form));
            System.gc();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        InsertAdActivity.isStartActivityForResult = false;//reset
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_LOCATION) {
            ACReferences ref = ACReferences.getACReferences();
            setLocation(ref.getInsertAdRegionId(), ref.getInsertAdMunicipalityId());
            if (!ACUtils.isEmpty(ref.getInsertAdRegionId()) && !ACUtils.isEmpty(ref.getInsertAdMunicipalityId()))
                saveLocation();
        } else if (requestCode == REQUEST_PICTURE) {
            lastImagePosition = data.getIntExtra(ACInsertAdPictureChooser.LAST_IMAGE_POSITION, 0);
            if (data.getBooleanExtra(ACInsertAdPictureChooser.RESULT_DELETE, false)) {
                picturesAdapter.removeItem(lastImagePosition);
            } else {
                String imageUrl = data.getStringExtra(ACInsertAdPictureChooser.RESULT_IMAGE_URL);
                String imageDigest = data.getStringExtra(ACInsertAdPictureChooser.RESULT_IMAGE_DIGEST);
                String status = data.getStringExtra(ACInsertAdPictureChooser.RESULT_STATUS);
                String errorMessage = data.getStringExtra(ACInsertAdPictureChooser.RESULT_ERROR_MESSAGE);
                if (status != null && status.equalsIgnoreCase("OK")) {
                    if (imageUrl != null && imageDigest != null) {
                        // upload succeeded
                        if (picturesAdapter.getRealCount() > 0 && data.getBooleanExtra(ACInsertAdPictureChooser.EXTRA_CHANGE, false)) {
                            picturesAdapter.setItem(lastImagePosition, imageUrl, imageDigest);
                        } else {
                            picturesAdapter.addItem(imageUrl, imageDigest);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), ACUtils.getHtmlFromString(errorMessage), Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == REQUEST_CATEGORY) {
            updateCategory(null);
        }
    }

    @Override
    public Map<String, String> getState() {
        Map<String, String> params = new HashMap<>();
        ACReferences ref = ACReferences.getACReferences();
        if (ref.getInsertAdCategoryId() != null) {
            params.put(Constants.CATEGORY_TXT, ref.getInsertAdCategoryId());
            params.put(Constants.REGION, (ref.getInsertAdRegionId() == null) ? Constants.EMPTY_STRING : ref.getInsertAdRegionId());
            params.put(Constants.SUBAREA, (ref.getInsertAdMunicipalityId() == null) ? Constants.EMPTY_STRING : ref.getInsertAdMunicipalityId());
        }
        params.putAll(picturesAdapter.getState());
        params.putAll(categoryParamsAdapter.getState());
        if (params.containsKey(CategoryParamsAdapter.STR_AD_TYPE)) {
            String companyAd = categoryParamsAdapter.getCompanyAd(params.get(CategoryParamsAdapter.STR_AD_TYPE));
            if (!ACUtils.isEmpty(companyAd))
                params.put("company_ad", companyAd);
        }
        Log.d("params " + params.toString());
        return params;
    }

    @Override
    public void setState(Map<String, String> params) {
        Log.d(params + Constants.EMPTY_STRING);
        errors = null;
        isSetStateCalled = true;//need this flag for draft to load properly when clicking on notification
        if (getView() != null) {
            if (params == null) {
                params = new HashMap<>();
            }
            picturesAdapter.setState(params);
            loadLocation(params);
            ACReferences.getACReferences().setInsertAdCategoryId(params.get(Constants.CATEGORY_TXT));

            updateCategory(params);
        } else {
            oneTimeUseState = params;
        }
    }

    private void saveLocation() {
        ACReferences ref = ACReferences.getACReferences();
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(getActivity());
        pref.edit()
                .putString(PreferencesUtils.ITEM_REGION, ref.getInsertAdRegionId())
                .putString(PreferencesUtils.ITEM_MUNICIPALITY, ref.getInsertAdMunicipalityId())
                .apply();
    }

    public void loadLocation(Map<String, String> overwrite) {
        SharedPreferences pref = PreferencesUtils.getSharedPreferences(getActivity());
        String region = (!ACUtils.isEmpty(overwrite.get("region"))) ? overwrite.get("region") : pref.getString(PreferencesUtils.ITEM_REGION, null);
        String municipality = (!ACUtils.isEmpty(overwrite.get("subarea"))) ? overwrite.get("subarea") : pref.getString(PreferencesUtils.ITEM_MUNICIPALITY, null);
        setLocation(region, municipality);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("isVisibleToUser: " + isVisibleToUser + ", isSetStateCalled: " + isSetStateCalled);
        // This method is being called when setState() is called as well and it will overwrite the store state values.
        // To prevent double update, check first if setState() is called yet.
        // If isVisibleToUser is true and isSetStateCalled is also true, reset isSetStateCalled to false
        // so that this method can still update Category in future, like when user clicks back.
        if (isVisibleToUser && !isSetStateCalled) {
            if (getActivity() instanceof InsertAdActivity) {
                updateCategory(new HashMap<String, String>());
            }
        }
    }

    @Override
    public int setErrors(JSONObject errorObj) {
        this.errors = errorObj;
        int errorCount = 0;
        String errorMessage;
        Map<String, String> mapXitiErrorFormParams = new HashMap<>();

        if (!ACUtils.isEmpty(errorObj.optString("region_error_label"))) {
            errorMessage = errorObj.optString("region_error_label");
            String errorKey = XitiUtils.getInsertAdFormErrorMap().get("region");
            if (errorKey != null && errorObj.has("region"))
                mapXitiErrorFormParams.put(errorKey, errorObj.optString("region"));

            errorCount++;
        } else {
            errorMessage = null;
        }
        displayError(
                llLocation,
                R.id.rl_param_wrapper_location,
                R.id.ll_item_info_location,
                R.id.ll_item_error_location,
                R.id.tv_info_msg_location,
                R.id.tv_error_msg_location,
                errorMessage);

        if (!ACUtils.isEmpty(errorObj.optString("subarea_error_label"))) {
            errorMessage = errorObj.optString("subarea_error_label");
            String errorKey = XitiUtils.getInsertAdFormErrorMap().get("region");
            if (errorKey != null && errorObj.has("region"))
                mapXitiErrorFormParams.put(errorKey, errorObj.optString("region"));
            errorCount++;
        } else {
            errorMessage = null;
        }
        displayError(
                llLocation,
                R.id.rl_param_wrapper_location,
                R.id.ll_item_info_location,
                R.id.ll_item_error_location,
                R.id.tv_info_msg_location,
                R.id.tv_error_msg_location,
                errorMessage);

        if (errorObj != null && !ACUtils.isEmpty(errorObj.optString("category_error_label"))) {
            String errorKey = XitiUtils.getInsertAdFormErrorMap().get(Constants.CATEGORY_TXT);
            if (errorKey != null && errorObj.has(Constants.CATEGORY_TXT))
                mapXitiErrorFormParams.put(errorKey, errorObj.optString(Constants.CATEGORY_TXT));
            errorCount++;
        }

        int errorParamsCount = categoryParamsAdapter.setErrors(errorObj);
        if (errorParamsCount > 0) {
            JSONObject errorCode = categoryParamsAdapter.getErrorsLabel();
            Map<String, String> mapFormParams = XitiUtils.getErrorFormParamsMap(errorCode);
            if (mapFormParams.size() > 0) {
                //if there is error for Subject or Body, tag it as a page
                if (mapFormParams.containsValue(SUBJECT_ERROR_LABEL) || mapFormParams.containsValue(BODY_ERROR_LABEL)) {
                    sendErrorTagAsPage(mapFormParams);
                    mapFormParams.remove(SUBJECT_ERROR_XITI_KEY);
                    mapFormParams.remove(BODY_ERROR_XITI_KEY);
                }
                //if still have other params error
                if (mapFormParams.size() > 0) {
                    mapXitiErrorFormParams.putAll(mapFormParams);
                }
            }
            errorCount += errorParamsCount;
        }
        String errorPageName = InsertAdActivity.INSERT_AD_PAGE[InsertAdActivity.AD_DETAIL_FRAGMENT] + "_Error";
        if (mapXitiErrorFormParams.size() > 0) {
            EventTrackingUtils.sendLevel2CustomVariable(XitiUtils.LEVEL2_INSERT_AD, errorPageName, mapXitiErrorFormParams);//send page and error
        }
        setErrorCount(errorCount);
        return errorCount;
    }

    private void sendErrorTagAsPage(Map<String, String> mapFormParams) {
        String errorPageName = InsertAdActivity.INSERT_AD_PAGE[InsertAdActivity.AD_DETAIL_FRAGMENT];
        if (mapFormParams.containsValue(SUBJECT_ERROR_LABEL)) {
            errorPageName += InsertAdActivity.UNDERSCORE + SUBJECT_ERROR_LABEL;
        }
        if (mapFormParams.containsValue(BODY_ERROR_LABEL)) {
            errorPageName += InsertAdActivity.UNDERSCORE + BODY_ERROR_LABEL;
        }
        XitiUtils.sendTagWithMode(XitiUtils.MODE_OTHERS, getActivity(), errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID, null);
        TealiumHelper.tagTealiumPage(getActivity(), TealiumHelper.APPLICATION_AI, errorPageName, XitiUtils.LEVEL2_INSERT_AD_ID);
    }

    public void setLocation(String regionId, String municipalityId) {
        ACReferences ref = ACReferences.getACReferences();
        ref.setInsertAdRegionId(regionId);
        ref.setInsertAdMunicipalityId(municipalityId);

        if (!ACUtils.isEmpty(ref.getInsertAdMunicipalityId())) {
            bLocation.setText(ACSettings.getACSettings().getLocationString(ref.getInsertAdRegionId(), ref.getInsertAdMunicipalityId()));
        } else {
            bLocation.setText(getString(R.string.insert_ad_no_selection, getString(R.string.insert_ad_item_location).toLowerCase()));
        }
    }

    public InsertAdPreviewFragment.CategoryParams getCategoryParams() {
        return categoryParamsAdapter;
    }

    private void updateCategory(Map<String, String> categoryParamsState) {
        ACReferences ref = ACReferences.getACReferences();
        if (ref.getInsertAdCategoryId() == null) {
            resetCategory();
        } else {
            if (categoryParamsState != null && categoryParamsState.size() > 0) {
                //restoring params
                oneTimeUseCategoryParamsState = new HashMap<>();
                oneTimeUseCategoryParamsState.putAll(categoryParamsState);
                loadCategoryParams(ref);
            } else if (categoryParamsState != null && categoryParamsAdapter.getState().size() > 0) {
                //Click back button case.
                //Need to set adtype back to Adapter to get a correct params.
                //in case that users change adType on the profile page
                setAdapterAdType();
                categoryParamsAdapter.notifyDataSetChanged();
            } else {
                //when changing a new category
                oneTimeUseCategoryParamsState = null;
                loadCategoryParams(ref);
            }
        }
    }

    private void setAdapterAdType() {
        if (getActivity() instanceof InsertAdActivity && !ACUtils.isEmpty(((InsertAdActivity) getActivity()).getPostedByOption())) {
            categoryParamsAdapter.setPostedBy(((InsertAdActivity) getActivity()).getPostedByOption());
        }
    }

    private void loadCategoryParams(ACReferences ref) {
        categoryParamsAdapter.clearTypes();
        bCategory.setText(ACSettings.getACSettings().getCategoryName(ref.getInsertAdCategoryId()));
        getLoaderManager().restartLoader(LOADER_CATEGORY_PARAMS, null, newCategoryParamsCallbacks(ref.getInsertAdCategoryId()));
    }


    private BlocketLoader.Callbacks newCategoryParamsCallbacks(String categoryId) {
        llCategoryParams.removeAllViews();
        if (llCategoryParams.getChildAt(0) != loadingFooter) {
            llCategoryParams.addView(loadingFooter, 0);
            DialogUtils.showProgressDialog(getActivity(), getString(R.string.loading));
        }
        Map<String, Object> params = new HashMap<>();
        params.put("cg", categoryId);
        return new BlocketLoader.Callbacks(Method.GET, "params", params, getActivity()) {
            @Override
            public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
                super.onLoadFinished(loader, data);
                llCategoryParams.removeView(loadingFooter);
                DialogUtils.hideProgressDialog(getActivity());
            }

            @SuppressLint("NewApi")
            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                try {
                    final int oldMaxCount = picturesAdapter.getMaxCount();
                    final int oldRealCount = picturesAdapter.getRealCount();
                    int maxCount = data.getInt("extra_images") + 1;
                    picturesAdapter.setMaxCount(maxCount);
                    categoryParamsAdapter.setTypes(data, oneTimeUseCategoryParamsState);
                    paramsObj = data;
                    //get location label
                    if (data.has("location_label")) {
                        if (tvLocationLabel != null) {
                            tvLocationLabel.setText(data.getString("location_label"));
                        }
                    }

                    //get insert ad redirect to browser config
                    if (data.has("insert_ad_redirect_browser")) {
                        ApiConfigs.saveConfig(getActivity(), ApiConfigs.config_insertAdRedirect, data.getString("insert_ad_redirect_browser"));
                        boolean redirect = ApiConfigs.getConfigBoolean(getActivity().getApplicationContext(), ApiConfigs.config_insertAdRedirect);
                        if (redirect) {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.insert_ad_redirection_message), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getActivity(), AdsListActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(MudahUtil.IntentDataID.INTENT_REDIRECT_INSERT_AD, true);
                            getActivity().startActivity(intent);
                            getActivity().finish();
                        }
                    }


                    if (oneTimeUseCategoryParamsState == null) {
                        Log.d("fresh change of category");
                        // fresh change of category: auto-scroll
                        llCategoryParams.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                int maxCount = picturesAdapter.getMaxCount();
                                int realCount = picturesAdapter.getRealCount();

                                if (oldMaxCount != maxCount) {
                                    svForm.smoothScrollTo(0, 0);
                                    String notice;
                                    int length = Toast.LENGTH_SHORT;
                                    if (oldMaxCount > maxCount) {
                                        notice = getString(R.string.insert_ad_pictures_max_reduced, maxCount);
                                        if (realCount < oldRealCount) {
                                            notice += "\n" + getResources().getQuantityString(R.plurals.insert_ad_pictures_removed, oldRealCount - realCount, oldRealCount - realCount);
                                            length = Toast.LENGTH_LONG;
                                        }
                                    } else {
                                        notice = getString(R.string.insert_ad_pictures_max_increased, maxCount);
                                    }
                                    Toast.makeText(getActivity(), notice, length).show();
                                }
                                if (Build.VERSION.SDK_INT < 16) {
                                    llCategoryParams.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                } else {
                                    llCategoryParams.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                }
                            }
                        });
                    } else {
                        // restored change of category: don't auto-scroll
                        Log.d("restored change of category: " + oneTimeUseCategoryParamsState);
                        picturesAdapter.setState(oneTimeUseCategoryParamsState);
                        oneTimeUseCategoryParamsState = null;
                        if (isSetStateCalled) {
                            Log.d("restored back isSetStateCalled to false");
                            //reset isSetStateCalled
                            isSetStateCalled = false;
                        }
                    }
                } catch (JSONException e) {
                    Log.e(e);
                    onLoadError(loader, data);
                }
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                DialogUtils.showGeneralErrorAlert(getActivity());
                resetCategory();
                if (getActivity() instanceof InsertAdActivity) {
                    ((InsertAdActivity) getActivity()).tagConnectionLost(InsertAdActivity.INSERT_AD_CONNECTION_LOST, data);
                }
            }
        };
    }


    private void resetCategory() {
        ACReferences.getACReferences().setInsertAdCategoryId(null);
        if (bCategory != null)
            bCategory.setText(getString(R.string.insert_ad_no_selection, getString(R.string.insert_ad_category).toLowerCase()));
        if (categoryParamsAdapter != null)
            categoryParamsAdapter.clearTypes();
    }

    public void addTypeView() {
        try {
            llType.addView(categoryParamsAdapter.newTypesView(llCategoryParams.getContext()));
        } catch (JSONException e) {
            ACUtils.debug(e);
        }
    }

    private void redrawCategoryParams() {
        if (categoryParamsAdapter != null)
            if (categoryParamsAdapter != null && categoryParamsAdapter.getAdTypeLabels().length() > 0) {
                //remove all views
                preRedrawCategoryParams();
                if (getActivity() instanceof InsertAdActivity)
                    ((InsertAdActivity) getActivity()).redrawCategoryParams(categoryParamsAdapter, llCategoryParams, InsertAdActivity.PAGE_1, errors);
            }
    }

    private void preRedrawCategoryParams() {
        llCategoryParams.removeAllViews();
        llType.removeAllViews();
        clearRequireLoginAction();
        categoryParamsAdapter.setTextColor(Color.BLACK);
    }

    //Mudah Specific
    @Override
    public void onRequireLoginAction() {
        //Ex. Job with I'm an Employer requires login
        bNext.setVisibility(View.GONE);
        llLocationPlaceholder.setVisibility(View.GONE);
    }

    public void clearRequireLoginAction() {
        //Ex. Job with I'm an Employer requires login
        bNext.setVisibility(View.VISIBLE);
        llLocationPlaceholder.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCallerAction(Map<String, String> state, Map<String, String> stateLabels) {
        String categoryId = ACReferences.getACReferences().getInsertAdCategoryId();
        String adType = state.get("type");
        if (this.CAT_ID_HIDE_IMAGE_UPLOADER.equals(categoryId) && this.AD_TYPE_HIDE_IMAGE_UPLOADER.equalsIgnoreCase(adType)) {
            rlImageUploader.setVisibility(View.GONE);
            vDivider.setVisibility(View.GONE);
            gvPictures.setVisibility(View.GONE);
        } else {
            rlImageUploader.setVisibility(View.VISIBLE);
            vDivider.setVisibility(View.VISIBLE);
            gvPictures.setVisibility(View.VISIBLE);
        }

    }

    private void initializeTag() {
        String myTag = getTag();
        ((InsertAdActivity) getActivity()).setInsertAdDetailsFragmentTag(myTag);
    }

    public void setErrorCount(int errorCount) {
        errorCountDetailsPage = errorCount;
    }

    public Map<String, String> getFilledFieldsParams() {
        Map<String, String> mapXitiFormParams = new HashMap<>();

        if (bCategory != null && !getString(R.string.insert_ad_no_selection, getString(R.string.insert_ad_category).toLowerCase()).equals(bCategory.getText().toString()))
            mapXitiFormParams.put(XitiUtils.CATEGORY, bCategory.getText().toString());

        if (bLocation != null && !getString(R.string.insert_ad_no_selection, getString(R.string.insert_ad_item_location).toLowerCase()).equalsIgnoreCase(bLocation.getText().toString()))
            mapXitiFormParams.put(XitiUtils.LOCATION, bLocation.getText().toString());

        if (picturesAdapter != null) {
            mapXitiFormParams.put(XitiUtils.PHOTO, picturesAdapter.getRealCount() + "/" + picturesAdapter.getMaxCount());// Photo_filled/Photo_total
        }

        if (categoryParamsAdapter != null) {
            Map<String, String> currentFilledFields = categoryParamsAdapter.getState();
            //remove() returns the value of the removing mapping or null is the mappign is not found
            mapXitiFormParams.put(XitiUtils.COMMON_FIELDS, !ACUtils.isEmpty(currentFilledFields.remove("subject")) + Constants.COMMA
                    + !ACUtils.isEmpty(currentFilledFields.remove("body")) + Constants.COMMA
                    + !ACUtils.isEmpty(currentFilledFields.remove("price"))
            );//Ad_type,Heading, Description, Price

            //remove 'adtype' from the hashmap before counting the ad params
            currentFilledFields.remove("adtype");
            String type = currentFilledFields.remove("type"); // s/k/u/h
            String filledParamNames = getFilledParamNames(currentFilledFields);
            if (!ACUtils.isEmpty(filledParamNames)) {
                mapXitiFormParams.put(XitiUtils.CATEGORY_PARAMS_FIELDS, type + Constants.COMMA + filledParamNames);
                mapXitiFormParams.put(XitiUtils.CATEGORY_PARAMS, filledParamNames.split(Constants.COMMA).length + "/" + currentFilledFields.size());// category_params_filled / total
            } else {
                mapXitiFormParams.put(XitiUtils.CATEGORY_PARAMS_FIELDS, type);
                mapXitiFormParams.put(XitiUtils.CATEGORY_PARAMS, "0/" + currentFilledFields.size());
            }
        }

        Log.d("mapXitiFormParams: " + mapXitiFormParams);
        return mapXitiFormParams;
    }

    private String getFilledParamNames(Map<String, String> currentFilledFields) {
        String filledParamNames = Constants.EMPTY_STRING;

        for (String key : currentFilledFields.keySet()) {
            if (!ACUtils.isEmpty(currentFilledFields.get(key))) {
                filledParamNames += key + Constants.COMMA;
            }
        }
        //remove the extra last comma
        if (!ACUtils.isEmpty(filledParamNames)) {
            filledParamNames = filledParamNames.substring(0, filledParamNames.length() - 1);
        }

        return filledParamNames;

    }

}
