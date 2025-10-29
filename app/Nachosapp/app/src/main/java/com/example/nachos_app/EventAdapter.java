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

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private List<String> eventIdList;

    public EventAdapter(Context context) {
        this.context = context;
        this.eventList = new ArrayList<>();
        this.eventIdList = new ArrayList<>();
    }

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

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        String eventId = eventIdList.get(position);

        holder.eventNameTextView.setText(event.getEventName());
        holder.dateTimeTextView.setText(event.getDateTimeRange());

        // Display spots info
        if (event.getMaxParticipants() != null) {
            int spotsLeft = event.getMaxParticipants() - event.getCurrentWaitlistCount();
            holder.spotsTextView.setText(spotsLeft + " spots");
        } else {
            holder.spotsTextView.setText("Unlimited spots");
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