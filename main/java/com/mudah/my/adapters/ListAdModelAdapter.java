package com.mudah.my.adapters;

import android.content.Context;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib701.adapters.MediaPagerAdapter;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.fragments.ListAdViewFavouritesFragment;
import com.mudah.my.fragments.ListAdViewFavouritesFragment.ListMode;
import com.mudah.my.models.ListAdModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;

public class ListAdModelAdapter extends BaseAdapter {

    private static int[] aCatImages = {R.drawable.cat_property,
            R.drawable.cat_vehicle,
            R.drawable.cat_homefurniture,
            R.drawable.cat_hobbysport,
            R.drawable.cat_electronics,
            R.drawable.cat_jobs,
            R.drawable.cat_others,
            R.drawable.cat_b2b,
            R.drawable.cat_travel
    };
    private boolean itemsCanClick = true;
    private ArrayList<ListAdModel> items = new ArrayList<ListAdModel>();
    private ArrayList<String> itemIds = new ArrayList<String>();
    private ListMode listMode = ListMode.VIEW;

    public void setListMode(ListMode mode) {
        listMode = mode;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ListAdModel getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        ViewHolder holder;
        int defaultImgCnt = aCatImages.length;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.ads_item, parent, false);

            holder.adsLayout = (RelativeLayout) convertView.findViewById(R.id.view_holder);

            holder.tvSubject = (TextView) convertView.findViewById(R.id.tv_subject);
            holder.tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
            holder.tvPosted = (TextView) convertView.findViewById(R.id.tv_posted);
            holder.tvCompanyAd = (TextView) convertView.findViewById(R.id.tv_company_ad);

            holder.rlThumbHolder = (RelativeLayout) convertView.findViewById(R.id.rl_thumb_holder);
            holder.ivThumb = (ImageView) convertView.findViewById(R.id.iv_thumb);
            holder.ivCompanyAd = (ImageView) convertView.findViewById(R.id.iv_company_ad);

            holder.tvImageCount = (TextView) convertView.findViewById(R.id.tv_image_count);
            holder.aBitmap = new SparseArray<Integer>();
            for (int i = 0; i < defaultImgCnt; i++) {
                holder.aBitmap.put(i, aCatImages[i]);
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListAdModel item = getItem(position);

        if (!ACUtils.isEmpty(item.getAdSubject())) {
            holder.tvSubject.setText(item.getAdSubject());
        } else {
            holder.tvSubject.setText(R.string.ads_no_subject);
        }

        holder.tvPrice.setText(item.getAdPrice());
        if (!ACUtils.isEmpty(item.getAdTime())) {
            holder.tvPosted.setText(item.getAdTime());
        } else {
            holder.tvPosted.setText("");
        }


        final ImageView ivTmp = holder.ivThumb;
        String thumbUrl = item.getAdImgUrl();
        if (Config.imageMode) {
            holder.rlThumbHolder.setVisibility(View.VISIBLE);
            if (!ACUtils.isEmpty(thumbUrl) && !MediaPagerAdapter.DEFAULT_NO_IMAGE.equalsIgnoreCase(thumbUrl)) {
                Picasso.with(context).load(thumbUrl).fit().centerCrop().into(ivTmp);
            } else {
                // get parent category, e.g. 3060 / 1000 = 3 (int)
                int i = item.getAdCategoryId() / 1000;
                int res = R.drawable.cat_others;
                if (i > 0 && holder.aBitmap != null && holder.aBitmap.size() >= i)
                    res = holder.aBitmap.get(i - 1);

                Picasso.with(context).load(res).fit().centerCrop().into(ivTmp);
            }
        } else {
            holder.rlThumbHolder.setVisibility(View.GONE);
        }

        if (!ACUtils.isEmpty(item.getCompanyAd())) {
            if (item.getCompanyAd().equalsIgnoreCase(Constants.AD_SRC_COMPANY)) {
                Picasso.with(context).load(R.drawable.company).into(holder.ivCompanyAd);
                holder.tvCompanyAd.setText(Constants.COMPANY);
            } else {
                Picasso.with(context).load(R.drawable.private_icon).into(holder.ivCompanyAd);
                holder.tvCompanyAd.setText(Constants.PRIVATE);
            }
        }

        displayImageCount(item.getImgCount(), holder.tvImageCount);

        if (ListAdViewFavouritesFragment.ListMode.VIEW != listMode) {
            if (item.isSelected()) {
                setSelectedViewBackground(context, convertView);
            } else {
                removeViewBackground(context, convertView);
            }
        } else {
            item.setSelected(false);
            removeViewBackground(context, convertView);
        }
        return convertView;
    }

    private void displayImageCount(int imageCount, TextView tvImageCount) {
        if (imageCount > 0) {

            tvImageCount.setText(imageCount + Constants.EMPTY_STRING);

            //no need to show image count for 0 and 1
            if (imageCount < 2)
                tvImageCount.setVisibility(View.GONE);
            else
                tvImageCount.setVisibility(View.VISIBLE);

        } else {
            tvImageCount.setVisibility(View.GONE);
        }
    }

    private void removeViewBackground(Context context, View v) {
        if (Build.VERSION.SDK_INT < 19) {
            v.setBackgroundColor(context.getResources().getColor(R.color.white));
        } else
            v.setBackgroundResource(0);
    }

    private void setSelectedViewBackground(Context context, View v) {
        if (Build.VERSION.SDK_INT < 19) {
            v.setBackgroundColor(context.getResources().getColor(R.color.selected_box_background));
        } else
            v.setBackground(context.getResources().getDrawable(R.drawable.bg_selected_row));
    }

    public void setItems(ArrayList<ListAdModel> items) {
        if (items == null) items = new ArrayList<ListAdModel>();
        this.items = items;
        itemIds.clear();
        for (ListAdModel item : this.items) {
            itemIds.add(item.getAdId());
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyDataSetChanged();
    }

    public void removeAllItems() {
        items.clear();
        notifyDataSetChanged();
    }

    public void removeMultipleItems(SparseArray<Integer> positionsToRemove) {
        Iterator<ListAdModel> it = items.iterator();
        int position = 0;
        while (it.hasNext()) {
            it.next();
            if (positionsToRemove.get(position) != null) {
                it.remove();
            }
            position++;
        }

        notifyDataSetChanged();
    }

    public ArrayList<String> getItemIds() {
        itemIds.clear();
        for (ListAdModel item : this.items) {
            itemIds.add(item.getAdId());
        }
        return itemIds;
    }

    public void setItemsLiveAds(JSONArray itemsArray) {
        try {
            if (itemsArray == null) itemsArray = new JSONArray();
            items.clear();
            for (int i = 0; i < itemsArray.length(); i++) {
                ListAdModel model = new ListAdModel(itemsArray.optString(i));
                items.add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Exception : " + e);
        }
    }

    static class ViewHolder {
        TextView tvImageCount;
        TextView tvSubject;
        TextView tvPrice;
        TextView tvPosted;
        TextView tvCompanyAd;
        ImageView ivThumb;
        ImageView ivCompanyAd;
        SparseArray<Integer> aBitmap;
        RelativeLayout adsLayout;
        RelativeLayout rlThumbHolder;
    }
}
