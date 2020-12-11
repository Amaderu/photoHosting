package com.example.photohosting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    private EditText Mail;
    private EditText Password;
    private Button btnNext;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }
    private void init(){
        Mail = (EditText) findViewById(R.id.edtFieldMail);
        Password = (EditText) findViewById(R.id.edtFieldPass);
        btnNext = (Button) findViewById(R.id.login_btnNext);

    }

    private boolean validateUser() {
        if (!validatePassword() || !validateMail()) return false;
        return true;
    }
    private boolean validateMail() {
        final String MailInput = Mail.getText().toString().trim();
        if (MailInput.isEmpty()) {
            Mail.setError("Field can't be empty");
            return false;
        } else {
            Mail.setError(null);
            return true;
        }
    }
    private boolean validatePassword() {
        final String passwordInput = Password.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            Password.setError("Field can't be empty");
            return false;
        } else {
            Password.setError(null);
            return true;
        }
    }

    public void enter(View v) {
        if (!validateUser()) return;
        Intent intent = new Intent(Login.this, MainActivity.class);
        Intent notificationIntent = new Intent(Login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
    }
}