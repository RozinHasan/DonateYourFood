package com.rozin.donateyourfood.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class rawPushModel {
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("iconUrl")
    @Expose
    private String iconUrl;
    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("action_destination")
    @Expose
    private String actionDestination;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("email")
    @Expose
    private String email;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionDestination() {
        return actionDestination;
    }

    public void setActionDestination(String actionDestination) {
        this.actionDestination = actionDestination;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
