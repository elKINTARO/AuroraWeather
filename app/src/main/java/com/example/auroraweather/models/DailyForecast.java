package com.example.auroraweather.models;

public class DailyForecast {
    private final long timestamp;
    private final double minTemperature;
    private final double maxTemperature;
    private final int weatherCode;
    private final String description;
    private final double windSpeed;

    public DailyForecast(long timestamp, double minTemperature, double maxTemperature, int weatherCode, String description, double windSpeed) {
        this.timestamp = timestamp;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.weatherCode = weatherCode;
        this.description = description;
        this.windSpeed = windSpeed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
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