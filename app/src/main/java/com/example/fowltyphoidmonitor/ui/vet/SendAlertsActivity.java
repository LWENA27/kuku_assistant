package com.example.fowltyphoidmonitor.ui.vet;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SendAlertsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Send Alerts - Under Development");
        tv.setPadding(50, 50, 50, 50);
        setContentView(tv);
    }
}