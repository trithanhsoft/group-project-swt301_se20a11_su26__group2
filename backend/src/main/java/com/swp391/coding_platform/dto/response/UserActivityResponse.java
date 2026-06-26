package com.swp391.coding_platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserActivityResponse {
    private Integer userId;
    private int year;
    private Integer maxStreak;
    private Integer currentStreak;
    private List<LocalDate> activeDates;
}
