package com.mudah.my.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mudah.my.R;

/**
 * Created by pin on 3/1/16.
 */
public class CategoryGridHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView ivGrid;
    private OnItemClickListener parentListener;

    public CategoryGridHolder(View itemView, OnItemClickListener listener) {
        super(itemView);
        itemView.setOnClickListener(this);
        ivGrid = (ImageView) itemView.findViewById(R.id.imv_cat_grid);
        parentListener = listener;
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
        if (parentListener != null && position != RecyclerView.NO_POSITION) {
            parentListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}