package com.mudah.my.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lib701.datasets.ACCategory;
import com.mudah.my.R;
import com.mudah.my.viewholders.ACViewHolder;

import java.util.ArrayList;

public class ACCategoriesAdapter extends ArrayAdapter<ACCategory> {

    LayoutInflater layoutInflater;
    private ArrayList<ACCategory> categories;
    private Context context;

    public ACCategoriesAdapter(Context tmpContext, int textViewResourceId, ArrayList<ACCategory> arrayList) {
        super(tmpContext, textViewResourceId, arrayList);
        this.categories = arrayList;
        context = this.getContext();
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

        ACCategory category = categories.get(position);
        if (category != null) {
            holder.subject.setText(category.getName());
        }
        return convertView;
    }
}