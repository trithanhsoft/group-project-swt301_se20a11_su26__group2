package com.swp391.coding_platform.controller.category;

import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.entity.category.CategoryEntity;
import com.swp391.coding_platform.repository.category.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<List<CategoryEntity>> getAllCategories() {
        return ApiResponse.<List<CategoryEntity>>builder()
                .code(200)
                .message("Success")
                .result(categoryRepository.findAll())
                .build();
    }
}
