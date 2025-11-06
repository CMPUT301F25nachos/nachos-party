package com.example.nachos_app.ui.admin;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.R;

import java.util.ArrayList;
import java.util.List;



/**
 * RecyclerView adapter for the all events admin list.
 * <p>
 * Displays event name, date ranges and status
 * </p>
 *
 * @author Darius
 */
public class EventAdminAdapter extends RecyclerView.Adapter<EventAdminAdapter.VH> {

    /** row model for events. */
    public static class Row {
        public String id;
        public String name;
        public String dateTimeRange;
        public boolean registrationOpen;
        public boolean registrationUpcoming;
    }

    private final List<Row> data = new ArrayList<>();
    public void set(List<Row> rows) { data.clear(); data.addAll(rows); notifyDataSetChanged(); }


    /**
     * ViewHolder for an event row
     */

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView range;
        final TextView status;


        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            range = itemView.findViewById(R.id.tv_range);
            status = itemView.findViewById(R.id.tv_status);
        }
    }

    /**
     * Inflates the item view
     *
     * @param parent parent view group
     * @param viewType required but not used
     * @return a new VH
     */

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_admin, parent, false);
        return new VH(view);
    }



    /**
     * Binds a row at a given position
     *
     * @param h view holder
     * @param position adapter position
     */

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Row r = data.get(position);
        h.title.setText(r.name != null ? r.name : "(untitled)");
        h.range.setText(r.dateTimeRange != null ? r.dateTimeRange : "");
        String s = r.registrationOpen ? "Open" : (r.registrationUpcoming ? "Upcoming" : "Closed");
        h.status.setText(s);
    }


    /**
     * @return the number of rows in the adapter
     */

    @Override
    public int getItemCount() { return data.size(); }
}
