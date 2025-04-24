package com.example.auroraweather.models;

public class CurrentWeather {
    private final String cityName;
    private final double temperature;
    private final String description;
    private final int humidity;
    private final double windSpeed;
    private final int weatherCode;
    private final boolean isDay;
    private final int pressure; // Атмосферний тиск в гПа
    private final int visibility; // Видимість в метрах
    private final double uvIndex; // Індекс UV

    public CurrentWeather(String cityName, double temperature, String description, int humidity, double windSpeed, 
                         int weatherCode, boolean isDay, int pressure, int visibility, double uvIndex) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
        this.isDay = isDay;
        this.pressure = pressure;
        this.visibility = visibility;
        this.uvIndex = uvIndex;
    }

    public String getCityName() {
        return cityName;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public boolean isDay() {
        return isDay;
    }
    
    public int getPressure() {
        return pressure;
    }
    
    public int getVisibility() {
        return visibility;
    }
    
    public double getUvIndex() {
        return uvIndex;
    }
}