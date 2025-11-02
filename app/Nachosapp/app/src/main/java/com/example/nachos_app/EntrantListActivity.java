package com.example.nachos_app;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                    adapter.setUsers(userIds, userDataList);

                    if (userIds.isEmpty()) {
                        Toast.makeText(this, "No entrants in this list",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load list: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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