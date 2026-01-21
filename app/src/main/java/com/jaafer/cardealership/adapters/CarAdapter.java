package com.jaafer.cardealership.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jaafer.cardealership.components.CarCardView;
import com.jaafer.cardealership.models.Car;

import java.util.List;

public class CarAdapter extends BaseAdapter {

    private final Context context;
    private final List<Car> carList;

    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
    }

    @Override
    public int getCount() {
        return carList.size();
    }

    @Override
    public Object getItem(int position) {
        return carList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return carList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CarCardView carCardView;

        if (convertView == null) {
            carCardView = new CarCardView(context);
        } else {
            carCardView = (CarCardView) convertView;
        }

        Car currentCar = carList.get(position);

        carCardView.setData(currentCar);

        return carCardView;
    }
}