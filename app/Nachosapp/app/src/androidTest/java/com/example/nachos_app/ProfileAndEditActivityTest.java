package com.example.nachos_app;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * This class contains intent tests for the profile viewing and editing functionality. It verifies that
 * the user's information is displayed correctly on the profile screen, that the edit screen is launched
 * correctly, and that the updated information is saved and displayed.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileAndEditActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Initializes Intents before each test. This is necessary to capture and verify intents.
     */
    @Before
    public void setUp() {
        Intents.init();
    }

    /**
     * Releases Intents after each test to clean up the testing environment.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Tests the entire profile viewing and editing flow. It navigates to the profile screen, verifies
     * the initial data, navigates to the edit screen, updates the data, and then verifies that the
     * updated data is displayed on the profile screen.
     */
    @Test
    public void testProfileEditing() {
        // Navigate to the profile screen
        onView(withId(R.id.navigation_profile)).perform(click());

        // Check that the initial data is displayed
        onView(withId(R.id.nameTextView)).check(matches(withText("Test User")));
        onView(withId(R.id.emailTextView)).check(matches(withText("test@example.com")));

        // Navigate to the edit screen
        onView(withId(R.id.settingsButton)).perform(click());
        intended(hasComponent(EditProfileActivity.class.getName()));

        // Edit the user's information
        onView(withId(R.id.nameEditText)).perform(replaceText("Test User Updated"));
        onView(withId(R.id.emailEditText)).perform(replaceText("test_updated@example.com"));
        onView(withId(R.id.saveButton)).perform(click());

        // Give Firebase some time to save the data
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the updated data is displayed on the profile screen
        onView(withId(R.id.nameTextView)).check(matches(withText("Test User Updated")));
        onView(withId(R.id.emailTextView)).check(matches(withText("test_updated@example.com")));
    }
}
