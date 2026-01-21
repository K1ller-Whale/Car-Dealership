package com.jaafer.cardealership.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.jaafer.cardealership.AllCarsActivity;
import com.jaafer.cardealership.CarDetailsActivity;
import com.jaafer.cardealership.R;
import com.jaafer.cardealership.adapters.AdBannerAdapter;
import com.jaafer.cardealership.adapters.CarAdapter;
import com.jaafer.cardealership.database.DatabaseManager;
import com.jaafer.cardealership.models.Car;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private Button viewAllBtn;
    private DatabaseManager databaseManager;
    private ViewPager2 viewPagerAds;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootVew = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = rootVew.findViewById(R.id.rvFeaturedCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        databaseManager = new DatabaseManager(rootVew.getContext());
        setupAdBanner(rootVew);
        loadFeaturedCars();
        viewAllBtn = rootVew.findViewById(R.id.btnViewAll);
        viewAllBtn.setOnClickListener(view -> {
            startActivity(new Intent(rootVew.getContext(), AllCarsActivity.class));
        });

        return rootVew;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadFeaturedCars();
    }

    private void loadFeaturedCars() {
        List<Car> fiveCars = databaseManager.getFiveCars();
        carAdapter = new CarAdapter(fiveCars, car -> {
            Intent intent = new Intent(getContext(), CarDetailsActivity.class);
            intent.putExtra("extra_car_id", car.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(carAdapter);
    }

    private void setupAdBanner(View view) {
        viewPagerAds = view.findViewById(R.id.viewPagerAds);
        List<String> adImages = Arrays.asList(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/2023_Mercedes-AMG_C63_%28W206%29.jpg/1280px-2023_Mercedes-AMG_C63_%28W206%29.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d1/2018_Ford_Mustang_GT_5.0_facelift.jpg/1280px-2018_Ford_Mustang_GT_5.0_facelift.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/2019_Tesla_Model_3_Performance_AWD_Front.jpg/1280px-2019_Tesla_Model_3_Performance_AWD_Front.jpg"
        );

        AdBannerAdapter bannerAdapter = new AdBannerAdapter(getContext(), adImages);
        viewPagerAds.setAdapter(bannerAdapter);
        viewPagerAds.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int current = viewPagerAds.getCurrentItem();
            int total = viewPagerAds.getAdapter().getItemCount();

            if (current == total - 1) {
                viewPagerAds.setCurrentItem(0);
            } else {
                viewPagerAds.setCurrentItem(current + 1);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}