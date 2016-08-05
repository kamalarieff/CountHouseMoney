package com.mudah.my.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lib701.datasets.ACReferences;
import com.lib701.datasets.ACRegion;
import com.lib701.datasets.ACRegionsDataSet;
import com.lib701.datasets.ACSettings;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.XitiUtils;
import com.mudah.my.R;
import com.mudah.my.configs.Constants;
import com.mudah.my.helpers.ActionBarHelper;
import com.mudah.my.utils.EventTrackingUtils;

import java.util.HashMap;


/**
 * Created by moehninhtwee on 21/5/15.
 */
public class LocationActivity extends MudahBaseActivity {

    public static final String REQUEST_LOCATION_QUERY = "location_query";
    public static final String[] DECLARED_PARAMS = {
            Constants.AREA,
            Constants.SUBAREA,
            Constants.REGION};
    public static final int REQUEST_SUBREGION = 3;
    public static final int REGION_BUTTON = 1;
    public static final int AREA_BUTTON = 2;
    public static final int SUBREGION_BUTTON = 3;
    private static final int REGION_ONLY = 1;
    private static final int REGION_NEIGHBOURING = 2;
    private static final int DISABLE = View.VISIBLE;
    private static final int ENABLE = View.GONE;
    private static final int REQUEST_REGION = 1;
    public String area = null;
    private RelativeLayout rlRegionContainer;
    private TextView tvRegionSelected;
    private RelativeLayout rlAreaContainer;
    private TextView tvAreaSelected;
    private View vAreaOverlay;
    private RelativeLayout rlSubregionContainer;
    private TextView tvSubregionSelected;
    private View vSubregionOverlay;
    private TextView btnOK;
    private int xitiCategoryId;
    private HashMap<String, String> searchParams = new HashMap<>();

    private HashMap<String, String> searchLocation = new HashMap<>();
    private Spinner sArea;
    private final View.OnClickListener filterOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_region_container:
                    Intent intentRegion = new Intent(LocationActivity.this, ACAdsSearchRegionChooser.class);
                    startActivityForResult(intentRegion, REQUEST_REGION);
                    EventTrackingUtils.sendClickByCategoryId(null, xitiCategoryId, XitiUtils.LISTING, "Listing_filterRegion", XitiUtils.NAVIGATION);
                    break;
                case R.id.rl_subregion_container:
                    Intent intentSubregion = new Intent(LocationActivity.this, ACAdsSearchMunicipalityChooser.class);
                    startActivityForResult(intentSubregion, REQUEST_SUBREGION);
                    EventTrackingUtils.sendClickByCategoryId(null, xitiCategoryId, XitiUtils.LISTING, "Listing_filterMunicipality", XitiUtils.NAVIGATION);
                    break;
                case R.id.rl_area_container:
                    sArea.performClick();
                    EventTrackingUtils.sendClickByCategoryId(null, xitiCategoryId, XitiUtils.LISTING, "Listing_filterArea", XitiUtils.NAVIGATION);
                    break;
                case R.id.tv_location_ok:
                    clickOKBtn();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ActionBarHelper actionBar = new ActionBarHelper(this);
        actionBar.createActionBarWithTitle(R.id.actionbar, getResources().getString(R.string.location_page_title));

        rlRegionContainer = (RelativeLayout) findViewById(R.id.rl_region_container);
        rlRegionContainer.setOnClickListener(filterOnClickListener);
        tvRegionSelected = (TextView) findViewById(R.id.tv_region_selected);

        rlAreaContainer = (RelativeLayout) findViewById(R.id.rl_area_container);
        rlAreaContainer.setOnClickListener(filterOnClickListener);
        rlAreaContainer.setClickable(false);
        tvAreaSelected = (TextView) findViewById(R.id.tv_area_selected);
        vAreaOverlay = findViewById(R.id.v_area_overlay);

        rlSubregionContainer = (RelativeLayout) findViewById(R.id.rl_subregion_container);
        rlSubregionContainer.setOnClickListener(filterOnClickListener);
        rlSubregionContainer.setClickable(false);
        tvSubregionSelected = (TextView) findViewById(R.id.tv_subregion_selected);
        vSubregionOverlay = findViewById(R.id.v_subregion_overlay);

        sArea = (Spinner) findViewById(R.id.s_location_area);

