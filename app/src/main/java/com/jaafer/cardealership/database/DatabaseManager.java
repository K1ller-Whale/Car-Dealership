package com.jaafer.cardealership.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.jaafer.cardealership.utils.SecurityUtils;
import java.util.UUID;

import com.jaafer.cardealership.models.Car;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final DatabaseHelper dbHelper;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        dbHelper.getWritableDatabase();
    }

    public List<Car> getAllCars() {
        List<Car> carList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM cars WHERE is_sold = 'N'", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("car_id"));
                String make = cursor.getString(cursor.getColumnIndexOrThrow("manufacturer"));
                String model = cursor.getString(cursor.getColumnIndexOrThrow("model_name"));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow("car_year"));
                String color = cursor.getString(cursor.getColumnIndexOrThrow("color"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("selling_price"));
                int mileage = cursor.getInt(cursor.getColumnIndexOrThrow("mileage"));
                String trans = cursor.getString(cursor.getColumnIndexOrThrow("transmission_type"));
                String cond = cursor.getString(cursor.getColumnIndexOrThrow("car_condition"));
                String vin = cursor.getString(cursor.getColumnIndexOrThrow("vin_number"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                String img = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));
                int fav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));

                carList.add(new Car(id, make, model, year, color, price, mileage, trans, cond, vin, notes, img, fav));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return carList;
    }


    public boolean checkUserCredentials(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = SecurityUtils.hashPassword(password);

        // Query: Check if username exists and password matches hash
        String query = "SELECT * FROM system_users WHERE username = ? AND password_hash = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, hashedPassword});

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM system_users WHERE username = ?", new String[]{username});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues customerValues = new ContentValues();
            customerValues.put("customer_name", username); // Use username as name for now
            customerValues.put("national_id", UUID.randomUUID().toString().substring(0, 15)); // Random ID
            customerValues.put("phone_number", "0000000000");

            long customerId = db.insertOrThrow("customers", null, customerValues);

            // 2. Create User record linked to that employee
            ContentValues userValues = new ContentValues();
            userValues.put("username", username);
            userValues.put("password_hash", SecurityUtils.hashPassword(password));
            userValues.put("customer_id", customerId);
            userValues.put("is_active", "Y");

            db.insertOrThrow("system_users", null, userValues);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }
    public void insertDummyData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT count(*) FROM cars", null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            ContentValues values = new ContentValues();
            values.put("manufacturer", "BMW");
            values.put("model_name", "X5");
            values.put("car_year", 2022);
            values.put("color", "Black");
            values.put("engine_capacity", 3.0);
            values.put("transmission_type", "Automatic");
            values.put("car_condition", "Used");
            values.put("selling_price", 55000);
            values.put("mileage", 12000);
            values.put("vin_number", "123ABC456DEF789GH");
            values.put("is_sold", "N");
            values.put("notes", "Excellent condition, one previous owner.");
            values.put("image_uri", "https://upload.wikimedia.org/wikipedia/commons/1/1d/BMW_X5_%28G05%29_IMG_3659.jpg");
            db.insert("cars", null, values);

            values.clear();
            values.put("manufacturer", "Toyota");
            values.put("model_name", "Camry");
            values.put("car_year", 2024);
            values.put("color", "White");
            values.put("engine_capacity", 2.5);
            values.put("transmission_type", "CVT");
            values.put("car_condition", "New");
            values.put("selling_price", 28000);
            values.put("mileage", 0);
            values.put("vin_number", "987ZYX654CBA321");
            values.put("is_sold", "N");
            values.put("notes", "Brand new, zero meter.");
            values.put("image_uri", "https://upload.wikimedia.org/wikipedia/commons/a/ac/2018_Toyota_Camry_%28ASV70R%29_Ascent_sedan_%282018-08-27%29_01.jpg");
            db.insert("cars", null, values);
        }
        c.close();
    }
}