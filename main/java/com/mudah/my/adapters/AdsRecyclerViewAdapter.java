package com.mudah.my.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.utils.MudahUtil;
import com.mudah.my.viewholders.AdsListViewHolders;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pin on 3/1/16.
 */
public class AdsRecyclerViewAdapter extends RecyclerView.Adapter<AdsListViewHolders> {
    private static final int GRID_TYPE = 1;
    private static final int LIST_TYPE = 2;
    private static final String GRID_IMG = "grid_image";
    private static int[] aCatImages = {R.drawable.cat_vehicle,
            R.drawable.cat_property, R.drawable.cat_electronics,
            R.drawable.cat_homefurniture, R.drawable.cat_hobbysport,
            R.drawable.cat_b2b, R.drawable.cat_jobs, R.drawable.cat_others, R.drawable.cat_travel
    };
    private static int[] placeHolderGridImages = {R.drawable.cat_grid_vehicle,
            R.drawable.cat_grid_property, R.drawable.cat_grid_electronics,
            R.drawable.cat_grid_home, R.drawable.cat_grid_hobbies,
            R.drawable.cat_grid_b2b, R.drawable.cat_grid_jobs, R.drawable.cat_grid_others, R.drawable.cat_grid_travel
    };
    public HashMap<Integer, Object> googleAdHolder = new HashMap<Integer, Object>();
    private AdsListViewHolders.OnItemClickListener onItemClickListener;
    private OnFavItemClickListener onFavItemClickListener;
    private ArrayList<JSONObject> items = new ArrayList<>();
    private HashMap<String, Integer> orgPosition = new HashMap<>();
    private Context context;
    private boolean onBind;

    public AdsRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setOnFavItemClickListener(OnFavItemClickListener onFavItemClickListener) {
        this.onFavItemClickListener = onFavItemClickListener;
    }

    @Override
    public AdsListViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = null;

        switch (viewType) {
            case LIST_TYPE:
                layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ads_item, null);
                break;
            default://or  GRID_TYPE
                layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ads_grid_item, null);
                break;
        }

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
        } else {
            Picasso.with(context).load(R.drawable.icon_private_ad).into(holder.ivCompanyAd);
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



        if (Config.listViewMode == Config.ListViewMode.LIST_VIEW) {
            String thumbUrl = item.optString(Constants.IMAGE_KEY);

            if (thumbUrl.length() > 0) {
                //skip memory cache and disk cache
                Picasso.with(context)
                        .load(thumbUrl)
                        .tag(context)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(holder.ivThumb);
            } else {
                int res = R.drawable.cat_others;
                if (parentCategory > 0 && aCatImages.length >= parentCategory)
                    res = aCatImages[parentCategory - 1];

                Picasso.with(context)
                        .load(res)
                        .fit()
                        .centerCrop()
                        .into(holder.ivThumb);
            }

            if (item.has(Constants.COMPANY_AD) && (Constants.POSTED_BY_COMPANY_ID).equals(item.optString(Constants.COMPANY_AD))) {
                holder.tvCompanyAd.setText(Constants.COMPANY);
            } else {
                holder.tvCompanyAd.setText(Constants.PRIVATE);
            }

        } else {
            String imgURL = Constants.EMPTY_STRING;

            if (!ACUtils.isEmpty(item.optString(GRID_IMG))) {
                imgURL = item.optString(GRID_IMG);
            } else if (!ACUtils.isEmpty(item.optString(Constants.IMAGE_KEY))) {
                imgURL = item.optString(Constants.IMAGE_KEY);
            }

            if (imgURL.length() > 0) {
                //Load grid image
                Picasso.with(context)
                        .load(imgURL)
                        .tag(context)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(holder.ivGrid);
            } else {
                int res = R.drawable.cat_grid_others;
                if (parentCategory > 0 && placeHolderGridImages.length >= parentCategory)
                    res = placeHolderGridImages[parentCategory - 1];

                //Load placeholder
                Picasso.with(context)
                        .load(res)
                        .fit()
                        .centerCrop()
                        .into(holder.ivGrid);
            }

            Picasso.with(context).load(MudahUtil.isFavouritedAd(item.optInt(Constants.LIST_ID_KEY)) ?
                    R.drawable.ic_action_heart_red_grid :
                    R.drawable.ic_action_heart_grid).into(holder.imvFavourite);

            if (onFavItemClickListener != null) {
                holder.imvFavourite.setOnClickListener(onFavItemClickListener);
                holder.imvFavourite.setTag(holder);
            }
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

    public int getOriginalPosition(String list_id) {
        return orgPosition.get(list_id);
    }

    public String getCategoryId(int position) {
        JSONObject item = getItem(position);
        return item.optString(Constants.CATEGORY_TXT);
    }

    public ArrayList<String> getAllListIdFromOffset(int offset) {
        ArrayList<String> list = new ArrayList<>();
        int total = getItemCount();
        String listId;
        for (int i = offset; i < total; i++) {
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

    public PublisherAdView getGoogleAds(int position) {
        return (PublisherAdView) googleAdHolder.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearItems() {
        int itemCount = items.size();
        items.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    public void setItems(JSONArray itemsArray) {
        if (itemsArray == null) itemsArray = new JSONArray();
        int orgItemPosition = orgPosition.size();
        items.clear();
        for (int i = 0; i < itemsArray.length(); i++) {
            items.add(itemsArray.optJSONObject(i));
            if (Config.IS_GOOGLEAD_ENABLE) {
                orgPosition.put(itemsArray.optJSONObject(i).optString(Constants.LIST_ID_KEY), orgItemPosition);
                orgItemPosition++;
            }
        }
        notifyItemRangeInserted(0, itemsArray.length());
    }

    public void addItems(JSONArray newItems) {
        Log.d();
        if (newItems == null) return;
        if (items == null) items = new ArrayList<JSONObject>();
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

    @Override
    public int getItemViewType(int position) {
        if (Config.listViewMode == Config.ListViewMode.GRID_VIEW) {
            return GRID_TYPE;
        } else {
            return LIST_TYPE;
        }
    }

    public void changeDataSet() {
        notifyDataSetChanged();
    }

    public String getSubject(int position) {
        return getItem(position).optString(Constants.SUBJECT_KEY);
    }

    public void setOnItemClickListener(AdsListViewHolders.OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public interface OnFavItemClickListener extends View.OnClickListener {
        @Override
        void onClick(View v);
    }

}
