package com.example.fowltyphoidmonitor.data.models;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Consultation {
    @SerializedName("consultation_id")
    private Integer consultationId;

    @SerializedName("farmer_id")
    private Integer farmerId;

    @SerializedName("vet_id")
    private Integer vetId;

    @SerializedName("question")
    private String question;

    @SerializedName("answer")
    private String answer;

    @SerializedName("asked_at")
    private Date askedAt;

    @SerializedName("answered_at")
    private Date answeredAt;

    @SerializedName("status")
    private String status; // "pending", "answered", "closed"

    @SerializedName("priority")
    private String priority; // "low", "medium", "high", "urgent"

    // Constructors
    public Consultation() {}

    public Consultation(Integer farmerId, String question, String priority) {
        this.farmerId = farmerId;
        this.question = question;
        this.priority = priority;
        this.askedAt = new Date();
        this.status = "pending";
    }

    // Getters and Setters
    public Integer getConsultationId() { return consultationId; }
    public void setConsultationId(Integer consultationId) { this.consultationId = consultationId; }

    public Integer getFarmerId() { return farmerId; }
    public void setFarmerId(Integer farmerId) { this.farmerId = farmerId; }

    public Integer getVetId() { return vetId; }
    public void setVetId(Integer vetId) { this.vetId = vetId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Date getAskedAt() { return askedAt; }
    public void setAskedAt(Date askedAt) { this.askedAt = askedAt; }

    public Date getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Date answeredAt) { this.answeredAt = answeredAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    // Add getId method for compatibility - returns consultationId
    public Integer getId() { 
        return consultationId; 
    }
    
    // Overload setFarmerId method to accept a String parameter
    public void setFarmerId(String farmerIdStr) {
        try {
            this.farmerId = Integer.parseInt(farmerIdStr);
        } catch (NumberFormatException e) {
            // Handle the case where the ID isn't a valid integer
            this.farmerId = null;
        }
    }
}