package com.example.fowltyphoidmonitor.ui.vet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fowltyphoidmonitor.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * VetConsultationInboxAdapter - RecyclerView adapter for vet consultation inbox
 *
 * Displays farmer consultation requests in an inbox-style format
 *
 * @author LWENA27
 * @created 2025-07-06
 */
public class VetConsultationInboxAdapter extends RecyclerView.Adapter<VetConsultationInboxAdapter.ViewHolder> {

    private List<ConsultationInboxItem> consultationList;
    private OnConsultationClickListener clickListener;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public interface OnConsultationClickListener {
        void onConsultationClick(ConsultationInboxItem consultation);
    }

    public VetConsultationInboxAdapter(List<ConsultationInboxItem> consultationList,
                                      OnConsultationClickListener clickListener) {
        this.consultationList = consultationList;
        this.clickListener = clickListener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consultation_inbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConsultationInboxItem consultation = consultationList.get(position);
        holder.bind(consultation);
    }

    @Override
    public int getItemCount() {
        return consultationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtFarmerName;
        private TextView txtFarmerEmail;
        private TextView txtQuestionPreview;
        private TextView txtDateTime;
        private TextView txtPriority;
        private TextView txtStatus;
        private TextView txtUnreadCount;
        private TextView txtTags;
        private ImageView imgPriorityIndicator;
        private ImageView imgStatusIndicator;
        private View viewUnreadIndicator;
        private View cardBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFarmerName = itemView.findViewById(R.id.txtFarmerName);
            txtFarmerEmail = itemView.findViewById(R.id.txtFarmerEmail);
            txtQuestionPreview = itemView.findViewById(R.id.txtQuestionPreview);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            txtPriority = itemView.findViewById(R.id.txtPriority);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtUnreadCount = itemView.findViewById(R.id.txtUnreadCount);
            txtTags = itemView.findViewById(R.id.txtTags);
            imgPriorityIndicator = itemView.findViewById(R.id.imgPriorityIndicator);
            imgStatusIndicator = itemView.findViewById(R.id.imgStatusIndicator);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
            cardBackground = itemView.findViewById(R.id.cardBackground);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onConsultationClick(consultationList.get(position));
                }
            });
        }

        public void bind(ConsultationInboxItem consultation) {
            // Farmer information
            txtFarmerName.setText(consultation.getFarmerName());
            txtFarmerEmail.setText(consultation.getFarmerEmail());

            // Question preview
            txtQuestionPreview.setText(consultation.getQuestionPreview());

            // Date and time
            if (consultation.getAskedAt() != null) {
                String dateStr = dateFormat.format(consultation.getAskedAt());
                String timeStr = timeFormat.format(consultation.getAskedAt());
                txtDateTime.setText(dateStr + " " + timeStr);
            }

            // Priority
            txtPriority.setText(consultation.getPriorityText());
            txtPriority.setTextColor(ContextCompat.getColor(itemView.getContext(), consultation.getPriorityColor()));

            // Status
            txtStatus.setText(consultation.getStatusText());

            // Priority indicator
            setPriorityIndicator(consultation.getPriority());

            // Status indicator
            setStatusIndicator(consultation.getStatus());

            // Unread messages
            if (consultation.hasUnreadMessages()) {
                txtUnreadCount.setVisibility(View.VISIBLE);
                txtUnreadCount.setText(String.valueOf(consultation.getUnreadReplies()));
                viewUnreadIndicator.setVisibility(View.VISIBLE);
            } else {
                txtUnreadCount.setVisibility(View.GONE);
                viewUnreadIndicator.setVisibility(View.GONE);
            }

            // Tags
            if (consultation.getTags() != null && !consultation.getTags().isEmpty()) {
                txtTags.setVisibility(View.VISIBLE);
                txtTags.setText(consultation.getTags());
            } else {
                txtTags.setVisibility(View.GONE);
            }

            // Card background highlighting
            setCardHighlighting(consultation);
        }

        private void setPriorityIndicator(String priority) {
            switch (priority.toLowerCase()) {
                case "urgent":
                    imgPriorityIndicator.setImageResource(R.drawable.ic_priority_urgent);
                    imgPriorityIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                    break;
                case "high":
                    imgPriorityIndicator.setImageResource(R.drawable.ic_priority_high);
                    imgPriorityIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
                    break;
                case "medium":
                    imgPriorityIndicator.setImageResource(R.drawable.ic_priority_medium);
                    imgPriorityIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark));
                    break;
                case "low":
                    imgPriorityIndicator.setImageResource(R.drawable.ic_priority_low);
                    imgPriorityIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    break;
                default:
                    imgPriorityIndicator.setImageResource(R.drawable.ic_help);
                    imgPriorityIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    break;
            }
        }

        private void setStatusIndicator(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    imgStatusIndicator.setImageResource(R.drawable.ic_pending);
                    imgStatusIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_light));
                    break;
                case "answered":
                    imgStatusIndicator.setImageResource(R.drawable.ic_check_circle);
                    imgStatusIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                    break;
                case "closed":
                    imgStatusIndicator.setImageResource(R.drawable.ic_lock);
                    imgStatusIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    break;
                default:
                    imgStatusIndicator.setImageResource(R.drawable.ic_help);
                    imgStatusIndicator.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                    break;
            }
        }

        private void setCardHighlighting(ConsultationInboxItem consultation) {
            if (consultation.isUrgent()) {
                // Urgent consultations get red border
                cardBackground.setBackgroundResource(R.drawable.card_urgent_consultation);
            } else if (consultation.hasUnreadMessages()) {
                // Unread consultations get highlighted background
                cardBackground.setBackgroundResource(R.drawable.card_unread_consultation);
            } else if ("pending".equals(consultation.getStatus())) {
                // Pending consultations get subtle highlight
                cardBackground.setBackgroundResource(R.drawable.card_pending_consultation);
            } else {
                // Default card background
                cardBackground.setBackgroundResource(R.drawable.card_default_consultation);
            }
        }
    }
}
