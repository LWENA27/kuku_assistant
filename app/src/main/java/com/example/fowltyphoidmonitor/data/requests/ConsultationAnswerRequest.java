package com.example.fowltyphoidmonitor.data.requests;
import com.google.gson.annotations.SerializedName;

public class ConsultationAnswerRequest {
    @SerializedName("answer")
    private String answer;

    @SerializedName("vet_id")
    private String vetId;

    @SerializedName("status")
    private String status;

    public ConsultationAnswerRequest(String answer, String vetId) {
        this.answer = answer;
        this.vetId = vetId;
        this.status = "answered";
    }
    // Getters and Setters
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getVetId() { return vetId; }
    public void setVetId(String vetId) { this.vetId = vetId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

