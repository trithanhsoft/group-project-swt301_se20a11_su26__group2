package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContestSearchRequest {

    String search;

    @Builder.Default
    String status = "All";

    @Builder.Default
    String access = "All";

    @Builder.Default
    @Min(0)
    Integer page = 0;

    @Builder.Default
    @Min(1)
    @Max(100)
    Integer size = 5;

    @Builder.Default
    String sortBy = "id";

    @Builder.Default
    String sortDirection = "desc";
}
