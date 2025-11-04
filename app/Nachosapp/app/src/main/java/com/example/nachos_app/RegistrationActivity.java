package com.example.nachos_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

/**
 * This activity handles user registration. It collects the user's name, email, and optional phone number,
 * and then creates an anonymous user account in Firebase Authentication to identify the device. The user's
 * details are then stored in a "users" collection in Firestore, with the document ID matching the user's
 * Firebase UID.
 */
public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Initializes the activity, setting up the UI components and the Firebase instances. It also sets up the
     * click listener for the register button, which triggers the user registration process.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Registration");
        }

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            // 1. Validate user input
            if (name.isEmpty()) {
                nameEditText.setError("Name is required");
                nameEditText.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Please enter a valid email");
                emailEditText.requestFocus();
                return;
            }

            // Phone number is optional, but if entered, it must be valid
            if (!phone.isEmpty()) {
                if (!Patterns.PHONE.matcher(phone).matches()) {
                    phoneEditText.setError("Please enter a valid phone number (no letters)");
                    phoneEditText.requestFocus();
                    return;
                }
                // Also, check for a minimum of 7 digits
                if (phone.replaceAll("\\D", "").length() < 7) {
                    phoneEditText.setError("Phone number must have at least 7 digits");
                    phoneEditText.requestFocus();
                    return;
                }
            }

            // 2. Sign in the user anonymously to get a unique ID for the device
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                User userObj = new User(name, email, phone, new Date());

                                // 3. Save the user's data to Firestore using the unique user ID
                                db.collection("users").document(userId)
                                        .set(userObj)
                                        .addOnSuccessListener(aVoid -> {
                                            // 4. On success, navigate to the main activity
                                            startActivity(new Intent(this, MainActivity.class));
                                            finish(); // Prevent user from going back to registration
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Error saving user data.", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
