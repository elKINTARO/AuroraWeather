package com.example.auroraweather;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
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
import com.example.auroraweather.adapters.HourlyAdapter;
import com.example.auroraweather.api.OpenWeatherMapClient;
import com.example.auroraweather.api.WeatherCallback;
import com.example.auroraweather.models.CurrentWeather;
import com.example.auroraweather.models.DailyForecast;
import com.example.auroraweather.models.HourlyForecast;
import com.example.auroraweather.utils.AnimationHelper;
import com.example.auroraweather.utils.BackgroundManager;
import com.example.auroraweather.utils.LocalizationManager;
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

    // UI Components
    private ConstraintLayout mainLayout;
    private CardView weatherCard;
    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView weatherDescriptionTextView;
    private TextView dateTextView;
    private TextView humidityTextView;
    private TextView windSpeedTextView;
    private TextView forecastTitleTextView;
    private TextView pressureTextView;
    private TextView visibilityTextView;
    private TextView uvIndexTextView;
    private LottieAnimationView weatherAnimationView;
    private View weatherFront;
    private View weatherBack;
    private EditText searchEditText;
    private FloatingActionButton searchButton;
    private FloatingActionButton historyButton;
    private RecyclerView forecastRecyclerView;
    private RecyclerView hourlyRecyclerView;
    private LottieAnimationView loadingAnimation;
    
    // Adapters
    private ForecastAdapter forecastAdapter;
    private HourlyAdapter hourlyAdapter;
    
    // Managers
    private BackgroundManager backgroundManager;

    private OpenWeatherMapClient weatherClient;
    private List<DailyForecast> forecastList;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences preferences;
    private boolean themeInitialized = false; // Flag to track theme initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply localization
        LocalizationManager.getInstance(this).updateLocale(this);
        
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

        // Set up hourly recycler
        setupHourlyRecyclerView();

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
        weatherFront = findViewById(R.id.weather_front);
        weatherBack = findViewById(R.id.weather_back);
        cityNameTextView = findViewById(R.id.city_name);
        temperatureTextView = findViewById(R.id.temperature);
        weatherDescriptionTextView = findViewById(R.id.weather_description);
        dateTextView = findViewById(R.id.date);
        humidityTextView = findViewById(R.id.humidity_value);
        windSpeedTextView = findViewById(R.id.wind_value);
        pressureTextView = findViewById(R.id.pressure_value);
        visibilityTextView = findViewById(R.id.visibility_value);
        uvIndexTextView = findViewById(R.id.uv_value);
        weatherAnimationView = findViewById(R.id.weather_animation);
        searchEditText = findViewById(R.id.search_edit_text);
        searchEditText.setHint(LocalizationManager.getInstance(this).getString("search_hint"));
        searchButton = findViewById(R.id.search_button);
        historyButton = findViewById(R.id.history_button);
        forecastRecyclerView = findViewById(R.id.forecast_recycler_view);
        hourlyRecyclerView = findViewById(R.id.hourly_recycler_view);
        loadingAnimation = findViewById(R.id.loading_animation);
        forecastTitleTextView = findViewById(R.id.forecast_title);
        forecastTitleTextView.setText(LocalizationManager.getInstance(this).getString("forecast_title"));
        TextView hourlyTitle = findViewById(R.id.hourly_title);
        if (hourlyTitle != null) {
            hourlyTitle.setText(LocalizationManager.getInstance(this).getString("hourly_title"));
        }
        // Localize labels under icons
        TextView humidityLabel = findViewById(R.id.humidity_label);
        TextView windLabel = findViewById(R.id.wind_label);
        TextView pressureLabel = findViewById(R.id.pressure_label);
        TextView visibilityLabel = findViewById(R.id.visibility_label);
        TextView uvLabel = findViewById(R.id.uv_label);
        LocalizationManager lm = LocalizationManager.getInstance(this);
        if (humidityLabel != null) humidityLabel.setText(lm.getString("humidity"));
        if (windLabel != null) windLabel.setText(lm.getString("wind"));
        if (pressureLabel != null) pressureLabel.setText(lm.getString("pressure"));
        if (visibilityLabel != null) visibilityLabel.setText(lm.getString("visibility_short"));
        if (uvLabel != null) uvLabel.setText(lm.getString("uv"));

        // Set up flip buttons
        setupFlipButtons();
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

    private void setupHourlyRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        hourlyRecyclerView.setLayoutManager(layoutManager);
        hourlyRecyclerView.setNestedScrollingEnabled(false);
        hourlyRecyclerView.setHasFixedSize(false);

        hourlyAdapter = new HourlyAdapter(new ArrayList<>(), SettingsManager.getInstance(this));
        hourlyRecyclerView.setAdapter(hourlyAdapter);
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
                Toast.makeText(MainActivity.this, LocalizationManager.getInstance(MainActivity.this).getString("please_enter_city"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHistoryButton() {
        historyButton.setOnClickListener(v -> {
            AnimationHelper.bounceAnimation(historyButton);
            showSearchHistory();
        });
    }

    private void setupFlipButtons() {
        FloatingActionButton flipButtonFront = findViewById(R.id.flip_button_front);
        FloatingActionButton flipButtonBack = findViewById(R.id.flip_button_back);
        
        if (flipButtonFront != null) {
            flipButtonFront.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(flipButtonFront);
                toggleFlip();
            });
        }
        
        if (flipButtonBack != null) {
            flipButtonBack.setOnClickListener(v -> {
                AnimationHelper.bounceAnimation(flipButtonBack);
                toggleFlip();
            });
        }
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
                Toast.makeText(this, LocalizationManager.getInstance(this).getString("default_city_used"), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(MainActivity.this, LocalizationManager.getInstance(MainActivity.this).getString("location_error"), Toast.LENGTH_SHORT).show();
                            loadWeatherData("Kyiv");
                            preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMsg = LocalizationManager.getInstance(MainActivity.this).getString("location_error_with_message").replace("{0}", e.getMessage());
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    loadWeatherData("Kyiv");
                    preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
                });
    }

    private void getCityNameFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lon, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(List<Address> addresses) {
                    runOnUiThread(() -> handleAddressesResult(addresses));
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        String errorMsg = LocalizationManager.getInstance(MainActivity.this).getString("geocoding_error").replace("{0}", errorMessage);
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        loadWeatherData("Kyiv");
                        preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
                    });
                }
            });
        } else {
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                handleAddressesResult(addresses);
            } catch (IOException e) {
                String errorMsg = LocalizationManager.getInstance(this).getString("geocoding_error").replace("{0}", e.getMessage());
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                loadWeatherData("Kyiv");
                preferences.edit().putString(LAST_CITY_KEY, "Kyiv").apply();
            }
        }
    }

    private void handleAddressesResult(List<Address> addresses) {
        if (addresses != null && addresses.size() > 0) {
            String cityName = addresses.get(0).getLocality();
            if (cityName != null && !cityName.isEmpty()) {
                loadWeatherData(cityName);
                addToSearchHistory(cityName);
                preferences.edit().putString(LAST_CITY_KEY, cityName).apply();
            } else {
                String alternativeName = addresses.get(0).getAdminArea();
                if (alternativeName == null || alternativeName.isEmpty()) {
                    alternativeName = addresses.get(0).getCountryName();
                }
                if (alternativeName != null && !alternativeName.isEmpty()) {
                    loadWeatherData(alternativeName);
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
                        String errorMsg = LocalizationManager.getInstance(MainActivity.this).getString("forecast_error").replace("{0}", message);
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });

                // Load hourly forecast in parallel for back side
                weatherClient.getHourlyForecast(city, new WeatherCallback<List<HourlyForecast>>() {
                    @Override
                    public void onSuccess(List<HourlyForecast> data) {
                        Log.d(TAG, "Hourly forecast loaded successfully with " + data.size() + " items");
                        updateHourlyUI(data);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Error loading hourly: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading current weather: " + message);
                String errorMsg = LocalizationManager.getInstance(MainActivity.this).getString("error").replace("{0}", message);
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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
                // Use locale according to selected language
                String languageCode = SettingsManager.getInstance(MainActivity.this).getLanguageCode();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale(languageCode, "UA"));
                String formattedDate = dateFormat.format(new Date());
                dateTextView.setText(formattedDate);

                // Update additional info
                humidityTextView.setText(String.format(Locale.getDefault(), "%d%%", weather.getHumidity()));
                
                // Форматування швидкості вітру
                String windSpeedFormatted = SettingsManager.getInstance(MainActivity.this).formatWindSpeedCompact(weather.getWindSpeed());
                windSpeedTextView.setText(windSpeedFormatted);
                
                // Форматування тиску - тільки число без одиниць виміру
                pressureTextView.setText(String.format(Locale.getDefault(), "%d", weather.getPressure()));
                
                // Для відображення видимості - спрощений формат
                float visibilityInKm = weather.getVisibility() / 1000.0f;
                if (visibilityInKm >= 10) {
                    visibilityTextView.setText("10+");
                } else {
                    visibilityTextView.setText(String.format(Locale.getDefault(), "%.1f", visibilityInKm));
                }
                
                // Форматування UV-індексу з відповідним кольором
                double uvIndex = weather.getUvIndex();
                uvIndexTextView.setText(String.format(Locale.getDefault(), "%.1f", uvIndex));
                
                // Встановлення кольору для UV-індексу в залежності від його значення
                if (uvIndex < 3) {
                    // Низький рівень UV (зелений)
                    uvIndexTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.uv_low));
                } else if (uvIndex < 6) {
                    // Середній рівень UV (жовтий)
                    uvIndexTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.uv_medium));
                } else if (uvIndex < 8) {
                    // Високий рівень UV (оранжевий)
                    uvIndexTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.uv_high));
                } else {
                    // Дуже високий рівень UV (червоний)
                    uvIndexTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.uv_very_high));
                }

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
            setTextColors(R.color.white, R.color.white, R.color.white, R.color.white, R.color.white);
            weatherCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background_dark));
        } else {
            Log.d(TAG, "Ensuring text visibility in light mode");
            setTextColors(R.color.textColorPrimary, R.color.textColorPrimary, R.color.textColorSecondary, 
                         R.color.textColorSecondary, R.color.textColorPrimary);
            weatherCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background_light));
        }
    }
    
    private void setTextColors(int cityColor, int tempColor, int descColor, int dateColor, int titleColor) {
        cityNameTextView.setTextColor(ContextCompat.getColor(this, cityColor));
        temperatureTextView.setTextColor(ContextCompat.getColor(this, tempColor));
        weatherDescriptionTextView.setTextColor(ContextCompat.getColor(this, descColor));
        dateTextView.setTextColor(ContextCompat.getColor(this, dateColor));
        forecastTitleTextView.setTextColor(ContextCompat.getColor(this, titleColor));
    }

    private void updateForecastUI(List<DailyForecast> forecast) {
        forecastAdapter.updateData(forecast);

        // Animation for forecast list
        Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        forecastRecyclerView.startAnimation(slideInAnimation);
    }

    private void updateHourlyUI(List<HourlyForecast> hours) {
        Log.d(TAG, "updateHourlyUI called with " + (hours != null ? hours.size() : 0) + " items");
        if (hourlyAdapter == null) {
            Log.e(TAG, "hourlyAdapter is null!");
            return;
        }
        if (hourlyRecyclerView == null) {
            Log.e(TAG, "hourlyRecyclerView is null!");
            return;
        }
        if (hours == null || hours.isEmpty()) {
            Log.w(TAG, "No hourly forecast data to display");
            return;
        }
        
        // Update existing adapter data instead of creating new one
        hourlyAdapter.updateData(hours);
        Log.d(TAG, "Hourly forecast UI updated successfully");
    }

    private boolean isFlipped = false;
    private void toggleFlip() {
        final View front = weatherFront;
        final View back = weatherBack;
        if (front == null || back == null) return;

        float scale = getResources().getDisplayMetrics().density;
        front.setCameraDistance(8000 * scale);
        back.setCameraDistance(8000 * scale);

        if (!isFlipped) {
            front.animate().rotationY(90f).setDuration(350).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(() -> {
                front.setVisibility(View.GONE);
                back.setRotationY(-90f);
                back.setVisibility(View.VISIBLE);
                back.animate().rotationY(0f).setDuration(350).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }).start();
        } else {
            back.animate().rotationY(90f).setDuration(350).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(() -> {
                back.setVisibility(View.GONE);
                front.setRotationY(-90f);
                front.setVisibility(View.VISIBLE);
                front.animate().rotationY(0f).setDuration(350).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }).start();
        }
        isFlipped = !isFlipped;
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
            Toast.makeText(this, LocalizationManager.getInstance(this).getString("empty_history"), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create array for use in dialog
        final String[] historyArray = history.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(LocalizationManager.getInstance(this).getString("search_history"));

        builder.setItems(historyArray, (dialog, which) -> {
            String selectedCity = historyArray[which];
            searchEditText.setText(selectedCity);
            loadWeatherData(selectedCity);
            preferences.edit().putString(LAST_CITY_KEY, selectedCity).apply();
        });

        builder.setNegativeButton(LocalizationManager.getInstance(this).getString("cancel"), null);
        builder.setNeutralButton(LocalizationManager.getInstance(this).getString("clear_history"), (dialog, which) -> {
            clearSearchHistory();
            Toast.makeText(MainActivity.this, LocalizationManager.getInstance(MainActivity.this).getString("history_cleared"), Toast.LENGTH_SHORT).show();
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