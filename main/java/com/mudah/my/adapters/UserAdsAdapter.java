package com.mudah.my.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.mudah.my.viewholders.AdsListViewHolders;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pin on 3/1/16.
 */
public class UserAdsAdapter extends RecyclerView.Adapter<AdsListViewHolders> {
    private static int[] aCatImages = {R.drawable.cat_vehicle,
            R.drawable.cat_property, R.drawable.cat_electronics,
            R.drawable.cat_homefurniture, R.drawable.cat_hobbysport,
            R.drawable.cat_b2b, R.drawable.cat_jobs, R.drawable.cat_others, R.drawable.cat_travel
    };

    private AdsListViewHolders.OnItemClickListener onItemClickListener;
    private ArrayList<JSONObject> items = new ArrayList<>();
    private Context context;
    private boolean onBind;

    public UserAdsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public AdsListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ads_item, null);
        AdsListViewHolders viewHolder = new AdsListViewHolders(layoutView, onItemClickListener);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final AdsListViewHolders holder, int position) {
        onBind = true;
        final JSONObject item = getItem(position);

        holder.tvSubject.setText(getSubject(position));

        String price = item.optString(Constants.PRICE_KEY);
        if (ACUtils.isEmpty(price)) {
            holder.tvPrice.setVisibility(View.GONE);
        } else {
            holder.tvPrice.setText(price);
            holder.tvPrice.setVisibility(View.VISIBLE);
        }

        if (item.has(Constants.NAME_KEY) && item.has(Constants.FB_ID_KEY)) {
            holder.tvPosted.setText(context.getString(
                    R.string.ads_item_posted, item.optString(Constants.DATE_KEY),
                    item.optString(Constants.NAME_KEY)));
        } else {
            holder.tvPosted.setText(item.optString(Constants.DATE_KEY));
        }

        // get parent category, e.g. 3060 / 1000 = 3 (int)
        int parentCategory = item.optInt(Constants.CATEGORY_TXT) / 1000;

        if (item.has(Constants.COMPANY_AD) && (Constants.POSTED_BY_COMPANY_ID).equals(item.optString(Constants.COMPANY_AD))) {
            Picasso.with(context).load(R.drawable.icon_pro_ad).into(holder.ivCompanyAd);
            holder.tvCompanyAd.setText(Constants.COMPANY);
        } else {
            Picasso.with(context).load(R.drawable.icon_private_ad).into(holder.ivCompanyAd);
            holder.tvCompanyAd.setText(Constants.PRIVATE);
        }

        if (item.has(Constants.IMAGE_CNT_KEY)) {
            int numberOfImage = item.optInt(Constants.IMAGE_CNT_KEY);
            if (numberOfImage > 1) {
                holder.tvImageCount.setText(Constants.EMPTY_STRING + numberOfImage);
                holder.tvImageCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvImageCount.setVisibility(View.INVISIBLE);
            }
        }

        final ImageView ivTmp = holder.ivThumb;
        String thumbUrl = item.optString(Constants.IMAGE_KEY);
        if (thumbUrl.length() > 0) {
            Picasso.with(context).load(thumbUrl).fit().centerCrop().into(ivTmp);
        } else {
            int res = R.drawable.cat_others;
            if (parentCategory > 0 && aCatImages.length >= parentCategory)
                res = aCatImages[parentCategory - 1];

            Picasso.with(context).load(res).fit().centerCrop().into(ivTmp);
        }

        onBind = false;
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public JSONObject getItem(int position) {
        return items.get(position);
    }

    public ArrayList<String> getAllListId() {
        ArrayList<String> list = new ArrayList<>();
        int total = getItemCount();
        String listId;
        for (int i = 0; i < total; i++) {
            listId = getListId(i);
            if (!ACUtils.isEmpty(listId))
                list.add(listId);
        }

        return list;
    }

    public String getListId(int position) {
        JSONObject item = getItem(position);
        if (item != null) {
            return item.optString(Constants.LIST_ID_KEY);
        } else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(JSONArray itemsArray) {
        if (itemsArray == null) itemsArray = new JSONArray();
        items.clear();
        for (int i = 0; i < itemsArray.length(); i++) {
            items.add(itemsArray.optJSONObject(i));
        }
        notifyItemRangeInserted(0, itemsArray.length());
    }

    public void addItems(JSONArray newItems) {
        Log.d();
        if (newItems == null) return;
        if (items == null) items = new ArrayList<>();
        int positionStart = items.size();
        try {
            for (int i = 0; i < newItems.length(); i++) {
                items.add(newItems.getJSONObject(i));
            }
            notifyItemRangeInserted(positionStart, newItems.length());

        } catch (JSONException e) {
            ACUtils.debug(e);
            throw new RuntimeException(e);
        }
    }

    public void clearItems() {
        int itemCount = items.size();
        items.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    public String getSubject(int position) {
        return getItem(position).optString(Constants.SUBJECT_KEY);

    }

    public void setOnItemClickListener(AdsListViewHolders.OnItemClickListener listener) {
        onItemClickListener = listener;
    }
}
