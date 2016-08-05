package com.mudah.my.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lib701.connection.ACBlocketConnection;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACRegion;
import com.lib701.datasets.ACRegionsDataSet;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.ACRegionsAdapter;
import com.mudah.my.configs.Config;
import com.mudah.my.helpers.ActionBarHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class LaunchLocationActivity extends MudahBaseActivity {
    public static final String ALL = "0";
    public static ACRegionsDataSet acRegionsDataSet;
    private RegionHandler fetchHandler = new RegionHandler(this);
    private View footerLoadView;
    private ACRegionsAdapter regionAdapter;
    private ArrayList<ACRegion> regions;
    private ACReferences ref;
    private ACSettings acSettings;
    private ListView listView;

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (ref != null) {
                if (regions == null || (ALL).equalsIgnoreCase(regions.get(position).getId())) {
                    ref.setRegionId(ALL);
                    ref.setMunicipalityId(null);
                } else {
                    String regionId = regions.get(position).getId();
                    Log.d("selected regionId: " + regionId);
                    ref.setRegionId(regionId);
                    ref.setMunicipalityId(null);
                }
                Config.firstTimeUserAndChooseRegion = true;
                redirectResult();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        setContentView(R.layout.activity_launch_location);
        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getString(R.string.launch_location_title));

        ref = ACReferences.getACReferences();
        regionAdapter = new ACRegionsAdapter(LaunchLocationActivity.this, new ArrayList<ACRegion>());

        listView = (ListView) findViewById(R.id.location_list);
        listView.setTextFilterEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        footerLoadView = inflater.inflate(R.layout.loading_footer, null);
        listView.addFooterView(footerLoadView);
        listView.setOnItemClickListener(itemClickListener);
        listView.setAdapter(regionAdapter);

        if (!ACReferences.regionsFetched) {
            ACBlocketConnection.fetchRegions(LaunchLocationActivity.this, fetchHandler);
        } else {
            acSettings = ACSettings.getACSettings();
            if (acRegionsDataSet == null) {
                acRegionsDataSet = acSettings.getAcRegionsDataSet();
            }

            regions = new ArrayList<>(acRegionsDataSet.getRegions());
            regionAdapter.addRegions(regions);
            listView.removeFooterView(footerLoadView);
        }

    }

    private void redirectResult() {
        Intent intent = new Intent(LaunchLocationActivity.this, LocationActivity.class);
        startActivity(intent);
        finish();
    }

    static class RegionHandler extends Handler {
        private final WeakReference<LaunchLocationActivity> mTarget;

        public RegionHandler(LaunchLocationActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final LaunchLocationActivity target = mTarget.get();
            if (target == null) {
                return;
            }
            if (msg.arg1 == ACReferences.RESULT_OK) {
                target.acSettings = ACSettings.getACSettings();
                if (acRegionsDataSet == null) {
                    acRegionsDataSet = target.acSettings.getAcRegionsDataSet();
                }
                target.regions = acRegionsDataSet.getRegions();

                target.regionAdapter.addRegions(target.regions);
                target.listView.removeFooterView(target.footerLoadView);
            } else {
                target.redirectResult();
            }
        }
    }
}
