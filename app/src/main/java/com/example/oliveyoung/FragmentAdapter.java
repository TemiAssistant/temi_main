package com.example.oliveyoung;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.oliveyoung.ui.checkout.CheckoutFragment;
import com.example.oliveyoung.ui.follow.FollowFragment;
import com.example.oliveyoung.ui.search.SearchFragment;

public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
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
