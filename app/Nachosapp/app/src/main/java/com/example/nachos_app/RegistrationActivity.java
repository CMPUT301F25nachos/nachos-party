package com.example.nachos_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "Name and email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Sign in the user anonymously to get a unique ID for the device
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                if (!phone.isEmpty()) {
                                    userData.put("phoneNumber", phone);
                                }
                                userData.put("createdAt", new Date());

                                // 2. Save the user's data to Firestore using the unique user ID
                                db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            // 3. On success, navigate to the main activity
                                            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish(); // Prevent user from going back to registration
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(RegistrationActivity.this, "Error saving user data.", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
