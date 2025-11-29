package com.example.nachos_app.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
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

import java.util.ArrayList;
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
    private String currentFilter = "All";

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
        if (currentUserId != null) {
            dashboardViewModel.loadMyEvents(currentUserId);
        }


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
            // Optionally, handle loading indicator visibility here
        });

        // Observe events and eventIds to apply filter
        dashboardViewModel.getEvents().observe(getViewLifecycleOwner(), events -> applyFilter(currentFilter));
        dashboardViewModel.getEventIds().observe(getViewLifecycleOwner(), eventIds -> applyFilter(currentFilter));


        // Observe errors
        dashboardViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (binding == null || error == null) return;
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates the RecyclerView with the given events and ids.
     * Shows an empty state message if the list is empty.
     */
    private void updateRecyclerView(List<Event> events, List<String> ids) {
        if (binding == null) return;

        if (events == null || events.isEmpty()) {
            binding.emptyStateTextView.setVisibility(View.VISIBLE);
            binding.emptyStateTextView.setText("No events here.\nCreate/Join one to get started!");
            binding.myEventsRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyStateTextView.setVisibility(View.GONE);
            binding.myEventsRecyclerView.setVisibility(View.VISIBLE);
            eventAdapter.setEvents(events, ids);
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
     * Sets up the filter button click listener to show a popup menu and apply the selected filter.
     */
    private void setupFilterButton() {
        filterButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), filterButton);
            popup.getMenu().add("All");
            popup.getMenu().add("Created");
            popup.getMenu().add("Joined");
            popup.getMenu().add("Ongoing");
            popup.getMenu().add("Past");
            popup.getMenu().add("Future");

            popup.setOnMenuItemClickListener(item -> {
                currentFilter = item.getTitle().toString();
                applyFilter(currentFilter);
                return true;
            });
            popup.show();
        });
    }


    /**
     * Applies the selected filter to the list of user events and updates the RecyclerView.
     * @param filter The filter string to apply.
     */
    private void applyFilter(String filter) {
        List<Event> allUserEvents = dashboardViewModel.getEvents().getValue();
        List<String> allUserEventIds = dashboardViewModel.getEventIds().getValue();

        if (allUserEvents == null || allUserEventIds == null) {
            updateRecyclerView(new ArrayList<>(), new ArrayList<>());
            return;
        }

        List<Event> filteredEvents = new ArrayList<>();
        List<String> filteredIds = new ArrayList<>();

        for (int i = 0; i < allUserEvents.size(); i++) {
            Event event = allUserEvents.get(i);
            String id = allUserEventIds.get(i);
            boolean matches = false;

            switch (filter) {
                case "All":
                    matches = true;
                    break;
                case "Created":
                    matches = event.getOrganizerId().equals(currentUserId);
                    break;
                case "Joined":
                    matches = !event.getOrganizerId().equals(currentUserId);
                    break;
                case "Ongoing": // Assuming Registration Open means Ongoing
                    matches = event.isRegistrationOpen();
                    break;
                case "Past": // Assuming Registration Closed and not upcoming means Past
                    matches = !event.isRegistrationOpen() && !event.isRegistrationUpcoming();
                    break;
                case "Future": // Assuming Registration Upcoming means Future
                    matches = event.isRegistrationUpcoming();
                    break;
            }

            if (matches) {
                filteredEvents.add(event);
                filteredIds.add(id);
            }
        }
        updateRecyclerView(filteredEvents, filteredIds);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning to this fragment to reflect any updates
        if (currentUserId != null) {
            dashboardViewModel.loadMyEvents(currentUserId);
        }
    }
}
