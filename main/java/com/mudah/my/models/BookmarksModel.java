package com.mudah.my.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by thanhnd on 6/18/14.
 */
public class BookmarksModel {

    public final static boolean PLACEHOLDER_BOOKMARK = true;
    public final static boolean ACTUAL_BOOKMARK = false;
    public static int numberOfListIdSaved = 3;

    @SerializedName("id")
    private long id;
    @SerializedName("name")
    private String name;
    @SerializedName("query")
    private String query;
    @SerializedName("filter")
    private String filter;
    @SerializedName("createdTime")
    private long createdTime;
    @SerializedName("updatedTime")
    private long updatedTime;
    @SerializedName("listIds")
    private String listIds;
    @SerializedName("selected")
    private boolean selected = false;
    @SerializedName("placeholder")
    private boolean placeholder;

    public BookmarksModel(long id, String name, String query, String filter, long createdTime) {
        this(id, name, query, filter, createdTime, ACTUAL_BOOKMARK, 0l, "");
    }

    public BookmarksModel(long id, String name, String query, String filter, long createdTime, boolean placeholder) {
        this(id, name, query, filter, createdTime, placeholder, 0l, "");
    }

    public BookmarksModel(long id, String name, String query, String filter, long createdTime, long updatedTime, String listIds) {
        this(id, name, query, filter, createdTime, ACTUAL_BOOKMARK, updatedTime, listIds);
    }

    public BookmarksModel(long id, String name, String query, String filter, long createdTime, boolean placeholder, long updatedTime, String listIds) {
        this.id = id;
        this.name = name;
        this.query = query;
        this.filter = filter;
        this.createdTime = createdTime;
        this.placeholder = placeholder;
        this.updatedTime = updatedTime;
        this.listIds = listIds;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedTime() {
        return new Date(createdTime);
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public boolean getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    public String getListIds() {
        return listIds;
    }

    public void setListIds(String listIds) {
        this.listIds = listIds;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "Bookmarks{" +
                "id=" + id +
                ", query='" + query + '\'' +
                ", filter='" + filter + '\'' +
                ", createdTime=" + createdTime + '\'' +
                ", updatedTime=" + updatedTime + '\'' +
                ", listIds=" + listIds +
                '}';
    }
}

