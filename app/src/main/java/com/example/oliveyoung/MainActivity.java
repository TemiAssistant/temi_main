package com.example.oliveyoung;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TabLayout과 ViewPager2 연결
        TabLayout tabLayout = findViewById(R.id.tabLayout); // TabLayout을 찾아 연결
        ViewPager2 viewPager = findViewById(R.id.viewPager); // ViewPager2를 찾아 연결

        // FragmentAdapter 설정 (각 탭에 해당하는 Fragment 연결)
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter); // ViewPager에 어댑터 설정

        // TabLayout과 ViewPager2를 연결
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // 각 탭에 표시할 텍스트 설정
            switch (position) {
                case 0:
                    tab.setText("Follow");
                    break;
                case 1:
                    tab.setText("Search");
                    break;
                case 2:
                    tab.setText("Checkout");
                    break;
            }
        }).attach(); // TabLayout과 ViewPager2를 동기화
    }
}
