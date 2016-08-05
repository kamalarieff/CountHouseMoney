package com.mudah.my.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class ACInsertAdRegionChooser extends ListActivity {

    public static final String HIDE_ALL_COUNTRY_OPTION = "hide_all_country";
    //private static final String TAG = "ACRegionList";
    public static ACRegionsDataSet acRegionsDataSet;
    View footerLoadView;
    RegionHandler fetchHandler = new RegionHandler(this);
    private ACRegionsAdapter region_adapter;
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
        region_adapter = new ACRegionsAdapter(ACInsertAdRegionChooser.this, new ArrayList<ACRegion>());


        ListView listView = getListView();
        listView.setTextFilterEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerLoadView = inflater.inflate(R.layout.loading_footer, null);
        getListView().addFooterView(footerLoadView);
        //Assigning Header to the list view
        tvHeader = (TextView) findViewById(R.id.tv_list_header);
        tvHeader.append(getString(R.string.default_list_header_region));

        if (!ACReferences.regionsFetched) {
            ACBlocketConnection.fetchRegions(ACInsertAdRegionChooser.this, fetchHandler);
        } else {
            acSettings = ACSettings.getACSettings();
            if (acRegionsDataSet == null) {
                acRegionsDataSet = acSettings.getAcRegionsDataSet();
            }

            regions = new ArrayList<ACRegion>(acRegionsDataSet.getRegions());
            Log.d("region size: " + regions.size());
            if (regions.size() == 0) {
                ACBlocketConnection.fetchRegions(ACInsertAdRegionChooser.this, fetchHandler);
            } else {
                if (getIntent().getBooleanExtra(HIDE_ALL_COUNTRY_OPTION, false)) regions.remove(0);
                region_adapter.addRegions(regions);
                setListAdapter(region_adapter);
                getListView().removeFooterView(footerLoadView);
            }
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

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (ref == null) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            if (regions == null) {
                ref.setRegionId("0");
                finish();
            } else {
                ACRegion region = regions.get(position);
                ref.setInsertAdRegionId(regions.get(position).getId());
                //clear the previous selection of sub area
                ref.setInsertAdMunicipalityId(null);
                Log.d("Chose regionId: " + ref.getInsertAdRegionId() + ", municipalities size: " + region.getMunicipalities().size());
                if (region.getMunicipalities().size() > 0) {
                    Intent intent = new Intent(ACInsertAdRegionChooser.this, ACAdsSearchMunicipalityChooser.class);
                    intent.putExtra(ACInsertAdRegionChooser.HIDE_ALL_COUNTRY_OPTION, true);
                    startActivityForResult(intent, 0);
                    getWindow().getDecorView().setVisibility(View.INVISIBLE);
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
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

    static class RegionHandler extends Handler {
        private final WeakReference<ACInsertAdRegionChooser> mTarget;

        public RegionHandler(ACInsertAdRegionChooser target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final ACInsertAdRegionChooser target = mTarget.get();
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

                target.region_adapter.addRegions(newRegions);
                //getListView().removeFooterView(footerLoadView);
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
