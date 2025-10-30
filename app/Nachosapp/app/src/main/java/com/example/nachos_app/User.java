package com.example.nachos_app;

import java.util.Date;

public class User {
    private String name;
    private String email;
    private String phoneNumber;
    private Date createdAt;

    public User() {} // for Firestore

    public User(String name, String email, String phoneNumber, Date createdAt) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Date getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}