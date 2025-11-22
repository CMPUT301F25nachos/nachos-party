package com.example.nachos_app;

import java.util.Date;

/**
 * Model class representing an event in the lottery system.
 * This class stores all event-related information including registration periods,
 * participant limits, and QR code data.
 */
public class Event {
    private String organizerId; // Device ID from Firebase Auth
    private String organizerName;
    private String eventName;
    private String description;
    private String dateTimeRange; // e.g., "Dec 10-20"
    private Date registrationStartDate;
    private Date registrationEndDate;
    private Integer maxParticipants; // null = unlimited
    private String eventLocation; // optional
    private String bannerUrl; // Base64 encoded image
    private String qrCodeUrl; // Base64 encoded QR code image
    private String qrCodeData; // The actual data encoded in QR (event link)
    private Date createdAt;
    private int currentWaitlistCount; // Track how many people joined

    public Event() {} // Required for Firestore

    /**
     * Constructs a new Event with all required fields.
     *
     * @param organizerId ID of the user who created the event
     * @param organizerName name of the organizer
     * @param eventName name/title of the event
     * @param description description of the event
     * @param dateTimeRange date range (e.g., "Dec 10-20")
     * @param registrationStartDate registration opens
     * @param registrationEndDate registration closes
     * @param maxParticipants Maximum waitlist capacity (null for unlimited)
     * @param bannerUrl Base64 encoded event poster image
     * @param qrCodeUrl Base64 encoded QR code image
     * @param qrCodeData The data encoded in the QR code (event:// URL)
     * @param createdAt Timestamp when the event was created
     * @param eventLocation optional event's location
     */
    public Event(String organizerId, String organizerName, String eventName,
                 String description, String dateTimeRange, Date registrationStartDate,
                 Date registrationEndDate, Integer maxParticipants, String bannerUrl,
                 String qrCodeUrl, String qrCodeData, Date createdAt, String eventLocation) {
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.eventName = eventName;
        this.description = description;
        this.dateTimeRange = dateTimeRange;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.maxParticipants = maxParticipants;
        this.bannerUrl = bannerUrl;
        this.qrCodeUrl = qrCodeUrl;
        this.qrCodeData = qrCodeData;
        this.createdAt = createdAt;
        this.currentWaitlistCount = 0;
        this.eventLocation = eventLocation;
    }

    public String getOrganizerId() { return organizerId; }
    public String getOrganizerName() { return organizerName; }
    public String getEventName() { return eventName; }
    public String getDescription() { return description; }
    public String getDateTimeRange() { return dateTimeRange; }
    public Date getRegistrationStartDate() { return registrationStartDate; }
    public Date getRegistrationEndDate() { return registrationEndDate; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public String getBannerUrl() { return bannerUrl; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public String getQrCodeData() { return qrCodeData; }
    public Date getCreatedAt() { return createdAt; }
    public int getCurrentWaitlistCount() { return currentWaitlistCount; }
    public String getEventLocation() { return eventLocation; }

    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setDescription(String description) { this.description = description; }
    public void setDateTimeRange(String dateTimeRange) { this.dateTimeRange = dateTimeRange; }
    public void setRegistrationStartDate(Date registrationStartDate) { this.registrationStartDate = registrationStartDate; }
    public void setRegistrationEndDate(Date registrationEndDate) { this.registrationEndDate = registrationEndDate; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setCurrentWaitlistCount(int currentWaitlistCount) { this.currentWaitlistCount = currentWaitlistCount; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    /**
     * Checks if the waitlist has reached its maximum capacity.
     * @return true if waitlist is full, false otherwise (or if unlimited)
     */
    public boolean isWaitlistFull() {
        return maxParticipants != null && currentWaitlistCount >= maxParticipants;
    }

    /**
     * Checks if registration is currently open.
     * @return true if current time is within registration period
     */
    public boolean isRegistrationOpen() {
        Date now = new Date();
        return now.after(registrationStartDate) && now.before(registrationEndDate);
    }

    /**
     * Checks if registration has not yet started.
     * @return true if registration start date is in the future
     */
    public boolean isRegistrationUpcoming() {
        Date now = new Date();
        return now.before(registrationStartDate);
    }
}