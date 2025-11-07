package com.example.nachos_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SplashActivity serves as the initial entry point of the application.
 * Its primary role is to check the user's authentication state and route them
 * to the appropriate screen.
 * <p>
 * If a user is already signed in (i.e., their device is recognized via Firebase Authentication),
 * they are sent directly to the {@link MainActivity}.
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

        // Route user based on authentication state
        if (currentUser != null) {
            // User is already signed in, go to the main content
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            // No user is signed in, go to the registration screen
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        }

        // Finish this activity so the user cannot navigate back to it
        finish();
    }
}
