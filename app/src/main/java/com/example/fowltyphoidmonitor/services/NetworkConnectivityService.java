package com.example.fowltyphoidmonitor.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Service to monitor network connectivity and handle offline/online transitions
 * Automatically sends queued messages when connection is restored
 */
public class NetworkConnectivityService {
    private static final String TAG = "NetworkConnectivity";

    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final OfflineMessageQueue messageQueue;
    private final SupabaseChatService chatService;
    private NetworkReceiver networkReceiver;
    private boolean isRegistered = false;

    public NetworkConnectivityService(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.messageQueue = OfflineMessageQueue.getInstance(context);
        this.chatService = SupabaseChatService.getInstance(context);
    }

    /**
     * Start monitoring network connectivity
     */
    public void startMonitoring() {
        if (!isRegistered) {
            networkReceiver = new NetworkReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(networkReceiver, filter);
            isRegistered = true;
            Log.d(TAG, "Started network connectivity monitoring");
        }
    }

    /**
     * Stop monitoring network connectivity
     */
    public void stopMonitoring() {
        if (isRegistered && networkReceiver != null) {
            context.unregisterReceiver(networkReceiver);
            isRegistered = false;
            Log.d(TAG, "Stopped network connectivity monitoring");
        }
    }

    /**
     * Check if device is currently connected to internet
     */
    public boolean isConnected() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * BroadcastReceiver to listen for network state changes
     */
    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean isConnected = isConnected();
                Log.d(TAG, "Network state changed. Connected: " + isConnected);

                if (isConnected) {
                    // Connection restored - send queued messages
                    messageQueue.sendQueuedMessages(chatService);
                }
            }
        }
    }
}
