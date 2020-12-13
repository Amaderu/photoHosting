package com.example.photohosting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Logged_out extends AppCompatActivity {
    private Button btnLog,btnReg;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);*/
        setContentView(R.layout.activity_logged_out);

        btnLog = (Button) findViewById(R.id.log);
        btnReg = (Button) findViewById(R.id.reg);
    }


    public void nextActivity(View v) {
        switch (v.getId()) {
            case R.id.log:
                intent = new Intent(Logged_out.this, Login.class);
                intent.putExtra("Status","log");
                break;
            case R.id.reg:
                intent = new Intent(Logged_out.this, Login.class);
                intent.putExtra("Status","reg");
                break;
            default:
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}