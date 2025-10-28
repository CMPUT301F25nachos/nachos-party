package com.example.nachos_app;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * This class tests the full user profile flow, from registration to editing. It ensures that the app
 * correctly handles user creation, displays the data, and updates it.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileAndEditActivityTest {

    // Start with SplashActivity to mimic a real user launch and handle navigation logic.
    @Rule
    public ActivityScenarioRule<SplashActivity> activityRule = new ActivityScenarioRule<>(SplashActivity.class);

    /**
     * Sets up the test environment before each test. It initializes Espresso Intents and signs out
     * any currently logged-in user to ensure a clean, predictable state.
     */
    @Before
    public void setUp() {
        Intents.init();
        // Sign out any existing user to ensure we start from the registration screen.
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Cleans up the test environment after each test by releasing Espresso Intents.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Tests the entire profile creation and editing flow. This test performs the following steps:
     * 1. Registers a new user with known information.
     * 2. Navigates to the profile screen and verifies the information is displayed correctly.
     * 3. Navigates to the edit screen.
     * 4. Updates the user's information.
     * 5. Verifies that the updated information is reflected on the profile screen.
     */
    @Test
    public void testProfileCreationAndEditingFlow() {
        // Since we signed out, SplashActivity should navigate to RegistrationActivity.
        // 1. Register a new user with known data.
        onView(withId(R.id.nameEditText)).perform(typeText("Test User"));
        onView(withId(R.id.emailEditText)).perform(typeText("test@example.com"));
        onView(withId(R.id.registerButton)).perform(click());

        // After registration, we land on MainActivity.
        // A delay to allow for the activity transition and initial data load.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2. Navigate to the profile and verify the data.
        onView(withId(R.id.navigation_profile)).perform(click());
        // Add a delay to allow data to load from Firestore after navigating.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.nameTextView)).check(matches(withText("Test User")));
        onView(withId(R.id.emailTextView)).check(matches(withText("test@example.com")));

        // 3. Navigate to the edit screen.
        onView(withId(R.id.settingsButton)).perform(click());
        intended(hasComponent(EditProfileActivity.class.getName()));

        // 4. Edit the user's information.
        onView(withId(R.id.nameEditText)).perform(replaceText("Test User Updated"));
        onView(withId(R.id.emailEditText)).perform(replaceText("test_updated@example.com"));
        onView(withId(R.id.saveButton)).perform(click());

        // A delay to allow for the data to be saved to Firestore and the UI to update.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 5. Verify the updated data is displayed on the profile screen.
        onView(withId(R.id.nameTextView)).check(matches(withText("Test User Updated")));
        onView(withId(R.id.emailTextView)).check(matches(withText("test_updated@example.com")));
    }
}
