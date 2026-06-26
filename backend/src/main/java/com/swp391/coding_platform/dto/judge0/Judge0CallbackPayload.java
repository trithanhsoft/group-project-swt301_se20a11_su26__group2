package com.swp391.coding_platform.dto.judge0;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Judge0CallbackPayload {
    String token; // Tìm cái này trong DB!
    
    String time; // Ví dụ "0.045"
    
    Integer memory; // KB
    
    String stdout; // Console in ra cái gì
    
    String stderr; // Báo lỗi runtime
    
    @JsonProperty("compile_output")
    String compileOutput; // Lỗi biên dịch (thiếu dấu ; ...)

    
    Judge0Status status;

    // Class nội bộ ánh xạ Object "status" của Judge0
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Judge0Status {
        Integer id;          // 3 = Accepted, 4 = WA, 5 = TLE...
        String description;  // "Accepted", "Wrong Answer"...
    }
}