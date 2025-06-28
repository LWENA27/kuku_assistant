package com.example.fowltyphoidmonitor.ui.common;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportData {
    private long id;
    private String reportType;
    private String farmName;
    private String farmLocation;
    private int animalCount;
    private String symptoms;
    private String duration;
    private String additionalInfo;
    private String severity;
    private String urgency;
    private String timestamp;
    private String status;

    // Constructors
    public ReportData() {}

    public ReportData(long id, String reportType, String farmName, String farmLocation,
                      int animalCount, String symptoms, String duration, String additionalInfo,
                      String severity, String urgency, String timestamp, String status) {
        this.id = id;
        this.reportType = reportType;
        this.farmName = farmName;
        this.farmLocation = farmLocation;
        this.animalCount = animalCount;
        this.symptoms = symptoms;
        this.duration = duration;
        this.additionalInfo = additionalInfo;
        this.severity = severity;
        this.urgency = urgency;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public String getFarmLocation() { return farmLocation; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }

    public int getAnimalCount() { return animalCount; }
    public void setAnimalCount(int animalCount) { this.animalCount = animalCount; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Convert to JSON string for storage
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("reportType", reportType);
            json.put("farmName", farmName);
            json.put("farmLocation", farmLocation);
            json.put("animalCount", animalCount);
            json.put("symptoms", symptoms);
            json.put("duration", duration);
            json.put("additionalInfo", additionalInfo);
            json.put("severity", severity);
            json.put("urgency", urgency);
            json.put("timestamp", timestamp);
            json.put("status", status);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create from JSON string
    public static ReportData fromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            ReportData report = new ReportData();
            report.setId(json.getLong("id"));
            report.setReportType(json.getString("reportType"));
            report.setFarmName(json.getString("farmName"));
            report.setFarmLocation(json.getString("farmLocation"));
            report.setAnimalCount(json.getInt("animalCount"));
            report.setSymptoms(json.getString("symptoms"));
            report.setDuration(json.optString("duration", ""));
            report.setAdditionalInfo(json.optString("additionalInfo", ""));
            report.setSeverity(json.getString("severity"));
            report.setUrgency(json.getString("urgency"));
            report.setTimestamp(json.getString("timestamp"));
            report.setStatus(json.getString("status"));
            return report;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "ReportData{" +
                "id=" + id +
                ", reportType='" + reportType + '\'' +
                ", farmName='" + farmName + '\'' +
                ", farmLocation='" + farmLocation + '\'' +
                ", animalCount=" + animalCount +
                ", severity='" + severity + '\'' +
                ", urgency='" + urgency + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}