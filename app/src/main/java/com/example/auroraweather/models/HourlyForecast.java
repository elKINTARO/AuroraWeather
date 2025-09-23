package com.example.auroraweather.models;

public class HourlyForecast {
    private final long timestamp;
    private final double temperature;
    private final int weatherCode;
    private final String description;
    private final double windSpeed;

    public HourlyForecast(long timestamp, double temperature, int weatherCode, String description, double windSpeed) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.weatherCode = weatherCode;
        this.description = description;
        this.windSpeed = windSpeed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public String getDescription() {
        return description;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}


