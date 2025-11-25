package com.example.nachos_app;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity for displaying lists of entrants in different states.
 * Can display waitlist, selected, enrolled, or cancelled entrants for an event.
 */
public class EntrantListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private FirebaseFirestore db;
    private String eventId;
    private String listType; // "waitlist", "selected", "enrolled", "cancelled"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_list);

        db = FirebaseFirestore.getInstance();

        // Get parameters from intent
        eventId = getIntent().getStringExtra("eventId");
        listType = getIntent().getStringExtra("listType");

        if (eventId == null || listType == null) {
            Toast.makeText(this, "Invalid parameters", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupActionBar();
        setupRecyclerView();
        loadEntrants();
    }

    /**
     * Sets up the action bar with appropriate title based on list type.
     * Titles: "Waiting List", "Selected Entrants", "Enrolled Entrants", "Cancelled Entrants"
     */
    private void setupActionBar() {
        String title = getTitle(listType);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private String getTitle(String type) {
        switch (type) {
            case "waitlist":
                return "Waiting List";
            case "selected":
                return "Selected Entrants";
            case "enrolled":
                return "Enrolled Entrants";
            case "cancelled":
                return "Cancelled Entrants";
            default:
                return "Entrants";
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.entrantListRecyclerView);
        adapter = new UserAdapter(this, listType);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Loads entrants from the appropriate Firestore subcollection.
     * Fetches all documents from events/{eventId}/{listType} collection.
     * Extracts user IDs and relevant metadata (timestamps, status).
     */
    private void loadEntrants() {
        db.collection("events")
                .document(eventId)
                .collection(listType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> userIds = new ArrayList<>();
                    List<Map<String, Object>> userDataList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        userIds.add(doc.getId());

                        Map<String, Object> data = new HashMap<>();
                        data.put("uid", doc.getString("uid"));

                        // Add timestamps based on list type
                        switch (listType) {
                            case "waitlist":
                                data.put("joinedAt", doc.getTimestamp("joinedAt"));
                                break;
                            case "selected":
                                data.put("selectedAt", doc.getTimestamp("selectedAt"));
                                data.put("status", doc.getString("status"));
                                break;
                            case "enrolled":
                                data.put("enrolledAt", doc.getTimestamp("enrolledAt"));
                                break;
                            case "cancelled":
                                data.put("cancelledAt", doc.getTimestamp("cancelledAt"));
                                break;
                        }

                        userDataList.add(data);
                    }

                    fetchUserProfiles(userIds, userDataList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load list: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserProfiles(List<String> userIds, List<Map<String, Object>> userDataList) {
    if (userIds.isEmpty()) {
        adapter.setUsers(userIds, userDataList);
        Toast.makeText(this, "No entrants in this list", Toast.LENGTH_SHORT).show();
        return;
    }

    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
    for (String uid : userIds) {
        tasks.add(db.collection("users").document(uid).get());
    }

    Tasks.whenAllSuccess(tasks)
        .addOnSuccessListener(results -> {
            for (int i = 0; i < results.size(); i++) {
                DocumentSnapshot snapshot = (DocumentSnapshot) results.get(i);
                String name = snapshot.getString("name");
                if (name != null && !name.trim().isEmpty()) {
                    userDataList.get(i).put("name", name.trim());
                } 
                // Else fallback is already ID-based from userDataList
            }

            adapter.setUsers(userIds, userDataList);
        })
        .addOnFailureListener(e -> {
            // Worst case: fallback to IDs
            adapter.setUsers(userIds, userDataList);
        });
}


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
