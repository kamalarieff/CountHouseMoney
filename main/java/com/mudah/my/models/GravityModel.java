package com.mudah.my.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.lib701.utils.ACUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ondo on 12/11/15.
 */
public class GravityModel implements Parcelable {
    public static final Parcelable.Creator<GravityModel> CREATOR
            = new Parcelable.Creator<GravityModel>() {
        public GravityModel createFromParcel(Parcel in) {
            return new GravityModel(in);
        }

        public GravityModel[] newArray(int size) {
            return new GravityModel[size];
        }
    };
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String IMAGEURL = "imageurl";
    public static final String REGION = "region";
    public static final String AREA = "area";
    public static final String ADTYPE = "adtype";
    public static final String COMPANY_AD = "company_ad";
    public static final String UPLOAD_TS = "uploadtimestamp";
    public static final String PRICE = "price";
    public static final String CATEGORY_ID = "categoryid";
    public static final String CATEGORY_NAME = "categoryname";
    public static final String URL = "url";
    public static final String SELLER_ID = "sellerid";
    public static final String DELETE_REASON = "delete_reason";
    public static final String USED = "used";
    public static final String HIDDEN = "hidden";
    public static final String ITEM_ID = "itemid";
    private String itemId;
    private String title;
    private String description;
    private String imageUrl;
    private String region;
    private String area;
    private String adType;
    private String companyAd;
    private String uploadTimestamp;
    private String price;
    private String categoryId;
    private String categoryName;
    private String url;
    private String sellerId;
    private String deleteReason;
    private String used;
    private String hidden;
    private String recommendationId;

    public GravityModel(Parcel in) {
        List<String> strings = new ArrayList<String>();
        in.readStringList(strings);
        itemId = strings.get(0);
        title = strings.get(1);
        description = strings.get(2);
        imageUrl = strings.get(3);
        region = strings.get(4);
        area = strings.get(5);
        adType = strings.get(6);
        companyAd = strings.get(7);
        uploadTimestamp = strings.get(8);
        price = strings.get(9);
        categoryId = strings.get(10);
        categoryName = strings.get(11);
        url = strings.get(12);
        sellerId = strings.get(13);
        deleteReason = strings.get(14);
        used = strings.get(15);
        hidden = strings.get(16);
    }

    public GravityModel() {
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(String recommendationId) {
        this.recommendationId = recommendationId;
    }

    public String getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(String uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getCompanyAd() {
        return companyAd;
    }

    public void setCompanyAd(String companyAd) {
        this.companyAd = companyAd;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        try {
            return (String.format("%,d", Integer.parseInt(price))).replace(',', ' ');
        } catch (Exception exp) {
            //if price is not in integer format, show it as is
            return price;
        }
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLargeImageUrl() {
        if (!ACUtils.isEmpty(imageUrl)) {
            return imageUrl.replace("thumbs", "images");
        }
        return imageUrl;
    }

    public String getAdType() {
        return adType;
    }

    public void setAdType(String itemType) {
        this.adType = itemType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        List<String> strings = new ArrayList<String>();

        strings.add(itemId);
        strings.add(title);
        strings.add(description);
        strings.add(imageUrl);
        strings.add(region);
        strings.add(area);
        strings.add(adType);
        strings.add(companyAd);
        strings.add(uploadTimestamp);
        strings.add(price);
        strings.add(categoryId);
        strings.add(categoryName);
        strings.add(url);
        strings.add(sellerId);
        strings.add(deleteReason);
        strings.add(used);
        strings.add(hidden);
        dest.writeStringList(strings);
    }
}
