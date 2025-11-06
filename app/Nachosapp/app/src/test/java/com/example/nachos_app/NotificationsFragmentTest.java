package com.example.nachos_app;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.example.nachos_app.Notification;
import com.example.nachos_app.NotificationAdapter;
import com.example.nachos_app.NotificationRepository;
import com.example.nachos_app.ui.notifications.NotificationsFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for NotificationsFragment using mocks
 */
public class NotificationsFragmentTest {

    private NotificationsFragment fragment;
    private DatabaseReference mockRef;
    private NotificationAdapter mockAdapter;

    @Before
    public void setup() {
        fragment = new NotificationsFragment();

        // Mock Firebase DatabaseReference
        mockRef = mock(DatabaseReference.class);

        // Inject a mock adapter
        mockAdapter = mock(NotificationAdapter.class);
        fragment.setAdapter(mockAdapter);

        // Initialize the notification list
        fragment.setNotificationList(new java.util.ArrayList<>());

        // Inject repository with mockRef
        fragment.setRepository(new NotificationRepository(mockRef));
    }

    @Test
    public void fetchNotifications_populatesList() {
        // Capture the ValueEventListener passed to Firebase
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        doNothing().when(mockRef).addListenerForSingleValueEvent(listenerCaptor.capture());

        // Call fetchNotifications
        fragment.fetchNotifications();

        // Mock DataSnapshots
        DataSnapshot snapshot = mock(DataSnapshot.class);
        DataSnapshot child1 = mock(DataSnapshot.class);
        DataSnapshot child2 = mock(DataSnapshot.class);

        Notification n1 = new Notification("1", "event1", "Message 1", "lotteryWon", "Nov 5");
        Notification n2 = new Notification("2", "event2", "Message 2", "waitlisted", "Nov 4");

        when(snapshot.getChildren()).thenReturn(Arrays.asList(child1, child2));
        when(child1.getValue(Notification.class)).thenReturn(n1);
        when(child2.getValue(Notification.class)).thenReturn(n2);

        // Trigger Firebase callback
        listenerCaptor.getValue().onDataChange(snapshot);

        // Verify list contents
        List<Notification> notifications = fragment.getNotificationList();
        assertEquals(2, notifications.size());
        assertEquals("Message 1", notifications.get(0).getMessage());
        assertEquals("Message 2", notifications.get(1).getMessage());

        // Verify adapter notified
        verify(mockAdapter).notifyDataSetChanged();
    }

    @Test
    public void fetchNotifications_emptyList() {
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        doNothing().when(mockRef).addListenerForSingleValueEvent(listenerCaptor.capture());

        fragment.fetchNotifications();

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.getChildren()).thenReturn(Arrays.asList());

        listenerCaptor.getValue().onDataChange(snapshot);

        List<Notification> notifications = fragment.getNotificationList();
        assertEquals(0, notifications.size());

        verify(mockAdapter).notifyDataSetChanged();
    }
}
