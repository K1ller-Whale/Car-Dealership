package com.jaafer.cardealership;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.jaafer.cardealership.adapters.CarAdapter;
import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.models.Car;
import com.jaafer.cardealership.utils.SessionManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvWelcome;
    private Button btnLogout;
    private ListView listViewCars;
    private DatabaseManager dbManager;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        dbManager = new DatabaseManager(this);

        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        listViewCars = findViewById(R.id.listview_cars);
        SharedPreferences pref = getSharedPreferences("DealershipSession", MODE_PRIVATE);
        String username = pref.getString("username", "User");

        tvWelcome.setText(getString(R.string.welcome_message, username));
        btnLogout.setOnClickListener(v -> logout());
        loadCars();
    }

    private void loadCars() {
        List<Car> allCars = dbManager.getAllCars();
        CarAdapter adapter = new CarAdapter(this, allCars);
        listViewCars.setAdapter(adapter);
    }

    private void logout() {
        sessionManager.logoutUser();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}