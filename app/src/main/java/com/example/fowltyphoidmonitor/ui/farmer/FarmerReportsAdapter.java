package com.example.fowltyphoidmonitor.ui.farmer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fowltyphoidmonitor.R;
import com.google.android.material.chip.Chip;

import java.util.List;

public class FarmerReportsAdapter extends RecyclerView.Adapter<FarmerReportsAdapter.ReportViewHolder> {

    private List<FarmerReportsActivity.ReportItem> reportsList;
    private Context context;
    private OnReportClickListener onReportClickListener;

    // Interface for handling report clicks
    public interface OnReportClickListener {
        void onReportClick(FarmerReportsActivity.ReportItem report);
    }

    public FarmerReportsAdapter(List<FarmerReportsActivity.ReportItem> reportsList, OnReportClickListener listener) {
        this.reportsList = reportsList;
        this.onReportClickListener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_farmer_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        FarmerReportsActivity.ReportItem report = reportsList.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    // Method to update the reports list (for filtering)
    public void updateReports(List<FarmerReportsActivity.ReportItem> newReportsList) {
        this.reportsList = newReportsList;
        notifyDataSetChanged();
    }

    // ViewHolder class
    public class ReportViewHolder extends RecyclerView.ViewHolder {
        private CardView cardReport;
        private TextView txtReportTitle;
        private TextView txtReportId;
        private TextView txtReportDescription;
        private TextView txtSubmissionDate;
        private TextView txtAffectedBirds;
        private TextView txtLocation;
        private Chip chipStatus;
        private Chip chipSeverity;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            cardReport = itemView.findViewById(R.id.cardReport);
            txtReportTitle = itemView.findViewById(R.id.txtReportTitle);
            txtReportId = itemView.findViewById(R.id.txtReportId);
            txtReportDescription = itemView.findViewById(R.id.txtReportDescription);
            txtSubmissionDate = itemView.findViewById(R.id.txtSubmissionDate);
            txtAffectedBirds = itemView.findViewById(R.id.txtAffectedBirds);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            chipSeverity = itemView.findViewById(R.id.chipSeverity);
        }

        public void bind(FarmerReportsActivity.ReportItem report) {
            // Set basic information
            txtReportTitle.setText(report.getTitle());
            txtReportId.setText("ID: " + report.getId());
            txtReportDescription.setText(report.getDescription());
            txtSubmissionDate.setText("Tarehe: " + report.getSubmissionDate());
            txtAffectedBirds.setText("Kuku walioathiriwa: " + report.getAffectedBirds());
            txtLocation.setText("Eneo: " + report.getLocation());

            // Set status chip
            chipStatus.setText(getStatusText(report.getStatus()));
            setStatusChipColor(chipStatus, report.getStatus());

            // Set severity chip
            chipSeverity.setText(getSeverityText(report.getSeverity()));
            setSeverityChipColor(chipSeverity, report.getSeverity());

            // Set click listener
            cardReport.setOnClickListener(v -> {
                if (onReportClickListener != null) {
                    onReportClickListener.onReportClick(report);
                }
            });

            // Add ripple effect for better user experience
            cardReport.setClickable(true);
            cardReport.setFocusable(true);
        }

        private String getStatusText(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return "Inasubiri";
                case "approved":
                    return "Imeidhinishwa";
                case "rejected":
                    return "Imekataliwa";
                case "under_review":
                    return "Inakaguliwa";
                default:
                    return "Haijulikani";
            }
        }

        private String getSeverityText(String severity) {
            switch (severity.toLowerCase()) {
                case "mild":
                    return "Nyepesi";
                case "moderate":
                    return "Wastani";
                case "severe":
                    return "Kali";
                case "critical":
                    return "Hatari";
                default:
                    return "Kawaida";
            }
        }

        private void setStatusChipColor(Chip chip, String status) {
            int backgroundColor;
            int textColor = ContextCompat.getColor(context, android.R.color.white);

            switch (status.toLowerCase()) {
                case "pending":
                    backgroundColor = ContextCompat.getColor(context, R.color.accent_color);
                    break;
                case "approved":
                    backgroundColor = ContextCompat.getColor(context, R.color.primary_color);
                    break;
                case "rejected":
                    backgroundColor = ContextCompat.getColor(context, R.color.error_color);
                    break;
                case "under_review":
                    backgroundColor = ContextCompat.getColor(context, R.color.bottom_nav_selected);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.info_color);
                    break;
            }

            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(context, android.R.color.transparent));
            chip.getBackground().setTint(backgroundColor);
            chip.setTextColor(textColor);
        }

        private void setSeverityChipColor(Chip chip, String severity) {
            int backgroundColor;
            int textColor = ContextCompat.getColor(context, android.R.color.white);

            switch (severity.toLowerCase()) {
                case "mild":
                    backgroundColor = ContextCompat.getColor(context, R.color.primary_dark_color);
                    break;
                case "moderate":
                    backgroundColor = ContextCompat.getColor(context, R.color.primary_color_light);
                    break;
                case "severe":
                    backgroundColor = ContextCompat.getColor(context, R.color.warning_color);
                    break;
                case "critical":
                    backgroundColor = ContextCompat.getColor(context, R.color.accent_color);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.primary_color);
                    break;
            }

            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(context, android.R.color.transparent));
            chip.getBackground().setTint(backgroundColor);
            chip.setTextColor(textColor);
        }
    }
}