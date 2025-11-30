package com.example.oliveyoung.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.example.oliveyoung.api.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    private List<Product> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public ProductAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Product> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
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

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textBrandCategory = itemView.findViewById(R.id.textBrandCategory);
            textPriceAndZone = itemView.findViewById(R.id.textPriceAndZone);
        }

        public void bind(Product product, OnItemClickListener listener) {

            // 상품명
            textName.setText(product.getName());

            // 브랜드 + 카테고리 / 서브카테고리
            String brandCategory =
                    product.getBrand() + " · " +
                            product.getCategory() + " / " +
                            product.getSubCategory();
            textBrandCategory.setText(brandCategory);

            // 가격
            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            String priceStr = "₩" + nf.format(product.getPrice());

            // 존 정보 (null / 빈 문자열 처리)
            String zone = product.getZone();
            if (zone == null || zone.isEmpty()) {
                zone = "위치 정보 없음";
            }

            textPriceAndZone.setText(priceStr + " · 존 " + zone);

            // 클릭 이벤트
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(product);
                }
            });
        }
    }
}
