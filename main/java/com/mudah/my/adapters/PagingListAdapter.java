package com.mudah.my.adapters;

import android.annotation.SuppressLint;
import android.widget.BaseAdapter;

import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.lib701.utils.ACUtils;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("NewApi")
public abstract class PagingListAdapter extends BaseAdapter {
    protected static final int TYPE_ITEM = 0;
    protected static final int TYPE_GOOGLEADS = 1;
    protected static final int TYPE_MAX_COUNT = TYPE_GOOGLEADS + 1;
    public HashMap<Integer, Object> googleAdHolder = new HashMap<Integer, Object>();
    protected boolean itemsCanClick = true;
    private LinkedList<JSONObject> items = new LinkedList<JSONObject>();
    private HashMap<String, Integer> orgPosition = new HashMap<String, Integer>();
    private int category = 0;

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public JSONObject getItem(int position) {
        if (position < items.size())
            return items.get(position);
        else
            return new JSONObject();
    }

    public int getOriginalPosition(String list_id) {
        return orgPosition.get(list_id);
    }

    public void setGoogleAds(int position, PublisherAdView adView) {
        googleAdHolder.put(position, adView);
    }

    public void setItemsCanClick(boolean itemsCanClick) {
        this.itemsCanClick = itemsCanClick;
    }

    public String getCategoryId(int position) {
        JSONObject item = getItem(position);
        return item.optString(Constants.CATEGORY_TXT);
    }

    public List<String> getAllListId() {
        List<String> list = new ArrayList<String>();
        int total = getCount();
        for (int i = 0; i < total; i++) {
            if (getListId(i) != null)
                list.add(getListId(i));
        }

        return list;
    }

    public String getListId(int position) {
        JSONObject item = getItem(position);
        try {
            if (item.has("list_id")) {
                return item.getString("list_id");
            } else {
                return null;
            }

        } catch (JSONException e) {
            ACUtils.debug(e);
            throw new RuntimeException(e);
        }
    }

    public void setCategory(int cat) {
        category = cat;
    }

    public PublisherAdView getGoogleAds(int position) {
        return (PublisherAdView) googleAdHolder.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(JSONArray itemsArray) {
        if (itemsArray == null) itemsArray = new JSONArray();
        int orgItemPosition = orgPosition.size();
        items.clear();
        for (int i = 0; i < itemsArray.length(); i++) {
            items.add(itemsArray.optJSONObject(i));
            if (Config.IS_GOOGLEAD_ENABLE) {
                orgPosition.put(itemsArray.optJSONObject(i).optString("list_id"), orgItemPosition);
                orgItemPosition++;
            }
        }

        notifyDataSetChanged();
    }

    public void addItems(JSONArray newItems) {
        if (newItems == null) return;
        if (items == null) items = new LinkedList<JSONObject>();
        int orgItemPosition = orgPosition.size();
        try {
            for (int i = 0; i < newItems.length(); i++) {
                items.add(newItems.getJSONObject(i));
                if (Config.IS_GOOGLEAD_ENABLE) {
                    orgPosition.put(newItems.optJSONObject(i).optString("list_id"), orgItemPosition);
                    orgItemPosition++;
                }
            }

            notifyDataSetChanged();
        } catch (JSONException e) {
            ACUtils.debug(e);
            throw new RuntimeException(e);
        }
    }

    public void addGoogleAds(JSONObject obj, int position) {
        if (obj == null) return;
        if (items == null) items = new LinkedList<JSONObject>();

        if (position > items.size()) {
            items.add(obj);
        } else {
            if (!googleAdHolder.containsKey(position))
                items.add(position, obj);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (googleAdHolder.containsKey(position)) {
            return TYPE_GOOGLEADS;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    public void changeDataSet() {
        notifyDataSetChanged();
    }

    public String getSubject(int position) {
        JSONObject item = getItem(position);
        return item.optString("subject");

    }
}
