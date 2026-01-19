package com.jaafer.cardealership.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Dealership.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_CARS = "cars";
    // ... add other table names as constants if you like

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Settings
        db.execSQL("CREATE TABLE system_settings (setting_id INTEGER PRIMARY KEY AUTOINCREMENT, max_install_months INTEGER DEFAULT 24, install_inc_rate REAL DEFAULT 1.00, late_penalty REAL DEFAULT 2000, discount_threshold INTEGER DEFAULT 5, loyal_disc_rate REAL DEFAULT 2.00, min_down_pmt_pct REAL DEFAULT 25.00, max_unpaid_months INTEGER DEFAULT 3, is_active TEXT DEFAULT 'Y', effective_date TEXT DEFAULT CURRENT_TIMESTAMP, modified_date TEXT DEFAULT CURRENT_TIMESTAMP)");

        // 2. Employees
        db.execSQL("CREATE TABLE employees (employee_id INTEGER PRIMARY KEY AUTOINCREMENT, employee_name TEXT NOT NULL, national_id TEXT UNIQUE NOT NULL, phone_number TEXT NOT NULL, address TEXT, hire_date TEXT DEFAULT CURRENT_TIMESTAMP, base_salary REAL NOT NULL, is_active TEXT DEFAULT 'Y', created_date TEXT DEFAULT CURRENT_TIMESTAMP)");

        // 3. Customers
        db.execSQL("CREATE TABLE customers (customer_id INTEGER PRIMARY KEY AUTOINCREMENT, customer_name TEXT NOT NULL, national_id TEXT UNIQUE NOT NULL, phone_number TEXT NOT NULL, address TEXT, occupation TEXT, total_transactions INTEGER DEFAULT 0, is_loyal_customer TEXT DEFAULT 'N', registration_date TEXT DEFAULT CURRENT_TIMESTAMP)");

        // 4. Cars
        db.execSQL("CREATE TABLE cars (car_id INTEGER PRIMARY KEY AUTOINCREMENT, manufacturer TEXT NOT NULL, model_name TEXT NOT NULL, car_year INTEGER NOT NULL, registration_year INTEGER, color TEXT NOT NULL, engine_capacity REAL, fuel_tank_capacity REAL, transmission_type TEXT, car_condition TEXT NOT NULL, purchase_price REAL, selling_price REAL NOT NULL, profit_margin REAL, is_sold TEXT DEFAULT 'N', acquisition_date TEXT DEFAULT CURRENT_TIMESTAMP, sold_date TEXT, mileage INTEGER, vin_number TEXT UNIQUE, image_uri TEXT, is_favorite INTEGER DEFAULT 0, notes TEXT)");

        // 5. Users
        db.execSQL("CREATE TABLE system_users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password_hash TEXT NOT NULL, user_role TEXT NOT NULL, employee_id INTEGER, is_active TEXT DEFAULT 'Y', created_date TEXT DEFAULT CURRENT_TIMESTAMP, last_login TEXT, password_reset_req TEXT DEFAULT 'N', FOREIGN KEY(employee_id) REFERENCES employees(employee_id))");

        // 6. Contracts
        db.execSQL("CREATE TABLE sales_contracts (contract_id INTEGER PRIMARY KEY AUTOINCREMENT, contract_number TEXT UNIQUE NOT NULL, customer_id INTEGER NOT NULL, car_id INTEGER NOT NULL, employee_id INTEGER NOT NULL, sale_date TEXT DEFAULT CURRENT_TIMESTAMP, payment_type TEXT NOT NULL, original_price REAL NOT NULL, discount_amount REAL DEFAULT 0, final_price REAL NOT NULL, employee_commission REAL, down_payment REAL, remaining_amount REAL, installment_months INTEGER, monthly_payment REAL, contract_status TEXT DEFAULT 'Active', FOREIGN KEY(customer_id) REFERENCES customers(customer_id), FOREIGN KEY(car_id) REFERENCES cars(car_id), FOREIGN KEY(employee_id) REFERENCES employees(employee_id))");

        // 7. Installments
        db.execSQL("CREATE TABLE installment_payments (payment_id INTEGER PRIMARY KEY AUTOINCREMENT, contract_id INTEGER NOT NULL, payment_number INTEGER NOT NULL, scheduled_date TEXT NOT NULL, scheduled_amount REAL NOT NULL, actual_payment_date TEXT, actual_amount_paid REAL, days_late INTEGER DEFAULT 0, payment_status TEXT DEFAULT 'Pending', FOREIGN KEY(contract_id) REFERENCES sales_contracts(contract_id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS installment_payments");
        db.execSQL("DROP TABLE IF EXISTS sales_contracts");
        db.execSQL("DROP TABLE IF EXISTS system_users");
        db.execSQL("DROP TABLE IF EXISTS cars");
        db.execSQL("DROP TABLE IF EXISTS customers");
        db.execSQL("DROP TABLE IF EXISTS employees");
        db.execSQL("DROP TABLE IF EXISTS system_settings");
        onCreate(db);
    }
}