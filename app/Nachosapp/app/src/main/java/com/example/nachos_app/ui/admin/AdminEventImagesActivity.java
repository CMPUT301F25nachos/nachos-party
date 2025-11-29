package com.example.nachos_app.ui.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.Event;
import com.example.nachos_app.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin-only screen that shows all uploaded pictures along with the name of the event
 */
public class AdminEventImagesActivity extends AppCompatActivity {

    private AdminEventImagesAdapter adapter;
    private FirebaseFirestore db;

    /**
     * Called when the activity is created
     * <p>
     * Sets up the layout and firebase and then triggers the load of event banners
     * </p>
     *
     * @param savedInstanceState saved state bundle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_images);

        // hide the action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        // set up the view and then displays the images
        RecyclerView rv = findViewById(R.id.rv_event_images);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        // dialog setup for removing images
        adapter = new AdminEventImagesAdapter((row, position) ->
                showRemoveImageDialog(row, position));

        rv.setAdapter(adapter);

        // load event images from firebase
        loadEventImages();
    }

    /**
     * Loads all events from firebase and gets the events with an image present
     */
    private void loadEventImages() {
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // list to hold the rows
                    List<AdminEventImagesAdapter.Row> rows = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {

                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        // get the banner
                        String banner = event.getBannerUrl();

                        // only get events where there is an image present
                        if (banner != null && !banner.isEmpty()) {
                            AdminEventImagesAdapter.Row row = new AdminEventImagesAdapter.Row();
                            row.eventId = doc.getId();
                            row.eventName = event.getEventName();
                            row.bannerBase64 = banner;
                            rows.add(row);
                        }
                    }

                    // update adapter
                    adapter.set(rows);

                    // if there aren't any images present, show a toast
                    if (rows.isEmpty()) {
                        Toast.makeText(this,
                                getString(R.string.admin_no_images_found),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.admin_load_images_fail) + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    /**
     * Shows a confirmation dialog before removing the banner from the event
     * <p>
     * If the admin confirms, removeBanner is called.
     * If they cancel, nothing happens.
     * </p>
     */
    private void showRemoveImageDialog(AdminEventImagesAdapter.Row row, int position) {
        if (row == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_remove_image_title)
                .setMessage(R.string.admin_remove_image_message)
                .setPositiveButton(R.string.admin_remove_image_confirm,
                        (dialog, which) -> removeBanner(row, position))
                .setNegativeButton(R.string.admin_remove_image_cancel, null)
                .show();
    }

    /**
     * Clears the banner image from the given event in Firestore and updates UI
     * <p>
     * This method sets the bannerUrl field of the chosen event to null.
     * When the update goes through it refreshes the adapter so the UI and
     * firebase are in sync
     * </p>
     */
    private void removeBanner(AdminEventImagesAdapter.Row row, int position) {
        if (row.eventId == null) {
            return;
        }

        // edit the bannerUrl field in firebase and display toast for confirmation
        db.collection("events")
                .document(row.eventId)
                .update("bannerUrl", null)
                .addOnSuccessListener(aVoid -> {
                    adapter.removeAt(position);
                    Toast.makeText(this,
                            getString(R.string.admin_remove_image_success),
                            Toast.LENGTH_SHORT).show();
                })
                // if this fails, let the admin know
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.admin_remove_image_fail) + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
