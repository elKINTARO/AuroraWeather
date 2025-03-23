package com.example.auroraweather;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String SEARCH_HISTORY_KEY = "SearchHistoryList";
    private static final int MAX_HISTORY_ITEMS = 10;

    private ConstraintLayout mainLayout;
    private CardView weatherCard;
    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView weatherDescriptionTextView;
    private TextView dateTextView;
    private TextView humidityTextView;
    private TextView windSpeedTextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ініціалізація SharedPreferences
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Ініціалізація UI компонентів
        initializeComponents();

        // Ініціалізація провайдера локації
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Налаштування клієнта API
        weatherClient = new OpenWeatherMapClient(this);

        // Налаштування менеджера фонів
        backgroundManager = new BackgroundManager(mainLayout);

        // Налаштування RecyclerView для прогнозу погоди
        setupForecastRecyclerView();

        // Налаштування кнопки пошуку
        setupSearchButton();

        // Налаштування кнопки історії
        setupHistoryButton();

        // Налаштування TextWatcher для поля пошуку
        setupSearchTextWatcher();

        // Запитуємо дозвіл на використання місцезнаходження та завантажуємо погоду
        checkLocationPermissionAndLoadWeather();
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
    }

    private void setupForecastRecyclerView() {
        // Налаштування RecyclerView для прогнозу погоди
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        forecastRecyclerView.setLayoutManager(layoutManager);

        // Важливі налаштування для вирішення проблем з прокруткою
        forecastRecyclerView.setNestedScrollingEnabled(false);
        forecastRecyclerView.setHasFixedSize(false);

        // Додавання відступів між елементами
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.forecast_item_spacing);
        forecastRecyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(itemSpacing));

        // Налаштування адаптера
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
            // Додаємо відступ для всіх елементів, крім останнього
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
                // Додаємо місто до історії пошуку
                addToSearchHistory(city);
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
            // Якщо дозвіл не надано, запитуємо його
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Якщо дозвіл надано, отримуємо місцезнаходження
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Дозвіл надано, отримуємо місцезнаходження
                getUserLocation();
            } else {
                // Дозвіл не надано, використовуємо місто за замовчуванням
                Toast.makeText(this, "Використовується місто за замовчуванням - Київ", Toast.LENGTH_SHORT).show();
                loadWeatherData("Kyiv");
            }
        }
    }

    private void getUserLocation() {
        showLoading(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Цей блок не буде виконуватись, тому що ми вже перевірили дозвіл,
            // але компілятор все одно вимагає цю перевірку
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Отримали місцезнаходження, тепер перетворюємо координати на назву міста
                            getCityNameFromLocation(location);
                        } else {
                            // Не вдалося отримати місцезнаходження, використовуємо місто за замовчуванням
                            Toast.makeText(MainActivity.this, "Не вдалося отримати місцезнаходження. Використовується Київ за замовчуванням.", Toast.LENGTH_SHORT).show();
                            loadWeatherData("Kyiv");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Помилка отримання місцезнаходження: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadWeatherData("Kyiv");
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
                    // Додаємо поточне місто до історії пошуку
                    addToSearchHistory(cityName);
                } else {
                    // Якщо не вдалося отримати назву міста, спробуємо використати назву країни або адміністративну область
                    String alternativeName = addresses.get(0).getAdminArea();
                    if (alternativeName == null || alternativeName.isEmpty()) {
                        alternativeName = addresses.get(0).getCountryName();
                    }
                    if (alternativeName != null && !alternativeName.isEmpty()) {
                        loadWeatherData(alternativeName);
                        // Додаємо альтернативну назву до історії пошуку
                        addToSearchHistory(alternativeName);
                    } else {
                        loadWeatherData("Kyiv");
                    }
                }
            } else {
                loadWeatherData("Kyiv");
            }
        } catch (IOException e) {
            Toast.makeText(this, "Помилка геокодування: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadWeatherData("Kyiv");
        }
    }

    private void loadWeatherData(String city) {
        showLoading(true);

        // Отримання поточної погоди
        weatherClient.getCurrentWeather(city, new WeatherCallback<CurrentWeather>() {
            @Override
            public void onSuccess(CurrentWeather data) {
                updateCurrentWeatherUI(data);

                // Після успішного завантаження поточної погоди, завантажуємо прогноз
                weatherClient.getForecast(city, new WeatherCallback<List<DailyForecast>>() {
                    @Override
                    public void onSuccess(List<DailyForecast> data) {
                        updateForecastUI(data);
                        showLoading(false);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(MainActivity.this, "Помилка завантаження прогнозу: " + message, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, "Помилка: " + message, Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void updateCurrentWeatherUI(CurrentWeather weather) {
        // Анімація фейдінгу для плавного оновлення
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(weatherCard, "alpha", 1f, 0.3f);
        fadeOut.setDuration(500);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Оновлення даних погоди
                cityNameTextView.setText(weather.getCityName());
                temperatureTextView.setText(String.format(Locale.getDefault(), "%.1f°C", weather.getTemperature()));
                weatherDescriptionTextView.setText(weather.getDescription());

                // Оновлення дати
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("uk", "UA"));
                dateTextView.setText(dateFormat.format(new Date()));

                // Оновлення додаткової інформації
                humidityTextView.setText(String.format(Locale.getDefault(), "%d%%", weather.getHumidity()));
                windSpeedTextView.setText(String.format(Locale.getDefault(), "%.1f m/s", weather.getWindSpeed()));

                // Встановлення відповідної анімації погоди
                String animationFile = WeatherIconMapper.getAnimationForWeatherCode(weather.getWeatherCode());
                weatherAnimationView.setAnimation(animationFile);
                weatherAnimationView.playAnimation();

                // Оновлення фону відповідно до поточної погоди
                backgroundManager.updateBackground(weather.getWeatherCode(), weather.isDay());

                // Плавне повернення видимості
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(weatherCard, "alpha", 0.3f, 1f);
                fadeIn.setDuration(500);
                fadeIn.start();
            }
        });

        fadeOut.start();
    }

    private void updateForecastUI(List<DailyForecast> forecast) {
        forecastList.clear();
        forecastList.addAll(forecast);
        forecastAdapter.notifyDataSetChanged();

        // Анімація для списку прогнозу
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

    // Методи для роботи з історією пошуку

    /**
     * Додає місто до історії пошуку
     */
    private void addToSearchHistory(String city) {
        List<String> searchHistory = getSearchHistory();

        // Видаляємо місто, якщо воно вже є в історії (щоб додати його знову на початок)
        searchHistory.remove(city);

        // Додаємо місто на початок списку
        searchHistory.add(0, city);

        // Обмежуємо розмір історії
        if (searchHistory.size() > MAX_HISTORY_ITEMS) {
            searchHistory = searchHistory.subList(0, MAX_HISTORY_ITEMS);
        }

        // Зберігаємо оновлену історію
        saveSearchHistory(searchHistory);
    }

    /**
     * Отримує список історії пошуку
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
     * Зберігає список історії пошуку
     */
    private void saveSearchHistory(List<String> searchHistory) {
        SharedPreferences.Editor editor = preferences.edit();
        String historyJson = new Gson().toJson(searchHistory);
        editor.putString(SEARCH_HISTORY_KEY, historyJson);
        editor.apply();
    }

    /**
     * Показує діалог з історією пошуку
     */
    private void showSearchHistory() {
        List<String> history = getSearchHistory();

        if (history.isEmpty()) {
            Toast.makeText(this, "Історія пошуку порожня", Toast.LENGTH_SHORT).show();
            return;
        }

        // Створюємо масив для використання в діалозі
        final String[] historyArray = history.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Історія пошуку");

        builder.setItems(historyArray, (dialog, which) -> {
            String selectedCity = historyArray[which];
            searchEditText.setText(selectedCity);
            loadWeatherData(selectedCity);
        });

        builder.setNegativeButton("Скасувати", null);
        builder.setNeutralButton("Очистити історію", (dialog, which) -> {
            clearSearchHistory();
            Toast.makeText(MainActivity.this, "Історію пошуку очищено", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    /**
     * Очищає історію пошуку
     */
    private void clearSearchHistory() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(SEARCH_HISTORY_KEY);
        editor.apply();
    }
}