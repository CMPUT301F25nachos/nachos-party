package com.example.nachos_app;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import com.example.nachos_app.ui.admin.AdminAllEventsActivity;
import com.example.nachos_app.ui.admin.AdminAllUsersActivity;
import com.example.nachos_app.ui.admin.AdminMenuActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Simple tests that verify:
 * 1. tapping on view events sends an Intent to the AdminAllEventsActivity
 * 2. tapping on view profile sends an Intent to the AdminAllUsersActivity
 *
 * We stop all real intents so the tests stay isolated
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminMenuTest {


    /**
     * Launches AdminMenuActivity for each test using ActivityScenario.
     * We test just the intent actually working, as after that its just reads from Firestore
     */
    @Rule
    public ActivityScenarioRule<AdminMenuActivity> activityRule =
            new ActivityScenarioRule<>(AdminMenuActivity.class);

    @Before
    public void setUp() {
        // set up test environment - don't actually launch app
        init();

        // we're just testing that the activity is starting
        intending(anyIntent()).respondWith(
                new Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        );
    }

    @After
    public void tearDown() {
        release(); // dismantle test environment
    }

    @Test
    public void clickingViewEvents_sendsIntentTo_AdminAllEventsActivity() {
        // click on the view events button
        onView(withId(R.id.btn_view_events)).perform(click());

        // assert that an intent was sent to the class we want
        intended(hasComponent(AdminAllEventsActivity.class.getName()));
    }

    // same logic for this test
    @Test
    public void clickingViewProfiles_sendsIntentTo_AdminAllUsersActivity() {
        onView(withId(R.id.btn_view_profiles)).perform(click());
        intended(hasComponent(AdminAllUsersActivity.class.getName()));
    }
}
