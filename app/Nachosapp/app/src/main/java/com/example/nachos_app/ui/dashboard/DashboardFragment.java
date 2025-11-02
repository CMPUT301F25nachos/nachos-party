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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private Button filterButton;

    private FirebaseFirestore db;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews(root);
        setupRecyclerView();
        setupFilterButton();
        loadMyEvents();

        return root;
    }

    private void initViews(View root) {
        recyclerView = binding.myEventsRecyclerView;
        filterButton = binding.filterButton;
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(eventAdapter);
    }

    private void setupFilterButton() {
        filterButton.setOnClickListener(v -> {
            // TODO: Implement filter functionality
            Toast.makeText(requireContext(), "Filter coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMyEvents() {
        if (currentUserId == null) {
            return;
        }

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> allEventIds = new ArrayList<>();
                    List<Event> allEvents = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        allEventIds.add(doc.getId());
                        allEvents.add(doc.toObject(Event.class));
                    }

                    filterUserEvents(allEventIds, allEvents);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void filterUserEvents(List<String> allEventIds, List<Event> allEvents) {
        final List<Event> userEvents = new ArrayList<>();
        final List<String> userEventIds = new ArrayList<>();
        final int totalEvents = allEventIds.size();
        final int[] processedCount = {0};

        if (totalEvents == 0) {
            eventAdapter.setEvents(userEvents, userEventIds);
            return;
        }

        for (int i = 0; i < allEventIds.size(); i++) {
            final String eventId = allEventIds.get(i);
            final Event event = allEvents.get(i);

            // Check if user is organizer
            if (event.getOrganizerId().equals(currentUserId)) {
                userEvents.add(event);
                userEventIds.add(eventId);

                processedCount[0]++;
                if (processedCount[0] == totalEvents) {
                    eventAdapter.setEvents(userEvents, userEventIds);
                }
                continue;
            }

            // Check if user is participant (in any subcollection)
            checkIfUserIsParticipant(eventId, event, userEvents, userEventIds, () -> {
                processedCount[0]++;
                if (processedCount[0] == totalEvents) {
                    eventAdapter.setEvents(userEvents, userEventIds);
                }
            });
        }
    }

    private void checkIfUserIsParticipant(String eventId, Event event,
                                          List<Event> userEvents,
                                          List<String> userEventIds,
                                          Runnable onComplete) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        // Check waitlist first (most common case)
        eventRef.collection("waitlist").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userEvents.add(event);
                        userEventIds.add(eventId);
                        onComplete.run();
                        return;
                    }

                    // Not in waitlist, check selected
                    eventRef.collection("selected").document(currentUserId)
                            .get()
                            .addOnSuccessListener(selectedSnapshot -> {
                                if (selectedSnapshot.exists()) {
                                    userEvents.add(event);
                                    userEventIds.add(eventId);
                                    onComplete.run();
                                    return;
                                }

                                // Not in selected, check enrolled
                                eventRef.collection("enrolled").document(currentUserId)
                                        .get()
                                        .addOnSuccessListener(enrolledSnapshot -> {
                                            if (enrolledSnapshot.exists()) {
                                                userEvents.add(event);
                                                userEventIds.add(eventId);
                                                onComplete.run();
                                                return;
                                            }

                                            // Not in enrolled, check cancelled
                                            eventRef.collection("cancelled").document(currentUserId)
                                                    .get()
                                                    .addOnSuccessListener(cancelledSnapshot -> {
                                                        if (cancelledSnapshot.exists()) {
                                                            userEvents.add(event);
                                                            userEventIds.add(eventId);
                                                        }
                                                        onComplete.run();
                                                    })
                                                    .addOnFailureListener(e -> onComplete.run());
                                        })
                                        .addOnFailureListener(e -> onComplete.run());
                            })
                            .addOnFailureListener(e -> onComplete.run());
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}