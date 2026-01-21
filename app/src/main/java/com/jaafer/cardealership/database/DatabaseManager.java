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
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Cursor c = db.rawQuery("SELECT count(*) FROM cars", null);
            if (c != null) {
                c.moveToFirst();
                int count = c.getInt(0);
                c.close();

                if (count == 0) {
                    db.beginTransaction();
                    try {
                        addCar(db, "BMW", "X5", 2022, "Black", 3.0, "Automatic", "Used", 55000, 12000, "123ABC456DEF789GH",
                                "Excellent condition, one previous owner.",
                                "https://upload.wikimedia.org/wikipedia/commons/1/1d/BMW_X5_%28G05%29_IMG_3659.jpg");

                        addCar(db, "Toyota", "Camry", 2024, "White", 2.5, "CVT", "New", 28000, 0, "987ZYX654CBA321",
                                "Brand new, zero meter.",
                                "https://upload.wikimedia.org/wikipedia/commons/a/ac/2018_Toyota_Camry_%28ASV70R%29_Ascent_sedan_%282018-08-27%29_01.jpg");

                        addCar(db, "Mercedes-Benz", "C-Class", 2023, "Silver", 2.0, "Automatic", "New", 48500, 0, "MER123BENZ456CLS",
                                "Luxury interior, sunroof, full options.",
                                "https://upload.wikimedia.org/wikipedia/commons/4/44/Mercedes-Benz_W206_IMG_6424.jpg");

                        addCar(db, "Honda", "Civic", 2021, "Blue", 1.5, "CVT", "Used", 22000, 35000, "HON123CIV456VTEC",
                                "Reliable daily driver, fuel efficient.",
                                "https://upload.wikimedia.org/wikipedia/commons/3/36/2022_Honda_Civic_Sport_Touring_%28USA%29_front_view.jpg");

                        addCar(db, "Ford", "Mustang GT", 2020, "Red", 5.0, "Manual", "Used", 35000, 25000, "FOR123MUS456PONY",
                                "V8 Engine, loud exhaust, mint condition.",
                                "https://upload.wikimedia.org/wikipedia/commons/d/d1/2018_Ford_Mustang_GT_5.0_facelift.jpg");

                        addCar(db, "Tesla", "Model 3", 2023, "Grey", 0.0, "Automatic", "New", 42000, 0, "TES123MOD345ELEC",
                                "Long Range, Autopilot included.",
                                "https://upload.wikimedia.org/wikipedia/commons/9/91/2019_Tesla_Model_3_Performance_AWD_Front.jpg");

                        addCar(db, "Audi", "Q7", 2019, "Black", 3.0, "Automatic", "Used", 40000, 55000, "AUD123Q7456QUAT",
                                "7-seater SUV, leather seats, navigation.",
                                "https://upload.wikimedia.org/wikipedia/commons/7/77/2015_Audi_Q7_S_Line_Quattro_3.0_Front.jpg");

                        addCar(db, "Hyundai", "Tucson", 2024, "Dark Green", 2.5, "Automatic", "New", 31000, 0, "HYU123TUC456NEW",
                                "Compact SUV, modern design, warranty active.",
                                "https://upload.wikimedia.org/wikipedia/commons/2/22/2021_Hyundai_Tucson_Hybrid.jpg");

                        addCar(db, "Chevrolet", "Tahoe", 2022, "White", 5.3, "Automatic", "Used", 58000, 15000, "CHE123TAH456BIG",
                                "Large family SUV, towing package included.",
                                "https://upload.wikimedia.org/wikipedia/commons/f/f3/2021_Chevrolet_Tahoe_High_Country_4WD.jpg");

                        addCar(db, "Porsche", "911 Carrera", 2021, "Yellow", 3.0, "Automatic", "Used", 115000, 8000, "POR123911456SPD",
                                "Sport chrono package, track ready.",
                                "https://upload.wikimedia.org/wikipedia/commons/7/7a/Porsche_991_GT3_RS_%28991.2%29_IMG_2491.jpg");

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCar(SQLiteDatabase db, String make, String model, int year, String color,
                        double engine, String trans, String cond, double price, int miles,
                        String vin, String notes, String img) {
        ContentValues values = new ContentValues();
        values.put("manufacturer", make);
        values.put("model_name", model);
        values.put("car_year", year);
        values.put("color", color);
        values.put("engine_capacity", engine);
        values.put("transmission_type", trans);
        values.put("car_condition", cond);
        values.put("selling_price", price);
        values.put("mileage", miles);
        values.put("vin_number", vin);
        values.put("is_sold", "N"); // Default is not sold
        values.put("notes", notes);
        values.put("image_uri", img);

        db.insertOrThrow("cars", null, values);
    }
}