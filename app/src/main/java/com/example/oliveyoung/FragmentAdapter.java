package com.example.oliveyoung;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 탭에 맞는 Fragment를 반환
        switch (position) {
            case 0:
                return new FollowFragment();  // FollowFragment는 사용자가 정의한 Fragment
            case 1:
                return new SearchFragment(); // SearchFragment는 사용자가 정의한 Fragment
            case 2:
                return new CheckoutFragment(); // CheckoutFragment는 사용자가 정의한 Fragment
            default:
                return new FollowFragment();  // 기본값
        }
    }

    @Override
    public int getItemCount() {
        return 3; // 3개의 탭
    }
}
