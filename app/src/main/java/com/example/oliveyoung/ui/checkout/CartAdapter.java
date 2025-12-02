package com.example.oliveyoung.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartChangeListener {
        void onCartUpdated();
    }

    private List<CartItem> items = new ArrayList<>();
    private final OnCartChangeListener listener;

    public CartAdapter(OnCartChangeListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CartItem> newItems) {
        if (newItems == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = new ArrayList<>(newItems);
        }
        notifyDataSetChanged();
        notifyCartChanged();
    }

    public void addItem(CartItem item) {
        if (item == null) return;
        items.add(item);
        notifyDataSetChanged();
        notifyCartChanged();
    }

    public List<CartItem> getItems() {
        return items;
    }

    public long getTotalPrice() {
        long total = 0;
        for (CartItem item : items) {
            total += item.getLineTotal();
        }
        return total;
    }

    private void notifyCartChanged() {
        if (listener != null) {
            listener.onCartUpdated();
        }
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textLineTotal;
        private final Button buttonIncrease;
        private final Button buttonDecrease;

        public CartViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textProductName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textLineTotal = itemView.findViewById(R.id.textLineTotal);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
        }

        public void bind(CartItem item) {
            textName.setText(item.getProduct().getName());
            textQuantity.setText("수량: " + item.getQuantity());

            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            String totalStr = "합계: ₩" + nf.format(item.getLineTotal());
            textLineTotal.setText(totalStr);

            buttonIncrease.setOnClickListener(v -> {
                item.increase();
                notifyItemChanged(getAdapterPosition());
                notifyCartChanged();
            });

            buttonDecrease.setOnClickListener(v -> {
                item.decrease();
                notifyItemChanged(getAdapterPosition());
                notifyCartChanged();
            });
        }
    }
}
