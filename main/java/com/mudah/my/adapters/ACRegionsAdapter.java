package com.mudah.my.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lib701.datasets.ACRegion;
import com.mudah.my.R;
import com.mudah.my.viewholders.ACViewHolder;

import java.util.ArrayList;

public class ACRegionsAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<ACRegion> regions;
    private Context context;

    public ACRegionsAdapter(Context tmpContext, ArrayList<ACRegion> regions) {
        this.regions = regions;
        context = tmpContext;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ACViewHolder holder;
        if (convertView == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.default_list_row_black_text, null);

            holder = new ACViewHolder();
            holder.subject = (TextView) convertView.findViewById(R.id.region_list_row_title);

            convertView.setTag(holder);
        } else {
            holder = (ACViewHolder) convertView.getTag();
        }

        ACRegion region = regions.get(position);
        if (region != null) {
            holder.subject.setText(region.getName());
        }
        return convertView;
    }

    public void addRegions(ArrayList<ACRegion> regions) {
        this.regions = regions;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (regions != null)
            return regions.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}