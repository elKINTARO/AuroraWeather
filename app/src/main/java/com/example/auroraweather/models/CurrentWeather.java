package com.example.auroraweather.models;

public class CurrentWeather {
    private final String cityName;
    private final double temperature;
    private final String description;
    private final int humidity;
    private final double windSpeed;
    private final int weatherCode;
    private final boolean isDay;

    public CurrentWeather(String cityName, double temperature, String description, int humidity, double windSpeed, int weatherCode, boolean isDay) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
        this.isDay = isDay;
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
}