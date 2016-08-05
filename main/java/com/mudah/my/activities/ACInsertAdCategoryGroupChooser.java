package com.mudah.my.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.lib701.connection.ACBlocketConnection;
import com.lib701.datasets.ACCategoryGroup;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.ACCategoryGroupsAdapter;

import java.util.ArrayList;

public class ACInsertAdCategoryGroupChooser extends ListActivity {

    private ACReferences ref;
    private ACSettings settings;
    private Context context;
    private View loaderView;
    private ACCategoryGroupsAdapter categoryAdapter;
    private ArrayList<ACCategoryGroup> categoryGroups;
    private Handler fetchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == ACReferences.RESULT_OK) {
                categoryGroups = new ArrayList<ACCategoryGroup>(settings.getAcCategoriesDataSet().getCategoryGroups());
                categoryGroups.remove(0);
                categoryAdapter = new ACCategoryGroupsAdapter(context, R.layout.default_list_row_black_text, categoryGroups);
                setListAdapter(categoryAdapter);
                getListView().removeFooterView(loaderView);
            }
        }
    };
    private TextView tvHeader;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.default_list);
        context = this;

        ref = ACReferences.getACReferences();
        settings = ACSettings.getACSettings();
        loaderView = View.inflate(this, R.layout.loading_footer, null);
        getListView().addFooterView(loaderView);
        //Assigning Header to the list view
        tvHeader = (TextView) findViewById(R.id.tv_list_header);
        tvHeader.append(getString(R.string.default_list_header_category));
        // list footers are not displayed, and cannot be removed, until a list adapter is set
        setListAdapter(new ACCategoryGroupsAdapter(this, R.layout.default_list_row_black_text, null));
        if (categoryGroups == null) {
            if (!ACReferences.categoriesFetched) {
                ACBlocketConnection.fetchCategories(ACInsertAdCategoryGroupChooser.this, fetchHandler);
            } else {
                categoryGroups = (ArrayList<ACCategoryGroup>) settings.getAcCategoriesDataSet().getCategoryGroups().clone();
                //remove "All Categories" from the list
                categoryGroups.remove(categoryGroups.size()-1);
                categoryAdapter = new ACCategoryGroupsAdapter(this, R.layout.default_list_row_black_text, categoryGroups);
                setListAdapter(categoryAdapter);
                getListView().removeFooterView(loaderView);
            }

        } else {
            categoryAdapter = new ACCategoryGroupsAdapter(this, R.layout.default_list_row_black_text, categoryGroups);
            setListAdapter(categoryAdapter);
            getListView().removeFooterView(loaderView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        for (int i = 0; i <= this.getListView().getLastVisiblePosition(); i++) {
            if (i == position) {
                if (categoryGroups != null && categoryGroups.get(i) != null && categoryGroups.get(i).getCategories() != null) {
                    Intent intent = new Intent(this, ACInsertAdCategoryChooser.class);
                    intent.putExtra("insert_ad_category_group_name", categoryGroups.get(i).getName());
                    intent.putExtra("insert_ad_category_group_id", i );

                    startActivityForResult(intent, 0);
                    getWindow().getDecorView().setVisibility(View.INVISIBLE);
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("" + resultCode);

        setResult(resultCode);
        finish();
    }
}
