package com.example.auroraweather.api;

public interface WeatherCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}