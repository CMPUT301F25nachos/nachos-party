package com.example.nachos_app;

import android.content.Intent;
import android.os.SystemClock;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;

/**
 * These tests test the join/leave waitlist functionality
 * Works with any user regardless if they're on the waitlist already or not
 * Eg: If button shows "join waitlist" we expect it to change to "leave waitlist" and vice versa
 *
 * Notes: does not test firebase write/read functionality, only the UI changes
 */
@RunWith(AndroidJUnit4.class)
public class WaitlistUiTest {


    private static final String TEST_EVENT_ID = "F500zH1DxqdWEWCG6nNX";

    @Rule
    public ActivityScenarioRule<EventDetailsActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class)
                            .putExtra("eventId", TEST_EVENT_ID)
            );

    @Test
    public void leaveThenJoinThenLeave_fromAnyState() {

        // if we see leave waitlist, leave the waitlist and then rejoin
        if (isDisplayedNow(withId(R.id.joinWaitlistButton))) {
            onView(withId(R.id.joinWaitlistButton)).perform(click());
            onView(isRoot()).perform(waitForTextOnView(withId(R.id.joinWaitlistButton), "Join waitlist", 5000));
            onView(withId(R.id.joinWaitlistButton)).check(matches(withText("Join waitlist")));
        } else {
            // give the screen time to refresh
            onView(isRoot()).perform(waitForTextOnView(withId(R.id.joinWaitlistButton), "Join waitlist", 5000));
        }

        // If joining waitlist, we expect to see leave waitlist
        onView(withId(R.id.joinWaitlistButton)).check(matches(withText("Join waitlist")));
        onView(withId(R.id.joinWaitlistButton)).perform(click());
        onView(isRoot()).perform(waitForTextOnView(withId(R.id.joinWaitlistButton), "Leave waitlist", 5000));
        onView(withId(R.id.joinWaitlistButton)).check(matches(withText("Leave waitlist")));

        // if leaving waitlist, we expect to see join waitlist
        onView(withId(R.id.joinWaitlistButton)).perform(click());
        onView(isRoot()).perform(waitForTextOnView(withId(R.id.joinWaitlistButton), "Join waitlist", 5000));
        onView(withId(R.id.joinWaitlistButton)).check(matches(withText("Join waitlist")));
    }


    /** Returns true if the view is displayed and currently has the correct text */
    private boolean isDisplayedNow(Matcher<View> viewMatcher) {
        try {
            onView(viewMatcher).check(matches(org.hamcrest.Matchers.allOf(isDisplayed(), withText("Leave waitlist"))));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Waits until a view is displayed and has the expected text or it times out
     */
    public static ViewAction waitForTextOnView(final Matcher<View> viewMatcher, final String expectedText, final long timeoutMs) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() {
                return "wait up to " + timeoutMs + "ms for " + viewMatcher + " to show text \"" + expectedText + "\"";
            }
            @Override public void perform(UiController uiController, View root) {
                final long end = SystemClock.uptimeMillis() + timeoutMs;
                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(root)) {
                        if (viewMatcher.matches(child) && child.isShown()) {
                            try {
                                if (expectedText.contentEquals(((android.widget.TextView) child).getText())) {
                                    return;
                                }
                            } catch (ClassCastException ignored) {}
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (SystemClock.uptimeMillis() < end);
                throw new PerformException.Builder()
                        .withActionDescription(getDescription())
                        .withViewDescription(root.toString())
                        .build();
            }
        };
    }

    /** pause helper that waits for the ui to refresh */
    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for " + millis + " ms"; }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
