package com.example.oliveyoung;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Runnable onCartChanged;   // CheckoutFragment에서 updateTotalPrice() 전달
    private final List<CartItem> items = new ArrayList<>();

    public CartAdapter(Runnable onCartChanged) {
        this.onCartChanged = onCartChanged;
    }

    /** 외부에서 cartItems를 받아 화면 갱신 */
    public void setItems(List<CartItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
        if (onCartChanged != null) onCartChanged.run();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        TextView textProductName;
        TextView textBrand;
        TextView textPrice;
        TextView textQuantity;
        TextView textLineTotal;
        Button buttonDecrease;
        Button buttonIncrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            textProductName = itemView.findViewById(R.id.textProductName);
            textBrand = itemView.findViewById(R.id.textBrand);
            textPrice = itemView.findViewById(R.id.textPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textLineTotal = itemView.findViewById(R.id.textLineTotal);

            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
        }

        public void bind(CartItem item) {
            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);

            textProductName.setText(item.getProduct().getName());
            textBrand.setText(item.getProduct().getBrand());
            textPrice.setText("₩" + nf.format(item.getProduct().getPrice()));
            textQuantity.setText(String.valueOf(item.getQuantity()));
            textLineTotal.setText("합계: ₩" + nf.format(item.getLineTotal()));

            // ➕ 수량 증가
            buttonIncrease.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                CartItem target = items.get(pos);
                target.increase();
                notifyItemChanged(pos);

                if (onCartChanged != null) onCartChanged.run();
            });

            // ➖ 수량 감소 (0이면 자동 삭제)
            buttonDecrease.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                CartItem target = items.get(pos);
                target.decrease();

                if (target.isEmpty()) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                } else {
                    notifyItemChanged(pos);
                }

                if (onCartChanged != null) onCartChanged.run();
            });
        }
    }
}
