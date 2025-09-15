package com.example.miniproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.adapter.ProductTypeSpinnerAdapter;
import com.example.miniproject.model.Product;
import com.example.miniproject.model.ProductType;
import com.example.miniproject.model.User;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.task.FetchProductTypesTask;
import com.example.miniproject.task.ProductTask;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class EditProductActivity extends AppCompatActivity implements CallBackService<Product> {

    private ImageView imgProduct;
    private EditText editTextName, editTextPrice, editTextDetail, editTextQty;
    private Spinner spnType;
    private Button btnSave;
    private Product product;
    private List<ProductType> productTypes;
    private File imageFile;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // รับข้อมูลสินค้าจาก Intent
        product = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (product == null) {
            Toast.makeText(this, "Product data is missing", Toast.LENGTH_SHORT).show();
            finish(); // ปิด Activity หากไม่มีข้อมูลสินค้า
            return;
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        imgProduct = findViewById(R.id.imgProducts);
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDetail = findViewById(R.id.editTextDetail);
        editTextQty = findViewById(R.id.editTextQty);
        spnType = findViewById(R.id.spnType);
        btnSave = findViewById(R.id.btnSave);

        // แสดงข้อมูลสินค้า
        showProductDetails();

        setupBottomNavigation();

        // โหลดประเภทสินค้า
        loadProductTypes();

        // ปุ่มบันทึก
        btnSave.setOnClickListener(v -> updateProduct());
    }

    // แสดงข้อมูลสินค้า
    private void showProductDetails() {
        editTextName.setText(product.getProduct_name());
        editTextPrice.setText(String.valueOf(product.getPrice()));
        editTextDetail.setText(product.getDetail());
        editTextQty.setText(String.valueOf(product.getProduct_qty()));

        if (product.getPic_product() != null && !product.getPic_product().isEmpty()) {
            String imageUrl = getString(R.string.root_url) + getString(R.string.product_image) + product.getPic_product();
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background) // รูปภาพ placeholder ขณะโหลด
                    .error(R.drawable.item_background) // รูปภาพแสดงเมื่อเกิดข้อผิดพลาด
                    .into(imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.ic_launcher_background); // รูปภาพเริ่มต้นหากไม่มีรูปภาพ
        }
    }

    // โหลดประเภทสินค้า
    private void loadProductTypes() {
        FetchProductTypesTask fetchProductTypesTask = new FetchProductTypesTask(this, new FetchProductTypesTask.OnProductTypesFetchedListener() {
            @Override
            public void onProductTypesFetched(List<ProductType> productTypes) {
                if (productTypes != null && !productTypes.isEmpty()) {
                    // กำหนดค่า Spinner Adapter
                    ProductTypeSpinnerAdapter adapter = new ProductTypeSpinnerAdapter(
                            EditProductActivity.this,
                            R.layout.item_spinner_product_type,
                            productTypes
                    );
                    spnType.setAdapter(adapter);

                    // เลือกประเภทสินค้าปัจจุบัน (สำหรับ EditProductActivity)
                    for (int i = 0; i < productTypes.size(); i++) {
                        if (productTypes.get(i).getType_id() == product.getType_id()) {
                            spnType.setSelection(i);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(EditProductActivity.this, "No product types found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EditProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        fetchProductTypesTask.execute();
    }

    // อัปเดตสินค้า
    private void updateProduct() {
        String name = editTextName.getText().toString().trim();
        String priceText = editTextPrice.getText().toString().trim();
        String detail = editTextDetail.getText().toString().trim();
        String qtyText = editTextQty.getText().toString().trim();

        // ตรวจสอบข้อมูล
        if (name.isEmpty() || priceText.isEmpty() || detail.isEmpty() || qtyText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int qty;
        try {
            price = Double.parseDouble(priceText);
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        ProductType selectedType = (ProductType) spnType.getSelectedItem();
        if (selectedType == null) {
            Toast.makeText(this, "Please select a product type", Toast.LENGTH_SHORT).show();
            return;
        }

        // อัปเดตข้อมูลสินค้า
        product.setProduct_name(name);
        product.setPrice(price);
        product.setDetail(detail);
        product.setProduct_qty(qty);
        product.setType_id(selectedType.getType_id());

        // ส่งข้อมูลไปยังเซิร์ฟเวอร์
        ProductTask productTask = new ProductTask(this, this, "update", product, imageFile);
        productTask.execute();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Home selected");
                Intent cartIntent = new Intent(EditProductActivity.this, MainActivity.class);
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

                    Intent profileIntent = new Intent(EditProductActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("USER", (CharSequence) user);
                    startActivity(profileIntent);
                } else {
                    Intent loginIntent = new Intent(EditProductActivity.this, LoginActivity.class);
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
    // Callback เมื่ออัปเดตสินค้าสำเร็จ
    @Override
    public void onSuccess(List<Product> data) {
        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();

        // ส่งข้อมูลกลับไปยัง MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_PRODUCT", product);
        setResult(RESULT_OK, resultIntent);
        finish(); // ปิดหน้า EditProductActivity
    }

    @Override
    public void onSuccess(User user) {

    }

    @Override
    public void onSuccess(String message) {

    }

    // Callback เมื่อเกิดข้อผิดพลาด
    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}