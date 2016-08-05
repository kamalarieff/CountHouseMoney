package com.mudah.my.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mudah.my.R;
import com.mudah.my.viewholders.CategoryGridHolder;
import com.squareup.picasso.Picasso;

/**
 * Created by pin on 3/1/16.
 */
public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridHolder> {
    public static String ICON_MODE = "icon";
    public static String PHOTO_MODE = "photo";
    public static String[] aCatId = {"1020",
            "3020", "4200",
            "5080", "5000",
            "4020", "4100", "4300", "7020",
            "2000", "4060", ""
    };
    public static String[] aCatName = {"cars",
            "mobile phones gadgets", "home appliances",
            "pets", "leisure/sports/hobbies",
            "furniture decoration", "womens collections", "mens collections", "jobs",
            "properties", "moms kids", ""
    };
    private static int[] categoryIconImages = {R.drawable.ctgr_car_icon,
            R.drawable.ctgr_phones_icon, R.drawable.ctgr_homeapl_icon,
            R.drawable.ctgr_pet_icon, R.drawable.ctgr_sports_icon,
            R.drawable.ctgr_furniture_icon, R.drawable.ctgr_womans_icon, R.drawable.ctgr_mens_icon, R.drawable.ctgr_jobs_icon,
            R.drawable.ctgr_properties_icon, R.drawable.ctgr_momkids_icon, R.drawable.ctgr_more_icon
    };
    private static int[] categoryPhotoImages = {R.drawable.ctgr_car_photo,
            R.drawable.ctgr_phones_photo, R.drawable.ctgr_homeapl_photo,
            R.drawable.ctgr_pet_photo, R.drawable.ctgr_sports_photo,
            R.drawable.ctgr_furniture_photo, R.drawable.ctgr_womans_photo, R.drawable.ctgr_mens_photo, R.drawable.ctgr_jobs_photo,
            R.drawable.ctgr_properties_photo, R.drawable.ctgr_momkids_photo, R.drawable.ctgr_more_photo
    };

    private String categoryModeVariable;
    private CategoryGridHolder.OnItemClickListener onItemClickListener;
    private Context context;

    public CategoryGridAdapter(Context context, String mode) {
        this.context = context;
        this.categoryModeVariable = mode;
    }

    @Override
    public CategoryGridHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_grid_item, null);
        CategoryGridHolder viewHolder = new CategoryGridHolder(layoutView, onItemClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CategoryGridHolder holder, int position) {
        //Load grid image
        Picasso.with(context)
                .load(getCategoryImage(position))
                .tag(context)
                .into(holder.ivGrid);
    }

    private int getCategoryImage(int position) {
        if (PHOTO_MODE.equalsIgnoreCase(categoryModeVariable)) {
            return categoryPhotoImages[position];
        } else {
            return categoryIconImages[position];
        }
    }

    @Override
    public int getItemCount() {
        return this.categoryIconImages.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(CategoryGridHolder.OnItemClickListener listener) {
        onItemClickListener = listener;
    }

}
