package com.example.nachos_app;
import org.junit.Test;
import static org.junit.Assert.*;
public class NotificationTest {

    @Test
    public void testNotificationGettersSetters() {
        Notification notif = new Notification();
        notif.setUid("123");
        notif.setEventId("eventA");
        notif.setMessage("Hello");
        notif.setType("lotteryWon");
        notif.setSendTime("Nov 5, 2025");

        assertEquals("123", notif.getUid());
        assertEquals("eventA", notif.getEventId());
        assertEquals("Hello", notif.getMessage());
        assertEquals("lotteryWon", notif.getType());
        assertEquals("Nov 5, 2025", notif.getSendTime());
    }

    @Test
    public void testNotificationConstructor() {
        Notification notif = new Notification("456", "eventB", "Hi there", "info", "Nov 6, 2025");

        assertEquals("456", notif.getUid());
        assertEquals("eventB", notif.getEventId());
        assertEquals("Hi there", notif.getMessage());
        assertEquals("info", notif.getType());
        assertEquals("Nov 6, 2025", notif.getSendTime());
    }
}

