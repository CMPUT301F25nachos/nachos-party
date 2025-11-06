package com.example.nachos_app;

/**
 * POJO which Stores the basic information of a notification
 * @author sampickett
 * @version 1.0
 */
public class Notification {
    private String uid;
    private String eventId;
    private String message;
    private String type;
    private String sendTime;

    /**
     * Default constructor of Notification
     */
    public Notification() {
    }

    /**
     * Constructor with all required fields
     * @param uid Notification ID
     * @param eventId Id of the associated event
     * @param message Message content of the notification
     * @param type Type of Notification (won, waitlisted, lost)
     * @param sendTime Timestamp of notification
     */
    public Notification(String uid, String eventId, String message, String type, String sendTime) {
        this.uid = uid;
        this.eventId = eventId;
        this.message = message;
        this.type = type;
        this.sendTime = sendTime;
    }

    /**
     * @return unique notification ID
     */
    public String getUid() {
        return uid;
    }

    /**
     * @return associated events ID
     */
    public String getEventId() {
        return eventId;
    }
    /**
     * @return message of the notification
     */
    public String getMessage() {
    return message;
    }

    /**
     * @return the classification of notification
     */
    public String getType(){
        return type;
    }
    /**
     * @return the timestamp of when the notification was sent
     */
    public String getSendTime(){
        return sendTime;
    }

    /**
     * Sets the ID of the notification
     * @param uid the notification ID
     */
    public void setUid(String uid){
        this.uid = uid;
    }

    /**
     * Sets the event ID in the notification
     * @param eventId the events ID
     */
    public void setEventId(String eventId){
        this.eventId = eventId;
    }

    /**
     * Sets the message in the notification
     * @param message The string message in the notification
     */
    public void setMessage(String message){
        this.message = message;
    }

    /**
     * sets the type of notification
     * @param type classification of notification (win, waitlisted, lose)
     */
    public void setType(String type){
        this.type = type;
    }

    /**
     * sets the timestamp of when the notification was sent
     * @param sendTime time of when the notification was sent
     */
    public void setSendTime(String sendTime){
        this.sendTime = sendTime;
    }
}

