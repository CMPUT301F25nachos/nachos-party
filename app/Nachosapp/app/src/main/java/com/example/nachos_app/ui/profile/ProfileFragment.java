package com.example.nachos_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nachos_app.EditProfileActivity;
import com.example.nachos_app.databinding.FragmentProfileBinding;

/**
 * This fragment displays the user's profile information, including their name, email, and phone number.
 * It uses a ProfileViewModel to observe and display the user's data from Firestore in real-time. It also
 * provides a settings button that launches the EditProfileActivity, allowing the user to modify their details.
 */
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

        // Add the click listener to the settings button
        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
