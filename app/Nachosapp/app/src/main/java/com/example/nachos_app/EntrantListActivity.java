package com.example.nachos_app;

import static android.content.Intent.ACTION_CREATE_DOCUMENT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String eventName;
    private static final int CREATE_FILE_REQUEST_CODE = 1;
    private EditText notificationEditText;
    private NotificationSender notifSender;
    private boolean uidsExist = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Get parameters from intent
        eventId = getIntent().getStringExtra("eventId");
        listType = getIntent().getStringExtra("listType");
        eventName = getIntent().getStringExtra("eventName");

        // Setup notification sender
        if (notifSender == null) {
            notifSender = new NotificationSender(eventId, eventName, db);
            notifSender.setType(listType);
        } else {
            notifSender.setEventName(eventName);
            notifSender.setType(listType);
        }

        if (eventId == null || listType == null) {
            Toast.makeText(this, "Invalid parameters", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Choose a slightly different view for enrolled to display Export CSV button
        if (listType.equals("enrolled")) setContentView(R.layout.activity_entrant_list_enrolled);
        else setContentView(R.layout.activity_entrant_list);

        setupActionBar();
        setupRecyclerView();
        loadEntrants();

        // Export CSV button for enrolled entrants
        if (listType.equals("enrolled")) {
            findViewById(R.id.csv_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (uidsExist) {
                        createFile();
                    } else Toast.makeText(v.getContext(), "No entrants to export", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Setup notification sending UI for other list types
        else {
            // Text box
            notificationEditText = findViewById(R.id.notificationEditText);

            // Send button
            findViewById(R.id.send_notification_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String notificationText = notificationEditText.getText().toString().trim();
                    notifSender.sendListNotifications(notificationText);

                    if (notificationText.isEmpty()) {
                        notificationEditText.setError("Notification text is required");
                        notificationEditText.requestFocus();
                    }
                }
            });
        }



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
        adapter = new UserAdapter(this, listType, eventId);
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
                    notifSender.setUserIds(userIds);
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
            uidsExist = false;
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

    // Opens the system file dialogue to save a file
    private void createFile() {
        Intent intent = new Intent(ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "enrolled-entrants.csv");

        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    // Runs after createFile() file system dialogue activity completes, passes uri to exportCSV()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<String> csvData = getCSVData();

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String filePath = uri.getPath();

            if (filePath != null) {
                // We need to give Firebase operations from getCSVData() some time to finish, so delay calling exportCSV()
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exportCSV(uri, csvData);
                    }
                }, 5000);
            } else Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Pulls data from Firestore to be used with exportCSV()
     * @return An array list of CSV data, where each line is an entry in the array
     */
    //
    private ArrayList<String> getCSVData() {
        ArrayList<String> csvData = new ArrayList<>();
        ArrayList<String> uids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Date> timestamps = new ArrayList<>();
        String csvHeader = "name,uid,timestamp\n";

        // Get uids and timestamps from event document
        db.collection("events")
                .document(eventId)
                .collection(listType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    //csvData.add(csvHeader);
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String uid = doc.getId();
                        Timestamp timestamp = (Timestamp) doc.get("enrolledAt");
                        Date formattedTimestamp = timestamp.toDate();

                        uids.add(uid);
                        timestamps.add(formattedTimestamp);
                    }
                });

        // Look up names by UID, delaying slightly to let uids come in
        final Handler nameHandler = new Handler(Looper.getMainLooper());
        nameHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Look up names by uid
                for (int i = 0; i < uids.size(); i++) {
                    final String[] name = new String[1];
                    DocumentReference docRef = db.collection("users").document(uids.get(i));
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Map<String, Object> data = documentSnapshot.getData();
                                name[0] = data.get("name").toString();
                                names.add(name[0]);
                            }
                        }
                    });
                }

            }
        }, 200);

        // Build csvData with a delay to let uids/names Firebase operations complete
        final Handler csvDataHandler = new Handler(Looper.getMainLooper());
        csvDataHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                csvData.add(csvHeader);
                for (int i = 0; i < uids.size(); i++) {
                    String data = names.get(i) + "," + uids.get(i) + "," + timestamps.get(i) + "\n";
                    csvData.add(data);
                }
            }
        }, 400);
        return csvData;
    }

    /**
     * US 02.06.05
     * Exports user data on the enrolled list in the form of UID and timestamp CSV to an an existing file (created by createFile())
     * @param uri The path to a new, empty CSV file
     * @param csvData Data to write to CSV file, with each entry of the array being one line
     */
    private void exportCSV(Uri uri, ArrayList<String> csvData) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                try {
                    for(int i = 0; i < csvData.size(); i++) {
                        outputStream.write(csvData.get(i).getBytes());
                    }
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
                }
            }
        } catch(FileNotFoundException e) {
            Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
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
