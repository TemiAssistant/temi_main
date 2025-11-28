package com.example.oliveyoung;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout logoLayout;
    private LinearLayout buttonContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        logoLayout = findViewById(R.id.logoLayout);
        buttonContainer = findViewById(R.id.buttonContainer);

        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);

        // 홈 화면 버튼들 (CardView)
        CardView cardFollow = findViewById(R.id.cardFollow);
        CardView cardSearch = findViewById(R.id.cardSearch);
        CardView cardCheckout = findViewById(R.id.cardCheckout);

        // 테미 제어 버튼
        cardFollow.setOnClickListener(v -> {
            logoLayout.setVisibility(View.GONE);
            buttonContainer.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setCurrentItem(0);
        });

        // 상품 검색 버튼
        cardSearch.setOnClickListener(v -> {
            logoLayout.setVisibility(View.GONE);
            buttonContainer.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setCurrentItem(1);
        });

        // 결제 버튼
        cardCheckout.setOnClickListener(v -> {
            logoLayout.setVisibility(View.GONE);
            buttonContainer.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setCurrentItem(2);
        });
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getVisibility() == View.VISIBLE) {
            // Fragment에서 홈으로 돌아가기
            viewPager.setVisibility(View.GONE);
            logoLayout.setVisibility(View.VISIBLE);
            buttonContainer.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}