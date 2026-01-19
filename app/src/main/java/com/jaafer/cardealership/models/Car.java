package com.jaafer.cardealership.models;

public class Car {
    private int id;
    private String manufacturer;
    private String model;
    private int year; // Added
    private String color; // Added
    private double price;
    private int mileage; // Added
    private String transmission; // Added
    private String condition;
    private String vin; // Added
    private String description; // Map to 'notes'
    private String imageUri;
    private int isFavorite;

    public Car(int id, String manufacturer, String model, int year, String color,
               double price, int mileage, String transmission, String condition,
               String vin, String description, String imageUri, int isFavorite) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.year = year;
        this.color = color;
        this.price = price;
        this.mileage = mileage;
        this.transmission = transmission;
        this.condition = condition;
        this.vin = vin;
        this.description = description;
        this.imageUri = imageUri;
        this.isFavorite = isFavorite;
    }

    public int getId() {
        return id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public String getColor() {
        return color;
    }

    public double getPrice() {
        return price;
    }

    public int getMileage() {
        return mileage;
    }

    public String getTransmission() {
        return transmission;
    }

    public String getCondition() {
        return condition;
    }

    public String getVin() {
        return vin;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public boolean isFavorite() {
        return isFavorite == 1;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite ? 1 : 0;
    }
}