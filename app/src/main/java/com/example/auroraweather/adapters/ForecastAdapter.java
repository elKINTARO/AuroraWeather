package com.example.auroraweather.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.auroraweather.R;
import com.example.auroraweather.SettingsManager;
import com.example.auroraweather.models.DailyForecast;
import com.example.auroraweather.utils.LocalizationManager;
import com.example.auroraweather.utils.WeatherIconMapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private final List<DailyForecast> forecastList;

    public ForecastAdapter(List<DailyForecast> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        DailyForecast forecast = forecastList.get(position);
        Context context = holder.itemView.getContext();

        // Форматування дати
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
        Date forecastDate = new Date(forecast.getTimestamp());
        
        // Отримання локалізованого дня тижня
        String dayOfWeek = getDayOfWeekLocalized(forecastDate, context);
        holder.dayOfWeekTextView.setText(dayOfWeek);
        holder.dateTextView.setText(dateFormat.format(forecastDate));
        
        // Use SettingsManager to format temperature according to user preference
        holder.temperatureTextView.setText(SettingsManager.getInstance(context).formatTemperature(forecast.getMaxTemperature()));
        
        // Set wind speed using the new formatWindSpeedCompact method
        holder.windSpeedTextView.setText(SettingsManager.getInstance(context).formatWindSpeedCompact(forecast.getWindSpeed()));

        // Встановлення відповідної анімації
        String animationFile = WeatherIconMapper.getAnimationForWeatherCode(forecast.getWeatherCode());
        holder.weatherAnimationView.setAnimation(animationFile);
        holder.weatherAnimationView.playAnimation();
        
        // Ensure text visibility based on current theme
        ensureTextVisibility(holder, context);
    }
    
    /**
     * Отримує локалізовану назву дня тижня
     */
    private String getDayOfWeekLocalized(Date date, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        LocalizationManager localizationManager = LocalizationManager.getInstance(context);
        
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return localizationManager.getString("monday");
            case Calendar.TUESDAY:
                return localizationManager.getString("tuesday");
            case Calendar.WEDNESDAY:
                return localizationManager.getString("wednesday");
            case Calendar.THURSDAY:
                return localizationManager.getString("thursday");
            case Calendar.FRIDAY:
                return localizationManager.getString("friday");
            case Calendar.SATURDAY:
                return localizationManager.getString("saturday");
            case Calendar.SUNDAY:
                return localizationManager.getString("sunday");
            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView dayOfWeekTextView;
        TextView dateTextView;
        TextView temperatureTextView;
        TextView windSpeedTextView;
        LottieAnimationView weatherAnimationView;

        ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            dayOfWeekTextView = itemView.findViewById(R.id.day_of_week);
            dateTextView = itemView.findViewById(R.id.date);
            temperatureTextView = itemView.findViewById(R.id.temperature);
            windSpeedTextView = itemView.findViewById(R.id.wind_speed);
            weatherAnimationView = itemView.findViewById(R.id.weather_animation);
        }
    }
    
    /**
     * Ensures text visibility based on current theme
     */
    private void ensureTextVisibility(ForecastViewHolder holder, Context context) {
        // Check if we're in dark mode
        int nightModeFlags = context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        
        if (isDarkMode) {
            // For dark theme, ensure text is visible
            holder.dayOfWeekTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.dateTextView.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondaryDark));
            holder.temperatureTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.windSpeedTextView.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondaryDark));
            
            // Make sure card background is appropriate for dark mode
            ((CardView)holder.itemView).setCardBackgroundColor(ContextCompat.getColor(context, R.color.dark_background));
        } else {
            // For light theme
            holder.dayOfWeekTextView.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));
            holder.dateTextView.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondary));
            holder.temperatureTextView.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));
            holder.windSpeedTextView.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondary));
            
            // Make sure card background is appropriate for light mode
            ((CardView)holder.itemView).setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }
}