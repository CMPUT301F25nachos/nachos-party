package com.example.nachos_app;

import org.junit.Before;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Unit tests for the User model class.
 * Tests user profile data and notification preferences.
 * User Stories Tested:
 * - US 01.02.01: User profile information
 * - US 01.04.03: Notification preferences
 */
public class UserTest {

    private User user;
    private Date testDate;

    @Before
    public void setUp() {
        testDate = new Date();
        user = new User("John Doe", "john@example.com", "123-456-7890", testDate);
    }

    @Test
    public void testUserConstructor() {
        assertNotNull("User should not be null", user);
        assertEquals("Name should match", "John Doe", user.getName());
        assertEquals("Email should match", "john@example.com", user.getEmail());
        assertEquals("Phone should match", "123-456-7890", user.getPhoneNumber());
        assertEquals("Notification preference should default to yes", "yes", user.getNotificationPreference());
    }

    @Test
    public void testDefaultConstructor() {
        User emptyUser = new User();
        assertNotNull("Empty user should not be null", emptyUser);
    }

    @Test
    public void testSettersAndGetters() {
        user.setName("Jane Smith");
        assertEquals("Name should be updated", "Jane Smith", user.getName());

        user.setEmail("jane@example.com");
        assertEquals("Email should be updated", "jane@example.com", user.getEmail());

        user.setPhoneNumber("098-765-4321");
        assertEquals("Phone should be updated", "098-765-4321", user.getPhoneNumber());
    }

    @Test
    public void testNotificationPreference_OptOut() {
        user.setNotificationPreference("no");
        assertEquals("Notification preference should be no", "no", user.getNotificationPreference());
    }

    @Test
    public void testNotificationPreference_OptIn() {
        user.setNotificationPreference("no");
        user.setNotificationPreference("yes");
        assertEquals("Notification preference should be yes", "yes", user.getNotificationPreference());
    }

    @Test
    public void testNullPhoneNumber() {
        User userWithoutPhone = new User("Test User", "test@example.com", null, testDate);
        assertNull("Phone number should be null", userWithoutPhone.getPhoneNumber());
    }

    @Test
    public void testCreatedAtTimestamp() {
        assertEquals("Created at timestamp should match", testDate, user.getCreatedAt());

        Date newDate = new Date();
        user.setCreatedAt(newDate);
        assertEquals("Updated timestamp should match", newDate, user.getCreatedAt());
    }
}