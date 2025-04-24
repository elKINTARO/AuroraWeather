package com.example.auroraweather.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.auroraweather.models.CurrentWeather;
import com.example.auroraweather.models.DailyForecast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OpenWeatherMapClient {
    private static final String TAG = "OpenWeatherMapClient";
    private static final String API_KEY = "47b3867d61b81a3ba1b3bf943f11dc62"; // Потрібно замінити на ваш ключ API
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String ONE_CALL_URL = "https://api.openweathermap.org/data/3.0/onecall";

    private final RequestQueue requestQueue;

    public OpenWeatherMapClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void getCurrentWeather(String city, final WeatherCallback<CurrentWeather> callback) {
        // Спочатку отримуємо координати для міста
        String geoUrl = "https://api.openweathermap.org/geo/1.0/direct?q=" + city + "&limit=1&appid=" + API_KEY;
        
        // Використовуємо JsonArrayRequest замість JsonObjectRequest, оскільки відповідь - це JSON масив
        JsonArrayRequest geoRequest = new JsonArrayRequest(Request.Method.GET, geoUrl, null,
                geoResponse -> {
                    try {
                        // Перевіряємо, чи масив не порожній
                        if (geoResponse != null && geoResponse.length() > 0) {
                            JSONObject location = geoResponse.getJSONObject(0);
                            double lat = location.getDouble("lat");
                            double lon = location.getDouble("lon");
                            String cityName = location.getString("name");
                            
                            // Отримуємо поточну погоду, використовуючи координати
                            String weatherUrl = BASE_URL + "weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;
                            
                            JsonObjectRequest weatherRequest = new JsonObjectRequest(Request.Method.GET, weatherUrl, null,
                                    weatherResponse -> {
                                        try {
                                            // Отримуємо дані індексу UV через OneCall API
                                            String oneCallUrl = ONE_CALL_URL + "?lat=" + lat + "&lon=" + lon + "&exclude=minutely,hourly,daily,alerts&units=metric&appid=" + API_KEY;
                                            
                                            JsonObjectRequest uvRequest = new JsonObjectRequest(Request.Method.GET, oneCallUrl, null,
                                                    uvResponse -> {
                                                        try {
                                                            double uvIndex = 0.0;
                                                            if (uvResponse.has("current") && uvResponse.getJSONObject("current").has("uvi")) {
                                                                uvIndex = uvResponse.getJSONObject("current").getDouble("uvi");
                                                            }
                                                            
                                                            // Комбінуємо всі дані
                                                            weatherResponse.put("uvi", uvIndex);
                                                            CurrentWeather weather = parseCurrentWeatherResponse(weatherResponse, cityName);
                                                            callback.onSuccess(weather);
                                                        } catch (JSONException e) {
                                                            Log.e(TAG, "Error parsing UV data", e);
                                                            // Навіть якщо не вдалося отримати UV-дані, продовжуємо з погодою
                                                            try {
                                                                CurrentWeather weather = parseCurrentWeatherResponse(weatherResponse, cityName);
                                                                callback.onSuccess(weather);
                                                            } catch (JSONException ex) {
                                                                callback.onError("Помилка обробки даних погоди");
                                                            }
                                                        }
                                                    },
                                                    error -> {
                                                        Log.e(TAG, "Error fetching UV data", error);
                                                        // Навіть якщо не вдалося отримати UV-дані, продовжуємо з погодою
                                                        try {
                                                            CurrentWeather weather = parseCurrentWeatherResponse(weatherResponse, cityName);
                                                            callback.onSuccess(weather);
                                                        } catch (JSONException e) {
                                                            callback.onError("Помилка обробки даних погоди");
                                                        }
                                                    });
                                            
                                            requestQueue.add(uvRequest);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error processing weather data", e);
                                            callback.onError("Помилка обробки даних погоди");
                                        }
                                    },
                                    error -> {
                                        Log.e(TAG, "Error fetching weather data", error);
                                        callback.onError("Не вдалося завантажити дані погоди. Перевірте з'єднання або назву міста.");
                                    });
                            
                            requestQueue.add(weatherRequest);
                        } else {
                            callback.onError("Місто не знайдено");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing geo data", e);
                        callback.onError("Помилка обробки даних локації");
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching geo data", error);
                    callback.onError("Не вдалося визначити координати для вказаного міста");
                });
        
        requestQueue.add(geoRequest);
    }

    public void getForecast(String city, final WeatherCallback<List<DailyForecast>> callback) {
        String url = BASE_URL + "forecast?q=" + city + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<DailyForecast> forecast = parseForecastResponse(response);
                        callback.onSuccess(forecast);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing forecast data", e);
                        callback.onError("Помилка обробки даних прогнозу");
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching forecast data", error);
                    callback.onError("Не вдалося завантажити прогноз. Перевірте з'єднання.");
                });

        requestQueue.add(request);
    }

    private CurrentWeather parseCurrentWeatherResponse(JSONObject response, String cityName) throws JSONException {
        JSONObject main = response.getJSONObject("main");
        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");
        int pressure = main.getInt("pressure"); // Отримуємо значення тиску (в hPa)

        JSONObject wind = response.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");

        JSONArray weatherArray = response.getJSONArray("weather");
        JSONObject weatherData = weatherArray.getJSONObject(0);
        String description = weatherData.getString("description");
        int weatherCode = weatherData.getInt("id");

        // Отримуємо видимість (в метрах)
        int visibility = 10000; // Значення за замовчуванням, якщо поле відсутнє
        if (response.has("visibility")) {
            visibility = response.getInt("visibility");
        }

        // Отримуємо значення UV-індексу
        double uvIndex = 0.0; // Значення за замовчуванням, якщо поле відсутнє
        if (response.has("uvi")) {
            uvIndex = response.getDouble("uvi");
        }

        // Визначення, чи день зараз
        long sunrise = response.getJSONObject("sys").getLong("sunrise");
        long sunset = response.getJSONObject("sys").getLong("sunset");
        long currentTime = response.getLong("dt");
        boolean isDay = currentTime >= sunrise && currentTime < sunset;

        return new CurrentWeather(cityName, temperature, description, humidity, windSpeed, weatherCode, isDay, 
                                pressure, visibility, uvIndex);
    }

    private List<DailyForecast> parseForecastResponse(JSONObject response) throws JSONException {
        List<DailyForecast> forecasts = new ArrayList<>();
        JSONArray list = response.getJSONArray("list");

        // Отримуємо прогноз на 5 днів (з кроком у 24 години)
        for (int i = 0; i < list.length(); i += 8) {
            if (i + 4 < list.length()) { // Прогноз на полудень
                JSONObject forecastData = list.getJSONObject(i + 4);
                long timestamp = forecastData.getLong("dt") * 1000; // Конвертуємо в мілісекунди

                JSONObject main = forecastData.getJSONObject("main");
                double tempMax = main.getDouble("temp_max");
                double tempMin = main.getDouble("temp_min");

                JSONArray weatherArray = forecastData.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                int weatherCode = weather.getInt("id");
                String description = weather.getString("description");

                // Отримуємо швидкість вітру
                double windSpeed = 0.0;
                if (forecastData.has("wind")) {
                    JSONObject wind = forecastData.getJSONObject("wind");
                    windSpeed = wind.getDouble("speed");
                }

                DailyForecast forecast = new DailyForecast(timestamp, tempMin, tempMax, weatherCode, description, windSpeed);
                forecasts.add(forecast);
            }
        }

        return forecasts;
    }
}
