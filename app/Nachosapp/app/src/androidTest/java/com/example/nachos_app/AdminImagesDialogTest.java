package com.example.nachos_app;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.nachos_app.ui.admin.AdminEventImagesActivity;
import com.example.nachos_app.ui.admin.AdminEventImagesAdapter;

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
 * Tests the delete-image confirmation dialog in AdminEventImagesActivity
 *  1. Launch AdminEventImagesActivity.
 *  2. call showRemoveImageDialog (with a fake event)
 *  3. assert that the dialog shows
 *  4. Click the remove button
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImagesDialogTest {

    @Rule
    public ActivityScenarioRule<AdminEventImagesActivity> rule =
            new ActivityScenarioRule<>(AdminEventImagesActivity.class);

    @Test
    public void removeDialog_shows_andRemoveButtonClickable() {

        // trigger dialog with fake event data so we don't have to upload an image every
        // time we run tests
        rule.getScenario().onActivity(activity -> {
            AdminEventImagesAdapter.Row row = new AdminEventImagesAdapter.Row();
            row.eventId = null;
            row.eventName = "Test Event";
            row.bannerBase64 = "";

            try {
                // call the method
                Method m = AdminEventImagesActivity.class.getDeclaredMethod(
                        "showRemoveImageDialog",
                        AdminEventImagesAdapter.Row.class,
                        int.class
                );
                m.setAccessible(true);
                m.invoke(activity, row, 0);
            } catch (Exception e) {
                throw new RuntimeException("Cannot access method", e);
            }
        });

        // check that the dialog is showing
        onView(withText(R.string.admin_remove_image_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // click the remove button
        onView(withText(R.string.admin_remove_image_confirm))
                .inRoot(isDialog())
                .perform(click());


    }
}
