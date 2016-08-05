package com.mudah.my.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.comscore.analytics.comScore;
import com.lib701.connection.ACBlocketConnection;
import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACRegion;
import com.lib701.datasets.ACRegionsDataSet;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.Log;
import com.mudah.my.R;
import com.mudah.my.adapters.ACRegionsAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ACAdsSearchRegionChooser extends ListActivity {

    public static final String HIDE_ALL_COUNTRY_OPTION = "hide_all_country";
    //private static final String TAG = "ACRegionList";
    public static ACRegionsDataSet acRegionsDataSet;
    RegionHandler fetchHandler = new RegionHandler(this);
    private View footerLoadView;
    private ACRegionsAdapter regionAdapter;
    private ArrayList<ACRegion> regions;
    private ACReferences ref;
    private ACSettings acSettings;
    private boolean active = false;
    private TextView tvHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.strictMode();
        Log.d();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.default_list);

        ref = ACReferences.getACReferences();
        regionAdapter = new ACRegionsAdapter(ACAdsSearchRegionChooser.this, new ArrayList<ACRegion>());


        ListView listView = getListView();
        listView.setTextFilterEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        footerLoadView = inflater.inflate(R.layout.loading_footer, null);
        getListView().addFooterView(footerLoadView);
        //Assigning Header to the list view
        tvHeader = (TextView) findViewById(R.id.tv_list_header);
        tvHeader.append(getString(R.string.default_list_header_region));

        setListAdapter(regionAdapter);
        if (!ACReferences.regionsFetched) {
            ACBlocketConnection.fetchRegions(ACAdsSearchRegionChooser.this, fetchHandler);
        } else {
            acSettings = ACSettings.getACSettings();
            if (acRegionsDataSet == null) {
                acRegionsDataSet = acSettings.getAcRegionsDataSet();
            }

            regions = new ArrayList<>(acRegionsDataSet.getRegions());
            if (getIntent().getBooleanExtra(HIDE_ALL_COUNTRY_OPTION, false)) regions.remove(0);
            regionAdapter.addRegions(regions);
            getListView().removeFooterView(footerLoadView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d();
        active = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d();
        // Notify comScore about lifecycle usage
        comScore.onExitForeground();
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
        // Notify comScore about lifecycle usage
        comScore.onEnterForeground();
        active = true;
    }

    ;

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (ref == null) {
            setResult(RESULT_CANCELED);
        } else {

            if (regions == null) {
                ref.setRegionId("0");
            } else {
                ref.setRegionId(regions.get(position).getId());
            }

            setResult(RESULT_OK);
        }
        finish();
    }

    static class RegionHandler extends Handler {
        private final WeakReference<ACAdsSearchRegionChooser> mTarget;

        public RegionHandler(ACAdsSearchRegionChooser target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final ACAdsSearchRegionChooser target = mTarget.get();
            if (target == null) {
                return;
            }

            if (msg.arg1 == ACReferences.RESULT_OK) {
                target.acSettings = ACSettings.getACSettings();
                if (acRegionsDataSet == null) {
                    acRegionsDataSet = target.acSettings.getAcRegionsDataSet();
                }
                target.regions = acRegionsDataSet.getRegions();

                ArrayList<ACRegion> newRegions = new ArrayList<ACRegion>(target.regions);
                if (target.getIntent().getBooleanExtra(HIDE_ALL_COUNTRY_OPTION, false))
                    newRegions.remove(0);

                target.regionAdapter.addRegions(target.regions);
                target.getListView().removeFooterView(target.footerLoadView);
            } else if (!target.isFinishing() && target.active) {
                Dialog dialog = new AlertDialog.Builder(target)
                        .setIcon(R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dialog_error_load_error_title)
                        .setMessage(R.string.dialog_error_failed_to_load_regions)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_error_retry_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ACBlocketConnection.fetchRegions(target.getApplicationContext(), target.fetchHandler);
                                return;
                            }
                        })
                        .setNegativeButton(R.string.dialog_error_cancel_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                target.finish();
                                return;
                            }
                        })
                        .create();
                dialog.show();
            }
        }
    }
}
