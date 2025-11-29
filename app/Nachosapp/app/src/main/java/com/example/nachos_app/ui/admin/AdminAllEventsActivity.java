package com.example.nachos_app.ui.admin;



import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;



/**
 * Admin screen displaying all events from firebase
 * <p>
 * Reads the events collection and binds the fields to a
 * recyclerView
 * Computes the event status from the start and end timestamps
 * </p>
 */
public class AdminAllEventsActivity extends AppCompatActivity {
    private EventAdminAdapter adapter;
    private FirebaseFirestore db;


    /**
     * Called when the activity is created.
     * Sets up RecyclerView and loads event data
     *
     * @param savedInstanceState state bundle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_events);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        View back = findViewById(R.id.btn_back);

        if (back != null){
            back.setOnClickListener(v -> finish());
        }

        // set up views
        RecyclerView rv = findViewById(R.id.rv_events);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // wire adapter
        adapter = new EventAdminAdapter((row, position) ->
                showRemoveEventDialog(row, position));
        rv.setAdapter(adapter);

        // load data
        db = FirebaseFirestore.getInstance();
        loadEvents();
    }



    /**
     * Loads all event documents ordered by creation time (descending)
     * and updates the RecyclerView adapter
     */

    private void loadEvents() {
        db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<EventAdminAdapter.Row> rows = new ArrayList<>();
                    long now = System.currentTimeMillis();

                    // get data for each event
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        EventAdminAdapter.Row r = new EventAdminAdapter.Row();
                        r.id = d.getId();
                        r.name = d.getString("eventName");
                        r.dateTimeRange = d.getString("dateTimeRange");


                        // compute if the event is open, closed, or upcoming
                        Timestamp start = d.getTimestamp("registrationStartDate");
                        Timestamp end   = d.getTimestamp("registrationEndDate");
                        long s = (start != null) ? start.toDate().getTime() : 0L;
                        long e = (end   != null) ? end.toDate().getTime()   : 0L;

                        r.registrationOpen = (now >= s && now <= e);
                        r.registrationUpcoming = (now < s);

                        rows.add(r);
                    }
                    adapter.set(rows);
                })
                // just in case firebase stops working
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    /**
     * Show confirmation dialog before removing an event.
     */
    private void showRemoveEventDialog(EventAdminAdapter.Row row, int position) {
        if (row == null) return;
        // get the event name
        String eventName = (row.name != null && !row.name.trim().isEmpty()) ? row.name : "(untitled event)";

        // build the dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_remove_event_title)
                .setMessage(getString(R.string.admin_remove_event_message, eventName))
                .setPositiveButton(R.string.admin_remove_event_confirm,
                        (dialog, which) -> removeEvent(row, position))
                .setNegativeButton(R.string.admin_remove_event_cancel, null)
                .show();
    }

    /**
     * Remove event document from Firestore and from the adapter.
     */
    private void removeEvent(EventAdminAdapter.Row row, int position) {
        if (row == null || row.id == null) return;

        // find and remove the event from firebase
        db.collection("events")
                .document(row.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeAt(position);
                    Toast.makeText(this,
                            getString(R.string.admin_remove_event_success),
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.admin_remove_event_fail) + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }


}
