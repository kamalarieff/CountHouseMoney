package com.mudah.my.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mudah.my.R;
import com.mudah.my.fragments.ListBookmarksFragment.ListMode;
import com.mudah.my.models.BookmarksModel;

import java.util.ArrayList;
import java.util.Iterator;

public class ListBookmarksAdapter extends BaseAdapter {


    private ArrayList<BookmarksModel> items = new ArrayList<BookmarksModel>();
    private ListMode listMode = ListMode.VIEW;

    public void setListMode(ListMode mode) {
        listMode = mode;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public BookmarksModel getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(16)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.list_bookmarks_item, parent, false);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.cbBookmark = (CheckBox) convertView.findViewById(R.id.cb_bookmark);
            holder.cbBookmark.setClickable(false);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BookmarksModel item = getItem(position);
        holder.tvName.setText((position + 1) + ". " + item.getName());
        holder.tvName.setTextColor(context.getResources().getColor(R.color.black));

        if (ListMode.VIEW != listMode) {
            holder.cbBookmark.setVisibility(View.VISIBLE);
            if (item.isSelected()) {
                holder.cbBookmark.setChecked(true);
                holder.tvName.setTextColor(context.getResources().getColor(R.color.selected_text));
            } else {
                holder.cbBookmark.setChecked(false);
                holder.tvName.setTextColor(context.getResources().getColor(R.color.black));
            }
        } else {
            holder.cbBookmark.setVisibility(View.GONE);
            item.setSelected(false);
        }
        return convertView;
    }

    public void setItems(ArrayList<BookmarksModel> items) {
        if (items == null) items = new ArrayList<BookmarksModel>();
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItem(BookmarksModel newItem) {
        items.add(newItem);
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
        Iterator<BookmarksModel> it = items.iterator();
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

    static class ViewHolder {
        TextView tvName;
        CheckBox cbBookmark;
    }
}
