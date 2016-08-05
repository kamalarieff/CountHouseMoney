package com.mudah.my.models;

import com.google.gson.annotations.SerializedName;
import com.mudah.my.configs.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thanhnd on 6/11/14.
 */
public class ListAdModel {

    @SerializedName("id")
    private long id;
    @SerializedName("adId")
    private String adId;
    @SerializedName("adSubject")
    private String adSubject;
    @SerializedName("adPrice")
    private String adPrice;
    @SerializedName("adTime")
    private String adTime;
    @SerializedName("adImgUrl")
    private String adImgUrl;
    @SerializedName("adCategoryId")
    private int adCategoryId;
    @SerializedName("createdTime")
    private long createdTime;
    @SerializedName("selected")
    private boolean selected = false;
    @SerializedName("companyAd")
    private String companyAd;
    @SerializedName("imgCount")
    private int imgCount;
    @SerializedName("verified")
    private String verified;

    public ListAdModel() {
    }

    public ListAdModel(String item) throws JSONException {
        super();
        JSONObject jsonObject = new JSONObject(item);
        if (jsonObject.length() > 0) {
            this.companyAd = jsonObject.getString("company_ad");
            this.adSubject = jsonObject.getString("subject");
            this.adPrice = jsonObject.getString("price");
            this.adTime = jsonObject.getString("date");
            this.adImgUrl = jsonObject.getString("image");
            this.adId = jsonObject.getString("list_id");
            this.imgCount = jsonObject.getInt("image_count");
            this.verified = jsonObject.getString("store_verified");
            this.adCategoryId = jsonObject.getInt(Constants.CATEGORY_TXT);

        }
    }

    public ListAdModel(String adId, String adSubject) {
        super();
        this.adId = adId;
        this.adSubject = adSubject;
    }

    public String getCompanyAd() {
        return companyAd;
    }

    public void setCompanyAd(String companyAd) {
        this.companyAd = companyAd;
    }

    public String getVerified() { return verified; }

    public void setVerified(String verified) { this.verified = verified; }

    public int getImgCount() {
        return imgCount;
    }

    public void setImgCount(int imgCount) {
        this.imgCount = imgCount;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    //getters & setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdSubject() {
        return adSubject;
    }

    public void setAdSubject(String adSubject) {
        this.adSubject = adSubject;
    }

    public String getAdPrice() {
        return adPrice;
    }

    public void setAdPrice(String adPrice) {
        this.adPrice = adPrice;
    }

    public String getAdTime() {
        return adTime;
    }

    public void setAdTime(String adTime) {
        this.adTime = adTime;
    }

    public String getAdImgUrl() {
        return adImgUrl;
    }

    public void setAdImgUrl(String adImgUrl) {
        this.adImgUrl = adImgUrl;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public int getAdCategoryId() {
        return adCategoryId;
    }

    public void setAdCategoryId(int adCategoryId) {
        this.adCategoryId = adCategoryId;
    }

    @Override
    public String toString() {

        return "AdViewFavourite {" +
                "id=" + id +
                ", companyAd='" + companyAd + '\'' +
                ", adId='" + adId + '\'' +
                ", adSubject='" + adSubject + '\'' +
                ", adPrice='" + adPrice + '\'' +
                ", adTime='" + adTime + '\'' +
                ", adImgUrl='" + adImgUrl + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", verified='" + verified + '\'' +
                '}';
    }
}

