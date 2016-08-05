package com.mudah.my.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lib701.datasets.ACCategory;
import com.mudah.my.R;
import com.mudah.my.viewholders.ACViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pin on 29/4/16.
 */

public class CategoriesExpandableListAdapter extends BaseExpandableListAdapter {
    private static final int[] EMPTY_STATE_SET = {};
    private static final int[] GROUP_EXPANDED_STATE_SET =
            {android.R.attr.state_expanded};
    private static final int[][] GROUP_STATE_SETS = {
            EMPTY_STATE_SET, // 0
            GROUP_EXPANDED_STATE_SET // 1
    };
    private Context context;
    private ArrayList<ACCategory> expandableListTitle;

    public CategoriesExpandableListAdapter(Context context, ArrayList<ACCategory> expandableListTitle) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
    }

    @Override
    public ACCategory getChild(int listPosition, int expandedListPosition) {
        return getGroup(listPosition).getVirtualCategories().get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = getChild(listPosition, expandedListPosition).getName();
        ACViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chooser_child_list_row, null);
            holder = new ACViewHolder();
            holder.subject = (TextView) convertView.findViewById(R.id.tv_list_row_text);

            convertView.setTag(holder);
        } else {
            holder = (ACViewHolder) convertView.getTag();
        }

        holder.subject.setText(expandedListText);

        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return getGroup(listPosition).getVirtualCategories().size();
    }

    @Override
    public ACCategory getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ACCategory category = getGroup(listPosition);
        ACViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chooser_list_group_row, null);

            holder = new ACViewHolder();
            holder.subject = (TextView) convertView.findViewById(R.id.tv_group_list_row_text);
            holder.image = (ImageView) convertView.findViewById(R.id.iv_list_arrow);
            convertView.setTag(holder);
        } else {
            holder = (ACViewHolder) convertView.getTag();
        }

        if (category != null) {
            holder.subject.setText(category.getName());
        }

        if (getChildrenCount(listPosition) == 0) {
            holder.image.setVisibility(View.INVISIBLE);
        } else {
            holder.image.setVisibility(View.VISIBLE);
            int stateSetIndex = (isExpanded ? 1 : 0);
            holder.image.getDrawable().setState(GROUP_STATE_SETS[stateSetIndex]);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}