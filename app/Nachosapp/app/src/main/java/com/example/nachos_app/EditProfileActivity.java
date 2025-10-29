package com.example.nachos_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);

        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get().addOnSuccessListener(snapshot -> {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            nameEditText.setText(user.getName());
                            emailEditText.setText(user.getEmail());
                            phoneEditText.setText(user.getPhoneNumber());
                        }
                    });
        }

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(EditProfileActivity.this, "Name and email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", name);
                userData.put("email", email);
                if (!phone.isEmpty()) {
                    userData.put("phoneNumber", phone);
                }

                db.collection("users").document(currentUser.getUid())
                        .update(userData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
