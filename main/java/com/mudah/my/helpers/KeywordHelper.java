package com.mudah.my.helpers;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;
import com.mudah.my.utils.MudahPreferencesUtils;
import com.mudah.my.utils.MudahUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by w_ongkl on 6/9/15.
 */
public class KeywordHelper {

    final static int MAXIMUM_STORED_HISTORY_COUNT = Config.MAXIMUM_STORED_HISTORY_COUNT;

    private ArrayList<KeywordProperty> historyKeywordProperties;
    private Context context = null;

    public KeywordHelper(Context context) {
        if (this.context == null) {
            this.context = context;
        }
        if (this.historyKeywordProperties == null) {
            this.historyKeywordProperties = new ArrayList<KeywordProperty>();
        }

    }

    public void init() {
        try {
            if (context != null) {
                this.historyKeywordProperties = (ArrayList<KeywordProperty>)
                        MudahUtil.retrieveClassInSharedPreferences(
                                context,
                                MudahPreferencesUtils.SEARCHABLE_KEYWORD_OBJECTS,
                                new TypeToken<List<KeywordProperty>>(){}.getType(),
                                Constants.EMPTY_STRING);
                if (this.historyKeywordProperties == null) {
                    this.historyKeywordProperties = new ArrayList<KeywordProperty>();
                }
            }
            else {
                ACUtils.debug("KeywordHelper constructor was not called. Context is null");
            }
        } catch (MalformedJsonException e) {
            ACUtils.debug(e);
        }
    }

    public void save() {
        if (context != null) {
            if (historyKeywordProperties.size() > MAXIMUM_STORED_HISTORY_COUNT) {
                for (int i = historyKeywordProperties.size(); i > MAXIMUM_STORED_HISTORY_COUNT; i--) {
                    historyKeywordProperties.remove(0);
                }
            }
            MudahUtil.saveClassInSharedPreferences(context, MudahPreferencesUtils.SEARCHABLE_KEYWORD_OBJECTS, historyKeywordProperties);
        }
        else {
            ACUtils.debug("KeywordHelper constructor was not called. Context is null");
        }
    }

    public void addKeywordProperty(KeywordProperty keywordProperty) {
        if (keywordProperty.getType().equalsIgnoreCase(KeywordProperty.HISTORY)) {

            //Case there no saved keyword
            if(historyKeywordProperties.size() == 0) {
                historyKeywordProperties.add(keywordProperty);
                return;
            }

            //Case keyword already exist, update keyword with new data
            for (int i = 0; i < historyKeywordProperties.size(); i++) {
                KeywordProperty existingKeywordProperty = historyKeywordProperties.get(i);
                if (KeywordProperty.HISTORY.equalsIgnoreCase(existingKeywordProperty.getType())) {
                    String existing = existingKeywordProperty.getKeyword();
                    if (!ACUtils.isEmpty(existing) && existing.equalsIgnoreCase(keywordProperty.getKeyword())) {
                        historyKeywordProperties.remove(i);
                        historyKeywordProperties.add(keywordProperty);
                        return;
                    }
                }
            }

            //Case no keyword were found
            historyKeywordProperties.add(keywordProperty);
            return;
        }
        return;
    }

    public ArrayList<KeywordProperty> getKeywordProperties() {
        return historyKeywordProperties;
    }

    public class KeywordProperty {
        public static final String HISTORY = "history";

        @SerializedName("keyword")
        private String keyword;

        @SerializedName("timeStampInMilis")
        private long timeStampInMilis;

        @SerializedName("type")
        private String type;

        public KeywordProperty(String keyword, Calendar timeStamp, String type) {
            this.keyword = keyword;
            this.timeStampInMilis = timeStamp.getTimeInMillis();
            this.type = type;
        }

        public KeywordProperty(String keyword, long timeStampInMilis, String type) {
            this.keyword = keyword;
            this.timeStampInMilis = timeStampInMilis;
            this.type = type;
        }

        public String getKeyword() {
            return keyword;
        }

        public long getTimeStampInMilis() {
            return timeStampInMilis;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "keyword:"+this.keyword+"; timeStampInMilis:"+timeStampInMilis+"; type:"+type;
        }

    }


}
