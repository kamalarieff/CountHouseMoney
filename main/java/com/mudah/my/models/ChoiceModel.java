package com.mudah.my.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by w_ongkl on 9/3/14.
 */
public class ChoiceModel {
    @SerializedName("name")
    private String name;
    @SerializedName("value")
    private String value;

    public ChoiceModel(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getName().toString();
    }
}