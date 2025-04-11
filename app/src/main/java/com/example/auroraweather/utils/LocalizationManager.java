package com.example.auroraweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class LocalizationManager {
    private static final String TAG = "LocalizationManager";
    private static final String PREFS_NAME = "aurora_localization";
    private static final String KEY_LANGUAGE = "selected_language";
    
    // Доступні мови
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_UKRAINIAN = "uk";
    
    private static LocalizationManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private Map<String, String> translations;
    private String currentLanguage;
    
    public static LocalizationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalizationManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private LocalizationManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentLanguage = prefs.getString(KEY_LANGUAGE, getDeviceLanguage());
        this.translations = new HashMap<>();
        loadTranslations(currentLanguage);
    }
    
    /**
     * Отримує мову пристрою або використовує англійську як мову за замовчуванням
     */
    private String getDeviceLanguage() {
        String deviceLanguage = Locale.getDefault().getLanguage();
        // Перевіряємо, чи підтримується мова пристрою
        if (deviceLanguage.equals(LANGUAGE_UKRAINIAN)) {
            return LANGUAGE_UKRAINIAN;
        }
        return LANGUAGE_ENGLISH; // Мова за замовчуванням
    }
    
    /**
     * Завантажує переклади з JSON-файлу для вказаної мови
     */
    private void loadTranslations(String languageCode) {
        translations.clear();
        try {
            String jsonContent = loadJSONFromAsset("localization/" + languageCode + ".json");
            if (jsonContent != null) {
                JSONObject jsonObject = new JSONObject(jsonContent);
                Iterator<String> keys = jsonObject.keys();
                
                while (keys.hasNext()) {
                    String key = keys.next();
                    translations.put(key, jsonObject.getString(key));
                }
                
                currentLanguage = languageCode;
                saveLanguagePreference(languageCode);
            } else {
                Log.e(TAG, "Failed to load translations for language: " + languageCode);
                // Якщо не вдалося завантажити переклади, використовуємо англійську
                if (!languageCode.equals(LANGUAGE_ENGLISH)) {
                    loadTranslations(LANGUAGE_ENGLISH);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON for language: " + languageCode, e);
            // Якщо сталася помилка, використовуємо англійську
            if (!languageCode.equals(LANGUAGE_ENGLISH)) {
                loadTranslations(LANGUAGE_ENGLISH);
            }
        }
    }
    
    /**
     * Завантажує вміст JSON-файлу з assets
     */
    private String loadJSONFromAsset(String fileName) {
        String json;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "Error loading JSON file: " + fileName, ex);
            return null;
        }
        return json;
    }
    
    /**
     * Зберігає вибрану мову в SharedPreferences
     */
    private void saveLanguagePreference(String languageCode) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }
    
    /**
     * Змінює мову додатку
     */
    public void setLanguage(String languageCode) {
        if (!currentLanguage.equals(languageCode)) {
            loadTranslations(languageCode);
        }
    }
    
    /**
     * Повертає поточну мову
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Отримує переклад для вказаного ключа
     */
    public String getString(String key) {
        return translations.getOrDefault(key, key);
    }
    
    /**
     * Оновлює конфігурацію локалі для активності
     */
    public void updateLocale(Context context) {
        Locale locale = new Locale(currentLanguage);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}