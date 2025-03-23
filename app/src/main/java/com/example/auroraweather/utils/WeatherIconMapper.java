package com.example.auroraweather.utils;
import com.example.auroraweather.R;

public class WeatherIconMapper {

    // Метод для отримання назви файлу Lottie анімації на основі коду погоди
    public static String getAnimationForWeatherCode(int weatherCode) {
        // Групування кодів погоди OpenWeatherMap
        // 2xx: Гроза
        // 3xx: Мряка
        // 5xx: Дощ
        // 6xx: Сніг
        // 7xx: Атмосферні явища (туман, смог)
        // 800: Ясно
        // 80x: Хмарно

        if (weatherCode >= 200 && weatherCode < 300) {
            return "weather_thunder.json";
        } else if (weatherCode >= 300 && weatherCode < 400) {
            return "weather_drizzle.json";
        } else if (weatherCode >= 500 && weatherCode < 600) {
            return "weather_rain.json";
        } else if (weatherCode >= 600 && weatherCode < 700) {
            return "weather_snow.json";
        } else if (weatherCode >= 700 && weatherCode < 800) {
            return "weather_fog.json";
        } else if (weatherCode == 800) {
            return "weather_sunny.json";
        } else if (weatherCode > 800 && weatherCode < 900) {
            return "weather_cloudy.json";
        } else {
            return "weather_default.json";
        }
    }

    // Метод для отримання назви файлу фонової анімації на основі коду погоди
    public static int getBackgroundForWeatherCode(int weatherCode, boolean isDay) {
        if (weatherCode >= 200 && weatherCode < 300) {
            return R.drawable.bg_thunder;
        } else if (weatherCode >= 300 && weatherCode < 600) {
            return R.drawable.bg_rain;
        } else if (weatherCode >= 600 && weatherCode < 700) {
            return R.drawable.bg_snow;
        } else if (weatherCode >= 700 && weatherCode < 800) {
            return R.drawable.bg_fog;
        } else if (weatherCode == 800) {
            return isDay ? R.drawable.bg_clear_day : R.drawable.bg_clear_night;
        } else {
            return isDay ? R.drawable.bg_cloudy_day : R.drawable.bg_cloudy_night;
        }
    }
}
