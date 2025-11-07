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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nachos_app.EditProfileActivity;
import com.example.nachos_app.R;
import com.example.nachos_app.databinding.FragmentProfileBinding;
import com.example.nachos_app.ui.admin.AdminAllowList;
import com.example.nachos_app.ui.admin.AdminMenuActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This fragment displays the user's profile information, including their name, email, phone number, and
 * notification preferences. It uses a ProfileViewModel to observe and display the user's data from
 * Firestore in real-time and allows the user to update their notification setting.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseFirestore db;
    @Nullable private String lastQueriedUid = null;
    @Nullable private Boolean lastAllowed = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // allow access to the db
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
        // set up the button
        binding.btnAdminSignIn.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AdminMenuActivity.class)));

        // Initial evaluation with current user.
        evaluateAdmin(auth.getCurrentUser());

        // Listen for auth changes (will be needed later when we can sign out).
        authListener = fa -> evaluateAdmin(fa.getCurrentUser());




        return root;
    }

    // these methods preserve the state of the admin button
    @Override public void onStart() {
        super.onStart(); if (authListener != null) auth.addAuthStateListener(authListener);
    }

    @Override public void onStop()  {
        super.onStop();  if (authListener != null) auth.removeAuthStateListener(authListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        evaluateAdmin(auth.getCurrentUser());
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Reads users/{uid}.email and toggles the Admin button accordingly
     * - If user is null: set GONE and clear caches.
     * - If same UID and we have a cached decision: re-apply it and return (no new read).
     * - Else: read Firestore, compute allowed, cache it, and set visibility.
     */
    private void evaluateAdmin(@Nullable FirebaseUser user) {
        if (binding == null) return;

        if (user == null) {
            lastQueriedUid = null;
            lastAllowed = null;
            binding.btnAdminSignIn.setVisibility(View.GONE);
            return;
        }

        final String uid = user.getUid(); // get the current user ID

        // checking uid to avoid unnecessary reads
        if (uid.equals(lastQueriedUid) && lastAllowed != null) {
            binding.btnAdminSignIn.setVisibility(lastAllowed ? View.VISIBLE : View.GONE);
            if (lastAllowed) {
                try {
                    binding.btnAdminSignIn.setText(getString(R.string.admin_open_menu));
                } catch (Exception ignore) {
                    binding.btnAdminSignIn.setText("Open Admin Menu");
                }
            }
            return;
        }

        lastQueriedUid = uid; // mark which UID we're about to read

        // 1. get access to the users collection
        Task<DocumentSnapshot> t = db.collection("users").document(uid).get();
        t.addOnSuccessListener(snap -> {
            if (binding == null) return;

            // 2. get the email and check if its allowed to access admin mode
            String profileEmail = (snap != null) ? snap.getString("email") : null;
            boolean allowed = AdminAllowList.isAllowed(profileEmail);
            lastAllowed = allowed;

            // 3. set visibility accordingly
            binding.btnAdminSignIn.setVisibility(allowed ? View.VISIBLE : View.GONE);
            if (allowed) {
                try {
                    binding.btnAdminSignIn.setText(getString(R.string.admin_open_menu));
                } catch (Exception ignore) {
                    binding.btnAdminSignIn.setText("Open Admin Menu");
                }
            }
        }).addOnFailureListener(e -> {
            // just in case we cant read
            if (binding == null) return;
            lastAllowed = null;
            binding.btnAdminSignIn.setVisibility(View.GONE);
        });
    }
}

