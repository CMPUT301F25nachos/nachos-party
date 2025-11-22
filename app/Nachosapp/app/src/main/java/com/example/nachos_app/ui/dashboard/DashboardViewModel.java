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