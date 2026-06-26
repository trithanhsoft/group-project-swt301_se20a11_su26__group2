package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorCourseRegistrationsResponse {
    private List<CourseRegistrationsItem> courseRegistrations;
    private Integer totalTrendRegistrations;
}
