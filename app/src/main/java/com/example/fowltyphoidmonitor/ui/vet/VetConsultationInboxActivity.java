package com.example.fowltyphoidmonitor.ui.vet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.services.auth.AuthManager;
import com.example.fowltyphoidmonitor.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * VetConsultationInboxActivity - Inbox-style consultation management for vets
 * 
 * Features:
 * - View all farmer consultation requests
 * - Real-time chat interface for each consultation
 * - Filter by status, priority, and date
 * - Search functionality
 * - Multiple vets can respond to same farmer message
 * 
 * @author LWENA27
 * @created 2025-07-06
 */
public class VetConsultationInboxActivity extends AppCompatActivity {

    private static final String TAG = "VetConsultationInbox";
    
    // Request codes
    private static final int REQUEST_CHAT_CONSULTATION = 2001;
    
    // Refresh interval (30 seconds)
    private static final long REFRESH_INTERVAL = 30000;

    // UI Components
    private Toolbar toolbar;
    private ImageButton btnBack;
    private TextView txtTitle, txtUserInfo, txtStatsTotal, txtStatsPending, txtStatsAnswered;
    private EditText etSearch;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipPending, chipAnswered, chipUrgent, chipHigh, chipMedium, chipLow;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewConsultations;
    private LinearLayout emptyStateLayout;
    private TextView txtEmptyMessage;
    private MaterialButton btnRefreshEmpty;
    private FloatingActionButton fabComposeMessage;

    // Data and adapters
    private List<ConsultationInboxItem> consultationList;
    private List<ConsultationInboxItem> filteredList;
    private VetConsultationInboxAdapter adapter;
    
    // Managers and services
    private AuthManager authManager;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    // Current user data
    private String currentVetId;
    private String currentVetName;
    
    // Filters
    private String currentStatusFilter = "all";
    private String currentPriorityFilter = "all";
    private String currentSearchQuery = "";
    
    // Auto-refresh
    private boolean isAutoRefreshEnabled = true;
    private Runnable autoRefreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Check authentication
            authManager = AuthManager.getInstance(this);
            if (!authManager.isLoggedIn() || !authManager.isVet()) {
                Log.d(TAG, "Unauthorized access, redirecting to login");
                redirectToLogin();
                return;
            }

            setContentView(R.layout.activity_vet_consultation_inbox);

            // Initialize components
            initializeComponents();
            initializeViews();
            setupToolbar();
            setupFilters();
            setupRecyclerView();
            setupClickListeners();
            setupSearch();
            setupAutoRefresh();

            // Load data
            loadConsultationInbox();

