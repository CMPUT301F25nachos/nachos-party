package com.example.nachos_app.ui.admin;



import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;



/**
 * Admin screen displaying all user profiles from Firestore
 * <p>
 * Reads from the users collection and binds the related fields
 * </p>
 *
 */

public class AdminAllUsersActivity extends AppCompatActivity {
    private RecyclerView rv;
    private UserAdminAdapter adapter;
    private FirebaseFirestore db;


    /**
     * Called when the activity is created. Sets up RecyclerView and loads user data
     * from firebase
     *
     * @param savedInstanceState state bundle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_users);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // wire up back button
        View back = findViewById(R.id.btn_back);
        if (back != null){
            back.setOnClickListener(v -> finish());
        }

        // set up views/adapter
        rv = findViewById(R.id.rv_users);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdminAdapter((row, position) ->
                showRemoveUserDialog(row, position));
        rv.setAdapter(adapter);

        // load the user data
        db = FirebaseFirestore.getInstance();
        loadUsers();
    }


    /**
     * Loads all user documents ordered by creation time (descending)
     * and updates the RecyclerView adapter.
     */

    private void loadUsers() {
        db.collection("users")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<UserAdminAdapter.UserRow> rows = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        UserAdminAdapter.UserRow r = new UserAdminAdapter.UserRow();
                        r.id = d.getId();
                        r.name = d.getString("name");
                        r.email = d.getString("email");
                        r.createdAt = d.getTimestamp("createdAt");
                        rows.add(r);
                    }
                    adapter.setData(rows);
                })
                // just in case something goes wrong with firebase
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Show confirmation dialog before removing a user.
     */
    private void showRemoveUserDialog(UserAdminAdapter.UserRow row, int position) {
        if (row == null) return;

        String name = (row.name != null && !row.name.trim().isEmpty())
                ? row.name
                : "(no name)";

        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_remove_user_title)
                .setMessage(getString(R.string.admin_remove_user_message, name))
                .setPositiveButton(R.string.admin_remove_user_confirm,
                        (dialog, which) -> removeUser(row, position))
                .setNegativeButton(R.string.admin_remove_user_cancel, null)
                .show();
    }

    /**
     * Remove user and all events they may have created
     */
    private void removeUser(UserAdminAdapter.UserRow row, int position) {
        if (row == null || row.id == null) return;

        // find events created by user
        db.collection("events")
                .whereEqualTo("organizerId", row.id)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // delete those events
                    db.runBatch(batch -> {
                        for (DocumentSnapshot doc : querySnapshot) {
                            batch.delete(doc.getReference());
                        }
                    }).addOnSuccessListener(unusedBatch -> {

                        // delete the user
                        db.collection("users")
                                .document(row.id)
                                .delete()
                                .addOnSuccessListener(unusedUser -> {
                                    // Remove from adapter so it disappears from the list
                                    adapter.removeAt(position);
                                    Toast.makeText(this,
                                            getString(R.string.admin_remove_user_success),
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                getString(R.string.admin_remove_user_fail) + ": " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());

                    }).addOnFailureListener(e ->
                            Toast.makeText(this,
                                    getString(R.string.admin_remove_user_fail) + ": " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.admin_remove_user_fail) + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

}
