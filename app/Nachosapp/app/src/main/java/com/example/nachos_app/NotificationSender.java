package com.example.nachos_app;

import android.app.Notification;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationSender {

    private String eventId;
    private FirebaseFirestore db;

    NotificationSender(String eventId, FirebaseFirestore db) {
        this.db = db;
        this.eventId = eventId;
    }

    /**
     * Class that contains notification information. Used by NotificationSender to write
     * notifications to the database
     */
    private class Notification {
        // type can be "waitingList", or "selection"
        String notificationType;
        String notificationMessage;

        Notification(String type, String message) {
            notificationType =  type;
            notificationMessage = message;
        }
    }

    /**
     * Writes win/loss notifications to winners/the rest of the entrants respectively
     * @param winners List of UIDs of selected winners
     */
    public void sendSelectionNotifications(List<DocumentSnapshot> winners, List<DocumentSnapshot> waitlist) {
        WriteBatch batch = db.batch();
        ArrayList losers;

        for (DocumentSnapshot doc : winners) {
            String uid = doc.getString("uid");

            // Package notification
            Map<String, Object> winnerNotif = new HashMap<>();
            winnerNotif.put("uid", uid);
            winnerNotif.put("eventId", eventId);
            winnerNotif.put("sendTime", new Date());
            winnerNotif.put("type", "lotteryWon");
            winnerNotif.put("message", "You have won a lottery!");

            // Write winning data to db
            batch.set(db.collection("users")
                        .document(uid)
                        .collection("notifications")
                        .document(), winnerNotif);
        }

        // Get list of all entrants, subtract winners to get losers
        //losers = generateLosers(winners, waitlist);

        //TODO: send notifs to losers


        // Commit batch
        batch.commit();

    }

    // Get list of all entrants, subtract winners to get losers
    //private ArrayList<String> generateLosers(List<DocumentSnapshot> winners, List<DocumentSnapshot> waitlist) {}



}
