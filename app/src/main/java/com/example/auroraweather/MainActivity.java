package com.example.auroraweather;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.auroraweather.adapters.ForecastAdapter;
import com.example.auroraweather.api.OpenWeatherMapClient;
import com.example.auroraweather.api.WeatherCallback;
import com.example.auroraweather.models.CurrentWeather;
import com.example.auroraweather.models.DailyForecast;
import com.example.auroraweather.utils.AnimationHelper;
import com.example.auroraweather.utils.BackgroundManager;
import com.example.auroraweather.utils.WeatherIconMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.auroraweather.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Added for logging
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String SEARCH_HISTORY_KEY = "SearchHistoryList";
    private static final int MAX_HISTORY_ITEMS = 10;
    private static final String LAST_CITY_KEY = "LastCitySearched"; // Added to remember last city

    private ConstraintLayout mainLayout;
    private CardView weatherCard;
    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView weatherDescriptionTextView;
    private TextView dateTextView;
    private TextView humidityTextView;
    private TextView windSpeedTextView;
    private TextView forecastTitleTextView;
    private LottieAnimationView weatherAnimationView;
    private EditText searchEditText;
    private FloatingActionButton searchButton;
    private FloatingActionButton historyButton;
    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;
    private LottieAnimationView loadingAnimation;
    private BackgroundManager backgroundManager;

    private OpenWeatherMapClient weatherClient;
    private List<DailyForecast> forecastList;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences preferences;
    private boolean themeInitialized = false; // Flag to track theme initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starting activity creation");

        // Apply theme before super.onCreate() and setContentView
        applyThemeFromSettings();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize settings button
        FloatingActionButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeComponents();

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up weather API client
        weatherClient = new OpenWeatherMapClient(this);

        // Set up background manager
        backgroundManager = new BackgroundManager(mainLayout);

        // Set up forecast RecyclerView
        setupForecastRecyclerView();

        // Set up search button
        setupSearchButton();

        // Set up history button
        setupHistoryButton();

        // Set up search text watcher
        setupSearchTextWatcher();

        // Check if we have a last searched city
        String lastCity = preferences.getString(LAST_CITY_KEY, null);
        if (lastCity != null && !lastCity.isEmpty()) {
            Log.d(TAG, "Loading weather for last city: " + lastCity);
            loadWeatherData(lastCity);
        } else {
            // Request location permission and load weather
            checkLocationPermissionAndLoadWeather();
        }

        Log.d(TAG, "onCreate: Activity creation completed");
    }

    private void initializeComponents() {
        mainLayout = findViewById(R.id.main_layout);
        weatherCard = findViewById(R.id.weather_card);
        cityNameTextView = findViewById(R.id.city_name);
        temperatureTextView = findViewById(R.id.temperature);
        weatherDescriptionTextView = findViewById(R.id.weather_description);
        dateTextView = findViewById(R.id.date);
        humidityTextView = findViewById(R.id.humidity_value);
        windSpeedTextView = findViewById(R.id.wind_value);
        weatherAnimationView = findViewById(R.id.weather_animation);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        historyButton = findViewById(R.id.history_button);
        forecastRecyclerView = findViewById(R.id.forecast_recycler_view);
        loadingAnimation = findViewById(R.id.loading_animation);
        forecastTitleTextView = findViewById(R.id.forecast_title);
    }

    /**
     * Properly applies the theme based on settings without causing recreation loops
     */
    private void applyThemeFromSettings() {
        if (themeInitialized) {
            return; // Avoid applying theme multiple times
        }

        int themePreference = SettingsManager.getInstance(this).getThemePreference();
        Log.d(TAG, "Applying theme preference: " + themePreference);

        switch (themePreference) {
            case SettingsManager.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case SettingsManager.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SettingsManager.THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        themeInitialized = true;
    }

    /**
     * Method to check if app is in dark mode
     */
    private boolean isInDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void setupForecastRecyclerView() {
        // Set up RecyclerView for weather forecast
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        forecastRecyclerView.setLayoutManager(layoutManager);

        // Important settings to solve scrolling issues
        forecastRecyclerView.setNestedScrollingEnabled(false);
        forecastRecyclerView.setHasFixedSize(false);

        // Add spacing between items
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.forecast_item_spacing);
        forecastRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(itemSpacing));

        // Set up adapter
        forecastList = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(forecastList);
        forecastRecyclerView.setAdapter(forecastAdapter);
    }

    private static class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int horizontalSpacing;

        public HorizontalSpaceItemDecoration(int horizontalSpacing) {
            this.horizontalSpacing = horizontalSpacing;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            // Add spacing for all items except the last one
            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.right = horizontalSpacing;
            }
        }
    }

    private void setupSearchButton() {
        searchButton.setOnClickListener(v -> {
            String city = searchEditText.getText().toString().trim();
            if (!city.isEmpty()) {
                AnimationHelper.bounceAnimation(searchButton);
                loadWeatherData(city);
                // Add city to search history
                addToSearchHistory(city);
                // Save last searched city
                preferences.edit().putString(LAST_CITY_KEY, city).apply();
            } else {
                Toast.makeText(MainActivity.this, "Будь ласка, введіть назву міста", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHistoryButton() {
        historyButton.setOnClickListener(v -> {
            AnimationHelper.bounceAnimation(historyButton);
            showSearchHistory();
        });
    }

    private void setupSearchTextWatcher() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    AnimationHelper.rotateAnimation(searchButton);
                }
            }
        });
    }

    private void checkLocationPermissionAndLoadWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If permission not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // If permission granted, get location
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getUserLocation();
            } else {
                // Permission denied, use default city
                Toast.makeText(this, "Використовується місто за замовчуванням - Київ", Toast.LENGTH_SHORT).show();
                loadWeatherData("Kyiv");
                preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
            }
        }
    }

    private void getUserLocation() {
        showLoading(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // This block won't execute as we've already checked permission,
            // but the compiler still requires this check
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Got location, now convert coordinates to city name
                            getCityNameFromLocation(location);
                        } else {
                            // Couldn't get location, use default city
                            Toast.makeText(MainActivity.this, "Не вдалося отримати місцезнаходження. Використовується Київ за замовчуванням.", Toast.LENGTH_SHORT).show();
                            loadWeatherData("Kyiv");
                            preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Помилка отримання місцезнаходження: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadWeatherData("Kyiv");
                    preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
                });
    }

    private void getCityNameFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                String cityName = addresses.get(0).getLocality();
                if (cityName != null && !cityName.isEmpty()) {
                    loadWeatherData(cityName);
                    // Add current city to search history
                    addToSearchHistory(cityName);
                    preferences.edit().putString(LAST_CITY_KEY, cityName).apply();
                } else {
                    // If couldn't get city name, try to use admin area or country name
                    String alternativeName = addresses.get(0).getAdminArea();
                    if (alternativeName == null || alternativeName.isEmpty()) {
                        alternativeName = addresses.get(0).getCountryName();
                    }
                    if (alternativeName != null && !alternativeName.isEmpty()) {
                        loadWeatherData(alternativeName);
                        // Add alternative name to search history
                        addToSearchHistory(alternativeName);
                        preferences.edit().putString(LAST_CITY_KEY, alternativeName).apply();
                    } else {
                        loadWeatherData("Kyiv");
                        preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
                    }
                }
            } else {
                loadWeatherData("Kyiv");
                preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Помилка геокодування: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadWeatherData("Kyiv");
            preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
        }
    }

    private void loadWeatherData(String city) {
        Log.d(TAG, "Loading weather data for: " + city);
        showLoading(true);

        // Get current weather
        weatherClient.getCurrentWeather(city, new WeatherCallback<CurrentWeather>() {
            @Override
            public void onSuccess(CurrentWeather data) {
                Log.d(TAG, "Current weather data loaded successfully");
                updateCurrentWeatherUI(data);

                // After successfully loading current weather, load forecast
                weatherClient.getForecast(city, new WeatherCallback<List<DailyForecast>>() {
                    @Override
                    public void onSuccess(List<DailyForecast> data) {
                        Log.d(TAG, "Forecast data loaded successfully");
                        updateForecastUI(data);
                        showLoading(false);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Error loading forecast: " + message);
                        Toast.makeText(MainActivity.this, "Помилка завантаження прогнозу: " + message, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading current weather: " + message);
                Toast.makeText(MainActivity.this, "Помилка: " + message, Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void updateCurrentWeatherUI(CurrentWeather weather) {
        Log.d(TAG, "Updating current weather UI, dark mode: " + isInDarkMode());

        // Fade animation for smooth update
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(weatherCard, "alpha", 1f, 0.3f);
        fadeOut.setDuration(500);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Update weather data
                cityNameTextView.setText(weather.getCityName());

                // Ensure text colors are appropriate for the current theme
                ensureTextVisibility();

                temperatureTextView.setText(SettingsManager.getInstance(MainActivity.this).formatTemperature(weather.getTemperature()));
                weatherDescriptionTextView.setText(weather.getDescription());

                // Update date
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("uk", "UA"));
                String formattedDate = dateFormat.format(new Date());
                dateTextView.setText(formattedDate);

                // Update additional info
                humidityTextView.setText(String.format(Locale.getDefault(), "%d%%", weather.getHumidity()));
                windSpeedTextView.setText(SettingsManager.getInstance(MainActivity.this).formatWindSpeed(weather.getWindSpeed()));

                // Set appropriate weather animation
                String animationFile = WeatherIconMapper.getAnimationForWeatherCode(weather.getWeatherCode());
                weatherAnimationView.setAnimation(animationFile);
                weatherAnimationView.playAnimation();

                // Update background according to current weather
                backgroundManager.updateBackground(weather.getWeatherCode(), weather.isDay());

                // Smooth return of visibility
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(weatherCard, "alpha", 0.3f, 1f);
                fadeIn.setDuration(500);
                fadeIn.start();
            }
        });

        fadeOut.start();
    }

    /**
     * Ensures text visibility based on current theme
     */
    private void ensureTextVisibility() {
        if (isInDarkMode()) {
            Log.d(TAG, "Ensuring text visibility in dark mode");
            cityNameTextView.setTextColor(ContextCompat.getColor(this, R.color.white));
            temperatureTextView.setTextColor(ContextCompat.getColor(this, R.color.white));
            weatherDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.white));
            dateTextView.setTextColor(ContextCompat.getColor(this, R.color.white));
            forecastTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.white));

            // Make sure card background is appropriate for dark mode
            weatherCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dark_background));
        } else {
            Log.d(TAG, "Ensuring text visibility in light mode");
            cityNameTextView.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
            temperatureTextView.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
            weatherDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.textColorSecondary));
            dateTextView.setTextColor(ContextCompat.getColor(this, R.color.textColorSecondary));
            forecastTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));

            // Make sure card background is appropriate for light mode
            weatherCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void updateForecastUI(List<DailyForecast> forecast) {
        forecastList.clear();
        forecastList.addAll(forecast);
        forecastAdapter.notifyDataSetChanged();

        // Animation for forecast list
        Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        forecastRecyclerView.startAnimation(slideInAnimation);
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation();
            weatherCard.setVisibility(View.INVISIBLE);
            forecastRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            loadingAnimation.cancelAnimation();
            loadingAnimation.setVisibility(View.GONE);
            weatherCard.setVisibility(View.VISIBLE);
            forecastRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Methods for working with search history

    /**
     * Add city to search history
     */
    private void addToSearchHistory(String city) {
        List<String> searchHistory = getSearchHistory();

        // Remove city if it's already in history (to add it again at the beginning)
        searchHistory.remove(city);

        // Add city to the beginning of the list
        searchHistory.add(0, city);

        // Limit history size
        if (searchHistory.size() > MAX_HISTORY_ITEMS) {
            searchHistory = searchHistory.subList(0, MAX_HISTORY_ITEMS);
        }

        // Save updated history
        saveSearchHistory(searchHistory);
    }

    /**
     * Get search history list
     */
    private List<String> getSearchHistory() {
        String historyJson = preferences.getString(SEARCH_HISTORY_KEY, null);
        if (historyJson == null) {
            return new ArrayList<>();
        } else {
            Type type = new TypeToken<List<String>>() {}.getType();
            return new Gson().fromJson(historyJson, type);
        }
    }

    /**
     * Save search history list
     */
    private void saveSearchHistory(List<String> searchHistory) {
        SharedPreferences.Editor editor = preferences.edit();
        String historyJson = new Gson().toJson(searchHistory);
        editor.putString(SEARCH_HISTORY_KEY, historyJson);
        editor.apply();
    }

    /**
     * Show search history dialog
     */
    private void showSearchHistory() {
        List<String> history = getSearchHistory();

        if (history.isEmpty()) {
            Toast.makeText(this, "Історія пошуку порожня", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create array for use in dialog
        final String[] historyArray = history.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Історія пошуку");

        builder.setItems(historyArray, (dialog, which) -> {
            String selectedCity = historyArray[which];
            searchEditText.setText(selectedCity);
            loadWeatherData(selectedCity);
            preferences.edit().putString(LAST_CITY_KEY, selectedCity).apply();
        });

        builder.setNegativeButton("Скасувати", null);
        builder.setNeutralButton("Очистити історію", (dialog, which) -> {
            clearSearchHistory();
            Toast.makeText(MainActivity.this, "Історію пошуку очищено", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    /**
     * Clear search history
     */
    private void clearSearchHistory() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(SEARCH_HISTORY_KEY);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Only check for theme changes without recreating the activity
        if (themeInitialized) {
            int currentTheme = SettingsManager.getInstance(this).getThemePreference();
            int currentNightMode = AppCompatDelegate.getDefaultNightMode();

            int expectedNightMode;
            switch (currentTheme) {
                case SettingsManager.THEME_LIGHT:
                    expectedNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    break;
                case SettingsManager.THEME_DARK:
                    expectedNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                    break;
                case SettingsManager.THEME_SYSTEM:
                default:
                    expectedNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    break;
            }

            // If theme changed in settings but not yet applied to the app
            if (currentNightMode != expectedNightMode) {
                Log.d(TAG, "Theme change detected in onResume, recreating activity");
                recreate();
                return;
            }
        }

        // Update UI for current theme
        ensureTextVisibility();

        // Refresh weather data if needed
        String lastCity = preferences.getString(LAST_CITY_KEY, null);
        if (lastCity != null && !lastCity.isEmpty() && weatherCard.getVisibility() != View.VISIBLE) {
            loadWeatherData(lastCity);
        }
    }
}