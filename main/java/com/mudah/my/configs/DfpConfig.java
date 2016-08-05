package com.mudah.my.configs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DfpConfig {

    public static final String AD_UNIT_NUMBER = "/5908/";
    public static final String GOOGLE_ADS_UNIT_ID = "/android//listing//";
    public static Map<Integer, String> dfpMapBanner = new HashMap<Integer, String>();
    public static Map<Integer, String> shortCatMapBanner = new HashMap<Integer, String>();
    public static Map<Integer, JSONObject> dfpPriceSet = new HashMap<Integer, JSONObject>();

    public static void setDfpMapBanner(Map<Integer, String> dfpMap) {
        dfpMapBanner = dfpMap;
    }

    public static String getDfpAdUnit(int categoryId) {
        return dfpMapBanner.get(categoryId);
    }

    public static void setShortCatMapBanner(Map<Integer, String> shortCatMap) {
        shortCatMapBanner = shortCatMap;
    }

    public static String getShortCat(int categoryId) {
        return shortCatMapBanner.get(categoryId);
    }

    public static void setDfpPriceSet(Map<Integer, JSONObject> priceSet) {
        dfpPriceSet = priceSet;
    }

    public static JSONObject getDfpPriceByCategory(int category) {
        return dfpPriceSet.get(category);
    }

}
