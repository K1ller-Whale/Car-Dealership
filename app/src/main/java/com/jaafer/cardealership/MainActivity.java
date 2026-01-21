package com.jaafer.cardealership;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jaafer.cardealership.ui.activities.LoginActivity;
import com.jaafer.cardealership.ui.fragments.HistoryFragment;
import com.jaafer.cardealership.ui.fragments.HomeFragment;
import com.jaafer.cardealership.ui.fragments.ProfileFragment;
import com.jaafer.cardealership.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("DealershipSettings", MODE_PRIVATE);
        int savedTheme = settings.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
        String lang = settings.getString("app_lang", "en");
        ProfileFragment.setAppLocale(this, lang);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        loadFragment(new HomeFragment());
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            return loadFragment(selectedFragment);
        });

        if (getIntent().hasExtra("navigate_to")) {
            String dest = getIntent().getStringExtra("navigate_to");
            if ("history".equals(dest)) {
                bottomNav.setSelectedItemId(R.id.nav_history);
                loadFragment(new HistoryFragment());
            }
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}