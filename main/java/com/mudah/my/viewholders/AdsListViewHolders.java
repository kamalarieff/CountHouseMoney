package com.mudah.my.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mudah.my.R;

/**
 * Created by pin on 3/1/16.
 */
public class AdsListViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView tvImageCount;
    public TextView tvSubject;
    public TextView tvPrice;
    public TextView tvPosted;
    public ImageView ivCompanyAd;
    public ImageView ivGrid;
    public ImageView imvFavourite;
    //list view
    public TextView tvCompanyAd;
    public ImageView ivThumb;
    private OnItemClickListener parentListener;

    public AdsListViewHolders(View itemView, OnItemClickListener listener) {
        super(itemView);
        itemView.setOnClickListener(this);
        tvSubject = (TextView) itemView.findViewById(R.id.tv_subject);
        tvPrice = (TextView) itemView.findViewById(R.id.tv_price);
        ivCompanyAd = (ImageView) itemView.findViewById(R.id.iv_company_ad);
        tvPosted = (TextView) itemView.findViewById(R.id.tv_posted);
        ivGrid = (ImageView) itemView.findViewById(R.id.imv_grid);
        imvFavourite = (ImageView) itemView.findViewById(R.id.imv_favourite);
        tvImageCount = (TextView) itemView.findViewById(R.id.tv_image_count);
        //List View
        ivThumb = (ImageView) itemView.findViewById(R.id.iv_thumb);
        tvCompanyAd = (TextView) itemView.findViewById(R.id.tv_company_ad);

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