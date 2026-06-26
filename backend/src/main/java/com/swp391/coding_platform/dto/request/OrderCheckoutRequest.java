package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCheckoutRequest {
    @NotEmpty(message = "Course list cannot be empty")
    List<Long> courseIds;
}
