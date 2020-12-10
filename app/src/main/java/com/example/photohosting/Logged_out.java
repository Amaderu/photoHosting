package com.example.photohosting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Logged_out extends AppCompatActivity {
    Button btnLog,btnReg;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_out);
        btnLog = (Button) findViewById(R.id.log);
        btnReg = (Button) findViewById(R.id.reg);
    }

    public void nextActivity(View v) {
        switch (v.getId()) {
            case R.id.log:
                intent = new Intent(Logged_out.this, Login.class);
                break;
            case R.id.reg:
                intent = new Intent(Logged_out.this, Logged_out.class);
                break;
            default:
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}