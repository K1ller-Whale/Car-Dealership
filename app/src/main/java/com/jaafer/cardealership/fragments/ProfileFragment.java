package com.jaafer.cardealership.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jaafer.cardealership.LoginActivity;
import com.jaafer.cardealership.R;
import com.jaafer.cardealership.components.ProfileOptionView;
import com.jaafer.cardealership.utils.SessionManager;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());

        SharedPreferences prefs = requireContext().getSharedPreferences("DealershipSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileName.setText(username);

        ProfileOptionView btnLogout = view.findViewById(R.id.optLogout);

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}