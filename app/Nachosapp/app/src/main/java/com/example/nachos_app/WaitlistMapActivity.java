package com.example.nachos_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Creates an activity where the users on the waitlist are displayed on a google map api
 * @author sampickett
 * version 1
 */
public class WaitlistMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String eventId;
    private FirebaseFirestore db;

    /**
     * Initializes the activity, sets up the layout, configures the action bar,
     * retrieves the event ID passed via Intent, and attaches a Google Map fragment.
     *
     * @param savedInstanceState saved instance state bundle, or null if the
     *                           activity is being created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_map);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Entrant Locations");
        }

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        // Create the map fragment manually
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mapFragment, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);
    }

    /**
     * Callback triggered when the Google Map is fully initialized and ready.
     * Stores the map reference and begins loading entrant locations from Firestore.
     *
     * @param googleMap the initialized {@link GoogleMap} instance
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        loadEntrantLocations();
    }

    /**
     * Loads the geographic locations of all entrants in the event waitlist from Firestore.
     * <p>For each entrant:
     * <ul>
     *   <li>Reads the latitude and longitude from the waitlist document</li>
     *   <li>Fetches the corresponding user's name from the "users" collection</li>
     *   <li>Adds a marker to the map if a valid location exists</li>
     * </ul>
     *
     * <p>If at least one entrant has a location, the camera centers on the first one.
     * Otherwise, it falls back to a default location (Edmonton coordinates).
     */
    private void loadEntrantLocations() {
        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean hasAtLeastOneLocation = false;

                    for (var doc : snapshot) {

                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String uid = doc.getId();
                        // Skip entrants without location
                        if (lat == null || lng == null)
                            continue;

                        hasAtLeastOneLocation = true;

                        LatLng pos = new LatLng(lat, lng);

                        db.collection("users")
                                .document(uid)
                                .get()
                                        .addOnSuccessListener(userSnap -> {
                                            String userName = userSnap.getString("name");
                                            if (userName == null){
                                                userName = "Unknown User";
                                            }

                                        mMap.addMarker(new MarkerOptions()
                                                        .position(pos)
                                                        .title(userName));
                                        });

                    }

                    if (hasAtLeastOneLocation) {
                        for (var doc : snapshot) {
                            Double firstLat = doc.getDouble("latitude");
                            Double firstLng = doc.getDouble("longitude");
                            if (firstLat != null && firstLng != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(firstLat, firstLng), 11
                                ));
                                break;
                            }
                        }
                    } else {

                        // No markers → center on default view
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.5461, -113.4938), 9));
                    }
                })
                .addOnFailureListener(err -> {
                    // Optionally toast but avoid breaking UI
                });
    }
    /**
     * Handles the action bar "Up" button press by closing the activity
     * and returning to the previous screen.
     *
     * @return always true to indicate the event was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // closes the activity and returns to previous screen
        return true;
    }

}
