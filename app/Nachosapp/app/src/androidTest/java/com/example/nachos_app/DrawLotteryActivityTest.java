package com.example.nachos_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for DrawLotteryActivity.
 * Tests lottery drawing functionality.
 * User Stories Tested:
 * - US 02.05.02: Sample specified number of attendees
 */
@RunWith(AndroidJUnit4.class)
public class DrawLotteryActivityTest {

    private static final String TEST_EVENT_ID = "F500zH1DxqdWEWCG6nNX";

    @Test
    public void testLotteryViewsPresent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DrawLotteryActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);

        ActivityScenario<DrawLotteryActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.eventNameText)).check(matches(isDisplayed()));
        onView(withId(R.id.waitlistCountText)).check(matches(isDisplayed()));
        onView(withId(R.id.maxParticipantsText)).check(matches(isDisplayed()));
        onView(withId(R.id.numberOfWinnersInput)).check(matches(isDisplayed()));
        onView(withId(R.id.drawButton)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testInvalidEventId() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DrawLotteryActivity.class);
        // Missing eventId

        ActivityScenario<DrawLotteryActivity> scenario = ActivityScenario.launch(intent);

        assertEquals("Activity should finish with invalid eventId",
                Lifecycle.State.DESTROYED,
                scenario.getState());
    }

    @Test
    public void testDrawButtonClickable() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DrawLotteryActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);

        ActivityScenario<DrawLotteryActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.drawButton)).check(matches(isClickable()));

        scenario.close();
    }

    @Test
    public void testNumberOfWinnersInput() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DrawLotteryActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);

        ActivityScenario<DrawLotteryActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.numberOfWinnersInput))
                .perform(typeText("5"), closeSoftKeyboard());
        onView(withId(R.id.numberOfWinnersInput)).check(matches(withText("5")));

        scenario.close();
    }

    @Test
    public void testEmptyWinnersInputValidation() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DrawLotteryActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);

        ActivityScenario<DrawLotteryActivity> scenario = ActivityScenario.launch(intent);

        // Click draw without entering number
        onView(withId(R.id.drawButton)).perform(click());

        // Should still be on the same screen
        onView(withId(R.id.drawButton)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testZeroWinnersValidation() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                DrawLotteryActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);

        ActivityScenario<DrawLotteryActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.numberOfWinnersInput))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.drawButton)).perform(click());

        // Should still be on the same screen
        onView(withId(R.id.drawButton)).check(matches(isDisplayed()));

        scenario.close();
    }
}