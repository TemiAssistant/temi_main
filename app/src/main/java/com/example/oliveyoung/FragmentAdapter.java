package com.example.oliveyoung;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FollowFragment();
            case 1:
                return new SearchFragment();
            case 2:
                return new CheckoutFragment();
            default:
                return new FollowFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}