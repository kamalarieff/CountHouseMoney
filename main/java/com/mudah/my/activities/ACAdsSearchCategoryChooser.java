package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lib701.datasets.ACCategoriesDataSet;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACCategoryGroup;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.ACCategoriesAdapter;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;

import java.util.ArrayList;

public class ACAdsSearchCategoryChooser extends MudahBaseActivity {

    public ACCategoriesDataSet acCategoriesDataSet;
    Bundle bundle;
    private ArrayList<ACCategory> categories;
    private ACReferences ref;
    private String categoryGroupId;
    private ActionBarHelper actionBar;
    private ListView listView;
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (categories.size() > 1 && position == (categories.size() - 1)) {
                // User choose "All" categories, have to give category_group id & name to category
                ref.categoryId = null;
                ref.categoryGroupId = categoryGroupId;
                ACCategoryGroup acCategoriesGrp = acCategoriesDataSet.findCategoryGroupById(categoryGroupId);
                ref.hasFilter = acCategoriesGrp.hasFilter();
                ref.defaultAdType = acCategoriesGrp.getDefaultAdType();
            } else {
                // User choose a normal category
                ACCategory category = categories.get(position);
                ref.categoryGroupId = categoryGroupId; // set in case of UI race error (user click button too fast, opens two dialogs, sets category_id thru one, but category_group_id thru another)
                ref.categoryId = category.getId();
                ref.hasFilter = category.hasFilter();
                ref.defaultAdType = category.getDefaultAdType();
            }
            //reset some variables
            ref.extraQuery = null;
            ref.filterParams = null;
            setResult(RESULT_OK);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();

        Log.d();

        setContentView(R.layout.chooser_list);
        listView = (ListView) findViewById(R.id.chooser_list);
        listView.setOnItemClickListener(itemClickListener);
        actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.launch_category_title));
        Intent myIntent = getIntent();
        categoryGroupId = myIntent.getStringExtra(ACAdsSearchCategoryGroupChooser.CATEGORY_GROUP_ID);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d();

        Intent intent = getIntent();
        bundle = intent.getExtras();

        ref = ACReferences.getACReferences();
        ACSettings acSettings = ACSettings.getACSettings();
        if (acSettings != null)
            acCategoriesDataSet = acSettings.getAcCategoriesForSearchDataSet();

        if (acCategoriesDataSet != null && ref != null && categoryGroupId != null) {
            ACCategoryGroup acCategoriesGrp = acCategoriesDataSet.findCategoryGroupById(categoryGroupId);

            if (acCategoriesGrp != null) {
                categories = new ArrayList<ACCategory>(acCategoriesGrp.getCategories());
                actionBar.setMainTitle(acCategoriesGrp.getName());
            }
            if (categories != null && categories.size() == 2) {
                // remove "All" if categories are only "All", and one other
                if ((Constants.ALL_CATEGORY).equalsIgnoreCase(categories.get(1).getId()))
                    categories.remove(1);
            } else if (categories == null) {
                categories = new ArrayList<ACCategory>();
            }

            ACCategoriesAdapter categoryAdapter = new ACCategoriesAdapter(this, R.layout.chooser_list_row, categories);
            listView.setAdapter(categoryAdapter);
            listView.setTextFilterEnabled(true);
        }

        Log.d("acCategoriesDataSet= " + acCategoriesDataSet + ", ref= " + ref + ", category_group_id= " + categoryGroupId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d();
    }

}
