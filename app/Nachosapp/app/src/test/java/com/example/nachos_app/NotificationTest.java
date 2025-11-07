package com.example.nachos_app;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;


public class NotificationTest {

    @Test
    public void testNotificationGettersSetters() {
        Date now = new Date();
        Notification notif = new Notification();
        notif.setUid("123");
        notif.setEventId("eventA");
        notif.setMessage("Hello");
        notif.setType("lotteryWon");
        notif.setSendTime(now);
        assertEquals("123", notif.getUid());
        assertEquals("eventA", notif.getEventId());
        assertEquals("Hello", notif.getMessage());
        assertEquals("lotteryWon", notif.getType());
        assertEquals(now, notif.getSendTime());
    }

    @Test
    public void testNotificationConstructor() {
        Date now = new Date();
        Notification notif = new Notification("456", "eventB", "Hi there", "info", now);

        assertEquals("456", notif.getUid());
        assertEquals("eventB", notif.getEventId());
        assertEquals("Hi there", notif.getMessage());
        assertEquals("info", notif.getType());
        assertEquals(now, notif.getSendTime());
    }
}

