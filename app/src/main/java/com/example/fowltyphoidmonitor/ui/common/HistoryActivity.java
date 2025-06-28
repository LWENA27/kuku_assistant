package com.example.fowltyphoidmonitor.ui.common;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fowltyphoidmonitor.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private ImageButton btnBack;
    private TextView tvTitle, tvEmptyHistory;
    private RecyclerView rvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load history data
        loadHistoryData();

        Log.d(TAG, "HistoryActivity created");
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        rvHistory = findViewById(R.id.rvHistory);

        // Set up RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void loadHistoryData() {
        // In a real app, this would come from a database
        List<HistoryItem> historyItems = getDummyHistoryData();

        if (historyItems.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);

            // Set up the adapter with the history data
            HistoryAdapter adapter = new HistoryAdapter(historyItems);
            rvHistory.setAdapter(adapter);
        }
    }

    // For demonstration purposes - In a real app, this would be fetched from a database
    private List<HistoryItem> getDummyHistoryData() {
        List<HistoryItem> items = new ArrayList<>();

        // Adding some dummy history items
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        // Add some dummy history items
        items.add(new HistoryItem("Symptom Report", "Reported symptoms: Decreased appetite, diarrhea", currentTime));
        items.add(new HistoryItem("Medication Log", "Administered antibiotics to 15 chickens", currentTime));
        items.add(new HistoryItem("Disease Information", "Viewed information about Fowl Typhoid", currentTime));
        items.add(new HistoryItem("Vet Consultation", "Scheduled consultation with Dr. Johnson", currentTime));
        items.add(new HistoryItem("Symptom Report", "Reported symptoms: Decreased egg production", currentTime));

        return items;
    }

    // Model class for history items
    public static class HistoryItem {
        private String title;
        private String description;
        private String timestamp;

        public HistoryItem(String title, String description, String timestamp) {
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    // Adapter for the RecyclerView
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        private List<HistoryItem> historyItems;

        public HistoryAdapter(List<HistoryItem> historyItems) {
            this.historyItems = historyItems;
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            HistoryItem item = historyItems.get(position);
            holder.tvHistoryTitle.setText(item.getTitle());
            holder.tvHistoryDescription.setText(item.getDescription());
            holder.tvHistoryTimestamp.setText(item.getTimestamp());
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvHistoryTitle, tvHistoryDescription, tvHistoryTimestamp;

            public HistoryViewHolder(View itemView) {
                super(itemView);
                tvHistoryTitle = itemView.findViewById(R.id.tvHistoryTitle);
                tvHistoryDescription = itemView.findViewById(R.id.tvHistoryDescription);
                tvHistoryTimestamp = itemView.findViewById(R.id.tvHistoryTimestamp);
            }
        }
    }
}