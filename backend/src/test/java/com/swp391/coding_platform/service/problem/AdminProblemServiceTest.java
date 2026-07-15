package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.request.AdminProblemRequest;
import com.swp391.coding_platform.dto.response.AdminProblemResponse;
import com.swp391.coding_platform.entity.enums.*;
import com.swp391.coding_platform.entity.problem.*;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.problem.*;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProblemServiceTest {

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
    private AdminProblemService adminProblemService;

    @Test
    void getAdminProblems_Success() {
        ProblemEntity problem = new ProblemEntity();
        problem.setId(1);
        problem.setProblemScope(ProblemScope.PRACTICE);
        problem.setIsActive(true);
        problem.setTotalTestcase(5);
        ProblemVersionEntity version = new ProblemVersionEntity();
        version.setTitle("Test Problem");
        problem.setCurrentVersion(version);

        when(problemRepository.findByProblemScopeIn(anyList()))
                .thenReturn(Collections.singletonList(problem));
        when(problemTagMappingRepository.findByProblemId(1))
                .thenReturn(Collections.emptyList());

        List<AdminProblemResponse> result = adminProblemService.getAdminProblems();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Problem", result.get(0).getTitle());
    }

    @Test
    void createAdminProblem_UserNotFound_ThrowsException() {
        AdminProblemRequest request = new AdminProblemRequest();
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> adminProblemService.createAdminProblem(request, 1));
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createAdminProblem_Success() {
        UserEntity user = UserEntity.builder().id(1).build();
        AdminProblemRequest request = new AdminProblemRequest();
        request.setTitle("Two Sum");
        request.setDifficulty("EASY");
        request.setProblemScope("PRACTICE");
        request.setIsPublic(true); // Should force to false initially since total testcases is 0
        request.setTags(List.of("Array"));

        ProblemTagEntity tag = ProblemTagEntity.builder().id(10).name("Array").build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(problemTagRepository.findByName("Array")).thenReturn(Optional.of(tag));
        when(problemRepository.save(any(ProblemEntity.class))).thenAnswer(i -> {
            ProblemEntity p = i.getArgument(0);
            p.setId(5);
            return p;
        });

        AdminProblemResponse response = adminProblemService.createAdminProblem(request, 1);

        assertNotNull(response);
        assertEquals(5, response.getId());
        assertFalse(response.getIsPublic());
        verify(problemTagMappingRepository, times(1)).save(any(ProblemTagMappingEntity.class));
    }

    @Test
    void updateAdminProblem_ProblemNotFound_ThrowsException() {
        AdminProblemRequest request = new AdminProblemRequest();
        when(problemRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> adminProblemService.updateAdminProblem(1, request));
        verify(problemRepository, never()).save(any());
    }

    @Test
    void updateAdminProblem_Success_NoSubmissions() {
        ProblemVersionEntity version = ProblemVersionEntity.builder().id(100).title("Old Title").build();
        ProblemEntity problem = ProblemEntity.builder()
                .id(1)
                .isActive(true)
                .totalTestcase(5)
                .build();
        problem.setCurrentVersion(version);

        AdminProblemRequest request = new AdminProblemRequest();
        request.setTitle("New Title");
        request.setDifficulty("HARD");
        request.setProblemScope("CONTEST");
        request.setScore(50.0);
        request.setIsPublic(true);

        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemSubmissionRepository.countByProblemVersionId(100)).thenReturn(0L);
        when(problemRepository.save(any(ProblemEntity.class))).thenAnswer(i -> i.getArgument(0));

        AdminProblemResponse response = adminProblemService.updateAdminProblem(1, request);

        assertNotNull(response);
        assertEquals("New Title", version.getTitle());
        assertEquals(ProblemDifficulty.HARD, version.getDifficulty());
        assertEquals(ProblemScope.CONTEST, problem.getProblemScope());
        assertTrue(problem.getIsPublic());
    }

    @Test
    void updateAdminProblem_Success_WithSubmissions() {
        ProblemVersionEntity oldVersion = ProblemVersionEntity.builder()
                .id(100)
                .title("Old Title")
                .versionNumber(1)
                .build();
        
        List<ProblemVersionEntity> versions = new ArrayList<>();
        versions.add(oldVersion);

        ProblemEntity problem = ProblemEntity.builder()
                .id(1)
                .isActive(true)
                .totalTestcase(5)
                .versions(versions)
                .build();
        problem.setCurrentVersion(oldVersion);

        AdminProblemRequest request = new AdminProblemRequest();
        request.setTitle("New Title");

        ProblemTestcaseEntity oldTc = ProblemTestcaseEntity.builder().id(10).inputData("1").expectedOutput("2").build();

        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemSubmissionRepository.countByProblemVersionId(100)).thenReturn(5L); // Has submissions
        when(problemVersionRepository.save(any(ProblemVersionEntity.class))).thenAnswer(i -> {
            ProblemVersionEntity v = i.getArgument(0);
            v.setId(101);
            return v;
        });
        when(problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndexAsc(101)).thenReturn(List.of(oldTc));
        when(problemRepository.save(any(ProblemEntity.class))).thenAnswer(i -> i.getArgument(0));

        AdminProblemResponse response = adminProblemService.updateAdminProblem(1, request);

        assertNotNull(response);
        assertEquals(101, problem.getCurrentVersion().getId());
        assertEquals(2, problem.getCurrentVersion().getVersionNumber());
        assertEquals("New Title", problem.getCurrentVersion().getTitle());
        verify(problemTestcaseRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateAdminProblemScope_Success() {
        ProblemVersionEntity version = ProblemVersionEntity.builder().id(100).title("Title").build();
        ProblemEntity problem = ProblemEntity.builder().id(1).isActive(true).totalTestcase(5).build();
        problem.setCurrentVersion(version);
        
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(ProblemEntity.class))).thenAnswer(i -> i.getArgument(0));

        AdminProblemResponse response = adminProblemService.updateAdminProblemScope(1, "CONTEST");

        assertNotNull(response);
        assertEquals(ProblemScope.CONTEST, problem.getProblemScope());
    }

    @Test
    void updateAdminProblemPublicStatus_Success() {
        ProblemVersionEntity version = ProblemVersionEntity.builder().id(100).title("Title").build();
        ProblemEntity problem = ProblemEntity.builder().id(1).isActive(true).totalTestcase(5).build();
        problem.setCurrentVersion(version);
        
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(ProblemEntity.class))).thenAnswer(i -> i.getArgument(0));

        AdminProblemResponse response = adminProblemService.updateAdminProblemPublicStatus(1, true);

        assertNotNull(response);
        assertTrue(problem.getIsPublic());
    }

    @Test
    void updateAdminProblemPublicStatus_NoTestcases_ThrowsException() {
        ProblemEntity problem = ProblemEntity.builder().id(1).isActive(true).totalTestcase(0).build();
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));

        AppException ex = assertThrows(AppException.class, () -> adminProblemService.updateAdminProblemPublicStatus(1, true));
        assertEquals(ErrorCode.OJ_PROBLEM_MISSING_TESTCASE, ex.getErrorCode());
    }

    @Test
    void activateAdminProblem_Success() {
        ProblemVersionEntity version = ProblemVersionEntity.builder().id(100).title("Title").build();
        ProblemEntity problem = ProblemEntity.builder().id(1).isActive(false).totalTestcase(0).build();
        problem.setCurrentVersion(version);
        
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemRepository.save(any(ProblemEntity.class))).thenAnswer(i -> i.getArgument(0));

        AdminProblemResponse response = adminProblemService.activateAdminProblem(1, 10);

        assertNotNull(response);
        assertTrue(problem.getIsActive());
        assertEquals(10, problem.getTotalTestcase());
    }

    @Test
    void rollbackAdminProblem_Success() {
        ProblemVersionEntity oldVersion = ProblemVersionEntity.builder().id(100).title("Old Title").versionNumber(1).build();
        List<ProblemVersionEntity> versions = new ArrayList<>();
        versions.add(oldVersion);

        ProblemEntity problem = ProblemEntity.builder().id(1).isActive(true).totalTestcase(5).versions(versions).build();
        problem.setCurrentVersion(oldVersion);

        ProblemVersionEntity targetVersion = ProblemVersionEntity.builder().id(200).title("Target Title").problem(problem).build();
        ProblemTestcaseEntity tc = ProblemTestcaseEntity.builder().id(50).inputData("5").expectedOutput("6").build();

        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemVersionRepository.findById(200)).thenReturn(Optional.of(targetVersion));
        when(problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndexAsc(200)).thenReturn(List.of(tc));
        when(problemVersionRepository.save(any(ProblemVersionEntity.class))).thenAnswer(i -> {
            ProblemVersionEntity v = i.getArgument(0);
            v.setId(201);
            return v;
        });
        when(problemRepository.save(any(ProblemEntity.class))).thenAnswer(i -> i.getArgument(0));

        AdminProblemResponse response = adminProblemService.rollbackAdminProblem(1, 200);

        assertNotNull(response);
        assertEquals(201, problem.getCurrentVersion().getId());
        assertEquals("Target Title", problem.getCurrentVersion().getTitle());
        verify(problemTestcaseRepository, times(1)).saveAll(anyList());
    }

    @Test
    void deleteAdminProblem_SoftDelete_Success() {
        ProblemEntity problem = ProblemEntity.builder().id(1).isActive(true).totalTestcase(5).build();
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemSubmissionRepository.countByProblemId(1)).thenReturn(10L); // Has submissions

        adminProblemService.deleteAdminProblem(1);

        assertFalse(problem.getIsActive());
        assertFalse(problem.getIsPublic());
        verify(problemRepository, times(1)).save(problem);
        verify(problemRepository, never()).delete(any(ProblemEntity.class));
    }

    @Test
    void deleteAdminProblem_HardDelete_Success() {
        ProblemEntity problem = ProblemEntity.builder().id(1).isActive(true).totalTestcase(5).build();
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));
        when(problemSubmissionRepository.countByProblemId(1)).thenReturn(0L); // No submissions

        adminProblemService.deleteAdminProblem(1);

        verify(problemCommentRepository, times(1)).deleteByProblemId(1);
        verify(problemRepository, times(1)).delete(any(ProblemEntity.class));
    }
}
