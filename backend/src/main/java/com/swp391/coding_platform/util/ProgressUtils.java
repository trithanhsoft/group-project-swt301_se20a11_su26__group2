package com.swp391.coding_platform.util;

public class ProgressUtils {

    private ProgressUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Integer calculatePercentage(Integer completeLessons, Integer totalLessons) {
        if (totalLessons == null || totalLessons <= 0) {
            return 0;
        }
        return (int) Math.round((double) completeLessons / totalLessons * 100);
    }
}
