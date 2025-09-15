package com.example.miniproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.task.RegisterTask;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstname, etLastname, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoToLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // เชื่อมโยง UI กับตัวแปร
        etFirstname = findViewById(R.id.etFirstname);
        etLastname = findViewById(R.id.etLastname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        progressBar = findViewById(R.id.progressBar2);  // เชื่อมโยง ProgressBar

        // ปุ่มสมัครสมาชิก
        btnRegister.setOnClickListener(view -> registerUser());

        // ปุ่มไปหน้า Login
        btnGoToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String firstname = etFirstname.getText().toString().trim();
        String lastname = etLastname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // ตรวจสอบว่ากรอกข้อมูลครบถ้วน
        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
            return;
        }

        // ตรวจสอบอีเมล
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "อีเมลไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
            return;
        }

        // ตรวจสอบรหัสผ่าน
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show();
            return;
        }

        // สร้าง User object
        User newUser = new User(firstname, lastname, email, password);

        // แสดง Loading Indicator
        progressBar.setVisibility(View.VISIBLE);

        // เรียกใช้งาน RegisterTask
        new RegisterTask(this, new CallBackService<String>() {
            @Override
            public void onSuccess(List<String> data) {
                // Handle onSuccess (this should not be called since we're using onSuccess(String message))
            }

            @Override
            public void onSuccess(User user) {
                // Handle onSuccess (this should not be called since we're using onSuccess(String message))
            }

            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }).execute(newUser);
    }
}