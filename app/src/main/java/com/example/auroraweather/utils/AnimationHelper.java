package com.example.auroraweather.utils;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.auroraweather.R;

public class AnimationHelper {

    // Анімація підстрибування для кнопок
    public static void bounceAnimation(View view) {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f));
        scaleDown.setDuration(150);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleDown.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                ObjectAnimator scaleBack = ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat("scaleX", 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f));
                scaleBack.setDuration(150);
                scaleBack.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleBack.start();
            }
        });

        scaleDown.start();
    }

    // Анімація обертання для іконок
    public static void rotateAnimation(View view) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        rotation.setDuration(800);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation.start();
    }

    // Анімація пульсації
    public static void pulseAnimation(View view) {
        Animation pulse = AnimationUtils.loadAnimation(view.getContext(), R.anim.pulse);
        view.startAnimation(pulse);
    }
}