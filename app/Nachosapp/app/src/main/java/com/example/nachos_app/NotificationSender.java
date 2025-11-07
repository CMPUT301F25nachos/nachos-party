package com.example.nachos_app;

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
    private String eventName;
    private FirebaseFirestore db;

    NotificationSender(String eventId, String eventName, FirebaseFirestore db) {
        this.db = db;
        this.eventId = eventId;
        this.eventName = eventName;
    }

    /**
     * Writes win/loss notifications to winners/the rest of the entrants respectively
     * @param winners List of documents containing UIDs of selected winners
     * @param waitlist List of documents containing UIDs of waiting list
     */
    public void sendSelectionNotifications(List<DocumentSnapshot> winners, List<DocumentSnapshot> waitlist) {
        WriteBatch batch = db.batch();
        ArrayList<String> losers;
        
        for (DocumentSnapshot doc : winners) {
            String uid = doc.getString("uid");

            // Package notification
            Map<String, Object> winnerNotif = new HashMap<>();
            winnerNotif.put("uid", uid);
            winnerNotif.put("eventId", eventId);
            winnerNotif.put("sendTime", new Date());
            winnerNotif.put("type", "lotteryWon");
            winnerNotif.put("message", "You have been selected for " + eventName + ". Tap to confirm or decline your spot.");

            // Write winning data to db
            batch.set(db.collection("users")
                        .document(uid)
                        .collection("notifications")
                        .document(), winnerNotif);
        }

        // Get list of all entrants, subtract winners to get losers
        losers = generateLosers(winners, waitlist);

        for (String uid : losers) {
            // Package notification
            Map<String, Object> loserNotif = new HashMap<>();
            loserNotif.put("uid", uid);
            loserNotif.put("eventId", eventId);
            loserNotif.put("sendTime", new Date());
            loserNotif.put("type", "lotteryLost");
            loserNotif.put("message", "You were not selected for " + eventName + ".");

            // Write winning data to db
            batch.set(db.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .document(), loserNotif);
        }
        batch.commit();
    }

    // Get list of all entrants, subtract winners to get losers
    private ArrayList<String> generateLosers(List<DocumentSnapshot> winners, List<DocumentSnapshot> waitlist) {
        ArrayList<String> listWinners = new ArrayList<>();
        ArrayList<String> listWaiting = new ArrayList<>();

        for (DocumentSnapshot doc : waitlist) {
            String uid = doc.getString("uid");
            listWaiting.add(uid);
        }

        for (DocumentSnapshot doc : winners) {
            String uid = doc.getString("uid");
            listWinners.add(uid);
        }

        listWaiting.removeAll(listWinners);
        return listWaiting;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
