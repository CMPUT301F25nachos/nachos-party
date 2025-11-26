package com.example.nachos_app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.Event;
import com.example.nachos_app.EventAdapter;
import com.example.nachos_app.databinding.FragmentDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Fragment displaying the user's personalized dashboard of events.
 * Shows events where the user is either:
 * - The organizer
 * - On the waitlist
 * - Selected for participation
 * - Enrolled
 * - Cancelled
 * Provides filtering capabilities and navigation to event details.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private Button filterButton;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews(root);
        setupRecyclerView();
        setupFilterButton();
        observeViewModel();

        // Load events through ViewModel
        dashboardViewModel.loadMyEvents(currentUserId);

        return root;
    }

    /**
     * Observes LiveData from the ViewModel to update UI.
     * Watches for loading state, events list, event IDs, and errors.
     */
    private void observeViewModel() {
        // Observe loading state
        dashboardViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
        });

        // Observe events
        dashboardViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            updateEventsList();
        });

        dashboardViewModel.getEventIds().observe(getViewLifecycleOwner(), eventIds -> {
            updateEventsList();
        });

        // Observe errors
        dashboardViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (binding == null || error == null) return;
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates the RecyclerView with events data.
     * Shows empty state message if no events are available.
     * Ensures events and IDs lists are synchronized before updating.
     */
    private void updateEventsList() {
        if (binding == null) return;

        List<Event> events = dashboardViewModel.getEvents().getValue();
        List<String> eventIds = dashboardViewModel.getEventIds().getValue();

        if (events != null && eventIds != null && events.size() == eventIds.size()) {
            if (events.isEmpty()) {
                binding.emptyStateTextView.setVisibility(View.VISIBLE);
                binding.emptyStateTextView.setText("No events here.\nCreate/Join one to get started!");
                binding.myEventsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateTextView.setVisibility(View.GONE);
                binding.myEventsRecyclerView.setVisibility(View.VISIBLE);
                eventAdapter.setEvents(events, eventIds);
            }
        }
    }

    /**
     * Initializes view references from the binding.
     * @param root The root view of the fragment
     */
    private void initViews(View root) {
        recyclerView = binding.myEventsRecyclerView;
        filterButton = binding.filterButton;
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     * Uses LinearLayoutManager for vertical scrolling list.
     */
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(eventAdapter);
    }

    /**
     * Sets up the filter button click listener.
     * Currently shows a placeholder toast message.
     */
    private void setupFilterButton() {
        filterButton.setOnClickListener(v -> {
            // TODO: Implement filter functionality
            Toast.makeText(requireContext(), "Filter coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}