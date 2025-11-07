package com.example.nachos_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.List;

/**
 * Adapter class to display the notifications in a Recycler View
 * Displays message, header, timestamp, and shows appropriate button
 *
 * @author sampickett
 * version 1.0
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private Context context;

    /**
     * Constructs the NotificationAdapter
     * @param context context for the adapter
     * @param notificationList list of notifications to display
     */

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    /**
     * Inflates the layout for a single notification item
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return NotificationViewHolder a new instance of the holder
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds notification data to ViewHolder
     * Sets appropriate colors for Type, and displays corresponding buttons
     * Holds On Click Listeners for buttons
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(notification.getSendTime())
        );

        holder.btnViewQueue.setOnClickListener(null);
        holder.btnViewQueue.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);

        //remove the notif from firebase
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            Notification item = notificationList.get(adapterPosition);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .document(item.getId()) // using the Firestore document ID
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Notification deleted", Toast.LENGTH_SHORT).show();
                        notificationList.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to delete notification", Toast.LENGTH_SHORT).show()
                    );
        });
        switch (notification.getType()) {
            case "lotteryWon":
                holder.tvStatus.setText("Invited!");
                holder.tvStatus.setTextColor(Color.GREEN);
                holder.btnViewQueue.setVisibility(View.GONE);

                holder.itemView.setClickable(true);
                holder.itemView.setOnClickListener(v -> {
                    int adapterPosition = holder.getBindingAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    Notification selected = notificationList.get(adapterPosition);
                    openEventDetails(selected);
                });
                break;
            case "waitlisted":
                holder.tvStatus.setText("Waitlisted");
                holder.tvStatus.setTextColor(Color.YELLOW);
                holder.btnViewQueue.setVisibility(View.VISIBLE);
                holder.btnViewQueue.setOnClickListener(v -> {
                    // TODO: implement queue viewing
                    Toast.makeText(context, "View Queue clicked", Toast.LENGTH_SHORT).show();
                    //toast just a placeholder, can remove
                });
                break;
            case "lotteryLost":
                holder.tvStatus.setText("Declined");
                holder.tvStatus.setTextColor(Color.RED);
                holder.btnViewQueue.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Returns the number of notifications in the list
     * @return num of notifications
     */
    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * removes the notification from the list in positoin
     * @param position index in the list of notifications
     */
    public void deleteNotification(int position) {
        notificationList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * View Holder class for holding a reference to the view in a notification
     */

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;
        ImageButton btnDelete;
        Button btnViewQueue;

        /**
         * Constructs a NotificationViewHolder
         * @param itemView the root view of the notification item
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            tvStatus = itemView.findViewById(R.id.tvNotificationStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnViewQueue = itemView.findViewById((R.id.btnViewQueue));
        }
    }

    private void openEventDetails(Notification notification) {
        String eventId = notification.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(context, "Event unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra("eventId", eventId);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
