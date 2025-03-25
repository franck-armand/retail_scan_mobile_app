package com.maf.mafscan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class UserGuidePagerAdapter extends FragmentStateAdapter {

    public UserGuidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TechnicalGuideFragment();
            case 1:
                return new UserGuideFragment();
            default:
                return new TechnicalGuideFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}