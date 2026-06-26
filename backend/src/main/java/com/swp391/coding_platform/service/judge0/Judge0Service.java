package com.swp391.coding_platform.service.judge0;

import com.swp391.coding_platform.dto.judge0.Judge0BatchRequest;
import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload;
import com.swp391.coding_platform.dto.judge0.Judge0SubmissionItem;
import com.swp391.coding_platform.dto.judge0.Judge0TokenResponse;
import com.swp391.coding_platform.dto.request.OjSubmissionRequest;
import com.swp391.coding_platform.dto.response.OjSubmissionInitialResponse;
import com.swp391.coding_platform.dto.response.OjWebSocketMessage;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.event.SubmissionJudgedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Judge0Service {
    ProblemSubmissionRepository problemSubmissionRepository;
    ProblemSubmissionDetailRepository problemSubmissionDetailRepository;
    ProblemTestcaseRepository problemTestcaseRepository;
    UserRepository userRepository;
    ProblemRepository problemRepository;
    LessonRepository lessonRepository;
    ContestRepository contestRepository;
    ContestProblemRepository contestProblemRepository;
    LessonProblemRepository lessonProblemRepository;

    Judge0ClientService judge0ClientService;
    SimpMessagingTemplate simpMessagingTemplate;

    StringRedisTemplate stringRedisTemplate;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${app.webhook-base-url}")
    String webhookBaseUrl;

    public OjSubmissionInitialResponse submitCode(OjSubmissionRequest request, Integer userId) {
        // Check user status
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.validateStatus();

        ProblemEntity ojProblem = problemRepository.findByIdAndIsPublicTrue(request.getProblemId())
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        if (request.getContestId() == null && request.getLessonId() == null) {
            if (ojProblem.getIsPublic() == null || !ojProblem.getIsPublic()) {
                throw new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND);
            }
        }

        // Xác thực contest: check tồn tại, status ONGOING, và user đã registered
        if (request.getContestId() != null) {
            var contest = contestRepository.findById(request.getContestId())
                    .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

            // Tính toán contest status dựa trên thời gian thực
            Instant now = Instant.now();
            boolean isOngoing = contest.getStatus() != com.swp391.coding_platform.entity.enums.ContestStatus.DRAFT
                    && contest.getStatus() != com.swp391.coding_platform.entity.enums.ContestStatus.DELETED
                    && !now.isBefore(contest.getStartTime())
                    && !now.isAfter(contest.getEndTime());

            if (!isOngoing) {
                // Contest chưa bắt đầu hoặc đã kết thúc -> không được submit
                throw new AppException(ErrorCode.CONTEST_SUBMISSION_NOT_ALLOWED);
            }

            // Check user đã đăng ký contest chưa
            boolean isRegistered = contestRepository.isUserRegistered(request.getContestId(), userId);
            if (!isRegistered) {
                throw new AppException(ErrorCode.CONTEST_NOT_JOINED);
            }

            // Check bài toán thuộc contest
            boolean belongsToContest = contestProblemRepository.existsByContestIdAndProblemId(
                    request.getContestId(), request.getProblemId());
            if (!belongsToContest) {
                throw new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND);
            }
        }

        if (request.getLessonId() != null) {
            boolean belongsToLesson = lessonProblemRepository.existsByLessonIdAndProblemId(request.getLessonId(),
                    request.getProblemId());
            if (!belongsToLesson) {
                throw new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND);
            }
        }

        // Kiểm tra bài toán và lấy danh sách Testcases từ Database
        List<ProblemTestcaseEntity> problemTestcaseEntityList = problemTestcaseRepository
                .findByProblemIdOrderByOrderIndex(request.getProblemId());
        if (problemTestcaseEntityList.isEmpty()) {
            throw new AppException(ErrorCode.TESTCASE_NOT_FOUND);
        }

        // Tạo bản ghi "Mẹ" (ProblemSubmissionEntity) với trạng thái mặc định là
        // PENDING
        ProblemSubmissionEntity onlineJudgeSubmissionEntity = ProblemSubmissionEntity.builder()
                .user(userRepository.getReferenceById(userId))
                .problem(problemRepository.getReferenceById(request.getProblemId()))
                .languageId(request.getLanguageId())
                .sourceCode(request.getSourceCode())
                .verdict(OjVerdict.PENDING)
                .lesson(request.getLessonId() != null ? lessonRepository.getReferenceById(request.getLessonId()) : null)
                .contest(request.getContestId() != null ? contestRepository.getReferenceById(request.getContestId())
                        : null)
                .build();

        // Đóng gói dữ liệu (Code + Testcases) để gửi sang Judge0
        List<Judge0SubmissionItem> judge0SubmissionItemList = new ArrayList<>();
        String callbackUrl = webhookBaseUrl + "/online-judge/submissions";

        // Tính limit dựa trên ngôn ngữ (Gợi ý dùng hệ số nhân)
        double timeLimitSeconds = calculateTimeLimitForLanguage(ojProblem.getTimeLimitMs(), request.getLanguageId());

        for (ProblemTestcaseEntity testcase : problemTestcaseEntityList) {
            // Chuẩn hóa lại chuỗi \n bị gõ nhầm thành ký tự literal trong DB
            String cleanStdin = testcase.getInputData() != null ? testcase.getInputData().replace("\\n", "\n") : "";
            String cleanExpected = testcase.getExpectedOutput() != null
                    ? testcase.getExpectedOutput().replace("\\n", "\n")
                    : "";

            Judge0SubmissionItem item = Judge0SubmissionItem.builder()
                    .languageId(request.getLanguageId())
                    .sourceCode(request.getSourceCode())
                    .stdin(cleanStdin)
                    .expectedOutput(cleanExpected)
                    .callbackUrl(callbackUrl)
                    .cpuTimeLimit(timeLimitSeconds)
                    .memoryLimit(ojProblem.getMemoryLimitKb())
                    .build();
            judge0SubmissionItemList.add(item);
        }

        Judge0BatchRequest judge0BatchRequest = Judge0BatchRequest.builder()
                .submissions(judge0SubmissionItemList)
                .build();

        // Gọi API Judge0 (Giai đoạn giao tiếp mạng)
        List<Judge0TokenResponse> tokenList = judge0ClientService.sendBatchSubmission(judge0BatchRequest);

        // Đảm bảo Judge0 trả về số lượng token khớp với số lượng testcase
        if (tokenList.isEmpty() || tokenList.size() != problemTestcaseEntityList.size()) {
            throw new AppException(ErrorCode.JUDGE0_SUBMISSION_FAILED);
        }

        // Lưu submission "mẹ" trước để có ID cho các chi tiết
        problemSubmissionRepository.save(onlineJudgeSubmissionEntity);

        // Tạo bản ghi "Con" (ProblemSubmissionDetailEntity) cho từng testcase
        List<ProblemSubmissionDetailEntity> submissionDetails = new ArrayList<>();
        for (int i = 0; i < tokenList.size(); i++) {
            ProblemSubmissionDetailEntity detailEntity = ProblemSubmissionDetailEntity.builder()
                    .submission(onlineJudgeSubmissionEntity)
                    .testcase(problemTestcaseEntityList.get(i))
                    .token(tokenList.get(i).getToken())
                    .verdict(OjVerdict.PENDING)
                    .build();
            submissionDetails.add(detailEntity);
        }
        problemSubmissionDetailRepository.saveAll(submissionDetails);

        // Tăng tổng số lượt nộp của Problem
        problemRepository.incrementTotalSubmission(request.getProblemId());

        // Trả về Response cho Frontend ngay lập tức
        return OjSubmissionInitialResponse.builder()
                .submissionId(onlineJudgeSubmissionEntity.getId())
                .status(OjVerdict.PENDING.toString())
                .message("Submission received and is being processed.")
                .build();

    }

    public void processJudge0Callback(Judge0CallbackPayload judge0CallbackPayload) {

        // Lấy thông tin SubmissionDetail (bao gồm luôn Submission cha và Problem nhờ
        // JOIN FETCH)
        ProblemSubmissionDetailEntity submissionDetail = problemSubmissionDetailRepository
                .findByTokenWithSubmissionAndProblem(judge0CallbackPayload.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.JUDGE0_SUBMISSION_FAILED));

        // Không cần truy vấn tìm Submission nữa vì đã JOIN FETCH ở trên
        ProblemSubmissionEntity submissionEntity = submissionDetail.getSubmission();

        // Chuyển đổi trạng thái từ Judge0 sang hệ thống của mình
        OjVerdict testcaseVerdict = mapJudge0StatusToOjVerdict(judge0CallbackPayload.getStatus().getId());

        // Cập nhật kết quả cho SubmissionDetail (Submission Con)
        submissionDetail.setVerdict(testcaseVerdict);
        submissionDetail.setExecutionTime(parseExecutionTime(judge0CallbackPayload.getTime()));
        submissionDetail.setMemoryUsed(judge0CallbackPayload.getMemory());

        // Judge0 Webhook luôn trả output dưới dạng Base64, nên ta cần giải mã nó
        submissionDetail.setStdout(decodeBase64Safe(judge0CallbackPayload.getStdout()));
        submissionDetail.setStderr(decodeBase64Safe(judge0CallbackPayload.getStderr()));
        submissionDetail.setCompileOutput(decodeBase64Safe(judge0CallbackPayload.getCompileOutput()));

        problemSubmissionDetailRepository.save(submissionDetail);

        // Kiểm tra xem ĐÃ CHẤM XONG HẾT CHƯA?
        Integer submissionId = submissionEntity.getId();
        long totalTestcases = problemSubmissionDetailRepository.countBySubmissionId(submissionId);
        boolean isContestMode = submissionEntity.getContest() != null;

        // ==========================================
        // 3. LOGIC REDIS ATOMIC COUNTER & SHORT-CIRCUIT
        // ==========================================
        String redisKey = "oj_progress:" + submissionId;
        String failedKey = "oj_failed:" + submissionId; // Cờ đánh dấu đã có testcase sai

        boolean isEarlyFinish = false;

        // SHORT-CIRCUIT (CONTEST MODE): Chốt sổ ngay lập tức nếu gặp testcase sai đầu
        // tiên!
        if (isContestMode && testcaseVerdict != OjVerdict.ACCEPTED) {
            // setIfAbsent đảm bảo chỉ có 1 luồng duy nhất (lỗi đầu tiên) được quyền chốt sổ
            Boolean isFirstFail = stringRedisTemplate.opsForValue().setIfAbsent(failedKey, "1", Duration.ofHours(1));
            if (Boolean.TRUE.equals(isFirstFail)) {
                isEarlyFinish = true;
            }
        }

        Long processedCount = stringRedisTemplate.opsForValue().increment(redisKey);

        // Đặt TTL 1 tiếng cho lần đếm đầu tiên đề phòng Judge0 chết giữa chừng
        if (processedCount != null && processedCount == 1L) {
            stringRedisTemplate.expire(redisKey, Duration.ofHours(1));
        }

        // Chấm xong bình thường: Đủ testcase VÀ chưa từng bị chốt sổ sớm
        // (short-circuit)
        boolean isNormalFinish = processedCount != null && processedCount == totalTestcases
                && Boolean.FALSE.equals(stringRedisTemplate.hasKey(failedKey));

        // Khởi tạo overallVerdict (mặc định là PENDING)
        OjVerdict overallVerdict = OjVerdict.PENDING;

        if (isEarlyFinish || isNormalFinish) {
            if (isEarlyFinish) {
                overallVerdict = testcaseVerdict; // Chốt ngay kết quả lỗi hiện tại
            } else {
                // Đã chấm xong tất cả Testcase bình thường -> Tìm lỗi đầu tiên (nếu có)
                overallVerdict = problemSubmissionDetailRepository
                        .findFirstBySubmissionIdAndVerdictNotOrderByTestcaseOrderIndexAsc(submissionId,
                                OjVerdict.ACCEPTED)
                        .map(ProblemSubmissionDetailEntity::getVerdict)
                        .orElse(OjVerdict.ACCEPTED);
            }

            // Lấy thời gian và bộ nhớ sử dụng tối đa (tính đến thời điểm hiện tại)
            var maxStats = problemSubmissionDetailRepository.findMaxStatsBySubmissionId(submissionId)
                    .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

            // Cập nhật trạng thái, thời gian, bộ nhớ tổng của Submission và lưu vào DB
            submissionEntity.setVerdict(overallVerdict);
            submissionEntity.setExecutionTime(maxStats.getMaxTime());
            submissionEntity.setMemoryUsed(maxStats.getMaxMemory());
            problemSubmissionRepository.save(submissionEntity);

            // Tăng tổng số lượt AC của Problem nếu kết quả là ACCEPTED
            if (overallVerdict == OjVerdict.ACCEPTED) {
                problemRepository.incrementTotalAccepted(submissionEntity.getProblem().getId());
            }

            // Bắn sự kiện SubmissionJudgedEvent qua Redis Pub/Sub
            SubmissionJudgedEvent event = SubmissionJudgedEvent.builder()
                    .submissionId(submissionEntity.getId())
                    .userId(submissionEntity.getUser().getId())
                    .contestId(submissionEntity.getContest() != null ? submissionEntity.getContest().getId() : null)
                    .problemId(submissionEntity.getProblem().getId())
                    .verdict(overallVerdict.name())
                    .submitTime(submissionEntity.getSubmittedAt())
                    .build();
            try {
                String eventJson = objectMapper.writeValueAsString(event);
                stringRedisTemplate.convertAndSend("contest:events", eventJson);
                log.info("Successfully published SubmissionJudgedEvent to Redis channel contest:events: {}", eventJson);
            } catch (Exception e) {
                log.error("Failed to publish SubmissionJudgedEvent to Redis Pub/Sub", e);
            }
        }

        // HIỆU SUẤT: Luôn dọn dẹp key Redis giải phóng RAM khi tất cả webhook đã về đủ
        if (processedCount != null && processedCount == totalTestcases) {
            stringRedisTemplate.delete(redisKey);
            stringRedisTemplate.delete(failedKey);
        }

        OjWebSocketMessage wsMessage = OjWebSocketMessage.builder()
                .submissionId(submissionId)
                .testcaseId(submissionDetail.getTestcase().getId())
                .testcaseVerdict(testcaseVerdict)
                .overallVerdict(overallVerdict)
                // Nếu đã chốt sổ (Early hoặc Normal), gửi MAX time/memory. Nếu chưa, gửi
                // time/memory hiện tại
                .executionTimeMs((isEarlyFinish || isNormalFinish) ? submissionEntity.getExecutionTime()
                        : submissionDetail.getExecutionTime())
                .memoryUsedKb((isEarlyFinish || isNormalFinish) ? submissionEntity.getMemoryUsed()
                        : submissionDetail.getMemoryUsed())
                .totalTestcases((int) totalTestcases)
                .processedTestcases(processedCount.intValue())
                .build();

        // Thêm dữ liệu output nếu không phải Contest (Thi đấu)
        if (!isContestMode) {
            wsMessage.setInput(submissionDetail.getTestcase().getInputData());
            wsMessage.setExpectedOutput(submissionDetail.getTestcase().getExpectedOutput());
            wsMessage.setCompileOutput(submissionDetail.getCompileOutput());

            // Ưu tiên hiển thị stderr vào actualOutput nếu stdout trống (giúp user dễ debug
            // Runtime Error)
            String actual = submissionDetail.getStdout();
            if ((actual == null || actual.trim().isEmpty()) && submissionDetail.getStderr() != null
                    && !submissionDetail.getStderr().trim().isEmpty()) {
                actual = submissionDetail.getStderr();
            }
            wsMessage.setActualOutput(actual);
        }

        // RẼ NHÁNH GỬI WEBSOCKET
        if (!isContestMode) {
            // CHẾ ĐỘ LUYỆN TẬP (PRACTICE): Chấm xong testcase nào, bắn ngay testcase đó để
            // chạy Progress Bar
            simpMessagingTemplate.convertAndSend("/topic/submissions/" + submissionEntity.getUser().getId(), wsMessage);
            simpMessagingTemplate.convertAndSend("/topic/submissions/admin", wsMessage);
            log.info("PRACTICE MODE: Bắn WebSocket tiến trình {}/{} cho Submission {}",
                    wsMessage.getProcessedTestcases(), wsMessage.getTotalTestcases(), submissionId);

        } else if (isEarlyFinish || isNormalFinish) {
            // CHẾ ĐỘ THI ĐẤU (CONTEST): ĐỌC , không bắn lẻ tẻ.
            // CHỈ BẮN 1 LẦN DUY NHẤT khi chốt sổ (Short-circuit sớm HOẶC chấm xong toàn bộ)
            wsMessage.setTestcaseId(null);
            wsMessage.setTestcaseVerdict(null);

            simpMessagingTemplate.convertAndSend("/topic/submissions/" + submissionEntity.getUser().getId(), wsMessage);
            // Gửi thêm lên kênh Admin toàn cục để monitor
            simpMessagingTemplate.convertAndSend("/topic/submissions/admin", wsMessage);
            log.info("CONTEST MODE: Đã chấm xong toàn bộ. Bắn WebSocket tổng kết (Verdict: {}) cho Submission {}",
                    overallVerdict, submissionId);
        } else {
            // Đang chấm dở dang trong Contest -> Im lặng
            log.info("CONTEST MODE: Đang chấm testcase lẻ (Submission {}). Bỏ qua bắn WebSocket để bảo mật.",
                    submissionId);
        }
    }

    // --- Hàm bổ trợ ---

    private OjVerdict mapJudge0StatusToOjVerdict(Integer judge0StatusId) {
        return switch (judge0StatusId) {
            case 3 -> OjVerdict.ACCEPTED;
            case 4 -> OjVerdict.WRONG_ANSWER;
            case 5 -> OjVerdict.TIME_LIMIT_EXCEEDED;
            case 6 -> OjVerdict.COMPILATION_ERROR;
            // Map thêm các trạng thái khác (Runtime Error, Memory Limit...)
            default -> OjVerdict.RUNTIME_ERROR;
        };
    }

    private Integer parseExecutionTime(String timeStr) {
        // Judge0 trả về time dạng string ví dụ "0.045" (giây)
        if (timeStr == null)
            return 0;
        try {
            return (int) (Double.parseDouble(timeStr) * 1000); // Chuyển sang mili-giây
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double calculateTimeLimitForLanguage(Integer baseTimeMs, Integer languageId) {
        if (baseTimeMs == null) {
            return 2.0; // Default fallback to 2 seconds if null
        }
        double baseSec = baseTimeMs / 1000.0;
        return switch (languageId) {
            // C, C++ & Golang (compiled languages, extremely fast execution)
            case 48, 49, 50, 75, 52, 53, 54, 76, 60 -> baseSec;

            // Java & C# (VM/CLR-based, needs JVM/Mono startup overhead buffer)
            case 62, 51 -> baseSec * 2.0 + 1.0;

            // JavaScript & TypeScript (Node.js JIT startup overhead)
            case 63, 74 -> baseSec * 2.0;

            // Python (interpreted, slower execution speed)
            case 70, 71 -> baseSec * 3.0;

            // Default fallback to base time
            default -> baseSec;
        };
    }

    private String decodeBase64Safe(String base64Str) {
        if (base64Str == null || base64Str.trim().isEmpty())
            return base64Str;
        try {
            // Loại bỏ khoảng trắng/xuống dòng thừa vì Judge0 có thể có \n trong chuỗi
            // Base64
            String cleanBase64 = base64Str.replaceAll("\\s+", "");
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(cleanBase64);
            return new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Lỗi giải mã Base64 từ Judge0: {}", e.getMessage());
            // Trả về chuỗi gốc nếu không phải Base64 hợp lệ
            return base64Str;
        }
    }

}
