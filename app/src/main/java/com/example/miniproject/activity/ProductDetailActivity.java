package com.example.miniproject.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.manager.*;
import com.example.miniproject.model.Product;
import com.example.miniproject.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class ProductDetailActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // รับข้อมูลสินค้าจาก Intent
        Bundle bundle = getIntent().getExtras();
        if (bundle == null || !bundle.containsKey("product")) {
            Toast.makeText(this, "Product data is missing", Toast.LENGTH_SHORT).show();
            finish(); // ปิด Activity หากไม่มีข้อมูลสินค้า
            return;
        }

        Product product = (Product) bundle.getSerializable("product");
        if (product == null) {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish(); // ปิด Activity หากข้อมูลสินค้าไม่ถูกต้อง
            return;
        }

        // กำหนดค่า View
        ImageView imgProduct = findViewById(R.id.imgProductDetail);
        TextView tvProductName = findViewById(R.id.tvProductNameDetail);
        TextView tvPrice = findViewById(R.id.tvPriceDetail);
        TextView tvDetail = findViewById(R.id.tvDetailDetail);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        tvProductName.setText(product.getProduct_name());
        tvPrice.setText("ราคา: " + product.getPrice() + " บาท");
        tvDetail.setText(product.getDetail());

        if (product.getPic_product() != null && !product.getPic_product().isEmpty()) {
            String imageUrl = getString(R.string.root_url) + getString(R.string.product_image) + product.getPic_product();
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background) // รูปภาพ placeholder หากโหลดไม่สำเร็จ
                    .error(R.drawable.item_background) // รูปภาพ error หากโหลดไม่สำเร็จ
                    .into(imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.ic_launcher_background); // รูปภาพเริ่มต้นหากไม่มี URL
        }

        // จัดการการคลิกปุ่มเพิ่มสินค้าลงตะกร้า
        btnAddToCart.setOnClickListener(v -> {
            boolean isAdded = CartManager.getInstance().addProduct(product);
            if (isAdded) {
                Toast.makeText(this, "เพิ่มสินค้าเข้าตะกร้าแล้ว", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "สินค้านี้มีอยู่ในตะกร้าแล้ว จำนวนเพิ่มขึ้น", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);

        });

        // ตั้งค่า Bottom Navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e("ProductDetailActivity", "BottomNavigationView is null");
            return;
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Home selected");
                Intent homeIntent = new Intent(ProductDetailActivity.this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // ล้าง stack ของ Activity
                startActivity(homeIntent);
                return true;
            } else if (itemId == R.id.nav_cart) {
                Log.d("Navigation", "Cart selected");
                Intent cartIntent = new Intent(ProductDetailActivity.this, CartActivity.class);
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

                    Intent profileIntent = new Intent(ProductDetailActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("USER", user);
                    startActivity(profileIntent);
                } else {
                    Intent loginIntent = new Intent(ProductDetailActivity.this, LoginActivity.class);
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