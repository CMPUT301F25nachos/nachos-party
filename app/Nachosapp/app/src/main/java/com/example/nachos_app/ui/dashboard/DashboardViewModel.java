package com.example.nachos_app.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nachos_app.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Dashboard Fragment.
 * Manages loading and filtering of events relevant to the current user.
 * Filters events based on user participation status (organizer, waitlist, selected, enrolled, cancelled).
 */
public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<List<Event>> mEvents;
    private final MutableLiveData<List<String>> mEventIds;
    private final MutableLiveData<Boolean> mLoading;
    private final MutableLiveData<String> mError;
    private final FirebaseFirestore db;

    public DashboardViewModel() {
        mEvents = new MutableLiveData<>();
        mEventIds = new MutableLiveData<>();
        mLoading = new MutableLiveData<>();
        mError = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Event>> getEvents() {
        return mEvents;
    }

    public LiveData<List<String>> getEventIds() {
        return mEventIds;
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<String> getError() {
        return mError;
    }

    /**
     * Loads all events and filters them to show only those relevant to the current user.
     * Retrieves all events from Firestore, then checks user's participation status.
     * @param currentUserId The ID of the currently logged-in user
     */
    public void loadMyEvents(String currentUserId) {
        if (currentUserId == null) {
            mError.setValue("User not authenticated");
            return;
        }

        mLoading.setValue(true);
        mError.setValue(null);

        db.collection("events")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> allEventIds = new ArrayList<>();
                    List<Event> allEvents = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        allEventIds.add(doc.getId());
                        allEvents.add(doc.toObject(Event.class));
                    }

                    filterUserEvents(allEventIds, allEvents, currentUserId);
                })
                .addOnFailureListener(e -> {
                    mError.setValue("Failed to load events: " + e.getMessage());
                    mLoading.setValue(false);
                });
    }

    /**
     * Filters events to include only those where the user is involved.
     * Checks if user is organizer or participant in any capacity.
     * @param allEventIds List of all event IDs from Firestore
     * @param allEvents List of all event objects
     * @param currentUserId The current user's ID
     */
    private void filterUserEvents(List<String> allEventIds, List<Event> allEvents, String currentUserId) {
        final List<Event> userEvents = new ArrayList<>();
        final List<String> userEventIds = new ArrayList<>();
        final int totalEvents = allEventIds.size();
        final int[] processedCount = {0};

        if (totalEvents == 0) {
            mEvents.setValue(userEvents);
            mEventIds.setValue(userEventIds);
            mLoading.setValue(false);
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
                    mEvents.setValue(userEvents);
                    mEventIds.setValue(userEventIds);
                    mLoading.setValue(false);
                }
                continue;
            }

            // Check if user is participant
            checkIfUserIsParticipant(eventId, event, userEvents, userEventIds, () -> {
                processedCount[0]++;
                if (processedCount[0] == totalEvents) {
                    mEvents.setValue(userEvents);
                    mEventIds.setValue(userEventIds);
                    mLoading.setValue(false);
                }
            });
        }
    }

    /**
     * Checks if the user is a participant in an event.
     * Sequentially checks waitlist, selected, enrolled, and cancelled collections.
     * @param eventId The event ID to check
     * @param event The event object
     * @param userEvents List to add event to if user is participant
     * @param userEventIds List to add event ID to if user is participant
     * @param onComplete Callback to run when check is complete
     */
    private void checkIfUserIsParticipant(String eventId, Event event,
                                          List<Event> userEvents,
                                          List<String> userEventIds,
                                          Runnable onComplete) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.collection("waitlist").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userEvents.add(event);
                        userEventIds.add(eventId);
                        onComplete.run();
                        return;
                    }

                    eventRef.collection("selected").document(currentUserId)
                            .get()
                            .addOnSuccessListener(selectedSnapshot -> {
                                if (selectedSnapshot.exists()) {
                                    userEvents.add(event);
                                    userEventIds.add(eventId);
                                    onComplete.run();
                                    return;
                                }

                                eventRef.collection("enrolled").document(currentUserId)
                                        .get()
                                        .addOnSuccessListener(enrolledSnapshot -> {
                                            if (enrolledSnapshot.exists()) {
                                                userEvents.add(event);
                                                userEventIds.add(eventId);
                                                onComplete.run();
                                                return;
                                            }

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
}