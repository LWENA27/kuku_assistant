package com.example.fowltyphoidmonitor.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fowltyphoidmonitor.ui.farmer.FarmerConsultationsActivity;
import com.example.fowltyphoidmonitor.R;

import java.util.List;

public class ConsultationsAdapter extends RecyclerView.Adapter<ConsultationsAdapter.ConsultationViewHolder> {

    private List<FarmerConsultationsActivity.ConsultationItem> consultations;
    private FarmerConsultationsActivity.OnConsultationItemClickListener clickListener;

    public ConsultationsAdapter(List<FarmerConsultationsActivity.ConsultationItem> consultations,
                                FarmerConsultationsActivity.OnConsultationItemClickListener clickListener) {
        this.consultations = consultations;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ConsultationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consultation, parent, false);
        return new ConsultationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultationViewHolder holder, int position) {
        FarmerConsultationsActivity.ConsultationItem consultation = consultations.get(position);
        holder.bind(consultation, clickListener);
    }

    @Override
    public int getItemCount() {
        return consultations.size();
    }

    static class ConsultationViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView txtConsultationType, txtPatientName, txtDate, txtTime;
        private TextView txtStatus, txtUrgency, txtSymptoms;
        private View statusIndicator;

        public ConsultationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            txtConsultationType = itemView.findViewById(R.id.txtConsultationType);
            txtPatientName = itemView.findViewById(R.id.txtPatientName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtUrgency = itemView.findViewById(R.id.txtUrgency);
            txtSymptoms = itemView.findViewById(R.id.txtSymptoms);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(FarmerConsultationsActivity.ConsultationItem consultation,
                         FarmerConsultationsActivity.OnConsultationItemClickListener clickListener) {

            txtConsultationType.setText(consultation.consultationType);
            txtPatientName.setText(consultation.patientName);
            txtDate.setText(consultation.preferredDate);
            txtTime.setText(consultation.preferredTime);
            txtStatus.setText(consultation.getStatusInSwahili());
            txtUrgency.setText(consultation.urgencyLevel);

            // Show first 100 characters of symptoms
            String symptoms = consultation.symptoms;
            if (symptoms.length() > 100) {
                symptoms = symptoms.substring(0, 100) + "...";
            }
            txtSymptoms.setText(symptoms);

            // Set status color
            txtStatus.setTextColor(consultation.getStatusColor());
            statusIndicator.setBackgroundColor(consultation.getStatusColor());

            // Set urgency text color
            if (consultation.urgencyLevel.contains("Haraka Sana")) {
                txtUrgency.setTextColor(0xFFF44336); // Red
            } else if (consultation.urgencyLevel.contains("Haraka")) {
                txtUrgency.setTextColor(0xFFFF9800); // Orange
            } else {
                txtUrgency.setTextColor(0xFF757575); // Gray
            }

            // Set click listener
            cardView.setOnClickListener(v -> clickListener.onConsultationItemClick(consultation));
        }
    }
}