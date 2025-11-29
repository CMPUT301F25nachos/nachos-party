package com.example.nachos_app;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.nachos_app.ui.admin.AdminLogsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Basic UI test for the admin logs screen
 *<p>
 * This test: checks that the logs activity can be launched and the
 *  recycler view that displays the logs is there and viewable
 *  </p>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminLogsUiTest {

    /**
     * open and close the activity
     */
    @Rule
    public ActivityScenarioRule<AdminLogsActivity> rule =
            new ActivityScenarioRule<>(AdminLogsActivity.class);

    /**
     * checks that the view is there and visible
     */
    @Test
    public void logsRecyclerView_isDisplayed() {
        onView(withId(R.id.rv_admin_logs))
                .check(matches(isDisplayed()));
    }
}
