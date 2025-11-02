package com.example.nachos_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    // Entrant views
    private ImageView bannerImage;
    private TextView titleText;
    private TextView dateText;
    private TextView spotsText;
    private TextView registrationPeriodText;
    private TextView descriptionText;
    private Button joinButton;
    private Button showQRCodeButton;

    // Organizer views
    private View organizerSection;
    private TextView statusText;
    private TextView waitingCountText;
    private TextView selectedCountText;
    private TextView enrolledCountText;
    private TextView cancelledCountText;
    private Button editEventButton;
    private Button viewWaitingListButton;
    private Button viewEnrolledButton;
    private Button viewSelectedButton;
    private Button viewCancelledButton;
    private Button drawLotteryButton;
    private Button sendNotificationButton;
    private View organizerDivider;

    private FirebaseFirestore db;
    private String uid;
    private String eventId;
    private DocumentReference eventRef;
    private DocumentReference waitListRef;
    private boolean isOnWaitlist = false;
    private boolean isOrganizer = false;
    private boolean isSelected = false;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        setupActionBar();
        initViews();

        db = FirebaseFirestore.getInstance();

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
            return;
        }
        uid = user.getUid();

        // Retrieve eventId from Intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventRef = db.collection("events").document(eventId);
        waitListRef = eventRef.collection("waitlist").document(uid);

        fetchEventDetails();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        // Entrant views
        bannerImage = findViewById(R.id.eventBannerImageView);
        titleText = findViewById(R.id.eventTitleTextView);
        dateText = findViewById(R.id.eventDateTextView);
        spotsText = findViewById(R.id.eventSpotsTextView);
        registrationPeriodText = findViewById(R.id.eventDrawPeriodTextView);
        descriptionText = findViewById(R.id.eventDescriptionTextView);
        joinButton = findViewById(R.id.joinWaitlistButton);
        showQRCodeButton = findViewById(R.id.showQRCodeButton);

        // Organizer views
        organizerSection = findViewById(R.id.organizerSection);
        organizerDivider = findViewById(R.id.organizerDivider);
        statusText = findViewById(R.id.statusText);
        waitingCountText = findViewById(R.id.waitingCountText);
        selectedCountText = findViewById(R.id.selectedCountText);
        enrolledCountText = findViewById(R.id.enrolledCountText);
        cancelledCountText = findViewById(R.id.cancelledCountText);

        editEventButton = findViewById(R.id.editEventButton);
        viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        viewEnrolledButton = findViewById(R.id.viewEnrolledButton);
        viewSelectedButton = findViewById(R.id.viewSelectedButton);
        viewCancelledButton = findViewById(R.id.viewCancelledButton);
        drawLotteryButton = findViewById(R.id.drawLotteryButton);
        sendNotificationButton = findViewById(R.id.sendNotificationButton);

        // Initialize visibility - hide organizer section initially
        organizerSection.setVisibility(View.GONE);
        organizerDivider.setVisibility(View.GONE);
        joinButton.setVisibility(View.GONE);

        // Show QR Code button is visible to all users
        showQRCodeButton.setOnClickListener(v -> showQRCodeDialog());

        setupOrganizerButtons();
    }

    private void setupOrganizerButtons() {
        editEventButton.setOnClickListener(v -> {
            Toast.makeText(this, "Edit event coming soon", Toast.LENGTH_SHORT).show();
        });

        viewWaitingListButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "waitlist");
            startActivity(intent);
        });

        viewEnrolledButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "enrolled");
            startActivity(intent);
        });

        viewSelectedButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "selected");
            startActivity(intent);
        });

        viewCancelledButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "cancelled");
            startActivity(intent);
        });

        drawLotteryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawLotteryActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        sendNotificationButton.setOnClickListener(v -> {
            Toast.makeText(this, "Send notifications coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void showQRCodeDialog() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String qrCodeBase64 = currentEvent.getQrCodeUrl();
        if (qrCodeBase64 == null || qrCodeBase64.isEmpty()) {
            Toast.makeText(this, "QR code not available", Toast.LENGTH_SHORT).show();
            return;
        }

        QRCodeDialogFragment dialog = QRCodeDialogFragment.newInstance(
                currentEvent.getEventName(),
                qrCodeBase64
        );
        dialog.show(getSupportFragmentManager(), "QRCodeDialog");
    }

    private void fetchEventDetails() {
        eventRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Event event = snapshot.toObject(Event.class);
                    if (event == null) {
                        Toast.makeText(this, "Invalid event data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Store current event for QR code display
                    currentEvent = event;

                    // Check if user is the organizer
                    isOrganizer = event.getOrganizerId().equals(uid);

                    populateUI(event);

                    if (isOrganizer) {
                        showOrganizerView(event);
                    } else {
                        showEntrantView();
                        checkWaitlist();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void populateUI(Event event) {
        titleText.setText(event.getEventName());
        descriptionText.setText(event.getDescription());

        String dateRange = event.getDateTimeRange();
        dateText.setText("Date and Time: " + dateRange);

        Integer maxParticipants = event.getMaxParticipants();
        spotsText.setText("Total spots: " +
                (maxParticipants != null ? maxParticipants : "Unlimited"));

        setRegistrationText(event.getRegistrationStartDate(), event.getRegistrationEndDate());

        ImageUtils.loadBase64Image(
                bannerImage,
                event.getBannerUrl(),
                R.drawable.ic_camera_placeholder
        );
    }

    private void showOrganizerView(Event event) {
        // Hide join button for organizers
        joinButton.setVisibility(View.GONE);

        // Show divider and organizer section
        organizerDivider.setVisibility(View.VISIBLE);
        organizerSection.setVisibility(View.VISIBLE);

        // Update status
        boolean isOpen = event.isRegistrationOpen();
        boolean isUpcoming = event.isRegistrationUpcoming();

        String status = isOpen ? "Registration Open" :
                (isUpcoming ? "Registration Upcoming" : "Registration Closed");
        statusText.setText("Status: " + status);

        // Get counts from subcollections
        updateCounts();
    }

    private void updateCounts() {
        // Waiting count
        eventRef.collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    waitingCountText.setText("Waiting: " + count);
                    viewWaitingListButton.setText("View Waiting List (" + count + ")");
                });

        // Selected count
        eventRef.collection("selected")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    selectedCountText.setText("Selected: " + count);
                    viewSelectedButton.setText("View Selected Entrants (" + count + ")");
                });

        // Enrolled count
        eventRef.collection("enrolled")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    enrolledCountText.setText("Enrolled: " + count);
                    viewEnrolledButton.setText("View Enrolled Entrants (" + count + ")");
                });

        // Cancelled count
        eventRef.collection("cancelled")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    cancelledCountText.setText("Cancelled: " + count);
                    viewCancelledButton.setText("View Cancelled (" + count + ")");
                });
    }

    private void showEntrantView() {
        // Hide organizer section
        organizerSection.setVisibility(View.GONE);

        // Show join button
        joinButton.setVisibility(View.VISIBLE);

        joinButton.setOnClickListener(v -> {
            if (isOnWaitlist) {
                leaveWaitlist();
            } else {
                joinWaitlist();
            }
        });
    }

    private void setRegistrationText(Date start, Date end) {
        String text = "Registration: " + DATE_FORMAT.format(start)
                + " – " + DATE_FORMAT.format(end);
        registrationPeriodText.setText(text);
    }

    private void checkWaitlist() {
        if (waitListRef == null) return;

        waitListRef.addSnapshotListener((snap, e) -> {
            if (e != null) return;
            isOnWaitlist = (snap != null && snap.exists());

            // Also check if user is selected
            checkSelectedStatus();
        });
    }

    private void checkSelectedStatus() {
        eventRef.collection("selected")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    isSelected = (snapshot != null && snapshot.exists());
                    updateJoinButton();
                });
    }

    private void updateJoinButton() {
        // Check if registration is open
        boolean registrationOpen = currentEvent != null && currentEvent.isRegistrationOpen();
        boolean registrationUpcoming = currentEvent != null && currentEvent.isRegistrationUpcoming();

        if (isSelected) {
            joinButton.setText("You have been selected");
            joinButton.setEnabled(false);
        } else if (isOnWaitlist) {
            joinButton.setText("Leave waitlist");
            joinButton.setEnabled(true);
        } else if (!registrationOpen) {
            // Registration is not open
            if (registrationUpcoming) {
                joinButton.setText("Registration not open yet");
            } else {
                joinButton.setText("Registration closed");
            }
            joinButton.setEnabled(false);
        } else {
            joinButton.setText("Join waitlist");
            joinButton.setEnabled(true);
        }
    }

    private void joinWaitlist() {
        joinButton.setEnabled(false);

        // Check if registration is open
        if (currentEvent == null || !currentEvent.isRegistrationOpen()) {
            if (currentEvent != null && currentEvent.isRegistrationUpcoming()) {
                toast("Registration has not opened yet");
            } else {
                toast("Registration period has closed");
            }
            joinButton.setEnabled(true);
            return;
        }

        // First check if user is already selected
        eventRef.collection("selected")
                .document(uid)
                .get()
                .addOnSuccessListener(selectedSnap -> {
                    if (selectedSnap.exists()) {
                        toast("You have already been selected for this event");
                        joinButton.setEnabled(true);
                        return;
                    }

                    // Check if user is enrolled
                    eventRef.collection("enrolled")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(enrolledSnap -> {
                                if (enrolledSnap.exists()) {
                                    toast("You are already enrolled in this event");
                                    joinButton.setEnabled(true);
                                    return;
                                }

                                // Proceed with joining waitlist
                                proceedJoinWaitlist();
                            });
                });
    }

    private void proceedJoinWaitlist() {
        eventRef.get().addOnSuccessListener(eventSnap -> {
            Long currentWaitlistCount = safeLong(eventSnap.getLong("currentWaitlistCount"), 0L);

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("uid", uid);
            data.put("joinedAt", FieldValue.serverTimestamp());

            waitListRef.set(data)
                    .addOnSuccessListener(aVoid -> {
                        eventRef.update("currentWaitlistCount", currentWaitlistCount + 1);
                        toast("You have joined this waitlist");
                        joinButton.setEnabled(true);
                    })
                    .addOnFailureListener(err -> {
                        toast("Could not join waitlist");
                        joinButton.setEnabled(true);
                    });

        }).addOnFailureListener(err -> {
            toast("Could not check waitlist.");
            joinButton.setEnabled(true);
        });
    }

    private void leaveWaitlist() {
        joinButton.setEnabled(false);

        eventRef.get().addOnSuccessListener(eventSnap -> {
            Long currentWaitlistCount = safeLong(eventSnap.getLong("currentWaitlistCount"), 0L);

            waitListRef.get().addOnSuccessListener(wlSnap -> {
                if (!wlSnap.exists()) {
                    toast("You're not on the waitlist.");
                    joinButton.setEnabled(true);
                    return;
                }

                waitListRef.delete().addOnSuccessListener(aVoid -> {
                    long newCount = Math.max(0, currentWaitlistCount - 1);
                    eventRef.update("currentWaitlistCount", newCount);
                    toast("Removed from waitlist.");
                    joinButton.setEnabled(true);

                }).addOnFailureListener(err -> {
                    toast("Could not leave waitlist.");
                    joinButton.setEnabled(true);
                });

            }).addOnFailureListener(err -> {
                toast("Could not check position on waitlist.");
                joinButton.setEnabled(true);
            });

        }).addOnFailureListener(err -> {
            toast("Could not check waitlist.");
            joinButton.setEnabled(true);
        });
    }

    private long safeLong(Long val, long def) {
        return (val == null) ? def : val;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh counts when returning from other activities
        if (isOrganizer) {
            updateCounts();
        }
    }
}