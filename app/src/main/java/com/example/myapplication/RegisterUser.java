package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    private TextView banner, registeruser;
    private EditText edtextFullName, edtextAge, edtextEmail, edtextPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        banner = (TextView) findViewById(R.id.tvBanner);
        banner.setOnClickListener(this);

        registeruser = (Button) findViewById(R.id.btnRegister);
        registeruser.setOnClickListener(this);


        edtextFullName = (EditText) findViewById(R.id.edtFullName);
        edtextAge = (EditText) findViewById(R.id.edtAge);
        edtextEmail = (EditText) findViewById(R.id.edtEmail);
        edtextPassword = (EditText) findViewById(R.id.edtPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvBanner:
                startActivity(new Intent(this, Login.class));
                break;
            case R.id.btnRegister:
                registerUser();
                break;
        }
    }
    private void registerUser() {
        String email = edtextEmail.getText().toString().trim();
        String password = edtextPassword.getText().toString().trim();
        String age = edtextAge.getText().toString().trim();
        String fullName = edtextFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            edtextFullName.setError("FullName is required");
            edtextFullName.requestFocus();
            return;
        }
        if (age.isEmpty()) {
            edtextAge.setError("Age is required");
            edtextAge.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edtextEmail.setError("Email is required");
            edtextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtextEmail.setError("Please provide valid email!");
            edtextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtextPassword.setError("Password is required");
            edtextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            edtextPassword.setError("Min password length should be 6 characters");
            edtextPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        task.isSuccessful();
                        if (task.isComplete())
                        {
                            User us = new User(fullName, age, email);
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(us).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterUser.this, "User has been registered successfully", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        Toast.makeText(RegisterUser.this, "Failed to Register! Try again", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(RegisterUser.this, "Failed to Register1! Try again", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}