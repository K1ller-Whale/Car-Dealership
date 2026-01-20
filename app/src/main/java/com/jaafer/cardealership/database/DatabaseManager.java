package com.jaafer.cardealership.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jaafer.cardealership.models.Car;
import com.jaafer.cardealership.models.User;
import com.jaafer.cardealership.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final DatabaseHelper dbHelper;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        dbHelper.getWritableDatabase();
    }

    public User retrieveUserProfileInfo(int systemUserID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String query = "SELECT c.* FROM customers c " +
                "INNER JOIN system_users u ON c.customer_id = u.customer_id " +
                "WHERE u.user_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(systemUserID)});

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("customer_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
            String nationalId = cursor.getString(cursor.getColumnIndexOrThrow("national_id"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
            String occupation = cursor.getString(cursor.getColumnIndexOrThrow("occupation"));

            user = new User(id, name, nationalId, phone, occupation);
        }

        cursor.close();
        return user;
    }
    public int getCustomerIdFromUserId(int systemUserID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int customerId = -1;
        Cursor cursor = db.rawQuery("SELECT customer_id FROM system_users WHERE user_id = ?", new String[]{String.valueOf(systemUserID)});
        if (cursor.moveToFirst()) {
            customerId = cursor.getInt(0);
        }
        cursor.close();
        return customerId;
    }

    public boolean buyCar(int carID, int customerID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String priceQuery = "SELECT selling_price FROM cars WHERE car_id = ? AND is_sold = 'N'";
            Cursor cursor = db.rawQuery(priceQuery, new String[]{String.valueOf(carID)});
            if (!cursor.moveToFirst()) {
                cursor.close();
                return false;
            }
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("selling_price"));
            cursor.close();

            ContentValues carValues = new ContentValues();
            carValues.put("is_sold", "Y");
            String currentTimestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
            carValues.put("sold_date", currentTimestamp);

            int rowsAffected = db.update("cars", carValues, "car_id = ?", new String[]{String.valueOf(carID)});
            if (rowsAffected == 0) {
                return false;
            }

            ContentValues contractValues = new ContentValues();
            contractValues.put("contract_number", "CNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            contractValues.put("customer_id", customerID);
            contractValues.put("car_id", carID);
            contractValues.put("sale_date", currentTimestamp);
            contractValues.put("payment_type", "Cash");
            contractValues.put("original_price", price);
            contractValues.put("final_price", price);
            contractValues.put("contract_status", "Completed");

            db.insertOrThrow("sales_contracts", null, contractValues);
            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public List<Car> getFiveCars() {
        List<Car> allCars = new ArrayList<>(getAllCars());
        Collections.shuffle(allCars);

        int endIndex = Math.min(allCars.size(), 5);
        return allCars.subList(0, endIndex);
    }

    public List<Car> getAllCarsByCustomerID(int customerId) {
        List<Car> carHistory = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT c.* FROM cars c " +
                "INNER JOIN sales_contracts sc ON c.car_id = sc.car_id " +
                "WHERE sc.customer_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(customerId)});

        if (cursor.moveToFirst()) {
            do {
                carHistory.add(mapCursorToCar(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return carHistory;
    }

    public List<Car> getAllCars() {
        List<Car> carList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM cars WHERE is_sold = 'N'", null);

        if (cursor.moveToFirst()) {
            do {
                carList.add(mapCursorToCar(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return carList;
    }
    private Car mapCursorToCar(Cursor cursor) {
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

        return new Car(id, make, model, year, color, price, mileage, trans, cond, vin, notes, img, fav);
    }

    public boolean checkUserCredentials(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = SecurityUtils.hashPassword(password);
        String query = "SELECT * FROM system_users WHERE username = ? AND password_hash = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, hashedPassword});

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
    public int getUserId(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int userId = -1;
        Cursor cursor = db.rawQuery("SELECT user_id FROM system_users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM system_users WHERE username = ?", new String[]{username});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean isNationalIDTaken(String nationalID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT customer_id FROM customers WHERE national_id = ?", new String[]{nationalID});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean registerUser(String username, String password, String nationalId, String phoneNumber) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues customerValues = new ContentValues();
            customerValues.put("customer_name", username);
            customerValues.put("national_id", nationalId);
            customerValues.put("phone_number", phoneNumber);

            long customerId = db.insertOrThrow("customers", null, customerValues);

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