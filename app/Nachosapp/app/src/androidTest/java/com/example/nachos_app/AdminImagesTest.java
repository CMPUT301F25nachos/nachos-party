package com.example.nachos_app;




import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.nachos_app.ui.admin.AdminEventImagesActivity;

import com.example.nachos_app.ui.admin.AdminMenuActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


/**
 * Instrumentation test for the view images button
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImagesTest {

    /**
     * Launches the admin menu activity for the test below
     */
    @Rule
    public ActivityScenarioRule<AdminMenuActivity> adminMenuRule =
            new ActivityScenarioRule<>(AdminMenuActivity.class);

    @Before
    public void setUp() {
        // Initialize intents
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Clicking the view images button from the admin menu
     * should send and intent to the admin images activity
     */
    @Test
    public void clickingViewImages_sendsIntentTo_AdminEventImagesActivity() {

        // click on view images button
        onView(withId(R.id.btn_view_images)).perform(click());

        // assert that an intent was sent
        intended(hasComponent(AdminEventImagesActivity.class.getName()));
    }
}

