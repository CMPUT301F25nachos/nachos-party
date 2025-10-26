package com.example.nachos_app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nachos_app.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView nameTextView = binding.nameTextView;
        profileViewModel.getName().observe(getViewLifecycleOwner(), nameTextView::setText);

        final TextView emailTextView = binding.emailTextView;
        profileViewModel.getEmail().observe(getViewLifecycleOwner(), emailTextView::setText);

        final TextView phoneTextView = binding.phoneTextView;
        profileViewModel.getPhone().observe(getViewLifecycleOwner(), phoneTextView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
