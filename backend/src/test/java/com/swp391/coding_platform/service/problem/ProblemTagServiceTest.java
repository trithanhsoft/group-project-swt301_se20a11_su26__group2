package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.response.ProblemTagResponse;
import com.swp391.coding_platform.entity.problem.ProblemTagEntity;
import com.swp391.coding_platform.repository.problem.ProblemTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemTagServiceTest {

    @Mock
    private ProblemTagRepository problemTagRepository;

    @InjectMocks
    private ProblemTagService problemTagService;

    @Test
    void getAllTags_WhenEmpty_SavesDefaultsAndReturnsTags() {
        when(problemTagRepository.count()).thenReturn(0L);
        
        ProblemTagEntity tag = mock(ProblemTagEntity.class);
        when(tag.getId()).thenReturn(1);
        when(tag.getName()).thenReturn("Arrays");
        when(tag.getSlug()).thenReturn("arrays");
        when(problemTagRepository.findAll()).thenReturn(List.of(tag));

        List<ProblemTagResponse> tags = problemTagService.getAllTags();

        assertFalse(tags.isEmpty());
        assertEquals("Arrays", tags.get(0).getName());
        verify(problemTagRepository, times(10)).save(any(ProblemTagEntity.class));
    }

    @Test
    void getAllTags_WhenNotEmpty_ReturnsTags() {
        when(problemTagRepository.count()).thenReturn(5L);
        
        ProblemTagEntity tag = mock(ProblemTagEntity.class);
        when(tag.getName()).thenReturn("Math");
        when(problemTagRepository.findAll()).thenReturn(List.of(tag));

        List<ProblemTagResponse> tags = problemTagService.getAllTags();

        assertFalse(tags.isEmpty());
        assertEquals("Math", tags.get(0).getName());
        verify(problemTagRepository, never()).save(any(ProblemTagEntity.class));
    }
}
