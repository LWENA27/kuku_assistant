package com.example.fowltyphoidmonitor.data.requests;
import com.google.gson.annotations.SerializedName;

public class ReminderStatusRequest {
    @SerializedName("is_sent")
    private boolean isSent;

    public ReminderStatusRequest(boolean isSent) {
        this.isSent = isSent;
    }

    public boolean isSent() { return isSent; }
    public void setSent(boolean sent) { isSent = sent; }
}

