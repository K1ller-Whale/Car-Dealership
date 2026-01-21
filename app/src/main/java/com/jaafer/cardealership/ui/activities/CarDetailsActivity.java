package com.jaafer.cardealership.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.jaafer.cardealership.MainActivity;
import com.jaafer.cardealership.R;
import com.jaafer.cardealership.adapters.CarImageAdapter;
import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.models.Car;
import com.jaafer.cardealership.utils.SessionManager;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;

public class CarDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CAR_ID = "extra_car_id";
    private DatabaseManager dbManager;
    private int carId;
    private TextView tvTitle, tvPrice, tvCondition, tvYear, tvMileage, tvTrans, tvVin, tvNotes;
    private Button btnBuy;
    private ViewPager2 vpImages;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        dbManager = new DatabaseManager(this);
        sessionManager = new SessionManager(this);

        if (getIntent().hasExtra(EXTRA_CAR_ID)) {
            carId = getIntent().getIntExtra(EXTRA_CAR_ID, -1);
        } else {
            finish();
            return;
        }

        initViews();
        loadData();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvCondition = findViewById(R.id.tvDetailCondition);
        tvYear = findViewById(R.id.tvSpecYear);
        tvMileage = findViewById(R.id.tvSpecMileage);
        tvTrans = findViewById(R.id.tvSpecTrans);
        tvVin = findViewById(R.id.tvSpecVin);
        tvNotes = findViewById(R.id.tvDetailNotes);
        btnBuy = findViewById(R.id.btnBuyNow);
        vpImages = findViewById(R.id.vpCarImages);

        btnBuy.setOnClickListener(v -> showPurchaseConfirmation());
    }

    private void loadData() {
        Car car = dbManager.getCarById(carId);

        if (car != null) {
            tvTitle.setText(car.getManufacturer() + " " + car.getModel());

            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
            format.setMaximumFractionDigits(0);
            tvPrice.setText(format.format(car.getPrice()));

            tvCondition.setText(car.getCondition());
            tvYear.setText(String.valueOf(car.getYear()));
            tvMileage.setText(String.format(Locale.US, "%,d km", car.getMileage()));
            tvTrans.setText(car.getTransmission());
            tvVin.setText(car.getVin());
            tvNotes.setText(car.getDescription());
            if (car.getImageUri() != null) {
                vpImages.setAdapter(new CarImageAdapter(this, Collections.singletonList(car.getImageUri())));
            }
            if (car.isSold()) {
                btnBuy.setText("Bought");
                btnBuy.setEnabled(false);
                btnBuy.setBackgroundColor(getColor(R.color.slate_purple));
            } else {
                btnBuy.setText(R.string.btn_buy_now);
                btnBuy.setEnabled(true);
                btnBuy.setBackgroundColor(getColor(R.color.royal_purple));
            }
        }
    }

    private void showPurchaseConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Purchase")
                .setMessage("Are you sure you want to buy this car?")
                .setPositiveButton("Yes, Buy", (dialog, which) -> processPurchase())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPurchase() {
        String username = sessionManager.getUserDetails().get("username");
        if (username == null) {
            username = getSharedPreferences("DealershipSession", MODE_PRIVATE).getString("username", "");
        }

        int userId = dbManager.getUserId(username);
        int customerId = dbManager.getCustomerIdFromUserId(userId);

        if (customerId == -1) {
            Toast.makeText(this, "Error: Customer profile not found", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = dbManager.buyCar(carId, customerId);

        if (success) {
            Toast.makeText(this, R.string.msg_buy_success, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("navigate_to", "history");
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Purchase failed. Car might be unavailable.", Toast.LENGTH_SHORT).show();
        }
    }
}