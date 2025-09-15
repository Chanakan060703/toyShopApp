package com.example.miniproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.miniproject.R;
import com.example.miniproject.model.ProductType;
import java.util.List;

public class ProductTypeSpinnerAdapter extends ArrayAdapter<ProductType> {

    private Context context;
    private List<ProductType> productTypes;

    public ProductTypeSpinnerAdapter(Context context, int resource, List<ProductType> productTypes) {
        super(context, resource, productTypes);
        this.context = context;
        this.productTypes = productTypes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_spinner_product_type, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.tvProductType);
        ProductType productType = productTypes.get(position);
        textView.setText(productType.getType_name());

        return convertView;
    }
}