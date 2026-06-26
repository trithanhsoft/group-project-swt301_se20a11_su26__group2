package com.swp391.coding_platform.service.contest;

import com.swp391.coding_platform.dto.request.AdminContestRequest;
import com.swp391.coding_platform.dto.response.AdminContestResponse;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import com.swp391.coding_platform.entity.enums.ScoringRule;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.ContestMapper;
import com.swp391.coding_platform.repository.contest.ContestParticipantRepository;
import com.swp391.coding_platform.repository.contest.ContestProblemAttemptRepository;
import com.swp391.coding_platform.repository.contest.ContestProblemRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContestServiceTest {

    @Mock
    ContestRepository contestRepository;
    @Mock
    ContestMapper contestMapper;
    @Mock
    UserRepository userRepository;
    @Mock
    ProblemSubmissionRepository problemSubmissionRepository;
    @Mock
    ContestParticipantRepository contestParticipantRepository;
    @Mock
    ContestProblemRepository contestProblemRepository;
    @Mock
    ContestProblemAttemptRepository contestProblemAttemptRepository;
    @Mock
    ProblemRepository problemRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    ContestService contestService;

    @Test
    void testUpdateAdminContest_Ongoing_CoreFieldsLock() {
        // Arrange
        Integer contestId = 1;
        Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .title("Original Title")
                .description("Original Description")
                .scoringRule(ScoringRule.ICPC)
                .startTime(startTime)
                .endTime(endTime)
                .status(ContestStatus.PUBLISHED) // Status dynamically becomes ONGOING because now is between start and end
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));

        AdminContestRequest request = new AdminContestRequest();
        request.setTitle("Updated Title");
        request.setDescription("Original Description");
        request.setScoringRule("IOI"); // Core field modification!
        request.setStartTime(startTime);
        request.setEndTime(endTime.plus(1, ChronoUnit.HOURS));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            contestService.updateAdminContest(contestId, request);
        });

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(contestRepository, never()).save(any(ContestEntity.class));
    }

    @Test
    void testUpdateAdminContest_Ongoing_AllowedFieldsOnly() {
        // Arrange
        Integer contestId = 1;
        Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .title("Original Title")
                .description("Original Description")
                .scoringRule(ScoringRule.ICPC)
                .startTime(startTime)
                .endTime(endTime)
                .status(ContestStatus.PUBLISHED)
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestMapper.toAdminContestResponse(any(ContestEntity.class)))
                .thenReturn(AdminContestResponse.builder().id(contestId).title("Updated Title").build());

        AdminContestRequest request = new AdminContestRequest();
        request.setTitle("Updated Title"); // Allowed field modification
        request.setDescription("Updated Description"); // Allowed field modification
        request.setScoringRule("ICPC"); // Unchanged core field
        request.setStartTime(startTime); // Unchanged core field
        request.setEndTime(endTime); // Unchanged core field

        // Act
        AdminContestResponse response = contestService.updateAdminContest(contestId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated Title", contest.getTitle());
        assertEquals("Updated Description", contest.getDescription());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testDeleteAdminContest_Ongoing_ShouldThrow() {
        // Arrange
        Integer contestId = 1;
        Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .startTime(startTime)
                .endTime(endTime)
                .status(ContestStatus.PUBLISHED)
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            contestService.deleteAdminContest(contestId);
        });

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(contestRepository, never()).save(any(ContestEntity.class));
    }

    @Test
    void testDeleteAdminContest_Draft_ShouldSucceed() {
        // Arrange
        Integer contestId = 1;
        Instant startTime = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);

        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .startTime(startTime)
                .endTime(endTime)
                .status(ContestStatus.DRAFT)
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));

        // Act
        contestService.deleteAdminContest(contestId);

        // Assert
        assertEquals(ContestStatus.DELETED, contest.getStatus());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testPublishAdminContest_Draft_ShouldSucceed() {
        // Arrange
        Integer contestId = 1;
        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .status(ContestStatus.DRAFT)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestMapper.toAdminContestResponse(any(ContestEntity.class)))
                .thenReturn(AdminContestResponse.builder().id(contestId).databaseStatus("PUBLISHED").build());

        // Act
        AdminContestResponse response = contestService.publishAdminContest(contestId);

        // Assert
        assertNotNull(response);
        assertEquals(ContestStatus.PUBLISHED, contest.getStatus());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testRestoreAdminContest_Deleted_ShouldSucceed() {
        // Arrange
        Integer contestId = 1;
        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .status(ContestStatus.DELETED)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestMapper.toAdminContestResponse(any(ContestEntity.class)))
                .thenReturn(AdminContestResponse.builder().id(contestId).isDeleted(false).build());

        // Act
        AdminContestResponse response = contestService.restoreAdminContest(contestId);

        // Assert
        assertNotNull(response);
        assertEquals(ContestStatus.DRAFT, contest.getStatus());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testHardDeleteAdminContest_DraftWithSubmissions_ShouldThrow() {
        // Arrange
        Integer contestId = 1;
        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .status(ContestStatus.DELETED)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(problemSubmissionRepository.countByContestId(contestId)).thenReturn(5L); // Has submissions!

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            contestService.hardDeleteAdminContest(contestId);
        });

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(contestRepository, never()).delete(any(ContestEntity.class));
    }

    @Test
    void testHardDeleteAdminContest_DraftWithoutSubmissions_ShouldSucceed() {
        // Arrange
        Integer contestId = 1;
        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .status(ContestStatus.DELETED)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(problemSubmissionRepository.countByContestId(contestId)).thenReturn(0L);

        // Act
        contestService.hardDeleteAdminContest(contestId);

        // Assert
        verify(contestParticipantRepository, times(1)).deleteByContestId(contestId);
        verify(contestProblemRepository, times(1)).deleteByContestId(contestId);
        verify(contestRepository, times(1)).delete(contest);
    }
}
