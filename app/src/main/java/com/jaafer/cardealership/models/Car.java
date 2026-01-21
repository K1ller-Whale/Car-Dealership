package com.jaafer.cardealership.models;

public class Car {
    private int id;
    private String manufacturer;
    private String model;
    private int year;
    private String color;
    private double price;
    private int mileage;
    private String transmission;
    private String condition;
    private String vin;
    private String description;
    private String imageUri;
    private int isFavorite;
    private boolean isSold;

    public Car(int id, String manufacturer, String model, int year, String color,
               double price, int mileage, String transmission, String condition,
               String vin, String description, String imageUri, int isFavorite, boolean isSold) {
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
        this.isSold = isSold;
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

    public boolean isSold() {
        return isSold;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite ? 1 : 0;
    }
}