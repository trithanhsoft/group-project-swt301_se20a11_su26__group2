package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.response.ProblemSubmissionResponse;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemSubmissionServiceTest {

    @Mock
    private ProblemSubmissionRepository problemSubmissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private ProblemSubmissionService problemSubmissionService;

    @Test
    void getSubmissions_UserIdNull_ReturnsEmptyList() {
        List<ProblemSubmissionResponse> result = problemSubmissionService.getSubmissions(1, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSubmissions_ProblemNotFound_ThrowsAppException() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, 
            () -> problemSubmissionService.getSubmissions(1, 1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getSubmissions_NoSubmissions_ReturnsEmptyList() {
        ProblemEntity problem = mock(ProblemEntity.class);
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(problem));
        when(problemSubmissionRepository.findByUserIdAndProblemId(1, 1)).thenReturn(Collections.emptyList());

        List<ProblemSubmissionResponse> result = problemSubmissionService.getSubmissions(1, 1);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getSubmissions_Success() {
        ProblemEntity problem = mock(ProblemEntity.class);
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(problem));

        ProblemSubmissionEntity submission = mock(ProblemSubmissionEntity.class);
        when(submission.getVerdict()).thenReturn(OjVerdict.ACCEPTED);
        when(submission.getLanguageId()).thenReturn(62); // Java
        when(submission.getExecutionTime()).thenReturn(150);
        when(submission.getMemoryUsed()).thenReturn(10240);
        when(submission.getSubmittedAt()).thenReturn(Instant.now());

        when(problemSubmissionRepository.findByUserIdAndProblemId(1, 1))
                .thenReturn(List.of(submission));

        List<ProblemSubmissionResponse> result = problemSubmissionService.getSubmissions(1, 1);
        
        assertFalse(result.isEmpty());
        assertEquals("Accepted", result.get(0).getStatus());
        assertEquals("Java", result.get(0).getLang());
    }
}
