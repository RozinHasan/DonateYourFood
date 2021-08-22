package com.rozin.donateyourfood.models;

public class ItemModel {
    private String organization;
    private String address;
    private String zipcode;
    private String phone;
    private String peoplAapprox;
    private String postTime;
    private String status;
    private String name;
    private String userName;
    private String uid;
    private String acceptedId;
    private String objectId;
    private String city;
    private int rating;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getAcceptedId() {
        return acceptedId;
    }

    public void setAcceptedId(String acceptedId) {
        this.acceptedId = acceptedId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPeoplAapprox() {
        return peoplAapprox;
    }

    public void setPeoplAapprox(String peoplAapprox) {
        this.peoplAapprox = peoplAapprox;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }
}
