package com.example.nachos_app.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nachos_app.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Home Fragment.
 * Manages loading and filtering of available events.
 * Shows only events with open registration (and not full) or upcoming registration.
 */
public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Event>> mEvents;
    private final MutableLiveData<List<String>> mEventIds;
    private final MutableLiveData<Boolean> mLoading;
    private final MutableLiveData<String> mError;
    private final FirebaseFirestore db;

    public HomeViewModel() {
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
     * Loads all events from Firestore and filters them for display.
     * Shows events with:
     * - Open registration that are not full
     * - Upcoming registration
     * Orders results by creation date (newest first).
     */
    public void loadEvents() {
        mLoading.setValue(true);
        mError.setValue(null);

        db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> openEvents = new ArrayList<>();
                    List<String> openIds = new ArrayList<>();
                    List<Event> upcomingEvents = new ArrayList<>();
                    List<String> upcomingIds = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);

                        // Only show events with open registration and available spots
                        if (event.isRegistrationOpen() && !event.isWaitlistFull()) {
                            openEvents.add(event);
                            openIds.add(document.getId());
                        } else if (event.isRegistrationUpcoming()) {
                            // Show upcoming events (registration hasn't started yet)
                            upcomingEvents.add(event);
                            upcomingIds.add(document.getId());
                        }
                    }

                    openEvents.addAll(upcomingEvents);
                    openIds.addAll(upcomingIds);
                    mEvents.setValue(openEvents);
                    mEventIds.setValue(openIds);
                    mLoading.setValue(false);
                }).addOnFailureListener(e -> {
                    mError.setValue("Failed to load events: " + e.getMessage());
                    mLoading.setValue(false);
                });
    }
}