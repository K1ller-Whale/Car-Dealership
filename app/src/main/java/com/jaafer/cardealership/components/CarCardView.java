package com.jaafer.cardealership.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.jaafer.cardealership.R;
import com.jaafer.cardealership.models.Car;

import java.util.Locale;

public class CarCardView extends MaterialCardView {

    private ImageView imgCar;
    private TextView tvTitle, tvPrice, tvCondition;

    public CarCardView(Context context) {
        super(context);
        init(context);
    }

    public CarCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_car_card, this, true);
        imgCar = findViewById(R.id.imgCar);
        tvTitle = findViewById(R.id.tvCarTitle);
        tvPrice = findViewById(R.id.tvCarPrice);
        tvCondition = findViewById(R.id.tvCarCondition);
    }

    public void setData(Car car) {
        tvTitle.setText(String.format("%s %s", car.getManufacturer(), car.getModel()));
        String formattedPrice = String.format(Locale.US, "%,.0f", car.getPrice());
        tvPrice.setText(getContext().getString(R.string.currency) + " " + formattedPrice);

        tvCondition.setText(car.getCondition());
        if (car.getImageUri() != null && !car.getImageUri().isEmpty()) {
            Glide.with(getContext())
                    .load(car.getImageUri())
                    .centerCrop()
                    .placeholder(R.drawable.ic_home)
                    .into(imgCar);
        }
    }
}