package com.example.auroraweather;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREFS_NAME = "aurora_settings";
    private static final String KEY_TEMP_UNIT = "temp_unit";
    private static final String KEY_WIND_UNIT = "wind_unit";
    private static final String KEY_THEME = "theme";

    // Constants for temperature units
    public static final int TEMP_CELSIUS = 0;
    public static final int TEMP_FAHRENHEIT = 1;
    public static final int TEMP_KELVIN = 2;

    // Constants for wind speed units
    public static final int WIND_MS = 0;
    public static final int WIND_KMH = 1;
    public static final int WIND_MPH = 2;

    // Constants for theme
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    private static SettingsManager instance;
    private SharedPreferences prefs;
    private Context context;

    public static SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    private SettingsManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getTemperatureUnit() {
        return prefs.getInt(KEY_TEMP_UNIT, TEMP_CELSIUS);
    }

    public void setTemperatureUnit(int unit) {
        prefs.edit().putInt(KEY_TEMP_UNIT, unit).apply();
    }

    public int getWindSpeedUnit() {
        return prefs.getInt(KEY_WIND_UNIT, WIND_MS);
    }

    public void setWindSpeedUnit(int unit) {
        prefs.edit().putInt(KEY_WIND_UNIT, unit).apply();
    }

    public int getThemePreference() {
        return prefs.getInt(KEY_THEME, THEME_SYSTEM);
    }

    public void setThemePreference(int theme) {
        prefs.edit().putInt(KEY_THEME, theme).apply();
    }

    /**
     * Formats temperature according to user preference
     * @param tempCelsius Temperature in Celsius
     * @return Formatted temperature string with unit
     */
    public String formatTemperature(double tempCelsius) {
        int unit = getTemperatureUnit();
        double convertedTemp;
        String unitSymbol;

        switch (unit) {
            case TEMP_FAHRENHEIT:
                convertedTemp = celsiusToFahrenheit(tempCelsius);
                unitSymbol = "°F";
                break;
            case TEMP_KELVIN:
                convertedTemp = celsiusToKelvin(tempCelsius);
                unitSymbol = "K";
                break;
            case TEMP_CELSIUS:
            default:
                convertedTemp = tempCelsius;
                unitSymbol = "°C";
                break;
        }

        return String.format("%.1f%s", convertedTemp, unitSymbol);
    }

    /**
     * Formats wind speed according to user preference
     * @param windSpeedMs Wind speed in meters per second
     * @return Formatted wind speed string with unit
     */
    public String formatWindSpeed(double windSpeedMs) {
        int unit = getWindSpeedUnit();
        double convertedSpeed;
        String unitSymbol;

        switch (unit) {
            case WIND_KMH:
                convertedSpeed = msToKmh(windSpeedMs);
                unitSymbol = "км/год";
                break;
            case WIND_MPH:
                convertedSpeed = msToMph(windSpeedMs);
                unitSymbol = "миль/год";
                break;
            case WIND_MS:
            default:
                convertedSpeed = windSpeedMs;
                unitSymbol = "м/с";
                break;
        }

        return String.format("%.1f %s", convertedSpeed, unitSymbol);
    }

    // Conversion methods
    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }

    private double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    private double msToKmh(double ms) {
        return ms * 3.6;
    }

    private double msToMph(double ms) {
        return ms * 2.237;
    }
}
