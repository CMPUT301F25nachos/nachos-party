package com.example.nachos_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

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

        // Show status if available (for selected list)
        if (displayMode.equals("selected") && data.containsKey("status")) {
            String status = (String) data.get("status");
            holder.statusTextView.setVisibility(View.VISIBLE);
            holder.statusTextView.setText("Status: " + capitalizeFirst(status));
        } else {
            holder.statusTextView.setVisibility(View.GONE);
        }

        //adds cancel button for any selected, waitlisted
        if (displayMode.equals("selected") || displayMode.equals("waitlist")){
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> cancelEntrant(uid, eventId));
        }
        else{
            holder.cancelButton.setVisibility(View.GONE);
        }
    }

    private void cancelEntrant(String uid, String eventId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Remove from current list
        db.collection("events")
                .document(eventId)
                .collection(displayMode) // must be "waitlist" or "selected"
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {

                    int index = userIds.indexOf(uid);
                    if (index != -1) {
                        userIds.remove(index);
                        userDataList.remove(index);
                        notifyItemRemoved(index);
                    }
                    // Add to cancelled list
                    Map<String, Object> cancelledData = new HashMap<>();
                    cancelledData.put("uid", uid);
                    cancelledData.put("cancelledAt", FieldValue.serverTimestamp());
                    cancelledData.put("reason", "Manually Cancelled");


                    db.collection("events")
                            .document(eventId)
                            .collection("cancelled")
                            .document(uid)
                            .set(cancelledData)
                            .addOnSuccessListener(done ->
                                Toast.makeText(context, "Entrant cancelled", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Failed to cancel entrant: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to remove entrant: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

    }

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
     * @return String with first letter capitalized
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
