package com.example.fowltyphoidmonitor.ui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages real-time dashboard updates for admin interface
 * Simulates real-time data updates and provides callback mechanisms
 */
public class DashboardManagerActivity {
    private static final String TAG = "DashboardManager";
    private static final String PREFS_NAME = "FowlTyphoidMonitorAdminPrefs";

    // Update intervals
    private static final long DASHBOARD_UPDATE_INTERVAL = 30000; // 30 seconds
    private static final long STATISTICS_UPDATE_INTERVAL = 60000; // 1 minute

    private Context context;
    private Handler mainHandler;
    private ExecutorService executorService;
    private List<DashboardUpdateListener> listeners;
    private boolean isUpdating = false;

    // Singleton instance
    private static com.example.fowltyphoidmonitor.ui.common.DashboardManagerActivity instance;

    // Dashboard data
    private DashboardData currentData;

    public static synchronized com.example.fowltyphoidmonitor.ui.common.DashboardManagerActivity getInstance(Context context) {
        if (instance == null) {
            instance = new com.example.fowltyphoidmonitor.ui.common.DashboardManagerActivity(context.getApplicationContext());
        }
        return instance;
    }

    private DashboardManagerActivity(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newFixedThreadPool(2);
        this.listeners = new ArrayList<>();
        this.currentData = new DashboardData();

        // Load initial data
        loadInitialData();
    }

    /**
     * Interface for dashboard update callbacks
     */
    public interface DashboardUpdateListener {
        void onDashboardDataUpdated(DashboardData data);
        void onStatisticsUpdated(DashboardStatistics stats);
        void onNewAlert(AlertData alert);
        void onError(String error);
    }

    /**
     * Dashboard data container
     */
    public static class DashboardData {
        public int totalFarmers;
        public int activeReports;
        public int pendingConsultations;
        public int totalAlerts;
        public int newUsersToday;
        public int criticalReports;
        public long lastUpdated;

