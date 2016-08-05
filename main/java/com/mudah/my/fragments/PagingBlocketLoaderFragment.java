package com.mudah.my.fragments;

import android.os.Bundle;
import android.view.View;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.loaders.BlocketLoader;
import com.mudah.my.loaders.Method;

import org.json.JSONObject;

import java.util.Map;

public class PagingBlocketLoaderFragment extends BlocketLoaderFragment {
    public static final int FIRST_PAGE = 1;
    public static final int FIRST_OFFSET = 0;
    /**
     * The minimum amount of items to have below your current scroll position, before loading more.
     */
    public static final int VISIBLE_THRESHOLD = 10;
    protected int currentTotal = 0;
    protected int grandTotal = 0;
    private int currentPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setApi(Method method, String resource, Map<String, Object> params) {
        vLoading.setVisibility(View.VISIBLE);
        setApi(method, resource, params, FIRST_OFFSET);
    }

    @Override
    public void restartLoader() {
        super.restartLoader();
        currentPage = 0;
        currentTotal = 0;
        grandTotal = 0;
    }

    protected int getGrandTotal() {
        return grandTotal;
    }

    @Override
    protected void onConnectionRetry() {
        loadAndAppend();
    }

    @Override
    protected void onLoadComplete(BlocketLoader loader, JSONObject data) throws LoadException {
        super.onLoadComplete(loader, data);

        int page = (((Integer) loader.getParams().get("o")) / VISIBLE_THRESHOLD) + 1;
        if (currentPage < page) {
            currentPage = page;
            int total = 0;
            if (data.optJSONArray("ads") != null)
                total = data.optJSONArray("ads").length();

            String strGrandTotal = data.optString("filtered");
            if (!ACUtils.isEmpty(strGrandTotal))
                grandTotal = Integer.parseInt(strGrandTotal);
            else
                grandTotal = 0;

            currentTotal += total;
            Log.d("loaded " + currentTotal + "/" + grandTotal);
            if (total == 0 && currentTotal < grandTotal) {
                // currentTotal<grandTotal when finished loading all pages of
                // ads can happen
                // e.g. when an ad is deleted from the listing before loading
                // all pages of ads in the same listing
                // therefore: be more tolerant of an issue
                Log.d("current total < grand total");
                currentTotal = grandTotal;

            }
            setListShown(true);
        }
    }

    protected int getCurrentPage() {
        return currentPage;
    }

    private void loadAndAppend() {
        super.restartLoader(false);
    }

    public void setApi(Method method, String resource, Map<String, Object> params, int offset) {
        params.put("o", offset); // number of ads starts from 0
        params.put("limit", VISIBLE_THRESHOLD);
        super.setApi(method, resource, params);
    }

}

