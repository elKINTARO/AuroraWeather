package com.example.auroraweather.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.auroraweather.R;
import com.example.auroraweather.models.DailyForecast;
import com.example.auroraweather.utils.WeatherIconMapper;

import java.text.SimpleDateFormat;
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

        // Форматування дати
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", new Locale("uk", "UA"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
        Date forecastDate = new Date(forecast.getTimestamp());

        holder.dayOfWeekTextView.setText(dayFormat.format(forecastDate));
        holder.dateTextView.setText(dateFormat.format(forecastDate));
        holder.temperatureTextView.setText(String.format(Locale.getDefault(), "%.1f°C", forecast.getMaxTemperature()));

        // Встановлення відповідної анімації
        String animationFile = WeatherIconMapper.getAnimationForWeatherCode(forecast.getWeatherCode());
        holder.weatherAnimationView.setAnimation(animationFile);
        holder.weatherAnimationView.playAnimation();
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView dayOfWeekTextView;
        TextView dateTextView;
        TextView temperatureTextView;
        LottieAnimationView weatherAnimationView;

        ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            dayOfWeekTextView = itemView.findViewById(R.id.day_of_week);
            dateTextView = itemView.findViewById(R.id.date);
            temperatureTextView = itemView.findViewById(R.id.temperature);
            weatherAnimationView = itemView.findViewById(R.id.weather_animation);
        }
    }
}