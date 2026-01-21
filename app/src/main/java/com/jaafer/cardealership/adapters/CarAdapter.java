package com.jaafer.cardealership.adapters;

import android.view.ViewGroup;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jaafer.cardealership.components.CarCardView;
import com.jaafer.cardealership.models.Car;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private final List<Car> carList;
    private final OnCarClickListener listener;

    public interface OnCarClickListener {
        void onCarClick(Car car);
    }

    public CarAdapter(List<Car> carList, OnCarClickListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CarCardView carCardView = new CarCardView(parent.getContext());
        carCardView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
//        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) carCardView.getLayoutParams();
//        layoutParams.setMargins(0, 0, 0, 24);

        return new CarViewHolder(carCardView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car currentCar = carList.get(position);
        holder.carCardView.setData(currentCar);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCarClick(currentCar);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        CarCardView carCardView;

        public CarViewHolder(@NonNull CarCardView itemView) {
            super(itemView);
            this.carCardView = itemView;
        }
    }
}