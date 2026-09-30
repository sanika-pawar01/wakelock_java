package com.example.wakelockjava.camera;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class PushUpCounter {

    public interface RepListener {
        void onRepCounted(int count);
    }

    private enum State { UP, DOWN }

    private State state = State.UP;
    private int repCount = 0;
    private long lastRepTimeMs = 0;
    private final RepListener listener;

    private double downThreshold = 100.0;
    private double upThreshold = 150.0;
    private static final float MIN_CONFIDENCE = 0.5f;
    private static final long MIN_MS_BETWEEN_REPS = 600;

    public PushUpCounter(RepListener listener, String difficulty) {
        this.listener = listener;
        setDifficulty(difficulty);
    }

    private void setDifficulty(String difficulty) {
        if ("EASY".equals(difficulty)) {
            // Very lenient: small arm bend counts
            downThreshold = 145.0;
            upThreshold = 165.0;
        } else if ("HARD".equals(difficulty)) {
            // Strict: chest to floor
            downThreshold = 80.0;
            upThreshold = 150.0;
        } else {
            // Medium
            downThreshold = 110.0;
            upThreshold = 155.0;
        }
    }

    public void processPose(Pose pose) {
        PoseLandmark shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark elbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);

        if (!isReliable(shoulder) || !isReliable(elbow) || !isReliable(wrist)) {
            shoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
            elbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
            wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
            if (!isReliable(shoulder) || !isReliable(elbow) || !isReliable(wrist)) return;
        }

        double angle = calculateAngle(
                shoulder.getPosition().x, shoulder.getPosition().y,
                elbow.getPosition().x, elbow.getPosition().y,
                wrist.getPosition().x, wrist.getPosition().y
        );

        long now = System.currentTimeMillis();

        if (state == State.UP && angle < downThreshold) {
            state = State.DOWN;
        } else if (state == State.DOWN && angle > upThreshold
                && (now - lastRepTimeMs) > MIN_MS_BETWEEN_REPS) {
            state = State.UP;
            repCount++;
            lastRepTimeMs = now;
            listener.onRepCounted(repCount);
        }
    }

    private boolean isReliable(PoseLandmark landmark) {
        return landmark != null && landmark.getInFrameLikelihood() >= MIN_CONFIDENCE;
    }

    private double calculateAngle(float ax, float ay, float bx, float by, float cx, float cy) {
        double angleA = Math.atan2(ay - by, ax - bx);
        double angleC = Math.atan2(cy - by, cx - bx);
        double angle = Math.toDegrees(angleA - angleC);
        angle = Math.abs(angle);
        if (angle > 180) angle = 360 - angle;
        return angle;
    }

    public void reset() {
        repCount = 0;
        state = State.UP;
        lastRepTimeMs = 0;
    }
}