package com.mudah.my.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lib701.datasets.ACCategoryGroup;
import com.mudah.my.R;
import com.mudah.my.viewholders.ACViewHolder;

import java.util.ArrayList;

public class ACCategoryGroupsAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<ACCategoryGroup> categoryGroups;
    private Context context;

    public ACCategoryGroupsAdapter(Context tmpContext, int textViewResourceId, ArrayList<ACCategoryGroup> arrayList) {
        this.categoryGroups = arrayList;
        context = tmpContext;
    }

    public void addCategoryGroups(ArrayList<ACCategoryGroup> category_groups) {
        this.categoryGroups = category_groups;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ACViewHolder holder;
        if (convertView == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chooser_list_row, null);

            holder = new ACViewHolder();
            holder.subject = (TextView) convertView.findViewById(R.id.tv_list_row_text);

            convertView.setTag(holder);
        } else {
            holder = (ACViewHolder) convertView.getTag();
        }

        ACCategoryGroup category_group = categoryGroups.get(position);
        if (category_group != null) {
            holder.subject.setText(category_group.getName().toString());
        }
        return convertView;
    }

    @Override
    public int getCount() {
        if (categoryGroups != null)
            return categoryGroups.size();
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