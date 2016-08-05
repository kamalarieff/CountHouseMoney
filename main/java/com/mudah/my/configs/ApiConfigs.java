package com.mudah.my.configs;

import android.content.Context;

import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.lib701.utils.PreferencesUtils;
import com.mudah.my.utils.MudahPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to keep configs that will be stored as a static object and
 * retrieve from shared preferences if the static object does not return any value
 */
public class ApiConfigs {

    public static String[] config_bgColor = {MudahPreferencesUtils.SPLASH_IMG_BG_COLOR, "#CD2027", "bgColor"};
    public static String[] config_insertAdRedirect = {MudahPreferencesUtils.INSERT_AD_REDIRECT, "false", "insertAdRedirect"};

    // Need to safe this value in PreferencesUtils to accounts for condition where image is chosen before
    // params.py is called (happens when image is chosen before category is chosen)
    public static String[] config_watermarkAllow = {MudahPreferencesUtils.BLOCK_WATERMARK_IMAGE_REUPLOAD, "true", "watermarkImageReupload"};

    private static int PREFERENCES_UTILS = 0;
    private static int DEFAULT_VALUE = 1;
    private static int CONFIG_NAME = 2;

    private static int maxWidth = 640;
    private static int minHeight = 10;
    private static int maxHeight = 480;
    private static int minWidth = 10;
    private static int quality = 75;
    private static int maxSize = 71680;
    private static Boolean allowWatermark = null;

    private static String locationLabel = "Item Location";

    public static void saveConfig(Context context, String[] config, String value) {
        if (context != null) {
            PreferencesUtils.getSharedPreferences(context).edit()
                    .putString(config[PREFERENCES_UTILS], value)
                    .apply();
        }
        Log.d("Saved config value of " + config[CONFIG_NAME] + ":" + value);
    }

    public static void saveConfig(Context context, String[] config, JSONObject jsonObject) {
        if (context != null) {
            PreferencesUtils.getSharedPreferences(context).edit()
                    .putString(config[PREFERENCES_UTILS], jsonObject.toString())
                    .apply();
        }
        Log.d("Saved config value of " + config[CONFIG_NAME] + ":" + jsonObject.toString());
    }

    public static String getConfigString(Context context, String[] config) {
        String strConfig = Constants.EMPTY_STRING;
        if (context != null) {
            strConfig = PreferencesUtils.getSharedPreferences(context)
                    .getString(config[PREFERENCES_UTILS], config[DEFAULT_VALUE]);
        }

        if (strConfig.equalsIgnoreCase(Constants.EMPTY_STRING) && strConfig == Constants.EMPTY_STRING) {
            strConfig = config[DEFAULT_VALUE];
        }

        Log.d("Retrieve config value of " + config[CONFIG_NAME] + ":" + strConfig);
        return strConfig;
    }

    public static boolean getConfigBoolean(Context context, String[] config) {
        String strConfig = getConfigString(context, config);
        return (strConfig.equalsIgnoreCase("true") || strConfig.equalsIgnoreCase("1")) ? true : false;
    }

    public static JSONObject getConfigJSONObject(Context context, String[] config) {
        JSONObject jsonObjectConfig = null;
        try {
            String strConfig = getConfigString(context, config);
            jsonObjectConfig = new JSONObject(strConfig);
        } catch (JSONException e) {
            ACUtils.debug(e);
        }
        return jsonObjectConfig;
    }

    public static void setImageConfig(Context context, JSONObject imageConfigData) {
        if (!ACUtils.isEmpty(imageConfigData.toString())) {
            try {
                maxWidth = imageConfigData.getInt("max_width");
                minHeight = imageConfigData.getInt("min_height");
                maxHeight = imageConfigData.getInt("max_height");
                minWidth = imageConfigData.getInt("min_width");
                quality = imageConfigData.getInt("quality");
                maxSize = imageConfigData.getInt("max_size");

                String strAllowWatermark = imageConfigData.getString("allow_watermark");
                allowWatermark = (strAllowWatermark.equalsIgnoreCase("true") || strAllowWatermark.equalsIgnoreCase("1")) ? true : false;
                saveConfig(context, ApiConfigs.config_watermarkAllow, String.valueOf(allowWatermark));
            } catch (JSONException e) {
                ACUtils.debug(e);
            }
        }
    }

    public static int getMaxWidth() {
        return maxWidth;
    }

    public static int getMinHeight() {
        return minHeight;
    }

    public static int getMaxHeight() {
        return maxHeight;
    }

    public static int getMinWidth() {
        return minWidth;
    }

    public static int getQuality() {
        return quality;
    }

    public static int getMaxSize() {
        return maxSize;
    }

    public static boolean isAllowWatermark(Context context) {
        if (allowWatermark != null) {
            return allowWatermark;
        } else {
            getConfigBoolean(context, config_watermarkAllow);
        }
        return true;
    }

    public static String getLocationLabel() {
        return locationLabel;
    }

    public static void setLocationLabel(String label) {
        locationLabel = label;
    }

}
