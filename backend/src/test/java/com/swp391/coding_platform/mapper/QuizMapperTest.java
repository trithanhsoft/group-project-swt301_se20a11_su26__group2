package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.QuizOptionResultResponse;
import com.swp391.coding_platform.entity.course.QuizOptionEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizMapperTest {

    private final QuizMapper mapper = new QuizMapperImpl();

    @Test
    void toQuizOptionResultResponse() {
        QuizOptionEntity option = new QuizOptionEntity();
        option.setId(100);
        option.setContent("Option A");
        option.setIsCorrect(true);

        QuizOptionResultResponse response = mapper.toQuizOptionResultResponse(option);

        assertEquals(100, response.getOptionId());
        assertEquals("Option A", response.getContent());
        assertTrue(response.getIsCorrect());
    }
}