        btnOK = (TextView) findViewById(R.id.tv_location_ok);
        btnOK.setOnClickListener(filterOnClickListener);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (savedInstanceState != null) {
            searchParams = (HashMap<String, String>) savedInstanceState.getSerializable(AdsListActivity.SEARCH_PARAMS);
            loadLastSearchLocation();
        } else if (bundle != null && (bundle.containsKey(AdsListActivity.SEARCH_PARAMS))) {
            searchParams = (HashMap<String, String>) bundle.getSerializable(AdsListActivity.SEARCH_PARAMS);
            loadLastSearchLocation();
        } else {
            updateNewRegionSelected();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ACReferences ref = ACReferences.getACReferences();
        String categoryId = (ref.categoryId != null) ? ref.categoryId : ref.categoryGroupId;
        xitiCategoryId = !ACUtils.isEmpty(categoryId) ? Integer.parseInt(categoryId) : 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clickOKBtn() {
        Intent intent = new Intent(LocationActivity.this, AdsListActivity.class);
        searchLocation = getParamValues();
        intent.putExtra(REQUEST_LOCATION_QUERY, searchLocation);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(AdsListActivity.FUNCTION_REQUEST, AdsListActivity.REQUEST_LOCATION);
        startActivity(intent);
        finish();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d();
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(AdsListActivity.SEARCH_PARAMS, searchParams);
    }

    private HashMap<String, String> getParamValues() {
        HashMap<String, String> paramMap = new HashMap<>();
        ACReferences ref = ACReferences.getACReferences();

        if (ref.getRegionId() != null && !Constants.ALL_REGION.equals(ref.getRegionId())) {

            ACSettings acSettings = ACSettings.getACSettings();
            if (ref.getAreaId() != null && !Constants.ALL_REGION.equals(ref.getAreaId())) {
                if (Constants.AREA_NEIGHBOURING.equals(ref.getAreaId())) {
                    // Eg: Kuala Lumpur > Kuala Lumpur & neighbour > null
                    paramMap.put(Constants.REGION, ref.getRegionId());
                    paramMap.put(Constants.AREA, ref.getAreaId());//AREA_NEIGHBOURING
                    paramMap.put(Constants.SUBAREA, null);
                    Log.d("Search for "
                            + acSettings.getRegionName(ref.getRegionId()) + " > "
                            + acSettings.getRegionName(ref.getRegionId()) + " & neighbour > "
                            + "null");

                } else if (Constants.AREA_SPECIFIC.equals(ref.getAreaId())) {

                    if (ref.getMunicipalityId() != null && !Constants.ALL_REGION.equals(ref.getMunicipalityId())) {
                        // Eg: Kuala Lumpur > Kuala Lumpur only > Bangsar
                        paramMap.put(Constants.REGION, ref.getRegionId());
                        paramMap.put(Constants.AREA, ref.getAreaId());
                        paramMap.put(Constants.SUBAREA, ref.getMunicipalityId());
                        Log.d("Search for "
                                + acSettings.getRegionName(ref.getRegionId()) + " > "
                                + acSettings.getRegionName(ref.getRegionId()) + " only > "
                                + acSettings.getMunicipalityName(ref.getMunicipalityId()));
                    } else {
                        // Eg: Kuala Lumpur > Kuala Lumpur only > Entire Region
                        paramMap.put(Constants.REGION, ref.getRegionId());
                        paramMap.put(Constants.AREA, ref.getAreaId());//AREA_SPECIFIC
                        paramMap.put(Constants.SUBAREA, null);
                        Log.d("Search for "
                                + acSettings.getRegionName(ref.getRegionId()) + " > "
                                + acSettings.getRegionName(ref.getRegionId()) + " only > "
                                + "Entire Region");
                    }
                }
            }
        } else {
            // Eg: Entire Malaysia > null > null
            paramMap.put(Constants.REGION, null);
            paramMap.put(Constants.AREA, null);
            paramMap.put(Constants.SUBAREA, null);
            Log.d("Search for "
                    + "Entire Malaysia" + " > "
                    + "null > "
                    + "null");
        }

        return paramMap;
    }

    private void loadLastSearchLocation() {
        if (searchParams.containsKey(Constants.REGION)) {
            updateMenuClickable(SUBREGION_BUTTON, ENABLE);
            updateMenuClickable(AREA_BUTTON, ENABLE);
        }
        if (searchParams.containsKey(Constants.SUBAREA)) {
            updateMenuClickable(SUBREGION_BUTTON, ENABLE);
        }
        if (searchParams.containsKey(Constants.AREA)) {
            String lastSearchArea = searchParams.get(Constants.AREA);
            if (!ACUtils.isEmpty(lastSearchArea))
                area = lastSearchArea;
        }
        updateQueryFields();
        updateAreaFilter();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_REGION:
                updateNewRegionSelected();
                break;
            case REQUEST_SUBREGION:
                ACReferences ref = ACReferences.getACReferences();
                updateQueryFields();
                if (ref.getMunicipalityId() != null && !Constants.ALL_REGION.equals(ref.getMunicipalityId())) {
                    area = null;
                    updateAreaFilter();
                }
                break;
        }
    }

    private void updateNewRegionSelected() {
        updateQueryFields();
        updateAreaFilter();
        updateSubRegionFilter();
    }

