package com.example.nachos_app;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.nachos_app.ui.admin.AdminAllEventsActivity;
import com.example.nachos_app.ui.admin.EventAdminAdapter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Tests the delete-event confirmation dialog in AdminAllEventsActivity.
 *
 * This test:
 *  1. Launches the all events activity
 *  2. calls the showRemoveEventDialog method
 *  3. checks that the dialog is shown
 *  4. clicks the remove button
 *
 *  This test does not check firebase functionality
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminRemoveEventDialogTest {

    @Rule
    public ActivityScenarioRule<AdminAllEventsActivity> rule =
            new ActivityScenarioRule<>(AdminAllEventsActivity.class);

    @Test
    public void removeEventDialog_shows_andRemoveButtonClickable() {
        // trigger the dialog
        rule.getScenario().onActivity(activity -> {
            EventAdminAdapter.Row row = new EventAdminAdapter.Row();
            row.id = null;                 // early return so i don't have to mess with firebase
            row.name = "Test Event";
            row.dateTimeRange = "";
            row.registrationOpen = false;
            row.registrationUpcoming = false;

            try {
                // access the method
                Method m = AdminAllEventsActivity.class.getDeclaredMethod(
                        "showRemoveEventDialog",
                        EventAdminAdapter.Row.class,
                        int.class
                );
                m.setAccessible(true);
                m.invoke(activity, row, 0);
            } catch (Exception e) {
                throw new RuntimeException("Could not access method", e);
            }
        });

        // check the title of dialog
        onView(withText(R.string.admin_remove_event_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // click remove
        onView(withText(R.string.admin_remove_event_confirm))
                .inRoot(isDialog())
                .perform(click());

    }
}
