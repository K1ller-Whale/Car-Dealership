package com.jaafer.cardealership;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaafer.cardealership.adapters.CarAdapter;
import com.jaafer.cardealership.database.DatabaseManager;

public class AllCarsActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_cars);
        databaseManager = new DatabaseManager(this);
        recyclerView = findViewById(R.id.rvAllCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        carAdapter = new CarAdapter(databaseManager.getAllCars());
        recyclerView.setAdapter(carAdapter);

    }
}
