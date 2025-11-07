package com.example.nachos_app;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class NotificationTest {

    @Test
    public void testNotificationGettersSetters() {
        Date now = new Date();
        Notification notif = new Notification();
        Date timestamp = new Date(1730784000000L); // Nov 5, 2024 UTC

        notif.setUid("123");
        notif.setEventId("eventA");
        notif.setMessage("Hello");
        notif.setType("lotteryWon");
        notif.setSendTime(timestamp);

        assertEquals("123", notif.getUid());
        assertEquals("eventA", notif.getEventId());
        assertEquals("Hello", notif.getMessage());
        assertEquals("lotteryWon", notif.getType());
        assertEquals(timestamp, notif.getSendTime());
    }

    @Test
    public void testNotificationConstructor() {
        Date timestamp = new Date(1730870400000L); // Nov 6, 2024 UTC
        Notification notif = new Notification("456", "eventB", "Hi there", "info", timestamp);

        assertEquals("456", notif.getUid());
        assertEquals("eventB", notif.getEventId());
        assertEquals("Hi there", notif.getMessage());
        assertEquals("info", notif.getType());
        assertEquals(timestamp, notif.getSendTime());
    }
}

