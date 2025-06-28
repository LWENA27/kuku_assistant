package com.example.fowltyphoidmonitor.ui.common;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;

import java.util.HashMap;

public class ConsultationActivity extends AppCompatActivity {

    EditText edtName, edtSymptoms;
    Button btnSendConsultation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation);

        edtName = findViewById(R.id.edtName);
        edtSymptoms = findViewById(R.id.edtSymptoms);
        btnSendConsultation = findViewById(R.id.btnSendConsultation);

        btnSendConsultation.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String symptoms = edtSymptoms.getText().toString();

            if (name.isEmpty() || symptoms.isEmpty()) {
                Toast.makeText(this, "Tafadhali jaza sehemu zote!", Toast.LENGTH_SHORT).show();
            } else {
                sendConsultation(name, symptoms);
            }
        });
    }

    private void sendConsultation(String name, String symptoms) {
        String url = "http://your_server_ip/poultry_api/submit_consultation.php";

        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("symptoms", symptoms);

        com.example.fowltyphoidmonitor.data.api.RequestHandler.sendPostRequest(url, params, response -> {
            Toast.makeText(this, "Ujumbe umetumwa kwa daktari.", Toast.LENGTH_SHORT).show();
            edtSymptoms.setText("");
        });
    }
}
