package com.example.auroraweather.utils;

import android.animation.ValueAnimator;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.auroraweather.R;

public class BackgroundManager {

    private final ConstraintLayout layout;
    private int currentBackground = -1;

    public BackgroundManager(ConstraintLayout layout) {
        this.layout = layout;
    }

    public void updateBackground(int weatherCode, boolean isDay) {
        int newBackground = WeatherIconMapper.getBackgroundForWeatherCode(weatherCode, isDay);

        if (currentBackground == newBackground) {
            return;
        }

        if (currentBackground == -1) {
            // Перше встановлення фону
            layout.setBackgroundResource(newBackground);
            animateBackgroundElements(layout);
        } else {
            // Плавна зміна фону
            TransitionDrawable transition = new TransitionDrawable(new android.graphics.drawable.Drawable[]{
                    layout.getContext().getDrawable(currentBackground),
                    layout.getContext().getDrawable(newBackground)
            });

            layout.setBackground(transition);
            transition.startTransition(1000);
            animateBackgroundElements(layout);
        }

        currentBackground = newBackground;
    }

    private void animateBackgroundElements(View view) {
        // Анімація прозорості або інша анімація для елементів фону
        ValueAnimator animator = ValueAnimator.ofFloat(0.8f, 1.0f);
        animator.setDuration(2000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setAlpha(value);
        });

        animator.start();
    }
}