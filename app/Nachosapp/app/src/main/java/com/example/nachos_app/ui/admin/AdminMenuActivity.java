package com.example.nachos_app.ui.admin;



import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import com.example.nachos_app.R;


/**
 * Main Admin Menu screen
 * <p>
 * Shows buttons to navigate to different parts of the project
 * includes exit admin mode button to return
 * </p>
 *
 * @author Darius
 */
public class AdminMenuActivity extends AppCompatActivity {


    /**
     * Called when the activity is created. Sets up window inserts and
     * wires the main menu buttons
     *
     * @param savedInstanceState state bundle
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // go to all events
        findViewById(R.id.btn_view_events).setOnClickListener(v ->
                startActivity(new Intent(this, AdminAllEventsActivity.class)));

        // go to all profiles
        findViewById(R.id.btn_view_profiles).setOnClickListener(v ->
                startActivity(new Intent(this, AdminAllUsersActivity.class)));

        // go to all images
        findViewById(R.id.btn_view_images).setOnClickListener(v ->
                startActivity(new Intent(this, AdminEventImagesActivity.class)));

        // go to all logs
        findViewById(R.id.btn_view_logs).setOnClickListener(v ->
                startActivity(new Intent(this, AdminLogsActivity.class)));

        // return to previous screen
        findViewById(R.id.btn_exit_admin).setOnClickListener(v -> finish());
    }
}
