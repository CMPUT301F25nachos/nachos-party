package com.example.nachos_app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView adapter for displaying user/entrant lists.
 * Supports different display modes: waitlist, selected, enrolled, cancelled, profile.
 * Shows user ID, relevant timestamp, and status information based on mode.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<String> userIds;
    private List<Map<String, Object>> userDataList;
    private SimpleDateFormat dateFormat;
    private String displayMode; // "waitlist", "selected", "enrolled", "cancelled", "profile"
    private String eventId;
    public UserAdapter(Context context, String displayMode, String eventId) {
        this.context = context;
        this.displayMode = displayMode;
        this.eventId = eventId;
        this.userIds = new ArrayList<>();
        this.userDataList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    }

    /**
     * Updates the adapter with new user data.
     * @param ids List of user IDs
     * @param dataList List of user data maps containing timestamps and status
     */
    public void setUsers(List<String> ids, List<Map<String, Object>> dataList) {
        this.userIds = ids;
        this.userDataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds user data to the ViewHolder.
     * Displays user ID (truncated), timestamp based on display mode, and status if applicable.
     * @param holder The ViewHolder to bind data to
     * @param position Position in the data list
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String uid = userIds.get(position);
        Map<String, Object> data = userDataList.get(position);

        // Prefer displaying the entrant's name; fall back to a shortened UID
        holder.nameTextView.setText(getDisplayName(uid, data));

        // Display timestamp based on mode
        String timestampLabel = getTimestampLabel();
        if (data.containsKey(timestampLabel)) {
            Object timestampObj = data.get(timestampLabel);
            if (timestampObj instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) timestampObj;
                String formattedDate = dateFormat.format(timestamp.toDate());
                holder.detailTextView.setText(getDetailText(formattedDate));
            }
        } else {
            holder.detailTextView.setText(getDetailText("Unknown"));
        }

        // Show cancel button only for selected and enrolled lists
        if (displayMode.equals("selected") || displayMode.equals("enrolled")) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> cancelEntrant(uid, eventId, displayMode));
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }
    }

    /**
     * Initiates the cancellation process for an entrant.
     * Determines cancellation reason based on current display mode:
     * - "manually_cancelled" for selected list
     * - "dropped_out" for enrolled list
     * Shows confirmation dialog before proceeding.
     * @param uid User ID of the entrant to cancel
     * @param eventId ID of the event
     * @param displayMode Current list type ("selected" or "enrolled")
     */
    private void cancelEntrant(String uid, String eventId, String displayMode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Determine the reason based on where they're being cancelled from
        String reason;
        if (displayMode.equals("selected")) {
            reason = "manually_cancelled";
        } else if (displayMode.equals("enrolled")) {
            reason = "dropped_out";
        } else {
            Toast.makeText(context, "Invalid cancellation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(context)
                .setTitle("Confirm Cancellation")
                .setMessage("Are you sure you want to cancel this entrant?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    performCancellation(uid, eventId, displayMode, reason);
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Performs the actual cancellation by moving entrant to cancelled collection.
     * Uses Firestore batch to:
     * 1. Create document in cancelled collection with reason and timestamps
     * 2. Delete document from source collection (selected or enrolled)
     * Preserves all historical timestamps (joinedAt, selectedAt, enrolledAt).
     * Sets replacementFilled to false to allow drawing replacements.
     * Deletes selection notification if cancelling from selected list.
     * @param uid User ID of the entrant
     * @param eventId ID of the event
     * @param displayMode Source collection type
     * @param reason Cancellation reason
     */
    private void performCancellation(String uid, String eventId, String displayMode, String reason) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference sourceRef = db.collection("events")
                .document(eventId)
                .collection(displayMode)
                .document(uid);

        sourceRef.get().addOnSuccessListener(sourceSnapshot -> {
            if (!sourceSnapshot.exists()) {
                Toast.makeText(context, "Entrant not found", Toast.LENGTH_SHORT).show();
                return;
            }

            WriteBatch batch = db.batch();

            Map<String, Object> cancelledData = new HashMap<>();
            cancelledData.put("uid", uid);
            cancelledData.put("cancelledAt", FieldValue.serverTimestamp());
            cancelledData.put("reason", reason);
            cancelledData.put("replacementFilled", false);  // Track if slot is filled

            // Preserve historical data
            if (sourceSnapshot.contains("joinedAt")) {
                cancelledData.put("joinedAt", sourceSnapshot.get("joinedAt"));
            }
            if (sourceSnapshot.contains("selectedAt")) {
                cancelledData.put("selectedAt", sourceSnapshot.get("selectedAt"));
            }
            if (sourceSnapshot.contains("enrolledAt")) {
                cancelledData.put("enrolledAt", sourceSnapshot.get("enrolledAt"));
            }

            DocumentReference cancelledRef = db.collection("events")
                    .document(eventId)
                    .collection("cancelled")
                    .document(uid);
            batch.set(cancelledRef, cancelledData);

            batch.delete(sourceRef);

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        int index = userIds.indexOf(uid);
                        if (index != -1) {
                            userIds.remove(index);
                            userDataList.remove(index);
                            notifyItemRemoved(index);
                        }

                        if (displayMode.equals("selected")) {
                            deleteSelectionNotification(uid, eventId);
                        }

                        Toast.makeText(context, "Entrant cancelled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to cancel entrant: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Deletes the selection notification for a user when they are cancelled.
     * Queries and deletes notifications with type "selected" for this event.
     * @param uid User ID
     * @param eventId Event ID
     */
    private void deleteSelectionNotification(String uid, String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "selected")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("CancelEntrant", "Failed to delete notification: " + e.getMessage())
                );
    }

    /**
     * Returns the display name for a user, preferring their actual name over user ID.
     * Falls back to truncated user ID if name is not available.
     * @param uid The user ID
     * @param data User data map containing potential "name" field
     * @return Display name string
     */
    private String getDisplayName(String uid, Map<String, Object> data) {
        Object nameObj = data.get("name");
        if (nameObj instanceof String) {
            String name = ((String) nameObj).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        return "User: " + uid.substring(0, Math.min(8, uid.length()));
    }

    /**
     * Determines which timestamp field to use based on display mode.
     * @return The field name for the timestamp (e.g., "joinedAt", "selectedAt")
     */
    private String getTimestampLabel() {
        switch (displayMode) {
            case "waitlist":
                return "joinedAt";
            case "selected":
                return "selectedAt";
            case "enrolled":
                return "enrolledAt";
            case "cancelled":
                return "cancelledAt";
            default:
                return "joinedAt";
        }
    }

    /**
     * Formats the detail text with appropriate label based on display mode.
     * @param date The formatted date string
     * @return Formatted detail text (e.g., "Joined: Dec 25, 2024")
     */
    private String getDetailText(String date) {
        switch (displayMode) {
            case "waitlist":
                return "Joined: " + date;
            case "selected":
                return "Selected: " + date;
            case "enrolled":
                return "Enrolled: " + date;
            case "cancelled":
                return "Cancelled: " + date;
            default:
                return date;
        }
    }

    /**
     * Capitalizes the first letter of a string.
     * @param str The string to capitalize
     * @return String with first letter capitalized, or original if null/empty
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public int getItemCount() {
        return userIds.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView detailTextView;
        TextView statusTextView;
        Button cancelButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.userNameTextView);
            detailTextView = itemView.findViewById(R.id.userDetailTextView);
            statusTextView = itemView.findViewById(R.id.userStatusTextView);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
