package com.mudah.my.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib701.utils.ACUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.mudah.my.models.GravityModel;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pin on 6/3/16.
 */
public class AdViewRecommendedAdAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> recommendedAdList = new ArrayList<>();
    private List<GravityModel> gravityModelList = new ArrayList<>();

    public AdViewRecommendedAdAdapter(Context context, List<GravityModel> gravityModels) {

        this.context = context;
        this.gravityModelList = gravityModels;
        setAllAdIdList();
        inflater = LayoutInflater.from(context);
    }

    private void setAllAdIdList() {
        for (GravityModel gravityModel : gravityModelList) {
            recommendedAdList.add(gravityModel.getItemId());
        }
    }

    public ArrayList<String> getAllAdIdList() {
        return recommendedAdList;
    }

    @Override
    public int getCount() {
        return gravityModelList.size();
    }

    @Override
    public GravityModel getItem(int position) {
        if (position < getCount())
            return gravityModelList.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (null == convertView) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.recommended_ads_item, parent, false);
            holder.ivImage = (ImageView) convertView.findViewById(R.id.iv_thumb);
            holder.tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final GravityModel gravityModel = getItem(position);
        holder.tvTitle.setText(gravityModel.getTitle());

        if (!ACUtils.isEmpty(gravityModel.getPrice()))
            holder.tvPrice.setText(Constants.RM_PREFIX + gravityModel.getPrice());

        loadImage(gravityModel.getImageUrl(), holder.ivImage);

        return convertView;
    }

    public void loadImage(String thumbUrl, ImageView imageView) {

        if (!ACUtils.isEmpty(thumbUrl)) {
            Picasso.with(context)
                    .load(thumbUrl)
                    .resizeDimen(R.dimen.adview_recommend_ad_weight, R.dimen.adview_recommend_ad_height)
                    .centerCrop()
                    .onlyScaleDown()
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .into(imageView);
        } else {
            Picasso.with(context)
                    .load(R.drawable.cat_others)
                    .into(imageView);
        }
    }

    static class ViewHolder {
        ImageView ivImage;
        TextView tvPrice;
        TextView tvTitle;
    }
}