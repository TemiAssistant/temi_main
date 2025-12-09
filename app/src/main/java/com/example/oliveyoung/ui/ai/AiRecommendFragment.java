package com.example.oliveyoung.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oliveyoung.R;
import com.example.oliveyoung.api.AiChatRequest;
import com.example.oliveyoung.api.AiChatResponse;
import com.example.oliveyoung.api.AiChatExtractedInfo;
import com.example.oliveyoung.api.AiChatPriceRange;
import com.example.oliveyoung.api.ProductApi;
import com.example.oliveyoung.api.RetrofitClient;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiRecommendFragment extends Fragment {

    private EditText editQuestion;
    private LinearLayout btnAskAi;
    private Button buttonBack;
    private TextView textAnalysis;
    private RecyclerView recyclerAi;
    private AiRecommendationAdapter aiAdapter;

    private ProductApi productApi;
    private TextView textStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_recommend, container, false);

        editQuestion = view.findViewById(R.id.editQuestion);
        btnAskAi = view.findViewById(R.id.btnAskAi);
        buttonBack = view.findViewById(R.id.buttonBack);
        textAnalysis = view.findViewById(R.id.textAnalysis);
        recyclerAi = view.findViewById(R.id.recyclerAiRecommendations);

        productApi = RetrofitClient.getClient().create(ProductApi.class);

        aiAdapter = new AiRecommendationAdapter();
        recyclerAi.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAi.setAdapter(aiAdapter);

        btnAskAi.setOnClickListener(v -> callAi());

        // ✅ 뒤로가기 버튼 클릭 리스너 - 초기화 후 뒤로가기
        buttonBack.setOnClickListener(v -> {
            // 추천 내역 초기화
            aiAdapter.clearAll();

            // 입력 필드 비우기
            editQuestion.setText("");

            // 분석 결과 숨기기
            textAnalysis.setVisibility(View.GONE);
            textAnalysis.setText("");

            // ✅ textStatus가 null이 아닌지 확인하고 실행
            if (textStatus != null) {
                textStatus.setText("💡 AI에게 원하는 상품을 물어보세요");
            }
            // 뒤로가기
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });


        return view;
    }

    private void callAi() {
        String question = editQuestion.getText().toString().trim();
        if (TextUtils.isEmpty(question)) {
            Toast.makeText(getContext(), "질문을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        AiChatRequest req = new AiChatRequest("user_001", 5, question);

        productApi.getAiRecommendations(req).enqueue(new Callback<AiChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<AiChatResponse> call,
                                   @NonNull Response<AiChatResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(),
                            "요청 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                AiChatResponse body = response.body();
                if (!body.isSuccess()) {
                    Toast.makeText(getContext(),
                            "서버 응답 실패: " + body.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 분석 결과 표시
                updateAnalysis(body.getExtracted_info());

                // 추천 리스트 표시
                aiAdapter.setItems(body.getRecommendations());
            }

            @Override
            public void onFailure(@NonNull Call<AiChatResponse> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(),
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAnalysis(AiChatExtractedInfo info) {
        if (info == null) {
            textAnalysis.setText("분석 정보가 없습니다.");
            return;
        }

        StringBuilder sb = new StringBuilder("질문 분석 결과\n");
        if (info.getSkin_type() != null)
            sb.append("- 피부 타입: ").append(info.getSkin_type()).append("\n");
        if (info.getCategory() != null)
            sb.append("- 카테고리: ").append(info.getCategory()).append("\n");

        AiChatPriceRange pr = info.getPrice_range();
        if (pr != null) {
            // 이 부분은 서버 설계가 확실하지 않아서 **추측입니다**
            sb.append("- 가격 범위 (추측입니다): ")
                    .append(pr.getAdditionalProp1()).append(" ~ ")
                    .append(pr.getAdditionalProp2()).append("\n");
        }

        if (info.getKeywords() != null && !info.getKeywords().isEmpty())
            sb.append("- 키워드: ").append(TextUtils.join(", ", info.getKeywords()));

        textAnalysis.setText(sb.toString());
    }
}