package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.request.AdminTestcaseRequest;
import com.swp391.coding_platform.dto.response.AdminTestcaseResponse;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTestcaseRepository;
import com.swp391.coding_platform.repository.problem.ProblemVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemTestcaseServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ProblemTestcaseRepository problemTestcaseRepository;

    @Mock
    private ProblemSubmissionRepository problemSubmissionRepository;

    @Mock
    private ProblemVersionRepository problemVersionRepository;

    @InjectMocks
    private ProblemTestcaseService problemTestcaseService;

    @Test
    void getProblemTestcases_ProblemNotFound_ThrowsAppException() {
        when(problemRepository.findById(1)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> problemTestcaseService.getProblemTestcases(1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getProblemTestcases_Success() {
        ProblemEntity problem = mock(ProblemEntity.class);
        ProblemVersionEntity version = mock(ProblemVersionEntity.class);
        when(version.getId()).thenReturn(10);
        when(problem.getCurrentVersion()).thenReturn(version);
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));

        ProblemTestcaseEntity tc = mock(ProblemTestcaseEntity.class);
        when(tc.getInputData()).thenReturn("input");
        when(tc.getExpectedOutput()).thenReturn("output");
        when(problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndexAsc(10)).thenReturn(List.of(tc));

        List<AdminTestcaseResponse> responses = problemTestcaseService.getProblemTestcases(1);

        assertFalse(responses.isEmpty());
        assertEquals("input", responses.get(0).getInputData());
    }

    @Test
    void saveProblemTestcases_InvalidRequest_ThrowsAppException() {
        ProblemEntity problem = mock(ProblemEntity.class);
        when(problem.getIsActive()).thenReturn(true);
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));

        AdminTestcaseRequest req = mock(AdminTestcaseRequest.class);
        when(req.getInputData()).thenReturn("");

        AppException exception = assertThrows(AppException.class, () -> problemTestcaseService.saveProblemTestcases(1, List.of(req)));
        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    @Test
    void saveProblemTestcases_Success_WithoutSubmissions() {
        ProblemEntity problem = mock(ProblemEntity.class);
        when(problem.getIsActive()).thenReturn(true);
        ProblemVersionEntity version = mock(ProblemVersionEntity.class);
        when(version.getId()).thenReturn(10);
        when(problem.getCurrentVersion()).thenReturn(version);
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));

        AdminTestcaseRequest req = mock(AdminTestcaseRequest.class);
        when(req.getInputData()).thenReturn("in");
        when(req.getExpectedOutput()).thenReturn("out");

        when(problemSubmissionRepository.countByProblemVersionId(10)).thenReturn(0L);

        ProblemTestcaseEntity savedTc = mock(ProblemTestcaseEntity.class);
        when(savedTc.getInputData()).thenReturn("in");
        when(problemTestcaseRepository.saveAll(any())).thenReturn(List.of(savedTc));

        List<AdminTestcaseResponse> responses = problemTestcaseService.saveProblemTestcases(1, List.of(req));

        assertFalse(responses.isEmpty());
        verify(problemTestcaseRepository, times(1)).deleteByProblemVersionId(10);
        verify(problemRepository, times(1)).save(problem);
    }
}
