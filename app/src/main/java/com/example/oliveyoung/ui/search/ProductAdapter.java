package com.example.oliveyoung.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.example.oliveyoung.api.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private List<Product> items = new ArrayList<>();
    private final OnProductClickListener listener;

    public ProductAdapter(List<Product> initialItems,
                          OnProductClickListener listener) {
        if (initialItems != null) {
            this.items = initialItems;
        }
        this.listener = listener;
    }

    public void setItems(List<Product> newItems) {
        if (newItems == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = new ArrayList<>(newItems);
        }
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
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final TextView textProductName;
        private final TextView textBrand;
        private final TextView textPrice;
        private final TextView textLocation;

        public ProductViewHolder(View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
            textBrand = itemView.findViewById(R.id.textBrand);
            textPrice = itemView.findViewById(R.id.textPrice);
            textLocation = itemView.findViewById(R.id.textLocation);
        }

        public void bind(final Product product,
                         final OnProductClickListener listener) {
            textProductName.setText(product.getName());
            textBrand.setText(product.getBrand());

            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            String priceStr = "₩" + nf.format(product.getPrice());
            textPrice.setText(priceStr);

            String zone = product.getZone();
            if (zone == null || zone.trim().isEmpty()) {
                textLocation.setText("📍 위치 정보 없음");
            } else {
                textLocation.setText("📍 " + zone);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
