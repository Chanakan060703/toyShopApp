package com.example.miniproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.R;
import com.example.miniproject.manager.CartManager;
import com.example.miniproject.model.Product;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<Product> cartItems;
    private final OnQuantityChangeListener quantityChangeListener;

    public CartAdapter(List<Product> cartItems, OnQuantityChangeListener listener) {
        this.cartItems = cartItems;
        this.quantityChangeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartItems.get(position);

        holder.tvProductName.setText(product.getProduct_name());
        holder.tvPrice.setText(String.format("ราคา: %.2f บาท", product.getPrice()));
        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = product.getQuantity() + 1;
            CartManager.getInstance().updateProductQuantity(product, newQuantity);
            holder.tvQuantity.setText(String.valueOf(newQuantity));
            quantityChangeListener.onQuantityChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (product.getQuantity() > 1) {
                int newQuantity = product.getQuantity() - 1;
                CartManager.getInstance().updateProductQuantity(product, newQuantity);
                holder.tvQuantity.setText(String.valueOf(newQuantity));
                quantityChangeListener.onQuantityChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPrice, tvQuantity;
        Button btnIncrease, btnDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}