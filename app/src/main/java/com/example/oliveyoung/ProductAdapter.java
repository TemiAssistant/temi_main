package com.example.oliveyoung;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    private List<Product> items = new ArrayList<>();
    private OnItemClickListener listener;

    public ProductAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Product> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = items.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textBrandCategory;
        TextView textPriceAndZone;

        public ProductViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textBrandCategory = itemView.findViewById(R.id.textBrandCategory);
            textPriceAndZone = itemView.findViewById(R.id.textPriceAndZone);
        }

        public void bind(Product product, OnItemClickListener listener) {
            textName.setText(product.getName());

            String brandCategory = product.getBrand() + " · " +
                    product.getCategory() + " / " + product.getSub_category();
            textBrandCategory.setText(brandCategory);

            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            String priceStr = "₩" + nf.format(product.getPrice());

            String zone = product.getLocationZone();
            if (zone == null) zone = "위치 정보 없음";

            textPriceAndZone.setText(priceStr + " · 존 " + zone);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(product);
                }
            });
        }
    }
}