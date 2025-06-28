package com.example.fowltyphoidmonitor.services.auth;
import com.example.fowltyphoidmonitor.models.Farmer;
import com.example.fowltyphoidmonitor.models.Vet;

public interface ProfileCallback {
    void onFarmerProfileLoaded(Farmer farmer);
    void onVetProfileLoaded(Vet vet);
    void onError(String error);
}
