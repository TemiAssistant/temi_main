package com.example.oliveyoung;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    // 홈 화면(버튼들) 래퍼
    private LinearLayout buttonContainer;
    private LinearLayout btnFollow;
    private LinearLayout btnSearch;
    private LinearLayout btnCheckout;

    // 로고 뷰
    private View temiLogo;
    private View oliveLogo;

    // 뒤로가기 두 번 눌러 종료
    private long backPressedTime = 0;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 뷰 찾기
        viewPager       = findViewById(R.id.viewPager);
        buttonContainer = findViewById(R.id.buttonContainer);
        btnFollow       = findViewById(R.id.btnFollow);
        btnSearch       = findViewById(R.id.btnSearch);
        btnCheckout     = findViewById(R.id.btnCheckout);
        temiLogo        = findViewById(R.id.imageTemiAssistantLogo);
        oliveLogo       = findViewById(R.id.imageOliveYoungLogo);

        // 2. ViewPager + 어댑터 연결
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setUserInputEnabled(false);  // 스와이프 금지 (필요하면 true로 바꿔도 됨)

        // 3. 처음에는 홈 화면만 보이게
        showHome();

        // 4. 버튼 클릭 시 각 프래그먼트로 이동
        btnFollow.setOnClickListener(v -> showFragment(0));   // FollowFragment
        btnSearch.setOnClickListener(v -> showFragment(1));   // SearchFragment
        btnCheckout.setOnClickListener(v -> showFragment(2)); // CheckoutFragment
    }

    /** ✅ 홈 화면(로고 + 3버튼) 보여주기 */
    private void showHome() {
        // ViewPager 숨기고
        viewPager.setVisibility(View.GONE);

        // 홈 영역(버튼 + 로고) 보여주기
        buttonContainer.setVisibility(View.VISIBLE);
        temiLogo.setVisibility(View.VISIBLE);
        oliveLogo.setVisibility(View.VISIBLE);
    }

    /** ✅ index 번째 프래그먼트 전체 화면으로 보여주기 */
    private void showFragment(int index) {
        // 홈 영역 숨기고
        buttonContainer.setVisibility(View.GONE);
        temiLogo.setVisibility(View.GONE);
        oliveLogo.setVisibility(View.GONE);

        // ViewPager 보이고 해당 프래그먼트로 이동
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setCurrentItem(index, false);
    }

    @Override
    public void onBackPressed() {
        // 1) 프래그먼트 화면이면 → 홈으로 돌아가기
        if (viewPager.getVisibility() == View.VISIBLE) {
            showHome();
            return;
        }

        // 2) 이미 홈 화면이면 → 두 번 눌러서 앱 종료
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel();
            super.onBackPressed();
        } else {
            backToast = Toast.makeText(
                    this,
                    "뒤로 버튼을 한 번 더 누르면 종료됩니다.",
                    Toast.LENGTH_SHORT
            );
            backToast.show();
            backPressedTime = System.currentTimeMillis();
        }
    }
}
