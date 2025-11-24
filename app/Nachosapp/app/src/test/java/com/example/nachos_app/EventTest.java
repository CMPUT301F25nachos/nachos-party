package com.example.nachos_app;

import org.junit.Before;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Unit tests for the Event model class.
 * Tests all business logic methods and field validations.
 * User Stories Tested:
 * - US 02.01.04: Registration period logic
 * - US 02.03.01: Waitlist capacity limits
 */
public class EventTest {

    private Event event;
    private Date pastDate;
    private Date futureDate;
    private Date currentDate;

    @Before
    public void setUp() {
        // Set up test dates
        Calendar cal = Calendar.getInstance();

        // Past date (yesterday)
        cal.add(Calendar.DAY_OF_MONTH, -1);
        pastDate = cal.getTime();

        // Current date
        cal = Calendar.getInstance();
        currentDate = cal.getTime();

        // Future date (tomorrow)
        cal.add(Calendar.DAY_OF_MONTH, 1);
        futureDate = cal.getTime();

        // Create test event
        event = new Event("organizer123", "Test Organizer",
                "Test Event", "Test Description", "Dec 10-20",
                pastDate, futureDate, null, // eventDate is null (optional)
                10, null, null,
                "event://test123", currentDate, "");
    }

    @Test
    public void testEventConstructor() {
        assertNotNull("Event should not be null", event);
        assertEquals("Event name should match", "Test Event", event.getEventName());
        assertEquals("Organizer ID should match", "organizer123", event.getOrganizerId());
        assertEquals("Max participants should match", Integer.valueOf(10), event.getMaxParticipants());
    }

    @Test
    public void testIsRegistrationOpen_WithinPeriod() {
        // Registration should be open (pastDate < now < futureDate)
        assertTrue("Registration should be open", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_BeforeStart() {
        // Set registration to start in the future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        Date dayAfter = cal.getTime();

        event.setRegistrationStartDate(tomorrow);
        event.setRegistrationEndDate(dayAfter);

        assertFalse("Registration should not be open before start", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationOpen_AfterEnd() {
        // Set registration to have ended
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        Date twoDaysAgo = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date threeDaysAgo = cal.getTime();

        event.setRegistrationStartDate(threeDaysAgo);
        event.setRegistrationEndDate(twoDaysAgo);

        assertFalse("Registration should not be open after end", event.isRegistrationOpen());
    }

    @Test
    public void testIsRegistrationUpcoming() {
        // Set registration to start in the future
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();

        event.setRegistrationStartDate(tomorrow);
        assertTrue("Registration should be upcoming", event.isRegistrationUpcoming());
    }

    @Test
    public void testIsWaitlistFull_WithLimit() {
        event.setMaxParticipants(5);
        event.setCurrentWaitlistCount(5);

        assertTrue("Waitlist should be full", event.isWaitlistFull());
    }

    @Test
    public void testIsWaitlistFull_NotFull() {
        event.setMaxParticipants(10);
        event.setCurrentWaitlistCount(5);

        assertFalse("Waitlist should not be full", event.isWaitlistFull());
    }

    @Test
    public void testIsWaitlistFull_Unlimited() {
        event.setMaxParticipants(null); // Unlimited
        event.setCurrentWaitlistCount(1000);

        assertFalse("Unlimited waitlist should never be full", event.isWaitlistFull());
    }

    @Test
    public void testIsWaitlistFull_ExceedsLimit() {
        event.setMaxParticipants(5);
        event.setCurrentWaitlistCount(10);

        assertTrue("Waitlist exceeding limit should be marked as full", event.isWaitlistFull());
    }

    @Test
    public void testSettersAndGetters() {
        event.setEventName("New Name");
        assertEquals("Event name should be updated", "New Name", event.getEventName());

        event.setDescription("New Description");
        assertEquals("Description should be updated", "New Description", event.getDescription());

        event.setOrganizerName("New Organizer");
        assertEquals("Organizer name should be updated", "New Organizer", event.getOrganizerName());

        event.setCurrentWaitlistCount(7);
        assertEquals("Waitlist count should be updated", 7, event.getCurrentWaitlistCount());
    }

    @Test
    public void testQRCodeData() {
        String qrData = "event://abc123";
        event.setQrCodeData(qrData);
        assertEquals("QR code data should match", qrData, event.getQrCodeData());
    }

    @Test
    public void testBannerUrl() {
        String bannerBase64 = "base64encodedstring";
        event.setBannerUrl(bannerBase64);
        assertEquals("Banner URL should match", bannerBase64, event.getBannerUrl());
    }

    @Test
    public void testGetRemainingSpots_WithLimit() {
        event.setMaxParticipants(10);
        event.setCurrentWaitlistCount(3);

        assertEquals("Should have 7 spots remaining", 7, event.getRemainingSpots());
    }

    @Test
    public void testGetRemainingSpots_Unlimited() {
        event.setMaxParticipants(null); // Unlimited
        event.setCurrentWaitlistCount(100);

        assertEquals("Unlimited should return -1", -1, event.getRemainingSpots());
    }

    @Test
    public void testGetRemainingSpots_Full() {
        event.setMaxParticipants(10);
        event.setCurrentWaitlistCount(10);

        assertEquals("Full waitlist should have 0 spots", 0, event.getRemainingSpots());
    }

    @Test
    public void testGetRemainingSpots_OverCapacity() {
        event.setMaxParticipants(10);
        event.setCurrentWaitlistCount(15);

        assertEquals("Over capacity should return 0", 0, event.getRemainingSpots());
    }
}