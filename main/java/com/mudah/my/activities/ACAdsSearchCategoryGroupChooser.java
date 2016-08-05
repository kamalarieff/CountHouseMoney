package com.mudah.my.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lib701.connection.ACBlocketConnection;
import com.lib701.datasets.ACCategoriesDataSet;
import com.lib701.datasets.ACCategoryGroup;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.ACCategoryGroupsAdapter;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;

import java.util.ArrayList;

public class ACAdsSearchCategoryGroupChooser extends MudahBaseActivity {
    private static final int SELECT_SUB_CAT_ID = 1;
    public static final String CATEGORY_GROUP_ID = "categoryGroupId";
    public ACCategoriesDataSet acCategoriesDataSet;
    private View footerLoadView;
    private ACCategoryGroupsAdapter categoryAdapter;
    private ArrayList<ACCategoryGroup> categoryGroups;
    private ACReferences ref;
    private ACSettings acSettings;
    private ListView listView;
    private boolean active = false;
    Handler fetchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == ACReferences.RESULT_OK) {
                ref = ACReferences.getACReferences();
                acSettings = ACSettings.getACSettings();
                if (acSettings != null && acCategoriesDataSet == null) {
                    acCategoriesDataSet = acSettings.getAcCategoriesForSearchDataSet();
                }

                if (acCategoriesDataSet != null)
                    categoryGroups = acCategoriesDataSet.getCategoryGroups();
                else// if null, create an empty list. However, this should not happen!
                    categoryGroups = new ArrayList<ACCategoryGroup>();

                categoryAdapter.addCategoryGroups(categoryGroups);
                if (listView != null) {
                    listView.removeFooterView(footerLoadView);
                }
            } else if (!isFinishing() && active == true) {
                Dialog dialog = new AlertDialog.Builder(ACAdsSearchCategoryGroupChooser.this)
                        .setIcon(R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dialog_error_load_error_title)
                        .setMessage(R.string.dialog_error_failed_to_load_categories)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_error_retry_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ACBlocketConnection.fetchRegions(ACAdsSearchCategoryGroupChooser.this, fetchHandler);
                                return;
                            }
                        })
                        .setNegativeButton(R.string.dialog_error_cancel_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                                return;
                            }
                        })
                        .create();
                dialog.show();
            }
        }
    };
    private boolean requestFromHomepage = false;
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            boolean isRedirected = false;
            if (ref == null) {
                setResult(RESULT_CANCELED);
                finish();
            } else if (categoryGroups != null && categoryGroups.get(position) != null) {
                ACCategoryGroup acCategoriesGrp = categoryGroups.get(position);
                String categoryGroupId = acCategoriesGrp.getId();
                if (categoryGroups.get(position).getCategories().size() > 0) {

                    Intent intent;
                    if (acCategoriesGrp.hasVirtualCategory()) {
                        intent = new Intent(getApplicationContext(), ACAdsSearchCategoryChooserWithVirtual.class);
                    } else {
                        intent = new Intent(getApplicationContext(), ACAdsSearchCategoryChooser.class);
                    }
                    intent.putExtra(CATEGORY_GROUP_ID, categoryGroupId);
                    startActivityForResult(intent, SELECT_SUB_CAT_ID);
                    isRedirected = true;
                } else if (!(Constants.ALL_CATEGORY).equalsIgnoreCase(categoryGroupId)) {
                    ref.categoryId = null;
                    ref.categoryGroupId = categoryGroupId;
                    ref.hasFilter = acCategoriesGrp.hasFilter();
                    ref.defaultAdType = acCategoriesGrp.getDefaultAdType();
                    isRedirected = true;
                    setResult(RESULT_OK);
                    redirectToListing();
                    finish();
                }
            }

            if (!isRedirected) {
                ref.categoryGroupId = null;
                ref.categoryId = null;
                ref.filterParams = null;
                ref.hasFilter = true;
                setResult(RESULT_OK);
                redirectToListing();
                finish();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d();
        // To avoid OutOfMemory, removes the reference to the activity, so that it can be garbage collected.
        // Ref: http://stackoverflow.com/questions/9536521/outofmemoryerror-when-loading-activities
        ACUtils.unbindDrawables(findViewById(R.id.ll_default_list));
        System.gc();
        active = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d();
        active = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d();
        active = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d();
        active = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        setContentView(R.layout.chooser_list);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.launch_category_title));

        footerLoadView = View.inflate(this, R.layout.loading_footer, null);
        //Assigning Header to the list view
        this.categoryAdapter = new ACCategoryGroupsAdapter(this, R.layout.chooser_list, categoryGroups);

        listView = (ListView) findViewById(R.id.chooser_list);
        listView.setTextFilterEnabled(true);
        listView.addFooterView(footerLoadView);
        listView.setAdapter(categoryAdapter);
        listView.setOnItemClickListener(itemClickListener);

        if (!ACReferences.categoriesFetched) {
            ACBlocketConnection.fetchCategories(ACAdsSearchCategoryGroupChooser.this, fetchHandler);
        } else {
            ref = ACReferences.getACReferences();
            acSettings = ACSettings.getACSettings();

            if (acCategoriesDataSet == null) {
                acCategoriesDataSet = acSettings.getAcCategoriesForSearchDataSet();
            }
            categoryGroups = acSettings.getAcCategoriesForSearchDataSet().getCategoryGroups();
            categoryAdapter.addCategoryGroups(categoryGroups);
            listView.removeFooterView(footerLoadView);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            requestFromHomepage = bundle.getBoolean(HomepageActivity.REQUEST_FROM_HOMEPAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //for first time user, access the app after the session has expired, redirect them to listing
        if (requestFromHomepage && resultCode == Activity.RESULT_OK) {
            redirectToListing();
            finish();
        }

        //if users change category in the listing page and result is ok, meaning users already chose the sub-category, then we can close this main category
        //otherwise, this mean users click back from sub-category page, leave this page open so that users can select a new main category.
        else if (resultCode == Activity.RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void redirectToListing() {
        Intent intent = new Intent(this, AdsListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (requestFromHomepage) {
            intent.putExtra(AdsListActivity.FUNCTION_REQUEST, AdsListActivity.REQUEST_CATEGORY_LOCATION);
        } else {
            intent.putExtra(AdsListActivity.FUNCTION_REQUEST, AdsListActivity.REQUEST_CATEGORY);
        }
        startActivity(intent);
    }
}
