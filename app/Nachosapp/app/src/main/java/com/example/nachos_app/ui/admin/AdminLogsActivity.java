package com.example.nachos_app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.Notification;
import com.example.nachos_app.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AdminLogsActivity
 * <p>
 *     Admin-only screen that shows a log of notifications
 *     sent by organizers to entrants.
 * </p>
 */
public class AdminLogsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private AdminLogsAdapter adapter;
    private TextView tvEmpty;

    /**
     * Small helper class to hold event name and organizer name
     */
    private static class EventMeta {
        final String eventName;
        final String organizerName;

        EventMeta(String eventName, String organizerName) {
            this.eventName = eventName;
            this.organizerName = organizerName;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_logs);

        // hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        // wire back button
        View back = findViewById(R.id.btn_back);
        if (back != null){
            back.setOnClickListener(v -> finish());
        }


        tvEmpty = findViewById(R.id.tv_logs_empty);

        RecyclerView rv = findViewById(R.id.rv_admin_logs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminLogsAdapter(this);
        rv.setAdapter(adapter);

        loadAllNotifications();
    }

    /**
     * Loads all organizer to entrant notifications.
     *
     * Step 1: Load all events so we can resolve eventId
     * Step 2: For each user, load their notifications subcollection and keep only
     *         notifications that have a non-null eventId. (because we're only interested in
     *         notifications sent to entrants from organizers)
     */
    private void loadAllNotifications() {
        // load events
        db.collection("events")
                .get()
                .addOnSuccessListener(eventsSnap -> {
                    Map<String, EventMeta> eventMetaMap = new HashMap<>();
                    for (DocumentSnapshot doc : eventsSnap.getDocuments()) {
                        String eventId = doc.getId();
                        String eventName = doc.getString("eventName");
                        String organizerName = doc.getString("organizerName");
                        eventMetaMap.put(eventId, new EventMeta(eventName, organizerName));
                    }
                    // load notifications for all users
                    loadOrganizerToEntrantNotifications(eventMetaMap);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            getString(R.string.admin_logs_load_fail) + ": " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // still try to load notifications with an empty event map
                    loadOrganizerToEntrantNotifications(new HashMap<>());
                });
    }

    /**
     * Loads notifications from all users, filters for event-based notifications
     * and maps them into rows for the adapter.
     *
     * @param eventMetaMap mapping from eventId to event metadata
     */
    private void loadOrganizerToEntrantNotifications(Map<String, EventMeta> eventMetaMap) {
        db.collection("users")
                .get()
                .addOnSuccessListener(usersSnap -> {

                    // List that will hold all rows from all users
                    List<AdminLogsAdapter.Row> rows = new ArrayList<>();

                    // If there are no users, we are done
                    if (usersSnap.isEmpty()) {
                        applyRows(rows);
                        return;
                    }

                    // we need to know when all user notification queries finish
                    // so remaining here = number of user docs whose notifications are still loading
                    AtomicInteger remaining = new AtomicInteger(usersSnap.size());

                    for (DocumentSnapshot userDoc : usersSnap.getDocuments()) {
                        String uid = userDoc.getId();

                        // get user name
                        final String recipientName = userDoc.getString("name");

                        db.collection("users")
                                .document(uid)
                                .collection("notifications")
                                .get()
                                .addOnSuccessListener(notifSnap -> {
                                    for (DocumentSnapshot notifDoc : notifSnap) {
                                        Notification n = notifDoc.toObject(Notification.class);
                                        if (n == null) continue;

                                        String eventId = n.getEventId();

                                        // Only keep notifications tied to an event:
                                        // these are the organizer to entrant ones
                                        if (eventId == null || eventId.isEmpty()) {
                                            continue;
                                        }

                                        EventMeta meta = eventMetaMap.get(eventId);
                                        if (meta == null) {
                                            // Event was deleted or not found
                                            continue;
                                        }

                                        // set up the data to be displayed
                                        AdminLogsAdapter.Row r = new AdminLogsAdapter.Row();
                                        r.eventName = meta.eventName;
                                        r.senderName = meta.organizerName;
                                        r.recipientName = recipientName;
                                        r.message = n.getMessage();
                                        r.sendTime = n.getSendTime();
                                        rows.add(r);
                                    }
                                    // decrement the remaining counter
                                    // once it gets to 0 we can sort and display the notifications
                                    if (remaining.decrementAndGet() == 0) {
                                        applyRows(rows);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // this part makes sure that if one persons notifs fail to load
                                    // we can still display the ones that didn't
                                    if (remaining.decrementAndGet() == 0) {
                                        applyRows(rows);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            getString(R.string.admin_logs_load_fail) + ": " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    applyRows(new ArrayList<>());
                });
    }

    /**
     * Sorts the given rows and passes them to the adapter.
     *
     * @param rows list of rows to display (may be empty)
     */
    private void applyRows(List<AdminLogsAdapter.Row> rows) {
        Collections.sort(rows, (o1, o2) -> {
            if (o1.sendTime == null && o2.sendTime == null) return 0;
            if (o1.sendTime == null) return 1;
            if (o2.sendTime == null) return -1;
            return o2.sendTime.compareTo(o1.sendTime);
        });

        adapter.set(rows);

        if (rows.isEmpty()) {
            tvEmpty.setVisibility(TextView.VISIBLE);
        } else {
            tvEmpty.setVisibility(TextView.GONE);
        }
    }
}
