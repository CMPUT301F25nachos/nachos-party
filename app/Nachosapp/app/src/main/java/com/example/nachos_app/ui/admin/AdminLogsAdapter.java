package com.example.nachos_app.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
 *
 * <p>
 * This adapter shows a list of all notifications
 * </p>
 */
public class AdminLogsAdapter extends RecyclerView.Adapter<AdminLogsAdapter.VH> {

    /**
     * Row model for the logs list.
     */
    public static class Row {
        public String recipientName;
        public String type;
        public Date sendTime;
    }

    private final List<Row> rows = new ArrayList<>();
    private final Context context;

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
     * Creates a new ViewHolder instance by inflating the notification_card layout
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);
        return new VH(view);
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

        // set recipient name
        String name = (row.recipientName != null && !row.recipientName.trim().isEmpty())
                ? row.recipientName
                : "(unknown user)";
        holder.tvMessage.setText(name);

        // set the time of notification
        if (row.sendTime != null) {
            holder.tvTime.setText(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(row.sendTime)
            );
        } else {
            holder.tvTime.setText("");
        }


        // use the current notification type, if its null or empty just display general
        String displayType = (row.type != null && !row.type.trim().isEmpty())
                ? row.type
                : "general";
        holder.tvStatus.setText(displayType);

        // Since we're reusing the notification card
        // we need to hide all buttons
        holder.btnDelete.setVisibility(View.GONE);
        holder.btnDelete.setOnClickListener(null);

        holder.btnViewQueue.setVisibility(View.GONE);
        holder.btnViewQueue.setOnClickListener(null);

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
     * ViewHolder class for the logs list item.
     * <p>
     * It reuses the views from the existing notification card
     * </p>
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvMessage;
        final TextView tvTime;
        final TextView tvStatus;
        final ImageButton btnDelete;
        final Button btnViewQueue;

        /**
         * Constructs a new ViewHolder and binds its view references
         *
         * @param itemView the inflated notification_card layout
         */
        VH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            tvStatus = itemView.findViewById(R.id.tvNotificationStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnViewQueue = itemView.findViewById(R.id.btnViewQueue);
        }
    }
}
