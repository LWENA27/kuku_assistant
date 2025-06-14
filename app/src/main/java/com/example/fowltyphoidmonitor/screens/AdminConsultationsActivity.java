package com.example.fowltyphoidmonitor.screens;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminConsultationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Admin Consultations - Under Development");
        tv.setPadding(50, 50, 50, 50);
        setContentView(tv);
    }
}