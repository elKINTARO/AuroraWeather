package com.example.auroraweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryManager {
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String SEARCH_HISTORY_KEY = "SearchHistoryList";
    private static final int MAX_HISTORY_ITEMS = 10;

    private final SharedPreferences preferences;
    private final Gson gson;

    public SearchHistoryManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<String> getSearchHistory() {
        String historyJson = preferences.getString(SEARCH_HISTORY_KEY, null);
        if (historyJson == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        return gson.fromJson(historyJson, type);
    }

    public void addToHistory(String city) {
        List<String> history = getSearchHistory();

        // Remove if it already exists (to avoid duplicates)
        history.remove(city);

        // Add the city at the beginning (most recent first)
        history.add(0, city);

        // Trim the list if it exceeds the maximum size
        if (history.size() > MAX_HISTORY_ITEMS) {
            history = history.subList(0, MAX_HISTORY_ITEMS);
        }

        // Save the updated history
        String historyJson = gson.toJson(history);
        preferences.edit().putString(SEARCH_HISTORY_KEY, historyJson).apply();
    }

    public void clearHistory() {
        preferences.edit().remove(SEARCH_HISTORY_KEY).apply();
    }
}