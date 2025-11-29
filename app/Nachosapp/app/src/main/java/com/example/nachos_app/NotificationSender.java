package com.example.nachos_app;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationSender responsible for sending win and loss notifications after the lottery
 * is drawn.
 */
public class NotificationSender {

    private String eventId;
    private String eventName;
    private FirebaseFirestore db;
    private List<String> uids;
    private String type;

    /**
     * Creates a new notification sender to use in sending notifications
     * @param eventId The ID of the event performing a lottery draw
     * @param eventName Name of event that corresponds with eventId
     * @param db The connection to the firebase database
     */
    public NotificationSender(String eventId, String eventName, FirebaseFirestore db) {
        this.db = db;
        this.eventId = eventId;
        this.eventName = eventName;
    }

    /**
     * Writes win/loss notifications to winners/the rest of the entrants respectively.
     * Checks user's notification preferences before sending.
     * @param winners List of documents containing UIDs of selected winners
     * @param waitlist List of documents containing UIDs of waiting list
     */
    public void sendSelectionNotifications(List<DocumentSnapshot> winners, List<DocumentSnapshot> waitlist) {
        ArrayList<String> winnerUids = new ArrayList<>();
        for (DocumentSnapshot doc : winners) {
            String uid = doc.getString("uid");
            if (uid != null) winnerUids.add(uid);
        }
        
        ArrayList<String> loserUids = generateLosers(winners, waitlist);
        
        // Process Winners
        for (String uid : winnerUids) {
            db.collection("users").document(uid).get().addOnSuccessListener(userSnap -> {
                if (userSnap.exists()) {
                    String pref = userSnap.getString("notificationPreference");
                    // Default to "yes" if missing
                    if (pref == null || "yes".equalsIgnoreCase(pref)) {
                        Map<String, Object> notif = new HashMap<>();
                        notif.put("uid", uid);
                        notif.put("eventId", eventId);
                        notif.put("sendTime", new Date());
                        notif.put("type", "lotteryWon");
                        notif.put("message", "You have been selected for " + eventName + ". Tap to confirm or decline your spot.");
                        
                        db.collection("users").document(uid).collection("notifications").add(notif);
                    }
                }
            });
        }
        
        // Process Losers
        for (String uid : loserUids) {
            db.collection("users").document(uid).get().addOnSuccessListener(userSnap -> {
                if (userSnap.exists()) {
                    String pref = userSnap.getString("notificationPreference");
                    // Default to "yes" if missing
                    if (pref == null || "yes".equalsIgnoreCase(pref)) {
                        Map<String, Object> notif = new HashMap<>();
                        notif.put("uid", uid);
                        notif.put("eventId", eventId);
                        notif.put("sendTime", new Date());
                        notif.put("type", "lotteryLost");
                        notif.put("message", "You were not selected for " + eventName + ".");
                        
                        db.collection("users").document(uid).collection("notifications").add(notif);
                    }
                }
            });
        }
    }

    // Get list of all entrants, subtract winners to get losers
    private ArrayList<String> generateLosers(List<DocumentSnapshot> winners, List<DocumentSnapshot> waitlist) {
        ArrayList<String> listWinners = new ArrayList<>();
        ArrayList<String> listWaiting = new ArrayList<>();

        for (DocumentSnapshot doc : waitlist) {
            String uid = doc.getString("uid");
            if (uid != null) listWaiting.add(uid);
        }

        for (DocumentSnapshot doc : winners) {
            String uid = doc.getString("uid");
            if (uid != null) listWinners.add(uid);
        }

        listWaiting.removeAll(listWinners);
        return listWaiting;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setUserIds(List<String> uids) {
        this.uids = uids;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Sends notifications to the selected, cancelled, or waiting lists
    // Does not send notifications to users who have opted out of them

    /**
     * US 02.07.01, US 02.07.02, US 02.07.03
     * Sends notifications to the selected, cancelled or waiting lists.
     * Before calling this method one needs to call setUserIds and setType on their NotificationSender
     * Respects notification preferences defined by the user
     * @param message Custom text to send to the user
     */
    public void sendListNotifications(String message) {
        if (uids == null && type == null) return;
        for (String uid : uids) {
            db.collection("users").document(uid).get().addOnSuccessListener(userSnap -> {
                if (userSnap.exists()) {
                    String pref = userSnap.getString("notificationPreference");
                    // Checks user notification preferences before sending notif
                    if (pref == null || "yes".equalsIgnoreCase(pref)) {
                        Map<String, Object> notif = new HashMap<>();
                        notif.put("uid", uid);
                        notif.put("eventId", eventId);
                        notif.put("sendTime", new Date());
                        notif.put("type", type);
                        notif.put("message", eventName + ": " + message);

                        db.collection("users").document(uid).collection("notifications").add(notif);
                    }
                }
            });
        }
    }

}
