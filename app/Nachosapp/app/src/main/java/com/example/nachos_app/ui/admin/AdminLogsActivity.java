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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AdminLogsActivity
 * <p>
 * Admin-only screen that aggregates all notifications ever sent in the app,
 * sorted by newest first.
 * It reads every user document from the users collection and, for each user,
 * loads their notifications subcollection from firebase
 * </p>
 *
 *
 */
public class AdminLogsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private AdminLogsAdapter adapter;
    private TextView tvEmpty;

    /**
     * Called when the activity is first created
     * <p>
     * Initializes Firestore, sets up the RecyclerView and starts loading
     * notification data.
     * </p>
     *
     * @param savedInstanceState activity state bundle, if any
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_logs);

        // hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // wire back button
        View back = findViewById(R.id.btn_back);
        if (back != null){
            back.setOnClickListener(v -> finish());
        }

        db = FirebaseFirestore.getInstance();

        tvEmpty = findViewById(R.id.tv_logs_empty);

        RecyclerView rv = findViewById(R.id.rv_admin_logs);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminLogsAdapter(this);
        rv.setAdapter(adapter);

        loadAllNotifications();
    }

    /**
     * Loads all notifications from all users.
     *
     *
     */
    private void loadAllNotifications() {
        db.collection("users")
                .get()
                .addOnSuccessListener(usersSnap -> {
                    // List that will hold all rows from all users
                    List<AdminLogsAdapter.Row> rows = new ArrayList<>();

                    // if there arent any notifications (database reset) apply empty list
                    if (usersSnap.isEmpty()) {
                        applyRows(rows);
                        return;
                    }
                    // we need to know when all user notification queries finish
                    // so remaining here = number of user docs whose notifications are still loading
                    AtomicInteger remaining = new AtomicInteger(usersSnap.size());

                    // loop through all users
                    for (DocumentSnapshot userDoc : usersSnap.getDocuments()) {
                        String uid = userDoc.getId();
                        // get the users name
                        String recipientName = userDoc.getString("name");

                        // load users notification document
                        db.collection("users")
                                .document(uid)
                                .collection("notifications")
                                .get()
                                .addOnSuccessListener(notifSnap -> {

                                    // convert notification into a row
                                    for (DocumentSnapshot notifDoc : notifSnap) {
                                        Notification n = notifDoc.toObject(Notification.class);
                                        if (n == null) continue;

                                        AdminLogsAdapter.Row r = new AdminLogsAdapter.Row();
                                        r.recipientName = recipientName;
                                        r.type = n.getType();
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
                    // if the initial query fails we toast and show an empty list
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
        // Sort newest first by sendTime
        Collections.sort(rows, (o1, o2) -> {
            if (o1.sendTime == null && o2.sendTime == null) return 0;
            if (o1.sendTime == null) return 1;
            if (o2.sendTime == null) return -1;
            return o2.sendTime.compareTo(o1.sendTime);
        });

        adapter.set(rows);

        // show or hide the no logs message
        if (rows.isEmpty()) {
            tvEmpty.setVisibility(TextView.VISIBLE);
        } else {
            tvEmpty.setVisibility(TextView.GONE);
        }
    }
}
