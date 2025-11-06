package com.example.prm392_android_app_frontend.presentation.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.prm392_android_app_frontend.presentation.fragment.NewAddressFragment;
import com.example.prm392_android_app_frontend.presentation.fragment.SavedAddressesFragment;

public class AddressPagerAdapter extends FragmentStateAdapter {

    public AddressPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new NewAddressFragment();
            case 1:
                return new SavedAddressesFragment();
            default:
                return new NewAddressFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 tabs
    }
}