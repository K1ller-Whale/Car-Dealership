package com.jaafer.cardealership.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.jaafer.cardealership.R;
import com.jaafer.cardealership.database.DatabaseManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirm, etNational, etPhone;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbManager = new DatabaseManager(this);

        etUsername = findViewById(R.id.etUsernameReg);
        etPassword = findViewById(R.id.etPasswordReg);
        etConfirm = findViewById(R.id.etConfirmPassword);
        etNational = findViewById(R.id.etNationalID);
        etPhone = findViewById(R.id.etPhoneNumber);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> register());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();
        String nationalID = etNational.getText().toString().trim();
        String phoneNumber = etPhone.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || nationalID.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, R.string.error_passwords_match, Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbManager.isUsernameTaken(username)) {
            Toast.makeText(this, R.string.error_username_taken, Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbManager.isNationalIDTaken(nationalID)) {
            Toast.makeText(this, R.string.error_username_taken, Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbManager.registerUser(username, password, nationalID, phoneNumber)) {
            Toast.makeText(this, R.string.success_registration, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}