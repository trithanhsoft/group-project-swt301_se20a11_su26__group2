package com.swp391.coding_platform.service.judge0;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.judge0.Judge0BatchRequest;
import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload;
import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload.Judge0Status;
import com.swp391.coding_platform.dto.judge0.Judge0TokenResponse;
import com.swp391.coding_platform.dto.request.OjSubmissionRequest;
import com.swp391.coding_platform.dto.response.OjSubmissionInitialResponse;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionDetailEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.contest.ContestProblemRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.course.LessonProblemRepository;
import com.swp391.coding_platform.repository.course.LessonRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionDetailRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTestcaseRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Judge0ServiceTest {

    @Mock
    private ProblemSubmissionRepository problemSubmissionRepository;
    @Mock
    private ProblemSubmissionDetailRepository problemSubmissionDetailRepository;
    @Mock
    private ProblemTestcaseRepository problemTestcaseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private ContestRepository contestRepository;
    @Mock
    private ContestProblemRepository contestProblemRepository;
    @Mock
    private LessonProblemRepository lessonProblemRepository;
    @Mock
    private Judge0ClientService judge0ClientService;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private Judge0Service judge0Service;

    private UserEntity mockUser;
    private ProblemEntity mockProblem;
    private ProblemTestcaseEntity mockTestcase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(judge0Service, "webhookBaseUrl", "http://localhost:8080");

        mockUser = UserEntity.builder().id(1).status(com.swp391.coding_platform.entity.enums.UserStatus.ACTIVE).build();
        
        com.swp391.coding_platform.entity.problem.ProblemVersionEntity mockVersion = com.swp391.coding_platform.entity.problem.ProblemVersionEntity.builder()
                .id(1)
                .isActive(true)
                .timeLimitMs(1000)
                .memoryLimitKb(128000)
                .build();
                
        mockProblem = ProblemEntity.builder()
                .id(1)
                .isPublic(true)
                .versions(new java.util.ArrayList<>(java.util.List.of(mockVersion)))
                .build();
        mockVersion.setProblem(mockProblem);
        
        mockTestcase = ProblemTestcaseEntity.builder().id(1).inputData("1 2").expectedOutput("3").build();
    }

    // ======================== submitCode ========================

    @Test
    void submitCode_success() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setLanguageId(71);
        request.setSourceCode("print(sum(map(int, input().split())))");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndex(1)).thenReturn(List.of(mockTestcase));

        Judge0TokenResponse tokenResponse = new Judge0TokenResponse();
        tokenResponse.setToken("token123");
        when(judge0ClientService.sendBatchSubmission(any(Judge0BatchRequest.class))).thenReturn(List.of(tokenResponse));
        when(userRepository.getReferenceById(1)).thenReturn(mockUser);
        when(problemRepository.getReferenceById(1)).thenReturn(mockProblem);

        OjSubmissionInitialResponse response = judge0Service.submitCode(request, 1);

        assertNotNull(response);
        assertEquals(OjVerdict.PENDING.toString(), response.getStatus());
        verify(problemSubmissionRepository, times(1)).save(any(ProblemSubmissionEntity.class));
        verify(problemSubmissionDetailRepository, times(1)).saveAll(anyList());
        verify(problemRepository, times(1)).incrementTotalSubmission(1);
    }

    @Test
    void submitCode_userNotFound() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void submitCode_problemNotFound() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void submitCode_tokenListMismatch_ThrowsAppException() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setLanguageId(71);
        request.setSourceCode("print(sum(map(int, input().split())))");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndex(1)).thenReturn(List.of(mockTestcase));
        when(judge0ClientService.sendBatchSubmission(any(Judge0BatchRequest.class))).thenReturn(Collections.emptyList());

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.JUDGE0_SUBMISSION_FAILED, ex.getErrorCode());
    }

    @Test
    void submitCode_withContestId_ContestNotFound_ThrowsAppException() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setContestId(99);

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(contestRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.CONTEST_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void submitCode_withContestId_ContestNotOngoing_ThrowsAppException() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setContestId(99);

        ContestEntity contest = ContestEntity.builder()
                .id(99)
                .status(ContestStatus.DRAFT)
                .startTime(Instant.now().plusSeconds(3600)) // not started yet
                .endTime(Instant.now().plusSeconds(7200))
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(contestRepository.findById(99)).thenReturn(Optional.of(contest));

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.CONTEST_SUBMISSION_NOT_ALLOWED, ex.getErrorCode());
    }

    @Test
    void submitCode_withContestId_UserNotRegistered_ThrowsAppException() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setContestId(99);

        ContestEntity contest = ContestEntity.builder()
                .id(99)
                .status(ContestStatus.PUBLISHED)
                .startTime(Instant.now().minusSeconds(3600)) // already started
                .endTime(Instant.now().plusSeconds(3600))   // not ended yet
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(contestRepository.findById(99)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(99, 1)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.CONTEST_NOT_JOINED, ex.getErrorCode());
    }

    @Test
    void submitCode_withContestId_ProblemNotInContest_ThrowsAppException() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setContestId(99);

        ContestEntity contest = ContestEntity.builder()
                .id(99)
                .status(ContestStatus.PUBLISHED)
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(contestRepository.findById(99)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(99, 1)).thenReturn(true);
        when(contestProblemRepository.existsByContestIdAndProblemId(99, 1)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void submitCode_withLessonId_ProblemNotInLesson_ThrowsAppException() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setLessonId(55);

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(lessonProblemRepository.existsByLessonIdAndProblemId(55, 1)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void submitCode_emptyTestcaseList_ThrowsAppException() {
        OjSubmissionRequest request = new OjSubmissionRequest();
        request.setProblemId(1);
        request.setLanguageId(71);
        request.setSourceCode("code");

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(mockProblem));
        when(problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndex(1)).thenReturn(Collections.emptyList());

        AppException ex = assertThrows(AppException.class, () -> judge0Service.submitCode(request, 1));
        assertEquals(ErrorCode.TESTCASE_NOT_FOUND, ex.getErrorCode());
    }

    // ======================== processJudge0Callback ========================

    @Test
    void processJudge0Callback_SubmissionDetailNotFound_ThrowsAppException() {
        Judge0CallbackPayload payload = new Judge0CallbackPayload();
        payload.setToken("invalid-token");

        when(problemSubmissionDetailRepository.findByTokenWithSubmissionAndProblem("invalid-token")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> judge0Service.processJudge0Callback(payload));
        assertEquals(ErrorCode.JUDGE0_SUBMISSION_FAILED, ex.getErrorCode());
    }

    @Test
    void processJudge0Callback_AcceptedVerdict_ShouldUpdateDetailAndCheckCompletion() {
        Judge0CallbackPayload payload = buildCallbackPayload("token-1", 3, null); // status 3 = AC

        ProblemEntity problem = ProblemEntity.builder().id(1).isPublic(true).build();
        UserEntity user = UserEntity.builder().id(1).build();

        ProblemSubmissionEntity submission = ProblemSubmissionEntity.builder()
                .id(10)
                .problem(problem)
                .user(user)
                .verdict(OjVerdict.PENDING)
                .build();

        ProblemSubmissionDetailEntity detail = ProblemSubmissionDetailEntity.builder()
                .id(1)
                .token("token-1")
                .submission(submission)
                .verdict(OjVerdict.PENDING)
                .build();

        when(problemSubmissionDetailRepository.findByTokenWithSubmissionAndProblem("token-1"))
                .thenReturn(Optional.of(detail));
        when(problemSubmissionDetailRepository.countBySubmissionId(10)).thenReturn(1L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
        when(problemSubmissionDetailRepository.findFirstBySubmissionIdAndVerdictNotOrderByTestcaseOrderIndexAsc(anyInt(), any()))
                .thenReturn(Optional.empty());
        // findMaxStatsBySubmissionId returns empty → throws SUBMISSION_NOT_FOUND, but that's OK for this test scope
        when(problemSubmissionDetailRepository.findMaxStatsBySubmissionId(anyInt()))
                .thenReturn(Optional.empty());

        // Will throw SUBMISSION_NOT_FOUND due to empty maxStats - that's acceptable for testing verdict mapping
        assertThrows(AppException.class, () -> judge0Service.processJudge0Callback(payload));

        verify(problemSubmissionDetailRepository).save(detail);
        assertEquals(OjVerdict.ACCEPTED, detail.getVerdict());
    }

    @Test
    void processJudge0Callback_WrongAnswerVerdict_ShouldMarkWA() {
        Judge0CallbackPayload payload = buildCallbackPayload("token-2", 4, null); // status 4 = WA

        ProblemEntity problem = ProblemEntity.builder().id(1).isPublic(true).build();
        UserEntity user = UserEntity.builder().id(1).build();

        ProblemSubmissionEntity submission = ProblemSubmissionEntity.builder()
                .id(11)
                .problem(problem)
                .user(user)
                .verdict(OjVerdict.PENDING)
                .build();

        ProblemSubmissionDetailEntity detail = ProblemSubmissionDetailEntity.builder()
                .id(2)
                .token("token-2")
                .submission(submission)
                .verdict(OjVerdict.PENDING)
                .build();

        when(problemSubmissionDetailRepository.findByTokenWithSubmissionAndProblem("token-2"))
                .thenReturn(Optional.of(detail));
        when(problemSubmissionDetailRepository.countBySubmissionId(11)).thenReturn(1L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
        when(problemSubmissionDetailRepository.findFirstBySubmissionIdAndVerdictNotOrderByTestcaseOrderIndexAsc(anyInt(), any()))
                .thenReturn(Optional.of(detail));
        when(problemSubmissionDetailRepository.findMaxStatsBySubmissionId(anyInt()))
                .thenReturn(Optional.empty());

        // Will throw SUBMISSION_NOT_FOUND due to empty maxStats
        assertThrows(AppException.class, () -> judge0Service.processJudge0Callback(payload));

        verify(problemSubmissionDetailRepository).save(detail);
        assertEquals(OjVerdict.WRONG_ANSWER, detail.getVerdict());
    }

    // ======================== HELPER ========================

    private Judge0CallbackPayload buildCallbackPayload(String token, int statusId, String time) {
        Judge0CallbackPayload payload = new Judge0CallbackPayload();
        payload.setToken(token);
        payload.setTime(time);
        payload.setMemory(null);

        Judge0Status status = new Judge0Status();
        status.setId(statusId);
        payload.setStatus(status);
        return payload;
    }
}
