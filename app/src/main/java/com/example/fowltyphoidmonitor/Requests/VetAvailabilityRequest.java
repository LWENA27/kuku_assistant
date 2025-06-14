package com.example.fowltyphoidmonitor.Requests;
import com.google.gson.annotations.SerializedName;

public class VetAvailabilityRequest {
    @SerializedName("is_available")
    private boolean isAvailable;

    public VetAvailabilityRequest(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
