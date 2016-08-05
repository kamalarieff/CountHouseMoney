package com.mudah.my.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.lib701.adapters.MediaPagerAdapter;
import com.lib701.datasets.ACAd;
import com.lib701.datasets.ACAdParameter;
import com.lib701.helper.FullScreenGallery;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.lib701.widgets.UnderlinePageIndicator;
import com.mudah.my.R;
import com.mudah.my.activities.AdViewActivity;
import com.mudah.my.adapters.AdViewRecommendedAdAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.datasets.AdViewAd;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;
import com.mudah.my.models.GravityModel;
import com.mudah.my.utils.EventTrackingUtils;
import com.mudah.my.utils.GravityUtils;
import com.mudah.my.utils.MudahUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class AdViewFragment extends Fragment {
    public static final String EXTRA_LIST_ID = "list_id";
    public static final String AD_NOT_EXIST_CODE = "204";
    private static final String PARAM_KEY = "tv_param_key";
    private static final String PARAM_VALUE = "tv_param_value";
    private static final String AD_DESC = "tv_ad_description";
    private static final String ADVIEW_CONNECTION_LOST = "Adview_Connection_Lost";
    private static final String RECOMMENDED_AD_CLICK_NAME = "Adview_recommended_ad";
    private static final String TITLE_TEXT = "ad_view_title_text";
    private static final String CAR_CATEGORY = "1020";
    private static final String VERIFIED = "verified";

    protected UnderlinePageIndicator mIndicator;
    protected ViewPager mMedia;
    protected ImageView zoomIcon;
    private AdViewAd data;
    private int listId = 1;
    private int categoryId = 0;
    private View vConnectionLost;
    private int adPosition = 0;
    private ProgressBar pbLoading;
    private boolean isTopPaddingSet = false;
    private boolean facebook = true;
    private boolean instagram = true;
    private boolean whatsapp = true;
    private boolean sms = true;
    private boolean isVisibleToUser;
    private ImageView facebookBottom;
    private ImageView instagramBottom;
    private ImageView whatsappBottom;
    private ImageView smsBottom;
    private ScrollView scrollView;
    private ArrayList<GravityModel> recommendedItems = null;
    private int recommendedAdsLimit = 4;
    private ListView recommendedAdList;
    private AdapterView.OnItemClickListener recommendedAdClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AdViewRecommendedAdAdapter adapter = (AdViewRecommendedAdAdapter) recommendedAdList.getAdapter();
            GravityUtils.tagGravityEvent(GravityUtils.EVENT_TYPE_REC_CLICK, adapter.getItem(position));
            EventTrackingUtils.sendClickByCategoryId(null, categoryId, XitiUtils.AD_VIEW, RECOMMENDED_AD_CLICK_NAME, XitiUtils.NAVIGATION);

            Intent intent = new Intent(getActivity(), AdViewActivity.class);
            intent.putExtra(AdViewActivity.EXTRA_LIST_ITEM_POSITION, position);
            intent.putExtra(AdViewActivity.EXTRA_GRAND_TOTAL, recommendedAdsLimit);
            intent.putStringArrayListExtra(AdViewActivity.EXTRA_ALL_LIST_ID, adapter.getAllAdIdList());
            startActivity(intent);
        }
    };

    /**
     * Empty constructor as per the Fragment documentation
     */
    public AdViewFragment() {
    }

    /**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */
    public static AdViewFragment newInstance(int listId, int adPosition) {
        AdViewFragment f = new AdViewFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("listId", listId);
        args.putInt("adPosition", adPosition);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //setRetainInstance(true);
        if (getArguments() != null) {
            listId = getArguments().getInt("listId");
            adPosition = getArguments().getInt("adPosition");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Need to verify whether this is AdViewActivity or InsertAdActivity
        //Call API for AdViewActivity

        if (getActivity() != null && (getActivity() instanceof AdViewActivity)) {
            //Need to manage call the bottom action bar for the first page view
            //add updateUI() in both OnloadComplete and OnResume to handle the rotation(there is no api call)
            int currentPosition = ((AdViewActivity) getActivity()).getCurrentPosition();
            boolean currentAd = (adPosition == currentPosition);
            if (currentAd && data != null) {
                ((AdViewActivity) getActivity()).updateUI();
            } else {
                updateLoading();
            }
        }
    }

    private void updateLoading() {
        pbLoading.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(adPosition, null, newAdViewCallback());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d();
        View rootView = inflater.inflate(R.layout.ad_view, container, false);
        vConnectionLost = rootView.findViewById(R.id.view_connection_lost);
        ImageView connectionLostImg = (ImageView) rootView.findViewById(R.id.imgv_connection_lost);

        vConnectionLost.setVisibility(View.GONE);
        vConnectionLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoading();
            }
        });

        pbLoading = (ProgressBar) rootView.findViewById(R.id.adv_pb_loading);

        mMedia = (ViewPager) rootView.findViewById(R.id.media);
        mIndicator = (UnderlinePageIndicator) rootView.findViewById(R.id.indicator);

        zoomIcon = (ImageView) rootView.findViewById(R.id.icon_zoom);

        facebookBottom = (ImageView) rootView.findViewById(R.id.share_to_facebook_bottom);
        instagramBottom = (ImageView) rootView.findViewById(R.id.share_to_instagram_bottom);
        whatsappBottom = (ImageView) rootView.findViewById(R.id.share_to_whatsapp_bottom);
        smsBottom = (ImageView) rootView.findViewById(R.id.share_to_sms_bottom);

        scrollView = (ScrollView) rootView.findViewById(R.id.sv_ad_view);
        recommendedAdList = (ListView) rootView.findViewById(R.id.adview_recommended_ad);

        Context context = rootView.getContext();
        Picasso.with(context).load(R.drawable.loader_connection_lost).fit().centerInside().into(connectionLostImg);
        Picasso.with(context).load(R.drawable.icon_zoom).into(zoomIcon);
        Picasso.with(context).load(R.drawable.share_ic_fb).into(facebookBottom);
        Picasso.with(context).load(R.drawable.share_ic_ig).into(instagramBottom);
        Picasso.with(context).load(R.drawable.share_ic_wa).into(whatsappBottom);
        Picasso.with(context).load(R.drawable.share_ic_sms).into(smsBottom);

        return rootView;
    }

    public void getSimilarRecommendations() {
        GetSimilarRecommendationsAsyncTask getSimilarRecommendationsAsyncTask = new GetSimilarRecommendationsAsyncTask(this);
        getSimilarRecommendationsAsyncTask.execute();
    }

    private void initImagePager(ACAd ad) {
        boolean noImageAd = false;
        // No need for an indicator if there's only 1 image
        if (ad == null || ad.getImages().size() <= 1) {
            mIndicator.setVisibility(View.INVISIBLE);
        }
        //create a default no image picture
        if (ad != null && ad.getImages().size() == 0) {
            ad.getImages().add(MediaPagerAdapter.DEFAULT_NO_IMAGE);
            if (zoomIcon != null)
                zoomIcon.setVisibility(View.GONE);
            noImageAd = true;
        }

        // Create the gallery
        if (getActivity() == null) return; //do nothing
        if (ad != null && !noImageAd) {
            final ArrayList<String> resources = ad.getImages();
            PagerAdapter adapter = new MediaPagerAdapter(getActivity().getApplicationContext(), resources, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = FullScreenGallery.newIntent(
                            getActivity(), resources, mMedia.getCurrentItem());
                    getActivity().startActivity(intent);
                    EventTrackingUtils.sendClickByCategoryId(null, categoryId, XitiUtils.AD_VIEW,
                            "Ad_detail_click_gallery", XitiUtils.NAVIGATION);
                }
            });
            mMedia.setAdapter(adapter);
            // Some logic to determine when to scroll the gallery and when to scroll the page
            // http://stackoverflow.com/questions/8381697/viewpager-includes-a-scrollview-does-not-scroll-correclty
            mMedia.setOnTouchListener(new View.OnTouchListener() {

                private int dragThreshold = 300;
                private int downX;
                private int downY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                downX = (int) event.getRawX();
                                downY = (int) event.getRawY();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                int distanceX = Math.abs((int) event.getRawX() - downX);
                                int distanceY = Math.abs((int) event.getRawY() - downY);
                                if (distanceY > distanceX && distanceY > dragThreshold) {
                                    mMedia.getParent().requestDisallowInterceptTouchEvent(true);
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                EventTrackingUtils.sendClickByCategoryId(null, categoryId, XitiUtils.AD_VIEW,
                                        "Ad_detail_swipe_gallery", XitiUtils.NAVIGATION);
                                break;
                        }
                    } catch (Exception error) {
                        ACUtils.debug(error, "Adview_crash", "adPostion: " + adPosition + ", isAdded(): " + isAdded() + ", isRemoving(): " + isRemoving());
                    }
                    return false;
                }
            });

            mIndicator.setViewPager(mMedia);
        } else if (noImageAd) {
            ArrayList<String> resources = ad.getImages();
            PagerAdapter adapter = new MediaPagerAdapter(getActivity().getApplicationContext(), resources);
            mMedia.setAdapter(adapter);
            mIndicator.setViewPager(mMedia);
        }
    }

    public AdViewAd getData() {
        return data;
    }

    public void setData(AdViewAd data) {
        this.data = data;
        categoryId = ACUtils.isEmpty(data.getCategoryId()) ? 0 : Integer.parseInt(data.getCategoryId());
    }

    private void addTableRow(TableLayout tl, String key, String value, boolean lastItem) {
        TableRow tr = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.ad_view_info_row, null);
        if (!isTopPaddingSet && lastItem) {
            tr.setPadding(tr.getPaddingLeft(), 0, tr.getPaddingRight(), 0);
            isTopPaddingSet = true;
        } else if (!isTopPaddingSet) {
            tr.setPadding(tr.getPaddingLeft(), 0, tr.getPaddingRight(), tr.getPaddingBottom());
            isTopPaddingSet = true;
        } else if (lastItem) {
            tr.setPadding(tr.getPaddingLeft(), tr.getPaddingTop(), tr.getPaddingRight(), 0);
        }

        // Add Label Column
        TextView tvTitle = (TextView) tr.findViewWithTag("tv_key");
        tvTitle.setText(ACUtils.getHtmlFromString(key));

        // Add Value Column
        TextView tvValue = (TextView) tr.findViewWithTag("tv_value");
        tvValue.setText(ACUtils.getHtmlFromString(Constants.BOLD_OPEN_TAG + value + Constants.BOLD_CLOSE_TAG));

        tl.addView(tr);
    }

    private void addSpecialTableRow(TableLayout tl, String key, String value) {
        TableRow tr = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.ad_view_special_info_row, null);
        // Add Label Column
        TextView tvTitle = (TextView) tr.findViewWithTag(PARAM_KEY);
        tvTitle.setText(ACUtils.getHtmlFromString(key));

        // Add Value Column
        TextView tvValue = (TextView) tr.findViewWithTag(PARAM_VALUE);
        tvValue.setText(ACUtils.getHtmlFromString(Constants.BOLD_OPEN_TAG + value + Constants.BOLD_CLOSE_TAG));
        if (Config.RED_ADVIEW_PARAMS_LIST.contains(key)) {
            tvValue.setTextColor(getResources().getColor(R.color.ad_price));
            data.setPrice(value);
        }

        tl.addView(tr);
    }

    private String formatString(String string) {
        if (string != null) {
            if (string.contains(Constants.STRONG_OPEN_TAG)) {
                string = string.replace(Constants.STRONG_OPEN_TAG, Constants.BOLD_OPEN_TAG);
            }
            if (string.contains(Constants.STRONG_CLOSE_TAG)) {
                string = string.replace(Constants.STRONG_CLOSE_TAG, Constants.BOLD_CLOSE_TAG);
            }
            if (string.contains(Constants.NEW_LINE_TAG)) {
                string = string.replace(Constants.NEW_LINE_TAG, Constants.BREAK_TAG);
            }
        }
        return string;
    }

    public void updateUI() {
        Log.d();
        View view = getView();
        if (data != null && view != null) {

            Log.d(" adPosition: " + adPosition + ", list_id: " + data.getListId() + ", ad_id: " + data.getAdId());
            initImagePager(data);
            TextView firstTitle = (TextView) view.findViewWithTag(TITLE_TEXT);
            firstTitle.setText(data.getSubject());

            ImageView sellerIcon = (ImageView) view.findViewById(R.id.ad_view_seller_type_icon);

            if ((Constants.AD_SRC_COMPANY).equals(data.getCompanyAd())) {
                Picasso.with(getActivity().getApplicationContext()).load(R.drawable.company).into(
                        sellerIcon);
            } else {
                Picasso.with(getActivity().getApplicationContext()).load(R.drawable.private_icon).into(
                        sellerIcon);
            }

            TextView adViewSeller = (TextView) view.findViewById(R.id.ad_view_seller_text);

            if (!ACUtils.isEmpty(data.getCompanyRoc())) {
                adViewSeller.setText(String.format("%s (%s)", data.getName(), data.getCompanyRoc()));
            } else {
                adViewSeller.setText(String.format("%s", data.getName()));
            }

            ImageView verifiedIcon = (ImageView) view.findViewById(R.id.ad_view_verified_badge_icon);
            TextView verifiedText = (TextView) view.findViewById(R.id.ad_view_verified_text);
            View verifiedDivider = (View) view.findViewById(R.id.verified_divider);

            if (CAR_CATEGORY.equals(data.getCategoryId()) && VERIFIED.equals(data.getStoreVerified())) {
                verifiedIcon.setVisibility(View.VISIBLE);
                verifiedDivider.setVisibility(View.VISIBLE);
                verifiedText.setVisibility(View.VISIBLE);
            } else {
                verifiedIcon.setVisibility(View.GONE);
                verifiedText.setVisibility(View.GONE);
                verifiedDivider.setVisibility(View.GONE);
            }


            TextView adViewDate = (TextView) view.findViewById(R.id.ad_view_date_text);
            adViewDate.setText(String.format("%s", data.getDate()));

            TableLayout tlInfo = (TableLayout) view.findViewById(R.id.tl_ad_info);
            tlInfo.removeAllViews();
            isTopPaddingSet = false;
            ArrayList<ACAdParameter> infoList = data.getParameters();
            if (infoList != null) {
                int infolistSize = infoList.size();
                int lastItemIndex = infolistSize - 1;
                for (int i = 0; i < infolistSize; i++) {
                    ACAdParameter info = infoList.get(i);
                    if (!ACUtils.isEmpty(info.getLabel()) && !ACUtils.isEmpty(info.getValue())) {
                        if (Config.SPECIAL_ADVIEW_PARAMS_LIST.contains(info.getLabel())) {
                            addSpecialTableRow(tlInfo, info.getLabel(), info.getValue());
                        } else {
                            addTableRow(tlInfo, info.getLabel(), info.getValue(), (lastItemIndex == i));
                        }
                    }
                }
            }

            TextView desc = (TextView) view.findViewWithTag(AD_DESC);
            if (data.getBody() != null) {
                String string = data.getBody();
                desc.setText(ACUtils.getHtmlFromString(formatString(string)));
            }
            //Mudah specific
            if (!ACUtils.isEmpty(data.getWhatsApp()))
                desc.append(ACUtils.getHtmlFromString(Constants.BREAK_TAG + Constants.BREAK_TAG + Constants.BOLD_OPEN_TAG + data.getWhatsApp() + Constants.BOLD_CLOSE_TAG));

        }

        if (data != null)
            checkShareIcons();
    }

    private void checkShareIcons() {
        if (getActivity() instanceof AdViewActivity) {
            AdViewActivity adViewActivity = ((AdViewActivity) getActivity());
            facebook = adViewActivity.facebook;
            instagram = adViewActivity.instagram;
            whatsapp = adViewActivity.whatsapp;
            sms = adViewActivity.sms;
        }

        for (int i = 0; i < Config.SHARE_PACKAGES.length; i++) {

            switch (Config.SHARE_PACKAGES[i]) {
                case Constants.FACEBOOK:
                    setVisibilityShareAdIcons(Constants.FACEBOOK, facebook);
                    break;
                case Constants.INSTAGRAM:
                    if (!instagram)
                        setVisibilityShareAdIcons(Constants.INSTAGRAM, instagram);
                    else if (data != null && data.getImageCount() == 0) {
                        /** Hide Instagram icon when there is no image to share **/
                        setVisibilityShareAdIcons(Constants.INSTAGRAM, false);
                    }
                    break;
                case Constants.WHATSAPP:
                    setVisibilityShareAdIcons(Constants.WHATSAPP, whatsapp);
                    break;
            }
        }

        setVisibilityShareAdIcons(Constants.SMS, sms);
    }

    private void setVisibilityShareAdIcons(String packageName, boolean status) {
        /** Hide share icons when there is no package installed **/
        View view = getView();
        switch (packageName) {
            case Constants.FACEBOOK:
                if (!status && facebookBottom != null) {
                    facebookBottom.setVisibility(View.GONE);
                }
                break;
            case Constants.INSTAGRAM:
                if (!status && instagramBottom != null) {
                    instagramBottom.setVisibility(View.GONE);
                }
                break;
            case Constants.WHATSAPP:
                if (!status && whatsappBottom != null) {
                    whatsappBottom.setVisibility(View.GONE);
                }
                break;
            case Constants.SMS:
                if (!status && smsBottom != null) {
                    smsBottom.setVisibility(View.GONE);
                }
                break;
        }
    }

    public void updateAdNotExistUI(int listId) {
        Log.d();
        View view = getView();
        View adNotExist = (View) view.findViewById(R.id.ad_not_exist);
        if (adNotExist != null) {
            adNotExist.setVisibility(View.VISIBLE);
        }
        AdViewAd acAd = new AdViewAd();
        acAd.setListId(listId);
        acAd.setIsDeletedAd(true);
        setData(acAd);
    }

    private BlocketLoader.Callbacks newAdViewCallback() {
        Log.d("calling data for adPosition: " + adPosition);
        HashMap<String, Object> p = new HashMap<String, Object>();
        p.put(EXTRA_LIST_ID, listId);
        return new BlocketLoader.Callbacks(Method.GET, "view", p, getActivity()) {
            @Override
            public void onLoadComplete(BlocketLoader loader, JSONObject data) {
                Log.d("adPosition: " + adPosition);
                if (!isAdded() || isRemoving()) {
                    Crashlytics.getInstance().core.setString("Adview", "adPostion: " + adPosition + ", isAdded(): " + isAdded() + ", isRemoving(): " + isRemoving());
                    return;
                }
                int currPosition = getCurrentPosition();
                boolean currentAd = isCurrentViewingAd(currPosition);

                if (data != null && data.optJSONObject("ad") != null) {
                    int gap = Math.abs(adPosition - currPosition);
                    Log.d("adPosition: " + adPosition + ", gap= " + gap + ", currentAd=" + currentAd);
                    //When the result comes back, user may not viewing this page any more.
                    //Need to verify and only proceed if the adPosition is within the OFF_PAGE_LIMIT
                    if (isVisibleToUser || gap <= AdViewActivity.OFF_PAGE_LIMIT) {
                        AdViewAd acAd = new AdViewAd(data);
                        acAd.setListId(listId);
                        setData(acAd);
                        updateUI();
                        pbLoading.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        vConnectionLost.setVisibility(View.GONE);
                    } else
                        Log.d("off screen, do nothing");
                } else if (data != null && data.optJSONObject("error") != null
                        && data.optJSONObject("error").optString("code") != null
                        && data.optJSONObject("error").optString("code").equals(AD_NOT_EXIST_CODE)) {
                    updateAdNotExistUI(listId);
                    EventTrackingUtils.sendClick(XitiUtils.LEVEL2_OTHERS_DEFAULT, XitiUtils.NO_AD_FOUND, XitiUtils.NAVIGATION);
                    if (getActivity() instanceof AdViewActivity) {
                        ((AdViewActivity) getActivity()).updateFavouriteStatus(listId);
                    }
                    vConnectionLost.setVisibility(View.GONE);
                    pbLoading.setVisibility(View.GONE);
                } else {
                    onLoadError(loader, data);
                }
                //Adview.updateUI will be called in Adview's onPageScrollStateChanged
                //However, for the first ad, onPageScrollStateChanged has already been called before this onLoadComplete.
                //As a result, we need to manually call Adview.updateUI
                if (currentAd && (getActivity() instanceof AdViewActivity)) {
                    AdViewActivity adView = ((AdViewActivity) getActivity());
                    adView.resetUpdateUIFlag();
                    adView.updateUI();
                }
            }

            @Override
            public void onLoadError(BlocketLoader loader, JSONObject data) {
                tagConnectionLost(data);
                vConnectionLost.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                vConnectionLost.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);
                getLoaderManager().destroyLoader(loader.getId());
            }

        };
    }

    private void tagConnectionLost(JSONObject data) {
        if (getActivity() instanceof AdViewActivity) {
            ((AdViewActivity) getActivity()).tagConnectionLost(ADVIEW_CONNECTION_LOST, data);
        }
    }

    private int getCurrentPosition() {
        int currPosition = 0;
        if (getActivity() instanceof AdViewActivity) {
            currPosition = ((AdViewActivity) getActivity()).getCurrentPosition();
        }
        return currPosition;
    }

    private boolean isCurrentViewingAd(int currPosition) {
        return (adPosition == currPosition);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        int gap = Math.abs(adPosition - getCurrentPosition());
        if (scrollView != null && gap > AdViewActivity.OFF_PAGE_LIMIT) {
            ACUtils.unbindDrawables(scrollView);
        }

        if (vConnectionLost != null) {
            ACUtils.unbindDrawables(vConnectionLost);
        }

        if (recommendedAdList != null) {
            recommendedAdList = null;
        }
        System.gc();
        //A memory leak detection
        MudahUtil.detectMemoryLeakForFragment(getActivity(), this);
    }

    private void showRecommendedList() {
        recommendedAdList.setAdapter(new AdViewRecommendedAdAdapter(getActivity(), recommendedItems));
        recommendedAdList.setVisibility(View.VISIBLE);
        recommendedAdList.setOnItemClickListener(recommendedAdClickListener);
    }

    private static class GetSimilarRecommendationsAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<AdViewFragment> fragmentWeakReference;

        private GetSimilarRecommendationsAsyncTask(AdViewFragment adViewFragment) {
            this.fragmentWeakReference = new WeakReference<>(adViewFragment);
        }


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (fragmentWeakReference == null || fragmentWeakReference.get() == null)
                    return null;
                AdViewFragment adViewFragment = fragmentWeakReference.get();
                adViewFragment.recommendedItems = GravityUtils.getSimilarItem(GravityUtils.SCENARIO_ITEM_PAGE_SIMILAR_APP, adViewFragment.recommendedAdsLimit, Integer.toString(adViewFragment.listId));
                Log.d("Request similar items from gravity");
            } catch (Exception e) {
                Log.d("Fail to get similar items from gravity");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //if the activity is finishing or destroyed, do nothing
            if (fragmentWeakReference == null || fragmentWeakReference.get() == null) {
                return;
            }
            AdViewFragment adViewFragment = fragmentWeakReference.get();
            if (adViewFragment.getView() != null && adViewFragment.recommendedItems != null && adViewFragment.recommendedItems.size() > 0) {
                TextView recommendedAdTitle = (TextView) adViewFragment.getView().findViewById(R.id.tv_recommended_title);
                recommendedAdTitle.setVisibility(View.VISIBLE);
                adViewFragment.showRecommendedList();
            }
        }
    }
}
