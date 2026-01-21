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
        String isSoldStr = cursor.getString(cursor.getColumnIndexOrThrow("is_sold"));
        boolean isSold = "Y".equals(isSoldStr);

        return new Car(id, make, model, year, color, price, mileage, trans, cond, vin, notes, img, fav, isSold);
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

    public Car getCarById(int carId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Car car = null;

        String query = "SELECT * FROM cars WHERE car_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(carId)});

        if (cursor.moveToFirst()) {
            car = mapCursorToCar(cursor);
        }
        cursor.close();
        return car;
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
                                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSbH-i1iLKK9o3X338oyNNKCo8Y1g_my3z538KteTrPHgG0zcatEzrMzCoGFSUW0m_aUiwpVqCPB0jKkHayz1-Y_J2G0sj3AdXeZlkS1xn5&s=10");

                        addCar(db, "Toyota", "Camry", 2024, "White", 2.5, "CVT", "New", 28000, 0, "987ZYX654CBA321",
                                "Brand new, zero meter.",
                                "https://upload.wikimedia.org/wikipedia/commons/a/ac/2018_Toyota_Camry_%28ASV70R%29_Ascent_sedan_%282018-08-27%29_01.jpg");

                        addCar(db, "Mercedes-Benz", "C-Class", 2023, "Silver", 2.0, "Automatic", "New", 48500, 0, "MER123BENZ456CLS",
                                "Luxury interior, sunroof, full options.",
                                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxMTEhUTExMWFhUVFRUVFxcYGRcbGBUVFRUWFhcVFxgdHSkgGBolHRUVITEhJSkrLi4uFx8zODMtNygtLisBCgoKDg0OGhAQGisfHSUtLS0tLS0tLS0tLS0tLS0tLS0rLS0rLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLf/AABEIAKgBLAMBIgACEQEDEQH/xAAcAAABBQEBAQAAAAAAAAAAAAAFAAIDBAYBBwj/xABIEAABAwEEBgcFBQUHAgcAAAABAAIDEQQSITEFQVFhcZEGEyKBobHBMkJS0fAUYnKCkjNTorLhBxUjQ8LS8WPiFjRzg5PD0//EABkBAQEBAQEBAAAAAAAAAAAAAAABAgMEBf/EACQRAQEBAAEDBAIDAQAAAAAAAAABEQISIUEDEzFRFCIEYZEy/9oADAMBAAIRAxEAPwD00ztGuv1tTDIXZRud4DvLs+6qIss7RkAN+ZUhpvV1nA1tnlOd1g2DE8z8k/7ONZJU89oDc6DiQqn26pwa93Bpp+p1AmmRM2EJ1GtxNAomTOPuhvE1PIKNwO09wA80Ez520wBPdhzyQ+XSjRheYN1ank0HzVh9nLh7N78RJ8AmCz3BVzmRjdcaPGpQqmba8+y2V/4WBo5vVad859yJm+WUuP6G4eKtyaSsowM4edjb0nlgpItIxe6x9N91o+apgYyxzuwNocBsghawfqekOjbHe218p2zTPd/A3sou3SjNQaTuvPPIBcfpN+prgPyMH8RDkOyOyaDuijGsj/8ATja3xdVTvsEY/aSdznn+UGngonSyOHu97nO8KAeKrPsjj7UhH4GtaP4rx8UXsnfpCyxZVO5jKeJohGkemjWYMsxO+R4b4Y+avQaMhLwHBzqn3nPI/TWngq9oiEb3NiZGyjiKhoryATE65AeTpNb5P2bGMG1kT3H9RqFVks2kpcZJpgPxtjb3hpr4I5JDI72pH8Gi6iMcX+AW01UoczxKuLecjDjRDGmss0V7XUvlP+lIW+ztNGvmkOyKJo5Xmk+K0zdHY1DYx+S8eZIUpsTv3jgNjQxo8q+K053lLPhmG2qR3sWCd++aVzR+km74JkhtjBg2x2Qbeze8BQrUnR0ebnOP4pHkcq08FXbLZI6hpiB2MDb3JuKMsZabLM/9pbpZK/uY30/U2rUOk0GzGrJ3/ekka0H9Jr4LcWvSbMbscrvyOb4uujxQS126QjswBu57h/pvqxNrJWvR13/LjbszeeZAQC3R0zp3Ci1mkZZjqaNtAT/t8lmbeDrr4fNKsoY9v19BPsw7Q4j6zTnhOs+Y+vVSLfhf0ay89oJa1uFTQbchXX9Y5I7bNKOuhkHYZiCRg51Np1DdzqgAeaADIGvE7Uc0No8SYvmZGK+8ceS6yvH6m+EUEkh38VO2QjMBaOyWSwszndIfutPoFOBAT2LM8jaRieast1xvH9M8sxHaauF5poMw3Wn6X64sBENyPVgK95zW2ss4j/Z2RgO17xX1Q/TxmmH+K9jGbGg07yUu2k45HnVzaV1zBqxWl+w2ZvtTA8Fx32UZFzuadLLNR2ep2IlY4YwwAzlpxwz18Fcklg1RuPd80yOjhVsFRxaFZInKy/NfQVQDknPcaLNWh1pccZ7g2MAr4CviuT6IDQ173vkLtpJ2fEcF5MfatkGyW19poO6lfUpOczMknn6lCDUM7DQKfE6mB/CohDO732t/BFU/qefRDe40Laz3RXvr4AKla9JSD2Q1u8ho/mPoqsWi3E9uSZ24vDRyYFO3RcYPsCu/E8ySrib2DtJQWl7Q51oc0HY4jwYBXmhLNBNdi4vcdoaB4vqtlam0YK4AIa+RmxzuDXEfJajNtCWaPjZkwV++8+Qw8EnMlPsOiYPuxue7xRVknwwkcbo9VJ1kmpjRxcfQIgXHYZD7ctpk72RN5ChV6CyFvsxsbvLnOPfh6qV0kmt7G8GknmT6Ku+QuPtyP3AtA9PNBZe59MXgfhaB5kqsRXMvdxcR4CgUws7yMGAfid8q+as2exn3nDuHzUXENiiAcCGgb6Y8062N7Z4+iIx2Vg1k8/6KUhta0PgFdZ6QgNOoeCnax1w4UKJFzfhrxK4ZQMg0Jq4ACzyHb4eqZJo4nMvPBzh/LRF5be0ZyNG4EBDZ7axxwc53AOKsZqs6wRNxMbSdpAJ8cVx0zQKBru4UCmuA5A8k18JRAy0zfd5kIParS3eN9EbtVmOvBBrXYm6yfrgtRkBt8jTrCyuk2rV6QsTNePesvpGADIURqBL2p1mZU0T5G/X0FyDNDkvw3Rm2vefktZ0YsEcovPuMFdgJ5u+SyLEQsZXTi8vO2PSbljiH7QHgR5AKjLpCG92Q4jfVZaEOKI2WOua308fu15Ofqc74xoodOxsFGQjkAqPSm1l8eVE2Oy4YDyVHSgecyfruU5cePhJy53/qsx1VcTgpwDTsgqZgNcBUq66wTOGwclMS0Be811psD46dsOrXUdXNEJrBT2nAKuxkYrV5zRZI9+Zo0nPzVp9iBDQfdVZ+nGA0AHOvkn2vSLgwODc6ZCpxXmvHlPl9zZUhgAFAogzfyCqttLnVJr3/ACTQ87eSzlNi7c+iV1szRm9o71SiY0k5nDWSfNVXMAyaArInUI26YOaS11aa8xn4oSZydZ/TTzVyF3YdwKHvtLQcieAJ8gtxm1whx/zCOShlhZ788n6w3yAKey0VNGwuJ/CPU1Vlr3DNrGfiexp5VqlFKKGzj3XPO0iSTzqr0dopgyF9Pwtb5kJfaP8AqwD/AN0egSpO4f4ZifuY9rv9QWey5VoSPI9inE/IFN6x25ArdpeWLCVjmnhTlWtVS/8AEjdleJ+QSFrVNtLq+0F22ON7AkYDJZUdJ9jW8yuS9Ky7Gg7q6ltmtI4bST3qeP2Ds1rFu6RjM1pXZ4VU7elrGtIpTea/JW4klH2yt1Dk1x8gozazqiefygfzEIDF0paNlNxPh2cU+XpJZ6Ve+VtdpY1ufxVujvKXDKOieQ/5ThxcweTiq89pkFa9S38Up8rvqoWQsdj1T3A41dI2h5EqI6NbjSzRfmkefC4Vk7Bmk9MtZ7Vqgbwa53+v0QO06fgOVpefwRgfzNK009g/6NmH5C7xq1VZIXge0wfgiA83FVGNtOkGOybO7eQB/LRBrY+vuuH4ifUra2+KTW9/c1g8mrKaUaRmXHiqsCHhch+vqqkeuQ/X1VVOS7G3BX7CFTjApn4/1V6xzho9po7wt8bJdeP1OPKz4F4GIjZLLXchUWlWjOXuxV2PT8AzcTwBW+fqTlXDj6HP6o7AKYeWKqaUdRuLXcaFUP8AxPCCCL5p9ayuWvpYx7bvVvI4D5rn1Nfj874VI7S1hqR81WtuknyCgqBsHquS6SjP+Q899PRM/vKmVm5l3yTqhP4/L+v9ig2xPeaDPPE+qpvhIJRiXS0pFOpFNhrRVDbJP3TOX9VOzpPSs8z/AF7RFbAXUa1/Ei75q9I68ylaZ+FFQeImOxz37+JTxbWYUGW8a1Ld7yPfO3zTQ8iuJPewKhNLL7rY6/fmcfBoRBk0YyaOf9E825taU/mPkFz3u1kxW0dNJe7RZ+Rjz4kJOtBGpx/LTzKtstdTQN/hf5lVRaXvddazvLQABtJJyTe5kxPYZi4PBaR2cK0x3ChQu22hjXXKF8pNBG1xo3An/EcPZwBNBjQFUulPSIw/4MJHWkVfJSlxp+EbTjTgVmLLp0NczGjQ01NMS5xoXE1xOBFd6XfmLJPLYzNdSkkpA/dx9hnD4nHfVUZRA0Ytrxx/mJQG36YDRWrnDaAXeQWdn6UsIIDXkYkkimDWlzs9dAOYWHRtbGYn3nOa2gNGggYHM07ro7k+SOIYtvNOotJFOAy8FiH9KHMZduZON7tZur2qYZZKselzz/lD9R+Srlter6I0p19bJaTUkExSHP8A5Hj4oZabEGOdHI3tNND6EbiMV5u/pVJVputbRwcHVJLSDmMvqq9Yjtf2yzNtDRSaIXZG6y0YkU3VvDcStS41O/yDPso1AHzUckLRjhvwGPEevmiIocfFB9JzUVrd4wOtLxUkAVIxBxB5Z8ULktF35GtRvB1hOtVsG3Whdukc4Yajmp5S8YL2SSN7SKuvDMAim4iuSrzmRtSw3hsNAeBGRQyywPa4POIyPfqKP/YXHEMYScRU5hb9u1x5crxqhonpTJZndjAV7UZBuH8vuneKL0PQfTGK0DAxxupi1w8jXFY12ji8dqNnkQhts0E+PtsPI/WPml9KpPVlepS6U2St/Kwn5obarc8+/MeELv8AYsdY+lFoLWxvnkYBheAa402ODs+Y9FM20SOdcltrwXCrC0VbIK0o0gg3hraQCFiSN7fte0g95/fd4u+oWU0qD97v/wCSi9rsu20Tu/LIPVA7dGBXtPP4q+q3iaoOJ+h/RcjJOdUnt3eH/auRDd4f9qM2itmsQOY/iPzRWw6LjJxa364oPZ5yPr+iIQWwj/ldJjy8+V+2ms+hYPhZ3/0V+LQ8P3e5qzUOlHDIBXGaXk3eK12cuvj9NA3RESmZoiFZl2lX7lz+95NoUTr4/TVCwQDUOSl+yw01BYx2lH7uSjfpSTaOSp7s+myfo6zHNzQq7tH2L4xyWKl0rJu5Kq7S0u0fpCa1PUl8R6tabU1zzRr+NRq8dajeH6g79ZHoh0kzK4yEfm4bF2S1xYU613DrCsdVx7BBodUktcd3WOpyVqO9ndA7yfRAhaG1/ZzcnfNXLFNG5waRMK6z1jRzJoudb4zbg1DoYy4uc+n4nDkAVdZosRsuRNAGZxxcd5Oaa7T8TBdu1AAGJ+qqtL0lioaRknE0aBUndlUrnrpJIxtt6CWt5c8vjL3uLndrDHIDCuAoEPm/s8tmoxnLWdQp8K1mj+kEUtS9pAIqA6ExkY0oavdXwUz5YDlgm0yMC/oPpFmTQRuL/wDaqNu6LWwg34cw4HFo9sXScSK4BekmnuyvHBzvmo+vtIFYnzSGuTSCaHI0ccqBFeXW/o895p1MjLzsXUq0VuguNMhh5ovoa22Gztutswe5uBkkHWFx2i6S0DcFu3W60++Ad0sQ9QKoRNo6zuJdJYYXEkk3S9mJzwBoOSJOzP8ASiezWuzPDY2tla0ujutLSXD3aUxqEW0BaJbLJHM5pbFKRE6u0fsnkZjK5jqLNigt+iLAASyzTxP1GOc4b8aoVoKxWh96Oe23Yz7rwXVu9ppD8brgQ013JqtrpqxFlZYmuMTscBjGTm2nw7ONFkNKh7x2GvJ2XXeGC9P0VELt1kzHggjsuBwOCvuszXYSxtrrddwO+oy4FXV2vC4NCTONXRPPdTlVFodDuAp1ZI3lvzXqsnR6B2Lbw/C6o8aodaejFPZl5j5LU54l1563Q7wcGDvd5iilZZJgCDE1w1HtOI7wFrJdDTNyuu4GnmqkkEjc2O7sfJb93kxZ9s4RMCOywcRieNU4deNUeO4I8LWRhXuPyK4ZIz7UY4t7J8Fn3OX2z0cfpkbToe+6+5waTndGfEZJgsTWU7RN0hwqAcRly571qZtHxP8A2ct0/DIKj9QQPSej5Y8Xs7Pxt7TeYy71m22tSYZPpS8MbtfxOb54eKCaSa5wJDHHh2hzDlO+zXslxmjnZtKvdMjPvYdnl/VMj+vqi1Jhf77Q78Q9c1J9jicMRTccR3HMK6ljPwj6+grkSvzaFZSrHU3YKAWF41V4fJanKPL6np8kkatxqpGFbjW3jrrkwqQqIojiY9dKa5VFSZVXKzMqzlG+L0bqmXql3luV29HhSanC76oNaNKSV/ZHuPDaFE7SMh9yccHN+S5dUfV6KPsLCf2pP6VX01aGxtYWEEk4nDKm5DLO57jnO3iYz5qppt72uDC5zuyDUhoOv4cFnlYvHj37irtKRtYHyOpXIDEngPVUX6Sjfiw9xwKwum5nTz9SCQ1oF8jwaPPZiTqUlnsvUAPidVtaOAdeFc6fddStNRx7sOmNYdJUTm6TQGSWuIyOI700SFFadmlN6ZPaI5WBsgqBQggkOBGxwNe7IrO9cUmWg0Qa2C3BuRPeSaUFMKnD+pVluk1jRaSpBaztQa6S0tdmoTGzy8Ss422lSC3HDHWg0LrO3Unw2maP2JnjcHGnLJBWW9XrHNXM4a+CDQ2LTdqJxLX73NFeYojcOkyR2mkcDUeK860h03is5utpXeCSfyjLvXbB/aE15o4Nx1EOYe6uB4J2Tu9HMrXZOHkfX0UT4jqd3HXw1HuWOk0wX4s8VSh6TvZVs8Rdn2WzXLw1UAo4jvorsh8tpabKffYDxCFz6JYcg5h+6cORwXIdN2M/vWYDK0zimN0YkluJqBtoicdpidS5LaRrzikHi2qsqWAEugJvccH7j2T8lWdBPHg4Obxy55Fan7S/3bREd0kT2n9TSR4KG0W6ZoMj4WysaB+ylY4NqaVLXXTXEYnLzaYypgBxuiu4fJPbZj8PkjVg0zZbTQtjkbXI3CBzFQodLWyzw+08A7K48hitzkzgeIDsHmo/7qDj7QbxBoqNq6WQD2Q93BtNu2mynGm1CLV0z+GPm75AimWNfeByrS9UTpEbRZCCRWtFTkvN1qjbdKWotDjHcvEUFKksIJDqFwdqdhTUgcmmJjm5vI/Pks2xcrRyW8H2yMMzrp6q1GViX299QTQgHZ9Y7lsdC2hk1cLp9q601Aa7Km6urVUK8OXh5f5PpduqJyonIkbC3aVG6wN2ldXi6aHFMciDrCNpUbrCNpROkIlVVxRSaxD4iqj7IK+0UrfGPVnWBhzaD3Jv91x/CPFWBaQnC0BeHs+53Ujohmq8ODnD1Wd6U2IMfGQSaimJJOB2nVith14QnpFo7r2gsPbZW6Dk4GlWnYcBQ/NWJXj+N6U63yuH5WmgHgrscfVEXsGv7B4GmPEG6eIStlkdHK5rmlp6xxocPaId6ojpixG4xpAvF5JIJJocs8uzjliDrzW2VKxSZtdm00I2A19Q4dyIsYw+8O/BAzLS0EfE+RvIlwPgeaJPBGYI4hQXHWI6lAbK4KBryMiRwUsekJBrB4hUNcwhMJVoaQB9pnI+hTusidrpxCiKoel1nmrf2UH2SDwKglspHMeaB8cqZp3S5hh7PtOwHHb3UJ7gm0ogunnXpom5gAuptpjT+E81VO0TYAAZJHAOxLnuF4g0rdY33nUIJqQBUY5VIuhjk7DX3iTQNkY1t4kVo1wcQDuNOKrsbeeGam1AOoj3z3mrlYmsVBXZjxIAZTmUE+hra6J4jcSWkkNvZtcM43eNK8NiN26Fkrbr2gjVtG8HMLL2o32BxJvCjHO1l7RWN/4qNcD+Aayi+jbd1kbScznTURgfEFZoT7BkGODQCaZgY1woKgZnFt3NX9FaEtDHh0Ty1ta9m4cQKAgEhQkLWaAtLSwDWFrjNZ1QGi3MaSGSSPJr23tALq1rg7DHHJcnhtBYRaZGRwk1MMQA6w5/4jqVpXGlccFoJZ2jWOaDdICyRmDxUbwtYmqFr0xcY1kIF91QxooKAZnGg4V9CsbarPM5xv3akmt6WPXga1dj68i1aSY55zFKAUqBgK4Y5jE88cMDSfox2oeIp9fLc1TWsSv0e6nalgbxladRrW7WuXgdgp2z2KNrhfnYSXAARudevFxANSymBqeIO0hQDRzsqVdXURlwGv6216zQkpyjO6lfrUPDZiEelDJIJZXuDZZJGStjrR4Yxrw2g1UD2hozozLJPtRha94c2QvD3ggOa1ntEC72SfoolY9BWkS9d1FJAS+853Za7PrbhGYOOJIClh6IyGrnPYMak3rxr2c7tSTVzeYQrOSTg+zE0byXOPiaeCJaLmdZ3McaZdofcea3abaUNOCpWl4ie9gbixzmE72uLSeYVZ0rnUAbi9wbeOJ7RyFfRNS8ZZj0U6TaNfgmHSzNvgUbd0fgPus/QPSihk6LxH3Wc5B5PW/def8AE/sJ/vOP4h4prtIR/EEQf0UZqa3ufJ6uKrydFR8Lu6Q+rSnup+JfsMltcZ94c1UdOzaOYRKXot92T/5GH/61Xd0X3S84/knuw/Fr0LrE0yBDzxokK7Vwe5f64bVz7RvVF17aq00DjkUAbp1BUskHw0/QSeZDv4EFtGkTLcvH2W3fwgZnuFeSN6Q0fMRg6tDUa6EfRHAlZvScNooQLLcrg5zMb3DHAclqVmwFtVoNb4zvl3Mk09Fp+jdLU65FJdeG3iDeFBgCcAcKkc1lZbDOf8l44hFNB6RlsbXXIjffS84g5DJoGoYnjyRG2d0QlI7MkDzsr2udB5qhaei9pZnC78hvfNAJul9pObqflHqoGdKrU01bM8cDhyyQEprE5pxDgdjmkfPyURhO48CPI0KdD/aNbG4OLJBskYD4iitM/tEhdhNo+M7TG8tPIj1VwUXMIzBHEEeKcJ3AYOOrXXWikfSXRMmbbRAeAc3+E1UlbBJjHbYuEoLCO9w9VMAkzE5oXaP/ADLD92nJ39VqJNCYVY+N42skafUjuWZ0pGYp4qggguFDtIAb4ouLvR2OrzgRhxz1imtG3GsZLnYuyaWXbjaZtri43hrHv/dxG6KAjtGFSAc9xNWgDWSLtOK2umoYeqe8GrgTdGujW3HMrXtCpc4V1lurBVHnLD2XjAAAUGeIe015XuZS0PPRz27HB3c4fMFOkiADsMbpNR95zW+TvBU9HH/GcPuNPI/1UpGtbO0NLnGgAqTuQW36UkBFCRe7QjxBufE8jEVwoBvru5apqubHqHbfvA9kHvx7kJimdI98h944Y5DUMjqopBPJaCc4Wnvef9aje0km7EKVwxdl+pTCuzxH+1FdF6Elnx7TI/iJz/CKCvktID2bR87z2WBoyxdh3VJqrtp6O2oNwGO9tBTktsw2exR3jnlU4vcdgWS030snlrcPVtxoG+13u28FciKeitGTRSnrML7SA7IDtNJB7gSt9Y7c5gBJFHNhxe67RpZ2g0E0NMO0NoqKheTSOmcaudI46jeceWxJ9ldjUY4Z7uK3ef6dGedZ6P36t8Y9BGkooiS6eJx6oMBc5rquu5OugnA3d2BwxCHWjpLZm3aPcboLWhjCcCb1LziMKnZjdaVjjZqZkd31wToLMHODGh0jjk1ox2fJc21+26Zjlc93UkBz3OrUVF412b1zRkJdNBQ1YJ4iTtHWNzrlTYili6EzkCojjGx7rxrwaCPFF9E9DCyUSSzhwDg4tYCLxBqKk5DgEtMbpdqFD1gTTKFltMSFG5wULpQonyIJnOURcoHSKIyILAlTg4KuPrBPbT/gqKsDvXVCPrBSsf8AVUDrqXUg6k4P3eie0jegj+yt2Jp0ew6grQG8eS7Q7PIoBsugYXZsae5U5eh9nd7tOBR6v1iF0SIMpN0DhOTiFQn/ALOwfZcOS3fW/WSXW7kHmc/9nkoyI5obaOhFobk2q9fEwXb1U2mR4ZaOjc7c4zyVOSzStzDhTjgRrXvjmA5gKtaNGxPzYOSupjzzRU3WMbKPaiaXObrNwYGmumXANO1W2W6rbpdRw7PHstIPGoCM2/ouAb0LrjhiMxjxCFzw2xmUbSfiY2KtdtTQ135q6mBOkWXG3Dm4h7h8OGA76l1DsaUEsdqDJi44AtI5kH0V63WW0k/sJN5ONScyTrVB2h7Sf8l3goCkekoTiSKjJwpUbiDmFe0bpyyxNLeqhcCb1SSCKgYDsmgw261nWaFtH7p/JO/uOX928dyu4mNc3pXZRlBF+o//AJrto6cNIo0Mbsxkd4Bg81kP7ll+B3Irn91P+E8ir1HSt2vTTXOvnrJX6iaMaNwGJpyVObS0jsmtbwFfE1Klj0Q/4TyVqLQb/hKmmBD7XIfeUZDjrK08WgDuVuPQA10U1cY1tnJ296vWJ0seMbnNrndJFeNFrGaDYNRVhmi2DUmmAMGlbWPfceOKK2TStp10V0WMDJPFnKaYki0lJrCsN0gdhVTqiNS6irotiRtIVK8F28E0WzNvTb6rVC5QbVBbbJxClbNXWD5oYH02hOEvA+BRRZsvEeKkE3AoQJ+IUrZt4KAsJuKeJhuQps3cpGzbwgKCROE+9DBL9BO6/fzQFPtSaZ2lDTJ9BMMu9AUDxqKfUoSJVK2figIGQ7EhKFS+0710WlBeD967e3qmLQF0vCC4JFw0OpU729dEiInLAmFgUZkKZ1yKn6sJdU3YoRKu9aoqXqW7EjA3YFEJV3rVMHTZW7E37M3Yu9au9Yophs7di4bO1OMiaXqoY6zBMMCeXppk+irojdHuUTmKx1iaXJorFnFROarZouFqgouadiic1EDEmmJNTA1yZeRJ0IUZs42K6Yoh54rt8cEklUdB2FcLt3JJJA5sx1HmpBaNoXEkEjZxwTxP3pJIrhn7khPvSSQOE25OFo3pJIEZ+Cb13FJJBIy0b1J164kgX2hOE6SSB4tCRnSSQN65cNoSSRC69dE6SSK71y6Jt6SSikZ1z7QkkgaZkusSSUCv70g5JJA6/wAUr3BJJQdSrvSSQLkuU3JJKD//2Q==");

                        addCar(db, "Honda", "Civic", 2021, "Blue", 1.5, "CVT", "Used", 22000, 35000, "HON123CIV456VTEC",
                                "Reliable daily driver, fuel efficient.",
                                "data:image/jpeg;base64,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");

                        addCar(db, "Ford", "Mustang GT", 2020, "Red", 5.0, "Manual", "Used", 35000, 25000, "FOR123MUS456PONY",
                                "V8 Engine, loud exhaust, mint condition.",
                                "data:image/jpeg;base64,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");

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
        values.put("is_sold", "N");
        values.put("notes", notes);
        values.put("image_uri", img);

        db.insertOrThrow("cars", null, values);
    }
}