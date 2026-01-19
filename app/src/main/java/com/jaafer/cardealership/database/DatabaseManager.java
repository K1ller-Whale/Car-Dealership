package com.jaafer.cardealership.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jaafer.cardealership.models.Car;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final DatabaseHelper dbHelper;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        // Use this to trigger onCreate if DB doesn't exist
        SQLiteDatabase db = dbHelper.getWritableDatabase();
    }

    // Method to get all cars for the list
    public List<Car> getAllCars() {
        List<Car> carList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Select only unsold cars
        Cursor cursor = db.rawQuery("SELECT * FROM cars WHERE is_sold = 'N'", null);

        if (cursor.moveToFirst()) {
            do {
                // Map DB columns to Object
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("car_id"));
                String make = cursor.getString(cursor.getColumnIndexOrThrow("manufacturer"));
                String model = cursor.getString(cursor.getColumnIndexOrThrow("model_name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("selling_price"));
                String cond = cursor.getString(cursor.getColumnIndexOrThrow("car_condition"));
                String img = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));
                int fav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));

                carList.add(new Car(id, make, model, price, cond, img, fav));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return carList;
    }

    // Run this once to fill DB with data
    public void insertDummyData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Check if empty first
        Cursor c = db.rawQuery("SELECT count(*) FROM cars", null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            // Add a car
            ContentValues values = new ContentValues();
            values.put("manufacturer", "BMW");
            values.put("model_name", "X5");
            values.put("car_year", 2022);
            values.put("color", "Black");
            values.put("car_condition", "Used");
            values.put("selling_price", 55000);
            values.put("is_sold", "N");
            values.put("image_uri", "https://upload.wikimedia.org/wikipedia/commons/1/1d/BMW_X5_%28G05%29_IMG_3659.jpg");
            db.insert("cars", null, values);

            // Add another car... (repeat for 4-5 cars)
        }
        c.close();
    }
}