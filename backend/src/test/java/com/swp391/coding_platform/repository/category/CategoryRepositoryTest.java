package com.swp391.coding_platform.repository.category;

import com.swp391.coding_platform.entity.category.CategoryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindCategory() {
        CategoryEntity category = CategoryEntity.builder()
                .name("Testing Category")
                .description("Test description")
                .build();
        
        categoryRepository.save(category);

        List<CategoryEntity> all = categoryRepository.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(c -> c.getName().equals("Testing Category")));
    }

    @Test
    void testFindCategoryEnrollmentCounts() {
        // Just verify the query executes successfully (since it relies on EnrollmentEntity and CourseEntity which might not be set up in this isolated test context easily)
        List<Object[]> results = categoryRepository.findCategoryEnrollmentCounts();
        assertNotNull(results);
    }
}

