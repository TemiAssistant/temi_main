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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> items = new ArrayList<>();

    public void setItems(List<CartItem> newItems) {
        items = newItems;
        notifyDataSetChanged();
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

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textQuantity;
        TextView textLineTotal;

        public CartViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textLineTotal = itemView.findViewById(R.id.textLineTotal);
        }

        public void bind(CartItem item) {
            textName.setText(item.getProduct().getName());
            textQuantity.setText("수량: " + item.getQuantity());

            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            String totalStr = "합계: ₩" + nf.format(item.getLineTotal());
            textLineTotal.setText(totalStr);
        }
    }
}