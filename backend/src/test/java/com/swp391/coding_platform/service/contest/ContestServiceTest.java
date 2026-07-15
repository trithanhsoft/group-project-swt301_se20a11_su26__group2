package com.swp391.coding_platform.service.contest;

import com.swp391.coding_platform.dto.request.AdminContestProblemRequest;
import com.swp391.coding_platform.dto.request.AdminContestRequest;
import com.swp391.coding_platform.dto.request.ContestRegisterRequest;
import com.swp391.coding_platform.dto.request.ContestSearchRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestParticipantEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.enums.ScoringRule;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.ContestMapper;
import com.swp391.coding_platform.repository.contest.ContestParticipantRepository;
import com.swp391.coding_platform.repository.contest.ContestProblemAttemptRepository;
import com.swp391.coding_platform.repository.contest.ContestProblemRepository;
import com.swp391.coding_platform.repository.contest.ContestRankingRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagMappingRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContestServiceTest {

    @Mock
    private ContestRepository contestRepository;
    @Mock
    private ContestMapper contestMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProblemSubmissionRepository problemSubmissionRepository;
    @Mock
    private ContestParticipantRepository contestParticipantRepository;
    @Mock
    private ContestProblemRepository contestProblemRepository;
    @Mock
    private ContestProblemAttemptRepository contestProblemAttemptRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private ProblemTagMappingRepository problemTagMappingRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ContestRankingRepository contestRankingRepository;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ContestService contestService;

    // ======================== getContests ========================

    @Test
    void getContests_Success() {
        ContestSearchRequest request = new ContestSearchRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSearch("Math");
        request.setSortBy("title");
        request.setSortDirection("asc");

        ContestEntity contest = ContestEntity.builder().id(1).status(ContestStatus.PUBLISHED).startTime(Instant.now()).endTime(Instant.now()).build();
        Object[] row = new Object[]{contest, 5L, 2L};

        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row));
        when(contestRepository.searchContestsWithStats(eq("Math"), anyString(), any(Instant.class), anyString(), any(Pageable.class)))
                .thenReturn(page);
        when(contestMapper.toContestResponse(contest)).thenReturn(new ContestResponse());

        PageResponse<ContestResponse> res = contestService.getContests(request, 1);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        verify(contestRepository).isUserRegistered(1, 1);
    }

    @Test
    void getContests_NonWhitelistedSort_ShouldFallback() {
        ContestSearchRequest request = new ContestSearchRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("invalid_column_name"); // Not allowed

        Page<Object[]> page = new PageImpl<>(Collections.emptyList());
        when(contestRepository.searchContestsWithStats(any(), anyString(), any(Instant.class), anyString(), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<ContestResponse> res = contestService.getContests(request, null);

        assertNotNull(res);
        assertTrue(res.getContent().isEmpty());
    }

    // ======================== getBannerContest ========================

    @Test
    void getBannerContest_Success() {
        ContestEntity contest = ContestEntity.builder().id(1).status(ContestStatus.PUBLISHED).startTime(Instant.now()).endTime(Instant.now()).build();
        Page<ContestEntity> page = new PageImpl<>(List.of(contest));

        when(contestRepository.findUpcomingContests(any(Instant.class), any(Pageable.class))).thenReturn(page);
        when(contestRepository.countParticipants(1)).thenReturn(10L);
        when(contestRepository.countProblems(1)).thenReturn(3L);
        when(contestRepository.isUserRegistered(1, 2)).thenReturn(true);
        when(contestMapper.toContestResponse(contest)).thenReturn(new ContestResponse());

        ContestResponse res = contestService.getBannerContest(2);

        assertNotNull(res);
        assertTrue(res.getIsUserRegistered());
        assertEquals(10, res.getParticipantCount());
    }

    @Test
    void getBannerContest_NoUpcomingContests_ReturnsNull() {
        when(contestRepository.findUpcomingContests(any(Instant.class), any(Pageable.class))).thenReturn(Page.empty());

        ContestResponse res = contestService.getBannerContest(1);

        assertNull(res);
    }

    // ======================== getUserStats ========================

    @Test
    void getUserStats_Success() {
        UserEntity user = UserEntity.builder().id(1).displayname("Alice").avatarurl("avatar.png").score(1500).build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.getUserRanking(1)).thenReturn(5);
        when(userRepository.count()).thenReturn(100L);
        when(contestRepository.countUserContests(1)).thenReturn(3L);
        when(problemSubmissionRepository.findByUserId(1)).thenReturn(Collections.emptyList());

        ContestUserStatsResponse res = contestService.getUserStats(1);

        assertNotNull(res);
        assertEquals("Alice", res.getDisplayName());
        assertEquals(5, res.getRank());
        assertEquals(1500, res.getScore());
    }

    @Test
    void getUserStats_UserIdNull_ReturnsNull() {
        assertNull(contestService.getUserStats(null));
    }

    @Test
    void getUserStats_UserNotFound_ReturnsNull() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        assertNull(contestService.getUserStats(999));
    }

    // ======================== registerForContest ========================

    @Test
    void registerForContest_UserNotAuthenticated_ShouldThrow() {
        AppException ex = assertThrows(AppException.class, () ->
            contestService.registerForContest(1, null, new ContestRegisterRequest())
        );
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void registerForContest_UserNotFound_ShouldThrow() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
            contestService.registerForContest(1, 999, new ContestRegisterRequest())
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void registerForContest_ContestNotFound_ShouldThrow() {
        UserEntity user = UserEntity.builder().id(1).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(contestRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
            contestService.registerForContest(999, 1, new ContestRegisterRequest())
        );
        assertEquals(ErrorCode.CONTEST_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void registerForContest_ContestEnded_ShouldThrow() {
        UserEntity user = UserEntity.builder().id(1).build();
        ContestEntity contest = ContestEntity.builder()
                .id(1)
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .status(ContestStatus.PUBLISHED)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));

        AppException ex = assertThrows(AppException.class, () ->
            contestService.registerForContest(1, 1, new ContestRegisterRequest())
        );
        assertEquals(ErrorCode.CONTEST_ALREADY_ENDED, ex.getErrorCode());
    }

    @Test
    void registerForContest_AlreadyRegistered_ShouldReturnEarly() {
        UserEntity user = UserEntity.builder().id(1).build();
        ContestEntity contest = ContestEntity.builder()
                .id(1)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .status(ContestStatus.PUBLISHED)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(1, 1)).thenReturn(true);

        // Should return early and not save participant again
        assertDoesNotThrow(() -> contestService.registerForContest(1, 1, new ContestRegisterRequest()));
        verify(contestParticipantRepository, never()).save(any());
    }

    @Test
    void registerForContest_PasswordInvalid_ShouldThrow() {
        UserEntity user = UserEntity.builder().id(1).build();
        ContestEntity contest = ContestEntity.builder()
                .id(1)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .status(ContestStatus.PUBLISHED)
                .passwordHash("hashed_password")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(1, 1)).thenReturn(false);
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        ContestRegisterRequest request = new ContestRegisterRequest("wrong_password");

        AppException ex = assertThrows(AppException.class, () ->
            contestService.registerForContest(1, 1, request)
        );
        assertEquals(ErrorCode.CONTEST_PASSWORD_INVALID, ex.getErrorCode());
    }

    // ======================== Admin operations ========================

    @Test
    void testUpdateAdminContest_Ongoing_CoreFieldsLock() {
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

        AdminContestRequest request = new AdminContestRequest();
        request.setTitle("Updated Title");
        request.setDescription("Original Description");
        request.setScoringRule("IOI");
        request.setStartTime(startTime);
        request.setEndTime(endTime.plus(1, ChronoUnit.HOURS));

        AppException exception = assertThrows(AppException.class, () ->
            contestService.updateAdminContest(contestId, request)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(contestRepository, never()).save(any(ContestEntity.class));
    }

    @Test
    void testUpdateAdminContest_Ongoing_AllowedFieldsOnly() {
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
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");
        request.setScoringRule("ICPC");
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        AdminContestResponse response = contestService.updateAdminContest(contestId, request);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated Title", contest.getTitle());
        assertEquals("Updated Description", contest.getDescription());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testDeleteAdminContest_Ongoing_ShouldThrow() {
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

        AppException exception = assertThrows(AppException.class, () ->
            contestService.deleteAdminContest(contestId)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(contestRepository, never()).save(any(ContestEntity.class));
    }

    @Test
    void testDeleteAdminContest_Draft_ShouldSucceed() {
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

        contestService.deleteAdminContest(contestId);

        assertEquals(ContestStatus.DELETED, contest.getStatus());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testPublishAdminContest_Draft_ShouldSucceed() {
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

        AdminContestResponse response = contestService.publishAdminContest(contestId);

        assertNotNull(response);
        assertEquals(ContestStatus.PUBLISHED, contest.getStatus());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testRestoreAdminContest_Deleted_ShouldSucceed() {
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

        AdminContestResponse response = contestService.restoreAdminContest(contestId);

        assertNotNull(response);
        assertEquals(ContestStatus.DRAFT, contest.getStatus());
        verify(contestRepository, times(1)).save(contest);
    }

    @Test
    void testHardDeleteAdminContest_DraftWithSubmissions_ShouldThrow() {
        Integer contestId = 1;
        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .status(ContestStatus.DELETED)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(problemSubmissionRepository.countByContestId(contestId)).thenReturn(5L);

        AppException exception = assertThrows(AppException.class, () ->
            contestService.hardDeleteAdminContest(contestId)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(contestRepository, never()).delete(any(ContestEntity.class));
    }

    @Test
    void testHardDeleteAdminContest_DraftWithoutSubmissions_ShouldSucceed() {
        Integer contestId = 1;
        ContestEntity contest = ContestEntity.builder()
                .id(contestId)
                .status(ContestStatus.DELETED)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(problemSubmissionRepository.countByContestId(contestId)).thenReturn(0L);

        contestService.hardDeleteAdminContest(contestId);

        verify(contestParticipantRepository, times(1)).deleteByContestId(contestId);
        verify(contestProblemRepository, times(1)).deleteByContestId(contestId);
        verify(contestRepository, times(1)).delete(contest);
    }

    // ======================== getContestProblems ========================

    @Test
    void getContestProblems_Unauthenticated_ShouldThrow() {
        AppException ex = assertThrows(AppException.class, () ->
            contestService.getContestProblems(1, null, false)
        );
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void getContestProblems_ContestNotFound_ShouldThrow() {
        when(contestRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
            contestService.getContestProblems(999, 1, false)
        );
        assertEquals(ErrorCode.CONTEST_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getContestProblems_NotRegisteredNonAdmin_ShouldThrow() {
        ContestEntity contest = ContestEntity.builder()
                .id(1)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .status(ContestStatus.PUBLISHED)
                .build();

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(1, 1)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () ->
            contestService.getContestProblems(1, 1, false) // isAdmin=false
        );
        assertEquals(ErrorCode.CONTEST_NOT_JOINED, ex.getErrorCode());
    }

    @Test
    void getContestProblems_UpcomingContest_ShouldThrow() {
        ContestEntity contest = ContestEntity.builder()
                .id(1)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .status(ContestStatus.PUBLISHED)
                .build();

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(1, 1)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () ->
            contestService.getContestProblems(1, 1, false)
        );
        assertEquals(ErrorCode.CONTEST_NOT_STARTED, ex.getErrorCode());
    }

    // ======================== Problem updates in contests ========================

    @Test
    void addProblemToContest_OngoingContest_ShouldThrow() {
        ContestEntity contest = ContestEntity.builder()
                .id(1)
                .status(ContestStatus.PUBLISHED)
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));

        AdminContestProblemRequest request = new AdminContestProblemRequest();
        request.setProblemId(10);
        request.setOrderIndex(1);

        AppException ex = assertThrows(AppException.class, () ->
            contestService.addProblemToContest(1, request)
        );
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void addProblemToContest_AlreadyAdded_ShouldThrow() {
        ContestEntity contest = ContestEntity.builder()
                .id(1)
                .status(ContestStatus.DRAFT)
                .startTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(problemRepository.findById(10)).thenReturn(Optional.of(new ProblemEntity()));
        when(contestProblemRepository.existsByContestIdAndProblemId(1, 10)).thenReturn(true);

        AdminContestProblemRequest request = new AdminContestProblemRequest();
        request.setProblemId(10);
        request.setOrderIndex(1);

        AppException ex = assertThrows(AppException.class, () ->
            contestService.addProblemToContest(1, request)
        );
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }
}
