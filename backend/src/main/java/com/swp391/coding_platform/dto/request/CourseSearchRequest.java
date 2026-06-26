package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseSearchRequest {

    /* --- FILED SEARCH --- */
    String keyword;
    List<Long> categoryIds;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    Double minRating;
    Double maxRating;
    String instructorName;

    /* --- FILED PAGE --- */
    @Builder.Default
    @Min(value = 0, message = "PAGE_INVALID")
    int page = 0;

    @Builder.Default
    @Max(value = 20, message = "PAGE_SIZE_INVALID")
    int size = 12;

    /* --- FILED SEARCH --- */
    @Builder.Default
    String[] sortBy = {"totalEnrolled"};
    @Builder.Default
    String[] order = {"desc"};


    /* --- HELPER FUNCTION --- */
    public Pageable getPageable(){
        List<Sort.Order> sortOrders = new ArrayList<>();
        for (int i = 0; i < sortBy.length; i++) {
            String sortDir = i < order.length ? order[i] : "desc";
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortOrders.add(new Sort.Order(direction, sortBy[i]));
        }

        Sort dynamicSort = sortOrders.isEmpty() ? Sort.unsorted() : Sort.by(sortOrders);

        Sort finalSort = dynamicSort.and(Sort.by(Sort.Direction.DESC, "totalEnrolled"));

        return PageRequest.of(page, size, finalSort);
    }



}