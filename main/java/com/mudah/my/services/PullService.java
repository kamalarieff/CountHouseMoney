package com.mudah.my.services;

import android.app.IntentService;
import android.content.Intent;

import com.lib701.connection.ACBlocketConnection;
import com.lib701.datasets.ACReferences;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;

/**
 * Created by Pin on 3/2/15.
 */

public class PullService extends IntentService {

    public PullService() {
        super("PullService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Log.d("Start pulling categories and region lists, needRegionUpdate= " + Config.needRegionUpdate + ", needCategoryUpdate= " + Config.needCategoryUpdate);

        if (Config.needRegionUpdate || !ACReferences.regionsFetched)
            ACBlocketConnection.fetchRegions(this);

        if (Config.needCategoryUpdate || !ACReferences.categoriesFetched)
            ACBlocketConnection.fetchCategories(this);

        Log.d("Finished pulling");
    }
}