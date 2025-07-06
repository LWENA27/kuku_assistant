package com.example.fowltyphoidmonitor.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.UUID;

/**
 * Model representing a symptom report submitted by a farmer
 */
public class SymptomReport {
    @SerializedName("id")
    private String id;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("farmer_id")
    private String farmerId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("symptoms")
    private String symptoms;

    @SerializedName("flock_size")
    private Integer flockSize;

    @SerializedName("bird_age")
    private Integer birdAge;

    @SerializedName("mortality_rate")
    private Double mortalityRate;

    @SerializedName("temperature")
    private Double temperature;

    @SerializedName("image_urls")
    private String[] imageUrls;

    @SerializedName("status")
    private String status;

    @SerializedName("diagnosis")
    private String diagnosis;

    @SerializedName("recommendations")
    private String recommendations;

    public SymptomReport() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.status = "PENDING";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Integer getFlockSize() {
        return flockSize;
    }

    public void setFlockSize(Integer flockSize) {
        this.flockSize = flockSize;
    }

    public Integer getBirdAge() {
        return birdAge;
    }

    public void setBirdAge(Integer birdAge) {
        this.birdAge = birdAge;
    }

    public Double getMortalityRate() {
        return mortalityRate;
    }

    public void setMortalityRate(Double mortalityRate) {
        this.mortalityRate = mortalityRate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
}
