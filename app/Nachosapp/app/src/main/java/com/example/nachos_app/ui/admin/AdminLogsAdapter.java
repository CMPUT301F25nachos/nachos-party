package com.example.nachos_app.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RecyclerView adapter for the admin logs screen.
 * <p>
 * Each row shows:
 *  - Event name
 *  - Recipient name
 *  - Notification message content
 *  - Sender name
 *  - Time the notification was sent
 *  </p>
 */
public class AdminLogsAdapter extends RecyclerView.Adapter<AdminLogsAdapter.VH> {

    /**
     * Row model for the logs list.
     */
    public static class Row {
        public String eventName;
        public String recipientName;
        public String message;
        public String senderName;
        public Date sendTime;
    }
    private final List<Row> rows = new ArrayList<>();

    @SuppressWarnings("unused")
    private final Context context;

    /**
     * Creates a new adapter instance.
     *
     * @param context activity
     */
    public AdminLogsAdapter(Context context) {
        this.context = context;
    }

    /**
     * Replaces the adapter's data set with a new list of rows
     *
     * @param items list of rows to display
     */
    public void set(List<Row> items) {
        rows.clear();
        if (items != null) {
            rows.addAll(items);
        }
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder instance
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_log, parent, false);
        return new VH(v);
    }

    /**
     * Binds data from a row into a ViewHolder
     *
     * @param holder   the target ViewHolder
     * @param position adapter position of the row
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Row row = rows.get(position);

        // Event name
        String eventName = (row.eventName != null && !row.eventName.trim().isEmpty())
                ? row.eventName
                : "(unknown event)";
        holder.tvEventName.setText(eventName);

        // Recipient
        String recipient = (row.recipientName != null && !row.recipientName.trim().isEmpty())
                ? row.recipientName
                : "(unknown recipient)";
        holder.tvRecipient.setText("To: " + recipient);

        // Message content
        holder.tvMessage.setText(
                row.message != null && !row.message.trim().isEmpty()
                        ? row.message
                        : "(no message)"
        );

        // Sender
        String sender = (row.senderName != null && !row.senderName.trim().isEmpty())
                ? row.senderName
                : "(unknown organizer)";
        holder.tvSender.setText("Sender: " + sender);

        // Time
        if (row.sendTime != null) {
            holder.tvTime.setText(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(row.sendTime)
            );
        } else {
            holder.tvTime.setText("");
        }

        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);
    }

    /**
     * @return number of rows currently displayed by the adapter
     */
    @Override
    public int getItemCount() {
        return rows.size();
    }

    /**
     * ViewHolder for a log row.
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvEventName;
        final TextView tvRecipient;
        final TextView tvMessage;
        final TextView tvSender;
        final TextView tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_log_event_name);
            tvRecipient = itemView.findViewById(R.id.tv_log_recipient);
            tvMessage = itemView.findViewById(R.id.tv_log_message);
            tvSender = itemView.findViewById(R.id.tv_log_sender);
            tvTime = itemView.findViewById(R.id.tv_log_time);
        }
    }
}
