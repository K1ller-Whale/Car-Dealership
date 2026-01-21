package com.jaafer.cardealership.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.jaafer.cardealership.R;

public class ProfileOptionView extends ConstraintLayout {

    private ImageView imgIcon;
    private TextView tvTitle;

    public ProfileOptionView(Context context) {
        super(context);
        init(context, null);
    }

    public ProfileOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_profile_option, this, true);

        imgIcon = findViewById(R.id.imgOptionIcon);
        tvTitle = findViewById(R.id.tvOptionTitle);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProfileOptionView);

            String title = a.getString(R.styleable.ProfileOptionView_optionTitle);
            int iconResId = a.getResourceId(R.styleable.ProfileOptionView_optionIcon, 0);

            if (title != null) tvTitle.setText(title);
            if (iconResId != 0) imgIcon.setImageResource(iconResId);

            a.recycle();
        }
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }
}