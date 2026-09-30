package com.example.wakelockjava.camera;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class SkeletonOverlayView extends View {
    private Pose pose;
    private Paint linePaint;
    private Paint dotPaint;
    private Paint glowPaint;
    private boolean isRepHighlight = false;
    private int imageWidth = 480;
    private int imageHeight = 640;
    private boolean isMirrored = true;
    
    public SkeletonOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(6f);
        linePaint.setAlpha(160);
        linePaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        glowPaint = new Paint();
        glowPaint.setColor(Color.WHITE);
        glowPaint.setAlpha(100);
        glowPaint.setAntiAlias(true);
        glowPaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
        
        // Software layer for BlurMaskFilter
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setPose(Pose pose, int imageWidth, int imageHeight) {
        this.pose = pose;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }

    public void triggerHighlight() {
        isRepHighlight = true;
        postDelayed(() -> {
            isRepHighlight = false;
            invalidate();
        }, 600);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose == null) return;

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) return;

        float widthScale = (float) getWidth() / (float) imageWidth;
        float heightScale = (float) getHeight() / (float) imageHeight;

        if (isRepHighlight) {
            linePaint.setColor(Color.parseColor("#81C784")); // success green
            linePaint.setAlpha(255);
            linePaint.setStrokeWidth(12f);
        } else {
            linePaint.setColor(Color.WHITE);
            linePaint.setAlpha(160);
            linePaint.setStrokeWidth(6f);
        }

        // Draw connections
        drawConnection(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, widthScale, heightScale);
        
        drawConnection(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, widthScale, heightScale);
        
        drawConnection(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, widthScale, heightScale);
        drawConnection(canvas, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, widthScale, heightScale);

        // Draw landmarks with glow
        for (PoseLandmark landmark : landmarks) {
            if (landmark.getInFrameLikelihood() > 0.5) {
                float rawX = landmark.getPosition().x;
                float x = isMirrored ? (imageWidth - rawX) * widthScale : rawX * widthScale;
                float y = landmark.getPosition().y * heightScale;
                
                canvas.drawCircle(x, y, 12f, glowPaint);
                canvas.drawCircle(x, y, 6f, dotPaint);
            }
        }
    }

    private void drawConnection(Canvas canvas, int startId, int endId, float sw, float sh) {
        PoseLandmark start = pose.getPoseLandmark(startId);
        PoseLandmark end = pose.getPoseLandmark(endId);
        if (start != null && end != null && start.getInFrameLikelihood() > 0.5 && end.getInFrameLikelihood() > 0.5) {
            float startX = isMirrored ? (imageWidth - start.getPosition().x) * sw : start.getPosition().x * sw;
            float endX = isMirrored ? (imageWidth - end.getPosition().x) * sw : end.getPosition().x * sw;
            
            canvas.drawLine(
                    startX, start.getPosition().y * sh,
                    endX, end.getPosition().y * sh,
                    linePaint
            );
        }
    }
}