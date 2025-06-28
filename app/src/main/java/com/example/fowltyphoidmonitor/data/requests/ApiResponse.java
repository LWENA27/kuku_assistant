package com.example.fowltyphoidmonitor.data.requests;
import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    // Constructors
    public ApiResponse() {}

    public ApiResponse(T data, boolean success) {
        this.data = data;
        this.success = success;
    }

    // Getters and Setters
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
