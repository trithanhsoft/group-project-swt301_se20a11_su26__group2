package com.swp391.coding_platform.controller.category;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.swp391.coding_platform.entity.category.CategoryEntity;
import com.swp391.coding_platform.repository.category.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)

@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    @Test
    void getAllCategories_ShouldReturnListOfCategories() throws Exception {
        CategoryEntity c1 = CategoryEntity.builder().id(1).name("Backend").description("Backend course").build();
        CategoryEntity c2 = CategoryEntity.builder().id(2).name("Frontend").description("Frontend course").build();
        List<CategoryEntity> categories = Arrays.asList(c1, c2);

        when(categoryRepository.findAll()).thenReturn(categories);

        mockMvc.perform(get("/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].name").value("Backend"))
                .andExpect(jsonPath("$.result[1].id").value(2))
                .andExpect(jsonPath("$.result[1].name").value("Frontend"));
    }
}
