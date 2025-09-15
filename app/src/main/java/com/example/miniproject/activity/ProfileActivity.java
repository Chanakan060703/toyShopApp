package com.example.miniproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.miniproject.R;
import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ProfileActivity extends AppCompatActivity implements CallBackService<User> {

    private BottomNavigationView bottomNavigationView;
    private TextView tvFirstname, tvLastname, tvEmail;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        tvFirstname = findViewById(R.id.tvFirstname);
        tvLastname = findViewById(R.id.tvLastname);
        tvEmail = findViewById(R.id.tvEmail);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER")) {
            User user = intent.getParcelableExtra("USER");
            if (user != null) {
                Log.d("ProfileActivity", "User data: " + user.toString());
                tvFirstname.setText(user.getFirstname());
                tvLastname.setText(user.getLastname());
                tvEmail.setText(user.getEmail());
            } else {
                Toast.makeText(this, "User data is null", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupBottomNavigation();

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // ลบข้อมูลผู้ใช้ทั้งหมด
        editor.apply();

        Toast.makeText(this, "ออกจากระบบสำเร็จ", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onSuccess(List<User> data) {}

    @Override
    public void onSuccess(User user) {
        tvFirstname.setText(user.getFirstname());
        tvLastname.setText(user.getLastname());
        tvEmail.setText(user.getEmail());
    }

    @Override
    public void onSuccess(String message) {}

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(ProfileActivity.this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (!isLoggedIn()) {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
                return true;
            }
            return false;
        });
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.contains("user_id");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "อนุญาตการแจ้งเตือนแล้ว", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "ไม่อนุญาตให้แอปโพสต์การแจ้งเตือน", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
