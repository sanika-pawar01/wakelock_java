package com.example.wakelockjava.camera;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class SquatAnalyzer extends ExerciseAnalyzer {
    
    private final String difficulty;
    private double downThreshold;
    private static final double STANDING_THRESHOLD = 165.0; // Body must be mostly straight to count as standing

    public SquatAnalyzer(String difficulty) {
        this.difficulty = difficulty != null ? difficulty : "MEDIUM";
        setThresholds();
    }

    private void setThresholds() {
        switch (difficulty) {
            case "EASY":
                downThreshold = 140.0;
                break;
            case "HARD":
                downThreshold = 95.0;
                break;
            case "MEDIUM":
            default:
                downThreshold = 115.0;
                break;
        }
    }

    @Override
    public void processPose(Pose pose) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        boolean leftValid = isReliable(leftHip) && isReliable(leftKnee) && isReliable(leftAnkle);
        boolean rightValid = isReliable(rightHip) && isReliable(rightKnee) && isReliable(rightAnkle);

        if (!leftValid && !rightValid) {
            currentState = State.SEARCHING;
            feedback = "Show full body (hips to ankles)";
            return;
        }

        double leftAngle = calculateAngle(leftHip, leftKnee, leftAnkle);
        double rightAngle = calculateAngle(rightHip, rightKnee, rightAnkle);
        
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
                if (smoothedAngle > STANDING_THRESHOLD) {
                    currentState = State.READY;
                    feedback = "Ready! Squat down";
                } else {
                    feedback = "Stand up straight";
                    currentState = State.INITIALIZING;
                }
                break;
                
            case READY:
                if (smoothedAngle < downThreshold) {
                    currentState = State.DOWNWARD;
                    feedback = "Good, now stand up";
                } else if (smoothedAngle > STANDING_THRESHOLD + 10) {
                    // stabilizing start
                } else {
                    feedback = "Lower your hips further";
                }
                break;
                
            case DOWNWARD:
                if (smoothedAngle > STANDING_THRESHOLD) {
                    repCount++;
                    currentState = State.READY;
                    feedback = "Excellent! " + repCount;
                } else if (smoothedAngle < downThreshold - 10) {
                    feedback = "Deep squat! Now up";
                }
                break;
        }
    }
}