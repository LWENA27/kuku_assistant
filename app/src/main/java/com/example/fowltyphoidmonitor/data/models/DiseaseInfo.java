package com.example.fowltyphoidmonitor.data.models;

import com.google.gson.annotations.SerializedName;

public class DiseaseInfo {
    @SerializedName("disease_id")
    private Integer diseaseId;

    @SerializedName("name")
    private String name;

    @SerializedName("causes")
    private String causes;

    @SerializedName("symptoms")
    private String symptoms;

    @SerializedName("treatment")
    private String treatment;

    @SerializedName("prevention")
    private String prevention;

    @SerializedName("description")
    private String description;

    // Constructors
    public DiseaseInfo() {}

    public DiseaseInfo(Integer diseaseId, String name, String causes, String symptoms,
                       String treatment, String prevention, String description) {
        this.diseaseId = diseaseId;
        this.name = name;
        this.causes = causes;
        this.symptoms = symptoms;
        this.treatment = treatment;
        this.prevention = prevention;
        this.description = description;
    }

    // Getters and Setters
    public Integer getDiseaseId() { return diseaseId; }
    public void setDiseaseId(Integer diseaseId) { this.diseaseId = diseaseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCauses() { return causes; }
    public void setCauses(String causes) { this.causes = causes; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getPrevention() { return prevention; }
    public void setPrevention(String prevention) { this.prevention = prevention; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}