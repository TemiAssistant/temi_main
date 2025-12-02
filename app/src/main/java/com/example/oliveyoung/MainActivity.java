package com.example.oliveyoung;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ImageView imageTemiAssistantLogo;
    private ImageView imageOliveYoungLogo;
    private LinearLayout buttonContainer;
    private ViewPager2 viewPager;

    // 뒤로 두 번 눌러 종료
    private long backPressedTime = 0L;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 상단 로고들
        imageTemiAssistantLogo = findViewById(R.id.imageTemiAssistantLogo);
        imageOliveYoungLogo = findViewById(R.id.imageOliveYoungLogo);

        // 하단 버튼 박스
        buttonContainer = findViewById(R.id.buttonContainer);

        // 버튼 3개 (LinearLayout이지만 클릭 가능)
        LinearLayout btnFollow = findViewById(R.id.btnFollow);
        LinearLayout btnSearch = findViewById(R.id.btnSearch);
        LinearLayout btnCheckout = findViewById(R.id.btnCheckout);

        // 내부 화면용 ViewPager
        viewPager = findViewById(R.id.viewPager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);

        // 처음에는 홈 화면만 보이게
        showHome();

        // 각 버튼 누르면 해당 프래그먼트로 이동
        btnFollow.setOnClickListener(v -> showFragment(0));   // FollowFragment
        btnSearch.setOnClickListener(v -> showFragment(1));   // SearchFragment
        btnCheckout.setOnClickListener(v -> showFragment(2)); // CheckoutFragment
    }

    /** 홈 화면(로고 + 3버튼)만 보이게 */
    private void showHome() {
        imageTemiAssistantLogo.setVisibility(View.VISIBLE);
        imageOliveYoungLogo.setVisibility(View.VISIBLE);
        buttonContainer.setVisibility(View.VISIBLE);

        viewPager.setVisibility(View.GONE);
    }

    /** 특정 프래그먼트 화면 보여주기 */
    private void showFragment(int index) {
        imageTemiAssistantLogo.setVisibility(View.GONE);
        imageOliveYoungLogo.setVisibility(View.GONE);
        buttonContainer.setVisibility(View.GONE);

        viewPager.setVisibility(View.VISIBLE);
        viewPager.setCurrentItem(index, false);
    }

    @Override
    public void onBackPressed() {
        // 프래그먼트 화면이면 → 홈으로
        if (viewPager.getVisibility() == View.VISIBLE) {
            showHome();
            return;
        }

        // 이미 홈이면 → 두 번 눌러 종료
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel();
            super.onBackPressed();
        } else {
            backToast = Toast.makeText(this, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT);
            backToast.show();
            backPressedTime = System.currentTimeMillis();
        }
    }
}
