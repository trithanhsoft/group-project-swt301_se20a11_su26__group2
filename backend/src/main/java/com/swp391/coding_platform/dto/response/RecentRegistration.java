package com.swp391.coding_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentRegistration {
    private String studentName;
    private String avatar;
    private String course;
    private String time;
    private String amount;
}
