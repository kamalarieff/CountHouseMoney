package com.mudah.my.datasets;

import com.lib701.datasets.ACAd;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

public class AdViewAd extends ACAd implements Serializable {

    protected String whatsApp;
    private boolean isDeletedAd;

    public AdViewAd() {
        super();
    }

    public AdViewAd(JSONObject rawJsonObject) {
        super(rawJsonObject);
        if (rawJsonObject != null) {
            JSONArray rawParameters = rawJsonObject.optJSONArray("ordered_parameters");
            if (rawParameters != null) {
                for (int i = 0; i < rawParameters.length(); i++) {
                    JSONObject eachParam = rawParameters.optJSONObject(i);
                    if (("whats_app").equalsIgnoreCase(eachParam.optString("id"))) {
                        whatsApp = eachParam.optString("label");
                    }
                }
            }
        }
    }

    public boolean isDeletedAd() {
        return isDeletedAd;
    }

    public void setIsDeletedAd(boolean isDeletedAd) {
        this.isDeletedAd = isDeletedAd;
    }

    public String getWhatsApp() {
        return whatsApp;
    }

}