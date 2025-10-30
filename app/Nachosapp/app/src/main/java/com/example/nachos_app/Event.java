package com.example.nachos_app;

import java.util.Date;

public class Event {
    private String organizerId; // Device ID from Firebase Auth
    private String organizerName;
    private String eventName;
    private String description;
    private String dateTimeRange; // e.g., "Dec 10-20"
    private Date registrationStartDate;
    private Date registrationEndDate;
    private Integer maxParticipants; // null = unlimited
    private String bannerUrl;
    private String qrCodeUrl;
    private String qrCodeData; // The actual data encoded in QR (event link)
    private Date createdAt;
    private int currentWaitlistCount; // Track how many people joined

    public Event() {} // Required for Firestore

    public Event(String organizerId, String organizerName, String eventName,
                 String description, String dateTimeRange, Date registrationStartDate,
                 Date registrationEndDate, Integer maxParticipants, String bannerUrl,
                 String qrCodeUrl, String qrCodeData, Date createdAt) {
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

    // Check if waitlist is full
    public boolean isWaitlistFull() {
        return maxParticipants != null && currentWaitlistCount >= maxParticipants;
    }

    // Check if registration is open
    public boolean isRegistrationOpen() {
        Date now = new Date();
        return now.before(registrationEndDate);
    }
}