package com.example.auroraweather;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup rgTemperature, rgWindSpeed, rgTheme;
    private Button btnSave, btnFeedback;
    private boolean settingsChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply theme before setting content view
        applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        rgTemperature = findViewById(R.id.rgTemperature);
        rgWindSpeed = findViewById(R.id.rgWindSpeed);
        rgTheme = findViewById(R.id.rgTheme);
        btnSave = findViewById(R.id.btnSave);
        btnFeedback = findViewById(R.id.btnFeedback);

        // Load current settings from SettingsManager
        int tempUnit = SettingsManager.getInstance(this).getTemperatureUnit();
        int windUnit = SettingsManager.getInstance(this).getWindSpeedUnit();
        int themePref = SettingsManager.getInstance(this).getThemePreference();
        
        // Set selection based on current preferences
        ((RadioButton)rgTemperature.getChildAt(tempUnit)).setChecked(true);
        ((RadioButton)rgWindSpeed.getChildAt(windUnit)).setChecked(true);
        ((RadioButton)rgTheme.getChildAt(themePref)).setChecked(true);

        // Listener for changes
        RadioGroup.OnCheckedChangeListener changeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                settingsChanged = true;
                btnSave.setEnabled(true);
            }
        };

        rgTemperature.setOnCheckedChangeListener(changeListener);
        rgWindSpeed.setOnCheckedChangeListener(changeListener);
        rgTheme.setOnCheckedChangeListener(changeListener);

        btnSave.setEnabled(false);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the new values
                int newTempUnit = rgTemperature.indexOfChild(findViewById(rgTemperature.getCheckedRadioButtonId()));
                int newWindUnit = rgWindSpeed.indexOfChild(findViewById(rgWindSpeed.getCheckedRadioButtonId()));
                int newTheme = rgTheme.indexOfChild(findViewById(rgTheme.getCheckedRadioButtonId()));
                
                // Save using SettingsManager
                SettingsManager.getInstance(SettingsActivity.this).setTemperatureUnit(newTempUnit);
                SettingsManager.getInstance(SettingsActivity.this).setWindSpeedUnit(newWindUnit);
                SettingsManager.getInstance(SettingsActivity.this).setThemePreference(newTheme);
                
                // Apply theme changes immediately
                applyThemeImmediate(newTheme);
                
                // Return to MainActivity
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Telegram group
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/auroraweather"));
                startActivity(browserIntent);
            }
        });
    }
    
    private void applyTheme() {
        int themePreference = SettingsManager.getInstance(this).getThemePreference();
        applyThemeImmediate(themePreference);
    }
    
    private void applyThemeImmediate(int themePreference) {
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        int newNightMode;
        
        switch (themePreference) {
            case SettingsManager.THEME_LIGHT:
                newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case SettingsManager.THEME_DARK:
                newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case SettingsManager.THEME_SYSTEM:
            default:
                newNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        
        // Only recreate if the night mode has changed
        if (currentNightMode != newNightMode) {
            AppCompatDelegate.setDefaultNightMode(newNightMode);
        }
    }
}
