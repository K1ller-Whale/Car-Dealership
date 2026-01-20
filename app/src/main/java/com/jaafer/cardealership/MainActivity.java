package com.jaafer.cardealership;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jaafer.cardealership.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvWelcome, tvRole;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences pref = getSharedPreferences("DealershipSession", MODE_PRIVATE);
        String username = pref.getString("username", "User");
        String role = pref.getString("userRole", "Staff");

        tvWelcome.setText(getString(R.string.welcome_message, username));
        tvRole.setText(getString(R.string.role_display, role));
        btnLogout.setOnClickListener(v -> logout());
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