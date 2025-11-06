package com.example.nachos_app.ui.notifications;

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
import com.example.nachos_app.NotificationRepository;
import com.example.nachos_app.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private DatabaseReference userRef;

    // Optional repository injection for testing
    private NotificationRepository repository;

    public NotificationsFragment() {
        // default constructor
    }

    // Setter for injecting repository (for tests)
    public void setRepository(NotificationRepository repository) {
        this.repository = repository;
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
        if (repository == null) {
            mAuth = FirebaseAuth.getInstance();
            String currentUserId = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUserId)
                    .child("notifications");
            repository = new NotificationRepository(userRef);
        }

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
    public void fetchNotifications() {
        repository.fetchNotifications(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                notificationList.clear();
                for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                    Notification notif = data.getValue(Notification.class);
                    if (notif != null) {
                        notificationList.add(notif);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load notifications.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
