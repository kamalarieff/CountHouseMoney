package com.mudah.my.utils;


import android.os.AsyncTask;

import com.gravityrd.receng.web.webshop.jsondto.GravityEvent;
import com.gravityrd.receng.web.webshop.jsondto.GravityItem;
import com.gravityrd.receng.web.webshop.jsondto.GravityItemRecommendation;
import com.gravityrd.receng.web.webshop.jsondto.GravityNameValue;
import com.gravityrd.receng.web.webshop.jsondto.GravityRecEngException;
import com.gravityrd.receng.web.webshop.jsondto.GravityRecommendationContext;
import com.gravityrd.recengclient.webshop.GravityClient;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;
import com.mudah.my.models.GravityModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ondo on 5/11/15.
 */
public class GravityUtils {
    public static final String RECOMMENDED_AD_CLICK_NAME = "Homepage_recommended_ad";
    public static final String SCENARIO_HOMEPAGE_APP = "MAIN_PAGE_APP";
    public static final String SCENARIO_ITEM_PAGE_SIMILAR_APP = "ITEM_PAGE_SIMILAR_APP";
    public static final String EVENT_TYPE_VIEW = "VIEW";
    public static final String EVENT_TYPE_SEARCH = "SEARCH";
    public static final String EVENT_TYPE_LETTER_SEND = "LETTER_SEND";
    public static final String EVENT_TYPE_ADD_TO_FAVORITES = "ADD_TO_FAVORITES";
    public static final String EVENT_TYPE_REC_CLICK = "REC_CLICK";
    public static final String EVENT_TYPE_PHONE_CLICK = "PHONE_CLICK";
    public static final String EVENT_TYPE_BROWSE = "BROWSE";
    public static final String FIELD_METHOD = "method";
    public static final String METHOD_PHONE = "tel";
    public static final String METHOD_SMS = "sms";
    public static final String FIELD_REGION = "region";
    public static final String FIELD_AREA = "area";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_KEYWORD = "searchString";
    private static final String DEVICE = "device";
    private static final String APP = "app";
    private static final String ITEM_ID = "itemId";
    private static final String username = "mudah";
    private static final String password = "zip7Ohz5lo";
    private static final String remoteUrl = "https://mudah-sin.gravityrd-services.com/grrec-mudah-war/WebshopServlet";
    // the unique user identifier
    private static final String userId = null;
    //private static final String userId = "testUser1";

    // the unique cookie id
    private static final String cookieId = Config.deviceId;
    // the number of recommended items
    private static final int limit = 10;
    // the time of the recommendation is requested for
    private static final int recommendationTime = (int) (System.currentTimeMillis() / 1000);
    private static GravityClient client = null;

    public static void init() {
        if (!Config.enableGravity) {
            client = null;
            return;
        }
        if (client == null) {
            client = new GravityClient();
            client.setRemoteUrl(remoteUrl);
            client.setUserName(username);
            client.setPassword(password);
        }
    }

    private static ArrayList<GravityModel> getItemRecommendation(GravityRecommendationContext recommendationContext) {
        ArrayList<GravityModel> gravityAdsList = new ArrayList<GravityModel>();
        // requesting the recommendation
        GravityItemRecommendation itemRecommendation = null;
        if (client != null && recommendationContext != null) {
            try {
                itemRecommendation = client.getItemRecommendation(userId, cookieId, recommendationContext);
            } catch (GravityRecEngException e) {
                Log.d("Error happened by getting the item recommendation!");
                Log.d("Message: " + e.getMessage() + " Fault info: " + e.faultInfo);
            } catch (IOException ioe) {
                Log.d("IO error getting item recommendation!");
                Log.d("Message: " + ioe.getMessage());
            } finally {
            }
        }

        if (itemRecommendation != null) {

            for (GravityItem item : itemRecommendation.items) {

                GravityModel ad = new GravityModel();
                ad.setItemId(item.itemId);
                ad.setRecommendationId(itemRecommendation.recommendationId);

                for (GravityNameValue itemNameValue : item.nameValues) {
                    String itemName = itemNameValue.name.toLowerCase();
                    //Log.d(itemName + ": " + itemNameValue.value);
                    switch (itemName) {
                        case GravityModel.TITLE:
                            ad.setTitle(itemNameValue.value);
                            break;
                        case GravityModel.DESCRIPTION:
                            ad.setDescription(itemNameValue.value);
                            break;
                        case GravityModel.IMAGEURL:
                            ad.setImageUrl(itemNameValue.value);
                            break;
                        case GravityModel.REGION:
                            ad.setRegion(itemNameValue.value);
                            break;
                        case GravityModel.AREA:
                            ad.setArea(itemNameValue.value);
                            break;
                        case GravityModel.ADTYPE:
                            ad.setAdType(itemNameValue.value);
                            break;
                        case GravityModel.COMPANY_AD:
                            ad.setCompanyAd(itemNameValue.value);
                            break;
                        case GravityModel.UPLOAD_TS:
                            ad.setUploadTimestamp(itemNameValue.value);
                            break;
                        case GravityModel.PRICE:
                            ad.setPrice(itemNameValue.value);
                            break;
                        case GravityModel.CATEGORY_ID:
                            ad.setCategoryId(itemNameValue.value);
                            break;
                        case GravityModel.CATEGORY_NAME:
                            ad.setCategoryName(itemNameValue.value);
                            break;
                        case GravityModel.URL:
                            ad.setUrl(itemNameValue.value);
                            break;
                        case GravityModel.SELLER_ID:
                            ad.setSellerId(itemNameValue.value);
                            break;
                        case GravityModel.DELETE_REASON:
                            ad.setDeleteReason(itemNameValue.value);
                            break;
                        case GravityModel.USED:
                            ad.setUsed(itemNameValue.value);
                            break;
                        case GravityModel.HIDDEN:
                            ad.setHidden(itemNameValue.value);
                            break;
                    }
                }
                gravityAdsList.add(ad);
            }

        }
        return gravityAdsList;
    }

