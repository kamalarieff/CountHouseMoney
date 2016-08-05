package com.mudah.my.activities;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.lib701.datasets.ACMunicipality;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACRegion;
import com.lib701.datasets.ACRegionsDataSet;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.ACMunicipalitiesAdapter;
import com.mudah.my.configs.Constants;

import java.util.ArrayList;

public class ACAdsSearchMunicipalityChooser extends ListActivity {

    public static ACRegionsDataSet acRegionsDataSet;
    private View footerLoadView;
    private ACMunicipalitiesAdapter municipalitiesAdapter;
    private ArrayList<ACMunicipality> municipalities;
    private ACReferences ref;
    private ACSettings acSettings;
    private boolean insertAdRequest = false;
    private TextView tvHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.default_list);

        String regionId;
        ref = ACReferences.getACReferences();
        acSettings = ACSettings.getACSettings();
        municipalitiesAdapter = new ACMunicipalitiesAdapter(ACAdsSearchMunicipalityChooser.this, new ArrayList<ACMunicipality>());


        ListView listView = getListView();
        listView.setTextFilterEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerLoadView = inflater.inflate(R.layout.loading_footer, null);
        getListView().addFooterView(footerLoadView);
        //Assigning Header to the list view
        tvHeader = (TextView) findViewById(R.id.tv_list_header);
        tvHeader.append(getString(R.string.default_list_header_subarea));

        if (acRegionsDataSet == null) {
            acRegionsDataSet = acSettings.getAcRegionsDataSet();
        }
        if ((ref.getRegionId() != null && ref.getRegionId() != "0") || (ref.getInsertAdRegionId() != null && ref.getInsertAdRegionId() != "0")) {
            municipalities = new ArrayList<ACMunicipality>();

            if (getIntent() != null)
                insertAdRequest = getIntent().getBooleanExtra(ACInsertAdRegionChooser.HIDE_ALL_COUNTRY_OPTION, false);

            if (!insertAdRequest) {
                regionId = ref.getRegionId();
                ACMunicipality entireRegion = new ACMunicipality();
                entireRegion.setId(Constants.ALL_REGION);
                entireRegion.setName("Entire Region");
                municipalities.add(entireRegion);
            } else {
                regionId = ref.getInsertAdRegionId();
            }
            //Array starts with index 0
            ACRegion selectedRegion = acRegionsDataSet.findRegionById(regionId);
            if (selectedRegion != null) {
                municipalities.addAll(selectedRegion.getMunicipalities());
            }
            municipalitiesAdapter.addMunicipalities(municipalities);
        }
        setListAdapter(municipalitiesAdapter);
        getListView().removeFooterView(footerLoadView);

    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (ref == null) {
            setResult(RESULT_CANCELED);
        } else {

            if (insertAdRequest) {
                ref.setInsertAdMunicipalityId(municipalities.get(position).getId());
            } else {
                if (municipalities == null)
                    ref.setMunicipalityId("0");
                else
                    ref.setMunicipalityId(municipalities.get(position).getId());
            }

            setResult(RESULT_OK);
        }
        finish();
    }
}
