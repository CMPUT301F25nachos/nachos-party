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

public class CreateEventActivity extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText descriptionEditText;
    private EditText maxParticipantsEditText;
    private TextView registrationStartTextView;
    private TextView registrationEndTextView;
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
        registrationStartTextView = findViewById(R.id.registrationStartTextView);
        registrationEndTextView = findViewById(R.id.registrationEndTextView);
        enableGeoLocationCheckBox = findViewById(R.id.enableGeoLocationCheckBox);
        bannerPreviewImageView = findViewById(R.id.bannerPreviewImageView);
        uploadBannerButton = findViewById(R.id.uploadBannerButton);
        createEventButton = findViewById(R.id.createEventButton);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Set up date pickers
        registrationStartTextView.setOnClickListener(v -> showDatePicker(true));
        registrationEndTextView.setOnClickListener(v -> showDatePicker(false));

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

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        // Set time to 0:01 (00:01:00)
                        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                        selectedDate.set(Calendar.MINUTE, 1);
                        selectedDate.set(Calendar.SECOND, 0);
                        selectedDate.set(Calendar.MILLISECOND, 0);
                        registrationStartDate = selectedDate.getTime();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        registrationStartTextView.setText(sdf.format(registrationStartDate));
                    } else {
                        // Set time to 23:59 (23:59:59)
                        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
                        selectedDate.set(Calendar.MINUTE, 59);
                        selectedDate.set(Calendar.SECOND, 59);
                        selectedDate.set(Calendar.MILLISECOND, 999);
                        registrationEndDate = selectedDate.getTime();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        registrationEndTextView.setText(sdf.format(registrationEndDate));
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

    private void validateAndCreateEvent() {
        String eventName = eventNameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String maxParticipantsStr = maxParticipantsEditText.getText().toString().trim();

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
        createEvent(eventName, description, maxParticipants);
    }

    private void createEvent(String eventName, String description, Integer maxParticipants) {
        String eventId = db.collection("events").document().getId();
        String organizerId = currentUser.getUid();

        // Get organizer name from Firestore
        db.collection("users").document(organizerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User organizer = documentSnapshot.toObject(User.class);
                    String organizerName = (organizer != null) ? organizer.getName() : "Unknown";

                    if (selectedBannerUri != null) {
                        // Process banner as base64
                        processBanner(eventId, eventName, description, maxParticipants, organizerId, organizerName);
                    } else {
                        // Create event without banner
                        saveEventToFirestore(eventId, eventName, description, maxParticipants, organizerId, organizerName, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    resetCreateButton();
                });
    }

    private void processBanner(String eventId, String eventName, String description,
                               Integer maxParticipants, String organizerId, String organizerName) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedBannerUri);

            // Resize bitmap to reduce size (max width 600px for smaller file)
            int maxWidth = 600;
            int maxHeight = (int) (bitmap.getHeight() * (maxWidth / (double) bitmap.getWidth()));
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true);

            // Convert to base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Lower quality = smaller size
            byte[] data = baos.toByteArray();

            // Check size (Firestore has 1MB limit per document)
            if (data.length > 500000) { // 500KB safety margin
                Toast.makeText(this, "Image too large, please select a smaller image", Toast.LENGTH_LONG).show();
                resetCreateButton();
                return;
            }

            String base64Banner = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);

            saveEventToFirestore(eventId, eventName, description, maxParticipants,
                    organizerId, organizerName, base64Banner);
        } catch (Exception e) {
            Toast.makeText(this, "Error processing banner", Toast.LENGTH_SHORT).show();
            resetCreateButton();
        }
    }

    private void saveEventToFirestore(String eventId, String eventName, String description,
                                      Integer maxParticipants, String organizerId, String organizerName, String bannerBase64) {
        // Generate QR code data
        String qrCodeData = "event://" + eventId;

        // Generate dateRange string for display
        String dateRange = generateDateRangeString(registrationStartDate, registrationEndDate);

        Event event = new Event(organizerId, organizerName, eventName, description, dateRange,
                registrationStartDate, registrationEndDate, maxParticipants, bannerBase64,
                null, // QR code will be generated next
                qrCodeData, new Date());

        db.collection("events").document(eventId)
                .set(event).addOnSuccessListener(aVoid -> {
                    // Generate and save QR code
                    generateAndSaveQRCode(eventId, qrCodeData);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating event", Toast.LENGTH_SHORT).show();
                    resetCreateButton();
                });
    }

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

    private void generateAndSaveQRCode(String eventId, String qrCodeData) {
        try {
            // Generate QR code bitmap
            Bitmap qrCodeBitmap = generateQRCodeBitmap(qrCodeData, 512, 512);

            // Convert bitmap to base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            String base64QrCode = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);

            // Store base64 QR code in Firestore
            db.collection("events").document(eventId)
                    .update("qrCodeUrl", base64QrCode).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

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