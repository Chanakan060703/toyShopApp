package com.example.miniproject.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.miniproject.R;
import com.example.miniproject.activity.EditProductActivity;
import com.example.miniproject.model.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnEditClickListener onEditClickListener;
    private OnItemClickListener onItemClickListener;

    public interface OnEditClickListener {
        void onEditClick(Product product);
    }
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getProduct_name());
        holder.tvPrice.setText("ราคา :" + product.getPrice() + "บาท");
        holder.tvDetail.setText(product.getDetail());

        String imageUrl = context.getString(R.string.root_url) +
                context.getString(R.string.product_image) +
                product.getPic_product();

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.item_background)
                .into(holder.imgProduct);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(product);
            }
        });

        holder.btnEditProduct.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(product);

                // สร้าง Intent เพื่อเปิดหน้า EditProductActivity
                Intent editIntent = new Intent(context, EditProductActivity.class);
                editIntent.putExtra("PRODUCT", product); // ส่งข้อมูลสินค้าผ่าน Intent
                ((Activity) context).startActivityForResult(editIntent, 1); // ใช้ startActivityForResult เพื่อรับผลลัพธ์กลับมา
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<Product> newProductList) {
        if (newProductList != null) {
            this.productList = newProductList;
            notifyDataSetChanged();
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvPrice, tvDetail;
        Button btnEditProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            btnEditProduct = itemView.findViewById(R.id.btnEditProduct);
        }
    }

}