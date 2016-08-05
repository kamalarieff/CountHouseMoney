package com.mudah.my.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by pin on 12/1/16.
 */
public class LinearLayoutPicasso extends LinearLayout implements Target {

    public LinearLayoutPicasso(Context context) {
        super(context);
    }

    public LinearLayoutPicasso(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutPicasso(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setBackground(new BitmapDrawable(getResources(), bitmap));
        } else {
            setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        //Set your error drawable
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        //Set your placeholder
    }
}
