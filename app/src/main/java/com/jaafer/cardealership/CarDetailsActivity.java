package com.jaafer.cardealership;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.jaafer.cardealership.adapters.CarImageAdapter;
import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.models.Car;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details); // The XML you sent

        dbManager = new DatabaseManager(this);

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

        btnBuy.setOnClickListener(v -> {
            Toast.makeText(this, "Buy feature coming next!", Toast.LENGTH_SHORT).show();
        });
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
        }
    }
}