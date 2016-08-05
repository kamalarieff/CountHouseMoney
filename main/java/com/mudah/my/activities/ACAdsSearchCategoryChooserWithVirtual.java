package com.mudah.my.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.lib701.datasets.ACCategoriesDataSet;
import com.lib701.datasets.ACCategory;
import com.lib701.datasets.ACCategoryGroup;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.CategoriesExpandableListAdapter;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;

import java.util.ArrayList;
import java.util.List;

public class ACAdsSearchCategoryChooserWithVirtual extends MudahBaseActivity {

    public ACCategoriesDataSet acCategoriesDataSet;
    Bundle bundle;
    private ArrayList<ACCategory> categories;
    private ACReferences ref;
    private String categoryGroupId;
    private ActionBarHelper actionBar;
    private ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();

        Log.d();

        setContentView(R.layout.chooser_expand_list);
        actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.launch_category_title));
        Intent myIntent = getIntent();
        categoryGroupId = myIntent.getStringExtra(ACAdsSearchCategoryGroupChooser.CATEGORY_GROUP_ID);

        expandableListView = (ExpandableListView) findViewById(R.id.expandable_list);
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
                categories = new ArrayList<>(acCategoriesGrp.getCategories());
                actionBar.setMainTitle(acCategoriesGrp.getName());
            }
            if (categories != null && categories.size() == 2) {
                // remove "All" if categories are only "All", and one other
                if ((Constants.ALL_CATEGORY).equalsIgnoreCase(categories.get(1).getId()))
                    categories.remove(1);
            } else if (categories == null) {
                categories = new ArrayList<>();
            }

            setExpandableListViewAdapter();
        }

        Log.d("acCategoriesDataSet= " + acCategoriesDataSet + ", ref= " + ref + ", category_group_id= " + categoryGroupId);
    }

    private void setExpandableListViewAdapter() {
        CategoriesExpandableListAdapter expandableListAdapter = new CategoriesExpandableListAdapter(this, categories);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (!hasChilds(groupPosition)) {
                    onItemChosen(categories, groupPosition);
                }
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        categories.get(groupPosition).getName() + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                onItemChosen(categories.get(groupPosition).getVirtualCategories(), childPosition, groupPosition, true);
                return false;
            }
        });
    }

    private boolean hasChilds(int position) {
        return (categories.get(position).getVirtualCategories().size() > 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d();
    }

    private void onItemChosen(List<ACCategory> categoriesList, int position) {
        onItemChosen(categoriesList, position, -1, false);
    }

    private void onItemChosen(List<ACCategory> categoriesList, int position, int groupPosition, boolean isSubVirtualItem) {
        if (groupPosition < 0 )
            groupPosition = position;
        ACCategory groupCategory = categories.get(groupPosition);
        ref.extraQuery = groupCategory.getExtraQuery();
        if (categoriesList.size() > 1 && position == (categoriesList.size() - 1)) {
            // User choose "All" categories, have to give category_group id & name to category
            if (isSubVirtualItem) {
                ref.categoryId = groupCategory.getId();
            } else {
                ref.categoryId = null;
            }
            ref.categoryGroupId = categoryGroupId;
            ACCategoryGroup acCategoriesGrp = acCategoriesDataSet.findCategoryGroupById(categoryGroupId);
            ref.hasFilter = acCategoriesGrp.hasFilter();
            ref.defaultAdType = acCategoriesGrp.getDefaultAdType();
            ref.filterParams = null;
        } else {
            // User choose a normal category
            ACCategory category = categoriesList.get(position);
            ref.categoryGroupId = categoryGroupId; // set in case of UI race error (user click button too fast, opens two dialogs, sets category_id thru one, but category_group_id thru another)
            ref.categoryId = category.getId();
            ref.hasFilter = category.hasFilter();
            ref.filterParams = null;
            ref.defaultAdType = category.getDefaultAdType();
        }
        setResult(RESULT_OK);
        finish();
    }

}
