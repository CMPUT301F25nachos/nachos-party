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

public class DrawLotteryActivity extends AppCompatActivity {

    private TextView eventNameText;
    private TextView waitlistCountText;
    private TextView maxParticipantsText;
    private EditText numberOfWinnersInput;
    private Button drawButton;

    private FirebaseFirestore db;
    private String eventId;
    private int waitlistCount = 0;
    private Integer maxParticipants = null;
    private NotificationSender notifSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_lottery);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Draw Lottery");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        notifSender = new NotificationSender(eventId, db);

        if (eventId == null) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadEventData();
    }

    private void initViews() {
        eventNameText = findViewById(R.id.eventNameText);
        waitlistCountText = findViewById(R.id.waitlistCountText);
        maxParticipantsText = findViewById(R.id.maxParticipantsText);
        numberOfWinnersInput = findViewById(R.id.numberOfWinnersInput);
        drawButton = findViewById(R.id.drawButton);

        drawButton.setOnClickListener(v -> performDraw());
    }

    private void loadEventData() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Event event = snapshot.toObject(Event.class);
                    if (event == null) return;

                    eventNameText.setText("Event: " + event.getEventName());
                    maxParticipants = event.getMaxParticipants();
                    maxParticipantsText.setText("Max Participants: " +
                            (maxParticipants != null ? maxParticipants : "Unlimited"));

                    loadWaitlistCount();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event: " + e.getMessage(),
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
                    waitlistCountText.setText("Current Waitlist: " + waitlistCount + " entrants");

                    // Suggest number of winners
                    if (maxParticipants != null) {
                        numberOfWinnersInput.setHint("Max: " +
                                Math.min(maxParticipants, waitlistCount));
                    } else {
                        numberOfWinnersInput.setHint("Up to " + waitlistCount);
                    }
                });
    }

    private void performDraw() {
        String input = numberOfWinnersInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter number of winners", Toast.LENGTH_SHORT).show();
            return;
        }

        int numWinners;
        try {
            numWinners = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numWinners <= 0) {
            Toast.makeText(this, "Number must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numWinners > waitlistCount) {
            Toast.makeText(this, "Cannot draw more winners than waitlist size",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (maxParticipants != null && numWinners > maxParticipants) {
            Toast.makeText(this, "Cannot draw more winners than max participants",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Draw")
                .setMessage("Draw " + numWinners + " winners from the waiting list?")
                .setPositiveButton("Draw", (dialog, which) -> executeDraw(numWinners))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeDraw(int numWinners) {
        drawButton.setEnabled(false);
        Toast.makeText(this, "Drawing lottery...", Toast.LENGTH_SHORT).show();

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

                    // Randomly shuffle and select winners
                    Collections.shuffle(waitlistDocs);
                    List<DocumentSnapshot> winners = waitlistDocs.subList(0,
                            Math.min(numWinners, waitlistDocs.size()));

                    // Move winners to selected collection
                    moveToSelected(winners);
                    notifSender.sendSelectionNotifications(winners, waitlistDocs);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to draw lottery: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    drawButton.setEnabled(true);
                });
    }

    private void moveToSelected(List<DocumentSnapshot> winners) {
        WriteBatch batch = db.batch();

        for (DocumentSnapshot doc : winners) {
            String uid = doc.getString("uid");

            // Add to selected collection
            Map<String, Object> selectedData = new HashMap<>();
            selectedData.put("uid", uid);
            selectedData.put("joinedAt", doc.get("joinedAt"));
            selectedData.put("selectedAt", FieldValue.serverTimestamp());
            selectedData.put("status", "pending"); // pending, accepted, declined

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

        // Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully selected " + winners.size() + " winners!",
                            Toast.LENGTH_LONG).show();

                    // Update waitlist count
                    db.collection("events")
                            .document(eventId)
                            .update("currentWaitlistCount",
                                    FieldValue.increment(-winners.size()));

                    drawButton.setEnabled(true);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to complete draw: " + e.getMessage(),
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