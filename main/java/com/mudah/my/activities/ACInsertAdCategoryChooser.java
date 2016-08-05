package com.mudah.my.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.ACCategoriesAdapter;

import java.util.ArrayList;

public class ACInsertAdCategoryChooser extends ListActivity {

    private ACSettings settings;
    private ACReferences ref;
    private Bundle bundle;
    private ACCategoriesAdapter categoryAdapter;
    private ArrayList<ACCategory> categories;
    private TextView tvHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.default_list);
        //Assigning Header to the list view
        tvHeader = (TextView) findViewById(R.id.tv_list_header);
        tvHeader.append(getString(R.string.default_list_header_subcategory));

        Intent intent = this.getIntent();
        bundle = intent.getExtras();

        ref = ACReferences.getACReferences();
        settings = ACSettings.getACSettings();

        if (categories == null) {
            categories = new ArrayList<>(settings.getAcCategoriesDataSet().getCategoryGroups().get(bundle.getInt("insert_ad_category_group_id")).getCategories());
            categories.remove(categories.size()-1);
        }

        if (categories != null && categories.size() > 0) {
            categoryAdapter = new ACCategoriesAdapter(ACInsertAdCategoryChooser.this, R.layout.default_list_row_black_text, categories);
            setListAdapter(categoryAdapter);
        }
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        ref.setInsertAdCategoryId(categories.get(position).getId());
        setResult(RESULT_OK);
        finish();
    }
}
