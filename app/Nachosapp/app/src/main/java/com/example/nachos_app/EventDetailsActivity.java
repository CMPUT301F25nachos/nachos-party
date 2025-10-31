package com.example.nachos_app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Displays details for a specific event selected by the user.
 * Fetches data from Firestore using the eventId passed via Intent.
 */
public class EventDetailsActivity extends AppCompatActivity {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private ImageView bannerImage;
    private TextView titleText;
    private TextView dateText;
    private TextView spotsText;
    private TextView entrantsText;
    private TextView registrationPeriodText;
    private TextView descriptionText;
    private MaterialButton joinButton;

    private FirebaseFirestore db;

    private String uid;
    private DocumentReference eventRef;
    private DocumentReference waitListRef;
    private boolean isOnWaitlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        setupActionBar();
        initViews();
        joinButton = findViewById(R.id.joinWaitlistButton);
        db = FirebaseFirestore.getInstance();

        // get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // currently here to fix null pointers, we're gonna have to figure this out later
        if (user == null){
            joinButton.setEnabled(false);
            joinButton.setText("Sign in required");
        }
        uid = user.getUid();

        // Retrieve eventId from Intent
        String eventId = getIntent().getStringExtra("eventId");

        eventRef = db.collection("events").document(eventId);
        waitListRef = eventRef.collection("waitlist").document(uid);


        checkWaitlist();

        joinButton.setOnClickListener(v -> {
            if (isOnWaitlist){
                leaveWaitlist(); // user is currently on waitlist
            } else {
                joinWaitlist(); // add user to waitlist
            }
        });

        fetchEventDetails(eventId);
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        bannerImage = findViewById(R.id.eventBannerImageView);
        titleText = findViewById(R.id.eventTitleTextView);
        dateText = findViewById(R.id.eventDateTextView);
        spotsText = findViewById(R.id.eventSpotsTextView);
        entrantsText = findViewById(R.id.eventEntrantsTextView);
        registrationPeriodText = findViewById(R.id.eventDrawPeriodTextView);
        descriptionText = findViewById(R.id.eventDescriptionTextView);
        joinButton = findViewById(R.id.joinWaitlistButton);
    }

    private void fetchEventDetails(String eventId) {
        db.collection("events")
                .document(eventId)
                .get()
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

                    populateUI(event);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void populateUI(Event event) {
        // Title
        titleText.setText(event.getEventName());

        // Description
        descriptionText.setText(event.getDescription());

        // Schedule
        String dateRange = event.getDateTimeRange();
        dateText.setText("Date and Time: " + dateRange);

        // Spots and entrants
        Integer maxParticipants = event.getMaxParticipants();
        spotsText.setText("Total spots: " +
                (maxParticipants != null ? maxParticipants : "Unlimited"));

        entrantsText.setText("Total entrants: " + event.getCurrentWaitlistCount());

        // Registration window
        setRegistrationText(event.getRegistrationStartDate(), event.getRegistrationEndDate());

        // Banner image
        ImageUtils.loadBase64Image(
                bannerImage,
                event.getBannerUrl(),
                R.drawable.ic_camera_placeholder
        );

        // Button logic
        //configureJoinButton(event);
    }

    private void setRegistrationText(Date start, Date end) {
        String text = "Registration: " + DATE_FORMAT.format(start)
                + " – " + DATE_FORMAT.format(end);
        registrationPeriodText.setText(text);
    }

    /**private void configureJoinButton(Event event) {
        boolean isOpen = event.isRegistrationOpen();
        boolean isUpcoming = event.isRegistrationUpcoming();

        if (isOpen) {
            joinButton.setEnabled(true);
            joinButton.setText("Join Waitlist");
            // TODO: Add waiting list code here @Darius
            joinButton.setOnClickListener(v ->
                    Toast.makeText(this, "Joining coming soon", Toast.LENGTH_SHORT).show()
            );
        } else {
            joinButton.setEnabled(false);
            joinButton.setText("Registration is not open yet");
            joinButton.setOnClickListener(null);
        }
    } */

    private String defaultText(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * check event waitlist for uid
     * if the doc exists in firebase then user is already on waitlist - button should say Leave
     * if the doc does not exist then user is not already on waitlist - button should say Join*/
    public void checkWaitlist(){
        if (waitListRef == null) return; // just in case onCreate failed

        waitListRef.addSnapshotListener((snap, e) -> {
            if (e != null) return;
            isOnWaitlist = (snap != null && snap.exists());
            updateJoinButton();
        });
    }


    /** Updates button label depending if user is on waitlist or not */
    private void updateJoinButton(){
        joinButton.setText(isOnWaitlist ? "Leave waitlist" : "Join waitlist");
        joinButton.setEnabled(true);
    }


    /** Puts user on event waitlist
     * Creates wailist collection which stores uid and joinAt time
     * Increments currentWaitlistCount in events*/
    public void joinWaitlist(){

        joinButton.setEnabled(false); // diable button while adding to avoid double clicks

        // get event data
        eventRef.get().addOnSuccessListener(eventSnap -> {
            // get current waitlist count, set to 0 if null pointer
            Long currentWaitlistCount = safeLong(eventSnap.getLong("currentWaitlistCount"), 0L);

            // create enrollment document
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("uid", uid);
            data.put("joinedAt", FieldValue.serverTimestamp());

            waitListRef.set(data)
                    .addOnSuccessListener(aVoid -> {
                        // increment counter
                        eventRef.update("currentWaitlistCount", currentWaitlistCount + 1);
                        toast("You have joined this waitlist");
                        joinButton.setEnabled(true); // re-enable button

                    })
                    .addOnFailureListener(err -> { // catch all for errors
                        toast("Could not join waitlist");
                        joinButton.setEnabled(true);
                    });

        }).addOnFailureListener(err -> {
            toast("Could not check waitlist.");
        });
    }



    /** Removes user from waitlist if they are on it
     * Deletes the enrolled document in waitlist collection
     * Decrements currentWaitlistCount counter */
    private void leaveWaitlist(){
        joinButton.setEnabled(false);

        // get event data
        eventRef.get().addOnSuccessListener(eventSnap -> {
            Long currentWaitlistCount = safeLong(eventSnap.getLong("currentWaitlistCount"), 0L);


            // check if user is on waitlist
            waitListRef.get().addOnSuccessListener(wlSnap -> {
                if (!wlSnap.exists()) {
                    toast("You're not on the waitlist.");
                    joinButton.setEnabled(true);
                    return;
                }

                // remove the user
                waitListRef.delete().addOnSuccessListener(aVoid ->{
                    long newCount = Math.max(0, currentWaitlistCount -1);
                    eventRef.update("currentWaitlistCount", newCount);
                    toast("Removed from waitlist.");
                    joinButton.setEnabled(true); // update button


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

    /** Simple long function to protect against null pointers */
    private long safeLong(Long val, long def){
        return (val == null) ? def : val;

    }


    /** custom toast function (Im lazy lol) */
    private void toast(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }
}
