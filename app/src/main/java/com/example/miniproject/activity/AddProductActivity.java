package com.example.miniproject.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.miniproject.R;
import com.example.miniproject.adapter.ProductTypeSpinnerAdapter;
import com.example.miniproject.model.*;
import com.example.miniproject.model.ProductType;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.task.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class AddProductActivity extends AppCompatActivity implements CallBackService<Product> {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imgProduct;
    private BottomNavigationView bottomNavigationView;

    private File imageFile;
    private EditText editTextName, editTextPrice, editTextDetail, editTextQty;
    private Spinner spnType;
    private Button btnSave;
    private List<ProductType> productTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // กำหนดค่า View
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDetail = findViewById(R.id.editTextDetail);
        editTextQty = findViewById(R.id.editTextQty);
        spnType = findViewById(R.id.spnType);
        btnSave = findViewById(R.id.btnSave);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);


        // โหลดประเภทสินค้า
        loadProductTypes();

        setupBottomNavigation();

        // จัดการการคลิกปุ่ม Save
        btnSave.setOnClickListener(v -> {
            addProduct();
        });
    }

    // โหลดประเภทสินค้า
    private void loadProductTypes() {
        FetchProductTypesTask fetchProductTypesTask = new FetchProductTypesTask(this, new FetchProductTypesTask.OnProductTypesFetchedListener() {
            @Override
            public void onProductTypesFetched(List<ProductType> productTypes) {
                if (productTypes != null && !productTypes.isEmpty()) {
                    // กำหนดค่า Spinner Adapter
                    ProductTypeSpinnerAdapter adapter = new ProductTypeSpinnerAdapter(
                            AddProductActivity.this,
                            R.layout.item_spinner_product_type,
                            productTypes
                    );
                    spnType.setAdapter(adapter);

                    // เลือกประเภทสินค้าปัจจุบัน (สำหรับ EditProductActivity)
                    if (productTypes != null) {
                        for (int i = 0; i < productTypes.size(); i++) {
                            if (productTypes.get(i).getType_id() == productTypes.get(0).getType_id()) {
                                spnType.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(AddProductActivity.this, "No product types found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AddProductActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        fetchProductTypesTask.execute();
    }

    // เพิ่มสินค้า
    private void addProduct() {
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

        // สร้างสินค้าใหม่
        Product product = new Product(0, name, price, detail, qty, "", selectedType.getType_id());

        // ส่งคำขอเพิ่มสินค้าไปยัง API
        ProductTask productTask = new ProductTask(this, this, "add", product, null);
        productTask.execute();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Home selected");
                Intent cartIntent = new Intent(AddProductActivity.this, MainActivity.class);
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

                    Intent profileIntent = new Intent(AddProductActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("USER", (CharSequence) user);
                    startActivity(profileIntent);
                } else {
                    Intent loginIntent = new Intent(AddProductActivity.this, LoginActivity.class);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgProduct.setImageBitmap(bitmap);
                imageFile = new File(getRealPathFromURI(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }


    @Override
    public void onSuccess(List<Product> data) {
        Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();

        // ส่งข้อมูลกลับไปยัง MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("ADDED_PRODUCT", data.get(0)); // ส่งสินค้าที่เพิ่มใหม่กลับไป
        setResult(RESULT_OK, resultIntent);
        finish(); // ปิดหน้า AddProductActivity
    }

    @Override
    public void onSuccess(User user) {

    }

    @Override
    public void onSuccess(String message) {

    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}