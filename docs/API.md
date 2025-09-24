# 🌐 API Documentation - Aurora Weather

Цей документ описує API інтеграції та використання зовнішніх сервісів в Aurora Weather.

## 📋 Зміст

- [OpenWeatherMap API](#openweathermap-api)
- [Google Play Services](#google-play-services)
- [Внутрішні API](#внутрішні-api)
- [Обробка помилок](#обробка-помилок)
- [Кешування](#кешування)
- [Приклади використання](#приклади-використання)

## 🌤️ OpenWeatherMap API

### Огляд

Aurora Weather використовує кілька API від OpenWeatherMap для отримання повної інформації про погоду.

### API Endpoints

#### 1. **Current Weather API**
```
GET https://api.openweathermap.org/data/2.5/weather
```

**Параметри:**
- `lat` - широта (обов'язково)
- `lon` - довгота (обов'язково)
- `units` - одиниці вимірювання (metric)
- `appid` - API ключ (обов'язково)
- `lang` - мова відповіді (en, uk)

**Приклад запиту:**
```java
String url = "https://api.openweathermap.org/data/2.5/weather?lat=50.4501&lon=30.5234&units=metric&appid=YOUR_API_KEY&lang=uk";
```

**Приклад відповіді:**
```json
{
  "coord": {
    "lon": 30.5234,
    "lat": 50.4501
  },
  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "ясно",
      "icon": "01d"
    }
  ],
  "main": {
    "temp": 23.5,
    "feels_like": 24.2,
    "temp_min": 21.0,
    "temp_max": 26.0,
    "pressure": 1013,
    "humidity": 65
  },
  "wind": {
    "speed": 3.2,
    "deg": 180
  },
  "visibility": 10000,
  "dt": 1640995200,
  "sys": {
    "sunrise": 1640952000,
    "sunset": 1640988000
  },
  "name": "Kyiv"
}
```

#### 2. **5 Day Forecast API**
```
GET https://api.openweathermap.org/data/2.5/forecast
```

**Параметри:**
- `q` - назва міста (обов'язково)
- `units` - одиниці вимірювання (metric)
- `appid` - API ключ (обов'язково)
- `lang` - мова відповіді (en, uk)

**Приклад відповіді:**
```json
{
  "list": [
    {
      "dt": 1640995200,
      "main": {
        "temp": 23.5,
        "temp_min": 21.0,
        "temp_max": 26.0,
        "pressure": 1013,
        "humidity": 65
      },
      "weather": [
        {
          "id": 800,
          "main": "Clear",
          "description": "ясно",
          "icon": "01d"
        }
      ],
      "wind": {
        "speed": 3.2,
        "deg": 180
      }
    }
  ]
}
```

#### 3. **Geocoding API**
```
GET https://api.openweathermap.org/geo/1.0/direct
```

**Параметри:**
- `q` - назва міста (обов'язково)
- `limit` - кількість результатів (1)
- `appid` - API ключ (обов'язково)
- `lang` - мова відповіді (en, uk)

**Приклад відповіді:**
```json
[
  {
    "name": "Kyiv",
    "local_names": {
      "uk": "Київ",
      "en": "Kyiv"
    },
    "lat": 50.4501,
    "lon": 30.5234,
    "country": "UA"
  }
]
```

#### 4. **OneCall API**
```
GET https://api.openweathermap.org/data/3.0/onecall
```

**Параметри:**
- `lat` - широта (обов'язково)
- `lon` - довгота (обов'язково)
- `exclude` - виключити дані (minutely,hourly,daily,alerts)
- `units` - одиниці вимірювання (metric)
- `appid` - API ключ (обов'язково)
- `lang` - мова відповіді (en, uk)

**Приклад відповіді:**
```json
{
  "current": {
    "dt": 1640995200,
    "uvi": 5.2,
    "temp": 23.5
  }
}
```

### Коди погоди

| Код | Опис | Анімація |
|-----|------|----------|
| 200-299 | Гроза | weather_thunder.json |
| 300-399 | Мряка | weather_drizzle.json |
| 500-599 | Дощ | weather_rain.json |
| 600-699 | Сніг | weather_snow.json |
| 700-799 | Туман | weather_fog.json |
| 800 | Ясно | weather_sunny.json |
| 801-899 | Хмарно | weather_cloudy.json |

## 📍 Google Play Services

### Location Services

Aurora Weather використовує Google Play Services для отримання поточної локації користувача.

**Дозволи:**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

**Використання:**
```java
FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
    fusedLocationClient.getLastLocation()
        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    // Використовувати координати для отримання погоди
                }
            }
        });
}
```

## 🔧 Внутрішні API

### OpenWeatherMapClient

Основний клас для роботи з OpenWeatherMap API.

```java
public class OpenWeatherMapClient {
    private static final String API_KEY = "your_api_key";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String ONE_CALL_URL = "https://api.openweathermap.org/data/3.0/onecall";
    
    private final RequestQueue requestQueue;
    private final Context context;
    
    public OpenWeatherMapClient(Context context) {
        this.context = context.getApplicationContext();
        requestQueue = Volley.newRequestQueue(this.context);
    }
    
    // Методи для отримання даних погоди
    public void getCurrentWeather(String city, WeatherCallback<CurrentWeather> callback);
    public void getForecast(String city, WeatherCallback<List<DailyForecast>> callback);
    public void getHourlyForecast(String city, WeatherCallback<List<HourlyForecast>> callback);
}
```

### WeatherCallback Interface

```java
public interface WeatherCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}
```

### Приклад використання

```java
OpenWeatherMapClient weatherClient = new OpenWeatherMapClient(context);

// Отримання поточної погоди
weatherClient.getCurrentWeather("Kyiv", new WeatherCallback<CurrentWeather>() {
    @Override
    public void onSuccess(CurrentWeather data) {
        // Оновлення UI з даними погоди
        updateWeatherUI(data);
    }
    
    @Override
    public void onError(String message) {
        // Показ помилки користувачу
        showError(message);
    }
});
```

## ⚠️ Обробка помилок

### Типи помилок

1. **Мережеві помилки**
   - Немає інтернет-з'єднання
   - Таймаут запиту
   - Помилка сервера

2. **API помилки**
   - Неправильний API ключ
   - Місто не знайдено
   - Перевищено ліміт запитів

3. **Помилки парсингу**
   - Неправильний JSON формат
   - Відсутні обов'язкові поля

### Обробка помилок

```java
public void getCurrentWeather(String city, final WeatherCallback<CurrentWeather> callback) {
    String url = buildWeatherUrl(city);
    
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
        response -> {
            try {
                CurrentWeather weather = parseCurrentWeatherResponse(response);
                callback.onSuccess(weather);
            } catch (JSONException e) {
                callback.onError("Помилка обробки даних погоди");
            }
        },
        error -> {
            String errorMessage = getErrorMessage(error);
            callback.onError(errorMessage);
        }
    );
    
    requestQueue.add(request);
}

private String getErrorMessage(VolleyError error) {
    if (error instanceof NetworkError) {
        return "Помилка мережі. Перевірте інтернет-з'єднання.";
    } else if (error instanceof TimeoutError) {
        return "Таймаут запиту. Спробуйте пізніше.";
    } else if (error instanceof ServerError) {
        return "Помилка сервера. Спробуйте пізніше.";
    } else {
        return "Невідома помилка: " + error.getMessage();
    }
}
```

## 💾 Кешування

### Поточне кешування

Наразі Aurora Weather не має вбудованого кешування, але планується додати:

```java
public class WeatherCache {
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 хвилин
    private Map<String, CachedWeather> cache = new HashMap<>();
    
    public void saveCurrentWeather(String city, CurrentWeather weather) {
        cache.put(city, new CachedWeather(weather, System.currentTimeMillis()));
    }
    
    public CurrentWeather getCurrentWeather(String city) {
        CachedWeather cached = cache.get(city);
        if (cached != null && !cached.isExpired()) {
            return cached.getWeather();
        }
        return null;
    }
    
    private boolean isExpired(long timestamp) {
        return System.currentTimeMillis() - timestamp > CACHE_DURATION;
    }
}
```

### Рекомендації для кешування

1. **Кешування в SharedPreferences**
   - Для налаштувань користувача
   - Для історії пошуку

2. **Кешування в пам'яті**
   - Для поточної погоди
   - Для прогнозу на кілька днів

3. **Кешування на диску**
   - Для великих об'ємів даних
   - Для офлайн режиму

## 📊 Приклади використання

### Повний цикл завантаження погоди

```java
public void loadWeatherData(String city) {
    showLoading(true);
    
    // 1. Отримання координат міста
    getCityCoordinates(city, new WeatherCallback<Location>() {
        @Override
        public void onSuccess(Location location) {
            // 2. Отримання поточної погоди
            getCurrentWeatherByCoordinates(location, new WeatherCallback<CurrentWeather>() {
                @Override
                public void onSuccess(CurrentWeather currentWeather) {
                    updateCurrentWeatherUI(currentWeather);
                    
                    // 3. Отримання прогнозу
                    getForecast(city, new WeatherCallback<List<DailyForecast>>() {
                        @Override
                        public void onSuccess(List<DailyForecast> forecast) {
                            updateForecastUI(forecast);
                            showLoading(false);
                        }
                        
                        @Override
                        public void onError(String message) {
                            showError(message);
                            showLoading(false);
                        }
                    });
                }
                
                @Override
                public void onError(String message) {
                    showError(message);
                    showLoading(false);
                }
            });
        }
        
        @Override
        public void onError(String message) {
            showError(message);
            showLoading(false);
        }
    });
}
```

### Обробка зміни налаштувань

```java
public void onSettingsChanged() {
    // Оновлення форматування температури
    String formattedTemp = SettingsManager.getInstance(this).formatTemperature(currentTemp);
    temperatureTextView.setText(formattedTemp);
    
    // Оновлення форматування швидкості вітру
    String formattedWind = SettingsManager.getInstance(this).formatWindSpeedCompact(currentWindSpeed);
    windSpeedTextView.setText(formattedWind);
    
    // Оновлення мови
    LocalizationManager.getInstance(this).setLanguage(newLanguage);
    updateLocalizedTexts();
}
```

## 🔒 Безпека

### Захист API ключа

1. **Винесення в BuildConfig**
```gradle
android {
    buildTypes {
        debug {
            buildConfigField "String", "API_KEY", "\"your_debug_api_key\""
        }
        release {
            buildConfigField "String", "API_KEY", "\"your_release_api_key\""
        }
    }
}
```

2. **Використання в коді**
```java
private static final String API_KEY = BuildConfig.API_KEY;
```

3. **Obfuscation**
```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### Валідація даних

```java
private boolean isValidCity(String city) {
    return city != null && !city.trim().isEmpty() && city.length() >= 2;
}

private boolean isValidCoordinates(double lat, double lon) {
    return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
}
```

## 📈 Моніторинг та аналітика

### Логування

```java
private static final String TAG = "OpenWeatherMapClient";

Log.d(TAG, "Loading weather for city: " + city);
Log.e(TAG, "Error loading weather: " + error.getMessage());
```

### Метрики

- Кількість успішних запитів
- Час відповіді API
- Кількість помилок
- Популярні міста

---

Ця документація допоможе розробникам розуміти, як працює API інтеграція в Aurora Weather та як її розширювати.
