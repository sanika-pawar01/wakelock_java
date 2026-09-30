package com.example.wakelockjava.camera;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class SquatCounter {

    public interface RepListener {
        void onRepCounted(int count);
    }

    private enum State { STANDING, SQUATTING }

    private State state = State.STANDING;
    private int repCount = 0;
    private long lastRepTimeMs = 0;
    private final RepListener listener;

    private double downThreshold = 100.0;
    private double upThreshold = 150.0;
    private static final float MIN_CONFIDENCE = 0.5f;
    private static final long MIN_MS_BETWEEN_REPS = 600;

    public SquatCounter(RepListener listener, String difficulty) {
        this.listener = listener;
        setDifficulty(difficulty);
    }

    private void setDifficulty(String difficulty) {
        if ("EASY".equals(difficulty)) {
            // Very lenient: small dip counts as squat
            downThreshold = 150.0;
            upThreshold = 170.0;
        } else if ("HARD".equals(difficulty)) {
            // Strict: deep squat required
            downThreshold = 90.0;
            upThreshold = 150.0;
        } else {
            // Medium: moderate depth
            downThreshold = 120.0;
            upThreshold = 160.0;
        }
    }

    public void processPose(Pose pose) {
        PoseLandmark hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (!isReliable(hip) || !isReliable(knee) || !isReliable(ankle)) {
            hip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
            knee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
            ankle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
            if (!isReliable(hip) || !isReliable(knee) || !isReliable(ankle)) return;
        }

        double angle = calculateAngle(
                hip.getPosition().x, hip.getPosition().y,
                knee.getPosition().x, knee.getPosition().y,
                ankle.getPosition().x, ankle.getPosition().y
        );

        long now = System.currentTimeMillis();

        if (state == State.STANDING && angle < downThreshold) {
            state = State.SQUATTING;
        } else if (state == State.SQUATTING && angle > upThreshold
                && (now - lastRepTimeMs) > MIN_MS_BETWEEN_REPS) {
            state = State.STANDING;
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
        state = State.STANDING;
        lastRepTimeMs = 0;
    }
}