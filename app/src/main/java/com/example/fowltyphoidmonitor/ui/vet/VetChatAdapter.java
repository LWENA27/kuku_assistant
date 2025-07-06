package com.example.fowltyphoidmonitor.ui.vet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fowltyphoidmonitor.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * VetChatAdapter - RecyclerView adapter for chat messages in vet-farmer consultations
 *
 * @author LWENA27
 * @created 2025-07-06
 */
public class VetChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FARMER_MESSAGE = 1;
    private static final int VIEW_TYPE_VET_MESSAGE = 2;
    private static final int VIEW_TYPE_CURRENT_VET_MESSAGE = 3;

    private List<ChatMessage> messageList;
    private String currentVetId;
    private SimpleDateFormat timeFormat;

    public VetChatAdapter(List<ChatMessage> messageList, String currentVetId) {
        this.messageList = messageList;
        this.currentVetId = currentVetId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);

        if (message.isFromFarmer()) {
            return VIEW_TYPE_FARMER_MESSAGE;
        } else if (message.isFromCurrentUser(currentVetId)) {
            return VIEW_TYPE_CURRENT_VET_MESSAGE;
        } else {
            return VIEW_TYPE_VET_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_FARMER_MESSAGE:
                View farmerView = inflater.inflate(R.layout.item_chat_farmer_message, parent, false);
                return new FarmerMessageViewHolder(farmerView);

            case VIEW_TYPE_VET_MESSAGE:
                View vetView = inflater.inflate(R.layout.item_chat_vet_message, parent, false);
                return new VetMessageViewHolder(vetView);

            case VIEW_TYPE_CURRENT_VET_MESSAGE:
                View currentVetView = inflater.inflate(R.layout.item_chat_current_vet_message, parent, false);
                return new CurrentVetMessageViewHolder(currentVetView);

            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_FARMER_MESSAGE:
                ((FarmerMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_VET_MESSAGE:
                ((VetMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_CURRENT_VET_MESSAGE:
                ((CurrentVetMessageViewHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for farmer messages (left side)
    public class FarmerMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSenderName;
        private TextView txtMessageText;
        private TextView txtTime;

        public FarmerMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSenderName = itemView.findViewById(R.id.txtSenderName);
            txtMessageText = itemView.findViewById(R.id.txtMessageText);
            txtTime = itemView.findViewById(R.id.txtTime);
        }

        public void bind(ChatMessage message) {
            txtSenderName.setText(message.getSenderName());
            txtMessageText.setText(message.getMessageText());

            if (message.getSentAt() != null) {
                txtTime.setText(timeFormat.format(message.getSentAt()));
            }
        }
    }

    // ViewHolder for other vet messages (left side, different color)
    public class VetMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSenderName;
        private TextView txtMessageText;
        private TextView txtTime;

        public VetMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSenderName = itemView.findViewById(R.id.txtSenderName);
            txtMessageText = itemView.findViewById(R.id.txtMessageText);
            txtTime = itemView.findViewById(R.id.txtTime);
        }

        public void bind(ChatMessage message) {
            txtSenderName.setText(message.getSenderName() + " (Daktari)");
            txtMessageText.setText(message.getMessageText());

            if (message.getSentAt() != null) {
                txtTime.setText(timeFormat.format(message.getSentAt()));
            }
        }
    }

    // ViewHolder for current vet messages (right side)
    public class CurrentVetMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView txtMessageText;
        private TextView txtTime;

        public CurrentVetMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageText = itemView.findViewById(R.id.txtMessageText);
            txtTime = itemView.findViewById(R.id.txtTime);
        }

        public void bind(ChatMessage message) {
            txtMessageText.setText(message.getMessageText());

            if (message.getSentAt() != null) {
                txtTime.setText(timeFormat.format(message.getSentAt()));
            }
        }
    }
}
