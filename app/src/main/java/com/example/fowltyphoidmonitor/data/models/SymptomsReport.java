package com.example.fowltyphoidmonitor.data.models;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class SymptomsReport {
    @SerializedName("report_id")
    private Integer reportId;

    @SerializedName("farmer_id")
    private Integer farmerId;

    @SerializedName("symptom_reported")
    private String symptomReported;

    @SerializedName("reported_at")
    private Date reportedAt;

    @SerializedName("severity")
    private String severity;

    @SerializedName("affected_chickens")
    private int affectedChickens;

    @SerializedName("total_chickens")
    private int totalChickens;

    @SerializedName("additional_notes")
    private String additionalNotes;

    @SerializedName("status")
    private String status; // "pending", "reviewed", "resolved"

    // Constructors
    public SymptomsReport() {}

    public SymptomsReport(Integer farmerId, String symptomReported, String severity,
                          int affectedChickens, int totalChickens, String additionalNotes) {
        this.farmerId = farmerId;
        this.symptomReported = symptomReported;
        this.severity = severity;
        this.affectedChickens = affectedChickens;
        this.totalChickens = totalChickens;
        this.additionalNotes = additionalNotes;
        this.reportedAt = new Date();
        this.status = "pending";
    }

    // Getters and Setters
    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public Integer getFarmerId() { return farmerId; }
    public void setFarmerId(Integer farmerId) { this.farmerId = farmerId; }

    public String getSymptomReported() { return symptomReported; }
    public void setSymptomReported(String symptomReported) { this.symptomReported = symptomReported; }

    public Date getReportedAt() { return reportedAt; }
    public void setReportedAt(Date reportedAt) { this.reportedAt = reportedAt; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public int getAffectedChickens() { return affectedChickens; }
    public void setAffectedChickens(int affectedChickens) { this.affectedChickens = affectedChickens; }

    public int getTotalChickens() { return totalChickens; }
    public void setTotalChickens(int totalChickens) { this.totalChickens = totalChickens; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}