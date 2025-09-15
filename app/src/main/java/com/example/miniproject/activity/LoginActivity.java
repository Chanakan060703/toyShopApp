package com.example.miniproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.manager.UserManager;
import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.task.LoginTask;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        userManager = new UserManager(this);

        // ตรวจสอบว่าเป็นผู้ใช้ที่ล็อกอินแล้วหรือยัง
        if (userManager.isLoggedIn()) {
            goToMainActivity(); // ถ้าล็อกอินแล้วไปที่หน้า MainActivity
        }

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกอีเมลและรหัสผ่าน", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        // เรียก API เพื่อเข้าสู่ระบบ
        new LoginTask(this, new CallBackService<String>() {
            @Override
            public void onSuccess(List<String> response) {
                progressBar.setVisibility(View.GONE);
                Log.d("LoginActivity", "Response: " + response); // ตรวจสอบข้อมูลที่ได้รับจาก API

                // ตรวจสอบขนาดของข้อมูล
                if (response.size() == 1) {
                    // สมมติว่า response.get(0) คือ ข้อความ "Login successful, user ID: 1"
                    String result = response.get(0);

                    // แยกข้อมูลจากข้อความที่ได้รับ
                    if (result.contains("user ID:")) {
                        String userId = result.split("user ID:")[1].trim(); // ดึง user ID ออกมา
                        // บันทึกข้อมูลผู้ใช้ใน SharedPreferences
                        saveUserData(userId, "Username", "Email");

                        goToMainActivity();  // ไปที่หน้า MainActivity
                    } else {
                        Toast.makeText(LoginActivity.this, "ข้อมูลไม่ครบถ้วนจากเซิร์ฟเวอร์", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LoginActivity", "Invalid response size: " + response.size());
                    Toast.makeText(LoginActivity.this, "ข้อมูลไม่ครบถ้วนจากเซิร์ฟเวอร์", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(User user) {
                // หากใช้ User object ในการส่งกลับข้อมูล
            }

            @Override
            public void onSuccess(String message) {
                // หากมีข้อความที่ต้องการแสดงผล
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "เข้าสู่ระบบล้มเหลว: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }).execute(email, password);
    }

    private void saveUserData(String userId, String username, String email) {
        // บันทึกข้อมูลผู้ใช้ใน SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", userId); // บันทึก user_id
        editor.putString("username", username); // บันทึก username
        editor.putString("email", email); // บันทึก email
        editor.apply(); // บันทึกข้อมูล

        String savedUserId = sharedPreferences.getString("user_id", "default");
        Log.d("LoginActivity", "User ID saved: " + savedUserId);
    }

    private void goToMainActivity() {
        Log.d("LoginActivity", "Navigating to MainActivity");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // ปิดหน้า LoginActivity
    }
}