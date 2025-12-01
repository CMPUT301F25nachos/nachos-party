package com.example.nachos_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Main activity for viewing event details. Provides different views based on
 * user role (organizer vs entrant). Organizers see management controls and
 * statistics, while entrants see the join/leave waitlist button.
 */
public class EventDetailsActivity extends AppCompatActivity {
    // Entrant views
    private ImageView bannerImage;
    private TextView titleText;
    private TextView dateText;
    private TextView spotsText;
    private TextView locationText;
    private TextView organizerText;
    private TextView registrationPeriodText;
    private TextView descriptionText;
    private Button joinButton;
    private Button showQRCodeButton;
    private View selectionActionContainer;
    private TextView selectionStatusMessage;
    private Button confirmSelectionButton;
    private Button declineSelectionButton;
    private TextView entrantWaitlistCountText;
    private TextView waitlistNoticeText;
    private TextView enrolledStatusText;

    // Organizer views
    private View organizerSection;
    private TextView statusText;
    private TextView waitingCountText;
    private TextView selectedCountText;
    private TextView enrolledCountText;
    private TextView cancelledCountText;
    private Button updateBannerButton;
    private Button viewWaitingListButton;
    private Button viewEnrolledButton;
    private Button viewSelectedButton;
    private Button viewCancelledButton;
    private Button drawLotteryButton;
    private Button drawReplacementButton;
    private Button sendNotificationButton;
    private View organizerDivider;
    private ActivityResultLauncher<Intent> updateBannerLauncher;
    private Uri selectedNewBannerUri;

