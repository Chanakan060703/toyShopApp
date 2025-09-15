package com.example.miniproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.R;
import com.example.miniproject.adapter.CartAdapter;
import com.example.miniproject.manager.CartManager;
import com.example.miniproject.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnQuantityChangeListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        recyclerView = findViewById(R.id.recyclerViewCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadCartItems();

        btnCheckout.setOnClickListener(v -> {
            createReceipt();
        });

        setupBottomNavigation();
    }

    private void loadCartItems() {
        cartAdapter = new CartAdapter(CartManager.getInstance().getCartItems(), this);
        recyclerView.setAdapter(cartAdapter);
        updateTotalPrice();
    }

    @Override
    public void onQuantityChanged() {
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double totalPrice = CartManager.getInstance().getTotalPrice();
        tvTotalPrice.setText("รวมทั้งหมด: " + totalPrice + " บาท");
    }

    private void createReceipt() {
        if (isLoggedIn()) {
            Intent intent = new Intent(CartActivity.this, ReceiptActivity.class);
            startActivity(intent);

            Toast.makeText(this, "สร้างใบเสร็จเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();

            finish();
        }else {
            Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Home selected");
                Intent cartIntent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(cartIntent);
                return true;
            } else if (itemId == R.id.nav_cart) {
                Log.d("Navigation", "Cart selected");
                return true;
            } else if (itemId == R.id.nav_profile) {
                Log.d("Navigation", "Profile selected");
                if (isLoggedIn()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    User user = new User(
                            sharedPreferences.getString("firstname", ""),
                            sharedPreferences.getString("lastname", ""),
                            sharedPreferences.getString("email", ""),
                            ""
                    );

                    Intent profileIntent = new Intent(CartActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("USER", (CharSequence) user);
                    startActivity(profileIntent);
                } else {
                    Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
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
}