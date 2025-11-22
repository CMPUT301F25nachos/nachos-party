package com.example.nachos_app;

import android.content.Context;
import android.content.Intent;
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
        holder.dateTimeTextView.setText(event.getDateTimeRange());

        boolean registrationOpen = event.isRegistrationOpen();
        boolean registrationUpcoming = event.isRegistrationUpcoming();
        DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        // Dim the item if registration is not open
        holder.itemView.setAlpha(registrationOpen ? 1f : 0.4f);

        // Set spots/status text based on registration period
        if (registrationOpen) {
            // Registration is currently open
            String closeDate = dateFormat.format(event.getRegistrationEndDate());
            holder.spotsTextView.setText("Registration closes on " + closeDate);
        } else if (registrationUpcoming) {
            // Registration hasn't started yet
            String startDate = dateFormat.format(event.getRegistrationStartDate());
            holder.spotsTextView.setText("Registration opens on " + startDate);
        } else {
            // Registration is closed
            holder.spotsTextView.setText("Registration closed");
        }

        // Load banner
        ImageUtils.loadBase64Image(holder.bannerImageView, event.getBannerUrl(), R.drawable.ic_camera_placeholder);

        // Click listener to view event details
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
        TextView dateTimeTextView;
        TextView spotsTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.eventBannerImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            dateTimeTextView = itemView.findViewById(R.id.eventDateTextView);
            spotsTextView = itemView.findViewById(R.id.eventSpotsTextView);
        }
    }
}