package com.example.miniproject.manager;

import android.content.Context;

import com.example.miniproject.model.Product;
import com.example.miniproject.service.CallBackService;
import com.example.miniproject.task.ProductTask;

import java.io.File;
import java.util.List;

public class ProductManager {

    private Context context;

    public ProductManager(Context context) {
        this.context = context;
    }

    public void fetchProducts(CallBackService<Product> callback) {
        new ProductTask(context, callback).execute();
    }

    public void searchProducts(String keyword, CallBackService<Product> callback) {
        new ProductTask(context, callback, keyword).execute();
    }

    public void addProduct(Product product, File imageFile, CallBackService<Product> callback) {
        new ProductTask(context, callback, "add", product, imageFile).execute();
    }

    public void updateProduct(Product product, File imageFile, CallBackService<Product> callback) {
        new ProductTask(context, callback, "update", product, imageFile).execute();
    }
}