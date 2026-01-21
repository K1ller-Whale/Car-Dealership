package com.jaafer.cardealership.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaafer.cardealership.ui.activities.CarDetailsActivity;
import com.jaafer.cardealership.R;
import com.jaafer.cardealership.adapters.CarAdapter;
import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.models.Car;

import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private DatabaseManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.rvHistory);
        tvEmpty = view.findViewById(R.id.tvEmptyHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbManager = new DatabaseManager(getContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences("DealershipSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        int userId = dbManager.getUserId(username);
        int customerId = dbManager.getCustomerIdFromUserId(userId);
        List<Car> historyList = dbManager.getAllCarsByCustomerID(customerId);
        if (historyList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            CarAdapter adapter = new CarAdapter(historyList, car -> {
                Intent intent = new Intent(getContext(), CarDetailsActivity.class);
                intent.putExtra("extra_car_id", car.getId());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }
    }
}