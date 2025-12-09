package com.example.oliveyoung.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<CartItem> items = new ArrayList<>();
    private OnItemChangeListener listener;

    public interface OnItemChangeListener {
        void onItemRemoved(int position);
        void onQuantityChanged();
    }

    public void setOnItemChangeListener(OnItemChangeListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            if (listener != null) {
                listener.onItemRemoved(position);
            }
        }
    }

    public void clearAll() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textBrand;
        TextView textName;
        TextView textPrice;
        TextView textQuantity;
        Button btnDecrease;
        Button btnIncrease;
        Button btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textBrand = itemView.findViewById(R.id.textBrand);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem item, int position) {
            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);

            // 브랜드
            textBrand.setText(item.getProduct().getBrand());

            // 상품명
            textName.setText(item.getProduct().getName());

            // 가격
            textPrice.setText(nf.format(item.getLineTotal()) + " 원");

            // 수량
            textQuantity.setText(String.valueOf(item.getQuantity()));

            // 감소 버튼
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.decrease();
                    notifyItemChanged(position);
                    if (listener != null) {
                        listener.onQuantityChanged();
                    }
                }
            });

            // 증가 버튼
            btnIncrease.setOnClickListener(v -> {
                item.increase();
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onQuantityChanged();
                }
            });

            // 삭제 버튼
            btnRemove.setOnClickListener(v -> removeItem(position));
        }
    }
}