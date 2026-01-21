package com.jaafer.cardealership.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaafer.cardealership.AllCarsActivity;
import com.jaafer.cardealership.LoginActivity;
import com.jaafer.cardealership.R;
import com.jaafer.cardealership.RegisterActivity;
import com.jaafer.cardealership.adapters.CarAdapter;
import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.models.Car;

import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private Button viewAllBtn;
    private DatabaseManager databaseManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootVew = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = rootVew.findViewById(R.id.rvFeaturedCars);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        databaseManager = new DatabaseManager(rootVew.getContext());
        List<Car> fiveCars = databaseManager.getFiveCars();

        carAdapter = new CarAdapter(fiveCars);
        recyclerView.setAdapter(carAdapter);
        viewAllBtn = rootVew.findViewById(R.id.btnViewAll);
        viewAllBtn.setOnClickListener(view -> {
            startActivity(new Intent(rootVew.getContext(), AllCarsActivity.class));
        });


        return rootVew;
    }

}