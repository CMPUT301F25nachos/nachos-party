package com.example.nachos_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<String> userIds;
    private List<Map<String, Object>> userDataList;
    private SimpleDateFormat dateFormat;
    private String displayMode; // "waitlist", "selected", "enrolled", "cancelled", "profile"

    public UserAdapter(Context context, String displayMode) {
        this.context = context;
        this.displayMode = displayMode;
        this.userIds = new ArrayList<>();
        this.userDataList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    }

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

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String uid = userIds.get(position);
        Map<String, Object> data = userDataList.get(position);

        // Display UID
        holder.nameTextView.setText("User: " + uid.substring(0, Math.min(8, uid.length())));

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
    }

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

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.userNameTextView);
            detailTextView = itemView.findViewById(R.id.userDetailTextView);
            statusTextView = itemView.findViewById(R.id.userStatusTextView);
        }
    }
}