    private static ArrayList<GravityModel> getItemRecommendationWithKeyValue(String scenarioId, int limit, GravityNameValue[] nameValues) {
        // Scenario id is indicating that the recommended items will be displayed
        // on the main page and it uses personalized recommendation logic.
        // the context of the recommendation is represented by a complex object
        GravityRecommendationContext recommendationContext = new GravityRecommendationContext();
        recommendationContext.scenarioId = scenarioId;
        recommendationContext.numberLimit = limit;
        recommendationContext.recommendationTime = recommendationTime;
        recommendationContext.resultNameValues = new String[]{"itemId", "title", "url", "imageURL", "price", "categoryID"};
        recommendationContext.nameValues = nameValues;
        return getItemRecommendation(recommendationContext);
    }

    public static List<GravityModel> getData(String scenario, int count) {
        return getItemRecommendationWithKeyValue(scenario, count, new GravityNameValue[0]);

    }

    public static ArrayList<GravityModel> getVisitedData(String scenario, int count) {
        return getItemRecommendationWithKeyValue(scenario, count, new GravityNameValue[0]);
    }

    public static ArrayList<GravityModel> getCategoryData(String scenario, int count, String categoryType, int offset) {
        GravityNameValue filter = new GravityNameValue("filter.categoryId", categoryType);
        GravityNameValue of = new GravityNameValue("pagingOffset", "" + offset);
        return getItemRecommendationWithKeyValue(scenario, count, new GravityNameValue[]{filter, of});
    }

    public static ArrayList<GravityModel> getSimilarItem(String scenario, int count, String itemId) {
        GravityNameValue pageItemId = new GravityNameValue(ITEM_ID, itemId);
        return getItemRecommendationWithKeyValue(scenario, count, new GravityNameValue[]{pageItemId});
    }

    public static void sendEventAsync(final GravityEvent event) {
        event.cookieId = cookieId;
        event.userId = userId;
        if (client != null) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        event.time = (int) (System.currentTimeMillis() / 1000);
                        GravityNameValue device = new GravityNameValue(DEVICE, APP);
                        event.nameValues = new GravityNameValue[] { device };
                        client.addEvents(new GravityEvent[]{event}, true);
                    } catch (GravityRecEngException e) {
                        Log.e("failed to send event", e);
                    } catch (IOException e) {
                        Log.e("failed to send event with IO", e);
                    }
                    return null;
                }
            }.execute();
        }
    }

    public static void sendEventAsyncWithEventType(String eventType) {
        sendEventAsyncWithEventType(eventType, null);
    }

    public static void sendEventAsyncWithEventTypeAndItemId(String eventType, String itemId) {
        if (!ACUtils.isEmpty(itemId)) {
            GravityModel model = new GravityModel();
            model.setItemId(itemId);
            sendEventAsyncWithEventType(eventType, model);
        }
    }

    public static void sendEventAsyncWithEventType(String eventType, GravityModel ad) {
        GravityEvent event = new GravityEvent();
        event.eventType = eventType;
        if (ad != null) {
            if (!ACUtils.isEmpty(ad.getItemId()))
                event.itemId = ad.getItemId();

            if (!ACUtils.isEmpty(ad.getRecommendationId()))
                event.recommendationId = ad.getRecommendationId();
        }
        sendEventAsync(event);
    }

    public void addEvents(GravityEvent[] events) throws IOException {
        if (client != null) {
            try {
                client.addEvents(events, false);
            } catch (GravityRecEngException e) {
                Log.e("Error failed to add events!");
                Log.e("Message: " + e.getMessage() + " Fault info: " + e.faultInfo);
            } catch (IOException ioe) {
                Log.e("IO error add events!");
                Log.e("Message: ", ioe);
            }
        }
    }

    public static void tagGravityEvent(String eventType, GravityModel item) {
        GravityModel model = new GravityModel();
        model.setRecommendationId(item.getRecommendationId());
        model.setItemId(item.getItemId());
        sendEventAsyncWithEventType(eventType, model);
    }

}
