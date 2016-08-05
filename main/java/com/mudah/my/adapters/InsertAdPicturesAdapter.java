package com.mudah.my.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.activities.InsertAdActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertAdPicturesAdapter extends BaseAdapter implements InsertAdActivity.Form {
    private static final int DEFAULT_MAX_COUNT = 3;
    private List<String> imageURLs = new ArrayList<String>(DEFAULT_MAX_COUNT);
    private List<String> imageDigests = new ArrayList<String>(DEFAULT_MAX_COUNT);
    private int maxCount = DEFAULT_MAX_COUNT;
    private int maxDraw = maxCount;

    @Override
    public int getCount() {
        return maxDraw;
    }

    @Override
    public Object getItem(int position) {
        if (position < imageURLs.size())
            return imageURLs.get(position);
        else
            return "";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        View view;
        if (position <= getRealCount()) {
            view = LayoutInflater.from(context).inflate(R.layout.insert_ad_picture, null);
            TextView tvLabel = (TextView) view.findViewById(R.id.tv_label);
            final ImageView ivThumb = (ImageView) view.findViewById(R.id.iv_thumb);
            if (position < getRealCount()) {
                final String imageUrl = imageURLs.get(position);

                Picasso.with(context).load(imageUrl).into(ivThumb, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing
                    }

                    @Override
                    public void onError() {
                        Log.e(imageUrl + " cannot be loaded.");
                        Picasso.with(context).cancelRequest(ivThumb);
                        Toast.makeText(context, context.getString(R.string.insert_ad_picture_load_error, position + 1), Toast.LENGTH_SHORT).show();
                        removeItem(position);
                    }
                });
                tvLabel.setText(R.string.insert_ad_picture_edit_picture);
            } else {
                Picasso.with(context).load(R.drawable.ai_camera).into(ivThumb);
                tvLabel.setText(R.string.insert_ad_picture_add_picture);
            }
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.insert_ad_picture_empty, null);
        }
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        //return true;
        return (position <= getRealCount());
    }

    @Override
    public Map<String, String> getState() {
        Map<String, String> params = new HashMap<String, String>();
        if (imageURLs.size() == imageDigests.size()) {
            for (int i = 0; i < imageURLs.size(); i++) {
                params.put("image_url" + i, imageURLs.get(i));
                params.put("thumbnail_digest" + i, imageDigests.get(i));
            }
        }
        return params;
    }

    @Override
    public void setState(Map<String, String> params) {
        imageURLs.clear();
        imageDigests.clear();
        int i = 0;
        while (params.containsKey("image_url" + i)) {
            String id = params.get("image_url" + i);
            String digestID = params.get("thumbnail_digest" + i);
            if (id != null && digestID != null) {
                imageURLs.add(id);
                imageDigests.add(digestID);
            }
            i++;
        }
        updateMaxDraw();
        notifyDataSetChanged();
    }

    @Override
    public int setErrors(JSONObject errors) {
        // should never have errors
        return 0;
    }

    public int getRealCount() {
        return imageURLs.size();
    }

    public void addItem(String imageId, String imageDigest) {
        imageURLs.add(imageId);
        imageDigests.add(imageDigest);
        updateMaxDraw();
        notifyDataSetChanged();
    }

    public void setItem(int position, String imageId, String imageDigest) {
        imageURLs.set(position, imageId);
        imageDigests.set(position, imageDigest);
        updateMaxDraw();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position < getRealCount()) {
            Log.d("Delete image at position " + position + " from total " + getRealCount());
            imageURLs.remove(position);
            imageDigests.remove(position);
        }
        updateMaxDraw();
        notifyDataSetChanged();
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;

        while (getRealCount() > getMaxCount()) {
            removeItem(getRealCount() - 1);
        }
        updateMaxDraw();
        notifyDataSetChanged();
    }

    public void updateMaxDraw() {
        if (getRealCount() < maxCount)
            this.maxDraw = getRealCount() + 1;
        else
            this.maxDraw = getRealCount();
    }

}
