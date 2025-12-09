package com.example.oliveyoung.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.example.oliveyoung.api.AiChatRecommendation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AiRecommendationAdapter extends RecyclerView.Adapter<AiRecommendationAdapter.ViewHolder> {

    private final List<AiChatRecommendation> items = new ArrayList<>();

    // 리스트 갱신 메서드
    public void setItems(List<AiChatRecommendation> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    // ✅ 전체 초기화 메서드 추가
    public void clearAll() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        TextView textBrandPrice;
        TextView textReason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textBrandPrice = itemView.findViewById(R.id.textBrandPrice);
            textReason = itemView.findViewById(R.id.textReason);
        }

        public void bind(AiChatRecommendation item) {
            // 상품명
            textName.setText(item.getName());

            // 가격 표시 (1,000 단위 콤마)
            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            String price = nf.format(item.getPrice()) + "원";

            // 브랜드 + 가격
            textBrandPrice.setText(item.getBrand() + " | " + price);

            // 추천 이유 표시
            String reason = item.getReason();
            if (reason == null || reason.isEmpty()) {
                reason = "추천 이유가 제공되지 않았습니다.";
            }
            textReason.setText("추천 이유: " + reason);
        }
    }
}
