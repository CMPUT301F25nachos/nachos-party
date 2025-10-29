package com.example.nachos_app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nachos_app.CreateEventActivity;
import com.example.nachos_app.Event;
import com.example.nachos_app.EventAdapter;
import com.example.nachos_app.databinding.FragmentHomeBinding;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EventAdapter eventAdapter;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup RecyclerView
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(getContext());
        binding.eventsRecyclerView.setAdapter(eventAdapter);

        // Setup button listeners
        binding.createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateEventActivity.class);
            startActivity(intent);
        });

        binding.filterButton.setOnClickListener(v -> {
            // TODO: Implement filter functionality
            Toast.makeText(getContext(), "Filter coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.scanQrButton.setOnClickListener(v -> {
            // TODO: Implement QR scanning
            Toast.makeText(getContext(), "QR Scanner coming soon", Toast.LENGTH_SHORT).show();
        });

        // Observe ViewModel
        observeViewModel();

        // Load events
        homeViewModel.loadEvents();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning to this fragment
        homeViewModel.loadEvents();
    }

    private void observeViewModel() {
        // Observe loading state
        homeViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;

            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.emptyStateTextView.setVisibility(View.GONE);
                binding.eventsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        // Observe events data
        homeViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            updateEventsList();
        });

        homeViewModel.getEventIds().observe(getViewLifecycleOwner(), eventIds -> {
            updateEventsList();
        });

        // Observe errors
        homeViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (binding == null) return;

            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                binding.emptyStateTextView.setVisibility(View.VISIBLE);
                binding.emptyStateTextView.setText("Error loading events");
            }
        });
    }

    private void updateEventsList() {
        if (binding == null) return;

        List<Event> events = homeViewModel.getEvents().getValue();
        List<String> eventIds = homeViewModel.getEventIds().getValue();

        if (events != null && eventIds != null && events.size() == eventIds.size()) {
            if (events.isEmpty()) {
                binding.emptyStateTextView.setVisibility(View.VISIBLE);
                binding.emptyStateTextView.setText("No events available.\nCreate one to get started!");
                binding.eventsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateTextView.setVisibility(View.GONE);
                binding.eventsRecyclerView.setVisibility(View.VISIBLE);
                eventAdapter.setEvents(events, eventIds);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}