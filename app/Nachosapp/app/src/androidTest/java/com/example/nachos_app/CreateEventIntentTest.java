package com.example.nachos_app;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Intent tests for CreateEventActivity.
 * Tests user stories: US 02.01.01, US 02.01.04, US 02.03.01, US 02.04.01
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventIntentTest {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);

    /**
     * US 02.01.01 & US 02.01.04: Test that all required event creation fields are present.
     * Verifies organizers can input event name, description, and set registration period.
     */
    @Test
    public void testEventCreationFieldsPresent() {
        onView(withId(R.id.eventNameEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.descriptionEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.registrationStartTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.registrationEndTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.createEventButton)).check(matches(isDisplayed()));
    }

    /**
     * US 02.01.01: Test basic event information input.
     */
    @Test
    public void testEnterEventInformation() {
        onView(withId(R.id.eventNameEditText))
                .perform(typeText("Swimming Lessons"), closeSoftKeyboard());
        onView(withId(R.id.descriptionEditText))
                .perform(typeText("Kids swimming lessons"), closeSoftKeyboard());

        onView(withId(R.id.eventNameEditText)).check(matches(withText("Swimming Lessons")));
        onView(withId(R.id.descriptionEditText)).check(matches(withText("Kids swimming lessons")));
    }

    /**
     * US 02.03.01: Test optional waitlist limit field.
     * Verifies that max participants can be left empty (unlimited) or set to a number.
     */
    @Test
    public void testOptionalWaitlistLimit() {
        onView(withId(R.id.maxParticipantsEditText)).check(matches(isDisplayed()));

        // Test it can be left empty
        onView(withId(R.id.maxParticipantsEditText)).check(matches(withText("")));

        // Test it can accept a number
        onView(withId(R.id.maxParticipantsEditText))
                .perform(typeText("20"), closeSoftKeyboard());
        onView(withId(R.id.maxParticipantsEditText)).check(matches(withText("20")));
    }

    /**
     * US 02.04.01: Test event poster upload button is present and clickable.
     * Banner preview should be hidden initially and only show after image selection.
     */
    @Test
    public void testPosterUploadButton() {
        onView(withId(R.id.uploadBannerButton)).check(matches(isDisplayed()));
        onView(withId(R.id.uploadBannerButton)).check(matches(isClickable()));

        // Banner preview exists but should be hidden initially
        onView(withId(R.id.bannerPreviewImageView))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * US 02.01.04: Test registration period date pickers are accessible.
     */
    @Test
    public void testRegistrationPeriodDatePickers() {
        onView(withId(R.id.registrationStartTextView)).check(matches(isClickable()));
        onView(withId(R.id.registrationEndTextView)).check(matches(isClickable()));
    }

    /**
     * Test validation: Empty event name should prevent submission.
     */
    @Test
    public void testValidationEmptyName() {
        onView(withId(R.id.descriptionEditText))
                .perform(typeText("Description"), closeSoftKeyboard());
        onView(withId(R.id.createEventButton)).perform(click());

        // Should still be on create screen
        onView(withId(R.id.eventNameEditText)).check(matches(isDisplayed()));
    }

    /**
     * Test validation: Zero or negative max participants should be rejected.
     */
    @Test
    public void testValidationInvalidMaxParticipants() {
        onView(withId(R.id.eventNameEditText))
                .perform(typeText("Test Event"), closeSoftKeyboard());
        onView(withId(R.id.descriptionEditText))
                .perform(typeText("Description"), closeSoftKeyboard());
        onView(withId(R.id.maxParticipantsEditText))
                .perform(typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.createEventButton)).perform(click());

        // Should still be on create screen
        onView(withId(R.id.createEventButton)).check(matches(isDisplayed()));
    }
}