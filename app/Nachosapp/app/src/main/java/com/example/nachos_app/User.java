package com.example.nachos_app;

import java.util.Date;

/**
 * Model class representing a user/entrant.
 * Stores user profile information and notification preferences.
 */
public class User {
    private String name;
    private String email;
    private String phoneNumber; // Optional field
    private Date createdAt;
    private String notificationPreference; // "yes" or "no"

    // Required empty constructor for Firestore
    public User() {}

    /**
     * Constructs a new User with the specified details.
     * Notification preference defaults to "yes".
     * @param name user's full name
     * @param email user's email address
     * @param phoneNumber user's phone number (optional, can be null)
     * @param createdAt Timestamp when the user profile was created
     */
    public User(String name, String email, String phoneNumber, Date createdAt) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.notificationPreference = "yes"; // Default to "yes" on creation
    }

    // Getters and setters for all fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(String notificationPreference) {
        this.notificationPreference = notificationPreference;
    }
}
