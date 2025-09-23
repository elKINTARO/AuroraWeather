package com.example.auroraweather.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.auroraweather.R;
import com.example.auroraweather.SettingsManager;
import com.example.auroraweather.models.HourlyForecast;
import com.example.auroraweather.utils.WeatherIconMapper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder> {

    private final List<HourlyForecast> items;
    private final SettingsManager settingsManager;

    public HourlyAdapter(List<HourlyForecast> items, SettingsManager settingsManager) {
        this.items = items;
        this.settingsManager = settingsManager;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        HourlyForecast forecast = items.get(position);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.time.setText(df.format(forecast.getTimestamp()));

        String tempFormatted = settingsManager.formatTemperature(forecast.getTemperature());
        holder.temp.setText(tempFormatted);

        String anim = WeatherIconMapper.getAnimationForWeatherCode(forecast.getWeatherCode());
        holder.icon.setAnimation(anim);
        holder.icon.playAnimation();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        LottieAnimationView icon;
        TextView temp;

        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.hour_time);
            icon = itemView.findViewById(R.id.hour_icon);
            temp = itemView.findViewById(R.id.hour_temp);
        }
    }
}


