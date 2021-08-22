package com.rozin.donateyourfood.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FirebaseResponse {
    @SerializedName("multicast_id")
    @Expose
    private long multicastId;
    @SerializedName("success")
    @Expose
    private long success;
    @SerializedName("failure")
    @Expose
    private long failure;
    @SerializedName("results")
    @Expose
    private List<Result> results = null;

    public long getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(long multicastId) {
        this.multicastId = multicastId;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFailure() {
        return failure;
    }

    public void setFailure(long failure) {
        this.failure = failure;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

}
