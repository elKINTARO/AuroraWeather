package com.example.auroraweather;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Toast;

import com.example.auroraweather.utils.LocalizationManager;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private RadioGroup rgTemperature, rgWindSpeed, rgTheme, rgLanguage;
    private Button btnSave, btnFeedback, btnExit;
    
    // State
    private boolean settingsChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply theme before setting content view
        applyTheme();
        
        // Apply localization
        LocalizationManager localizationManager = LocalizationManager.getInstance(this);
        localizationManager.updateLocale(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        rgTemperature = findViewById(R.id.rgTemperature);
        rgWindSpeed = findViewById(R.id.rgWindSpeed);
        rgTheme = findViewById(R.id.rgTheme);
        rgLanguage = findViewById(R.id.rgLanguage);
        btnSave = findViewById(R.id.btnSave);
        btnFeedback = findViewById(R.id.btnFeedback);
        btnExit = findViewById(R.id.btnExit);
        
        // Встановлюємо локалізовані тексти для заголовків
        ((TextView)findViewById(R.id.tvSettingsTitle)).setText(localizationManager.getString("settings_title"));
        ((TextView)findViewById(R.id.tvTemperatureUnits)).setText(localizationManager.getString("temperature_units"));
        ((TextView)findViewById(R.id.tvWindSpeedUnits)).setText(localizationManager.getString("wind_speed_units"));
        ((TextView)findViewById(R.id.tvTheme)).setText(localizationManager.getString("theme"));
        ((TextView)findViewById(R.id.tvLanguage)).setText(localizationManager.getString("language"));
        
        // Встановлюємо локалізовані тексти для радіокнопок
        // Температура
        ((RadioButton)findViewById(R.id.rbTempC)).setText(localizationManager.getString("temp_celsius"));
        ((RadioButton)findViewById(R.id.rbTempF)).setText(localizationManager.getString("temp_fahrenheit"));
        ((RadioButton)findViewById(R.id.rbTempK)).setText(localizationManager.getString("temp_kelvin"));
        
        // Швидкість вітру
        ((RadioButton)findViewById(R.id.rbWindMs)).setText(localizationManager.getString("unit_ms"));
        ((RadioButton)findViewById(R.id.rbWindKmh)).setText(localizationManager.getString("unit_kmh"));
        ((RadioButton)findViewById(R.id.rbWindMph)).setText(localizationManager.getString("unit_mph"));
        
        // Тема
        ((RadioButton)findViewById(R.id.rbLight)).setText(localizationManager.getString("light_theme"));
        ((RadioButton)findViewById(R.id.rbDark)).setText(localizationManager.getString("dark_theme"));
        ((RadioButton)findViewById(R.id.rbSystem)).setText(localizationManager.getString("system_default"));
        
        // Мова
        ((RadioButton)findViewById(R.id.rbEnglish)).setText(localizationManager.getString("english"));
        ((RadioButton)findViewById(R.id.rbUkrainian)).setText(localizationManager.getString("ukrainian"));
        
        // Кнопки
        btnSave.setText(localizationManager.getString("save_changes"));
        btnFeedback.setText(localizationManager.getString("feedback"));

        // Load current settings from SettingsManager
        int tempUnit = SettingsManager.getInstance(this).getTemperatureUnit();
        int windUnit = SettingsManager.getInstance(this).getWindSpeedUnit();
        int themePref = SettingsManager.getInstance(this).getThemePreference();
        int languagePref = SettingsManager.getInstance(this).getLanguagePreference();
        
        // Set selection based on current preferences
        ((RadioButton)rgTemperature.getChildAt(tempUnit)).setChecked(true);
        ((RadioButton)rgWindSpeed.getChildAt(windUnit)).setChecked(true);
        ((RadioButton)rgTheme.getChildAt(themePref)).setChecked(true);
        ((RadioButton)rgLanguage.getChildAt(languagePref)).setChecked(true);

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
        rgLanguage.setOnCheckedChangeListener(changeListener);

        btnSave.setEnabled(false);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the new values
                int newTempUnit = rgTemperature.indexOfChild(findViewById(rgTemperature.getCheckedRadioButtonId()));
                int newWindUnit = rgWindSpeed.indexOfChild(findViewById(rgWindSpeed.getCheckedRadioButtonId()));
                int newTheme = rgTheme.indexOfChild(findViewById(rgTheme.getCheckedRadioButtonId()));
                int newLanguage = rgLanguage.indexOfChild(findViewById(rgLanguage.getCheckedRadioButtonId()));
                
                // Save using SettingsManager
                SettingsManager.getInstance(SettingsActivity.this).setTemperatureUnit(newTempUnit);
                SettingsManager.getInstance(SettingsActivity.this).setWindSpeedUnit(newWindUnit);
                SettingsManager.getInstance(SettingsActivity.this).setThemePreference(newTheme);
                SettingsManager.getInstance(SettingsActivity.this).setLanguagePreference(newLanguage);
                
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

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to MainActivity
                finish();
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
