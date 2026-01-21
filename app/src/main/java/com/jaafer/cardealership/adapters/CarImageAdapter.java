package com.jaafer.cardealership.adapters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.jaafer.cardealership.R;
import java.util.List;

public class CarImageAdapter extends RecyclerView.Adapter<CarImageAdapter.ImgHolder> {
    private Context context;
    private List<String> urls;

    public CarImageAdapter(Context context, List<String> urls) {
        this.context = context;
        this.urls = urls;
    }

    @NonNull @Override
    public ImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ImgHolder(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull ImgHolder holder, int position) {
        Glide.with(context)
                .load(urls.get(position))
                .placeholder(R.drawable.ic_car_placeholder)
                .into((ImageView)holder.itemView);
    }

    @Override
    public int getItemCount() { return urls.size(); }

    static class ImgHolder extends RecyclerView.ViewHolder {
        public ImgHolder(@NonNull android.view.View itemView) { super(itemView); }
    }
}