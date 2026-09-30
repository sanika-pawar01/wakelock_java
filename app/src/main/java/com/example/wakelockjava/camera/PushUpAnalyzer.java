package com.example.wakelockjava.camera;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class PushUpAnalyzer extends ExerciseAnalyzer {
    
    private final String difficulty;
    private double downThreshold;
    private static final double STRAIGHT_ARM_THRESHOLD = 158.0;

    public PushUpAnalyzer(String difficulty) {
        this.difficulty = difficulty != null ? difficulty : "MEDIUM";
        setThresholds();
    }

    private void setThresholds() {
        switch (difficulty) {
            case "EASY":
                downThreshold = 125.0;
                break;
            case "HARD":
                downThreshold = 75.0;
                break;
            case "MEDIUM":
            default:
                downThreshold = 95.0;
                break;
        }
    }

    @Override
    public void processPose(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);

        boolean leftValid = isReliable(leftShoulder) && isReliable(leftElbow) && isReliable(leftWrist);
        boolean rightValid = isReliable(rightShoulder) && isReliable(rightElbow) && isReliable(rightWrist);

        if (!leftValid && !rightValid) {
            currentState = State.SEARCHING;
            feedback = "Show arms clearly";
            return;
        }

        double leftAngle = calculateAngle(leftShoulder, leftElbow, leftWrist);
        double rightAngle = calculateAngle(rightShoulder, rightElbow, rightWrist);
        
        double currentAngle;
        if (leftAngle != -1 && rightAngle != -1) {
            currentAngle = (leftAngle + rightAngle) / 2.0;
        } else {
            currentAngle = (leftAngle != -1) ? leftAngle : rightAngle;
        }

        if (currentAngle == -1) {
            feedback = "Tracking joints...";
            return;
        }
        
        double smoothedAngle = getAverageAngle(currentAngle);

        switch (currentState) {
            case SEARCHING:
            case INITIALIZING:
                if (smoothedAngle > STRAIGHT_ARM_THRESHOLD) {
                    currentState = State.READY;
                    feedback = "Ready! Push down";
                } else {
                    feedback = "Straighten your arms";
                    currentState = State.INITIALIZING;
                }
                break;
                
            case READY:
                if (smoothedAngle < downThreshold) {
                    currentState = State.DOWNWARD;
                    feedback = "Great, push up!";
                } else {
                    feedback = "Lower your chest";
                }
                break;
                
            case DOWNWARD:
                if (smoothedAngle > STRAIGHT_ARM_THRESHOLD) {
                    repCount++;
                    currentState = State.READY;
                    feedback = "Perfect form! " + repCount;
                }
                break;
        }
    }
}