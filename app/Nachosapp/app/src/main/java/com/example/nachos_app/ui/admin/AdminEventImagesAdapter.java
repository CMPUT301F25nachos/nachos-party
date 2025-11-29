package com.example.nachos_app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.ImageUtils;
import com.example.nachos_app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the admin event images list to display the event
 * images in a grid view along with the event name
 *
 */
public class AdminEventImagesAdapter extends RecyclerView.Adapter<AdminEventImagesAdapter.VH> {

    /**
     * Data holder for a row in the images list
     */
    public static class Row {
        public String eventId;
        public String eventName;
        public String bannerBase64;
    }

    /**
     * Callback to notify when an image is clicked
     * <p>
     *     This adapter just notifies that an image is clicked.
     *     The logic is handled in the image activity
     * </p>
     */
    public interface OnImageClickListener {
        void onImageClicked(Row row, int position);
    }


    private final List<Row> data = new ArrayList<>();
    private final OnImageClickListener listener;

    public AdminEventImagesAdapter(OnImageClickListener listener) {
        this.listener = listener;
    }

    /**
     * Replaces the current list of rows with a new list and refreshes the UI
     * @param rows new list of rows to show
     */
    public void set(List<Row> rows) {
        data.clear();
        if (rows != null) {
            data.addAll(rows);
        }
        notifyDataSetChanged();
    }


    /**
     * Removes a row at a given position
     *
     * @param position adapter position to remove
     */
    public void removeAt(int position) {

        // make sure the position is valid before removing
        if (position < 0 || position >= data.size()) return;
        data.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event_image, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Row row = data.get(position);

        holder.title.setText(
                row.eventName != null && !row.eventName.trim().isEmpty()
                        ? row.eventName
                        : "(untitled event)"
        );

        ImageUtils.loadBase64Image(
                holder.bannerImage,
                row.bannerBase64,
                R.drawable.ic_camera_placeholder
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onImageClicked(data.get(adapterPos), adapterPos);
                }
            }
        });
    }

    /**
     * @return total number of rows in the adapter.
     */

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * ViewHolder for a banner item
     * <p>
     *     Each row contains an ImageView for the banner image
     *     and a TextView for the event name
     * </p>
     */
    static class VH extends RecyclerView.ViewHolder {
        final ImageView bannerImage;
        final TextView title;

        VH(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.image_banner);
            title = itemView.findViewById(R.id.tv_event_name);
        }
    }
}
