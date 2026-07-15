package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.response.ProblemDescriptionResponse;
import com.swp391.coding_platform.dto.response.ProblemListItemResponse;
import com.swp391.coding_platform.dto.response.ProblemSolutionResponse;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.enums.ProblemDifficulty;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.entity.problem.ProblemTagEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.problem.*;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private ProblemTagMappingRepository problemTagMappingRepository;
    @Mock
    private ProblemSubmissionRepository problemSubmissionRepository;
    @Mock
    private ProblemCommentRepository problemCommentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProblemTestcaseRepository problemTestcaseRepository;
    @Mock
    private ProblemTagRepository problemTagRepository;
    @Mock
    private ProblemVersionRepository problemVersionRepository;

    @InjectMocks
    private UserProblemService userProblemService;

    private ProblemEntity mockProblem;
    private ProblemVersionEntity mockVersion;

    @BeforeEach
    void setUp() {
        mockVersion = ProblemVersionEntity.builder()
                .title("Test Problem")
                .isActive(true)
                .difficulty(ProblemDifficulty.EASY)
                .starterTemplates("{\"python\":\"def solve(): pass\"}")
                .solutions("def solve():\n    return 42")
                .build();
        
        mockProblem = ProblemEntity.builder()
                .id(1)
                .isActive(true)
                .isPublic(true)
                .problemScope(ProblemScope.PRACTICE)
                .versions(new ArrayList<>(List.of(mockVersion)))
                .score(java.math.BigDecimal.valueOf(10))
                .totalSubmission(100)
                .totalAccepted(50)
                .build();
    }

    // ======================== getProblems ========================

    @Test
    void getProblems_success() {
        when(problemRepository.findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(anyList()))
                .thenReturn(List.of(mockProblem));
        when(problemTagMappingRepository.findByProblemIdIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(problemSubmissionRepository.findByUserIdAndProblemIdIn(eq(1), anyList()))
                .thenReturn(Collections.emptyList());

        List<ProblemListItemResponse> result = userProblemService.getProblems(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Test Problem", result.get(0).getTitle());
        assertEquals("Easy", result.get(0).getDifficulty());
    }

    @Test
    void getProblems_empty() {
        when(problemRepository.findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(anyList()))
                .thenReturn(Collections.emptyList());

        List<ProblemListItemResponse> result = userProblemService.getProblems(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProblems_userIdNull_ShouldSkipSubmissions() {
        when(problemRepository.findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(anyList()))
                .thenReturn(List.of(mockProblem));
        when(problemTagMappingRepository.findByProblemIdIn(anyList()))
                .thenReturn(Collections.emptyList());

        List<ProblemListItemResponse> result = userProblemService.getProblems(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("unsolved", result.get(0).getStatus());
        verifyNoInteractions(problemSubmissionRepository);
    }

    @Test
    void getProblems_withAcceptedSubmission_returnsSolved() {
        when(problemRepository.findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(anyList()))
                .thenReturn(List.of(mockProblem));
        when(problemTagMappingRepository.findByProblemIdIn(anyList()))
                .thenReturn(Collections.emptyList());

        ProblemSubmissionEntity sub = ProblemSubmissionEntity.builder()
                .problem(mockProblem)
                .verdict(OjVerdict.ACCEPTED)
                .build();
        when(problemSubmissionRepository.findByUserIdAndProblemIdIn(eq(1), anyList()))
                .thenReturn(List.of(sub));

        List<ProblemListItemResponse> result = userProblemService.getProblems(1);

        assertNotNull(result);
        assertEquals("solved", result.get(0).getStatus());
        assertTrue(Boolean.TRUE.equals(result.get(0).getIsSolved()));
    }

    @Test
    void getProblems_withFailedSubmissions_returnsAttempted() {
        when(problemRepository.findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(anyList()))
                .thenReturn(List.of(mockProblem));
        when(problemTagMappingRepository.findByProblemIdIn(anyList()))
                .thenReturn(Collections.emptyList());

        ProblemSubmissionEntity sub = ProblemSubmissionEntity.builder()
                .problem(mockProblem)
                .verdict(OjVerdict.WRONG_ANSWER)
                .build();
        when(problemSubmissionRepository.findByUserIdAndProblemIdIn(eq(1), anyList()))
                .thenReturn(List.of(sub));

        List<ProblemListItemResponse> result = userProblemService.getProblems(1);

        assertNotNull(result);
        assertEquals("attempted", result.get(0).getStatus());
        assertFalse(Boolean.TRUE.equals(result.get(0).getIsSolved()));
    }

    @Test
    void getProblems_difficultyAndScoreNull_usesDefaults() {
        mockVersion.setDifficulty(null);
        mockProblem.setScore(null);
        mockProblem.setTotalSubmission(null);
        mockProblem.setTotalAccepted(null);

        when(problemRepository.findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(anyList()))
                .thenReturn(List.of(mockProblem));
        when(problemTagMappingRepository.findByProblemIdIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(problemSubmissionRepository.findByUserIdAndProblemIdIn(eq(1), anyList()))
                .thenReturn(Collections.emptyList());

        List<ProblemListItemResponse> result = userProblemService.getProblems(1);

        assertNotNull(result);
        assertEquals("Medium", result.get(0).getDifficulty());
        assertEquals(0, result.get(0).getScore());
        assertEquals(0, result.get(0).getTotalSubmission());
        assertEquals(0, result.get(0).getTotalAccepted());
    }

    // ======================== getProblemDescription ========================

    @Test
    void getProblemDescription_notFound_throwsException() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userProblemService.getProblemDescription(1, 1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getProblemDescription_userIdNull_success() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTagMappingRepository.findByProblemId(1)).thenReturn(Collections.emptyList());

        ProblemDescriptionResponse result = userProblemService.getProblemDescription(1, null);

        assertNotNull(result);
        assertEquals("Test Problem", result.getTitle());
        assertEquals("unsolved", result.getStatus());
        assertEquals("50.0%", result.getAcceptance());
        assertNotNull(result.getTemplates());
        assertEquals("def solve(): pass", result.getTemplates().get("python"));
    }

    @Test
    void getProblemDescription_invalidTemplatesJson_ShouldHandleException() {
        mockVersion.setStarterTemplates("invalid json content");
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTagMappingRepository.findByProblemId(1)).thenReturn(Collections.emptyList());

        ProblemDescriptionResponse result = userProblemService.getProblemDescription(1, null);

        assertNotNull(result);
        assertTrue(result.getTemplates().isEmpty()); // Should default to empty map silently
    }

    @Test
    void getProblemDescription_userHasAcceptedSubmission_returnsSolvedAndSource() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTagMappingRepository.findByProblemId(1)).thenReturn(Collections.emptyList());

        ProblemSubmissionEntity s1 = ProblemSubmissionEntity.builder()
                .verdict(OjVerdict.ACCEPTED)
                .sourceCode("accepted python code")
                .languageId(71)
                .submittedAt(Instant.now().minusSeconds(10))
                .build();

        ProblemSubmissionEntity s2 = ProblemSubmissionEntity.builder()
                .verdict(OjVerdict.WRONG_ANSWER)
                .sourceCode("wrong code")
                .languageId(71)
                .submittedAt(Instant.now())
                .build();

        when(problemSubmissionRepository.findByUserIdAndProblemId(1, 1)).thenReturn(new ArrayList<>(List.of(s1, s2)));

        ProblemDescriptionResponse result = userProblemService.getProblemDescription(1, 1);

        assertNotNull(result);
        assertEquals("solved", result.getStatus());
        assertEquals("accepted python code", result.getSourceCode());
        assertEquals(71, result.getLanguageId());
    }

    @Test
    void getProblemDescription_userHasOnlyFailedSubmission_returnsAttemptedAndLatestSource() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTagMappingRepository.findByProblemId(1)).thenReturn(Collections.emptyList());

        ProblemSubmissionEntity s1 = ProblemSubmissionEntity.builder()
                .verdict(OjVerdict.TIME_LIMIT_EXCEEDED)
                .sourceCode("old source")
                .languageId(71)
                .submittedAt(Instant.now().minusSeconds(10))
                .build();

        ProblemSubmissionEntity s2 = ProblemSubmissionEntity.builder()
                .verdict(OjVerdict.WRONG_ANSWER)
                .sourceCode("latest source")
                .languageId(71)
                .submittedAt(Instant.now())
                .build();

        when(problemSubmissionRepository.findByUserIdAndProblemId(1, 1)).thenReturn(new ArrayList<>(List.of(s1, s2)));

        ProblemDescriptionResponse result = userProblemService.getProblemDescription(1, 1);

        assertNotNull(result);
        assertEquals("attempted", result.getStatus());
        assertEquals("latest source", result.getSourceCode());
        assertEquals(71, result.getLanguageId());
    }

    @Test
    void getProblemDescription_noSubmissions_acceptanceZero() {
        mockProblem.setTotalSubmission(0);
        mockProblem.setTotalAccepted(0); // Explicitly zero totalAccepted to match expected totalSolved in test
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTagMappingRepository.findByProblemId(1)).thenReturn(Collections.emptyList());

        ProblemDescriptionResponse result = userProblemService.getProblemDescription(1, 1);

        assertNotNull(result);
        assertEquals("0.0%", result.getAcceptance());
        assertEquals(0, result.getTotalSolved());
    }

    // ======================== getProblemSolution ========================

    @Test
    void getProblemSolution_userIdNull_throwsUnauthenticated() {
        AppException ex = assertThrows(AppException.class, () -> userProblemService.getProblemSolution(1, null));
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void getProblemSolution_notFound_throwsNotFound() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userProblemService.getProblemSolution(1, 1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getProblemSolution_notSolvedByUser_throwsLocked() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemSubmissionRepository.findByUserIdAndProblemId(1, 1)).thenReturn(Collections.emptyList());

        AppException ex = assertThrows(AppException.class, () -> userProblemService.getProblemSolution(1, 1));
        assertEquals(ErrorCode.OJ_SOLUTION_LOCKED, ex.getErrorCode());
    }

    @Test
    void getProblemSolution_solvedButBlankSolution_returnsDefaultPlaceholder() {
        mockVersion.setSolutions(null);
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));

        ProblemSubmissionEntity sub = ProblemSubmissionEntity.builder()
                .verdict(OjVerdict.ACCEPTED)
                .build();
        when(problemSubmissionRepository.findByUserIdAndProblemId(1, 1)).thenReturn(List.of(sub));

        ProblemSolutionResponse result = userProblemService.getProblemSolution(1, 1);

        assertNotNull(result);
        assertEquals("// An official solution for this problem is not available yet.", result.getSolutionCode());
    }

    @Test
    void getProblemSolution_solvedWithSolution_returnsSolutionCode() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));

        ProblemSubmissionEntity sub = ProblemSubmissionEntity.builder()
                .verdict(OjVerdict.ACCEPTED)
                .build();
        when(problemSubmissionRepository.findByUserIdAndProblemId(1, 1)).thenReturn(List.of(sub));

        ProblemSolutionResponse result = userProblemService.getProblemSolution(1, 1);

        assertNotNull(result);
        assertEquals("def solve():\n    return 42", result.getSolutionCode());
    }
}
