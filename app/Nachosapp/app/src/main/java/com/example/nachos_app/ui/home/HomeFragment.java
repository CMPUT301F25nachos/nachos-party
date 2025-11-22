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
import androidx.activity.result.ActivityResultLauncher;

import com.example.nachos_app.CreateEventActivity;
import com.example.nachos_app.Event;
import com.example.nachos_app.EventAdapter;
import com.example.nachos_app.EventDetailsActivity;
import com.example.nachos_app.databinding.FragmentHomeBinding;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

/**
 * Main home screen fragment displaying available events.
 * Shows events with open registration and provides options to:
 * - Create new events
 * - Filter events (upcoming feature)
 * - Scan QR codes to view event details
 * Automatically refreshes event list when fragment is resumed.
 */
public class HomeFragment extends Fragment {

    private static final String QR_PREFIX = "event://";
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

        binding.scanQrButton.setOnClickListener(v -> launchQrScanner());

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

    /**
     * Observes ViewModel LiveData to update UI based on data changes.
     * Handles loading states, event data updates, and error messages.
     */
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

    /**
     * Updates the RecyclerView with events data.
     * Shows empty state message if no events are available.
     * Ensures events and IDs lists are synchronized before updating.
     */
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

    private final ActivityResultLauncher<ScanOptions> qrScanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    handleScanResult(result.getContents());
                }
            });

    /**
     * Launches the QR code scanner with appropriate configuration.
     * Configures scanner to accept only QR codes and locks orientation.
     */
    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan event QR code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        qrScanLauncher.launch(options);
    }

    /**
     * Processes QR code scan results.
     * Validates QR code format (must start with "event://") and extracts event ID.
     * Opens EventDetailsActivity if valid, shows error toast otherwise.
     * @param contents The scanned QR code content
     */
    private void handleScanResult(String contents) {
        if (!contents.startsWith(QR_PREFIX)) {
            Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventID = contents.substring(QR_PREFIX.length()).trim();
        if (eventID.isEmpty()) {
            Toast.makeText(getContext(), "Malformed event QR", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", eventID);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}