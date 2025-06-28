package com.example.fowltyphoidmonitor.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.vet.VetConsultationActivity;

import java.util.ArrayList;

/**
 * Custom adapter for displaying chat messages with different layouts based on message type
 */
public class ChatMessageAdapter extends ArrayAdapter<VetConsultationActivity.ChatMessage> {

    private Context context;

    public ChatMessageAdapter(Context context, ArrayList<VetConsultationActivity.ChatMessage> messages) {
        super(context, 0, messages);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VetConsultationActivity.ChatMessage message = getItem(position);

        // Determine which layout to use based on message type and sender
        int layoutResource;
        if (message.getType() == VetConsultationActivity.ChatMessage.MessageType.SYSTEM) {
            layoutResource = R.layout.item_system_message;
        } else if (message.isFromCurrentUser()) {
            layoutResource = R.layout.item_sent_message;
        } else {
            layoutResource = R.layout.item_received_message;
        }

        // Inflate the appropriate layout
        if (convertView == null || getItemViewType(position) != getViewTypeForLayout(layoutResource)) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutResource, parent, false);
        }

        // Set the message content
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView senderTextView = convertView.findViewById(R.id.senderTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);

        // Set message text
        messageTextView.setText(message.getMessage());

        // Set sender name if it's a non-system message
        if (message.getType() != VetConsultationActivity.ChatMessage.MessageType.SYSTEM) {
            senderTextView.setText(message.getSender());
        }

        // Set timestamp
        timeTextView.setText(message.getFormattedTime());

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        VetConsultationActivity.ChatMessage message = getItem(position);

        if (message.getType() == VetConsultationActivity.ChatMessage.MessageType.SYSTEM) {
            return 0;
        } else if (message.isFromCurrentUser()) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3; // System, sent, and received messages
    }

    private int getViewTypeForLayout(int layoutResource) {
        if (layoutResource == R.layout.item_system_message) {
            return 0;
        } else if (layoutResource == R.layout.item_sent_message) {
            return 1;
        } else {
            return 2;
        }
    }
}