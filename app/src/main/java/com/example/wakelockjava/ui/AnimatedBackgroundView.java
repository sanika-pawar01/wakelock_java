package com.example.wakelockjava.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.wakelockjava.data.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimatedBackgroundView extends View {
    
    private String themeMode = "CALM";
    private Paint bgPaint;
    private final List<Blob> blobs = new ArrayList<>();
    private final Random random = new Random();
    private long startTime;
    private LinearGradient dreamyGrad;
    private LinearGradient sunriseGrad;
    
    private int color1, color2, bgColor;

    public AnimatedBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        PreferencesManager prefs = new PreferencesManager(context);
        themeMode = prefs.getThemeMode();
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startTime = System.currentTimeMillis();
        
        switch (themeMode) {
            case "DARK":
                bgColor = Color.parseColor("#0F110E");
                color1 = Color.parseColor("#1A2D2436");
                color2 = Color.parseColor("#1A0F110E");
                break;
            case "DREAMY":
                color1 = Color.parseColor("#409BA9C1");
                color2 = Color.parseColor("#40CBA6B9");
                break;
            case "SUNRISE":
            case "LIGHT":
                color1 = Color.parseColor("#1AFF9F43");
                color2 = Color.parseColor("#1AFF6B6B");
                break;
            case "CALM":
            default:
                bgColor = Color.parseColor("#F7F8F6");
                color1 = Color.parseColor("#1A8C9A86");
                color2 = Color.parseColor("#1A73826E");
                break;
        }
        
        // Setup blobs for animated themes
        if (themeMode != null && (themeMode.equals("DREAMY") || themeMode.equals("CALM") || themeMode.equals("DARK") || themeMode.equals("SUNRISE") || themeMode.equals("LIGHT"))) {
            for (int i = 0; i < 4; i++) {
                blobs.add(new Blob());
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            dreamyGrad = new LinearGradient(0, 0, 0, h,
                    Color.parseColor("#F3E7E9"), Color.parseColor("#E3EEFF"), Shader.TileMode.CLAMP);
            sunriseGrad = new LinearGradient(0, 0, w, h,
                    Color.parseColor("#FFF9F0"), Color.parseColor("#FFE4D1"), Shader.TileMode.CLAMP);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        long elapsed = System.currentTimeMillis() - startTime;

        switch (themeMode) {
            case "DARK":
                canvas.drawColor(bgColor);
                drawBlobs(canvas, elapsed, color1, color2);
                break;
            case "DREAMY":
                if (dreamyGrad != null) bgPaint.setShader(dreamyGrad);
                canvas.drawRect(0, 0, w, h, bgPaint);
                bgPaint.setShader(null);
                drawBlobs(canvas, elapsed, color1, color2);
                break;
            case "MINIMAL":
            case "FOCUS":
                canvas.drawColor(Color.WHITE);
                break;
            case "SUNRISE":
            case "LIGHT":
                if (sunriseGrad != null) bgPaint.setShader(sunriseGrad);
                canvas.drawRect(0, 0, w, h, bgPaint);
                bgPaint.setShader(null);
                drawBlobs(canvas, elapsed, color1, color2);
                break;
            case "CALM":
            default:
                canvas.drawColor(bgColor);
                drawBlobs(canvas, elapsed, color1, color2);
                break;
        }
        
        invalidate();
    }

    private void drawBlobs(Canvas canvas, long elapsed, int color1, int color2) {
        float w = getWidth();
        float h = getHeight();
        for (int i = 0; i < blobs.size(); i++) {
            Blob b = blobs.get(i);
            float x = w * (b.xStart + (float) Math.sin(elapsed * b.xSpeed + b.phase) * b.xRange);
            float y = h * (b.yStart + (float) Math.cos(elapsed * b.ySpeed + b.phase) * b.yRange);
            float radius = w * b.size;
            
            bgPaint.setColor(i % 2 == 0 ? color1 : color2);
            canvas.drawCircle(x, y, radius, bgPaint);
        }
    }

    private class Blob {
        float xStart, yStart, xRange, yRange, xSpeed, ySpeed, size, phase;
        Blob() {
            xStart = random.nextFloat();
            yStart = random.nextFloat();
            xRange = 0.1f + random.nextFloat() * 0.2f;
            yRange = 0.1f + random.nextFloat() * 0.2f;
            xSpeed = 0.0001f + random.nextFloat() * 0.0002f;
            ySpeed = 0.0001f + random.nextFloat() * 0.0002f;
            size = 0.3f + random.nextFloat() * 0.4f;
            phase = random.nextFloat() * (float) Math.PI * 2;
        }
    }
}