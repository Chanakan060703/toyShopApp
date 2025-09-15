package com.example.miniproject.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Product implements Serializable {
    private int product_id;

    private String product_name;

    private double price;

    private String detail;

    private int product_qty;

    private String pic_product;

    private int type_id;

    private int quantity;

    public Product() {
        this.quantity = 1;
    }

    public Product(int product_id, String product_name, double price, String detail, int product_qty, String pic_product, int type_id) {
        this.product_id = product_id;
        this.product_name = product_name != null ? product_name : "";
        this.price = price >= 0 ? price : 0;
        this.detail = detail != null ? detail : "";
        this.product_qty = product_qty;
        this.pic_product = pic_product;
        this.type_id = type_id;
        this.quantity = 1;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name != null ? product_name : "";
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price >= 0 ? price : 0;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail != null ? detail : "";
    }

    public int getProduct_qty() {
        return product_qty;
    }

    public void setProduct_qty(int product_qty) {
        this.product_qty = product_qty >= 0 ? product_qty : 0;
    }

    public String getPic_product() {
        return pic_product;
    }

    public void setPic_product(String pic_product) {
        this.pic_product = pic_product != null ? pic_product : "";
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "product_id=" + product_id +
                ", product_name='" + product_name + '\'' +
                ", price=" + price +
                ", detail='" + detail + '\'' +
                ", product_qty=" + product_qty +
                ", pic_product='" + pic_product + '\'' +
                ", type_id=" + type_id +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return product_id == product.product_id;
    }

    @Override
    public int hashCode() {
        return product_id;
    }
}
