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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification);
    }

    public interface OnDismissClickListener {
        void onDismissClick(NotificationItem notification);
    }

    private Context context;
    private List<NotificationItem> notifications;
    private OnNotificationClickListener clickListener;
    private OnDismissClickListener dismissListener;
    private SimpleDateFormat dateFormat;

    public NotificationsAdapter(Context context, OnNotificationClickListener clickListener, OnDismissClickListener dismissListener) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.clickListener = clickListener;
        this.dismissListener = dismissListener;
        this.dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<NotificationItem> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgIcon;
        private TextView txtTitle;
        private TextView txtMessage;
        private TextView txtTimestamp;
        private ImageView imgDismiss;
        private View unreadIndicator;
        private View itemBackground;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            imgDismiss = itemView.findViewById(R.id.imgDismiss);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            itemBackground = itemView.findViewById(R.id.itemBackground);
        }

        public void bind(NotificationItem notification) {
            // Set title and message
            txtTitle.setText(notification.getTitle());
            txtMessage.setText(notification.getMessage());
            txtTimestamp.setText(dateFormat.format(notification.getTimestamp()));

            // Set icon and colors based on notification type
            switch (notification.getType()) {
                case CRITICAL:
                    imgIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                    imgIcon.setColorFilter(0xFFDC2626); // Red
                    itemBackground.setBackgroundColor(0xFFFFEBEE); // Light red
                    break;
                case WARNING:
                    imgIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                    imgIcon.setColorFilter(0xFFF59E0B); // Orange
                    itemBackground.setBackgroundColor(0xFFFFF3E0); // Light orange
                    break;
                case INFO:
                    imgIcon.setImageResource(android.R.drawable.ic_dialog_info);
                    imgIcon.setColorFilter(0xFF2563EB); // Blue
                    itemBackground.setBackgroundColor(0xFFE3F2FD); // Light blue
                    break;
                case SUCCESS:
                    imgIcon.setImageResource(android.R.drawable.checkbox_on_background);
                    imgIcon.setColorFilter(0xFF10B981); // Green
                    itemBackground.setBackgroundColor(0xFFE8F5E8); // Light green
                    break;
            }

            // Show/hide unread indicator
            if (notification.isRead()) {
                unreadIndicator.setVisibility(View.GONE);
                // Slightly transparent for read notifications
                itemView.setAlpha(0.7f);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification);
                }
            });

            imgDismiss.setOnClickListener(v -> {
                if (dismissListener != null) {
                    dismissListener.onDismissClick(notification);
                }
            });
        }
    }
}