package com.mudah.my.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by w_ongkl on 9/2/14.
 */
public class InsertAdAdapterTagModel {
    @SerializedName("name")
    private String name;
    @SerializedName("page")
    private String page;
    @SerializedName("label")
    private String label;
    @SerializedName("error")
    private String error;

    public InsertAdAdapterTagModel(String name, String page, String label, String error) {
        this.name = name;
        this.page = page;
        this.label = label;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public String getPage() {
        return page;
    }

    public String getLabel() {
        return label;
    }

    public String getError() {
        return error;
    }

}