    private void updateSubRegionFilter() {
        ACReferences ref = ACReferences.getACReferences();
        if (ref.getRegionId() != null && !Constants.ALL_REGION.equals(ref.getRegionId())
                && !Constants.REGION_SINGAPORE.equals(ref.getRegionId())) {
            updateMenuClickable(SUBREGION_BUTTON, ENABLE);
        } else {
            updateMenuClickable(SUBREGION_BUTTON, DISABLE);
        }
    }

    private void updateMenuClickable(int type, int itemMode) {

        boolean clickable = false;
        switch (itemMode) {
            case DISABLE:
                clickable = false;
                break;
            case ENABLE:
                clickable = true;
                break;
        }

        switch (type) {
            case AREA_BUTTON:
                vAreaOverlay.setVisibility(itemMode);
                rlAreaContainer.setClickable(clickable);
                break;
            case SUBREGION_BUTTON:
                vSubregionOverlay.setVisibility(itemMode);
                rlSubregionContainer.setClickable(clickable);
                break;
        }
    }

    private void updateAreaFilter() {
        ACReferences ref = ACReferences.getACReferences();
        if (ref.getRegionId() != null && !Constants.ALL_REGION.equals(ref.getRegionId())) {
            updateMenuClickable(AREA_BUTTON, ENABLE);
            String region = ACSettings.getACSettings().getRegionName(ref.getRegionId());
            ArrayAdapter<Item<Integer>> adapter = new ArrayAdapter<>(LocationActivity.this, R.layout.simple_spinner_item, android.R.id.text1);
            adapter.add(new Item<>(getString(R.string.ads_search_region_single, region), REGION_ONLY));
            adapter.add(new Item<>(getString(R.string.ads_search_region_neighbouring), REGION_NEIGHBOURING));
            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            sArea.setAdapter(adapter);
            if (!ACUtils.isEmpty(area)) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (area.equals(adapter.getItem(i).value.toString())) {
                        sArea.setSelection(i);
                        ref.setAreaId(adapter.getItem(i).name);
                        setMenuLabel(AREA_BUTTON, adapter.getItem(i).name);
                        break;
                    }
                }
            } else {//set default to '0' for the first time user
                setMenuLabel(AREA_BUTTON, getString(R.string.ads_search_region_single, region));
            }
        } else {
            ArrayAdapter<Item<Integer>> adapter = new ArrayAdapter<>(LocationActivity.this, R.layout.simple_spinner_item, android.R.id.text1);
            sArea.setAdapter(adapter);
            setMenuLabel(AREA_BUTTON, "");
            updateMenuClickable(AREA_BUTTON, DISABLE);
        }
        sArea.setOnItemSelectedListener(new IgnoreFirstSelectedListener(AREA_BUTTON));
    }

    private void updateQueryFields() {
        ACReferences ref = ACReferences.getACReferences();
        ACSettings set = ACSettings.getACSettings();
        String region;
        String subRegion;
        if (ref.getRegionId() != null) {
            region = set.getRegionName(ref.getRegionId());
            ACRegionsDataSet acRegionDataSet = set.getAcRegionsDataSet();
            if (acRegionDataSet == null) {
                ref.setMunicipalityId(null);
            } else {
                ACRegion acRegion = acRegionDataSet.findRegionById(ref.getRegionId());
                if (acRegion != null && !acRegion.checkMunicipalityInRegion(ref.getMunicipalityId())) {
                    ref.setMunicipalityId(null);
                }
            }
        } else {
            region = getString(R.string.ads_search_region_all);
        }
        if (ref.getMunicipalityId() != null) {
            subRegion = set.getMunicipalityName(ref.getMunicipalityId());
        } else {
            subRegion = getString(R.string.ads_search_municipality_all);
        }
        setMenuLabel(REGION_BUTTON, region);
        setMenuLabel(SUBREGION_BUTTON, subRegion);
    }

    public void setMenuLabel(int type, String label) {
        switch (type) {
            case REGION_BUTTON:
                tvRegionSelected.setText(label);
                break;
            case SUBREGION_BUTTON:
                tvSubregionSelected.setText(label);
                break;
            case AREA_BUTTON:
                tvAreaSelected.setText(label);
                break;
        }
    }

    private static class Item<T> {
        private final String name;
        private final T value;

        public Item(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class IgnoreFirstSelectedListener implements AdapterView.OnItemSelectedListener {
        private int type;

        public IgnoreFirstSelectedListener(int type) {
            this.type = type;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Item<?> item = ((Item<?>) parent.getSelectedItem());
            String name = item.name;
            setMenuLabel(type, name);
            ACReferences ref = ACReferences.getACReferences();
            ref.setAreaId(item.value.toString());
            if (position == 1) {
                ref.setMunicipalityId(null);
                updateQueryFields();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}
