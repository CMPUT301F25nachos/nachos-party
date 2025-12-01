package com.example.nachos_app;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawReplacementActivity extends AppCompatActivity {

    private TextView eventNameText;
    private TextView availableSlotsText;
    private TextView waitlistCountText;
    private EditText numberOfReplacementsInput;
    private Button drawButton;

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private int availableSlots = 0;
    private int waitlistCount = 0;
    private NotificationSender notifSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_replacement);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Draw Replacements");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId == null) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadData();
    }

    private void initViews() {
        eventNameText = findViewById(R.id.eventNameText);
        availableSlotsText = findViewById(R.id.availableSlotsText);
        waitlistCountText = findViewById(R.id.waitlistCountText);
        numberOfReplacementsInput = findViewById(R.id.numberOfReplacementsInput);
        drawButton = findViewById(R.id.drawButton);

        eventNameText.setText("Event: " + eventName);
        drawButton.setOnClickListener(v -> performDraw());
    }

    private void loadData() {
        if (notifSender == null) {
            notifSender = new NotificationSender(eventId, eventName, db);
        }

        // Count only cancelled entries that haven't been replaced yet
        db.collection("events")
                .document(eventId)
                .collection("cancelled")
                .whereEqualTo("replacementFilled", false)  // Only unfilled slots
                .get()
                .addOnSuccessListener(cancelledSnapshot -> {
                    availableSlots = cancelledSnapshot.size();

                    availableSlotsText.setText("Available replacement slots: " + availableSlots);

                    if (availableSlots == 0) {
                        Toast.makeText(this, "No replacement slots available", Toast.LENGTH_SHORT).show();
                        drawButton.setEnabled(false);
                    }

                    loadWaitlistCount();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadWaitlistCount() {
        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    waitlistCount = querySnapshot.size();
                    waitlistCountText.setText("Entrants on waitlist: " + waitlistCount);

                    if (waitlistCount == 0) {
                        Toast.makeText(this, "No entrants available on waitlist", Toast.LENGTH_SHORT).show();
                        drawButton.setEnabled(false);
                    }

                    // Set hint for input
                    int maxDraw = Math.min(availableSlots, waitlistCount);
                    numberOfReplacementsInput.setHint("Max: " + maxDraw);
                });
    }

    private void performDraw() {
        String input = numberOfReplacementsInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter number of replacements", Toast.LENGTH_SHORT).show();
            return;
        }

        int numReplacements;
        try {
            numReplacements = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numReplacements <= 0) {
            Toast.makeText(this, "Number must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxDraw = Math.min(availableSlots, waitlistCount);
        if (numReplacements > maxDraw) {
            Toast.makeText(this, "Cannot draw more than " + maxDraw + " replacements",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Replacement Draw")
                .setMessage("Draw " + numReplacements + " replacement(s) from the waiting list?")
                .setPositiveButton("Draw", (dialog, which) -> executeDraw(numReplacements))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeDraw(int numReplacements) {
        drawButton.setEnabled(false);
        Toast.makeText(this, "Drawing replacements...", Toast.LENGTH_SHORT).show();

        // Get all waitlist entries
        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> waitlistDocs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        waitlistDocs.add(doc);
                    }

                    if (waitlistDocs.isEmpty()) {
                        Toast.makeText(this, "No entrants on waitlist", Toast.LENGTH_SHORT).show();
                        drawButton.setEnabled(true);
                        return;
                    }

                    // Randomly shuffle and select replacements
                    Collections.shuffle(waitlistDocs);
                    List<DocumentSnapshot> replacements = waitlistDocs.subList(0,
                            Math.min(numReplacements, waitlistDocs.size()));

                    // Move replacements to selected collection
                    moveToSelected(replacements);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to draw replacements: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    drawButton.setEnabled(true);
                });
    }

    private void moveToSelected(List<DocumentSnapshot> replacements) {
        // Fetch the cancelled slots to mark as filled
        db.collection("events")
                .document(eventId)
                .collection("cancelled")
                .whereEqualTo("replacementFilled", false)
                .limit(replacements.size())
                .get()
                .addOnSuccessListener(cancelledSnapshot -> {
                    // Create the batch with all operations
                    WriteBatch batch = db.batch();

                    // Add replacements to selected and remove from waitlist
                    for (DocumentSnapshot doc : replacements) {
                        String uid = doc.getString("uid");

                        // Add to selected collection
                        Map<String, Object> selectedData = new HashMap<>();
                        selectedData.put("uid", uid);
                        selectedData.put("joinedAt", doc.get("joinedAt"));
                        selectedData.put("selectedAt", FieldValue.serverTimestamp());

                        batch.set(db.collection("events")
                                .document(eventId)
                                .collection("selected")
                                .document(uid), selectedData);

                        // Remove from waitlist
                        batch.delete(db.collection("events")
                                .document(eventId)
                                .collection("waitlist")
                                .document(uid));
                    }

                    // Mark cancelled slots as filled
                    for (DocumentSnapshot cancelledDoc : cancelledSnapshot.getDocuments()) {
                        batch.update(cancelledDoc.getReference(), "replacementFilled", true);
                    }

                    // Commit the complete batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Successfully selected " + replacements.size() +
                                        " replacement(s)!", Toast.LENGTH_LONG).show();

                                // Update waitlist count
                                db.collection("events")
                                        .document(eventId)
                                        .update("currentWaitlistCount",
                                                FieldValue.increment(-replacements.size()));

                                // Send notifications
                                notifSender.sendSelectionNotifications(replacements, new ArrayList<>());

                                drawButton.setEnabled(true);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to complete draw: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                drawButton.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch cancelled slots: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    drawButton.setEnabled(true);
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