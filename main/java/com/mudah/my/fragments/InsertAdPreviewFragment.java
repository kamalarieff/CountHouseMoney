package com.mudah.my.fragments;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableRow;

import com.lib701.widgets.UnderlinePageIndicator;
import com.mudah.my.R;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.activities.InsertAdActivity.Form;
import com.mudah.my.datasets.AdViewAd;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InsertAdPreviewFragment extends AdViewFragment implements Form {
    private CategoryParams categoryParams;
    private ProgressBar pbLoading;
    private ScrollView scrollView;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public InsertAdPreviewFragment() {
    }

    public static InsertAdPreviewFragment newInstance() {
        InsertAdPreviewFragment f = new InsertAdPreviewFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.insert_ad_preview, container,
                false);
        //Set pbLoading and scrollView to be hidden and showed in Preview
        // So that the data can be shown when we call AdViewFragment.updateUI()
        pbLoading = (ProgressBar) rootView.findViewById(R.id.adv_pb_loading);
        scrollView = (ScrollView) rootView.findViewById(R.id.sv_ad_view);

        mMedia = (ViewPager) rootView.findViewById(R.id.media);
        mIndicator = (UnderlinePageIndicator) rootView.findViewById(R.id.indicator);
        zoomIcon = (ImageView) rootView.findViewById(R.id.icon_zoom);
        return rootView;
    }

    @Override
    public Map<String, String> getState() {
        return new HashMap<>();
    }

    @Override
    public void setState(Map<String, String> curState) {
    }

    @Override
    public int setErrors(JSONObject errors) {
        return 0;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        pbLoading.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        Button bPrev = (Button) getView().findViewById(R.id.b_prev);
        bPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof InsertAdActivity) {
                    ((InsertAdActivity) getActivity()).prev();
                }
            }
        });

        Button bSubmit = (Button) getView().findViewById(R.id.b_submit);
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof InsertAdActivity) {
                    ((InsertAdActivity) getActivity()).submit();
                }
            }
        });

        //Hide bottom share icons in insert ad preview
        View shareBottomIcons = (View) getView().findViewById(R.id.tb_share_bottom);
        shareBottomIcons.setVisibility(View.GONE);
    }

    public void setPreviewParams(JSONObject previewParams) {
        if (categoryParams == null) {
            return;
        }
        AdViewAd ad = new AdViewAd(previewParams);
        setData(ad);
    }

    public void setCategoryParams(CategoryParams categoryParams) {
        this.categoryParams = categoryParams;
    }


    public static interface CategoryParams {
        public String getTypeName();

        public String getParamLabel(String paramName);

        public String getParamSuffix(String paramName);

        public JSONObject getParam(String paramName);
    }
}
