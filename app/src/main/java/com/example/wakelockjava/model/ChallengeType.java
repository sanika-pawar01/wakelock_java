package com.example.wakelockjava.model;

public enum ChallengeType {
    SQUATS("Squats", "Get your body moving.", true, 10),
    PUSHUPS("Push-ups", "Start your day with strength.", true, 10),
    WATER("Drink Water", "Start your morning hydrated.", true, 0),
    MAKE_BED("Make Your Bed", "Complete your first task of the day.", true, 0),
    MATH("Math Challenge", "Wake up your brain.", true, 5);

    public final String displayName;
    public final String description;
    public final boolean isAvailable;
    public final int defaultTarget;

    ChallengeType(String displayName, String description, boolean isAvailable, int defaultTarget) {
        this.displayName = displayName;
        this.description = description;
        this.isAvailable = isAvailable;
        this.defaultTarget = defaultTarget;
    }

    public boolean hasCount() {
        return this == SQUATS || this == PUSHUPS || this == MATH;
    }
}