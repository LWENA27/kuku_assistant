package com.example.fowltyphoidmonitor.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.data.models.ConsultationMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Custom adapter for displaying chat messages with different layouts based on message type
 * Updated to work with ConsultationMessage objects for Supabase integration
 */
public class ChatMessageAdapter extends ArrayAdapter<ConsultationMessage> {

    private static final int VIEW_TYPE_MY_MESSAGE = 0;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    private Context context;
    private String currentUserId;

    public ChatMessageAdapter(Context context, ArrayList<ConsultationMessage> messages, String currentUserId) {
        super(context, 0, messages);
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        ConsultationMessage message = getItem(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConsultationMessage message = getItem(position);
        int viewType = getItemViewType(position);

        if (convertView == null) {
            int layoutId = (viewType == VIEW_TYPE_MY_MESSAGE)
                    ? R.layout.item_my_message
                    : R.layout.item_other_message;

            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }

        TextView messageTextView = convertView.findViewById(R.id.txtMessage);
        TextView timestampView = convertView.findViewById(R.id.txtTimestamp);
        TextView senderView = convertView.findViewById(R.id.txtSender);

        // Set message content
        messageTextView.setText(message.getMessage());

        // Format and set timestamp
        String timestamp = formatTimestamp(message.getCreatedAt());
        timestampView.setText(timestamp);

        // Set sender name based on sender type
        String senderName = getSenderDisplayName(message);
        if (senderView != null) { // Only for other messages
            senderView.setText(senderName);

            // Style based on sender type
            if ("vet".equals(message.getSenderType())) {
                senderView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
            } else {
                senderView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            }
        }

        return convertView;
    }

    private String formatTimestamp(Date timestamp) {
        if (timestamp == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        return sdf.format(timestamp);
    }

    private String getSenderDisplayName(ConsultationMessage message) {
        String senderType = message.getSenderType();
        if ("vet".equals(senderType)) {
            return "Mshauri";
        } else if ("farmer".equals(senderType)) {
            return "Mfugaji";
        } else {
            return "Unknown";
        }
    }
}