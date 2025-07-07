package com.example.fowltyphoidmonitor.ui.common;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fowltyphoidmonitor.R;

/**
 * Activity for viewing chat attachments (images, documents, etc.)
 * This is a placeholder implementation that can be enhanced later
 */
public class AttachmentViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For now, just show a toast and close
        String attachmentUrl = getIntent().getStringExtra("attachment_url");
        String attachmentType = getIntent().getStringExtra("attachment_type");

        Toast.makeText(this, "Kivinjari cha viunganishi kitaongezwa hivi karibuni", Toast.LENGTH_LONG).show();

        // Close the activity after showing the message
        finish();
    }
}
