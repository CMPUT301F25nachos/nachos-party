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
 * A UI controller that displays the user's profile information. This fragment is responsible for:
 * <ul>
 *     <li>Displaying the user's name, email, phone number, and profile image.</li>
 *     <li>Observing data from {@link ProfileViewModel} to ensure the UI is always up-to-date with the latest
 *     information from Firestore.</li>
 *     <li>Handling user interactions, such as launching the {@link EditProfileActivity} when the settings
 *     button is clicked.</li>
 *     <li>Managing the notification preference spinner, allowing the user to view and update their choice,
 *     which is then persisted back to Firestore via the ViewModel.</li>
 * </ul>
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    /**
     * Called to have the fragment instantiate its user interface view. This method sets up the layout,
     * initializes the {@link ProfileViewModel}, and establishes observers for the live data.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI.
     */
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
        profileViewModel.getNotificationPreference().observe(getViewLifecycleOwner(), preference -> {
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
                    !profileViewModel.getNotificationPreference().getValue().equals(selectedPreference)) {
                    profileViewModel.updateNotificationPreference(selectedPreference);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * This is used to clean up resources associated with the view, in this case, by setting the binding to null.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
