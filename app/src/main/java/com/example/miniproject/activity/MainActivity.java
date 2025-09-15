package com.example.miniproject.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject.R;
import com.example.miniproject.adapter.ProductAdapter;
import com.example.miniproject.adapter.ProductTypeSpinnerAdapter;
import com.example.miniproject.model.*;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.task.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CallBackService<Product>, FetchProductTypesTask.OnProductTypesFetchedListener, ProductAdapter.OnEditClickListener {

    private Spinner spnType;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> allProducts;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spnType = findViewById(R.id.spnType);
        recyclerView = findViewById(R.id.show_listProduct);
        searchView = findViewById(R.id.searchView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnAddProduct = findViewById(R.id.btnaddproduct2);

        // กำหนดค่า RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // โหลดข้อมูลประเภทสินค้า
        fetchProductTypes();

        // โหลดข้อมูลสินค้า
        loadProducts();

        // ตั้งค่า SearchView
        setupSearchView();

        // ตั้งค่า Bottom Navigation
        setupBottomNavigation();

        // ตั้งค่าปุ่มเพิ่มสินค้า
        setupAddProductButton();
    }

    // โหลดประเภทสินค้า
    private void fetchProductTypes() {
        FetchProductTypesTask fetchProductTypesTask = new FetchProductTypesTask(this, this);
        fetchProductTypesTask.execute();
    }

    // โหลดข้อมูลสินค้า
    private void loadProducts() {
        ProductTask productTask = new ProductTask(this, this);
        productTask.execute();
    }

    // ตั้งค่า SearchView
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadProducts(); // โหลดข้อมูลทั้งหมดหากไม่มีคำค้นหา
                } else {
                    searchProducts(newText); // ค้นหาสินค้า
                }
                return true;
            }
        });
    }

    // ค้นหาสินค้า
    private void searchProducts(String keyword) {
        ProductTask productTask = new ProductTask(this, this, keyword);
        productTask.execute();
    }

    public void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Home selected");
                return true;
            } else if (itemId == R.id.nav_cart) {
                Log.d("Navigation", "Cart selected");
                Intent cartIntent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(cartIntent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Log.d("Navigation", "Profile selected");
                if (isLoggedIn()) {
                    // ถ้าผู้ใช้ล็อกอินแล้วให้โหลดข้อมูลโปรไฟล์
                    loadProfile();
                } else {
                    // ถ้ายังไม่ได้ล็อกอินให้เปิดหน้า LoginActivity
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                }
                return true;
            }
            return false;
        });
    }

    // ตรวจสอบการล็อกอิน
    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return sharedPreferences.contains("user_id");
    }

    // โหลดข้อมูลโปรไฟล์
    private void loadProfile() {
        if (isInternetAvailable()) {
            if (isLoggedIn()) {
                String userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("user_id", "");
                if (userId != null && !userId.isEmpty()) {
                    GetProfileTask getProfileTask = new GetProfileTask(MainActivity.this, new CallBackService<User>() {
                        @Override
                        public void onSuccess(List<User> data) {
                            Log.d("ProfileActivity", "User data received: " + data);
                            if (data != null && !data.isEmpty()) {
                                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("USER", data.get(0)); // ส่งออบเจกต์ User
                                startActivity(profileIntent);
                            } else {
                                Toast.makeText(MainActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onSuccess(User user) {
                            // Handle single user success if needed
                        }

                        @Override
                        public void onSuccess(String message) {
                            // Handle success message if needed
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("ProfileActivity", "Error loading profile: " + errorMessage);
                            Toast.makeText(MainActivity.this, "Failed to load profile: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                    getProfileTask.execute(userId); // ส่ง user_id ไปที่ GetProfileTask
                } else {
                    Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // ตั้งค่าปุ่มเพิ่มสินค้า
    private void setupAddProductButton() {
        btnAddProduct.setOnClickListener(v -> {
            Intent addProductIntent = new Intent(MainActivity.this, AddProductActivity.class);
            startActivity(addProductIntent);
        });
    }

    // Callback เมื่อโหลดประเภทสินค้าสำเร็จ
    @Override
    public void onProductTypesFetched(List<ProductType> productTypes) {
        if (productTypes != null && !productTypes.isEmpty()) {
            productTypes.add(0, new ProductType(-1, "ทั้งหมด")); // เพิ่มประเภท "ทั้งหมด"

            ProductTypeSpinnerAdapter adapter = new ProductTypeSpinnerAdapter(
                    this,
                    R.layout.item_spinner_product_type,
                    productTypes
            );
            spnType.setAdapter(adapter);

            spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ProductType selectedType = (ProductType) parent.getItemAtPosition(position);
                    if (selectedType != null) {
                        int typeId = selectedType.getType_id();
                        if (typeId == -1) {
                            loadProducts(); // โหลดข้อมูลทั้งหมด
                        } else {
                            filterProductsByTypeId(typeId); // กรองข้อมูลตามประเภท
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    loadProducts(); // โหลดข้อมูลทั้งหมด
                }
            });
        } else {
            Toast.makeText(this, "No product types found", Toast.LENGTH_SHORT).show();
        }
    }

    // กรองสินค้าตามประเภท
    private void filterProductsByTypeId(int typeId) {
        if (allProducts != null) {
            List<Product> filteredProducts = new ArrayList<>();
            for (Product product : allProducts) {
                if (product.getType_id() == typeId) {
                    filteredProducts.add(product);
                }
            }
            if (filteredProducts.isEmpty()) {
                Toast.makeText(this, "ไม่พบสินค้าตามประเภทที่เลือก", Toast.LENGTH_SHORT).show();
            }
            productAdapter.updateData(filteredProducts);
        }
    }

    @Override
    public void onSuccess(List<Product> data) {
        if (data != null && !data.isEmpty()) {
            allProducts = data;
            productAdapter = new ProductAdapter(MainActivity.this, data); // ส่ง context และ data
            productAdapter.setOnEditClickListener(this); // ตั้งค่า listener สำหรับการคลิกปุ่ม Edit

            productAdapter.setOnItemClickListener(product -> {
                // ส่งข้อมูลสินค้าไปยัง ProductDetailActivity
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                intent.putExtra("product", product); // ใช้ชื่อ "product"
                startActivity(intent);
            });

            recyclerView.setAdapter(productAdapter);
        } else {
            Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(User user) {
        // Handle single user success if needed
    }

    @Override
    public void onSuccess(String message) {
        // Handle success message if needed
    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Callback เมื่อคลิกปุ่ม Edit
    @Override
    public void onEditClick(Product product) {
        // ส่งข้อมูลสินค้าที่ต้องการแก้ไขไปที่ EditProductActivity
        Intent intent = new Intent(MainActivity.this, EditProductActivity.class);
        intent.putExtra("product", product); // ส่งข้อมูลสินค้า
        startActivityForResult(intent, 1); // รอรับผลลัพธ์จาก EditProductActivity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // เมื่อกลับมาจากการแก้ไขสินค้า, อัปเดตสินค้าในรายการ
            loadProducts();
        }
    }
}