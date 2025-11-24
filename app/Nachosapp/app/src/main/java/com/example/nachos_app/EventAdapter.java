package com.example.nachos_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying event items in a list.
 * Shows event details including name, date range, banner image, and registration status.
 * Handles click events to navigate to event details.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private List<String> eventIdList;

    public EventAdapter(Context context) {
        this.context = context;
        this.eventList = new ArrayList<>();
        this.eventIdList = new ArrayList<>();
    }

    /**
     * Updates the adapter with new event data.
     * @param events List of Event objects to display
     * @param eventIds Corresponding list of event IDs
     */
    public void setEvents(List<Event> events, List<String> eventIds) {
        this.eventList = events;
        this.eventIdList = eventIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder.
     * Sets event name, date range, registration status, and banner image.
     * Dims items with closed or upcoming registration.
     * @param holder The ViewHolder to bind data to
     * @param position Position in the data list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        String eventId = eventIdList.get(position);

        holder.eventNameTextView.setText(event.getEventName());

        String organizerName = event.getOrganizerName();
        if (organizerName != null && !organizerName.trim().isEmpty()) {
            holder.organizerTextView.setText("Organized by " + organizerName);
        } else {
            holder.organizerTextView.setText("Organized by Unknown");
        }

        // Display event date if available, otherwise show "Date TBA"
        Date eventDate = event.getEventDate();
        if (eventDate != null) {
            // Show the event date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            holder.dateTimeTextView.setText("Event date: " + dateFormat.format(eventDate));
            holder.dateTimeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.dateTimeTextView.setText("Event date: TBA");
            holder.dateTimeTextView.setVisibility(View.VISIBLE);
        }

        boolean registrationOpen = event.isRegistrationOpen();

        // Dim the item if registration is not open
        holder.itemView.setAlpha(registrationOpen ? 1f : 0.4f);

        // Display remaining spots
        int remainingSpots = event.getRemainingSpots();
        if (remainingSpots == -1) {
            // Unlimited spots available
            holder.spotsTextView.setText("Unlimited spots!");
            holder.spotsTextView.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else if (remainingSpots > 0) {
            // Some spots remaining
            holder.spotsTextView.setText(remainingSpots + " spots remaining!");
            holder.spotsTextView.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else {
            // Waitlist is full
            holder.spotsTextView.setText("Waitlist full");
            holder.spotsTextView.setTextColor(Color.parseColor("#C62828")); // Red
        }

        // Show when registration closes
        SimpleDateFormat regDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String closeDate = regDateFormat.format(event.getRegistrationEndDate());
        holder.registrationTextView.setText("Registration closes on " + closeDate);

        // Load event banner image
        ImageUtils.loadBase64Image(holder.bannerImageView, event.getBannerUrl(),
                R.drawable.ic_camera_placeholder);

        // Set click listener to open event details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", eventId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImageView;
        TextView eventNameTextView;
        TextView organizerTextView;
        TextView dateTimeTextView;
        TextView spotsTextView;
        TextView registrationTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.eventBannerImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            organizerTextView = itemView.findViewById(R.id.eventOrganizerTextView);
            dateTimeTextView = itemView.findViewById(R.id.eventDateTextView);
            spotsTextView = itemView.findViewById(R.id.eventSpotsTextView);
            registrationTextView = itemView.findViewById(R.id.eventRegistrationTextView);
        }
    }
}