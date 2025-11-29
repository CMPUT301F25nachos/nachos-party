package com.example.nachos_app;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.nachos_app.ui.admin.AdminAllUsersActivity;
import com.example.nachos_app.ui.admin.UserAdminAdapter;

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
 * Tests the delete-user confirmation dialog in AdminAllUsersActivity.
 *
 * This test:
 *  1. launches the all users activity
 *  2. calls the showRemoveUserDialog method
 *  3. check that the dialog is shown
 *  4. click the remove button
 *
 *  This test does not check firebase functionality
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminRemoveUserDialogTest {

    @Rule
    public ActivityScenarioRule<AdminAllUsersActivity> rule =
            new ActivityScenarioRule<>(AdminAllUsersActivity.class);

    @Test
    public void removeUserDialog_shows_andRemoveButtonClickable() {
        // trigger the dialog
        rule.getScenario().onActivity(activity -> {
            UserAdminAdapter.UserRow row = new UserAdminAdapter.UserRow();
            row.id = null;                // ensures removeUser(...) does nothing
            row.name = "Test User";
            row.email = "test@example.com";
            row.createdAt = null;

            try {
                // call the method
                Method m = AdminAllUsersActivity.class.getDeclaredMethod(
                        "showRemoveUserDialog",
                        UserAdminAdapter.UserRow.class,
                        int.class
                );
                m.setAccessible(true);
                m.invoke(activity, row, 0);
            } catch (Exception e) {
                throw new RuntimeException("Could not access method", e);
            }
        });

        // check the dialogs title
        onView(withText(R.string.admin_remove_user_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // click remove button
        onView(withText(R.string.admin_remove_user_confirm))
                .inRoot(isDialog())
                .perform(click());

    }
}
