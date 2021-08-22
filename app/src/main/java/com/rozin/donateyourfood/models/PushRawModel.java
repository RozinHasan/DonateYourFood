package com.rozin.donateyourfood.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PushRawModel {

    @SerializedName("device_token")
    @Expose
    private List<String> deviceToken = null;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lng")
    @Expose
    private String lng;
    @SerializedName("uid")
    @Expose
    private String uid;

    public List<String> getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(List<String> deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}