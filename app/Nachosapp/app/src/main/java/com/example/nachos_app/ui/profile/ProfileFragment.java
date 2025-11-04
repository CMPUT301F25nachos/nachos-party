package com.example.nachos_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nachos_app.EditProfileActivity;
import com.example.nachos_app.R;
import com.example.nachos_app.databinding.FragmentProfileBinding;

/**
 * This fragment displays the user's profile information, including their name, email, phone number, and
 * notification preferences. It uses a ProfileViewModel to observe and display the user's data from
 * Firestore in real-time and allows the user to update their notification setting.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Observe user details
        profileViewModel.getName().observe(getViewLifecycleOwner(), binding.nameTextView::setText);
        profileViewModel.getEmail().observe(getViewLifecycleOwner(), binding.emailTextView::setText);
        profileViewModel.getPhone().observe(getViewLifecycleOwner(), binding.phoneTextView::setText);

        // Setup Edit Profile button
        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Setup Notification Spinner
        final Spinner notificationSpinner = binding.notificationSpinner;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.notification_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notificationSpinner.setAdapter(adapter);

        // Observe notification preference from ViewModel and update spinner
        profileViewModel.getNotificationPreference().observe(getViewLifecycleOwner(), preference -> { // Renamed
            if (preference != null) {
                int position = adapter.getPosition(preference);
                notificationSpinner.setSelection(position);
            }
        });

        // Listen for user selection and update preference in ViewModel
        notificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPreference = parent.getItemAtPosition(position).toString();
                // Prevent initial update on fragment load
                if (profileViewModel.getNotificationPreference().getValue() != null && 
                    !profileViewModel.getNotificationPreference().getValue().equals(selectedPreference)) { // Renamed
                    profileViewModel.updateNotificationPreference(selectedPreference);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
