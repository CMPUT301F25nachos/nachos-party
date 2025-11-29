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
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

/**
 * Tests the filtering functionality in the DashboardFragment (My Events).
 */
@RunWith(AndroidJUnit4.class)
public class DashboardFilterTest {

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
    public void testDashboardFilter() {
        // 1. Register a user
        onView(withId(R.id.nameEditText)).perform(typeText("Filter User"));
        onView(withId(R.id.emailEditText)).perform(typeText("filter@example.com"));
        onView(withId(R.id.registerButton)).perform(click());

        try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }

        // 2. Go to My Events (Dashboard)
        onView(withId(R.id.navigation_dashboard)).perform(click());

        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        // 3. Click Filter button
        onView(withId(R.id.filterButton)).perform(click());

        // 4. Verify Popup menu options are visible (at least one of them)
        onView(withText("Created")).check(matches(isDisplayed()));
        onView(withText("Joined")).check(matches(isDisplayed()));
        onView(withText("Ongoing")).check(matches(isDisplayed()));

        // 5. Click "Created"
        onView(withText("Created")).perform(click());
        
        // Since we haven't created any events, the list should be empty or handled.
        // (This test mainly ensures the UI elements exist and are interactable without crashing)
        // Further testing would require mocking Firestore data or creating events via UI first.
    }
}
