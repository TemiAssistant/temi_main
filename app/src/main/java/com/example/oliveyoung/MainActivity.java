package com.example.oliveyoung;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout buttonFollow;
    private LinearLayout buttonSearch;
    private LinearLayout buttonCheckout;

    // 버튼들이 들어 있는 전체 컨테이너
    private LinearLayout buttonContainer;

    private ImageView imageTemiAssistantLogo;
    private ImageView imageOliveYoungLogo;

    // ✅ 새로 추가: AI 상품 추천 버튼 (TextView든 LinearLayout이든 View 로 받아도 됨)
    private View buttonAiRecommend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ViewPager2
        viewPager = findViewById(R.id.viewPager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setUserInputEnabled(false);   // 스와이프로는 이동 안 함 (버튼으로만)

        // 로고들
        imageTemiAssistantLogo = findViewById(R.id.imageTemiAssistantLogo);
        imageOliveYoungLogo = findViewById(R.id.imageOliveYoungLogo);

        // 하단 버튼 컨테이너 + 각 버튼
        buttonContainer = findViewById(R.id.buttonContainer);
        buttonFollow = findViewById(R.id.buttonFollow);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonCheckout = findViewById(R.id.buttonCheckout);

        // ✅ AI 상품 추천 버튼 찾기 (activity_main.xml에 android:id="@+id/buttonAiRecommend" 로 정의되어 있어야 함)
        buttonAiRecommend = findViewById(R.id.buttonAiRecommend);

        // 처음에는 홈(로고 + 버튼만)
        showHome();

        // 버튼 클릭 리스너 설정
        buttonFollow.setOnClickListener(v -> openPage(0));
        buttonSearch.setOnClickListener(v -> openPage(1));
        buttonCheckout.setOnClickListener(v -> openPage(2));

        // ✅ AI 상품 추천 버튼 클릭 시 3번 인덱스 페이지로 이동
        buttonAiRecommend.setOnClickListener(v -> openPage(3));
    }

    /**
     * 홈 상태: 로고 + 버튼 보이고, ViewPager는 숨김
     */
    private void showHome() {
        viewPager.setVisibility(View.GONE);

        imageTemiAssistantLogo.setVisibility(View.VISIBLE);
        imageOliveYoungLogo.setVisibility(View.VISIBLE);

        // 👉 버튼 다시 보이게
        buttonContainer.setVisibility(View.VISIBLE);

        // ✅ AI 버튼도 홈에서 보여야 하므로 VISIBLE
        if (buttonAiRecommend != null) {
            buttonAiRecommend.setVisibility(View.VISIBLE);
        }
    }

    /**
     * index에 해당하는 페이지로 이동하면서
     * 로고/버튼 숨기고 ViewPager만 보여주기
     */
    private void openPage(int index) {
        imageTemiAssistantLogo.setVisibility(View.GONE);
        imageOliveYoungLogo.setVisibility(View.GONE);

        // 👉 버튼들 통째로 숨기기
        buttonContainer.setVisibility(View.GONE);

        // ✅ 프래그먼트 화면에서는 AI 버튼도 숨김
        if (buttonAiRecommend != null) {
            buttonAiRecommend.setVisibility(View.GONE);
        }

        viewPager.setVisibility(View.VISIBLE);
        viewPager.setCurrentItem(index, false);
    }

    @Override
    public void onBackPressed() {
        // 프래그먼트 화면(뷰페이저 보이는 상태)이면 → 홈으로 복귀
        if (viewPager.getVisibility() == View.VISIBLE) {
            showHome();
        } else {
            // 이미 홈이면 → 기존 동작(앱 종료)
            super.onBackPressed();
        }
    }
}
