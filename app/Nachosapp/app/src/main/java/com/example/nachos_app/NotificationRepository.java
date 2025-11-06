package com.example.nachos_app;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class NotificationRepository {
    private DatabaseReference userRef;

    public NotificationRepository(DatabaseReference ref) {
        this.userRef = ref;
    }

    public void fetchNotifications(ValueEventListener listener) {
        userRef.addListenerForSingleValueEvent(listener);
    }
}
