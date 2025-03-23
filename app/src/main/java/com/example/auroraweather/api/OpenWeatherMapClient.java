package com.example.auroraweather.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

    private final RequestQueue requestQueue;

    public OpenWeatherMapClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void getCurrentWeather(String city, final WeatherCallback<CurrentWeather> callback) {
        String url = BASE_URL + "weather?q=" + city + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        CurrentWeather weather = parseCurrentWeatherResponse(response);
                        callback.onSuccess(weather);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing weather data", e);
                        callback.onError("Помилка обробки даних погоди");
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching weather data", error);
                    callback.onError("Не вдалося завантажити дані погоди. Перевірте з'єднання або назву міста.");
                });

        requestQueue.add(request);
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

    private CurrentWeather parseCurrentWeatherResponse(JSONObject response) throws JSONException {
        String cityName = response.getString("name");

        JSONObject main = response.getJSONObject("main");
        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");

        JSONObject wind = response.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");

        JSONArray weatherArray = response.getJSONArray("weather");
        JSONObject weatherData = weatherArray.getJSONObject(0);
        String description = weatherData.getString("description");
        int weatherCode = weatherData.getInt("id");

        // Визначення, чи день зараз
        long sunrise = response.getJSONObject("sys").getLong("sunrise");
        long sunset = response.getJSONObject("sys").getLong("sunset");
        long currentTime = response.getLong("dt");
        boolean isDay = currentTime >= sunrise && currentTime < sunset;

        return new CurrentWeather(cityName, temperature, description, humidity, windSpeed, weatherCode, isDay);
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

                DailyForecast forecast = new DailyForecast(timestamp, tempMin, tempMax, weatherCode, description);
                forecasts.add(forecast);
            }
        }

        return forecasts;
    }
}