        public DashboardData() {
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    /**
     * Dashboard statistics container
     */
    public static class DashboardStatistics {
        public float reportIncreaseRate;
        public float userGrowthRate;
        public int mostActiveRegion;
        public String trendingDisease;
        public int consultationResponseRate;

        public DashboardStatistics() {
            // Default values
        }
    }

    /**
     * Alert data container
     */
    public static class AlertData {
        public String id;
        public String title;
        public String message;
        public String priority; // HIGH, MEDIUM, LOW
        public String type; // DISEASE_OUTBREAK, SYSTEM_ALERT, USER_ALERT
        public long timestamp;

        public AlertData(String id, String title, String message, String priority, String type) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.priority = priority;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Start real-time dashboard updates
     */
    public void startRealTimeUpdates() {
        if (isUpdating) {
            Log.d(TAG, "Dashboard updates already running");
            return;
        }

        isUpdating = true;
        Log.d(TAG, "Starting real-time dashboard updates");

        // Schedule periodic updates
        scheduleDashboardUpdates();
        scheduleStatisticsUpdates();

        // Simulate random alerts
        scheduleRandomAlerts();
    }

    /**
     * Stop real-time dashboard updates
     */
    public void stopRealTimeUpdates() {
        isUpdating = false;
        Log.d(TAG, "Stopping real-time dashboard updates");

        // Cancel all pending updates
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Add dashboard update listener
     */
    public void addUpdateListener(DashboardUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Log.d(TAG, "Added dashboard update listener");
        }
    }

    /**
     * Remove dashboard update listener
     */
    public void removeUpdateListener(DashboardUpdateListener listener) {
        listeners.remove(listener);
        Log.d(TAG, "Removed dashboard update listener");
    }

    /**
     * Get current dashboard data
     */
    public DashboardData getCurrentData() {
        return currentData;
    }

    /**
     * Force immediate dashboard update
     */
    public void forceUpdate() {
        Log.d(TAG, "Forcing immediate dashboard update");
        executorService.execute(this::updateDashboardData);
    }

    /**
     * Load initial dashboard data from SharedPreferences
     */
    private void loadInitialData() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        currentData.totalFarmers = prefs.getInt("totalFarmers", 125);
        currentData.activeReports = prefs.getInt("activeReports", 8);
        currentData.pendingConsultations = prefs.getInt("pendingConsultations", 3);
        currentData.totalAlerts = prefs.getInt("totalAlerts", 0);
        currentData.newUsersToday = prefs.getInt("newUsersToday", 0);
        currentData.criticalReports = prefs.getInt("criticalReports", 0);

        Log.d(TAG, "Loaded initial dashboard data");
    }

    /**
     * Schedule periodic dashboard updates
     */
    private void scheduleDashboardUpdates() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isUpdating) {
                    executorService.execute(() -> updateDashboardData());
                    mainHandler.postDelayed(this, DASHBOARD_UPDATE_INTERVAL);
                }
            }
        }, DASHBOARD_UPDATE_INTERVAL);
    }

    /**
     * Schedule periodic statistics updates
     */
    private void scheduleStatisticsUpdates() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isUpdating) {
                    executorService.execute(() -> updateStatistics());
                    mainHandler.postDelayed(this, STATISTICS_UPDATE_INTERVAL);
                }
            }
        }, STATISTICS_UPDATE_INTERVAL);
    }

    /**
     * Schedule random alerts
     */
    private void scheduleRandomAlerts() {
        Random random = new Random();

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isUpdating) {
                    // Random chance of generating an alert
                    if (random.nextFloat() < 0.3f) { // 30% chance
                        generateRandomAlert();
                    }
                    // Schedule next check
                    mainHandler.postDelayed(this, 45000 + random.nextInt(30000)); // 45-75 seconds
                }
            }
        }, 60000); // Initial delay of 1 minute
    }

    /**
     * Update dashboard data (simulated)
     */
    private void updateDashboardData() {
        try {
            // Simulate data changes
            Random random = new Random();

            // Small random changes to simulate real-time updates
            if (random.nextFloat() < 0.7f) { // 70% chance of change
                currentData.totalFarmers += random.nextInt(3) - 1; // -1 to +1
                currentData.activeReports += random.nextInt(5) - 2; // -2 to +2
                currentData.pendingConsultations += random.nextInt(3) - 1; // -1 to +1

                // Ensure non-negative values
                currentData.totalFarmers = Math.max(0, currentData.totalFarmers);
                currentData.activeReports = Math.max(0, currentData.activeReports);
                currentData.pendingConsultations = Math.max(0, currentData.pendingConsultations);

                currentData.lastUpdated = System.currentTimeMillis();

                // Save to SharedPreferences
                saveDashboardData();

                // Notify listeners on main thread
                mainHandler.post(() -> {
                    for (DashboardUpdateListener listener : listeners) {
                        listener.onDashboardDataUpdated(currentData);
                    }
                });

                Log.d(TAG, "Dashboard data updated");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating dashboard data: " + e.getMessage());
            mainHandler.post(() -> {
                for (DashboardUpdateListener listener : listeners) {
                    listener.onError("Kosa katika kusasisha data: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Update statistics (simulated)
     */
    private void updateStatistics() {
        try {
            DashboardStatistics stats = new DashboardStatistics();
            Random random = new Random();

            // Generate realistic statistics
            stats.reportIncreaseRate = random.nextFloat() * 20 - 10; // -10% to +10%
            stats.userGrowthRate = random.nextFloat() * 15; // 0% to 15%
            stats.mostActiveRegion = random.nextInt(5) + 1; // Region 1-5
            stats.consultationResponseRate = 75 + random.nextInt(25); // 75-100%

            // Trending diseases
            String[] diseases = {"Fowl Typhoid", "Newcastle Disease", "Avian Influenza", "Infectious Bronchitis", "Marek's Disease"};
            stats.trendingDisease = diseases[random.nextInt(diseases.length)];

            // Notify listeners on main thread
            mainHandler.post(() -> {
                for (DashboardUpdateListener listener : listeners) {
                    listener.onStatisticsUpdated(stats);
                }
            });

            Log.d(TAG, "Statistics updated");
        } catch (Exception e) {
            Log.e(TAG, "Error updating statistics: " + e.getMessage());
        }
    }

    /**
     * Generate random alert
     */
    private void generateRandomAlert() {
        try {
            Random random = new Random();
            String[] alertTypes = {"DISEASE_OUTBREAK", "SYSTEM_ALERT", "USER_ALERT"};
            String[] priorities = {"HIGH", "MEDIUM", "LOW"};

            String[] titles = {
                    "Mlipuko wa Ugonjwa",
                    "Ripoti Mpya za Kigurumu",
                    "Mfumo wa Kumbuka",
                    "Takwimu za Haraka",
                    "Taarifa za Usalama"
            };

            String[] messages = {
                    "Mlipuko wa fowl typhoid umeonekana katika eneo la Arusha",
                    "Ripoti 5 mpya za ugonjwa zimesajiliwa",
                    "Mfumo wa kumbuka: Wakati wa chanjo umefika",
                    "Ongezeko la asilimia 15 la matukio ya ugonjwa",
                    "Angalizo: Hakikisha usafi wa mazingira"
            };

            String alertId = "ALERT_" + System.currentTimeMillis();
            String alertType = alertTypes[random.nextInt(alertTypes.length)];
            String priority = priorities[random.nextInt(priorities.length)];
            String title = titles[random.nextInt(titles.length)];
            String message = messages[random.nextInt(messages.length)];

            AlertData alert = new AlertData(alertId, title, message, priority, alertType);

            // Update alert count
            currentData.totalAlerts++;
            if ("HIGH".equals(priority)) {
                currentData.criticalReports++;
            }

            // Notify listeners on main thread
            mainHandler.post(() -> {
                for (DashboardUpdateListener listener : listeners) {
                    listener.onNewAlert(alert);
                }
            });

            Log.d(TAG, "Generated random alert: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error generating alert: " + e.getMessage());
        }
    }

    /**
     * Save dashboard data to SharedPreferences
     */
    private void saveDashboardData() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("totalFarmers", currentData.totalFarmers);
        editor.putInt("activeReports", currentData.activeReports);
        editor.putInt("pendingConsultations", currentData.pendingConsultations);
        editor.putInt("totalAlerts", currentData.totalAlerts);
        editor.putInt("newUsersToday", currentData.newUsersToday);
        editor.putInt("criticalReports", currentData.criticalReports);
        editor.putLong("lastDashboardUpdate", currentData.lastUpdated);

        editor.apply();
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        stopRealTimeUpdates();
        listeners.clear();

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        Log.d(TAG, "Dashboard manager cleanup completed");
    }
}