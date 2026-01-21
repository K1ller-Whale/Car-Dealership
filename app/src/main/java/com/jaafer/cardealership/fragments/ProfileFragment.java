package com.jaafer.cardealership.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.jaafer.cardealership.LoginActivity;
import com.jaafer.cardealership.R;
import com.jaafer.cardealership.components.ProfileOptionView;
import com.jaafer.cardealership.utils.SessionManager;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView imgProfile;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "DealershipSettings";
    private static final String KEY_PROFILE_URI = "profile_uri_";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_LANG = "app_lang";
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        try {
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    selectedUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        String username = getUsername();
                        prefs.edit().putString(KEY_PROFILE_URI + username, selectedUri.toString()).apply();
                        Glide.with(this).load(selectedUri).into(imgProfile);
                    }
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SessionManager sessionManager = new SessionManager(requireContext());
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileName.setText(getUsername());

        imgProfile = view.findViewById(R.id.imgProfile);
        ProfileOptionView btnTheme = view.findViewById(R.id.optTheme);
        ProfileOptionView btnLanguage = view.findViewById(R.id.optLanguage);
        ProfileOptionView btnLogout = view.findViewById(R.id.optLogout);
        loadProfileImage();
        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });
        btnTheme.setOnClickListener(v -> toggleTheme());
        btnLanguage.setOnClickListener(v -> toggleLanguage());
        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void loadProfileImage() {
        String uriString = prefs.getString(KEY_PROFILE_URI + getUsername(), null);
        if (uriString != null) {
            Glide.with(this)
                    .load(Uri.parse(uriString))
                    .placeholder(R.drawable.ic_person)
                    .into(imgProfile);
        }
    }

    private void toggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode;

        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            newMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newMode = AppCompatDelegate.MODE_NIGHT_YES;
        }

        AppCompatDelegate.setDefaultNightMode(newMode);
        prefs.edit().putInt(KEY_THEME, newMode).apply();
    }

    private void toggleLanguage() {
        String currentLang = Locale.getDefault().getLanguage();
        String newLang = currentLang.equals("ar") ? "en" : "ar";
        prefs.edit().putString(KEY_LANG, newLang).apply();
        setAppLocale(requireActivity(), newLang);
        requireActivity().recreate();
    }

    public static void setAppLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private String getUsername() {
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("DealershipSession", Context.MODE_PRIVATE);
        return sessionPrefs.getString("username", "User");
    }
}