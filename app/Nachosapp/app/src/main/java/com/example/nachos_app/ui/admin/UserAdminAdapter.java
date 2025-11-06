package com.example.nachos_app.ui.admin;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.R;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;



/**
 * RecyclerView adapter for the all user admin list
 * <p>
 * Binds row with name, email and created at timestamp
 * Will be implementing further at a later date
 * </p>
 *
 * @author Darius
 */
public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.VH> {

    /** Simple row model to hold data */
    public static class UserRow {
        public String id;
        public String name;
        public String email;
        public Timestamp createdAt;
    }

    private final List<UserRow> data = new ArrayList<>();

    /**
     * Replaces the list contents and refreshes the view
     *
     * @param rows new rows to display
     */

    public void setData(List<UserRow> rows) {
        data.clear();
        data.addAll(rows);
        notifyDataSetChanged();
    }


    /**
     * ViewHolder for a user row
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView email;
        final TextView created;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            email = itemView.findViewById(R.id.tv_email);
            created = itemView.findViewById(R.id.tv_created);
        }
    }

    /**
     * Inflates the item view
     *
     * @param parent parent view group
     * @param viewType not used
     * @return a new vh
     */

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_admin, parent, false);
        return new VH(v);
    }


    /**
     * Binds a row at a given position.
     *
     * @param h   view holder
     * @param position adapter position
     */

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        // get data
        UserRow r = data.get(position);

        // set details, if name is empty, use placeholder
        h.name.setText((r.name != null && !r.name.isEmpty()) ? r.name : "(no name)");
        h.email.setText(r.email != null ? r.email : "");
        String createdStr = (r.createdAt != null)
                ? DateFormat.getDateTimeInstance().format(r.createdAt.toDate())
                : "";
        h.created.setText(createdStr);
    }


    /**
     * @return the number of rows in the adapter
     */
    @Override
    public int getItemCount() { return data.size(); }
}
