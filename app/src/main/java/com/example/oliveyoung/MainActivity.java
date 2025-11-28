package com.example.oliveyoung;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);

        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);

        // 홈 화면 버튼들
        Button buttonTemiControl = findViewById(R.id.buttonTemiControl);
        Button buttonSearch = findViewById(R.id.buttonSearch);
        Button buttonCheckout = findViewById(R.id.buttonCheckout);

        buttonTemiControl.setOnClickListener(v -> viewPager.setCurrentItem(0));
        buttonSearch.setOnClickListener(v -> viewPager.setCurrentItem(1));
        buttonCheckout.setOnClickListener(v -> viewPager.setCurrentItem(2));
    }
}