    private FirebaseFirestore db;
    private String uid;
    private String eventId;
    private DocumentReference eventRef;
    private DocumentReference waitListRef;
    private boolean isOnWaitlist = false;
    private boolean isOrganizer = false;
    private boolean isSelected = false;
    private boolean isEnrolled = false;
    private String selectionStatus;
    private ListenerRegistration selectionStatusRegistration;
    private ListenerRegistration enrolledStatusRegistration;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateBannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedNewBannerUri = result.getData().getData();
                        if (selectedNewBannerUri != null) {
                            updateEventBanner();
                        }
                    }
                }
        );

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
        locationText = findViewById(R.id.eventLocationTextView);
        organizerText = findViewById(R.id.eventOrganizerTextView);
        registrationPeriodText = findViewById(R.id.eventDrawPeriodTextView);
        descriptionText = findViewById(R.id.eventDescriptionTextView);
        entrantWaitlistCountText = findViewById(R.id.entrantWaitlistCountTextView);
        waitlistNoticeText = findViewById(R.id.waitlistNoticeTextView);
        enrolledStatusText = findViewById(R.id.enrolledStatusTextView);
        joinButton = findViewById(R.id.joinWaitlistButton);
        showQRCodeButton = findViewById(R.id.showQRCodeButton);
        selectionActionContainer = findViewById(R.id.selectionActionContainer);
        selectionStatusMessage = findViewById(R.id.selectionStatusMessage);
        confirmSelectionButton = findViewById(R.id.confirmSelectionButton);
        declineSelectionButton = findViewById(R.id.declineSelectionButton);

        // Organizer views
        organizerSection = findViewById(R.id.organizerSection);
        organizerDivider = findViewById(R.id.organizerDivider);
        statusText = findViewById(R.id.statusText);
        waitingCountText = findViewById(R.id.waitingCountText);
        selectedCountText = findViewById(R.id.selectedCountText);
        enrolledCountText = findViewById(R.id.enrolledCountText);
        cancelledCountText = findViewById(R.id.cancelledCountText);

        updateBannerButton = findViewById(R.id.updateBannerButton);
        viewWaitingListButton = findViewById(R.id.viewWaitingListButton);
        viewEnrolledButton = findViewById(R.id.viewEnrolledButton);
        viewSelectedButton = findViewById(R.id.viewSelectedButton);
        viewCancelledButton = findViewById(R.id.viewCancelledButton);
        drawLotteryButton = findViewById(R.id.drawLotteryButton);
        drawReplacementButton = findViewById(R.id.drawReplacementButton);
        sendNotificationButton = findViewById(R.id.sendNotificationButton);

        // Initialize visibility - hide organizer section initially
        organizerSection.setVisibility(View.GONE);
        organizerDivider.setVisibility(View.GONE);
        joinButton.setVisibility(View.GONE);
        selectionActionContainer.setVisibility(View.GONE);
        enrolledStatusText.setVisibility(View.GONE);

        // Show QR Code button is visible to all users
        showQRCodeButton.setOnClickListener(v -> showQRCodeDialog());

        confirmSelectionButton.setOnClickListener(v -> respondToSelection(true));
        declineSelectionButton.setOnClickListener(v -> respondToSelection(false));

        setupOrganizerButtons();
    }

    private void setupOrganizerButtons() {
        updateBannerButton.setOnClickListener(v -> {openBannerPicker();});

        viewWaitingListButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "waitlist");
            intent.putExtra("eventName", currentEvent.getEventName());
            startActivity(intent);
        });

        viewEnrolledButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "enrolled");
            intent.putExtra("eventName", currentEvent.getEventName());
            startActivity(intent);
        });

        viewSelectedButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "selected");
            intent.putExtra("eventName", currentEvent.getEventName());
            startActivity(intent);
        });

        viewCancelledButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantListActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("listType", "cancelled");
            intent.putExtra("eventName", currentEvent.getEventName());
            startActivity(intent);
        });

        drawLotteryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawLotteryActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", currentEvent.getEventName());
            startActivity(intent);
        });

        drawReplacementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawReplacementActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", currentEvent.getEventName());
            startActivity(intent);
        });

        sendNotificationButton.setOnClickListener(v -> {
            Toast.makeText(this, "Send notifications coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Shows a dialog displaying the event's QR code.
     * Uses QRCodeDialogFragment to display the base64 encoded QR image.
     */
    private void showQRCodeDialog() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String qrCodeBase64 = currentEvent.getQrCodeUrl();
        if (qrCodeBase64 == null || qrCodeBase64.isEmpty()) {
            Toast.makeText(this, "QR code not ready yet, please try again in a moment", Toast.LENGTH_SHORT).show();
            return;
        }

        QRCodeDialogFragment dialog = QRCodeDialogFragment.newInstance(
                currentEvent.getEventName(),
                qrCodeBase64
        );
        dialog.show(getSupportFragmentManager(), "QRCodeDialog");
    }

    /**
     * Fetches event details from Firestore and determines user role.
     * If user is the organizer, displays organizer view.
     * Otherwise, displays entrant view with join/leave button.
     */
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
                    loadEntrantWaitlistCount();

                    if (isOrganizer) {
                        showOrganizerView(event);
                    } else {
                        showEntrantView();
                        checkEnrolledStatus();
                        checkWaitlist();
                        checkSelectedStatus();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void populateUI(Event event) {
        titleText.setText(event.getEventName());

        String organizerName = event.getOrganizerName();
        if (organizerName != null && !organizerName.trim().isEmpty()) {
            organizerText.setText("Organizer\n" + organizerName);
        } else {
            organizerText.setText("Organizer\nUnknown");
        }

        descriptionText.setText(event.getDescription());

        // Display event date if available, otherwise show "TBA"
        Date eventDate = event.getEventDate();
        if (eventDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            dateText.setText("Event Date\n " + dateFormat.format(eventDate));
        } else {
            dateText.setText("Event Date\n"  + "TBA");
        }

        Integer maxParticipants = event.getMaxParticipants();
        spotsText.setText("Total spots\n" +
                (maxParticipants != null ? maxParticipants : "Unlimited"));

        // Display location if available
        String eventLocation = event.getEventLocation();
        if (eventLocation != null && !eventLocation.trim().isEmpty()) {
            locationText.setText("Location\n" + eventLocation);
            locationText.setVisibility(View.VISIBLE);
        } else {
            locationText.setVisibility(View.GONE);
        }

        setRegistrationText(event.getRegistrationStartDate(), event.getRegistrationEndDate());

        ImageUtils.loadBase64Image(
                bannerImage,
                event.getBannerUrl(),
                R.drawable.ic_camera_placeholder
        );
    }

    /**
     * Displays the organizer management view with:
     * - Event status (open/upcoming/closed)
     * - Count statistics (waiting, selected, enrolled, cancelled)
     * - Navigation buttons to view each list
     * - Draw lottery button
     */
    private void showOrganizerView(Event event) {
        // Hide join button for organizers
        joinButton.setVisibility(View.GONE);
        selectionActionContainer.setVisibility(View.GONE);
        if (enrolledStatusText != null) {
            enrolledStatusText.setVisibility(View.GONE);
        }

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

    private void loadEntrantWaitlistCount() {
        if (eventRef == null || entrantWaitlistCountText == null) {
            return;
        }

        eventRef.collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    entrantWaitlistCountText.setText("Waitlist\n" + count + " entrants");
                    entrantWaitlistCountText.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> entrantWaitlistCountText.setVisibility(View.GONE));
    }

    /**
     * Displays the entrant view with join/leave waitlist button.
     * Sets up real-time listener for waitlist status changes.
     */
    private void showEntrantView() {
        // Hide organizer section
        organizerSection.setVisibility(View.GONE);

        // Show join button
        joinButton.setVisibility(View.VISIBLE);
        selectionActionContainer.setVisibility(View.GONE);

        joinButton.setOnClickListener(v -> {
            if (isOnWaitlist) {
                leaveWaitlist();
            } else {
                joinWaitlist();
            }
        });
    }

    /**
     * Formats and displays the registration period.
     * @param start Registration start date
     * @param end Registration end date
     */
    private void setRegistrationText(Date start, Date end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String text = "Registration\n" + dateFormat.format(start)
                + " - " + dateFormat.format(end);
        registrationPeriodText.setText(text);
    }

    private void checkWaitlist() {
        if (waitListRef == null) return;

        waitListRef.addSnapshotListener((snap, e) -> {
            if (e != null) return;
            isOnWaitlist = (snap != null && snap.exists());
            updateJoinButton();
        });
    }

    private void checkSelectedStatus() {
        if (eventRef == null) {
            return;
        }

        // Remove old listener if exists
        if (selectionStatusRegistration != null) {
            selectionStatusRegistration.remove();
            selectionStatusRegistration = null;
        }

        selectionStatusRegistration = eventRef.collection("selected")
                .document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    isSelected = (snapshot != null && snapshot.exists());
                    selectionStatus = isSelected ? "pending" : null;

                    updateJoinButton();
                });
    }

    private void checkEnrolledStatus() {
        if (eventRef == null) {
            return;
        }

        if (enrolledStatusRegistration != null) {
            enrolledStatusRegistration.remove();
            enrolledStatusRegistration = null;
        }

        enrolledStatusRegistration = eventRef.collection("enrolled")
                .document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    boolean currentlyEnrolled = snapshot != null && snapshot.exists();
                    if (currentlyEnrolled != isEnrolled) {
                        isEnrolled = currentlyEnrolled;
                        if (isEnrolled) {
                            isSelected = false;
                            selectionStatus = null;
                        }
                    }

                    updateJoinButton();
                });
    }

    /**
     * Updates the join button state and text based on:
     * - Whether user is on waitlist
     * - Whether user has been selected
     * - Whether registration is open
     * - Registration period status
     */
    private void updateJoinButton() {
        boolean registrationOpen = currentEvent != null && currentEvent.isRegistrationOpen();
        boolean registrationUpcoming = currentEvent != null && currentEvent.isRegistrationUpcoming();

        if (enrolledStatusText != null) {
            enrolledStatusText.setVisibility(View.GONE);
        }

        if (isSelected) {
            joinButton.setVisibility(View.GONE);
            if (waitlistNoticeText != null) {
                waitlistNoticeText.setVisibility(View.GONE);
            }
            selectionActionContainer.setVisibility(View.VISIBLE);
            selectionStatusMessage.setText(getString(R.string.selection_pending_message));
            confirmSelectionButton.setVisibility(View.VISIBLE);
            declineSelectionButton.setVisibility(View.VISIBLE);
            setSelectionButtonsEnabled(true);
            return;
        }

        if (isEnrolled) {
            joinButton.setVisibility(View.GONE);
            selectionActionContainer.setVisibility(View.GONE);
            if (waitlistNoticeText != null) {
                waitlistNoticeText.setVisibility(View.GONE);
            }
            if (enrolledStatusText != null) {
                enrolledStatusText.setVisibility(View.VISIBLE);
            }
            return;
        }

        // Not selected - hide selection UI
        selectionActionContainer.setVisibility(View.GONE);
        joinButton.setVisibility(View.VISIBLE);
        waitlistNoticeText.setVisibility(View.GONE);

        if (isOnWaitlist) {
            joinButton.setText("Leave waitlist");
            joinButton.setEnabled(true);
            waitlistNoticeText.setVisibility(View.VISIBLE);
        } else if (!registrationOpen) {
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

    private void setSelectionButtonsEnabled(boolean enabled) {
        confirmSelectionButton.setEnabled(enabled);
        declineSelectionButton.setEnabled(enabled);
    }

    private void respondToSelection(boolean accept) {
        if (!isSelected) {
            toast("Invitation unavailable");
            return;
        }

        setSelectionButtonsEnabled(false);

        DocumentReference selectedRef = eventRef.collection("selected").document(uid);
        selectedRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) {
                        toast("Invitation no longer available");
                        setSelectionButtonsEnabled(true);
                        return;
                    }

                    WriteBatch batch = db.batch();

                    if (accept) {
                        // ACCEPTING: Move from selected to enrolled

                        // Add to enrolled collection
                        DocumentReference enrolledRef = eventRef.collection("enrolled").document(uid);
                        Map<String, Object> enrolledData = new HashMap<>();
                        enrolledData.put("uid", uid);
                        enrolledData.put("enrolledAt", FieldValue.serverTimestamp());
                        enrolledData.put("joinedAt", snapshot.get("joinedAt"));
                        enrolledData.put("selectedAt", snapshot.get("selectedAt"));
                        batch.set(enrolledRef, enrolledData);

                        // Remove from selected collection
                        batch.delete(selectedRef);

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    toast("Spot confirmed!");
                                    // Update local state
                                    isSelected = false;
                                    isEnrolled = true;
                                    selectionStatus = null;
                                    setSelectionButtonsEnabled(true);
                                    updateJoinButton();
                                })
                                .addOnFailureListener(e -> {
                                    toast("Failed to confirm spot: " + e.getMessage());
                                    setSelectionButtonsEnabled(true);
                                });
                    } else {
                        // DECLINING: Move from selected to cancelled

                        // Add to cancelled collection
                        DocumentReference cancelledRef = eventRef.collection("cancelled").document(uid);
                        Map<String, Object> cancelledData = new HashMap<>();
                        cancelledData.put("uid", uid);
                        cancelledData.put("cancelledAt", FieldValue.serverTimestamp());
                        cancelledData.put("reason", "declined");
                        cancelledData.put("replacementFilled", false);
                        cancelledData.put("joinedAt", snapshot.get("joinedAt"));
                        cancelledData.put("selectedAt", snapshot.get("selectedAt"));
                        batch.set(cancelledRef, cancelledData);

                        // Remove from selected collection
                        batch.delete(selectedRef);

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    toast("Invitation declined");
                                    // Update local state
                                    isSelected = false;
                                    selectionStatus = null;
                                    setSelectionButtonsEnabled(true);
                                    updateJoinButton();
                                })
                                .addOnFailureListener(e -> {
                                    toast("Failed to decline invitation: " + e.getMessage());
                                    setSelectionButtonsEnabled(true);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    toast("Failed to process response: " + e.getMessage());
                    setSelectionButtonsEnabled(true);
                });
    }

    /**
     * Initiates the process to join the event waitlist.
     * Validates:
     * - Registration is currently open
     * - User is not already selected
     * - User is not already enrolled
     * Then proceeds with adding user to waitlist collection.
     */
    private void joinWaitlist() {
        joinButton.setEnabled(false);

        if (isEnrolled) {
            toast("You are already enrolled in this event");
            joinButton.setEnabled(true);
            return;
        }

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

    /**
     * Adds the user to the waitlist collection.
     * Increments the currentWaitlistCount in the event document.
     * Sets joinedAt timestamp using server timestamp.
     */
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
                        loadEntrantWaitlistCount();
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

    /**
     * Removes the user from the waitlist collection.
     * Decrements the currentWaitlistCount in the event document.
     */
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
                    loadEntrantWaitlistCount();
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

    /**
     * Opens image picker for selecting a new event banner.
     */
    private void openBannerPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        updateBannerLauncher.launch(intent);
    }

    /**
     * Updates the event banner in Firestore.
     * Processes the selected image by resizing and converting to base64,
     * then updates the event document.
     */
    private void updateEventBanner() {
        if (selectedNewBannerUri == null) {
            return;
        }

        Toast.makeText(this, "Updating banner...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedNewBannerUri);

                // Calculate new dimensions maintaining aspect ratio
                int maxWidth = 800;
                int maxHeight = 600;

                float ratio = Math.min(
                        (float) maxWidth / bitmap.getWidth(),
                        (float) maxHeight / bitmap.getHeight()
                );

                int newWidth = Math.round(bitmap.getWidth() * ratio);
                int newHeight = Math.round(bitmap.getHeight() * ratio);

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                // Convert to base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] data = baos.toByteArray();

                // Check size
                if (data.length > 500000) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Image too large, please select a smaller image",
                                Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String base64Banner = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);

                // Update in Firestore
                runOnUiThread(() -> saveBannerToFirestore(base64Banner));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error processing image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Saves the new banner to Firestore and updates the UI.
     * @param base64Banner Base64 encoded banner image
     */
    private void saveBannerToFirestore(String base64Banner) {
        eventRef.update("bannerUrl", base64Banner)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Banner updated successfully!", Toast.LENGTH_SHORT).show();

                    // Update the current event object
                    if (currentEvent != null) {
                        currentEvent.setBannerUrl(base64Banner);
                    }

                    // Update the banner image view
                    ImageUtils.loadBase64Image(bannerImage, base64Banner, R.drawable.ic_camera_placeholder);

                    // Clear the selected URI
                    selectedNewBannerUri = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update banner: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        if (selectionStatusRegistration != null) {
            selectionStatusRegistration.remove();
            selectionStatusRegistration = null;
        }
        if (enrolledStatusRegistration != null) {
            enrolledStatusRegistration.remove();
            enrolledStatusRegistration = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh counts when returning from other activities
        if (isOrganizer) {
            updateCounts();
        }
        loadEntrantWaitlistCount();
    }
}
