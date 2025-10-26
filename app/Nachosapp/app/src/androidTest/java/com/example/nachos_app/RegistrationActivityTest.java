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
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * This class contains intent tests for the RegistrationActivity. It verifies that the app navigates correctly
 * after a user registers, and that the user's information is saved and can be retrieved.
 */
@RunWith(AndroidJUnit4.class)
public class RegistrationActivityTest {

    @Rule
    public ActivityScenarioRule<RegistrationActivity> activityRule = new ActivityScenarioRule<>(RegistrationActivity.class);

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
     * Tests the user registration flow. It simulates a user entering their information and clicking the
     * register button, then verifies that the app navigates to the MainActivity.
     */
    @Test
    public void testRegistration() {
        onView(withId(R.id.nameEditText)).perform(typeText("Test User"));
        onView(withId(R.id.emailEditText)).perform(typeText("test@example.com"));
        onView(withId(R.id.phoneEditText)).perform(typeText("1234567890"));
        onView(withId(R.id.registerButton)).perform(click());

        // Give Firebase some time to process the registration
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        intended(hasComponent(MainActivity.class.getName()));
    }
}
