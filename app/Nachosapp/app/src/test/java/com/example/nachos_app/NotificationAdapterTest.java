package com.example.nachos_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class NotificationAdapterTest {

    private Application context;
    private RecyclerView recyclerView;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Test
    public void lotteryWinnerCard_isClickableAndLaunchesEventDetails() {
        Notification notification = buildNotification("event123", "lotteryWon");
        clearStartedActivities();

        NotificationAdapter.NotificationViewHolder holder = bind(notification);

        assertTrue(holder.itemView.isClickable());
        assertEquals(View.GONE, holder.btnViewQueue.getVisibility());

        holder.itemView.performClick();

        ShadowApplication shadowApplication = Shadows.shadowOf(context);
        Intent startedIntent = shadowApplication.getNextStartedActivity();
        assertNotNull("EventDetailsActivity should be launched", startedIntent);
        assertEquals(EventDetailsActivity.class.getName(),
                startedIntent.getComponent().getClassName());
        assertEquals("event123", startedIntent.getStringExtra("eventId"));
    }

    @Test
    public void waitlistedNotification_showsQueueButtonAndDoesNotLaunchEvent() {
        Notification notification = buildNotification("event456", "waitlisted");
        clearStartedActivities();

        NotificationAdapter.NotificationViewHolder holder = bind(notification);

        assertEquals(View.VISIBLE, holder.btnViewQueue.getVisibility());
        assertFalse("Card tap should remain disabled for waitlisted users", holder.itemView.isClickable());

        holder.itemView.performClick();

        ShadowApplication shadowApplication = Shadows.shadowOf(context);
        Intent startedIntent = shadowApplication.getNextStartedActivity();
        assertNull("Non-winning notifications should not trigger navigation", startedIntent);
    }

    @Test
    public void lotteryLostNotification_hidesActionsAndPreventsClicks() {
        Notification notification = buildNotification("event789", "lotteryLost");
        clearStartedActivities();

        NotificationAdapter.NotificationViewHolder holder = bind(notification);

        assertEquals(View.GONE, holder.btnViewQueue.getVisibility());
        assertFalse(holder.itemView.isClickable());
    }

    private NotificationAdapter.NotificationViewHolder bind(Notification notification) {
        List<Notification> singleItem = new ArrayList<>();
        singleItem.add(notification);

        NotificationAdapter adapter = new NotificationAdapter(context, singleItem);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        layoutRecyclerView();

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull("ViewHolder should not be null", holder);
        return (NotificationAdapter.NotificationViewHolder) holder;
    }

    private void layoutRecyclerView() {
        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        );
        recyclerView.layout(0, 0, 1080, 1920);
    }

    private void clearStartedActivities() {
        ShadowApplication shadowApplication = Shadows.shadowOf(context);
        while (shadowApplication.getNextStartedActivity() != null) {
            // Drain any previously started intents so assertions are deterministic.
        }
    }

    private Notification buildNotification(String eventId, String type) {
        Notification notification = new Notification();
        notification.setUid("notification-" + type);
        notification.setEventId(eventId);
        notification.setType(type);
        notification.setMessage("Sample message");
        notification.setSendTime(new Date());
        return notification;
    }
}
