package com.example.nachos_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Intent tests for EntrantListActivity.
 * Tests viewing different entrant lists.
 * User Stories Tested:
 * - US 02.02.01: View waitlist entrants
 * - US 02.06.01: View selected entrants
 * - US 02.06.03: View enrolled entrants
 */
@RunWith(AndroidJUnit4.class)
public class EntrantListActivityTest {

    @Test
    public void testWaitlistDisplay() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                EntrantListActivity.class);
        intent.putExtra("eventId", "test123");
        intent.putExtra("listType", "waitlist");

        ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.entrantListRecyclerView)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testSelectedListDisplay() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                EntrantListActivity.class);
        intent.putExtra("eventId", "test123");
        intent.putExtra("listType", "selected");

        ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.entrantListRecyclerView)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testEnrolledListDisplay() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                EntrantListActivity.class);
        intent.putExtra("eventId", "test123");
        intent.putExtra("listType", "enrolled");

        ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.entrantListRecyclerView)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testCancelledListDisplay() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                EntrantListActivity.class);
        intent.putExtra("eventId", "test123");
        intent.putExtra("listType", "cancelled");

        ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.entrantListRecyclerView)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testInvalidParameters() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                EntrantListActivity.class);
        // Missing eventId and listType

        ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent);

        assertEquals("Activity should finish with invalid parameters",
                Lifecycle.State.DESTROYED,
                scenario.getState());
    }

    @Test
    public void testCorrectTitleForWaitlist() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                EntrantListActivity.class);
        intent.putExtra("eventId", "test123");
        intent.putExtra("listType", "waitlist");

        ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            String title = activity.getSupportActionBar() != null ?
                    activity.getSupportActionBar().getTitle().toString() : "";
            assertEquals("Title should be Waiting List", "Waiting List", title);
        });

        scenario.close();
    }
}