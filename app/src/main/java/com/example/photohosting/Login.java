package com.example.photohosting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    private EditText Mail;
    private EditText Password;
    private Button btnNext;

    private FirebaseAuth mAuth;
    private FirebaseUser cUser;

    private String Status;
    private TextView lable;
    private Intent intent;


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
    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth!=null) cUser = mAuth.getCurrentUser();
        if(cUser != null)
        {
            intent = new Intent(Login.this, MainActivity.class);
            final String userId = cUser.getUid();
            intent.putExtra("userId",userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//root
            startActivity(intent);
        }
    }

    private void init() {
        Intent intent = getIntent();
        Status = intent.getStringExtra("Status");
        mAuth = FirebaseAuth.getInstance();
        Mail = (EditText) findViewById(R.id.edtFieldMail);
        Password = (EditText) findViewById(R.id.edtFieldPass);
        btnNext = (Button) findViewById(R.id.login_btnNext);
        lable = (TextView) findViewById(R.id.textView);
        if (Status.equals("reg")) lable.setText(R.string.reg);
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

    private void createUser(String mail, String password) {
        mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    lable.setText(R.string.auth);
                    Status = "log";
                    Toast.makeText(getApplicationContext(), "User SignUp Successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "User SignUp failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logInUser(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "User SignIn Successful", Toast.LENGTH_SHORT).show();
                    cUser = mAuth.getCurrentUser();
                    if(cUser != null)
                    {
                        intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtra("userId",cUser.getUid());
                        //intent.putExtra
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//root
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "User SignIn failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void enter(View v) {
        if (!validateUser()) return;
        final String mail, pass;
        mail = Mail.getText().toString();
        pass = Password.getText().toString();
        switch (Status) {
            case "log":
                logInUser(mail, pass);
                break;
            case "reg":
                createUser(mail, pass);
                break;
            default:
                break;
        }
    }
}