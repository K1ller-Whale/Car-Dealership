package com.jaafer.cardealership.models;

public class User {
    final private int id;
    final private String name;
    final private String nationalId;
    final private String phoneNumber;
    final private String occupation;

    public User(int id, String name, String nationalId, String phoneNumber,
                    String occupation) {
        this.id = id;
        this.name = name;
        this.nationalId = nationalId;
        this.phoneNumber = phoneNumber;
        this.occupation = occupation;
    }

    // Getters
    public int getId() { return id; }

    public String getName() { return name; }

    public String getNationalId() { return nationalId; }

    public String getPhoneNumber() { return phoneNumber; }

    public String getOccupation() { return occupation; }
}
