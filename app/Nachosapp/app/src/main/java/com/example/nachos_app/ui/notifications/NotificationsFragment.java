package com.example.nachos_app.ui.notifications;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nachos_app.Notification;
import com.example.nachos_app.NotificationAdapter;
import com.example.nachos_app.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Refactored Fragment for displaying the notifications of a current user
 * Data fetching is injectable for unit testing
 */
public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationAdapter adapter = new NotificationAdapter(null, notificationList);
    

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration listener;

    public NotificationsFragment() {
        // default constructor
    }

    // Getter for unit tests
    public List<Notification> getNotificationList() {
        return notificationList;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firebase if repository not injected

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        RecyclerView recyclerView = binding.recyclerNotifications;
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        // Fetch notifications
        fetchNotifications();

        return root;
    }

    /**
     * Fetch notifications from repository
     */
    @SuppressLint("NotifyDataSetChanged")
    private void fetchNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No logged-in user.", Toast.LENGTH_SHORT).show();
            return;
        }

        listener = db.collection("users")
                .document(currentUser.getUid())
                .collection("notifications")
                .orderBy("sendTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        Toast.makeText(getContext(), "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notificationList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Notification notif = doc.toObject(Notification.class);
                        if (notif != null) {
                            notif.setId(doc.getId()); // store document ID for deletion
                            notificationList.add(notif);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.remove(); // stop Firestore listener
        binding = null;
    }
    // For test injection
    public void setAdapter(NotificationAdapter adapter) {
        this.adapter = adapter;
    }

    public void setNotificationList(List<Notification> list) {
        this.notificationList = list;
    }
}
