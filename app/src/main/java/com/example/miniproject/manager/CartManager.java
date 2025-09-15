package com.example.miniproject.manager;

import com.example.miniproject.model.Product;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<Product> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public boolean addProduct(Product product) {
        for (Product item : cartItems) {
            if (item.getProduct_id() == product.getProduct_id()) { // ใช้ == สำหรับตัวเลข
                item.setQuantity(item.getQuantity() + 1); // เพิ่มจำนวนสินค้า
                return false; // แจ้งว่าสินค้าเคยถูกเพิ่มแล้ว
            }
        }
        product.setQuantity(1); // กำหนดค่าเริ่มต้นของสินค้า
        cartItems.add(product);
        return true; // แจ้งว่าสินค้าเพิ่มใหม่สำเร็จ
    }

    public void removeProduct(Product product) {
        cartItems.remove(product);
    }

    public void updateProductQuantity(Product product, int newQuantity) {
        for (Product item : cartItems) {
            if (item.getProduct_id() == product.getProduct_id()) {  // ใช้ == ถ้าเป็น int
                item.setQuantity(newQuantity);
                break;  // เพิ่มการหยุดลูปหลังจากหาสินค้าเจอแล้ว
            }
        }
    }


    public double getTotalPrice() {
        double totalPrice = 0;
        for (Product product : cartItems) {
            totalPrice += product.getPrice() * product.getQuantity();
        }
        return totalPrice;
    }

    public void clearCart() {
        cartItems.clear();
    }
}