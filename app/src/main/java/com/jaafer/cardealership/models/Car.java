package com.jaafer.cardealership.models;

public class Car {
    private int id;
    private String manufacturer;
    private String model;
    private double price;
    private String condition; // "New" or "Used"
    private String imageUri; // URL or File path
    private int isFavorite; // 1 = true, 0 = false

    // Constructor
    public Car(int id, String manufacturer, String model, double price, String condition, String imageUri, int isFavorite) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.price = price;
        this.condition = condition;
        this.imageUri = imageUri;
        this.isFavorite = isFavorite;
    }

    // Getters
    public int getId() { return id; }
    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public double getPrice() { return price; }
    public String getCondition() { return condition; }
    public String getImageUri() { return imageUri; }
    public boolean isFavorite() { return isFavorite == 1; }
}