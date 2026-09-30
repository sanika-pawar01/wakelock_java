package com.example.wakelockjava.camera;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.List;

public abstract class ExerciseAnalyzer {
    
    public enum State {
        SEARCHING,   // No body detected
        INITIALIZING, // Body detected but not in start position
        READY,       // In start position
        DOWNWARD,    // Moving to target
        UPWARD,      // Moving back to start
        COMPLETED_REP
    }

    protected State currentState = State.SEARCHING;
    protected int repCount = 0;
    protected String feedback = "Move into frame";
    protected final List<Double> angleHistory = new ArrayList<>();
    protected static final int HISTORY_SIZE = 8; // Increased for better smoothing
    
    public abstract void processPose(Pose pose);

    public State getCurrentState() {
        return currentState;
    }

    public int getRepCount() {
        return repCount;
    }

    public String getFeedback() {
        return feedback;
    }

    protected double getAverageAngle(double newAngle) {
        if (newAngle == -1) return -1;
        angleHistory.add(newAngle);
        if (angleHistory.size() > HISTORY_SIZE) {
            angleHistory.remove(0);
        }
        double sum = 0;
        for (double a : angleHistory) sum += a;
        return sum / angleHistory.size();
    }

    protected double calculateAngle(PoseLandmark first, PoseLandmark mid, PoseLandmark last) {
        if (first == null || mid == null || last == null) return -1;
        if (first.getInFrameLikelihood() < 0.5f || mid.getInFrameLikelihood() < 0.5f || last.getInFrameLikelihood() < 0.5f) return -1;
        
        double result = Math.toDegrees(
                Math.atan2(last.getPosition().y - mid.getPosition().y, last.getPosition().x - mid.getPosition().x)
                        - Math.atan2(first.getPosition().y - mid.getPosition().y, first.getPosition().x - mid.getPosition().x));
        result = Math.abs(result);
        if (result > 180) {
            result = 360.0 - result;
        }
        return result;
    }

    protected boolean isReliable(PoseLandmark landmark) {
        return landmark != null && landmark.getInFrameLikelihood() > 0.75f;
    }
}