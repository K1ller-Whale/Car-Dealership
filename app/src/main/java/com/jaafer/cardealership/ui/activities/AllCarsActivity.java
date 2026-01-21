package com.jaafer.cardealership.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaafer.cardealership.R;
import com.jaafer.cardealership.adapters.CarAdapter;
import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.models.Car;

import java.util.List;

public class AllCarsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_cars);

        RecyclerView recyclerView = findViewById(R.id.rvAllCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseManager dbManager = new DatabaseManager(this);
        List<Car> allCars = dbManager.getAllCars();

        CarAdapter adapter = new CarAdapter(allCars, car -> {
            Intent intent = new Intent(AllCarsActivity.this, CarDetailsActivity.class);
            intent.putExtra("extra_car_id", car.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}