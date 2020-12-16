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
    private EditText Password ,ConPass;
    private Button btnNext, btnBack;

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
                    "(?=.*[a-zA-Z0-9])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 4 characters
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
        btnBack = (Button) findViewById(R.id.btnBackLog);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        lable = (TextView) findViewById(R.id.textView);
        if (Status.equals("reg")) {
            lable.setText(R.string.reg);
            ConPass= (EditText) findViewById(R.id.edtFieldConPass);
            ConPass.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateUser() {
        if (!validateMail() ||!validatePassword()) return false;
        return true;
    }

    private boolean validateMail() {
        final String MailInput = Mail.getText().toString().trim();
        if (MailInput.isEmpty()) {
            Mail.setError("Незаполненое поле");
            return false;
        } else {
            Mail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        final String passwordInput = Password.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            Password.setError("Незаполненое поле");
            return false;
        } else {
            Password.setError(null);
            return true;
        }
    }
    private boolean validateCreateUser() {
        if (!validateEmailReg() || !validatePasswordReg() || !validateConPassword() ) return false;
        return true;
    }

    private boolean validateEmailReg() {
        String emailInput = Mail.getText().toString().trim();
        if (emailInput.isEmpty()) {
            Mail.setError("Незаполненое поле");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            Mail.setError("введите существующий электронный адресс");
            return false;
        } else {
            Mail.setError(null);
            return true;
        }
    }

    private boolean validatePasswordReg() {
        String passwordInput = Password.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            Password.setError("Незаполненое поле");
            return false;
        }else if (passwordInput.length() < 8) {
            Password.setError("Пароль должен быть длиннее 8 символов");
            return false;
        }else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            Password.setError("Слабый пароль");
            return false;
        } else {
            Password.setError(null);
            return true;
        }
    }
    private boolean validateConPassword(){
        String passwordInput = Password.getText().toString().trim();
        String ConPasswordInput = ConPass.getText().toString().trim();
        if(!passwordInput.equals(ConPasswordInput)){
            ConPass.setError("Пароли не соврадают");
            return false;
        }
        else{
            ConPass.setError(null);
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
                    ConPass.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Вы успешно зарегистрировались", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Регистрация была отклонена", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logInUser(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Успешный вход", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Вход в аккаунт отклонён", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void enter(View v) {
        final String mail, pass;
        mail = Mail.getText().toString();
        pass = Password.getText().toString();
        switch (Status) {
            case "log":
                if (!validateUser()) return;
                logInUser(mail, pass);
                break;
            case "reg":
                if (!validateCreateUser()) return;
                createUser(mail, pass);
                break;
            default:
                break;
        }
    }
}