            Log.d(TAG, "VetConsultationInboxActivity created for vet: " + currentVetName);

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Hitilafu wakati wa kuanza programu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeComponents() {
        executorService = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Get current vet information
        currentVetId = authManager.getUserId();
        currentVetName = authManager.getUserEmail(); // Or get actual name from profile
        
        consultationList = new ArrayList<>();
        filteredList = new ArrayList<>();
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtUserInfo = findViewById(R.id.txtUserInfo);
        
        // Statistics
        txtStatsTotal = findViewById(R.id.txtStatsTotal);
        txtStatsPending = findViewById(R.id.txtStatsPending);
        txtStatsAnswered = findViewById(R.id.txtStatsAnswered);
        
        // Search and filters
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipAnswered = findViewById(R.id.chipAnswered);
        chipUrgent = findViewById(R.id.chipUrgent);
        chipHigh = findViewById(R.id.chipHigh);
        chipMedium = findViewById(R.id.chipMedium);
        chipLow = findViewById(R.id.chipLow);
        
        // Content area
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewConsultations = findViewById(R.id.recyclerViewConsultations);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        txtEmptyMessage = findViewById(R.id.txtEmptyMessage);
        btnRefreshEmpty = findViewById(R.id.btnRefreshEmpty);
        fabComposeMessage = findViewById(R.id.fabComposeMessage);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Mahojiano ya Wafugaji");
            }
        }

        if (txtTitle != null) {
            txtTitle.setText("Mahojiano ya Wafugaji");
        }

        if (txtUserInfo != null) {
            txtUserInfo.setText("Daktari: " + currentVetName);
        }
    }

    private void setupFilters() {
        // Status filters
        chipAll.setOnClickListener(v -> applyStatusFilter("all"));
        chipPending.setOnClickListener(v -> applyStatusFilter("pending"));
        chipAnswered.setOnClickListener(v -> applyStatusFilter("answered"));
        
        // Priority filters
        chipUrgent.setOnClickListener(v -> applyPriorityFilter("urgent"));
        chipHigh.setOnClickListener(v -> applyPriorityFilter("high"));
        chipMedium.setOnClickListener(v -> applyPriorityFilter("medium"));
        chipLow.setOnClickListener(v -> applyPriorityFilter("low"));
        
        // Set default selection
        chipAll.setChecked(true);
    }

    private void setupRecyclerView() {
        adapter = new VetConsultationInboxAdapter(filteredList, this::onConsultationItemClick);
        recyclerViewConsultations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewConsultations.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadConsultationInbox);

        // Empty state refresh
        if (btnRefreshEmpty != null) {
            btnRefreshEmpty.setOnClickListener(v -> loadConsultationInbox());
        }

        // Compose new message/broadcast
        if (fabComposeMessage != null) {
            fabComposeMessage.setOnClickListener(v -> openComposeBroadcast());
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAutoRefresh() {
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoRefreshEnabled) {
                    loadConsultationInbox();
                    mainHandler.postDelayed(this, REFRESH_INTERVAL);
                }
            }
        };
    }

    private void loadConsultationInbox() {
        executorService.execute(() -> {
            try {
                // Simulate loading consultations - replace with actual API call
                List<ConsultationInboxItem> newConsultations = fetchConsultationsFromServer();
                
                mainHandler.post(() -> {
                    consultationList.clear();
                    consultationList.addAll(newConsultations);
                    applyFilters();
                    updateStatistics();
                    swipeRefreshLayout.setRefreshing(false);
                    
                    Log.d(TAG, "Loaded " + consultationList.size() + " consultations");
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading consultations: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(VetConsultationInboxActivity.this, 
                        "Hitilafu wakati wa kupakia mahojiano", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private List<ConsultationInboxItem> fetchConsultationsFromServer() {
        // Mock data - replace with actual API call to fetch consultations
        List<ConsultationInboxItem> mockData = new ArrayList<>();
        
        // Sample urgent consultation
        mockData.add(new ConsultationInboxItem(
            "1", "Mfugaji John Doe", "john@example.com",
            "Kuku wangu wanaumwa sana, wamepoteza hamu ya kula na wanateseka. Je, ni nini kifanyike?",
            "urgent", "pending", new Date(System.currentTimeMillis() - 3600000), // 1 hour ago
            null, 3, "Kuku, Ugonjwa, Haraka"
        ));
        
        // Sample high priority consultation
        mockData.add(new ConsultationInboxItem(
            "2", "Mfugaji Mary Mwangi", "mary@example.com",
            "Nimegundua kuku mmoja amekufa, wengine wanaonyesha dalili za ugonjwa. Naomba msaada wa haraka.",
            "high", "answered", new Date(System.currentTimeMillis() - 7200000), // 2 hours ago
            new Date(System.currentTimeMillis() - 1800000), // Answered 30 minutes ago
            1, "Kifo, Dalili, Msaada"
        ));
        
        // Sample medium priority consultation
        mockData.add(new ConsultationInboxItem(
            "3", "Mfugaji Peter Kamau", "peter@example.com",
            "Kuna njia gani bora za kuzuia magonjwa ya kuku wakati wa kipindi cha baridi?",
            "medium", "pending", new Date(System.currentTimeMillis() - 10800000), // 3 hours ago
            null, 0, "Kuzuia, Baridi, Afya"
        ));
        
        return mockData;
    }

    private void applyStatusFilter(String status) {
        currentStatusFilter = status;
        updateFilterChips();
        applyFilters();
    }

    private void applyPriorityFilter(String priority) {
        currentPriorityFilter = priority;
        updateFilterChips();
        applyFilters();
    }

    private void updateFilterChips() {
        // Reset all chips
        chipAll.setChecked(false);
        chipPending.setChecked(false);
        chipAnswered.setChecked(false);
        chipUrgent.setChecked(false);
        chipHigh.setChecked(false);
        chipMedium.setChecked(false);
        chipLow.setChecked(false);
        
        // Set active chips
        switch (currentStatusFilter) {
            case "all": chipAll.setChecked(true); break;
            case "pending": chipPending.setChecked(true); break;
            case "answered": chipAnswered.setChecked(true); break;
        }
        
        switch (currentPriorityFilter) {
            case "urgent": chipUrgent.setChecked(true); break;
            case "high": chipHigh.setChecked(true); break;
            case "medium": chipMedium.setChecked(true); break;
            case "low": chipLow.setChecked(true); break;
        }
    }

    private void applyFilters() {
        filteredList.clear();
        
        for (ConsultationInboxItem item : consultationList) {
            boolean matchesStatus = currentStatusFilter.equals("all") || 
                                  item.getStatus().equals(currentStatusFilter);
            
            boolean matchesPriority = currentPriorityFilter.equals("all") || 
                                    item.getPriority().equals(currentPriorityFilter);
            
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                                  item.getFarmerName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                  item.getQuestion().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                  item.getTags().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            if (matchesStatus && matchesPriority && matchesSearch) {
                filteredList.add(item);
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateStatistics() {
        int total = consultationList.size();
        int pending = 0;
        int answered = 0;
        
        for (ConsultationInboxItem item : consultationList) {
            if ("pending".equals(item.getStatus())) {
                pending++;
            } else if ("answered".equals(item.getStatus())) {
                answered++;
            }
        }
        
        if (txtStatsTotal != null) txtStatsTotal.setText(String.valueOf(total));
        if (txtStatsPending != null) txtStatsPending.setText(String.valueOf(pending));
        if (txtStatsAnswered != null) txtStatsAnswered.setText(String.valueOf(answered));
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredList.isEmpty();
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewConsultations.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        if (isEmpty && txtEmptyMessage != null) {
            if (consultationList.isEmpty()) {
                txtEmptyMessage.setText("Hakuna mahojiano ya wafugaji kwa sasa.\nMahojiano mapya yataonekana hapa.");
            } else {
                txtEmptyMessage.setText("Hakuna mahojiano yanayolingana na vichungi vyako.\nJaribu kubadilisha vichungi au kutafuta kitu kingine.");
            }
        }
    }

    private void onConsultationItemClick(ConsultationInboxItem item) {
        // Open chat interface for this consultation
        Intent chatIntent = new Intent(this, VetConsultationChatActivity.class);
        chatIntent.putExtra("consultation_id", item.getConsultationId());
        chatIntent.putExtra("farmer_name", item.getFarmerName());
        chatIntent.putExtra("farmer_email", item.getFarmerEmail());
        chatIntent.putExtra("priority", item.getPriority());
        startActivityForResult(chatIntent, REQUEST_CHAT_CONSULTATION);
    }

    private void openComposeBroadcast() {
        // Open activity to compose broadcast message to all farmers
        Intent broadcastIntent = new Intent(this, VetBroadcastMessageActivity.class);
        startActivity(broadcastIntent);
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAutoRefreshEnabled = true;
        if (autoRefreshRunnable != null) {
            mainHandler.postDelayed(autoRefreshRunnable, REFRESH_INTERVAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAutoRefreshEnabled = false;
        if (autoRefreshRunnable != null) {
            mainHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        if (autoRefreshRunnable != null) {
            mainHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHAT_CONSULTATION && resultCode == RESULT_OK) {
            // Refresh the consultation list after returning from chat
            loadConsultationInbox();
        }
    }
}
