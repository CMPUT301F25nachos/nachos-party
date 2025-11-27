package com.example.nachos_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * Activity responsible for creating new events with all required details.
 * Allows organizers to set event information, registration periods, participant
 * limits, upload banners, and automatically generates QR codes.
 */
public class CreateEventActivity extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText descriptionEditText;
    private EditText maxParticipantsEditText;
    private EditText eventLocationEditText;
    private TextView registrationStartTextView;
    private TextView registrationEndTextView;
    private TextView eventDateTextView;
    private Date eventDate;
    private CheckBox enableGeoLocationCheckBox;
    private ImageView bannerPreviewImageView;
    private Button uploadBannerButton;
    private Button createEventButton;
    private ImageButton backButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private Uri selectedBannerUri;
    private Date registrationStartDate;
    private Date registrationEndDate;
    private boolean geoLocationEnabled = false;
    private Double eventLatitude = null;
    private Double eventLongitude = null;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedBannerUri = result.getData().getData();
                        if (selectedBannerUri != null) {
                            bannerPreviewImageView.setImageURI(selectedBannerUri);
                            bannerPreviewImageView.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        setContentView(R.layout.activity_create_event);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        backButton = findViewById(R.id.backButton);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        maxParticipantsEditText = findViewById(R.id.maxParticipantsEditText);
        eventLocationEditText = findViewById(R.id.eventLocationEditText);
        registrationStartTextView = findViewById(R.id.registrationStartTextView);
        registrationEndTextView = findViewById(R.id.registrationEndTextView);
        eventDateTextView = findViewById(R.id.eventDateTextView);
        enableGeoLocationCheckBox = findViewById(R.id.enableGeoLocationCheckBox);
        bannerPreviewImageView = findViewById(R.id.bannerPreviewImageView);
        uploadBannerButton = findViewById(R.id.uploadBannerButton);
        createEventButton = findViewById(R.id.createEventButton);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Set up date pickers
        eventDateTextView.setOnClickListener(v -> showDatePicker("event"));
        registrationStartTextView.setOnClickListener(v -> showDatePicker("registrationStart"));
        registrationEndTextView.setOnClickListener(v -> showDatePicker("registrationEnd"));

        // GeoLocation checkbox
        enableGeoLocationCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            geoLocationEnabled = isChecked;
            if (isChecked) {
                // TODO: Implement geolocation functionality
                // For now, just show a toast
                Toast.makeText(this, "GeoLocation will be implemented", Toast.LENGTH_SHORT).show();
                // When implemented, you would call: requestUserLocation();
            }
        });

        // Upload banner
        uploadBannerButton.setOnClickListener(v -> openImagePicker());

        // Create event
        createEventButton.setOnClickListener(v -> validateAndCreateEvent());
    }

    /**
     * Displays a date picker dialog for selecting any date.
     * Sets time to 00:00:01 for event and registration start dates.
     * Sets time to 23:59:59 for registration end date.
     * @param dateType Either "event", "registrationStart", or "registrationEnd"
     */
    private void showDatePicker(String dateType) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

                    // Registration end gets end of day, everything else gets start of day
                    if (dateType.equals("registrationEnd")) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
                        selectedDate.set(Calendar.MINUTE, 59);
                        selectedDate.set(Calendar.SECOND, 59);
                        registrationEndDate = selectedDate.getTime();
                        registrationEndTextView.setText(sdf.format(registrationEndDate));
                    } else {
                        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                        selectedDate.set(Calendar.MINUTE, 0);
                        selectedDate.set(Calendar.SECOND, 1);

                        if (dateType.equals("event")) {
                            eventDate = selectedDate.getTime();
                            eventDateTextView.setText(sdf.format(eventDate));
                        } else {
                            registrationStartDate = selectedDate.getTime();
                            registrationStartTextView.setText(sdf.format(registrationStartDate));
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Validates all input fields and initiates event creation process.
     * Checks for:
     * - Non-empty event name and description
     * - Valid registration period dates
     * - Valid max participants (if specified)
     * - User authentication
     */
    private void validateAndCreateEvent() {
        String eventName = eventNameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String maxParticipantsStr = maxParticipantsEditText.getText().toString().trim();
        String eventLocation = eventLocationEditText.getText().toString().trim();

        // Validation
        if (eventName.isEmpty()) {
            Toast.makeText(this, "Event name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (registrationStartDate == null || registrationEndDate == null) {
            Toast.makeText(this, "Please select registration period", Toast.LENGTH_SHORT).show();
            return;
        }

        if (registrationEndDate.before(registrationStartDate)) {
            Toast.makeText(this, "Registration end date must be after start date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate event date if provided
        if (eventDate != null) {
            // Event date must be at least 1 week after registration closes
            Calendar registrationEndCal = Calendar.getInstance();
            registrationEndCal.setTime(registrationEndDate);

            Calendar oneWeekAfterRegEnd = (Calendar) registrationEndCal.clone();
            oneWeekAfterRegEnd.add(Calendar.DAY_OF_YEAR, 7);

            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(eventDate);

            if (eventCal.before(oneWeekAfterRegEnd)) {
                Toast.makeText(this, "Event date must be at least 1 week after registration closes",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        Integer maxParticipants = null;
        if (!maxParticipantsStr.isEmpty()) {
            try {
                maxParticipants = Integer.parseInt(maxParticipantsStr);
                if (maxParticipants <= 0) {
                    Toast.makeText(this, "Max participants must be positive", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid max participants number", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        createEventButton.setEnabled(false);
        createEventButton.setText("Creating...");

        // Create event
        createEvent(eventName, description, maxParticipants, eventLocation);
    }

    private void createEvent(String eventName, String description, Integer maxParticipants, String eventLocation) {
        String eventId = db.collection("events").document().getId();
        String organizerId = currentUser.getUid();

        // Get organizer name from Firestore
        db.collection("users").document(organizerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User organizer = documentSnapshot.toObject(User.class);
                    String organizerName = (organizer != null) ? organizer.getName() : "Unknown";

                    if (selectedBannerUri != null) {
                        // Process banner as base64
                        processBanner(eventId, eventName, description, maxParticipants, organizerId, organizerName, eventLocation);
                    } else {
                        // Create event without banner
                        saveEventToFirestore(eventId, eventName, description, maxParticipants, organizerId, organizerName, null, eventLocation);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    resetCreateButton();
                });
    }

    /**
     * Processes the selected banner image by resizing and compressing it.
     * Limits image size to prevent Firestore document size issues (1MB limit).
     * Max width: 600px, JPEG quality: 70%, Max size: 500KB
     * @param eventId ID of the event being created
     * @param eventName name of the event
     * @param description event description
     * @param maxParticipants Maximum number of participants (null for unlimited)
     * @param organizerId organizer's user ID
     * @param organizerName organizer's name
     * @param eventLocation optional event's location
     */
    private void processBanner(String eventId, String eventName, String description, Integer maxParticipants,
                               String organizerId, String organizerName, String eventLocation) {
        // Process image on background thread
        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedBannerUri);

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

                // Check size (Firestore has 1MB limit per document)
                if (data.length > 500000) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Image too large, please select a smaller image", Toast.LENGTH_LONG).show();
                        resetCreateButton();
                    });
                    return;
                }

                String base64Banner = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);

                // Save on main thread
                runOnUiThread(() -> saveEventToFirestore(eventId, eventName, description, maxParticipants,
                        organizerId, organizerName, base64Banner, eventLocation));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error processing banner", Toast.LENGTH_SHORT).show();
                    resetCreateButton();
                });
            }
        }).start();
    }

    /**
     * Saves the event data to Firestore.
     * After successful save, initiates QR code generation.
     * @param eventId ID for the event
     * @param eventName name of the event
     * @param description event description
     * @param maxParticipants Maximum participants allowed (null for unlimited)
     * @param organizerId organizer's user ID
     * @param organizerName organizer's name
     * @param bannerBase64 Base64 encoded banner image (null if no banner)
     * @param eventLocation optional event's location
     */
    private void saveEventToFirestore(String eventId, String eventName, String description,
                                      Integer maxParticipants, String organizerId,
                                      String organizerName, String bannerBase64, String eventLocation) {
        // Generate QR code data
        String qrCodeData = "event://" + eventId;

        // Generate dateRange string for display
        String dateRange = generateDateRangeString(registrationStartDate, registrationEndDate);

        Event event = new Event(organizerId, organizerName, eventName, description, dateRange,
                registrationStartDate, registrationEndDate, eventDate, maxParticipants,
                bannerBase64, null, qrCodeData, new Date(), eventLocation);

        db.collection("events").document(eventId)
                .set(event).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Exit immediately

                    // Generate QR code in background
                    generateAndSaveQRCode(eventId, qrCodeData);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating event", Toast.LENGTH_SHORT).show();
                    resetCreateButton();
                });
    }

    /**
     * Generates a formatted date range string for display.
     * Formats dates based on whether they're in the same day/month.
     * Examples: "Dec 10", "Dec 10-15", "Dec 10 - Jan 5"
     * @param startDate Registration start date
     * @param endDate Registration end date
     * @return Formatted date range string
     */
    private String generateDateRangeString(Date startDate, Date endDate) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        SimpleDateFormat monthDay = new SimpleDateFormat("MMM d", Locale.getDefault());
        SimpleDateFormat dayOnly = new SimpleDateFormat("d", Locale.getDefault());

        // Same day
        if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH) &&
                startCal.get(Calendar.DAY_OF_MONTH) == endCal.get(Calendar.DAY_OF_MONTH)) {
            return monthDay.format(startDate);
        }

        // Same month
        if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
            return monthDay.format(startDate) + "-" + dayOnly.format(endDate);
        }

        // Different months
        return monthDay.format(startDate) + " - " + monthDay.format(endDate);
    }

    /**
     * Generates a QR code bitmap and saves it as base64 to Firestore.
     * The QR code encodes the event URL in format: "event://[eventId]"
     * Uses 512x512 pixel resolution.
     * @param eventId event ID to update
     * @param qrCodeData The data to encode in the QR code
     */
    private void generateAndSaveQRCode(String eventId, String qrCodeData) {
        new Thread(() -> {
            try {
                // Generate QR code bitmap
                Bitmap qrCodeBitmap = generateQRCodeBitmap(qrCodeData, 512, 512);

                // Convert bitmap to base64 string
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();
                String base64QrCode = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);

                // Update Firestore with QR code
                db.collection("events").document(eventId).update("qrCodeUrl", base64QrCode);
            } catch (Exception e) {
                // Log error
                android.util.Log.e("CreateEventActivity", "Failed to generate QR code for event " + eventId, e);
            }
        }).start();
    }

    /**
     * Generates a QR code bitmap from the given content string.
     * Uses ZXing library to encode the content into QR_CODE format.
     * @param content data to encode in the QR code
     * @param width width of the QR code in pixels
     * @param height height of the QR code in pixels
     * @return Bitmap representation of the QR code
     * @throws WriterException if QR code generation fails
     */
    private Bitmap generateQRCodeBitmap(String content, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    private void resetCreateButton() {
        createEventButton.setEnabled(true);
        createEventButton.setText("Create Event");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestUserLocation() {
        // TODO: Implement location request
    }
}