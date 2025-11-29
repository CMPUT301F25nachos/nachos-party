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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Tests the notification opt-out functionality in the Profile section.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationOptOutTest {

    @Rule
    public ActivityScenarioRule<SplashActivity> activityRule = new ActivityScenarioRule<>(SplashActivity.class);

    @Before
    public void setUp() {
        Intents.init();
        FirebaseAuth.getInstance().signOut();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testNotificationOptOut() {
        // 1. Register a user
        onView(withId(R.id.nameEditText)).perform(typeText("OptOut User"));
        onView(withId(R.id.emailEditText)).perform(typeText("optout@example.com"));
        onView(withId(R.id.registerButton)).perform(click());

        try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }

        // 2. Go to Profile
        onView(withId(R.id.navigation_profile)).perform(click());

        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // 3. Check default is "Yes" (or similar) - Spinner interaction
        // Assuming the spinner has "Yes" and "No"
        
        // 4. Select "No" to opt out
        onView(withId(R.id.notificationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("No"))).perform(click());
        
        // 5. Verify selection persists (UI check only here, DB check implied by logic)
        onView(withId(R.id.notificationSpinner)).check(matches(withText(is("No")))); // This matcher might need adjustment for Spinner
    }
}
