package com.mudah.my.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * User: RobGThai
 * Date: 9/27/13 AD
 * Time: 2:32 PM
 */
public class ChatImageItem implements Parcelable, Comparable<ChatImageItem> {

    @SerializedName("post_id")
    private int postId;

    private int id;
    private String url;
    private String uri;
    @SerializedName("rank")
    private int order = -1;
    private String name;
    private String type;

    @SerializedName("rotate")
    private int angle;

    @SerializedName("thumb")
    private String thumbUrl;
    @SerializedName("medium")
    private String mediumUrl;
    @SerializedName("large")
    private String largeUrl;

    public ChatImageItem() {

    }

    public ChatImageItem(Parcel in) {
        postId = in.readInt();
        id = in.readInt();
        url = in.readString();
        uri = in.readString();
        order = in.readInt();
        angle = in.readInt();
        name = in.readString();
        type = in.readString();

        thumbUrl = in.readString();
        mediumUrl = in.readString();
        largeUrl = in.readString();
    }

    public static final Creator<ChatImageItem> CREATOR = new Creator<ChatImageItem>() {
        @Override
        public ChatImageItem createFromParcel(Parcel source) {
            return new ChatImageItem(source);
        }

        @Override
        public ChatImageItem[] newArray(int size) {
            return new ChatImageItem[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     *         by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(postId);
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(uri);
        dest.writeInt(order);
        dest.writeInt(angle);
        dest.writeString(name);
        dest.writeString(type);

        dest.writeString(thumbUrl);
        dest.writeString(mediumUrl);
        dest.writeString(largeUrl);
    }

    public String getBestQualityImage() {
        if(!TextUtils.isEmpty(largeUrl)) {
            return largeUrl;
        }else if(!TextUtils.isEmpty(mediumUrl)) {
            return mediumUrl;
        }else {
            return thumbUrl;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getMediumUrl() {
        return mediumUrl;
    }

    public void setMediumUrl(String mediumUrl) {
        this.mediumUrl = mediumUrl;
    }

    public String getLargeUrl() {
        return largeUrl;
    }

    public void setLargeUrl(String largeUrl) {
        this.largeUrl = largeUrl;
    }

    @Override
    public int compareTo(ChatImageItem dfImageItem) {

        if (getId() != dfImageItem.getId())
            return 1;

        return 0;
    }
}
