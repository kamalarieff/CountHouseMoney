package com.mudah.my.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.lib701.utils.ACUtils;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.activities.AdsListActivity;
import com.mudah.my.activities.HomepageActivity;
import com.mudah.my.activities.InsertAdActivity;
import com.mudah.my.adapters.BannerFragmentAdapter;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.Banner;
import com.mudah.my.utils.EventTrackingUtils;
import com.squareup.picasso.Picasso;

import java.io.StringReader;

public final class CircleIndicatorFragment extends Fragment {
    private static final String BANNER = "HP Banner";
    private static final String CONTENT = "savedContent";
    private static final String INSERT_AD = "insert_ad";
    private Banner mContent = new Banner();
    private Gson gson = new Gson();
    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent;
            String query = mContent.getQuery();
            if (INSERT_AD.equalsIgnoreCase(query)) {
                intent = new Intent(getActivity(), InsertAdActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else {
                String filter = mContent.getFilter();
                intent = new Intent(getActivity(), AdsListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(AdsListActivity.QUERY, query);
                intent.putExtra(AdsListActivity.FILTER, filter);
                intent.putExtra(HomepageActivity.BANNER_LINK, true);
            }
            EventTrackingUtils.sendClick(XitiUtils.LEVEL2_HOMEPAGE_ID, BANNER + mContent.getPosition() + XitiUtils.CHAPTER_SIGN + mContent.getName(), XitiUtils.NAVIGATION);

            startActivity(intent);
        }
    };

    public static CircleIndicatorFragment newInstance(Banner banner) {
        CircleIndicatorFragment fragment = new CircleIndicatorFragment();
        fragment.mContent = banner;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String strContent = savedInstanceState.getString(CONTENT, Constants.EMPTY_STRING);
            if (!ACUtils.isEmpty(strContent)) {
                JsonReader reader = new JsonReader(new StringReader(strContent));
                reader.setLenient(true);
                mContent = gson.fromJson(reader, Banner.class);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String jsonContent = gson.toJson(mContent);
        savedInstanceState.putString(CONTENT, jsonContent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.banner_image, container, false);
        ImageView banner = (ImageView) rootView.findViewById(R.id.iv_banner);
        TextView desc = (TextView) rootView.findViewById(R.id.tv_banner_desc);
        if (mContent != null) {
            desc.setText(mContent.getDescription());
        }

        if (mContent == null || BannerFragmentAdapter.DEFAULT_BANNER.equals(mContent.getImageUrl()) || ACUtils.isEmpty(mContent.getImageUrl())) {
            Picasso.with(getActivity())
                    .load(BannerFragmentAdapter.DEFAULT_BANNER_DRAWABLE)
                    .priority(Picasso.Priority.HIGH)
                    .into(banner);
        } else {
            Picasso.with(getActivity())
                    .load(mContent.getImageUrl())
                    .fit()
                    .centerInside()
                    .priority(Picasso.Priority.HIGH)
                    .into(banner);
        }
        banner.setOnClickListener(clickListener);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getView() != null) {
            ACUtils.unbindDrawables(getView());
        }
    }

}
