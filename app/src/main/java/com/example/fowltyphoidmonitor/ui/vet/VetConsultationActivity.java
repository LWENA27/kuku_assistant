package com.example.fowltyphoidmonitor.ui.vet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.fowltyphoidmonitor.R;
import com.example.fowltyphoidmonitor.ui.common.ChatMessageAdapter;

public class VetConsultationActivity extends AppCompatActivity {

    private ListView chatListView;
    private EditText messageEditText;
    private Button sendButton;
    private ArrayList<ChatMessage> chatMessages;
    private ChatMessageAdapter chatAdapter;
    private LinearLayout typingIndicatorLayout;
    private ProgressBar typingProgressBar;
    private TextView typingTextView;

    // Role selection (for demo purposes - in a real app you might determine this from login)
    private boolean isVeterinarian = false; // Default as farmer
    private Button switchRoleButton;
    private TextView currentRoleTextView;
    private Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingRunnable;
    private boolean isOtherUserTyping = false;

    // For real-time typing feedback
    private static final long TYPING_TIMEOUT = 1000; // 1 second
    private Handler typingTimeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable typingTimeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vet_consultation);

        // Initialize UI components
        chatListView = findViewById(R.id.chatListView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        switchRoleButton = findViewById(R.id.switchRoleButton);
        currentRoleTextView = findViewById(R.id.currentRoleTextView);
        typingIndicatorLayout = findViewById(R.id.typingIndicatorLayout);
        typingProgressBar = findViewById(R.id.typingProgressBar);
        typingTextView = findViewById(R.id.typingTextView);

        // Initialize chat components
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(this, chatMessages);
        chatListView.setAdapter(chatAdapter);

        // Add initial system message
        String welcomeMessage = "Karibu kwenye Mazungumzo ya Fowl Typhoid Monitor. " +
                "Tumia nafasi hii kushauriana kuhusu masuala ya afya ya kuku.";
        addSystemMessage(welcomeMessage);

        // Add sample messages for demonstration
        addSampleMessages();

        // Set up click listeners
        setupClickListeners();

        // Set up typing indicators
        setupTypingIndicators();

        // Update the role display
        updateRoleDisplay();
    }

    private void setupClickListeners() {
        // Send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                // Hide keyboard after sending message
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
            }
        });

        // Switch role button (for demonstration purposes)
        switchRoleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVeterinarian = !isVeterinarian;
                updateRoleDisplay();
                Toast.makeText(VetConsultationActivity.this,
                        "Umebadili kuwa " + (isVeterinarian ? "Daktari wa Mifugo" : "Mkulima"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTypingIndicators() {
        // Set up text change listener for typing indication
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Only send typing indicator if text is being added (not when deleted)
                if (count > before) {
                    // Broadcast to other users that this user is typing
                    broadcastTypingStatus(true);

                    // Reset typing timeout
                    resetTypingTimeout();
                }

                // Enable send button only when text is available
                sendButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initially disable send button
        sendButton.setEnabled(false);
    }

    private void resetTypingTimeout() {
        typingTimeoutHandler.removeCallbacks(typingTimeoutRunnable);

        typingTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                // User stopped typing
                broadcastTypingStatus(false);
            }
        };

        typingTimeoutHandler.postDelayed(typingTimeoutRunnable, TYPING_TIMEOUT);
    }

    private void broadcastTypingStatus(boolean isTyping) {
        // In a real app, this would send the typing status to a server or directly to the other user
        // For now, we'll just simulate it

        // In a production app, you would send this status to your backend or through a direct P2P connection
        System.out.println("User is " + (isTyping ? "typing" : "stopped typing"));

        // Nothing else to do in the demo as we're simulating the other user
    }

    private void updateRoleDisplay() {
        String roleText = isVeterinarian ? "Daktari wa Mifugo" : "Mkulima";
        currentRoleTextView.setText("Wajibu wa sasa: " + roleText);

        // Update typing indicator text based on current role
        typingTextView.setText((isVeterinarian ? "Mkulima" : "Daktari wa Mifugo") + " anaandika...");
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String sender = isVeterinarian ? "Daktari" : "Mkulima";
            long timestamp = System.currentTimeMillis();

            // Create a new message object
            ChatMessage newMessage = new ChatMessage(
                    ChatMessage.MessageType.USER,
                    sender,
                    messageText,
                    timestamp,
                    true // Message sent by the current user
            );

            chatMessages.add(newMessage);
            chatAdapter.notifyDataSetChanged();

            // Auto-scroll to the bottom
            chatListView.smoothScrollToPosition(chatMessages.size() - 1);

            // Clear the input field
            messageEditText.setText("");

            // Signal that we've stopped typing
            broadcastTypingStatus(false);

            // For demo purposes, add a simulated response if the user is a farmer
            if (!isVeterinarian) {
                simulateVetResponse(messageText);
            } else {
                simulateFarmerResponse(messageText);
            }
        }
    }

    private void simulateVetResponse(final String userMessage) {
        // First show typing indicator
        showTypingIndicator(true);

        // Determine response time based on message length (more realistic)
        int responseDelay = Math.min(1000 + userMessage.length() * 30, 5000);

        // Simulate a delay before the vet responds
        typingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hide typing indicator before sending message
                showTypingIndicator(false);

                // Generate contextual response based on user's message
                String response = generateVetResponse(userMessage);
                long timestamp = System.currentTimeMillis();

                ChatMessage vetMessage = new ChatMessage(
                        ChatMessage.MessageType.OTHER,
                        "Daktari",
                        response,
                        timestamp,
                        false // Not sent by current user
                );

                chatMessages.add(vetMessage);
                chatAdapter.notifyDataSetChanged();
                chatListView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }, responseDelay);
    }

    private void simulateFarmerResponse(final String userMessage) {
        // First show typing indicator
        showTypingIndicator(true);

        // Determine response time based on message length (more realistic)
        int responseDelay = Math.min(1000 + userMessage.length() * 30, 5000);

        // Simulate a delay before the farmer responds
        typingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hide typing indicator before sending message
                showTypingIndicator(false);

                // Generate contextual response based on user's message
                String response = generateFarmerResponse(userMessage);
                long timestamp = System.currentTimeMillis();

                ChatMessage farmerMessage = new ChatMessage(
                        ChatMessage.MessageType.OTHER,
                        "Mkulima",
                        response,
                        timestamp,
                        false // Not sent by current user
                );

                chatMessages.add(farmerMessage);
                chatAdapter.notifyDataSetChanged();
                chatListView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }, responseDelay);
    }

    private String generateVetResponse(String userMessage) {
        // More intelligent response generator based on user input
        userMessage = userMessage.toLowerCase();

        if (userMessage.contains("dalili") || userMessage.contains("symptom")) {
            return "Kulingana na dalili ulizoeleza, inaweza kuwa ishara za awali za typhoid ya kuku. "
                    + "Tafadhali niambie zaidi kuhusu muda ambao umeona dalili hizi.";
        } else if (userMessage.contains("chakula") || userMessage.contains("food") || userMessage.contains("kula")) {
            return "Usimamizi wa chakula ni muhimu. Je, umebadilisha aina yoyote ya chakula hivi karibuni? "
                    + "Napendekeza kuhakikisha chakula chao ni kavu na hakijahifadhiwa katika hali yenye unyevu.";
        } else if (userMessage.contains("maji") || userMessage.contains("water")) {
            return "Usafi wa maji ni muhimu sana kwa kuzuia kuenea kwa homa ya matumbo. "
                    + "Hakikisha kuwa maji yanayotolewa kwa kuku ni safi na yanabadilishwa angalau mara mbili kwa siku.";
        } else if (userMessage.contains("dawa") || userMessage.contains("medicine") || userMessage.contains("treatment")) {
            return "Kwa matibabu, ningeshauri kutumia antibiotiki iliyopendekezwa chini ya usimamizi wa mtaalamu. "
                    + "Nitakuja kutembelea shamba lako kuchunguza kuku kabla ya kutoa dawa mahususi.";
        } else if (userMessage.contains("asante") || userMessage.contains("thank")) {
            return "Karibu sana. Niko hapa kukusaidia. Je, kuna maswali mengine yoyote kuhusu afya ya kuku wako?";
        } else {
            return "Asante kwa kushiriki taarifa hizo. Je, unaweza kuniambia zaidi kuhusu mabadiliko yoyote ya hivi karibuni "
                    + "katika mazingira ya kuku wako au mabadiliko yoyote ya tabia?";
        }
    }

    private String generateFarmerResponse(String userMessage) {
        // More intelligent response generator based on user input
        userMessage = userMessage.toLowerCase();

        if (userMessage.contains("dalili") || userMessage.contains("symptom")) {
            return "Nimeona kuku kadhaa wana hamu ndogo ya kula na kuhara manjano tangu jana. "
                    + "Wengine pia wanaonekana kuwa na upanga mweupe zaidi kuliko kawaida.";
        } else if (userMessage.contains("chakula") || userMessage.contains("food")) {
            return "Nimekuwa nikiwapa chakula cha kawaida cha virutubisho kwa wiki kadhaa sasa. "
                    + "Sikubadilisha aina ya chakula, lakini nilinunua mfuko mpya wiki iliyopita kutoka kwa muuzaji tofauti.";
        } else if (userMessage.contains("maji") || userMessage.contains("water")) {
            return "Ninawapa maji safi kutoka kisima chetu. Nimegundua kwamba wanapunguza matumizi ya maji "
                    + "siku chache zilizopita. Ninahakikisha ninabadilisha maji yao mara mbili kwa siku.";
        } else if (userMessage.contains("idadi") || userMessage.contains("how many")) {
            return "Nina jumla ya kuku 40. Kati ya hao, kuku 8 wanaonyesha dalili. "
                    + "Niliongeza kuku 10 wapya wiki iliyopita kutoka sokoni.";
        } else if (userMessage.contains("tembelea") || userMessage.contains("visit")) {
            return "Asante daktari. Kesho asubuhi inafaa kwangu. "
                    + "Nitahakikisha ninatenga kuku walioathirika kabla ya kuwasili kwako.";
        } else {
            return "Nashukuru kwa msaada wako. Nina wasiwasi juu ya kuku wangu kwani hiki ndicho chanzo "
                    + "kikuu cha mapato yangu. Je, kuna hatua za dharura ninazoweza kuchukua sasa?";
        }
    }

    private void showTypingIndicator(boolean show) {
        if (show) {
            typingIndicatorLayout.setVisibility(View.VISIBLE);
        } else {
            typingIndicatorLayout.setVisibility(View.GONE);
        }
    }

    private void addSystemMessage(String message) {
        long timestamp = System.currentTimeMillis();
        ChatMessage systemMessage = new ChatMessage(
                ChatMessage.MessageType.SYSTEM,
                "Mfumo",
                message,
                timestamp,
                false // System messages are not from the current user
        );
        chatMessages.add(systemMessage);
        chatAdapter.notifyDataSetChanged();
    }

    private void addSampleMessages() {
        // Add some sample messages to demonstrate the conversation
        long baseTimestamp = System.currentTimeMillis() - 3600000; // 1 hour ago

        chatMessages.add(new ChatMessage(
                ChatMessage.MessageType.OTHER,
                "Mkulima",
                "Hujambo daktari, ninaona tabia za ajabu kwa kuku wangu.",
                baseTimestamp,
                false
        ));

        chatMessages.add(new ChatMessage(
                ChatMessage.MessageType.OTHER,
                "Daktari",
                "Hujambo! Unaweza kuelezea unachoona?",
                baseTimestamp + 120000, // 2 minutes later
                false
        ));

        chatMessages.add(new ChatMessage(
                ChatMessage.MessageType.OTHER,
                "Mkulima",
                "Kuku kadhaa wanaonekana dhaifu na wana kuhara kwa rangi ya kijani.",
                baseTimestamp + 180000, // 3 minutes later
                false
        ));

        chatMessages.add(new ChatMessage(
                ChatMessage.MessageType.OTHER,
                "Daktari",
                "Kuku wangapi wameathirika? Je, kuna mabadiliko yoyote ya hivi karibuni katika chakula au maji?",
                baseTimestamp + 300000, // 5 minutes later
                false
        ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handlers
        typingTimeoutHandler.removeCallbacks(typingTimeoutRunnable);
        typingHandler.removeCallbacks(typingRunnable);
    }

    // ChatMessage class to store message data
    public static class ChatMessage {
        public enum MessageType {
            SYSTEM, USER, OTHER
        }

        private MessageType type;
        private String sender;
        private String message;
        private long timestamp;
        private boolean isFromCurrentUser;

        public ChatMessage(MessageType type, String sender, String message, long timestamp, boolean isFromCurrentUser) {
            this.type = type;
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
            this.isFromCurrentUser = isFromCurrentUser;
        }

        public MessageType getType() {
            return type;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        public boolean isFromCurrentUser() {
            return isFromCurrentUser;
        }
    }
}