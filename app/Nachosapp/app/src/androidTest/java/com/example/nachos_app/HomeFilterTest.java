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

/**
 * Tests the filtering functionality in the HomeFragment.
 */
@RunWith(AndroidJUnit4.class)
public class HomeFilterTest {

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
    public void testHomeFilter() {
        // 1. Register a user
        onView(withId(R.id.nameEditText)).perform(typeText("Home Filter User"));
        onView(withId(R.id.emailEditText)).perform(typeText("homefilter@example.com"));
        onView(withId(R.id.registerButton)).perform(click());

        try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }

        // 2. Stay on HomeFragment (default)
        
        // 3. Click Filter button
        onView(withId(R.id.filterButton)).perform(click());

        // 4. Verify Popup menu options are visible
        onView(withText("Ongoing")).check(matches(isDisplayed()));
        onView(withText("Future")).check(matches(isDisplayed()));

        // 5. Click "Ongoing"
        onView(withText("Ongoing")).perform(click());
        
        // Ensure app doesn't crash and filter is applied (UI remains visible)
        onView(withId(R.id.eventsRecyclerView)).check(matches(isDisplayed()));
    }
}
