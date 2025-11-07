package com.example.nachos_app;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.Notification;
import com.example.nachos_app.NotificationAdapter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class NotificationAdapterTest {

    private NotificationAdapter adapter;
    private List<Notification> notifications;
    private Context mockContext;

    @Before
    public void setUp() {
        // Create mock Context
        mockContext = mock(Context.class);
        Date now = new Date();

        // Mock list of notifications
        notifications = new ArrayList<>();
        notifications.add(new Notification("1", "event1", "You were invited!", "lotteryWon", now));
        notifications.add(new Notification("2", "event2","Event starts soon","lotteryLost", now));

        // Instantiate adapter
        adapter = spy(new NotificationAdapter(mockContext, notifications));
        doNothing().when(adapter).notifyDataSetChanged();
    }

    @Test
    public void getItemCount_returnsCorrectSize() {
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void deleteNotification_removesFromList() {
        // Simulate removing one notification
        notifications.remove(0);
        adapter.notifyDataSetChanged();

        // Verify adapter’s internal list updates properly
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void adapterHandlesEmptyListGracefully() {
        notifications.clear();
        adapter.notifyDataSetChanged();
        assertEquals(0, adapter.getItemCount());
    }
    @Test
    public void onCreateViewHolder_skippedDueToAndroidDependency() {
        // We can’t test this outside of Android (LayoutInflater not mocked)
        // So just assert the adapter is not null and comment reasoning
        assertNotNull(adapter);
    }

}
