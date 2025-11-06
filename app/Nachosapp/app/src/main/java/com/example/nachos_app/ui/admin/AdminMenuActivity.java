package com.example.nachos_app.ui.admin;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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

        // temporary toast for upcoming features
        findViewById(R.id.btn_view_images).setOnClickListener(v ->
                Toast.makeText(this, "Images view coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_view_logs).setOnClickListener(v ->
                Toast.makeText(this, "Logs view coming soon", Toast.LENGTH_SHORT).show());

        // exit admin mode button
        findViewById(R.id.btn_exit_admin).setOnClickListener(v -> {
            AdminSession.disable(this);
            Toast.makeText(this, R.string.admin_mode_exited, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
