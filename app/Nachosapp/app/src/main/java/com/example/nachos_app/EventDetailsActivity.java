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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        setupActionBar();
        initViews();
        db = FirebaseFirestore.getInstance();

        // Retrieve eventId from Intent
        String eventId = getIntent().getStringExtra("eventId");

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
        configureJoinButton(event);
    }

    private void setRegistrationText(Date start, Date end) {
        String text = "Registration: " + DATE_FORMAT.format(start)
                + " – " + DATE_FORMAT.format(end);
        registrationPeriodText.setText(text);
    }

    private void configureJoinButton(Event event) {
        boolean isOpen = event.isRegistrationOpen();
        boolean isUpcoming = event.isRegistrationUpcoming();

        if (isOpen) {
            joinButton.setEnabled(true);
            joinButton.setText("Join waiting list");
            // TODO: Add waiting list code here @Darius
            joinButton.setOnClickListener(v ->
                    Toast.makeText(this, "Joining coming soon", Toast.LENGTH_SHORT).show()
            );
        } else {
            joinButton.setEnabled(false);
            joinButton.setText("Registration is not open yet");
            joinButton.setOnClickListener(null);
        }
    }

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
}
