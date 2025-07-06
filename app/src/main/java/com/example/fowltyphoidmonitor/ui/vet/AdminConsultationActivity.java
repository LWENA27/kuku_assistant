package com.example.fowltyphoidmonitor.ui.vet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;

import java.util.ArrayList;
import java.util.List;

public class AdminConsultationActivity extends AppCompatActivity {
    private ListView chatListView;
    private EditText messageEditText;
    private Button sendButton;
    private TextView currentRoleTextView;
    private Button switchRoleButton;
    private LinearLayout typingIndicatorLayout;

    private ArrayAdapter<String> chatAdapter;
    private List<String> messages;
    private boolean isAdminMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_consultation);

        initializeViews();
        setupChatList();
        setupMessageInput();
        setupRoleSwitch();
        updateRoleDisplay();
    }

    private void initializeViews() {
        chatListView = findViewById(R.id.chatListView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        currentRoleTextView = findViewById(R.id.currentRoleTextView);
        switchRoleButton = findViewById(R.id.switchRoleButton);
        typingIndicatorLayout = findViewById(R.id.typingIndicatorLayout);
    }

    private void setupChatList() {
        messages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(chatAdapter);

        // Add some sample messages for testing
        messages.add("Mshauri: Habari za kuku wako?");
        messages.add("Mfugaji: Kuku wangu wanaonekana hawana afya nzuri.");
        messages.add("Mshauri: Ni dalili gani unaziona?");
        chatAdapter.notifyDataSetChanged();
    }

    private void setupMessageInput() {
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        sendButton.setOnClickListener(v -> sendMessage());

        // Handle enter key to send message
        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupRoleSwitch() {
        switchRoleButton.setOnClickListener(v -> {
            isAdminMode = !isAdminMode;
            updateRoleDisplay();
        });
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            String prefix = isAdminMode ? "Mshauri: " : "Mfugaji: ";
            messages.add(prefix + message);
            chatAdapter.notifyDataSetChanged();
            messageEditText.setText("");

            // Auto-scroll to the bottom
            chatListView.post(() -> chatListView.smoothScrollToPosition(messages.size() - 1));
        }
    }

    private void updateRoleDisplay() {
        String roleText = isAdminMode ? "Mtumiaji: Mshauri" : "Mtumiaji: Mfugaji";
        currentRoleTextView.setText(roleText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("messages", new ArrayList<>(messages));
        outState.putBoolean("isAdminMode", isAdminMode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<String> savedMessages = savedInstanceState.getStringArrayList("messages");
            if (savedMessages != null) {
                messages.clear();
                messages.addAll(savedMessages);
                chatAdapter.notifyDataSetChanged();
            }
            isAdminMode = savedInstanceState.getBoolean("isAdminMode", false);
            updateRoleDisplay();
        }
    }
}
