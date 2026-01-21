package com.jaafer.cardealership;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        databaseManager = new DatabaseManager(this);

        new Handler().postDelayed(() -> {
            databaseManager.insertDummyData();
            SessionManager session = new SessionManager(SplashActivity.this);
            Intent intent;
            if (session.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}