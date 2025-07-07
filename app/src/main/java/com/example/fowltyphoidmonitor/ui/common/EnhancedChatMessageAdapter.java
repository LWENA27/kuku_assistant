package com.example.fowltyphoidmonitor.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Enhanced Chat Message Adapter for Robust Consultation Interface
 * Supports different message types, professional badges, and quick actions
 *
 * @author LWENA27
 * @created 2025-07-07
 */
public class EnhancedChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;

    private Context context;
    private List<ConsultationMessage> messages;
    private String currentUserId;
    private OnMessageActionListener actionListener;

    // Interface for handling message actions
    public interface OnMessageActionListener {
        void onMarkResolved(ConsultationMessage message);
        void onNeedMoreInfo(ConsultationMessage message);
        void onAttachmentClick(ConsultationMessage message);
        void onQuickReply(String replyText);
    }

    public EnhancedChatMessageAdapter(Context context, List<ConsultationMessage> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ConsultationMessage message = messages.get(position);

        if (message.getMessageType() != null && message.getMessageType().equals("system")) {
            return VIEW_TYPE_SYSTEM;
        }

        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case VIEW_TYPE_SENT:
                View sentView = inflater.inflate(R.layout.item_chat_message_sent, parent, false);
                return new SentMessageViewHolder(sentView);

            case VIEW_TYPE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_chat_message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView);

            case VIEW_TYPE_SYSTEM:
                View systemView = inflater.inflate(R.layout.item_chat_system_message, parent, false);
                return new SystemMessageViewHolder(systemView);

            default:
                View defaultView = inflater.inflate(R.layout.item_chat_message_received, parent, false);
                return new ReceivedMessageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConsultationMessage message = messages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT:
                bindSentMessage((SentMessageViewHolder) holder, message);
                break;

            case VIEW_TYPE_RECEIVED:
                bindReceivedMessage((ReceivedMessageViewHolder) holder, message);
                break;

            case VIEW_TYPE_SYSTEM:
                bindSystemMessage((SystemMessageViewHolder) holder, message);
                break;
        }
    }

    private void bindSentMessage(SentMessageViewHolder holder, ConsultationMessage message) {
        holder.txtMessage.setText(message.getMessageText());
        holder.txtTimestamp.setText(formatTimestamp(message.getCreatedAt()));

        // Handle message status
        updateMessageStatus(holder.imgMessageStatus, message);

        // Handle attachments
        if (message.hasAttachment()) {
            holder.attachmentPreview.setVisibility(View.VISIBLE);
            // Load attachment preview
            loadAttachmentPreview(holder.imgAttachment, message.getAttachmentUrl());

            holder.attachmentPreview.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAttachmentClick(message);
                }
            });
        } else {
            holder.attachmentPreview.setVisibility(View.GONE);
        }
    }

    private void bindReceivedMessage(ReceivedMessageViewHolder holder, ConsultationMessage message) {
        holder.txtMessage.setText(message.getMessageText());
        holder.txtTimestamp.setText(formatTimestamp(message.getCreatedAt()));
        holder.txtSenderName.setText(message.getSenderName());

        // Load sender avatar
        loadSenderAvatar(holder.imgSenderAvatar, message.getSenderRole(), message.getSenderName());

        // Show professional badge for vets
        if ("vet".equals(message.getSenderRole()) || "admin".equals(message.getSenderRole())) {
            holder.chipProfessionalBadge.setVisibility(View.VISIBLE);
            holder.chipProfessionalBadge.setText("Daktari");
        } else {
            holder.chipProfessionalBadge.setVisibility(View.GONE);
        }

        // Show quick action buttons for vet messages to farmers
        boolean showQuickActions = "vet".equals(message.getSenderRole()) &&
                                 message.getConsultationStatus() != null &&
                                 message.getConsultationStatus().equals("active");

        if (showQuickActions) {
            holder.quickActionButtons.setVisibility(View.VISIBLE);

            holder.btnMarkResolved.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onMarkResolved(message);
                }
            });

            holder.btnNeedMoreInfo.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onNeedMoreInfo(message);
                }
            });
        } else {
            holder.quickActionButtons.setVisibility(View.GONE);
        }

        // Handle attachments
        if (message.hasAttachment()) {
            holder.attachmentPreview.setVisibility(View.VISIBLE);
            loadAttachmentPreview(holder.imgAttachment, message.getAttachmentUrl());

            holder.attachmentPreview.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAttachmentClick(message);
                }
            });
        } else {
            holder.attachmentPreview.setVisibility(View.GONE);
        }
    }

    private void bindSystemMessage(SystemMessageViewHolder holder, ConsultationMessage message) {
        holder.txtSystemMessage.setText(message.getMessageText());
        holder.txtTimestamp.setText(formatTimestamp(message.getCreatedAt()));
    }

    private void updateMessageStatus(ImageView statusIcon, ConsultationMessage message) {
        // Update based on message status
        String status = message.getMessageStatus();
        if ("delivered".equals(status)) {
            statusIcon.setImageResource(R.drawable.ic_check_double);
            statusIcon.setAlpha(1.0f);
        } else if ("sent".equals(status)) {
            statusIcon.setImageResource(R.drawable.ic_check);
            statusIcon.setAlpha(0.7f);
        } else {
            statusIcon.setImageResource(R.drawable.ic_schedule);
            statusIcon.setAlpha(0.5f);
        }
    }

    private void loadSenderAvatar(CircleImageView avatarView, String senderRole, String senderName) {
        // Set default avatar based on role
        if ("vet".equals(senderRole) || "admin".equals(senderRole)) {
            avatarView.setImageResource(R.drawable.ic_vet_avatar);
        } else {
            avatarView.setImageResource(R.drawable.ic_farmer_avatar);
        }
    }

    private void loadAttachmentPreview(ImageView imageView, String attachmentUrl) {
        // Load attachment preview - implement with your image loading library (Glide/Picasso)
        // For now, show placeholder
        imageView.setImageResource(R.drawable.ic_image);
    }

    // Helper method to format timestamp - updated to handle Date objects
    private String formatTimestamp(Date date) {
        if (date == null) {
            return "Sasa";
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return outputFormat.format(date);
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public void updateMessages(List<ConsultationMessage> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(ConsultationMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // ViewHolder classes
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTimestamp;
        ImageView imgMessageStatus, imgAttachment;
        View attachmentPreview;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            imgMessageStatus = itemView.findViewById(R.id.imgMessageStatus);
            attachmentPreview = itemView.findViewById(R.id.attachmentPreview);
            imgAttachment = itemView.findViewById(R.id.imgAttachment);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTimestamp, txtSenderName;
        CircleImageView imgSenderAvatar;
        ImageView imgAttachment;
        View attachmentPreview, quickActionButtons;
        MaterialButton btnMarkResolved, btnNeedMoreInfo;
        Chip chipProfessionalBadge;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            txtSenderName = itemView.findViewById(R.id.txtSenderName);
            imgSenderAvatar = itemView.findViewById(R.id.imgSenderAvatar);
            attachmentPreview = itemView.findViewById(R.id.attachmentPreview);
            imgAttachment = itemView.findViewById(R.id.imgAttachment);
            quickActionButtons = itemView.findViewById(R.id.quickActionButtons);
            btnMarkResolved = itemView.findViewById(R.id.btnMarkResolved);
            btnNeedMoreInfo = itemView.findViewById(R.id.btnNeedMoreInfo);
            chipProfessionalBadge = itemView.findViewById(R.id.chipProfessionalBadge);
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtSystemMessage, txtTimestamp;

        public SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSystemMessage = itemView.findViewById(R.id.txtSystemMessage);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
        }
    }
}
