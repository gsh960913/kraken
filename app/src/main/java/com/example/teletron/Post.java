package com.example.teletron;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Post {

    @Expose
    @SerializedName("uuid")
    private String uuid;
    @Expose
    @SerializedName("date")
    private String date;
    @Expose
    @SerializedName("reason")
    private String reason;

    public Post(String uuid, String date, String reason) {
        this.uuid = uuid;
        this.date = date;
        this.reason = reason;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
