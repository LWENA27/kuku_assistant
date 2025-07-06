package com.example.fowltyphoidmonitor.ui.vet;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ManageUsersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Manage Users - Under Development");
        tv.setPadding(50, 50, 50, 50);
        tv.setTextSize(18);
        setContentView(tv);
    }
}