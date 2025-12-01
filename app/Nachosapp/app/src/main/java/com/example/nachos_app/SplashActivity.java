package com.example.nachos_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * SplashActivity serves as the initial entry point of the application.
 * Its primary role is to check the user's authentication state and route them
 * to the appropriate screen.
 * <p>
 * If a user is already signed in (i.e., their device is recognized via Firebase Authentication),
 * we check firebase for an existing UID. If the document doesn't exist, it likely means an admin has
 * removed their account, so we make them register again.
 * If the document does exist, they are sent directly to the {@link MainActivity}.
 * <p>
 * If no user is signed in, they are directed to the {@link RegistrationActivity} to create an account.
 * This activity has no UI and finishes immediately after starting the next activity.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created. This method checks the current
     * Firebase user status and launches the corresponding activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data
     *                           it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // new user -> start registration activity
        if (currentUser == null){
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
            return;
        }

        // existing user logic
        String uid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Firestore user document exists → treat as a normal logged-in user
                        // Send them to the main page
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        // The Auth user exists, but the Firestore user document doesn't
                        // this likely means an admin removed their account
                        // so we sign them out and require them to go through registration again
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(this, RegistrationActivity.class);
                        startActivity(intent);
                    }

                    // Finish SplashActivity so the user cannot navigate back to it.
                    finish();
                })
                .addOnFailureListener(e -> {
                    // if the Firestore check fails, we fall back to
                    // allowing the user into MainActivity based purely on Auth
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });


    }
}
