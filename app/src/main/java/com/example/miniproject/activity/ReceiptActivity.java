package com.example.miniproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.miniproject.R;
import com.example.miniproject.manager.CartManager;
import com.example.miniproject.model.Product;
import com.example.miniproject.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ReceiptActivity extends AppCompatActivity {

    private Button btnBackToHome;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        btnBackToHome = findViewById(R.id.btnBackToHome);


        TextView tvReceiptDetails = findViewById(R.id.tvReceiptDetails);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);

        StringBuilder receiptDetails = new StringBuilder();
        double totalAmount = 0;

        for (Product product : CartManager.getInstance().getCartItems()) {
            int quantity = product.getQuantity();
            double itemTotal = product.getPrice() * quantity;
            totalAmount += itemTotal;

            receiptDetails.append(product.getProduct_name())
                    .append(" x ")
                    .append(quantity)
                    .append(" = ")
                    .append(itemTotal)
                    .append(" บาท\n");
        }

        tvReceiptDetails.setText(receiptDetails.toString());
        tvTotalAmount.setText("รวมทั้งหมด: " + totalAmount + " บาท");

        CartManager.getInstance().clearCart();

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiptActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Home selected");
                Intent cartIntent = new Intent(ReceiptActivity.this, MainActivity.class);
                startActivity(cartIntent);
                return true;
            } else if (itemId == R.id.nav_cart) {
                Log.d("Navigation", "Cart selected");
                Intent cartIntent = new Intent(ReceiptActivity.this, CartActivity.class);
                startActivity(cartIntent);
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

                    Intent profileIntent = new Intent(ReceiptActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("USER", (CharSequence) user);
                    startActivity(profileIntent);
                } else {
                    Intent loginIntent = new Intent(ReceiptActivity.this, LoginActivity.class);
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