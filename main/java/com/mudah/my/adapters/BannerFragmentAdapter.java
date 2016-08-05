package com.mudah.my.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mudah.my.R;
import com.mudah.my.fragments.CircleIndicatorFragment;
import com.mudah.my.models.Banner;

import org.json.JSONArray;
import org.json.JSONObject;

public class BannerFragmentAdapter extends FragmentPagerAdapter {
    public static final String DEFAULT_BANNER = "R.drawable.homepage_banner_1";
    public static final int DEFAULT_BANNER_DRAWABLE = R.drawable.homepage_banner_1;
    private static final String NAME = "name";
    private static final String QUERY = "query";
    private static final String FILTER = "filter";
    private static final String IMAGE = "image";
    private static final String DESC = "description";
    private static final String DEFAULT_DESC = "Great deals on luxury cars";
    private static final String DEFAULT_QUERY = "st=s&cg=1020";
    private static final String DEFAULT_FILTER = "pricelist=19-";
    private Banner[] items;

    public BannerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return CircleIndicatorFragment.newInstance(items[position]);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    public void setItems(JSONArray banners) {

        if (banners != null && banners.length() > 0) {
            int bannerSize = banners.length();
            items = new Banner[bannerSize];
            for (int i = 0; i < bannerSize; i++) {
                Banner banner = new Banner();
                banner.setPosition(i + 1);
                JSONObject eachBanner = banners.optJSONObject(i);
                if (eachBanner != null) {
                    banner.setName(eachBanner.optString(NAME));
                    banner.setQuery(eachBanner.optString(QUERY));
                    banner.setFilter(eachBanner.optString(FILTER));
                    banner.setImageUrl(eachBanner.optString(IMAGE));
                    banner.setDescription(eachBanner.optString(DESC));
                } else {
                    //something wrong with the banner data, just use the default one
                    initDefaultBanner();
                    break;
                }
                items[i] = banner;
            }
        } else {
            initDefaultBanner();
        }
        notifyDataSetChanged();
    }

    private void initDefaultBanner() {
        items = new Banner[1];
        Banner banner = new Banner();
        banner.setPosition(1);
        banner.setImageUrl(DEFAULT_BANNER);
        banner.setDescription(DEFAULT_DESC);
        banner.setQuery(DEFAULT_QUERY);
        banner.setFilter(DEFAULT_FILTER);
        items[0] = banner;
    }
}